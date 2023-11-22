package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.security.SecurityPermission;
import java.util.ArrayList;

public class rating  extends PBase {

    private clsWSEnvio vWSEnvio;

    private Button cmdEnviar;
    private RatingBar ratingBar;
    private Spinner cmbTransError;
    private EditText txtComentarioU;
    private TextView lblCaracteres;

    private ArrayList<String> spincode= new ArrayList<String>();
    private ArrayList<String> spinlist = new ArrayList<String>();

    private String idtranse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        super.InitBase();

        addlog("Rating",""+du.getActDateTime(),gl.vend);

        ratingBar =(RatingBar) findViewById(R.id.rbROAD);
        cmdEnviar =(Button) findViewById(R.id.cmdEnviar);
        cmbTransError = (Spinner) findViewById(R.id.cmbTransError);
        txtComentarioU = (EditText) findViewById(R.id.txtComentarioU);
        lblCaracteres = (TextView) findViewById(R.id.lblCaracteres);

        setHandlers();

        fillSpinner();

    }

    public void GuardaRating(View view) {

        try{

            float rating=ratingBar.getRating();
            String comentario = txtComentarioU.getText().toString();
            String ruta = gl.ruta;
            String  vendedor=gl.vend;
            String idtranserror = idtranse;
            String fecha = du.univfechaseg();

            if (rating==0){
                mu.msgbox("Debe ingresar su rating para poder tener una evaluación de nuestra aplicación");
                return;
            }

            ins.init("D_RATING");

            ins.add("RUTA", ruta);
            ins.add("VENDEDOR",vendedor);
            ins.add("RATING",rating);
            ins.add("COMENTARIO",comentario);
            ins.add("IDTRANSERROR",idtranserror);
            ins.add("FECHA",fecha);
            ins.add("STATCOM","N");

            db.execSQL(ins.sql());

            msgRatingIngresado("Gracias por sus comentarios");

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            mu.msgbox("Error : " + e.getMessage());
        }

    }

    private void fillSpinner(){
        Cursor DT;
        String icode,iname;

        try {

            spincode.add("0");
            spinlist.add("< Sin especificar >");

             sql="SELECT IDTRANSERROR,TRANSERROR FROM P_TRANSERROR ORDER BY TRANSERROR";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                icode=DT.getString(0);
                iname=DT.getString(1);

                spincode.add(icode);
                spinlist.add(iname);

                DT.moveToNext();
            }

        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            mu.msgbox( e.getMessage());
        }

        try {

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            cmbTransError.setAdapter(dataAdapter);

            cmbTransError.setSelection(0);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            cmbTransError.setSelection(0);
        }

    }

    private void setHandlers(){

        cmbTransError.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TextView spinlabel;
                String scod, idposition;

                try {
                    spinlabel = (TextView) parentView.getChildAt(0);
                    spinlabel.setTextColor(Color.BLACK);
                    spinlabel.setPadding(5, 0, 0, 0);
                    spinlabel.setTextSize(18);
                    spinlabel.setTypeface(spinlabel.getTypeface(), Typeface.BOLD);

                    scod = spincode.get(position);
                    idtranse=scod;

                    cmbTransError.requestFocus();
                    //if (act>0) {hidekeyb();}
                    hidekeyb();

                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    mu.msgbox(e.getMessage());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }

        });

        txtComentarioU.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String tamanoString = String.valueOf(s.length());
                lblCaracteres.setText("("+tamanoString + " caracteres)");
            }
        });
    }

    private void doExit(){
        try{
            super.finish();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgAskExit(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("¿" + msg + "?");

            dialog.setIcon(R.drawable.ic_quest);
            dialog.setCancelable(false);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    doExit();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgRatingIngresado(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);

            dialog.setIcon(R.drawable.ic_info);
            dialog.setCancelable(false);

            dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //EnviaRating();
                    doExit();
                }
            });

            dialog.setNegativeButton("", null);

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private boolean EnviaRating(){

        boolean vEnvia=false;

        try{

            vWSEnvio = new clsWSEnvio(this, gl.ruta, gl.emp,2);
            vWSEnvio.runExecuteEnvio();

        }catch (Exception e){
            mu.toast("Ocurrió un error enviando los datos " + e.getMessage() );
        }

        return vEnvia;
    }

    @Override
    public void onBackPressed() {
        try{
            msgAskExit("Salir");
            super.onBackPressed();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

}
