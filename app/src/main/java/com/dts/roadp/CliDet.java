package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;

import uk.co.senab.photoview.PhotoViewAttacher;

import static android.widget.ImageView.ScaleType.CENTER_CROP;


public class CliDet extends PBase {

	private TextView lblNom,lblRep,lblDir,lblAten,lblTel,lblGPS,lblDescripcionPago,lblTipologia;
	private TextView lblCLim,lblCUsed,lblCDisp,lblCobro,lblDevol,lblCantDias,lblClientePago;
	private TextView lblRuta,lblRuta2, lblDespacho, lblCanal, lblCanalsub,lblPrior,lblProv,lblDist;
	private RelativeLayout relV,relP,relD,relCamara,relCanasta;
	private ImageView imgCobro,imgDevol,imgRoadTit, imgTel, imgWhatsApp, imgWaze, imgVenta, imgPreventa, imgDespacho, imgCamara, imgMap;
	private EditText txtRuta;
	private RadioButton chknc,chkncv;

	private PhotoViewAttacher zoomFoto;
	private AppMethods app;

	private String cod,tel, Nombre, NIT, sgp1, sgp2, canal, canalsub,prior,idtipo;
	private String imagenbase64,path,fechav;
	private Boolean imgPath, imgDB, ventaGPS,flagGPS=true,permiteVenta=true,clicred;
	private double gpx,gpy,credito,clim,cused,cdisp,cred;
	private int nivel,browse,merc,rangoGPS,modoGPS;
	private boolean porcentaje = false,clinue,pedclinue, diaCorrecto;
	private byte[] imagenBit;
	private Boolean georefCanasta, georefPreventa, georefPrefactura, georefAutoVenta, permitePedidoExtraRuta;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_det);

		super.InitBase();
		addlog("CliDet",""+du.getActDateTime(),gl.vend);

		lblNom= (TextView) findViewById(R.id.lblNom);
		lblRep= (TextView) findViewById(R.id.lblPres);
		lblDir= (TextView) findViewById(R.id.lblDir);
		lblAten= (TextView) findViewById(R.id.lblCant);
		lblTel= (TextView) findViewById(R.id.lblTel);
		lblGPS= (TextView) findViewById(R.id.textView2);
		lblCobro= (TextView) findViewById(R.id.textView6);
		lblDevol= (TextView) findViewById(R.id.textView3);
		lblCLim= (TextView) findViewById(R.id.lblCLim);
		lblCUsed= (TextView) findViewById(R.id.lblCUsed);
		lblCDisp= (TextView) findViewById(R.id.lblCDisp);
		lblCantDias = (TextView) findViewById(R.id.lblCantDias);
		lblClientePago = (TextView) findViewById(R.id.lblClientePago);
		lblRuta = (TextView) findViewById(R.id.lblRuta);lblRuta.setVisibility(View.INVISIBLE);
		lblRuta2 = (TextView) findViewById(R.id.lblRuta2);lblRuta2.setVisibility(View.INVISIBLE);
        lblCanal = findViewById(R.id.lblClase);
        lblCanalsub = findViewById(R.id.lblClaseSub);
		lblTipologia = findViewById(R.id.lblDescripcionPago2);
        lblPrior = findViewById(R.id.textView100);
        lblProv = findViewById(R.id.textView98);
        lblDist= findViewById(R.id.textView99);
		lblDescripcionPago=(TextView)findViewById(R.id.lblDescripcionPago);

		txtRuta = (EditText) findViewById(R.id.txtRuta);
		txtRuta.setVisibility(View.INVISIBLE);
		txtRuta.setFocusable(false);

		chknc = new RadioButton(this,null);
		chkncv = new RadioButton(this,null);

		lblDespacho = (TextView) findViewById(R.id.lblDespacho);

		//	relMain=(RelativeLayout) findViewById(R.id.relclimain);
		relV=(RelativeLayout) findViewById(R.id.relVenta);
		relP=(RelativeLayout) findViewById(R.id.relPreventa);
		relD=(RelativeLayout) findViewById(R.id.relDespacho);
		relCamara=(RelativeLayout) findViewById(R.id.relCamara);
        relCanasta=(RelativeLayout) findViewById(R.id.relCanastas);

		imgCobro= (ImageView) findViewById(R.id.imageView2);
		imgDevol= (ImageView) findViewById(R.id.imageView1);
		imgRoadTit = (ImageView) findViewById(R.id.imgRoadTit);
		imgDespacho = (ImageView) findViewById(R.id.imgDespacho);

		app = new AppMethods(this, gl, Con, db);

		cod=gl.cliente;
		gl.cobroPendiente = false;
		gl.pagocobro = false;
		//#CKFK20230904 Quité esta inicialización porque afectaba la impresión.
        //gl.tiponcredito = 0;

		showData();

		gl.iddespacho = null;

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			//#AT 20220303 Para Toledado tanto la ruta como el cliente deben estan en true para realizar la validación
			if (gl.validar_posicion_georef) {
				validaGeoreferencia();
			} else {
				ventaGPS = false;
			}
		} else {
			//#AT 20220303 Para los clientes que no sean Toledano se valida GPS en base a tabla P_PARAMEXT
			ventaGPS=gl.peVentaGps!=0;
		}

		//#AT 20220303 La bandera flagGPS se llena con las condiciones que estan definidas aquí arriba
		flagGPS=ventaGPS;

		//#AT 20220303 Aquí validamos si el cliente está en un rango válido en caso de que se deba validar la distancia GPS
		rangoGPS=gl.peLimiteGPS+gl.peMargenGPS;
		if (flagGPS) {
			if (rangoGPS <= 1)  {
				permiteVenta = true;
			} else if (gl.gpsdist < 1) {
				permiteVenta = true;
			} else {
				permiteVenta = gl.gpsdist <= rangoGPS;
			}
		} else {
			permiteVenta=true;
		}

		gl.gpspass=false;

		sgp2 =" ( "+mu.frmint(rangoGPS)+"m ) ";
		sgp1 =" ( "+mu.frmint(gl.gpsdist)+"m ) ";

		Toast.makeText(this, "permiteVenta: "+permiteVenta+ " validar_posicion_georef: "+gl.validar_posicion_georef, Toast.LENGTH_SHORT).show();
		/*if (!gl.devol) {
			lblDevol.setVisibility(View.INVISIBLE);
			imgDevol.setVisibility(View.INVISIBLE);
		}*/

		//showData();
		calcCredit();
		credito=gl.credito;

		//browse=0;
		merc=1;

		habilitaOpciones();

		miniFachada();

		validaDespacho();

		setHandlers();

		gl.corelFac="";

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try{
			super.onSaveInstanceState(savedInstanceState);
			savedInstanceState.putString("CLIID", cod);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		try{
			super.onRestoreInstanceState(savedInstanceState);
			String cliid=savedInstanceState.getString("CLIID");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void validaGeoreferencia() {
		ventaGPS = false;

		try {
			switch (gl.rutatipog) {
				case "C": //Canasta
					if (georefCanasta) {
						ventaGPS = true;
					}
					break;
				case "P": //Preventa
					if (georefPreventa) {
						ventaGPS = true;
					}
					break;
				case "D": //Prefactura
					if (georefPrefactura) {
						ventaGPS = true;
					}
					break;
				case "V": // AutoVenta
					if (georefAutoVenta) {
						ventaGPS = true;
					}
					break;
			}
		} catch (Exception e) {
			mu.msgbox("Valida georeferencia TOL: "+ e.getMessage());
		}
	}

	//region  Events

	public void showVenta(View view){
		Toast.makeText(this, "gl.validar_posicion_georef: "+gl.validar_posicion_georef+" georefAutoVenta: "+georefAutoVenta+" permiteVenta: "+permiteVenta+ "Distancia Clie-Vend: "+(gl.gpsdist - rangoGPS)+" MargenGps: "+rangoGPS, Toast.LENGTH_LONG).show();

		//#CKFK 20210922 Inicializa la variable del total de la devolución
		gl.devtotal=0;

		//#CKFK 20240313 Inicializa la variable de la orden de compra
		gl.ordenCompra = "";

		if (!permiteVenta) {
			if (gl.peVentaGps==1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 +"!\nPara realizar la venta debe acercarse más al cliente.");
				return;
			} else {
				modoGPS=1;
				msgAskGPSVenta();
			}
		} else {
			doVenta();
		}
	}

	public void showPreventa(View view) {
		Toast.makeText(this, "gl.validar_posicion_georef: "+gl.validar_posicion_georef+" georefPreventa: "+georefPreventa+" permiteVenta: "+permiteVenta + " Distancia Clie-Vend: "+(gl.gpsdist - rangoGPS)+" MargenGps: "+rangoGPS, Toast.LENGTH_LONG).show();

	    if (clinue) {
            if (!pedclinue) {
                msgbox("No se permite crear pedido al cliente nuevo.");return;
            }
        }

		if (montoMinimo()==0) {
			msgbox("No está definido monto minimo, no se puede vender.");return;
		}

		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\nPara realizar la venta debe asercarse más al cliente.");
				return;
			} else {
				modoGPS = 2;
				msgAskGPSVenta();
			}
		} else {
			doPreventa();
		}
	}

	public void showDespacho(View view) {
		//#CKFK 20240313 Inicializa la variable de la orden de compra
		gl.ordenCompra = "";

		if (!permiteVenta) {
			msgbox("¡Distancia del cliente mayor que permitida!\nPara realizar la venta debe acercarse más al cliente.");return;
		}else{
			try{
				gl.banderaCobro = false;
				if (!validaVenta()) return;//Se valida si hay correlativos de factura para la venta
				if(porcentaje == false) VerificaCantidadDesp();
			} catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				mu.msgbox("showDespacho: " + e.getMessage());
			}
		}
	}

	public void showCredit(View viev){

		clsFinDia claseFinDia;
		claseFinDia = new clsFinDia(this);
		//#CKFK20220530 Agregué validación para verificar si ya se realizó el depósito
		if (claseFinDia.getDeposito() == 4)	{
			msgbox("Ya realizó el depósito, no puede hacer nuevas cobros o anule el depósito realizado");
			return;
		}

		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\nPara realizar el cobro debe acercarse más al cliente.");
				return;
			} else {
				modoGPS = 3;
				msgAskGPSVenta();
			}
		} else {
			doCredit();
		}
	}

	public void showDevol(View view){

		clsFinDia claseFinDia;
		claseFinDia = new clsFinDia(this);
		//#CKFK20220530 Agregué validación para verificar si ya se realizó la devolución a Bodega
		if ((claseFinDia.getDevBodega() == 5)) {
			msgbox("Ya realizó la devolución a Bodega, no puede hacer nuevas notas de Crédito o anule la devolución realizada");
			return;
		}

		Toast.makeText(this, "gl.validar_posicion_georef: "+gl.validar_posicion_georef+" georefPrefactura: "+georefPrefactura+" permiteVenta: "+permiteVenta+ " Distancia Clie-Vend: "+(gl.gpsdist - rangoGPS)+" MargenGps: "+rangoGPS, Toast.LENGTH_LONG).show();

		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que permitida "+ sgp2 + "!\nPara realizar la devolución debe asercarse más al cliente.");
				return;
			} else {
				modoGPS = 4;
				msgAskGPSVenta();
			}
		} else {

			if (gl.rutatipo.equals("D")){

				if (!clicred){
					clsDs_pedidoObj Ds_pedidoObj;

					Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);
					Ds_pedidoObj.fill("WHERE (CLIENTE='"+gl.cliente+"') AND (BANDERA='N')");

					if (Ds_pedidoObj.count>0){
						msgAskTipoDev();
					}else{
						msgbox("El cliente no tiene prefacturas, no puede hacer devoluciones");
					}
				}else{
					msgAskTipoDev();
				}

			}else{
				msgAskTipoDev();
			}
		}
	}

	public void tomarFoto(View view){
        File URLfoto;
		int codResult = 1;

		try{
			if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
				msgbox("El dispositivo no soporta toma de foto");return;
			}

			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());

			//	try {

			Intent intento1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			if (clinue) {
                URLfoto = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + cod + ".jpg");
            } else {
                URLfoto = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
            }

			intento1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(URLfoto));
			startActivityForResult(intento1,codResult);

		/*	}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				//mu.msgbox("tomarFoto: "+ e.getMessage());
				mu.msgbox("No se puede activar la camara. ");
			}*/
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}


	}

	public void mostrarFachada(View view){
		Cursor DT;
		imgDB = false; imgPath=false;

		try {

		    if (clinue) {
                path = (Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + cod + ".jpg");
            } else {
                path = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
            }

			File archivo = new File(path);

			sql = "SELECT IMAGEN FROM P_CLIENTE_FACHADA WHERE CODIGO ='"+ cod +"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount() > 0){
				DT.moveToFirst();
				imagenbase64 = DT.getString(0);
				imgDB = true;
			}

			if (archivo.exists()) {
				imgPath = true;
				inputFachada();
			}else if(imgDB == true){
				inputFachada();
			}else{
				Toast.makeText(this,"Fachada no disponible",Toast.LENGTH_LONG).show();
			}

			if(DT!=null) DT.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("inputFachada: " + e.getMessage());
		}

	}

	public void rutaHelp(View view) {
		mostraRutaSupervisor();
	}

	public void showCanastas(View view) {
		try {
			Intent i = new Intent(this, Canastas.class);
			startActivity(i);
		} catch (Exception e) {

		}
	}

    public void showDir(View view) {
	    //mu.msgbox(lblDir.getText().toString());
        try{
            msgAskEditCliente();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String paht;

		if (requestCode == 1) {
			try {

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				if (clinue) {
                    paht = (Environment.getExternalStorageDirectory() + "/RoadFotos/clinue/" + cod + ".jpg");
                } else {
                    paht = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
                }

                try {
                    Bitmap bitmap1 = BitmapFactory.decodeFile(paht);
                    bitmap1 = redimensionarImagen(bitmap1, 640, 360);

                    FileOutputStream out = new FileOutputStream(paht);
                    bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();

                    File iarchivo = new File(paht);
                    imgRoadTit.setImageURI(Uri.fromFile(iarchivo));
                } catch (Exception e) { }

			} catch (Exception e){
				//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				mu.msgbox("onActivityResult: " + e.getMessage());
			}

		}

	}

	private void setHandlers(){
		try {
			chknc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (chknc.isChecked()==true){
						chkncv.setChecked(false);
						gl.tiponcredito = 1;
					}
				}
			});

			chkncv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (chkncv.isChecked()==true){
						chknc.setChecked(false);
						gl.tiponcredito = 2;
						VerificaCantidad();
					}
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region Main

	private void showData() {
		Cursor DT;
		int uvis,dcred;
		String contr,sgps="0.00000000 , 0.00000000",uv,uvy,uvm,uvd;
        String idmuni,iddep,muni,dep;

		lblNom.setText("");lblRep.setText("");
		lblDir.setText("");lblAten.setText("");lblTel.setText("");
		tel="";

		try {

			sql="SELECT NOMBRE,NOMBRE_PROPIETARIO,DIRECCION,ULTVISITA,TELEFONO,LIMITECREDITO,NIVELPRECIO,PERCEPCION,TIPO_CONTRIBUYENTE, " +
					"COORX,COORY,MEDIAPAGO,NIT,VALIDACREDITO,BODEGA,CHEQUEPOST,TIPO,DIACREDITO,INGRESA_CANASTAS, " +
                    "CANAL,SUBCANAL,PRIORIZACION,MUNICIPIO, GEOREFERENCIA_CANASTA, GEOREFERENCIA_PREVENTA, GEOREFERENCIA_PREFACTURA, " +
					"GEOREFERENCIA_AUTOVENTA, DESCRIPCION_PAGO, PERMITIR_PEDIDO_EXTRA_RUTA,TIPOLOGIA "+
					"FROM P_CLIENTE WHERE CODIGO='"+cod+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();


			lblNom.setText(cod + " - " + DT.getString(0));
			lblRep.setText(DT.getString(12));
			lblDir.setText(DT.getString(2));
			lblCantDias.setText(DT.getString(17));
			lblDescripcionPago.setText(DT.getString(27)==null?"":DT.getString(27));

			if(lblDescripcionPago.getText().toString().contains("ACH")) lblDescripcionPago.setTextColor(Color.RED);

			georefCanasta    = (DT.getInt(23) == 1 ? true : false);
			georefPreventa   = (DT.getInt(24) == 1 ? true : false);
			georefPrefactura = (DT.getInt(25) == 1 ? true : false);
			georefAutoVenta  = (DT.getInt(26) == 1 ? true : false);
			permitePedidoExtraRuta = (DT.getInt(28) == 1 ? true : false);

			tel=DT.getString(4);
			lblTel.setText(DT.getString(4));
			//uvis=DT.getInt(3);
            uv=DT.getString(3);

            try {
                uvy=uv.substring(0,2);
                uvm=uv.substring(2,4);
                uvd=uv.substring(4,6);
                long ff=du.cfecha(mu.CInt(uvy),mu.CInt(uvm),mu.CInt(uvd));
                lblAten.setText(du.sfechalocal(ff));
            } catch (Exception e) {
                lblAten.setText("");
            }

			nivel=DT.getInt(6);
			gl.nivel=nivel;
			gl.percepcion=DT.getDouble(7);

			contr=""+DT.getString(8);
			gl.contrib=contr;

			//#CKFK 20190619 Cambié las coordenadas que se le envían al Waze, en lugar de X,Y le estoy enviando Y,X
			sgps=mu.frmgps(DT.getDouble(9))+ " , "+ mu.frmgps(DT.getDouble(10));
			lblGPS.setText(sgps);

			gl.media=DT.getInt(11);

			if(gl.media != 4){
				lblClientePago.setText("CONTADO");
				if (gl.peModal.equalsIgnoreCase("TOL")) clicred=false;
			}else if(gl.media == 4){
				lblClientePago.setText("CRÉDITO");
				if (gl.peModal.equalsIgnoreCase("TOL")) clicred=true;
			}

			dcred=DT.getInt(17);

			clim=DT.getDouble(5);

			gl.fnombre=DT.getString(0);
			gl.fnit=DT.getString(12);
			gl.fdir=DT.getString(2);
			gl.vcredito = DT.getString(13).equalsIgnoreCase("S");
			gl.vcheque = DT.getString(14).equalsIgnoreCase("S");
			gl.vchequepost = DT.getString(15).equalsIgnoreCase("S");
			gl.clitipo = DT.getString(16);
			//gl.ingresaCanastas = DT.getInt(18) == 1;
            canal =DT.getString(19);
            canalsub =DT.getString(20);
			idtipo=DT.getString(29);
            prior=DT.getString(21);
            idmuni=DT.getString(22);
            gl.IdMun = idmuni;

			if(DT!=null) DT.close();

			sql="SELECT RUTA FROM P_CLIRUTA WHERE CLIENTE='"+cod+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			gl.rutasup=DT.getString(0);
			lblRuta.setText("Ruta: ");
			txtRuta.setText(gl.rutasup);
			if(DT!=null) DT.close();

			sql="SELECT DIA FROM P_CLIRUTA WHERE CLIENTE='"+cod+"' AND DIA = " + du.dayofweek(du.getActDate());
			DT=Con.OpenDT(sql);
			if (DT!=null){
				diaCorrecto = (DT.getCount()>0);
			}
			if(DT!=null) DT.close();

			sql="SELECT PEDIDOS_CLINUEVO,VENTA FROM P_RUTA ";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            pedclinue=DT.getInt(0)==1;
            gl.rutatipo = DT.getString(1);

            sql="SELECT NOMBRE FROM P_CANAL WHERE CODIGO='"+canal+"'";
            DT=Con.OpenDT(sql);
            try {
                DT.moveToFirst();lblCanal.setText("Canal : "+DT.getString(0));
            } catch (Exception e) {
                lblCanal.setText("Canal : ");
            }

            sql="SELECT NOMBRE FROM P_CANALSUB WHERE CODIGO='"+canalsub+"'";
            DT=Con.OpenDT(sql);
            try {
                DT.moveToFirst();lblCanalsub.setText("Subcanal : "+DT.getString(0));
            } catch (Exception e) {
                lblCanalsub.setText("Subcanal : ");
            }

			sql="SELECT NOMBRE FROM P_TIPOLOGIA WHERE CODIGO='"+idtipo+"'";
			DT=Con.OpenDT(sql);
			try {
				DT.moveToFirst();lblTipologia.setText("Tipologia : "+DT.getString(0));
			} catch (Exception e) {
				lblTipologia.setText("Tipologia : ");
			}


            lblPrior.setText("Categorización : "+prior);

            lblProv.setText("Provincia : ");lblDist.setText("Distrito/Corregimiento : ");
            try {
                sql="SELECT DEPAR, NOMBRE FROM P_MUNI WHERE CODIGO='"+idmuni+"'";
                DT=Con.OpenDT(sql);
                DT.moveToFirst();

                iddep=DT.getString(0);
                lblDist.setText("Distrito/Corregimiento : "+DT.getString(1));

                try {
                    sql="SELECT NOMBRE FROM P_DEPAR WHERE CODIGO='"+iddep+"'";
                    DT=Con.OpenDT(sql);
                    DT.moveToFirst();

                    lblProv.setText("Provincia : "+DT.getString(0));
                } catch (Exception e) {}
            } catch (Exception e) {}
      	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

	private void calcCredit() {

		try{
			NumberFormat format = NumberFormat.getInstance();
			format.setGroupingUsed(true);
			format.setMaximumFractionDigits(0);

			lblCLim.setText("-");lblCUsed.setText("-");lblCDisp.setText("-");

			cused=getUsedCred();
			cdisp=clim-cused;if (cdisp<0) cdisp=0;
			gl.credito=cdisp;

			if (!gl.peModal.equalsIgnoreCase("TOL")) clicred=gl.credito>0;

			lblCLim.setText(mu.frmcur0(clim));
			lblCUsed.setText(mu.frmcur0(cused));
			lblCDisp.setText(mu.frmcur0(cdisp));

			if (cused==0) {
				lblCobro.setVisibility(View.INVISIBLE);
				imgCobro.setVisibility(View.INVISIBLE);
			}

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				if (!esClienteNuevo()){
					lblCobro.setVisibility(View.VISIBLE);
					imgCobro.setVisibility(View.VISIBLE);
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private double getUsedCred(){
		Cursor DT;
		double tpg,tsal,cu=0;

		try {
			sql="SELECT SUM(SALDO) FROM P_COBRO WHERE (CLIENTE='"+cod+"')";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			tsal=DT.getDouble(0);


			sql="SELECT SUM(TOTAL) FROM D_COBRO WHERE (ANULADO='N') AND (CLIENTE='"+cod+"')";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			tpg=DT.getDouble(0);

			cu=tsal-tpg;

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			cu=0;mu.msgbox(e.getMessage());
		}
		return cu;
	}

	private void initVenta(){
        Cursor dt;

        if (gl.rutatipo.equalsIgnoreCase("T")) {
            if (tieneDespacho()) {
                showDespacho(null);return;
            }
        }

        try {
            sql="SELECT COREL FROM D_PEDIDO WHERE (CLIENTE='"+gl.cliente+"') AND (ANULADO='N') AND (STATCOM='N')";
            dt=Con.OpenDT(sql);
            gl.modpedid="";

            if (dt.getCount()>0) {
                if (gl.listapedidos) {
                    startActivity(new Intent(this,ListaPedidos.class));
                }
            } else {
                startActivity(new Intent(this,Venta.class));
            }

            gl.listapedidos=false;

            //startActivity(new Intent(this,Venta.class));
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
	}

	private boolean validaVenta() {
		Cursor DT;
		int ci,cf,ca1,ca2;
		long fecha_vigencia, diferencia;
		double dd;
		boolean resguardo=false;


		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN,FECHAVIG,RESGUARDO FROM P_COREL ";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();

			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
			fecha_vigencia=DT.getLong(4);
			resguardo=DT.getInt(5)==1;

			if(resguardo==false){
				if(fecha_vigencia< du.getActDate()){
					//#HS_20181128_1556 Cambie el contenido del mensaje.
					mu.msgbox("La resolución esta vencida. No se puede continuar con la venta.");
					return false;
				}
			}

			if(resguardo==false){
				diferencia = fecha_vigencia - du.getActDate();
				if( diferencia <= 30){
					//#HS_20181128_1556 Cambie el contenido del mensaje.
					mu.msgbox("La resolución vence en "+diferencia+". No se puede continuar con la venta.");
					return false;
				}
			}

			if (ca1>=cf) {
				//#HS_20181128_1556 Cambie el contenido del mensaje.
				mu.msgbox("Se han terminado los correlativos de facturas. No se puede continuar con la venta.");
				return false;
			}

			dd=cf-ci;dd=0.75*dd;
			ca2=ci+((int) dd);

			if (ca1>ca2) {
				//toastcent("Queda menos que 25% de talonario de facturas.");
				//#HS_20181129_1040 agregue nuevo tipo de mensaje
				porcentaje = true;
				msgAskVenta();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n"); //+e.getMessage());
			return false;
		}

		return true;

	}

	private void miniFachada(){
		Cursor DT;
		imgDB = false;
		try {

			path = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
			File archivo = new File(path);

			sql = "SELECT IMAGEN FROM P_CLIENTE_FACHADA WHERE CODIGO ='"+ cod +"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount() > 0){
				DT.moveToFirst();
				imagenbase64 = DT.getString(0);
				imgDB = true;
			}

			if(archivo.exists()){
				imgRoadTit.setImageURI(Uri.fromFile(archivo));
			} else if (imgDB == true){
				byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
				Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
				imgRoadTit.setImageBitmap(redimensionarImagen(bitm,200,200));
			} else {
				imgRoadTit.setImageResource(R.drawable.cliente);
			}

			if(DT!=null) DT.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("inputFachada: " + e.getMessage());
		}
	}

	//endregion

	//region Fachada

	public Bitmap redimensionarImagen(Bitmap mBitmap, float newWidth, float newHeigth){

		int width = mBitmap.getWidth();
		int height = mBitmap.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeigth) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);

	}

	public void inputFachada(){

		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			final ImageView imgFachada = new ImageView(this);
			imgFachada.setScaleType(CENTER_CROP);


			if(imgPath == true) {
				Bitmap bitmap1 = BitmapFactory.decodeFile(path);
				imgFachada.setImageBitmap(redimensionarImagen(bitmap1, 640, 360));
			}else if(imgDB == true) {
				byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
				Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
				imgFachada.setImageBitmap(redimensionarImagen(bitm,640,360));
			}

			alert.setView(imgFachada);

			alert.show();

			imgFachada.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					zoomFoto = new PhotoViewAttacher(imgFachada);
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void validaDespacho() {

		try {

			lblDespacho.setVisibility(View.INVISIBLE);
			imgDespacho.setVisibility(View.INVISIBLE);
			relD.setVisibility(View.GONE);

			clsDs_pedidoObj Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);
			Ds_pedidoObj.fill("WHERE (CLIENTE='"+gl.cliente+"') AND (BANDERA='N')");

			if (Ds_pedidoObj!= null){

				if (Ds_pedidoObj.count>0) {

					lblDespacho.setVisibility(View.VISIBLE);
					imgDespacho.setVisibility(View.VISIBLE);
					relD.setVisibility(View.VISIBLE);
				}
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

    private boolean tieneDespacho() {
        try {
           clsDs_pedidoObj Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);
            Ds_pedidoObj.fill("WHERE (CLIENTE='"+gl.cliente+"') AND (BANDERA='N')");
            return (Ds_pedidoObj.count>0) ;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }
    }


    //endregion

	//region  Aux

	private void doVenta(){

		try{
			gl.banderaCobro = false;

			if (!validaVenta()) return;//Se valida si hay correlativos de factura para la venta

			if(porcentaje == false) VerificaCantidad();

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("doVenta: " + e.getMessage());
		}

	}

	private void doPreventa() {
		try{
			gl.rutatipo="P";

			if (gl.tolsuper) {
				if (!validaRutaSupervisor()) return;
			}

            gl.listapedidos=true;
			runVenta();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void doCredit(){
		try{
			gl.validarCred=2;
			gl.banderaCobro = true;
			Intent intent = new Intent(this,Cobro.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void VerificaCantidad(){
		Float cantidad;
		gl.rutatipo="V";
		//Asigna conexión actual a la forma de Existencias.
		cantidad = Float.valueOf(CantExistencias());

		try{
			if (cantidad == 0){
                if (gl.rutatipo.equalsIgnoreCase("V")) mu.msgbox("No hay existencias disponibles.");
			}else{
				if(gl.tiponcredito == 2){
					return;
				}else {
					clsFinDia claseFinDia;
					claseFinDia = new clsFinDia(this);
					//#CKFK 20190305 Agregué validación para verificar si ya se realizó el depósito
					if ((claseFinDia.getDeposito() != 4) && (claseFinDia.getDocPendientesDeposito()>0)) {
						runVenta();
					}else{
						if (claseFinDia.getDocPendientesDeposito()!=0 || claseFinDia.getDeposito() == 4)	{
							msgbox("Ya realizó el depósito, no puede hacer nuevas facturas o anule el depósito realizado");
							return;
						}else{
							runVenta();
						}
					}
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	public void VerificaCantidadDesp(){
		Float cantidad;
		gl.rutatipo="D";
		//Asigna conexión actual a la forma de Existencias.
		cantidad = Float.valueOf(CantExistencias());

		try{
			if(cantidad == 0){
                if (gl.rutatipo.equalsIgnoreCase("D")) {
					msgAskListaPedidos("No hay existencias disponibles ¿Quiere continuar?");
				}
            }else{
				clsFinDia claseFinDia;
				claseFinDia = new clsFinDia(this);
				//#CKFK 20220426 Agregué validación para verificar si ya se realizó el depósito
				if ((claseFinDia.getDeposito() != 4) && (claseFinDia.getDocPendientesDeposito()>0)) {
					try {
						startActivity(new Intent(this, activity_despacho_list.class));
					} catch (Exception e) {
						addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
					}
				}else{
					if (claseFinDia.getDocPendientesDeposito()!=0 || claseFinDia.getDeposito() == 4)	{
						msgbox("Ya realizó el depósito, no puede hacer nuevas facturas o anule el depósito realizado");
						return;
					}else{
						try {
							startActivity(new Intent(this, activity_despacho_list.class));
						} catch (Exception e) {
							addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
						}
					}
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void runVenta() {
		try{
			if (merc==1) {
				browse=1;
				Intent intent = new Intent(this,MercLista.class);
				startActivity(intent);
			} else {
				initVenta();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	private void EditarDatosCliente()
	{
		browse = 2;

		gl.EditarClienteSubcanal = lblCanalsub.getText().toString().substring(11);
		gl.EditarClienteCanal = lblCanal.getText().toString().substring(8);
		gl.CliProvincia = lblProv.getText().toString().substring(12);
		gl.CliDistrito = lblDist.getText().toString().substring(25);
		gl.IdCanal = canal;
		gl.IdSubcanal = canalsub;

		Intent intent = new Intent(this, editar_cliente.class);
		startActivity(intent);
	}

	private void inputCliente() {

		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Editar Cliente");

			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			final TextView lblNombre = new TextView(this);
			lblNombre.setTextSize(10);
			lblNombre.setText("Nombre:");

			final TextView lblNit = new TextView(this);
			lblNit.setTextSize(10);
			lblNit.setText("NIT:");

			final EditText editNombre = new EditText(this);
			editNombre.setInputType(InputType.TYPE_CLASS_TEXT);
			editNombre.setText(lblDir.getText().toString());

			final EditText editNit = new EditText(this);
			editNit.setInputType(InputType.TYPE_CLASS_TEXT);
			editNit.setText(lblRep.getText().toString());

			layout.addView(lblNombre);
			layout.addView(editNombre);
			layout.addView(lblNit);
			layout.addView(editNit);

			alert.setView(layout);

			alert.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String corel=mu.getCorelBase();
					gl.fnombre = editNombre.getText().toString();
					gl.fnit = editNit.getText().toString();
					ActualizarCliente(corel,gl.fnombre, gl.fnit);
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void ActualizarCliente(String corel, String NombreEdit, String NitEdit){
		Cursor DT;
		try {
			db.execSQL("INSERT INTO D_FACTURAF(COREL, NOMBRE, NIT, DIRECCION) VALUES('"+corel+"','"+NombreEdit+"','"+NitEdit+"','')");
			mu.msgbox("Registro actualizado");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("ActualizarCliente: "+e.getMessage());
		}
	}

	public void setGPS(View view) {
		try{
			//#CKFK20220516 Se puso en comentario porque se va a modificar con los demás datos del cliente
			//browse=2;
			//startActivity(new Intent(this,CliGPS.class));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void sendSMS(View view){
		String to=tel;

		if (to.length()==0) {
			msgbox("Número incorrecto ");return;
		}

		try {
			Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
			startActivity(launchIntent);
			//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+to)));
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("No pudo enviar mensaje verifique que esté instalada la aplicación");
		}

		//try {
		//    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
		//     i.setType("vnd.android-dir/mms-sms");
		//     startActivity(i);
		//} catch (Exception e) {
		//     mu.msgbox("E","No se puede enviar mensaje");
		//}
	}

	@SuppressLint("MissingPermission")
	public void callPhone(View view){
		String to=tel;


		//to="42161467";

		try {

			if (to.length()==0) {
				msgbox("Número incorrecto ");return;
			}

			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+to));
			startActivity(callIntent);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("No pudo llamar : "+e.getMessage());
		}

	}

	public void callWaze(View view) {

		/*
		try {
			Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.example.wazetest");
			this.startActivity(intent);
		} catch ( Exception ex  )	{
		}
		*/

		try {

			String sgps = "";

			sgps=lblGPS.getText().toString();

			if (!sgps.equalsIgnoreCase("0.0000000 , 0.0000000")){
				String url = "waze://?ll="+sgps;
				//"waze://?ll=14.6017278,-90.5236343";
				//"waze://?ll=14.586997,-90.513685";
				Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
				startActivity( intent );
			}else{
				msgbox("El cliente no tiene definidas las coordenadas GPS");
			}

		} catch ( ActivityNotFoundException ex )	{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			Intent intent =
					new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
			startActivity(intent);
		}

	}

	private void setDevType(String tdev) {
		try {

			gl.dvbrowse=0;
			gl.devtipo=tdev;

            sql = "SELECT VENTA FROM P_RUTA";
            Cursor DT = Con.OpenDT(sql);
            DT.moveToFirst();
            String srutatipo = DT.getString(0);

            if (srutatipo.equalsIgnoreCase("D")) {

                int ii=gl.tiponcredito;

                gl.despdevflag=false;gl.closeCliDet=true;
            }
			Intent intent = new Intent(this,DevolCli.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

    private void tiporuta() {
        try {
            sql = "SELECT VENTA FROM P_RUTA";
            Cursor DT = Con.OpenDT(sql);
            DT.moveToFirst();
            gl.rutatipo = DT.getString(0);
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

	private void habilitaOpciones() {
		try {
			String rt;
			boolean flag;

			rt=gl.rutatipog;
            if (rt.equalsIgnoreCase("P")) relCanasta.setVisibility(View.GONE);

			flag=false;
			if (rt.equalsIgnoreCase("V") || rt.equalsIgnoreCase("T")) flag=true;
			if (flag) relV.setVisibility(View.VISIBLE);else relV.setVisibility(View.GONE);

			flag=false;
			if ((rt.equalsIgnoreCase("P") || rt.equalsIgnoreCase("T")) && ((diaCorrecto) ||
					(!diaCorrecto && permitePedidoExtraRuta))) flag=true;
			if (flag) relP.setVisibility(View.VISIBLE);else relP.setVisibility(View.GONE);

			flag=false;

			if (rt.equalsIgnoreCase("D") || rt.equalsIgnoreCase("T")) flag=true;
			if (flag){
				validaDespacho();
			}else relD.setVisibility(View.GONE);

            clinue=esClienteNuevo();

			if (clinue || rt.equalsIgnoreCase("C")){
				imgDevol.setVisibility(View.INVISIBLE);
				lblDevol.setVisibility(View.INVISIBLE);
				imgCobro.setVisibility(View.INVISIBLE);
				lblCobro.setVisibility(View.INVISIBLE);
			}else{
				imgDevol.setVisibility(View.VISIBLE);
				lblDevol.setVisibility(View.VISIBLE);
				imgCobro.setVisibility(View.VISIBLE);
				lblCobro.setVisibility(View.VISIBLE);
			}

		} catch (Exception ex) 	{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			Log.d("habilitaOpciones_err", ex.getMessage());
		}


		if (gl.tolsuper) {
			relP.setVisibility(View.VISIBLE);
			relV.setVisibility(View.GONE);relD.setVisibility(View.GONE);
			imgDevol.setVisibility(View.INVISIBLE);lblDevol.setVisibility(View.INVISIBLE);
			imgCobro.setVisibility(View.INVISIBLE);lblCobro.setVisibility(View.INVISIBLE);

			lblRuta.setVisibility(View.VISIBLE);
			txtRuta.setVisibility(View.VISIBLE);
			lblRuta2.setVisibility(View.VISIBLE);
		}

		//relP.setVisibility(View.VISIBLE);

	}

	protected void toastcent(String msg) {

		try{
			Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void doExit(){
		try{
			gl.closeCliDet=true;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private boolean esClienteNuevo() {
		Cursor dt;
		String sql = "";

		try {
			sql="SELECT CODIGO FROM D_CLINUEVO WHERE CODIGO='"+cod+"' " +
					"UNION SELECT CODIGO FROM D_CLINUEVOT WHERE CODIGO='"+cod+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage()+"\n"+sql);
		}

		return true;
	}

	private boolean validaRutaSupervisor() {
		Cursor dt;
		String ruta=txtRuta.getText().toString();

		if (ruta.isEmpty()) {
			msgbox("Falta definir la ruta");return false;
		}

		try {
			sql="SELECT * FROM P_CLIRUTA WHERE (CLIENTE='"+cod+"') AND (RUTA='"+ruta+"') COLLATE NOCASE";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) {
				msgbox("Ruta incorrecta");return false;
			}

			gl.rutasup=ruta;
			return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}

	private void mostraRutaSupervisor() {
		Cursor dt;
		String ss="";

		try {
			sql="SELECT DISTINCT RUTA FROM P_CLIRUTA WHERE (CLIENTE='"+cod+"') AND RUTA <> '"+"' ORDER BY RUTA";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				while (!dt.isAfterLast()) {
					ss+=dt.getString(0)+"\n";
					dt.moveToNext();
				}
			}
			toast(ss);
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	public float CantExistencias() {
		Cursor DT;
		float cantidad=0,cantb=0;

		try {

			sql = "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.CANT,P_STOCK.CANTM,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS " +
					"FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
			if (Con != null){
				DT = Con.OpenDT(sql);
				cantidad = DT.getCount();
			}else {
				cantidad = 0;
			}

			sql = "SELECT BARRA FROM P_STOCKB";
			if (Con != null){
				DT = Con.OpenDT(sql);
				cantb = DT.getCount();

				if(DT!=null) DT.close();
			}else {
				cantb = 0;
			}

			cantidad=cantidad+cantb;
		} catch (Exception e) {
			return 0;
		}

		return cantidad;
	}

	private double montoMinimo()  {
		double montomin=0;

		try {

			sql="SELECT MM_ESTANDAR FROM P_monto_minimo_cliente WHERE (CLIENTE='"+gl.cliente+"')";
			Cursor dt=Con.OpenDT(sql);

			dt.moveToFirst();
			montomin=dt.getDouble(0);
			if(dt!=null) dt.close();

			return montomin;
		} catch (Exception e) {
			return 0;
		}
	}

	//endregion

	//region Dialogs

	private void msgAskGPSVenta() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\n¿Está seguro de continuar?");
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gl.gpspass=true;
					switch (modoGPS) {
						case 1:
							doVenta();break;
						case 2:
							doPreventa();break;
						case 3:
							doCredit();break;
						case 4:
							msgAskTipoDev();break;
					}
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

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskTipoEstadoDev(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Bueno", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
                    gl.listapedidos=true;
					setDevType("B");
				}
			});

			dialog.setNegativeButton("Malo", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
                    gl.listapedidos=true;
					setDevType("M");
				}
			});

			dialog.setCancelable(false);
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskTipoDev() {

		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Devolución");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(chknc.getParent()!= null){
				((ViewGroup) chknc.getParent()).removeView(chknc);
			}

			if(chkncv.getParent()!= null){
				((ViewGroup) chknc.getParent()).removeView(chknc);
			}

			chknc.setText("Nota de crédito");
			chkncv.setText("Nota de crédito con venta");

			chknc.setChecked(false);
			chkncv.setChecked(false);

			if (clicred)layout.addView(chknc);
			if (!clicred) layout.addView(chkncv);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(chkncv.isChecked() || chknc.isChecked()){
						msgAskTipoEstadoDev("Devolución de producto en estado...");
						layout.removeAllViews();
					}else{
						//toast("Seleccione accion a realizar");
						closekeyb();
						layout.removeAllViews();

						msgAskTipoDev();
					}
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					layout.removeAllViews();
				}
			});

			alert.show();

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskVenta() {

		try{
			AlertDialog.Builder dialog1 = new  AlertDialog.Builder(this);

			dialog1.setTitle("Road");
			dialog1.setMessage("Quedan menos del 25% de correlativos disponibles.");

			dialog1.setIcon(R.drawable.ic_quest);

			dialog1.setPositiveButton("Enterado", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					porcentaje = false;
					VerificaCantidad();
				}
			});

			dialog1.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskEditCliente() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Editar datos de cliente?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					EditarDatosCliente();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					closekeyb();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskListaPedidos(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage(msg);
			dialog.setCancelable(false);

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (gl.rutatipo.equalsIgnoreCase("D")) {
						try {
							startActivity(new Intent(CliDet.this, activity_despacho_list.class));
						} catch (Exception e) {
							addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
						}
					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}


	//endregion

	//region Activity Events

	@Override
	protected void onResume() {

		try{
			super.onResume();

			if (gl.closeCliDet) super.finish();

			calcCredit();
			habilitaOpciones();

			if (gl.despdevflag) {
                gl.despdevflag=false;
                if (gl.rutatipo.equalsIgnoreCase("D")) {
                    showDespacho(null);
                    return;
                }
            }

			if (browse==1) {
				browse=0;
				initVenta();return;
			}

			if (browse==2) {
				browse=0;
				showData();return;
			}

			if (gl.dvbrowse!=0){
				//gl.rutatipo = "V";
                tiporuta();
                String ss=gl.rutatipo;
                browse=0;
				initVenta();
				return;
			}
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	@Override
	public void onBackPressed() {
		try{
			//msgAskExit("Salir");
			super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

}
