package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class BonBarra extends PBase {

    private EditText txtBarra;
    private TextView lblProd;

    private String bcode,bonid,prodname,barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bon_barra);

        super.InitBase();

        lblProd=(TextView) findViewById(R.id.textView45);
        txtBarra=(EditText) findViewById(R.id.editText8);

        bonid=gl.bonbarid;

        productoNombre();
        lblProd.setText("Escanea un codigo de barra de :\n\n"+prodname);
        txtBarra.requestFocus();txtBarra.setText("");

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction()==KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            bcode=txtBarra.getText().toString().trim();
            barcodeBolsa();
        }
        return super.dispatchKeyEvent(e);
    }

    //region Events

    public void doMissing(View view) {
        msgAsk("Reportar faltante de bonificacion");
    }

    public void doFocus(View view) {
        txtBarra.requestFocus();txtBarra.setText("");
    }

    //endregion

    //region Main

    private void barcodeBolsa() {
        Cursor dt;
        String bon,barra;
        double peso;

        try {
            sql="SELECT Barra,Peso,Codigo FROM P_STOCKB WHERE BARRA='"+bcode+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()==0) {
                msgbox("La barra no existe " + bcode + " ");
                txtBarra.setText("");txtBarra.requestFocus();return;
            } else {
                dt.moveToFirst();
            }

            barra=dt.getString(0);
            peso=dt.getDouble(1);
            bon=dt.getString(2);

            if(dt!=null) dt.close();

            txtBarra.setText("");txtBarra.requestFocus();

            if (!bon.equalsIgnoreCase(bonid)) {
                msgbox("El producto escaneado no es \n"+prodname);return;
            }

            switch (barraVenta(barra)) {
                 case 1:
                     msgbox("Barra es parte de venta");return;
                case -1:
                    return;
            }

            switch (barraBonif(barra)) {
                 case 1:
                    msgbox("Barra es parte de una bonificacion");return;
                case -1:
                    return;
            }

            ins.init("T_BARRA_BONIF");

            ins.add("BARRA",barra);
            ins.add("CODIGO",bonid);
            ins.add("PRECIO",0);
            ins.add("PESO",peso);
            ins.add("PESOORIG",peso);
            ins.add("PRODUCTO",gl.bonbarprod);

            db.execSQL(ins.sql());

            barcode=barra;

            reportBonif();
            finish();

        } catch (Exception e) {
            //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            //msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void registerMissing() {

        try {
            ins.init("T_BONIFFALT");

            ins.add("PRODID", bonid);
            ins.add("PRODUCTO",gl.bonbarprod);
            ins.add("CANT", 1);

            db.execSQL(ins.sql());

        } catch (Exception ee) {

            try {
                sql="UPDATE T_BONIFFALT SET CANT=CANT+1 WHERE (PRODID='"+bonid+"') AND (PRODUCTO='"+gl.bonbarprod+"')";
                db.execSQL(sql);
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            }
        }

        toastlong("Se reporto faltante de bonificación.");
        reportBonif();

        finish();

    }

    //endregion

    //region Dialogs

    private void msgAsk(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg  + " ?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    msgAsk2("Está seguro");
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void msgAsk2(String msg) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg + " ?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    registerMissing();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    //endregion

    //region Aux

    private void productoNombre() {
        Cursor dt;
        String nom;

        try {
            sql="SELECT DESCLARGA FROM P_PRODUCTO WHERE CODIGO='"+bonid+"'";
            dt=Con.OpenDT(sql);

            dt.moveToFirst();
            nom=dt.getString(0);

            if(dt!=null) dt.close();

            prodname=bonid+" -"+nom;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            prodname=bonid;
        }
    }

    private int barraVenta(String barra) {
        int rslt;

        try {
            sql="SELECT BARRA FROM T_BARRA WHERE BARRA='"+barra+"'";
            Cursor dt=Con.OpenDT(sql);

            if(dt!=null) dt.close();

            if (dt.getCount()>0) rslt=1 ;else rslt= 0;

            return rslt;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return -1;
        }
    }

    private int barraBonif(String barra) {
        int rslt;

        try {
            sql="SELECT BARRA FROM T_BARRA_BONIF WHERE BARRA='"+barra+"'";
            Cursor dt=Con.OpenDT(sql);

           if(dt!=null) dt.close();

            if (dt.getCount()>0) rslt=1 ;else rslt= 0;

            return rslt;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return -1;
        }
    }

    private int cantBon() {
        int rslt;

        try {
            sql="SELECT BARRA FROM T_BARRA_BONIF WHERE PRODUCTO='"+gl.bonbarprod+"'";
            Cursor dt=Con.OpenDT(sql);

            rslt=dt.getCount();
            if(dt!=null) dt.close();

            return rslt;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return -1;
        }
    }

    private int cantFalt() {
        int rslt;

        try {
            sql="SELECT PRODID FROM T_BONIFFALT WHERE PRODUCTO='"+gl.bonbarprod+"'";
            Cursor dt=Con.OpenDT(sql);

            rslt=dt.getCount();
            if(dt!=null) dt.close();

            return rslt;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return 0;
        }
    }

    private void reportBonif() {
        int bont,bon,bonf;

        bon=cantBon();
        bonf=cantFalt();
        bont=bon+bonf;

        toast(barcode);

        if (bonf==0) {
            toast("Bonificado : "+bon);
        } else {
            toast("Bonificado : "+bon+" / "+bont);
        }


    }

    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        msgbox("No está permitido salir de la pantalla.\nDebe escanear la barra o reportar faltante");
    }

    //endregion

}
