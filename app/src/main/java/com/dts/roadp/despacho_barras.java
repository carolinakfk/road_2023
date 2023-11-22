package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

public class despacho_barras extends PBase {

    private ListView listView;
    private EditText txtFilter;
    private Spinner spinFam;

    private final ArrayList<String> spincode= new ArrayList<String>();
    private final ArrayList<String> spinlist = new ArrayList<String>();

    private ArrayList<clsClasses.clsCD> items;
    private ListAdaptProd adapter;
    private AppMethods app;

    private String famid,itemid,prname,um,ubas,tiposcan,barcode, prodid, rutatipo, emp, cliid;
    private int act,prodtipo, nivel;
    private double disp_und;
    private double disp_peso;
    private boolean ordPorNombre,modotol,softscanexist,usarscan,porpeso, contrans;
    private boolean isDialogBarraShowed = false;
    private AlertDialog.Builder dialogBarra;
    private double prec, cant, prodtot, percep;
    private Precio prc, prcEsp;
    private PrecioTran prctr;

    private EditText txtBarra;
    private TextView txtRoadTit;

    private Runnable scanCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despacho_barras);

        super.InitBase();
        addlog("Producto",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        txtFilter = (EditText) findViewById(R.id.editText1);
        spinFam = (Spinner) findViewById(R.id.spinner1);

        prodtipo=gl.prodtipo;
        gl.prodtipo=0;
        this.setTitle("Barras a escanear");

        modotol=gl.peModal.equalsIgnoreCase("TOL");

        app = new AppMethods(this, gl, Con, db);

        items = new ArrayList<clsClasses.clsCD>();

        act=0;famid="";
        ordPorNombre=gl.peOrdPorNombre;
        emp=gl.emp;
        nivel=gl.nivel;
        cliid=gl.cliente;
        rutatipo=gl.rutatipo;

        txtBarra=(EditText) findViewById(R.id.txtBarra);
        txtRoadTit=(TextView) findViewById(R.id.txtRoadTit);

        scanCallBack= new Runnable() {
            public void run() {

                try {
                    if (contrans) {
                        addBarcodeTrans();
                    } else {
                        addBarcode();
                    }
                } catch (Exception e) {
                }

                Handler handlerTimer = new Handler();
                handlerTimer.postDelayed(new Runnable() {
                    public void run() {
                        txtBarra.setText("");
                    }
                }, 1000);
            }
        };

        prc=new Precio(this,mu,2);

        fillSpinner();

        setHandlers();

        listItems();

        initValues();

        txtBarra.requestFocus();txtBarra.setText("");
        dialogBarra= new AlertDialog.Builder(this);

        //spinFam.requestFocus();

    }

    private void initValues() {
        Cursor DT;
        String contrib;

        tiposcan = "*";

        try {
            sql = "SELECT TIPO_HH FROM P_ARCHIVOCONF WHERE RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                tiposcan = DT.getString(0);
            }

            if (DT != null) DT.close();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            tiposcan = "*";
            msgbox(e.getMessage());
        }

        usarscan = false;
        softscanexist = false;
        if (!mu.emptystr(tiposcan)) {
            if (tiposcan.equalsIgnoreCase("SOFTWARE")) {
                softscanexist = detectBarcodeScanner();
                usarscan = true;
            }
            if (!tiposcan.equalsIgnoreCase("SIN ESCANER")) usarscan = true;
        }

    }

    private boolean detectBarcodeScanner() {

        String packagename="com.google.zxing.client.android";
        PackageManager pm = this.getPackageManager();

        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            toast("Aplicacion ZXing Barcode Scanner no esta instalada");return false;
        }

    }

    // Events

        public void porCodigo(View view) {
            try{
                ordPorNombre=false;
                listItems();
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

        public void porNombre(View view) {
            try{
                ordPorNombre=true;
                listItems();
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

        private void setHandlers() {

            try {

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

                spinFam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                            famid = scod;

                            listItems();

                            spinFam.requestFocus();
                            //if (act>0) {hidekeyb();}
                            hidekeyb();

                            act += 1;
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

            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            barcode = txtBarra.getText().toString().trim();

            Handler handlerTimer = new Handler();
            handlerTimer.postDelayed(new Runnable() {
                public void run() {
                    txtBarra.setText("");
                }
            }, 20);

            txtRoadTit.setText(barcode);

            if (!isDialogBarraShowed)	{
                if (!mu.emptystr(barcode)) 	scanCallBack.run();
            }

            Handler handlerTimer2 = new Handler();
            handlerTimer2.postDelayed(new Runnable() {
                public void run() {
                    txtBarra.requestFocus();
                }
            }, 100);
        }
        return super.dispatchKeyEvent(e);
    }

        // Main

        private void listItems() {
            Cursor DT;
            clsClasses.clsCD vItem = null;
            int cantidad = 0;
            String vF,cod,name,um;
            double vFaltante = 0, vCantOriginal = 0, vPesoOriginal = 0;

            ArrayList<clsClasses.clsCD> vitems = new ArrayList<clsClasses.clsCD>();;

            items.clear();itemid="*";//famid="0";
            vitems.clear();

            adapter=new ListAdaptProd(this,vitems);
            listView.setAdapter(adapter);

            vF=txtFilter.getText().toString().replace("'","");
            vF=vF.replace("\r","");

            String sql = "";

            try {

               sql="SELECT DISTINCT P.CODIGO, P.DESCCORTA, R.UNIDADMEDIDA, D.CANTDIF, D.CANTORIGINAL, D.PESOORIGINAL " +
                    "FROM T_VENTA_DESPACHO D INNER JOIN P_PRODUCTO P ON D.PRODUCTO=P.CODIGO INNER JOIN " +
                    "P_PRODPRECIO R ON (D.PRODUCTO=R.CODIGO) " +
                    "WHERE (D.CANTDIF > 0)  AND (R.NIVEL = " + gl.nivel + ") AND (P.ES_VENDIBLE=1)";
                if (!mu.emptystr(famid)){
                    if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P.LINEA='"+famid+"') ";
                }

                if (ordPorNombre) {
                    sql += "ORDER BY P.DESCCORTA";
                } else {
                    sql += "ORDER BY P.CODIGO";
                }

                DT=Con.OpenDT(sql);

                cantidad = DT.getCount();
                if (cantidad==0) {
                    toast("Ya no hay barras pendientes de despachar");
                    finish();
                    return;
                }

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    cod=DT.getString(0);
                    name=DT.getString(1);
                    um=DT.getString(2);
                    vFaltante = DT.getDouble(3);
                    vCantOriginal = DT.getDouble(4);
                    vPesoOriginal = DT.getDouble(5);

                    vItem = clsCls.new clsCD();

                    vItem.Cod=cod;
                    vItem.Desc=name;

                    //#EJC20181127: En aprof. no tienen un viene vacío, colocar por defecto un.
                    if (um.equalsIgnoreCase(""))  um="UN";

                    vItem.um=um;
                    vItem.Text="";
                    vItem.bandera=false;
                    vItem.faltante=vFaltante;
                    vItem.cantOriginal =vCantOriginal;
                    vItem.pesoOriginal = vPesoOriginal;
                    vItem.es_despacho = true;

                    if (prodtipo==0 && modotol) {
                        if (DT.getString(3).equalsIgnoreCase("C")) vItem.bandera=true;
                    }

                    items.add(vItem);
                    vitems.add(vItem);

                    DT.moveToNext();
                }

                if(DT!=null) DT.close();
            } catch (Exception e) {
                //	mu.msgbox( e.getMessage());
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                Log.d("prods",e.getMessage());
            }

            items = (ArrayList<clsClasses.clsCD>) vitems.clone();

            adapter=new ListAdaptProd(this,vitems);
            listView.setAdapter(adapter);

            if (prodtipo==1) dispUmCliente();

            if (cantidad==1) {

                itemid = vItem.Cod;
                prname = vItem.Desc;
                gl.um = vItem.um;
                gl.pprodname = prname;

                //if (!app.prodBarra(itemid)) appProd();
            }
        }

        private void appProd(){
            try{
                gl.gstr=itemid;
                System.gc();
                super.finish();
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

        private void dispUmCliente() {
            String sdisp;
            try{
                for (int i = items.size()-1; i >=0; i--) {
                    if (getDisp(items.get(i).Cod)) {
                        sdisp=mu.frmdecimal(disp_und,gl.peDecImp)+" "+ltrim(um,6)+"  "+mu.frmdecimal(disp_peso,gl.peDecImp)+" "+ltrim(gl.umpeso,6);
                        items.get(i).Text=sdisp;
                    } else {
                        items.remove(i);
                    }
                }

                adapter.notifyDataSetChanged();
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }
        }

        private boolean getDisp(String prodid) {
            Cursor dt;
            String umstock = "";
            double umf1,umf2,umfactor;
            boolean porpeso=prodPorPeso(prodid);

            disp_und =0;

            try {
                //sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
                sql=" SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"') " +
                        " UNION " +
                        " SELECT UNIDADMEDIDA FROM P_STOCKB WHERE (CODIGO='" + prodid + "') ";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0){
                    dt.moveToFirst();
                    um=dt.getString(0);
                }

                dt.close();

                sql="SELECT UNIDBAS	FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0){
                    dt.moveToFirst();
                    ubas=dt.getString(0);
                }

                dt.close();

            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                return false;
            }

            try {
                sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
                        " FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                        " FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')" +
                        " UNION \n" +
                        " SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                        " FROM P_STOCKB WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')) AS A";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0) {

                    dt.moveToFirst();
                    disp_und =dt.getDouble(0);
                    disp_peso =dt.getDouble(1);
                }

                dt.close();

                if (disp_und ==0) {

                    sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
                            " FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                            " FROM P_STOCK WHERE (CODIGO='"+prodid+"')" +
                            " UNION \n" +
                            " SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                            " FROM P_STOCKB WHERE (CODIGO='"+prodid+"')) AS A";
                    dt=Con.OpenDT(sql);

                    if (dt.getCount()>0) {
                        dt.moveToFirst();
                        disp_und =dt.getDouble(0);
                        disp_peso =dt.getDouble(1);
                    }
                }

                dt.close();

                if (disp_und >0)	return true;

            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            }

            try {
                sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"')" +
                        " UNION \n" +
                        " SELECT UNIDADMEDIDA FROM P_STOCKB WHERE (CODIGO='"+prodid+"')";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0){
                    dt.moveToFirst();

                    umstock=dt.getString(0);
                }

                dt.close();

                sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
                        "WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0) {
                    dt.moveToFirst();
                    umf1=dt.getDouble(0);
                } else {
                    return false;
                }

                dt.close();

                sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
                        "WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";
                dt=Con.OpenDT(sql);

                if (dt.getCount()>0) {
                    dt.moveToFirst();
                    umf2=dt.getDouble(0);
                } else {
                    return false;
                }

                dt.close();

                umfactor=umf1/umf2;

                sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
                        " FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                        " FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')" +
                        " UNION \n" +
                        " SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
                        " FROM P_STOCKB WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')) AS A";
                dt=Con.OpenDT(sql);
                if(dt.getCount()>0) {
                    dt.moveToFirst();
                    disp_und =dt.getDouble(0);
                    disp_peso =dt.getDouble(1);
                }

                dt.close();

			/*if (disp_und ==0) {
				sql="SELECT SUM(CANT),SUM(PESO) FROM P_STOCKB WHERE (CODIGO='"+prodid+"')";
				dt=Con.OpenDT(sql);
				if(dt.getCount()>0) {
					dt.moveToFirst();
					disp_und =dt.getDouble(0);
				}
			}

			if (!porpeso) disp_und = disp_und /umfactor; else disp_und =dt.getDouble(1);*/

                return true;
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                return false;
            }

        }

        // Aux

    //region Barras

    private void addBarcode() 	{
        int bbolsa;

        if (!isDialogBarraShowed) 	{

            if (barcode.length()>gl.pLongitudBarra){

                if (gl.pPrefijoBarra.length()>0)
                    barcode=gl.pPrefijoBarra+barcode;

                gl.barra=barcode.substring(0,gl.pLongitudBarra);
                barcode=gl.barra;

            } else {
                gl.barra=barcode;
            }

            try {

                opendb();

                if (barraBonif()) {
                    toastlong("¡La barra es parte de bonificacion!");
                    txtBarra.setText("");return;
                }

                if (rutatipo.equalsIgnoreCase("V") ||
                        rutatipo.equalsIgnoreCase("D") ) {
                    bbolsa=barraBolsa();
                    if (bbolsa==1) {
                        txtBarra.setText("");
                        txtBarra.requestFocus();
                        listItems();
                        return;
                    } else if (bbolsa==-1) {
                        toast("Barra vendida");
                        txtBarra.setText("");
                        txtBarra.requestFocus();
                        return;
                    }else if (bbolsa==-2) {
                        msgbox("Esa barra está reservada para otros despachos");
                        txtBarra.setText("");
                        txtBarra.requestFocus();
                        return;
                    }else if (bbolsa==-3) {
                        msgbox("Al cliente no se le pueden vender productos nuevos");
                        return;
                    }else if (bbolsa==-4) {
                        msgbox("Al cliente no se le pueden vender mas cantidad de la solicitada");
                        return;
                    }
                }

                msgbox("¡La barra "+barcode+" no existe!");
                txtBarra.setText("");
                txtBarra.requestFocus();

            } catch (Exception e) {
                msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
                Log.d("VENTA","trans fail "+e.getMessage());
            }


        } else {
            toastlong("¡Conteste la pregunta por favor!");
            txtBarra.setText("");
            txtBarra.requestFocus();
        }

    }

    private void addBarcodeTrans() 	{
        int bbolsa;

        if (!isDialogBarraShowed) 	{

            if (barcode.length()>18){
                if (Build.VERSION.SDK_INT >= 28){
                    gl.barra=barcode.substring(0,18);
                }else{
                    gl.barra=barcode.substring(1,18);
                }
                barcode=gl.barra;
            }else{
                gl.barra=barcode;
            }

            try {

                opendb();

                db.beginTransaction();

                if (barraBonif()) {
                    toastlong("¡La barra es parte de una bonificacion!");
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    txtBarra.requestFocus();
                    txtBarra.setText("");
                    return;
                }else{
                    db.endTransaction();
                }

                bbolsa=barraBolsaTrans();
                if (bbolsa==1) {

                    txtBarra.setText("");
                    listItems();

                    return;
                } else if (bbolsa==-1) {
                    toast("Barra vendida");
                    return;
                }else if (bbolsa==-2) {
                    msgbox("Esa barra está reservada para otros despachos");
                    return;
                }else if (bbolsa==-3) {
                    msgbox("Al cliente no se le pueden vender productos nuevos");
                    return;
                }else if (bbolsa==-4) {
                    msgbox("Al cliente no se le pueden vender mas cantidad de la solicitada");
                    return;
                }

                db.beginTransaction();

                toast("¡La barra "+barcode+" no existe!");

            } catch (Exception e) {
                msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
                Log.d("VENTA","trans fail "+e.getMessage());
            }

            txtBarra.setText("");
            txtBarra.requestFocus();
        } else {
            toastlong("¡Conteste la pregunta por favor!");
            txtBarra.setText("");
            txtBarra.requestFocus();
        }

    }

    private double DameProporcionVenta(String vProd , String vCliente , int vNivelPrec) {
        String UnidadInventario="",UnidadVentaCliente="";
        double varZ=0,varP=0,proporcion=0;

        try {
            UnidadInventario = DameUnidadMinimaVenta(vProd);//Depende de la unidad mínima de venta del producto
            UnidadVentaCliente = app.umVenta(vProd);//'Depende de la lista de precio del cliente

            if ((!UnidadInventario.equalsIgnoreCase(UnidadVentaCliente)) && (EsUnidadSuperior(UnidadInventario, vProd))
                    && (EsUnidadSuperior(UnidadVentaCliente, vProd)) && (!gl.umpeso.equalsIgnoreCase(UnidadVentaCliente))) {
                varZ = DameFactor(UnidadInventario, vProd);
                varP = DameFactor(UnidadVentaCliente, vProd);
                if (varP>0) proporcion = varZ / varP;
            } else if ((UnidadInventario.equalsIgnoreCase(UnidadVentaCliente)) | (UnidadVentaCliente.equalsIgnoreCase(gl.umpeso))) {
                proporcion = 1;
            } else{
                proporcion = DameFactor(UnidadInventario, vProd);
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

        return proporcion;
    }

    public String DameUnidadMinimaVenta(String vProd )  {
        Cursor dt;
        String ss;

        try {
            sql = "SELECT UNIDBAS FROM P_PRODUCTO WHERE (CODIGO='"+vProd+"') AND (ES_PROD_BARRA=0)";
            dt = Con.OpenDT(sql);
            if (dt.getCount()>0) {
                dt.moveToFirst();
                ss=dt.getString(0);
                if(dt!=null) dt.close();

                return ss;
            }

            sql="SELECT UM_SALIDA FROM P_PRODUCTO WHERE (CODIGO='"+vProd+"') AND (ES_PROD_BARRA=1)";
            dt = Con.OpenDT(sql);
            if (dt.getCount()>0) {
                dt.moveToFirst();
                ss=dt.getString(0);
                if (dt!=null) dt.close();

                return ss;
            } else {
                return "";
            }
        } catch (Exception e) {
            msgbox("Ocurrió un error obteniendo la unidad mínima de venta "+e.getMessage());
            return "";
        }
    }

    public boolean EsUnidadSuperior(String vUM,String vProd )  {
        Cursor dt;
        int cnt;

        try {
            sql = "SELECT * FROM P_FACTORCONV WHERE (UNIDADSUPERIOR='"+vUM+"') AND (PRODUCTO='"+vProd+"') AND (UNIDADSUPERIOR<>'"+gl.umpeso+"')";
            dt = Con.OpenDT(sql);
            cnt=dt.getCount();
            if (dt!=null) dt.close();

            return (cnt>0);
        } catch (Exception e) {
            msgbox(e.getMessage());
            return false;
        }
    }

    public double DameFactor(String vUM,String vProd) {
        Cursor dt;
        double val;

        try {
            sql = "SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (UNIDADSUPERIOR='"+vUM+"')  AND (PRODUCTO='"+vProd+"')";
            dt = Con.OpenDT(sql);
            if(dt.getCount()>0) {
                dt.moveToFirst();
                val=dt.getDouble(0);
                if (dt!=null) dt.close();

                return val;
            } else {
                return 0;
            }
        } catch (Exception e) {
            msgbox(e.getMessage());return 0;
        }
    }

    private int barraBolsa() {
        Cursor dt;
        double ppeso=0,pprecdoc=0,factbolsa,factorconv,diferencia=0;
        String uum,umven,uunistock;
        boolean reservado = false;
        boolean isnew=true;

        porpeso=true;

        try {

            //db.beginTransaction();

            sql = "SELECT CODIGO,CANT,PESO,UNIDADMEDIDA " +
                    "FROM P_STOCKB WHERE (BARRA='" + barcode + "') ";
            dt = Con.OpenDT(sql);

            if (dt.getCount() == 0) {
                sql = "SELECT Barra FROM D_FACTURA_BARRA  WHERE (BARRA='" + barcode + "') ";
                dt = Con.OpenDT(sql);
                //db.endTransaction();
                if (dt.getCount() == 0) {
                    if (dt != null) dt.close();
                    return 0;
                } else {
                    if (dt != null) dt.close();
                    return -1;
                }
            }

            dt.moveToFirst();

            prodid = dt.getString(0);
            cant = dt.getInt(1);
            ppeso = dt.getDouble(2);
            ppeso = mu.round(ppeso, 3);
            uum = dt.getString(3);

            if (dt != null) dt.close();

            if (gl.iddespacho !=null ){

                if (!gl.iddespacho.isEmpty()) {

                    sql="SELECT PRODUCTO, CANTDIF " +
                            "FROM T_VENTA_DESPACHO WHERE (PRODUCTO='"+prodid+"') ";
                    dt=Con.OpenDT(sql);

                    if (dt.getCount()==0) {
                        //Es un producto nuevo, validaremos si al cliente se le pueden vender productos nuevos
                        //y si hay barras disponibles

                        if (gl.permitir_producto_nuevo) {

                            if (reservado) {
                                //La barra no está disponible
                                return -2;
                            }

                        } else {
                            //No se le pueden vender productos nuevos
                            return -3;
                        }

                    }else{
                        //Vamos a validar si está solicitando más producto
                        dt.moveToFirst();

                        diferencia = dt.getDouble(1);

                        if (diferencia==0){

                            if (gl.permitir_cantidad_mayor) {

                                if (reservado) {
                                    //La barra no está disponible
                                    return -2;
                                }

                            } else {
                                //No se le pueden vender cantidades mayores
                                return -4;
                            }
                        }
                    }
                }
            }

            if(dt!=null) dt.close();

            //#CKFK 20191204 Modifiqué la forma de obtener la unidad de medida (um)
            //um = uum;
            //La um se obtenía antes de la umventa
            umven = app.umVenta(prodid);
            um = (umven==gl.umpeso?uum:umven);
            factbolsa = DameProporcionVenta(prodid, gl.cliente, gl.nivel);//#CKFK Modifiqué la forma de obtener el factor de conversion
            // app.factorPres(prodid, umven, um);

            cant = cant; //* factbolsa; #CKFK 19-09-2019 Quité la multiplicación por el factor de conversión porque siempre se debe guardar la Unidad de Medida de la barra

            if (prodPorPeso(prodid)) {
                prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, ppeso, umven);
                if (prc.existePrecioEspecial(prodid, cant, gl.cliente, gl.clitipo, umven, gl.umpeso, ppeso)) {
                    if (prc.precioespecial > 0) prec = prc.precioespecial;
                }
            } else {
                prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, 0, umven);
                if (prc.existePrecioEspecial(prodid, cant, gl.cliente, gl.clitipo, umven, gl.umpeso, 0)) {
                    if (prc.precioespecial > 0) prec = prc.precioespecial;
                }
            }

            //if (prodPorPeso(prodid)) prec=mu.round2(prec/ppeso);
            if (prodPorPeso(prodid)) prec = mu.round2(prec);

            if (prec == 0) {
                msgbox("El producto no tiene precio definido para nivel de precio " + gl.nivel);
                return 0;
            }

            pprecdoc = prec;

            //#CKFK 18-09-2019 Agregué la siguiente validación, de forma tal que el precio solo se multiplique por la el factbolsa
            // cuando sea mayor que 1
            if (factbolsa > 1) {
                prodtot = cant * factbolsa * prec;
            }else{
                prodtot = prec;
            }
            if (prodPorPeso(prodid)) prodtot = prec * ppeso;

            //#CKFK20231120 Se quito el reondeo en total, por error en el total de la factura
            //prodtot = mu.round2(prodtot);

            //region T_BARRA

            try {

                ins.init("T_BARRA");
                ins.add("BARRA", barcode);
                ins.add("CODIGO", prodid);
                ins.add("PRECIO", prodtot);
                ins.add("PESO", ppeso);
                ins.add("PESOORIG", ppeso);
                ins.add("CANTIDAD", cant);
                db.execSQL(ins.sql());
                //toast(barcode);

            } catch (Exception e) {

                Log.d("Err_AF20190702", e.getMessage());

                isnew = false;

                if (!isDialogBarraShowed) {

                    txtBarra.setText("");

                    isDialogBarraShowed = true;

                    dialogBarra.setTitle(R.string.app_name);
                    dialogBarra.setMessage("Borrar la barra \n" + barcode + "\n ?");
                    dialogBarra.setIcon(R.drawable.ic_quest);

                    dialogBarra.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            isDialogBarraShowed = false;
                            borraBarra();
                            try {
                                //db.setTransactionSuccessful();
                            } catch (Exception ee) {
                                String er = ee.getMessage();
                            }
                        }
                    });

                    dialogBarra.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            txtBarra.setText("");
                            txtBarra.requestFocus();
                            isDialogBarraShowed = false;
                        }
                    });

                    dialogBarra.show();
                    txtBarra.requestFocus();

                } else {
                    Log.d("CerrarDialog", "vos");
                    isDialogBarraShowed = false;
                }

//						msgAskBarra("Borrar la barra "+barcode);
                try {
                    //db.endTransaction();
                } catch (Exception e1) {
                }
                return 1;
            }

            //endregion

            prec = mu.round(prec, 2);
            prodtot = mu.round(prodtot, 2);

            ins.init("T_VENTA");
            ins.add("PRODUCTO", prodid);
            ins.add("EMPRESA", emp);

            if (prodPorPeso(prodid)) {
                ins.add("UM", gl.umpeso);//ins.add("UM",gl.umpeso);
            } else {
                if (factbolsa == 1) ins.add("UM", umven);
                else ins.add("UM", umven);
            }

            ins.add("CANT", cant);

            uunistock = DameUnidadMinimaVenta(prodid);
            factorconv = DameProporcionVenta(prodid, gl.cliente, gl.nivel);

            ins.add("FACTOR", factorconv);
            ins.add("UMSTOCK", uunistock);

            if (prodPorPeso(prodid)) {
                //ins.add("PRECIO",gl.prectemp);
                ins.add("PRECIO", prec);
            } else {
                ins.add("PRECIO", prec);
            }

            ins.add("IMP", 0);
            ins.add("DES", 0);
            ins.add("DESMON", 0);
            ins.add("TOTAL", prodtot);

            if (prodPorPeso(prodid)) {
                //ins.add("PRECIODOC",gl.prectemp);
                ins.add("PRECIODOC", pprecdoc);
            } else {
                ins.add("PRECIODOC", pprecdoc);
            }

            ins.add("PESO", ppeso);
            ins.add("VAL1", 0);
            ins.add("VAL2", "");
            ins.add("VAL3", 0);
            ins.add("VAL4", "");
            ins.add("PERCEP", percep);
            ins.add("SIN_EXISTENCIA", 0);

            try {
                db.execSQL(ins.sql());
            } catch (SQLException e) {
                Log.d(e.getMessage(), "");
            }

            if (gl.iddespacho !=null ){
                if (!gl.iddespacho.isEmpty()) actualizaTotalesBarraDespacho();
            }

            actualizaTotalesBarra();

            if (isnew) validaBarraBon();

            //db.setTransactionSuccessful();
            //db.endTransaction();

            return 1;

        } catch (Exception e) {
            //	msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            //	addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            Log.d("Err_On_Insert", e.getMessage());
            //db.endTransaction();
            return 0;
        }

    }

    private void validaBarraBon() {
        clsBonif clsBonif;
        int bcant,bontotal,boncant,bfaltcant,bon;

        gl.bonbarprod=prodid;

        bcant=cantBolsa();
        boncant=cantBonif();
        if (boncant>0) bfaltcant=cantFalt();else bfaltcant=0;

        clsBonif = new clsBonif(this, prodid, bcant, 0);
        if (clsBonif.tieneBonif()) {
            bon=(int) clsBonif.items.get(0).valor;
            gl.bonbarid=clsBonif.items.get(0).lista;
        } else {
            bon=0;gl.bonbarid="";
        }

        bontotal=boncant+bfaltcant;

        //toast("Bolsas : "+bcant+" bon : "+bon+"  / "+bontotal);
        if (bon>bontotal) startActivity(new Intent(this,BonBarra.class));

    }

    private int barraBolsaTrans() {
        Cursor dt;
        double ppeso=0,pprecdoc=0,factbolsa,diferencia=0;
        String uum,umven,uunistock;
        boolean reservado = false;
        boolean isnew=true;

        porpeso=true;

        try {

            db.beginTransaction();

            sql="SELECT CODIGO,CANT,PESO,UNIDADMEDIDA, RESERVADO " +
                    "FROM P_STOCKB WHERE (BARRA='"+barcode+"') ";
            dt=Con.OpenDT(sql);

            if (dt.getCount()==0) {
                sql="SELECT Barra FROM D_FACTURA_BARRA  WHERE (BARRA='"+barcode+"') ";
                dt=Con.OpenDT(sql);

                db.endTransaction();

                if (dt.getCount()==0) {
                    return 0;
                }else{
                    return -1;
                }
            }

            dt.moveToFirst();

            prodid = dt.getString(0);
            cant = dt.getInt(1);
            ppeso = dt.getDouble(2);
            uum = dt.getString(3);
            reservado = (dt.getInt(4)==0?true:false);

            if(dt!=null) dt.close();

            if (gl.iddespacho !=null ){
                if (!gl.iddespacho.isEmpty()) {

                    sql="SELECT PRODUCTO, CANTDIF " +
                            "FROM T_VENTA_DESPACHO WHERE (PRODUCTO='"+prodid+"') ";
                    dt=Con.OpenDT(sql);
                    db.endTransaction();

                    if (dt.getCount()==0) {
                        //Es un producto nuevo, validaremos si al cliente se le pueden vender productos nuevos
                        //y si hay barras disponibles

                        if (gl.permitir_producto_nuevo) {

                            if (reservado) {
                                //La barra no está disponible
                                return -2;
                            }

                        } else {
                            //No se le pueden vender productos nuevos
                            return -3;
                        }

                    }else{
                        //Vamos a validar si está solicitando más producto
                        dt.moveToFirst();

                        diferencia = dt.getDouble(1);

                        if (diferencia==0){

                            if (gl.permitir_cantidad_mayor) {

                                if (reservado) {
                                    //La barra no está disponible
                                    return -2;
                                }

                            } else {
                                //No se le pueden vender cantidades mayores
                                return -4;
                            }
                        }
                    }
                }
            }

            if(dt!=null) dt.close();

            um=uum;
            umven=app.umVenta(prodid);
            factbolsa=app.factorPres(prodid,umven,um);
            cant=cant; //*factbolsa; #CKFK 18-09-2019 Quité la multiplicación por factura bolsa

            //if (sinimp) precdoc=precsin; else precdoc=prec;

            if (prodPorPeso(prodid)) {
                prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, ppeso,umven);
                if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,ppeso)) {
                    if (prctr.precioespecial>0) prec=prctr.precioespecial;
                }
            } else {
                prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, 0,umven);
                if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,0)) {
                    if (prctr.precioespecial>0) prec=prctr.precioespecial;
                }
            }

            if (prodPorPeso(prodid)) prec=mu.round2(prec/ppeso);
            pprecdoc = prec;

            //#CKFK 19-09-2019 Agregué la siguiente validación, de forma tal que el precio solo se multiplique por la el factbolsa
            // cuando sea mayor que 1
            if (factbolsa>1) {
                prodtot = cant*factbolsa*prec;
            }else{
                prodtot = prec;
            }
            if (prodPorPeso(prodid)) prodtot=mu.round2(prec*ppeso);

            //region T_BARRA

            try {

                ins.init("T_BARRA");
                ins.add("BARRA",barcode);
                ins.add("CODIGO",prodid);
                ins.add("PRECIO",prodtot);
                ins.add("PESO",ppeso);
                ins.add("PESOORIG",ppeso);
                ins.add("CANTIDAD",cant);
                db.execSQL(ins.sql());
                //toast(barcode);

            } catch (Exception e) 	{

                Log.d("Err_AF20190702",e.getMessage());

                isnew=false;

                if (!isDialogBarraShowed)	{

                    txtBarra.setText("");

                    isDialogBarraShowed = true;

                    dialogBarra.setTitle(R.string.app_name);
                    dialogBarra.setMessage("Borrar la barra \n"+ barcode  + "\n ?");
                    dialogBarra.setIcon(R.drawable.ic_quest);

                    dialogBarra.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            isDialogBarraShowed = false;
                            borraBarra();
                            try {
                                db.setTransactionSuccessful();
                            } catch (Exception ee) {
                                String er=ee.getMessage();
                            }
                        }
                    });

                    dialogBarra.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            txtBarra.setText("");
                            txtBarra.requestFocus();
                            isDialogBarraShowed = false;
                        }
                    });

                    dialogBarra.show();
                    txtBarra.requestFocus();

                } else {
                    Log.d("CerrarDialog","vos");
                    isDialogBarraShowed=false;
                }

//						msgAskBarra("Borrar la barra "+barcode);
                try {
                    db.endTransaction();
                } catch (Exception e1) {
                }
                return 1;
            }

            //endregion

            prec=mu.round(prec,2);
            prodtot=mu.round(prodtot,2);

            ins.init("T_VENTA");
            ins.add("PRODUCTO",prodid);
            ins.add("EMPRESA",emp);

            if (prodPorPeso(prodid)) {
                ins.add("UM",gl.umpeso);//ins.add("UM",gl.umpeso);
            } else {
                if (factbolsa==1) ins.add("UM",umven);else ins.add("UM",umven);
            }

            ins.add("CANT",cant);

            if (prodPorPeso(prodid)) uunistock=um; else uunistock=umven;

			/*
			if (prodid.equalsIgnoreCase("0006") || prodid.equalsIgnoreCase("0629") || prodid.equalsIgnoreCase("0747") ) {
				Double stfact;

				uunistock=um;
				umven=app.umVenta(prodid);
				stfact=app.factorPres(prodid,uunistock,umven);
				ins.add("FACTOR",stfact);
			} else {
				ins.add("FACTOR",gl.umfactor);
			}
			*/

            double factorconv=DameProporcionVenta(prodid,gl.cliente,gl.nivel);

            ins.add("FACTOR",factorconv);

            ins.add("UMSTOCK",uunistock);

            if (prodPorPeso(prodid)) {
                //ins.add("PRECIO",gl.prectemp);
                ins.add("PRECIO",prec);
            } else {
                ins.add("PRECIO",prec);
            }

            ins.add("IMP",0);
            ins.add("DES",0);
            ins.add("DESMON",0);
            ins.add("TOTAL",prodtot);

            if (prodPorPeso(prodid)) {
                //ins.add("PRECIODOC",gl.prectemp);
                ins.add("PRECIODOC",pprecdoc);
            } else {
                ins.add("PRECIODOC",pprecdoc);
            }

            ins.add("PESO",ppeso);
            ins.add("VAL1",0);
            ins.add("VAL2","");
            ins.add("VAL3",0);
            ins.add("VAL4","");
            ins.add("PERCEP",percep);

            try {
                db.execSQL(ins.sql());
            } catch (SQLException e) {
                Log.d(e.getMessage(),"");
            }

            actualizaTotalesBarra();

            if (gl.iddespacho !=null ){
                if (!gl.iddespacho.isEmpty()) actualizaTotalesBarraDespacho();
            }

            if (isnew) validaBarraBon();

            db.setTransactionSuccessful();
            db.endTransaction();

            return 1;

        } catch (Exception e) {
            //	msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            //	addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            Log.d("Err_On_Insert",e.getMessage());
            //db.endTransaction();
            return 0;
        }

    }

    private boolean barraBonif() {
        Cursor dt;

        try {

            sql="SELECT PRODUCTO FROM T_BARRA_BONIF WHERE (BARRA='"+barcode+"')";
            dt=Con.OpenDT(sql);

            boolean rslt=dt.getCount()>0;

            if(dt!=null) dt.close();

            return rslt;

        } catch (Exception e) {
            return false;
        }
        //return true;
    }
/*

    private boolean barraProducto() {
        Cursor dt;

        try {

            sql="SELECT P_STOCK.CODIGO " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO	" +
                    "WHERE (P_PRODUCTO.CODBARRA='"+barcode+"') ";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();

                gl.gstr=dt.getString(0);gl.um="UN";
                processItem();
                if(dt!=null) dt.close();

                return true;
            }

        } catch (Exception e) {
            Log.d("Error en barraBonif",e.getMessage());
            //msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

        return false;
    }
*/

    private void borraBarra() {
        //clsBonifTran clsBoniftr;
        clsBonif clsBoniftr;
        int bcant,bontotal,boncant,bfaltcant,bon;
        String bprod="";

        try {
            db.execSQL("DELETE FROM T_BARRA WHERE BARRA='"+gl.barra+"' AND CODIGO='"+prodid+"'");
            Log.d("BARRA","Borrar barra");
            actualizaTotalesBarra();

            if (gl.iddespacho !=null ){
                if (!gl.iddespacho.isEmpty()) actualizaTotalesBarraDespacho();
            }

            gl.bonbarprod=prodid;

            bcant=cantBolsa();
            boncant=cantBonif();
            bfaltcant=cantFalt();

            //clsBoniftr = new clsBonifTran(this, prodid, bcant, 0,Con,db);
            clsBoniftr = new clsBonif(this, prodid, bcant, 0);
            if (clsBoniftr.tieneBonif()) {
                bon=(int) clsBoniftr.items.get(0).valor;
                bprod=clsBoniftr.items.get(0).lista;
                gl.bonbarid=clsBoniftr.items.get(0).lista;
            } else {
                bon=0;gl.bonbarid="";
            }

            bontotal=boncant+bfaltcant;

            //toast("Bolsas : "+bcant+" bon : "+bon+"  / "+bontotal);
            if (bon<bontotal) {
                removerBonif(bprod,(bontotal-bon));
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void actualizaTotalesBarra() {
        Cursor dt;
        int ccant;
        double ppeso,pprecio,unfactor,stfact;

        try {

            sql="SELECT Factor FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();
            unfactor=dt.getDouble(0);

            //sql="SELECT COUNT(BARRA),SUM(PESO),SUM(PRECIO) FROM T_BARRA WHERE CODIGO='"+prodid+"'";
            //#CKFK 20190410 se modificó esta consulta para sumar la cantidad y no contar las barras
            sql="SELECT SUM(CANTIDAD),SUM(PESO),SUM(PRECIO) FROM T_BARRA WHERE CODIGO='"+prodid+"'";
            dt=Con.OpenDT(sql);

            ccant=0;ppeso=0;pprecio=0;

            if (dt.getCount()>0) {
                dt.moveToFirst();

                ccant=dt.getInt(0);
                ppeso=dt.getDouble(1);
                pprecio=dt.getDouble(2);
            }

            //Puse esto en comentario
			/*if (prodid.equalsIgnoreCase("0006") || prodid.equalsIgnoreCase("0629") || prodid.equalsIgnoreCase("0747") ) {
				stfact=ccant;
				stfact=stfact/unfactor;
				ccant=(int) stfact;
			}*/

            if(dt!=null) dt.close();

            sql="UPDATE T_VENTA SET Cant="+ccant+",Peso="+ppeso+",Total="+pprecio+" WHERE PRODUCTO='"+prodid+"'";
            db.execSQL(sql);

            sql="DELETE FROM T_VENTA WHERE Cant=0";
            db.execSQL(sql);

            listItems();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void actualizaTotalesBarraDespacho() {
        Cursor dt;
        int ccant;
        double ppeso,pprecio,unfactor,stfact;

        try {

            sql="SELECT Factor FROM T_VENTA_DESPACHO WHERE PRODUCTO='"+prodid+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();
            unfactor=dt.getDouble(0);

            sql="SELECT SUM(CANTIDAD),SUM(PESO),SUM(PRECIO) FROM T_BARRA WHERE CODIGO='"+prodid+"'";
            dt=Con.OpenDT(sql);

            ccant=0;ppeso=0;pprecio=0;

            if (dt.getCount()>0) {
                dt.moveToFirst();

                ccant=dt.getInt(0);
                ppeso=dt.getDouble(1);
                pprecio=dt.getDouble(2);
            }

            if(dt!=null) dt.close();

            sql="UPDATE T_VENTA_DESPACHO SET CANTREC="+ccant+",CANTDIF=CANTSOL -"+ccant+"," +
                    " PESO="+ppeso+",   TOTAL="+pprecio+" WHERE PRODUCTO='"+prodid+"'";
            db.execSQL(sql);

            //#CKFK 20210725 Puse esto en comentario porque voy a necesitar validar si el producto existía en el pedido
            // o es un producto nuevo
			/*sql="DELETE FROM T_VENTA_DESPACHO WHERE CANTDIF=0";
			db.execSQL(sql);*/

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private int cantBolsa() {
        try {
            sql="SELECT BARRA FROM T_BARRA WHERE CODIGO='"+prodid+"'";
            Cursor dt=Con.OpenDT(sql);

            int cant=dt.getCount();
            if(dt!=null) dt.close();

            return cant;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return 0;
        }
    }

    private int cantBonif() {
        try {
            sql="SELECT BARRA FROM T_BARRA_BONIF WHERE PRODUCTO='"+prodid+"'";
            Cursor dt=Con.OpenDT(sql);

            int cant=dt.getCount();
            if(dt!=null) dt.close();

            return cant;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return 0;
        }
    }

    private int cantFalt() {
        try {
            opendb();

            sql="SELECT PRODID FROM T_BONIFFALT WHERE PRODUCTO='"+prodid+"'";
            Cursor dt=Con.OpenDT(sql);

            int cant=dt.getCount();

            if(dt!=null) dt.close();

            return cant;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            return 0;
        }
    }

    private void removerBonif(String bprod,int bcant) {
        Cursor dt;
        String barra,sbarra="";
        int bc=0;

        try {
            for (int i = 1; i == bcant; i++) {

                sql = "SELECT CANT FROM T_BONIFFALT WHERE (PRODID='"+prodid+"') ";
                dt = Con.OpenDT(sql);

                if (dt.getCount() > 0) {
                    dt.moveToFirst();

                    sql="UPDATE T_BONIFFALT SET CANT=CANT-1 WHERE (PRODID='"+prodid+"') ";
                    db.execSQL(sql);

                    sql="DELETE FROM T_BONIFFALT WHERE CANT=0";
                    db.execSQL(sql);

                    if(dt!=null) dt.close();

                } else {

                    sql = "SELECT BARRA FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"') ";
                    dt = Con.OpenDT(sql);

                    if (dt.getCount() > 0) {
                        dt.moveToLast();
                        barra=dt.getString(0);sbarra+=barra+"\n";bc++;

                        sql = "DELETE FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"') AND (BARRA='"+barra+"') ";
                        db.execSQL(sql);
                    }

                    if(dt!=null) dt.close();
                }

                if(dt!=null) dt.close();
            }

            if (bc>0) msgbox("Las barra devueltas : \n"+sbarra);

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }
    }

    //endregion

        private void fillSpinner(){
            Cursor DT;
            String icode,iname;

            spincode.add("0");
            spinlist.add("< TODAS >");

            try {
                sql="SELECT DISTINCT P.LINEA,L.NOMBRE " +
                    "FROM T_VENTA_DESPACHO D INNER JOIN P_PRODUCTO P ON D.PRODUCTO=P.CODIGO " +
                    "INNER JOIN P_LINEA L ON P.LINEA=L.CODIGO " +
                    "WHERE (D.CANTDIF > 0) ORDER BY L.NOMBRE";

                DT=Con.OpenDT(sql);
                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    icode=DT.getString(0);
                    iname=DT.getString(1);

                    spincode.add(icode);
                    spinlist.add(iname);

                    DT.moveToNext();
                }

                if(DT!=null) DT.close();


            } catch (SQLException e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                mu.msgbox(e.getMessage());
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                mu.msgbox( e.getMessage());
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinFam.setAdapter(dataAdapter);

            try {
                spinFam.setSelection(0);
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                spinFam.setSelection(0);
            }

        }

        public String ltrim(String ss,int sw) {
            try{
                int l=ss.length();
                if (l>sw) {
                    ss=ss.substring(0,sw);
                } else {
                    String frmstr="%-"+sw+"s";
                    ss=String.format(frmstr,ss);
                }


            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }
            return ss;
        }

        private boolean prodPorPeso(String prodid) {
            try {
                return app.ventaPeso(prodid);
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                return false;
            }
        }

        private boolean prodBarra(String prodid) {
            try {
                return app.prodBarra(prodid);
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                return false;
            }
        }

        public void limpiaFiltro(View view) {
            try{
                txtFilter.setText("");
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

        public void doFocus(View view) {
            try {
                txtBarra.requestFocus();
            } catch (Exception e) {}
        }

        // Activity Events

        protected void onResume() {
            try{
                super.onResume();
                try {
                    txtBarra.requestFocus();
                } catch (Exception e) {

                }
            }catch (Exception e){
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

        }

    }
