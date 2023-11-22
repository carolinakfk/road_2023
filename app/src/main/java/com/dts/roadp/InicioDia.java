package com.dts.roadp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsExist;

import java.util.ArrayList;
import java.util.Calendar;


public class InicioDia extends PBase implements View.OnClickListener{

    private TextView etFecha;
    private ImageView ibObtenerFecha,  imgIniciar;

    private static final String CERO = "0";
    private static final String BARRA = "/";

    public final Calendar c = Calendar.getInstance();

    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    private int cyear, cmonth, cday;
    private long fechae;
    private boolean seleccionFecha;

    //#HS_20181212 para imprimir el inventario
    private clsDocExist doc;
    private printer prn;
    private Runnable printclose;
    private int lns;
    private ArrayList<clsClasses.clsExist> items= new ArrayList<clsClasses.clsExist>();
    private clsRepBuilder rep;
    public int yy, impcorel;
    public String mm,dd,impserie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_dia);

        etFecha = (TextView)findViewById(R.id.lblFecha);
        ibObtenerFecha = (ImageView)findViewById(R.id.imgCalendario);
        ibObtenerFecha.setOnClickListener(this);
        imgIniciar = (ImageView)findViewById(R.id.imgSiguiente);
        imgIniciar.setOnClickListener(this);

        super.InitBase();
        addlog("InicioDia",""+du.getActDateTime(),gl.vend);

        seleccionFecha = false;
        setActDate();
        fechae=fecha;etFecha.setText(du.sfecha(fechae));

        printclose= new Runnable() {
            public void run() {
                InicioDia.super.finish();
            }
        };

        prn=new printer(this,printclose,gl.validimp);
        doc=new clsDocExist(this,prn.prw, "");
        rep=new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp, "");

        listItems();

        obtenerCorel();

    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.imgCalendario:
                    obtenerFecha();
                    break;

                case R.id.imgSiguiente:
                    try {
                        askFinalizar();
                        break;
                    }catch (Exception e){
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox("InicioDia Imp: "+e.getMessage());
                    }
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void obtenerFecha(){

        try{
            DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    final int mesActual = month + 1;
                    String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                    String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
                    etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
                    yy = year;mm = mesFormateado; dd = diaFormateado;
                    seleccionFecha = true;
                }
            },anio, mes, dia);

            recogerFecha.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void setActDate(){
        try{
            final Calendar c = Calendar.getInstance();
            cyear = c.get(Calendar.YEAR);
            cmonth = c.get(Calendar.MONTH)+1;
            cday = c.get(Calendar.DAY_OF_MONTH);
            fecha=du.cfecha(cyear,cmonth,cday);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void askFinalizar() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("¿Esta seguro de cambiar la fecha de las factura e imprimir el invetario disponible?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    printDoc();
                    fechaNueva();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void fechaNueva(){
        int fecha;

        try {

            if (seleccionFecha) {

                yy = yy - 2000;

                fecha = Integer.valueOf(String.valueOf(yy) + mm + dd + "0000");

                gl.nuevaFecha = fecha;

            } else {

                gl.nuevaFecha = fechae;

            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            mu.msgbox("fechaNueva: " + e.getMessage());
        }

    }

    ////////////////// Proceso para impresión //////////////////

    public void printDoc() {
        try{
            if (doc.buildPrint("0",0)) prn.printask();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private class clsDocExist extends clsDocument {

        public clsDocExist(Context context, int printwidth, String archivo) {
            super(context, printwidth,gl.peMon,gl.peDecImp, archivo);

            nombre="Existencias";
            numero="";
            serie="";
            ruta=gl.ruta;
            vendedor=gl.vendnom;
            cliente="";

        }

        protected boolean buildDetail() {
            clsExist item;
            String s1,s2;

            try {

                rep.add("REPORTE DE EXISTENCIAS");
                rep.line();lns=items.size();

                for (int i = 0; i <items.size(); i++) {
                    item=items.get(i);
                    rep.add(item.Desc);
                    rep.add3lrr(item.Cod,item.Peso,item.Valor);
                    if (item.flag==1) rep.add3lrr("Est.malo" ,item.PesoM,item.ValorM);
                }

                rep.line();

                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                return false;
            }

        }

        protected boolean buildFooter() {

            try {
                rep.add("Total lineas : "+lns);
                rep.add("");
                rep.line();
                rep.add("");
                rep.add("Serie: "+impserie);
                rep.add("Próximo correlativo: "+(impcorel + 1));
                rep.add("");rep.add("");rep.add("");
                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                return false;
            }

        }

    }

    private void obtenerCorel(){
        Cursor DT;

        try{

            sql = "SELECT SERIE, CORELULT FROM P_COREL";

            DT = Con.OpenDT(sql);

            if(DT.getCount() > 0) {
                DT.moveToFirst();

                impserie = DT.getString(0);
                impcorel = DT.getInt(1);
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("obtenerCorel: "+e.getMessage());
        }
    }

    private void listItems() {
        Cursor DT;
        clsClasses.clsExist item;
        String vF, cod, name, um, ump, sc, scm, sct, sp, spm, spt;
        double val, valm, valt,peso,pesom,pesot;

        items.clear();

        try {

            //vSQL="SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,SUM(P_STOCK.CANT),SUM(P_STOCK.CANTM) "+
            //     "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            //if (vF.length()>0) vSQL=vSQL+"AND ((P_PRODUCTO.DESCLARGA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
            //vSQL+="GROUP BY P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA ORDER BY P_PRODUCTO.DESCLARGA";

            sql = "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.CANT,P_STOCK.CANTM,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
            sql += "ORDER BY P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA";

            DT = Con.OpenDT(sql);

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
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

    }

    ////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        try{
            super.onResume();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

}