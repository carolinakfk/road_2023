package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class Soporte extends PBase {

    private int tipo;
    private String email,subj,body,fname;
    private File file;
    private Uri uri;
    private ArrayList<Uri> uris = new ArrayList<Uri>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);

        super.InitBase();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        subj="Road :: "+gl.empnom+" "+du.sfecha(fecha)+" "+du.shora(fecha);
        body="Ruta :"+gl.rutanom+"\nVendedor : "+gl.vendnom;
    }

    //region Events

    public void doBaseDatos(View view) {
        tipo=1;
        msgAsk("Enviar la base de datos");
    }

    public void doCarga(View view) {
        tipo=2;
        msgAsk("Enviar archivo de carga");
    }

    public void doEnvio(View view) {
        tipo=3;
        msgAsk("Enviar archivo de envio");
     }

    public void doError(View view) {
        tipo=4;
        msgAsk("Enviar archivo de error de envio");
    }

    public void doBitacora(View view) {
        tipo=5;
        msgAsk("Enviar archivo de bitacora");
     }

    //endregion

    //region Main

    private void send(String msg) {
        Intent intent;

        try {
            if (!file.exists()) {
                msgbox(msg);return;
            }

            uri = Uri.fromFile(file);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "dtsolutionsgt@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subj);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            //startActivity(Intent.createChooser(emailIntent, null));
            startActivity(emailIntent);

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void sendDBase() {
        body="Base de datos \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/road.db";
        file=new File(fname);

        send("No se pudo enviar la base de datos datos");
    }

    private void sendCarga() {
        body="Carga \n"+body;

       fname= Environment.getExternalStorageDirectory()+"/roadcarga.txt";
       file=new File(fname);

        send("El archivo de carga no existe. Debe realizar una carga de datos");
    }

    private void sendEnvio() {
        body="Envio \n"+body;

        fname= Environment.getExternalStorageDirectory()+"/roadenvio.txt";
        file=new File(fname);

        send("El archivo de envio no existe. Debe realizar un envio de datos");
    }

    private void sendEnvioPorDia() {
        body="Envio \n"+body;
        int contfile=0;

        Intent intent;

        try {

           Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            String to= "dtsolutionsgt@gmail.com";
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subj);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            for (int i=0;i<8;i++){

                fname= Environment.getExternalStorageDirectory()+"/roadenvio"+(i==0?"":i)+".txt";
                file=new File(fname);

                if(file.exists()){
                    contfile++;
                    uri = Uri.fromFile(file);
                    uris.add(uri);
                }
            }

            emailIntent.putExtra(Intent.EXTRA_STREAM,  uris);

            if (contfile>0) {
                //startActivity(emailIntent);
                startActivity(Intent.createChooser(emailIntent , "Send with..."));
            }else{
                msgbox("No hay archivos de envío de datos");
            }


        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    private void sendError() {
        body="Error de envío \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/roaderror.txt";
        file=new File(fname);

        send("El archivo de detalle de error de envío no existe");
    }

    private void sendBitacora() {
        body="Bitacora \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/roadlog.txt";
        file=new File(fname);

        send("El archivo de bitacora no existe");
    }

    //endregion

    //region Aux

    private void msgAsk(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Road Soporte");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                switch (tipo) {
                    case 1:
                        sendDBase();break;
                    case 2:
                        sendCarga();break;
                    case 3:
                        sendEnvio();break;
                    case 4:
                        sendError();break;
                    case 5:
                        sendBitacora();break;
                 }
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

    //endregion

}
