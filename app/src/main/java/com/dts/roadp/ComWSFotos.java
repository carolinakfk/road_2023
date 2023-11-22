package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ComWSFotos extends PBase {

    private TextView lblInfo,lblParam,lblAct;
    private ProgressBar barInfo;
    private EditText txtRuta,txtWS,txtEmp;
    private CheckBox chkAll;

    private int isbusy,reccnt,rec;
    private String ruta,rootdir;

    private SQLiteDatabase dbT;
    private BaseDatos ConT;
    private BaseDatos.Insert insT;
    private AppMethods clsAppM;

    private ArrayList<String> listItems=new ArrayList<String>();


    // Web Service

    public AsyncCallRec wsRtask;

    private static String sstr,fstr,fprog,ferr,idbg,dbg;
    private int scon;
    private String gEmpresa;

    private final String NAMESPACE ="http://tempuri.org/";
    private String METHOD_NAME,URL;

    private clsDataBuilder dbld;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wsfotos);

        super.InitBase();
        addlog("ComWSFotos",""+du.getActDateTime(),gl.vend);

        System.setProperty("line.separator","\r\n");

        rootdir= Environment.getExternalStorageDirectory()+"/RoadFotos/";

        clsAppM = new AppMethods(this, gl, Con, db);

        lblParam= (TextView) findViewById(R.id.lblProd);
        lblInfo= (TextView) findViewById(R.id.lblETipo);
        lblAct= (TextView) findViewById(R.id.textView5);lblAct.setVisibility(View.INVISIBLE);
        barInfo= (ProgressBar) findViewById(R.id.progressBar2);
        txtRuta= (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
        txtWS= (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
        txtEmp= (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);
        chkAll= (CheckBox) findViewById(R.id.checkBox);

        isbusy=0;

        lblInfo.setText("");lblParam.setText("");
        barInfo.setVisibility(View.INVISIBLE);

        ruta=gl.ruta;
        gEmpresa=gl.emp;

        gl.isOnWifi = clsAppM.isOnWifi();

        getWSURL();

        dbld=new clsDataBuilder(this);

    }

    //region Events

    public void askRec(View view) {

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Imagenes");
            dialog.setMessage("¿Actualizar imagenes?");

            dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runRecep();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    //endregion

    //region Main

    private void runRecep() {
        Cursor dt;

        listItems.clear();rec=0;

        try{
            if (isbusy==1) return;
            if (!setComParams()) return;

            if (chkAll.isChecked()) {
                sql = "SELECT CODIGO FROM P_PRODUCTO";
            } else {
                sql = "SELECT DISTINCT CODIGO FROM P_STOCK";
            }
            dt = Con.OpenDT(sql);
            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    listItems.add(dt.getString(0));
                    dt.moveToNext();
                }
            }
            if(dt!=null) dt.close();

            reccnt=listItems.size();
            if (reccnt==0) return;

            isbusy=1;

            barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
            lblAct.setVisibility(View.VISIBLE);
            lblInfo.setText("Conectando ...");

            wsRtask = new AsyncCallRec();
            wsRtask.execute();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

    //region Web Service Methods

    public int guardaImagen(String idprod) {
        int rc;
        String s, ss,resstr;

        METHOD_NAME = "getImage";
        sstr = "OK";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("idprod");
            param.setValue(idprod);
            request.addProperty(param);

            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            resstr = response.toString();

            try {
                //byte[] imgbytes = resstr.getBytes();

                byte[] imgbytes= Base64.decode(resstr, Base64.DEFAULT);

                int bs=imgbytes.length;

                FileOutputStream fos = new FileOutputStream(rootdir+idprod+".jpg");
                BufferedOutputStream outputStream = new BufferedOutputStream(fos);
                outputStream.write(imgbytes);
                outputStream.close();

            } catch (Exception ee) {
                sstr = ee.getMessage();return 0;
            }

            sstr =""+resstr.length();
            return 1;
        } catch (Exception e) {
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
            param.setName("Value");param.setValue("OK");

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);

            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString()+"..";

            return 1;
        } catch (Exception e) {
            sstr=e.getMessage();
        }

        return 0;
    }

    //endregion

    //region Web Service - Recepcion

    private boolean getData() {
        String ims;

        ferr = "";

        try {

            fprog = "Procesando 1/"+reccnt;
            wsRtask.onProgressUpdate();

            for (int i = 0; i <reccnt; i++) {

                ims=listItems.get(i);
                if (guardaImagen(ims)==1) rec++;

                fprog = "Procesando "+(i+1)+" / "+reccnt;
                wsRtask.onProgressUpdate();
            }

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase = dbT;
            insT = ConT.Ins;

            fprog = "Fin de actualización";
            wsRtask.onProgressUpdate();

        } catch (Exception e) {

            try {
                ConT.close();
            } catch (Exception ee) {}

            sstr = e.getMessage();
            ferr = sstr + "\n" + sql;

            return false;
        }

        try {
            ConT.close();
        } catch (Exception e) {
            addlog(new Object()  {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

        return true;
    }

    //endregion

    //region Web Service handling Methods

    public void wsExecute(){

        fstr="No connect";scon=0;

        try {

            if (getTest()==1) scon=1;

            idbg=idbg + sstr;

            if (scon==1) {
                fstr="Sync OK";
                if (!getData()) fstr="Recepción incompleta : "+sstr;
            } else {
                fstr="No se puede conectar al web service : "+sstr;
            }

        } catch (Exception e) {
            scon=0;
            fstr="No se puede conectar al web service. "+e.getMessage();
            Log.d("E",fstr+sstr);
        }

    }

    public void wsFinished(){

        barInfo.setVisibility(View.INVISIBLE);
        lblParam.setVisibility(View.INVISIBLE);
        lblAct.setVisibility(View.INVISIBLE);

        try{
            if (fstr.equalsIgnoreCase("Sync OK")) {
                lblInfo.setText(" ");
                msgExit("Actualizado imagenes : "+rec);
            } else {
                lblInfo.setText(fstr);
                mu.msgbox("Ocurrio error : \n"+fstr+" ("+reccnt+") " + ferr);
            }

            isbusy=0;
            //mu.msgbox("::"+dbg);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }


    }

    private class AsyncCallRec extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                wsExecute();
            } catch (Exception e) {
                if (scon==0) fstr="No se puede conectar al web service : "+sstr;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try{
                wsFinished();
            }catch (Exception e){
                Log.d("onPostExecute",e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {
                Log.d("onPreExecute",e.getMessage());
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                lblInfo.setText(fprog);
            } catch (Exception e) {
                Log.d("onProgressUpdate",e.getMessage());
            }
        }

    }

    //endregion

    //region Aux

    public void comManual(View view) {
        try{
            Intent intent = new Intent(this,ComDrop.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

    }

    public void getWSURL() {
        Cursor DT;
        String wsurl;

        txtRuta.setText(ruta);
        txtEmp.setText(gEmpresa);

        try {
            sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            if (gl.isOnWifi==1) {
                URL = DT.getString(0);
            }else if(gl.isOnWifi==2){
                URL = DT.getString(1);
            }

            if(DT!=null) DT.close();

           // URL=wsurl;
            if (URL!=null && URL.equalsIgnoreCase("")){
                txtWS.setText(URL);
            }else{
                toast("No hay configurada ruta para transferencia de datos");
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            //MU.msgbox(e.getMessage());
            URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
            //txtWS.setText("");
            return;
        }

    }

    private boolean setComParams() {
        String ss;

        /*JP 20210714
        ruta="0001-1";
        URL="http://200.46.46.104:8001/RDC7_SAP_DEV_ANDR/wsAndr.asmx";
        txtRuta.setText(ruta);txtEmp.setText("03");
        txtWS.setText(URL);
        */

        ss=txtRuta.getText().toString().trim();

        try{
            if (mu.emptystr(ss)) {
                mu.msgbox("La ruta no está definida.");return false;
            }

            ss=txtEmp.getText().toString().trim();
            if (mu.emptystr(ss)) {
                mu.msgbox("La empresa no está definida.");return false;
            }
            gEmpresa=ss;

            ss=txtWS.getText().toString().trim();
            if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
                mu.msgbox("La dirección de Web service no está definida.");return false;
            }
            URL=ss;
        }catch (Exception e){
            String sse=e.getMessage();
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }


        return true;
    }

    private void msgExit(String msg) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Actualización completa");
            dialog.setMessage(msg);

            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   finish();
                }
            });

            dialog.show();
        } catch (Exception e) {}

    }

    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        try {
            if (isbusy==0) {
                super.onBackPressed();
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

    }

    //endregion

}
