package com.dts.roadp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class comWSLic extends PBase {

    private TextView lblInfo, lblParam, lblEnv,lblDev;
    private ProgressBar barInfo;
    private EditText txtRuta, txtWS, txtEmp;
    private ImageView imgEnv;
    private RelativeLayout ralBack;

    private int isbusy, fecha, lin, reccnt;
    private String err, ruta, rutatipo, sp, docstock;
    private boolean fFlag, showprogress, errflag;

    private SQLiteDatabase dbT;
    private BaseDatos ConT;
    private BaseDatos.Insert insT;
    private AppMethods app;

    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayList<String> results = new ArrayList<String>();

    private ArrayList<clsClasses.clsEnvio> items = new ArrayList<clsClasses.clsEnvio>();
    private ListAdaptEnvio adapter;

    private clsDataBuilder dbld;
    private DateUtils DU;

    private boolean licenciaRuta=false, licenciaHH=false;

    // 355030097127235

    // Web Service -

    public AsyncCallSend wsStask;

    private static String sstr, fstr, fprog, finf, ferr, fterr, idbg, dbg, ftmsg, esql, ffpos;
    private int scon, running, pflag, stockflag, conflag;
    private String ftext, slsync, senv, gEmpresa, ActRuta,  strliqid, devinfo ;
    private boolean  ftflag;

    private final String NAMESPACE = "http://tempuri.org/";
    private String METHOD_NAME, URL, URL_Remota;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wslic);

        super.InitBase();
        addlog("ComWSLic", "" + du.getActDateTime(), gl.vend);

        System.setProperty("line.separator", "\r\n");

        dbld = new clsDataBuilder(this);
        app = new AppMethods(this, gl, Con, db);

        lblInfo = (TextView) findViewById(R.id.lblETipo);
        lblParam = (TextView) findViewById(R.id.lblProd);
        barInfo = (ProgressBar) findViewById(R.id.progressBar2);
        txtRuta = (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
        txtWS = (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
        txtEmp = (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);
        lblEnv = (TextView) findViewById(R.id.btnSend);
        imgEnv = (ImageView) findViewById(R.id.imageView6);
        lblDev = (TextView) findViewById(R.id.textView87);
        ralBack = (RelativeLayout) findViewById(R.id.relwsmail);

        isbusy = 0;

        lblInfo.setText("");
        lblParam.setText("");
        barInfo.setVisibility(View.INVISIBLE);

        ruta = gl.ruta;
        ActRuta = ruta;
        gEmpresa = gl.emp;
        rutatipo = gl.rutatipog;

        gl.isOnWifi = app.isOnWifi();

        getWSURL();

        devinfo=gl.devicename+" / "+Build.MODEL;
        lblDev.setText(devinfo+"\n"+gl.deviceId);

        setHandlers();

        validaLicencia();

    }


    //region Events

    public void askSend(View view) {

        try {

            if (isbusy == 1) {
                toastcent("Por favor, espere que se termine la tarea actual.");
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Envio");
            dialog.setMessage("¿Enviar requerimiento de licencia?");

            dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runSend();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();
        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private void validaLicencia() {

        CryptUtil cu = new CryptUtil();
        Cursor dt;
        String lic, lickey, licruta, rutaencrypt;

        try {

            lickey = cu.encrypt(gl.deviceId);
            rutaencrypt = cu.encrypt(gl.ruta);

            sql = "SELECT lic, licparam FROM Params";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();
            lic = dt.getString(0);
            licruta = dt.getString(1);

           if(dt!=null) dt.close();

            if (!mu.emptystr(lic)){
                if (lic.equalsIgnoreCase(lickey) ) {
                    licenciaHH=true;
                }
            }

            if (!mu.emptystr(licruta)){
                if (licruta.equalsIgnoreCase(rutaencrypt)) {
                    licenciaRuta=true;
                }
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            mu.msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " : " + e.getMessage());
        }
    }

    private void setHandlers() {
        ralBack.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeRight() {
                onBackPressed();
            }

            public void onSwipeLeft() {
            }
        });
    }

    //endregion

    //region Main

    private void runSend() {

        try {
            if (isbusy == 1) return;

            if (!setComParams()) return;

            isbusy = 1;

            barInfo.setVisibility(View.VISIBLE);
            barInfo.invalidate();
            lblInfo.setText("Conectando ...");

            showprogress = true;
            wsStask = new AsyncCallSend();
            wsStask.execute();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    //endregion

    //region Web Service Methods

    public int fillTable(String value, String delcmd) {

        int rc;
        String s, ss;

        METHOD_NAME = "getIns";

        sstr = "OK";

        try {

            idbg = idbg + " filltable ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(value);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL,60000);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc = resSoap.getPropertyCount() - 1;
            idbg = idbg + " rec " + rc + "  ";

            s = "";
            if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
                if (rc == 1) {
                    stockflag = 0;//return 1;
                } else {
                    stockflag = 1;
                }
            }

            // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
            // 	idbg=idbg+" RC ="+rc+"---";
            //}


            for (int i = 0; i < rc; i++) {
                String str = "";
                try {
                    str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);
                    //s=s+str+"\n";
                } catch (Exception e) {
                    mu.msgbox("error: " + e.getMessage());
                }

                if (i == 0) {

                    idbg = idbg + " ret " + str + "  ";

                    if (str.equalsIgnoreCase("#")) {
                        listItems.add(delcmd);
                    } else {
                        idbg = idbg + str;
                        ftmsg = ftmsg + "\n" + str;
                        ftflag = true;
                        sstr = str;
                        return 0;
                    }
                } else {
                    try {
                        sql = str;
                        listItems.add(sql);
                        sstr = str;
                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                        sstr = e.getMessage();
                    }
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            //#EJC20190226: Evitar que se muestre OK después del nombre de la tabla cuando da error de timeOut.
            sstr = e.getMessage();
            idbg = idbg + " ERR " + e.getMessage();
            return 0;
        }
    }

    public int commitSQL() {
        int rc;
        String s, ss;

        METHOD_NAME = "Commit";
        sstr = "OK";

        if (dbld.size() == 0) return 1;

        s = "";
        for (int i = 0; i < dbld.size(); i++) {
            ss = dbld.items.get(i);
            s = s + ss + "\n";
        }
        if (showprogress) {
            fprog = "Enviando ...";
            wsStask.onProgressUpdate();
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(s);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            sstr = s;
            return 0;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            sstr = e.getMessage();
        }

        return 0;
    }

    public int requestLicence(String serial, String devname) {
        int rc;
        String s, ss;

        METHOD_NAME = "requestLicence";
        sstr = "OK";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("Serial");
            param.setValue(serial);
            request.addProperty(param);

            PropertyInfo param2 = new PropertyInfo();
            param2.setType(String.class);
            param2.setName("Name");
            param2.setValue(devname);
            request.addProperty(param2);

            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            sstr = s;
            return 0;
        } catch (Exception e) {
             sstr = e.getMessage();
        }

        return 0;
    }

    public int requestLicenceRuta(String ruta) {
        int rc;
        String s, ss;

        METHOD_NAME = "requestLicenceRuta";
        sstr = "OK";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("Ruta");
            param.setValue(ruta);
            request.addProperty(param);

            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            sstr = s;
            return 0;
        } catch (Exception e) {
            sstr = e.getMessage();
        }

        return 0;
    }

    public int OpenDTt(String sql) {
        int rc;

        METHOD_NAME = "OpenDT";

        results.clear();

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(sql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc = resSoap.getPropertyCount() - 1;

            for (int i = 0; i < rc + 1; i++) {
                String str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                if (i == 0) {
                    sstr = str;
                    if (!str.equalsIgnoreCase("#")) {
                        sstr = str;
                        return 0;
                    }
                } else {
                    results.add(str);
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            sstr = e.getMessage();
        }

        return 0;

    }

    public int getTest() {

        METHOD_NAME = "TestWS";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("Value");
            param.setValue("OK");

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);

            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString() + "..";

            return 1;
        } catch (Exception e) {
            sstr = e.getMessage();
        }

        return 0;
    }

    //endregion

    //region WS Envio Methods

    private boolean sendData() {

        errflag = false;

        senv = "Envío terminado \n \n";

        items.clear();
        dbld.clearlog();

        try {
            envioLic();
            dbld.savelog();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return errflag;
    }

    public void envioLic() {
        Cursor DT;
        String cod, ss;
        int stp;
        double px, py;

        fprog = " ";
        wsStask.onProgressUpdate();

        try {

            if (!licenciaHH){
                if (requestLicence(gl.deviceId,devinfo)==1) {
                    errflag=false;
                } else {
                    fterr = sstr;errflag=true;
                }
            }

            if (!licenciaRuta){
                if (requestLicenceRuta(gl.ruta)==1) {
                    errflag=false;
                } else {
                    fterr = sstr;
                    errflag=true;
                }
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = e.getMessage();errflag=true;
        }

    }

    public void addItem(String nombre,int env,int pend) {
        clsClasses.clsEnvio item;

        try {
            item=clsCls.new clsEnvio();

            item.Nombre=nombre;
            item.env=env;
            item.pend=pend;

            items.add(item);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
    }

    //endregion

    //region WS Envio Handling Methods

    public void wsSendExecute(){

        running=1;fstr="No connect";scon=0;
        errflag=false;

        try {

            if (getTest() == 1) {
                scon = 1;
            } else {
                URL = URL_Remota;
                if (getTest() == 1) {
                    scon = 1;
                }
            }

            if (scon==1) {
                fstr="Sync OK";

                if (!sendData()) {
                    fstr="Envio incompleto : "+sstr;
                } else {
                }
            } else {
                fstr="No se puede conectar al web service : "+sstr;
            }

        } catch (Exception e) {
            scon=0;
            fstr="No se puede conectar al web service. "+e.getMessage();
            Log.d("E", fstr + sstr);
        }
    }

    public void wsSendFinished(){

        barInfo.setVisibility(View.INVISIBLE);
        lblParam.setVisibility(View.INVISIBLE);
        running=0;

        //senv="Envio completo\n";

        try{
            if (scon==0) {
                lblInfo.setText(fstr);writeErrLog(fstr);
                mu.msgbox(fstr);
                lblInfo.setText(fstr);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                addlog("Envío", fterr + " " + fstr, esql);
                return;
            }

            if (!errflag) {
                lblInfo.setText(" ");
                msgResultEnvio(senv);

            } else {
                lblInfo.setText(fterr);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                mu.msgbox("Ocurrió error : \n" + fterr );
                addlog("Envío", fterr, esql);
                return;
            }

            //if (!errflag) ComWS.super.finish();

            isbusy=0;

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private class AsyncCallSend extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                Looper.prepare();
                wsSendExecute();
            } catch (Exception e) {
                if (scon == 0) {
                    fstr = "No se puede conectar al web service : " + sstr;
                    //lblInfo.setText(fstr);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsSendFinished();
                Looper.loop();
            }catch (Exception e) {
                Log.d("onPostExecute", e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                lblInfo.setText(fprog);
            } catch (Exception e) { }
        }

    }

    //endregion

    //region Aux

    public void comManual(View view) {
        try{
            Intent intent = new Intent(this,ComDrop.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void getWSURL() {
        Cursor DT;
        String wsurl;

        if (!gl.debug) {
            txtRuta.setText(ruta);
            txtEmp.setText(gEmpresa);
        }

        try {

            sql = "SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='" + ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                URL = DT.getString(0);
                URL_Remota = DT.getString(1);

                gl.URLtemp = URL;

                if (!URL.isEmpty()) {
                    txtWS.setText(URL);
                } else if (!URL_Remota.isEmpty()) {
                    txtWS.setText(URL);
                } else {
                    toast("No hay configurada URL para transferencia de datos");
                }

            }

            if (DT != null) DT.close();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            URL = "*";
            txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");

        }

    }

    private boolean setComParams() {
        String ss;

        ss=txtRuta.getText().toString().trim();

        try{
            if (mu.emptystr(ss)) {
                mu.msgbox("La ruta no esta definida.");return false;
            }
            ActRuta=ss;

            ss=txtEmp.getText().toString().trim();
            if (mu.emptystr(ss)) {
                mu.msgbox("La empresa no esta definida.");return false;
            }
            gEmpresa=ss;

            ss=txtWS.getText().toString().trim();
            //ss="http://192.168.1.142/wsAndr/wsandr.asmx";
            if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
                mu.msgbox("La dirección de Web service no esta definida.");return false;
            }
            URL=ss;
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
        return true;
    }

    private int getDocCount(String ss,String pps) {

        Cursor DT;
        int cnt = 0;
        String st;

        try {

            sql=ss;
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
                DT.moveToFirst();
                cnt=DT.getInt(0);
            }

            if(DT!=null) DT.close();

            if (cnt>0) {
                st=pps+" "+cnt;
                sp=sp+st+", ";
            }

        } catch (Exception e) {
            //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            //mu.msgbox(sql+"\n"+e.getMessage());
        }

        return cnt;

    }

    private void paramsExtra() {
        try {
            AppMethods app=new AppMethods(this,gl,Con,db);
            app.parametrosExtra();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(e.getMessage());
        }
    }

    private void writeErrLog(String errstr) {
        BufferedWriter writer = null;
        FileWriter wfile;

        try {
            String fname = Environment.getExternalStorageDirectory()+"/roaderror.txt";

            wfile=new FileWriter(fname,false);
            writer = new BufferedWriter(wfile);
            writer.write(errstr);writer.write("\r\n");
            writer.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    private void restarApp(){
        try{
            PackageManager packageManager = this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent =Intent.makeRestartActivityTask(componentName);
            //Intent mainIntent = IntentCompat..makeRestartActivityTask(componentName);
            this.startActivity(mainIntent);
            System.exit(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgResultEnvio(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Envio correcto", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskExit(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (gl.modoadmin) {
                        restarApp();
                    } else {
                        finish();
                    };
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskExitComplete() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("Está seguro de salir de la aplicación?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });

            dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }



    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        try{
            if (isbusy==0) {
                if (gl.modoadmin) {
                    msgAskExitComplete();
                } else {
                    super.onBackPressed();
                }
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

	/*@Override
	protected void onResume() {
		super.onResume();
		try {
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
	}

	@Override
	protected void onPause() {
		try {
			this.wakeLock.release();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		this.wakelock.release();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		this.wakelock.release();
	}
	*/

    //endregion


}
