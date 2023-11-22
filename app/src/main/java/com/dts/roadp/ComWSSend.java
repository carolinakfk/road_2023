package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.epson.eposdevice.commbox.SendDataListener;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class ComWSSend extends PBase {

    private TextView lbl1;
    private RelativeLayout relSend;
    private ProgressBar pbar;

    private WebService ws;
    private clsDataBuilder dbld;

    private final String NAMESPACE = "http://tempuri.org/";
    private String METHOD_NAME;
    private String URL="";
    private String URL_Remota="";
    private int pcount=0;
    private String sstr="";
    private int contador=0;
    private int scon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wssend);

        super.InitBase();

        lbl1 = (TextView) findViewById(R.id.textView72);lbl1.setText("Enviando . . .");
        pbar = (ProgressBar) findViewById(R.id.progressBar5);
        relSend = (RelativeLayout) findViewById(R.id.relSend);relSend.setVisibility(View.VISIBLE);

        dbld = new clsDataBuilder(this);

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                initSession();
            }
        };
        mtimer.postDelayed(mrunner,500);

    }

    //region Events

    public void doSend(View view) {
        //initSession();
    }

    //endregion

    //region Main

    private void initSession() {
        Cursor dt;

        if (!getWSURL()) return;

        try {

            sql="SELECT COREL FROM D_PEDIDO WHERE STATCOM='N'";
            dt=Con.OpenDT(sql);
            pcount = dt.getCount();

            sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
            dt=Con.OpenDT(sql);
            pcount+=dt.getCount();

            if (pcount==0) toast("No existen pedidos ni clientes nuevos pendientes de envio.");
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgExit(e.getMessage());
        }

        if (pcount==0) {
            relSend.setVisibility(View.INVISIBLE);
            finish();return;
        }

        lbl1.setText("Enviando : "+pcount+" registros . . .");

        contador = 0;

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                sendData();
            }
        };
        mtimer.postDelayed(mrunner,200);

    }

    private void sendData() {
        Cursor dt;
        String cor;
        scon = 0;

        try {

            ws = new WebService(ComWSSend.this, URL);
            dbld.clear();

            sql = "SELECT COREL FROM D_PEDIDO WHERE STATCOM='N'";
            dt = Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    cor = dt.getString(0);
                    dbld.insert("D_PEDIDO", "WHERE COREL='" + cor + "'");
                    dbld.insert("D_PEDIDOD", "WHERE COREL='" + cor + "'");

                    dt.moveToNext();
                }
            }

            sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
            dt = Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    cor = dt.getString(0);
                    dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");

                    dt.moveToNext();
                }
            }

            ws.sqls.clear();
            for (int i = 0; i <dbld.items.size(); i++) {
                ws.sqls.add(dbld.items.get(i));
            }

            ws.commit();

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            msgExit(e.getMessage());return;
        }

    }


    //endregion

    //region Web Service

    @Override
    public void wsCallBack(int callmode,Boolean throwing,String errmsg) {
        String ss;

        try {
            super.wsCallBack(callmode,throwing, errmsg);

            try {
                db.beginTransaction();

                sql = "UPDATE D_PEDIDO SET STATCOM='S' WHERE STATCOM='N'";
                db.execSQL(sql);

                sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE STATCOM='N'";
                db.execSQL(sql);

                db.setTransactionSuccessful();
                db.endTransaction();
                relSend.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                relSend.setVisibility(View.INVISIBLE);
                db.endTransaction();
                msgbox(e.getMessage());return;
            }

            toast("Registros enviados : "+pcount);
            finish();
        } catch (Exception e) {
            if (contador==0){
                contador=1;URL=URL_Remota;
                sendData();
            } else {
                msgExit("Error de envio : " + e.getMessage());
            }
        }
    }

    //endregion

    //region Aux

    public boolean getWSURL() {
        Cursor dt;

        try {

            sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0){
                dt.moveToFirst();

                URL = dt.getString(0);
                URL_Remota = dt.getString(1);

                if (URL.isEmpty()){
                    URL=URL_Remota;
                }

                if (URL.isEmpty()){
                    toast("No existe configuración para transferencia de datos");
                    return false;
                }

            }

            return true;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());return false;
        }

    }

    //endregion

    //region Dialogs

    private void msgExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Envío de pedidos");
        dialog.setMessage(msg+"\nURL:"+URL);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                relSend.setVisibility(View.INVISIBLE);
                finish();
            }
        });

        dialog.show();
    }

    //endregion

    //region Activity Events


    //endregion

}
