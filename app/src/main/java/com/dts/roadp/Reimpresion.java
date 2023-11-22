package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class Reimpresion extends PBase {

	private ListView listView;
	private TextView lblTipo;
	private ImageView imgPrint;

	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDVB adapter;
	private clsClasses.clsCFDV selitem;

	private AppMethods app;
	private printer prn;
	private printer prn_nc;
	private printer  prn_can,prn_paseante;
	private Runnable printclose,printcallback,printvoid;
	public  clsRepBuilder rep;
	
	private clsDocFactura fdoc;
	private clsDocMov mdoc;
	private clsDocDepos ddoc;
	private clsDocCobro cdoc;
	private clsDocDevolucion fdev;
	private clsDocCanastaBod fcanastabod;
	private clsDocCanastaBod fpaseantebod;
	private clsDocPedido docPed;

	private int tipo,impres;
	private String selid,itemid,corelNC,asignFact;
	//Para reimpresión de devolución de canastas y paseante
	private String  corel,existenciaC,existenciaP;

	// impresion nota credito

	private ArrayList<String> lines= new ArrayList<String>();
	private String pserie,pnumero,pruta,pvend,pcli,presol,presfecha,pfser,pfcor;
	private String presvence,presrango,pvendedor,pcliente,pclicod,pclidir;
	private double ptot;
	private boolean imprimecan=false;
	private int residx,ncFact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reimpresion);
	
		super.InitBase();
		addlog("Reimpresion",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		lblTipo= (TextView) findViewById(R.id.lblFecha);
        imgPrint= (ImageView) findViewById(R.id.imgPrint);

        if (gl.debug) imgPrint.setVisibility(View.VISIBLE);
        else imgPrint.setVisibility(View.INVISIBLE);

		app = new AppMethods(this, gl, Con, db);
		gl.validimp=app.validaImpresora();
		//if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

		if (!gl.debug)imgPrint.setVisibility(View.INVISIBLE);
		else imgPrint.setVisibility(View.VISIBLE);

		tipo=gl.tipo;
		itemid="*";
		
		setHandlers();
		insertaRegistrosFaltantes();
		listItems();

		prn=new printer(this,printclose,gl.validimp);
		prn_nc=new printer(this,printclose,gl.validimp);

		prn_can=new printer(this,printclose,gl.validimp);
		prn_paseante=new printer(this,printclose,gl.validimp);

		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
		fdoc.deviceid =gl.numSerie;
		fdoc.medidapeso=gl.umpeso;

		fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
		fdev.deviceid =gl.numSerie;

		fcanastabod=new clsDocCanastaBod(this,prn_can.prw,gl.peMon,gl.peDecImp, "printdevcan.txt");
		fcanastabod.deviceid =gl.numSerie;
		fcanastabod.vTipo="CANASTA";

		fpaseantebod=new clsDocCanastaBod(this,prn_paseante.prw,gl.peMon,gl.peDecImp, "printpaseante.txt");
		fpaseantebod.deviceid =gl.numSerie;
		fpaseantebod.vTipo="PASEANTE";

		printclose = new Runnable() {
			public void run() {
				int ii=1;
				try {
					if (ncFact == 1) {
						String corelFactura = getCorelFact(itemid);
						fdoc.buildPrint(corelFactura, 1, "TOL");
						prn.printnoask(printvoid, "print.txt");
					}
				} catch (Exception e) {
				}
			}
		};

		printvoid= new Runnable() {
			public void run() {
			}
		};

		printcallback = new Runnable() {
			public void run() {

				try {
					//#CKFK_20190401 03:43 PM Agregué esto para imprimir la NC cuando la factura está asociada a una
					corelNC = getCorelNotaCred(itemid);

					if (!corelNC.isEmpty()) {

						fdev.buildPrint(corelNC, 1, "TOL");
						prn_nc.printnoask(printvoid, "printnc.txt");
					}

					if (imprimecan){
						prn_can.printnoask(printclose, "printdevcan.txt");
						imprimecan=false;
					}

					askPrint();
				} catch (Exception e) {
					msgbox(e.getMessage());
				}

			}
		};

		switch (tipo) {
		case 0:
			docPed = new clsDocPedido(this,prn.prw,gl.peMon,gl.peDecImp,"");
			docPed.global=gl;
			lblTipo.setText("Pedido");break;
		case 1:
			cdoc=new clsDocCobro(this,prn.prw,gl.peMon,gl.peDecImp, gl.numSerie, "");
			lblTipo.setText("Recibo");break;	
		case 2:  
			ddoc=new clsDocDepos(this,prn.prw,gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp, "");
			lblTipo.setText("Deposito");break;
		case 3:  
			fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
			fdoc.medidapeso=gl.umpeso;
			lblTipo.setText("Factura");break;
		case 4:  
			mdoc=new clsDocMov(this,prn.prw,"Recarga",gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp, "");
			lblTipo.setText("Recarga");break;
		case 5:  
			mdoc=new clsDocMov(this,prn.prw,"Dvolucion a bodega",gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp, "");
			lblTipo.setText("Devolución a bodega");break;
		case 6:  
			fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
			fdev.deviceid =gl.numSerie;
			lblTipo.setText("Nota Crédito");break;

		case 7:
			fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
			fdev.deviceid =gl.numSerie;
			lblTipo.setText("Nota Débito");break;
		case 99:  
			lblTipo.setText("Cierre de día");break;
		}		
			
	}
	
	
	// Events
	
	public void printDoc(View view){

		try{
			if (itemid.equalsIgnoreCase("*")) {
				mu.msgbox("Debe seleccionar un documento.");return;
			}

			printDocument();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void printTxt(View view){

		try{
			prn.printask(printcallback);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void setHandlers(){
		try{

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					onBackPressed();
				}
				public void onSwipeLeft() {}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						itemid=vItem.Cod;
						adapter.setSelectedIndex(position);
						//printDocument();
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
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						itemid=vItem.Cod;
						adapter.setSelectedIndex(position);

						printDocument();
						//printDoc(view);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}


	// Main

	public void insertaRegistrosFaltantes(){

		try{
			sql="INSERT INTO D_FACTURA_CONTROL_CONTINGENCIA " +
					"SELECT (SELECT IFNULL(MAX(IDTABLACONTROL),0)+1 IDTABLACONTROL " +
					"FROM D_FACTURA_CONTROL_CONTINGENCIA) IDTABLACONTROL,'' CUFE, '01' TIPODOCUMENTO, " +
					"substr('0000000000'|| CORELATIVO, length('0000000000'||CORELATIVO)-9 ,10) NUMERORODOCUMENTO, " +
					"(SELECT SUCURSAL FROM P_RUTA) SUCURSAL, SERIE CAJA, '' ESTADO, '' MENSAJE, " +
					"'' VALOR_XML, '" + String.valueOf(du.getFechaCompleta()) + "' FECHAENVIO, '01' TIPOFACTURA, " +
					"'" + String.valueOf(du.getFechaCompleta()) + "' FECHAAGR, " +
					"'' QR, COREL, RUTA, VENDEDOR, '' HOST, '0' CODIGOLIQUIDACION, " +
					"substr('0000000000'|| CORELATIVO, length('0000000000'||CORELATIVO)-9 ,10), '' QRIMAGE, " +
					" '1900-01-01T00:00:00' FECHA_AUTORIZACION, '' NUMERO_AUTORIZACION " +
					"FROM D_FACTURA " +
					"WHERE COREL NOT IN (SELECT COREL FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE TIPODOCUMENTO = '01' )";
			db.execSQL(sql);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		try{
			sql = " INSERT INTO D_FACTURA_CONTROL_CONTINGENCIA " +
					" SELECT (SELECT IFNULL(MAX(IDTABLACONTROL),0)+1 IDTABLACONTROL " +
					" FROM D_FACTURA_CONTROL_CONTINGENCIA) IDTABLACONTROL,'' CUFE, " +
					" CASE WHEN TIPO_DOCUMENTO = 'ND' THEN '07' ELSE " +
					" CASE WHEN TIPO_DOCUMENTO = 'NC' AND LENGTH(FACTURA)>9 THEN '04' ELSE '07' END END TIPODOCUMENTO, " +
					" CORELATIVO NUMERORODOCUMENTO, " +
					" (SELECT SUCURSAL FROM P_RUTA), SERIE CAJA, ''ESTADO, '' MENSAJE, " +
					" '' VALOR_XML, '" + String.valueOf(du.getFechaCompleta()) + "' FECHAENVIO, " +
					" CASE WHEN TIPO_DOCUMENTO = 'ND' THEN '07' ELSE " +
					" CASE WHEN TIPO_DOCUMENTO = 'NC' AND LENGTH(FACTURA)>9 THEN '04' ELSE '07' " +
					" END END TIPOFACTURA, '" + String.valueOf(du.getFechaCompleta()) + "' FECHAAGR, " +
					" '' QR, COREL, RUTA, VENDEDOR, '' HOST, '0' CODIGOLIQUIDACION, CORELATIVO, " +
					" '' QRIMAGE, '1900-01-01T00:00:00','' NUMERO_AUTORIZACION " +
					" FROM D_NOTACRED " +
					" WHERE D_NOTACRED.COREL NOT IN  " +
					" (SELECT C.COREL FROM D_FACTURA_CONTROL_CONTINGENCIA C " +
					"  WHERE C.TIPODOCUMENTO <> '01')";
			db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}

	public void listItems() {
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		int vP;
		long f;
		double val;
		String id,sf,sval,tm;
			
		items.clear();
		
		selidx=-1;vP=0;
		
		try {
				
			if (tipo==0) {
				sql= " SELECT D_PEDIDO.COREL,P_CLIENTE.NOMBRE,D_PEDIDO.FECHA,D_PEDIDO.TOTAL,D_PEDIDO.BANDERA "+
					 " FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO "+
					 " ORDER BY D_PEDIDO.COREL DESC ";
			}
			
			if (tipo==1) {
				sql="SELECT D_COBRO.COREL,P_CLIENTE.NOMBRE,D_COBRO.FECHA,D_COBRO.TOTAL "+
					 "FROM D_COBRO INNER JOIN P_CLIENTE ON D_COBRO.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_COBRO.ANULADO='N') AND (D_COBRO.DEPOS<>'S') ORDER BY D_COBRO.COREL DESC ";	
			}
			
			if (tipo==2) {
				sql="SELECT D_DEPOS.COREL,P_BANCO.NOMBRE,D_DEPOS.FECHA,D_DEPOS.TOTAL,D_DEPOS.CUENTA "+
					 "FROM D_DEPOS INNER JOIN P_BANCO ON D_DEPOS.BANCO=P_BANCO.CODIGO "+
					 "WHERE (D_DEPOS.ANULADO='N') ORDER BY D_DEPOS.COREL DESC ";	
			}

			if (tipo == 3) {
				if (gl.peModal.equalsIgnoreCase("TOL")) {
					sql = " SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO," +
						  " D_FACTURA.IMPRES,D_FACTURA.CUFE, D_FACTURA_CONTROL_CONTINGENCIA.NUMERO_AUTORIZACION, " +
					      " D_FACTURA.CERTIFICADA_DGI, D_FACTURA_CONTROL_CONTINGENCIA.Estado "+
						  " FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO " +
						  "  INNER JOIN D_FACTURA_CONTROL_CONTINGENCIA ON D_FACTURA.COREL=D_FACTURA_CONTROL_CONTINGENCIA.COREL "+
						  " WHERE (D_FACTURA.STATCOM='N') AND " +
						  " (D_FACTURA_CONTROL_CONTINGENCIA.TIPODOCUMENTO = '01') " +
						  " ORDER BY D_FACTURA.COREL DESC";
				} else {
					sql = "SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO,D_FACTURA.IMPRES " +
							"FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO " +
							"WHERE AND (D_FACTURA.STATCOM='N') ORDER BY D_FACTURA.COREL DESC LIMIT 1";
				}
			}
				
			if (tipo==4 || tipo==5) {
				tm="R";if (tipo==5) tm="D";
				sql="SELECT COREL,COREL,FECHA,0 AS TOTAL "+
					 "FROM D_MOV WHERE (TIPO='"+tm+"') AND (ANULADO='N')  ORDER BY COREL DESC ";	
			}
			
			if (tipo==6) {
				sql="SELECT D_NOTACRED.COREL,P_CLIENTE.CODIGO || ' - ' || P_CLIENTE.NOMBRE,D_NOTACRED.SERIE,D_NOTACRED.TOTAL," +
					"       D_NOTACRED.CORELATIVO,D_NOTACRED.IMPRES,D_NOTACRED.CUFE, D_FACTURA_CONTROL_CONTINGENCIA.NUMERO_AUTORIZACION," +
					"       D_NOTACRED.CERTIFICADA_DGI, D_FACTURA_CONTROL_CONTINGENCIA.Estado "+
					"FROM D_NOTACRED INNER JOIN P_CLIENTE ON D_NOTACRED.CLIENTE=P_CLIENTE.CODIGO "+
					"      INNER JOIN D_FACTURA_CONTROL_CONTINGENCIA ON D_NOTACRED.COREL=D_FACTURA_CONTROL_CONTINGENCIA.COREL "+
					" WHERE (D_NOTACRED.STATCOM='N') AND (D_FACTURA_CONTROL_CONTINGENCIA.TIPODOCUMENTO IN ('04','06')) " +
					" AND (D_NOTACRED.TIPO_DOCUMENTO = 'NC') " +
					" AND (ANULADO = 'N') " +
					" ORDER BY D_NOTACRED.COREL DESC ";
			}

			if (tipo==7) {
				sql="SELECT D_NOTACRED.COREL,P_CLIENTE.CODIGO || ' - ' || P_CLIENTE.NOMBRE,D_NOTACRED.SERIE,D_NOTACRED.TOTAL," +
						"       D_NOTACRED.CORELATIVO,D_NOTACRED.IMPRES,D_NOTACRED.CUFE, D_FACTURA_CONTROL_CONTINGENCIA.NUMERO_AUTORIZACION," +
						"       D_NOTACRED.CERTIFICADA_DGI, D_FACTURA_CONTROL_CONTINGENCIA.Estado "+
						" FROM D_NOTACRED INNER JOIN P_CLIENTE ON D_NOTACRED.CLIENTE=P_CLIENTE.CODIGO "+
						" INNER JOIN D_FACTURA_CONTROL_CONTINGENCIA ON D_NOTACRED.COREL=D_FACTURA_CONTROL_CONTINGENCIA.COREL "+
						" WHERE (D_NOTACRED.STATCOM='N') AND (D_FACTURA_CONTROL_CONTINGENCIA.TIPODOCUMENTO IN ('05','07')) AND (D_NOTACRED.TIPO_DOCUMENTO = 'ND')" +
						" ORDER BY D_NOTACRED.COREL DESC ";
			}
			
			if (tipo<99) {
				
				DT=Con.OpenDT(sql);
	
				if (DT.getCount()>0) {

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						id=DT.getString(0);

						vItem =clsCls.new clsCFDV();

						vItem.Cod=DT.getString(0);
						vItem.Desc=DT.getString(1);
						if (tipo==2) vItem.Desc+=" - "+DT.getString(4);

						vItem.tipodoc = tipo;

						if (tipo==3) {
							sf=DT.getString(2)+ StringUtils.right("000000" + Integer.toString(DT.getInt(4)), 6);
							vItem.Cufe =DT.getString(6);
							vItem.Numero_Autorizacion=DT.getString(7);
							vItem.Certificada_DGI = (DT.getInt(8)==1?"Si":"No");
							vItem.Estado  = DT.getString(9);
						} else if (tipo==1||tipo==6 || tipo==7){
							sf=DT.getString(0);
						}else {
							f=DT.getLong(2);sf=du.sfecha(f)+" "+du.shora(f);
						}

						vItem.Fecha=sf;

						if (tipo==6 || tipo == 7){
							vItem.Cufe = DT.getString(6);
							vItem.Certificada_DGI = (DT.getInt(8)==1?"Si":"No");
							vItem.Estado = DT.getString(9);
						}

						val=DT.getDouble(3);sval=""+val;
						vItem.Valor=sval;	  

						if (tipo==4 || tipo==5) {
							vItem.Valor="";
						} else {
							vItem.Valor=mu.frmcur(val);
						}

						vItem.bandera=0;
						if (tipo==0) {
						    if (DT.getString(4).equalsIgnoreCase("S")) vItem.bandera=1;
                        }

						if (tipo==3 || tipo==6) {
							if (gl.peModal.equalsIgnoreCase("TOL")) {
								items.add(vItem);
							} else {
								if (DT.getInt(5)<=1) items.add(vItem);
							}
						} else {	
							items.add(vItem);	
						}
	
						if (id.equalsIgnoreCase(selid)) selidx=vP;
						vP+=1;

						DT.moveToNext();					

					}	
				}

			} else {	
				
				if (tipo==99) {

					vItem =clsCls.new clsCFDV();

					vItem.Cod="";
					vItem.Desc="";
					vItem.Fecha="Ultimo Cierre de dia";
					vItem.Valor="";	  

					items.add(vItem);				
				}		
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox("listItems: "+ e.getMessage());
	    }
			 
		adapter=new ListAdaptCFDVB(this, items);
		listView.setAdapter(adapter);
		
		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}
	    	    
	}
	
	private void printDocument() {

		try{
			switch (tipo) {
				case 0:
					imprPedido();break;
				case 1:
					imprRecibo();break;
				case 2:
					imprDeposito();break;
				case 3:
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						imprFactura();
					} else {
						imprUltFactura();
					}
					break;
				case 4:
					imprRecarga();break;
				case 5:
					imprDevol();break;
				case 6:
					imprUltNotaCredito();break;
				case 7:
					imprUltNotaDebito();break;
				case 99:
					imprFindia();break;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void imprPedido() {
		try{

			docPed.global=gl;
			docPed.deviceid =gl.numSerie;

			// #KM 2021/11/16 Inicializamos el valor por defecto
			// se pasaba vacio docPed.buildPrint(itemid,1,"");
			String modo = "*";

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				modo = "TOl";
			}

			docPed.buildPrint(itemid,0,modo);

			prn.printask(printcallback);

			toast("Reimpresion pedido");

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		
	}
	
	private void imprRecibo() {
		try {
			if(prn.isEnabled()){
				cdoc.buildPrint(itemid,1,"");
				prn.printask(printcallback);
			}else if(!prn.isEnabled()){
				cdoc.buildPrint(itemid,1,"");
				toast("Reimpresion de recibo generada");
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}

	private void imprDeposito() {
		try {
			if (prn.isEnabled()){
				ddoc.buildPrint(itemid, 1,gl.peModal);
				prn.printask(printcallback);
			} else if (!prn.isEnabled()){
				ddoc.buildPrint(itemid, 1,gl.peModal);
				toast("Reimpresion de deposito generada");
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}	
	}
	
	private void imprFactura() {
		Cursor dt;
		int impr;

		fdoc.deviceid =gl.numSerie;

		try {
			sql="SELECT IMPRES FROM D_FACTURA WHERE COREL='"+itemid+"'";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			impr=dt.getInt(0);
		} catch (Exception e) {
			impr=1;
		}

		try {

			if(prn.isEnabled()){
				if (fdoc.buildPrint(itemid,impr,gl.peFormatoFactura))
				    prn.printask(printcallback);

			}else if(!prn.isEnabled()){
				fdoc.buildPrint(itemid,impr,gl.peFormatoFactura);

				corelNC=getCorelNotaCred(itemid);

				if (!corelNC.isEmpty()){
					/*fdev=new clsDocDevolucion(this,prn.prw,gl.peMon,gl.peDecImp, "printnc.txt");
					fdev.deviceid =gl.numSerie;*/

					fdev.buildPrint(corelNC, 1, "TOL");
					toast("Reimpresion de factura y nota de credito generada");
				}else{
					toast("Reimpresion de factura generada");
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}	
	
	private void imprRecarga() {
		try {
			if(prn.isEnabled()){
				mdoc.buildPrint(itemid,1);
				prn.printask();
			}else if(!prn.isEnabled()){
				mdoc.buildPrint(itemid,1);
				toast("Reimpresion de recarga generada");
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}

	private void imprDevol() {
		try {

			corel = itemid;

			impres=0;

			existenciaC=tieneCanasta(corel);
			existenciaP=tienePaseante(corel);

			if(existenciaC.isEmpty() && !existenciaP.isEmpty()) impres=1;
			if(!existenciaC.isEmpty() && existenciaP.isEmpty()) impres=2;
			if(existenciaC.isEmpty() && existenciaP.isEmpty()) impres=3;

			if (prn_can.isEnabled()) {

				String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
				try {
					if(impres==0 || impres==1){
						fpaseantebod.buildPrint(corel,0,vModo);
					}
				} catch (Exception e) {
				}

				try {
					if(impres==0 || impres==2){
						imprimecan=true;
						fcanastabod.buildPrint(corel,0, vModo);
					}
				} catch (Exception e) {
				}

				if(impres==0) {
					prn_paseante.printask(printcallback, "printpaseante.txt");
				}else if(impres==1) {
					prn_paseante.printask(printcallback, "printpaseante.txt");
				}else if(impres==2) {
					prn_can.printask(printcallback, "printdevcan.txt");
				}

			}else if(!prn_can.isEnabled()){

				String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");

				if(impres==0 || impres==1){
					fpaseantebod.buildPrint(corel,0,vModo);
				}

				if(impres==0 || impres==2){
					imprimecan=true;
					fcanastabod.buildPrint(corel,0, vModo);
				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}

	public void prnTxt(View view){
	    try{
            prn.printask(printcallback);
        }catch (Exception ex){
	        msgbox("Ocurrió un error imprimiendo la factura");
        }
    }

	//CM_20190506: Valida si tiene cantastas y devolución

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

		}catch (Exception ex){
			mu.msgbox("Ocurrió un error "+ex.getMessage());
		}

		return vtieneCanasta;
	}

	private String tienePaseante(String vCorel){

		Cursor DT;
		String vtienePaseante= "";

		try{

			sql = "SELECT COREL FROM D_MOVD WHERE COREL = '" + vCorel + "' AND COREL IN (SELECT COREL FROM D_MOV)";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				vtienePaseante = DT.getString(0);
			}

		}catch (Exception ex){
			mu.msgbox("Ocurrió un error "+ex.getMessage());
		}

		return vtienePaseante;
	}

	private void imprFindia() {
		try {
			if(prn.isEnabled()){
				prn.printask("SyncFold/findia.txt");
			}else if(!prn.isEnabled()){
				toast("No hay impresora configurada");
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}	
	
	
	// Ultima factura + nota credito
	
	private void imprUltFactura() {
		Cursor dt;
		String id,serie;
		int corel;

		try {

			sql="SELECT COREL,IMPRES,SERIE,CORELATIVO FROM D_FACTURA WHERE COREL='"+itemid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) {
				msgbox("¡No existe ninguna factura elegible para reimpresion!");return;
			}
			
			dt.moveToFirst();
			
			id=dt.getString(0);
			serie=dt.getString(2);
			corel=dt.getInt(3);		
			
			if (dt.getInt(1)>1) {
				msgbox("¡La factura "+serie+" - "+corel+" no se puede imprimir porque ya fue reimpresa anteriormente!");return;
			}

			if(prn.isEnabled()){
				if (fdoc.buildPrint(id,1,gl.peFormatoFactura)) prn.printask();

				try {
					sql="UPDATE D_FACTURA SET IMPRES=2 WHERE COREL='"+itemid+"'";
					db.execSQL(sql);
				} catch (Exception e) {
				}

				//#CKFK_20190401 03:43 PM Agregué esto para imprimir la NC cuando la factura está asociada a una
				String corelNC=getCorelNotaCred(itemid);

				if (!corelNC.isEmpty()){
					fdev=new clsDocDevolucion(this,prn.prw,gl.peMon,gl.peDecImp, "printnc.txt");
					fdev.deviceid =gl.numSerie;
					fdev.buildPrint(corelNC, 1, "TOL"); prn_nc.printask(printclose, "printnc.txt");
				}

			}else if(!prn.isEnabled()){

				fdoc.buildPrint(id,1,gl.peFormatoFactura);

				try {
					sql="UPDATE D_FACTURA SET IMPRES=2 WHERE COREL='"+itemid+"'";
					db.execSQL(sql);
				} catch (Exception e) {
				}

				String corelNC=getCorelNotaCred(itemid);

				if (!corelNC.isEmpty()){
					fdev.buildPrint(corelNC, 1, "TOL");
					toast("Reimpresion de UltFactura generada");
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}
	
	private void imprUltNotaCredito() {
		Cursor dt;
		String id,serie;
		int corel;
		String corelFactura=getCorelFact(itemid);

		try {

			if(prn.isEnabled()){

				if(ncFact==1){
					if(tipo==6){
						fdev.buildPrint(itemid, 3, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");

						toast("Reimpresión de nota de crédito y factura generada");
					}else {
						fdev.buildPrint(itemid, 1, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");
						toast("Reimpresión de nota de crédito y factura generada");
					}

				}else if(ncFact==2){
					if(tipo==6){
						fdev.buildPrint(itemid, 3, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");

						toast("Reimpresion de nota de credito generada");
					}else{
						fdev.buildPrint(itemid, 1, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");
						toast("Reimpresion de nota de credito generada");
					}
				}

			} else if(!prn.isEnabled()){

				if(ncFact==1){
					fdev.buildPrint(itemid, 1, "TOL");
					fdoc.buildPrint(corelFactura, 1, "TOL");

					toast("Reimpresion de nota de credito y factura generada");

				}else if(ncFact==2){
					fdev.buildPrint(itemid, 1, "TOL");

					toast("Reimpresion de nota de credito generada");
				}

			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("imprUltNotaCredito: "+new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	private void imprUltNotaDebito() {
		Cursor dt;
		String id,serie;
		int corel;
		String corelFactura=getCorelFact(itemid);

		try {

			if(prn.isEnabled()){

				if(ncFact==1){
					if(tipo==6){
						fdev.buildPrint(itemid, 3, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");

						toast("Reimpresión de nota de débito y factura generada");
					}else {
						fdev.buildPrint(itemid, 1, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");
						toast("Reimpresión de nota de débito y factura generada");
					}

				}else if(ncFact==2){
					if(tipo==6){
						fdev.buildPrint(itemid, 3, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");

						toast("Reimpresion de nota de débito generada");
					}else{
						fdev.buildPrint(itemid, 1, "TOL");
						prn_nc.printask(printcallback, "printnc.txt");

						toast("Reimpresion de nota de débito generada");
					}
				}

			} else if(!prn.isEnabled()){

				if(ncFact==1){
					fdev.buildPrint(itemid, 1, "TOL");
					fdoc.buildPrint(corelFactura, 1, "TOL");

					toast("Reimpresion de nota de debito y factura generada");

				}else if(ncFact==2){
					fdev.buildPrint(itemid, 1, "TOL");

					toast("Reimpresion de nota de debito generada");
				}

			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("imprUltNotaCredito: "+new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}


	private String getCorelFact(String vCorel){

		Cursor DT;
		String vtieneFacturaNC= "";
		ncFact=0;

		try{

			sql = "SELECT F.COREL, F.ASIGNACION, N.COREL "+
				  "FROM D_FACTURA F INNER JOIN D_NOTACRED N ON F.COREL = N.FACTURA "+
				  "WHERE N.COREL = '"+vCorel+"'";

			DT=Con.OpenDT(sql);

			if(DT.getCount()==0){
				sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '"+ vCorel +"'";
				DT=Con.OpenDT(sql);

				if(DT.getCount()>0){
					DT.moveToFirst();
					vtieneFacturaNC = DT.getString(0);
					ncFact=2;

					return vtieneFacturaNC;
				}

			}else if(DT.getCount()>0){
				DT.moveToFirst();
				vtieneFacturaNC = DT.getString(0);
				asignFact = DT.getString(2);
				ncFact=1;
			}


		}catch (Exception ex){
			mu.msgbox("tieneFacturaNC ocurrió un error: "+ex.getMessage());
		}

		return vtieneFacturaNC;
	}

	private String getCorelNotaCred(String vCorel){

		Cursor DT;
		String vCorelNC= "";

		try{

			sql = "SELECT N.COREL FROM D_FACTURA F  INNER JOIN D_NOTACRED N ON F.COREL = N.FACTURA "+
				  "WHERE F.COREL = '" + vCorel + "'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				vCorelNC = DT.getString(0);
			}

		}catch (Exception ex){
			mu.msgbox("tieneNCFactura ocurrió un error "+ex.getMessage());
		}

		return vCorelNC;
	}

	// Aprofam
	
	private void aprNotePrn(String corel) {
		
		aprLoadHeadData(corel);
		
		try {
			
			rep=new clsRepBuilder(this,prn.prw,true,gl.peMon,gl.peDecImp, "");
			
			buildHeader(corel,1);
			
			rep.line();
			rep.empty();
			rep.addc("NOTA CREDITO");
			rep.empty();
			rep.line();
			rep.empty();
				
			rep.add("Factura serie : "+pfser+" numero : "+pfcor);
			rep.add("Monto total : "+mu.frmdec(ptot));			
			rep.empty();
			rep.line();
			rep.empty();
			rep.empty();
			rep.empty();
				
			rep.save();
			
			prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private boolean aprLoadHeadData(String corel) {
		Cursor DT;
		long ff;
					
		try {
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,SERIEFACT,CORELFACT FROM D_NOTACRED WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pserie=DT.getString(0);
			pnumero=""+DT.getInt(1);
			pruta=DT.getString(2);
			
			pvend=DT.getString(3);
			pcli=DT.getString(4);		
			ptot=DT.getDouble(5);
			
			pfser=DT.getString(6);
			pfcor=DT.getString(7);
	
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());return false;
	    }	
		
		try {
			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			presol="Resolucion No. : "+DT.getString(0);
			ff=DT.getLong(1);presfecha="De Fecha : "+du.sfecha(ff);
			ff=DT.getLong(2);presvence="Resolucion vence : "+du.sfecha(ff);
			presrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
			try {
			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+pvend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pvendedor=DT.getString(0);
		} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			pvendedor=pvend;
	    }	
		
		try {
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+pcli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pcliente=DT.getString(0);       		
			pclicod=pcli;
			pclidir=DT.getString(3);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			pcliente=pcli;
	    }	
		
			
		return true;
		
	}

	private boolean buildHeader(String corel,int reimpres) {

		lines.clear();

		try {	
			loadHeadLines();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());return false;
		}		

		saveHeadLines(reimpres);

		return true;
	}

	private void saveHeadLines(int reimpres) {
		String s;

		rep.empty();rep.empty();

		try{
			for (int i = 0; i <lines.size(); i++) {
				s=lines.get(i);
				s=encabezado(s);
				if (residx==1) {
					rep.add(presol);
					rep.add(presfecha);
					rep.add(presvence);
					rep.add(presrango);
					residx=0;
				}
				if (!s.equalsIgnoreCase("@@")) rep.add(s);
			}

			if (!mu.emptystr(pclicod)) rep.add(pclicod);
			if (!mu.emptystr(pclidir)) rep.add(pclidir);

			if (reimpres==1) rep.add("-------  R E I M P R E S I O N  -------");
			if (reimpres==2) rep.add("------  C O N T A B I L I D A D  ------");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private String encabezado(String l) {
		String s,lu;
		int idx;

		residx=0;

		//lu=l.toUpperCase().trim();
		lu=l.trim();

		try{
			if (lu.length()==1 && lu.equalsIgnoreCase("N")) {
				s="NOTA CREDITO";s=rep.ctrim(s);return s;
			}

			if (l.indexOf("dd-MM-yyyy")>=0) {
				s=du.sfecha(du.getActDateTime());
				l=l.replace("dd-MM-yyyy",s);return l;
			}

			if (l.indexOf("HH:mm:ss")>=0) {
				s=du.shora(du.getActDateTime());
				l=l.replace("HH:mm:ss",s);return l;
			}

			idx=lu.indexOf("SS");
			if (idx>=0) {
				if (mu.emptystr(pserie)) return "@@";
				if (mu.emptystr(pnumero)) return "@@";

				s=lu.substring(0,idx);
				s="Nota credito serie : ";
				s=s+pserie+" numero : "+pnumero;
				residx=1;
				return s;
			}

			idx=lu.indexOf("VV");
			if (idx>=0) {
				if (mu.emptystr(pvendedor)) return "@@";
				l=l.replace("VV",pvendedor);return l;
			}

			idx=lu.indexOf("RR");
			if (idx>=0) {
				if (mu.emptystr(pruta)) return "@@";
				l=l.replace("RR",pruta);return l;
			}

			idx=lu.indexOf("CC");
			if (idx>=0) {
				if (mu.emptystr(pcliente)) return "@@";
				l=l.replace("CC",pcliente);return l;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


		return l;
	}
	
	private boolean loadHeadLines() {
		Cursor DT;	
		String s;
		
		try {
			sql="SELECT TEXTO FROM P_ENCABEZADO_REPORTESHH ORDER BY CODIGO";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return false;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				s=DT.getString(0);	
				lines.add(s);	
				DT.moveToNext();
			}

			return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());return false;
		}				
	}

	
	// Aux
	
	private void msgAsk(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("ROAD");
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					printDocument();
				}
			});
			dialog.setNegativeButton("No", null);
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void askPrint() {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					try {

						switch (tipo) {
							case 0:
								sql = "UPDATE D_PEDIDO SET IMPRES=IMPRES+1 WHERE COREL='" + itemid + "'";
								db.execSQL(sql);
								break;
							case 1:
								sql = "UPDATE D_COBRO SET IMPRES=IMPRES+1 WHERE COREL='" + itemid + "'";
								db.execSQL(sql);
								break;
							case 2:
								sql = "UPDATE D_DEPOS SET IMPRES=IMPRES+1 WHERE COREL='" + itemid + "'";
								db.execSQL(sql);
								db.execSQL("UPDATE FinDia SET val3=1");
								break;
							case 3:
								sql = "UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='" + itemid + "'";
								db.execSQL(sql);
								try {
									sql = "UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='" + corelNC + "'";
									db.execSQL(sql);
								} catch (Exception e) {}
								break;
							case 5:
								sql = "UPDATE D_MOV SET IMPRES=IMPRES+1 WHERE COREL='" + itemid + "' AND TIPO='D' ";
								db.execSQL(sql);
								break;
						}
					} catch (Exception e) {
						msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	private String androidid() {
	    String uniqueID="";
        try {
            uniqueID = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            uniqueID="0000000000";
        }

		return uniqueID;
	}


}
