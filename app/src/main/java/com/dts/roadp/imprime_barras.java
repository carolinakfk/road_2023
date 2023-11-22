package com.dts.roadp;

import android.database.Cursor;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsBarras;

import java.util.ArrayList;

public class imprime_barras extends PBase{

    private Spinner spin;
    private ListView list;
    private  Button btnPrint;

    private ArrayList<clsBarras> items = new ArrayList<clsBarras>();

    private ArrayList<String> spincode= new ArrayList<String>();
    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> barlist = new ArrayList<String>();

    private list_view_barras adapter;
    private String selbarra;

    private String item;
    private int selidx;

    private printer prn;

    private AppMethods app;
    
    private Runnable printexit,printcallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprime_barras);

        super.InitBase();
        addlog("ImprimeBarras",""+du.getActDateTime(),gl.vend);

        list = (ListView) findViewById(R.id.listBars);
        spin = (Spinner) findViewById(R.id.spinner9);
        btnPrint=(Button) findViewById(R.id.btnprint);

        app = new AppMethods(this, gl, Con, db);
        gl.validimp=app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        printexit= new Runnable() {
            public void run() {
                imprime_barras.super.finish();
            }
        };

        prn=new printer(this,printexit,gl.validimp);

        FillSpinner();

        setHandlers();

    }


    private void setHandlers(){

        try{

            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    TextView spinlabel;

                    try {

                        spinlabel=(TextView)parentView.getChildAt(0);

                        if (spinlabel!=null){
                            spinlabel.setTextColor(Color.BLACK);
                            spinlabel.setPadding(5, 0, 0, 0);
                            spinlabel.setTextSize(18);
                        }

                        item=spincode.get(position);

                        listItems();

                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox( e.getMessage());
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    return;
                }

            });

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = list.getItemAtPosition(position);
                    clsBarras sitem = (clsBarras) lvObj;

                    selbarra = sitem.barra;
                    selidx = position;
                    adapter.setSelectedIndex(position);

                }
            });

        }catch (Exception ex){
            msgbox(ex.getMessage());
        }

    }

    private void FillSpinner(){
        Cursor DT;
        String icode,iname;

        spincode.add("0");
        spinlist.add("Seleccione un producto ....");

        try{

            sql="select DISTINCT ST.CODIGO,PP.DESCCORTA from P_STOCKB ST INNER JOIN \n" +
                    "P_PRODUCTO PP ON PP.CODIGO = ST.CODIGO \n" +
                    "WHERE ST.RUTA='"+gl.ruta+"'";

            DT=Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                icode=DT.getString(0);
                iname=DT.getString(1);

                spincode.add(icode);
                spinlist.add(iname);

                DT.moveToNext();
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            spin.setSelection(0);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(dataAdapter);

    }

    private void listItems() {
        Cursor DT;
        clsBarras vItem;

        items.clear();
        barlist.clear();

        try {

            sql = "SELECT BARRA,PESO FROM P_STOCKB WHERE CODIGO='" + item + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    vItem = clsCls.new clsBarras();

                    vItem.barra = DT.getString(0);
                    vItem.peso = DT.getString(1);

                    items.add(vItem);
                    barlist.add(vItem.barra);

                    DT.moveToNext();
                }
            }


        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

        adapter = new list_view_barras(this, items);
        list.setAdapter(adapter);

        if (selidx > -1) {
            adapter.setSelectedIndex(selidx);
            list.setSelection(selidx);

        }

    }

    public void imprimebarras(View viev){

        try{

            if (barlist.size()>0){
               // btnPrint.setVisibility(View.INVISIBLE);
                mu.toast("Imprimiendo barras...");
                prn.printBarra(barlist);
               // btnPrint.setVisibility(View.VISIBLE);
            }else{
                mu.msgbox("No hay barras para imprimir.");
            }

        }catch (Exception e){

        }

    }

}
