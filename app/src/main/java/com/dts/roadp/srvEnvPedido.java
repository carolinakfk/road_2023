package com.dts.roadp;

import android.content.Intent;

public class srvEnvPedido extends srvBase {

    private wsEnvPedido wscom;

    private Runnable rnEnvioPedido;

    private String command,corel;

    @Override
    public void execute() {
        rnEnvioPedido = new Runnable() {
            public void run() {
                mensajeEnvio();
            }
        };

        wscom =new wsEnvPedido(URL);
        wscom.execute(command,corel,rnEnvioPedido);
    }

    @Override
    public void loadParams(Intent intent) {
        command = intent.getStringExtra("command");
        corel = intent.getStringExtra("correlativo");
    }

    private void mensajeEnvio() {
        if (!wscom.correcto) notification("No se logro enviar pedido.\nRevise la conexion al internet");
    }

}
