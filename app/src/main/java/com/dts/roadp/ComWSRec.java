package com.dts.roadp;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.ByteArrayInputStream;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.io.StringWriter;
        import java.lang.reflect.Field;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.net.URLConnection;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;

        import org.ksoap2.SoapEnvelope;
        import org.ksoap2.serialization.PropertyInfo;
        import org.ksoap2.serialization.SoapObject;
        import org.ksoap2.serialization.SoapPrimitive;
        import org.ksoap2.serialization.SoapSerializationEnvelope;
        import org.ksoap2.transport.HttpTransportSE;
        import org.w3c.dom.Document;
        import org.w3c.dom.Element;
        import org.w3c.dom.Node;
        import org.w3c.dom.NodeList;

        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.Looper;
        import android.os.SystemClock;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;
        import javax.xml.transform.OutputKeys;
        import javax.xml.transform.Transformer;
        import javax.xml.transform.TransformerFactory;
        import javax.xml.transform.dom.DOMSource;
        import javax.xml.transform.stream.StreamResult;

public class ComWSRec extends PBase {

    private TextView lblInfo,lblParam;
    private ProgressBar barInfo;
    private EditText txtRuta,txtWS,txtEmp;

    private int isbusy,reccnt,conflag;
    private String ruta, docstock, xmlresult, argstr,esql,ftmsg;

    private SQLiteDatabase dbT;
    private BaseDatos ConT;
    private BaseDatos.Insert insT;
    private AppMethods clsAppM;
    private boolean showprogress,pedidos, ftflag;

    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<String> results=new ArrayList<String>();
    private ArrayList<String> listDocs = new ArrayList<>();

    // Web Service

    public AsyncCallRec wsRtask;
    public ComWSRec.AsyncCallConfirm wsCtask;

    private static String sstr,fstr,fprog,ferr,idbg,dbg;
    private int scon;
    private String gEmpresa;

    private final String NAMESPACE ="http://tempuri.org/";
    private String METHOD_NAME,URL;

    private clsDataBuilder dbld;

    private String nombretabla;
    private int indicetabla;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wsrec);

        super.InitBase();
        addlog("ComWSPrec",""+du.getActDateTime(),gl.vend);

        System.setProperty("line.separator","\r\n");

        lblInfo= (TextView) findViewById(R.id.lblETipo);
        lblParam= (TextView) findViewById(R.id.lblProd);
        barInfo= (ProgressBar) findViewById(R.id.progressBar2);
        txtRuta= (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
        txtWS= (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
        txtEmp= (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);

        clsAppM = new AppMethods(this, gl, Con, db);

        isbusy=0;

        lblInfo.setText("");lblParam.setText("");
        barInfo.setVisibility(View.INVISIBLE);

        ruta=gl.ruta;
        gEmpresa=gl.emp;

        gl.isOnWifi = clsAppM.isOnWifi();

        getWSURL();

        dbld=new clsDataBuilder(this);

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                runRecep();
            }
        };
        mtimer.postDelayed(mrunner,1000);

    }

    // Events

    public void askRec(View view)
    {

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Recarga del inventario");
            dialog.setMessage("¿Iniciar recarga del inventario?");

            dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
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

    // Main

    private void runRecep() {

        try{
            if (isbusy==1) return;

            if (!setComParams()) return;

            isbusy=1;

            barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
            lblInfo.setText("Conectando ...");

            wsRtask = new AsyncCallRec();
            wsRtask.execute();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    // Web Service Methods

    public int fillTable(String value,String delcmd) {
        int rc;
        String s,ss;

        METHOD_NAME = "getIns";
        sstr="OK";

        try {

            idbg=idbg+" filltable ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");param.setValue(value);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapObject resSoap =(SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc=resSoap.getPropertyCount()-1;
            idbg=idbg+" rec " +rc +"  ";

            s="";

            // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
            // 	idbg=idbg+" RC ="+rc+"---";
            //}

            for (int i = 0; i < rc; i++) {

                String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
                //s=s+str+"\n";

                if (i==0) {

                    idbg=idbg+" ret " +str +"  ";

                    if (str.equalsIgnoreCase("#")) {
                        listItems.add(delcmd);
                    } else {
                        idbg=idbg+str;
                        sstr=str;return 0;
                    }
                } else {
                    try {
                        sql=str;
                        listItems.add(sql);
                        sstr=str;
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                        sstr=e.getMessage();
                    }
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            idbg=idbg+" ERR "+e.getMessage();
            return 0;
        }
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
            param.setName("SQL");param.setValue(sql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapObject resSoap =(SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc=resSoap.getPropertyCount()-1;

            for (int i = 0; i < rc+1; i++)
            {
                String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);

                if (i==0) {
                    sstr=str;
                    if (!str.equalsIgnoreCase("#")) {
                        sstr=str;
                        return 0;
                    }
                } else {
                    results.add(str);
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            sstr=e.getMessage();
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

    // WEB SERVICE - RECEPCION

    private boolean getData(){
        Cursor DT;
        int rc,prn,jj;
        String s,val="";
        boolean TieneRuta = false;
        boolean TieneClientes = false;
        boolean TieneProd = false;

        try {

            sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                val=DT.getString(0);
            }

            DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            val="N";
        }

        if (val.equalsIgnoreCase("S"))gl.peStockItf=true; else gl.peStockItf=false;


        listItems.clear();
        idbg="";

        try {

            sql = "SELECT CODIGO FROM P_RUTA";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                TieneRuta = DT.getCount() > 0;
            }

            DT.close();

            sql = "SELECT CODIGO FROM P_CLIENTE";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                TieneClientes = DT.getCount() > 0;
            }

            DT.close();


            sql = "SELECT CODIGO FROM P_PRODUCTO";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                TieneProd = DT.getCount() > 0;
            }

            DT.close();

            if(TieneRuta && TieneProd && TieneClientes){
                if (!AddTable_SinBorrar("P_STOCK")) return false;
                if (!AddTable_SinBorrar("P_STOCK_PALLET")) return false;
                if (!AddTable_SinBorrar("P_STOCKB")) return false;
                if (!AddTable("TMP_PRECESPEC")) return false;
                if (!AddTable("P_PRODPRECIO")) return false;
                if (!AddTable("P_FACTORCONV")) return false;
            }else{
                msgbox("No tiene datos de la ruta, clientes y productos, debe hacer una carga de datos completa");
                return false;
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            return false;
        }

        ferr="";

        try {

            rc=listItems.size();reccnt=rc;
            if (rc==0) return true;

            fprog="Procesando ...";
            wsRtask.onProgressUpdate();

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase =dbT;
            insT=ConT.Ins;

            dbT.beginTransaction();

            prn=0;jj=0;

            for (int i = 0; i < rc; i++)
            {
                sql=listItems.get(i);

                try
                {
                    dbT = ConT.getWritableDatabase();
                    dbT.execSQL(sql);
                }
                catch (Exception e)
                {
                    Log.d("M","Something happend there " + e.getMessage());
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage() + "EJC","Yo fui " + sql);
                    Log.e("z", e.getMessage());
                }


                try {
                    if (i % 10==0)
                    {
                        fprog = "Procesando: " + i + " de: " + (rc-1);
                        wsRtask.onProgressUpdate();
                        SystemClock.sleep(20);
                    }
                } catch (Exception e)
                {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                    Log.e("z", e.getMessage());
                }
            }

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            fprog="Registrando el documento recibido de inventario en BOF...";
            wsRtask.onProgressUpdate();

            Actualiza_Documentos();

            fprog="Estandarizando el inventario...";
            wsRtask.onProgressUpdate();

            clsAppM.estandartInventario();

            fprog="Fin de actualización";
            wsRtask.onProgressUpdate();

        } catch (Exception e) {

            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);

            try {
                ConT.close();
            } catch (Exception ee) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            }

            sstr=e.getMessage();
            ferr=sstr+"\n"+sql;

            return false;
        }

        try {
            ConT.close();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return true;
    }

    private String fterr;

    public int commitSQL() {
        int rc;
        String s,ss;

        METHOD_NAME = "Commit";
        sstr="OK";

        if (dbld.size()==0) return 1;

        s="";
        for (int i = 0; i < dbld.size(); i++) {
            ss=dbld.items.get(i);
            s=s+ss+"\n";
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");param.setValue(s);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            sstr = s;
            return 0;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            sstr=e.getMessage();
        }

        return 0;
    }

    private boolean AddTable(String TN) {
        String SQL;

        try {

            fprog=TN;idbg=TN;
            wsRtask.onProgressUpdate();

            SQL=getTableSQL(TN);
            if (fillTable(SQL,"DELETE FROM "+TN+"")==1) {
                idbg=idbg +SQL+"#"+"PASS OK";
                return true;
            } else {
                idbg=idbg +SQL+"#"+" PASS FAIL  ";
                fstr=sstr;
                return false;
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
            return false;
        }
    }

    private boolean AddTable_SinBorrar(String TN) {
        String SQL;

        try {

            fprog=TN;idbg=TN;
            wsRtask.onProgressUpdate();

            SQL=getTableSQL(TN);
            if (fillTable(SQL,"DELETE FROM "+TN+" WHERE 1 = 0 ")==1) {
                idbg=idbg +SQL+"#"+"PASS OK";
                return true;
            } else {
                idbg=idbg +SQL+"#"+" PASS FAIL  ";
                fstr=sstr;
                return false;
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
            return false;
        }
    }

    //region WS Recepcion por tabla

    private class WSRec extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                wsCargaTabla();
            } catch (Exception e) {
                if (scon == 0) fstr = "No se puede conectar al web service : " + sstr;
                Log.d("onPostExecute", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsCallback();
            } catch (Exception e) {
                Log.d("onPostExecute", e.getMessage());
            }

        }

        @Override
        protected void onPreExecute() {  }

        @Override
        protected void onProgressUpdate(Void... values) { }

    }

    public void wsCargaTabla() {
        try {
//        	if (nombretabla.contains("P_IMPRESORA")){
//				fillTableImpresora();
//			}else{
            AddTable(nombretabla);
//			}
        } catch (Exception e) {
            String ee=e.getMessage();
        }
    }

    public void wsCallback() {
        boolean ejecutar=true;

        try {
            indicetabla++;

            switch (indicetabla) {
                case 0:
                    procesaRuta();
                    nombretabla ="";
                    break;
                case 1:
                    nombretabla="P_STOCK";break;
                case 2:
                    nombretabla="P_STOCK_PALLET";break;
                case 3:
                    nombretabla="P_STOCKB";break;
                case 4:
                    nombretabla="TMP_PRECESPEC";break;
                case 5:
                    nombretabla="P_PRODPRECIO";break;
                case 6:
                    nombretabla="P_FACTORCONV";break;
                case 7://#CKFK 20210813 Cambié esto para el final
                    nombretabla="Procesando tablas ...";break;
                case 8:
                    procesaDatos();
                    ejecutar = false;
                    break;

            }

            if (ejecutar) executaTabla();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void executaTabla() {
        try {
            lblInfo.setText(nombretabla);
            ComWSRec.WSRec wsrec = new ComWSRec.WSRec();
            wsrec.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void procesaRuta() {
        Cursor dt;
        String sql, val = "";
        int ival, rc;

        try {

            rc = listItems.size();
            reccnt = rc;
            if (rc == 0) return;

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase = dbT;
            insT = ConT.Ins;

            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {

                sql = listItems.get(i);
                esql = sql;

                dbT.execSQL(sql);

                try {
                    if (i % 10 == 0) SystemClock.sleep(20);
                } catch (Exception e) {}
            }

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            try {
                sql = "SELECT VENTA FROM P_RUTA";
                dt = Con.OpenDT(sql);
                dt.moveToFirst();
                val = dt.getString(0);

                if (dt != null) dt.close();

            } catch (Exception e) {
                val = "V";
            }
            gl.rutatipo = val;

        } catch (Exception e) {
            try {
                ConT.close();
            } catch (Exception ee) {
            }
        }

    }

    private boolean procesaDatos() {
        Cursor DT;
        BufferedWriter writer = null;
        FileWriter wfile;
        int rc, scomp, prn, jj;
        int ejecutarhh = 0;
        String s, val = "";

        ferr = "";
        lblInfo.setText("Procesando tablas . . .");

        try {

            rc = listItems.size();
            reccnt = rc;
            if (rc == 0) return true;

            try {
                String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
                wfile = new FileWriter(fname, false);
                writer = new BufferedWriter(wfile);
            } catch (IOException e) {}


            fprog = "Procesando ...";
            wsRtask.onProgressUpdate();

            ConT=new BaseDatos(this);
            dbT=ConT.getWritableDatabase();
            ConT.vDatabase = dbT;
            insT = ConT.Ins;

            prn = 0;jj = 0;

            try{

                for (int i = 0; i < rc; i++) {

                    sql = listItems.get(i);
                    esql = sql;
                    sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
                    sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
                    sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

                    try {
                        writer.write(sql);writer.write("\r\n");
                    } catch (Exception e) {
                    }
                }

                writer.close();

            } catch (Exception ex){}


            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {

                sql = listItems.get(i);esql = sql;
                sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
                sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
                sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

                try {
                    dbT = ConT.getWritableDatabase();
                    dbT.execSQL(sql);
                } catch (Exception e) {
                    ferr += " " +e.getMessage();
                }

                try {
                    if (i % 10 == 0) {
                        fprog = "Procesando: " + i + " de: " + (rc - 1);
                        wsRtask.onProgressUpdate();SystemClock.sleep(20);
                    }
                } catch (Exception e) {
                    ferr += " " +e.getMessage();
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                }
            }

            try {
                writer.close();
            } catch (Exception e) {
                String ss=e.getMessage();
            }

            fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
            wsRtask.onProgressUpdate();

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            fprog = "Documento de inventario recibido en BOF...";
            wsRtask.onProgressUpdate();

            Actualiza_Documentos();

            fprog = "Fin de actualización";wsRtask.onProgressUpdate();

            scomp = 1;

            try {
                ConT.close();
            } catch (Exception e) {
                //addlog(new Object() {	}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            }

            lblInfo.setText(" ");
            s = "Recepción completa.";

            try {
                Cursor dt1 = Con.OpenDT(sql);
                sql = "SELECT VENTA FROM P_RUTA";
                dt1 = Con.OpenDT(sql);
                dt1.moveToFirst();
                val = dt1.getString(0);

                if (dt1 != null) dt1.close();

            } catch (Exception e) {
                val = "V";
            }

           sql = "SELECT Codigo FROM P_STOCK UNION SELECT Codigo FROM P_STOCKB ";

           Cursor dt = Con.OpenDT(sql);
           if (dt.getCount() > 0) s = s + "\nSe actualizó inventario.";

            clsAppM.estandartInventario();
            clsAppM.estandartInventarioPedido();
            validaDatos(true);

           // if (stockflag == 1) sendConfirm();
            isbusy = 0;

            isbusy = 0;

            //msgAskExit(s);

            barInfo.setVisibility(View.INVISIBLE);
            lblParam.setVisibility(View.INVISIBLE);

            return true;

        } catch (Exception e) {
            fprog = "Actualización incompleta";
            wsRtask.onProgressUpdate();

            try {
                ConT.close();
            } catch (Exception ee) {
                sstr = e.getMessage();ferr += " " + sstr;
            }

            sstr = e.getMessage();ferr += " " + sstr + "\n" + sql;esql = sql;
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), ferr, esql);

            return false;

        }

    }

    private boolean validaDatos(boolean completo) {
        Cursor dt;

        try {

            pedidos=gl.rutatipo.equals("P");

            if (!gl.rutatipo.equalsIgnoreCase("P") && !gl.rutatipo.equalsIgnoreCase("C")) {
                sql = "SELECT RESOL FROM P_COREL";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    if (!pedidos) {
                        toastlong("No está definido correlativo de facturas");return false;
                    }
                }
            }

            sql = "SELECT Codigo FROM P_CLIENTE";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                toastlong("Lista de clientes está vacia");
                return false;
            }

            sql = "SELECT Ruta FROM P_CLIRUTA";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                toastlong("Lista de clientes por ruta está vacia");
                return false;
            }

            sql = "SELECT Codigo FROM P_PRODUCTO";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                toastlong("Lista de productos está vacia");
                return false;
            }

            if (completo) {

                sql = "SELECT Nivel FROM P_PRODPRECIO ";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    msgbox("Lista de precios está vacia");
                    return false;
                }

                sql = "SELECT Producto FROM P_FACTORCONV ";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    toastlong("Lista de conversiones está vacia");
                    return false;
                }

                if (pedidos) {
                    sql = "SELECT Codigo FROM P_STOCK_PV ";
                } else {
                    sql = "SELECT Codigo FROM P_STOCK UNION SELECT Codigo FROM P_STOCKB";
                }

                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    if (!gl.rutatipo.equals("C")){
                        toastlong("La de carga inventario de productos está vacia");
                        return false;
                    }
                }

            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            Log.d("ValidaDatos", e.getMessage());
        }

        return true;
    }

    //endregion

    public int commitSQL_otro() {
        int rc;
        String s, ss="";
        //#CKFK 20190429 Creé esta variable para retornar si la comunicación fue correcta o no
        //e hice modificaciones en la función para garantizar esta funcionalidad
        int vCommit=0;

        METHOD_NAME = "Commit";
        sstr = "OK";

        if (dbld.size() == 0) vCommit =1;//return 1

        s = "";
        for (int i = 0; i < dbld.size(); i++) {
            ss = dbld.items.get(i);
            s = s + ss + "\n";
        }

        s=s.replace("&","&amp;");
        s=s.replace("\"", "&quot;");
        s=s.replace("'","&apos;");
        s=s.replace("<", "&lt;");
        s=s.replace(">", "&gt;");

        nombretabla = "commitSQL";
        vCommit=fillTable2(s,"commitSQL");

        return vCommit;
    }

    public int fillTable2(String value, String delcmd) {
        int rc,retFillTable = 0;
        String str, ss, tabla;
        String[] sitems;

        String xr;

        try {
            sstr = "OK";

           if (nombretabla.contains("commitSQL")){

                callMethod("Commit", "SQL", value);
                xr=getXMLRegionSingle("CommitResult");
                xr=(String) getSingle(xr,"CommitResult",String.class);

            }else{

                value=value.replace("&", "&amp;");
                value=value.replace("\"", "&quot;");
                value=value.replace("'", "&apos;");
                value=value.replace("<", "&lt;");
                value=value.replace(">", "&gt;");

                callMethod("getIns", "SQL", value);
                xr=getXMLRegionSingle("getInsResult");
            }

            sitems=xr.split("\n");
            rc=sitems.length;

            s = "";

                       if (!delcmd.contains("commitSQL")){
                tabla=delcmd.substring(12);
                switch (tabla){

                    case "P_PRODPRECIO":
                        if (rc==1){
                            borraDatos();
                            throw new Exception("No hay precios definidos para los productos de esta ruta:" + ruta + ", no se puede continuar la carga de datos");
                        }
                        break;
                }

                for (int i=1; i < rc-2; i++) {

                    try {
                        ss=sitems[i];
                        ss=ss.replace("<string>","");
                        str=ss.replace("</string>","");
                        str=str.replace("&amp;", "&");
                        str=str.replace("&quot;", "\"");
                        str=str.replace("&apos;", "'");
                        str=str.replace("&lt;", "<");
                        str=str.replace("&gt;", ">");
                    } catch (Exception e) {
                        str="";
                    }

                    if (i == 1) {

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
                            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                            sstr = e.getMessage();
                            return 0;
                        }
                    }
                }
            } else if (delcmd.contains("commitSQL")){
                //Corregir esto
                str=xr;
                if (str.equalsIgnoreCase("#")) {
                    listItems.add(delcmd);
                } else {
                    idbg = idbg + str;
                    ftmsg = ftmsg + "\n" + str;
                    ftflag = true;
                    sstr = str;
                    return 0;
                }
            }

            retFillTable= 1;

        } catch (Exception e) {
            sstr = e.getMessage();
            idbg = idbg + " ERR " + e.getMessage();
            retFillTable= 0;
        }

        return  retFillTable;
    }


    private boolean borraDatos() {

        try {

            db.beginTransaction();

            sql = "DELETE FROM P_RUTA";
            db.execSQL(sql);
            sql = "DELETE FROM P_PRODUCTO";
            db.execSQL(sql);
            sql = "DELETE FROM P_COREL";
            db.execSQL(sql);
            sql = "DELETE FROM P_PARAMEXT";
            db.execSQL(sql);
            sql = "DELETE FROM P_PRODPRECIO";
            db.execSQL(sql);
            sql = "DELETE FROM P_CLIENTE";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLException e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            //mu.msgbox("Error : " + e.getMessage());
            return false;
        }

        return true;
    }

    public String getXMLRegionSingle(String nodename) throws Exception {
        String st,ss,sv,en,sxml;
        Node xmlnode;

        try {

            InputStream istream = new ByteArrayInputStream( xmlresult.getBytes() );
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(istream);

            Element root=doc.getDocumentElement();

            NodeList children=root.getChildNodes();
            Node bodyroot=children.item(0);
            NodeList body=bodyroot.getChildNodes();
            Node responseroot=body.item(0);
            NodeList response=responseroot.getChildNodes();

            ss="";
            for(int i =0;i<response.getLength();i++) {
                ss+=response.item(i).getNodeName()+",\n";

                if (response.item(i).getNodeName().equalsIgnoreCase(nodename)) {
                    xmlnode=response.item(i);
                    sxml=nodeToString(xmlnode);
                    return sxml;
                }
            }
        } catch (Exception e) {
            throw new Exception(" XMLObject getXMLRegion : "+ e.getMessage());
        }
        return "";
    }

    private String nodeToString(Node node)  throws Exception {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (Exception te) {
            throw new Exception("XMLObject nodeToString : "+te.getMessage());
        }
        return sw.toString();
    }

    public Object getSingle( String body, String name, Class<?> cl)  throws Exception {

        int start = body.indexOf("<" + name + ">");
        if (start>-1)  start += name.length() + 2;else start=0;//with <and > char
        int end = body.indexOf("</" + name + ">");
        if (end == -1) body = "";else body = body.substring(start, end);

        String gname = cl.getName();

        if (cl.getName().toLowerCase().contains("string")) {
            return body;
        }
        if (cl.getName().toLowerCase().contains("double")) {
            if (body.isEmpty()) return 0; else return
                    Double.parseDouble(body);
        }
        if (cl.getName().toLowerCase().contains("int")) {
            if (body.isEmpty()) return 0; else return
                    Integer.parseInt(body);
        }

        if (cl.getName().toLowerCase().contains("boolean")) {
            return Boolean.parseBoolean(body);
        }

        return null;
    }

    public void callMethod(String methodName, Object... args) throws Exception {
        int mTimeOut=5000;
        String mResult,line="";
        java.net.URL mUrl = new URL(URL);

        try{
            mResult = "";xmlresult="";

            URLConnection conn = mUrl.openConnection();
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.addRequestProperty("SOAPAction", "http://tempuri.org/" + methodName);

            //#EJC 20200601: Set Timeout
            conn.setConnectTimeout(mTimeOut);
            conn.setReadTimeout(mTimeOut);

            conn.setDoOutput(true);

            OutputStream ostream = conn.getOutputStream();

            OutputStreamWriter wr = new OutputStreamWriter(ostream);

            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:" +
                    "xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:" +
                    "soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soap:Body>" +
                    "<" + methodName + " xmlns=\"http://tempuri.org/\">";

            body += buildArgs(args);
            body += "</" + methodName + ">" +
                    "</soap:Body>" +
                    "</soap:Envelope>";
            wr.write(body);
            wr.flush();

            int responsecode = ((HttpURLConnection) conn).getResponseCode();

            //#EJC20200702:Capturar excepcion de SQL (No se sabe el error pero sabemos que no se proceso)
            if (responsecode==500) {
                throw new Exception("Error 500: Esto es poco usual pero algún problema ocurrió del lado del motor de BD al ejecutar sentencia SQL: \n" +
                        "\n" + args[1].toString());
            }else if (responsecode!=299 && responsecode!=404) {

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) mResult += line;
                rd.close();rd.close();

                mResult=mResult.replace("ñ","n");
                xmlresult=mResult;

            } if (responsecode==299) {

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) mResult += line;
                rd.close();rd.close();

                mResult=mResult.replace("ñ","n");
                xmlresult=mResult;

                throw new Exception("Error al procesar la solicitud :\n " );

            } if (responsecode==404) {
                throw new Exception("Error 404: No se obtuvo acceso a: \n" + mUrl.toURI() +
                        "\n" + "Verifique que el WS Existe y es accesible desde el explorador.");
            }

        } catch (Exception e) {
            sstr=e.getMessage();
            throw new Exception(sstr);
        }
    }

    private String buildArgs(Object... args) throws IllegalArgumentException, IllegalAccessException    {
        String result = "";
        String argName = "";
        String valor = "";

        for (int i = 0; i < args.length; i++)   {
            if (i % 2 == 0) {
                argName = args[i].toString();
            } else {
                result += "<" + argName + ">";
                argstr = result;

                result += buildArgValue(args[i]);
                argstr = result;
                result += "</" + argName + ">";
                argstr = result;
            }
        }
        return result;
    }

    private String buildArgValue(Object obj) throws IllegalArgumentException, IllegalAccessException   {

        Class<?> cl = null;

        try  {
            cl = obj.getClass();
        } catch (Exception e) {
            return "";
        }

        String result = "";

        if (cl.isPrimitive()) return obj.toString();
        if (cl.getName().contains("java.lang.")) return obj.toString();
        if (cl.getName().equals("java.util.Date"))  {
            DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            return dfm.format((Date) obj);
        }

        if (cl.isArray())  {
            String xmlName = cl.getName().substring(cl.getName().lastIndexOf(".") + 1);
            xmlName = xmlName.replace(";", "");
            Object[] arr = (Object[]) obj;

            for (int i = 0; i < arr.length; i++) {
                result += "<" + xmlName + ">";
                result += buildArgValue(arr[i]);
                result += "</" + xmlName + ">";
            }

            return result;
        }

        Field[] fields = cl.getDeclaredFields();

        for (int i = 0; i < fields.length - 1; i++) {
            result += "<" + fields[i].getName() + ">";
            result += buildArgValue(fields[i].get(obj));
            result += "</" + fields[i].getName() + ">";
        }

        return result;
    }

    private String getTableSQL(String TN) {

        String SQL="";
        String fsql,fsqli,fsqlf;

        fsql=du.univfechasql(du.getActDate());
        fsqli=du.univfechasql(du.ffecha00(du.getActDate()))+" 00:00:00";
        fsqlf=du.univfechasql(du.ffecha24(du.getActDate()))+" 23:59:59";

        try{

            if (TN.equalsIgnoreCase("P_STOCK")) {

                if (gl.peModal.equalsIgnoreCase("TOL")) {
                    SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
                          "STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
                          "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND  (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
                          "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)" +
                          "AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
                          "AND (FECHA<='" + fsqlf + "'))";
                } else if (gl.peModal.equalsIgnoreCase("APR")) {
                    SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
                          "STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
                          "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
                } else {
                    SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
                          "STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
                          "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
                }

                return SQL;
            }

            //CKFK 20190222 Agregué a la consulta el AND (ENVIADO = 0)
            if (TN.equalsIgnoreCase("P_STOCKB")) {
                SQL = "SELECT RUTA, BARRA, CODIGO, CANT, COREL, PRECIO, PESO, DOCUMENTO,dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
                      "STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, DOC_ENTREGA " +
                      "FROM P_STOCKB WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
                      "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) " +
                      "AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
                        "AND (FECHA<='" + fsqlf + "'))";
                return SQL;
            }

            //CKFK 20190304 Agregué la consulta para obtener los datos de P_STOCK_PALLET
            if (TN.equalsIgnoreCase("P_STOCK_PALLET")) {
                SQL = "SELECT DOCUMENTO, RUTA, BARRAPALLET, CODIGO, BARRAPRODUCTO, LOTEPRODUCTO, CANT, COREL, PRECIO, PESO, " +
                      "UNIDADMEDIDA,dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, DOC_ENTREGA  " +
                      "FROM P_STOCK_PALLET WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
                      "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) " +
                      "AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
                        "AND (FECHA<='" + fsqlf + "'))";
                return SQL;
            }

            if (TN.equalsIgnoreCase("P_FACTORCONV")) {
                //#EJC20181112
                //SQL = "SELECT PRODUCTO,UNIDADSUPERIOR,FACTORCONVERSION,UNIDADMINIMA FROM P_FACTORCONV ";
                SQL = " SELECT * FROM P_FACTORCONV WHERE PRODUCTO IN (SELECT CODIGO " +
                      " FROM P_PRODUCTO WHERE LINEA IN (SELECT DISTINCT LINEA FROM P_LINEARUTA " +
                      " WHERE RUTA = '" + gl.ruta + "')) " +
                      " OR ((PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + gl.ruta + "') " +
                      " OR PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + gl.ruta + "')))";

                return SQL;
            }

            if (TN.equalsIgnoreCase("P_PRODPRECIO")) {

                SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO " +
                      " WHERE ( (CODIGO IN ( SELECT CODIGO FROM P_PRODUCTO WHERE " +
                      "(LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE RUTA='" + gl.ruta + "')))) " +
                      "OR  (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + gl.ruta + "'))) " +
                      "AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE " +
                      "WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + gl.ruta + "'))) ";
                return SQL;
            }

            if (TN.equalsIgnoreCase("TMP_PRECESPEC")) {
                SQL = "SELECT CODIGO,VALOR,PRODUCTO,PRECIO,UNIDADMEDIDA FROM TMP_PRECESPEC " +
                      " WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
                return SQL;
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
        return SQL;
    }

    //#EJC20181120: Inserta los documentos que bajaron a la HH
    private boolean Actualiza_Documentos()
    {

        DateUtils DU = new DateUtils();
        long Now=du.getFechaActual();

        String ruta = txtRuta.getText().toString().trim();

        try{

            String SQL = " INSERT INTO P_DOC_ENVIADOS_HH " +
                    " SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
                    " FROM P_STOCK WHERE FECHA = '" +  Now + "' AND RUTA = '" + ruta + "' " +
                    " AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
                    " UNION " +
                    " SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
                    " FROM P_STOCKB WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
                    " AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
                    " UNION " +
                    " SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
                    " FROM P_STOCK_PALLET WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
                    " AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)";

            dbld.clear();
            dbld.add(SQL);

            if (commitSQL()==1)
            {
                return  true;
            } else
            {
                fterr+="\n"+sstr;
                dbg=sstr;
                return  false;
            }

        }catch (Exception e)
        {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            Log.e("Error",e.getMessage());
            return  false;
        }

    }

    //region "Web Service handling Methods"
    public void wsExecute(){

        fstr="No connect";scon=0;

        try {

            if (getTest()==1) {
                scon=1;
            } else {
            }

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
        try{
            if (fstr.equalsIgnoreCase("Sync OK")) {
                lblInfo.setText(" ");
                s="Actualización completa.";
                toastcent(s);
                super.finish();
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
                if (scon==0){
                    fstr="No se puede conectar al web service : "+sstr;
                    //lblInfo.setText(fstr);
                }
                msgbox(fstr);
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

    //region WS Confirm Handling Methods

    public void wsConfirmExecute() {
        String univdate = du.univfecha(du.getActDate());
        isbusy = 1;

        try {
            conflag = 0;

            dbld.clear();

            if (listDocs.size()>0){
                for (int i = 0; i < listDocs.size(); i++) {
                    docstock = listDocs.get(i);
                    dbld.add("DELETE FROM P_DOC_ENVIADOS_HH WHERE DOCUMENTO='" + docstock + "'");
                    dbld.add("INSERT INTO P_DOC_ENVIADOS_HH VALUES ('" + docstock + "','" + ruta + "','" + univdate + "',1)");
                }
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fterr += "\n" + e.getMessage();
            dbg = e.getMessage();
        }
    }

    public void wsConfirmFinished() {
        try {
            isbusy = 0;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private class AsyncCallConfirm extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                wsConfirmExecute();
            } catch (Exception e) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsConfirmFinished();
            } catch (Exception e) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            }

        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
            } catch (Exception e) {
            }
        }

    }

    //endregion

    //endregion

    //region "Aux"
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

            //URL=wsurl;
            if (URL!=null && !URL.equalsIgnoreCase("")){
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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }


        return true;
    }
    //endregion

    //region "Activity Events"

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
