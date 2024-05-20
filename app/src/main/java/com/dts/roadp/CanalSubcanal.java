package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import com.dts.roadp.clsClasses.clsCanal;
import com.dts.roadp.clsClasses.clsSubCanal;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CanalSubcanal extends PBase {

    private ListView listaCanales,listaSubcanal,listaTipologia;
    private TextView txtFiltroCanal, txtFiltroCanalSub, txtFiltroTipologia;
    private ImageView btnAceptar,btnLimpiar1,btnLimpiar2,btnLimpiar3;

    private ArrayList<clsClasses.clsCanal> items = new ArrayList<clsClasses.clsCanal>();
    private ArrayList<clsClasses.clsSubCanal> items1 = new ArrayList<clsClasses.clsSubCanal>();
    private ArrayList<clsClasses.clsP_tipologia> items2 = new ArrayList<clsClasses.clsP_tipologia>();

    private clsClasses.clsCanal selitem;
    private clsClasses.clsSubCanal selitem1;
    private clsClasses.clsP_tipologia selitem2;

    private ListAdaptCanal adapter;
    private ListAdaptSubCanal adapter1;
    private ListAdaptTipologia adapter2;

    private int selidx,selidx1,selidx2;
    private String selid="",selid1="",selid2="", claseNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canal_subcanal);

        super.InitBase();

        listaCanales =  (ListView) findViewById(R.id.listaCanales);
        listaSubcanal =  (ListView) findViewById(R.id.listaSubcanal);
        listaTipologia =  (ListView) findViewById(R.id.listaTipologia);

        txtFiltroCanal = (TextView) findViewById(R.id.txtFiltroCanal);
        txtFiltroCanalSub = (TextView) findViewById(R.id.txtFiltroCanalSub);
        txtFiltroTipologia = (TextView) findViewById(R.id.txtFiltroTipologia);

        btnAceptar = (ImageView)findViewById(R.id.btnAceptar);
        btnLimpiar1 = (ImageView)findViewById(R.id.imgNext);
        btnLimpiar2 = (ImageView)findViewById(R.id.imgNext2);
        btnLimpiar3 = (ImageView)findViewById(R.id.imgNext3);

        ListaCanales();
        setHandlers();

        gl.IdTipologia = " ";
        gl.EditarTipologia = " ";
    }

    private void setHandlers(){

        try {

            listaCanales.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaCanales.getItemAtPosition(position);

                    clsCanal sitem = (clsCanal) lvObj;
                    selitem = sitem;

                    selid=sitem.codigo;selidx=position;
                    adapter.setSelectedIndex(position);

                    if (selid!=""){
                        gl.IdCanal = sitem.codigo;
                        gl.EditarClienteCanal  = sitem.nombre;

                        ListaSubCanales();
                    }

                }
            });

            listaSubcanal.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaSubcanal.getItemAtPosition(position);

                    clsSubCanal sitem = (clsSubCanal) lvObj;
                    selitem1 = sitem;

                    selid1=sitem.codigo;selidx1=position;
                    adapter1.setSelectedIndex(position);

                    if (selid1!=""){
                        gl.IdSubcanal = sitem.codigo;
                        gl.EditarClienteSubcanal = sitem.nombre;

                        ListaTipologia();
                    }

                }
            });

            listaTipologia.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaTipologia.getItemAtPosition(position);

                    clsClasses.clsP_tipologia sitem = (clsClasses.clsP_tipologia) lvObj;
                    selitem2 = sitem;

                    selid2=sitem.codigo;selidx2=position;
                    adapter2.setSelectedIndex(position);

                    if (selid2!=""){
                        gl.IdTipologia = sitem.codigo;
                        gl.EditarTipologia = sitem.nombre;

                        finish();
                    }
                }
            });

            txtFiltroCanal.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl = txtFiltroCanal.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        items1.clear();items2.clear();
                        ListaCanales();
                    }
                }
            });

            txtFiltroCanalSub.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl = txtFiltroCanalSub.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        items2.clear();
                        ListaSubCanales();
                    }
                }
            });

            txtFiltroTipologia.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl = txtFiltroTipologia.getText().toString().length();

                    if (tl == 0 || tl > 1) ListaTipologia();
                }
            });

            btnLimpiar1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txtFiltroCanal.setText("");
                }
            });

            btnLimpiar2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txtFiltroCanalSub.setText("");
                }
            });

            btnLimpiar3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txtFiltroTipologia.setText("");
                }
            });

            btnAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void ListaCanales(){
        Cursor DT;
        clsCanal item;

        String cadena=txtFiltroCanal.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try {

            sql="SELECT * FROM P_CANAL";

            if (cadena.length()>0) {
                sql=sql+" WHERE NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
            }

            DT=Con.OpenDT(sql);

            items.clear();
            if (DT!=null){

                if (DT.getCount()>0) {
                    DT.moveToFirst();

                    while (!DT.isAfterLast())  {
                        item = clsCls.new clsCanal();
                        item.codigo=DT.getString(0);
                        item.nombre=DT.getString(1);

                        items.add(item);

                        DT.moveToNext();
                    }
                }
            }

            if(DT!=null) DT.close();

            adapter = new ListAdaptCanal(this,items);
            listaCanales.setAdapter(adapter);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void ListaSubCanales(){
        Cursor DT;
        clsSubCanal item1;

        String cadena=txtFiltroCanalSub.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try{
            if (gl.IdCanal.length() > 0) {
                sql="SELECT * FROM P_CANALSUB WHERE CANAL='" + gl.IdCanal + "'";

                if (cadena.length()>0) {
                    sql=sql+" AND NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
                }

                DT=Con.OpenDT(sql);

                items1.clear();
                if (DT!=null){

                    if (DT.getCount()>0) {
                        DT.moveToFirst();

                        while (!DT.isAfterLast()) {

                            item1 = clsCls.new clsSubCanal();

                            item1.codigo=DT.getString(0);
                            //item1.canal=DT.getString(1);
                            item1.nombre=DT.getString(2);

                            items1.add(item1);

                            DT.moveToNext();
                        }

                    }

                }else{
                    toast("Selecciona el canal");
                }

                if(DT!=null) DT.close();

                adapter1=new ListAdaptSubCanal(this,items1);
                listaSubcanal.setAdapter(adapter1);
            }

        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }
    }

    private void ListaTipologia(){
        Cursor DT;
        clsClasses.clsP_tipologia item2;

        String cadena=txtFiltroTipologia.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try{
            if (gl.IdSubcanal.length() > 0) {
                sql="SELECT * FROM P_TIPOLOGIA WHERE CANALSUB='" + gl.IdSubcanal + "'";

                if (cadena.length()>0) {
                    sql=sql+" AND NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
                }

                DT=Con.OpenDT(sql);

                items2.clear();
                if (DT!=null){

                    if (DT.getCount()>0) {
                        DT.moveToFirst();

                        while (!DT.isAfterLast()) {
                            item2 = clsCls.new clsP_tipologia();
                            item2.codigo=DT.getString(0);
                            item2.nombre=DT.getString(2);
                            items2.add(item2);

                            DT.moveToNext();
                        }
                    }

                } else {
                    toast("Selecciona el subcanal");
                }

                if (DT!=null) DT.close();

                adapter2=new ListAdaptTipologia(this,items2);
                listaTipologia.setAdapter(adapter2);
            }

        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);
        }

    }

    @Override
    public void onBackPressed() {
        super.finish();
    }

}