package com.dts.roadp;

import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DepartamentoMun extends PBase {
    private ListView listaDep,listaMun;
    private TextView txtFiltroDep, txtFiltroMun;
    private ImageView btnAceptar;

    private ArrayList<clsClasses.clsDepartamento> items = new ArrayList<clsClasses.clsDepartamento>();
    private ArrayList<clsClasses.clsMunicipio> items1 = new ArrayList<clsClasses.clsMunicipio>();
    private clsClasses.clsDepartamento selitem;
    private clsClasses.clsMunicipio selitem1;
    private ListAdaptDep adapter;
    private ListAdaptMun adapter1;
    private String depar;
    private int selidx,selidx1;
    private String selid="",selid1="", claseNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departamento_mun);
        super.InitBase();
        claseNombre = getIntent().getStringExtra("clase");

        listaDep =  (ListView) findViewById(R.id.listaDep);
        listaMun =  (ListView) findViewById(R.id.listaMun);
        txtFiltroDep= (TextView) findViewById(R.id.txtFiltroDep);
        txtFiltroMun = (TextView) findViewById(R.id.txtFiltroMun);

        btnAceptar = (ImageView)findViewById(R.id.btnAceptar);

        ListaDepartamento();
        setHandlers();

        btnAceptar.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void ListaDepartamento(){

        Cursor DT;
        clsClasses.clsDepartamento item;

        items.clear();

        String cadena;
        cadena=txtFiltroDep.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try {

            sql="SELECT CODIGO,NOMBRE FROM P_DEPAR";

            if (cadena.length()>0) {
                sql=sql+" WHERE NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
            }

            DT=Con.OpenDT(sql);

            if (DT!=null){

                if(DT.getCount()>0) {
                    DT.moveToFirst();

                    while (!DT.isAfterLast())
                    {
                        item = clsCls.new clsDepartamento();
                        item.codigo=DT.getString(0);
                        item.nombre=DT.getString(1);

                        items.add(item);

                        DT.moveToNext();
                    }

                }

            }

            if(DT!=null) DT.close();

            adapter = new ListAdaptDep(this,items);
            listaDep.setAdapter(adapter);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void  ListaMunicipios(){
        Cursor DT;
        clsClasses.clsMunicipio item1;
        items1.clear();

        String cadena;
        cadena=txtFiltroMun.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");

        try{
            if  (gl.IdDep!=null){

                if (gl.IdDep.length() > 0) {

                    sql="SELECT CODIGO,NOMBRE FROM P_MUNI WHERE DEPAR='" + gl.IdDep + "'";

                    if (cadena.length()>0) {
                        sql=sql+" AND NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
                    }

                    DT=Con.OpenDT(sql);

                    if (DT!=null){

                        if(DT.getCount()>0) {
                            DT.moveToFirst();

                            while (!DT.isAfterLast()) {

                                item1 = clsCls.new clsMunicipio();

                                item1.codigo=DT.getString(0);
                                item1.nombre=DT.getString(1);

                                items1.add(item1);

                                DT.moveToNext();
                            }

                        }

                    }

                    if(DT!=null) DT.close();

                    adapter1=new ListAdaptMun (this,items1);
                    listaMun.setAdapter(adapter1);
                }
            }else{
                toast("Seleccione una provincia");
            }


        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void setHandlers(){

        try {
            listaDep.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaDep.getItemAtPosition(position);

                    clsClasses.clsDepartamento sitem = (clsClasses.clsDepartamento) lvObj;
                    selitem = sitem;

                    selid=sitem.codigo;selidx=position;
                    adapter.setSelectedIndex(position);

                    if (selid!=""){
                        gl.IdDep = sitem.codigo;
                        gl.CliProvincia  = sitem.nombre;

                        ListaMunicipios();
                    }

                }
            });

            listaMun.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaMun.getItemAtPosition(position);

                    clsClasses.clsMunicipio sitem = (clsClasses.clsMunicipio) lvObj;
                    selitem1 = sitem;

                    selid1=sitem.codigo;selidx1=position;
                    adapter1.setSelectedIndex(position);

                    if (selid1!=""){
                        gl.IdMun = sitem.codigo;
                        gl.CliDistrito = sitem.nombre;
                    }

                }
            });

            txtFiltroDep.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;
                    tl = txtFiltroDep.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        ListaDepartamento();
                    }
                }
            });

            txtFiltroMun.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;
                    tl = txtFiltroMun.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        ListaMunicipios();
                    }
                }
            });

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void setDepartamento() {
       //Intent intent = new Intent(this, CliNuevoT.class);
        // startActivity(intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}