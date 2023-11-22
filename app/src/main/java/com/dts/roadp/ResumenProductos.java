package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class ResumenProductos extends PBase {

    private clsResProd doc;
    private printer prn;
    private Runnable printclose;
    private clsRepBuilder rep;

    private ArrayList<clsClasses.clsResProducto> items = new ArrayList<clsClasses.clsResProducto>();

    private ListAdaptResProd adapter;
    private ListView lista;
    private TextView totcant, totpeso, titulo;
    private EditText filtro;

    private AppMethods app;

    public double totCant, totPeso;
    public int lns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_productos);
        super.InitBase();

        lista = (ListView) findViewById(R.id.lista);

        totcant = (TextView) findViewById(R.id.lblTotCant);
        totpeso = (TextView) findViewById(R.id.lblTotPeso);
        titulo = (TextView) findViewById(R.id.txtRoadTit);
        filtro = (EditText) findViewById(R.id.filtro);

        rep = new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp, "");

        app = new AppMethods(this, gl, Con, db);
        gl.validimp = app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        printclose= new Runnable() {
            public void run() {
                ResumenProductos.super.finish();
            }
        };

        prn=new printer(this,printclose,gl.validimp);
        doc=new clsResProd(this,prn.prw,"");

        titulo.setText("Reporte de Productos");

        loadData();
        setHandlers();
    }

    //region Carga de Datos
    public void loadData() {
        Cursor DT;
        clsClasses.clsResProducto item;
        String cadena = filtro.getText().toString().replace("'","");
        String sCant, sPeso;

        items.clear();
        totCant = 0.00;
        totPeso = 0.00;

        try {
            sql = "SELECT P.CODIGO,P.DESCCORTA, SUM(D.CANT) AS CANT,SUM(D.PESO) AS PESO " +
                    "FROM DS_PEDIDOD D INNER JOIN P_PRODUCTO P ON D.PRODUCTO = P.CODIGO";

            if (cadena.length() > 0) {
                sql=sql+" WHERE P.DESCCORTA LIKE '%" + cadena + "%' OR P.CODIGO LIKE '%"+cadena+"%'";
            }

            sql += " GROUP BY P.CODIGO,P.DESCCORTA";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0 && DT != null) {
                DT.moveToFirst();

                while (!DT.isAfterLast())
                {
                    sCant = mu.frmdec(DT.getDouble(2));
                    sPeso = mu.frmdec(DT.getDouble(3));

                    item = clsCls.new clsResProducto();
                    item.codigo=DT.getString(0);
                    item.nombre=DT.getString(1);
                    item.cantidad = sCant;
                    item.peso = sPeso;

                    totCant += DT.getDouble(2);
                    totPeso += DT.getDouble(3);

                    items.add(item);

                    DT.moveToNext();
                }

            } else {
                toast("No se han encontrado productos");
            }

            if(DT != null) DT.close();

            totcant.setText("Cant: " + mu.frmdec(totCant));
            totpeso.setText("Peso: " + mu.frmdec(totPeso));

            adapter = new ListAdaptResProd(this,items);
            lista.setAdapter(adapter);

        } catch (Exception e) {
            mu.msgbox( e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
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

    private class clsResProd extends clsDocument {

        public clsResProd(Context context, int printwidth, String archivo) {
            super(context, printwidth,gl.peMon,gl.peDecImp, archivo);

            nombre=" REPORTE DE PRODUTOS PREFACTURA ";
            numero="";
            serie="";
            ruta=gl.ruta;
            vendedor=gl.vendnom;
            cliente="";
            vendcod=gl.vend;
            fsfecha=du.getActDateStr();
        }

        protected boolean buildDetail() {
            clsClasses.clsResProducto item;
            String s1,s2,lote;
            int ic;

            try {

                rep.empty();
                rep.line();
                rep.add("Cod  Descripcion");
                rep.add("Cantidad            Peso");
                rep.line();
                lns = items.size();
                for (int i = 0; i <items.size(); i++) {

                    item=items.get(i);

                    rep.add(item.codigo + " " + item.nombre);
                    rep.add3lrr(item.cantidad, item.peso, "");
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
                rep.add("TOTAL CANTIDAD:     "+ mu.frmdec(totCant));
                rep.add("TOTAL PESO:         "+ mu.frmdec(totPeso));
                rep.add("TOTAL REGISTROS:    "+ lns);
                rep.line();
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
    //endregion

    //region Aux
    public void setHandlers() {
        filtro.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int tl;
                tl = filtro.getText().toString().length();

                if (tl == 0 || tl > 1) {
                    loadData();
                }
            }
        });
    }
    //endregion

}