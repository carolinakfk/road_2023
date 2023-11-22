package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edocsdk.Fimador;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import Entidades.Detalle;
import Entidades.Receptor;
import Entidades.Referencia;
import Entidades.RespuestaEdoc;
import Entidades.gDFRefFE;
import Entidades.gDFRefNum;
import Entidades.gFormaPago;
import Entidades.gPagPlazo;
import Entidades.gRucEmDFRef;
import Entidades.gRucRec;
import Entidades.gUbiRec;
import Entidades.rFE;
import Facturacion.CatalogoFactura;

public class FacturaRes extends PBase {

	private ListView listView;
	private TextView lblPago,lblFact,lblTalon,lblMPago,lblCred,lblPend,lblCash;
	private ImageView imgBon,imgMPago,imgCred,imgPend, imgCash, imgBack;
	private CheckBox contadoCheck;
	private TextView lblVuelto;
	private EditText txtVuelto;
	private RelativeLayout rl_facturares;
	private ProgressBar pbar;
	private ProgressDialog progress;
	private final ArrayList<clsClasses.clsCDB> items= new ArrayList<>();
	private Runnable printcallback;
	private Runnable printclose;

	private printer prn;
	private printer prn_nc;
	private clsDocFactura fdoc;
	private clsDocDevolucion fdev;
	private AppMethods app;

	private long fecha,fechae;
	private int fcorel;
	private int clidia;
	private String cliid,corel,sefect,fserie,desc1,svuelt,corelNC,consprod,lotelote;

	private int notaC;
	private int impres;

	private double dmax;
	private double descpmon;
	private double descg;
	private double descgmon;
	private double descgtotal;
	private double tot;
	private double stot0;
	private double stot;
	private double descmon;
	private double totimp;
	private double totperc;
	private double dispventa;
	private boolean acum;
	private boolean saved;
	private boolean porpeso;
	private boolean pagocompleto=false;
	private boolean pagando = false;

	private clsClasses.clsSucursal Sucursal = clsCls.new clsSucursal();
	private final rFE Factura = new rFE();
	private final rFE NotaCredito = new rFE();
	private CatalogoFactura Catalogo;
	private String urltoken =  "";
	private String usuario = "";
	private String clave = "";
	private String urlDoc = "";
	private String urlDocNT = "";
	private String QR = "";
	private boolean exito = true;

	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factura_res);

		super.InitBase();
        addlog("FacturaRes",""+du.getActDateTime(),gl.vend);

		listView = findViewById(R.id.listView1);
		lblPago = findViewById(R.id.TextView01);
		lblFact = findViewById(R.id.lblFact);
		lblTalon = findViewById(R.id.lblTalon);
		lblMPago = findViewById(R.id.lblCVence);
		lblCred = findViewById(R.id.lblPend);
		lblPend = findViewById(R.id.lblCVence2);
		lblCash = findViewById(R.id.textView4);

		imgBon = findViewById(R.id.imageView6);
		imgMPago = findViewById(R.id.imageView1);
		imgCred = findViewById(R.id.imageView3);
		imgPend = findViewById(R.id.imageView12);
		imgCash = findViewById(R.id.imageView2);
		imgBack = findViewById(R.id.imageView5);

		contadoCheck = findViewById(R.id.checkContado);
		rl_facturares= findViewById(R.id.relativeLayout1);rl_facturares.setVisibility(View.VISIBLE);
		pbar = findViewById(R.id.progressBar);pbar.setVisibility(View.INVISIBLE);

		lblVuelto = new TextView(this,null);
		txtVuelto = new EditText(this,null);

		cliid=gl.cliente;
		boolean rutapos = gl.rutapos;
		int media = gl.media;
		double credito = gl.credito;
		gl.cobroPendiente = false;
		dispventa = gl.dvdispventa;dispventa=mu.round(dispventa,2);
		notaC = gl.tiponcredito;
		gl.corelFac=gl.ruta+"_"+mu.getCorelBase();

		//#CKFK20230118 Agregamos esta información quemada como variables
		urltoken = gl.url_token;
		usuario = gl.usuario_api;
		clave = gl.clave_api;
		urlDoc = gl.url_doc;
		QR = gl.qr_api;
		urlDocNT = gl.url_emision_nc_b2b_hh;

		app = new AppMethods(this, gl, Con, db);

		if (rutapos) {
			lblMPago.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblCred.setText("Pago\nTarjeta");
			//imgCred.setImageResource(R.drawable.card_credit);
		} else {
			lblMPago.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblCred.setText("Pago\nCrédito");
			//imgCred.setImageResource(R.drawable.credit);
		}

		if (media ==1) {
			contadoCheck.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		}

		if (media <= 3){
			contadoCheck.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			if (app.esClienteNuevo(gl.cliente)){
				imgPend.setVisibility(View.INVISIBLE);
				lblPend.setVisibility(View.INVISIBLE);
			}else{
				imgPend.setVisibility(View.VISIBLE);
				lblPend.setVisibility(View.VISIBLE);
			}
		}

		if (media ==4) {

			if (gl.vcredito) {

				if (credito <=0 || gl.facturaVen != 0) {
					contadoCheck.setVisibility(View.INVISIBLE);
					lblCash.setVisibility(View.VISIBLE);
					imgCash.setVisibility(View.VISIBLE);
					lblPend.setVisibility(View.INVISIBLE);
					imgPend.setVisibility(View.INVISIBLE);
					imgCred.setVisibility(View.INVISIBLE);
					lblCred.setVisibility(View.INVISIBLE);
					imgMPago.setVisibility(View.VISIBLE);
					lblMPago.setVisibility(View.VISIBLE);
				}else if(credito > 0){
					contadoCheck.setVisibility(View.VISIBLE);
					lblCash.setVisibility(View.INVISIBLE);
					imgCash.setVisibility(View.INVISIBLE);
					lblPend.setVisibility(View.INVISIBLE);
					imgPend.setVisibility(View.INVISIBLE);
					imgCred.setVisibility(View.VISIBLE);
					lblCred.setVisibility(View.VISIBLE);
					imgMPago.setVisibility(View.INVISIBLE);
					lblMPago.setVisibility(View.INVISIBLE);
				}
			} else {
				contadoCheck.setVisibility(View.VISIBLE);
				lblCash.setVisibility(View.INVISIBLE);
				imgCash.setVisibility(View.INVISIBLE);
				lblPend.setVisibility(View.INVISIBLE);
				imgPend.setVisibility(View.INVISIBLE);
				imgCred.setVisibility(View.VISIBLE);
				lblCred.setVisibility(View.VISIBLE);
				imgMPago.setVisibility(View.INVISIBLE);
				lblMPago.setVisibility(View.INVISIBLE);
			}

		}

		if (gl.dvbrowse!=0){
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		}else{
			if(credito > 0){
				lblPend.setVisibility(View.INVISIBLE);
				imgPend.setVisibility(View.INVISIBLE);
			}
		}

		fecha=du.getActDateTime();
		fechae=fecha;
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());

		clsDescGlob clsDesc = new clsDescGlob(this);

		descpmon=totalDescProd();

		dmax= clsDesc.dmax;
		acum= clsDesc.acum;

		try {
			db.execSQL("DELETE FROM T_PAGO");
		} catch (SQLException e) {
		}

		processFinalPromo();

		printcallback= () -> {

			if (notaC==2){

				String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
				fdev.buildPrint(gl.dvcorrelnc,0, vModo);

				SystemClock.sleep(3000);
				prn_nc.printnoask(printclose, "printnc.txt");
				SystemClock.sleep(3000);
				if (impres>0) prn_nc.printnoask(printclose, "printnc.txt");
			}

			askPrint();

		};

		printclose= () -> {
			//FacturaRes.super.finish();
		};

		Runnable printexit = FacturaRes.super::finish;

		prn=new printer(this, printexit,gl.validimp);
		prn_nc=new printer(this,printclose,gl.validimp);

		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(cliid),gl.codCliNuevo,gl.peModal);
		fdoc.deviceid =gl.numSerie;fdoc.medidapeso=gl.umpeso;

		fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
		fdev.deviceid =gl.numSerie;

		Catalogo = new CatalogoFactura(this, Con, db);

		saved=false;
		assignCorel();

		cliPorDia();

		setHandlers();

		txtVuelto.setInputType(InputType.TYPE_CLASS_NUMBER);

	}

	//region Events

	public void prevScreen(View view) {
		try{
			clearGlobals();
			/*if(gl.dvbrowse!=0){
				gl.dvbrowse =0;
			}*/
			super.finish();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	public void paySelect(View view) {

		try{

			if (pagando) return;

			pagando = true;

			rl_facturares.setEnabled(false);
			imgMPago.setEnabled(false);

			rl_facturares.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);

			if (tieneCanastas()) return;

			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			gl.pagoval=tot;
			gl.pagolim=tot;
			gl.pagocobro=false;
			browse=1;

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);

		}catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(), e.getMessage(), "");
			mu.msgbox("paySelect: " + e.getMessage());
		}finally {

			rl_facturares.setEnabled(true);
			imgMPago.setEnabled(true);

			rl_facturares.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);

		}
	}

	public void payCash(View view) {

		try{

			if (pagando) return;

			pagando = true;

			rl_facturares.setEnabled(false);
			imgCash.setEnabled(false);

			rl_facturares.setVisibility(View.INVISIBLE);
			imgCash.setVisibility(View.INVISIBLE);

			if (tieneCanastas()) return;

			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			//inputEfectivo();
			inputVuelto();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("payCash: " + e.getMessage());
		}finally {

			rl_facturares.setEnabled(true);
			imgCash.setEnabled(true);

			rl_facturares.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);

		}
	}

	public void payCred(View view) {

		try{

			if (pagando) return;

			pagando = true;

			rl_facturares.setEnabled(false);
			imgCred.setEnabled(false);

			rl_facturares.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);

			if (tieneCanastas()) return;

			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			inputCredito();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("payCred: " + e.getMessage());
		}finally {

			rl_facturares.setEnabled(true);
			imgCred.setEnabled(true);

			rl_facturares.setVisibility(View.VISIBLE);
			imgCred.setVisibility(View.VISIBLE);
		}

	}

	public void checkedBox(View view){
		contadoCheck.setVisibility(View.VISIBLE);
		if(contadoCheck.isChecked()){
			contadoCheck.setText("Pagar al Contado");
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		} else if(!contadoCheck.isChecked()) {
			contadoCheck.setText("Pagar al Contado");
			lblCash.setVisibility(View.INVISIBLE);
			imgCash.setVisibility(View.INVISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.VISIBLE);
			lblCred.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblMPago.setVisibility(View.INVISIBLE);
		}
	}

	private boolean tieneCanastas() {

		if (gl.ingresaCanastas) {

			long fecha = app.fechaFactTol(du.getActDateTime());

			String sql = "SELECT ifnull(count(*), 0) as cant FROM T_CANASTA " +
					"WHERE ruta='" + gl.ruta + "' " +
					"AND ANULADO=0 " +
					"AND cliente='" + gl.cliente + "' " +
					"AND CORELTRANS='"+gl.corelFac+"' "+
					"AND fecha=" + fecha;

			Cursor DT = Con.OpenDT(sql);

			if (DT != null) {
				if(DT.getCount() >= 1) {
					DT.moveToFirst();
					int cant = DT.getInt(0);
					if (cant >= 1) {
						return false;
					}else {
						toastcent("Debe registrar canastas antes de pagar.");
						Intent canastas = new Intent(this, Canastas.class);
						startActivity(canastas);
						return true;
					}
				}
			}
		}

		return false;
	}

	public void showBon(View view) {
		try{
			Intent intent = new Intent(this,BonVenta.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void setHandlers(){

		try{


			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					prevScreen(null);
				}
				public void onSwipeLeft() {
					if (imgCash.getVisibility()==View.VISIBLE) {
						payCash(null);
					}
				}
			});

			txtVuelto.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {
					//Davuelto();
				}

			});

			txtVuelto.setOnKeyListener((v, keyCode, event) -> {
				if ((keyCode == KeyEvent.KEYCODE_ENTER)) {

					DaVuelto(v);

					return true;
				}else if ((keyCode == KeyEvent.KEYCODE_DEL)){
					lblVuelto.setText("");
				}

				return false;
			});

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	public void DaVuelto(View view) {
		try{
			Davuelto();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}
	}

	public void pendientePago(View view){
		try{
			if (tieneCanastas()) return;

			askPendientePago();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}


	//endregion

	//region Main

	private void processFinalPromo(){

		descg=gl.descglob;
		descgtotal=gl.descgtotal;

		try{

			//descgmon=(double) (stot0*descg/100);
			descgmon=(double) (descg*descgtotal/100);
			totalOrder();

			if (descg>0) {
				final Handler handler = new Handler();
				handler.postDelayed(this::showPromo, 300);
			}

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("processFinalPromo: " + e.getMessage());
		}

	}

	public void showPromo(){

		try {

			browse=1;
			gl.promprod="";
			gl.promcant=0;
			gl.promdesc=descg;

			Intent intent = new Intent(this,DescBon.class);
			startActivity(intent);

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}

	}

	public void showCanastas(View view) {
		try {
			Intent iCanasta = new Intent(this, Canastas.class);
			startActivity(iCanasta);

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}
	}

	private void updDesc(){

		try{

			descg=gl.promdesc;
			descgmon= descg*descgtotal/100;
			totalOrder();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	private void totalOrder(){
		double dmaxmon;

		try{
			double dfinmon;
			if (acum) {
				dfinmon =descpmon+descgmon;
			} else {
				if (descpmon>=descgmon) {
					dfinmon =descpmon;
				} else {
					dfinmon =descgmon;
				}
			}

			dmaxmon= stot0*dmax/100;
			if (dmax>0) {
				if (dfinmon >dmaxmon) dfinmon =dmax;
			}

			descmon=mu.round2(dfinmon);
			stot=mu.round2(stot0);

			fillTotals();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("totalOrder: " + e.getMessage());
		}


	}

	private void fillTotals() {
		clsClasses.clsCDB item;

		items.clear();

		try {

			if (gl.sinimp) {

				totimp=mu.round2(totimp);
				stot=stot-totimp;

				totperc=stot*(gl.percepcion/100);
				totperc=mu.round2(totperc);

				tot=stot+totimp-descmon+totperc;
				tot=mu.round2(tot);

				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);

				item = clsCls.new clsCDB();
				item.Cod="Impuesto";item.Desc=mu.frmcur(totimp);item.Bandera=0;
				items.add(item);

				if (gl.contrib.equalsIgnoreCase("C")) {
					item = clsCls.new clsCDB();
					item.Cod="Percepción";item.Desc=mu.frmcur(totperc);item.Bandera=0;
					items.add(item);
				}

				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);

				if (gl.dvbrowse!=0){
					item = clsCls.new clsCDB();
					item.Cod="Nota Crédito";item.Desc=mu.frmcur(-dispventa);item.Bandera=0;
					items.add(item);

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot-dispventa);item.Bandera=1;
					items.add(item);

				}else{
					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
					items.add(item);
				}



			} else {

				totimp=mu.round2(totimp);
				tot=stot-descmon;
				tot=mu.round2(tot);


				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);

				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);

				if (gl.dvbrowse!=0){

					item = clsCls.new clsCDB();
					item.Cod="Nota Crédito";item.Desc=mu.frmcur(-dispventa);item.Bandera=0;
					items.add(item);

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot-dispventa);item.Bandera=1;
					items.add(item);

				}else{

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
					items.add(item);

				}

			}

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

		ListAdaptTotals adapter = new ListAdaptTotals(this, items);
		listView.setAdapter(adapter);
	}

 	private void finishOrder(){

		if (!gl.cobroPendiente) {
			ProgressDialog("Certificando factura...");
		} else {
			ProgressDialog("Procesando factura...");
		}

		if (!saved) {
			if (!saveOrder()) return;
		}

		if (gl.cobroPendiente) {
			impressOrder();
		}

	}

	private void impressOrder(){
		try{

			rl_facturares.setVisibility(View.INVISIBLE);
			pbar.setVisibility(View.VISIBLE);
			imgBack.setVisibility(View.INVISIBLE);

			if(gl.dvbrowse!=0) gl.dvbrowse =0;

			impres=0;

			if (prn.isEnabled()) {

				if (gl.peModal.equalsIgnoreCase("APR")) {
					fdoc.buildPrintExt(corel,2,"APR");
				} else if (gl.peModal.equalsIgnoreCase("TOL")) {
					if (!gl.cobroPendiente) {
						if (impres==0) {
							fdoc.buildPrint(corel, 0,gl.peFormatoFactura);
						} else {
							fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
						}
					}else{
						fdoc.buildPrint(corel,4,gl.peFormatoFactura);
					}
				}

				if (gl.peImprFactCorrecta) {
					prn.printask(printcallback);
				} else {
					singlePrint();
				}

			} else if(!prn.isEnabled()){
				if (gl.peModal.equalsIgnoreCase("APR")) {
					fdoc.buildPrintExt(corel,2,"APR");
				} else if (gl.peModal.equalsIgnoreCase("TOL")) {

					if (!gl.cobroPendiente) {
						if (impres==0) {
							fdoc.buildPrint(corel, 0,gl.peFormatoFactura);
						} else {
							fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
						}
					}else{
						fdoc.buildPrint(corel,4,gl.peFormatoFactura);
					}
				}

				if (notaC==2){
					fdev.buildPrint(gl.dvcorrelnc,0);
				}
			}

			gl.closeCliDet=true;
			gl.closeVenta=true;

			if (!prn.isEnabled()) super.finish();

		}catch (Exception e) {
			Log.d("impressOrder","err: " + e.getMessage());
			addlog(Objects.requireNonNull(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod())).getName(),e.getMessage(),"");
			mu.msgbox("impressOrder: "  + e.getMessage());

			gl.closeCliDet = true;
			gl.closeVenta = true;

			super.finish();
		}

	}

 	private void singlePrint() 	{
		try 		{
			prn.printask(printcallback);
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}
 	}

	private boolean saveOrder() {

		Cursor dt;
		String vprod,vumstock,vumventa,vbarra,vumentr;
		double vcant,vpeso,vfactor,peso,factpres,vtot,vprec;
		int mitem,bitem,ncItem=0;

		corel=gl.corelFac;

        sql="SELECT MAX(ITEM) FROM D_FACT_LOG";
        dt=Con.OpenDT(sql);

        if(dt.getCount()>0){
            dt.moveToFirst();
            mitem=dt.getInt(0);
        }else{
            mitem=0;
        }

		mitem++;

		try {

			db.beginTransaction();

			//#CKFK0220606 Agregué estas validaciones al inicio del guardar de la factura
			if (!consistenciaLotes()) {
				addlog(Objects.requireNonNull(new Object() {
				}.getClass().getEnclosingMethod()).getName(),"Inconsistencia de lotes , producto : "+consprod+" / "+corel,"");
				db.endTransaction();
				mu.msgbox("Inconsistencia de lotes , producto : "+consprod+" / "+corel);
				return false;
			}

			if (!consistenciaBarras()) {
				addlog(Objects.requireNonNull(new Object() {
				}.getClass().getEnclosingMethod()).getName(),"Inconsistencia de barras "+corel,"");
				db.endTransaction();
				mu.msgbox("Inconsistencia de barras , producto : "+consprod+" / "+corel);
				return false;
			}

			//region D_FACTURA

			sql="SELECT SUM(TOTAL),SUM(DESMON),SUM(IMP),SUM(PESO) FROM T_VENTA";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			peso=dt.getDouble(3);

			ins.init("D_FACTURA");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",fecha);
			ins.add("EMPRESA",gl.emp);
			ins.add("RUTA",gl.ruta);
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("KILOMETRAJE",0);
			ins.add("FECHAENTR",fechae);
			ins.add("FACTLINK"," ");
	   		ins.add("TOTAL",tot);
			ins.add("DESMONTO",descmon);
			ins.add("IMPMONTO",totimp+totperc);
			ins.add("PESO",peso);
			ins.add("BANDERA","N");
			ins.add("STATCOM","N");
			ins.add("CALCOBJ","N");
			ins.add("SERIE",fserie);
			ins.add("CORELATIVO",fcorel);
			ins.add("IMPRES",0);
			ins.add("CERTIFICADA_DGI", 0);

			if (gl.peModal.equalsIgnoreCase("TOL") && app.esClienteNuevo(gl.cliente)) {
				ins.add("ADD1","NUEVO");
			}else{
				ins.add("ADD1",gl.ref1);
			}
			ins.add("ADD2",gl.ref2);
			ins.add("ADD3",gl.ref3);

			ins.add("DEPOS","N");

			if (gl.iddespacho !=null ) {
				if (!gl.iddespacho.isEmpty()) {
					ins.add("PEDCOREL",gl.pedCorel);
				}else{
					ins.add("PEDCOREL","");
				}
			}else{
				ins.add("PEDCOREL","");
			}

			if (gl.iddespacho !=null ) {
				if (!gl.iddespacho.isEmpty()) {
					ins.add("DESPCOREL",gl.iddespacho);
				}else{
					ins.add("DESPCOREL","");
				}
			}else{
				ins.add("DESPCOREL","");
			}

			ins.add("REFERENCIA","");

			if (gl.dvbrowse!=0){
				ins.add("ASIGNACION",gl.dvcorreld);
			}else{
				ins.add("ASIGNACION","");
			}

			ins.add("SUPERVISOR",gl.codSupervisor);
			ins.add("AYUDANTE",gl.ayudanteID);//#HS_20181207 Agregue parametro de ayudanteID
			ins.add("VEHICULO",gl.vehiculoID);//#HS_20181207 Agregue parametro de vehiculoID
			ins.add("CODIGOLIQUIDACION",0);
			ins.add("RAZON_ANULACION","");

			if (gl.iddespacho !=null ) {
				if (!gl.iddespacho.isEmpty()) {
					ins.add("CODIGO_RUTA_PEDIDO",gl.rutaPedido);
				}else{
					ins.add("CODIGO_RUTA_PEDIDO","");
				}
			}else{
				ins.add("CODIGO_RUTA_PEDIDO","");
			}

			db.execSQL(ins.sql());

			//region Bonificacion

			clsBonifSave bonsave=new clsBonifSave(this,corel,"V");

			bonsave.ruta=gl.ruta;
			bonsave.cliente=gl.cliente;
			bonsave.fecha=fecha;
			bonsave.emp=gl.emp;

			bonsave.save();

			//endregion

			//region CANASTAS

			sql="INSERT INTO D_CANASTA (RUTA, FECHA, CLIENTE, PRODUCTO, CANTREC, CANTENTR, STATCOM, CORELTRANS, ANULADO, UNIDBAS, VENDEDOR, PESOREC, PESOENTR) " +
					"SELECT RUTA, FECHA, CLIENTE, PRODUCTO, CANTREC, CANTENTR, STATCOM, CORELTRANS, ANULADO, UNIDBAS, VENDEDOR, PESOREC, PESOENTR " +
					"FROM T_CANASTA WHERE CORELTRANS='"+gl.corelFac+"'";
			db.execSQL(sql);

			sql="SELECT PRODUCTO,CANTENTR,UNIDBAS, PESOENTR FROM T_CANASTA";
			dt=Con.OpenDT(sql);

			if (dt!=null){

				if(dt.getCount()>0){

					dt.moveToFirst();
					while (!dt.isAfterLast()) {

						vprod=dt.getString(0);
						vcant= dt.getDouble(1);
						vumentr=dt.getString(2);
						vpeso= dt.getDouble(3);
						vfactor = vpeso/(vcant==0?1:vcant);
						rebajaStockCanastas(vprod, vcant, vumentr, vfactor);
						dt.moveToNext();
					}
				}

			}

			sql="DELETE FROM T_CANASTA";
			db.execSQL(sql);

			//endregion CANASTAS

			//region Factura Electrónica
			Sucursal = Catalogo.getSucursal();

			if (Sucursal == null) {
				progress.cancel();
				msgbox("Sucursal sin datos");
				return false;
			}

			clsClasses.clsCliente cliente = Catalogo.getCliente(gl.cliente);

			Factura.gDGen.iTpEmis = "01"; // 'Fijo salvo que sea autorización en contingencia cambiar a valor 04
			Factura.gDGen.iDoc = "01"; //'Para Factura fijo.
			Factura.gDGen.dNroDF = StringUtils.right("0000000000" + (fcorel), 10);//P_COREL.Correlativo
			Factura.gDGen.dPtoFacDF = fserie; // BeSucursal.CODIGO '"002" 'Punto de Facturación del documento fiscal. (Ruta, Serie del disp.)
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
			Factura.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + cliente.codigo + ";" + Sucursal.sitio_web + ";";

			Factura.gDGen.Emisor.dNombEm = Sucursal.nombre;
			Factura.gDGen.Emisor.dTfnEm = Sucursal.telefono;
			Factura.gDGen.Emisor.dSucEm = Sucursal.codigo;
			Factura.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
			Factura.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
			Factura.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
			Factura.gDGen.Emisor.dDirecEm = Sucursal.direccion;
			Factura.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
			Factura.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
			Factura.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

			clsClasses.clsCiudad ciudad;

			if (!Sucursal.codubi.isEmpty() || Sucursal.codubi != null) {

				ciudad = Catalogo.getCiudad(Sucursal.codubi);

				if (ciudad !=null) {

					Factura.gDGen.Emisor.gUbiEm.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
					Factura.gDGen.Emisor.gUbiEm.dDistr = (ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
					Factura.gDGen.Emisor.gUbiEm.dProv = (ciudad.provincia==null?"":ciudad.provincia.toUpperCase().trim());

					if (ciudad.provincia.isEmpty()) {
						Factura.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
					}

				} else {
					toastlongd("No se encontraron los datos de la ubicación para este código: " + cliente.ciudad);
				}
			}

			Factura.gDGen.Receptor = new Receptor();
			Factura.gDGen.Receptor.gRucRec = new gRucRec();
			Factura.gDGen.Receptor.gUbiRec = new gUbiRec();

			Factura.gDGen.Receptor.gRucRec.dTipoRuc = cliente.tipoContribuyente;
			Factura.gDGen.Receptor.iTipoRec = cliente.tipoRec;
			Factura.gDGen.Receptor.dCorElectRec = cliente.email;
			Factura.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(cliente.telefono);
			Factura.gDGen.Receptor.cPaisRec = cliente.codPais;
			Factura.gDGen.Receptor.dNombRec = cliente.nombre;
			Factura.gDGen.Receptor.dDirecRec = (cliente.direccion==null?"": cliente.direccion.substring(0,(cliente.direccion.length()>=100?100: cliente.direccion.length())));
			Factura.gDGen.Receptor.gUbiRec.dCodUbi = (cliente.ciudad==null?"": cliente.ciudad);

			if (cliente.ciudad != null) {

				if (!cliente.ciudad.isEmpty() ){

					ciudad = Catalogo.getCiudad(cliente.ciudad);

					if (ciudad !=null) {

						Factura.gDGen.Receptor.gUbiRec.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
						Factura.gDGen.Receptor.gUbiRec.dDistr =(ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
						Factura.gDGen.Receptor.gUbiRec.dProv = (ciudad.provincia==null?"":ciudad.provincia.toUpperCase().trim());

						if (ciudad.provincia.isEmpty()) {
							Factura.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
						}

					} else {
						if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
						}
					}
				}else {
					if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
						toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
					}
				}
			}else {
				if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
					toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
				}
			}

			// #CKFK20221206 Si el iTipoRec 01:Contribuyente, 02:Consumidor final, 03:Gobierno, 04:Extranjero
			//endregion Factura electrónica
			clsClasses.clsRUC BeRUC= Catalogo.getRUC(cliente.nit);
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

				if(BeRUC.sRUC!=null){
					if(!BeRUC.sRUC.trim().equals("")){
						Factura.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}
				}else{
					toastlongd("El RUC asociado al cliente es nullo y el tipo de Receptor lo requiere.");
				}

				if (!BeRUC.sDV.trim().equals("")) {
					Factura.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
				} else {
					Factura.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
					Factura.gDGen.Receptor.gRucRec.dDV = "";
				}

			}

			//region Devolución de  producto.
			clsClasses.clsProducto producto;

			if (gl.dvbrowse!=0) {

				Cursor DT;
				String pcod;
				Double pcant;

				sql="SELECT MAX(ITEM) FROM D_NOTACRED_LOG ";
				DT=Con.OpenDT(sql);

				if (DT!=null){
					if(DT.getCount()>0){
						DT.moveToFirst();
						ncItem=DT.getInt(0);
					}else{
						ncItem=0;
					}

					DT.close();

					ncItem++;
				}

				ins.init("D_CxC");
				ins.add("COREL",gl.dvcorreld);
				ins.add("RUTA",gl.ruta);
				ins.add("CLIENTE",gl.cliente);
				ins.add("FECHA",fecha);
				ins.add("ANULADO","N");
				ins.add("EMPRESA",gl.emp);
				ins.add("TIPO", gl.dvestado);
				ins.add("REFERENCIA",corel);
				ins.add("IMPRES",0);
				ins.add("STATCOM","N");
				ins.add("VENDEDOR",gl.vend);
				ins.add("TOTAL",dispventa);
				ins.add("SUPERVISOR",gl.codSupervisor);
				ins.add("AYUDANTE",gl.ayudanteID);
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("ESTADO","S");

				db.execSQL(ins.sql());

				ins.init("D_NOTACRED");
				ins.add("COREL",gl.dvcorrelnc);corelNC=gl.dvcorrelnc;
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",gl.cliente);
				ins.add("TOTAL",dispventa);
				ins.add("FACTURA",corel);
				ins.add("SERIE","0");
				ins.add("CORELATIVO",gl.dvactualnc);
				ins.add("STATCOM","N");
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("RESOLNC","N");
				ins.add("SERIEFACT",0);
				ins.add("CORELFACT",0);
				ins.add("IMPRES",0);
				ins.add("CERTIFICADA_DGI", 0);
				ins.add("TIPO_DOCUMENTO", "NC");

				db.execSQL(ins.sql());

				int vNroDF;
				String vSerie;

				int tamanio = gl.dvcorrelnc.length();
				vNroDF = Integer.parseInt(gl.dvcorrelnc.substring(3,tamanio));

				vSerie = StringUtils.right("000" + gl.dvcorrelnc.substring(0,3), 3);

				NotaCredito.gDGen.iTpEmis = "01";
				NotaCredito.gDGen.iDoc = "04"; //Tipo de documento (04:Nota de Crédito  referente a facturas, 06:Nota de crédito genérica )
				NotaCredito.gDGen.dNroDF = StringUtils.right("0000000000" + (vNroDF), 10);//String.valueOf(vNroDF);
				NotaCredito.gDGen.dPtoFacDF = vSerie; //000
				NotaCredito.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";
				NotaCredito.gDGen.iNatOp = "01";
				NotaCredito.gDGen.iTipoOp = 1;
				NotaCredito.gDGen.iDest = 1;
				NotaCredito.gDGen.iFormCAFE = 1;
				NotaCredito.gDGen.iEntCAFE = 1;
				NotaCredito.gDGen.dEnvFE = 1;
				NotaCredito.gDGen.iProGen = 2;
				NotaCredito.gDGen.iTipoTranVenta = 1;
				NotaCredito.gDGen.iTipoSuc = 2;
				NotaCredito.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + cliente.codigo + ";" + Sucursal.sitio_web + ";";

				//Datos Emisor
				NotaCredito.gDGen.Emisor.dNombEm = Sucursal.nombre;
				NotaCredito.gDGen.Emisor.dTfnEm = Sucursal.telefono;
				NotaCredito.gDGen.Emisor.dSucEm = Sucursal.codigo;
				NotaCredito.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
				NotaCredito.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
				NotaCredito.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
				NotaCredito.gDGen.Emisor.dDirecEm = Sucursal.direccion;
				NotaCredito.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
				NotaCredito.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
				NotaCredito.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

				if (!Sucursal.codubi.isEmpty() || Sucursal.codubi != null) {

					ciudad = Catalogo.getCiudad(Sucursal.codubi);

					if (ciudad !=null) {

						NotaCredito.gDGen.Emisor.gUbiEm.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
						NotaCredito.gDGen.Emisor.gUbiEm.dDistr = (ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
						NotaCredito.gDGen.Emisor.gUbiEm.dProv = (ciudad.distrito==null?"":ciudad.provincia.toUpperCase().trim());

						if (ciudad.provincia.isEmpty()) {
							NotaCredito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
						}

					} else {
						msgbox("No se encontraron los datos de la ubicación para este código:" + cliente.ciudad);
						return false;
					}
				}

				NotaCredito.gDGen.Receptor = new Receptor();
				NotaCredito.gDGen.Receptor.gRucRec = new gRucRec();
				NotaCredito.gDGen.Receptor.gUbiRec = new gUbiRec();
				NotaCredito.gDGen.Receptor.gRucRec.dTipoRuc = cliente.tipoContribuyente;
				NotaCredito.gDGen.Receptor.iTipoRec = cliente.tipoRec;
				NotaCredito.gDGen.Receptor.dCorElectRec = cliente.email;
				NotaCredito.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(cliente.telefono);
				NotaCredito.gDGen.Receptor.cPaisRec = cliente.codPais;
				NotaCredito.gDGen.Receptor.dNombRec = cliente.nombre;
				NotaCredito.gDGen.Receptor.dDirecRec = (cliente.direccion==null?"": cliente.direccion.substring(0,(cliente.direccion.length()>=100?100: cliente.direccion.length())));
				NotaCredito.gDGen.Receptor.gUbiRec.dCodUbi = (cliente.ciudad==null?"": cliente.ciudad);

				if (cliente.ciudad != null) {

					if (!cliente.ciudad.isEmpty() ){

						ciudad = Catalogo.getCiudad(cliente.ciudad);

						if (ciudad !=null) {

							NotaCredito.gDGen.Receptor.gUbiRec.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
							NotaCredito.gDGen.Receptor.gUbiRec.dDistr = (ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
							NotaCredito.gDGen.Receptor.gUbiRec.dProv = (ciudad.distrito==null?"":ciudad.provincia.toUpperCase().trim());

							if (ciudad.provincia.isEmpty()) {
								NotaCredito.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
							}

						} else {
							if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
								toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
							}
						}
					}else {
						if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
						}
					}
				}else {
					if (cliente.tipoRec.equals("01")|| cliente.tipoRec.equals("03")){
						toastlongd("La ubicación del cliente está vacía Cliente:" + cliente.nombre);
					}
				}

				//#CKFK20221206 Si el dTipoRuc Tipo de Contribuyente (1:Natural, 2:Jurídico)
				// #CKFK20221206 Si el iTipoRec 01:Contribuyente, 02:Consumidor final, 03:Gobierno, 04:Extranjero
				BeRUC= Catalogo.getRUC(cliente.nit);
				if (NotaCredito.gDGen.Receptor.iTipoRec.equals("01") || NotaCredito.gDGen.Receptor.iTipoRec.equals("03")) {

					if(!BeRUC.sRUC.trim().equals("")){
						NotaCredito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
					}

				}else{

					if(!BeRUC.sRUC.trim().equals("")){
						NotaCredito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						NotaCredito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
						NotaCredito.gDGen.Receptor.gRucRec.dDV = "";
					}

				}

				sql="SELECT Item,CODIGO,CANT,CODDEV,TOTAL,PRECIO,PRECLISTA,REF,PESO,LOTE,UMVENTA,UMSTOCK,UMPESO,FACTOR,POR_PESO FROM T_CxCD WHERE CANT>0";
				DT=Con.OpenDT(sql);

				DT.moveToFirst();
				int Correlativo = 1;
				double TotalAcumulado = 0;

				while (!DT.isAfterLast()) {

					Detalle detalle = new Detalle();

					pcod=DT.getString(1);
					pcant=DT.getDouble(2);

					ins.init("D_CxCD");
					ins.add("COREL",gl.dvcorreld);
					ins.add("ITEM",DT.getInt(0));
					ins.add("CODIGO",DT.getString(1));
					ins.add("CANT",DT.getDouble(2));
					ins.add("CODDEV",DT.getString(3));
					ins.add("ESTADO",gl.dvestado);
					ins.add("TOTAL",DT.getDouble(4));
					ins.add("PRECIO",DT.getDouble(5));
					ins.add("PRECLISTA",DT.getDouble(6));
					ins.add("REF",DT.getString(7));
					ins.add("PESO",DT.getDouble(8));
					ins.add("FECHA_CAD",0);
					ins.add("LOTE",DT.getString(9));
					ins.add("UMVENTA",DT.getString(10));
					ins.add("UMSTOCK",DT.getString(11));
					ins.add("UMPESO",DT.getString(12));
					ins.add("FACTOR",DT.getDouble(13));
					db.execSQL(ins.sql());

					ins.init("D_NOTACREDD");
					ins.add("COREL",gl.dvcorrelnc);
					ins.add("PRODUCTO",DT.getString(1));
					ins.add("PRECIO_ORIG",DT.getDouble(5));
					ins.add("PRECIO_ACT",0);
					ins.add("CANT",DT.getDouble(2));
					ins.add("PESO",DT.getDouble(8));
					ins.add("POR_PESO", DT.getString(14));
					ins.add("UMVENTA",DT.getString(10));
					ins.add("UMSTOCK",DT.getString(11));
					ins.add("UMPESO",DT.getString(12));
					ins.add("FACTOR",DT.getDouble(13));
					ins.add("TIPO_DOCUMENTO", "NC");
					db.execSQL(ins.sql());

					double ntPeso = DT.getDouble(8);
					Double ntFactor = DT.getDouble(13);

					if (!gl.peModal.equalsIgnoreCase("TOL")) {

						try {
							sql="INSERT INTO P_STOCK VALUES ('"+pcod+"',0,0,0)";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(Objects.requireNonNull(new Object() {
							}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
						}

						if (gl.dvestado.equalsIgnoreCase("M")) {
							sql="UPDATE P_STOCK SET CANTM=CANTM+"+pcant+" WHERE CODIGO='"+pcod+"'";
						} else {
							sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"'";
						}
						db.execSQL(sql);

					}

					producto = Catalogo.getProducto(pcod);

					detalle.dSecItem = Correlativo;
					detalle.dDescProd = producto.nombre; //Hay que ver de donde se obtiene el nombre del producto
					detalle.dCodProd = producto.codigo;

					if (!producto.um.isEmpty()) {
						String CodDGI;

						if (prodPorPeso(producto.codigo)) {
							CodDGI = Catalogo.getUMDGI(gl.umpeso);
						} else {
							CodDGI = Catalogo.getUMDGI(producto.um);
						}

						if (!CodDGI.isEmpty()) {
							detalle.cUnidad = CodDGI;
						} else {
							detalle.cUnidad = gl.unidad_medida_defecto;
						}
					}

					if (prodPorPeso(producto.codigo)) {
						detalle.dCantCodInt = mu.formatTresDecimales(mu.round(ntPeso, 3));
					} else {
						if (app.esRosty(producto.codigo)) {
							detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(pcant * ntFactor));
						} else {
							detalle.dCantCodInt = mu.formatDosDecimales(pcant);
						}
					}

					String TotalItem = mu.formatDosDecimales(mu.round2dec(DT.getDouble(4)));

					if (producto.subBodega.length() > 1) {
						detalle.dCodCPBSabr = producto.subBodega.substring(0, 2);
						detalle.dCodCPBScmp = producto.subBodega;
					}

					detalle.gPrecios.dPrUnit = mu.formatDosDecimales(mu.round2dec(DT.getDouble(5)));
					detalle.gPrecios.dPrUnitDesc = "0.000000";
					detalle.gPrecios.dPrItem = TotalItem;
					detalle.gPrecios.dValTotItem = TotalItem;
					detalle.gITBMSItem.dTasaITBMS = "00";
					detalle.gITBMSItem.dValITBMS = "0.00";

					NotaCredito.Detalles.add(detalle);

					Correlativo++;
					TotalAcumulado += mu.round2dec((Double.parseDouble(TotalItem)));
					DT.moveToNext();

				}

				String TotalNT = mu.formatDosDecimales(TotalAcumulado);

				NotaCredito.gTot.dTotNeto = TotalNT;
				NotaCredito.gTot.dTotITBMS = "0.00";
				NotaCredito.gTot.dTotGravado = "0.00";
				NotaCredito.gTot.dTotDesc = "0.00";
				NotaCredito.gTot.dVTot = TotalNT;
				NotaCredito.gTot.dTotRec = TotalNT;
				NotaCredito.gTot.dNroItems = String.valueOf(NotaCredito.Detalles.size());
				NotaCredito.gTot.dVTotItems = TotalNT;

				gFormaPago PagosNt = new gFormaPago();
				PagosNt.iFormaPago = "02";
				NotaCredito.gTot.iPzPag = "1";
				PagosNt.dVlrCuota = TotalNT;
				NotaCredito.gTot.gFormaPago.add(PagosNt);

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactuald+" WHERE RUTA='"+gl.ruta+"' AND TIPO='D'";
				db.execSQL(sql);

				ins.init("D_NOTACRED_LOG");
				ins.add("ITEM",ncItem);
				ins.add("SERIE",fserie);
				ins.add("COREL",gl.dvactuald);
				ins.add("FECHA",du.getActDateTime());
				ins.add("RUTA",gl.ruta);
				ins.add("TIPO","D");
				db.execSQL(ins.sql());

				ncItem +=1;

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactualnc+" WHERE RUTA='"+gl.ruta+"' AND TIPO='NC'";
				db.execSQL(sql);

				ins.init("D_NOTACRED_LOG");
				ins.add("ITEM",ncItem);
				ins.add("SERIE",fserie);
				ins.add("COREL",gl.dvactualnc);
				ins.add("FECHA",du.getActDateTime());
				ins.add("RUTA",gl.ruta);
				ins.add("TIPO","NC");
				db.execSQL(ins.sql());

				ncItem +=1;

				Toast.makeText(this,"Devolución guardada", Toast.LENGTH_SHORT).show();

				sql="DELETE FROM T_CxCD";
				db.execSQL(sql);

			}

			//endregion

			//region D_FACTURAD , D_FACTURAD_LOTES

			sql="SELECT PRODUCTO,CANT,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2,UM,FACTOR,UMSTOCK FROM T_VENTA";
			dt=Con.OpenDT(sql);

			double TotalFact = 0;
			int CorrelativoFac = 1;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				Detalle detalle = new Detalle();

				porpeso=prodPorPeso(dt.getString(0));
				factpres=dt.getDouble(12);
				peso=dt.getDouble(8);

				ins.init("D_FACTURAD");
				ins.add("COREL",corel);
				ins.add("PRODUCTO",dt.getString(0));
				ins.add("EMPRESA",gl.emp);
				ins.add("ANULADO","N");
				ins.add("CANT",dt.getDouble(1));
				ins.add("PRECIO",dt.getDouble(2));
				ins.add("IMP",dt.getDouble(3));
				ins.add("DES",dt.getDouble(4));
				ins.add("DESMON",dt.getDouble(5));
				ins.add("TOTAL",mu.round2(dt.getDouble(6)));
				ins.add("PRECIODOC",dt.getDouble(7));
				ins.add("PESO",mu.round(dt.getDouble(8),3));
				ins.add("VAL1",dt.getDouble(9));
				ins.add("VAL2",dt.getDouble(9));
				ins.add("UMVENTA",dt.getString(11));
				ins.add("FACTOR",dt.getDouble(12));
				ins.add("UMSTOCK",dt.getString(13));ss=dt.getString(13);
				ins.add("UMPESO",gl.umpeso); //#HS_20181120_1625 Se agrego el valor gl.umpeso anteriormente estaba ""
			    db.execSQL(ins.sql());

			    vprod=dt.getString(0);
				vumstock=dt.getString(13);
				vcant=dt.getDouble(1);//#CKFK 20190720 Aquí debo guardar la cantidad por la unidad de medida del stock no por la unidad de medida de venta
				vpeso=dt.getDouble(8);
				vfactor=vpeso/(vcant*factpres);
				vumventa=dt.getString(11);

				if (esProductoConStock(dt.getString(0))) {
					rebajaStockUM(vprod, vumstock, vcant, vfactor, vumventa,factpres,peso);
				}

				if (!app.prodBarra(vprod)) {

					ins.init("D_FACTURAD_LOTES");
					ins.add("COREL", corel);
					ins.add("PRODUCTO", vprod);
					ins.add("LOTE", lotelote);
					ins.add("CANTIDAD", vcant);
					ins.add("PESO", vpeso);
					ins.add("UMSTOCK", vumstock);
					ins.add("UMPESO", gl.umpeso);
					ins.add("UMVENTA", vumventa);
					db.execSQL(ins.sql());

				}

				//AT20220822 Acá va el detalle de la factura
				producto = Catalogo.getProducto(vprod);

				detalle.dSecItem = CorrelativoFac;
				detalle.dDescProd = producto.nombre; //Hay que ver de donde se obtiene el nombre del producto
				detalle.dCodProd = producto.codigo;

				if (!producto.um.isEmpty()) {
					String CodDGI;

					if (porpeso) {
						CodDGI = Catalogo.getUMDGI(gl.umpeso);
					} else {
						CodDGI = Catalogo.getUMDGI(producto.um);
					}

					if (!CodDGI.isEmpty()) {
						detalle.cUnidad = CodDGI;
					} else {
						detalle.cUnidad = gl.unidad_medida_defecto; //Utiliza codigo de la cgi hy que sacarlo con una consulta
					}
				}

				//Definir que se va enviar en la cantidad.
				if (porpeso) {
					detalle.dCantCodInt = mu.formatTresDecimales(mu.round(vpeso, 3));
				} else {
					if (app.esRosty(producto.codigo)) {
						detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(vcant * factpres));
					} else {
						detalle.dCantCodInt = mu.formatDosDecimales(vcant);
					}
				}

				String TotalItem = mu.formatDosDecimales(mu.round2dec(dt.getDouble(6)));

				//Validar esto preguntar #AT20221019
				if (producto.subBodega.length() > 1) {
					detalle.dCodCPBSabr = producto.subBodega.substring(0, 2);
				}

				detalle.dCodCPBScmp = producto.subBodega;

				if (Factura.gDGen.Receptor.iTipoRec.equals("03") ) {
					if (producto.subBodega.isEmpty()) {
						toastlong("El código de familia  no puede ser vacío, para este tipo de receptor.");
					}
				}

				detalle.gPrecios.dPrUnit = String.valueOf(dt.getDouble(7));
				detalle.gPrecios.dPrUnitDesc = "0.000000";
				detalle.gPrecios.dPrItem = TotalItem;
				detalle.gPrecios.dValTotItem = TotalItem;
				detalle.gITBMSItem.dTasaITBMS = "00";
				detalle.gITBMSItem.dValITBMS = "0.00";

				Factura.Detalles.add(detalle);

				CorrelativoFac++;
				TotalFact+= mu.round2dec(dt.getDouble(6));

				dt.moveToNext();
			}

			//endregion

			//region D_FACTURAD_MODIF

			sql="SELECT COREL, ANULADO, PRODUCTO, CANTSOLICITADA, UMVENTASOLICITADA, PESOSOLICITADO, CANTENTREGADA, " +
				"UMVENTAENTREGADA, PESOENTREGADO, IDRAZON, PEDCOREL, STATCOM, DESPCOREL FROM T_FACTURAD_MODIF " +
			    "WHERE IDRAZON <> ''";
			dt=Con.OpenDT(sql);

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				ins.init("D_FACTURAD_MODIF");
				ins.add("COREL",corel);
				ins.add("ANULADO",0);
				ins.add("PRODUCTO",dt.getString(2));
				ins.add("CANTSOLICITADA",dt.getDouble(3));
				ins.add("UMVENTASOLICITADA",dt.getString(4));
				ins.add("PESOSOLICITADO",dt.getDouble(5));
				ins.add("CANTENTREGADA",dt.getDouble(6));
				ins.add("UMVENTAENTREGADA",dt.getString(7));
				ins.add("PESOENTREGADO",dt.getDouble(8));
				ins.add("IDRAZON",dt.getString(9));
				ins.add("PEDCOREL",dt.getString(10));
				ins.add("STATCOM",dt.getString(11));
				ins.add("DESPCOREL",dt.getString(12));
				db.execSQL(ins.sql());
				dt.moveToNext();
			}

			//endregion

			//region D_FACTURAP
			int CodPago;

			if(!gl.cobroPendiente) {

				sql = "SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				dt = Con.OpenDT(sql);

				CodPago = dt.getInt(1);

				dt.moveToFirst();

				while (!dt.isAfterLast()) {

					ins.init("D_FACTURAP");
					ins.add("COREL", corel);
					ins.add("ITEM", dt.getInt(0));
					ins.add("ANULADO", "N");
					ins.add("EMPRESA", gl.emp);
					ins.add("CODPAGO", dt.getInt(1));
					ins.add("TIPO", dt.getString(2));
					ins.add("VALOR", dt.getDouble(3));
					ins.add("DESC1", dt.getString(4));
					ins.add("DESC2", dt.getString(5));
					ins.add("DESC3", dt.getString(6));
					ins.add("DEPOS", "N");
					db.execSQL(ins.sql());
					dt.moveToNext();
				}

				//AT20220823 Formas de pago

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

				if (CodPago == 4) {

					Pagos.iFormaPago = "01";
					Factura.gTot.iPzPag = "2";

					Factura.gTot.gPagPlazo = new ArrayList();
					gPagPlazo PagoPlazo = new gPagPlazo();
					PagoPlazo.dSecItem = "1";

					PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(cliente.diascredito);
					PagoPlazo.dValItPlazo = Total;
					PagoPlazo.dInfPagPlazo =  StringUtils.right("000000000000000" +  String.valueOf(cliente.diascredito),15);

					Factura.gTot.gPagPlazo.add(PagoPlazo);

				} else {
					Pagos.iFormaPago = "02";
					Factura.gTot.iPzPag = "1";
				}

				Pagos.dVlrCuota = Total;
				Factura.gTot.gFormaPago.add(Pagos);

			} else {

				try {

					ins.init("P_COBRO");
					ins.add("DOCUMENTO", corel);
					ins.add("EMPRESA", gl.emp);
					ins.add("RUTA", gl.ruta);
					ins.add("CLIENTE", gl.cliente);
					ins.add("TIPODOC", "R");
					ins.add("VALORORIG", tot);
					ins.add("SALDO", tot);
					ins.add("CANCELADO", 0);
					ins.add("FECHAEMIT", fecha);
					ins.add("FECHAV", fecha);
					ins.add("CONTRASENA", corel);
					ins.add("ID_TRANSACCION", 0);
					ins.add("REFERENCIA", 0);
					ins.add("ASIGNACION", 0);
					db.execSQL(ins.sql());

					Toast.makeText(this, "Se guardó la factura pendiente de pago",Toast.LENGTH_LONG).show();

				}catch (Exception e){
					addlog(Objects.requireNonNull(new Object() {
					}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
					mu.msgbox("PendientePago: "+e.getMessage());
				}

			}

			//endregion

			//region D_FACTURAF

			ins.init("D_FACTURAF");
			ins.add("COREL",corel);
			ins.add("NOMBRE",gl.fnombre);
			ins.add("NIT",gl.fnit);
			ins.add("DIRECCION",gl.fdir);
			db.execSQL(ins.sql());

			//endregion

			//region D_STOCKB_DEV

			//#CKFK 20190720 Modifiqué la información que se guarda en D_FACTURA_BARRA y en D_STOCKB_DEV porque se estaban
			//guardando las barras sin repesaje.
			//#CKFK 20210803 Agregué el campo Reservado
			sql=" INSERT INTO D_FACTURA_BARRA "+
				" SELECT  S.RUTA, B.BARRA, S.CODIGO, S.CANT, S.COREL, S.PRECIO, B.PESO, S.DOCUMENTO, " +
				" S.FECHA, S.ANULADO, S.CENTRO, S.STATUS, S.ENVIADO, S.CODIGOLIQUIDACION, " +
				" S.COREL_D_MOV, S.UNIDADMEDIDA, S.DOC_ENTREGA, S.RESERVADO " +
				" FROM P_STOCKB S INNER JOIN T_BARRA B ON S.BARRA = B.BARRA ";
			db.execSQL(sql);

			sql="UPDATE D_FACTURA_BARRA SET Corel='"+corel+"' WHERE Corel=''";
			db.execSQL(sql);

			try {

				sql="SELECT BARRA,CODIGO,PESO FROM T_BARRA ";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0) dt.moveToFirst();
				while (!dt.isAfterLast()) {

					ins.init("D_STOCKB_DEV");
					ins.add("BARRA",dt.getString(0));
					ins.add("RUTA",gl.ruta);
					ins.add("VENDEDOR",gl.vend);
					ins.add("CODIGO",dt.getString(1));
					ins.add("COREL",corel);
					ins.add("FECHA",fecha);
					ins.add("PESO",dt.getDouble(2));
					ins.add("CODIGOLIQUIDACION",0);
					db.execSQL(ins.sql());
					dt.moveToNext();
				}
			} catch (Exception e) {
				addlog(Objects.requireNonNull(new Object() {
				}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
				msgbox(Objects.requireNonNull(new Object() {
				}.getClass().getEnclosingMethod()).getName()+" . "+e.getMessage());
			}

			sql="DELETE FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA)";
			db.execSQL(sql);

			//endregion

			//region Bonificaciones

            //region D_BONIF

			sql="SELECT ITEM,PRODID,BONIID,CANT,PRECIO,COSTO,UMVENTA,UMSTOCK,UMPESO,FACTOR FROM T_BONITEM";
			dt=Con.OpenDT(sql);

			if(dt.getCount()>0){

				dt.moveToFirst();bitem=1;
				while (!dt.isAfterLast()) {

					vcant= dt.getDouble(3);
					vprec= dt.getDouble(4);
					vtot= vcant*vprec;vtot=mu.roundr(vtot,2);

					ins.init("D_BONIF");
					ins.add("COREL",corel);
					ins.add("ITEM",bitem);
					ins.add("FECHA",fecha);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("RUTA",gl.ruta);
					ins.add("CLIENTE",gl.cliente);
					ins.add("PRODUCTO",dt.getString(2));
					ins.add("CANT",vcant);
					ins.add("VENPED","V");
					ins.add("TIPO","");
					ins.add("PRECIO",vprec);
					ins.add("COSTO",dt.getDouble(5));
					ins.add("TOTAL",vtot );
					ins.add("STATCOM","N");
					ins.add("UMVENTA",dt.getString(6));
					ins.add("UMSTOCK",dt.getString(7) );
					ins.add("UMPESO",dt.getString(8));
					ins.add("FACTOR",dt.getDouble(9));
					db.execSQL(ins.sql());

					vprod=dt.getString(2);
					vumstock=dt.getString(7);
					vumventa=dt.getString(6);
					vfactor=dt.getDouble(9);
					peso=vcant*vfactor;
					factpres=app.factorPres(vprod,vumventa,vumstock);

					rebajaStockBonif(vprod, vumstock, vcant, vfactor, vumventa,factpres,peso);

					dt.moveToNext();bitem++;
				}
			}

            //endregion

            //region T_BARRA_BONIF

            sql="INSERT INTO D_FACTURA_BARRA SELECT * FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA_BONIF)";
            db.execSQL(sql);

            sql="UPDATE D_FACTURA_BARRA SET Corel='"+corel+"' WHERE Corel=''";
            db.execSQL(sql);

			sql="SELECT BARRA,CODIGO,PESO FROM T_BARRA_BONIF";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {

				dt.moveToFirst();

				while (!dt.isAfterLast()) {

					vbarra=dt.getString(0);
					vprod=dt.getString(1);
					vpeso=dt.getDouble(2);

					vumstock=app.umStock(vprod);
					vumventa=app.umVenta(vprod);
					vfactor=app.factorPeso(vprod);

					ins.init("D_BONIF_BARRA");
					ins.add("COREL",corel);
					ins.add("BARRA",vbarra);
					ins.add("PESO",vpeso);
					ins.add("PRODUCTO",vprod);
					ins.add("UMVENTA",vumventa);
					ins.add("UMSTOCK",vumstock);
					ins.add("UMPESO",gl.umpeso);
					ins.add("FACTOR",vfactor);
					db.execSQL(ins.sql());

					dt.moveToNext();
				}
			}

			sql="DELETE FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA_BONIF)";
			db.execSQL(sql);

            //endregion

			//region T_BONIFFALT

            sql = "SELECT PRODID,PRODUCTO,CANT FROM T_BONIFFALT";
            dt = Con.OpenDT(sql);

            if (dt.getCount() > 0) dt.moveToFirst();
            while (!dt.isAfterLast()) {

                ins.init("D_BONIFFALT");
                ins.add("COREL", corel);
                ins.add("FECHA", fecha);
                ins.add("ANULADO", "N");
                ins.add("RUTA", gl.ruta);
                ins.add("CLIENTE", gl.cliente);
                ins.add("PRODUCTO", dt.getString(1));
                ins.add("CANT", dt.getDouble(2));
                db.execSQL(ins.sql());

                dt.moveToNext();
            }

            //endregion

			//endregion

			//region Despacho

			if (gl.coddespacho !=null){
				if (!gl.coddespacho.isEmpty()) {
					db.execSQL("UPDATE DS_PEDIDO SET BANDERA='S' WHERE COREL='"+gl.coddespacho+"'");
				}
			}

			//endregion

			//region Actualización del último correlativo

			sql="UPDATE P_COREL SET CORELULT="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";
			db.execSQL(sql);

			ins.init("D_FACT_LOG");
			ins.add("ITEM",mitem);
			ins.add("SERIE",fserie);
			ins.add("COREL",fcorel);
			ins.add("FECHA",du.getActDateTime());
			ins.add("RUTA",gl.ruta);
			db.execSQL(ins.sql());

			//endregion

            //region actualizacion de Nota credito
            if (gl.devtotal>0) {
                sql="UPDATE D_CxC SET REFERENCIA='"+corel+"' WHERE COREL='"+gl.devcord+"'";
                db.execSQL(sql);

                sql="UPDATE D_NOTACRED SET FACTURA='"+corel+"' WHERE COREL='"+gl.devcornc+"'";
                db.execSQL(sql);

                gl.devtotal=0;
            }

			saved=true;

			upd.init("P_CLIRUTA");
			upd.add("BANDERA",0);
			upd.Where("CLIENTE='"+cliid+"'");
			db.execSQL(upd.SQL());

			dt.close();

			db.setTransactionSuccessful();
			db.endTransaction();

			db.beginTransaction();
			//#AT20230203 Inserta en D_FACTURA_CONTROL_CONTIGENCIA de forma temporal
			if(!InsertaFacturaTmp()) {
				toast("ERROR20230313_A No se logró insertar datos temporales en D_FACTURA_CONTROL_CONTIGENCIA");
				exito = false;
			}
			db.setTransactionSuccessful();
			db.endTransaction();

	} catch (Exception e) {
			db.endTransaction();
            addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
            mu.msgbox("Error (factura) " + e.getMessage());
			return false;
        }

		if (!gl.cobroPendiente && saved) {

			try {

				Handler mtimer = new Handler();
				Runnable mrunner = () -> {

					CertificarFactura();

					if (gl.dvbrowse != 0) {
						gl.dvbrowse = 0;
						gl.tiponcredito = 0;
					}

					saveAtten(tot);
					progress.cancel();
					impressOrder();
				};
				mtimer.postDelayed(mrunner, 3000);

			} catch (Exception e) {
				if (progress != null) progress.cancel();
				Log.e("respuestageneradaBTB", e.getMessage());
			} catch (Throwable e) {
				if (progress != null) progress.cancel();
				e.printStackTrace();
			}
		} else {
			if (saved) {
				if (gl.dvbrowse != 0) {
					gl.dvbrowse = 0;
					gl.tiponcredito = 0;
				}
				saveAtten(tot);
			}
		}

		return true;
	}

	private void CertificarFactura() {

		try {

			Fimador Firmador = new Fimador(this);
			RespuestaEdoc RespuestaEdocFac = new RespuestaEdoc();

			if(!exito) {
				if (!InsertaFacturaTmp()) {
					toast("ERROR20230313_B No se logró insertar datos temporales en D_FACTURA_CONTROL_CONTIGENCIA");
				}
			}

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocFac = Firmador.EmisionDocumentoBTBTimeOut(5000,Factura, urltoken, usuario, clave, urlDoc, gl.ambiente);

					if (RespuestaEdocFac.Cufe == null) {
						for (int i = 0; i < 2; i++) {
							if (RespuestaEdocFac.Cufe == null && (RespuestaEdocFac.Estado == null || !RespuestaEdocFac.Estado.equals("15"))) {
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
					msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
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

			//#AT20230316 Nueva forma para guardar en D_CONTROL
			clsClasses.clsControlFEL ControlFEL = clsCls.new clsControlFEL();

			if (RespuestaEdocFac != null) {
				ControlFEL.Cufe = (RespuestaEdocFac.Cufe == null ? "" : RespuestaEdocFac.Cufe);
				ControlFEL.Estado = (RespuestaEdocFac.Estado == null ? "" : RespuestaEdocFac.Estado);
				ControlFEL.Mensaje = (RespuestaEdocFac.MensajeRespuesta == null ? "" : RespuestaEdocFac.MensajeRespuesta);
				ControlFEL.ValorXml = RespuestaEdocFac.XML != null ? Catalogo.ReplaceXML(RespuestaEdocFac.XML) : "";
				ControlFEL.QR = (RespuestaEdocFac.UrlCodeQR == null ? "" : RespuestaEdocFac.UrlCodeQR);
				ControlFEL.Fecha_Autorizacion = RespuestaEdocFac.FechaAutorizacion;
				ControlFEL.Numero_Autorizacion = RespuestaEdocFac.NumAutorizacion;
			}

			ControlFEL.TipoDoc = Factura.gDGen.iDoc;
			ControlFEL.NumDoc = Factura.gDGen.dNroDF;
			ControlFEL.Sucursal = gl.sucur;
			ControlFEL.Caja = fserie;
			String[] fechaEnvio = Factura.gDGen.dFechaEm.split("-05:00", 0);
			ControlFEL.FechaEnvio = fechaEnvio[0];
			ControlFEL.TipFac = Factura.gDGen.iDoc;
			ControlFEL.FechaAgr = String.valueOf(du.getFechaCompleta());
			ControlFEL.Corel = corel;
			ControlFEL.Ruta = gl.ruta;
			ControlFEL.Vendedor = gl.vend;
			ControlFEL.Correlativo = String.valueOf(fcorel);

			try{
				Catalogo.Reconectar(Con, db);
			}catch (Exception e){
				Catalogo = new CatalogoFactura(this, Con, db);
			}

			//#AT20230203 Actualiza los campos faltantes en D_FACTURA_CONTROL_CONTIGENCIA
			ActualizaFacturaTmp(corel, ControlFEL);
			Catalogo.UpdateEstadoFactura(RespuestaEdocFac.Cufe, RespuestaEdocFac.Estado, corel);

			if (RespuestaEdocFac.Estado.equals("2")) {

				toastlong("FACTURA CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocFac.Estado + " - " + RespuestaEdocFac.MensajeRespuesta);

				if (gl.dvbrowse!=0) {
					GeneraNotaCredito(ControlFEL.Cufe, ControlFEL.FechaEnvio);
				}

			} else if(!ConexionValida() && ControlFEL.Estado.equals("1")) {
				if (gl.dvbrowse!=0) {
					GeneraNotaCredito(ControlFEL.Cufe, ControlFEL.FechaEnvio);
				}
			} else if(ConexionValida() && !ControlFEL.Estado.equals("15")) {
				if (gl.dvbrowse!=0) {
					GeneraNotaCredito(ControlFEL.Cufe, ControlFEL.FechaEnvio);
				}
			} else {
				toastlong("ERR_233121237B: NO SE LOGRÓ CERTIFICAR LA FACTURA -- " + " ESTADO: " + RespuestaEdocFac.Estado + " - " + (RespuestaEdocFac.MensajeRespuesta == null ? "":RespuestaEdocFac.MensajeRespuesta));
			}

			//#AT20230313 Si no existe la factura en d control la intenta insertar de nuevo
			if (Catalogo.ExisteFacturaDControl(ControlFEL.Corel).isEmpty()) {
				Catalogo.InsertarFELControl(ControlFEL);
			}

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private boolean InsertaFacturaTmp() {

		clsClasses.clsControlFEL TmpControlFEL;

		try {

			TmpControlFEL = clsCls.new clsControlFEL();
			TmpControlFEL.TipoDoc = Factura.gDGen.iDoc;
			TmpControlFEL.NumDoc = Factura.gDGen.dNroDF;
			TmpControlFEL.Sucursal = gl.sucur;
			TmpControlFEL.Caja = fserie;
			String[] fechaEnvio = Factura.gDGen.dFechaEm.split("-05:00", 0);
			TmpControlFEL.FechaEnvio = fechaEnvio[0];
			TmpControlFEL.TipFac = Factura.gDGen.iDoc;
			TmpControlFEL.FechaAgr = String.valueOf(du.getFechaCompleta());
			TmpControlFEL.Corel = corel;
			TmpControlFEL.Ruta = gl.ruta;
			TmpControlFEL.Vendedor = gl.vend;
			TmpControlFEL.Correlativo = String.valueOf(fcorel);

			try {

				Catalogo.InsertarFELControl(TmpControlFEL);

				if (gl.dvbrowse!=0) {

					TmpControlFEL.TipoDoc = NotaCredito.gDGen.iDoc;
					TmpControlFEL.NumDoc = NotaCredito.gDGen.dNroDF;
					TmpControlFEL.TipFac = NotaCredito.gDGen.iDoc;
					TmpControlFEL.Corel = gl.devcornc;
					TmpControlFEL.Correlativo = String.valueOf(NotaCredito.gDGen.dNroDF);

					Catalogo.InsertarFELControl(TmpControlFEL);
				}

			} catch (Exception e) {
				msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
				return false;
			}
		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
			return false;
		}

		return true;
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
			msgbox(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName() + " - " + e.getMessage());
		}

		return valida;
	}

	private void GeneraNotaCredito(String CufeFact, String FechaFact) {

		try {

			clsClasses.clsControlFEL FacturaControlNC = clsCls.new clsControlFEL();
			Fimador Firmador = new Fimador(this);
			RespuestaEdoc RespuestaEdocNC = null;

			gDFRefNum gDFRefNum= new gDFRefNum();
			gDFRefNum.gDFRefFE = new gDFRefFE();
			gDFRefNum.gDFRefFE.dCUFERef = CufeFact;

			Referencia referencia= new Referencia();
			referencia.dFechaDFRef = FechaFact+"-05:00";
			referencia.dNombEmRef = Sucursal.nombre;
			referencia.gRucEmDFRef = new gRucEmDFRef();
			referencia.gRucEmDFRef.dRuc =  Sucursal.nit;
			referencia.gRucEmDFRef.dTipoRuc = Sucursal.tipoRuc;
			referencia.gRucEmDFRef.dDV = Sucursal.texto;
			referencia.gDFRefNum = gDFRefNum;

			NotaCredito.gDGen.Referencia.add(referencia);

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocNC = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaCredito, urltoken, usuario, clave, urlDocNT, gl.ambiente);

					if (RespuestaEdocNC.Cufe == null) {
						for (int i = 0; i < 2; i++) {
							if (RespuestaEdocNC.Cufe == null && !RespuestaEdocNC.Estado.equals("15")) {
								RespuestaEdocNC = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaCredito, urltoken, usuario, clave, urlDocNT, gl.ambiente);

								if (RespuestaEdocNC.Cufe != null) {
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

				NotaCredito.gDGen.iTpEmis = "02";
				NotaCredito.gDGen.dMotCont = "Autorización Previa contingencia";
				NotaCredito.gDGen.dFechaCont = NotaCredito.gDGen.dFechaEm;

				RespuestaEdocNC = Firmador.EmisionDocumentoBTC(NotaCredito,gl.url_b2c_hh,"/data/data/com.dts.roadp/"+gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
			}

			//#AT20230316 Nueva forma de guardar en D_CONTROL
			if (RespuestaEdocNC != null) {
				FacturaControlNC.Cufe = RespuestaEdocNC.Cufe;
				FacturaControlNC.Estado = RespuestaEdocNC.Estado;
				FacturaControlNC.Mensaje = RespuestaEdocNC.MensajeRespuesta;
				FacturaControlNC.ValorXml = RespuestaEdocNC.XML != null ? Catalogo.ReplaceXML(RespuestaEdocNC.XML) : "";
				FacturaControlNC.QR = RespuestaEdocNC.UrlCodeQR;
				FacturaControlNC.Fecha_Autorizacion = RespuestaEdocNC.FechaAutorizacion;
				FacturaControlNC.Numero_Autorizacion = RespuestaEdocNC.NumAutorizacion;
			}

			FacturaControlNC.TipoDoc = NotaCredito.gDGen.iDoc;
			FacturaControlNC.NumDoc = NotaCredito.gDGen.dNroDF;
			FacturaControlNC.Sucursal = gl.sucur;
			FacturaControlNC.Caja = NotaCredito.gDGen.dPtoFacDF;
			String[] FechaEnv = NotaCredito.gDGen.dFechaEm.split("-05:00", 0);
			FacturaControlNC.FechaEnvio = FechaEnv[0];
			FacturaControlNC.TipFac = NotaCredito.gDGen.iDoc;
			FacturaControlNC.FechaAgr = String.valueOf(du.getFechaCompleta());
			FacturaControlNC.Corel = gl.devcornc;
			FacturaControlNC.Ruta = gl.ruta;
			FacturaControlNC.Vendedor = gl.vend;
			FacturaControlNC.Correlativo = String.valueOf(NotaCredito.gDGen.dNroDF);

			if (RespuestaEdocNC.Estado.equals("2")) {
				toastlong("NOTA DE CREDITO CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocNC.Estado + " - " + RespuestaEdocNC.MensajeRespuesta);

			} else {
				toastlong("NO SE LOGRÓ CERTIFICAR LA NOTA DE CREDITO -- " + " ESTADO: " + RespuestaEdocNC.Estado + " - " + (RespuestaEdocNC.MensajeRespuesta == null ? "":RespuestaEdocNC.MensajeRespuesta));
			}

			try{
				Catalogo.Reconectar(Con, db);
			}catch (Exception e){
				Catalogo = new CatalogoFactura(this, Con, db);
			}

			ActualizaFacturaTmp(gl.devcornc, FacturaControlNC);
			Catalogo.UpdateEstadoNotaCredito(FacturaControlNC.Cufe, CufeFact, (RespuestaEdocNC.Estado == null ? "" : RespuestaEdocNC.Estado));

			//#AT20230313 Si no existe la NC en d control la intenta insertar de nuevo
			if (Catalogo.ExisteFacturaDControl(FacturaControlNC.Corel).isEmpty()) {
				Catalogo.InsertarFELControl(FacturaControlNC);
			}

		} catch (Exception e) {
			mu.msgbox(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName() +" - "+ e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void rebajaStockUM(String prid,String umstock,double cant,double factor, String umventa,double factpres,double ppeso) {

		Cursor dt;
		double cantapl,dispcant,umfactor,actcant,pesoapl,disppeso,actpeso,speso;
		String lote,doc,stat;

		umfactor=1;
		if (!umstock.equalsIgnoreCase(umventa)) umfactor=factpres;

		if (porpeso) {
			actcant=cant;
			actpeso=ppeso;
		} else {
			//#CKFK 20211020 Cambie factpres por umfactor
			actcant=cant*umfactor;
			actpeso=cant*factor;
		}

		try {

			sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
					"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"') ORDER BY CANT";


			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				lote="";

				cant=dt.getDouble(0);
				speso=dt.getDouble(2);
				lote=dt.getString(4);
				doc=dt.getString(5);
				stat=dt.getString(9);

				if (actcant>cant) cantapl=cant;else cantapl=actcant;
				dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
				actcant=actcant-cantapl;

				if (porpeso) {
					if (actpeso>speso) pesoapl=speso;else pesoapl=actpeso;
					actpeso=actpeso-pesoapl;
				} else {
					pesoapl=cantapl*factor;
				}
				disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

				// Stock
				sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"')";
				db.execSQL(sql);

				//KM120821 Agregué validacion para no eliminar eliminar el stock de las canastas
				sql="DELETE FROM P_STOCK WHERE (CANT<=0) AND (CANTM<=0) " +
					"AND CODIGO NOT IN(SELECT CODIGO FROM P_PRODUCTO WHERE ES_CANASTA = 1)";
				db.execSQL(sql);

				// Factura Stock
				lotelote = "";

				ins.init("D_FACTURA_STOCK");
				ins.add("COREL",corel);
				ins.add("CODIGO",prid );
				ins.add("CANT",cantapl );
				ins.add("CANTM",dt.getDouble(1));
				ins.add("PESO",pesoapl);
				ins.add("plibra",dt.getDouble(3));
				ins.add("LOTE",lote );lotelote=lote;
				ins.add("DOCUMENTO",doc);
				ins.add("FECHA",dt.getInt(6));
				ins.add("ANULADO",dt.getInt(7));
				ins.add("CENTRO",dt.getString(8));
				ins.add("STATUS",stat);
				ins.add("ENVIADO",dt.getInt(10));
				ins.add("CODIGOLIQUIDACION",dt.getInt(11));
				ins.add("COREL_D_MOV",dt.getString(12));
				ins.add("UNIDADMEDIDA",umstock);
				db.execSQL(ins.sql());

				dt.moveToNext();
			}

            db.execSQL("DELETE FROM D_FACTURAD_LOTES WHERE CANTIDAD<=0");

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("rebajaStockUM: "+e.getMessage());
		}
	}

	private void rebajaStockBonif(String prid,String umstock,double cant,double factor, String umventa,double factpres,double ppeso) {
		Cursor dt;
		double cantapl,dispcant,actcant,pesoapl,disppeso,actpeso,speso,umfactor;
		String lote,doc,stat;

		umfactor=1;
		if (!umstock.equalsIgnoreCase(umventa)) umfactor=factpres;

		if (porpeso) {
			actcant=cant;
			actpeso=ppeso;
		} else {
			//#CKFK 20211020 Cambie factpres por umfactor
			actcant=cant*umfactor;
			actpeso=cant*factor;
		}

		try {

			sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
					"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"') ORDER BY CANT";

			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				lote = "";

				cant=dt.getDouble(0);
				speso=dt.getDouble(2);
				lote=dt.getString(4);
				doc=dt.getString(5);
				stat=dt.getString(9);

				if (actcant>cant) cantapl=cant;else cantapl=actcant;
				dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
				actcant=actcant-cantapl;

				if (porpeso) {
					if (actpeso>speso) pesoapl=speso;else pesoapl=actpeso;
					actpeso=actpeso-pesoapl;
				} else {
					pesoapl=cantapl*factor;
				}
				disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

				// Stock

				//sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"')  AND (STATUS='"+stat+"') AND (UNIDADMEDIDA='"+umstock+"')";
				sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"')";
				db.execSQL(sql);

				//KM120821 Agregué validacion para no eliminar eliminar el stock de las canastas
				sql="DELETE FROM P_STOCK WHERE (CANT<=0) AND (CANTM<=0) " +
					"AND CODIGO NOT IN(SELECT CODIGO FROM P_PRODUCTO WHERE ES_CANASTA = 1)";
				db.execSQL(sql);


				// Bonif Stock

				ins.init("D_BONIF_STOCK");

				ins.add("COREL",corel);
				ins.add("CODIGO",prid );
				ins.add("CANT",cantapl );
				ins.add("CANTM",dt.getDouble(1));
				ins.add("PESO",pesoapl);
				ins.add("plibra",dt.getDouble(3));
				ins.add("LOTE",lote );

				ins.add("DOCUMENTO",doc);
				ins.add("FECHA",dt.getInt(6));
				ins.add("ANULADO",dt.getInt(7));
				ins.add("CENTRO",dt.getString(8));
				ins.add("STATUS",stat);
				ins.add("ENVIADO",dt.getInt(10));
				ins.add("CODIGOLIQUIDACION",dt.getInt(11));
				ins.add("COREL_D_MOV",dt.getString(12));
				ins.add("UNIDADMEDIDA",umstock);

				db.execSQL(ins.sql());

				// Bonif lotes

				try {
					ins.init("D_BONIF_LOTES");

					ins.add("COREL",corel);
					ins.add("PRODUCTO",prid );
					ins.add("LOTE",lote );

					if (porpeso) {
						ins.add("CANT",cantapl);
					} else {
						ins.add("CANT",cantapl/factpres);
					}

					ins.add("PESO",pesoapl);
					ins.add("UMSTOCK",umstock);
					ins.add("UMPESO",gl.umpeso);
					ins.add("UMVENTA",umventa);
					ins.add("FACTOR",factor);

					db.execSQL(ins.sql());

					//Toast.makeText(this,ins.SQL(),Toast.LENGTH_LONG).show();

				} catch (SQLException e) {
					sql="UPDATE D_BONIF_LOTES SET CANT=CANT+"+cantapl+",PESO=PESO+"+pesoapl+"  " +
						"WHERE (COREL='"+corel+"') AND (PRODUCTO='"+prid+"') AND (LOTE='"+lote+"')";
					db.execSQL(sql);
				}

				if (actcant<=0) return;

				dt.moveToNext();
			}

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("rebajaStockUM: "+e.getMessage());
		}

	}

	private void rebajaStockCanastas(String prid, double cant, String umentr, double vfactor) {
		Cursor dt;
		double cantapl,dispcant,actcant,pesoapl,disppeso,speso;
		String doc,stat;


		actcant=cant;

		try {

			sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
					"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umentr+"') ORDER BY CANT";


			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				cant=dt.getDouble(0);
				speso=dt.getDouble(2);
				doc=dt.getString(5);
				stat=dt.getString(9);

				if (actcant>cant) cantapl=cant;else cantapl=actcant;

				dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
				actcant=actcant-cantapl;

				pesoapl=cantapl*vfactor;
				disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

				// Stock
                sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"')";
				db.execSQL(sql);

				if(actcant==0) return;

				dt.moveToNext();
			}

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("rebajaStockCanastas: "+e.getMessage());
		}
	}

	private boolean consistenciaLotes() {
		Cursor dt,dtl;
		String prod="";
		double cantl,cantd;

		consprod="";

		try {
			sql="SELECT PRODUCTO,SUM(CANTIDAD) FROM D_FACTURAD_LOTES " +
				"WHERE (COREL='"+corel+"') GROUP BY PRODUCTO";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return true;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {
				prod=dt.getString(0);
				cantl=dt.getDouble(1);

				sql="SELECT SUM(CANT) FROM D_FACTURAD WHERE (COREL='"+corel+"') AND (PRODUCTO='"+prod+"') ";
				dtl=Con.OpenDT(sql);

				cantd=dtl.getDouble(0);
				if (cantd!=cantl) throw new Exception();

				dt.moveToNext();
			}

			return true;
		} catch (Exception e) {
			consprod=prod;
			return false;
		}

	}

	private boolean consistenciaBarras() {

		Cursor dt,dtl;
		String barra, barra2="", proddup="";

		consprod="";

		try {

			sql="SELECT BARRA,CODIGO,PESO FROM T_BARRA WHERE BARRA IN (SELECT BARRA FROM D_STOCKB_DEV)";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return true;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				barra=dt.getString(0);
				proddup=dt.getString(1);

				sql="SELECT BARRA FROM D_STOCKB_DEV WHERE (BARRA='" + barra + "') ";
				dtl=Con.OpenDT(sql);

				barra2 = dtl.getString(0);

				if (barra.equals(barra2)) throw new Exception("Barra duplicada " + barra);

				dt.moveToNext();
			}

			return true;
		} catch (Exception e) {
			consprod=proddup;
			return false;
		}

	}

	private void saveAtten(double tot) {
		long ti,tf,td;

		ti=gl.atentini;tf=du.getActDateTime();
		td=du.timeDiff(tf,ti);if (td<1) td=1;

		try {
			ins.init("D_ATENCION");

			ins.add("RUTA",gl.ruta);
			ins.add("FECHA",ti);
			ins.add("HORALLEG",gl.ateninistr);
			ins.add("HORASAL",du.shoraseg(tf));
			ins.add("TIEMPO",td);

			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("DIAACT",du.dayofweek(ti));
			ins.add("DIA",du.dayofweek(ti));
			ins.add("DIAFLAG","S");

			ins.add("SECUENCIA",1);
			ins.add("SECUENACT",1);
			ins.add("CODATEN","");
			ins.add("KILOMET",0);

			ins.add("VALORVENTA",tot);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",clidia);
			ins.add("CODOPER","V");
			ins.add("COREL",corel);

			if (gl.gpspass) ins.add("SCANNED","G");else ins.add("SCANNED",gl.escaneo);
			ins.add("STATCOM","N");
			ins.add("LLEGO_COMPETENCIA_ANTES",0);

			ins.add("CoorX",gl.gpspx);
			ins.add("CoorY",gl.gpspy);
			ins.add("CliCoorX",gl.gpscpx);
			ins.add("CliCoorY",gl.gpscpy);
			ins.add("Dist",gl.gpscdist);

			db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			//String s=gl.ruta+" / "+ti+" / "+gl.ateninistr;
			mu.msgbox("Error (att) : " + e.getMessage());
		}

	}

	private double totalDescProd(){
		Cursor DT;

		try {
			sql="SELECT SUM(DESMON),SUM(TOTAL),SUM(IMP) FROM T_VENTA";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                DT.moveToFirst();

                tot=DT.getDouble(1);
                stot0=tot+DT.getDouble(0);

                totimp=DT.getDouble(2);

                double rslt=DT.getDouble(0);

				DT.close();

				return rslt;

            }else {
			    return 0;
            }

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("totalDescProd: " + e.getMessage());

			return 0;
		}

	}

	private void assignCorel(){

		Cursor DT;
		int ca,cf,ca1,ca2;

		fcorel=0;fserie="";

		try {

			sql = "SELECT SERIE,CORELULT,CORELINI,CORELFIN FROM P_COREL WHERE RUTA='" + gl.ruta + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();

				fserie = DT.getString(0);
				ca1 = DT.getInt(1);
				cf = DT.getInt(3);
			} else {
				fcorel = 0;
				fserie = "";
				mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n");
				return;
			}


			sql = "SELECT MAX(COREL) FROM D_FACT_LOG WHERE RUTA='" + gl.ruta + "' AND SERIE='" + fserie + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				ca2 = DT.getInt(0);

			} else {
				ca2 = 0;
			}

			DT.close();

			ca = ca1;
			if (ca2 > ca) ca = ca2;
			fcorel = ca + 1;

			if (fcorel > cf) {
				mu.msgbox("Se ha acabado el talonario de facturas. No se puede continuar con la venta.");
				fcorel = 0;
				return;
			}

			//#HS_20181128_1602 Cambie el texto del mensaje.
			if (fcorel == cf) mu.msgbox("Esta es la última factura disponible.");

			lblFact.setText("Factura : " + fserie + " - " + fcorel);

			s = "Talonario : " + fcorel + " / " + cf + "\n";
			s = s + "Disponible : " + (cf - fcorel);
			lblTalon.setText(s);

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(), e.getMessage(), sql);
			mu.msgbox("assignCorel: " + e.getMessage());
		}

	}

	private boolean esProductoConStock(String prcodd) {
		Cursor DT;

		try {

			sql="SELECT TIPO FROM P_PRODUCTO WHERE CODIGO='"+prcodd+"'";
           	DT=Con.OpenDT(sql);

           	if (DT.getCount()>0){

                DT.moveToFirst();

                boolean rslt=DT.getString(0).equalsIgnoreCase("P");
				DT.close();

                return rslt;
            }else {
           	    return false;
            }


		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("esProductoConStock: " + e.getMessage());
			return false;
	    }
	}

	//endregion

	//region Pago

	private void inputEfectivo() {

		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Pago Efectivo");
			alert.setMessage("Monto a pagar");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(""+tot);
			input.requestFocus();

			showkeyb();

			alert.setPositiveButton("Aplicar", (dialog, whichButton) -> {
				sefect=input.getText().toString();
				closekeyb();
				applyCash();
				checkPago();
			});

			alert.setNegativeButton("Cancelar", (dialog, whichButton) -> closekeyb());
			alert.show();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	private void inputVuelto() {

		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            if (gl.dvbrowse!=0){
                double totdv;
                if (tot>=dispventa){
                    totdv = mu.round(tot-dispventa,2);
                    alert.setTitle("A pagar : "+mu.frmcur(totdv));
                }
            }else{
                alert.setTitle("A pagar : "+mu.frmcur(tot));
            }

			alert.setMessage("Pagado con billete : ");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(txtVuelto.getParent()!= null){
				txtVuelto.setText("");
				((ViewGroup)txtVuelto.getParent()).removeView(txtVuelto);
			}

			if(lblVuelto.getParent()!= null){
				lblVuelto.setText("");
				((ViewGroup)lblVuelto.getParent()).removeView(lblVuelto);
			}

			layout.addView(txtVuelto);
			layout.addView(lblVuelto);

			lblVuelto.setTextSize(20);
			lblVuelto.setTextColor(Color.rgb(54,184,238));
			lblVuelto.setGravity(Gravity.LEFT);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Pagar", (dialog, whichButton) -> {
				svuelt= txtVuelto.getText().toString();
				gl.brw=1;

				if (!svuelt.equalsIgnoreCase("")){
					if (Double.parseDouble(svuelt)<0.0){
					toast("Monto ingresado no genera vuelto");
					}
				}

					svuelt=""+tot;
					sefect=""+tot;

					Log.d("IniApplyCAsh","todobene");

					applyCash();
					Log.d("FinApplyCAsh","todobene");
					checkPago();
				    pagando = false;
					Log.d("FinCheckpago","todobene?");
				});

				alert.setNegativeButton("Cancelar", (dialog, whichButton) -> {
					lblVuelto.setText("");
					txtVuelto.setText("");
					layout.removeAllViews();
					pagando = false;
				});

				alert.show();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("InputVuelto: " + e.getMessage());
		}

	}

	public void Davuelto(){

		try{

			double pg,vuel;

			svuelt= txtVuelto.getText().toString();

			if (!svuelt.equals("")){

				pg=Double.parseDouble(svuelt);

				if (pg > 0) {

					if (gl.dvbrowse!=0){
						double totdv;
						totdv = mu.round(tot-dispventa,2);

						if (pg<totdv) {
							msgbox("Monto menor que total");
						}

						vuel=pg-tot;

						lblVuelto.setText(String.format("    Vuelto: " + mu.frmcur(vuel)) );

					}else{

						if (pg<tot) {
							msgbox("Monto menor que total");
						}

						vuel=pg-tot;

						lblVuelto.setText(String.format("    Vuelto: " + mu.frmcur(vuel)) );

					}

				}

			}

		}catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}
	}

	private void applyCash() {

		double epago;

		try {

			epago=Double.parseDouble(sefect);
			//if (epago==0) return;

			if (epago<0) throw new Exception("Err_2303131034: El valor del pago es 0 en sefect.");

			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");
			ins.add("ITEM",1);
			ins.add("CODPAGO",1);
			ins.add("TIPO","E");

			if(gl.dvbrowse!=0){
				if (epago>=dispventa) {
					epago=mu.round(epago-dispventa,2);
				}
			}

			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");
		    db.execSQL(ins.sql());

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("Err_233131019_applyCash: "+e.getMessage());
	    }

	}

	private void inputCredito() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Pago Crédito");
			alert.setMessage("Valor a pagar");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_NULL);
			input.setText(""+tot);
			input.requestFocus();

			alert.setCancelable(false);
			showkeyb();

			alert.setPositiveButton("Aplicar", (dialog, whichButton) -> {
				sefect=input.getText().toString();
				closekeyb();
				applyCredit();
				checkPago();
				pagando = false;
			});

			alert.setNegativeButton("Cancelar", (dialog, whichButton) -> closekeyb());
			alert.show();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	private void applyCredit() {

		double epago;

		try {

			epago=Double.parseDouble(sefect);
			if (epago==0) return;

			if (epago<0) throw new Exception();

			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");
			ins.add("ITEM",1);
			ins.add("CODPAGO",4);
			ins.add("TIPO","K");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");
		    db.execSQL(ins.sql());

			//msgAskSave("Aplicar pago y crear un recibo");

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			inputEfectivo();
			mu.msgbox("Err_233131020_applyCredit: "+e.getMessage());
	    }

	}

	private void inputCard() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Número de tarjeta");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setText("");input.requestFocus();

			showkeyb();

			alert.setPositiveButton("Aplicar", (dialog, whichButton) -> {
				if (checkNum(input.getText().toString())) addPagoTar();
			});

			alert.setNegativeButton("Cancelar", (dialog, whichButton) -> closekeyb());
			alert.show();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	private boolean checkNum(String s) {

		try{

			if (mu.emptystr(s)) {
				showkeyb();
				inputCard();
				mu.msgbox("Número incorrecto");showkeyb();
				return false;
			}

			desc1=s;

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
			mu.msgbox("checkNum: " + e.getMessage());
		}

		return true;

	}

	private void addPagoTar(){

		sql="DELETE FROM T_PAGO";
		db.execSQL(sql);

		try {

			ins.init("T_PAGO");
			ins.add("ITEM",1);
			ins.add("CODPAGO",3);
			ins.add("TIPO","K");
			ins.add("VALOR",tot);
			ins.add("DESC1",desc1);
			ins.add("DESC2","");
			ins.add("DESC3","");

	    	db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		checkPago();

	}

	private void checkPago() {

		Cursor DT;
		double tpago;

		if (pagocompleto) return;

		try {

			sql = "SELECT SUM(VALOR) FROM T_PAGO";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				tpago = DT.getDouble(0);
			} else {
				tpago = 0;
			}

			s = mu.frmcur(tpago);

			if (gl.dvbrowse != 0) {
				if (gl.brw > 0) {
					lblPago.setText("Pago COMPLETO.\n" + s);
					pagocompleto = true;
					finishOrder();
				}
			} else {
				if (tpago < tot) {
					lblPago.setText("Pago incompleto.\n" + s);
				} else {
					lblPago.setText("Pago COMPLETO.\n" + s);
					pagocompleto = true;
					//if (rutapos) askSavePos(); else askSave();
					finishOrder();
				}
			}

		} catch (Exception e) {
			Log.d("Un_Elol", "Factura_Vaca checkpago" + e.getMessage());
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(), e.getMessage(), sql);
			mu.msgbox(e.getMessage());
		}

	}

	//endregion

	//region Aux

	public void askSave(View view) {
		try{
			checkPago();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	//#HS_20181212 Dialogo para Pendiente de pago
	private void askPendientePago() {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Road");
			dialog.setMessage("Esta factura quedará PENDIENTE DE PAGO, deberá realizar el pago posteriormente. ¿Está seguro?");

			dialog.setPositiveButton("Si", (dialog1, which) -> {
				gl.cobroPendiente = true;
				finishOrder();
			});

			dialog.setNegativeButton("Cancelar", null);
			dialog.setCancelable(false);
			dialog.show();

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}


	}

	private void askPrint() {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", (dialog1, which) -> {

				gl.closeCliDet = true;
				gl.closeVenta = true;

				impres++;toast("Impres "+impres);

				try {
					if (!gl.cobroPendiente){
						sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
						db.execSQL(sql);
					}
				} catch (Exception e) {
					msgbox(Objects.requireNonNull(new Object() {
					}.getClass().getEnclosingMethod()).getName()+" . "+e.getMessage());
				}

				try {
					sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+corelNC+"'";
					db.execSQL(sql);
				} catch (Exception e) {
					addlog(Objects.requireNonNull(new Object() {
					}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
				}

				if (impres>1) {

					try {
						if (!gl.cobroPendiente){
							sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
							db.execSQL(sql);
						}
					} catch (Exception e) {
						msgbox(Objects.requireNonNull(new Object() {
						}.getClass().getEnclosingMethod()).getName()+" . "+e.getMessage());
					}

					try {
						sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+corelNC+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						addlog(Objects.requireNonNull(new Object() {
						}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
					}

					gl.brw=0;
					FacturaRes.super.finish();
				} else {

					if (!gl.cobroPendiente) {
						fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
					}else{
						fdoc.buildPrint(corel,4,gl.peFormatoFactura);
					}

					prn.printask(printcallback);

				}
			});

			dialog.setNegativeButton("No", (dialog12, which) -> {
				//singlePrint();
				prn.printask(printcallback);
				//finish();
			});

			dialog.show();

		} catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	private void clearGlobals() {

		try {

			db.execSQL("DELETE FROM T_PAGO");

			db.execSQL("DELETE FROM T_BONITEM WHERE PRODID='*'");

			sql="DELETE FROM T_FACTURAD_MODIF";
			db.execSQL(sql);

		} catch (SQLException e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
		}
	}

	private void checkPromo() {
		Cursor DT;

		imgBon.setVisibility(View.INVISIBLE);

		try {
			sql="SELECT ITEM FROM T_BONITEM";
           	DT=Con.OpenDT(sql);
			if (DT.getCount()>0) imgBon.setVisibility(View.VISIBLE);
			DT.close();

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
	    }
	}

	private void cliPorDia() {
		Cursor DT;

		int dweek=mu.dayofweek();

		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                clidia=DT.getCount();
            }else {
                clidia=0;
            }

			DT.close();

		} catch (Exception e) {
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
            mu.msgbox("cliPorDia: " + e.getMessage() );
		}

	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			return false;
		}
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

	//endregion

	//region Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			if (browse==1) {
				browse=0;

				Catalogo = new CatalogoFactura(this, Con, db);

				checkPromo();
				checkPago();
                pagando = false;

				if (gl.promapl) updDesc();

			}else{

				checkPromo();
				checkPago();

			}

		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}
	}

	@Override
	public void onBackPressed() {
		try{
			clearGlobals();
			super.onBackPressed();
		}catch (Exception e){
			addlog(Objects.requireNonNull(new Object() {
			}.getClass().getEnclosingMethod()).getName(),e.getMessage(),"");
		}

	}

	//endregion

}
