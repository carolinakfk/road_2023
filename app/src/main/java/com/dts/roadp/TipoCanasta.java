package com.dts.roadp;

import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class TipoCanasta extends PBase {
    private ListView listView;
    private EditText txtFilter;
    private String cliente;
    private ArrayList<clsClasses.clsTipoCanastas> items;
    private ListAdaptTipoCanasta adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_canasta);
        super.InitBase();

        listView = (ListView) findViewById(R.id.listView1);
        txtFilter = (EditText)findViewById(R.id.editText1);

        items = new ArrayList<clsClasses.clsTipoCanastas>();
        gl.prodCanasta = "";
        cliente = gl.cliente;
        setHandlers();
        listItems();
    }

    public void limpiaFiltro(View view) {
        try{
            txtFilter.setText("");
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }
    private void setHandlers() {

        try {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {

                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsTipoCanastas item = (clsClasses.clsTipoCanastas) lvObj;

                        adapter.setSelectedIndex(position);
                        gl.prodCanasta = item.codigo;

                        finish();return;
                        
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            txtFilter.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int tl;

                    tl = txtFilter.getText().toString().length();

                    if (tl == 0 || tl > 1) {
                        listItems();
                    }
                }
            });


        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void listItems() {
        Cursor DT;
        items.clear();
        clsClasses.clsTipoCanastas cItem = null;
        int cantidad = 0;
        String vF, tabla="D_CANASTA";

        ArrayList<clsClasses.clsTipoCanastas> citems = new ArrayList<clsClasses.clsTipoCanastas>();;

        vF=txtFilter.getText().toString().replace("'","");
        vF=vF.replace("\r","");

        if (!mu.emptystr(gl.corelFac)) tabla="T_CANASTA";

        try {
            String sql = "SELECT " +
                    "a.CODIGO, " +
                    "a.DESCCORTA, " +
                    "a.DESCLARGA, " +
                    "ifnull(sum(b.CANTENTR),0) as tentregado," +
                    "ifnull(sum(b.CANTREC),0) as trecibido " +
                    "FROM P_PRODUCTO a " +
                    "LEFT JOIN "+tabla+" b ON b.PRODUCTO = a.CODIGO " +
                    "AND b.CLIENTE = '" + cliente + "' " +
                    "AND b.ANULADO = 0 " +
                    "WHERE a.ES_CANASTA=1 ";


            if (vF.length() >= 1) {
                sql += "AND (a.desclarga LIKE '%" + vF + "%') ";
            }
            sql += "GROUP BY a.CODIGO;";

            DT = Con.OpenDT(sql);

            cantidad = DT != null ? DT.getCount() : 0;
            if (cantidad == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                cItem = clsCls.new clsTipoCanastas();
                cItem.codigo = DT.getString(0);
                cItem.desccorta = DT.getString(1);
                cItem.desclarga = DT.getString(2);
                cItem.tenregado = DT.getInt(3);
                cItem.trecibido = DT.getInt(4);
                cItem.totales   = false;

                items.add(cItem);
                citems.add(cItem);

                DT.moveToNext();
            }

            DT.close();
        }catch (Exception e) {
            mu.msgbox( e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            Log.d("tipoCanasta",e.getMessage());
        }

        items = (ArrayList<clsClasses.clsTipoCanastas>) citems.clone();

        adapter=new ListAdaptTipoCanasta(this,citems);
        listView.setAdapter(adapter);
    }
}