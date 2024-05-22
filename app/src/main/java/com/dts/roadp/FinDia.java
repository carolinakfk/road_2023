package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

//  validaFinDia - Fin de Dia ya fue efectuado 
//  devproductos - delete from P_stock


public class FinDia extends PBase {

    private TextView lbl1;
    private ImageView img1;
    private ProgressBar pBar;

    private AppMethods app;
    private clsRepBuilder rep;
    private Runnable printclose;
    private printer prn;

    private String rutatipo, fserie, sp, devcorel;
    private int corelz, fac, faca, cfac, cfaca, rec, reca, ptot, ped, peda, fcorel, mw,pedanul;
    private double val, tot, tte, ttc, ttk, tto, tre, trc, tro, tote, totc, depe, depc, bale, balc;
    private boolean idle = true, fullfd, fail;
    private clsFinDia claseFinDia;

    private double gSumados=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_dia);

        super.InitBase();
        addlog("FinDia",""+du.getActDateTime(),gl.vend);

        lbl1 = (TextView) findViewById(R.id.textView17);
        img1 = (ImageView) findViewById(R.id.imageView9);
        pBar = (ProgressBar) findViewById(R.id.progressBar1);
        pBar.setVisibility(View.INVISIBLE);

        rutatipo = gl.rutatipog;

        app = new AppMethods(this, gl, Con, db);

        gl.validimp=app.validaImpresora();
        //if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        gl.devfindia=app.getDevolBod();

        fullfd = false;
        if (rutatipo.equalsIgnoreCase("T")) {
            fullfd = true;
        } else {
            if (rutatipo.equalsIgnoreCase("V") ||
                rutatipo.equalsIgnoreCase("D")  ||
                rutatipo.equalsIgnoreCase("C")) fullfd = true;
            else fullfd = false;
        }

        if (gl.banderafindia) {
            lbl1.setVisibility(View.VISIBLE);
            img1.setVisibility(View.VISIBLE);
        } else {
            lbl1.setVisibility(View.INVISIBLE);
            img1.setVisibility(View.INVISIBLE);
        }

        rep = new clsRepBuilder(this, gl.prw, false, gl.peMon, gl.peDecImp, "");
        mw = 6 + gl.peDecImp + 7;

        printclose = new Runnable() {
            public void run() {
                FinDia.super.finish();
            }
        };

        prn = new printer(this, printclose,gl.validimp);

    }

    //region Events

    public void iniciaCierre(View view) {
       iniciaCierreDia();
    }

    public void iniciaCierreDia(){

        try{

            gl.enviaMov =false;

            //#HS_20181128_0906 Agregue validacion para FinDia.
            if (gl.banderafindia) {
                //if (validaFinDia()) #CKFK 20190305 Quité la validación de aquí
                if (!yaInicioFinDia())  {
                    msgAskFinDiaTrue();
                }else{
                    validaFinDia();
                }
            } else {
                //if (validaFinDia()) #CKFK 20190305 Quité la validación de aquí
                msgAsk();
            }
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

    //region Main

	public void startFDD()  {

		boolean rslt;

		File fd=new File(Environment.getExternalStorageDirectory()+"/SyncFold/findia.txt");
		FileUtils.deleteQuietly(fd);

        idle = false;
        fail = false;

        try{
            if (!gl.peModal.equalsIgnoreCase("TOL")) {
                buildReports();
            }

            if (fail) rslt = false;
            else rslt = completeProcess();

            pBar.setVisibility(View.INVISIBLE);
            idle = true;

            if (!rslt) {
                msgExit("Proceso cierre del día falló. Intente de nuevo, por favor.");
                delPrintFiles();
                return;
            }

            try {
                File f1 = new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt");
                File f2 = new File(Environment.getExternalStorageDirectory() + "/print.txt");
                if (f1.exists()){
                    FileUtils.copyFile(f1, f2);
                }
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                msgExit("No se pudo crear archivo de impresión.");
                return;
            }

            //#CKFK 20190304 Agregué validación para verificar si ya se realizó la comunicación de los datos.
            if (gl.banderafindia) {
                if (claseFinDia.getComunicacion() != 2) {
                    msgAskComunicacion();
                    return;
                }
            }

            if (!gl.banderafindia) {
                db.execSQL("UPDATE FinDia SET val1="+du.getActDate());
            }

            Toast.makeText(FinDia.this, "Cierre del día completo.", Toast.LENGTH_SHORT).show();

            gl.findiaactivo=true;
            gl.modoadmin = false;
            gl.autocom = 1;

            startActivity(new Intent(this, ComWS.class));

            try  {

                if (prn.isEnabled())  {
                    final Handler shandler = new Handler();
                    shandler.postDelayed(new Runnable() {
                        @Override
                        public void run()  {
                            gl.prdlgmode=1;
                            Intent intent = new Intent(FinDia.this, PrintDialog.class);
                            startActivity(intent);
                        }
                    }, 2000);
                }
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            }

            FinDia.super.finish();

        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public boolean completeProcess() {
        Cursor DT;
        String corel;

        try {

            db.beginTransaction();

            sql = "SELECT COREL FROM D_FACTURA WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_FACTURA WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAP WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAD_LOTES WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAF WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURA_STOCK WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURA_BARRA WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAD_MODIF WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_STOCKB_DEV WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_LOTES WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_STOCK WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_BARRA WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_REL_PROD_BON WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIFFALT";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_CANASTA WHERE CORELTRANS='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_PEDIDO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_PEDIDO WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_PEDIDOD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    sql = "DELETE FROM D_BONIF";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_REL_PROD_BON";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIFFALT";
                    db.execSQL(sql);

                    //CKFK 20210828 Agregué el limpiar la tabla del inventario de Preventa
                    // cuando se hace el fin de día
                    sql = "DELETE FROM P_STOCK_PV";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql = "SELECT COREL FROM D_COBRO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_COBRO WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_COBROD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_COBROP WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_DEPOS WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_DEPOS WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_DEPOSD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_MOV WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql="DELETE FROM D_MOV WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVD WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDB WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDCAN WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDPALLET WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            //Delete D_NOTACRED y D_NOTACRED
            sql = "SELECT COREL FROM D_NOTACRED WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql="DELETE FROM D_NOTACRED WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql="DELETE FROM D_NOTACREDD WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE COREL='" + corel + "'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            //Delete D_CXC y D_CXCD
            sql = "SELECT COREL FROM D_CXC WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql="DELETE FROM D_CXC WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql="DELETE FROM D_CXCD WHERE COREL='" + corel + "'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            //sql="DELETE FROM D_MOVD WHERE CODIGOLIQUIDACION=0";db.execSQL(sql);


            sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_CLINUEVO WHERE CODIGO='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_CLINUEVO_APR WHERE CODIGO='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql = "SELECT COREL FROM D_CLIENTE_MODIF WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_CLIENTE_MODIF WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql = "SELECT CODIGO FROM D_CLINUEVOT WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_CLINUEVOT WHERE CODIGO='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql = "SELECT COREL FROM D_SOLICINV WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_SOLICINV WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_SOLICINVD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "DELETE FROM D_ATENCION";
            db.execSQL(sql);
            sql = "DELETE FROM D_CLICOORD WHERE STATCOM='S'";
            db.execSQL(sql);

			/*sql="UPDATE P_RUTA SET Email='0'";
			db.execSQL(sql);*/

            sql = "DELETE FROM D_CANASTA WHERE STATCOM= 'S'";
            db.execSQL(sql);

            sql = "DELETE FROM D_DESPACHOD_NO_ENTREGADO WHERE STATCOM= 'S'";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            //mu.msgbox("Error : " + e.getMessage());
            return false;
        }

        return true;
    }

    private void buildReports() {

        rep.empty();

		rep.add("CIERRE DEL DIA");
		rep.line();
		rep.add("Vendedor : "+ gl.vend+" "+gl.vendnom);
		rep.add("Fecha : "+du.sfecha(fecha)+" "+du.shora(fecha));
        rep.empty();

        if (fullfd) {
            rep.line();
            rep.add("INFORME Z #" + corelz);
        }

        repFacturas();
        if (gl.peModal.equalsIgnoreCase("APR")) repFacturasCredito();
        repPedidos();
        repProductos();
        repTotales();

        if (fullfd) {
            rep.empty();
            rep.line();
            rep.add("Siguiente factura : " + fserie + " - " + fcorel);
            rep.empty();
            rep.add("Siguiente Informe Z : " + mu.CStr(corelz + 1));
            rep.line();
        }

        rep.empty();
        rep.empty();
        rep.empty();

        try {

            sql = "DELETE FROM D_REPFINDIA";
            db.execSQL(sql);

            for (int i = 0; i < rep.items.size(); i++) {
                s = rep.items.get(i).trim();

                sql = "INSERT INTO D_REPFINDIA VALUES ('" + gl.ruta + "'," + i + ",'" + s + "')";
                db.execSQL(sql);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        rep.save();

    }

    private boolean Inserta_Enc_D_Mov() {

        String corel;
        claseFinDia = new clsFinDia(this);
        int i;

        corel=gl.ruta+"_"+mu.getCorelBase();devcorel=corel;
        gl.corel_d_mov=corel;

        try {

            db.beginTransaction();
            ins.init("D_MOV");
            ins.add("COREL",corel);
            ins.add("RUTA",((appGlobals) vApp).ruta);
            ins.add("ANULADO","N");
            ins.add("FECHA",fecha);
            ins.add("TIPO","D");
            ins.add("USUARIO",((appGlobals) vApp).vend);
            ins.add("REFERENCIA","Devolucion");
            ins.add("STATCOM","N");
            ins.add("IMPRES",0);
            ins.add("CODIGOLIQUIDACION",0);
            db.execSQL(ins.sql());

            sql="UPDATE FinDia SET val5 = 1";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

            return true;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            //mu.msgbox( e.getMessage());
            return false;
        }

    }

    private boolean validaFinDia() {
        Cursor DT;
        int pend;
        long fechaUltimoCierre;

        claseFinDia = new clsFinDia(this);

        try{

            fechaUltimoCierre = claseFinDia.ultimoCierreFecha();

            if (fullfd) {

                if (rutatipo.equals("C") && claseFinDia.getCantCanasta() == 0) {
                    msgExit("No hay canastas, no se puede realizar el Fin de Día");
                    return false;
                }

                if (claseFinDia.getCantFactura() == 0 && !rutatipo.equals("C")) {
                    msgExit("No hay facturas, no se puede realizar el Fin de Día");
                    return false;
                }

                if (!validaPagosPend()) {
                    msgPendPago("Existen facturas pendientes de pago. No se puede realizar fin del día");
                    return false;
                }

                //#CKFK 20210824 Valida si existen despachos pendientes de entregar
                if (rutatipo.equals("D")){
                    if (!validaDespachosPendientes()) {
                        msgDespachosPendientesEntrega("Existen prefacturas pendientes de entrega. No se puede realizar fin del día");
                        return false;
                    }
                }

                if (gl.peModal.equalsIgnoreCase("APR")) {
                    setFactCor();
                    if (fcorel == 0) {
                        msgExit("No Están definidos los correlativos de factura.");
                        return false;
                    }
                }

                corelz = claseFinDia.setCorrelZ();
                if (corelz == 0) {
                    //msgExit("No esta definido correlativo de cierre Z.");return false;
                    claseFinDia.updateCorrelativoZ(1);
                }
            }

            if (du.getActDate() == fechaUltimoCierre) {
                msgExit("Fin de Día ya fue efectuado el día de hoy");

                Toast.makeText(FinDia.this, "Cierre del día completo.", Toast.LENGTH_SHORT).show();

                gl.findiaactivo=true;
                gl.modoadmin = false;
                gl.autocom = 1;

                FinDia.super.finish();

                return false;
            }

            if (gl.banderafindia == true) {

                //#CKFK 20190304 Agregué validación para verificar si ya se realizó la devolución a Bodega.
                if (claseFinDia.getDevBodega() != 5 ){
                    if (!Ya_Realizo_Devolucion()){
                        msgAskDevInventario();
                        return false;
                    }
                }

                //#CKFK 20190305 Agregué validación para verificar si ya se realizó el depósito
                if ((claseFinDia.getDeposito() != 4) && (claseFinDia.getDocPendientesDeposito()>0)) {
                    msgAskDeposito();
                    return false;
                }

                //#CKFK 20190304 Agregué validación para verificar si ya se realizó la impresión del depósito.
                if (gl.impresora.equalsIgnoreCase("N")) {
                    claseFinDia.updateImpDeposito(3);
                } else {
                    if (claseFinDia.getImpresionDeposito() <1) {

                         totDeposito();
                         if ((depe+depc)>0){
                             msgAskImpresionDeposito();
                             return false;
                         }else{
                             claseFinDia.updateImpDeposito(3);
                         }
                    }
                }

                //#CKFK 20190304 Agregué validación para verificar si ya se generó el cierreZ.
                if ((claseFinDia.getGeneroCierreZ()!=6) || (claseFinDia.getImprimioCierreZ()!=7)){
                    msgAskGeneraCierreZ();
                    return false;
                }

                //#CKFK 20190304 Agregué validación para verificar si ya se realizó la comunicación de los datos.
                if (claseFinDia.getComunicacion() != 2) {
                        msgAskComunicacion();
                        return false;
                }

            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

        return true;
    }

    private boolean validaPagosPend() {
        Cursor dt;

        try {
            sql = "SELECT DISTINCT CLIENTE FROM D_FACTURA WHERE (ANULADO='N') AND (COREL NOT IN " +
                    "(SELECT DISTINCT D_FACTURA_1.COREL " +
                    "FROM D_FACTURA AS D_FACTURA_1 INNER JOIN " +
                    "D_FACTURAP ON D_FACTURA_1.COREL=D_FACTURAP.COREL))";
            dt = Con.OpenDT(sql);

            return (dt.getCount() == 0);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            return false;
        }

    }

    private boolean validaDespachosPendientes() {
        Cursor dt;

        try {

            db.execSQL("UPDATE DS_PEDIDO SET BANDERA= 'S' " +
                       " WHERE CLIENTE NOT IN (SELECT CLIENTE FROM P_CLIRUTA)" +
                       " OR CLIENTE NOT IN (SELECT CODIGO FROM P_CLIENTE)");

            sql = "SELECT COREL FROM DS_PEDIDO WHERE (ANULADO='N') AND BANDERA = 'N'";
            dt = Con.OpenDT(sql);

            return (dt.getCount() == 0);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            return false;
        }

    }

    private boolean imprimeCierreZ(){

        boolean vImprime=false;

        try {

            File f1 = new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt");
            File f2 = new File(Environment.getExternalStorageDirectory() + "/print.txt");
            FileUtils.copyFile(f1, f2);

            if (gl.impresora.equalsIgnoreCase("S")) {
                if (prn.isEnabled())  {
                    final Handler shandler = new Handler();
                    shandler.postDelayed(new Runnable() {
                        @Override
                        public void run()  {
                            Intent intent = new Intent(FinDia.this, PrintDialog.class);
                            startActivity(intent);
                        }
                    }, 2000);
                }
            }else{
                claseFinDia.updateImprimioCierreZ(7);
            }

            vImprime=true;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        return vImprime;

    }

    //endregion

    //region Activities

    public void ActivityDeposito() {
        try{
            Intent deposito = new Intent(this, Deposito.class);
            startActivity(deposito);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void ActivityImpresion(int doctipo) {
        try{
            gl.tipo = doctipo;
            Intent intent = new Intent(this, Reimpresion.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void ActivityComunicacion() {
        try{
            Intent intent = new Intent(this, ComWS.class);
            startActivity(intent);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void ActivityMenu() {
        try{
            Intent menu = new Intent(this, Menu.class);
            startActivity(menu);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }


    //endregion

    //region Reportes

    private void repProductos() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;

        rep.empty();
        rep.addc("PRODUCTOS VENDIDOS");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {
            sql = "SELECT  D_FACTURAD.PRODUCTO, P_PRODUCTO.DESCLARGA, SUM(D_FACTURAD.CANT) AS CANT,D_FACTURAD.UMVENTA, SUM(D_FACTURAD.PESO),D_FACTURAD.UMPESO " +
                    "FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURAD.COREL = D_FACTURA.COREL " +
                    "INNER JOIN P_PRODUCTO ON D_FACTURAD.PRODUCTO = P_PRODUCTO.CODIGO " +
                    "WHERE (D_FACTURA.BANDERA<>'F') AND (D_FACTURA.ANULADO<>'S') " +
                    "GROUP BY D_FACTURAD.PRODUCTO, P_PRODUCTO.DESCLARGA,D_FACTURAD.UMVENTA " +
                    "ORDER BY P_PRODUCTO.DESCLARGA";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2 + " " + s3);
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + "Productos : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.empty();
    }

    private void repFacturas() {
        Cursor DT;
        String s1, s2, s3;

        tot = 0;
        fac = 0;
        faca = 0;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS");
        rep.empty();
        rep.add3lrr("Factura", "Total", "");
        rep.line();

        try {
            sql = "SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0);
                s2 = DT.getString(1);
                s1 = s1 + "-" + s2;
                val = DT.getDouble(2);
                s2 = mu.frmcur(val);
                s3 = DT.getString(3);
                if (s3.equalsIgnoreCase("N")) {
                    s3 = "";
                    tot += val;
                    fac++;
                } else {
                    s3 = "ANULADO";
                    faca++;
                }

                rep.add3lrr(s1, s2, s3);

                DT.moveToNext();

            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + "Facturas : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.add3lrr("Total", mu.frmcur(tot), "");
        rep.empty();
    }

    private void repFacturasCredito() {
        Cursor DT;
        String s1, s2, s3;

        tot = 0;
        cfac = 0;
        cfaca = 0;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS CREDITO");
        rep.empty();
        rep.add3lrr("Factura", "Total", "");
        rep.line();

        try {
            sql = "SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";

            sql = "SELECT D_FACTURA.SERIE, D_FACTURA.CORELATIVO, D_FACTURA.ANULADO, SUM(D_FACTURAP.VALOR) AS TOTAL " +
                    "FROM D_FACTURA INNER JOIN D_FACTURAP ON D_FACTURA.COREL =D_FACTURAP.COREL " +
                    "WHERE  (D_FACTURAP.TIPO='K') GROUP BY D_FACTURA.SERIE, D_FACTURA.CORELATIVO, D_FACTURA.ANULADO " +
                    "ORDER BY D_FACTURA.CORELATIVO";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0);
                s2 = DT.getString(1);
                s1 = s1 + "-" + s2;
                val = DT.getDouble(2);
                s2 = mu.frmval(val);
                s3 = DT.getString(3);
                if (s3.equalsIgnoreCase("N")) {
                    s3 = "";
                    tot += val;
                    cfac++;
                } else {
                    s3 = "ANULADO";
                    cfaca++;
                }

                rep.add3lrr(s1, s2, s3);

                DT.moveToNext();

            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Facturas credito : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.add3lrr("Total", mu.frmval(tot), "");
        rep.empty();
    }

    private void repPedidos() {
        Cursor DT;
        String s1, s2, s3;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE PEDIDOS");
        rep.empty();
        rep.addtot("Cliente", "Total  ");
        rep.line();

        ptot = 0;
        ped = 0;
        peda = 0;

        try {
            sql = "SELECT P_CLIENTE.NOMBRE,D_PEDIDO.TOTAL,D_PEDIDO.ANULADO " +
                    "FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO " +
                    "WHERE (D_PEDIDO.BANDERA<>'F') ORDER BY D_PEDIDO.COREL";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0) + "";
                val = DT.getDouble(1);
                s2 = mu.frmval(val);
                s3 = DT.getString(2);
                if (s3.equalsIgnoreCase("N")) {
                    s2 = s2 + "  ";
                    ptot += val;
                    ped++;
                } else {
                    s2 = s2 + " A";
                    peda++;
                }

                rep.addtot(s1, s2);

                DT.moveToNext();

            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + "Pedidos : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.addtot("Total", mu.frmval(ptot) + "  ");
        rep.empty();
    }

    private void repTotales() {

        try{
            rep.line();
            rep.empty();
            rep.add("LIQUIDACION");
            rep.line();

            rep.addtot("Facturas emitidas", "" + fac);
            rep.addtot("Facturas anuladas", "" + faca);
            rep.addtot("Total facturas", "" + (faca + fac));
            rep.line();

            rep.addtot("Pedidos emitidos", "" + ped);
            rep.addtot("Pedidos anulados", "" + peda);
            rep.addtot("Total pedidos", "" + (peda + ped));
            rep.line();

            totRecibos();

            rep.addtot("Recibos emitidos", "" + rec);
            rep.addtot("Recibos anulados", "" + reca);
            rep.addtot("Total recibos", "" + (reca + rec));

            detVentas();

            rep.empty();
            rep.addtot("Ventas efectivo", tte);
            rep.addtot("Ventas cheque", ttc);
            rep.addtot("Ventas credito", ttk);
            rep.addtot("Ventas otro", tto);
            rep.line();
            tot = tte + ttc + ttk + tto;
            rep.addtot("Ventas total", tot);

            detRecibos();

            rep.empty();
            rep.addtot("Recibos efectivo", tre);
            rep.addtot("Recibos cheque", trc);
            rep.addtot("Recibos otro", tro);
            rep.line();
            tot = tre + trc + tro;
            rep.addtot("Recibos total", tot);

            tote = tte + tre;
            totc = ttc + trc;

            rep.empty();
            rep.addtot("Total efectivo", tote);
            rep.addtot("Total cheque", totc);
            rep.line();
            rep.addtot("Pagos total", tote + totc);

            totDeposito();

            rep.empty();
            rep.addtot("Deposito efectivo", depe);
            rep.addtot("Deposito cheque", depc);
            rep.line();
            rep.addtot("Depositos total", depe + depc);

            bale = tote - depe;
            balc = totc - depc;

            rep.empty();
            rep.addtot("Balance efectivo", bale);
            rep.addtot("Balance cheque", balc);
            rep.line();
            rep.addtot("Balance", (bale + balc));
            rep.line();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void repDevolTotal() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;


        rep.empty();
        rep.addc("DEVOLUCION A BODEGA");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {

            sql = "SELECT  D_MOVD.PRODUCTO, P_PRODUCTO.DESCLARGA, SUM(D_MOVD.CANT) AS CANT,D_MOVD.UNIDADMEDIDA, SUM(D_MOVD.PESO),'' " +
                    "FROM D_MOVD INNER JOIN D_MOV ON D_MOVD.COREL = D_MOV.COREL " +
                    "INNER JOIN P_PRODUCTO ON D_MOVD.PRODUCTO = P_PRODUCTO.CODIGO  " +
                    "WHERE (D_MOV.COREL='" + devcorel + "') AND ((D_MOVD.CANT>0) OR (D_MOVD.PESO>0)) " +
                    "GROUP BY D_MOVD.PRODUCTO, P_PRODUCTO.DESCLARGA,D_MOVD.UNIDADMEDIDA	" +
                    "ORDER BY P_PRODUCTO.DESCLARGA ";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) {
                rep.line();
                rep.empty();
                return;
            }

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2 + " " + s3);
                }

                DT.moveToNext();
            }

            rep.line();
            rep.empty();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Devolucion a bodega : " + e.getMessage());
            fail = true;
        }

    }

    private void repEstadoMalo() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;

        rep.empty();
        rep.addc("INVENTARIO DANADO");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {

            sql = "SELECT  P_STOCK.CODIGO, P_PRODUCTO.DESCLARGA, SUM(P_STOCK.CANTM) AS CANT,P_STOCK.UNIDADMEDIDA, SUM(P_STOCK.PLIBRA),'' " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO = P_PRODUCTO.CODIGO  " +
                    "WHERE ((P_STOCK.CANTM>0) OR (P_STOCK.PLIBRA>0)) " +
                    "GROUP BY P_STOCK.CODIGO, P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA	" +
                    "ORDER BY P_PRODUCTO.DESCLARGA ";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) {
                rep.line();
                rep.empty();
                return;
            }

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

				/*
				s1=DT.getString(1);
				s1=rep.ltrim(s1,rep.prw-14);
				s3=DT.getString(3);s3=rep.ltrim(s3,6);
				val=DT.getDouble(2);s2=mu.frmint(val);
				s2=rep.rtrim(s2,6)+" "+s3;
				val=DT.getDouble(4);s4=mu.frmdec(val);
				ump=DT.getString(5);
				*/

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
					/*
					s1=DT.getString(1);
					s1=rep.ltrim(s1,rep.prw-2);
					rep.add(s1);
					s4=rep.rtrim(s4,8)+" "+rep.rtrim(ump,3);
					s5=s4+" "+s2;
					rep.add(s5);
					*/

                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2);
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Inventario dañado : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.empty();
    }

    //endregion

    //region Toledano Cierre Z

    //CKFK 20190226 Modifiqué este procedimiento a como debe ser el fin de día en Toledano
    private boolean buildReportsTOL() {

        Cursor DT;
        String vCadena;

        boolean vGeneroReportes=false;

        try {

            gSumados=0;

            rep.empty();
            sql = " SELECT CODIGO, EMPRESA, DESCRIPCION, NOMBRE, DIRECCION, TELEFONO, NIT, TEXTO " +
                    " FROM P_SUCURSAL WHERE CODIGO='" + gl.sucur + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                rep.empty();
                vCadena = DT.getString(3);//Nombre
                rep.add(rep.ctrim(vCadena));
                vCadena = "R.U.C: " + DT.getString(6) + " DV." + DT.getString(7);
                rep.add(rep.ctrim(vCadena));
                vCadena = DT.getString(4);//Dirección
                rep.add(rep.ctrim(vCadena));
                vCadena = "# DE SERIE:" + gl.deviceId;
                rep.add(rep.ctrim(vCadena));
                vCadena = StringUtils.leftPad(du.shora(fecha),12) + StringUtils.leftPad(du.sfecha(fecha),20);
                rep.add(rep.ctrim(vCadena));
                rep.empty();
                vCadena = "INFORME Z # " + corelz;
                rep.add(rep.ctrim(vCadena));
                rep.empty();
                vCadena = "VENDEDOR: " + gl.vend;
                rep.add(vCadena);
                rep.empty();

            }

            repPedidosTol();
            repFacturasTol();
            repCobrosTol();
            repNotasCreditoTol();
            repProductos();
            repTotalesTol();

            sql = "DELETE FROM D_REPFINDIA";
            db.execSQL(sql);

            for (int i = 0; i < rep.items.size(); i++) {
                s = rep.items.get(i).trim();

                sql = "INSERT INTO D_REPFINDIA VALUES ('" + gl.ruta + "'," + i + ",'" + s + "')";
                db.execSQL(sql);
            }

            if (rep.save()){
                claseFinDia.updateGeneroCierreZ(6);
            }

            vGeneroReportes=true;

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        return vGeneroReportes;

    }

    //CKFK 20190226 Agregué esta función para generar el listado de facturas de Toledano
    private void repFacturasTol() {
        Cursor DT;
        String s1, s2, s3;
        String vComunicacion = "";
        String vAuxCorel, vCadena;
        double sumagrav, sumaimp, sumanograv, totporfila, totgrav, totnograv, TotItbm, i, sumados;
        boolean anulada = false;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS");
        rep.line();
        rep.empty();
        rep.add("No. Doc");
        rep.add("  GRAV.  NO.GR  ITBM   Total  TP");
        rep.line();

        try {

            //IIf(Not CellCom Or gFinDia, " WHERE F.STATCOM = 'N' ", " ")
            if ((!gl.CellCom) || (gl.banderafindia)) {
                vComunicacion = " AND F.STATCOM = 'N' ";
            }

            //sql="SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";
            sql = " SELECT F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP > 0 " + vComunicacion +
                    " GROUP BY F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO " +
                    " UNION SELECT F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " 0 AS GRAVADO, SUM(D.TOTAL) AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP = 0 " + vComunicacion +
                    " GROUP BY F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                totgrav = 0;
                totnograv = 0;
                TotItbm = 0;
                sumados = 0;
                vAuxCorel = "";
                sumagrav = 0;
                sumanograv = 0;
                totporfila=0;
                sumaimp = 0;

                i=0;

                while (!DT.isAfterLast()) {

                    s1 = DT.getString(2);

                    if (!vAuxCorel.equalsIgnoreCase(s1)) {
                        vAuxCorel = s1;
                        totporfila = 0;

                        anulada = (DT.getString(5).equalsIgnoreCase("S"));

                        vCadena = DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)), 6);

                        if (anulada) vCadena += " - ANULADA";

                        rep.add(vCadena);

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(7);
                            totgrav = totgrav + DT.getDouble(6);
                            TotItbm = TotItbm + DT.getDouble(4);
                        }

                        totporfila = DT.getDouble(3);//Campo Total
                        sumaimp = DT.getDouble(4);//Campo ImpMonto
                        sumanograv = DT.getDouble(7);//Campo No Gravado
                        sumagrav = DT.getDouble(6);;//Campo Gravado

                        if ((i + 1) <= (DT.getCount() - 1)) {

                            DT.moveToNext();

                            if (!vAuxCorel.equalsIgnoreCase(DT.getString(0))){
                                vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 8);
                                vCadena +=  StringUtils.leftPad(mu.frmcur_sm(sumanograv), 8);
                                vCadena +=  StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8);
                                vCadena +=   StringUtils.leftPad(mu.frmcur_sm(totporfila), 6) + " F";
                                rep.add(vCadena);
                            }

                            DT.moveToPrevious();

                        }  else{

                            vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 8);
                            vCadena +=  StringUtils.leftPad(mu.frmcur_sm(sumanograv), 8);
                            vCadena += StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8);
                            vCadena +=  StringUtils.leftPad(mu.frmcur_sm(totporfila), 6) + " F";
                            rep.add(vCadena);
                        }
                    }
                    else {

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(5);
                            totgrav = totgrav + DT.getDouble(6);
                        }

                        sumanograv = sumanograv + DT.getDouble(7);//NoGravado
                        sumagrav = sumagrav + DT.getDouble(6);//Gravado

                        vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 8);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(sumanograv), 8);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(totporfila), 6) + " F";
                        rep.add(vCadena);

                    }

                    i += 1;

                    DT.moveToNext();
                }

                rep.line();

                sumados = totgrav + totnograv + TotItbm;
                gSumados+=sumados;

                vCadena = "Total";
                rep.add(vCadena);
                vCadena = StringUtils.leftPad(mu.frmcur_sm(totgrav), 10, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(totnograv), 7);
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(TotItbm), 8);
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(sumados), 7);
                rep.add(vCadena);
            }


        } catch(Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Facturas : " + e.getMessage());
            fail = true;
        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de pedidos de Toledano
    private void repPedidosTol() {

        Cursor DT;
        String s1;
        String vComunicacion = "";
        String vAuxCorel, vCadena;
        double sumagrav, sumaimp, sumanograv, totporfila, totgrav, totnograv, TotItbm, sumados, i;
        boolean anulada = false;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE PEDIDOS");
        rep.line();
        rep.empty();
        rep.add("No. Doc");
        rep.add("      GRAV.  NO.GR  ITBM   Total  TP");
        rep.line();

        try {

            if ((!gl.CellCom) || (gl.banderafindia)) {
                vComunicacion = " AND F.STATCOM = 'N' ";
            }
//SELECT F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
//                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +

            sql = " SELECT '' AS SERIE,'' AS CORELATIVO,F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +
                    " FROM D_PEDIDO F INNER JOIN D_PEDIDOD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP > 0 " + vComunicacion +
                    " GROUP BY F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO " +
                    " UNION SELECT '' AS SERIE,'' AS CORELATIVO,F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " 0 AS GRAVADO, SUM(D.TOTAL) AS NO_GRAVADO " +
                    " FROM D_PEDIDO F INNER JOIN D_PEDIDOD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP = 0 " + vComunicacion +
                    " GROUP BY F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                totgrav = 0;
                totnograv = 0;
                TotItbm = 0;
                sumados = 0;
                vAuxCorel = "";
                sumagrav = 0;
                sumanograv = 0;
                totporfila=0;
                sumaimp = 0;

                i=0;

                while (!DT.isAfterLast()) {

                    s1 = DT.getString(2);

                    if (!vAuxCorel.equalsIgnoreCase(s1)) {
                        vAuxCorel = s1;
                        totporfila = 0;

                        anulada = (DT.getString(5).equalsIgnoreCase("S"));

                        vCadena = DT.getString(2);

                        if (anulada) vCadena += " - ANULADO";

                        rep.add(vCadena);

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(7);
                            totgrav = totgrav + DT.getDouble(6);
                            TotItbm = TotItbm + DT.getDouble(4);
                        }

                        totporfila = DT.getDouble(3);//Campo Total
                        sumaimp = DT.getDouble(4);//Campo ImpMonto
                        sumanograv = DT.getDouble(7);//Campo No Gravado
                        sumagrav = DT.getDouble(6);;//Campo Gravado

                        if ((i + 1) <= (DT.getCount() - 1)) {

                            DT.moveToNext();

                            if (!vAuxCorel.equalsIgnoreCase(DT.getString(0))){
                                vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 8);
                                vCadena += StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9);
                                vCadena += StringUtils.leftPad(mu.frmcur_sm(sumaimp),8);
                                vCadena +=  StringUtils.leftPad(mu.frmcur_sm(totporfila), 9) + " F";
                                rep.add(vCadena);
                            }

                            DT.moveToPrevious();

                        }  else{

                            vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 8);
                            vCadena +=  StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9);
                            vCadena += StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8);
                            vCadena +=  StringUtils.leftPad(mu.frmcur_sm(totporfila), 9) + " F";
                            rep.add(vCadena);
                        }
                    }
                    else {

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(5);
                            totgrav = totgrav + DT.getDouble(6);
                        }

                        sumanograv = sumanograv + DT.getDouble(7);//NoGravado
                        sumagrav = sumagrav + DT.getDouble(6);//Gravado

                        vCadena = StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8);
                        vCadena += StringUtils.leftPad(mu.frmcur_sm(totporfila), 9) + " F";
                        rep.add(vCadena);

                    }
                    i += 1;

                    DT.moveToNext();
                }

                rep.line();

                sumados = totgrav + totnograv + TotItbm;
                gSumados+=sumados;

                vCadena = "Total";
                rep.add(vCadena);
                vCadena = StringUtils.leftPad(mu.frmcur_sm(totgrav), 10);
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(totnograv), 9);
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(TotItbm), 8);
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur_sm(sumados), 9);
                rep.add(vCadena);
            }


        } catch(Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Pedidos: " + e.getMessage());
            fail = true;
        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de cobros de Toledano
    private void repCobrosTol(){

        String vCadena;
        Cursor DT;
        float TotalRecibos = 0;
        boolean anulado;

        try{

            vCadena = "LISTADO DE COBROS";
            rep.add(vCadena);
            rep.line();

            vCadena= "No. Rec "  + StringUtils.leftPad("Total",10)  + StringUtils.leftPad("E",12);
            rep.add(vCadena);

            sql = "SELECT COREL, TOTAL, ANULADO FROM D_COBRO WHERE STATCOM='N'";
            DT = Con.OpenDT(sql);

            if (DT != null){

                if (DT.getCount() > 0) {

                    DT.moveToFirst();

                    while (!DT.isAfterLast()) {

                        anulado = (DT.getString(2).equalsIgnoreCase("S"));

                        vCadena = DT.getString(0);

                        if (!anulado) TotalRecibos += DT.getDouble(1);

                        vCadena += StringUtils.leftPad(mu.frmcur_sm(DT.getDouble(1)), 24);
                        if (anulado)  vCadena += "  A";

                        rep.add(vCadena);

                        DT.moveToNext();
                    }
                }

            }

            rep.line();
            vCadena = "Total:  " + StringUtils.leftPad(mu.frmcur_sm(TotalRecibos), 24);
            rep.add(vCadena);
            rep.empty();

        }catch (Exception ex){
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Cobros: " + ex.getMessage());
            fail = true;
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de notas de crédito de Toledano
    private void repNotasCreditoTol(){

        Cursor DT;
        String vCadena;
        boolean anulada;
        double TotalNotaCred = 0;
        double TotalNotaCredCred = 0;
        double TotalNotaCredCont = 0;

        try{

            vCadena = "LISTADO DE NOTAS DE CREDITO";
            rep.add(vCadena);
            rep.line();

            vCadena = "No. N.C " + StringUtils.leftPad("Total",10) + StringUtils.leftPad("E",12);
            rep.add(vCadena);

            vCadena = "AFECTAN CREDITO";
            rep.add(vCadena);
            rep.line();

            sql = " SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N, D_FACTURAP F " +
                    " WHERE N.FACTURA = F.COREL AND N.STATCOM='N'  AND F.TIPO = 'K' AND N.TIPO_DOCUMENTO = 'NC'  " +
                    " UNION SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N, D_CXC C " +
                    " WHERE N.FACTURA = C.COREL AND N.STATCOM='N' AND N.TIPO_DOCUMENTO = 'NC' ";

            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    anulada = (DT.getString(2).equalsIgnoreCase("S"));

                    vCadena = DT.getString(0);

                    if (!anulada) {
                        TotalNotaCred += DT.getDouble(1);
                        TotalNotaCredCred += DT.getDouble(1);
                    }

                    vCadena += StringUtils.leftPad(mu.frmcur_sm(DT.getDouble(1)), 20);
                    if (anulada)  vCadena += "  A";

                    rep.add(vCadena);

                    DT.moveToNext();
                }
            }

            vCadena = "AFECTAN CONTADO";
            rep.add(vCadena);
            rep.line();

            sql = " SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N " +
                    " WHERE N.STATCOM='N' AND N.TIPO_DOCUMENTO = 'NC' AND " +
                    " N.FACTURA IN (SELECT COREL FROM D_FACTURAP WHERE TIPO <> 'K')";

            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    anulada = (DT.getString(2).equalsIgnoreCase("S"));

                    vCadena = DT.getString(0);

                    if (!anulada) {
                        TotalNotaCred +=  DT.getDouble(1);
                        TotalNotaCredCont += DT.getDouble(1);
                    }

                    vCadena += StringUtils.leftPad(mu.frmcur_sm(DT.getDouble(1)), 24);
                    if (anulada)  vCadena += "  A";

                    rep.add(vCadena);

                    DT.moveToNext();
                }
            }

            rep.line();
            vCadena = "Total NC Credito:  " + StringUtils.leftPad( mu.frmcur_sm(TotalNotaCredCred), 13);
            rep.add(vCadena);
            vCadena = "Total NC Contado:  " + StringUtils.leftPad( mu.frmcur_sm(TotalNotaCredCont), 13);
            rep.add(vCadena);
            vCadena = "Total:             " + StringUtils.leftPad( mu.frmcur_sm(TotalNotaCred), 13);
            rep.add(vCadena);
            rep.empty();

        }catch (Exception ex){
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Notas de crédito: " + ex.getMessage());
            fail = true;
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);

        }

    }

    //CKFK 20190226 Agregué esta función para generar los totales del Cierre Z
    private void repTotalesTol() {

        Cursor DT;
        String vCadena, vComunicacion;
        double totgrav = 0;
        double totnograv = 0;
        double TotItbm = 0;
        double sumados = 0;

        try{

            vComunicacion = "";

            vCadena = "ACUMULADOS A LA FECHA";
            rep.add(vCadena);
            rep.empty();

            if ((!gl.CellCom) || (gl.banderafindia)) {
                vComunicacion = " AND F.STATCOM = 'N' ";
            }
            sql = " SELECT IFNULL(SUM(S.IMPMONTO),0) AS IMPMONTO, IFNULL(SUM(S.GRAVADO),0) AS GRAVADO, " +
                    " IFNULL(SUM(S.NO_GRAVADO),0) AS NO_GRAVADO " +
                    " FROM (SELECT SUM(D.IMP) AS IMPMONTO, " +
                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP > 0 " + vComunicacion +
                    " AND F.ANULADO = 'N' " +
                    " UNION SELECT SUM(D.IMP) AS IMPMONTO, " +
                    " 0 AS GRAVADO, SUM(D.TOTAL) AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    " WHERE D.IMP = 0 " + vComunicacion +
                    " AND F.ANULADO = 'N') S ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                totnograv = totnograv + DT.getDouble(2);
                totgrav = totgrav + DT.getDouble(1);
                TotItbm = TotItbm + DT.getDouble(0);
                sumados = totnograv + totgrav + TotItbm;
            }

            vCadena = "Ventas Gravadas       :" + StringUtils.leftPad(mu.frmcur_sm(totgrav), 13);
            rep.add(vCadena);
            vCadena = "Ventas No Gravadas    :" + StringUtils.leftPad(mu.frmcur_sm(totnograv), 13);
            rep.add(vCadena);
            vCadena = "Acumulado ITBM        :" + StringUtils.leftPad(mu.frmcur_sm(TotItbm), 13);
            rep.add(vCadena);
            vCadena = "GT                    :" + StringUtils.leftPad(mu.frmcur_sm(sumados), 13);
            rep.add(vCadena);
            rep.empty();

            String CorelZ = "";
            int vFacturasCredito = FacturasCredito();
            int vFacturasContado = FacturasContado();
            int vFacturasAnuladas = FacturasAnuladas();
            int vTotalFacturas = TotalFacturas();
            int vTotRecibos = TotRecibos();
            int vRecibosAnulados=RecibosAnulados();
            int vNotasCredito=NotasCredito();
            int vNotasCreditoAnuladas=NotasCreditoAnuladas();
            double vTotalNC_Credito = TotNotaC_Credito();
            double vTotalNC_Contado = TotNotaC_Contado();
            double vTotalCredito = TotalCredito2();
            double vTotalContado = TotalEfectivo2();
            double TotalRecibos=TotalRecibos();
            int corelativoZ = 0;

            //Totales para liquidacion
            vCadena = "Facturas Credito  :" + StringUtils.leftPad(mu.frmint(vFacturasCredito), 13);
            rep.add(vCadena);
            vCadena = "Facturas Contado  :" + StringUtils.leftPad(mu.frmint(vFacturasContado), 13);
            rep.add(vCadena);
            vCadena = "Facturas Anuladas :" + StringUtils.leftPad(mu.frmint(vFacturasAnuladas), 13);
            rep.add(vCadena);
            vCadena = "Cantidad Facturas :" + StringUtils.leftPad(mu.frmint(vTotalFacturas), 13);
            rep.add(vCadena);
            vCadena = "Recibos           :" + StringUtils.leftPad(mu.frmint(vTotRecibos), 13);
            rep.add(vCadena);
            vCadena = "Recibos anulados  :" + StringUtils.leftPad(mu.frmint(vRecibosAnulados), 13);
            rep.add(vCadena);
            vCadena = "Cantidad de NC    :" + StringUtils.leftPad(mu.frmint(vNotasCredito), 13);
            rep.add(vCadena);
            vCadena = "Cant. NC anuladas :" + StringUtils.leftPad(mu.frmint(vNotasCreditoAnuladas), 13);
            rep.add(vCadena);
            rep.empty();

            vCadena = "Venta Credito     :" + StringUtils.leftPad(mu.frmcur_sm(vTotalCredito), 13);
            rep.add(vCadena);
            vCadena = "Total NC Credito  :" + StringUtils.leftPad(mu.frmcur_sm(vTotalNC_Credito), 13);
            rep.add(vCadena);
            vCadena = "Total Credito     :" + StringUtils.leftPad(mu.frmcur_sm(vTotalCredito - vTotalNC_Credito), 13);
            rep.add(vCadena);
            vCadena = "Venta Contado     :" + StringUtils.leftPad(mu.frmcur_sm(vTotalContado), 13);
            rep.add(vCadena);
            vCadena = "Total NC Contado  :" + StringUtils.leftPad(mu.frmcur_sm(vTotalNC_Contado), 13);
            rep.add(vCadena);
            vCadena = "Total Contado     :" + StringUtils.leftPad(mu.frmcur_sm(vTotalContado - vTotalNC_Contado), 13);
            rep.add(vCadena);
            vCadena = "Venta Total       :" + StringUtils.leftPad(mu.frmcur_sm(vTotalCredito + vTotalContado), 13);
            rep.add(vCadena);
            vCadena = "Gran Total        :" + StringUtils.leftPad(mu.frmcur_sm(vTotalCredito + vTotalContado), 13);
            rep.add(vCadena);
            vCadena = "Total Recibos     :" + StringUtils.leftPad(mu.frmcur_sm(TotalRecibos), 13);
            rep.add(vCadena);
            rep.empty();

            sql = "select serie,corelult from p_corel";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                vCadena = "Sig. Factura      :" + StringUtils.leftPad(DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)+1), 6),13);
            }else{
                vCadena = "Sig. Factura      :" + StringUtils.leftPad("0", 13);
            }
            rep.add(vCadena);

            sql = "SELECT SERIE, ACTUAL FROM P_CORRELREC";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0){
                DT.moveToFirst();
                vCadena = "Sig. Recibo       :" + StringUtils.leftPad(DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)+1), 6),13);
            }else{
                vCadena = "Sig. Recibo       :" + StringUtils.leftPad("0", 13);
            }
            rep.add(vCadena);

            sql = "SELECT SERIE, ACTUAL FROM P_CORREL_OTROS WHERE TIPO = 'NC'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0){
                DT.moveToFirst();
                vCadena = "Sig. Nota Credito :" + StringUtils.leftPad(DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)+1), 6),13);
            }else{
                vCadena = "Sig. Nota Credito :" + StringUtils.leftPad("0", 13);
            }
            rep.add(vCadena);

            corelativoZ = corelz + 1;
            vCadena = "Sig. Informe Z    :" + StringUtils.leftPad(mu.frmint(corelativoZ), 13);

            rep.add(vCadena);

        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
            fail = true;
        }

    }

    private int FacturasCredito(){

        Cursor DT;
        int vFacturasCredito = 0;

        try{

            sql = " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_FACTURA F " +
                    " WHERE F.COREL IN (SELECT P.COREL FROM D_FACTURAP P WHERE (P.TIPO = 'K') " +
                    " AND P.ANULADO='N') AND F.RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vFacturasCredito = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vFacturasCredito;
    }

    private int FacturasContado(){

        Cursor DT;
        int vFacturasContado = 0;

        try{

            sql = " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_FACTURA F " +
                    " WHERE F.COREL IN (SELECT P.COREL FROM D_FACTURAP P WHERE (P.TIPO = 'E' OR P.TIPO = 'C')" +
                    " AND P.ANULADO='N') AND F.RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vFacturasContado = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vFacturasContado;
    }

    private int FacturasAnuladas(){

        Cursor DT;
        int vFacturasAnuladas = 0;

        try{

            sql = " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_FACTURA F " +
                    " WHERE F.RUTA='" + gl.ruta + "' AND F.ANULADO='S'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vFacturasAnuladas = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vFacturasAnuladas;
    }

    private int TotalFacturas(){

        Cursor DT;
        int vTotalFacturas = 0;

        try{

            sql = " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_FACTURA F " +
                    " WHERE F.RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotalFacturas = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotalFacturas;
    }

    private int TotRecibos(){

        Cursor DT;
        int vTotRecibos = 0;

        try{

            sql =  " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_COBRO F " +
                    " WHERE F.RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotRecibos = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotRecibos;
    }

    private int RecibosAnulados(){

        Cursor DT;
        int vTotRecibosAnulados = 0;

        try{

            sql =  " SELECT IFNULL(COUNT(F.COREL),0) AS TOTAL " +
                    " FROM D_COBRO F " +
                    " WHERE F.RUTA='" + gl.ruta + "' AND F.ANULADO='S'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotRecibosAnulados = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotRecibosAnulados;
    }

    private int NotasCredito(){

        Cursor DT;
        int vTotNotasCredito = 0;

        try{

            sql =  " SELECT IFNULL(COUNT(NC.COREL),0) AS TOTAL " +
                    " FROM D_NOTACRED NC " +
                    " WHERE NC.RUTA='" + gl.ruta + "' AND NC.TIPO_DOCUMENTO = 'NC' ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotNotasCredito = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotNotasCredito;
    }

    private int NotasCreditoAnuladas(){

        Cursor DT;
        int vTotNotasCredAnul = 0;

        try{

            sql =  " SELECT IFNULL(COUNT(NC.COREL),0) AS TOTAL " +
                    " FROM D_NOTACRED NC " +
                    " WHERE NC.RUTA='" + gl.ruta + "' AND NC.ANULADO='S'  AND NC.TIPO_DOCUMENTO = 'NC' ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotNotasCredAnul = DT.getInt(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotNotasCredAnul;
    }

    private double TotNotaC_Credito(){

        Cursor DT;
        double vTotNotaC_Credito = 0;

        try{

            sql =  " SELECT IFNULL(SUM(NC.TOTAL),0) AS TOTAL " +
                    " FROM D_NOTACRED NC " +
                    " WHERE NC.RUTA='" + gl.ruta + "' AND NC.ANULADO='N' AND NC.TIPO_DOCUMENTO = 'NC'  " +
                    " AND (FACTURA IN (SELECT COREL FROM D_FACTURAP WHERE TIPO = 'K')" +
                    " OR   FACTURA IN (SELECT COREL FROM D_CXC))";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotNotaC_Credito = DT.getDouble(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotNotaC_Credito;
    }

    private double TotNotaC_Contado(){

        Cursor DT;
        double vTotNotaC_Contado = 0;

        try{

            sql =  " SELECT IFNULL(SUM(NC.TOTAL),0) AS TOTAL " +
                    " FROM D_NOTACRED NC " +
                    " WHERE NC.RUTA='" + gl.ruta + "' AND NC.ANULADO='N'  AND NC.TIPO_DOCUMENTO = 'NC'  " +
                    " AND NC.FACTURA IN (SELECT COREL FROM D_FACTURAP WHERE TIPO <> 'K')";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotNotaC_Contado = DT.getDouble(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotNotaC_Contado;
    }

    private double TotalCredito2(){
        Cursor DT;
        double vTotCredito2 = 0;

        try{

            sql = " SELECT IFNULL(SUM(F.TOTAL),0) AS TOTAL " +
                    " FROM D_FACTURAP P, D_FACTURA F " +
                    " WHERE P.TIPO = 'K' AND P.COREL=F.COREL " +
                    " AND F.RUTA='" + gl.ruta + "' AND P.ANULADO='N'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotCredito2 = DT.getDouble(0);
            }
        }catch (Exception ex){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotCredito2;
    }

    private double TotalEfectivo2(){
        Cursor DT;
        double vTotEfectivo2 = 0;

        try{

            sql = " SELECT IFNULL(SUM(F.TOTAL),0) AS TOTAL FROM D_FACTURA F " +
                    " WHERE F.COREL  IN (SELECT COREL FROM D_FACTURAP P WHERE (P.TIPO <>'K') )" +
                    " AND F.RUTA='" + gl.ruta + "' AND F.ANULADO='N' ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotEfectivo2 = DT.getDouble(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotEfectivo2;
    }

    private double TotalRecibos(){

        Cursor DT;
        double vTotalRecibos = 0;

        try{

            sql = " SELECT IFNULL(SUM(C.TOTAL),0) AS TOTAL FROM D_COBRO C " +
                    " WHERE C.RUTA='" + gl.ruta + "' AND C.ANULADO='N' ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                vTotalRecibos = DT.getDouble(0);
            }
        }catch (Exception ex){
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vTotalRecibos;
    }

    //JP 20240520
    private void anularPedidosSinMontoMinimo() {
        Cursor dt;
        String cor;

        try {
            pedanul=0;

            sql = "SELECT COREL FROM D_PEDIDO  WHERE (CUMPLE_MONTO_MINIMO=0) AND (ANULADO='N')";
            dt = Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    cor=dt.getString(0);

                    try {
                        db.beginTransaction();

                        sql="UPDATE D_PEDIDO SET ANULADO='S',ANULADO_POR_MONTO_MINIMO=1 WHERE (COREL='"+cor+"')";
                        db.execSQL(sql);

                        sql="UPDATE D_PEDIDOD SET ANULADO='S' WHERE (COREL='"+cor+"')";
                        db.execSQL(sql);

                        db.setTransactionSuccessful();
                        db.endTransaction();

                        pedanul++;
                    } catch (Exception e) {
                        db.endTransaction();
                        msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
                    }

                    dt.moveToNext();
                }
            }

            if (dt!=null) dt.close();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Dialogos

    private void msgAskDeposito() {

        try{
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

            dialog1.setTitle("Road");
            dialog1.setMessage("No se ha realizado el depósito. ¿Quiere realizar el depósito?");

            dialog1.setIcon(R.drawable.ic_quest);

            dialog1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityDeposito();
                }
            });

            dialog1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            dialog1.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskImpresionDeposito() {

        try{
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

            dialog1.setTitle("Road");
            dialog1.setMessage("Debe imprimir el recibo de depósito. ¿Imprimir depósito?");

            dialog1.setIcon(R.drawable.ic_quest);

            dialog1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityImpresion(2);
                }
            });

            dialog1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            dialog1.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void msgAskComunicacion() {

        try{
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

            dialog1.setTitle("Road");
            dialog1.setMessage("No ha comunicado los datos.¿Quiere comunicar los datos?");

            dialog1.setIcon(R.drawable.ic_quest);

            dialog1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityComunicacion();
                }
            });

            dialog1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            dialog1.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void msgAskGeneraCierreZ() {

        try {
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

            dialog1.setTitle("Road");
            dialog1.setMessage("No ha generado el Cierre Z. ¿Quiere generarlo ahora?");

            dialog1.setIcon(R.drawable.ic_quest);

            dialog1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String cmsg="CIERRE DEL DIA COMPLETO";

                    //JP 20240520
                    anularPedidosSinMontoMinimo();

                    if ( buildReportsTOL()){
                        if (imprimeCierreZ()){
                            corelz+=1;
                            claseFinDia.updateGrandTotalCorelZ(gSumados,corelz);

                            //JP 20240520
                            if (pedanul>0) cmsg+="\n\nSE ANULO PEDIDOS: "+pedanul+"\nPOR RAZÓN DE MONTO MINIMO.";
                            msgExit(cmsg);
                        }
                   } else {
                       msgAskCierreIncompleto("No se pudo generar el reporte Z");
                   }
                }
            });

            dialog1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    msgAskCierreIncompleto("Proceso de fin de día incompleto");
                }
            });

            dialog1.show();
        } catch (Exception e) {
            addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private void msgAskCierreIncompleto(String mensaje) {

        try {
            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

            dialog1.setTitle("Road");
            dialog1.setMessage(mensaje + "¿Quiere volver a intentarlo?");

            dialog1.setIcon(R.drawable.ic_quest);

            dialog1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   msgAskGeneraCierreZ();
                }
            });

            dialog1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });

            dialog1.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private void msgAsk() {

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("Este proceso prepara el sistema para el siguiente día de venta. Continuar ?");

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startFDD();
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

	private void msgAskFinDiaTrue()	{
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("Este proceso prepara el sistema para el siguiente día de venta. Continuar?");
            dialog.setIcon(R.drawable.ic_quest);
            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //#HS_20181121_1453 Se habilito el mensaje de confirmación.
                    msgAsk2();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
	}

	private void msgAsk2()	{

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("¿Está seguro de continuar?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    validaFinDia();//#CKFK 20190305 Agregué esta validación aquí porque aquí es que inicia el proceso
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


	}

	private void msgAskDevInventario() {

        try{
            if (!Ya_Realizo_Devolucion()) {

                browse=1;
                startActivity(new Intent(this,DevolBodTol.class));
                toastlong("No ha efectuado la devolución a bodega,debe proceder a realizarla antes de fin del dia");

            } else  {
                startFDD();
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgExit(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);

            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    FinDia.super.finish();
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgPendPago(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);

            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    gl.filtrocli = 2;
                    startActivity(new Intent(FinDia.this, Clientes.class));
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgDespachosPendientesEntrega(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);

            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    gl.filtrocli = 3;
                    startActivity(new Intent(FinDia.this, Clientes.class));
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

	private void msgAskFlag(View view) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("Limpiar bandera cierre del día?");

            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    db.execSQL("UPDATE FinDia SET val1=0");
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


	}

	//endregion

    //region Aux

	//#EJC 20190226 Creé esta función para saber si hay inventario para devolver
    private boolean Tiene_Inventario_Devolucion(){

        boolean TieneInvDevol = false;

        Cursor DT;

        try
        {

            double vTotalLbsB = 0;
            double vTotalUB = 0;

            //Producto con lote
            sql= " SELECT SUM(ifnull(S.PESO,0)) AS PESOTOT, SUM(ifnull(S.CANT,0))AS CANTUNI " +
            " FROM P_STOCK S, P_PRODUCTO P " +
            " WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO " ;

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB =   DT.getDouble(0);
                vTotalUB = DT.getDouble(1);
            }

            //Producto con HU
            sql = " SELECT SUM(ifnull(S.CANT,0)) AS PESOTOT, COUNT(S.CODIGO)AS CANTUNI " +
            " FROM P_STOCKB S, P_PRODUCTO P " +
            " WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)";

                    /*+
            " AND S.BARRA NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN (" +
            " SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S')) "; */

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB +=   DT.getDouble(0);
                vTotalUB += DT.getDouble(1);
            }

            //Devolución de dañado.
            sql = " SELECT SUM(S.PESO) AS PESOTOT, SUM(S.CANT)AS CANTUNI " +
            " FROM D_CXC E, D_CxCD S, P_PRODUCTO P WHERE E.COREL = S.COREL AND S.CODIGO= P.CODIGO AND E.ANULADO = 'N' ";

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB +=   DT.getDouble(0);
                vTotalUB += DT.getDouble(1);
            }
            //Canastas.
            sql = "SELECT IFNULL(SUM(PESOREC),0) AS PESO, IFNULL(SUM(CANTREC),0) AS CANT" +
                    " FROM D_CANASTA WHERE ANULADO = 0 AND STATCOM = 'N'";
            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB +=   DT.getDouble(0);
                vTotalUB += DT.getDouble(1);
            }

            TieneInvDevol = (vTotalUB > 0);

        }catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            Log.e("TieneInvDevol",e.getMessage());
        }

        return  TieneInvDevol;

    }

    //#EJC 20190226 Creé esta función para saber si ya se había realizado la devolución
    private boolean Ya_Realizo_Devolucion(){

        boolean Ya_Realizo_Devol = false;

        Cursor DT;

        try  {

            boolean vTieneInvDevol = false;

            sql = "SELECT STATCOM FROM D_MOV WHERE TIPO = 'D' AND ANULADO = 'N'";
            DT=Con.OpenDT(sql);

            if (DT.getCount()==0) {

                vTieneInvDevol = Tiene_Inventario_Devolucion();

                if (vTieneInvDevol) {
                    Ya_Realizo_Devol = false;
                    claseFinDia.updateDevBodega(0);

                } else  {
                    Ya_Realizo_Devol = Inserta_Enc_D_Mov();
                    claseFinDia.updateDevBodega(5);
                }

            }else if (claseFinDia.getDevBodega() == 5)  {
                Ya_Realizo_Devol = true;
            }else  {
                Ya_Realizo_Devol = true;
                claseFinDia.updateDevBodega(5);
            }

        }catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            Log.e("TieneInvDevol",e.getMessage());
        }

        return  Ya_Realizo_Devol;

    }

    private boolean yaInicioFinDia(){

        boolean vInicio=false;
        Cursor DT;

        try{

            sql="SELECT val5 FROM findia ";
            DT=Con.OpenDT(sql);

            if (DT!=null){
                if (DT.getCount()>0){
                    DT.moveToFirst();

                    vInicio=((DT.getInt(0)==5));

                }
            }

        }catch (Exception ex){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
        }

        return vInicio;

    }

    private void totRecibos() {
        Cursor DT;
        String s1;

        rec = 0;
        reca = 0;

        try {
            sql = "SELECT ANULADO FROM D_COBRO WHERE BANDERA<>'F'";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                s1 = DT.getString(0);
                if (s1.equalsIgnoreCase("N")) rec++;
                else reca++;

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + "Recibos : " + e.getMessage());
            fail = true;
        }

    }

    private void totDeposito() {
        Cursor DT;

        depe = 0;
        depc = 0;

        try {
            sql = "SELECT SUM(TOTEFEC),SUM(TOTCHEQ) FROM D_DEPOS WHERE CODIGOLIQUIDACION=0 AND ANULADO='N'";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            depe = DT.getDouble(0);
            depc = DT.getDouble(1);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Depositos : " + e.getMessage());
            fail = true;
            depe = 0;
            depc = 0;
        }

    }

    private void detVentas() {
        Cursor DT;
        String s1;
        double val;
        int flag;

        tte = 0;
        ttc = 0;
        ttk = 0;
        tto = 0;

        try {
            sql = "SELECT SUM(D_FACTURAP.VALOR) AS Suma,D_FACTURAP.TIPO " +
                    "FROM D_FACTURAP INNER JOIN D_FACTURA ON D_FACTURAP.COREL = D_FACTURA.COREL " +
                    "GROUP BY D_FACTURAP.TIPO, D_FACTURA.ANULADO, D_FACTURA.BANDERA " +
                    "HAVING (D_FACTURA.ANULADO='N') AND (D_FACTURA.BANDERA<>'F')";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                val = DT.getDouble(0);
                s1 = DT.getString(1);

                flag = 0;
                if (s1.equalsIgnoreCase("E")) {
                    tte += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("C")) {
                    ttc += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("K")) {
                    ttk += val;
                    flag = 1;
                }
                if (flag == 0) tto += val;

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Det. ventas : " + e.getMessage());
            fail = true;
        }

    }

    private void detRecibos() {
        Cursor DT;
        String s1;
        double val;
        int flag;

        tre = 0;
        trc = 0;
        tro = 0;

        try {
            sql = "SELECT SUM(D_COBROP.VALOR) AS Suma,D_COBROP.TIPO " +
                    "FROM D_COBROP INNER JOIN D_COBRO ON D_COBROP.COREL = D_COBRO.COREL " +
                    "GROUP BY D_COBROP.TIPO, D_COBRO.ANULADO, D_COBRO.BANDERA " +
                    "HAVING (D_COBRO.ANULADO='N') AND (D_COBRO.BANDERA<>'F')";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                val = DT.getDouble(0);
                s1 = DT.getString(1);

                flag = 0;
                if (s1.equalsIgnoreCase("E")) {
                    tre += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("C")) {
                    trc += val;
                    flag = 1;
                }
                if (flag == 0) tro += val;

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Det. ventas : " + e.getMessage());
            fail = true;
        }

    }

    private void setFactCor() {
        Cursor DT;

        fcorel = 0;
        fserie = "";

        try {
            sql = "SELECT SERIE,CORELULT FROM P_COREL WHERE RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();

            fserie = DT.getString(0);
            fcorel = DT.getInt(1) + 1;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            fcorel = 0;
            fserie = "";
            return;
        }
    }

    private void delPrintFiles() {
        try {
            new File(Environment.getExternalStorageDirectory() + "/print.txt").delete();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
        try {
            new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt").delete();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    //endregion

	//region Activity Events
	@Override
	public void onBackPressed() {
		if (idle) super.onBackPressed();
	}

    @Override
    protected void onResume() {
        try{

            if (gl.findiaactivo){
                super.finish();
            }

            super.onResume();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion
}