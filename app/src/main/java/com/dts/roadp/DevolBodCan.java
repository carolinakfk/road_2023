package com.dts.roadp;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class DevolBodCan extends PBase {

    private ListView listView;
    private TextView lblReg,lblTot,lblTit;
    private ImageView imgNext;
    private ProgressBar pbar;

    public  String corel,existenciaC,existenciaP;
    private int imprimo=0,close=0;

    private printer prn_can,prn_paseante;
    private clsDocCanastaBod fcanastabod;
    private clsDocCanastaBod fpaseantebod;
    private DevolBod DevBod;
    public Runnable printclose, printcallback,printexit, printclosecan;

    private boolean imprimecopias=false,idle=true;
    private ArrayList<clsClasses.clsDevCan> items= new ArrayList<clsClasses.clsDevCan>();
    private list_view_dev_bod_can adapter;

    private clsRepBuilder rep;

    private AppMethods app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devol_bod_can);

        super.InitBase();
        addlog("DevolBodCan",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        lblReg = (TextView) findViewById(R.id.textView61);lblReg.setText("");
        imgNext = (ImageView) findViewById(R.id.imgTitLogo);
        pbar = (ProgressBar) findViewById(R.id.progressBar4);pbar.setVisibility(View.INVISIBLE);
        lblReg = (TextView) findViewById(R.id.textView61);lblReg.setText("");
        lblTit = (TextView) findViewById(R.id.txtRoadTit);lblTit.setText("Devolución de canastas");
        lblTot = (TextView) findViewById(R.id.textView9);lblTot.setText("0.00");

        rep=new clsRepBuilder(this,gl.prw,false,gl.peMon,gl.peDecImp,"");

        setHandlers();

        gl.devprncierre=false;

        app = new AppMethods(this, gl, Con, db);
        gl.validimp=app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        printclosecan = new Runnable() {
            public void run() {

            }
        };

        printclose = new Runnable() {
            public void run() {
                if (!gl.devfindia) {
                   //#CKFK 20211204 Agregué esta validació
                    if (!prn_can.isEnabled()) {
                        gl.closeDevBod=true;
                        DevolBodCan.super.finish();
                    }else{
                        pbar.setVisibility(View.INVISIBLE);
                        gl.closeDevBod=true;
                        DevolBodCan.super.finish();
                    }

                }
            }
        };

        printexit = new Runnable() {
            public void run() {
                if (!gl.devfindia) {

                }

                //#CKFK 20211204 Agregué esta validació
                if (!prn_can.isEnabled()) {
                    gl.closeDevBod=true;
                    DevolBodCan.super.finish();
                }else{
                    pbar.setVisibility(View.INVISIBLE);
                    gl.closeDevBod=true;
                    DevolBodCan.super.finish();
                }

            }
        };

        printcallback= new Runnable() {
            public void run() {

                if (!imprimecopias){

                    if (imprimo==0) {
                        prn_can.printnoask(printclosecan, "printdevcan.txt");
                    }

                    imprimecopias = true;

                }else{

                    if (imprimo==0) {
                        prn_can.printnoask(printclosecan, "printdevcan.txt");
                    }
                    imprimecopias = false;

                }

                askPrint();

            }
        };

        prn_can=new printer(this,printclose,gl.validimp);
        prn_paseante=new printer(this,printclose,gl.validimp);

        fcanastabod=new clsDocCanastaBod(this,prn_can.prw,gl.peMon,gl.peDecImp, "printdevcan.txt");
        fcanastabod.deviceid =gl.deviceId;
        fcanastabod.vTipo="CANASTA";

        fpaseantebod=new clsDocCanastaBod(this,prn_paseante.prw,gl.peMon,gl.peDecImp, "printpaseante.txt");
        fpaseantebod.deviceid =gl.deviceId;
        fpaseantebod.vTipo="PASEANTE";

        app = new AppMethods(this, gl, Con, db);
        gl.validimp=app.validaImpresora();
        if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

        listItems();

        habilitaEnvio();
    }

    //region Events

    public void doSave(View view) {
        try{
            if (!validaDevolucion()) {
                msgbox("La devolución está vacia, no se puede aplicar");
            } else {
                msgAskSave("Aplicar la devolución");
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void setHandlers() {

        try{
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsDevCan item = (clsClasses.clsDevCan) lvObj;

                        adapter.setSelectedIndex(position);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }
                }
            });

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Main

    private void listItems() {
        Cursor dt, dp;
        clsClasses.clsDevCan item,itemm,itemt;
        String vF,pcod, cod, name, um, ump, sc, scm, sct="", sp, spm, spt="";
        double val, valm, valt, peso, pesot;
        int icnt;

        items.clear(); valt=0;pesot=0;

        try {

            sql =  " SELECT D_CXCD.CODIGO AS CODIGO, P_PRODUCTO.DESCLARGA AS DESCLARGA " +
                    " FROM D_CXCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_CXCD.CODIGO " +
                    " INNER JOIN D_CXC ON D_CXC.COREL = D_CXCD.COREL WHERE D_CxC.STATCOM='N' AND D_CXC.ANULADO = 'N' " +
                    " GROUP BY D_CXCD.CODIGO,P_PRODUCTO.DESCLARGA ";
            sql += "UNION SELECT DISTINCT(PRODUCTO) AS CODIGO, '' as DESCLARGA FROM D_CANASTA WHERE CANTREC > 0 AND ANULADO=0";

            dp = Con.OpenDT(sql);

            if (dp == null || dp.getCount() == 0) {
                adapter = new list_view_dev_bod_can(this, items);
                listView.setAdapter(adapter);
                return;
            }

            lblReg.setText("Productos : " + dp.getCount());
            dp.moveToFirst();

            while (!dp.isAfterLast()) {

                pcod=dp.getString(0);

                sql =  "SELECT D_CXCD.CODIGO AS CODIGO, P_PRODUCTO.DESCLARGA AS DESCLARGA, SUM(D_CXCD.CANT) AS TOTAL,D_CXCD.UMVENTA AS UMVENTA, " +
                        "D_CXCD.LOTE AS LOTE, D_CXCD.COREL AS COREL, D_CXCD.ESTADO AS ESTADO ,SUM(D_CXCD.PESO) AS PESO " +
                        "FROM D_CXCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_CXCD.CODIGO INNER JOIN " +
                        "D_CxC ON D_CxC.COREL = D_CxCD.COREL  " +
                        "WHERE (P_PRODUCTO.CODIGO='"+pcod+"') AND D_CxC.STATCOM='N' AND D_CxC.ANULADO='N' " +
                        "GROUP BY D_CXCD.CODIGO,P_PRODUCTO.DESCLARGA,D_CXCD.UMVENTA,D_CXCD.LOTE,D_CXCD.COREL,D_CXCD.ESTADO ";

                sql += "UNION SELECT b.CODIGO AS CODIGO, b.DESCLARGA AS DESCLARGA, SUM(a.CANTREC) AS TOTAL, " +
                        "a.UNIDBAS AS UMVENTA, NULL AS LOTE, NULL as COREL, NULL AS ESTADO, 0 AS PESO " +
                        "FROM D_CANASTA a " +
                        "INNER JOIN P_PRODUCTO b ON b.CODIGO = a.PRODUCTO " +
                        "WHERE a.PRODUCTO = '" + pcod + "' " +
                        "AND a.CANTREC > 0 " +
                        "AND a.ANULADO=0 " +
                        "GROUP BY b.CODIGO, b.DESCLARGA, b.UM_SALIDA;";

                dt = Con.OpenDT(sql);
                icnt=dt.getCount();
                /*if (icnt==1) {
                    dt.moveToFirst();
                    if (dt.getDouble(2)>0 && dt.getDouble(3)>0) icnt=2;
                }*/

                item = clsCls.new clsDevCan();
                item.Cod = pcod;
                item.Desc = dt.getString(1);
                item.flag = 0;
                item.items=icnt;
                items.add(item);

                if (dt.getCount() > 0) dt.moveToFirst();
                else continue;

                while (!dt.isAfterLast()) {

                    cod = dt.getString(0);
                    name = dt.getString(1);
                    val = dt.getDouble(2);
                    valm = 0.0;
                    um = dt.getString(3);
                    peso =  dt.getDouble(7);

                    valt += val + valm;
                    pesot += peso ;

                    ump = gl.umpeso;
                    sp = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) sp = "";
                    spm = mu.frmdecimal(peso, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) spm = "";
                    spt = mu.frmdecimal(pesot, gl.peDecImp) + " " + rep.ltrim(ump, 3);
                    if (!gl.usarpeso) spt = "";

                    sc = mu.frmdecimal(val, gl.peDecImp) + " " + rep.ltrim(um, 2);
                    scm = mu.frmdecimal(valm, gl.peDecImp) + " " + rep.ltrim(um, 2);
                    sct = mu.frmdecimal(valt, gl.peDecImp) + " " + rep.ltrim(um, 2);

                    item = clsCls.new clsDevCan();
                    itemm = clsCls.new clsDevCan();

                    item.Cod = cod;itemm.Cod = cod;
                    item.Fecha = cod;itemm.Fecha = cod;
                    item.Desc = name;itemm.Desc = name;
                    item.cant = val;itemm.cant = val;
                    item.cantm = valm;itemm.cantm = valm;

                    item.Valor = sc;itemm.Valor = sc;
                    item.ValorM = scm;itemm.ValorM = scm;
                    item.ValorT = sct;itemm.ValorT = sct;

                    item.Peso = sp;itemm.Peso = sp;
                    item.PesoM = spm;itemm.PesoM = spm;
                    item.PesoT = spt;item.PesoT = spt;

                    item.Lote = dt.getString(5);
                    itemm.Lote = item.Lote;

                    if (val>0) {
                        item.flag = 1;
                        items.add(item);

                        if (valm > 0) {
                            icnt++;
                            itemm.flag = 2;
                            items.add(itemm);
                        }
                    } else {
                        item.flag = 2;
                        items.add(item);
                    }
                    dt.moveToNext();

                }

                if (icnt>=1) {
                    itemt = clsCls.new clsDevCan();
                    itemt.ValorT = sct;
                    itemt.PesoT = spt;
                    itemt.flag = 3;
                    items.add(itemt);
                }

                dp.moveToNext();

                if(dt!=null) dt.close();

            }

            lblTot.setText("Cant : " + mu.frmdecno(valt)+" Peso : "+ mu.round(pesot,gl.peDec)+" "+gl.umpeso);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +e.getMessage());
        }

        adapter = new list_view_dev_bod_can(this, items);
        listView.setAdapter(adapter);

    }

    private void save() {
        Cursor DT;
        String pcod,plote,um,barra,barrapallet;
        Double pcant,pcantm,ppeso;

        try {

            ocultaEnvio();

            corel=gl.ruta+"_"+mu.getCorelBase();

            gl.devfindia=app.getDevolBod();

            fecha=du.getActDateTime();
            if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());

            db.beginTransaction();

            ins.init("D_MOV");
            ins.add("COREL",corel);
            ins.add("RUTA",gl.ruta);
            ins.add("ANULADO","N");
            ins.add("FECHA",fecha);
            ins.add("TIPO","D");
            ins.add("USUARIO",gl.vend);
            ins.add("REFERENCIA","Devolucion");
            ins.add("STATCOM","N");
            ins.add("IMPRES",0);
            ins.add("CODIGOLIQUIDACION",0);

            db.execSQL(ins.sql());

            sql="SELECT CODIGO,LOTE,SUM(CANT),SUM(CANTM),SUM(PESO),UNIDADMEDIDA FROM P_STOCK GROUP BY CODIGO,LOTE,UNIDADMEDIDA " +
                    "HAVING SUM(CANT)>0 OR SUM(CANTM) >0";

            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod=DT.getString(0);
                    plote=DT.getString(1);
                    pcant=DT.getDouble(2);
                    pcantm=DT.getDouble(3);
                    ppeso=DT.getDouble(4);
                    um=DT.getString(5);

                    ins.init("D_MOVD");

                    ins.add("COREL",corel);
                    ins.add("PRODUCTO",pcod);
                    ins.add("CANT",pcant);
                    ins.add("CANTM",pcantm);
                    ins.add("PESO",ppeso);
                    ins.add("PESOM",ppeso);
                    ins.add("LOTE",plote);
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("UNIDADMEDIDA",um);

                    db.execSQL(ins.sql());

                    DT.moveToNext();
                }
            }

            sql="SELECT CODIGO,BARRA,SUM(CANT),SUM(PESO),CODIGOLIQUIDACION,UNIDADMEDIDA FROM P_STOCKB GROUP BY CODIGO,UNIDADMEDIDA,CODIGOLIQUIDACION,BARRA " +
                    "HAVING SUM(PESO)>0 ";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod=DT.getString(0);
                    barra=DT.getString(1);
                    ppeso=DT.getDouble(3);
                    um=DT.getString(5);

                    ins.init("D_MOVDB");

                    ins.add("COREL",corel);
                    ins.add("PRODUCTO",pcod);
                    ins.add("BARRA",barra);
                    ins.add("PESO",ppeso);
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("UNIDADMEDIDA",um);

                    db.execSQL(ins.sql());

                    DT.moveToNext();
                }

            }

            sql="SELECT CODIGO,BARRAPALLET,BARRAPRODUCTO,LOTEPRODUCTO,SUM(CANT),SUM(PESO),UNIDADMEDIDA,CODIGOLIQUIDACION FROM P_STOCK_PALLET GROUP BY CODIGO,UNIDADMEDIDA,CODIGOLIQUIDACION,BARRAPALLET,BARRAPRODUCTO,LOTEPRODUCTO";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod=DT.getString(0);
                    barrapallet = DT.getString(1);
                    barra = DT.getString(2);
                    plote=DT.getString(3);
                    pcant=DT.getDouble(4);
                    ppeso=DT.getDouble(5);
                    um=DT.getString(6);

                    ins.init("D_MOVDPALLET");

                    ins.add("COREL",corel);
                    ins.add("PRODUCTO",pcod);
                    ins.add("BARRAPALLET",barrapallet);
                    ins.add("BARRAPRODUCTO",barra);
                    ins.add("LOTEPRODUCTO",plote);
                    ins.add("CANT",pcant);
                    ins.add("PESO",ppeso);
                    ins.add("UNIDADMEDIDA",um);
                    ins.add("CODIGOLIQUIDACION",0);

                    db.execSQL(ins.sql());

                    DT.moveToNext();
                }

            }

            sql="SELECT CODIGO,SUM(CANT),SUM(PESO),LOTE,UMVENTA " +
                    "FROM D_CXCD INNER JOIN D_CxC ON D_CxC.COREL = D_CxCD.COREL " +
                    "WHERE D_CxC.STATCOM='N' GROUP BY CODIGO,UMVENTA,LOTE " +
                    "UNION " +
                    "SELECT PRODUCTO, SUM(CANTREC), SUM(PESOREC), '',UNIDBAS " +
                    "FROM D_CANASTA WHERE STATCOM = 'N' " +
                    "GROUP BY PRODUCTO, UNIDBAS ";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod=DT.getString(0);
                    pcant=DT.getDouble(1);
                    ppeso=DT.getDouble(2);
                    plote=DT.getString(3);
                    um=DT.getString(4);

                    ins.init("D_MOVDCAN");

                    ins.add("COREL",corel);
                    ins.add("PRODUCTO",pcod);
                    ins.add("CANT",pcant);
                    ins.add("CANTM",0);
                    ins.add("PESO",ppeso);
                    ins.add("PESOM",0);
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("BARRA","");
                    ins.add("LOTE",plote);
                    ins.add("PASEANTE",0);
                    ins.add("UNIDADMEDIDA",um);

                    db.execSQL(ins.sql());

                    DT.moveToNext();

                }

            }

            sql="DELETE FROM P_STOCK";
            db.execSQL(sql);

            sql="DELETE FROM P_STOCKB";
            db.execSQL(sql);

            sql="DELETE FROM P_STOCK_PALLET";
            db.execSQL(sql);

            sql="UPDATE FinDia SET val5 = 5";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

            if(DT!=null) DT.close();

            EnviaDev();

            createDoc();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            habilitaEnvio();
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    private void singlePrint() {
        try{
            prn_can.printask(printclose, "printdevcan.txt");
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private boolean EnviaDev(){

        boolean vEnvia=false;

        try{

            gl.enviaMov =true;
            vEnvia=true;
            startActivity(new Intent(this, ComWS.class));

        }catch (Exception e){
            mu.toast("Ocurrió un error enviando los datos " + e.getMessage() );
            vEnvia=false;
        }

        return vEnvia;
    }

    //endregion

    //region createdoc

    private void createDoc(){

        try{

            imprimo=0;

            existenciaC=tieneCanasta(corel);
            existenciaP=tienePaseante(corel);

            if(existenciaC.isEmpty() && !existenciaP.isEmpty()) imprimo=1;
            if(!existenciaC.isEmpty() && existenciaP.isEmpty()) imprimo=2;
            if(existenciaC.isEmpty() && existenciaP.isEmpty()) imprimo=3;

            if (prn_can.isEnabled()) {

                String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
                if(imprimo==0 || imprimo==1){
                    fpaseantebod.buildPrint(corel,0,vModo);
                }

                if(imprimo==0 || imprimo==2){
                    fcanastabod.buildPrint(corel,0, vModo);
                }

                if(imprimo==0) {
                    prn_paseante.printask(printcallback, "printpaseante.txt");
                }else if(imprimo==1) {
                    prn_paseante.printask(printcallback, "printpaseante.txt");
                }else if(imprimo==2) {
                    prn_can.printask(printcallback, "printdevcan.txt");
                }

            }else if(!prn_can.isEnabled()){

                String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");

                if(imprimo==0 || imprimo==1){
                    fpaseantebod.buildPrint(corel,0,vModo);
                }

                if(imprimo==0 || imprimo==2){
                    fcanastabod.buildPrint(corel,0, vModo);
                }
            }

            if (!prn_can.isEnabled()) {
                Toast.makeText(this, "Devolucion completada y guardada.",Toast.LENGTH_SHORT);
                gl.closeDevBod=true;
                super.finish();
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            mu.msgbox("createDoc: " + e.getMessage());
        }

    }

    private String tieneCanasta(String vCorel){

        Cursor DT;
        String vtieneCanasta= "";

        try{

            sql = "SELECT COREL FROM D_MOVDCAN WHERE COREL = '" + vCorel + "' AND COREL IN (SELECT COREL FROM D_MOV)";
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
                DT.moveToFirst();
                vtieneCanasta = DT.getString(0);
            }

            if(DT!=null) DT.close();

        }catch (Exception ex){
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Ocurrió un error "+ex.getMessage());
        }

        return vtieneCanasta;
    }

    private String tienePaseante(String vCorel){

        Cursor DT;
        String vtienePaseante= "";

        try{

            sql = "SELECT COREL FROM D_MOVD WHERE COREL = '" + vCorel + "' " +
                  "AND COREL IN (SELECT COREL FROM D_MOV) " +
                  "UNION " +
                  "SELECT COREL FROM D_MOVDB WHERE COREL = '" + vCorel  + "' " +
                  "AND COREL IN (SELECT COREL FROM D_MOV) ";
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
                DT.moveToFirst();
                vtienePaseante = DT.getString(0);
            }

            if(DT!=null) DT.close();

        }catch (Exception ex){
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " +"Ocurrió un error "+ex.getMessage());
        }

        return vtienePaseante;
    }

    //endregion

    //region Aux

    private boolean validaDevolucion() {
        Cursor dt;
        long cantcan=0,cantstock=0,cantbolsa=0;

        try {
            sql = "SELECT IFNULL(SUM(CANT),0) FROM D_CXC E INNER JOIN D_CXCD D ON  " +
                    " E.COREL = D.COREL WHERE E.ANULADO = 'N'";
            dt = Con.OpenDT(sql);
            if (dt!=null){
                cantstock += dt.getLong(0);
            }

            sql = "SELECT IFNULL(SUM(CANTREC),0) FROM D_CANASTA WHERE ANULADO = 0";
            dt = Con.OpenDT(sql);
            if (dt!=null){
                cantstock += dt.getLong(0);
            }

            sql = "SELECT IFNULL(SUM(CANT),0) FROM P_STOCK ";
            dt = Con.OpenDT(sql);
            if (dt!=null){
                cantstock += dt.getLong(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }

        try {
            sql="SELECT IFNULL(COUNT(BARRA),0) FROM P_STOCKB";
            dt=Con.OpenDT(sql);

            cantbolsa=dt.getLong(0);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }

        try {
            sql="SELECT IFNULL(SUM(CANT),0) FROM D_CXC E INNER JOIN D_CXCD D " +
                " ON  E.COREL = D.COREL WHERE E.ANULADO = 'N'";
            dt=Con.OpenDT(sql);

            cantcan=dt.getLong(0);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }

        if(dt!=null) dt.close();

        return (cantstock+cantbolsa+cantcan>0);
    }

    private void msgAskSave(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Devolución a bodega");
            dialog.setMessage("¿" + msg + "?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    save();
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

    private void askPrint() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("¿Impresión correcta?");
            dialog.setCancelable(false);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    //Actualiza el número de impresiones para poder definir cuando es una re impresión o primera impresión.
                    try {
                        sql = "UPDATE D_MOV SET IMPRES=IMPRES+1 WHERE COREL='" + corel + "' AND TIPO='D' ";
                        db.execSQL(sql);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }

                    if (imprimecopias){

                        if (prn_can.isEnabled()) {

                            String vModo = (gl.peModal.equalsIgnoreCase("TOL") ? "TOL" : "*");
                            if (imprimo == 0 || imprimo == 1) {
                                fpaseantebod.buildPrint(corel, 1, vModo);
                            }

                            if (imprimo == 0 || imprimo == 2) {
                                fcanastabod.buildPrint(corel, 1, vModo);
                            }

                            if (imprimo == 0) {
                                prn_paseante.printask(printcallback, "printpaseante.txt");
                            } else if (imprimo == 1) {
                                prn_paseante.printask(printcallback, "printpaseante.txt");
                            } else if (imprimo == 2) {
                                prn_can.printask(printcallback, "printdevcan.txt");
                            }
                        }

                    }

                    if (!imprimecopias){
                        if (!gl.devfindia) {
                        }

                        //#CKFK 20211204 Agregué esta validació
                        if (!prn_can.isEnabled()) {
                            gl.closeDevBod=true;
                            DevolBodCan.super.finish();
                        }else{
                            pbar.setVisibility(View.INVISIBLE);
                            gl.closeDevBod=true;
                            DevolBodCan.super.finish();
                        }

                    }
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    prn_can.printask(printclose, "printdevcan.txt");

                    if (!gl.devfindia) {
                    }

                    //#CKFK 20211204 Agregué esta validació
                    if (!prn_can.isEnabled()) {
                        gl.closeDevBod=true;
                        DevolBodCan.super.finish();
                    }else{
                        pbar.setVisibility(View.INVISIBLE);
                        gl.closeDevBod=true;
                        DevolBodCan.super.finish();
                    }

                }
            });

            dialog.show();
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void habilitaEnvio() {
        imgNext.setVisibility(View.VISIBLE);
        pbar.setVisibility(View.INVISIBLE);
        lblTit.setText("Devolución de canastas");
    }

    private void ocultaEnvio() {
        imgNext.setVisibility(View.INVISIBLE);
        pbar.setVisibility(View.VISIBLE);
        lblTit.setText("Espere, por favor . . .");
    }


    //endregion

    //region Activity Events

    @Override
    protected void onResume() {
        try{
            super.onResume();
            if (gl.closeDevBod) super.finish();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //endregion

}
