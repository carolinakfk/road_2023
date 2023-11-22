package com.dts.roadp;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import uk.co.senab.photoview.PhotoViewAttacher;

public class CliNuevoT extends PBase {
    private TextView lbGPS;
    private EditText txtCliNombre, txtCliNit, txtCliDireccion,
            txtProvincia, txtDistrito, txtCiudad, txtCliTelefono,
            txtCliEmail, txtCliContacto, txtCliCanal, txtCliSubCanal,
            txtCodVendedor, txtPollo, txtEmbutidos, txtHuevos, txtRes,
            txtCerdo, txtCongelados, txtSalsas;

    private int d1, d2, d3, d4, d5, d6, d7, nivel=0, tipo=0, codimg, correlativo = 0;
    private Boolean imgPath, imgDB;
    private PhotoViewAttacher zoomFoto;

    private Spinner spinnerPrecio;
    private ImageView imgGuardar, imgBuscarPro, imgBuscarCanal, imgRoadTit, imgVendedor;
    private CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7;
    private String imagenbase64,path;

    private ArrayList<String> spincode = new ArrayList<String>();
    private ArrayList<String> spinlist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_nuevo_t);

        super.InitBase();
        lbGPS = (TextView) findViewById(R.id.lbCoor);

        txtCliNombre = (EditText) findViewById(R.id.txtCliNombre);
        txtCliNit = (EditText) findViewById(R.id.txtCliNit);
        txtCliDireccion = (EditText) findViewById(R.id.txtCliDireccion);
        txtProvincia = (EditText) findViewById(R.id.txtProvincia);
        txtDistrito = (EditText) findViewById(R.id.txtDistrito);
        txtCiudad = (EditText) findViewById(R.id.txtCiudad);
        txtCliTelefono = (EditText) findViewById(R.id.txtCliTelefono);
        txtCliEmail = (EditText) findViewById(R.id.txtCliEmail);
        txtCliContacto = (EditText) findViewById(R.id.txtCliContacto);
        txtCliCanal = (EditText) findViewById(R.id.txtCliCanal);
        txtCliSubCanal = (EditText) findViewById(R.id.txtCliSubCanal);
        txtCodVendedor = (EditText) findViewById(R.id.txtCodVendedor);
        txtPollo = (EditText) findViewById(R.id.txtPollo);
        txtEmbutidos = (EditText) findViewById(R.id.txtEmbutidos);
        txtHuevos = (EditText) findViewById(R.id.txtHuevos);
        txtRes = (EditText) findViewById(R.id.txtRes);
        txtCerdo = (EditText) findViewById(R.id.txtCerdo);
        txtCongelados = (EditText) findViewById(R.id.txtCongelados);
        txtSalsas = (EditText) findViewById(R.id.txtSalsas);

        imgGuardar = (ImageView) findViewById(R.id.imgGuardar);
        imgBuscarCanal = (ImageView) findViewById(R.id.imgBuscarCanal);
        imgBuscarPro = (ImageView) findViewById(R.id.imgBuscarPro);
        imgRoadTit = (ImageView) findViewById(R.id.imgRoadTit);
        imgVendedor = (ImageView) findViewById(R.id.imgBuscarVen);

        spinnerPrecio = (Spinner) findViewById(R.id.spinnerPrecio);

        cb1 = (CheckBox) findViewById(R.id.checkBox1);
        cb2 = (CheckBox) findViewById(R.id.checkBox2);
        cb3 = (CheckBox) findViewById(R.id.checkBox3);
        cb4 = (CheckBox) findViewById(R.id.checkBox4);
        cb5 = (CheckBox) findViewById(R.id.checkBox5);
        cb6 = (CheckBox) findViewById(R.id.checkBox6);
        cb7 = (CheckBox) findViewById(R.id.checkBox7);

        setData();

        setDataSpinnerPrecio();
        setHandlers();
        miniFachada();
    }

    private void setCorel() {
        gl.corelCliente = mu.getCorelBase();
    }

    private void GuardarCliente() {
        Cursor DT;
        int codimagen = 0;
        String cliente = "";

        try {
            //Guardando Info en D_CLINUEVOT
            ins.init("D_CLINUEVOT");

            ins.add("CODIGO", gl.corelCliente);
            ins.add("RUTA", gl.ruta);
            ins.add("FECHA", fecha);
            ins.add("NOMBRE", txtCliNombre.getText().toString());

            if (gl.peModal.equalsIgnoreCase("TOL")) {
                ins.add("NEGOCIO", gl.cuentaCliNuevo);
            } else {
                ins.add("NEGOCIO", "");
            }

            ins.add("DIRECCION", txtCliDireccion.getText().toString());
            ins.add("TELEFONO", txtCliTelefono.getText().toString());
            ins.add("NIT", txtCliNit.getText().toString());
            ins.add("TIPONEG", "");
            ins.add("NIVPRECIO", nivel);
            ins.add("DIA1", valToStr(d1));
            ins.add("DIA2", valToStr(d2));
            ins.add("DIA3", valToStr(d3));
            ins.add("DIA4", valToStr(d4));
            ins.add("DIA5", valToStr(d5));
            ins.add("DIA6", valToStr(d6));
            ins.add("DIA7", valToStr(d7));
            ins.add("ORDVIS", 0);
            ins.add("BAND1", "");
            ins.add("BAND2", "");
            ins.add("STATCOM", "N");
            ins.add("IMAGEN", "");
            ins.add("CODIGO_ERP", "");
            ins.add("COORX", gl.gpspx);
            ins.add("COORY", gl.gpspy);
            ins.add("DEPARTAMENTO", gl.IdDep);
            ins.add("MUNICIPIO", gl.IdMun);
            ins.add("CIUDAD", txtCiudad.getText().toString());
            ins.add("EMAIL", txtCliEmail.getText().toString());
            ins.add("CONTACTO", txtCliContacto.getText().toString());
            ins.add("CANAL", gl.IdCanal);
            ins.add("SUBCANAL", gl.IdSubcanal);
            ins.add("VENDEDOR", txtCodVendedor.getText().toString());
            ins.add("CSPOLLO", txtPollo.getText().toString());
            ins.add("CSEMBUTIDO", txtEmbutidos.getText().toString());
            ins.add("CSHUEVO", txtHuevos.getText().toString());
            ins.add("CSRES", txtRes.getText().toString());
            ins.add("CSCERDO", txtCerdo.getText().toString());
            ins.add("CSCONGELADOS", txtCongelados.getText().toString());
            ins.add("CSSALSAS", txtSalsas.getText().toString());

            db.execSQL(ins.sql());

            //Guardando Info en P_Cliente
            ins.init("P_CLIENTE");

            ins.add("CODIGO", gl.corelCliente);
            ins.add("NOMBRE", txtCliNombre.getText().toString());
            ins.add("BLOQUEADO", "N");
            ins.add("TIPONEG", "");
            ins.add("TIPO", "NUEVO");
            if (gl.peModal.equalsIgnoreCase("TOL")) {
                ins.add("SUBTIPO", gl.cuentaCliNuevo);
            } else {
                ins.add("SUBTIPO", "PRE");
            }
            ins.add("CANAL", gl.IdCanal);
            ins.add("SUBCANAL", gl.IdSubcanal);
            ins.add("NIVELPRECIO", nivel);
            ins.add("MEDIAPAGO", "1");
            ins.add("LIMITECREDITO", 0);
            ins.add("DIACREDITO", 0);
            ins.add("DESCUENTO", "N");
            ins.add("BONIFICACION", "N");
            ins.add("ULTVISITA", fecha);
            ins.add("IMPSPEC", 0);
            ins.add("INVTIPO", "N");
            ins.add("INVEQUIPO", "N");
            ins.add("INV1", "N");
            ins.add("INV2", "N");
            ins.add("INV3", "N");
            ins.add("NIT", txtCliNit.getText().toString() + "");
            ins.add("MENSAJE", "N");
            ins.add("MUNICIPIO", gl.IdMun);
            ins.add("EMAIL", txtCliEmail.getText().toString());
            ins.add("TELEFONO", txtCliTelefono.getText().toString() + "");
            ins.add("DIRTIPO", "N");
            ins.add("DIRECCION", txtCliDireccion.getText().toString() + "");
            ins.add("SUCURSAL", "1");
            ins.add("COORX", gl.gpspx);
            ins.add("COORY", gl.gpspy);
            ins.add("FIRMADIG", "N");
            ins.add("CODBARRA", "");
            ins.add("VALIDACREDITO", "N");
            ins.add("PRECIO_ESTRATEGICO", "N");
            ins.add("NOMBRE_PROPIETARIO", "");
            ins.add("NOMBRE_REPRESENTANTE", "");
            ins.add("BODEGA", "");
            ins.add("COD_PAIS", "");
            ins.add("FACT_VS_FACT", "0");
            ins.add("CHEQUEPOST", "N");
            ins.add("DESCUENTO", "N");
            ins.add("BONIFICACION", "N");
            ins.add("PERCEPCION", 0);
            ins.add("TIPO_CONTRIBUYENTE", "");
            ins.add("ID_DESPACHO", 0);
            ins.add("ID_FACTURACION", 0);
            ins.add("MODIF_PRECIO", 0);
            ins.add("PRIORIZACION", "");
            ins.add("CONTACTO", txtCliContacto.getText().toString());

            db.execSQL(ins.sql());

            //Guardando Info CLIRUTA
            int dv;
            for (int i = 0; i < 8; i++) {

                dv = 0;
                switch (i) {
                    case 1:
                        dv = d1;
                        break;
                    case 2:
                        dv = d2;
                        break;
                    case 3:
                        dv = d3;
                        break;
                    case 4:
                        dv = d4;
                        break;
                    case 5:
                        dv = d5;
                        break;
                    case 6:
                        dv = d6;
                        break;
                    case 7:
                        dv = d7;
                        break;
                }

                if (dv == 1) {
                    ins.init("P_CLIRUTA");

                    ins.add("RUTA", gl.ruta);
                    ins.add("CLIENTE", gl.corelCliente);
                    ins.add("SEMANA", 0);
                    ins.add("DIA", i);
                    ins.add("SECUENCIA", -1);
                    ins.add("BANDERA", -1);

                    db.execSQL(ins.sql());
                }

            }

            //#AT20220425 Guardar documentos en D_CLINUEVOT_IMAGEN
            sql = "SELECT * FROM TMP_D_CLINUEVOT_IMAGEN WHERE CODIGO = '"+gl.corelCliente+"'";
            DT = Con.OpenDT(sql);
            if (DT == null || DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                codimagen = DT.getInt(0);
                cliente = DT.getString(1);

                ins.init("D_CLINUEVOT_IMAGEN");

                ins.add("CODIMAGEN", codimagen);
                ins.add("CODIGO", cliente);
                ins.add("RUTA", gl.ruta);
                ins.add("OBSERVACIONES", "");
                ins.add("FECHA", du.getFechaActual());
                ins.add("STATCOM", "N");

                db.execSQL(ins.sql());

                DT.moveToNext();
            }

            if (DT != null) DT.close();

            limpiar();
            Toast.makeText(this, "Cliente nuevo creado", Toast.LENGTH_SHORT).show();
            super.finish();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            mu.msgbox(e.getMessage());
        }
    }

    public void doSave(View view) {
        try {
            if (!checkValues()) return;

            msgAskSave("Crear cliente nuevo");
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    //Validaciones
    private String valToStr(int val) {
        if (val == 1) return "S";
        else return "N";
    }

    private boolean checkValues() {
        final Calendar c = Calendar.getInstance();
        int sc, dw;
        String s;

        try {
            s=txtCliNombre.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta nombre del cliente");
                txtCliNombre.requestFocus();
                return false;
            }

            s=txtCliNit.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta RUC del cliente");
                txtCliNit.requestFocus();
                return false;
            }

            s=txtCliDireccion.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta dirección del cliente");
                txtCliDireccion.requestFocus();
                return false;
            }

            s=txtCliTelefono.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta teléfono del cliente");
                txtCliTelefono.requestFocus();
                return false;
            }

            if (!telefonoValidator(txtCliTelefono)){
                mu.msgbox("El formato del teléfono no es válido");
                txtCliTelefono.requestFocus();
                return false;
            }

            s=txtCliEmail.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta correo del cliente");
                txtCliEmail.requestFocus();
                return false;
            }

            if (!emailValidator(txtCliEmail)){
                mu.msgbox("El formato del correo electrónico no es válido");
                txtCliEmail.requestFocus();
                return false;
            }

            s=txtCliContacto.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta contacto del cliente");
                txtCliContacto.requestFocus();
                return false;
            }

            s=txtCodVendedor.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta código del vendedor");
                txtCodVendedor.requestFocus();
                return false;
            }

            dw = c.get(Calendar.DAY_OF_WEEK);
            if (dw == 0) dw = 7;
            else dw -= 1;

            d1 = 0;
            d2 = 0;
            d3 = 0;
            d4 = 0;
            d5 = 0;
            d6 = 0;
            d7 = 0;

            if (cb1.isChecked()) d1 = 1;
            if (cb2.isChecked()) d2 = 1;
            if (cb3.isChecked()) d3 = 1;
            if (cb4.isChecked()) d4 = 1;
            if (cb5.isChecked()) d5 = 1;
            if (cb6.isChecked()) d6 = 1;
            if (cb7.isChecked()) d7 = 1;

            sc = d1 + d2 + d3 + d4 + d5 + d6 + d7;

            if (sc == 0) {

                switch (dw) {
                    case 1:
                        d1 = 1;
                        break;
                    case 2:
                        d2 = 1;
                        break;
                    case 3:
                        d3 = 1;
                        break;
                    case 4:
                        d4 = 1;
                        break;
                    case 5:
                        d5 = 1;
                        break;
                    case 6:
                        d6 = 1;
                        break;
                    case 7:
                        d7 = 1;
                        break;
                }

                mu.msgbox("Debe seleccionar por lo menos un día de visita");
                
                cb1.requestFocus();
                return false;
            }

            double coorx = gl.gpspx;
            double coory = gl.gpspy;
            if (coorx==0 || coory == 0) {
                mu.msgbox("Debe obtener las coordenadas georeferenciales del cliente");
                lbGPS.requestFocus();
                return false;
            }

            String vIdDepto = gl.IdDep;
            String vIdMuni = gl.IdMun;
            String vIdCanal = gl.IdCanal;
            String vIdSubcanal = gl.IdSubcanal;

            if (mu.emptystr(vIdDepto)) {
                mu.msgbox("Falta la provincia del cliente");
                imgBuscarPro.requestFocus();
                return false;
            }

            if (mu.emptystr(vIdMuni)) {
                mu.msgbox("Falta el distrito del cliente");
                imgBuscarPro.requestFocus();
                return false;
            }

            if (mu.emptystr(vIdCanal)) {
                mu.msgbox("Falta el canal del cliente");
                imgBuscarCanal.requestFocus();
                return false;
            }

            if (mu.emptystr(vIdSubcanal)) {
                mu.msgbox("Falta el subcanal del cliente");
                imgBuscarCanal.requestFocus();
                return false;
            }

            s=txtCiudad.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta la ciudad del cliente");
                txtCiudad.requestFocus();
                return false;
            }

            s=txtPollo.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresar la compra semanal de pollo");
                txtPollo.requestFocus();
                return false;
            }

            double vCPollo= Double.valueOf((txtPollo.getText().toString().isEmpty()?"0":txtPollo.getText().toString()));
            if (vCPollo<0) {
                mu.msgbox("La compra semanal de pollo del cliente debe ser mayor a 0");
                txtPollo.requestFocus();
                return false;
            }

            s=txtEmbutidos.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresar la compra semanal de embutidos");
                txtEmbutidos.requestFocus();
                return false;
            }

            double vCEmbutidos= Double.valueOf((txtEmbutidos.getText().toString().isEmpty()?"0":txtEmbutidos.getText().toString()));
            if (vCEmbutidos<0) {
                mu.msgbox("La compra semanal de embutidos del cliente debe ser mayor a 0");
                txtEmbutidos.requestFocus();
                return false;
            }

            s=txtHuevos.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresar la compra semanal de huevos");
                txtHuevos.requestFocus();
                return false;
            }

            double vCHuevos= Double.valueOf((txtHuevos.getText().toString().isEmpty()?"0":txtHuevos.getText().toString()));
            if (vCHuevos<0) {
                mu.msgbox("La compra semanal de posturas del cliente debe ser mayor a 0");
                txtHuevos.requestFocus();
                return false;
            }

            s=txtRes.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresar la compra semanal de res");
                txtRes.requestFocus();
                return false;
            }

            double vCRes= Double.valueOf((txtRes.getText().toString().isEmpty()?"0":txtRes.getText().toString()));
            if (vCRes<0) {
                mu.msgbox("La compra semanal de res del cliente debe ser mayor a 0");
                txtRes.requestFocus();
                return false;
            }

            s=txtCerdo.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresar la compra semanal de cerdo");
                txtCerdo.requestFocus();
                return false;
            }
            double vCCerdo= Double.valueOf((txtCerdo.getText().toString().isEmpty()?"0":txtCerdo.getText().toString()));
            if (vCCerdo<0) {
                mu.msgbox("La compra semanal de cerdo del cliente debe ser mayor a 0");
                txtCerdo.requestFocus();
                return false;
            }

            s=txtCongelados.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresa la compra semanal de congelados");
                txtCongelados.requestFocus();
                return false;
            }
            double vCCongelados= Double.valueOf((txtCongelados.getText().toString().isEmpty()?"0":txtCongelados.getText().toString()));
            if (vCCongelados<0) {
                mu.msgbox("La compra semanal de congelados del cliente debe ser mayor a 0");
                txtCongelados.requestFocus();
                return false;
            }

            s=txtSalsas.getText().toString();
            if (mu.emptystr(s)) {
                mu.msgbox("Falta ingresa la compra semanal de salsas");
                txtSalsas.requestFocus();
                return false;
            }

            double vCSalsas= Double.valueOf((txtSalsas.getText().toString().isEmpty()?"0":txtSalsas.getText().toString()));
            if (vCSalsas<0) {
                mu.msgbox("La compra semanal de salsas del cliente debe ser mayor a 0");
                txtSalsas.requestFocus();
                return false;
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(e.getMessage());
            return  false;
        }
        return true;
    }

    public boolean emailValidator(EditText etMail) {

        try{
            String emailToText = etMail.getText().toString();
            String emailPattern = "[a-zA-Z0-9._-]+\\@[a-z]+\\.+[a-z]+";
            String emailPattern1 = "[a-zA-Z0-9._-]+\\@[a-z]+\\.+[a-z]+\\.+[a-z]+";

            if (emailToText.trim().matches(emailPattern) || emailToText.trim().matches(emailPattern1)) {
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(e.getMessage());
            return  false;
        }
 }

    public boolean telefonoValidator(EditText etTelefono) {

        try{
            String telefonoToText = etTelefono.getText().toString();
            String telefonoPattern = "[0-9]+\\-[0-9]+";

            if (telefonoToText.trim().matches(telefonoPattern) && (telefonoToText.length()==9 || telefonoToText.length()==8)) {

                String[] sp= telefonoToText.split("-");
                String sp1,sp2;
                sp1=sp[0];sp2=sp[1];

                if ((sp1.length() == 3 && sp2.length()==4) || (sp1.length() == 4 && sp2.length()==4)){
                    return true;
                }else{
                    return false;
                }
            } else {
                return false;
            }
        }catch (Exception e){
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(e.getMessage());
            return  false;
        }
    }

    //Set datos en Spinner
    private void setDataSpinnerPrecio() {
        Cursor DT;

        try {
            if (gl.peModal.equalsIgnoreCase("TOL")) {
                sql = "SELECT Codigo,Nombre FROM P_NIVELPRECIO WHERE NOMBRE = 'GENERALES'";
            } else {
                sql = "SELECT Codigo,Nombre FROM P_NIVELPRECIO ORDER BY Codigo";
            }

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                spincode.add(DT.getString(0));
                spinlist.add(DT.getString(1));
                DT.moveToNext();
            }

            if (DT != null) DT.close();

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinlist);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerPrecio.setAdapter(dataAdapter);
            spinnerPrecio.setSelection(0);

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            mu.msgbox(e.getMessage());
        }

    }

    public void CapturarFoto(View view) {
        msgTomarFotos("Capturar imagen");
    }
    //Foto
    public void tomarFoto(){
        Cursor DT, DU;
        File URLfoto;
        String path = "";
        int codResult = 1;

        try{
            setDataClienteG();
            if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                msgbox("El dispositivo no soporta toma de foto");return;
            }

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            Intent intento1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //#AT20220422 Guardar foto
            //Si tipo == 1 foto de cliente (fachada), si tipo == 2 fotos para documentos.
            if (tipo == 1) {
                path = Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + gl.corelCliente + ".jpg";
            } else if (tipo == 2) {

                String tmp = "SELECT * FROM D_CLINUEVOT_IMAGEN";
                DU =Con.OpenDT(tmp);

                if (DU != null && DU.getCount() > 0) {
                    if (correlativo == 0) {
                        sql = "SELECT MAX(CODIMAGEN) FROM D_CLINUEVOT_IMAGEN";
                        DT = Con.OpenDT(sql);
                        DT.moveToFirst();

                        correlativo = DT.getInt(0);
                        if (DT != null) DT.close();
                    }
                    codimg = correlativo +1;
                    correlativo = codimg;
                } else {
                    sql = "SELECT MAX(CODIMAGEN) FROM TMP_D_CLINUEVOT_IMAGEN";
                    DT=Con.OpenDT(sql);
                    DT.moveToFirst();

                    codimg = DT.getInt(0) +1;
                    if(DT!=null) DT.close();
                }

                path = Environment.getExternalStorageDirectory() + "/RoadFotos/clidocs/" + codimg + ".jpg";
            }

            URLfoto = new File(path);

            intento1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(URLfoto));
            startActivityForResult(intento1,codResult);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            mu.msgbox(e.getMessage());
        }

    }

    public void mostrarFachada(View view){
        Cursor DT;
        imgDB = false; imgPath=false;

        try {

            path = (Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + gl.corelCliente + ".jpg");

            File archivo = new File(path);

            if (archivo.exists()) {
                imgPath = true;
                inputFachada();
            } else {
                Toast.makeText(this,"Fachada no disponible",Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("inputFachada: " + e.getMessage());
        }

    }

    private void miniFachada(){
        Cursor DT;
        imgDB = false;
        try {

            path = (Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + gl.corelCliente + ".jpg");
            File archivo = new File(path);

            sql = "SELECT IMAGEN FROM P_CLIENTE_FACHADA WHERE CODIGO ='"+ gl.corelCliente +"'";
            DT=Con.OpenDT(sql);

            if(DT.getCount() > 0){
                DT.moveToFirst();
                imagenbase64 = DT.getString(0);
                imgDB = true;
            }

            if(archivo.exists()){
                imgRoadTit.setImageURI(Uri.fromFile(archivo));
            } else if (imgDB == true){
                byte[] btImagen = android.util.Base64.decode(imagenbase64, Base64.DEFAULT);
                Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
                imgRoadTit.setImageBitmap(redimensionarImagen(bitm,200,200));
            } else {
                imgRoadTit.setImageResource(R.drawable.cliente);
            }

            if(DT!=null) DT.close();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("inputFachada: " + e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String pathFoto ="";

        if (requestCode == 1) {
            try {
                if (tipo == 1) {
                    pathFoto = (Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + gl.corelCliente + ".jpg");

                    try {
                        Bitmap bitmap1 = BitmapFactory.decodeFile(pathFoto);
                        bitmap1 = redimensionarImagen(bitmap1, 640, 360);

                        FileOutputStream out = new FileOutputStream(pathFoto);
                        bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, out);
                        out.flush();
                        out.close();

                        File iarchivo = new File(pathFoto);
                        imgRoadTit.setImageURI(Uri.fromFile(iarchivo));
                    } catch (Exception e) {
                    }
                } else if (tipo == 2) {
                    opendb();

                    ins.init("TMP_D_CLINUEVOT_IMAGEN");
                    ins.add("CODIMAGEN", codimg);
                    ins.add("CODIGO", gl.corelCliente);

                    db.execSQL(ins.sql());
                    db.close();

                    msgCapturarDocumento("Agregar otro documento");
                }
            } catch (Exception e) {
                mu.msgbox("onActivityResult: " + e.getMessage());
            }
        }
    }

    public Bitmap redimensionarImagen(Bitmap mBitmap, float newWidth, float newHeigth){

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeigth) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);

    }

    public void inputFachada(){

        try{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            final ImageView imgFachadaC = new ImageView(this);
            imgFachadaC.setScaleType(CENTER_CROP);


            if(imgPath == true) {
                Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                imgFachadaC.setImageBitmap(redimensionarImagen(bitmap1, 640, 360));
            }else if(imgDB == true) {
                byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
                Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
                imgFachadaC.setImageBitmap(redimensionarImagen(bitm,640,360));
            }

            alert.setView(imgFachadaC);

            alert.show();

            imgFachadaC.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    zoomFoto = new PhotoViewAttacher(imgFachadaC);
                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //GPS
    public void setGPS(View view) {
        try{
            setDataClienteG();
            gl.gpsCliente = true;
            startActivity(new Intent(this,CliGPS.class));

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //Modales
    private void msgAskSave(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("¿" + msg  + "?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    GuardarCliente();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private boolean getCountTmpImg() {
        Cursor DT;
        try {
            sql = "SELECT * FROM TMP_D_CLINUEVOT_IMAGEN";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                return true;
            }

            return false;

        } catch (Exception e) {
            mu.msgbox(e.getMessage() + sql);
            return false;
        }
    }
    private void msgTomarFotos(String msg) {

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("¿" + msg  + "?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Ver Documentos", (dialog1, which) -> {
                if (getCountTmpImg()) {
                    startActivity(new Intent(this, CliDocumentos.class));
                } else {
                    toast("No hay documentos disponibles.");
                }
            });

            dialog.setNegativeButton("Fachada", (dialog12, which) -> {
                tipo = 1;
                tomarFoto();
            });

            dialog.setNeutralButton("Documento", (dialog13, which) -> {
                tipo = 2;
                tomarFoto();
            });

            dialog.show();

        }catch (Exception e){
            addlog(Objects.requireNonNull(new Object()
            {
            }.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
        }

    }

    private void msgCapturarDocumento(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("¿" + msg  + "?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    tipo = 2;
                    tomarFoto();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //Set Handlers
    private void setHandlers() {
        spinnerPrecio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TextView spinlabel;
                String scod;

                try {
                    spinlabel = (TextView) parentView.getChildAt(0);
                    spinlabel.setTextColor(Color.BLACK);
                    spinlabel.setPadding(5, 0, 0, 0);
                    spinlabel.setTextSize(16);

                    scod = spincode.get(position);
                    nivel = Integer.parseInt(scod);
                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }
        });

        imgBuscarCanal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verCanales();
            }
        });

        imgBuscarPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verProvincia();
            }
        });

        imgVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verVendedor();
            }
        });

    }

    public void verCanales() {
        setDataClienteG();
        Intent i = new Intent(this,CanalSubcanal.class);
        startActivity(i);
    }

    public void verProvincia() {
        setDataClienteG();
        Intent i = new Intent(this,DepartamentoMun.class);
        startActivity(i);
    }

    public void verVendedor() {
        setDataClienteG();
        Intent i = new Intent(this,ListaVendedores.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        limpiar();
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();

    }

    private void setDataClienteG() {
        gl.CliNombre = txtCliNombre.getText().toString();
        gl.CliNit = txtCliNit.getText().toString();
        gl.CliDireccion = txtCliDireccion.getText().toString();
        gl.CliProvincia = txtProvincia.getText().toString();
        gl.CliDistrito = txtDistrito.getText().toString();
        gl.CliCiudad = txtCiudad.getText().toString();
        gl.CliTel = txtCliTelefono.getText().toString();
        gl.CliEmail = txtCliEmail.getText().toString();
        gl.CliContacto = txtCliContacto.getText().toString();
        gl.EditarClienteCanal = txtCliCanal.getText().toString();
        gl.EditarClienteSubcanal = txtCliSubCanal.getText().toString();
        gl.CliCodVen = txtCodVendedor.getText().toString();
        gl.CliCsPollo = txtPollo.getText().toString();
        gl.CliCsEmbutido = txtEmbutidos.getText().toString();
        gl.CliCsHuevo = txtHuevos.getText().toString();
        gl.CliCsRes = txtRes.getText().toString();
        gl.CliCsCerdo = txtCerdo.getText().toString();
        gl.CliCsCongelados = txtCongelados.getText().toString();
        gl.CliCsSalsas = txtSalsas.getText().toString();
    }

    public void limpiar() {
        gl.corelCliente = "";
        gl.CliNombre = "";
        gl.CliNit = "";
        gl.CliDireccion = "";
        gl.CliProvincia = "";
        gl.CliDistrito = "";
        gl.CliCiudad = "";
        gl.CliTel = "";
        gl.CliEmail = "";
        gl.CliContacto = "";
        gl.CliCodVen = "";
        gl.CliCsPollo = "";
        gl.CliCsEmbutido = "";
        gl.CliCsHuevo = "";
        gl.CliCsRes = "";
        gl.CliCsCerdo = "";
        gl.CliCsCongelados = "";
        gl.CliCsSalsas = "";
        gl.IdCanal = "";
        gl.IdSubcanal ="";
        gl.EditarClienteCanal = "";
        gl.EditarClienteSubcanal = "";
        gl.CliCodVen = "";
        gl.CliNomVen = "";
        gl.gpspx = 0.0000;
        gl.gpspy = 0.0000;
        gl.gpsCliente = false;

        txtCliNombre.setText("");
        txtCliNit.setText("");
        txtCliDireccion.setText("");
        txtProvincia.setText("");
        txtDistrito.setText("");
        txtCiudad.setText("");
        txtCliTelefono.setText("");
        txtCliEmail.setText("");
        txtCliContacto.setText("");
        txtCliCanal.setText("");
        txtCliSubCanal.setText("");
        txtCodVendedor.setText("");
        txtPollo.setText("");
        txtEmbutidos.setText("");
        txtHuevos.setText("");
        txtRes.setText("");
        txtCerdo.setText("");
        txtCongelados.setText("");
        txtSalsas.setText("");
    }

    private void setData() {

        if (gl.CliCodVen == null || gl.CliCodVen.isEmpty()) {
            gl.CliCodVen = gl.vend;
        }

        if (gl.corelCliente.isEmpty() || gl.corelCliente == null) {
            setCorel();
        }

        String coorx = String.valueOf(gl.gpspx);
        String coory  = String.valueOf(gl.gpspy);
        lbGPS.setText(coorx +" , "+coory);

        txtCliNombre.setText(gl.CliNombre);
        txtCliNit.setText(gl.CliNit);
        txtCliDireccion.setText(gl.CliDireccion);
        txtProvincia.setText(gl.CliProvincia);
        txtDistrito.setText(gl.CliDistrito);
        txtCiudad.setText(gl.CliCiudad);
        txtCliTelefono.setText(gl.CliTel);
        txtCliEmail.setText(gl.CliEmail);
        txtCliContacto.setText(gl.CliContacto);
        txtCliCanal.setText(gl.EditarClienteCanal);
        txtCliSubCanal.setText(gl.EditarClienteSubcanal);
        txtCodVendedor.setText(gl.CliCodVen);
        txtPollo.setText(gl.CliCsPollo);
        txtEmbutidos.setText(gl.CliCsEmbutido);
        txtHuevos.setText(gl.CliCsHuevo);
        txtRes.setText(gl.CliCsRes);
        txtCerdo.setText(gl.CliCsCerdo);
        txtCongelados.setText(gl.CliCsCongelados);
        txtSalsas.setText(gl.CliCsSalsas);
    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            if (db!= null) {
                Con.vDatabase=db;
                active=1;
            } else {
                active = 0;
            }
        } catch (Exception e) {
            //mu.msgbox(e.getMessage());
            Log.w("Error",e.getMessage());
            active= 0;
        }
    }

}