package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListaPedidos extends PBase {

    private ListView listView;
    private TextView lblTipo,lblCant,lblTotal;

    private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
    private ListAdaptCFDV adapter;

    private AppMethods app;

    private String itemid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);

        super.InitBase();

        listView = (ListView) findViewById(R.id.listView1);
        lblTipo= (TextView) findViewById(R.id.lblDescrip);lblTipo.setText(gl.fnombre);
        lblCant = (TextView) findViewById(R.id.textView88);
        lblTotal= (TextView) findViewById(R.id.textView9);

        setHandlers();
        listItems();

        gl.modpedid="";
    }

    //region Events

    public void nuevoPedido(View view){
        gl.modpedid="";
        iniciaPedido();
    }

    private void setHandlers(){
        try{

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

                        itemid=vItem.Cod;
                        adapter.setSelectedIndex(position);

                        msgAsk("Modificar pedido existente");
                    } catch (Exception e) {
                        mu.msgbox( e.getMessage());
                    }
                };
            });

        } catch (Exception e){}

    }

    //endregion

    //region Main

    public void listItems() {
        Cursor DT;
        clsClasses.clsCFDV vItem;
        int pcant=0;
        long f;
        double val,ptot=0;
        String sf,sval;

        items.clear();

        try {
            sql="SELECT D_PEDIDO.COREL,P_CLIENTE.NOMBRE,D_PEDIDO.FECHA_SISTEMA,D_PEDIDO.TOTAL "+
                "FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO "+
                "WHERE (CLIENTE='"+gl.cliente+"') AND (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') " +
                "ORDER BY D_PEDIDO.COREL DESC ";
            DT=Con.OpenDT(sql);

            pcant=DT.getCount();
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    vItem =clsCls.new clsCFDV();

                    vItem.Cod=DT.getString(0);
                    vItem.Desc=DT.getString(0);
                    f=DT.getLong(2);sf=du.sfecha(f)+" "+du.shora(f);
                    vItem.Fecha=sf;
                    val=DT.getDouble(3);ptot+=val;
                    try {
                        sval=mu.frmcur(val);
                    } catch (Exception e) {
                        sval=""+val;
                    }

                    vItem.Valor=sval;

                    items.add(vItem);

                    DT.moveToNext();
                }
            }

            if(DT!=null) DT.close();
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }

        adapter=new ListAdaptCFDV(this, items);
        listView.setAdapter(adapter);

        lblCant.setText("Pedidos : "+pcant);
        lblTotal.setText("Total : "+mu.frmcur(ptot));
    }

    private void iniciaPedido() {
        startActivity(new Intent(this,Venta.class));
        finish();
    }

    //endregion

    //region Aux

    private void msgAsk(String msg) {

        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("ROAD");
            dialog.setMessage("¿" + msg  + "?");

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    gl.modpedid=itemid;
                    iniciaPedido();
                }
            });

            dialog.setNegativeButton("No", null);
            dialog.show();
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

}