package com.dts.roadp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.edocsdk.Fimador;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import Entidades.Detalle;
import Entidades.Receptor;
import Entidades.RespuestaEdoc;
import Entidades.gFormaPago;
import Entidades.gPagPlazo;
import Entidades.gRucRec;
import Entidades.gUbiRec;
import Entidades.rFE;
import Facturacion.CatalogoFactura;

public class Cobro extends PBase {

	private ListView listView;
	private TextView lblSel,lblPag,lblPend;
	private ProgressDialog progress;
	private ArrayList<clsClasses.clsCobro> items= new ArrayList<clsClasses.clsCobro>();
	private ListAdaptCobro adapter;
	private clsClasses.clsCobro selitem;

	private Runnable printcallback,printclose,printValidate, printexit;
	private printer prn;
	private clsDocCobro fdoc;
	private clsDocFactura fdocf;
	private AppMethods app;

	private String cliid,cod,itemid,prodid,sefect,corel,fserie,dtipo="",fechav;
	private double ttot,tsel,tpag,tpagos,tpend,vefect,plim,cred,pg,sal,ssal,total,monto,pago;
	private boolean peexit;
	private boolean porcentaje = false, validarCred = false;
	private int fflag=1,fcorel,medPago,checkCheck=0, impres=0;
	private String crrf,docfact,anulado;
	private CheckBox cbCheckAll;
	private long fechaven;
	private RadioButton chkFactura,chkContado;

	//Factura Electronica
	private clsClasses.clsMunicipio Municipio = clsCls.new clsMunicipio();
	private clsClasses.clsDepartamento Departamento = clsCls.new clsDepartamento();
	private clsClasses.clsCiudad Ciudad = clsCls.new clsCiudad();
	private clsClasses.clsSucursal Sucursal = clsCls.new clsSucursal();
	private clsClasses.clsCliente Cliente = clsCls.new clsCliente();
	private clsClasses.clsProducto Producto = clsCls.new clsProducto();

	private rFE Factura = new rFE();
	private rFE NotaCredito = new rFE();
	private Detalle detalle = new Detalle();
	private CatalogoFactura Catalogo;

	private String urltoken = "";
	private String usuario = "";
	private String clave = "";
	private String urlDoc = "";
	private String urlDocNT = "";
	private String QR = "";
	private String corelFactura = "";

	private ProgressBar prgCobro;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cobro);

		super.InitBase();
		addlog("Cobro",""+du.getActDateTime(),gl.vend);

		listView = findViewById(R.id.listView1);
		lblSel = findViewById(R.id.lblSel);
		lblPag = findViewById(R.id.lblPag);
		lblPend = findViewById(R.id.lblPend);
		cbCheckAll= findViewById(R.id.cbCheckAll);

		chkFactura = new RadioButton(this,null);
		chkContado = new RadioButton(this,null);

		//#EJC202301191457: PRG.
		prgCobro = findViewById(R.id.prgcobro);
		prgCobro.setVisibility(View.GONE);

		cliid=gl.cliente;

		//#CKFK20230118 Agregamos esta información quemada como variables
		urltoken = gl.url_token;
		usuario = gl.usuario_api;
		clave = gl.clave_api;
		urlDoc = gl.url_doc;
		QR = gl.qr_api;
		urlDocNT = gl.url_emision_nc_b2c;

		app = new AppMethods(this, gl, Con, db);
		Catalogo = new CatalogoFactura(this, Con, db);

		setHandlers();

		initSession();

		clearAll();

		listItems();

		cbCheckAll.setChecked(true);

		showTotals();

		gl.pagomodo=0;
		cod=gl.cliente;

		printcallback= () -> askPrint();

		printclose= () -> {
			if(gl.banderaCobro){
				Cobro.super.finish();
			}else{
				if(browse==4){
					if (gl.validarCred==1) validaCredito(); //#CKFK 20190503 Printclose
					browse = 0;
				}
			}
		};

		printexit= () -> {
			if(gl.banderaCobro){
				Cobro.super.finish();
			}else{
				if(browse==4){
					if (gl.validarCred==1) validaCredito(); //#CKFK 20190503 Printclose
					browse = 0;
				}
			}
			//Cobro.super.finish();
		};

		prn=new printer(this,printexit,gl.validimp);
		fdoc=new clsDocCobro(this,prn.prw,gl.peMon,gl.peDecImp, gl.numSerie, "");
		fdoc.deviceid=gl.numSerie;

		fdocf = new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(cliid),gl.codCliNuevo,gl.peModal);
		fdocf.deviceid=gl.numSerie;
	}

	// Events

	public void paySelect(View view) {

		try{

			calcSelected();

			if (tsel==0) {
				mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
			}

			gl.pagomodo=0;
			gl.pagoval=tsel;
			gl.pagolim=tsel;

			if (dtipo.equalsIgnoreCase("R")) gl.pagocobro=false; else gl.pagocobro=true;

			browse=1;
			if(gl.validarCred!=2) gl.validarCred = 1;

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("Error al seleccionar metodo de pago: "+e.getMessage());
		}

	}

	public void payCash(View view) {

		try{

			if(gl.validarCred!=2) gl.validarCred = 1;

			calcSelected();

			if (tsel==0) {
				mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
			}

			if (dtipo.equalsIgnoreCase("R")) gl.pagocobro=false; else gl.pagocobro=true;

			inputEfectivo();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void checkAll(View view) {

		try{
			check();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("checkAll: "+ e.getMessage());
		}

	}

	public void check() {

		if (items.size()==0) return;

		try{

			if(cbCheckAll.isChecked()){

				dtipo=items.get(0).Tipo;

				if (dtipo.equalsIgnoreCase("R")) {
					clearAll();
					items.get(0).flag=1;

				}else{
					for (int i = 0; i <items.size(); i++) {
						dtipo=items.get(0).Tipo;

						if (!dtipo.equalsIgnoreCase("R")) {
							items.get(i).flag=1;
						}
					}
				}

				adapter.refreshItems();

				calcSelected();
				showTotals();

			}else{
				for (int i = 0; i <items.size(); i++) {
					items.get(i).flag=2;
				}

				adapter.refreshItems();

				calcSelected();
				showTotals();
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("check: "+ e.getMessage());
		}

	}

	public void sinRef(View view) {

		try{

			gl.pagomodo=1;
			gl.pagoval=0;
			gl.pagolim=0;
			gl.pagocobro=true;
			browse=1;
			dtipo="";

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("Sin referencia: " + e.getMessage());
		}
	}

	private void setHandlers(){

		try{

			chkFactura.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (chkFactura.isChecked()==true) chkContado.setChecked(false);
				}
			});

			chkContado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					if (chkContado.isChecked()==true) chkFactura.setChecked(false);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int flag;

					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCobro vItem = (clsClasses.clsCobro) lvObj;

					adapter.setSelectedIndex(position);

					dtipo = vItem.Tipo;
					if (dtipo.equalsIgnoreCase("R")) clearAll();

					flag = vItem.flag;
					if (flag == 0) flag = 1;else flag = 0;
					vItem.flag = flag;

					adapter.refreshItems();

					calcSelected();
					showTotals();

				}

			});

			cbCheckAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(cbCheckAll.isChecked()) checkAll(buttonView);
				}
			});

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("SetHandlers: " + e.getMessage());
		}

	}


	// Main
	private void listItems(){

		Cursor DT;
		clsClasses.clsCobro vItem;

		items.clear();ttot=0;tpag=0;

		try {

			sql=" SELECT DOCUMENTO,TIPODOC,VALORORIG,SALDO,FECHAEMIT,FECHAV " +
				" FROM P_COBRO WHERE CLIENTE='"+cliid+"' ORDER BY FECHAV";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;

			DT.moveToFirst();

			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCobro();
				vItem.Factura=DT.getString(0);
				vItem.Tipo=DT.getString(1);
				vItem.Valor=DT.getDouble(2);

				sal=DT.getDouble(3);
				pg=getDocPago(DT.getString(0),DT.getString(1));
				ssal=sal-pg;if (ssal<0) ssal=0;
				if (ssal>0) fflag=1; else fflag=0;

				vItem.Saldo=ssal;
				vItem.Pago=pg;
				vItem.flag=fflag;
				vItem.fini=sfecha(DT.getLong(4));
				vItem.ffin=sfecha(DT.getLong(5));

				ttot=ttot+DT.getDouble(3);
				tpag+=pg;

				items.add(vItem);

				DT.moveToNext();

			}

			if(DT!=null) DT.close();

			adapter=new ListAdaptCobro(this,items);adapter.cursym=gl.peMon;
			listView.setAdapter(adapter);

			calcSelected();

			showTotals();

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox("listItems: "+ e.getMessage());
		}

	}

	public void doExit(View view){
		try{
			validaCredito();
		}catch (Exception e){
			mu.msgbox("doExit: "+e.getMessage());
		}
	}

	public boolean validaCredito(){
		Cursor DT;
		Cursor DTFecha;
		boolean vValida = true;

		try{
			sql = "SELECT MEDIAPAGO,LIMITECREDITO FROM P_CLIENTE WHERE CODIGO ='"+cod+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			medPago=DT.getInt(0);
			cred=DT.getInt(1);
			gl.facturaVen = 0;

			if (medPago == 4) {

				sql = "SELECT DOCUMENTO,TIPODOC,FECHAV,SALDO FROM P_COBRO WHERE CLIENTE ='"+cod+"' ORDER BY FECHAV";
				DTFecha = Con.OpenDT(sql);
				DTFecha.moveToFirst();

				for (int i = 0; i != DTFecha.getCount(); i++) {
					double tot = 0;
					docfact = DTFecha.getString(0);
					fechaven = DTFecha.getLong(2);
					fechav = sfecha(fechaven);

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					Date strDate = sdf.parse(fechav);
					if (System.currentTimeMillis() > strDate.getTime()) {
						gl.facturaVen += 1;
					}

					sql="SELECT ANULADO,MONTO,PAGO FROM D_COBROD WHERE DOCUMENTO = "+ docfact;
					DT = Con.OpenDT(sql);

					if(DT.getCount() != 0){

						DT.moveToFirst();
						anulado = DT.getString(0);
						monto = DT.getDouble(1);
						pago = DT.getDouble(2);
						tot = monto - pago;

						if (tot == 0){
							if (anulado.equals("N")) {
								gl.facturaVen -= 1;
							}
						}

					}

					DTFecha.moveToPosition(i);
				}
			}

			if(DT!=null) DT.close();

			if(gl.vcredito){
				if(gl.facturaVen<0) gl.facturaVen=0;
				if(gl.facturaVen > 0) {
					vValida = false;
					msgAskFact();
				}else if(gl.facturaVen==0 & gl.media==4){
					//#AAS - 2019-03-21 - Cuando el credito disponible (gl.credito) del cliente sea  menor que 0 voy a preguntar si quiere hacer la venta al contado
					if (gl.credito<=0) {
						vValida = false;
						msgAskFact();
					}
				}else{
					exit();
				}
			}else{
				exit();
			}

		}catch (Exception e){
			mu.msgbox("validaCredito: "+e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return vValida;
	}

	private void  msgAskFact() {
		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Road");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(chkFactura.getParent()!= null){
				((ViewGroup) chkFactura.getParent()).removeView(chkFactura);
			}

			if(chkContado.getParent()!= null){
				((ViewGroup) chkContado.getParent()).removeView(chkContado);
			}

			if(gl.facturaVen > 0){

				alert.setMessage("Cliente tiene " +gl.facturaVen+ " Facturas vencidas.");

				chkFactura.setText("Pagar Facturas Vencidas");
				chkContado.setText("Continuar la venta al contado");

			} else if(gl.credito<=0){

				alert.setMessage("Cliente no tiene credito actualmente.");

				chkFactura.setText("Pagar Facturas");
				chkContado.setText("Continuar la venta al contado");

			}

			layout.addView(chkFactura);
			layout.addView(chkContado);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					if(chkFactura.isChecked()){
						closekeyb();
						layout.removeAllViews();
					} else if (chkContado.isChecked()){
						initVenta();
						layout.removeAllViews();
					}else{
						toast("Seleccione accion a realizar");
						closekeyb();
						msgAskFact();
					}
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
					layout.removeAllViews();
				}
			});

			alert.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void initVenta(){
		try{
			if (gl.peModal.equalsIgnoreCase("APR")) {
				startActivity(new Intent(this,Aprofam1.class));
			} else {
				browse=3;
				onPause();
				onResume();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void createDoc(){

		try{

			if (gl.pagomodo==0) {
				docList();
				if (!applyPay()) return;
			}

			if (gl.pagocobro) {
				if (saveCobro()) {
					listItems();
					if (dtipo.equalsIgnoreCase("R")) {
						if (prn.isEnabled()) {
							fdocf.buildPrint(crrf, 0, gl.peModal);
							prn.printask(printcallback);
						} else if (!prn.isEnabled()) {
							fdocf.buildPrint(crrf, 0, gl.peModal);

							if (gl.validarCred == 1) {
								validaCredito();
							} else if (gl.validarCred == 2) {
								Cobro.super.finish();
							}

							gl.validarCred = 0;
						}
					} else {

						if (prn.isEnabled()) {
							fdoc.buildPrint(corel, 0, gl.peModal);
							browse = 4;
							prn.printask(printcallback);
						} else if (!prn.isEnabled()) {
							fdoc.buildPrint(corel, 0, gl.peModal);

							if (gl.validarCred == 1) {
								validaCredito();
							} else if (gl.validarCred == 2) {
								Cobro.super.finish();
							}
							gl.validarCred = 0;
						}
					}
				}
			} else {

				if (saveCobroPendiente()) {
					try {

						//#AT20230123 Mostrar progress en certificación de facturas pendientes de pago
						ProgressDialog("Certificando factura...");

						Handler mtimer = new Handler();
						Runnable mrunner= () -> {
							CertificarFacturaDGI();
							ImprimirDocumento();
						};
						mtimer.postDelayed(mrunner,3000);

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("createDoc: " + e.getMessage());
		}

	}

	private void ImprimirDocumento() {
		try {
			listItems();
			if (dtipo.equalsIgnoreCase("R")) {
				if (prn.isEnabled()) {
					fdocf.buildPrint(crrf, 0, gl.peModal);
					prn.printask(printcallback);
				} else if (!prn.isEnabled()) {
					fdocf.buildPrint(crrf, 0, gl.peModal);

					if (gl.validarCred == 1) {
						validaCredito();
					} else if (gl.validarCred == 2) {
						Cobro.super.finish();
					}

					gl.validarCred = 0;
				}
			} else {

				if (prn.isEnabled()) {
					fdoc.buildPrint(corel, 0, gl.peModal);
					browse = 4;
					prn.printask(printcallback);
				} else if (!prn.isEnabled()) {
					fdoc.buildPrint(corel, 0, gl.peModal);

					if (gl.validarCred == 1) {
						validaCredito();
					} else if (gl.validarCred == 2) {
						Cobro.super.finish();
					}
					gl.validarCred = 0;
				}
			}
		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}
	}

	public String sfecha(long f) {
		long vy,vm,vd;
		String s;

		if (String.valueOf(f).length()==12){
			f = f/100000000;
		}else{
			f = f/1000000;
		}

		vy=(long) f/10000;f=f % 10000;
		vm=(long) f/100;f=f % 100;
		vd=f;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"/";} else {s=s+"0"+String.valueOf(vd)+"/";}
		if (vm>9) { s=s+String.valueOf(vm)+"/";} else {s=s+"0"+String.valueOf(vm)+"/";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s;
	}

	private boolean saveCobro(){

		Cursor DT;
		Cursor DT2;
		double tpago;
		String doc="";

		if (!assignCorel()) return false;

		corel= correlativo_recibo();
		fecha=du.getActDateTime();
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());


		try {

			if (!dtipo.equalsIgnoreCase("R")) {

				db.beginTransaction();

				// Encabezado

				sql="SELECT SUM(VALOR) FROM T_PAGO";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				tpago=DT.getDouble(0);

				ins.init("D_COBRO");
				ins.add("COREL",corel);
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("EMPRESA",gl.emp);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",gl.cliente);
				ins.add("KILOMETRAJE",0);
				ins.add("TOTAL",tpago);
				ins.add("DEPOS","");
				ins.add("CORELC","");
				ins.add("BANDERA","");
				ins.add("STATCOM","N");
				ins.add("CALCOBJ","N");
				ins.add("IMPRES",0);
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("SERIE",fserie);
				ins.add("CORELATIVO",fcorel);

				db.execSQL(ins.sql());

				if (gl.pagomodo==0) {

					// Cobro regular - Documentos

					sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD";
					DT=Con.OpenDT(sql);
					DT.moveToFirst();

					while (!DT.isAfterLast()) {

						ins.init("D_COBROD");
						ins.add("COREL",corel);
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("DOCUMENTO",DT.getString(0));
						ins.add("TIPODOC",DT.getString(1));
						ins.add("MONTO",DT.getDouble(2));
						ins.add("PAGO",DT.getDouble(3));
						ins.add("CONTRASENA","");
						ins.add("ID_TRANSACCION",0);
						ins.add("REFERENCIA","");
						ins.add("ASIGNACION","");
						db.execSQL(ins.sql());
						DT.moveToNext();
					}

				} else {

					ins.init("D_COBROD_SR");
					ins.add("COREL",corel);
					ins.add("DOCUMENTO",gl.cliente);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("TIPODOC","SR");
					ins.add("MONTO",tpago);
					ins.add("PAGO",tpago);
					ins.add("CONTRASENA","1");
					db.execSQL(ins.sql());

				}

				// Pagos

				sql="SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				DT=Con.OpenDT(sql);

				if (DT.getCount()>0){

					DT.moveToFirst();

					while (!DT.isAfterLast()) {

						ins.init("D_COBROP");
						ins.add("COREL",corel);
						ins.add("ITEM",DT.getInt(0));
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("CODPAGO",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("VALOR",DT.getDouble(3));
						ins.add("DESC1",DT.getString(4));
						ins.add("DESC2",DT.getString(5));
						ins.add("DESC3",DT.getString(6));
						ins.add("DEPOS","N");
						db.execSQL(ins.sql());

						DT.moveToNext();
					}

					// Ultimo corel
					sql="UPDATE P_CORRELREC SET ACTUAL="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";
					db.execSQL(sql);
				}

				db.setTransactionSuccessful();
				db.endTransaction();

			}else{

				db.beginTransaction();

				sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD";
				DT2=Con.OpenDT(sql);

				if (DT2.getCount()>0){
					DT2.moveToFirst();
					doc=DT2.getString(0);
					crrf = doc;
				}

				sql="SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				DT=Con.OpenDT(sql);

				if (DT.getCount()>0) {

					DT.moveToFirst();

					while (!DT.isAfterLast()) {

						ins.init("D_FACTURAP");
						ins.add("COREL",doc);
						ins.add("ITEM",DT.getInt(0));
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("CODPAGO",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("VALOR",DT.getDouble(3));
						ins.add("DESC1",DT.getString(4));
						ins.add("DESC2",DT.getString(5));
						ins.add("DESC3",DT.getString(6));
						ins.add("DEPOS","N");
						db.execSQL(ins.sql());
						DT.moveToNext();
					}

					sql = "DELETE FROM P_COBRO WHERE DOCUMENTO='" + doc + "'";
					db.execSQL(sql);

					db.setTransactionSuccessful();
					db.endTransaction();

				}

			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			db.endTransaction();
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("saveCobro_ "+ e.getMessage());return false;
		}

		return true;
	}

	private boolean saveCobroPendiente() {

		Cursor DT, DT2;
		String doc = "";

		if (!assignCorel()) return false;

		corel= correlativo_recibo();
		fecha=du.getActDateTime();
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());

		try {

			db.beginTransaction();

			sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD";
			DT2=Con.OpenDT(sql);

			if (DT2.getCount()>0){
				DT2.moveToFirst();
				doc=DT2.getString(0);
				crrf = doc;
			}

			if (DT2 != null)  DT2.close();

			sql="SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {

				DT.moveToFirst();

				while (!DT.isAfterLast()) {

					ins.init("D_FACTURAP");
					ins.add("COREL",doc);
					ins.add("ITEM",DT.getInt(0));
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("CODPAGO",DT.getInt(1));
					ins.add("TIPO",DT.getString(2));
					ins.add("VALOR",DT.getDouble(3));
					ins.add("DESC1",DT.getString(4));
					ins.add("DESC2",DT.getString(5));
					ins.add("DESC3",DT.getString(6));
					ins.add("DEPOS","N");
					db.execSQL(ins.sql());
					DT.moveToNext();
				}

				sql = "DELETE FROM P_COBRO WHERE DOCUMENTO='" + doc + "'";
				db.execSQL(sql);

			}

			if (DT != null) DT.close();

			db.setTransactionSuccessful();
			db.endTransaction();

			LlenaDatosFactura(doc);

		} catch (Exception e) {
			db.endTransaction();
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
			return false;
		}

		return true;
	}

	private boolean LlenaDatosFactura(String doc) {

		Cursor DT, DT2;

		try {

			sql="SELECT COREL, CLIENTE, SERIE, CORELATIVO FROM D_FACTURA WHERE COREL= '" + doc +"'";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			if (DT != null) {

				if (DT.getCount() > 0) {

					corelFactura = DT.getString(0);
					Sucursal = Catalogo.getSucursal();
					Cliente = Catalogo.getCliente(DT.getString(1));

					Factura.gDGen.iTpEmis = "01"; // 'Fijo salvo que sea autorización en contingencia cambiar a valor 04
					Factura.gDGen.iDoc = "01"; //'Para Factura fijo.
					Factura.gDGen.dNroDF = StringUtils.right("0000000000" + (DT.getInt(3)), 10);//String.valueOf(); //P_COREL.Correlativo
					Factura.gDGen.dPtoFacDF = DT.getString(2); // BeSucursal.CODIGO '"002" 'Punto de Facturación del documento fiscal. (Ruta, Serie del disp.)
					Factura.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";// 'Fecha de la FM.
					Factura.gDGen.iNatOp = "01"; //'Venta fijo.
					Factura.gDGen.iTipoOp = 1; //'Fijo.
					Factura.gDGen.iDest = 1; //'Fijo 1
					Factura.gDGen.iFormCAFE = 2; //'Fijo 2.
					Factura.gDGen.iEntCAFE = 1; //'Fijo 2
					Factura.gDGen.dEnvFE = 1; //'Fijo 1
					Factura.gDGen.iProGen = 2; //'Fijo 2
					Factura.gDGen.iTipoTranVenta = 1; // Fijo 1
					Factura.gDGen.iTipoSuc = 2; //'Fijo 2
					Factura.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + Cliente.codigo + ";" + Sucursal.sitio_web + ";";

					Factura.gDGen.Emisor.dNombEm =Sucursal.nombre;
					Factura.gDGen.Emisor.dTfnEm = Sucursal.telefono;
					Factura.gDGen.Emisor.dSucEm = Sucursal.codigo;
					Factura.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
					Factura.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
					Factura.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
					Factura.gDGen.Emisor.dDirecEm = Sucursal.direccion;
					Factura.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
					Factura.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
					Factura.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

					if (!Sucursal.codubi.isEmpty() || Sucursal.codubi != null) {

						Ciudad = clsCls.new clsCiudad();
						Ciudad = Catalogo.getCiudad(Sucursal.codubi);

						if (Ciudad!=null) {

							Factura.gDGen.Emisor.gUbiEm.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
							Factura.gDGen.Emisor.gUbiEm.dDistr = (Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
							Factura.gDGen.Emisor.gUbiEm.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

							if (Ciudad.provincia.isEmpty()) {
								Factura.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
							}

						} else {
							toastlongd("No se encontraron los datos de la ubicación para este código:" + Cliente.ciudad);
						}
					}

					Factura.gDGen.Receptor = new Receptor();
					Factura.gDGen.Receptor.gRucRec = new gRucRec();
					Factura.gDGen.Receptor.gUbiRec = new gUbiRec();

					Factura.gDGen.Receptor.gRucRec.dTipoRuc = Cliente.tipoContribuyente;
					Factura.gDGen.Receptor.iTipoRec = Cliente.tipoRec;
					Factura.gDGen.Receptor.dCorElectRec = Cliente.email;
					Factura.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(Cliente.telefono);
					Factura.gDGen.Receptor.cPaisRec = Cliente.codPais;
					Factura.gDGen.Receptor.dNombRec = Cliente.nombre;
					Factura.gDGen.Receptor.dDirecRec = (Cliente.direccion==null?"":Cliente.direccion.substring(0,(Cliente.direccion.length()>=100?100:Cliente.direccion.length())));
					Factura.gDGen.Receptor.gUbiRec.dCodUbi = (Cliente.ciudad==null?"":Cliente.ciudad);

					if (Cliente.ciudad != null) {

						if (!Cliente.ciudad.isEmpty() ){

							Ciudad = clsCls.new clsCiudad();
							Ciudad = Catalogo.getCiudad(Cliente.ciudad);

							if (Ciudad!=null) {

								Factura.gDGen.Receptor.gUbiRec.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
								Factura.gDGen.Receptor.gUbiRec.dDistr = (Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
								Factura.gDGen.Receptor.gUbiRec.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

								if (Ciudad.provincia.isEmpty()) {
									Factura.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
								}

							} else {
								if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
									toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
								}
							}
						} else {
							if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
								toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
							}
						}
					} else {
						if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
						}
					}

					clsClasses.clsRUC BeRUC= Catalogo.getRUC(Cliente.nit);
					if (Factura.gDGen.Receptor.iTipoRec.equals("01") || Factura.gDGen.Receptor.iTipoRec.equals("03")) {

						if(!BeRUC.sRUC.trim().equals("")){
							Factura.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
						}else{
							toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
						}

						if (!BeRUC.sDV.trim().equals("")) {
							Factura.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
						} else {
							toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
						}

					}else{

						if(!BeRUC.sRUC.trim().equals("")){
							Factura.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
						}else{
							toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
						}

						if (!BeRUC.sDV.trim().equals("")) {
							Factura.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
						} else {
							Factura.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
							Factura.gDGen.Receptor.gRucRec.dDV = "";
						}

					}

					int CorrelativoFac = 1;
					double TotalFact = 0;
					String CodProducto = "";
					boolean porpeso;

					sql = "SELECT PRODUCTO, FACTOR, CANT, TOTAL, PESO, PRECIODOC FROM D_FACTURAD WHERE COREL = '" + doc + "'";
					DT2 =Con.OpenDT(sql);
					DT2.moveToFirst();

					while (!DT2.isAfterLast()) {

						CodProducto = DT2.getString(0);
						Detalle detalle = new Detalle();

						Producto = clsCls.new clsProducto();
						Producto = Catalogo.getProducto(CodProducto);

						detalle.dSecItem = CorrelativoFac;
						detalle.dDescProd = Producto.nombre; //Hay que ver de donde se obtiene el nombre del producto
						detalle.dCodProd = Producto.codigo;

						porpeso= app.ventaPeso(CodProducto);

						if (!Producto.um.isEmpty()) {
							String CodDGI;

							if (porpeso) {
								CodDGI = Catalogo.getUMDGI(gl.umpeso);
							} else {
								CodDGI = Catalogo.getUMDGI(Producto.um);
							}

							if (!CodDGI.isEmpty()) {
								detalle.cUnidad = CodDGI;
							} else {
								detalle.cUnidad = gl.unidad_medida_defecto; //Utiliza codigo de la cgi hy que sacarlo con una consulta
							}
						}

						//Definir que se va enviar en la cantidad.
						if (porpeso) {
							detalle.dCantCodInt = mu.formatTresDecimales(mu.round(DT2.getDouble(4), 3));
						} else {
							if (app.esRosty(Producto.codigo)) {
								detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(DT2.getDouble(2) * DT.getDouble(1)));
							} else {
								detalle.dCantCodInt = mu.formatDosDecimales(DT2.getDouble(2));
							}
						}

						String TotalItem = mu.formatDosDecimales(mu.round2dec(DT2.getDouble(3)));

						//Validar esto preguntar #AT20221019
						if (Producto.subBodega.length() > 1) {
							detalle.dCodCPBSabr = Producto.subBodega.substring(0, 2);
						}

						detalle.dCodCPBScmp = Producto.subBodega;

						if (Factura.gDGen.Receptor.iTipoRec.equals("03") ) {
							if (Producto.subBodega.isEmpty()) {
								toastlong("El código de familia  no puede ser vacío para este tipo de receptor.");
							}
						}

						detalle.gPrecios.dPrUnit = mu.formatDosDecimales(mu.round2dec(DT2.getDouble(5)));
						detalle.gPrecios.dPrUnitDesc = "0.000000";
						detalle.gPrecios.dPrItem = TotalItem;
						detalle.gPrecios.dValTotItem = TotalItem;
						detalle.gITBMSItem.dTasaITBMS = "00";
						detalle.gITBMSItem.dValITBMS = "0.00";
						Factura.Detalles.add(detalle);

						CorrelativoFac++;
						TotalFact += mu.round2dec(DT2.getDouble(3));

						DT2.moveToNext();
					}

					gFormaPago Pagos = new gFormaPago();

					String Total = mu.formatDosDecimales(TotalFact);

					Factura.gTot.dTotNeto = Total;
					Factura.gTot.dTotITBMS = "0.00";
					Factura.gTot.dTotGravado = "0.00";
					Factura.gTot.dTotDesc = "0.00";
					Factura.gTot.dVTot = Total;
					Factura.gTot.dTotRec = Total;
					Factura.gTot.dNroItems = String.valueOf(Factura.Detalles.size());
					Factura.gTot.dVTotItems = Total;

					if (Cliente.mediapago == 4) {
						Pagos.iFormaPago = "01";
						Factura.gTot.iPzPag = "2";

						Factura.gTot.gPagPlazo = new ArrayList();
						gPagPlazo PagoPlazo = new gPagPlazo();
						PagoPlazo.dSecItem = "1";

						PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(Cliente.diascredito);
						PagoPlazo.dValItPlazo = Total;
						PagoPlazo.dInfPagPlazo = StringUtils.right("000000000000000" +  String.valueOf(Cliente.diascredito),15);

						Factura.gTot.gPagPlazo.add(PagoPlazo);
					} else {
						Pagos.iFormaPago = "02";
						Factura.gTot.iPzPag = "1";
					}

					Pagos.dVlrCuota = Total;
					Factura.gTot.gFormaPago.add(Pagos);

				} // Fin count > 0
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage() + " Err_2303131027" );
		}

		return true;
	}

	public void ProgressDialog(String mensaje) {
		try {
			progress = new ProgressDialog(this);
			progress.setMessage(mensaje);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.setProgress(0);
			progress.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void CertificarFacturaDGI() {

		try	{
			Log.d("Ruta",this.getApplicationContext().getFilesDir().toString());

			Fimador Firmador = new Fimador(this);
			RespuestaEdoc RespuestaEdocFac = new RespuestaEdoc();

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocFac = Firmador.EmisionDocumentoBTBTimeOut(5000,Factura, urltoken, usuario, clave, urlDoc, gl.ambiente);

					if (RespuestaEdocFac.Cufe == null) {
						for (int i = 0; i < 2; i++) {
							if (RespuestaEdocFac.Cufe == null && !RespuestaEdocFac.Estado.equals("15")) {
								RespuestaEdocFac = Firmador.EmisionDocumentoBTBTimeOut(5000,Factura, urltoken, usuario, clave, urlDoc, gl.ambiente);

								if (RespuestaEdocFac.Cufe != null) {
									break;
								}
							} else {
								break;
							}
						}
					}
				} catch (Exception e) {
					addlog(Objects.requireNonNull(new Object() { }.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
				}

			} else {
				//#AT20230315 LLamdo BTC, cambiar valores en campos del encabezado
				//Tipo de emisión (01:Autorización Previa normal,
				//02:Autorización Previa contingencia,
				//03:Autorización Posterior normal, 04:Autorización Posterior contingencia)

				Factura.gDGen.iTpEmis = "02";
				Factura.gDGen.dMotCont = "Autorización Previa contingencia";
				Factura.gDGen.dFechaCont = Factura.gDGen.dFechaEm;

				RespuestaEdocFac = Firmador.EmisionDocumentoBTC(Factura,gl.url_b2c_hh,"/data/data/com.dts.roadp/"+gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
			}

			//#AT20230316 Nueva forma de guardar en D_CONTROL
			clsClasses.clsControlFEL ControlFEL = clsCls.new clsControlFEL();
			if (RespuestaEdocFac != null) {
				ControlFEL.Cufe = (RespuestaEdocFac.Cufe == null ? "" : RespuestaEdocFac.Cufe);
				ControlFEL.Estado = (RespuestaEdocFac.Estado == null ? "" : RespuestaEdocFac.Estado);
				ControlFEL.Mensaje = (RespuestaEdocFac.MensajeRespuesta == null ? "" : RespuestaEdocFac.MensajeRespuesta);
				ControlFEL.ValorXml = RespuestaEdocFac.XML != null ? Catalogo.ReplaceXML(RespuestaEdocFac.XML) : "";
				ControlFEL.QR = (RespuestaEdocFac.UrlCodeQR == null ? "" : RespuestaEdocFac.UrlCodeQR);
				ControlFEL.Fecha_Autorizacion = RespuestaEdocFac.FechaAutorizacion;
				ControlFEL.Numero_Autorizacion = (RespuestaEdocFac.NumAutorizacion == null ? "" : RespuestaEdocFac.NumAutorizacion);
			}

			ControlFEL.TipoDoc = Factura.gDGen.iDoc;
			ControlFEL.NumDoc = Factura.gDGen.dNroDF;
			ControlFEL.Sucursal = gl.sucur;
			ControlFEL.Caja = Factura.gDGen.dPtoFacDF;
			String[] fechaEnvio = Factura.gDGen.dFechaEm.split("-05:00", 0);
			ControlFEL.FechaEnvio = fechaEnvio[0];
			ControlFEL.TipFac = Factura.gDGen.iDoc;
			ControlFEL.FechaAgr = String.valueOf(du.getFechaCompleta());
			ControlFEL.Corel = corelFactura;
			ControlFEL.Ruta = gl.ruta;
			ControlFEL.Vendedor = gl.vend;
			ControlFEL.Correlativo = Factura.gDGen.dNroDF;

			if (RespuestaEdocFac.Estado.equals("2")) {
				toastlong("FACTURA CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocFac.Estado + " - " + RespuestaEdocFac.MensajeRespuesta);
			} else {
				toastlong("ERR_233121237A: NO SE LOGRÓ CERTIFICAR LA FACTURA -- " + " ESTADO: " + RespuestaEdocFac.Estado + " - " + (RespuestaEdocFac.MensajeRespuesta == null ? "":RespuestaEdocFac.MensajeRespuesta));
			}

			try {
				ActualizaFacturaTmp(corelFactura, ControlFEL);
				Catalogo.UpdateEstadoFactura(RespuestaEdocFac.Cufe, RespuestaEdocFac.Estado, corelFactura);
			} catch (Exception e) {
				msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
			}

			progress.cancel();
		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}finally {
			progress.cancel();
		}
	}

	private void ActualizaFacturaTmp(String Corel, clsClasses.clsControlFEL ControlFEL) {
		try {
			if (!Catalogo.ExisteFacturaDControl(Corel).isEmpty()) {
				Catalogo.ActualizaFELControl(ControlFEL, Corel);
			} else {
				Catalogo.InsertarFELControl(ControlFEL);
			}
		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}
	}

	public boolean ConexionValida() {

		boolean valida = false;

		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnected()) {
				valida = true;
			}

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return valida;
	}

	private boolean certifico_factura_pendiente_pago= false;

	private class CertificarFactura extends AsyncTask<String, Void, Boolean> {

		boolean exito = false;

		@Override
		protected Boolean doInBackground(String... params) {

			try {
				CertificarFacturaDGI();
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				toast("Certificó: " + certifico_factura_pendiente_pago);
				prgCobro.setVisibility(View.GONE);
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}

		}

		@Override
		protected void onPreExecute() {
			try {

			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {

			} catch (Exception e) {

			}
		}

	}

	private String correlativo_recibo(){

		Cursor DT;
		int cor=0;
		String crr = "";

		try{

			sql="SELECT SERIE,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'" ;
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();
				cor =DT.getInt(1)+1;

				crr= DT.getString(0) + StringUtils.right("000000" + Integer.toString(cor), 6);

			}else{
				crr=gl.ruta+"_"+mu.getCorelBase();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return crr;

	}

	private void docList(){

		clsClasses.clsCobro vItem;

		int j=0;

		try {

			sql="DELETE FROM T_PAGOD";
			db.execSQL(sql);

			for (int i = 0; i < items.size(); i++ ) {

				vItem=items.get(i);

				if (vItem.flag==1) {

					if (vItem.Saldo>0) {

						j+=1;

						ins.init("T_PAGOD");
						ins.add("ITEM",j);
						ins.add("DOCUMENTO",vItem.Factura);
						ins.add("TIPODOC",vItem.Tipo);
						ins.add("MONTO",vItem.Saldo);
						ins.add("PAGO",0);
						db.execSQL(ins.sql());

					}
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("docList: "+ e.getMessage());
		}

	}

	private boolean applyPay(){
		Cursor DT;
		double tpago,apago,saldo,monto;
		int id;

		try {

			sql="SELECT SUM(VALOR) FROM T_PAGO";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {
				mu.msgbox("Total de pagos = 0 ");return false;
			}

			DT.moveToFirst();

			tpago=DT.getDouble(0);
			apago=tpago;

			sql="SELECT ITEM,MONTO FROM T_PAGOD";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				while (!DT.isAfterLast()) {
					id=DT.getInt(0);
					monto=DT.getDouble(1);

					if (apago>=monto) {
						saldo=monto;
						apago-=saldo;
					} else {
						saldo=apago;
						apago=0;
					}

					sql="UPDATE T_PAGOD SET PAGO="+saldo+" WHERE ITEM="+id;
					db.execSQL(sql);

					//MU.msgbox(sql+"\n"+apago);

					if (apago<=0) break;

					DT.moveToNext();

				}

				if(DT!=null) DT.close();

				return true;

			}else  {
				return  false;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("ApplyPay: "+ e.getMessage());return false;
		}

	}


	// Pago Efectivo

	private void inputEfectivo() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Pago Efectivo");
			alert.setMessage("Valor a pagar");
			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(""+ mu.round(tsel,2));
			input.requestFocus();

			alert.setCancelable(false);
			showkeyb();

			checkCheck =  1;

			alert.setPositiveButton("Aplicar", (dialog, whichButton) -> {
				peexit=false;
				sefect=input.getText().toString();
				checkCash();
			});

			alert.setNegativeButton("Cancelar", (dialog, whichButton) -> {
				peexit=true;
				closekeyb();
			});

			alert.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void checkCash() {

		double epago;

		try {

			//#CKFK 2019-04-10 Quité epago=tsel; porque aquí se debe asignar el valor ingresado por el usuario
			epago=Double.parseDouble(sefect);

			if (epago==0) return;

			if (epago<0) throw new Exception("Err_233131022: Monto pago es 0 en sefect.");

			if (epago>tsel) {
				mu.msgbox("Total a pagar mayor que total de monto seleccionado");return;
			}

			if (!gl.pagocobro){
				if (epago<tsel) {
					mu.msgbox("Pago no está completo, no se puede aplicar el pago");
					return;
				}
			}

			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");
			ins.add("ITEM",1);
			ins.add("CODPAGO",1);
			ins.add("TIPO","E");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");
			db.execSQL(ins.sql());

			msgAskSave("¿Aplicar pago?");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			inputEfectivo();
			mu.msgbox("Err_233131019_checkCash: "+e.getMessage());
		}

	}


	// Impresion

	private void singlePrint() {
		try{
			prn.printask(printcallback);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	// Aux

	private void showTotals(){

		try{

			total = tsel + tpagos;

			lblSel.setText(mu.frmcur(total));
			lblPag.setText(mu.frmcur(tpagos));

			if (tsel>=0.01) {
				lblPend.setText(mu.frmcur(tsel));
			} else {
				lblPend.setText(mu.frmcur(0));
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("No es posible mostrar los totales "+e.getMessage());
		}


	}

	private void calcSelected() {

		try{

			clsClasses.clsCobro vItem;
			Object lvObj;
			int flag,dc;
			double val;

			tsel=0;
			tpagos=0;

			if (adapter!=null){

				if (adapter.getCount()>0){

					dc=adapter.getCount();

					for (int i = 0; i < dc; i++ ) {
						lvObj = listView.getItemAtPosition(i);
						vItem = (clsClasses.clsCobro)lvObj;

						flag=vItem.flag;
						if (flag==1) {

							if(vItem.Pago > 0){

								val=vItem.Pago;
								tpagos+=val;

								//#CKFK 20190412 Agregué esto para que se sumara el dato de lo  para que llegue
								val=vItem.Saldo;
								tsel+=val;

							} else{

								val=vItem.Saldo;
								tsel+=val;

							}
						}
					}

				}

			}


		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("calcSelected: "+ e.getMessage());
		}
	}

	private void clearAll() {
		try{
			for (int i = 0; i < items.size(); i++ ) {
				items.get(i).flag=0;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private double getDocPago(String doc,String ptipo){
		Cursor DT;
		double tp;

		try {

			sql="SELECT SUM(PAGO) FROM D_COBROD "+
					"WHERE (ANULADO='N') AND (DOCUMENTO='"+doc+"') AND (TIPODOC='"+ptipo+"') ";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				tp=DT.getDouble(0);

			}else{
				tp=0;
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tp=0;
		}

		return tp;

	}

	private void initSession(){

		try {

			sql="DELETE FROM T_PAGO";

			db.execSQL(sql);

			sql="DELETE FROM T_PAGOD";
			db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("initSession"+ e.getMessage());
		}

	}

	private boolean assignCorel(){
		Cursor DT;
		int ca,ci,cf,ca1,ca2;

		fcorel=0;fserie="";

		try {

			sql="SELECT SERIE,INICIAL,FINAL,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()==0)return false;

			DT.moveToFirst();

			fserie=DT.getString(0);
			ci=DT.getInt(1);
			cf=DT.getInt(2);
			ca=DT.getInt(3);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			fcorel=0;fserie="";
			mu.msgbox("No está definido correlativo de recibos.");return false;
		}

		fcorel=ca+1;
		if (fcorel>cf) toast("Se ha acabado el talonario de los recibos.");

		return true;

	}

	private void askPrint() {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", (dialog1, which) -> {

				impres++;toast("Impres "+impres);

				try {
					sql="UPDATE D_COBRO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
					db.execSQL(sql);
				} catch (Exception e) {
					msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
				}

				if (dtipo.equalsIgnoreCase("R")) {
					sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+crrf+"'";
					db.execSQL(sql);
				}

				if (impres>1) {

/* try {
sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+gl.dvcorreld+"'";
db.execSQL(sql);
} catch (Exception e) {
addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
}*/

					gl.brw=0;

				} else {

					if (dtipo.equalsIgnoreCase("R")) {
						if (prn.isEnabled()) {
							fdocf.buildPrint(crrf,1,gl.peModal);
							//prn.printnoask(printcallback, "print.txt");
							prn.printnoask(printclose, "print.txt");
						}
					}else {
						if (prn.isEnabled()) {
							fdoc.buildPrint(corel,1,gl.peModal);
							//impres=0;
							browse = 4;
							//prn.printnoask(printcallback, "print.txt");
							prn.printnoask(printcallback, "print.txt");
						}
					}

				}

				if(gl.validarCred==1){
					validaCredito();//#CKFK 20190503 AskPrint
				}else if(gl.validarCred==2){
					Cobro.super.finish();
				}

				gl.validarCred=0;

			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					singlePrint();
					//prn.printask(printcallback);

				}
			});


			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void exit() {

		try{

			showTotals();
			if(tsel>0.01) {
				msgAskExit("Tiene documentos pendientes de pago. Salir");
			} else {
				super.finish();
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// MsgDialogs

	private void msgAskOverPayd(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setPositiveButton("Si", (dialog12, which) -> msgAskSave("Aplicar pago y crear un recibo"));
			dialog.setNegativeButton("No", (dialog1, which) -> closekeyb());
			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskSave(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setCancelable(false);
			dialog.setPositiveButton("Si", (dialog1, which) -> {
				createDoc();
				check();
			});
			dialog.setNegativeButton("No", (dialog12, which) -> closekeyb());
			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskExit(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setPositiveButton("Si", (dialog1, which) -> finish());
			dialog.setNegativeButton("No", (dialog12, which) -> closekeyb());
			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Activity Events

	@Override
	protected void onResume() {

		try{

			super.onResume();

			Catalogo.Reconectar(Con, db);

			if (browse==1) {
				browse=0;
				if (gl.pagado) createDoc();
			}

			if (browse==2) {
				browse=0;
				super.finish();
			}

			if (browse==3){
				browse=0;
				super.finish();
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	@Override
	public void onBackPressed() {

		try{
			browse=2;
			onPause();
			onResume();
		}catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

}
