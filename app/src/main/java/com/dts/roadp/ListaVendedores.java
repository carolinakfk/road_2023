package com.dts.roadp;

import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class ListaVendedores extends PBase {

    private EditText txtFiltroVendedor;
    private ListView listaVendedores;
    private ImageView imgAceptar;

    private ArrayList<clsClasses.clsAyudante> items = new ArrayList<clsClasses.clsAyudante>();
    private ListAdaptAyudante adapter;
    private clsClasses.clsAyudante selitem;
    private String selid="",selid1="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_vendedores);

        super.InitBase();

        txtFiltroVendedor = (EditText) findViewById(R.id.txtFiltroVendedor);
        listaVendedores = (ListView) findViewById(R.id.listaVendedores);
        imgAceptar = (ImageView) findViewById(R.id.imgAceptar);
        gl.CliCodVen = "";
        ListaAyudantes();
        setHandlers();

    }

    private void ListaAyudantes(){
        Cursor DT;
        clsClasses.clsAyudante item;

        items.clear();

        String cadena;
        cadena=txtFiltroVendedor.getText().toString().replace("'","");
        cadena=cadena.replace("\r","");


        try {

            sql="SELECT CODIGO,NOMBRE FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 5 ";

            if (cadena.length()>0) {
                sql=sql+" AND NOMBRE LIKE '%" + cadena + "%' OR CODIGO LIKE '%"+cadena+"%'";
            }

            DT=Con.OpenDT(sql);

            if(DT.getCount()>0) {

                DT.moveToFirst();

                while (!DT.isAfterLast())
                {

                    item = clsCls.new clsAyudante();

                    item.idayudante=DT.getString(0);
                    item.nombreayudante=DT.getString(1);

                    items.add(item);

                    DT.moveToNext();
                }

            }

            if(DT!=null) DT.close();

            adapter=new ListAdaptAyudante(this,items);
            listaVendedores.setAdapter(adapter);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void setHandlers(){

        try {
            listaVendedores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listaVendedores.getItemAtPosition(position);

                    clsClasses.clsAyudante sitem = (clsClasses.clsAyudante) lvObj;
                    selitem = sitem;

                    selid = sitem.idayudante;
                    selidx = position;
                    adapter.setSelectedIndex(position);

                    if (selid != "") {
                        gl.CliCodVen = sitem.idayudante;
                        gl.CliNomVen = sitem.nombreayudante;

                    }

                    finish();
                }
            });

            txtFiltroVendedor.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) { }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;
                    tl = txtFiltroVendedor.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        ListaAyudantes();
                    }
                }
            });

            imgAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.finish();
    }
}