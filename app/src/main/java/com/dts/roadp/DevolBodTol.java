package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DevolBodTol extends PBase {

    private ListView listView;
    private TextView lblReg,lblTot;
    private ImageView imgNext;

    private ArrayList<clsClasses.clsExist> items= new ArrayList<clsClasses.clsExist>();
    private ListAdaptExist adapter;

    private clsRepBuilder rep;

    private int lns;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devol_bod_tol);

        super.InitBase();
        addlog("DevolBodTol",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        lblReg = (TextView) findViewById(R.id.textView61);lblReg.setText("");
        lblTot = (TextView) findViewById(R.id.textView70);lblTot.setText("");
        imgNext = (ImageView) findViewById(R.id.imgTitLogo);

        setHandlers();

        try {
            sql="DELETE FROM P_STOCK WHERE CANT+CANTM=0";
            db.execSQL(sql);
        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        rep=new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp, "");

        listItems();

        gl.closeVenta=false;
    }

    //region Events

    public void doNext(View view) {
        browse=1;
        startActivity(new Intent(DevolBodTol.this,DevolBodCan.class));
    }

    private void setHandlers() {

        try{
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsExist item = (clsClasses.clsExist) lvObj;

                        adapter.setSelectedIndex(position);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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
        Cursor dt, dp;
        clsClasses.clsExist item,itemm,itemt;
        String vF,pcod, cod, name, um, ump, sc, scm, sct="", sp, spm, spt="", sct_por_prod, spt_por_prod;
        double val, valm, valt, peso, pesot, val_tot_prod, peso_tot_prod;
        int icnt;

        items.clear(); valt=0;pesot=0;

        try {

            sql =  "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA " +
                   "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            sql += "GROUP BY P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA ";

            sql += "UNION ";
            sql += "SELECT P_STOCKB.CODIGO,P_PRODUCTO.DESCLARGA " +
                   "FROM P_STOCKB INNER JOIN P_PRODUCTO ON P_STOCKB.CODIGO=P_PRODUCTO.CODIGO ";
            sql += "GROUP BY P_STOCKB.CODIGO, P_PRODUCTO.DESCLARGA ";

            sql += "UNION ";
            sql += "SELECT P_STOCK_PALLET.CODIGO,P_PRODUCTO.DESCLARGA " +
                    "FROM P_STOCK_PALLET INNER JOIN P_PRODUCTO ON P_STOCK_PALLET.CODIGO=P_PRODUCTO.CODIGO ";
            sql += "GROUP BY P_STOCK_PALLET.CODIGO, P_PRODUCTO.DESCLARGA ";
            sql += "ORDER BY P_PRODUCTO.DESCLARGA";

            dp = Con.OpenDT(sql);

            if (dp.getCount() == 0) {
                adapter = new ListAdaptExist(this, items);
                listView.setAdapter(adapter);
                return;
            }

            lblReg.setText("Productos : " + dp.getCount());
            dp.moveToFirst();

            while (!dp.isAfterLast()) {

                pcod=dp.getString(0);

                sql="SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK.CANT) AS TOTAL,\n" +
                        " SUM(P_STOCK.CANTM),P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,\n" +
                        " P_STOCK.CENTRO,P_STOCK.STATUS,SUM(P_STOCK.PESO) \n" +
                        " FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO \n" +
                        " WHERE (P_PRODUCTO.CODIGO='"+pcod+"') \n" +
                        " GROUP BY P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS \n" +
                        " UNION\n" +
                        " SELECT P_STOCKB.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCKB.CANT) AS TOTAL,0 as CANTM,P_STOCKB.UNIDADMEDIDA,'' as LOTE,P_STOCKB.DOCUMENTO,P_STOCKB.CENTRO,P_STOCKB.STATUS,SUM(P_STOCKB.PESO) \n" +
                        " FROM P_STOCKB INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCKB.CODIGO \n" +
                        " WHERE (P_PRODUCTO.CODIGO='"+pcod+"')\n" +
                        " GROUP BY P_STOCKB.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCKB.UNIDADMEDIDA,P_STOCKB.DOCUMENTO,P_STOCKB.CENTRO,P_STOCKB.STATUS \n" +
                        " UNION\n" +
                        " SELECT P_STOCK_PALLET.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK_PALLET.CANT) AS TOTAL,0 as CANTM,P_STOCK_PALLET.UNIDADMEDIDA, P_STOCK_PALLET.LOTEPRODUCTO as LOTE,P_STOCK_PALLET.DOCUMENTO,P_STOCK_PALLET.CENTRO,P_STOCK_PALLET.STATUS,SUM(P_STOCK_PALLET.PESO) \n" +
                        " FROM P_STOCK_PALLET INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_PALLET.CODIGO \n" +
                        " WHERE (P_PRODUCTO.CODIGO='"+pcod+"') AND P_PRODUCTO.ES_PROD_BARRA = 0\n" +
                        " GROUP BY P_STOCK_PALLET.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK_PALLET.UNIDADMEDIDA,\n" +
                        " P_STOCK_PALLET.DOCUMENTO,P_STOCK_PALLET.CENTRO,P_STOCK_PALLET.STATUS,P_STOCK_PALLET.LOTEPRODUCTO \n" +
                        " UNION\n" +
                        " SELECT P_STOCK_PALLET.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK_PALLET.CANT) AS TOTAL,0 as CANTM,P_STOCK_PALLET.UNIDADMEDIDA, P_STOCK_PALLET.LOTEPRODUCTO as LOTE,P_STOCK_PALLET.DOCUMENTO,P_STOCK_PALLET.CENTRO,P_STOCK_PALLET.STATUS,SUM(P_STOCK_PALLET.PESO) \n" +
                        " FROM P_STOCK_PALLET INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_PALLET.CODIGO \n" +
                        " WHERE (P_PRODUCTO.CODIGO='"+pcod+"') AND P_PRODUCTO.ES_PROD_BARRA = 0 AND P_PRODUCTO.ES_VENDIBLE = 1\n" +
                        " GROUP BY P_STOCK_PALLET.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK_PALLET.UNIDADMEDIDA,\n" +
                        " P_STOCK_PALLET.DOCUMENTO,P_STOCK_PALLET.CENTRO,P_STOCK_PALLET.STATUS,P_STOCK_PALLET.LOTEPRODUCTO ";

                /*
                sql =  "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK.CANT) AS TOTAL,SUM(P_STOCK.CANTM),P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS,SUM(P_STOCK.PESO)  " +
                        "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  " +
                        "WHERE (P_PRODUCTO.CODIGO='"+pcod+"') " +
                        "GROUP BY P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS ";

                sql+=  "UNION ";
                sql+=  "SELECT P_STOCKB.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCKB.CANT) AS TOTAL, 0 AS Expr2,P_STOCKB.UNIDADMEDIDA, '' AS Expr4, P_STOCKB.DOCUMENTO,P_STOCKB.CENTRO,P_STOCKB.STATUS, SUM(P_STOCKB.PESO)  "+
                        "FROM  P_STOCKB INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCKB.CODIGO "+
                        "WHERE (P_PRODUCTO.CODIGO='"+pcod+"') " +
                        "GROUP BY P_STOCKB.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCKB.UNIDADMEDIDA,P_STOCKB.DOCUMENTO,P_STOCKB.CENTRO,P_STOCKB.STATUS "+
                        "ORDER BY TOTAL ";
                */

                dt = Con.OpenDT(sql);
                icnt=dt.getCount();
                if (icnt==1) {
                    dt.moveToFirst();
                    if (dt.getDouble(2)>0 && dt.getDouble(3)>0) icnt=2;
                }

                item = clsCls.new clsExist();
                item.Cod = pcod;
                item.Desc = dp.getString(1);
                item.flag = 0;
                item.items=icnt;
                items.add(item);

                val_tot_prod = 0;
                sct_por_prod = "";
                peso_tot_prod = 0;
                spt_por_prod = "";

                if (dt.getCount() > 0) dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    cod = dt.getString(0);
                    name = dt.getString(1);
                    val = dt.getDouble(2);
                    valm = dt.getDouble(3);
                    um = dt.getString(4);
                    peso =  dt.getDouble(9);

                    valt += val + valm;
                    val_tot_prod += val + valm;
                    pesot += peso ;
                    peso_tot_prod += peso ;

                    ump = gl.umpeso;
                    sp = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) sp = "";
                    spm = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) spm = "";
                    spt = mu.frmdecimal(pesot, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    spt_por_prod = mu.frmdecimal(peso_tot_prod, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) {spt = ""; spt_por_prod = "";}

                    sc = mu.frmdecimal(val, gl.peDecImp) + " " + rep.ltrim(um, 2);
                    scm = mu.frmdecimal(valm, gl.peDecImp) + " " + rep.ltrim(um, 2);
                    sct = mu.frmdecimal(valt, gl.peDecImp) + " " + rep.ltrim(um, 2);
                    sct_por_prod = mu.frmdecimal(val_tot_prod, gl.peDecImp) + " " + rep.ltrim(um, 2);

                    item = clsCls.new clsExist();
                    itemm = clsCls.new clsExist();

                    item.Cod = cod;itemm.Cod = cod;
                    item.Fecha = cod;itemm.Fecha = cod;
                    item.Desc = name;itemm.Desc = name;
                    item.cant = val;itemm.cant = val;
                    item.cantm = valm;itemm.cantm = valm;

                    item.Valor = sc;itemm.Valor = sc;
                    item.ValorM = scm;itemm.ValorM = scm;
                    item.ValorT = sct;itemm.ValorT = sct;

                    item.Peso = sp;itemm.Peso = sp;
                    item.PesoM = spm;itemm.PesoM = spm;
                    item.PesoT = spt;item.PesoT = spt;

                    item.Lote = dt.getString(5);
                    itemm.Lote = item.Lote;

                    if (val>0) {
                        item.flag = 1;
                        items.add(item);

                        if (valm > 0) {
                            icnt++;
                            itemm.flag = 2;
                            items.add(itemm);
                        }
                    } else {
                        item.flag = 2;
                        items.add(item);
                    }
                    dt.moveToNext();
                }

                if (icnt>1) {
                    itemt = clsCls.new clsExist();
                    itemt.ValorT = sct_por_prod;
                    itemt.PesoT = spt_por_prod;
                    itemt.flag = 3;
                    items.add(itemt);
                }

                dp.moveToNext();
            }

            if(dp!=null) dp.close();

            lblTot.setText("Cant : " + mu.frmdecno(valt)+" Peso : "+ mu.round(pesot,gl.peDec)+" "+gl.umpeso);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter = new ListAdaptExist(this, items);
        listView.setAdapter(adapter);

    }

    //endregion

    //region Activity Events

    @Override
    protected void onResume() {
        try{
            super.onResume();

            if (gl.closeDevBod){
                gl.closeDevBod=false;
                super.finish();
            }

            if (browse==1) {
                browse=0;
                if (gl.closeVenta) {
                    gl.closeVenta=false;
                    super.finish();
                }
                return;
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion
}
