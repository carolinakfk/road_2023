package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class clsDesglose extends clsDocument {

    private ArrayList<itemData> items= new ArrayList<itemData>();

    private double tot,desc,imp,stot,percep,desgloseTotal=0;
    private boolean sinimp;
    private String 	contrib,recfact,estadoDev,corelNC,corelF,asignacion;

    public clsDesglose(Context context, int printwidth, String cursym, int decimpres, String archivo) {
        super(context, printwidth, cursym, decimpres, archivo);
        docpedido=false;
        docfactura=false;
        docrecibo=false;
        docdevolucion=false;
        docdesglose=true;
    }

    protected boolean detailToledano() {
        itemData item;
        String ss;

        rep.add("");
        rep.add("DENOMINACION     CANTIDAD      TOTAL");
        rep.add("      MONEDA        TIPO           ");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            ss=rep.ltrim(item.denom+" ",prw-20);
            ss=ss+rep.rtrim(frmdecimal(item.cant,0), 5)+" "+rep.rtrim(frmdecimal(item.total,2),14);
            rep.add(ss);
            ss=rep.rtrim(item.moneda,10)+" "+rep.rtrim(item.tipo,15);
            rep.add(ss);

        }

        rep.line();

        return true;
    }

    protected boolean buildFooter() {

        rep.add("");
        rep.addtotD("TOTAL DESGLOSE ", desgloseTotal);
        rep.add("");

        return super.buildFooter();
    }

    protected boolean loadHeadData(String corel) {
        super.loadHeadData(corel);

        nombre="DESGLOSE DE EFECTIVO";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        fsfecha = date;


        return true;

    }

    protected boolean loadDocData(String corel) {
        Cursor DT;
        double cantDenom;
        itemData item;

        loadHeadData(corel);

        items.clear();

        try {
            sql="SELECT D.DENOMINACION, D.CANTIDAD, D.TIPO, D.MONEDA "+
                    "FROM T_DEPOSB D ";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                item = new itemData();

                //item.corr=DT.getString(0);
                item.denom=DT.getDouble(0);
                item.cant=DT.getInt(1);

                item.tipo=DT.getString(2);

                item.moneda=DT.getString(3);

                cantDenom = Double.parseDouble( item.denom.toString());

                item.total = cantDenom * item.cant;

                if(item.tipo.equals("B")){
                    item.tipo = "Billete";
                }else if(item.tipo.equals("M")){
                    item.tipo = "Moneda";
                }else {
                    item.tipo = "";
                }

                desgloseTotal += item.total;

                items.add(item);

                DT.moveToNext();
            }



        } catch (Exception e) {
            Toast.makeText(cont,"loadHeadData"+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
        }

        return true;
    }

    protected boolean buildDetail() {
        itemData item;

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);
        }

        rep.add("");

        detailToledano();
        return true;
    }

    // Aux

    public double round2(double val){
        int ival;

        val=(double) (100*val);
        double rslt=Math.round(val);
        rslt=Math.floor(rslt);

        ival=(int) rslt;
        rslt=(double) ival;

        return (double) (rslt/100);
    }

    private class itemData {
        public Double denom,total;
        public String corr,tipo,moneda;
        public int cant;
    }
}
