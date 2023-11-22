package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.inputmethodservice.ExtractEditText;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.dts.roadp.clsClasses.clsVenta;
import java.util.ArrayList;

public class Venta extends PBase {

	private ListView listView;
	private TextView lblProd,lblPres,lblCant,lblPrec,lblTot,lblTit,lblVer;
	private EditText txtBarra;
	private ImageView imgroad,imgscan;
	private CheckBox chkBorrar;
	private Button cmdBarrasDespacho;


	private ArrayList<clsVenta> items= new ArrayList<clsVenta>();
	private ListAdaptVenta adapter;
	private clsVenta selitem;
	private Precio prc, prcEsp;
	private PrecioTran prctr;

	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();

	private ArrayList<String> lcodeM = new ArrayList<String>();
	private ArrayList<String> lnameM = new ArrayList<String>();

	private ArrayList<String> lProdSinInv = new ArrayList<String>();

	private int browse;

	private double cant,desc,mdesc,prec,precsin,imp,impval,cantOriginal,pesoOriginal, umfactor,pesoprom=0,pesostock=0;
	private double descmon,tot,totsin,percep,ttimp,ttperc,ttsin,prodtot,ipeso,icant,idisp;
	private double px,py,cpx,cpy,cdist;

	private String emp,cliid,rutatipo,prodid,um,tiposcan,umstock,upres;
	private int nivel,dweek,clidia,scanc=0;
	private boolean sinimp,rutapos,softscanexist,porpeso,usarscan,contrans,pedido;

	private AppMethods app;

	// Location
	private LocationManager locationManager;
	private Location location;

	private LocationListener locationListener;

	private boolean isGPSEnabled,isNetworkEnabled;
	private double  latitude,longitude;
	private String  barcode;

	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long  MIN_TIME_BW_UPDATES = 1000; // in Milliseconds

	private Runnable scanCallBack;

	private boolean isDialogBarraShowed = false;

	private AlertDialog.Builder dialogBarra;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_venta);

		super.InitBase();
		addlog("Venta",""+du.getActDateTime(),gl.vend);

		setControls();

		emp=gl.emp;
		nivel=gl.nivel;
		cliid=gl.cliente;
		rutatipo=gl.rutatipo;
		rutapos=gl.rutapos;

		gl.atentini=du.getActDateTime();
		gl.ateninistr=du.geActTimeStr();

		app = new AppMethods(this, gl, Con, db);

		if (rutatipo.equalsIgnoreCase("V")) {
			lblTit.setText("Venta");
			imgroad.setImageResource(R.drawable.factura);
		} else if (rutatipo.equalsIgnoreCase("D")) {
			lblTit.setText("Prefactura");
			imgroad.setImageResource(R.drawable.despacho1);
		} else {
		    String tstr="Preventa";
            if (!gl.modpedid.isEmpty()) tstr+="\n# "+gl.modpedid; else tstr+="\nNUEVO ";
			lblTit.setText(tstr);
			imgroad.setImageResource(R.drawable.pedidos_3_gray);
		}

		if (rutapos) imgroad.setImageResource(R.drawable.pedidos_3_gray);

        pedido=rutatipo.equalsIgnoreCase("P");

		contrans=gl.pTransBarra;

		if (contrans) {
			prctr=new PrecioTran(this,mu,2,Con,db);
			//msgbox("Con transaccion");
		} else {
			//msgbox("Sin transaccion");
		}
		prc=new Precio(this,mu,2);

		setHandlers();

		initValues();

		browse=0;
		gl.closeVenta = false;

		if (gl.iddespacho ==null ) {
			showCredit();
		}

		setGPS();
		cliPorDia();
		validaNivelPrecio();

        gl.peditems.clear();
        if (!gl.modpedid.isEmpty()) {
            gl.pedidomod=true;
		    cargaPedido();
        } else gl.pedidomod=false;

		txtBarra.requestFocus();txtBarra.setText("");

		dialogBarra= new AlertDialog.Builder(this);

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

		if (gl.iddespacho !=null ){
			if (!gl.iddespacho.isEmpty()) {
				listaModificacion();
				procesaDespacho();
			}
		} else {
			cmdBarrasDespacho.setVisibility(View.INVISIBLE);
		}

	}

	//region Events

	public void processFinishOrder() {
		try{
			clsBonifGlob clsBonG;
			clsDeGlob clsDeG;
			String s,ss;

			if (!hasProducts()) {
				mu.msgbox("No puede continuar, no ha vendido ninguno producto !");return;
			}

			if (gl.iddespacho!= null){
				if(!gl.iddespacho.isEmpty()){
					if(tieneProductosConDiferencias()){
						if(tieneProductosPendientesRazon()){
							solicitaRazonModificacion();
							return;
						}
					}
				}
			}

			gl.gstr="";
			browse=1;

			// Descuentos

			gl.bonprodid="*";
			gl.bonus.clear();

			clsDeG=new clsDeGlob(this,tot);ss="";

			if (clsDeG.tieneDesc()) {

				gl.descglob=clsDeG.valor;
				gl.descgtotal=clsDeG.vmonto;

				for (int i = 0; i <clsDeG.items.size(); i++) {
					s=clsDeG.items.get(i).valor+" , "+clsDeG.items.get(i).lista;
					ss=ss+s+"\n";
				}
			}

			ss=ss+"acum : "+clsDeG.acum+" , limit "+clsDeG.maxlimit+"\n";
			ss=ss+"Valor : "+clsDeG.valor+"\n";
			ss=ss+"acum : "+clsDeG.valacum+"\n";
			ss=ss+"max : "+clsDeG.valmax+"\n";
			//mu.msgbox(ss);

			// Bonificacion

			gl.bonprodid="*";
			gl.bonus.clear();

			clsBonG=new clsBonifGlob(this,tot);
			if (clsBonG.tieneBonif()) {
				for (int i = 0; i <clsBonG.items.size(); i++) {
					//s=clsBonG.items.get(i).valor+"   "+clsBonG.items.get(i).tipolista+"  "+clsBonG.items.get(i).lista;
					//Toast.makeText(this,s, Toast.LENGTH_SHORT).show();
					gl.bonus.add(clsBonG.items.get(i));
				}
			}

            if (gl.devtotal>0) {
                if (tot<gl.devtotal){
                    mu.msgbox("No puede totalizar la factura, es menor al monto permitido para la nota de crédito: " + gl.devtotal);return;
                }
            }

			/*
			if (gl.dvbrowse!=0){
				if (tot<gl.dvdispventa){
					mu.msgbox("No puede totalizar la factura, es menor al monto permitido para la nota de crédito: " + gl.dvdispventa);return;
				}
			}
			*/

			gl.brw=0;

			if (rutatipo.equalsIgnoreCase("V")) {
				Intent intent = new Intent(this,FacturaRes.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,PedidoRes.class);
				startActivity(intent);
			}

			//Toast.makeText(this,"Bon global "+clsBonG.items.size(), Toast.LENGTH_SHORT).show();

			if (gl.bonus.size()>0) {
				//Intent intent = new Intent(this,BonList.class);
				//startActivity(intent);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("processFinishOrder: "+e.getMessage());
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try{
			//if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				barcode=contents;
				//toast(barcode);
			}
			//}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void doFocus(View view) {
		try {
			txtBarra.requestFocus();
		} catch (Exception e) {}
	}

	public void muestraProdBarraDespacho(View view) {
		try {
			browse=6;
			startActivity(new Intent(Venta.this,despacho_barras.class));
		} catch (Exception e) {}
	}

	public void showProd(View view) {
		try{
			gl.gstr="";
			browse=1;

			if (rutatipo.equalsIgnoreCase("P")) gl.prodtipo=0;else gl.prodtipo=1;

			Intent intent = new Intent(this,Producto.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void finishOrder(View view) {
		try{
			processFinishOrder();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("finishOrder: "+e.getMessage());
		}

	}

	public void showPromo(View view){
		try{
			gl.gstr="*";
			browse=3;

			Intent intent = new Intent(this,ListaPromo.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void doSoftScan(View view) {

		//if (softscanexist) {
			try{
				browse=5;barcode="";

				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, 0);
			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
		//} else {
			//doFocus(view);
		//}
	}

	private void setHandlers(){
		try{

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					onBackPressed();
				}
				public void onSwipeLeft() {
					finishOrder(null);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsVenta vItem = (clsVenta)lvObj;

						prodid=vItem.Cod;
						gl.um=vItem.um;
						adapter.setSelectedIndex(position);

						//#CKFK 20190517 Agregué la validación de que esta pantalla solo se levanta cuando sea venta directa
						if (prodBarra(prodid) && gl.rutatipo.equalsIgnoreCase("V")) {
							gl.gstr=prodid;
							gl.gstr2=vItem.Nombre;
							browse=4;
							startActivity(new Intent(Venta.this,RepesajeLista.class));
						} else {
							setCant();
						}
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
				};
			});

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

					try {

						Object lvObj = listView.getItemAtPosition(position);
						clsVenta vItem = (clsVenta)lvObj;

						prodid=vItem.Cod;
						adapter.setSelectedIndex(position);

						//if (prodBarra(prodid)) return true;
						//#CKFK 20190517 Agregué la validación de que esta pantalla solo se levanta cuando sea venta directa
						if (prodRepesaje(prodid) && gl.rutatipo.equalsIgnoreCase("V")) {
							gl.gstr=prodid;
							gl.gstr2=vItem.Nombre;
							showItemMenu();
						} else {
							msgAskDel("Borrar producto");
						}
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;
				}
			});
		}catch (Exception e)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		txtBarra.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

			public void onTextChanged(CharSequence s, int start, int before, int count) 			{
/*
				//scanc++;lblTit.setText("Scan : "+scanc);
				Log.d("ElHuevo","Negro");

				barcode = txtBarra.getText().toString();

				if (barcode.length()>=10) {
					txtBarra.requestFocus();
					if (!mu.emptystr(barcode)) scanCallBack.run();
				}

				Handler handlerTimer = new Handler();
				handlerTimer.postDelayed(new Runnable() {
					public void run() {
							txtBarra.requestFocus();
					}
				}, 300);
*/
//				Handler handlerTimer = new Handler();
//				handlerTimer.postDelayed(new Runnable() {
//					public void run() {
//						barcode = txtBarra.getText().toString();
//						if (barcode.length()>=10)
//						{
//							txtBarra.requestFocus();
//							if (!mu.emptystr(barcode)) scanCallBack.run();
//						}
//					}
//				}, 300);
			}
		});
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

			lblTit.setText(barcode);

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

	//endregion

	//region Main

	public void listItems() {
		Cursor DT;
		clsVenta item;
		double tt;
		int ii;

		items.clear();tot=0;ttimp=0;ttperc=0;selidx=-1;ii=0;

		try {

			if (gl.iddespacho!= null){
				if(!gl.iddespacho.isEmpty()){
					barrasPendientesDesp();
				}
			}

			sql=" SELECT T_VENTA.PRODUCTO, P_PRODUCTO.DESCCORTA, T_VENTA.TOTAL, T_VENTA.CANT, "+
				" T_VENTA.PRECIODOC, T_VENTA.DES, T_VENTA.IMP, T_VENTA.PERCEP, T_VENTA.UM, " +
				" T_VENTA.PESO, T_VENTA.UMSTOCK, T_VENTA.PRECIO, T_VENTA.FACTOR, T_VENTA.SIN_EXISTENCIA " +
				" FROM T_VENTA INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_VENTA.PRODUCTO "+
				" ORDER BY P_PRODUCTO.DESCCORTA ";
            /*
            if (gl.rutatipo.equalsIgnoreCase("P") && gl.peModal.equalsIgnoreCase("TOL")) {
                sql=" SELECT T_VENTA.PRODUCTO, P_PRODUCTO.DESCCORTA, AVG(T_VENTA.TOTAL), SUM(T_VENTA.CANT), "+
                        " T_VENTA.PRECIODOC, T_VENTA.DES, T_VENTA.IMP, T_VENTA.PERCEP, T_VENTA.UM, " +
                        " SUM(T_VENTA.PESO), T_VENTA.UMSTOCK, T_VENTA.PRECIO, T_VENTA.FACTOR  " +
                        " FROM T_VENTA INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_VENTA.PRODUCTO " +
                        " GROUP BY T_VENTA.PRODUCTO, P_PRODUCTO.DESCCORTA,T_VENTA.PRECIODOC, T_VENTA.DES, " +
                        " T_VENTA.IMP, T_VENTA.PERCEP, T_VENTA.UM, T_VENTA.UMSTOCK, T_VENTA.PRECIO, T_VENTA.FACTOR "+
                        " ORDER BY P_PRODUCTO.DESCCORTA ";
            }
            */

			DT=Con.OpenDT(sql);

            if (DT==null) return;

			if (DT.getCount()>0) {

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					tt=DT.getDouble(2);

					item = clsCls.new clsVenta();

					item.Cod=DT.getString(0);if (item.Cod.equalsIgnoreCase(prodid)) selidx=ii;
					item.Nombre=DT.getString(1);
					item.Cant=DT.getDouble(3);
					item.Prec=DT.getDouble(4);
					item.Desc=DT.getDouble(5);
					item.sdesc=mu.frmdec(item.Desc)+" %";
					item.imp=DT.getDouble(6);
					item.percep=DT.getDouble(7);
					item.ums=DT.getString(10);
					item.um=DT.getString(8);
					item.factor=DT.getDouble(12);
					if (pedido) {
					    if (DT.getInt(13)==1) item.PE="S";else item.PE="F";
                    } else item.PE="";

					//if (prodPorPeso(item.Cod)) 	item.um=DT.getString(10); else item.um=app.umVenta(item.Cod);

//                    if (rutatipo.equalsIgnoreCase("V")) {
//                        item.um=DameUnidadMinimaVenta(item.Cod);
//                    } else {
//                        item.um=DT.getString(8);//app.umVenta(item.Cod);
//                    }

					if (item.um.equalsIgnoreCase(gl.umpeso)) {
						item.um = item.ums;
					}

                    item.Peso=DT.getDouble(9);
					item.precio=DT.getDouble(11);
					porpeso=prodPorPeso(item.Cod);

					//if (app.prodBarra(item.Cod)) {
					    if (pedido && app.esRosty(item.Cod)) {
                            item.Cant = item.Cant * 1;
                            //item.Cant = item.Cant / item.factor;
                            item.um=item.ums;
                        } else {
					    	if (porpeso){
								item.Cant = item.Cant;
							}else{
								item.Cant = item.Cant * item.factor;
							}
                        }
                    //}

                    if (pedido && app.esRosty(item.Cod)) {
                        item.val=mu.frmdecimal(item.Cant,gl.peDecImp)+" "+ltrim(app.umSalida(item.Cod),6);
                    } else {
                        item.val=mu.frmdecimal(item.Cant,gl.peDecImp)+" "+ltrim(item.um,6);
                    }

					if (gl.usarpeso) {
						item.valp=mu.frmdecimal(item.Peso,gl.peDecImp)+" "+ltrim(gl.umpeso,6);
					} else {
						item.valp=".";
					}

					if (sinimp) {
						ttsin=tt-item.imp-item.percep;
						item.Total=ttsin;
					} else {
						item.Total=tt;
					}

					items.add(item);

					tot+=tt;
					ttimp+=item.imp;
					ttperc+=item.percep;

					DT.moveToNext();ii++;
				}
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }

		adapter=new ListAdaptVenta(this, items);adapter.cursym=gl.peMon;
		listView.setAdapter(adapter);

		if (sinimp) {
			ttsin=tot-ttimp-ttperc;
			ttsin=mu.round(ttsin,2);
			lblTot.setText(mu.frmcur(ttsin));
		} else {
			tot=mu.round(tot,2);
			lblTot.setText(mu.frmcur(tot));
		}

		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.smoothScrollToPosition(selidx);
		}


	}

	private void processItem(){
		try{

			String pid;

			pid=gl.gstr;
			if (mu.emptystr(pid)) {return;}

			prodid=pid;
			gl.bonprodid=prodid;
			um=gl.um;

			setCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setCant(){
		try{
			browse=2;
			gl.prod=prodid;
			gl.precprev=0;

			if (pedido) {
                startActivity(new Intent(this,ProdCantPrev.class));
            } else {
                startActivity(new Intent(this,ProdCant.class));
            }

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void processCant(){
		clsDescuento clsDesc;
		clsBonif clsBonif;
		Cursor DT = null;
		double cnt,vv;
		String s;

		cnt = gl.dval;

		if (cnt <= 0) {
		    listItems();return;
        }

		try {
			try {
				sql = "SELECT CODIGO,DESCCORTA FROM P_PRODUCTO WHERE CODIGO='" + prodid + "'";
				DT = Con.OpenDT(sql);
				DT.moveToFirst();
				lblProd.setText(DT.getString(0) +" - "+ DT.getString(1));

			} catch (Exception e) {
				mu.msgbox(e.getMessage());
				addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			} finally {
				if(DT!=null) DT.close();
			}

			cant = cnt;
			um = gl.umpres;

			lblCant.setText(mu.frmdecimal(cant, gl.peDecImp) + " " + ltrim(um, 6));
			lblPres.setText("");

			// Bonificacion

			// Borra la anterior, si existe
			sql="DELETE FROM T_BONITEM WHERE Prodid='"+prodid+"'";
			db.execSQL(sql);

			desc = 0;
			if (rutatipo.equalsIgnoreCase("V")) {
				prodPrecio();
			} else {
				prec=gl.precprev;
			}

			prec = mu.round(prec, 2);
			gl.bonprodcant = cant;
			gl.bonus.clear();

			vv = cant * prec;vv = mu.round(vv, 2);

			clsBonif = new clsBonif(this, prodid, cant, vv);
			if (clsBonif.tieneBonif()) {
				for (int i = 0; i < clsBonif.items.size(); i++) {
					gl.bonus.add(clsBonif.items.get(i));
				}
			}

			// Descuento

			clsDesc = new clsDescuento(this, prodid, cant);
			desc = clsDesc.getDesc();
			mdesc = clsDesc.monto;

			if (desc + mdesc > 0) {

				browse = 3;
				gl.promprod = prodid;
				gl.promcant = cant;

				if (desc > 0) {
					gl.prommodo = 0;
					gl.promdesc = desc;
				} else {
					gl.prommodo = 1;
					gl.promdesc = mdesc;
				}

				startActivity(new Intent(this, DescBon.class));

			} else {
				if (gl.bonus.size() > 0) {
					Intent intent = new Intent(this, BonList.class);
					startActivity(intent);
				}
			}

			//prodPrecio();
			if (prodPorPeso(prodid)) {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, gl.dpeso,um);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			} else {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, 0,um);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			}

			precsin = prc.precsin;
			imp = prc.imp;
			impval = prc.impval;
			descmon = prc.descmon;

			if (rutatipo.equalsIgnoreCase("P")) {
                double factorconv=app.factorPeso(prodid);

				prec=gl.precprev;
				prc.precdoc=prec;
				prc.tot=mu.round2(prec*cant);

				if (prodPorPeso(prodid) | app.esRosty(prodid)) {
                    //prec=mu.round2(gl.precprev*factorconv);
				    //prc.precsin=mu.round2(prec*factorconv);
                    //prc.tot=mu.round2(prec*cant*factorconv);
                }

			}

			tot = prc.tot;
			prodtot = tot;
			totsin = prc.totsin;
			percep = 0;

			//Toast.makeText(this,"Impval : "+impval+" , prec sin : "+precsin+" tot sin  "+totsin, Toast.LENGTH_LONG).show();

			if (sinimp) lblPrec.setText(mu.frmcur(precsin));
			else lblPrec.setText(mu.frmcur(prec));

			addItem();
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void updDesc(){
		try{
			desc=gl.promdesc;
			prodPrecio();
			updItem();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void prodPrecio() {
		try{
			prec=prc.precio(prodid,cant,nivel,um,gl.umpeso,gl.dpeso,um);

            if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
                if (prc.precioespecial>0) prec=prc.precioespecial;
            }

			prec=mu.round(prec,2);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}

	private void getPrecio(){
		try{
			//prodPrecio();
			prc=new Precio(this,mu,gl.peDec);
			prcEsp=new Precio(this,mu,gl.peDec);
			if (prodPorPeso(prodid)) {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, gl.dpeso,um);
				if (prcEsp.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
					prc=prcEsp;
					//if (prcEsp.precioespecial>0) prec=prcEsp.precioespecial;
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			} else {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, 0,um);
				if (prcEsp.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
					prc=prcEsp;
					//if (prcEsp.precioespecial>0) prec=prcEsp.precioespecial;
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			}

			precsin = prc.precsin;
			imp = prc.imp;
			impval = prc.impval;
			descmon = prc.descmon;

			tot = prc.tot;
			prodtot = tot;
			totsin = prc.totsin;
			percep = 0;

			//Toast.makeText(this,"Impval : "+impval+" , prec sin : "+precsin+" tot sin  "+totsin, Toast.LENGTH_LONG).show();

			if (sinimp) lblPrec.setText(mu.frmcur(precsin));
			else lblPrec.setText(mu.frmcur(prec));

		}catch (Exception ex){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
		}
	}

	private void addItem(){
		Cursor dt;
		double precdoc,fact,cantbas,peso,cantapp,precapp,vprec=0,vprecdoc=0,pesopv;
		String umb,vumstock,vumventa;

		try {
			//sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"') AND (UM='"+um+"')";
			sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);

			sql="DELETE FROM T_VENTA_DESPACHO WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="SELECT UNIDADMINIMA,FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND " +
                    "(UNIDADSUPERIOR='"+um+"') ";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			umb=dt.getString(0);
			fact=dt.getDouble(1);

			if(dt!=null) dt.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			umb=um;fact=1;
		}

		if (app.esRosty(prodid) && pedido) fact=1;

		cantbas=cant*fact;
		//peso=mu.round(cant*fact,gl.peDec);

		porpeso=prodPorPeso(prodid);
		if (porpeso) {
			peso=mu.round(gl.dpeso,gl.peDec);
		} else {
            peso=mu.round(gl.dpeso,gl.peDec);
			//peso=mu.round(gl.dpeso*gl.umfactor,gl.peDec);
		}

		if (porpeso) {
			prodtot=mu.round(gl.prectemp*peso,2);
		} else {
			prodtot=mu.round(prec*cant,2);
		}

        if (rutatipo.equalsIgnoreCase("V")) {
            gl.umstock=app.umStock(prodid);
        }else {
            gl.umstock=app.umStockPV(prodid);
        }

        //#CKFK 20211221 Agregué esta validación para que no se guarde la UMSTOCK incorrecta
		if (gl.umstock.equals("KG")){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),"Error con el UMSTOCK " + gl.umstock,"Error");
			app.umSalida(prodid);
		}

		try {

            double factorconv=DameProporcionVenta(prodid,gl.cliente,gl.nivel);
            //if (app.esRosty(prodid)) factorconv=1;

            if (sinimp) precdoc=precsin; else precdoc=prec;

			ins.init("T_VENTA");

			ins.add("PRODUCTO",prodid);
			ins.add("SIN_EXISTENCIA",0);

			//#CKFK20220606 Validamos si es con existencia o sin existencia por el tipo de producto
			/*if (gl.tolprodcrit) {
				ins.add("SIN_EXISTENCIA",1);
			} else {
				ins.add("SIN_EXISTENCIA",0);
			}*/

			ins.add("EMPRESA",emp);

            if (rutatipo.equalsIgnoreCase("V")) {
                ins.add("CANT",cant);
            } else {
                if (!app.esRosty(prodid)) {
                    if (gl.tolprodcrit) {
                        ins.add("CANT",cant-gl.cstand);
                    } else {
                        ins.add("CANT",cant);
                    }
                } else {
                    if (gl.tolprodcrit) {
                        ins.add("CANT",(cant-gl.cstand));
                    } else {
                        ins.add("CANT",cant);
                    }
                }
            }

			if (rutatipo.equalsIgnoreCase("V")) {
				ins.add("UMSTOCK",gl.umstock);vumstock=gl.umstock;
                if (porpeso) {
                    ins.add("UM",gl.umpeso);vumventa=gl.umpeso;
                } else {
                    ins.add("UM",gl.umpres);vumventa=gl.umpres;
                }
			} else {
				ins.add("UMSTOCK",gl.umstock);vumstock=gl.umstock;
                ins.add("UM",gl.umpres);vumventa=gl.umpres;
			}

			if ((rutatipo.equalsIgnoreCase("P")) && (gl.umfactor==0)) gl.umfactor=1;
			ins.add("FACTOR",factorconv);

			ins.add("IMP",impval);
			ins.add("DES",desc);
			ins.add("DESMON",descmon);

            if (rutatipo.equalsIgnoreCase("V")) {
                if (porpeso) {
                    ins.add("PRECIO",gl.prectemp);
                    ins.add("PRECIODOC",gl.prectemp);
                } else {
                    ins.add("PRECIO",prec);
                    ins.add("PRECIODOC",precdoc);
                }
                ins.add("TOTAL",prodtot);
                ins.add("PESO",peso);

            } else {
                 if (porpeso) {
                    ins.add("PRECIO",gl.prectemp);vprec=gl.prectemp;
                    ins.add("PRECIODOC",gl.prectemp);vprecdoc=gl.prectemp;
                } else {
                    ins.add("PRECIO",prec);vprec=prec;
                    ins.add("PRECIODOC",precdoc);vprecdoc=precdoc;
                }

                if (gl.tolprodcrit) {
                    cantapp=cant-gl.cstand;
                } else {
                    cantapp=cant;
                }

                if (cant!=gl.cstand) {
                    pesopv=gl.umfactor*(cant-gl.cstand);
                } else {
                    pesopv=gl.umfactor*cant;
                }

                ins.add("PESO",pesopv);

                if (porpeso) {
                    //precapp=gl.precuni*gl.umfactor*cantapp;
                    //precapp=cantapp*gl.prectemp;
                    precapp=pesopv*gl.prectemp;
                    ins.add("TOTAL",precapp);
                } else {
                    //precapp=gl.precuni*cantapp;
                    precapp=cantapp*prec;
                    if (!app.esRosty(prodid)) {
                        ins.add("TOTAL",precapp);
                    } else {
                        ins.add("TOTAL",precapp*factorconv);
                    }
                }
            }

			ins.add("VAL1",0);
			ins.add("VAL2","0");
            if (gl.tolprodcrit) ins.add("VAL3",1);else ins.add("VAL3",0);
			ins.add("VAL4","");
			ins.add("PERCEP",percep);

			if (cant>0) {
			    String ss=ins.sql();
			    db.execSQL(ins.sql());
            }

            if ((gl.cstand>0) && rutatipo.equalsIgnoreCase("P") && gl.tolprodcrit) {

                /*
                if (porpeso) {
                    prodtot=gl.cstand*gl.prectemp;
                } else {
                    prodtot=gl.cstand*prec;
                }
                */

                ins.init("T_VENTA");

                ins.add("PRODUCTO",prodid);

                if (gl.tolprodcrit) {
                    ins.add("SIN_EXISTENCIA",1);
                } else {
                    ins.add("SIN_EXISTENCIA",0);
                }

                ins.add("EMPRESA",emp);
                ins.add("CANT",gl.cstand);
                //if (porpeso) ins.add("UM",gl.umpeso);else ins.add("UM",gl.umpresp);
                //ins.add("UMSTOCK",gl.um);
                //ins.add("UM",gl.umpresp);

                ins.add("UM",vumventa);
                ins.add("UMSTOCK",vumstock);

                if ((rutatipo.equalsIgnoreCase("P")) && (gl.umfactor==0)) gl.umfactor=1;
                ins.add("FACTOR",factorconv);

                ins.add("IMP",impval);
                ins.add("DES",desc);
                ins.add("DESMON",descmon);

                ins.add("PRECIO",vprec);
                ins.add("PRECIODOC",vprecdoc);

                pesopv=gl.umfactor*(gl.cstand);
                ins.add("PESO",pesopv);

                if (porpeso) {
                    precapp=pesopv*gl.prectemp;
                    ins.add("TOTAL",precapp);
                } else {
                    precapp=gl.cstand*vprec;
                    if (!app.esRosty(prodid)) {
                        ins.add("TOTAL",precapp);
                    } else {
                        ins.add("TOTAL",precapp*factorconv);
                    }
                }

                ins.add("VAL1",0);
                ins.add("VAL2","0");
                if (gl.tolprodcrit) ins.add("VAL3",1);else ins.add("VAL3",0);
                ins.add("VAL4","");
                ins.add("PERCEP",percep);

                if (gl.cstand>0) db.execSQL(ins.sql());
            }


        } catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_VENTA WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		listItems();

	}

	private void updItem(){

		try {

			upd.init("T_VENTA");

			upd.add("PRECIO",prec);
			upd.add("IMP",imp);
			upd.add("DES",desc);
			upd.add("DESMON",descmon);
			upd.add("TOTAL",tot);
			upd.add("PRECIODOC",prec);

			upd.Where("PRODUCTO='"+prodid+"'");

	    	db.execSQL(upd.SQL());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

    	listItems();

	}

	private void delItem(){
		try {
	    	db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+prodid+"'");
	    	listItems();
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
	}

	private void cargaPedido() {
        Cursor dt;

        try {

            db.beginTransaction();

            sql="SELECT PRODUCTO,SIN_EXISTENCIA,UMVENTA,CANT,FACTOR,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2 " +
                "FROM D_PEDIDOD WHERE COREL='"+gl.modpedid+"'";
            dt=Con.OpenDT(sql);

			if (dt==null) return;

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    ins.init("T_VENTA");

                    ins.add("PRODUCTO",dt.getString(0));
                    ins.add("SIN_EXISTENCIA",dt.getInt(1));
                    ins.add("EMPRESA",emp);
                    ins.add("UM",dt.getString(2));
                    ins.add("CANT",dt.getDouble(3));
                    ins.add("UMSTOCK",dt.getString(2));
                    ins.add("FACTOR",dt.getDouble(4));
                    ins.add("PRECIO",dt.getDouble(5));
                    ins.add("IMP",dt.getDouble(6));
                    ins.add("DES",dt.getDouble(7));
                    ins.add("DESMON",dt.getDouble(8));
                    ins.add("TOTAL",dt.getDouble(9));
                    ins.add("PRECIODOC",dt.getDouble(10));
                    ins.add("PESO",dt.getDouble(11));
                    ins.add("VAL1",dt.getDouble(12));
                    ins.add("VAL2",dt.getString(13));

                    ins.add("VAL3",0);
                    ins.add("VAL4","");
                    ins.add("PERCEP",0);

                    db.execSQL(ins.sql());

                    dt.moveToNext();
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            listItems();

        } catch (Exception e) {
            db.endTransaction();
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

	//endregion

	//region disponible

	private double getDisp(String prodDesp, String umDesp) {

		Cursor dt;
		double disp = 0;
		double umf1 =1;
		double umf2 =1;

		try {

			pesostock=0;

			sql=" SELECT IFNULL(SUM(CANT),0) AS CANT,IFNULL(SUM(PESO),0) AS PESO " +
					" FROM P_STOCK WHERE (CODIGO='"+prodDesp+"') AND (UNIDADMEDIDA='"+umDesp+"')";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {

				dt.moveToFirst();

				umstock = um;
				umfactor = 1;

				disp = dt.getDouble(0);
				ipeso = dt.getDouble(1);

				if (disp>0) {
					pesostock = ipeso / disp;
				} else {
					pesostock = ipeso / 1;
				}
			} else {
				pesostock=0;
			}

			//#CKFK 20190517 Agregué para que el umfactor sea igual al peso promedio en el pedido y se calcule correctamente
			if(gl.rutatipo.equalsIgnoreCase("P")){
				umfactor = pesoprom;
			}

			if (dt!=null) dt.close();

			if (disp>0) {
				idisp = disp;
				return disp;
			}

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		try {

			sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodDesp+"')";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();
				umstock=dt.getString(0);
			}

			if (dt!=null) dt.close();

			//sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodDesp+"') AND (UNIDADSUPERIOR='"+umDesp+"') ";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				umf1=dt.getDouble(0);
			} else 	{
				umf1=1;
				//#EJC20181127: No mostrar mensaje por versión de aprofam.
				toast("No existe factor de conversión para "+umDesp);return 0;
			}

			if (dt!=null) dt.close();

			//sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodDesp+"') AND (UNIDADSUPERIOR='"+umstock+"')";
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();
				umf2=dt.getDouble(0);
			} else {
				umf2=1;
				//#EJC20181127: No mostrar mensaje por versión de aprofam.
				toast("No existe factor de conversión para "+umDesp);return 0;
			}
			if (dt!=null) dt.close();

			umfactor=umf1/umf2;

			/*
			if (umf1>=umf2) {
				umfactor=umf1/umf2;
			} else {
				umfactor=umf2/umf1;
			}
			*/

			sql="SELECT IFNULL(SUM(CANT),0) AS CANT,IFNULL(SUM(PESO),0) AS PESO FROM P_STOCK " +
					" WHERE (CODIGO='"+prodDesp+"') AND (UNIDADMEDIDA='"+umstock+"')";
			dt=Con.OpenDT(sql);

			if(dt.getCount()>0){

				dt.moveToFirst();

				disp=dt.getDouble(0);
				if (!porpeso) {
					disp=disp/umfactor;
				}
				ipeso=dt.getDouble(1);
				pesostock = ipeso/disp;
			} else {
				pesostock=0;
			}

			if (dt!=null) dt.close();

			idisp = disp;

			return disp;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return 0;
	}

	private String applyCant(double cantDesp, double pesoDesp) {
		double ppeso = 0;
		String respuesta="";
		try {

			if (cantDesp < 0) {
				respuesta = "Cantidad incorrecta";
				return respuesta;
			}

			if (rutatipo.equalsIgnoreCase("V") || rutatipo.equalsIgnoreCase("D") ) {
				if (cantDesp > idisp) {
					cantDesp = idisp;
					respuesta = "Cantidad mayor que disponible.";
					//return respuesta;
				}
			}

			if (porpeso) {

				String spp = String.valueOf(pesoDesp);

				try {
					ppeso = Double.parseDouble(spp);
					if (ppeso <= 0) throw new Exception();
				} catch (Exception e) {
					if (porpeso) {
						respuesta = "Peso incorrecto";
						return respuesta;
					}
				}
			} else {
				if(Double.isNaN(pesostock))	pesostock=1;
				if (pesoprom == 0) ppeso = pesostock * cantDesp;
				else ppeso = pesoprom * cantDesp;
			}

			if (porpeso && (gl.rutatipo.equalsIgnoreCase("V") || gl.rutatipo.equalsIgnoreCase("D"))) {
				if (!checkLimits(ppeso,cantDesp*umfactor)) {
					respuesta = "Peso incorrecto";
					return respuesta;
				}
			}

			ppeso=mu.round(ppeso,3);

			gl.dval = cantDesp;
			gl.dpeso = ppeso;
			gl.um = upres;
			gl.umpres = upres;
			gl.umstock = umstock;
			gl.umfactor = umfactor;
			gl.prectemp = prec;

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

		return respuesta;

	}

	private boolean checkLimits(double vpeso,double opeso) {

		Cursor dt;
		double pmin,pmax;
		String ss;

		try {

			sql="SELECT PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE PRODUCTO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount() == 0) {
				//toast("No está definido rango de repesaje para el producto, no se podrá modificar el peso");
				return true;
			}

			dt.moveToFirst();

			pmin = opeso - dt.getDouble(0) * opeso / 100;
			pmax = opeso + dt.getDouble(1) * opeso / 100;

			if(dt!=null) dt.close();

			if (vpeso<pmin) {
				ss="El repesaje ("+mu.frmdecimal(vpeso, gl.peDecImp)+") está por debajo de los porcentajes permitidos," +
						" minimo : "+mu.frmdecimal(pmin, gl.peDecImp)+", no se puede aplicar.";
				msgbox(ss);return false;
			}

			if (vpeso>pmax) {
				ss="El repesaje ("+mu.frmdecimal(vpeso, gl.peDecImp)+") está por encima de los percentajes permitidos," +
						" máximo : "+mu.frmdecimal(pmax, gl.peDecImp)+", no se puede aplicar.";
				msgbox(ss);return false;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}

	private boolean validaRango() {

		Cursor dt;
		double pmin,pmax;
		String ss;

		try {

			sql="SELECT PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE PRODUCTO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount() == 0) {
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}

	//endregion

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
                }

				//db.beginTransaction();
				if (barraProducto()) {
					txtBarra.setText("");
					//db.beginTransaction();
					return;
				} else {
					//db.beginTransaction();
				}

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
					txtBarra.setText("");return;
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

				if (barraProducto()) {
					txtBarra.setText("");
					db.beginTransaction();
					return;
				}else{
					db.beginTransaction();
				}

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

	private int barraBolsa() {
		Cursor dt, dt2;
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

			sql = "SELECT Barra FROM T_BARRA WHERE (BARRA='" + barcode + "') ";
			dt2 = Con.OpenDT(sql);
			if (dt2.getCount() > 0) {
				if (dt2 != null) dt2.close();
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
				barrasPendientesDesp();
				return 1;
			}

			if (dt2 != null) dt2.close();

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

			//#AT20230125 Se quito el reondeo en total, por error en el total de la factura
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

				if (chkBorrar.isChecked()) {
					borraBarra();
					//db.setTransactionSuccessful();
					//db.endTransaction();
					return 1;
				} else {
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
			ins.add("VAL2", "0");
			ins.add("VAL3", 0);
			ins.add("VAL4", "");
			ins.add("PERCEP", percep);
			ins.add("SIN_EXISTENCIA", 0);

			try {
				db.execSQL(ins.sql());
			} catch (SQLException e) {
				Log.d(e.getMessage(), "");
			}

			actualizaTotalesBarra();

			if (gl.iddespacho !=null ){
				if (!gl.iddespacho.isEmpty()) actualizaTotalesBarraDespacho();
			}

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

	private boolean despachoTieneBarras(){

		boolean resultado=false;
		try{

			Cursor DT;

			String vSQL="SELECT COUNT(*) FROM T_VENTA_DESPACHO";
			sql="SELECT * FROM T_DEPOSB";

			DT=Con.OpenDT(sql);

			if (DT!=null){
				if (DT.getCount()>0) {
					DT.moveToFirst();
					resultado=DT.getInt(0)>0;
				}
			}

			if(DT!=null) DT.close();

		}catch (Exception e){
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return resultado;
	}

    //region ProporcionVenta

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

    //endregion

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

			//#AT20230125 Se quito el reondeo en total de t_barra, por error en el total de la factura
			//if (prodPorPeso(prodid)) prodtot=mu.round2(prec*ppeso);
			if (prodPorPeso(prodid)) prodtot=prec*ppeso;
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

				if (chkBorrar.isChecked()) {
					borraBarra();
					db.setTransactionSuccessful();
					db.endTransaction();
					return 1;
				} else 	{
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
			ins.add("VAL2","0");
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

	private void actualizaTotalesBarra() {
		Cursor dt;
		int ccant;
		double ppeso,pprecio,unfactor,stfact;

		try {

			sql="SELECT Factor FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

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

			reportBonif();

			if (bc>0) msgbox("Las barra devueltas : \n"+sbarra);

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}
	}

	private void reportBonif() {
		int bont,bon,bonf;

		bon=cantBonif();
		bonf=cantFalt();
		bont=bon+bonf;

		if (bonf==0) {
			toast("Bonificación actual : "+bon);
		} else {
			toast("Bonificación actual : "+bon+" / "+bont);
		}

	}

	//endregion

	//region No atencion

	private void listAten(){
		Cursor DT;
		String code,name;

		lcode.clear();lname.clear();

		try {

			sql="SELECT Codigo,Nombre FROM P_CODATEN ORDER BY Nombre";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				try {
					code=String.valueOf(DT.getInt(0));
					name=DT.getString(1);

					lcode.add(code);
					lname.add(name);
				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());return;
	    }

		showAtenDialog();

	}

	public void showAtenDialog() {
		try{
			final AlertDialog Dialog;

			final String[] selitems = new String[lname.size()];
			for (int i = 0; i < lname.size(); i++) {
				selitems[i] = lname.get(i);
			}

			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Razón de no atencion");

			mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					try {
						String s=lcode.get(item);
						setNoAtt(s);
						doExit();
					} catch (Exception e) {
					}
				}
			});

			mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setNoAtt(String scna){
		int cna;

		try {
			cna=Integer.parseInt(scna);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return;
		}

		String cliid=gl.cliente;

		try {
            db.beginTransaction();

            // JP 20210913 Borra las CxC y Nota Credito si existe una devolucion
            if (gl.devtotal>0) {
                db.execSQL("DELETE FROM D_CxC WHERE COREL='"+gl.devcord+"'");
                db.execSQL("DELETE FROM D_CxCD WHERE COREL='"+gl.devcord+"'");

                db.execSQL("DELETE FROM D_NOTACRED WHERE COREL='"+gl.devcornc+"'");
                db.execSQL("DELETE FROM D_NOTACREDD WHERE COREL='"+gl.devcornc+"'");

                gl.devtotal=0;
            }

			upd.init("P_CLIRUTA");
			upd.add("BANDERA",cna);
			upd.Where("CLIENTE='"+cliid+"' AND DIA="+dweek);

			db.execSQL(upd.SQL());

			//KM110821 Devulve a inventario las canastas entregadas
			String csql = "SELECT SUM(CANTENTR), PRODUCTO FROM T_CANASTA WHERE CORELTRANS='"+gl.corelFac+"' GROUP BY PRODUCTO";
			Cursor can = Con.OpenDT(csql);
			try	{

				if (can != null && can.getCount()>0) {
					can.moveToFirst();
					while (!can.isAfterLast()) {
						actualizaStockCanasta(can.getString(1), can.getInt(0));
						can.moveToNext();
					}

					db.execSQL("DELETE FROM T_CANASTA");
				}
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),csql);
			}

            db.setTransactionSuccessful();
            db.endTransaction();

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		saveAtten(""+cna);
	}

	private void actualizaStockCanasta(String prod, int cantidad) {
		String sql = "SELECT CANT FROM P_STOCK WHERE CODIGO='"+prod+"'";
		Cursor st = Con.OpenDT(sql);

		if (st != null || st.getCount() >= 1){
			st.moveToFirst();

			int cant = st.getInt(0);
			cant += cantidad;

			upd.init("P_STOCK");
			upd.Where("CODIGO = '"+ prod +"'");
			upd.add("CANT", cant);

			db.execSQL(upd.SQL());
		}
	}

	private void saveAtten(String codnoate) {
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
			ins.add("CODATEN",codnoate);
			ins.add("KILOMET",0);

			ins.add("VALORVENTA",0);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",clidia);
			ins.add("CODOPER","X");
			ins.add("COREL","");

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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox("Error : " + e.getMessage());
		}

	}

	//endregion

    //region Despacho

	private void procesaDespacho() {
		String umv,ums, respuesta;
        int cantProdBarra = 0;

		try {
			lblTit.setText("Prefactura");
			lProdSinInv.clear();

			gl.coddespacho=gl.iddespacho;

			clsClasses.clsDs_pedidod item;

			clsDs_pedidodObj Ds_pedidodObj=new clsDs_pedidodObj(this,Con,db);
			Ds_pedidodObj.fill("WHERE COREL='"+gl.coddespacho+"'");

			for (int i = 0; i <Ds_pedidodObj.count; i++) {

				item=Ds_pedidodObj.items.get(i);

				db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+item.producto+"'");
				db.execSQL("DELETE FROM T_VENTA_DESPACHO WHERE PRODUCTO='"+item.producto+"'");

				umv=item.umventa;//app.umVenta(item.producto);
				ums=item.umstock;//app.umStock(item.producto);if (ums.isEmpty()) ums=umv;

				prodid =item.producto;
                cant =item.cant;
				//nivel =item.producto;
				um =item.umventa;
				//gl.umpeso =item.umpeso;
				gl.dpeso =item.peso;

				//#CKFK 20210729 Obtener el precio del producto
				getPrecio();

				item.precio = prec;
				item.imp= prc.imp;
				item.des = desc;
				item.desmon = prc.descmon;
				item.total = prodtot;

				if (!app.prodBarra(item.producto)){
					if (prec!=0){

						porpeso=prodPorPeso(item.producto);
						//#CKFK 20211004 Agregué esto (item.umventa.equals(gl.umpeso)?item.umstock:item.umventa) para cuando
						//el producto sea CW y se venda por peso
						if (getDisp(item.producto, item.umventa) > 0){

							respuesta=applyCant(item.cant,item.peso);

							if (respuesta.equals("Cantidad mayor que disponible.")){
								respuesta="";
								item.cant = gl.dval;
								cant = item.cant;
								item.peso = gl.dpeso;

								//#CKFK 20210927 Vuelvo a obtener el precio del producto para obtener el total a facturar
								getPrecio();

								if (porpeso) {
									prodtot=mu.round(prec*item.peso,2);
								} else {
									prodtot=mu.round(prec*cant,2);
								}

								item.precio = prec;
								item.imp= prc.imp;
								item.des = desc;
								item.desmon = prc.descmon;
								item.total = prodtot;
							}

							if (respuesta.equals("")){

								if (porpeso) {
									item.total=mu.round(item.precio*item.peso,2);
								}

								ins.init("T_VENTA");
								ins.add("PRODUCTO",item.producto);
								ins.add("EMPRESA",emp);
								ins.add("UM",umv);
								ins.add("CANT",item.cant);
								ins.add("UMSTOCK",ums);
								ins.add("FACTOR",app.factorPres(item.producto,umv,ums));
								ins.add("PRECIO",item.precio);
								ins.add("IMP",item.imp);
								ins.add("DES",item.des);
								ins.add("DESMON",item.desmon);
								ins.add("TOTAL",item.total);
								ins.add("PRECIODOC",item.precio);
								ins.add("PESO",item.peso);
								ins.add("VAL1",i+1);
								ins.add("VAL2",i+1);
								ins.add("VAL3",0);
								ins.add("VAL4","");
								ins.add("PERCEP",percep);
								ins.add("SIN_EXISTENCIA",0);
								ins.add("CANTORIGINAL",item.cantOriginal);
								ins.add("PESOORIGINAL",item.pesoOriginal);

								db.execSQL(ins.sql());

							}
						}else{
							//CKFK 20211026 Agregar productos a lista para mostrarlos
							lProdSinInv.add(item.producto + " " + app.getNombreProducto(item.producto ));
						}

					}else{
						toast("Precio del producto es 0, no se puede agregar a la venta");
					}

				}else{
					ins.init("T_VENTA_DESPACHO");
					ins.add("PRODUCTO",item.producto);
					ins.add("EMPRESA",emp);
					ins.add("UM",umv);
					ins.add("CANTSOL",item.cant);
					ins.add("CANTREC",0);
					ins.add("CANTDIF", item.cant);
					ins.add("UMSTOCK",ums);
					ins.add("FACTOR",app.factorPres(item.producto,umv,ums));
					ins.add("PRECIO",item.precio);
					ins.add("IMP",item.imp);
					ins.add("DES",item.des);
					ins.add("DESMON",item.desmon);
					ins.add("TOTAL",item.total);
					ins.add("PRECIODOC",item.precio);
					ins.add("PESO",item.peso);
					ins.add("VAL1",i+1);
					ins.add("VAL2",i+1);
					ins.add("VAL3",0);
					ins.add("VAL4","");
					ins.add("CANTORIGINAL",item.cantOriginal);
					ins.add("PESOORIGINAL",item.pesoOriginal);

					db.execSQL(ins.sql());

					cantProdBarra +=1;
				}
			}

			if (cantProdBarra>0){
				cmdBarrasDespacho.setVisibility(View.VISIBLE);
				browse=6;

				Intent intent = new Intent(this,despacho_barras.class);
				startActivity(intent);
			}else{
				listItems();
			}

			if (lProdSinInv.size()>0){
				showProdSinInvDialog();
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	private void barrasPendientesDesp(){
		Cursor DT;
		int cantidad = 0;

		try{
			sql="SELECT DISTINCT P.CODIGO, P.DESCCORTA, R.UNIDADMEDIDA, D.CANTDIF " +
					"FROM T_VENTA_DESPACHO D INNER JOIN P_PRODUCTO P ON D.PRODUCTO=P.CODIGO INNER JOIN " +
					"P_PRODPRECIO R ON (D.PRODUCTO=R.CODIGO) " +
					"WHERE (D.CANTDIF > 0)  AND (R.NIVEL = " + gl.nivel + ") AND (P.ES_VENDIBLE=1)";

			DT=Con.OpenDT(sql);

			cantidad = DT.getCount();

			if (cantidad==0){
				cmdBarrasDespacho.setVisibility(View.INVISIBLE);
			}else{
				cmdBarrasDespacho.setVisibility(View.VISIBLE);
			}

		} catch (Exception e){

		}
	}

	//endregion

	//region Location

	private void setGPS() {

		try{
			locationListener = new LocationListener() {

				@Override
				public void onLocationChanged(Location arg0) {
				}

				@Override
				public void onProviderDisabled(String arg0) {}

				@Override
				public void onProviderEnabled(String arg0)  {}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

			};

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setGPSPos();
				}
			}, 500);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setGPSPos() {

		Cursor DT = null;
		int idist;

		latitude=0;longitude=0;cpx=0;cpy=0;cdist=-1.0;

		try {

			sql="SELECT COORX,COORY FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
			opendb();

           	DT=Con.OpenDT(sql);
           	if (DT.getCount()>0)            	{
				DT.moveToFirst();

				cpx=DT.getDouble(0);
				cpy=DT.getDouble(1);
			}

			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }

		try {
			getLocation();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			latitude=0;longitude=0;
		}

		px=latitude;py=longitude;

		if ((px+py!=0) && (cpx+cpy!=0)) {
			float[] results = new float[1];
			Location.distanceBetween(cpx,cpy,latitude,longitude, results);
			cdist=results[0];
		}

		idist=(int) cdist;cdist=idist;

		gl.gpspx=px;
		gl.gpspy=py;
		gl.gpscpx=cpx;
		gl.gpscpy=cpy;
		gl.gpscdist=cdist;

	}

	@SuppressLint("MissingPermission")
	public Location getLocation() {

		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGPSEnabled) {
			} else {
				Toast.makeText(this,"GPS Deshabilitado !", Toast.LENGTH_SHORT).show();
			}

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {

			} else {

				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return null;
		}

		return location;
	}

	//endregion

	//region Messages

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(gl.dvbrowse!=0){
						gl.dvbrowse =0;
					}
					if (rutapos) doExit();else listAten();
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

	private void msgAskDel(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					delItem();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { }
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskBarra(String msg) 	{
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					borraBarra();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { }
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
    }

	//endregion

	//region Aux

	private void showItemMenu() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Repesaje","Borrar"};

			AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
			menudlg.setTitle("Producto venta");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
						case 0:
							browse=4;
							startActivity(new Intent(Venta.this,RepesajeLista.class));break;
						case 1:
							msgAskDel("Borrar producto");break;
					}

					dialog.cancel();
				}
			});

			menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			Dialog = menudlg.create();
			Dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setControls() {

		try{
			
			listView = (ListView) findViewById(R.id.listView1);
			lblProd= (TextView) findViewById(R.id.lblProd);
			lblPres= (TextView) findViewById(R.id.lblPres);
			lblCant= (TextView) findViewById(R.id.lblCant);
			lblPrec= (TextView) findViewById(R.id.lblPNum);
			lblTot= (TextView) findViewById(R.id.lblTot);
			lblTit= (TextView) findViewById(R.id.txtRoadTit);
            lblVer= (TextView) findViewById(R.id.textView96);
            lblVer.setText(gl.parNumVer.replace("/",""));

			imgroad= (ImageView) findViewById(R.id.imgRoadTit);
			imgscan= (ImageView) findViewById(R.id.imageView13);imgscan.setVisibility(View.INVISIBLE);

			chkBorrar= (CheckBox) findViewById(R.id.checkBox1);chkBorrar.setVisibility(View.INVISIBLE);

			txtBarra=(EditText) findViewById(R.id.editText6);

			cmdBarrasDespacho=(Button) findViewById(R.id.cmdBarrasDespacho);



		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void initValues(){
		Cursor DT;
		String contrib;

		tiposcan="*";

		try {
			sql="SELECT TIPO_HH FROM P_ARCHIVOCONF WHERE RUTA='"+gl.ruta+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				tiposcan=DT.getString(0);
			}

			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tiposcan="*";msgbox(e.getMessage());
		}

		usarscan=false;softscanexist=false;
		if (!mu.emptystr(tiposcan)) {
			if (tiposcan.equalsIgnoreCase("SOFTWARE")) {
				softscanexist=detectBarcodeScanner();
				usarscan=true;
			}
			if (!tiposcan.equalsIgnoreCase("SIN ESCANER")) usarscan=true;
		}

		if (usarscan) {
			imgscan.setVisibility(View.VISIBLE);
			chkBorrar.setVisibility(View.VISIBLE);
		} else {
			imgscan.setVisibility(View.INVISIBLE);
		}

		try {
			sql="SELECT INITPATH FROM P_EMPRESA WHERE EMPRESA='"+emp+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			String sim=DT.getString(0);
			sinimp=sim.equalsIgnoreCase("S");

			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sinimp=false;
		}

		contrib=gl.contrib;
		if (contrib.equalsIgnoreCase("C")) sinimp=true;
		if (contrib.equalsIgnoreCase("F")) sinimp=false;

		gl.sinimp=sinimp;

		lblProd.setText("");
		lblPres.setText("");

		try {
			sql="DELETE FROM T_VENTA";
			db.execSQL(sql);

			sql="DELETE FROM T_VENTA_DESPACHO";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA_BONIF";
			db.execSQL(sql);

			sql="DELETE FROM T_BONIFFALT";
			db.execSQL(sql);

			sql="DELETE FROM T_CANASTA";
			db.execSQL(sql);

			sql="DELETE FROM T_FACTURAD_MODIF";
			db.execSQL(sql);

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_BONIFFALT";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_BONITEM";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		gl.ref1="";
		gl.ref2="";
		gl.ref3="";

		clsDescFiltro clsDFilt=new clsDescFiltro(this,gl.ruta,gl.cliente);

		clsBonFiltro  clsBFilt=new clsBonFiltro(this,gl.ruta,gl.cliente);

		dweek=mu.dayofweek();

		lblPrec.setText(mu.frmcur(0));
		lblTot.setText(mu.frmcur(0));

		System.gc();
	}

	private boolean hasProducts(){
		Cursor DT;

		try {
			sql="SELECT PRODUCTO FROM T_VENTA";
			DT=Con.OpenDT(sql);

			boolean rslt=DT.getCount()>0;

			if(DT!=null) DT.close();

			return rslt;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
	}

	private boolean tieneProductosConDiferencias(){
		Cursor DT;
		String producto="";
		double cant=0, cantvend = 0;
		String UM, UMV;
		int cantReg=0, regdif=0;
		boolean rslt=false;

        try {

            DT = null;

            clsClasses.clsDs_pedidod item;

            clsDs_pedidodObj Ds_pedidodObj=new clsDs_pedidodObj(this,Con,db);
            Ds_pedidodObj.fill("WHERE COREL='"+gl.iddespacho+"'");

            for (int i = 0; i <Ds_pedidodObj.count; i++) {

                producto = Ds_pedidodObj.items.get(i).producto.toString();
                UM = Ds_pedidodObj.items.get(i).umventa.toString();

                cant = Ds_pedidodObj.items.get(i).cant;

				/*if (!prodPorPeso(producto) && (!app.esRosty(producto)) && (!app.prodBarra(producto)) ){
					sql = "SELECT PRODUCTO, CANT, UMSTOCK FROM T_VENTA WHERE PRODUCTO = '" + producto + "' " +
							" AND UMSTOCK = '" + UM + "'";
				}else{*/
					sql = "SELECT PRODUCTO, CANT, UM FROM T_VENTA WHERE PRODUCTO = '" + producto + "' " +
							" AND UM = '" + UM + "'";
				//}

            	DT = Con.OpenDT(sql);

				cantReg = DT.getCount();

				//#CKFK se van a insertar en la tabla de modificación de la factura los productos con diferencia
				if (cantReg>0){

					DT.moveToFirst();

					cantvend = DT.getDouble(1);
					UMV = DT.getString(2);

				}else{
					cantvend = 0;
					UMV = "";
				}

				if (cant!=cantvend || !UM.equals(UMV)){
					ins.init("T_FACTURAD_MODIF");
					ins.add("COREL",gl.corelFac);
					ins.add("ANULADO",0);
					ins.add("PRODUCTO",producto);
					ins.add("CANTSOLICITADA",cant);
					ins.add("UMVENTASOLICITADA",UM);
					ins.add("PESOSOLICITADO",0);
					ins.add("CANTENTREGADA",cantvend);
					ins.add("UMVENTAENTREGADA",UMV);
					ins.add("PESOENTREGADO",0);
					ins.add("IDRAZON","");
					ins.add("PEDCOREL",gl.pedCorel);
					ins.add("STATCOM","N");
					ins.add("DESPCOREL",gl.iddespacho);

					db.execSQL(ins.sql());

					regdif += 1;
				}
			}

            if(DT!=null) DT.close();

			sql = "SELECT PRODUCTO, CANT, UM FROM T_VENTA WHERE PRODUCTO NOT IN " +
					"(SELECT PRODUCTO FROM DS_PEDIDOD WHERE COREL ='" + gl.coddespacho + "') " ;
			DT = Con.OpenDT(sql);

			cantReg += DT.getCount();

			//#CKFK se van a insertar en la tabla de modificación de la factura los productos
			//que no existen en la prefactura
			if (cantReg>0){

				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					producto = DT.getString(0);
					UM = DT.getString(2);
					cant = DT.getDouble(1);

					ins.init("T_FACTURAD_MODIF");
					ins.add("COREL",gl.corelFac);
					ins.add("ANULADO",0);
					ins.add("PRODUCTO",producto);
					ins.add("CANTSOLICITADA",0);
					ins.add("UMVENTASOLICITADA",UM);
					ins.add("PESOSOLICITADO",0);
					ins.add("CANTENTREGADA",cant);
					ins.add("UMVENTAENTREGADA",UM);
					ins.add("PESOENTREGADO",0);
					ins.add("IDRAZON","");
					ins.add("PEDCOREL","");
					ins.add("STATCOM","N");
					ins.add("DESPCOREL","");

					db.execSQL(ins.sql());

					regdif += 1;

					DT.moveToNext();
				}

				if(DT!=null) DT.close();

			}

			rslt = (regdif>0?true:false);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
		return rslt;
	}

	private boolean tieneProductosPendientesRazon(){
		Cursor DT;
		int cantReg=0;
		boolean rslt=false;

		try {

			DT = null;

			sql = "SELECT PRODUCTO FROM T_FACTURAD_MODIF WHERE IDRAZON = '' " ;
			DT = Con.OpenDT(sql);

			cantReg = DT.getCount();

			if(DT!=null) DT.close();

			rslt = cantReg>0;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
		return rslt;
	}

	private void showCredit(){
		try{
			if (hasCredits()){
				Intent intent = new Intent(this,Cobro.class);
				startActivity(intent);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	private boolean hasCredits(){
		Cursor DT;
		
		try {
			sql="SELECT SALDO FROM P_COBRO WHERE CLIENTE='"+cliid+"'";
			DT=Con.OpenDT(sql);

			boolean rslt=DT.getCount()>0;
			if(DT!=null) DT.close();

			if (rslt) return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
	    }			
		
		return false;
	}
	
	private void validaNivelPrecio(){
		Cursor DT;
		int np;
		
		np=gl.nivel;
		
		try {
			sql="SELECT CODIGO FROM P_PRODPRECIO WHERE NIVEL="+np;
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) msgbox("No existen los precios para nivel de precio del cliente");

			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
	    }			
	}
	
	private void doExit(){
		try{
			gl.closeCliDet=true;
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void cliPorDia() {
		Cursor DT;
		
		int dweek=mu.dayofweek();
		
		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);

			clidia=DT.getCount();

			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			clidia=0;
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

	private boolean prodRepesaje(String prodid) {
		try {
			return app.ventaRepesaje(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
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

	private void listaModificacion(){
		Cursor DT;
		String code,name;

		lcodeM.clear();lnameM.clear();

		try {

			sql="SELECT IDRAZON, DESCRIPCION FROM P_RAZON_DESP_INCOMP ORDER BY DESCRIPCION";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				try {
					code=DT.getString(0);
					name=DT.getString(1);

					lcodeM.add(code);
					lnameM.add(name);
				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());return;
		}

	}

	public void solicitaRazonModificacion(){
		Cursor DT;
		String prod;
        double dif;

		try{

			sql = "SELECT PRODUCTO, CANTSOLICITADA - CANTENTREGADA AS DIF " +
				  " FROM T_FACTURAD_MODIF WHERE IDRAZON = ''";

			DT=Con.OpenDT(sql);

			if (DT==null) return;

			if (DT.getCount()>0) {

				DT.moveToFirst();

				prod = DT.getString(0);
				dif = DT.getDouble(1);

				showNoDespDialog(prod,dif);
			}else{
				processFinishOrder();
			}

		}catch (Exception e){

		}
	}

	public void showNoDespDialog(final String prod, final double diferencia) {
		try{
			final AlertDialog Dialog;

			final String[] selitems = new String[lnameM.size()];
			for (int i = 0; i < lnameM.size(); i++) {
				selitems[i] = lnameM.get(i);
			}

			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Razón de modificación por " +
							"diferencia de " + diferencia  + " en el " +
					         prod + " " + app.getNombreProducto(prod));
            mMenuDlg.setCancelable(false);

			mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					try {
						String s=lcodeM.get(item);
						setModificacion(s, prod);
						solicitaRazonModificacion();
					} catch (Exception e) {
					}
				}
			});

			mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					sql="DELETE FROM T_FACTURAD_MODIF";
					db.execSQL(sql);

				}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showProdSinInvDialog() {
		try{
			final AlertDialog Dialog;

			final String[] selitems = new String[lProdSinInv.size()];
			for (int i = 0; i < lProdSinInv.size(); i++) {
				selitems[i] = lProdSinInv.get(i);
			}

			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Productos de lote sin inventario");
			mMenuDlg.setCancelable(false);

			mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {}
			});

			mMenuDlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setModificacion(String scna,String prod){

		try
		{
			upd.init("T_FACTURAD_MODIF");
			upd.add("IDRAZON",scna);
			upd.Where("PRODUCTO = '" + prod + "'");

			db.execSQL(upd.SQL());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
	}

	//endregion

	//region Activity Events
	
	@Override
	protected void onResume() {
		try{
			super.onResume();
			try {
				txtBarra.requestFocus();
			} catch (Exception e) {

			}

			if (gl.closeVenta) super.finish();

			if (browse==1) {
				browse=0;processItem();return;
			}

			if (browse==2) {
				browse=0;processCant();return;
			}

			if (browse==3) {
				browse=0;if (gl.promapl) updDesc();return;
			}

			if (browse==4) {
				browse=0;listItems();return;
			}

			if (browse==5) {
				browse=0;addBarcode();return;
			}

			if (browse==6) {
				browse=4;listItems();return;
			}

		}catch (Exception e)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	@Override
	public void onBackPressed() {
	    if (gl.devtotal>0) {
            msgAskExit("¿Está seguro de abandonar la venta? No se podrá aplicar la nota de crédito y se elminará la devolución");
        } else {
            msgAskExit("¿Salir sin terminar venta?");
        }
	}

	//endregion

}
