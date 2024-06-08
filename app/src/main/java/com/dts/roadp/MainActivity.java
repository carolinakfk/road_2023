package com.dts.roadp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class MainActivity extends PBase {

    private EditText txtUser, txtPass;
    private TextView lblRuta, lblRTit, lblLogin, lblVer, lblID;
    private ImageView imgLogo;

    private BaseDatosVersion dbVers;

    private boolean rutapos, scanning = false;
    private String cs1, cs2, cs3, barcode;

    private String parNumVer = "9.9.75 / ";
    private String parFechaVer = "29-05-2024";
    private String parTipoVer = "ROAD PRD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            grantPermissions();
        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    // Grant permissions

    private void grantPermissions() {

        try {
            if (Build.VERSION.SDK_INT >= 20) {

                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                        && checkCallingOrSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.CALL_PHONE,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WAKE_LOCK,
                                    Manifest.permission.READ_PHONE_STATE
                            }, 1);
                }
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void startApplication() {

        File ffile;

        try {

            super.InitBase();

            this.setTitle("ROAD");
            gl.parNumVer = parNumVer;
            gl.parFechaVer = parFechaVer;
            gl.parTipoVer = parTipoVer;

            txtUser = findViewById(R.id.txtUser);
            txtPass = findViewById(R.id.txtMonto);
            lblRuta = findViewById(R.id.lblCDisp);
            lblRTit = findViewById(R.id.lblCUsed);
            lblLogin = findViewById(R.id.lblDir);
            lblVer = findViewById(R.id.textView10);
            lblID = findViewById(R.id.textView81);
            imgLogo = findViewById(R.id.imgNext);

            lblVer.setText(gl.parTipoVer + " Version " + gl.parNumVer + gl.parFechaVer);

            try {

                File file1 = new File(Environment.getExternalStorageDirectory(), "/debug.txt");
                ffile = new File(file1.getPath());
                if (ffile.exists()) {
                    gl.debug=true;
                }else {
                    gl.debug=false;
                }

            } catch (Exception e) {
                gl.debug=false;
            }

            // DB VERSION
            dbVers = new BaseDatosVersion(this, db, Con);
            dbVers.checkVersion(1);

            setHandlers();

            txtUser.requestFocus();

            gl.tolsuper=false;

            initSession();

            if (!validaLicencia()) {
                startActivity(new Intent(this, comWSLic.class));
                return;
            } else {
                supervisorRuta();
            }

            //#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
            if (gl.debug) {
                txtUser.setText("00110698");txtPass.setText("inicio01"); // Administrador
                //txtUser.setText("00101051");txtPass.setText("na1051");//1002-3
                //txtUser.setText("00100858");txtPass.setText("lopez026");// 0050-1
                //txtUser.setText("00100938");txtPass.setText("MZ"); // 8001-1
                //txtUser.setText("00100825");txtPass.setText("cesar"); // 0002-1
                //txtUser.setText("00100993");txtPass.setText("2613");
                //txtUser.setText("00108457");txtPass.setText("108457");
                //txtUser.setText("00109776");txtPass.setText("109776");
                //txtUser.setText("1");txtPass.setText("1");  // P001-1
            }

            //#AT20221117 Set path
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gl.PathDataDir = this.getApplicationContext().getDataDir().getPath();
            }

            //#AT20221117 Se valida si existe el archivo
            String fname = gl.PathDataDir+"/"+gl.archivo_p12;
            File archivo= new File(fname);

            //#AT20221117 Si no existe, procede a realizar la copia
            if (!archivo.exists()) {
                CopiarArchivo(gl.archivo_p12);
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    private void CopiarArchivo(String filename) {
        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(gl.PathDataDir, filename);
            out = new FileOutputStream(outFile);

            GeneraArchivo(in, out);

            Toast.makeText(this, "Certificado guardado!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo copiar el certificado!", Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void GeneraArchivo(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            Toast.makeText(this, e+"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                startApplication();
            } else {
                super.finish();
            }
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    //region Events

    public void comMan(View view) {
        try {
            entraComunicacion();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    public void gotoMenu() {
        try {
            txtUser.setText("");
            txtPass.setText("");
            txtUser.requestFocus();

            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    public void doLogin(View view) {

        if (!validaLicencia()) {
            startActivity(new Intent(this, comWSLic.class));
            return;
        }

        try {
            processLogIn();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    public void doRegister(View view) {
        try {
            startActivity(new Intent(this, comWSLic.class));
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void setHandlers() {

        try {
            txtUser.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                //toast("key ");
                                txtPass.requestFocus();
                                return true;
                        }
                    }
                    return false;
                }
            });

            txtPass.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                processLogIn();
                                return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void compareSC(CharSequence s) {
        try {
            String os, bc;

            bc = txtUser.getText().toString();
            if (mu.emptystr(bc) || bc.length() < 2) {
                txtUser.setText("");
                scanning = false;
                return;
            }
            os = s.toString();

            if (bc.equalsIgnoreCase(os)) {
                //Toast.makeText(this,"Codigo barra : " +bc, Toast.LENGTH_SHORT).show();
                msgbox("Barra: " + bc);
            }

            txtUser.setText("");
            scanning = false;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    //endregion

    //region Main

    private void initSession() {
        Cursor DT;
        String s, rn = "";
        String vCellCom = "";

        if (dbVacia()) {

            if (gl==null){
                super.InitBase();
            }

            gl.modoadmin = true;
            gl.autocom = 0;
            toastcent("¡La base de datos está vacia!");
            startActivity(new Intent(MainActivity.this, ComWS.class));
        }

        try {
            //#HS_20181122_1505 Se agrego el campo Impresion.
            sql = "SELECT CODIGO,NOMBRE,VENDEDOR,VENTA,WLFOLD,IMPRESION,SUCURSAL,CELULAR," +
                  "PERMITIR_PRODUCTO_NUEVO, PERMITIR_CANTIDAD_MAYOR, VALIDAR_POSICION_GEOREFERENCIAL FROM P_RUTA";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {

                DT.moveToFirst();

                gl.ruta = DT.getString(0);
                gl.rutanom = DT.getString(1);
                gl.vend = DT.getString(2);
                gl.rutatipog = DT.getString(3);
                s = DT.getString(3);
                gl.wsURL = DT.getString(4);
                gl.impresora = DT.getString(5);
                gl.sucur = DT.getString(6);

                if (!mu.emptystr(DT.getString(7))) {
                    vCellCom = DT.getString(7);
                }
                gl.CellCom = (vCellCom.equalsIgnoreCase("S"));

                rutapos = s.equalsIgnoreCase("R");

                gl.permitir_cantidad_mayor=(DT.getInt(8)==1?true:false);
                gl.permitir_producto_nuevo=(DT.getInt(9)==1?true:false);
                gl.validar_posicion_georef = (DT.getInt(10)==1?true:false);


            } else {

                gl.ruta = "";
                gl.rutanom = "";
                gl.vend = "0";
                gl.rutatipog = "V";
                gl.wsURL = "http://192.168.1.1/wsAndr/wsAndr.asmx";
                gl.permitir_cantidad_mayor=false;
                gl.permitir_producto_nuevo=false;

            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

        if (rutapos) {
            lblRTit.setText("POS No. " + gl.ruta);
            imgLogo.setImageResource(R.drawable.retail_logo);
        } else {
            lblRTit.setText("Ruta No. " + gl.ruta);
        }

        try {
            //#HS_20181120_1616 Se agrego el campo UNIDAD_MEDIDA_PESO.//campo INCIDENCIA_NO_LECTURA
            sql = " SELECT EMPRESA,NOMBRE,DEVOLUCION_MERCANCIA,USARPESO,FIN_DIA,DEPOSITO_PARCIAL,UNIDAD_MEDIDA_PESO," +
                  " INCIDENCIA_NO_LECTURA, LOTE_POR_DEFECTO, URL_TOKEN, USUARIO_API, CLAVE_API, URL_EMISION_NC_B2C," +
                  " URL_EMISION_ND_B2C,URL_EMISION_FACTURA_B2C,QR_API,URL_BASE, ARCHIVO_P12,URL_B2C_HH,QR_CLAVE, URL_DOC, " +
                  " URL_EMISION_NC_B2B_HH, URL_EMISION_ND_B2B_HH, UNIDAD_MEDIDA_DEFECTO, AMBIENTE, URL_CONSULTAR_DOCUMENTO_POR_RUTA " +
                  " FROM P_EMPRESA ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                gl.emp = DT.getString(0);
                lblRuta.setText(DT.getString(1));
                gl.empnom = DT.getString(1);
                gl.devol = DT.getInt(2) == 1;
                s = DT.getString(3);
                gl.usarpeso = s.equalsIgnoreCase("S");
                gl.banderafindia = DT.getInt(4) == 1;
                gl.umpeso = DT.getString(6);
                gl.incNoLectura = DT.getInt(7) == 1; //#HS_20181211 Agregue campo incNoLectura para validacion en cliente.
                gl.depparc = DT.getInt(5) == 1;
                gl.lotedf = DT.getString(8);

                //#CKFK20230118 Llené las variables globales para la certificación
                gl.url_token = DT.getString(9);
                gl.usuario_api = DT.getString(10);
                gl.clave_api = DT.getString(11);
                gl.url_emision_nc_b2c = DT.getString(12);
                gl.url_emision_nd_b2c = DT.getString(13);
                gl.url_emision_factura_b2c = DT.getString(14);
                gl.qr_api = DT.getString(15);
                gl.url_base = DT.getString(16);
                gl.archivo_p12 = DT.getString(17);
                gl.url_b2c_hh = DT.getString(18);
                gl.qr_clave = DT.getString(19);
                gl.url_doc = DT.getString(20);
                gl.url_emision_nc_b2b_hh = DT.getString(21);
                gl.url_emision_nd_b2b_hh =DT.getString(22);
                gl.unidad_medida_defecto =DT.getString(23);
                gl.ambiente =DT.getString(24);
                gl.url_consultar_documento_por_ruta =DT.getString(25);
            } else {
                gl.emp = "";lblRuta.setText("");
                gl.devol = false;
                msgbox("¡No se pudo cargar configuración de la empresa!");
            }

            if(DT!=null) DT.close();

        } catch (Exception e) {
            msgbox("¡No se pudo cargar configuración de la empresa!");
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

        gl.vendnom = "Vendedor 1";

        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/SyncFold");
            directory.mkdirs();
        } catch (Exception e) {}

        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/RoadFotos");
            directory.mkdirs();
        } catch (Exception e) {}

        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/clinue");
            directory.mkdirs();
        } catch (Exception e) {}

        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/clidocs");
            directory.mkdirs();
        } catch (Exception e) {}

        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/RoadPedidos");
            directory.mkdirs();
        } catch (Exception e) {}

        //Id de Dispositivo
        gl.deviceId = androidid();
        gl.devicename = Build.MANUFACTURER + " " + Build.MODEL;;//getLocalBluetoothName();
        lblID.setText(gl.devicename);

        try {
            AppMethods app = new AppMethods(this, gl, Con, db);
            app.parametrosExtra();
            app.parametrosGlobales();
            app.parametrosBarras();

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(e.getMessage());
        }

    }

    private void processLogIn() {
        lblLogin.setVisibility(View.INVISIBLE);

        if (!validaLicencia()) {
            lblLogin.setVisibility(View.VISIBLE);
            mu.msgbox("¡Licencia invalida!");
            startActivity(new Intent(this,comWSLic.class));
            return;
        }

        if (checkUser()) gotoMenu();
        else lblLogin.setVisibility(View.VISIBLE);
    }

    private boolean checkUser() {
        Cursor DT;
        String usr, pwd, dpwd;

        try {

            usr = txtUser.getText().toString().trim();
            pwd = txtPass.getText().toString().trim();

            if (mu.emptystr(usr)) {
                mu.msgbox("Usuario incorrecto.");
                return false;
            }
            if (mu.emptystr(pwd)) {
                mu.msgbox("Contraseña incorrecta.");
                return false;
            }

            if (usr.equalsIgnoreCase("DTS") && pwd.equalsIgnoreCase("DTS")) {

                gl.vendnom = "DTS";
                gl.vend = "DTS";
                gl.vnivel = 1;
                gl.vnivprec = 1;

                return true;
            }

            if (usr.equalsIgnoreCase("Venta") && pwd.equalsIgnoreCase("Venta")) {
                showDemoMenu();
                return false;
            }

            sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + usr + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() == 0) {
                mu.msgbox("Usuario incorrecto !");
                return false;
            }

            DT.moveToFirst();
            dpwd = DT.getString(1);

            if (!pwd.equalsIgnoreCase(dpwd)) {
                mu.msgbox("Contraseña incorrecta !");
                return false;
            }

            gl.vendnom = DT.getString(0);
            gl.vend = usr;
            gl.vnivel = DT.getInt(2);
            gl.vnivprec = DT.getInt(3);

            gl.tolsuper=false;
            if (gl.peModal.equalsIgnoreCase("TOL")) {
                if (gl.vnivel==2){
                    gl.tolsuper=true;
                }
            }

            return true;

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return false;
        }

    }

    public void supervisorRuta() {
        Cursor DT;

        try {

            sql = "SELECT CODIGO FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 2";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            if (DT.getCount() > 0) {
                gl.codSupervisor = DT.getString(0);
            }else{
                gl.codSupervisor = "";
            }

            if(DT!=null) DT.close();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            Log.d("supervisorRuta error: ", e.getMessage());
        }

    }

    //endregion

    //region Ventas Demo

    public void showDemoMenu() {

        try {
            final AlertDialog Dialog;
            final String[] selitems = {"Datos de cliente", "Base de datos original", "Borrar datos de venta"};

            AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
            menudlg.setTitle("Datos de demo");

            menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    switch (item) {
                        case 0:
                            Intent intent = new Intent(MainActivity.this, DemoData.class);
                            startActivity(intent);
                            break;
                        case 1:
                            copyRawFile();
                            break;
                        case 2:
                            borrarDatos(1);
                            break;
                    }

                    dialog.cancel();
                }
            });

            menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            Dialog = menudlg.create();
            Dialog.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void copyRawFile() {
        String fn;
        int rid, rslt;
        try {
            Field[] fields = R.raw.class.getFields();
            for (Field f : fields)
                try {
                    fn = f.getName();
                    if (fn.equalsIgnoreCase("rd_param")) {
                        rid = f.getInt(null);
                        rslt = copyRawFile(rid);
                        if (rslt == 1) {
                            Intent intent = new Intent(this, ComDrop.class);
                            startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                }
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private int copyRawFile(int rawid) {

        try {
            InputStream in = getResources().openRawResource(rawid);
            File file = new File(Environment.getExternalStorageDirectory(), "/SyncFold/rd_param.txt");
            FileOutputStream out = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int read = 0;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
            return 1;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            mu.msgbox("Error : " + e.getMessage());
            return 0;
        }
    }

    private void borrarDatos(int showmsg) {

        try {

            db.beginTransaction();

            sql = "DELETE FROM D_FACTURA";
            db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAD";
            db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAP";
            db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAD_LOTES";
            db.execSQL(sql);
            sql="DELETE FROM D_FACTURA_BARRA";
            db.execSQL(sql);
            sql="DELETE FROM D_FACTURA_STOCK";
            db.execSQL(sql);
            sql="DELETE FROM D_FACTURAF";
            db.execSQL(sql);
            sql="DELETE FROM D_FACTURAD_MODIF";
            db.execSQL(sql);

            sql = "DELETE FROM D_PEDIDO";
            db.execSQL(sql);
            sql = "DELETE FROM D_PEDIDOD";
            db.execSQL(sql);

            sql = "DELETE FROM D_BONIF";
            db.execSQL(sql);
            sql = "DELETE FROM D_BONIF_LOTES";
            db.execSQL(sql);
            sql = "DELETE FROM D_REL_PROD_BON";
            db.execSQL(sql);
            sql = "DELETE FROM D_BONIFFALT";
            db.execSQL(sql);

            sql = "DELETE FROM D_DEPOS";
            db.execSQL(sql);
            sql = "DELETE FROM D_DEPOSD";
            db.execSQL(sql);

            sql = "DELETE FROM D_MOV";
            db.execSQL(sql);
            sql = "DELETE FROM D_MOVD";
            db.execSQL(sql);

            sql = "DELETE FROM D_ATENCION";
            db.execSQL(sql);

            sql = "DELETE FROM D_CANASTAS";
            db.execSQL(sql);

            sql = "DELETE FROM D_CLIENTE_MODIF";
            db.execSQL(sql);

            sql = "DELETE FROM D_CLINUEVOT";
            db.execSQL(sql);

            sql = "DELETE FROM D_RATING";
            db.execSQL(sql);

            sql = "DELETE FROM D_DESPACHOD_NO_ENTREGADO";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

            if (showmsg == 1)
                Toast.makeText(this, "Datos de venta borrados", Toast.LENGTH_SHORT).show();

        } catch (SQLException e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            mu.msgbox("Error : " + e.getMessage());
        }

    }

    //endregion

    //region Aux

    private void entraComunicacion() {

        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Contraseña de administrador");//	alert.setMessage("Serial");

            final EditText input = new EditText(this);
            alert.setView(input);

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setText("");
            input.requestFocus();

            alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String s;

                    try {
                        s = input.getText().toString();

                        if (s.equalsIgnoreCase("1965")) {
                            gl.modoadmin = true;
                            gl.autocom = 0;
                            startActivity(new Intent(MainActivity.this, ComWS.class));
                        } else {
                            mu.msgbox("Contraseña incorrecta");
                            return;
                        }

                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    }
                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private boolean dbVacia() {
        Cursor dt;

        try {
            sql = "SELECT CODIGO FROM P_RUTA";
            dt = Con.OpenDT(sql);

            return dt.getCount() == 0;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            return true;
        }
    }

    private boolean validaLicencia() {

        CryptUtil cu = new CryptUtil();
        Cursor dt;
        String lic, lickey, licruta, rutaencrypt;
        Integer msgLic = 0;

        if (gl.debug) return true;

        try {

            lickey = cu.encrypt(gl.deviceId);
            rutaencrypt = cu.encrypt(gl.ruta);

            sql = "SELECT lic, licparam FROM Params";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();
            lic = dt.getString(0);
            licruta = dt.getString(1);

            if (!gl.debug) {

                if (mu.emptystr(lic)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld");
                    return false;
                }

                if (mu.emptystr(licruta)) {
                    toastlong("El dispositivo no tiene licencia válida de ruta");
                    return false;
                }

                if (lic.equalsIgnoreCase(lickey) && licruta.equalsIgnoreCase(rutaencrypt)) {
                    return true;
                }

                if (!lic.equalsIgnoreCase(lickey) && !licruta.equalsIgnoreCase(rutaencrypt)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld, ni de ruta");
                    return false;
                }

                if (!lic.equalsIgnoreCase(lickey)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld");
                    return false;
                }

                if (!licruta.equalsIgnoreCase(rutaencrypt)) {
                    toastlong("El dispositivo no tiene licencia válida de ruta");
                    return false;
                }

            } else {
                return true;
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " : " + e.getMessage());
        }

        return false;

    }

    private void msgAskLic(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);
        dialog.setIcon(R.drawable.ic_quest);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private String androidid() {
        String uniqueID = "";
        try {

            TelephonyManager tm = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
            uniqueID = tm.getDeviceId();

            if (uniqueID==null){
                uniqueID = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            }

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            uniqueID = "0000000000";
        }

        return uniqueID;
    }

    public String getLocalBluetoothName() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            return "";
        } else {
            return mBluetoothAdapter.getName();
        }
    }

    //endregion

    //region Activity Events

    protected void onResume() {
        try {
            super.onResume();
            initSession();
            txtUser.requestFocus();
            lblLogin.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    //endregion

}



