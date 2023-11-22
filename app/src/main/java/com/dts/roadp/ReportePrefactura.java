package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class ReportePrefactura extends PBase {
    private clsResPrefactura doc;
    private printer prn;
    private Runnable printclose;
    private clsRepBuilder rep;
    private AppMethods app;

    private ArrayList<clsClasses.clsResPrefactura> items = new ArrayList<clsClasses.clsResPrefactura>();
    private ArrayList<clsClasses.clsResProducto> itemsP = new ArrayList<clsClasses.clsResProducto>();

    private ListAdaptResPrefactura adapter;

    private ListView lista;
    private TextView titulo,lblTotCant, lblTotPeso,textView9;
    private EditText filtro;
    private LinearLayout totales;

    public int lns;
    public String rutapreventa = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_productos);
        super.InitBase();

        filtro = (EditText) findViewById(R.id.filtro);
        lista = (ListView) findViewById(R.id.lista);
        titulo = (TextView) findViewById(R.id.txtRoadTit);
        lblTotCant = (TextView) findViewById(R.id.lblTotCant);
        lblTotPeso = (TextView) findViewById(R.id.lblTotPeso);
        textView9 = (TextView) findViewById(R.id.textView9);

        if (gl.repPrefactura) {
            filtro.setVisibility(View.INVISIBLE);
            lblTotCant.setVisibility(View.INVISIBLE);
            lblTotPeso.setVisibility(View.INVISIBLE);
            textView9.setVisibility(View.VISIBLE);
        }

        rep = new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp, "");

        app = new AppMethods(this, gl, Con, db);
        gl.validimp = app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        printclose= new Runnable() {
            public void run() {
                ReportePrefactura.super.finish();
            }
        };

        loadData();

        prn=new printer(this,printclose,gl.validimp);
        doc=new clsResPrefactura(this,prn.prw,"");

        titulo.setText("Reporte de Prefacturas");
    }

    //region Set Data
    public void loadData() {
        Cursor DT, DP, DD;
        String pcliente, sqlP, sqlProd, pedido;
        clsClasses.clsResPrefactura item, itemP;

        try {
            sql = "SELECT DISTINCT C.CODIGO,C.NOMBRE"+
                    " FROM DS_PEDIDO D INNER JOIN P_CLIENTE C ON D.CLIENTE = C.CODIGO";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            if (DT.getCount() > 0 && DT != null) {
                while (!DT.isAfterLast()) {
                    pcliente = DT.getString(0);

                    sqlP ="SELECT D.COREL AS COREL, ADD2 AS RUTA" +
                            " FROM DS_PEDIDO D" +
                            " WHERE D.CLIENTE ='"+pcliente+"'";

                    DD = Con.OpenDT(sqlP);
                    DD.moveToFirst();

                    if (DD.getCount() > 0 && DD != null) {
                        while (!DD.isAfterLast()) {

                            rutapreventa = DD.getString(1);
                            pedido = DD.getString(0);

                            sqlProd = "SELECT P.CODIGO,P.DESCCORTA, D.CANT,D.PESO, D.UMVENTA" +
                                    " FROM DS_PEDIDOD D INNER JOIN P_PRODUCTO P ON D.PRODUCTO = P.CODIGO" +
                                    " WHERE D.COREL='"+pedido+"'";

                            DP = Con.OpenDT(sqlProd);

                            item  = clsCls.new clsResPrefactura();
                            item.codigoCli = DT.getString(0);
                            item.nombreCli = DT.getString(1);
                            item.Prefact = DD.getString(0);
                            item.rutapreventa = rutapreventa;
                            item.flag = 0;

                            items.add(item);

                            if (DP.getCount() > 0) {
                                DP.moveToFirst();

                                while (!DP.isAfterLast()) {

                                    itemP = clsCls.new clsResPrefactura();
                                    itemP.codigoProd = DP.getString(0);
                                    itemP.nombreProd = DP.getString(1);
                                    itemP.cantidad = mu.frmdec(DP.getDouble(2));
                                    itemP.peso = mu.frmdec(DP.getDouble(3)) +" " +DP.getString(4);
                                    itemP.flag = 1;

                                    items.add(itemP);

                                    DP.moveToNext();
                                }
                            }

                            DD.moveToNext();
                        }
                    }

                    DT.moveToNext();
                }

            } else {
                toast("No se han encontrado prefacturas");
            }

            if(DT != null) DT.close();

        } catch (Exception e) {
            mu.msgbox( e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        adapter = new ListAdaptResPrefactura(this,items);
        lista.setAdapter(adapter);
    }
    //endregion

    //region Impresión

    public void printDoc(View view) {
        try{
            if(items.size()==0){
                msgbox("No hay productos disponibles");
                return;
            }
            if (doc.buildPrint("0",0)) prn.printask();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private class clsResPrefactura extends clsDocument {

        public clsResPrefactura(Context context, int printwidth, String archivo) {
            super(context, printwidth,gl.peMon,gl.peDecImp, archivo);

            nombre="REPORTE DE PREFACTURAS";
            numero="";
            serie="";
            ruta=gl.ruta;
            vendedor=gl.vendnom;
            cliente="";
            vendcod=gl.vend;
            //rutapv = rutapreventa;
            fsfecha=du.getActDateStr();

        }

        protected boolean buildDetail() {
            clsClasses.clsResPrefactura item;

            try {
                rep.empty();
                rep.line();lns=items.size();

                rep.add("Cod.Cli   Descripcion Cliente");
                rep.add("No. Prefactura  Ruta Preventa");
                rep.add("Cod.Prod  Descripcion Producto");
                rep.add("Cantidad        Peso");
                rep.line();

                for (int i = 0; i <items.size(); i++) {

                    item=items.get(i);

                    switch (item.flag) {
                        case 0:
                            rep.empty();
                            rep.add(item.codigoCli + " " + item.nombreCli);
                            rep.add(item.Prefact + "  " + item.rutapreventa);
                            break;
                        case 1:
                            rep.add(item.codigoProd + "  " + item.nombreProd);
                            rep.add3lrr(String.valueOf(item.cantidad), item.peso, "");
                            break;
                    }
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
                rep.empty();
                rep.add("Firma Vendedor" + StringUtils.leftPad( "__________________",5));
                rep.empty();
                rep.empty();
                rep.add("Firma Auditor" + StringUtils.leftPad("___________________",6));
                rep.empty();
                rep.empty();
                rep.empty();

                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                return false;
            }

        }

    }
    //endRegion

    @Override
    public void onBackPressed() {
        gl.repPrefactura = false;
        super.onBackPressed();
    }
}