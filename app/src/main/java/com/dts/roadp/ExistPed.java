package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class ExistPed extends PBase {

    private ListView listView;
    private EditText txtFilter;
    private TextView lblReg;

    private ArrayList<clsClasses.clsExist> items= new ArrayList<clsClasses.clsExist>();
    private ListAdaptExistPed adapter;
    private clsClasses.clsExist selitem;

    private clsRepBuilder rep;

    private int tipo,lns, cantExistencia;
    private String itemid;

    private clsDocExist doc;
    private printer prn;
    private Runnable printclose;

    private AppMethods app;
    public double SumaPeso;
    public double SumaCantidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exist_ped);

        super.InitBase();

        tipo=((appGlobals) vApp).tipo;

        app = new AppMethods(this, gl, Con, db);
        gl.validimp=app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        listView = (ListView) findViewById(R.id.listView1);
        txtFilter = (EditText) findViewById(R.id.txtMonto);
        lblReg = (TextView) findViewById(R.id.textView1);lblReg.setText("");

        setHandlers();

        rep=new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp, "");

        listItems();

        printclose= new Runnable() {
            public void run() {
                finish();
            }
        };

        prn=new printer(this,printclose,gl.validimp);
        doc=new clsDocExist(this,prn.prw,"");

        float cant = CantExistencias();
        doc.SumaPeso = getPeso();
        doc.SumaCant = getCantidad();

        Toast.makeText(this, "Cantidad : " + (int)cant, Toast.LENGTH_SHORT).show();
    }


    //region Events

    public void printDoc(View view) {
        try{
            if(items.size()==0){
                msgbox("No hay inventario disponible");
                return;
            }

            if (doc.buildPrint("0",0)) prn.printask();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void limpiaFiltro(View view) {
        try{
            txtFilter.setText("");
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

    //region Main

    private void setHandlers(){

        try{

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsExist item = (clsClasses.clsExist)lvObj;

                        itemid=item.Cod;
                        adapter.setSelectedIndex(position);
                    } catch (Exception e) {
                        mu.msgbox( e.getMessage());
                    }
                };
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsExist item = (clsClasses.clsExist) lvObj;

                        adapter.setSelectedIndex(position);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }
                    return true;
                }
            });

            txtFilter.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {}

                public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

                public void onTextChanged(CharSequence s, int start,int before, int count) {
                    int tl;

                    tl=txtFilter.getText().toString().length();

                    if (tl==0 || tl>1) listItems();
                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public float CantExistencias() {
        Cursor DT;
        float cantidad=0;

        try {

            sql = "SELECT P_STOCK_PV.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK_PV.CANT,0,P_STOCK_PV.UNIDADMEDIDA " +
                    "FROM P_STOCK_PV INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_PV.CODIGO  WHERE 1=1 ";
            if (Con != null){
                DT = Con.OpenDT(sql);
                cantidad = DT.getCount();
            }else {
                cantidad = 0;
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return 0;
        }

        return cantidad;
    }

    public double getPeso() {
        Cursor DT;
        double sumaPesoB=0,sumaPeso=0;

        sql = "SELECT IFNULL(SUM(PESO),0) AS PESOTOT " +
                " FROM P_STOCK_PV ";
        DT=Con.OpenDT(sql);

        if (DT!=null) {
            if (DT.getCount() > 0) {
                DT.moveToFirst();
                sumaPeso = DT.getDouble(0);
            }
        }

        if (DT!=null) DT.close();

        return sumaPeso;
    }

    public double getCantidad() {
        Cursor DT;
        double sumaCant=0;

        sql = "SELECT IFNULL(SUM(S.CANT),0) AS CANTUNI " +
                " FROM P_STOCK_PV S ";
        DT=Con.OpenDT(sql);

        if (DT!=null){

            if (DT.getCount()>0) {
                DT.moveToFirst();
                sumaCant=DT.getDouble(0);
            }

        }

        if(DT!=null) DT.close();

        return sumaCant;
    }

    private void listItems() {
        Cursor dt, dp;
        clsClasses.clsExist item,itemm,itemt;
        String vF,pcod, cod, name, um, ump, sc, scm, sct="", sp, spm, spt="";
        double val, valm, valt, peso, pesot;
        int icnt;

        items.clear();lblReg.setText(" ( 0 ) ");

        vF = txtFilter.getText().toString().replace("'", "");

        try {

            sql ="SELECT P_STOCK_PV.CODIGO,P_PRODUCTO.DESCLARGA " +
                 "FROM P_STOCK_PV INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_PV.CODIGO  WHERE 1=1 ";
            if (vF.length() > 0) sql = sql + "AND ((P_PRODUCTO.DESCLARGA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
            sql+="GROUP BY P_STOCK_PV.CODIGO,P_PRODUCTO.DESCLARGA ";
            sql+="ORDER BY P_STOCK_PV.CODIGO";

            dp = Con.OpenDT(sql);

            if (dp.getCount() == 0) {
                adapter = new ListAdaptExistPed(this, items);
                listView.setAdapter(adapter);
                return;
            }

            lblReg.setText(" ( " + dp.getCount() + " ) ");
            dp.moveToFirst();


            while (!dp.isAfterLast()) {

                pcod=dp.getString(0);
                valt=0;pesot=0;

                sql =  "SELECT P_STOCK_PV.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK_PV.CANT) AS TOTAL,0,P_STOCK_PV.UNIDADMEDIDA,'' AS GLOTE,'' AS GDOCUMENTO,'' AS GCENTRO,0 AS GSTATUS, SUM(P_STOCK_PV.PESO), P_STOCK_PV.ESTADO   " +
                        "FROM P_STOCK_PV INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_PV.CODIGO  " +
                        "WHERE (P_PRODUCTO.CODIGO='"+pcod+"') " +
                        "GROUP BY P_STOCK_PV.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK_PV.UNIDADMEDIDA,GLOTE,GDOCUMENTO,GCENTRO,GSTATUS ";

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

                //if (dt.getCount() == 0) return;

                if (dt.getCount()>0) dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    cod = dt.getString(0);
                    name = dt.getString(1);
                    val = dt.getDouble(2);
                    valm = 0;
                    um = dt.getString(4);
                    peso =  dt.getDouble(9);

                    valt += val + valm;
                    pesot += peso ;

                    ump = gl.umpeso;
                    sp = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) sp = "";
                    spm = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) spm = "";
                    spt = mu.frmdecimal(pesot, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) spt = "";

                    sc = mu.frmdecimal(val, gl.peDecImp) + " " + rep.ltrim(um, 3);
                    scm = mu.frmdecimal(valm, gl.peDecImp) + " " + rep.ltrim(um, 3);
                    sct = mu.frmdecimal(valt, gl.peDecImp) + " " + rep.ltrim(um, 3);

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

                    item.Lote = dt.getString(5);//if (mu.emptystr(item.Lote)) item.Lote =cod;
                    itemm.Lote = item.Lote;
                    item.Doc = dt.getString(6);itemm.Doc = dt.getString(6);
                    item.Centro = dt.getString(7);itemm.Centro = dt.getString(7);
                    item.Stat = dt.getString(8);itemm.Stat = dt.getString(8);

                    item.Estado=dt.getString(10);

                    if (val>=0) {
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
                    itemt.ValorT = sct;
                    itemt.PesoT = spt;
                    itemt.flag = 3;
                    items.add(itemt);
                }

                dp.moveToNext();
            }

            if(dp!=null) dp.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter = new ListAdaptExistPed(this, items);
        listView.setAdapter(adapter);

    }

    private void listItemsOld() {
        Cursor DT;
        clsClasses.clsExist item;
        String vF, cod, name, um, ump, sc, scm, sct, sp, spm, spt;
        double val, valm, valt,peso,pesom,pesot;

        items.clear();

        vF = txtFilter.getText().toString().replace("'", "");

        try {

            //vSQL="SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK.CANT),SUM(P_STOCK.CANTM) "+
            //     "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            //if (vF.length()>0) vSQL=vSQL+"AND ((P_PRODUCTO.DESCLARGA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
            //vSQL+="GROUP BY P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA ORDER BY P_PRODUCTO.DESCLARGA";

            sql = "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.CANT,P_STOCK.CANTM,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            if (vF.length() > 0)
                sql = sql + "AND ((P_PRODUCTO.DESCLARGA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
            sql += "ORDER BY P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA";

            DT = Con.OpenDT(sql);

            lblReg.setText(" ( " + DT.getCount() + " ) ");

            if (DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                cod = DT.getString(0);
                name = DT.getString(1);
                val = DT.getDouble(2);
                valm = DT.getDouble(3);
                um = DT.getString(4);
                peso=0;
                pesom=0;

                valt=val+valm;
                pesot=peso+pesom;

                ump = "";
                sp = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                if (!gl.usarpeso) sp = "";
                spm = mu.frmdecimal(pesom, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                if (!gl.usarpeso) spm = "";
                spt = mu.frmdecimal(pesot, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                if (!gl.usarpeso) spt = "";

                sc = mu.frmdecimal(val, gl.peDecImp) + " " + rep.ltrim(um, 6);
                scm = mu.frmdecimal(valm, gl.peDecImp) + " " + rep.ltrim(um, 6);
                sct = mu.frmdecimal(valt, gl.peDecImp) + " " + rep.ltrim(um, 6);

                item = clsCls.new clsExist();

                item.Cod = cod;
                item.Fecha = cod;
                item.Desc = name;
                item.cant = val;
                item.cantm = valm;

                item.Valor = sc;
                item.ValorM = scm;
                item.ValorT = sct;

                item.Peso = sp;
                item.PesoM = spm;
                item.PesoT = spt;

                item.Lote = DT.getString(5);
                item.Doc = DT.getString(6);
                item.Centro = DT.getString(7);
                item.Stat = DT.getString(8);

                if (valm == 0) item.flag = 0;
                else item.flag = 1;

                items.add(item);

                DT.moveToNext();
            }

            if(DT!=null) DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter = new ListAdaptExistPed(this, items);
        listView.setAdapter(adapter);
    }

    //endregion

    private class clsDocExist extends clsDocument {

        public clsDocExist(Context context, int printwidth, String archivo) {
            super(context, printwidth,gl.peMon,gl.peDecImp, archivo);

            nombre="REPORTE DE EXISTENCIAS";
            numero="";
            serie="";
            ruta=gl.ruta;
            vendedor=gl.vendnom;
            cliente="";
            vendcod=gl.vend;
            fsfecha=du.getActDateStr();

        }

        protected boolean buildDetail() {
            clsClasses.clsExist item;
            String s1,s2,lote;
            int ic;

            try {
                String vf=txtFilter.getText().toString();
                if (!mu.emptystr(vf)) rep.add("Filtro : "+vf);

                rep.empty();
                rep.line();lns=items.size();

                rep.add("Cod  Descripcion");
                rep.add("             Cantidad UM     Peso UM");
                rep.line();

                for (int i = 0; i <items.size(); i++) {

                    item=items.get(i);
                    ic=item.items;
                    lote=item.Lote;

                    switch (item.flag) {
                        case 0:
                            rep.add(item.Cod + " " + item.Desc);
                            /*if (ic<2) {
                                if (!(lote==null || lote.isEmpty())) rep.add(item.Cod);
                            } else {
                                rep.add(item.Cod);
                            }*/
                            break;
                        case 1:
                            rep.add3lrr(item.Lote,item.Valor,item.Peso);break;
                        case 2:
                            rep.add("Estado malo");
                            rep.add3lrr(item.Lote,item.ValorM,item.PesoM);break;
                        case 3:
                            rep.add3lrr("Total",item.ValorT,item.PesoT);break;
                    }

                    //rep.add3lrr(item.Cod,item.Peso,item.Valor);
                    //if (item.flag==1) rep.add3lrr("Est.malo" ,item.PesoM,item.ValorM);
                }
                rep.line();
                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                msgbox(e.getMessage());
                return false;
            }

        }

        protected boolean buildFooter() {

            try {

                rep.empty();
                rep.add("Total unidades:  " + StringUtils.leftPad(mu.frmdecimal(SumaCant,gl.peDecImp), 10));
                rep.add("Total peso:      " + StringUtils.leftPad(mu.frmdecimal(SumaPeso,gl.peDecImp), 10));
                rep.add("Total registros: " + StringUtils.leftPad(String.valueOf(lns), 7));
                rep.empty();
                rep.empty();
                rep.empty();
                rep.add("Firma Vendedor" + StringUtils.leftPad( "____________________",5));
                rep.empty();
                rep.empty();
                rep.add("Firma Auditor" + StringUtils.leftPad("____________________",6));
                rep.empty();
                rep.empty();
                rep.empty();

                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                return false;
            }

        }

        public double getPeso() {
            Cursor DT;
            double sumaPesoB=0,sumaPeso=0;

            sql = "SELECT IFNULL(SUM(PESO),0) AS PESOTOT " +
                    " FROM P_STOCK_PV ";
            DT=Con.OpenDT(sql);

            if (DT!=null) {
                if (DT.getCount() > 0) {
                    DT.moveToFirst();
                    sumaPeso = DT.getDouble(0);
                }
            }

            if (DT!=null) DT.close();

            return sumaPeso;
        }

        public double getCantidad() {
            Cursor DT;
            double sumaCant=0;

            sql = "SELECT IFNULL(SUM(S.CANT),0) AS CANTUNI " +
                    " FROM P_STOCK_PV S ";
            DT=Con.OpenDT(sql);

            if (DT!=null){

                if (DT.getCount()>0) {
                    DT.moveToFirst();
                    sumaCant=DT.getDouble(0);
                }

            }

            if(DT!=null) DT.close();

            return sumaCant;
        }

    }

    //region Activity Events

    @Override
    protected  void onResume(){
        try{
            super.onResume();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    @Override
    protected void onPause() {
        try{
            super.onPause();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    //endregion
}
