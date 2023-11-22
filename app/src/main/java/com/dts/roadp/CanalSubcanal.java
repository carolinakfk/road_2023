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

    private ListView listaCanales,listaSubcanal;
    private TextView txtFiltroCanal, txtFiltroCanalSub;
    private ImageView btnAceptar;

    private ArrayList<clsClasses.clsCanal> items = new ArrayList<clsClasses.clsCanal>();
    private ArrayList<clsClasses.clsSubCanal> items1 = new ArrayList<clsClasses.clsSubCanal>();
    private clsClasses.clsCanal selitem;
    private clsClasses.clsSubCanal selitem1;
    private ListAdaptCanal adapter;
    private ListAdaptSubCanal adapter1;
    private int selidx,selidx1;
    private String selid="",selid1="", claseNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canal_subcanal);

        super.InitBase();

        listaCanales =  (ListView) findViewById(R.id.listaCanales);
        listaSubcanal =  (ListView) findViewById(R.id.listaSubcanal);
        txtFiltroCanal = (TextView) findViewById(R.id.txtFiltroCanal);
        txtFiltroCanalSub = (TextView) findViewById(R.id.txtFiltroCanalSub);

        btnAceptar = (ImageView)findViewById(R.id.btnAceptar);

        ListaCanales();
        setHandlers();

    }

    private void ListaCanales(){

        Cursor DT;
        clsCanal item;

        items.clear();

        String cadena;
        cadena=txtFiltroCanal.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try {

            sql="SELECT * FROM P_CANAL";

            if (cadena.length()>0) {
                sql=sql+" WHERE NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
            }

            DT=Con.OpenDT(sql);

            if (DT!=null){

                if(DT.getCount()>0) {
                    DT.moveToFirst();

                    while (!DT.isAfterLast())
                    {
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

    private void  ListaSubCanales(){
        Cursor DT;
        clsSubCanal item1;
        items1.clear();

        String cadena;
        cadena=txtFiltroCanalSub.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try{
            if (gl.IdCanal.length() > 0) {
                sql="SELECT * FROM P_CANALSUB WHERE CANAL='" + gl.IdCanal + "'";

                if (cadena.length()>0) {
                    sql=sql+" AND NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
                }

                DT=Con.OpenDT(sql);

                if (DT!=null){

                    if(DT.getCount()>0) {
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

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

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
                    }

                }
            });

            txtFiltroCanal.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;
                    tl = txtFiltroCanal.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        ListaCanales();
                    }
                }
            });

            txtFiltroCanalSub.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;
                    tl = txtFiltroCanalSub.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        ListaSubCanales();
                    }
                }
            });

            btnAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    @Override
    public void onBackPressed() {
        super.finish();
    }
}