package com.dts.roadp;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.edocsdk.Fimador;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
import Facturacion.Anulacion.AnularFactura;
import Facturacion.Anulacion.ResultadoAnulacion;
import Facturacion.CatalogoFactura;
import Facturacion.ConfigRetrofit;
import Facturacion.Token;
import Interfaz.AnularDocs;
import Interfaz.ServicioToken;
import retrofit2.Call;
import retrofit2.Response;

public class Anulacion extends PBase {

	private ListView listView;
	private TextView lblTipo;

	private ImageView cmdAnular;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDVB adapter;
	private clsClasses.clsCFDV selitem;
	
	private Runnable printotrodoc,printclose;
	private printer prn;
    private printer prn_nc;
	public  clsRepBuilder rep;
	private clsDocAnul doc;
	private clsDocFactura fdoc;

	private clsClasses.clsCFDV sitem, tmpItem;
	private AppMethods app;
	
	private int tipo,depparc,fcorel;	
	private String selid,itemid,fserie,fres,scor, CUFE, corelNotaCre, corelFactura;
	private boolean modoapr=false,toledano;

	private String vError="";

	private int ncItem = 0;

	// impresion nota credito
	
	private ArrayList<String> lines= new ArrayList<String>();
	private String pserie,pnumero,pruta,pvend,pcli,presol,presfecha,pfser,pfcor;
	private String presvence,presrango,pvendedor,pcliente,pclicod,pclidir;
	private double ptot;
	private int residx;

	//#Anular Factura
	private ProgressDialog progress;
	private clsClasses.clsEmpresa Empresa = clsCls.new clsEmpresa();
	private ConfigRetrofit retrofit;
	private Token token = new Token();
	private ResultadoAnulacion resultado = new ResultadoAnulacion();
	boolean exito = false, NCRefencia = false;

	private rFE NotaDebito = new rFE();
	private CatalogoFactura Catalogo;
	private String cliente;
	private clsClasses.clsCliente Cliente;
	private clsClasses.clsSucursal Sucursal;
	private clsClasses.clsMunicipio Municipio;
	private clsClasses.clsDepartamento Departamento;
	private clsClasses.clsCiudad Ciudad;
	private ArrayList<clsClasses.clsBeNotaCreditoDet> DetalleNT;
	private clsClasses.clsProducto Producto;
	private clsClasses.clsNotaCreditoEnc NotaDebitoEnc;

	private String urltoken = "";
	private String usuario = "";
	private String clave = "";
	private String urlDoc = "";
	private String QR = "";
	private String urlanulacion="";

	private String referencia = "";
	private boolean exito_anula_nc = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Cursor DT;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anulacion);
		
		super.InitBase();
		addlog("Anulacion",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		lblTipo= (TextView) findViewById(R.id.lblDescrip);
		cmdAnular = (ImageView) findViewById(R.id.imgImg);

		app = new AppMethods(this, gl, Con, db);
		gl.validimp=app.validaImpresora();

		if (!gl.validimp) {
			msgbox("¡La impresora no está autorizada!");
		}

        toledano=gl.peModal.equalsIgnoreCase("TOL");

		//#CKFK20230118 Agregamos esta información quemada como variables
		urltoken = gl.url_token;
		usuario = gl.usuario_api;
		clave = gl.clave_api;
		urlDoc = gl.url_emision_nd_b2b_hh;
		QR = gl.qr_api;
		urlanulacion=gl.url_b2c_hh;

		tipo=gl.tipo;
		if (gl.peModal.equalsIgnoreCase("APR")) modoapr=true;
		
		if (tipo==0) lblTipo.setText("Pedido");
		if (tipo==1) lblTipo.setText("Recibo");
		if (tipo==2) lblTipo.setText("Depósito");
		if (tipo==3) lblTipo.setText("Factura");
		if (tipo==4) lblTipo.setText("Recarga");
		if (tipo==5) lblTipo.setText("Devolución a bodega");
		if (tipo==6) lblTipo.setText("Nota de crédito");

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

		ProgressDialog("Cargando pantalla...");
		itemid="*";
		printotrodoc = new Runnable() {
		    public void run() {

				askPrint();
		    }
		};
		printclose= new Runnable() {
		    public void run() {
			}
		};
		
		prn=new printer(this,printclose,gl.validimp);
        prn_nc=new printer(this,printclose,gl.validimp);

		setHandlers();
		insertaRegistrosFaltantes();
		listItems();
				
		doc=new clsDocAnul(this,prn.prw,"");

		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp,"",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
		fdoc.medidapeso=gl.umpeso;
		Catalogo = new CatalogoFactura(this, Con, db);
		retrofit = new ConfigRetrofit(this);
		getDatosEmpresa();
	}

	//region Events
	private void getDatosEmpresa() {
		Cursor DT;

		try	{
			sql = "SELECT URL_AUTENTICACION, URL_ANULACION, USUARIO_API, CLAVE_API FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			if (DT.getCount() > 0) {
				Empresa.urlToken = DT.getString(0);
				Empresa.urlAnulacion = DT.getString(1);
				Empresa.usuarioApi = DT.getString(2);
				Empresa.claveApi = DT.getString(3);
			} else {
				return;
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}
	}

	public void anulDoc(View view){
		try{

			if (itemid.equalsIgnoreCase("*")) {
				mu.msgbox("Debe seleccionar un documento.");return;
			}

			cmdAnular.setVisibility(View.INVISIBLE);

			if (tipo==3) {
				inputValor();
			} else {
				msgAsk("Anular documento");
			}

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
						cliente = vItem.Desc.split(" -")[0];
						adapter.setSelectedIndex(position);
						Cliente = Catalogo.getCliente(cliente);

						sitem=vItem;
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
						sitem=vItem;

						anulDoc(view);
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

	//endregion

	//region Main

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
				  " CASE WHEN TIPO_DOCUMENTO = 'NC' AND LENGTH(FACTURA)>9 THEN '04' ELSE '06' END END TIPODOCUMENTO, " +
				  " CORELATIVO NUMERORODOCUMENTO, " +
			      " (SELECT SUCURSAL FROM P_RUTA), SERIE CAJA, ''ESTADO, '' MENSAJE, " +
				  " '' VALOR_XML, '" + String.valueOf(du.getFechaCompleta()) + "' FECHAENVIO, " +
				  " CASE WHEN TIPO_DOCUMENTO = 'ND' THEN '07' ELSE " +
				  " CASE WHEN TIPO_DOCUMENTO = 'NC' AND LENGTH(FACTURA)>9 THEN '04' ELSE '06' " +
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
		String id,sf,sval;
			
		items.clear();
		selidx=-1;vP=0;
		
		try {
			
			if (tipo==0) {
				sql="SELECT D_PEDIDO.COREL,P_CLIENTE.NOMBRE,D_PEDIDO.FECHA,D_PEDIDO.TOTAL,D_PEDIDO.BANDERA "+
					 "FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') ORDER BY D_PEDIDO.COREL DESC ";	
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
			
			if (tipo==3) {
				sql=" SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO, " +
					" D_FACTURA.CUFE, D_FACTURA_CONTROL_CONTINGENCIA.NUMERO_AUTORIZACION, D_FACTURA.CERTIFICADA_DGI, " +
					" D_FACTURA_CONTROL_CONTINGENCIA.Estado, P_CLIENTE.CODIGO"+
					" FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO "+
					"  INNER JOIN D_FACTURA_CONTROL_CONTINGENCIA ON D_FACTURA.COREL=D_FACTURA_CONTROL_CONTINGENCIA.COREL "+
					" WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') AND " +
					"       (D_FACTURA_CONTROL_CONTINGENCIA.TIPODOCUMENTO = '01')" +
					" ORDER BY D_FACTURA.COREL DESC ";
			}
			
			if (tipo==4) {
				sql="SELECT COREL,REFERENCIA,FECHA,0 "+
					 "FROM D_MOV WHERE (TIPO='R') AND (ANULADO='N') AND (STATCOM='N') ORDER BY FECHA DESC ";	
			}
			
			if (tipo==5) {
				sql="SELECT COREL,REFERENCIA,FECHA,0 "+
					 "FROM D_MOV WHERE (TIPO='D') AND (ANULADO='N') AND (STATCOM='N') ORDER BY FECHA DESC ";	
			}

			if (tipo==6) {
				sql=" SELECT D_NOTACRED.COREL,P_CLIENTE.CODIGO || ' - ' || P_CLIENTE.NOMBRE AS DESC,FECHA,D_NOTACRED.TOTAL, "+
					"        D_NOTACRED.CUFE, D_FACTURA_CONTROL_CONTINGENCIA.NUMERO_AUTORIZACION, D_NOTACRED.CERTIFICADA_DGI, " +
					"        D_FACTURA_CONTROL_CONTINGENCIA.Estado, D_NOTACRED.CUFE_FACTURA "+
					" FROM D_NOTACRED INNER JOIN P_CLIENTE ON D_NOTACRED.CLIENTE=P_CLIENTE.CODIGO "+
					"      INNER JOIN D_FACTURA_CONTROL_CONTINGENCIA ON D_NOTACRED.COREL=D_FACTURA_CONTROL_CONTINGENCIA.COREL "+
					" WHERE (D_NOTACRED.ANULADO='N') AND (D_NOTACRED.STATCOM='N') AND (D_FACTURA_CONTROL_CONTINGENCIA.TIPODOCUMENTO IN ('04','06')) " +
					" AND (D_NOTACRED.FACTURA NOT IN (SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S')) " +
					" AND (D_NOTACRED.COREL NOT IN (SELECT COREL_REFERENCIA FROM D_NOTACRED WHERE TIPO_DOCUMENTO = 'ND')) " +
					" AND (D_NOTACRED.TIPO_DOCUMENTO = 'NC') " +
					" ORDER BY D_NOTACRED.COREL DESC ";
			}
			    		
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
						vItem.Desc = DT.getString(9) + " - " + DT.getString(1);
						vItem.flag = DT.getInt(4);

						if (DT.getString(5) == null) {
							vItem.Cufe = "";
						} else if (DT.getString(5).equals("null")) {
							vItem.Cufe = "";
						} else {
							vItem.Cufe = DT.getString(5);
						}
						sf=DT.getString(2)+ StringUtils.right("000000" + Integer.toString(DT.getInt(4)), 6);
                        vItem.Numero_Autorizacion=DT.getString(6);
						vItem.Certificada_DGI = (DT.getInt(7)==1?"Si":"No");
						vItem.Estado = DT.getString(8);
					}else if(tipo==1||tipo==6){
						sf=DT.getString(0);
					}else{
						f=DT.getLong(2);sf=du.sfecha(f)+" "+du.shora(f);
					}

					if (tipo==6){
						vItem.Cufe = (DT.getString(4).equals("null") ? "":DT.getString(4));
						vItem.Certificada_DGI = (DT.getInt(6)==1?"Si":"No");
						vItem.Estado = DT.getString(7);
						vItem.CufeFactura = (DT.getString(8) == null ? "": DT.getString(8));
					}

					vItem.Fecha=sf;
					val=DT.getDouble(3);
					try {
						sval=mu.frmcur(val);
					} catch (Exception e) {
						sval=""+val;
					}					
					
					vItem.Valor=sval;
					vItem.bandera=0;
                    if (tipo==0) {
                        if (DT.getString(4).equalsIgnoreCase("S")) vItem.bandera=1;
                    }

					if (tipo==4 || tipo==5) vItem.Valor="";
					
					items.add(vItem);	
			 
					if (id.equalsIgnoreCase(selid)) selidx=vP;
					vP+=1;
			  
					DT.moveToNext();
				}	
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox(e.getMessage());
	    } finally {
			progress.cancel();
		}
			 
		adapter=new ListAdaptCFDVB(this, items);
		listView.setAdapter(adapter);
		
		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}
	    
		listView.setVisibility(View.VISIBLE);
	}
	
	private void anulDocument() {

		try {
			db.beginTransaction();
			
			if (tipo==0) anulPedido(itemid);
			
			if (tipo==1) anulRecib(itemid);
			
			if (tipo==2) {
				getDepTipo();
				if (depparc==0) anulDepos(itemid); else anulDeposParc(itemid);
			}
			
			/*if (tipo==3) {
				if (checkFactDepos()) return;
				anulFactura(itemid);
			}*/
			
			if (tipo==4) anulRecarga(itemid);
			
			if (tipo==5) if (!anulDevol(itemid)) return;

			if (tipo==6) {
				anulNotaCredito(itemid);
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			
			mu.msgbox("El documento ha sido anulado.");

			if(tipo==3) {

				clsDocFactura fdoc;

				fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
				fdoc.deviceid =gl.numSerie;
				fdoc.medidapeso=gl.umpeso;
				fdoc.buildPrint(itemid, 3, "TOL");

				String corelNotaCred=tieneNotaCredFactura(itemid);

				if (!corelNotaCred.isEmpty()){
					prn.printask(printotrodoc);
				}else {
					prn.printask(printclose);
				}


			}else if (tipo==6){

				clsDocDevolucion fdev;

				fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
				fdev.deviceid =gl.numSerie;

				fdev.buildPrint(itemid, 3, "TOL");

				String corelFactura=tieneFacturaNC(itemid);

				if (!corelFactura.isEmpty()){
					prn_nc.printask(printotrodoc, "printnc.txt");
				}else {
					prn_nc.printask(printclose, "printnc.txt");
				}

			}

			//KM120821 Agregué validacion para no eliminar el stock de las canastas
			sql="DELETE FROM P_STOCK WHERE CANT=0 AND CANTM=0 " +
				"AND CODIGO NOT IN(SELECT CODIGO FROM P_PRODUCTO WHERE ES_CANASTA=1)";
			db.execSQL(sql);

			listItems();
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox(e.getMessage());
		}

		cmdAnular.setVisibility(View.VISIBLE);

	}

	private void AnularFactHH_DGI() {

		try {

			db.beginTransaction();

			if (anulFactura(itemid)){

				db.setTransactionSuccessful();
				db.endTransaction();

				if(tipo==3) {

					clsDocFactura fdoc;

					fdoc = new clsDocFactura(this, prn.prw, gl.peMon, gl.peDecImp, "", app.esClienteNuevo(pclicod), gl.codCliNuevo, gl.peModal);
					fdoc.deviceid = gl.numSerie;
					fdoc.medidapeso = gl.umpeso;
					fdoc.buildPrint(itemid, 3, "TOL");

					String corelNotaCred = tieneNotaCredFactura(itemid);

					if (!corelNotaCred.isEmpty()) {
						prn.printask(printotrodoc);
					} else {
						prn.printask(printclose);
					}
				}

				progress.cancel();
				mu.msgbox("Documento anulado.");
				cmdAnular.setVisibility(View.VISIBLE);

			}else{

				db.setTransactionSuccessful();
				db.endTransaction();

			}

			listItems();

		} catch (Exception e) {
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
		}
	}

	private void AnularFactHH_DGI(String corelF) {

		try {

			db.beginTransaction();

			if (anulFactura(corelF)){

				db.setTransactionSuccessful();
				db.endTransaction();

				if(tipo==3 || tipo==6) {

					clsDocFactura fdoc;

					fdoc = new clsDocFactura(this, prn.prw, gl.peMon, gl.peDecImp, "", app.esClienteNuevo(pclicod), gl.codCliNuevo, gl.peModal);
					fdoc.deviceid = gl.numSerie;
					fdoc.medidapeso = gl.umpeso;
					fdoc.buildPrint(corelF, 3, "TOL");

					String corelNotaCred = tieneNotaCredFactura(corelF);

					if (!corelNotaCred.isEmpty()) {
						prn.printask(printotrodoc);
					} else {
						prn.printask(printclose);
					}
				}

				progress.cancel();
				mu.msgbox("Documento anulado.");
				cmdAnular.setVisibility(View.VISIBLE);

			}else{

				db.setTransactionSuccessful();
				db.endTransaction();

			}

			listItems();

		} catch (Exception e) {
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
		}
	}

	private void AnularFactHHConNC() {

		try {

			db.beginTransaction();

		    //if (anulNotaCredito(itemid)){

				//db.setTransactionSuccessful();
				//db.endTransaction();

				clsDocDevolucion fdev;

				fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
				fdev.deviceid =gl.numSerie;

				fdev.buildPrint(itemid, 3, "TOL");

				if (NCRefencia){
					prn_nc.printask(printotrodoc, "printnc.txt");
				}else {
					prn_nc.printask(printclose, "printnc.txt");
				}

				progress.cancel();
				mu.msgbox("El documento ha sido anulado.");

			//}else{

				//db.setTransactionSuccessful();
				//db.endTransaction();

			//}

			listItems();

		} catch (Exception e) {
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
		}
	}

	private void getToken() {

		try {

			String base = Empresa.usuarioApi + ":" + Empresa.claveApi;
			String Credenciales = "Basic "+ encodeToString(base.getBytes(), NO_WRAP);

			ServicioToken client = retrofit.CrearServicio(ServicioToken.class);
			Call<Token> call = client.getToken(Credenciales);

			try {

				Response<Token> response = call.execute();

				if (response.isSuccessful()) {
					token = response.body();
				} else {
					toastlong("Error en respuesta: " + getClass());
				}

			} catch (Exception ex) {
				mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" Error en respuesta "+ ex.getMessage());
			}

		} catch (Exception e) {
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}
	}

	private boolean AnularFacturaDGI() throws Exception {

		try {

			String AuthToken = "Bearer "+ token.getToken();

			AnularDocs client = retrofit.CrearServicio(AnularDocs.class);
			AnularFactura data = new AnularFactura();

			if (!CUFE.isEmpty() && CUFE!=null){
				data.setCufe(CUFE);
			}

			data.setMotivoAnulacion("ANULACION_POR_ERROR");

			Call<ResultadoAnulacion> call = client.AnularFactura(data, AuthToken);

			try {

				Response<ResultadoAnulacion> response = call.execute();

				if (response !=null){

					if (response.isSuccessful()) {

						resultado = response.body();

						//En espera para definir el estado correcto 11 ó 2
						if (resultado.getEstado().equals("2") || response.body().getEstadoEmisionEDOC().equals("2")) {
							exito = true;
						}
						else{
							vError = resultado.getMensajeRespuesta();
						}
					}
				}

			} catch (Exception ex) {
				vError = new Object() {}.getClass().getEnclosingMethod().getName() +" Error en respuesta "+ ex.getMessage();
			}

		} catch (Exception e) {
			vError = new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage();

		}
		return exito;
	}

	private boolean CrearNotaDebito() {

		String corelFactura = "";
		boolean exito = false;

		try {

			//AT20221014 Se Obtiene corel para guardar la nota de debito
			gl.dvcorreld = Catalogo.obtienecorrel("D");
			gl.dvcorelnd = Catalogo.obtienecorrel("ND");

			if (gl.dvcorelnd.equals("")){
				throw new Exception("No está definido correlativo para notas de débito," +
						" no se puede continuar con la anulación de la nota de crédito.");
			}

			int vNroDF = Integer.valueOf(gl.dvcorelnd.substring(3,9));
			String vSerie = StringUtils.right("000" + gl.dvcorelnd.substring(0,3), 3);

			corelFactura=tieneFacturaNC(itemid);

			if (!GuardarNotaDebito(itemid)) {
				throw new Exception("Error al guardar encabezado ND");
			}

			DetalleNT = Catalogo.GetDetalleNT(itemid);

			if (!GuardarDetalleND(DetalleNT)) {
				throw new Exception("Error al guardar detalle ND");
			}

			referencia = corelFactura.isEmpty() ? NotaDebitoEnc.Factura : GetAsignacion(corelFactura);

			if (!GuardarCxC(referencia)) {
				throw new Exception("Error al guardar encabezado CxC");
			}

			if (!GuardarCxCDet(referencia)) {
				throw new Exception("Error al guardar detalle CxCD");
			}

			NotaDebito = new rFE();
			NotaDebito.gDGen.iTpEmis = "01";

			NotaDebito.gDGen.iDoc = "07";

			/*if (corelFactura.isEmpty()) {
				NotaDebito.gDGen.iDoc = "07"; //Tipo de documento //(05:Nota de debito  referente a facturas, 07:Nota de debito genérica )
			} else {
				NotaDebito.gDGen.iDoc = "05";
			}*/

			Sucursal = Catalogo.getSucursal();

			NotaDebito.gDGen.dNroDF = StringUtils.right("0000000000" + (vNroDF), 10); //String.valueOf(vNroDF); //Acá va un número entero 19
			NotaDebito.gDGen.dPtoFacDF = vSerie; //000 003
			NotaDebito.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";
			NotaDebito.gDGen.iNatOp = "01";
			NotaDebito.gDGen.iTipoOp = 1;
			NotaDebito.gDGen.iDest = 1;
			NotaDebito.gDGen.iFormCAFE = 1;
			NotaDebito.gDGen.iEntCAFE = 1;
			NotaDebito.gDGen.dEnvFE = 1;
			NotaDebito.gDGen.iProGen = 2;
			NotaDebito.gDGen.iTipoTranVenta = 1;
			NotaDebito.gDGen.iTipoSuc = 2;
			NotaDebito.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + Cliente.codigo + ";" + Sucursal.sitio_web + ";";

			NotaDebito.gDGen.Emisor.dNombEm = Sucursal.nombre;
			NotaDebito.gDGen.Emisor.dTfnEm = Sucursal.telefono;
			NotaDebito.gDGen.Emisor.dSucEm = Sucursal.codigo;
			NotaDebito.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
			NotaDebito.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
			NotaDebito.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
			NotaDebito.gDGen.Emisor.dDirecEm = Sucursal.direccion;
			NotaDebito.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
			NotaDebito.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
			NotaDebito.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

			clsClasses.clsCiudad ciudad = clsCls.new clsCiudad();
			if (Sucursal.codubi != null) {

				if (!Sucursal.codubi.isEmpty() ){

					ciudad = Catalogo.getCiudad(Sucursal.codubi);

					if (ciudad !=null) {

						NotaDebito.gDGen.Emisor.gUbiEm.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
						NotaDebito.gDGen.Emisor.gUbiEm.dDistr =(ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
						NotaDebito.gDGen.Emisor.gUbiEm.dProv = (ciudad.provincia==null?"":ciudad.provincia.toUpperCase().trim());

						if (ciudad.provincia.isEmpty()) {
							NotaDebito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
						}

					}
				}
			}

			NotaDebito.gDGen.Receptor = new Receptor();
			NotaDebito.gDGen.Receptor.gRucRec = new gRucRec();
			NotaDebito.gDGen.Receptor.gUbiRec = new gUbiRec();
			NotaDebito.gDGen.Receptor.gRucRec.dTipoRuc = Cliente.tipoContribuyente;
			NotaDebito.gDGen.Receptor.iTipoRec = Cliente.tipoRec;
			NotaDebito.gDGen.Receptor.dCorElectRec = Cliente.email;
			NotaDebito.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(Cliente.telefono);
			NotaDebito.gDGen.Receptor.cPaisRec = Cliente.codPais;
			NotaDebito.gDGen.Receptor.dNombRec = Cliente.nombre;
			NotaDebito.gDGen.Receptor.dDirecRec = (Cliente.direccion==null?"":Cliente.direccion.substring(0,(Cliente.direccion.length()>=100?100:Cliente.direccion.length())));
			NotaDebito.gDGen.Receptor.gUbiRec.dCodUbi = (Cliente.ciudad==null?"":Cliente.ciudad);

			if (Cliente.ciudad != null) {

				if (!Cliente.ciudad.isEmpty() ){

					Ciudad = clsCls.new clsCiudad();

					Ciudad = Catalogo.getCiudad(Cliente.ciudad);

					if (Ciudad!=null) {

						NotaDebito.gDGen.Receptor.gUbiRec.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dDistr =(Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

						if (Ciudad.provincia.isEmpty()) {
							NotaDebito.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
						}

					} else {
						if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
						}
					}
				}else {
					if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
						toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
					}
				}
			}else {
				if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
					toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
				}
			}

			// #CKFK20221206 Si el iTipoRec 01:Contribuyente, 02:Consumidor final, 03:Gobierno, 04:Extranjero
			if (NotaDebito.gDGen.Receptor.iTipoRec.equals("01") || NotaDebito.gDGen.Receptor.iTipoRec.equals("03")) {

				if (Cliente.nit.length()>0) {
					String[] DVRuc = Cliente.nit.split(" ");
					if (DVRuc.length > 1) {
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = DVRuc[0].trim();
						if (DVRuc[1].trim().equals("")){
							NotaDebito.gDGen.Receptor.gRucRec.dDV =  StringUtils.right("00" + DVRuc[3].trim(),2);
						}else{
							NotaDebito.gDGen.Receptor.gRucRec.dDV = StringUtils.right("00" + DVRuc[2].trim(),2);
						}
					}else{
						toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
					}
				}else {
					toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
				}
			}else{
				clsClasses.clsRUC BeRUC= Catalogo.getRUC(Cliente.nit);
				if (NotaDebito.gDGen.Receptor.iTipoRec.equals("01") || NotaDebito.gDGen.Receptor.iTipoRec.equals("03")) {

					if(!BeRUC.sRUC.trim().equals("")){
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
					}

				}else{

					if(!BeRUC.sRUC.trim().equals("")){
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
						NotaDebito.gDGen.Receptor.gRucRec.dDV = "";
					}

				}
			}

			int Correlativo = 1;
			double TotalAcumulado = 0;

			for (int i=0; i < DetalleNT.size(); i++) {

				Detalle detalle = new Detalle();

				Producto = clsCls.new clsProducto();
				Producto = Catalogo.getProducto(DetalleNT.get(i).codigoProd);

				detalle.dSecItem = Correlativo;
				detalle.dDescProd = Producto.nombre;
				detalle.dCodProd = Producto.codigo;

				if (!Producto.um.isEmpty()) {

					String CodDGI;

					if (DetalleNT.get(i).porpeso.equals("S")) {
						CodDGI = Catalogo.getUMDGI(DetalleNT.get(i).umpeso);
					} else {
						CodDGI = Catalogo.getUMDGI(Producto.um);
					}

					if (!CodDGI.isEmpty()) {
						detalle.cUnidad = CodDGI;
					} else {
						detalle.cUnidad = gl.unidad_medida_defecto;
					}
				}

				if (DetalleNT.get(i).porpeso.equals("S")) {
					detalle.dCantCodInt = mu.formatTresDecimales(mu.round(Double.valueOf(DetalleNT.get(i).peso), 3));
				} else {
					if (app.esRosty(Producto.codigo)) {
						detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(Double.valueOf(DetalleNT.get(i).cant) * Double.valueOf(DetalleNT.get(i).factor)));
					} else {
						detalle.dCantCodInt = mu.formatDosDecimales(DetalleNT.get(i).cant);
					}
				}

				String TotalItem = mu.formatDosDecimales(mu.round2dec(Double.valueOf(detalle.dCantCodInt) * Double.valueOf(DetalleNT.get(i).precio)));

				if (Producto.subBodega.length() > 1) {
					detalle.dCodCPBSabr = Producto.subBodega.substring(0, 2);
					detalle.dCodCPBScmp = Producto.subBodega;
				}

				detalle.gPrecios.dPrUnit = mu.formatDosDecimales(mu.round2dec(DetalleNT.get(i).precio));
				detalle.gPrecios.dPrUnitDesc = "0.000000";
				detalle.gPrecios.dPrItem = TotalItem;
				detalle.gPrecios.dValTotItem = TotalItem;
				detalle.gITBMSItem.dTasaITBMS = "00";
				detalle.gITBMSItem.dValITBMS = "0.00";

				NotaDebito.Detalles.add(detalle);

				Correlativo++;
				TotalAcumulado += (Double.valueOf(TotalItem));
			}

			String TotalNT = mu.formatDosDecimales(mu.round2dec(TotalAcumulado));

			NotaDebito.gTot.dTotNeto = TotalNT;
			NotaDebito.gTot.dTotITBMS = "0.00";
			NotaDebito.gTot.dTotGravado = "0.00";
			NotaDebito.gTot.dTotDesc = "0.00";
			NotaDebito.gTot.dVTot = TotalNT;
			NotaDebito.gTot.dTotRec = TotalNT;
			NotaDebito.gTot.dNroItems = String.valueOf(NotaDebito.Detalles.size());
			NotaDebito.gTot.dVTotItems = TotalNT;

			gFormaPago PagosNt = new gFormaPago();

			if (Cliente.mediapago == 4) {
				PagosNt.iFormaPago = "01";
				NotaDebito.gTot.iPzPag = "2";

				NotaDebito.gTot.gPagPlazo = new ArrayList();
				gPagPlazo PagoPlazo = new gPagPlazo();
				PagoPlazo.dSecItem = "1";
				PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(Cliente.diascredito);
				PagoPlazo.dValItPlazo = TotalNT;
				PagoPlazo.dInfPagPlazo = null;

				NotaDebito.gTot.gPagPlazo.add(PagoPlazo);

			} else {
				PagosNt.iFormaPago = "02";
				NotaDebito.gTot.iPzPag = "1";
			}

			PagosNt.dVlrCuota = TotalNT;
			NotaDebito.gTot.gFormaPago.add(PagosNt);

			if (!corelFactura.isEmpty() && NotaDebito.gDGen.iDoc.equals("05")) {

				gDFRefNum gDFRefNum= new gDFRefNum();
				gDFRefNum.gDFRefFE = new gDFRefFE();
				gDFRefNum.gDFRefFE.dCUFERef = getCufe(corelFactura);

				Referencia referencia= new Referencia();
				referencia.dFechaDFRef = NotaDebito.gDGen.dFechaEm;
				referencia.dNombEmRef = Sucursal.nombre;
				referencia.gRucEmDFRef = new gRucEmDFRef();
				referencia.gRucEmDFRef.dRuc =  Sucursal.nit;
				referencia.gRucEmDFRef.dTipoRuc = Sucursal.tipoRuc;
				referencia.gRucEmDFRef.dDV = Sucursal.texto;
				referencia.gDFRefNum = gDFRefNum;

				NotaDebito.gDGen.Referencia.add(referencia);
			}

			RespuestaEdoc RespuestaEdocND = new RespuestaEdoc();
			clsClasses.clsControlFEL ControlNotaDebito = clsCls.new clsControlFEL();
			Fimador Firmador = new Fimador(this);
			int EstadoND = 0;

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

					if (RespuestaEdocND != null) {
						if (RespuestaEdocND.Cufe == null) {
							for (int i = 0; i < 2; i++) {
								if (RespuestaEdocND.Cufe == null && !RespuestaEdocND.Estado.equals("15")) {
									RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

									if (RespuestaEdocND.Cufe != null) {
										break;
									}
								} else {
									break;
								}

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

				NotaDebito.gDGen.iTpEmis = "02";
				NotaDebito.gDGen.dMotCont = "Autorización Previa contingencia";
				NotaDebito.gDGen.dFechaCont = NotaDebito.gDGen.dFechaEm;

				RespuestaEdocND = Firmador.EmisionDocumentoBTC(NotaDebito,urlanulacion, "/data/data/com.dts.roadp/"+gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
			}

			if (RespuestaEdocND != null ) {
				ControlNotaDebito.Cufe = (RespuestaEdocND.Cufe == null ? "": RespuestaEdocND.Cufe);
				ControlNotaDebito.Estado = RespuestaEdocND.Estado;
				ControlNotaDebito.Mensaje = RespuestaEdocND.MensajeRespuesta;
				ControlNotaDebito.ValorXml = RespuestaEdocND.XML;
				ControlNotaDebito.QR = RespuestaEdocND.UrlCodeQR;
				ControlNotaDebito.Fecha_Autorizacion = RespuestaEdocND.FechaAutorizacion;
				ControlNotaDebito.Numero_Autorizacion = RespuestaEdocND.NumAutorizacion;
			}

			ControlNotaDebito.TipoDoc = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.NumDoc = NotaDebito.gDGen.dNroDF;
			ControlNotaDebito.Sucursal = gl.sucur;
			ControlNotaDebito.Caja = NotaDebito.gDGen.dPtoFacDF;
			String[] FechaEnv = NotaDebito.gDGen.dFechaEm.split("-05:00", 0);
			ControlNotaDebito.FechaEnvio = FechaEnv[0];
			ControlNotaDebito.TipFac = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.FechaAgr = String.valueOf(du.getFechaCompleta());
			ControlNotaDebito.Corel = gl.dvcorelnd;
			ControlNotaDebito.Ruta = gl.ruta;
			ControlNotaDebito.Vendedor = gl.vend;
			ControlNotaDebito.Correlativo = String.valueOf(NotaDebito.gDGen.dNroDF);

			if (RespuestaEdocND.Estado.equals("2")) {
				EstadoND = 1;
				toastlong("NOTA DE DEBITO CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			} else {
				toastlong("NO SE LOGRÓ CERTIFICAR LA NOTA DE DEBITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			}

			sql="UPDATE D_NOTACRED SET CUFE ='"+RespuestaEdocND.Cufe+"', CERTIFICADA_DGI="+EstadoND+"  WHERE COREL='"+gl.dvcorelnd+"'" +" AND TIPO_DOCUMENTO = 'ND'";
			db.execSQL(sql);

			InsertarFELControl(ControlNotaDebito);

			exito = true;

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - " + e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return exito;
	}

	private boolean CrearNotaDebito(String CorelNC) {

		String corelFactura = "";
		boolean exito = false;

		try {

			//AT20221014 Se Obtiene corel para guardar la nota de debito
			gl.dvcorreld = Catalogo.obtienecorrel("D");
			gl.dvcorelnd = Catalogo.obtienecorrel("ND");

			if (gl.dvcorelnd.equals("")){
				throw new Exception("No está definido correlativo para notas de débito," +
						" no se puede continuar con la anulación de la nota de crédito.");
			}

			int vNroDF = Integer.valueOf(gl.dvcorelnd.substring(3,9));
			String vSerie = StringUtils.right("000" + gl.dvcorelnd.substring(0,3), 3);

			corelFactura=tieneFacturaNC(CorelNC);

			if (!GuardarNotaDebito(CorelNC)) {
				throw new Exception("Error al guardar encabezado ND");
			}

			DetalleNT = Catalogo.GetDetalleNT(CorelNC);

			if (!GuardarDetalleND(DetalleNT)) {
				throw new Exception("Error al guardar detalle ND");
			}

			referencia = corelFactura.isEmpty() ? NotaDebitoEnc.Factura : GetAsignacion(corelFactura);

			if (!GuardarCxC(referencia)) {
				throw new Exception("Error al guardar encabezado CxC");
			}

			if (!GuardarCxCDet(referencia)) {
				throw new Exception("Error al guardar detalle CxCD");
			}

			NotaDebito = new rFE();
			NotaDebito.gDGen.iTpEmis = "01";

			NotaDebito.gDGen.iDoc = "07";

			/*if (corelFactura.isEmpty()) {
				NotaDebito.gDGen.iDoc = "07"; //Tipo de documento //(05:Nota de debito  referente a facturas, 07:Nota de debito genérica )
			} else {
				NotaDebito.gDGen.iDoc = "05";
			}*/

			Sucursal = Catalogo.getSucursal();

			NotaDebito.gDGen.dNroDF = StringUtils.right("0000000000" + (vNroDF), 10); //String.valueOf(vNroDF); //Acá va un número entero 19
			NotaDebito.gDGen.dPtoFacDF = vSerie; //000 003
			NotaDebito.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";
			NotaDebito.gDGen.iNatOp = "01";
			NotaDebito.gDGen.iTipoOp = 1;
			NotaDebito.gDGen.iDest = 1;
			NotaDebito.gDGen.iFormCAFE = 1;
			NotaDebito.gDGen.iEntCAFE = 1;
			NotaDebito.gDGen.dEnvFE = 1;
			NotaDebito.gDGen.iProGen = 2;
			NotaDebito.gDGen.iTipoTranVenta = 1;
			NotaDebito.gDGen.iTipoSuc = 2;
			NotaDebito.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + Cliente.codigo + ";" + Sucursal.sitio_web + ";";

			NotaDebito.gDGen.Emisor.dNombEm = Sucursal.nombre;
			NotaDebito.gDGen.Emisor.dTfnEm = Sucursal.telefono;
			NotaDebito.gDGen.Emisor.dSucEm = Sucursal.codigo;
			NotaDebito.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
			NotaDebito.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
			NotaDebito.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
			NotaDebito.gDGen.Emisor.dDirecEm = Sucursal.direccion;
			NotaDebito.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
			NotaDebito.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
			NotaDebito.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

			clsClasses.clsCiudad ciudad = clsCls.new clsCiudad();
			if (Sucursal.codubi != null) {

				if (!Sucursal.codubi.isEmpty() ){

					ciudad = Catalogo.getCiudad(Sucursal.codubi);

					if (ciudad !=null) {

						NotaDebito.gDGen.Emisor.gUbiEm.dCorreg = (ciudad.corregimiento==null?"":ciudad.corregimiento.toUpperCase().trim());
						NotaDebito.gDGen.Emisor.gUbiEm.dDistr =(ciudad.distrito==null?"":ciudad.distrito.toUpperCase().trim());
						NotaDebito.gDGen.Emisor.gUbiEm.dProv = (ciudad.provincia==null?"":ciudad.provincia.toUpperCase().trim());

						if (ciudad.provincia.isEmpty()) {
							NotaDebito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
						}

					}
				}
			}

			NotaDebito.gDGen.Receptor = new Receptor();
			NotaDebito.gDGen.Receptor.gRucRec = new gRucRec();
			NotaDebito.gDGen.Receptor.gUbiRec = new gUbiRec();
			NotaDebito.gDGen.Receptor.gRucRec.dTipoRuc = Cliente.tipoContribuyente;
			NotaDebito.gDGen.Receptor.iTipoRec = Cliente.tipoRec;
			NotaDebito.gDGen.Receptor.dCorElectRec = Cliente.email;
			NotaDebito.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(Cliente.telefono);
			NotaDebito.gDGen.Receptor.cPaisRec = Cliente.codPais;
			NotaDebito.gDGen.Receptor.dNombRec = Cliente.nombre;
			NotaDebito.gDGen.Receptor.dDirecRec = (Cliente.direccion==null?"":Cliente.direccion.substring(0,(Cliente.direccion.length()>=100?100:Cliente.direccion.length())));
			NotaDebito.gDGen.Receptor.gUbiRec.dCodUbi = (Cliente.ciudad==null?"":Cliente.ciudad);

			if (Cliente.ciudad != null) {

				if (!Cliente.ciudad.isEmpty() ){

					Ciudad = clsCls.new clsCiudad();

					Ciudad = Catalogo.getCiudad(Cliente.ciudad);

					if (Ciudad!=null) {

						NotaDebito.gDGen.Receptor.gUbiRec.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dDistr =(Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

						if (Ciudad.provincia.isEmpty()) {
							NotaDebito.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
						}

					} else {
						if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
						}
					}
				}else {
					if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
						toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
					}
				}
			}else {
				if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
					toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
				}
			}

			// #CKFK20221206 Si el iTipoRec 01:Contribuyente, 02:Consumidor final, 03:Gobierno, 04:Extranjero
			if (NotaDebito.gDGen.Receptor.iTipoRec.equals("01") || NotaDebito.gDGen.Receptor.iTipoRec.equals("03")) {

				if (Cliente.nit.length()>0) {
					String[] DVRuc = Cliente.nit.split(" ");
					if (DVRuc.length > 1) {
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = DVRuc[0].trim();
						if (DVRuc[1].trim().equals("")){
							NotaDebito.gDGen.Receptor.gRucRec.dDV =  StringUtils.right("00" + DVRuc[3].trim(),2);
						}else{
							NotaDebito.gDGen.Receptor.gRucRec.dDV = StringUtils.right("00" + DVRuc[2].trim(),2);
						}
					}else{
						toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
					}
				}else {
					toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
				}
			}else{
				clsClasses.clsRUC BeRUC= Catalogo.getRUC(Cliente.nit);
				if (NotaDebito.gDGen.Receptor.iTipoRec.equals("01") || NotaDebito.gDGen.Receptor.iTipoRec.equals("03")) {

					if(!BeRUC.sRUC.trim().equals("")){
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
					}

				}else{

					if(!BeRUC.sRUC.trim().equals("")){
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
					}else{
						toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
						NotaDebito.gDGen.Receptor.gRucRec.dDV = "";
					}

				}
			}

			int Correlativo = 1;
			double TotalAcumulado = 0;

			for (int i=0; i < DetalleNT.size(); i++) {

				Detalle detalle = new Detalle();

				Producto = clsCls.new clsProducto();
				Producto = Catalogo.getProducto(DetalleNT.get(i).codigoProd);

				detalle.dSecItem = Correlativo;
				detalle.dDescProd = Producto.nombre;
				detalle.dCodProd = Producto.codigo;

				if (!Producto.um.isEmpty()) {

					String CodDGI;

					if (DetalleNT.get(i).porpeso.equals("S")) {
						CodDGI = Catalogo.getUMDGI(DetalleNT.get(i).umpeso);
					} else {
						CodDGI = Catalogo.getUMDGI(Producto.um);
					}

					if (!CodDGI.isEmpty()) {
						detalle.cUnidad = CodDGI;
					} else {
						detalle.cUnidad = gl.unidad_medida_defecto;
					}
				}

				if (DetalleNT.get(i).porpeso.equals("S")) {
					detalle.dCantCodInt = mu.formatTresDecimales(mu.round(Double.valueOf(DetalleNT.get(i).peso), 3));
				} else {
					if (app.esRosty(Producto.codigo)) {
						detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(Double.valueOf(DetalleNT.get(i).cant) * Double.valueOf(DetalleNT.get(i).factor)));
					} else {
						detalle.dCantCodInt = mu.formatDosDecimales(DetalleNT.get(i).cant);
					}
				}

				String TotalItem = mu.formatDosDecimales(mu.round2dec(Double.valueOf(detalle.dCantCodInt) * Double.valueOf(DetalleNT.get(i).precio)));

				if (Producto.subBodega.length() > 1) {
					detalle.dCodCPBSabr = Producto.subBodega.substring(0, 2);
					detalle.dCodCPBScmp = Producto.subBodega;
				}

				detalle.gPrecios.dPrUnit = mu.formatDosDecimales(mu.round2dec(DetalleNT.get(i).precio));
				detalle.gPrecios.dPrUnitDesc = "0.000000";
				detalle.gPrecios.dPrItem = TotalItem;
				detalle.gPrecios.dValTotItem = TotalItem;
				detalle.gITBMSItem.dTasaITBMS = "00";
				detalle.gITBMSItem.dValITBMS = "0.00";

				NotaDebito.Detalles.add(detalle);

				Correlativo++;
				TotalAcumulado += (Double.valueOf(TotalItem));
			}

			String TotalNT = mu.formatDosDecimales(mu.round2dec(TotalAcumulado));

			NotaDebito.gTot.dTotNeto = TotalNT;
			NotaDebito.gTot.dTotITBMS = "0.00";
			NotaDebito.gTot.dTotGravado = "0.00";
			NotaDebito.gTot.dTotDesc = "0.00";
			NotaDebito.gTot.dVTot = TotalNT;
			NotaDebito.gTot.dTotRec = TotalNT;
			NotaDebito.gTot.dNroItems = String.valueOf(NotaDebito.Detalles.size());
			NotaDebito.gTot.dVTotItems = TotalNT;

			gFormaPago PagosNt = new gFormaPago();

			if (Cliente.mediapago == 4) {
				PagosNt.iFormaPago = "01";
				NotaDebito.gTot.iPzPag = "2";

				NotaDebito.gTot.gPagPlazo = new ArrayList();
				gPagPlazo PagoPlazo = new gPagPlazo();
				PagoPlazo.dSecItem = "1";
				PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(Cliente.diascredito);
				PagoPlazo.dValItPlazo = TotalNT;
				PagoPlazo.dInfPagPlazo = null;

				NotaDebito.gTot.gPagPlazo.add(PagoPlazo);

			} else {
				PagosNt.iFormaPago = "02";
				NotaDebito.gTot.iPzPag = "1";
			}

			PagosNt.dVlrCuota = TotalNT;
			NotaDebito.gTot.gFormaPago.add(PagosNt);

			if (!corelFactura.isEmpty() && NotaDebito.gDGen.iDoc.equals("05")) {

				gDFRefNum gDFRefNum= new gDFRefNum();
				gDFRefNum.gDFRefFE = new gDFRefFE();
				gDFRefNum.gDFRefFE.dCUFERef = getCufe(corelFactura);

				Referencia referencia= new Referencia();
				referencia.dFechaDFRef = NotaDebito.gDGen.dFechaEm;
				referencia.dNombEmRef = Sucursal.nombre;
				referencia.gRucEmDFRef = new gRucEmDFRef();
				referencia.gRucEmDFRef.dRuc =  Sucursal.nit;
				referencia.gRucEmDFRef.dTipoRuc = Sucursal.tipoRuc;
				referencia.gRucEmDFRef.dDV = Sucursal.texto;
				referencia.gDFRefNum = gDFRefNum;

				NotaDebito.gDGen.Referencia.add(referencia);
			}

			RespuestaEdoc RespuestaEdocND = new RespuestaEdoc();
			clsClasses.clsControlFEL ControlNotaDebito = clsCls.new clsControlFEL();
			Fimador Firmador = new Fimador(this);
			int EstadoND = 0;

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

					if (RespuestaEdocND != null) {
						if (RespuestaEdocND.Cufe == null) {
							for (int i = 0; i < 2; i++) {
								if (RespuestaEdocND.Cufe == null && !RespuestaEdocND.Estado.equals("15")) {
									RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

									if (RespuestaEdocND.Cufe != null) {
										break;
									}
								} else {
									break;
								}

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

				NotaDebito.gDGen.iTpEmis = "02";
				NotaDebito.gDGen.dMotCont = "Autorización Previa contingencia";
				NotaDebito.gDGen.dFechaCont = NotaDebito.gDGen.dFechaEm;

				RespuestaEdocND = Firmador.EmisionDocumentoBTC(NotaDebito,urlanulacion, "/data/data/com.dts.roadp/"+gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
			}

			if (RespuestaEdocND != null ) {
				ControlNotaDebito.Cufe = (RespuestaEdocND.Cufe == null ? "": RespuestaEdocND.Cufe);
				ControlNotaDebito.Estado = RespuestaEdocND.Estado;
				ControlNotaDebito.Mensaje = RespuestaEdocND.MensajeRespuesta;
				ControlNotaDebito.ValorXml = RespuestaEdocND.XML;
				ControlNotaDebito.QR = RespuestaEdocND.UrlCodeQR;
				ControlNotaDebito.Fecha_Autorizacion = RespuestaEdocND.FechaAutorizacion;
				ControlNotaDebito.Numero_Autorizacion = RespuestaEdocND.NumAutorizacion;
			}

			ControlNotaDebito.TipoDoc = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.NumDoc = NotaDebito.gDGen.dNroDF;
			ControlNotaDebito.Sucursal = gl.sucur;
			ControlNotaDebito.Caja = NotaDebito.gDGen.dPtoFacDF;
			String[] FechaEnv = NotaDebito.gDGen.dFechaEm.split("-05:00", 0);
			ControlNotaDebito.FechaEnvio = FechaEnv[0];
			ControlNotaDebito.TipFac = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.FechaAgr = String.valueOf(du.getFechaCompleta());
			ControlNotaDebito.Corel = gl.dvcorelnd;
			ControlNotaDebito.Ruta = gl.ruta;
			ControlNotaDebito.Vendedor = gl.vend;
			ControlNotaDebito.Correlativo = String.valueOf(NotaDebito.gDGen.dNroDF);

			if (RespuestaEdocND.Estado.equals("2")) {
				EstadoND = 1;
				toastlong("NOTA DE DEBITO CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			} else {
				toastlong("NO SE LOGRÓ CERTIFICAR LA NOTA DE DEBITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			}

			sql="UPDATE D_NOTACRED SET CUFE ='"+RespuestaEdocND.Cufe+"', CERTIFICADA_DGI="+EstadoND+"  WHERE COREL='"+gl.dvcorelnd+"'" +" AND TIPO_DOCUMENTO = 'ND'";
			db.execSQL(sql);

			InsertarFELControl(ControlNotaDebito);

			exito = true;

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - " + e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return exito;
	}

	//#CKFK20230331 Aquí se genera la nota de débito de la nota de crédito
	private boolean AnularNotaCreditoConFactura(String CorelNC, String Factura) {

		try {

			gl.dvcorreld = Catalogo.obtienecorrel("D");
			gl.dvcorelnd = Catalogo.obtienecorrel("ND");

			int vNroDF = Integer.valueOf(gl.dvcorelnd.substring(3,9));
			String vSerie = StringUtils.right("000" + gl.dvcorelnd.substring(0,3), 3);

			if (!GuardarNotaDebito(CorelNC)) {
				throw new Exception("Error al guardar encabezado ND");
			}

			DetalleNT = Catalogo.GetDetalleNT(CorelNC);

			if (!GuardarDetalleND(DetalleNT)) {
				throw new Exception("Error al guardar detalle ND");
			}

			referencia = GetAsignacion(Factura);

			if (!GuardarCxC(referencia)) {
				throw new Exception("Error al guardar encabezado CxC");
			}

			if (!GuardarCxCDet(referencia)) {
				throw new Exception("Error al guardar detalle CxCD");
			}

			Sucursal = Catalogo.getSucursal();

			NotaDebito = new rFE();
			NotaDebito.gDGen.iTpEmis = "01";
			NotaDebito.gDGen.iDoc = "07"; //Tipo de documento //(05:Nota de debito  referente a facturas, 07:Nota de debito genérica )
			NotaDebito.gDGen.dNroDF = StringUtils.right("0000000000" + (vNroDF), 10); //String.valueOf(vNroDF); //Acá va un número entero 19
			NotaDebito.gDGen.dPtoFacDF = vSerie; //000 003
			NotaDebito.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";
			NotaDebito.gDGen.iNatOp = "01";
			NotaDebito.gDGen.iTipoOp = 1;
			NotaDebito.gDGen.iDest = 1;
			NotaDebito.gDGen.iFormCAFE = 1;
			NotaDebito.gDGen.iEntCAFE = 1;
			NotaDebito.gDGen.dEnvFE = 1;
			NotaDebito.gDGen.iProGen = 2;
			NotaDebito.gDGen.iTipoTranVenta = 1;
			NotaDebito.gDGen.iTipoSuc = 2;
			NotaDebito.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + Cliente.codigo + ";" + Sucursal.sitio_web + ";";

			Sucursal = Catalogo.getSucursal();

			NotaDebito.gDGen.Emisor.dNombEm = Sucursal.nombre;
			NotaDebito.gDGen.Emisor.dTfnEm = Sucursal.telefono;
			NotaDebito.gDGen.Emisor.dSucEm = Sucursal.codigo;
			NotaDebito.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
			NotaDebito.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
			NotaDebito.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
			NotaDebito.gDGen.Emisor.dDirecEm = Sucursal.direccion;
			NotaDebito.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
			NotaDebito.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
			NotaDebito.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

			if (!Sucursal.codMuni.isEmpty() || Sucursal.codMuni != null) {
				Municipio = clsCls.new clsMunicipio();
				Departamento = clsCls.new clsDepartamento();

				Municipio = Catalogo.getMunicipio(Sucursal.codMuni);
				Departamento = Catalogo.getDepartamento(Municipio.depar);

				if (Municipio.nombre.contains("/")) {

					String[] DireccionCompleta = Municipio.nombre.split("/");

					NotaDebito.gDGen.Emisor.gUbiEm.dCorreg = DireccionCompleta[1].trim().toUpperCase();
					NotaDebito.gDGen.Emisor.gUbiEm.dDistr = DireccionCompleta[0].trim().toUpperCase();

					if (!Departamento.nombre.isEmpty()) {
						NotaDebito.gDGen.Emisor.gUbiEm.dProv = Departamento.nombre.toUpperCase();
					} else {
						NotaDebito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
					}

				} else {
					toastlongd("El nombre del corregimiento y distrito está mal formado para el código de municipio:" + Municipio.codigo);
				}

			}

			NotaDebito.gDGen.Receptor = new Receptor();
			NotaDebito.gDGen.Receptor.gRucRec = new gRucRec();
			NotaDebito.gDGen.Receptor.gUbiRec = new gUbiRec();
			NotaDebito.gDGen.Receptor.gRucRec.dTipoRuc = Cliente.tipoContribuyente;
			NotaDebito.gDGen.Receptor.iTipoRec = Cliente.tipoRec;
			NotaDebito.gDGen.Receptor.dCorElectRec = Cliente.email;
			NotaDebito.gDGen.Receptor.dTfnRec = Catalogo.ValidaTelefono(Cliente.telefono);
			NotaDebito.gDGen.Receptor.cPaisRec = Cliente.codPais;
			NotaDebito.gDGen.Receptor.dNombRec = Cliente.nombre;
			NotaDebito.gDGen.Receptor.dDirecRec = (Cliente.direccion==null?"":Cliente.direccion.substring(0,(Cliente.direccion.length()>=100?100:Cliente.direccion.length())));
			NotaDebito.gDGen.Receptor.gUbiRec.dCodUbi = (Cliente.ciudad==null?"":Cliente.ciudad);

			if (Cliente.ciudad != null) {

				if (!Cliente.ciudad.isEmpty() ){

					Ciudad = clsCls.new clsCiudad();

					Ciudad = Catalogo.getCiudad(Cliente.ciudad);

					if (Ciudad!=null) {

						NotaDebito.gDGen.Receptor.gUbiRec.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dDistr = (Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
						NotaDebito.gDGen.Receptor.gUbiRec.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

						if (Ciudad.provincia.isEmpty()) {
							NotaDebito.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
						}

					} else {
						if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
							toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
						}
					}
				}else {
					if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
						toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
					}
				}
			}else {
				if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
					toastlongd("La ubicación del cliente está vacía Cliente:" + Cliente.nombre);
				}
			}

			clsClasses.clsRUC BeRUC= Catalogo.getRUC(Cliente.nit);
			if (NotaDebito.gDGen.Receptor.iTipoRec.equals("01") || NotaDebito.gDGen.Receptor.iTipoRec.equals("03")) {

				if(!BeRUC.sRUC.trim().equals("")){
					NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
				}else{
					toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
				}

				if (!BeRUC.sDV.trim().equals("")) {
					NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
				} else {
					toastlongd(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
				}

			}else{

				if(!BeRUC.sRUC.trim().equals("")){
					NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC.trim();
				}else{
					toastlongd("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
				}

				if (!BeRUC.sDV.trim().equals("")) {
					NotaDebito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
				} else {
					NotaDebito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
					NotaDebito.gDGen.Receptor.gRucRec.dDV = "";
				}

			}

			int Correlativo = 1;
			double TotalAcumulado = 0;
			for (int i=0; i < DetalleNT.size(); i++) {
				Detalle detalle = new Detalle();

				Producto = clsCls.new clsProducto();
				Producto = Catalogo.getProducto(DetalleNT.get(i).codigoProd);

				detalle.dSecItem = Correlativo;
				detalle.dDescProd = Producto.nombre;
				detalle.dCodProd = Producto.codigo;

				if (!Producto.um.isEmpty()) {
					String CodDGI;

					if (DetalleNT.get(i).porpeso.equals("S")) {
						CodDGI = Catalogo.getUMDGI(DetalleNT.get(i).umpeso);
					} else {
						CodDGI = Catalogo.getUMDGI(Producto.um);
					}

					if (!CodDGI.isEmpty()) {
						detalle.cUnidad = CodDGI;
					} else {
						detalle.cUnidad = gl.unidad_medida_defecto;
					}
				}

				if (DetalleNT.get(i).porpeso.equals("S")) {
					detalle.dCantCodInt = mu.formatTresDecimales(mu.round(Double.valueOf(DetalleNT.get(i).peso), 3));
				} else {
					if (app.esRosty(Producto.codigo)) {
						detalle.dCantCodInt = mu.formatDosDecimales(mu.round2dec(Double.valueOf(DetalleNT.get(i).cant) * Double.valueOf(DetalleNT.get(i).factor)));
					} else {
						detalle.dCantCodInt = mu.formatDosDecimales(DetalleNT.get(i).cant);
					}
				}

				String TotalItem = mu.formatDosDecimales(mu.round2dec(Double.valueOf(detalle.dCantCodInt) * Double.valueOf(DetalleNT.get(i).precio)));

				if (Producto.subBodega.length() > 1) {
					detalle.dCodCPBSabr = Producto.subBodega.substring(0, 2);
					detalle.dCodCPBScmp = Producto.subBodega;
				} else {
					//throw new Exception("No tiene definido la familia del producto por la DGI");
				}

				detalle.gPrecios.dPrUnit = mu.formatDosDecimales(mu.round2dec(DetalleNT.get(i).precio));
				detalle.gPrecios.dPrUnitDesc = "0.000000";
				detalle.gPrecios.dPrItem = TotalItem;
				detalle.gPrecios.dValTotItem = TotalItem;
				detalle.gITBMSItem.dTasaITBMS = "00";
				detalle.gITBMSItem.dValITBMS = "0.00";

				NotaDebito.Detalles.add(detalle);

				Correlativo++;
				TotalAcumulado += Double.valueOf(TotalItem);
			}

			String TotalNT = mu.formatDosDecimales(mu.round2dec(TotalAcumulado));

			NotaDebito.gTot.dTotNeto = TotalNT;
			NotaDebito.gTot.dTotITBMS = "0.00";
			NotaDebito.gTot.dTotGravado = "0.00";
			NotaDebito.gTot.dTotDesc = "0.00";
			NotaDebito.gTot.dVTot = TotalNT;
			NotaDebito.gTot.dTotRec = TotalNT;
			NotaDebito.gTot.dNroItems = String.valueOf(NotaDebito.Detalles.size());
			NotaDebito.gTot.dVTotItems = TotalNT;

			gFormaPago PagosNt = new gFormaPago();

			if (Cliente.mediapago == 4) {
				PagosNt.iFormaPago = "01";
				NotaDebito.gTot.iPzPag = "2";

				NotaDebito.gTot.gPagPlazo = new ArrayList();
				gPagPlazo PagoPlazo = new gPagPlazo();
				PagoPlazo.dSecItem = "1";
				PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(Cliente.diascredito);
				PagoPlazo.dValItPlazo = TotalNT;
				PagoPlazo.dInfPagPlazo = null;

				NotaDebito.gTot.gPagPlazo.add(PagoPlazo);
			} else {
				PagosNt.iFormaPago = "02";
				NotaDebito.gTot.iPzPag = "1";
			}

			PagosNt.dVlrCuota = TotalNT;
			NotaDebito.gTot.gFormaPago.add(PagosNt);

			if (!Factura.isEmpty() && NotaDebito.gDGen.iDoc.equals("05")) {

				gDFRefNum gDFRefNum= new gDFRefNum();
				gDFRefNum.gDFRefFE = new gDFRefFE();
				gDFRefNum.gDFRefFE.dCUFERef = getCufe(Factura);

				Referencia referencia= new Referencia();
				referencia.dFechaDFRef = NotaDebito.gDGen.dFechaEm;
				referencia.dNombEmRef = Sucursal.nombre;
				referencia.gRucEmDFRef = new gRucEmDFRef();
				referencia.gRucEmDFRef.dRuc =  Sucursal.nit;
				referencia.gRucEmDFRef.dTipoRuc = Sucursal.tipoRuc;
				referencia.gRucEmDFRef.dDV = Sucursal.texto;
				referencia.gDFRefNum = gDFRefNum;

				NotaDebito.gDGen.Referencia.add(referencia);
			}

			RespuestaEdoc RespuestaEdocND = new RespuestaEdoc();
			clsClasses.clsControlFEL ControlNotaDebito = clsCls.new clsControlFEL();
			Fimador Firmador = new Fimador(this);
			int EstadoND = 0;

			if (ConexionValida()) {
				//#AT20230309 Intenta certificar 3 veces
				try {
					RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

					if (RespuestaEdocND.Cufe == null) {
						for (int i = 0; i < 2; i++) {
							if (RespuestaEdocND.Cufe == null && !RespuestaEdocND.Estado.equals("15")) {
								RespuestaEdocND = Firmador.EmisionDocumentoBTBTimeOut(5000,NotaDebito, urltoken, usuario, clave, urlDoc, gl.ambiente);

								if (RespuestaEdocND.Cufe != null) {
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

				NotaDebito.gDGen.iTpEmis = "02";
				NotaDebito.gDGen.dMotCont = "Autorización Previa contingencia";
				NotaDebito.gDGen.dFechaCont = NotaDebito.gDGen.dFechaEm;

				RespuestaEdocND = Firmador.EmisionDocumentoBTC(NotaDebito,urlanulacion, "/data/data/com.dts.roadp/"+gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
			}

			if (RespuestaEdocND != null ) {
				ControlNotaDebito.Cufe = (RespuestaEdocND.Cufe == null ? "": RespuestaEdocND.Cufe);
				ControlNotaDebito.Estado = RespuestaEdocND.Estado;
				ControlNotaDebito.Mensaje = RespuestaEdocND.MensajeRespuesta;
				ControlNotaDebito.ValorXml = RespuestaEdocND.XML;
				ControlNotaDebito.QR = RespuestaEdocND.UrlCodeQR;
				ControlNotaDebito.Fecha_Autorizacion = RespuestaEdocND.FechaAutorizacion;
				ControlNotaDebito.Numero_Autorizacion = RespuestaEdocND.NumAutorizacion;
			}

			ControlNotaDebito.TipoDoc = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.NumDoc = NotaDebito.gDGen.dNroDF;
			ControlNotaDebito.Sucursal = gl.sucur;
			ControlNotaDebito.Caja = NotaDebito.gDGen.dPtoFacDF;
			String[] FechaEnv = NotaDebito.gDGen.dFechaEm.split("-05:00", 0);
			ControlNotaDebito.FechaEnvio = FechaEnv[0];
			ControlNotaDebito.TipFac = NotaDebito.gDGen.iDoc;
			ControlNotaDebito.FechaAgr = String.valueOf(du.getFechaCompleta());
			ControlNotaDebito.Corel = gl.dvcorelnd;
			ControlNotaDebito.Ruta = gl.ruta;
			ControlNotaDebito.Vendedor = gl.vend;
			ControlNotaDebito.Correlativo = String.valueOf(NotaDebito.gDGen.dNroDF);

			if (RespuestaEdocND.Estado.equals("2")) {
				EstadoND = 1;
				toastlong("NOTA DE DEBITO CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			} else {
				toastlong("NO SE LOGRÓ CERTIFICAR LA NOTA DE DEBITO -- " + " ESTADO: " + RespuestaEdocND.Estado + " - " + RespuestaEdocND.MensajeRespuesta);
			}

			sql="UPDATE D_NOTACRED SET CUFE ='"+RespuestaEdocND.Cufe+"', CERTIFICADA_DGI="+EstadoND+"  WHERE COREL='"+gl.dvcorelnd+"'" +" AND TIPO_DOCUMENTO = 'ND'";
			db.execSQL(sql);

			InsertarFELControl(ControlNotaDebito);

			exito_anula_nc = true;

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
			progress.cancel();
		} catch (Throwable e) {
			progress.cancel();
			throw new RuntimeException(e);
		}

		return exito_anula_nc;
	}

	public void InsertarFELControl(clsClasses.clsControlFEL ItemFEL) {
		Cursor dt;
		String vFechaAutorizacion = "";
		try {

			sql = "SELECT MAX(IdTablaControl) FROM D_FACTURA_CONTROL_CONTINGENCIA";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			ItemFEL.Id = dt.getInt(0) + 1;

			ins.init("D_FACTURA_CONTROL_CONTINGENCIA");

			ins.add("IdTablaControl", ItemFEL.Id);
			ins.add("Cufe", ItemFEL.Cufe);
			ins.add("TipoDocumento", ItemFEL.TipoDoc);
			ins.add("NumeroDocumento", ItemFEL.NumDoc);
			ins.add("Sucursal", ItemFEL.Sucursal);
			ins.add("Caja", ItemFEL.Caja);
			ins.add("Estado", ItemFEL.Estado);
			ins.add("Mensaje", ItemFEL.Mensaje);
			ins.add("Valor_XML", ItemFEL.ValorXml);
			ins.add("FechaEnvio", ItemFEL.FechaEnvio);
			ins.add("TipoFactura", ItemFEL.TipFac);
			ins.add("Fecha_Agr", ItemFEL.FechaAgr);
			ins.add("QR", ItemFEL.QR);
			ins.add("COREL", ItemFEL.Corel);
			ins.add("RUTA", ItemFEL.Ruta);
			ins.add("VENDEDOR", ItemFEL.Vendedor);
			ins.add("HOST", ItemFEL.Host);
			ins.add("CODIGOLIQUIDACION", ItemFEL.CodLiquidacion);
			ins.add("CORELATIVO", ItemFEL.Correlativo);
			ins.add("QRIMAGE", ItemFEL.QRImg);

			if (ItemFEL.Fecha_Autorizacion!=null && ItemFEL.Fecha_Autorizacion.length() > 0){
				vFechaAutorizacion = ItemFEL.Fecha_Autorizacion.equals("0001-01-01T00:00:00")?
						"1900-01-01T00:00:00":
						ItemFEL.Fecha_Autorizacion.toString().substring(0,ItemFEL.Fecha_Autorizacion.length()-6);
			} else {
				vFechaAutorizacion = "1900-01-01T00:00:00";
			}

			ins.add("FECHA_AUTORIZACION", vFechaAutorizacion);
			ins.add("NUMERO_AUTORIZACION", ItemFEL.Numero_Autorizacion);

			db.execSQL(ins.sql());

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}
	}

	private boolean GuardarDetalleND(ArrayList<clsClasses.clsBeNotaCreditoDet>  detalle) {
		boolean exito = false;
		try {

			for (int i = 0; i < detalle.size(); i++) {
				ins.init("D_NOTACREDD");

				ins.add("COREL", gl.dvcorelnd);
				ins.add("PRODUCTO", detalle.get(i).codigoProd);
				ins.add("PRECIO_ORIG", detalle.get(i).precio);
				ins.add("PRECIO_ACT",detalle.get(i).precioAct);
				ins.add("CANT", detalle.get(i).cant);
				ins.add("PESO", detalle.get(i).peso);
				ins.add("POR_PESO", detalle.get(i).porpeso);
				ins.add("UMVENTA", detalle.get(i).umVenta);
				ins.add("UMSTOCK", detalle.get(i).umStock);
				ins.add("UMPESO", detalle.get(i).umpeso);
				ins.add("FACTOR", detalle.get(i).factor);
				ins.add("TIPO_DOCUMENTO", "ND");

				db.execSQL(ins.sql());
			}

			exito = true;

		} catch	(Exception e) {
			mu.msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
			exito = false;
		}

		return exito;
	}

	private boolean GuardarCxC(String FacturaRef) {
		boolean exito = false;
		Cursor DT;
		try {
			sql = "SELECT * FROM D_CxC WHERE COREL = '"+FacturaRef+"'";
			DT=Con.OpenDT(sql);
			if(DT.getCount()>0) {
				DT.moveToFirst();

				ins.init("D_CxC");

				ins.add("COREL", gl.dvcorreld);
				ins.add("RUTA", DT.getString(1));
				ins.add("CLIENTE", DT.getString(2));
				ins.add("FECHA", DT.getLong(3));
				ins.add("ANULADO", "N");
				ins.add("EMPRESA", gl.emp);
				ins.add("TIPO", DT.getString(6));
				ins.add("REFERENCIA", DT.getString(7));
				ins.add("IMPRES", 0);
				ins.add("STATCOM", "N");
				ins.add("VENDEDOR", gl.vend);
				ins.add("TOTAL", DT.getDouble(11));
				ins.add("SUPERVISOR", DT.getString(12));
				ins.add("AYUDANTE", DT.getString(13));
				ins.add("CODIGOLIQUIDACION", 0);
				ins.add("ESTADO", DT.getString(15));

				db.execSQL(ins.sql());
			}

			if (DT != null) DT.close();

			sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactuald+" WHERE RUTA='"+gl.ruta+"' AND TIPO='D'";
			db.execSQL(sql);

			ins.init("D_NOTACRED_LOG");
			ins.add("ITEM",ncItem);
			ins.add("SERIE",gl.dvSeried);
			ins.add("COREL",gl.dvactuald);
			ins.add("FECHA",du.getActDateTime());
			ins.add("RUTA",gl.ruta);
			ins.add("TIPO","D");
			db.execSQL(ins.sql());

			ncItem +=1;

			exito = true;

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return exito;
	}

	private boolean GuardarCxCDet(String FacturaRef) {
		boolean exito = false;
		Cursor DT;
		try {
			sql = "SELECT * FROM D_CxCD WHERE COREL = '"+FacturaRef+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {
				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					ins.init("D_CxCD");

					ins.add("COREL",gl.dvcorreld);
					ins.add("ITEM",DT.getInt(1));
					ins.add("CODIGO",DT.getString(2));
					ins.add("CANT",DT.getDouble(3));
					ins.add("CODDEV",DT.getString(4));
					ins.add("ESTADO",DT.getString(5));
					ins.add("TOTAL",DT.getDouble(6));
					ins.add("PRECIO",DT.getDouble(7));
					ins.add("PRECLISTA",DT.getDouble(8));
					ins.add("REF",DT.getString(9));
					ins.add("PESO",DT.getDouble(10));
					ins.add("FECHA_CAD",DT.getInt(11));
					ins.add("LOTE",DT.getString(12));
					ins.add("UMVENTA",DT.getString(13));
					ins.add("UMSTOCK",DT.getString(14));
					ins.add("UMPESO",DT.getString(15));
					ins.add("FACTOR",DT.getDouble(16));

					db.execSQL(ins.sql());

					DT.moveToNext();
				}
			}

			if (DT != null) DT.close();

			exito = true;

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return exito;
	}

	private String GetAsignacion(String FactCorel) {
		String asignacion = "";
		Cursor DT;
		try {

			sql = "SELECT ASIGNACION FROM D_FACTURA WHERE COREL = '"+FactCorel+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {
				DT.moveToFirst();

				asignacion = DT.getString(0);
			}

			if (DT!=null) DT.close();

		} catch (Exception e) {
			mu.msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return asignacion;
	}

	private boolean GuardarNotaDebito(String CorelNC) {
		boolean exito = false;
		Cursor DT;

		try {

			if (CorelNC!=null){
				//AT20221014 Se Obtiene Encabezado NT para la nota DB
				NotaDebitoEnc =  Catalogo.GetEncNotaCredito(CorelNC);
				NotaDebitoEnc.EsAnulacion = 1;
				NotaDebitoEnc.TipoDocumento = "ND";

			/*if (!CufeFactura.isEmpty()) {
				NotaDebitoEnc.CufeFactura = CufeFactura;
			}*/

				ins.init("D_NOTACRED");

				ins.add("COREL",gl.dvcorelnd);
				ins.add("ANULADO","N");
				ins.add("FECHA",NotaDebitoEnc.Fecha);
				ins.add("RUTA", NotaDebitoEnc.Ruta);
				ins.add("VENDEDOR",NotaDebitoEnc.Vendedor);
				ins.add("CLIENTE", NotaDebitoEnc.Cliente);
				ins.add("TOTAL", NotaDebitoEnc.Total);
				ins.add("FACTURA",gl.dvcorreld);
				ins.add("SERIE", NotaDebitoEnc.Serie);
				ins.add("CORELATIVO",gl.dvactualnd);
				ins.add("STATCOM",NotaDebitoEnc.Statcom);
				ins.add("CODIGOLIQUIDACION",NotaDebitoEnc.CodigoLiquidacion);
				ins.add("RESOLNC",NotaDebitoEnc.ResolNC);
				ins.add("SERIEFACT",NotaDebitoEnc.SerieFact);
				ins.add("CORELFACT",NotaDebitoEnc.CorelFact);
				ins.add("IMPRES",NotaDebitoEnc.Impres);
				ins.add("CERTIFICADA_DGI", 0);
				ins.add("TIPO_DOCUMENTO", NotaDebitoEnc.TipoDocumento);
				ins.add("COREL_REFERENCIA", NotaDebitoEnc.Corel);
				ins.add("ES_ANULACION", NotaDebitoEnc.EsAnulacion);
				ins.add("CUFE_FACTURA", NotaDebitoEnc.CufeFactura);

				db.execSQL(ins.sql());

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactualnd+" WHERE RUTA='"+gl.ruta+"' AND TIPO='ND'";
				db.execSQL(sql);

				ins.init("D_NOTACRED_LOG");
				ins.add("ITEM",ncItem);
				ins.add("SERIE",gl.dvSeriend);
				ins.add("COREL",gl.dvactualnd);
				ins.add("FECHA",du.getActDateTime());
				ins.add("RUTA",gl.ruta);
				ins.add("TIPO","ND");
				db.execSQL(ins.sql());

				ncItem +=1;

				exito = true;

			}

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return exito;
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

	//endregion

	//region Documents

    private void anulPedido(String itemid) {
        Cursor dt;
        double dcant,dpeso,dfact;
        String prid;

        try {
            db.beginTransaction();

            sql="UPDATE D_PEDIDO SET Anulado='S' WHERE COREL='"+itemid+"'";
            db.execSQL(sql);

            sql="UPDATE D_PEDIDOD SET Anulado='S' WHERE COREL='"+itemid+"'";
            db.execSQL(sql);

            sql = "UPDATE D_NOTACRED SET ANULADO ='S' WHERE FACTURA='" + itemid + "'";
            db.execSQL(sql);

            sql = "UPDATE D_CXC SET ANULADO ='S' WHERE REFERENCIA='" + itemid + "'";
            db.execSQL(sql);

            if (toledano) {

                sql="SELECT PRODUCTO,CANTPROC,PESO,FACTOR FROM D_PEDIDOD WHERE COREL='"+itemid+"'";
                dt=Con.OpenDT(sql);

                dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    prid=dt.getString(0);
                    dcant=dt.getDouble(1);
                    dpeso=dt.getDouble(2);
                    //dfact=dt.getDouble(3);
                    dfact=1;

                    if (dcant>0) {
                        sql="UPDATE P_STOCK_PV SET CANT=CANT+"+dcant*dfact+",PESO=PESO+"+dpeso+" WHERE (CODIGO='"+prid+"') ";
                        db.execSQL(sql);
                    }

                    dt.moveToNext();
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            db.endTransaction();
            msgbox(e.getMessage());
        }

		cmdAnular.setVisibility(View.VISIBLE);

	}
	
	private boolean anulFactura(String itemid) {

		Cursor dt;
		String prod,um,ncred;

		boolean vAnulFactura=false;

		try{

			sql="SELECT PRODUCTO,UMSTOCK FROM D_FACTURAD WHERE Corel='"+itemid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){

				dt.moveToFirst();

				while (!dt.isAfterLast()) {

					prod=dt.getString(0);
					um=dt.getString(1);

					if (valexist(prod)) {
						revertStock(itemid,prod,um);
					}

					dt.moveToNext();
				}

			}

			if(dt!=null) dt.close();

			sql="UPDATE D_FACTURA  SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_FACTURAD SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_FACTURAD_MODIF SET Anulado=1 WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_FACTURAP SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			//  Barras

			sql="INSERT INTO P_STOCKB SELECT * FROM D_FACTURA_BARRA WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE P_STOCKB SET Corel='' WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_FACTURA_BARRA WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_STOCKB_DEV WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			//String EstadoNC = getEstadoNC_By_Factura(itemid);

			//if (EstadoNC=="2" || EstadoNC!="15" || EstadoNC!="20" ){
			sql = "UPDATE D_NOTACRED SET ANULADO ='S' WHERE FACTURA ='" + itemid + "' AND TIPO_DOCUMENTO = 'NC'";
			db.execSQL(sql);

			sql = "UPDATE D_CXC SET ANULADO ='S' WHERE REFERENCIA ='" + itemid + "'";
			db.execSQL(sql);
			//}

			anulBonif(itemid);
			anularCanastas(itemid);

			// Nota credito
//
//			sql="SELECT COREL FROM D_NOTACRED WHERE FACTURA='"+itemid+"'";
//			dt=Con.OpenDT(sql);
//			if (dt.getCount()>0) {
//				dt.moveToFirst();ncred=dt.getString(0);
//
//				sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + ncred + "' ";
//				db.execSQL(sql);
//
//				sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + ncred + "'";
//				db.execSQL(sql);
//			}

			//ImpresionFactura();

			//Despacho
			sql="UPDATE DS_PEDIDO SET BANDERA='N' WHERE COREL IN (" +
				"SELECT DESPCOREL FROM D_FACTURA WHERE COREL = '"+itemid+"')";
			db.execSQL(sql);

			if(dt!=null) dt.close();

			vAnulFactura=true;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			vAnulFactura=false;
		}

		return vAnulFactura;

	}

	private void anularCanastas(String itemid) {
		try {
			String sql;
			sql = "SELECT PRODUCTO, SUM(CANTENTR) AS CANT " +
					"FROM D_CANASTA WHERE CORELTRANS='"+itemid+"' " +
					"AND ANULADO=0 " +
					"AND CANTENTR > 0 " +
					"GROUP BY PRODUCTO";

			Cursor can = Con.OpenDT(sql);

			if (can != null || can.getCount() > 0) {
				can.moveToFirst();

				while (!can.isAfterLast()) {
					float cant=0;
					String codigo="";
					codigo = can.getString(0);
					cant = can.getFloat(1);

					Cursor stock = Con.OpenDT("SELECT CANT FROM P_STOCK WHERE CODIGO='"+codigo+"'");
					if (stock != null && stock.getCount() >= 1) {
						stock.moveToFirst();
						cant += stock.getFloat(0);
					}


					upd.init("P_STOCK");
					upd.Where("CODIGO='"+codigo+"'");
					upd.add("CANT", cant);
					db.execSQL(upd.SQL());
					can.moveToNext();
				}

				sql = "UPDATE D_CANASTA SET ANULADO=1 WHERE CORELTRANS='"+itemid+"'";
				db.execSQL(sql);
			}

		}catch (Exception e) {
			addlog("anularCanastas", e.getMessage(), null);
		}
	}
	
	private void anulBonif(String itemid) {
		Cursor dt;
		String prod,um;

		try{

			sql = "SELECT CODIGO,UNIDADMEDIDA FROM D_BONIF_STOCK WHERE Corel='" + itemid + "'";
			dt = Con.OpenDT(sql);

			if (dt.getCount() > 0) {

				dt.moveToFirst();
				while (!dt.isAfterLast()) {

					prod = dt.getString(0);
					um = dt.getString(1);

					revertStockBonif(itemid, prod, um);

					dt.moveToNext();
				}

				if(dt!=null) dt.close();
			}

			sql = "UPDATE D_BONIF SET Anulado='S' WHERE COREL='" + itemid + "'";
			db.execSQL(sql);

			sql = "UPDATE D_BONIFFALT SET Anulado='S' WHERE COREL='" + itemid + "'";
			db.execSQL(sql);

			sql="DELETE FROM D_BONIF_STOCK WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_BONIF_LOTES WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			//sql="DELETE FROM D_REL_PROD_BON WHERE COREL='"+itemid+"'";
			//db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}
	}
	
	private void revertStock(String corel) {
		Cursor dt;
		String doc,stat,lot,cod,um;
		double cant,ppeso;
		
		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV,CODIGO,UNIDADMEDIDA FROM D_FACTURA_STOCK " +
					"WHERE (COREL='" + corel + "') ";
			dt = Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);
				cod=dt.getString(13);
				um=dt.getString(14);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", cod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + cod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void revertStock(String corel,String pcod,String um) {
		Cursor dt;
		String doc,stat,lot;
		double cant,ppeso;

		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV FROM D_FACTURA_STOCK " +
					"WHERE (COREL='" + corel + "') AND (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "')";
			dt = Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", pcod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void revertStockBonif(String corel,String pcod,String um) {
		Cursor dt;
		String doc,stat,lot;
		double cant,ppeso;

		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV FROM D_BONIF_STOCK " +
					"WHERE (COREL='" + corel + "') AND (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "')";
			dt = Con.OpenDT(sql);
			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", pcod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void anulDepos(String itemid) {
		Cursor DT;
		String tdoc;

		try{

			db.beginTransaction();

			if (gl.depparc){
				sql="UPDATE D_DEPOS SET Anulado='S' WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
			}

			sql="SELECT DISTINCT DOCCOREL,TIPODOC FROM D_DEPOSD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				tdoc=DT.getString(1);

				if (tdoc.equalsIgnoreCase("F")) {
					sql="UPDATE D_FACTURA SET DEPOS='N' WHERE (COREL='"+DT.getString(0)+"')";
				} else {
					sql="UPDATE D_COBRO SET DEPOS='N' WHERE (COREL='"+DT.getString(0)+"')";
				}

				db.execSQL(sql);

				DT.moveToNext();
			}
			if(DT!=null) DT.close();

			if (!gl.depparc){
				sql="DELETE FROM D_DEPOS WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
				sql="DELETE FROM D_DEPOSD WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
				sql="DELETE FROM D_DEPOSB WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
			}

			sql="UPDATE FinDia SET val3 = 0, val4=0";
			db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

		}catch (Exception e){

			db.endTransaction();
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		cmdAnular.setVisibility(View.VISIBLE);

	}
	
	private void anulDeposParc(String itemid) {
		try{
			sql="UPDATE D_DEPOS SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		cmdAnular.setVisibility(View.VISIBLE);

	}	
	
	private void anulRecarga(String itemid) {
		Cursor DT;
		String prod;
		double cant,cantm;

		try{
			sql="UPDATE D_MOV SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="SELECT PRODUCTO,CANT,CANTM FROM D_MOVD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				prod=DT.getString(0);
				cant=DT.getDouble(1);
				cantm=DT.getDouble(2);

				try {
					sql="UPDATE P_STOCK SET CANT=CANT-"+cant+", CANTM=CANTM-"+cantm+" WHERE CODIGO='"+prod+"'";
					db.execSQL(sql);
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					mu.msgbox(e.getMessage()+"\n"+sql);
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		cmdAnular.setVisibility(View.VISIBLE);

	}

	private boolean anulDevol(String itemid) {
		Cursor DT;
		String prod;
		double cant,cantm;

		boolean vAnulDevol=false;

		try{

			db.beginTransaction();

			sql="UPDATE D_MOV SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="SELECT PRODUCTO,CANT,CANTM, UNIDADMEDIDA FROM D_MOVD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				sql="INSERT INTO P_STOCK SELECT PRODUCTO, CANT, CANTM, PESO, 0, LOTE, '',0,'N', '','',0,0,'', UNIDADMEDIDA,0 " +
						"FROM D_MOVD WHERE (COREL='"+itemid+"')";
				db.execSQL(sql);
			}

			sql="SELECT PRODUCTO,UNIDADMEDIDA FROM D_MOVDB WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				sql="INSERT INTO P_STOCKB " +
					"SELECT M.RUTA, D.BARRA, D.PRODUCTO, 1, '' AS COREL, 0 AS PRECIO, D.PESO, '' AS DOCUMENTO, " +
					" M.FECHA, 0 AS ANULADO, '' AS CENTRO, 'A' AS ESTATUS, " +
					"0 AS ENVIADO, 0 AS CODIGOLIQUIDACION, '' AS COREL_D_MOV, D.UNIDADMEDIDA, '' AS DOCENTREGA, 0 " +
					"FROM D_MOV M INNER JOIN D_MOVDB D ON M.COREL = D.COREL WHERE (M.COREL='"+itemid+"')";
				db.execSQL(sql);
			}

			if(DT!=null) DT.close();

			sql="UPDATE FinDia SET val5 = 0";
			db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

			vAnulDevol=true;

		}catch (Exception e){

			db.endTransaction();
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		cmdAnular.setVisibility(View.VISIBLE);

		return vAnulDevol;

	}

	private void anulRecib(String itemid) {
		try{
			sql="UPDATE D_COBRO  SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
			sql="UPDATE D_COBROD SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
			sql="UPDATE D_COBROP SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		cmdAnular.setVisibility(View.VISIBLE);

	}

	private boolean anulNotaCredito(String itemid) {

		Cursor DT2;
		String vCorelFactura = "";
		String vCorelDevol="";
		boolean vAnulNotaCredito=false;
		String vCorelNotaC = itemid;

		try{

			sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" + itemid + "'";
			DT2=Con.OpenDT(sql);

			if (DT2.getCount()>0){
				DT2.moveToFirst();
				vCorelFactura = DT2.getString(0);
			}

			DT2.close();

			itemid = vCorelFactura;//En la variable vCorelFactura se guarda el corel de la Factura si es una NC con venta y sino el corel de D_CXC

			if (ExisteFactura(itemid)){
				msgAsk("Anular nota de crédito con factura");
			}else{

				CrearNotaDebito();

				vCorelDevol = itemid;

				//String EstadoNC = getEstadoNC(vCorelNotaC);

				//if (EstadoNC=="2" || EstadoNC!="15" || EstadoNC!="20" ){

				sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + vCorelDevol + "' ";
				db.execSQL(sql);

				sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + vCorelNotaC + "'  AND TIPO_DOCUMENTO = 'NC'";
				db.execSQL(sql);

				//}

				vAnulNotaCredito=true;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			vAnulNotaCredito=false;
		}

		cmdAnular.setVisibility(View.VISIBLE);

		return vAnulNotaCredito;
	}
	private boolean anulNotaCreditoF(String vCorelNotaC) {

		String vCorelDevol="";
		boolean vAnulNotaCreditoF=false;

		try{

			vCorelDevol = getCorelDevol(vCorelNotaC);

			String EstadoNC = getEstadoNC(vCorelNotaC);

			if (EstadoNC=="2" || EstadoNC!="15" || EstadoNC!="20" ){

				sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + vCorelDevol + "' ";
				db.execSQL(sql);

				sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + vCorelNotaC + "'  AND TIPO_DOCUMENTO = 'NC'";
				db.execSQL(sql);

			}

			vAnulNotaCreditoF=true;

			progress.cancel();
			mu.msgbox("Documento anulado.");
			cmdAnular.setVisibility(View.VISIBLE);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			vAnulNotaCreditoF=false;
		}

		listItems();

		return vAnulNotaCreditoF;
	}

	private String tieneFacturaNC(String vCorel){

		Cursor DT;
		String vtieneFacturaNC= "";

		try{

			sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" + vCorel + "' AND FACTURA IN (SELECT COREL FROM D_FACTURA)";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				vtieneFacturaNC = DT.getString(0);
			}

			if (DT != null) DT.close();

		}catch (Exception ex){
		    mu.msgbox("Ocurrió un error "+ex.getMessage());
		}

		return vtieneFacturaNC;
	}

	private String getCufe(String Factura){

		Cursor DT;
		String Cufe= "";

		try{

			sql = "SELECT CUFE FROM D_FACTURA WHERE COREL = '" + Factura + "'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				Cufe = DT.getString(0);
			}

		}catch (Exception ex){
			mu.msgbox("Ocurrió un error "+ex.getMessage());
		}

		return Cufe;
	}

	private String tieneNotaCredFactura(String vCorel){
	Cursor DT;
	String vtieneNotaCredFactura= "";

	try{

		sql = "SELECT COREL FROM D_NOTACRED WHERE FACTURA = '" + vCorel + "' AND FACTURA IN (SELECT COREL FROM D_FACTURA)";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0){
			DT.moveToFirst();
			vtieneNotaCredFactura = DT.getString(0);
		}

	}catch (Exception ex){
		mu.msgbox("Ocurrió un error "+ex.getMessage());
	}

	return vtieneNotaCredFactura;
}

    private boolean ExisteFactura(String vCorel){

        Cursor DT;
        boolean vExisteFactura = false;

        try{

            sql = "SELECT COREL FROM D_FACTURA WHERE COREL = '" + vCorel + "'";
            DT=Con.OpenDT(sql);

            vExisteFactura = (DT.getCount()>0?true:false);

        }catch (Exception ex){

        }

        return vExisteFactura;
    }

    //endregion
	
	//region Impresion
	
	private void ImpresionFactura() {
		try{
			if (fdoc.buildPrint(itemid,3,gl.peFormatoFactura)) prn.printask();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	private void Impresion() {
		try{
			if (doc.buildPrintSimple("0",0)) prn.printask();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void ImprimeNC_Fact(){

		try{

			if (tipo==3){

				clsDocDevolucion fdev;
				String corelNotaCred=tieneNotaCredFactura(itemid);

				if (!corelNotaCred.isEmpty()){

					fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
					fdev.deviceid =gl.numSerie;

					fdev.buildPrint(corelNotaCred, 3, "TOL"); prn_nc.printnoask(printclose, "printnc.txt");

				}
			}else if (tipo==6){

				String corelFactura=tieneFacturaNC(itemid);

				if (!corelFactura.isEmpty()){
					clsDocFactura fdoc;

					fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
					fdoc.deviceid =gl.numSerie;
					fdoc.medidapeso=gl.umpeso;
					fdoc.buildPrint(corelFactura, 3, "TOL"); prn.printnoask(printclose,"print.txt");
				}

			}

		}catch(Exception ex){

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
					if (tipo==3 || tipo==6){
						ImprimeNC_Fact();
					}

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.show();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private class clsDocAnul extends clsDocument {

		public clsDocAnul(Context context, int printwidth, String archivo) {
			super(context, printwidth ,gl.peMon,gl.peDecImp, archivo);

			nombre="Existencias";
			numero="";
			serie="";
			ruta=gl.ruta;
			vendedor=gl.vendnom;
			cliente="";

		}

		protected boolean buildDetail() {


			return true;
		}

		protected boolean buildFooter() {

			try {
				rep.add("");rep.add("");
				rep.addc("ANULACION");
				rep.add("");
				rep.add("Ruta : "+ruta);
				rep.add("Vendedor : "+vendedor);
				rep.add("");

				rep.add("");

				//if (tipo==1) lblTipo.setText("Recibo");
				if (gl.tipo==2) {
					rep.add("Deposito");
					rep.add("Fecha : "+sitem.Fecha);
					rep.add("Cuenta : "+sitem.Desc);
					rep.add("Total : "+sitem.Valor);
				}
				if (gl.tipo==3) {
					rep.add("Factura");
					rep.add("Numero : "+sitem.Fecha);
					rep.add("Total : "+sitem.Valor);
					rep.add("Cliente : "+sitem.Desc);
				}
				if (gl.tipo==4) {
					rep.add("Recarga");
					rep.add("Fecha : "+sitem.Fecha);
				}
				//if (tipo==5) lblTipo.setText("Devoluci�n a bodega");

				rep.add("");
				rep.add("");
				rep.add("");
				rep.add("");

				return true;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				return false;
			}

		}
		
	}

	//endregion
	
	//region Aprofam
	
	private boolean aprValidaNC() {
		Cursor DT;
		int ci,cf,ca1,ca2;
		double dd;
		
		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN FROM P_CORELNC ";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
	
			if (ca1>=cf) {
				msgbox("Se ha acabado el talonario de notas de crédito. No se puede continuar con la anulación de factura.");
				return false;
			}
			
			dd=cf-ci;dd=0.75*dd;
			ca2=ci+((int) dd);
			
			if (ca1>ca2) {
				toastcent("Queda menos que 25% de talonario de notas de crédito.");
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("No esta definido correlativo de notas de crédito. No se puede continuar con la anulación de factura.\n"+e.getMessage());
			return false;
		}	
					
		return true;
		
	}
	
	private void aprNuevaNC() {
		Cursor DT;
		String corel,factser,cli;
		int factcor;
		double facttot;
		
		aprAssignCorel();		
		if (fcorel==0) return;		
		
		try {	
			
			corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
			fecha=du.getActDateTime();
			
			sql="SELECT TOTAL,SERIE,CORELATIVO,CLIENTE FROM D_FACTURA WHERE Corel='"+itemid+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			factser=DT.getString(1);
			factcor=DT.getInt(2);
			facttot=DT.getDouble(0);
			cli=DT.getString(3);
					
			try {
				
				db.beginTransaction();
				    			
				ins.init("D_NOTACRED");
				
				ins.add("COREL",corel);
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",cli);
				ins.add("TOTAL",facttot);
				ins.add("FACTURA",itemid);
				ins.add("SERIE",fserie);
				ins.add("CORELATIVO",fcorel);
				ins.add("STATCOM","N");				
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("RESOLNC",fres);
				ins.add("SERIEFACT",factser);pfser=factser;
				ins.add("CORELFACT",factcor);pfcor=""+factcor;
				ins.add("IMPRES",1);
					      	
				db.execSQL(ins.sql());
								
				// Actualizacion de ultimo correlativo				
				sql="UPDATE P_CORELNC SET CORELULT="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";	
				db.execSQL(sql);
							
				db.setTransactionSuccessful();					
				db.endTransaction();
			
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				db.endTransaction();
				mu.msgbox("Error (nota credito) " + e.getMessage());return;
			}

			if (prn.isEnabled()) aprNotePrn(corel);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
			
	}	
	
	private void aprNotePrn(String corel) {
		
		aprLoadHeadData(corel);
		
		try {
			
			rep=new clsRepBuilder(this,prn.prw,true,gl.peMon,gl.peDecImp, "");
			
			buildHeader(corel,0);
			
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
			rep.empty();
			rep.empty();
			
			buildHeader(corel,2);
			
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
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL FROM D_NOTACRED WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pserie=DT.getString(0);
			pnumero=""+DT.getInt(1);
			pruta=DT.getString(2);
			
			pvend=DT.getString(3);
			pcli=DT.getString(4);		
			ptot=DT.getDouble(5);

			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			presol="Resolucion No. : "+DT.getString(0);
			ff=DT.getLong(1);presfecha="De Fecha : "+du.sfecha(ff);
			ff=DT.getLong(2);presvence="Resolucion vence : "+du.sfecha(ff);
			presrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);

			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+pvend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pvendedor=DT.getString(0);

			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+pcli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pcliente=DT.getString(0);       		
			pclicod=pcli;
			pclidir=DT.getString(3);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());
			pcliente=pcli;
			pvendedor=pvend;
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

			if (reimpres==3) rep.add("--------  A N U L A C I O N  --------");
			if (reimpres==1) rep.add("------  R E I M P R E S I O N  ------");
			if (reimpres==2) rep.add("-----  C O N T A B I L I D A D  -----");
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

	private void aprAssignCorel(){
		Cursor DT;
		int cf,ca1;
		
		fcorel=0;fserie="";fres="";
		try{
			try {
				sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN,RESOL FROM P_CORELNC WHERE RUTA='"+gl.ruta+"'";
				DT=Con.OpenDT(sql);

				DT.moveToFirst();

				fserie=DT.getString(0);
				ca1=DT.getInt(1);
				cf=DT.getInt(3);
				fres=DT.getString(4);

				fcorel=ca1+1;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				fcorel=0;fserie="";
				msgbox("No existe correlativo disponible, no se puede emitir la nota de crédito");
				return;
			}

			if (fcorel>cf) {
				msgbox("Se ha acabado el talonario de notas de crédito. No se puede continuar con la anulación de factura.");
				fcorel=0;return;
			}

			if (fcorel==cf) mu.msgbox("Esta es la última nota de crédito.");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

				
	}

	//endregion
	
	//region Aux
	
	private void msgAsk(String msg) {
		AtomicReference<String> CorelNC = new AtomicReference<>("");

		try{

			NCRefencia = false;

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("ROAD");
			dialog.setMessage("¿" + msg  + "?");
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setPositiveButton("Si", (dialog1, which) -> {
				AsyncGetToken Token = new AsyncGetToken();
				Token.execute();

				if (tipo == 3) {

					CorelNC.set(TieneNotaCredito());

					if (!CorelNC.get().isEmpty()) {
						NCRefencia = true;
					}

					//#AT20230309 Solo mandar anular facturas a la DGI si el estado es 2,15 0 20
					if (sitem.Estado.equals("2") || sitem.Estado.equals("15")  || sitem.Estado.equals("20")) {
						if (!ConexionValida()) {
							toast("No hay conexión a internet.");
							return;
						}

						ProgressDialog("Anulando factura...");

						if (NCRefencia) {

							if (!sitem.CufeFactura.isEmpty() && sitem.CufeFactura != null) {
								CUFE = sitem.CufeFactura;
								corelNotaCre = CorelNC.get();
								corelFactura=itemid;
							} else {
								CUFE = sitem.Cufe;
								corelNotaCre = CorelNC.get();
								corelFactura=itemid;
							}

							try{
								AsyncAnularDocumento anular = new AsyncAnularDocumento();
								anular.execute();
							}catch (Exception e) {
								msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
							}

						} else {

							if (!sitem.CufeFactura.isEmpty() && sitem.CufeFactura != null) {
								CUFE = sitem.CufeFactura;
							} else {
								CUFE = sitem.Cufe;
							}

							try{
								AsyncAnularDocumento anular = new AsyncAnularDocumento();
								anular.execute();
							}catch (Exception e) {
								msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
							}

						}

					} else {
						AnularFactHH_DGI();
						if (NCRefencia){
							corelNotaCre = CorelNC.get();
							corelFactura=itemid;
							//#CKFK20230331 Aquí se genera la nota de débito de la nota de crédito
							AnularNotaCreditoConFactura(corelNotaCre, corelFactura);
						}
					}
				} else if (tipo == 6) {

					Cursor DT;
					String vCorelFactura = "";
					corelNotaCre = itemid;

					sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" + corelNotaCre + "' AND TIPO_DOCUMENTO = 'NC' ";
					DT=Con.OpenDT(sql);

					if (DT.getCount()>0){
						DT.moveToFirst();
						vCorelFactura = DT.getString(0);
					}

					if (ExisteFactura(vCorelFactura)) {
						NCRefencia=true;
					}

					//#AT20230309 Solo mandar anular la nota de crédito a la DGI si el estado es 2,15 0 20
					if (sitem.Estado.equals("2") || sitem.Estado.equals("15")  || sitem.Estado.equals("20")) {
						if (!ConexionValida()) {
							toast("No hay conexión a internet.");
							return;
						}

						if (NCRefencia) {

							if (!sitem.CufeFactura.isEmpty() && sitem.CufeFactura != null) {
								CUFE = sitem.CufeFactura;
								corelFactura = vCorelFactura;
								corelNotaCre = itemid;
							} else {
								CUFE = sitem.Cufe;
								corelFactura = vCorelFactura;
								corelNotaCre = itemid;
							}

							ProgressDialog("Anulando Nota Crédito...");

							try{
								AsyncAnularDocumento anular = new AsyncAnularDocumento();
								anular.execute();
							}catch (Exception e) {
								msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
							}

						} else {
							generaNotaDebito_y_ActualizaCorrelativos(corelNotaCre);
						}

					} else {
						if (NCRefencia){
							AnularFactHH_DGI(vCorelFactura);
							generaNotaDebito_y_ActualizaCorrelativos(corelNotaCre);
							//msgAskSoloND("Solo se generará la nota de débito porque no está certificada la nota de crédito.");
						}else{
							generaNotaDebito_y_ActualizaCorrelativos(corelNotaCre);
							//msgAskSoloND("Solo se generará la nota de débito porque no está certificada la nota de crédito.");
						}
						cmdAnular.setVisibility(View.VISIBLE);
					}
				} else {
					anulDocument();
				}
			});
			dialog.setNegativeButton("No",  new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					cmdAnular.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
			
	}

	private void msgAskSoloND(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("ROAD");
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setPositiveButton("Aceptar", (dialog1, which) -> {
				generaNotaDebito_y_ActualizaCorrelativos(corelNotaCre);
			});

			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private String TieneNotaCredito() {
		Cursor DT;
		String Corel = "";
		try {
			sql = "SELECT COREL FROM D_NOTACRED WHERE FACTURA = '" +itemid+ "' AND TIPO_DOCUMENTO = 'NC' ";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				Corel = DT.getString(0);
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return Corel;
	}

	private String getEstadoNC(String pNotaCD) {
		Cursor DT;
		String estado = "";
		try {
			sql = "SELECT ESTADO FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE COREL = '" +itemid+ "' " +
				  " AND TIPODOCUMENTO IN ('04','06') ";
			DT = Con.OpenDT(sql);

			if (DT!=null){
				if (DT.getCount() > 0) {
					estado = DT.getString(0);
				}
				DT.close();
			}

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return estado;
	}

	private String getCorelDevol(String pNotaCD) {
		Cursor DT;
		String vCorelDevol = "";
		try {
			sql = " SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" +pNotaCD+ "' " +
				  " AND TIPO_DOCUMENTO 'NC' ";
			DT = Con.OpenDT(sql);

			if (DT!=null){
				if (DT.getCount() > 0) {
					vCorelDevol = DT.getString(0);
				}
				DT.close();
			}

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return vCorelDevol;
	}

	private String getEstadoNC_By_Factura(String pFactura) {
		Cursor DT;
		String estado = "";
		try {
			sql = " SELECT ESTADO FROM D_FACTURA_CONTROL_CONTINGENCIA " +
				  " WHERE COREL IN (SELECT COREL FROM  D_NOTACRED WHERE FACTURA = '" +itemid+ "') " +
				  " AND TIPODOCUMENTO IN ('04','06') ";
			DT = Con.OpenDT(sql);

			if (DT!=null){
				if (DT.getCount() > 0) {
					estado = DT.getString(0);
				}
				DT.close();
			}

		} catch (Exception e) {
			msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return estado;
	}
	private void getDepTipo() {
		Cursor DT;
		
		try {
			sql="SELECT BOLETA_DEPOSITO,DEPOSITO_PARCIAL FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			gl.boldep=DT.getInt(0);
			depparc=DT.getInt(1);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			gl.boldep=0;
			depparc=0;
		}
		
	}	
	
	private boolean checkFactDepos() {
		Cursor dt;

		try {

			sql="SELECT D_DEPOSD.DOCCOREL,D_DEPOS.ANULADO "+
				"FROM D_DEPOS INNER JOIN D_DEPOSD ON D_DEPOS.COREL=D_DEPOSD.COREL " +
				"WHERE D_DEPOSD.DOCCOREL='"+itemid+"' AND D_DEPOS.ANULADO='N'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return false;
			msgbox("La factura está depositada, no se puede anular");
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private boolean valexist(String prcodd) {
		Cursor DT;
		
		try {
			sql="SELECT TIPO FROM P_PRODUCTO WHERE CODIGO='"+prcodd+"'";
           	DT=Con.OpenDT(sql);
           	if (DT.getCount()==0) return false;

           	DT.moveToFirst();
			
           	return DT.getString(0).equalsIgnoreCase("P");
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
	    }
	}

	private void inputValor() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		int cor;

		cor=sitem.flag;
		cor=cor % 1000;
		scor=""+cor;
		if (cor<10) {
			scor="00"+scor;
		} else if (cor<10) {
			scor="0"+scor;
		}

		alert.setTitle("Ingrese valor : "+scor);

		final EditText input = new EditText(this);
		alert.setView(input);

		input.setInputType(InputType.TYPE_CLASS_NUMBER );
		input.setText("");
		input.requestFocus();

		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					String s=input.getText().toString();
					if (s.equalsIgnoreCase(scor)) {
						msgAsk("Anular factura");
					} else {
						msgbox("Valor incorrecto");
					}
				} catch (Exception e) {
					mu.msgbox("Valor incorrecto");return;
				}
			}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});

		alert.show();
	}

	//endregion
	public class AsyncGetToken extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... vd) {
			getToken();
			return null;
		}

		@Override
		protected void onPostExecute(String vdata){
			super.onPostExecute(vdata);
		}
	}

	public class AsyncAnularDocumento extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... vd) {
			try {
				vError = "";
				AnularFacturaDGI();
			} catch (Exception e) {
				progress.cancel();
				msgbox(vError);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String vdata){
			try{
				if (exito) {

					if (tipo == 6 ){
						//#CKFK20230331 Aquí se revierte el stock, se actualizan las
						// tablas con el Anulado en true y se imprime
						//Esta función se llama cuando es desde la nota de crédito y el itemid es de la Nota
						AnularFactHH_DGI(corelFactura);
					}else if (tipo == 3 ){
						//#CKFK20230331 Aquí se revierte el stock, se actualizan las
						// tablas con el Anulado en true y se imprime
						//Esta función se llama cuando es desde la factura y el itemid es de la factura
						AnularFactHH_DGI();
					}

					if (tipo == 6 || tipo==3) {
						if (NCRefencia){
							//#CKFK20230331 Aquí se genera la nota de débito de la nota de crédito
							AnularNotaCreditoConFactura(corelNotaCre, corelFactura);
							//#CKFK20230331 Aquí se actualizan las tablas con el Anulado en true
							anulNotaCreditoF(corelNotaCre);
						}
					}

				} else {
					progress.cancel();
					if(NCRefencia){
						throw new Exception("No se pudo anular la factura con nota de crédito " + resultado.getMensajeRespuesta());
					}else{
						throw new Exception("No se pudo anular la factura " + resultado.getMensajeRespuesta());
					}
				}
			}catch (Exception ex){
				 msgbox(ex.getMessage());
			}

		}
	}

	public void generaNotaDebito_y_ActualizaCorrelativos(String vCorelNotaC){

		try{

			CrearNotaDebito(vCorelNotaC);

			//String EstadoNC = getEstadoNC(vCorelNotaC);
			String vCorelDevol = getCorelDevol(vCorelNotaC);

			//if (EstadoNC=="2" || EstadoNC!="15" || EstadoNC!="20" ){

			sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + vCorelDevol + "' ";
			db.execSQL(sql);

			sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + vCorelNotaC + "'  AND TIPO_DOCUMENTO = 'NC'";
			db.execSQL(sql);

			//}

		}catch (Exception ex){
			mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + ex.getMessage());
		}

		listItems();

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
}
