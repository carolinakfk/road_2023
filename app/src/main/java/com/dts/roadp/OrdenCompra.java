package com.dts.roadp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class OrdenCompra  extends PBase{

    private EditText txtOrdenCompra;
    private TextView lblFactura;

    private Button cmdAceptar, cmdCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orden_compra);

        super.InitBase();
        addlog("Orden compra",""+du.getActDateTime(),gl.vend);

        txtOrdenCompra = (EditText) findViewById(R.id.txtOrdenCompra);
        txtOrdenCompra.requestFocus();
        cmdAceptar = (Button) findViewById(R.id.cmdAceptar);
        cmdCancelar = (Button) findViewById(R.id.cmdCancelar);
        lblFactura = (TextView) findViewById(R.id.lblFactura);

        setHandlers();

        txtOrdenCompra.setText("");
    }

    private void setHandlers() {

        try{
            txtOrdenCompra.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                guardar();
                                return true;
                        }
                    }
                    return false;
                }
            });

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    public void aceptar(View view) {
        guardar();
    }

    public void guardar(){
        String sOrdenCompra;

        try{
            sOrdenCompra=txtOrdenCompra.getText().toString();

            if (mu.emptystr(sOrdenCompra.trim())) {
                msgbox("La orden de compra debe ser distinta de vacía");return;
            }else if (sOrdenCompra.trim().equals("0")) {
                msgbox("La orden de compra debe ser distinta de 0");return;
            }

            gl.ordenCompra = sOrdenCompra;

            finish();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void cancelar(View view) {

        try{
            finish();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

}
