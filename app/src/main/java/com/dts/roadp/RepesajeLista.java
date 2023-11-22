package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class RepesajeLista extends PBase {

    private ListView listView;
    private TextView lblProd,lblPres,lblCant,lblPrec,lblTot;

    private ArrayList<clsClasses.clsCD> items= new ArrayList<clsClasses.clsCD>();
    private ListAdaptRepesList adapter;

    private String prodid;
    private boolean esbarra;

    private AppMethods app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repesaje_lista);

        super.InitBase();
        addlog("RepesajeLista",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);

        lblProd= (TextView) findViewById(R.id.textView31);
        lblPres= (TextView) findViewById(R.id.textView42);
        lblCant= (TextView) findViewById(R.id.textView35);
        lblPrec= (TextView) findViewById(R.id.textView36);
        lblTot = (TextView) findViewById(R.id.textView37);

        prodid=gl.gstr;
        lblProd.setText(gl.gstr2);

        app = new AppMethods(this, gl, Con, db);
        esbarra=app.prodBarra(prodid);

        if (esbarra) lblPres.setText("Barra"); else lblPres.setText("Codigo");

        setHandlers();

        listItems();
    }


    //region Events

    public void repesaje(View view) {

        if (items.size()==0) {
            msgbox("No se puede realizar repesaje");return;
        }

        try{
            browse=1;
            startActivity(new Intent(this,Repesaje.class));
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void exit(View view) {
        finish();
    }

    private void setHandlers() {

        try{
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsCD vItem = (clsClasses.clsCD) lvObj;

                        adapter.setSelectedIndex(position);

                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox(e.getMessage());
                    }
                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Main

    private void listItems() {
        try{
            if (esbarra) listItemsBarra();else listItemsSingle();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void listItemsSingle() {
        Cursor dt;
        clsClasses.clsCD item;

        items.clear();

        try {
            sql = "SELECT PESO,TOTAL FROM T_VENTA WHERE PRODUCTO='"+prodid+"' ";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            item = clsCls.new clsCD();

            item.Cod = prodid;
            item.Desc = mu.frmdecimal(dt.getDouble(0),gl.peDecImp);
            item.Text = mu.frmdecimal(dt.getDouble(1),2);

            items.add(item);

            lblCant.setText("1");
            lblPrec.setText(mu.frmdecimal(dt.getDouble(0),gl.peDecImp));
            lblTot.setText(mu.frmdecimal(dt.getDouble(1),2));

            if(dt!=null) dt.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter=new ListAdaptRepesList(this, items);
        listView.setAdapter(adapter);
    }

    private void listItemsBarra() {
        Cursor dt;
        clsClasses.clsCD item;
        double ppeso,pprec,tpeso=0,tprec=0;

        items.clear();

        try {
            sql = "SELECT BARRA,PESO,PRECIO FROM T_BARRA WHERE CODIGO='"+prodid+"'";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            while (!dt.isAfterLast()) {

                item = clsCls.new clsCD();

                ppeso=mu.round(dt.getDouble(1),gl.peDecImp);tpeso+=ppeso;
                pprec=mu.round(dt.getDouble(2),2);tprec+=pprec;

                item.Cod = dt.getString(0);
                item.Desc = mu.frmdecimal(ppeso,gl.peDecImp);
                item.Text = mu.frmdecimal(pprec,2);

                items.add(item);

                dt.moveToNext();
            }

            if(dt!=null) dt.close();

            lblCant.setText(""+items.size());
            lblPrec.setText(mu.frmdecimal(tpeso,gl.peDecImp));
            lblTot.setText(mu.frmdecimal(tprec,2));

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter=new ListAdaptRepesList(this, items);
        listView.setAdapter(adapter);

    }

    //endregion

    //region Aux

    //endregion

    //region Activity Events

    @Override
    protected void onResume() {
        try{
            super.onResume();

            if (browse==1) {
                browse=0;
                listItems();return;
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

}