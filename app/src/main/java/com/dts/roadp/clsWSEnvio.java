package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class clsWSEnvio {

    protected int active;

    private String ruta;

    private SQLiteDatabase db;
    private BaseDatos Con;
    private BaseDatos.Insert ins;
    private BaseDatos.Update upd;

    private ArrayList<String> listItems=new ArrayList<String>();

    // Web Service

    public AsyncCallEnv wsEtask;

    private static String fprog,idbg,dbg, fterr;
    public static String sstr, ferr,fstr, senv;

    private int scon;
    private String gEmpresa, sql, proceso;
    private int isOnWifi=0;

    private final String NAMESPACE ="http://tempuri.org/";
    private String METHOD_NAME,URL;

    private clsDataBuilder dbld;

    private int tipoEnvio;
    private Context Cont;

    public clsWSEnvio(Context cont,String gRuta, String gEmp,int vTipoEnvio) {

        ruta=gRuta;
        gEmpresa=gEmp;

        Cont =cont;

        Con = new BaseDatos(Cont);
        opendb();
        ins=Con.Ins;upd=Con.Upd;

        isOnWifi = isOnWifi();

        getWSURL();

        dbld=new clsDataBuilder(Cont);

        tipoEnvio=vTipoEnvio;
    }

    public int isOnWifi(){

        int activo=0;

        try{

            ConnectivityManager connectivityManager = (ConnectivityManager) Cont.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()){

                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    activo=1;
                }

                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    activo = 2;
                }

            }

        }catch (Exception ex){

        }

        return activo;

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
            ferr=e.getMessage();	active= 0;
        }
    }

    public void getWSURL() {
        Cursor DT;
        String wsurl;


        try {
            sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            if (isOnWifi==1) {
                URL = DT.getString(0);
            }else if(isOnWifi==2){
                URL = DT.getString(1);
            }

            if (URL.equals(null) && URL.equals("")){
                Toast.makeText(Cont,"No hay configurada ruta para transferencia de datos",Toast.LENGTH_SHORT).show();
            }

            //URL=wsurl;
        } catch (Exception e) {
            ferr=e.getMessage();
            URL="*";
            return;
        }

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

    public  void runExecuteEnvio(){
        wsEtask=new AsyncCallEnv();
        wsEtask.execute();
    }

    private void wsExecuteEnvio(){

        fstr="No connect";scon=0;

        try {

            if (getTest()==1)
            scon=1;

            idbg=idbg + sstr;

            if (scon==1) {

                fstr="Envío OK";

                switch (tipoEnvio){

                    case 1:
                        if (!envio_D_MOV_en_dev()) fstr="Envío incompleto : "+sstr;
                    case 2:
                        if (!envio_Rating()) fstr="Envío incompleto : "+sstr;
                }


            } else {
                fstr="No se puede conectar al web service : "+sstr;
            }

        } catch (Exception e) {
            scon=0;
            fstr="No se puede conectar al web service. "+e.getMessage() + " " + sstr;
            Log.d("E",fstr+sstr);
        }

    }

    public void wsFinished(){

        try{
            if (fstr.equalsIgnoreCase("Envío OK")) {
                Toast.makeText(Cont, "Envio correcto", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Cont, "Ocurrió error : \n"+fstr, Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }


    }

    private class AsyncCallEnv extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                wsExecuteEnvio();
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
                proceso=fprog;
            } catch (Exception e) {
                Log.d("onProgressUpdate",e.getMessage());
            }
        }

    }

    protected void addlog(final String methodname, String msg, String info) {

        final String vmethodname = methodname;
        final String vmsg = msg;
        final String vinfo = info;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAddlog(vmethodname,vmsg, vinfo);
            }
        }, 500);

    }

    protected void setAddlog(String methodname,String msg,String info) {

        BufferedWriter writer = null;
        FileWriter wfile;

        try {

            String fname = Environment.getExternalStorageDirectory()+"/roadlog.txt";
            wfile=new FileWriter(fname,true);
            writer = new BufferedWriter(wfile);

            writer.write("Método: " + methodname + " Mensaje: " +msg + " Info: "+ info );
            writer.write("\r\n");

            writer.close();

        } catch (Exception e) {
            proceso="Error " + e.getMessage();
        }
    }

    private boolean envio_D_MOV_en_dev() {
        Cursor DT;
        String cor;
        int i, pc = 0, pcc = 0;

        boolean vEnvio=false;

        try {

            sql = "SELECT COREL FROM D_MOV WHERE STATCOM='N'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() == 0) {
                senv += "Inventario : " + pc + "\n";
                vEnvio=true;
                return vEnvio;
            }

            pcc = DT.getCount();
            pc = 0;
            i = 0;

            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                cor = DT.getString(0);

                try {

                    i += 1;
                    fprog = "Inventario " + i;
                    wsEtask.onProgressUpdate();

                    dbld.clear();

                    dbld.insert("D_MOV", "WHERE COREL='" + cor + "'");
                    dbld.insert("D_MOVD", "WHERE COREL='" + cor + "'");
                    dbld.insert("D_MOVDB", "WHERE COREL='" + cor + "'");
                    dbld.insert("D_MOVDCAN", "WHERE COREL='" + cor + "'");
                    dbld.insert("D_MOVDPALLET", "WHERE COREL='" + cor + "'");

                    //#CKFK 20190415 Corrección del envío otra vez
                    dbld.add("INSERT INTO P_DEVOLUCIONES_SAP " +
                            " SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,'', D.LOTE, 'N', GETDATE(), D.CANT, 'N'" +
                            " FROM D_MOV E INNER JOIN D_MOVD D ON E.COREL = D.COREL" +
                            " WHERE E.COREL = '" + cor + "'" +
                            " UNION" +
                            " SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,D.BARRA, '', 'N', GETDATE(), 1, 'N'" +
                            " FROM D_MOV E INNER JOIN D_MOVDB D ON E.COREL = D.COREL" +
                            " WHERE E.COREL = '" + cor + "'");

                    if (commitSQL() == 1) {
                        sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
                        db.execSQL(sql);

                        sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
                        db.execSQL(sql);

                        pc += 1;

                    } else {
                        fterr += "\n" + sstr;
                    }

                } catch (Exception e) {
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    fterr += "\n" + e.getMessage();
                }

                DT.moveToNext();
            }

            vEnvio=true;

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = e.getMessage();
        }

        if (pc != pcc) {
            int pf = pcc - pc;
            senv += "Inventario : " + pc + " , NO ENVIADO : " + pf + " \n";
        } else {
            senv += "Inventario : " + pc + "\n";
        }

        return  vEnvio;

    }

    //#CKFK 20190522 Función creada para enviar los rating
    public boolean envio_Rating() {
        Cursor DT;
        String ruta, vendedor, comentario, fecha,ss;
        int id, idtranserror;
        float rating;

        boolean vEnvio=false;

        try {

            wsEtask.onProgressUpdate();

            sql = " SELECT IDRATING, RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM " +
                    " FROM D_RATING WHERE STATCOM='N'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) return true;

            dbld.clear();

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                id = DT.getInt(0);
                ruta = DT.getString(1);
                vendedor = DT.getString(2);
                rating = DT.getFloat(3);
                comentario = DT.getString(4);
                idtranserror=DT.getInt(5);
                fecha=DT.getString(6);

                try {

                    ss = "INSERT INTO D_RATING (RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM)"+
                            " VALUES('" + ruta +"','" + vendedor +"'," + rating +",'" + comentario +"'," +
                            "" + idtranserror +",'" + fecha +"','N')";
                    dbld.add(ss);

                } catch (Exception e) {
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    fterr += "\n" + e.getMessage();
                }

                DT.moveToNext();
            }

            if (commitSQL() == 1) {
                sql = "UPDATE D_RATING SET STATCOM='S' WHERE STATCOM='N'";
                db.execSQL(sql);
                vEnvio = true;
            }else{
                fterr += "\n" + sstr;
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = e.getMessage();
        }

        return vEnvio;

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

}
