package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalculadoraKgs extends PBase {

    private TextView lblTitulo, lblUnidad, lblResultado, lblUnidadR, btnConvertir;
    private ImageView btnAccion, btnRegresar;
    private EditText txtCantidad;
    private RelativeLayout relResultado;

    public int tipo;
    public double resultado = 0.0;
    public double factor = 2.20462262;
    public String unidad = "", color="", titulo="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora_kgs);
        super.InitBase();

        lblTitulo = (TextView) findViewById(R.id.lblTitulo);
        lblUnidad = (TextView) findViewById(R.id.lblUnidad);
        lblResultado = (TextView) findViewById(R.id.lblResultado);
        lblUnidadR = (TextView) findViewById(R.id.lblUnidadR);
        btnConvertir = (TextView) findViewById(R.id.btnConvertir);

        btnAccion = (ImageView) findViewById(R.id.btnAccion);
        btnRegresar = (ImageView) findViewById(R.id.btnRegresar);

        relResultado = (RelativeLayout) findViewById(R.id.relResultado);
        txtCantidad = (EditText) findViewById(R.id.txtCantidad);

        setHandlers();

        tipo = 1;
        lblTitulo.setText("CONVERTIR LBS A KGS");
        lblUnidad.setText("LBS");
        lblUnidadR.setText("KGS");
        lblUnidadR.setBackgroundColor(Color.parseColor("#FAF6AE"));
        lblUnidad.setBackgroundColor(Color.parseColor("#93F19D"));
        txtCantidad.requestFocus();
    }

    public void setHandlers() {
        btnAccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarAccion();
                txtCantidad.requestFocus();
            }
        });

        btnConvertir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtCantidad.getText().toString().isEmpty()) {
                    convetirLbsKgs();
                    txtCantidad.requestFocus();
                    txtCantidad.selectAll();
                } else {
                    toast("Debe ingresar un valor mayor a 0");
                }
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresar();
            }
        });

        txtCantidad.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { } });

        txtCantidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    if (!txtCantidad.getText().toString().isEmpty()) {
                        convetirLbsKgs();
                        txtCantidad.requestFocus();
                        txtCantidad.selectAll();
                        return true;
                    } else {
                        toast("Debe ingresar un valor mayor a 0");

                        final Handler cbhandler = new Handler();
                        cbhandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                txtCantidad.requestFocus();
                            }
                        }, 500);
                    }

                }
                return false;
            }
        });
    }

    public void cambiarAccion()
    {
        if (tipo == 1) {
            tipo = 2;
            lblUnidad.setText("KGS");
            lblUnidad.setBackgroundColor(Color.parseColor("#FAF6AE"));
        } else if (tipo == 2){
            tipo = 1;
            lblUnidad.setText("LBS");
            lblUnidad.setBackgroundColor(Color.parseColor("#93F19D"));
        }

        setEstilo();
        lblResultado.setText("0.000");
    }

    public void convetirLbsKgs()
    {
        resultado = tipo == 1 ?
                    Double.valueOf(txtCantidad.getText().toString()) / factor:
                    Double.valueOf(txtCantidad.getText().toString()) * factor;

        lblResultado.setText(String.valueOf(mu.frmdecimal(resultado, 3)));
        relResultado.setVisibility(View.VISIBLE);

        setEstilo();
    }

    public void setEstilo()
    {
        unidad = tipo == 1 ? "KGS":"LBS";
        color = tipo == 1 ? "#FAF6AE":"#93F19D";
        titulo = tipo == 1 ? "CONVERTIR LBS A KGS": "CONVERTIR KGS A LBS";

        lblTitulo.setText(titulo);
        lblUnidadR.setBackgroundColor(Color.parseColor(color));
        lblUnidadR.setText(unidad);
    }

    public void regresar() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}