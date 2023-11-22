package com.dts.roadp;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;

public class WebService {

    public Cursor openDTCursor;
    public String scalarValue;
    //public String openDTResult;
    public clsDataBuilder sqls;

    public String  error="";
    public Boolean errflag,status;

    // private

    private PBase parent;

    private ArrayList<String> results=new ArrayList<String>();


    private String sql,scalsql;
    private int mode;

    public String URL;

    // OpenDT
    private int odt_rows,odt_cols;

    private final String NAMESPACE ="http://tempuri.org/";
    private String METHOD_NAME;

    public WebService(PBase Parent, String Url) {
        parent=Parent;
        URL=Url;
        mode=0;

        sqls = new clsDataBuilder(Parent.getBaseContext());
    }

    public void openDT(String SQL) {
        mode=1;
        sql=SQL;
        execute();
    }

    public void commit() {
        mode=2;
        execute();
    }

    public void commitScalar(String SQL) {
        mode=3;
        scalsql=SQL;
        execute();
    }

    //region OpenDT

    private void processOpenDT() {
        String str;
        int rc;

        errflag = false;

        METHOD_NAME = "getDT";
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

            for (int i = 0; i < rc; i++) {
                str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                if (i==0) {
                    if (!str.equalsIgnoreCase("#")) throw new Exception(str);
                } else {
                    results.add(str);

                    if (i==1) odt_rows = Integer.parseInt(str);
                    if (i==2) odt_cols=Integer.parseInt(str);
                }
            }

            createCursor();

        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            createVoidCursor();
        }

    }

    private void createCursor() {
        String[] mRow = new String[odt_cols];
        MatrixCursor cursor = new MatrixCursor(mRow);
        int pos;
        String ss;

        try {
            createVoidCursor();
            if (odt_rows==0) return;

            pos=2;
            for (int i = 0; i <odt_rows; i++) {
                for (int j = 0; j <odt_cols; j++) {
                    try {
                        ss=results.get(pos);
                        if (ss.equalsIgnoreCase("anyType{}")) ss = "";
                        mRow[j]=ss;
                    } catch (Exception e) {
                        mRow[j]="";
                    }
                    pos++;
                }
                cursor.addRow(mRow);
            }

            int rc=cursor.getCount();

            openDTCursor=cursor;
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            createVoidCursor();
        }
    }

    private void createVoidCursor() {
        String[] mRow = new String[odt_cols];
        MatrixCursor cursor = new MatrixCursor(mRow);

        try {
            openDTCursor=cursor;
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
        }
    }

    //endregion

    //region Commit

    private int commitSQL() {
        String resp,ssql,ss;

        errflag = false;

        METHOD_NAME = "Commit";

        if (sqls.size()==0) return 1;

        ssql = "";
        for (int i = 0; i < sqls.size(); i++) {
            ss = sqls.items.get(i);
            ssql=ssql+ss+"\n";
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(ssql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            resp = response.toString();

            if (resp.equalsIgnoreCase("#")) {
                return 1;
            } else {
                errflag=true;
                error=resp;
                return 0;
            }
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            return 0;
        }

    }

    private int commitToScalar(String sql) {
        String resp,ssql,ss,str;
        int rc;

        errflag = false;

        scalarValue="";

        METHOD_NAME = "Commit";

        if (sqls.size()==0) return 1;

        ssql = "";
        for (int i = 0; i < sqls.size(); i++) {
            ss = sqls.items.get(i);
            ssql=ssql+ss+"\n";
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(ssql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            resp = response.toString();

            if (!resp.equalsIgnoreCase("#")) {
                errflag=true;
                error=resp;
                return 0;
            }


            METHOD_NAME = "getDT";
            results.clear();

            request = new SoapObject(NAMESPACE, METHOD_NAME);
            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(sql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc = resSoap.getPropertyCount() - 1;

            for (int i = 0; i < rc; i++) {
                str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                if (i == 0) {
                    if (!str.equalsIgnoreCase("#")) throw new Exception(str);
                } else {
                    results.add(str);

                    if (i == 1) odt_rows = Integer.parseInt(str);
                    if (i == 2) odt_cols = Integer.parseInt(str);
                    if (i == 3) scalarValue=str;

                }
            }

            return 1;
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            return 0;
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
            param.setName("Value");
            param.setValue("OK");

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL,60000);

            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            error = response.toString() + "..";

            return 1;
        } catch (Exception e) {
            error = e.getMessage();
        }

        return 0;
    }

    //endregion

    //region WebService Core

    private void execute() {
        AsyncCallWS wstask = new AsyncCallWS();
        wstask.execute();
    }

    public void wsExecute(){

        errflag=false;status=false;error="";

        try {
            switch (mode) {
                case 1:
                    processOpenDT();break;
                case 2:
                    if (commitSQL()==0) throw new Exception(error);
                    break;
                case 3:
                    if (commitToScalar(scalsql)==0) throw new Exception(error);
                    break;
            }

            status=errflag;
        } catch (Exception e) {
            error=e.getMessage();
            status=false;
        }
    }

    public void wsFinished()  {
        status=!errflag;
        //
        try {
            parent.wsCallBack(mode,errflag,error);
        } catch (Exception e) {
        }
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                wsExecute();
            } catch (Exception e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsFinished();
            } catch (Exception e) {
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

    //endregion

}
