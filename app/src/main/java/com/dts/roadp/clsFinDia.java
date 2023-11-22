package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.dts.roadp.clsClasses.clsFinDiaItems;

import java.util.ArrayList;

//#HS_20181121_1548 Se agregó lpa clase para FinDia.
public class clsFinDia extends PBase{

    private ArrayList<clsFinDiaItems> items = new ArrayList<clsFinDiaItems>();

    private int active, corelz;
    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private Context cont;
    String sp;

    public clsFinDia(Context context){
        try{
            cont = context;
            active=0;
            Con = new BaseDatos(context);
            opendatabase();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }
    private AppMethods claseAppMethods;

    private void opendatabase() {
        try {
            db = Con.getWritableDatabase();
            if (db!= null)
            {
                Con.vDatabase =db;
                active=1;
            }else{
                active = 0;
            }
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
            active= 0;
        }
    }

    public int getCantFactura(){
        Cursor DT;
        int result=0;

        try
        {
            sql="SELECT COUNT(COREL) FROM D_FACTURA";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            result = DT.getInt(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getCantFactura: " + e.getMessage());
        }

        return result;
    }

    public int getCantCanasta(){
        Cursor DT;
        int result=0;

        try
        {
            sql="SELECT ifnull(COUNT(*), 0) as CANTIDAD FROM D_CANASTA";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            result = DT.getInt(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getCantCanasta: " + e.getMessage());
        }

        return result;
    }

    //#HS_20181121_1431 Esta función anteriormente pertenecía a la clase FinDia.
    public int setCorrelZ(){
        Cursor DT;
        int corelz=0;

        try {
            sql="SELECT Corel FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                corelz=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("setCorrelZ: "+ e.getMessage());
        }
        return corelz;
    }

    //#HS_20181121_1642 Esta función anteriormente pertenecía a la clase FinDia.
    public long ultimoCierreFecha() {
        Cursor DT;
        long rslt=0;

        try {
            sql="SELECT val1 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getLong(0);
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("ultimoCierreFecha: " + e.getMessage());
        }
        return rslt;
    }

    public int getDevBodega() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val5 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getDevBodega: " + e.getMessage());
        }
        return rslt;
    }

    public void updateDevBodega(int valor){
        try{
            sql="UPDATE FinDia SET val5 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateDevBodega: " + e.getMessage());
        }
    }

    public int getDeposito() {
        Cursor DT;
        int rslt=0;

        try 
        {
            sql="SELECT val4 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getdeposito: " + e.getMessage());
        }
        return rslt;
    }

    public void updateDeposito(int valor){
        try{
            sql="UPDATE FinDia SET val4 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateDeposito: " + e.getMessage());
        }
    }

    public int getImpresionDeposito() {
        Cursor DT;
        int rslt=0;

        try {

            sql="SELECT val3 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getImpDeposito: " + e.getMessage());
        }
        return rslt;
    }

    public void updateImpDeposito(int valor){
        try{
            sql="UPDATE FinDia SET val3 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateImpDeposito: " + e.getMessage());
        }
    }

    public int getComunicacion() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val2 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getcomunicacion: " + e.getMessage());
        }
        return rslt;
    }

    public void updateComunicacion(int valor){
        try{
            sql="UPDATE FinDia SET val2 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateComunicacion: " + e.getMessage());
        }
    }

    //#CKFK 20190305 Creé esta función para saber si ya se generó el cierre Z
    public int getGeneroCierreZ() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val6 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getGeneroCierreZ: " + e.getMessage());
        }
        return rslt;
    }

    //#CKFK 20190305 Creé esta función para actualizar el val6 en la tabla FinDia
    public void updateGeneroCierreZ(int valor){
        try{
            sql=" UPDATE FinDia SET val6 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateComunicacion: " + e.getMessage());
        }
    }

    //#CKFK 20190305 Creé esta función para saber si ya se imprimió el cierre Z
    public int getImprimioCierreZ() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val7 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getGeneroCierreZ: " + e.getMessage());
        }
        return rslt;
    }

    //#CKFK 20190305 Creé esta función para actualizar el val7 en la tabla FinDia
    public void updateImprimioCierreZ(int valor){
        try{
            sql=" UPDATE FinDia SET val7 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateImprimioCierreZ: " + e.getMessage());
        }
    }

    //#CKFK 20190305 Creé esta función para actualizar el val8 en la tabla FinDia
    public void updateGrandTotalCorelZ(double valor, int corel){
        try{

            //#CKFK_20190328 Moví esto que estaba en ProcessComplete para acá porque de lo contrario no se actualizaban los valores.
            sql = "UPDATE FinDia SET Corel=" + corel + ", val8 = val8 + " + valor;
            db.execSQL(sql);
            sql = "UPDATE P_HANDHELD SET CorelZ=" + corel + ", GrandTotal = GrandTotal + " + valor;
            db.execSQL(sql);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateGrandTotal: " + e.getMessage());
        }
    }

    public void updateCorrelativoZ(int valor){
        try{
            sql="UPDATE FinDia SET Corel = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateCorrelativoZ: " + e.getMessage());
        }
    }

    public void updateFinDia(long valor){
        try{
            sql="UPDATE FinDia SET val1 = " + String.valueOf(valor);
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateFinDia: " + e.getMessage());
        }
    }

    //#HS_20181127_1052 Agregué funcion que elimina los datos de las tablas D.
    public boolean eliminarTablasD(){
        Cursor DT;
        String corel;
        corelz=setCorrelZ();

        try {

            db.beginTransaction();

            sql="SELECT COREL FROM D_FACTURA WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_FACTURA WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAP WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAD_LOTES WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURA_BARRA WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_STOCKB_DEV WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURA_BARRA WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURA_STOCK WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAF WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAD_MODIF WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_CANASTA WHERE CORELTRANS='"+corel+"'";db.execSQL(sql);

                    sql = "DELETE FROM D_STOCKB_DEV WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_LOTES WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_STOCK WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_BARRA WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_REL_PROD_BON WHERE COREL='" + corel + "'";db.execSQL(sql);
                    sql = "DELETE FROM D_BONIFFALT";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql="SELECT COREL FROM D_PEDIDO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_PEDIDO WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_PEDIDOD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    sql="DELETE FROM D_BONIF";db.execSQL(sql);
                    sql="DELETE FROM D_REL_PROD_BON";db.execSQL(sql);
                    sql="DELETE FROM D_BONIFFALT";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql="SELECT COREL FROM D_COBRO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_COBRO WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_COBROD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_COBROP WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_DEPOS WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_DEPOS WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_DEPOSD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_MOV WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_MOV WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDB WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDCAN WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVDPALLET WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }
            //sql="DELETE FROM D_MOVD WHERE CODIGOLIQUIDACION=0";db.execSQL(sql);

            sql="SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_CLINUEVO WHERE CODIGO='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_CLINUEVO_APR WHERE CODIGO='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            //#CKFK 20210928 Agregué el cliente nuevo de Toledano
            sql="SELECT CODIGO FROM D_CLINUEVOT WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_CLINUEVOT WHERE CODIGO='"+corel+"'";db.execSQL(sql);
                    DT.moveToNext();
                }
            }

            //#AT 20220426 Elimina datos de la tabla D_CLINUEVOT_IMAGEN
            sql="SELECT CODIMAGEN FROM D_CLINUEVOT_IMAGEN WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_CLINUEVOT_IMAGEN WHERE CODIMAGEN='"+corel+"'";db.execSQL(sql);
                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_CLIENTE_MODIF WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_CLIENTE_MODIF WHERE COREL='"+corel+"'";db.execSQL(sql);

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

            sql="SELECT COREL FROM D_SOLICINV WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_SOLICINV WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_SOLICINVD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            //#CKFK 20210928 Agregué el delete de los pedidos
            sql="SELECT COREL FROM DS_PEDIDO WHERE BANDERA='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM DS_PEDIDO WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM DS_PEDIDOD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    DT.moveToNext();
                }
            }

            sql="DELETE FROM D_CANASTA WHERE STATCOM = 'S'";db.execSQL(sql);
            sql="DELETE FROM D_ATENCION";db.execSQL(sql);
            sql="DELETE FROM D_CLICOORD WHERE STATCOM='S'";db.execSQL(sql);

            sql="DELETE FROM D_RATING";db.execSQL(sql);

            sql ="DELETE FROM D_DESPACHOD_NO_ENTREGADO WHERE STATCOM= 'S'";db.execSQL(sql);

            //#CKFK_20190325 Se modificó para que solo actualizara el CorelZ y no las demás banderas
            corelz++;
            sql="UPDATE FinDia SET Corel="+corelz+"";
            db.execSQL(sql);

			/*sql="UPDATE P_RUTA SET Email='0'";
			db.execSQL(sql);*/

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

    public int getDocPendientesDeposito(){

        int pend = 0;
        Cursor DT;

        try{

            pend = 0;

            sql = " SELECT E.COREL FROM D_FACTURA E INNER JOIN D_FACTURAP P ON E.COREL = P.COREL " +
                  " WHERE E.ANULADO='N' AND E.DEPOS<>'S' AND P.TIPO <>'K'";
            DT = Con.OpenDT(sql);

            if (DT.getCount()>0){
                pend = pend + DT.getCount();
            }

            sql = "SELECT COREL FROM D_COBRO WHERE ANULADO='N' AND DEPOS<>'S' ";
            DT = Con.OpenDT(sql);
            if (DT.getCount()>0){
                pend = pend + DT.getCount();
            }


        }catch (Exception ex){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
            mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + "\n" + ex.getMessage());
        }

        return pend;

    }

    public int getFactCount(String ss, String pps) {

        Cursor DT;
        int cnt = 0;

        try {

            sql = ss;
            DT = Con.OpenDT(sql);
            sp = "";

            if (DT.getCount()>0){

                cnt = DT.getCount();
                sp += pps + " ";
                sp = sp + cnt + "\n";

            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(sql + "\n" + e.getMessage());
        }

        return cnt;
    }

    public int getDeposCount(String ss, String pps) {

        Cursor DT;
        int cnt = 0;
        String st;

        try {

            sql = ss;
            DT = Con.OpenDT(sql);
            sp = "";

            if (DT.getCount()>0){
                cnt = DT.getCount();
                sp += pps + " ";
                sp = sp + cnt + "\n";
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(sql + "\n" + e.getMessage());
        }

        return cnt;
    }

    public boolean yaHizoFindeDia() {

        Cursor DT;
        boolean vFinDia = false;
        long fechaUltimoCierre = ultimoCierreFecha();

        try{

            sql = "SELECT val1 FROM FinDia";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                if (DT.getInt(0) == 0) {
                    vFinDia = false;
                } else {
                    if (du.getActDate() == fechaUltimoCierre || fechaUltimoCierre != 0) {
                        vFinDia = true;
                    } else {
                        vFinDia = false;
                    }
                }
            }

        }catch (Exception ex){
            vFinDia=false;

        }

        return  vFinDia;
    }

}
