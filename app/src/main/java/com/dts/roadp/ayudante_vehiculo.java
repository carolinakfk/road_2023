package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import com.dts.roadp.clsClasses.clsAyudante;
import com.dts.roadp.clsClasses.clsVehiculo;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ayudante_vehiculo extends PBase {

    private ListView listAyu,listVehi;
    private Button btnOmitir,btnAsignar;
    private TextView Ayudante,Vehiculo;

    private ArrayList<clsClasses.clsAyudante> items = new ArrayList<clsClasses.clsAyudante>();
    private ArrayList<clsClasses.clsVehiculo> items1 = new ArrayList<clsClasses.clsVehiculo>();
    private clsAyudante selitem;
    private clsVehiculo selitem1;
    private ListAdaptAyudante adapter;
    private ListAdaptVehiculo adapter1;

    private int selidx,selidx1;
    private String selid="",selid1="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayudante_vehiculo);

        super.InitBase();

        gl.ayudanteID = "";
        gl.vehiculoID="";

        listAyu =  (ListView) findViewById(R.id.listAyudante);
        listVehi =  (ListView) findViewById(R.id.listVehiculo);

        btnOmitir = (Button)findViewById(R.id.btnOmitr);
        btnAsignar = (Button)findViewById(R.id.btnAsignar);

        Ayudante = (TextView)findViewById(R.id.lblAyudante);
        Vehiculo = (TextView)findViewById(R.id.lblVehiculo);

        setHandlers();

        ListaAyudantes();
        ListaVehiculos();

        btnOmitir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAsignar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectAyudate();
            }
        });

    }


    private void ListaAyudantes(){

        Cursor DT;
        clsAyudante item;

        items.clear();

        try {

            sql="SELECT CODIGO,NOMBRE FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 5 ";

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
            listAyu.setAdapter(adapter);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void  ListaVehiculos(){
        Cursor DT;
        clsVehiculo item1;

        items1.clear();

    try{

        sql="SELECT CODIGO, MARCA, PLACA FROM P_VEHICULO ";
        DT=Con.OpenDT(sql);

        if(DT.getCount()>0) {
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                item1 = clsCls.new clsVehiculo();

                item1.idVehiculo=DT.getString(0);
                item1.marca=DT.getString(1);
                item1.placa=DT.getString(2);

                items1.add(item1);

                DT.moveToNext();
            }

        }

        if(DT!=null) DT.close();

        adapter1=new ListAdaptVehiculo(this,items1);
        listVehi.setAdapter(adapter1);

    }catch (Exception e){
        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        mu.msgbox(e.getMessage()+"\n"+sql);

    }

    }

    private void setHandlers(){

        try {
            listAyu.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object lvObj = listAyu.getItemAtPosition(position);

                    clsAyudante sitem = (clsAyudante) lvObj;
                    selitem = sitem;

                    selid=sitem.idayudante;selidx=position;
                    adapter.setSelectedIndex(position);

                    if (selid!=""){
                        Ayudante.setText(sitem.nombreayudante);
                    }

                }
            });

        listAyu.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Object lvObj = listAyu.getItemAtPosition(position);

                clsAyudante sitem = (clsAyudante) lvObj;
                selitem = sitem;

                selid=sitem.idayudante;selidx=position;
                adapter.setSelectedIndex(position);

                quitarAyudante();

                return true;
            }
        });

            listVehi.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Object vObjVehi = listVehi.getItemAtPosition(position);

                    clsVehiculo sitem1 = (clsVehiculo)vObjVehi;
                    selitem1 = sitem1;

                    selid1 = sitem1.idVehiculo;selidx1=position;
                    adapter1.setSelectedIndex(position);

                    if (selid1!=""){
                        Vehiculo.setText(sitem1.marca+" "+sitem1.placa);
                    }

                }
            });

            listVehi.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    Object vObjVehi = listVehi.getItemAtPosition(position);

                    clsVehiculo sitem1 = (clsVehiculo)vObjVehi;
                    selitem1 = sitem1;

                    selid1 = sitem1.idVehiculo;selidx1=position;
                    adapter1.setSelectedIndex(position);

                    quitarVehiculo();

                    return false;
                }
            });

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void SelectAyudate(){

        try{

        gl.ayudanteID = selid;
        gl.vehiculoID = selid1;

            if(gl.ayudanteID.equals("")){
                msgAskAyudante();
            }else if(gl.vehiculoID.equals("")){
                msgAskVehiculo();
            }else{
                finish();
            }

    }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgAskAyudante() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("¿Esta seguro de continuar sin asignar un ayudante?");

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                finish();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void quitarAyudante(){

        try{

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage(String.format("¿Quitar ayudante: %s?",selid));

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    gl.ayudanteID = "";
                    selid="";
                    if (selid.equals("")){
                        Ayudante.setText("No seleccionado");
                    }


                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();


        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void quitarVehiculo(){

        try{

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage(String.format("¿Quitar vehiculo: %s?",selid1));

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    gl.vehiculoID = "";
                    selid1="";
                    if (selid.equals("")){
                        Vehiculo.setText("No seleccionado");
                    }


                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();


        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void msgAskVehiculo() {

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("¿Esta seguro de continuar sin asignar un vehículo?");

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

}
