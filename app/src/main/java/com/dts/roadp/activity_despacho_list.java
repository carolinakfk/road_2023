package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class activity_despacho_list extends PBase {

    private ListView listView;
    private TextView lblCantReg, txtClienteDespacho;
    private LA_Ds_pedido adapter;
    private clsDs_pedidoObj Ds_pedidoObj;

    private ArrayList<String> lcodeM = new ArrayList<String>();
    private ArrayList<String> lnameM = new ArrayList<String>();
    private AlertDialog.Builder mMenuDlg;

    private String clinom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despacho_list);

        super.InitBase();

        listView = (ListView) findViewById(R.id.lvDespacho);
        lblCantReg = (TextView) findViewById(R.id.lblCant2);
        txtClienteDespacho = (TextView) findViewById(R.id.txtClienteDespacho);

        Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);

        setHandlers();

        showCredit();

        listaModificacion();
        listItems();

        gl.modpedid="";

    }

//region Events

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsDs_pedido item = (clsClasses.clsDs_pedido)lvObj;

                adapter.setSelectedIndex(position);

                gl.iddespacho=item.corel;
                gl.cliente = item.cliente;
                gl.pedCorel=item.add1;
                gl.rutaPedido = item.add2;

                iniciaVenta();

            };
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                selid = 0;

                if (position >= 0) {
                    Object lvObj = listView.getItemAtPosition(position);
                    clsClasses.clsDs_pedido item = (clsClasses.clsDs_pedido) lvObj;

                    adapter.setSelectedIndex(position);

                    gl.iddespacho = item.corel;
                    gl.pedCorel = item.add1;
                    activity_despacho_list.this.msgAskNoDespachar("Quiere cancelar la entrega de la prefactura " + gl.iddespacho);

                }

                return true;
            }
        });

    }

//endregion

//region Main

    private void listItems() {

        gl.iddespacho="";
        gl.pedCorel="";
        gl.rutaPedido = "";

        try {
            Ds_pedidoObj.fill("WHERE (CLIENTE='"+gl.cliente+"') AND (BANDERA='N')");

            for (int i = 0; i <Ds_pedidoObj.count; i++) {
                nombreCliente(Ds_pedidoObj.items.get(i).cliente);
            }

            adapter=new LA_Ds_pedido(this ,this,Ds_pedidoObj.items);
            listView.setAdapter(adapter);
            lblCantReg.setText("Total: " + Ds_pedidoObj.count);
            txtClienteDespacho.setText(clinom);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void iniciaVenta() {
        Cursor DT;
        Float cantidad;
        gl.rutatipo="D";
        cantidad = Float.valueOf(CantExistencias());

        if(cantidad == 0){
            if (gl.rutatipo.equalsIgnoreCase("D")) {
                msgbox("No hay existencias disponibles, no podrá realizar las prefacturas");
            }
        }else{
            gl.closeCliDet = false;
            gl.closeVenta = false;
            gl.credito=0;
            gl.banderaCobro = false;
            gl.rutatipo="V";

            try {

                sql="SELECT NOMBRE,NOMBRE_PROPIETARIO,DIRECCION,ULTVISITA,TELEFONO,LIMITECREDITO,NIVELPRECIO,PERCEPCION,TIPO_CONTRIBUYENTE, " +
                        "COORX,COORY,MEDIAPAGO,NIT,VALIDACREDITO,BODEGA,CHEQUEPOST,TIPO,DIACREDITO "+
                        "FROM P_CLIENTE WHERE CODIGO='"+gl.cliente+"'";
                DT=Con.OpenDT(sql);
                DT.moveToFirst();

                gl.nivel=DT.getInt(6);
                gl.percepcion=DT.getDouble(7);
                gl.contrib=""+DT.getString(8);;
                gl.media=DT.getInt(11);
                gl.fnombre=DT.getString(0);
                gl.fnit=DT.getString(12);
                gl.fdir=DT.getString(2);
                gl.vcredito = DT.getString(13).equalsIgnoreCase("S");
                gl.vcheque = DT.getString(14).equalsIgnoreCase("S");
                gl.vchequepost = DT.getString(15).equalsIgnoreCase("S");
                gl.clitipo = DT.getString(16);

                startActivity(new Intent(activity_despacho_list.this,Venta.class));
                finish();
            } catch (Exception e) {
                msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            }
        }
    }

    public float CantExistencias() {
        Cursor DT;
        float cantidad=0,cantb=0;

        try {

            sql = "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.CANT,P_STOCK.CANTM,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            if (Con != null){
                DT = Con.OpenDT(sql);
                cantidad = DT.getCount();
            }else {
                cantidad = 0;
            }

            sql = "SELECT BARRA FROM P_STOCKB";
            if (Con != null){
                DT = Con.OpenDT(sql);
                cantb = DT.getCount();

                if(DT!=null) DT.close();
            }else {
                cantb = 0;
            }

            cantidad=cantidad+cantb;
        } catch (Exception e) {
            return 0;
        }

        return cantidad;
    }

    private void listaModificacion(){
        Cursor DT;
        String code,name;

        lcodeM.clear();lnameM.clear();

        try {

            sql="SELECT IDRAZON, DESCRIPCION FROM P_RAZON_DESP_INCOMP ORDER BY DESCRIPCION";

            DT=Con.OpenDT(sql);
            if (DT.getCount()==0) {return;}

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                try {
                    code=DT.getString(0);
                    name=DT.getString(1);

                    lcodeM.add(code);
                    lnameM.add(name);
                } catch (Exception e) {
                    mu.msgbox(e.getMessage());
                }
                DT.moveToNext();
            }

            if(DT!=null) DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox( e.getMessage());return;
        }

    }

    public void showNoDespDialog() {
        try{
            final AlertDialog Dialog;

            final String[] selitems = new String[lnameM.size()];
            for (int i = 0; i < lnameM.size(); i++) {
                selitems[i] = lnameM.get(i);
            }

            mMenuDlg = new AlertDialog.Builder(this);
            mMenuDlg.setTitle("Razón de modificación");
            mMenuDlg.setCancelable(false);

            mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    try {
                        String s=lcodeM.get(item);
                        setModificacion(s);
                    } catch (Exception e) {
                    }
                }
            });

            mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            Dialog = mMenuDlg.create();
            Dialog.show();

            Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
            nbutton.setTextColor(Color.WHITE);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void setModificacion(String scna){

        try
        {

            if (scna=="" || scna.isEmpty()){
                msgbox("Seleccione una razón de modificación");
            }else{
                gl.coddespacho=gl.iddespacho;

                clsClasses.clsDs_pedidod item;
                clsClasses.clsDs_pedido iteme;

                clsDs_pedidodObj Ds_pedidodObj=new clsDs_pedidodObj(this,Con,db);
                Ds_pedidodObj.fill("WHERE COREL='"+gl.coddespacho+"'");

                clsDs_pedidoObj Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);
                Ds_pedidoObj.fill("WHERE COREL='"+gl.coddespacho+"'");

                iteme=Ds_pedidoObj.items.get(0);

                for (int i = 0; i <Ds_pedidodObj.count; i++) {

                    item=Ds_pedidodObj.items.get(i);

                    ins.init("D_DESPACHOD_NO_ENTREGADO");
                    ins.add("COREL",item.corel);
                    ins.add("ANULADO",item.anulado);
                    ins.add("PRODUCTO",item.producto);
                    ins.add("CANTSOLICITADA",item.cant);
                    ins.add("UMVENTASOLICITADA",item.umventa);
                    ins.add("PESOSOLICITADO",item.peso);
                    ins.add("CANTENTREGADA",0);
                    ins.add("UMVENTAENTREGADA","");
                    ins.add("PESOENTREGADO",0);
                    ins.add("IDRAZON",scna);
                    ins.add("STATCOM","N");

                    db.execSQL(ins.sql());

                }

                iteme.bandera = "S";
                Ds_pedidoObj.updateBandera(iteme);

                listItems();

            }

        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("Error : " + e.getMessage());
        }
    }

    private void msgAskNoDespachar(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg  + " ?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   showNoDespDialog();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void showCredit(){
        try{
            if (hasCredits()){
                Intent intent = new Intent(this,Cobro.class);
                startActivity(intent);
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private boolean hasCredits(){
        Cursor DT;

        try {
            sql="SELECT SALDO FROM P_COBRO WHERE CLIENTE='"+gl.cliente+"'";
            DT=Con.OpenDT(sql);

            boolean rslt=DT.getCount()>0;
            if(DT!=null) DT.close();

            if (rslt) return true;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        return false;
    }

//endregion

//region Aux

    private void nombreCliente(String cliid) {
        Cursor dt;

        clinom="";
        try {
            sql="SELECT Nombre FROM P_CLIENTE WHERE Codigo='"+cliid+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                clinom=dt.getString(0);
            }

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    public void cancelaNotaCredito() {
        try {
            db.beginTransaction();

            db.execSQL("DELETE FROM D_CxC WHERE COREL='"+gl.devcord+"'");
            db.execSQL("DELETE FROM D_CxCD WHERE COREL='"+gl.devcord+"'");

            db.execSQL("DELETE FROM D_NOTACRED WHERE COREL='"+gl.devcornc+"'");
            db.execSQL("DELETE FROM D_NOTACREDD WHERE COREL='"+gl.devcornc+"'");

            db.setTransactionSuccessful();
            db.endTransaction();

            finish();
        } catch (Exception e) {
            db.endTransaction();
            msgbox(e.getMessage());
        }
   }

//endregion

//region Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Devolución");
        dialog.setMessage(msg);

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelaNotaCredito();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

//endregion

//region Activity Events

    @Override
    protected void onResume() {
        super.onResume();
        try {
           Ds_pedidoObj.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (gl.devtotal>0) {
            msgAskExit("Está seguro de abandonar la venta? No se podrá aplicar la nota de crédito y se elminará la devolución");
        } else {
            super.onBackPressed();
        }
    }

//endregion

}