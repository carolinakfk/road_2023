package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;

public class ProdCant extends PBase {

	private EditText txtCant,txtPeso;
	private TextView lblDesc,lblCant,lblPrec,lblDisp,lblBU,lblTot,lblCodProd;
	private TextView lblDispLbl,lblPesoLbl,lblFactor,lblCantPeso,lblPesoUni, lblPesoOriginal, lblCantOriginal,
	                 txtCantOriginal, txtPesoOriginal;
	private ImageView imgProd,imgUpd,imgDel;
	private RelativeLayout rlValoresOriginales;
	
	private Precio prc;
	
	private String prodid,prodimg,proddesc,rutatipo,um,umstock,ubas,upres,umfact,umini;
	private int nivel,browse=0,deccant,prevfact=1;
	private double cant,peso,prec,icant,idisp,ipeso,umfactor,pesoprom=0,pesostock=0, vCantO, vPesoO;
	private boolean pexist,esdecimal,porpeso,esbarra,idle=true;
	private AppMethods app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prod_cant);
		
		super.InitBase();
		addlog("ProdCant",""+du.getActDateTime(),gl.vend);

		setControls();
				
		prodid=gl.prod;lblCodProd.setText(prodid);
		um=gl.um;
		nivel=gl.nivel;
		rutatipo=gl.rutatipo;

		if (rutatipo.equalsIgnoreCase("V")) imgUpd.setVisibility(View.INVISIBLE);
		if (gl.iddespacho!=null) {
			rlValoresOriginales.setVisibility(View.VISIBLE);
		}else{
			rlValoresOriginales.setVisibility(View.INVISIBLE);
		}
		imgUpd.setVisibility(View.INVISIBLE);

		prc=new Precio(this,mu,gl.peDec);
		getDisp();

		app = new AppMethods(this, gl, Con, db);

		setHandlers();

		if (gl.peDecCant==0) {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
		} else {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		gl.dval=-1;
		gl.gstr="";

        paramProd();

		txtPeso.setEnabled(validaRango());

		showData();
		
	}

	//region Events
	
	public void sendCant(View view) {
		try{
			if (setCant(false)<1) applyCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	public void showPromo(View view){
		try{
			gl.gstr=prodid;

			Intent intent = new Intent(this,ListaPromo.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPic(View view){
		try{
			gl.gstr=proddesc;
			gl.imgpath=prodimg;

			Intent intent = new Intent(this,PicView.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	public void doDelete(View view) {
		try{
			msgAskDel("Borrar producto");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
		
	public void askExist(View view) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Existencias bodega");
			dialog.setMessage("Actualizar existencias ?");

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					browse=1;
					startActivity(new Intent(ProdCant.this,ComWSExist.class));
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}
	
 	private void setHandlers(){

		try{

			txtCant.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {
					setCant(true);
				}

			});

			txtCant.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
						if (porpeso) {
							txtPeso.requestFocus();
							txtPeso.setSelection(0,txtPeso.length());
						} else {
							sendCant(v);
						}
						return true;
					}
					return false;
				}
			});

			txtPeso.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {
					setPrecio();
				}
			});

			txtPeso.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
							(keyCode == KeyEvent.KEYCODE_ENTER)) {
						sendCant(v);
						return true;
					}
					return false;
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//region Main
 	
	private void showData() {
		Cursor dt;
		double ippeso=0;

		try {

			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			um=dt.getString(0);umini=um;
			ubas=um;umfact=um;
			lblBU.setText(ubas);gl.ubas=ubas;upres=ubas;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("1-"+ e.getMessage());
		}
	
		try {
							
			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,TIPO,PESO_PROMEDIO,FACTORCONV "+
				 "FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	dt=Con.OpenDT(sql);
			dt.moveToFirst();
				
			ubas=dt.getString(0);gl.ubas=ubas;			
			lblDesc.setText(dt.getString(7));
			
			//prodimg=DT.getString(6);
			prodimg=prodid;
			proddesc=dt.getString(7);

			pesoprom = dt.getDouble(10);
			if (pesoprom==0) pesoprom = dt.getDouble(9);

			if (dt.getString(8).equalsIgnoreCase("P")) pexist=true; else pexist=false;


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		    mu.msgbox("1-"+ e.getMessage());
	    }

		try {
			sql="SELECT DECIMALES FROM P_NIVELPRECIO WHERE CODIGO="+nivel;
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			deccant=dt.getInt(0);

			if(dt!=null) dt.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			deccant=0;
		}

		if (deccant==0) {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
		} else {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		try {
			imgProd.setVisibility(View.INVISIBLE);
			if (!mu.emptystr(prodimg)) {
				try {
					prodimg = Environment.getExternalStorageDirectory()+ "/RoadFotos/"+prodimg+".jpg";
					File file = new File(prodimg); 
					if (file.exists()) {
						try {
							Bitmap bmImg = BitmapFactory.decodeFile(prodimg);
							imgProd.setImageBitmap(bmImg);
							imgProd.setVisibility(View.VISIBLE);
						} catch (Exception e) {
							toast(e.getMessage());
							imgProd.setVisibility(View.INVISIBLE);
						}
					}
				} catch (Exception e) {
					imgProd.setVisibility(View.INVISIBLE);
				}
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("3-"+ e.getMessage());
		}

		prec=prc.precio(prodid,1,nivel,um,gl.umpeso,0,um);
		if (prc.existePrecioEspecial(prodid,1,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
			if (prc.precioespecial>0) prec=prc.precioespecial;
		}

		lblPrec.setText("Precio: "+mu.frmcur(0));

		if (rutatipo.equalsIgnoreCase("P") && (prec==0)){
			prec=precioPreventaPres();
			prec=prec*prevfact;
		}

		if (prec==0) {
			hidekeyb();
			msgSinPrecio("El producto no tiene definido precio");return;
		}
		
		if (gl.sinimp) prec=prc.precsin;
		
		try {
			sql="SELECT CANT,PESO FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
           	dt=Con.OpenDT(sql);

           	if(dt.getCount()>0){
				dt.moveToFirst();
				icant=dt.getDouble(0);
				ippeso=dt.getDouble(1);
			}

			if(dt!=null) dt.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			icant=0;ippeso=0;
	    }

		if (icant>0) {
			parseCant(icant);
			imgDel.setVisibility(View.VISIBLE);
		} else {
			imgDel.setVisibility(View.INVISIBLE);
		}
		
		if (rutatipo.equalsIgnoreCase("V")) {
			idisp=getDisp();
		} else {
		}

		/*
		prec=prc.precio(prodid,1,nivel,um,gl.umpeso,0,um);
		if (prc.existePrecioEspecial(prodid,1,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
			if (prc.precioespecial>0) prec=prc.precioespecial;
		}
		*/

		idisp=mu.trunc(idisp);

		if (porpeso) {
			lblBU.setText(umstock);
			upres = umstock;

			lblPrec.setText(mu.frmcur(prec)+" x "+gl.umpeso);
		} else {
			lblPrec.setText(mu.frmcur(prec)+" x "+upres);
		}

		if (pexist) lblDisp.setText(""+((int) idisp)); else lblDisp.setText("");
		lblDisp.setText(mu.frmdecimal(idisp, gl.peDecImp)+" "+upres);
		lblFactor.setText("x "+mu.frmdecimal(umfactor, gl.peDecImp));
		lblPesoUni.setText(gl.umpeso);

		if (rutatipo.equalsIgnoreCase("P") && (idisp==0)) {
			lblDisp.setText("");lblDispLbl.setText("");
		}

		try {
			txtCant.setSelection(txtCant.getText().length());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

        txtPeso.setText(mu.frmdecimal(ippeso, gl.peDecImp));

		try {

		//	if(gl.rutatipo.equals("D")){

				sql="SELECT CANT_ORIGINAL, PESO_ORIGINAL FROM DS_PEDIDOD " +
						" WHERE (PRODUCTO='"+prodid+"') " +
						" AND (COREL = '"+gl.iddespacho+"')";
				dt=Con.OpenDT(sql);

				if (dt!=null){
					if (dt.getCount()>0){
						dt.moveToFirst();
						vCantO=dt.getDouble(0);
						vPesoO=dt.getDouble(1);

						txtCantOriginal.setText(String.valueOf(vCantO));
						txtPesoOriginal.setText(String.valueOf(vPesoO));
					}
				}

			//}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Obteniendo Cant Original y Peso Original -"+ e.getMessage());
		}

	}
	
	private double getDisp() {

		Cursor dt;
		double disp = 0;
		double umf1 =1;
		double umf2 =1;
			
		try {

			pesostock=0;

			sql=" SELECT IFNULL(SUM(CANT),0) AS CANT,IFNULL(SUM(PESO),0) AS PESO " +
					" FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')";
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

			if (disp>0) return disp;

			dt.close();

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"')";	
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();
				umstock=dt.getString(0);
			}

			dt.close();

			//sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') ";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf1=dt.getDouble(0);
			} else 	{
				umf1=1;
				//#EJC20181127: No mostrar mensaje por versión de aprofam.
				msgFactor("No existe factor de conversión para "+um);return 0;
			}	

			dt.close();

			//sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"')";
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf2=dt.getDouble(0);
			} else {
				umf2=1;
				//#EJC20181127: No mostrar mensaje por versión de aprofam.
				msgFactor("No existe factor de conversión para "+um);return 0;
			}
			dt.close();

			umfactor=umf1/umf2;

			/*
			if (umf1>=umf2) {
				umfactor=umf1/umf2;
			} else {
				umfactor=umf2/umf1;
			}
			*/
			
			sql="SELECT IFNULL(SUM(CANT),0) AS CANT,IFNULL(SUM(PESO),0) AS PESO FROM P_STOCK " +
				" WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')";
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

			dt.close();

			return disp;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }

		return 0;
	}

	private void delItem(){	
		try {
	    	db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+prodid+"'");
			db.execSQL("DELETE FROM T_BONITEM WHERE Prodid='"+prodid+"'");
			gl.dval=0;
	    	super.finish();
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
	}

	private void applyCant() {
		double ppeso = 0;

		try {

			/*
			if (rutatipo.equalsIgnoreCase("V")) {
				if (peso<=0) {
					mu.msgbox("Peso incorrecto");
					txtCant.requestFocus();
					return;
				}
			}
			*/

			if (cant < 0) {
				mu.msgbox("Cantidad incorrecta");
				txtCant.requestFocus();
				return;
			}

			if (rutatipo.equalsIgnoreCase("V") || rutatipo.equalsIgnoreCase("D") ) {
				if (cant > idisp) {
					mu.msgbox("Cantidad mayor que disponible.");
					txtCant.requestFocus();
					return;
				}
			}

			if (porpeso) {

				String spp = txtPeso.getText().toString();

				try {
					ppeso = Double.parseDouble(spp);
					if (ppeso <= 0) throw new Exception();
				} catch (Exception e) {
					if (porpeso) {
						mu.msgbox("Peso incorrecto");
						txtPeso.requestFocus();
						return;
					}
				}
			} else {
				if(Double.isNaN(pesostock))	pesostock=1;
				if (pesoprom == 0) ppeso = pesostock * cant;
				else ppeso = pesoprom * cant;
			}

			if (porpeso && gl.rutatipo.equalsIgnoreCase("V")) {
				if (!checkLimits(ppeso,cant*umfactor)) {
					return;
				}
			}

			ppeso=mu.round(ppeso,3);

			gl.dval = cant;
			gl.dpeso = ppeso;
			gl.um = upres;
			gl.umpres = upres;
			gl.umstock = umstock;
			gl.umfactor = umfactor;
			gl.prectemp = prec;

			if (rutatipo.equalsIgnoreCase("P")) {
				gl.um=um;
				gl.umpres = um;
				gl.precprev=prec*prevfact;
				gl.prectemp=gl.precprev;
			}

			hidekeyb();
			super.finish();
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	//endregion

	//region Update Disp
	
	public void updDisp(){

		try{
			gl.gstr=prodid;
			browse=1;

			Intent intent = new Intent(this,ActDisp.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
	
	//region Aux
	
	private void setControls() {

		try{
			txtCant= (EditText) findViewById(R.id.txtMonto);
			txtPeso= (EditText) findViewById(R.id.txtPeso);txtPeso.setVisibility(View.INVISIBLE);
			lblDesc=(TextView) findViewById(R.id.lblFecha);
			lblCant=(TextView) findViewById(R.id.lblCant);
			lblPrec=(TextView) findViewById(R.id.lblPNum);
			lblDisp=(TextView) findViewById(R.id.lblDisp);
			lblBU=(TextView) findViewById(R.id.lblBU);
			lblTot=(TextView) findViewById(R.id.textView1);lblTot.setText("");
			lblDispLbl=(TextView) findViewById(R.id.textView8);
			lblPesoUni=(TextView) findViewById(R.id.textView25);lblPesoUni.setVisibility(View.INVISIBLE);
			lblPesoLbl=(TextView) findViewById(R.id.textView24); lblPesoLbl.setVisibility(View.INVISIBLE);
			lblFactor=(TextView) findViewById(R.id.textView22);lblFactor.setVisibility(View.INVISIBLE);
			lblCantPeso=(TextView) findViewById(R.id.textView21);lblCantPeso.setText("");lblCantPeso.setVisibility(View.INVISIBLE);
			lblCodProd=(TextView) findViewById(R.id.txtRoadTit);
			imgProd=(ImageView) findViewById(R.id.imgPFoto);
			imgUpd=(ImageView) findViewById(R.id.ImageView1);
			imgDel=(ImageView) findViewById(R.id.imageView2);
			lblCantOriginal=(TextView) findViewById(R.id.textView9);
			lblPesoOriginal=(TextView) findViewById(R.id.textView10);
			txtCantOriginal=(TextView) findViewById(R.id.textView11);
			txtPesoOriginal=(TextView) findViewById(R.id.textView12);
			rlValoresOriginales=(RelativeLayout) findViewById(R.id.rlValoresOriginales);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}

	private int setCant(boolean mode){
		double cu=0.0,tv=0,corig,cround,fruni,frcant,adcant,vpeso=0,opeso;
		boolean ajust=false;

		//if (!idle) return 0;
		
		lblTot.setText("***");
		if (mode) {
			txtPeso.setText("0");peso=0;
		} else {
			try {
				if (!txtPeso.getText().toString().trim().isEmpty()){
					ss=txtPeso.getText().toString();
					peso=Double.parseDouble(ss);
				} else {
					cant=-1;peso=0;return 2;
				}

				if (rutatipo.equalsIgnoreCase("V")) {
					if (peso<=0) throw new Exception();
				}
			} catch (Exception e) {
				cant=-1;peso=0;return 2;
			}
		}
		cu=0;

		try {
			if (!txtCant.getText().toString().trim().isEmpty()){
				cu=Double.parseDouble(txtCant.getText().toString());
			}
			cant=cu;corig=cant;cround=Math.floor(cant);
			esdecimal=(corig!=cround)?true:false;
			cant=mu.round(cant,deccant);

			if (cant<=0) {
				if (!txtCant.getText().toString().trim().isEmpty()){
					mu.msgbox("Cantidad incorrecta");
				}
				return 2;
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//cant=-1;
			cant=0;
			if (!txtCant.getText().toString().trim().isEmpty()){
				mu.msgbox("Cantidad incorrecta");
			}
			return 2;
		}

		// ajuste a unidades menores
		if (esdecimal) {

			if (umfactor==0) {
				//msgbox("Factor de conversion incorrecto");return -1;
			}

			fruni=1/umfactor;
			frcant=corig/fruni;
			adcant=Math.floor(frcant)+1;
			adcant=adcant*fruni;

			if (adcant!=cant) {
				ajust=true;cant=adcant;
			}
		}

		cant = mu.round(cant, gl.peDecImp);

		if (porpeso) {

			if (gl.rutatipo.equalsIgnoreCase("V")){
				prec = prc.precio(prodid, 0, nivel, um, gl.umpeso, peso,um);
				if (prc.existePrecioEspecial(prodid, 1, gl.cliente, gl.clitipo, um, gl.umpeso, umfactor * cant)) {
					if (prc.precioespecial > 0) prec = prc.precioespecial;
				}
			}
		} else {
			prec = prc.precio(prodid, 0, nivel, um, gl.umpeso, 0,um);
			if (prc.existePrecioEspecial(prodid, 1, gl.cliente, gl.clitipo, um, gl.umpeso, 0)) {
				if (prc.precioespecial > 0) prec = prc.precioespecial;
			}
		}

		if (rutatipo.equalsIgnoreCase("P") && (prec==0)){
			prec=precioPreventaPres();
			prec=prec*prevfact;
		}

		try {

			if (gl.iddespacho !=null ) {
				if (!gl.iddespacho.isEmpty()) {
					if (permitirCantidadMayores(prodid, cant)){

						if (cant<0)	lblCant.setText(""); else lblCant.setText(String.valueOf(cant));
						if (porpeso) {
							//tv=prec*cant;
							tv=prec*peso;
						} else {
							tv=prec*cant;
						}

					}else{
						txtCant.setText("");
						throw new Exception ("El cliente no puede aumentar la cantidad asignada del producto");
					}
				}
			}else{
				if (cant<0)	lblCant.setText(""); else lblCant.setText(String.valueOf(cant));
				if (porpeso) {
					//tv=prec*cant;
					tv=prec*peso;
				} else {
					tv=prec*cant;
				}
			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			tv=0;mu.msgbox(e.getMessage());
		}

        lblTot.setText(mu.frmcur(tv));

		//#CKFK 20190517 Agregué para que el umfactor sea igual al peso promedio en el pedido y se calcule correctamente
		if(gl.rutatipo.equalsIgnoreCase("P")){
			umfactor=(umfactor==1 || umfactor==0?pesoprom:umfactor);
		}

		opeso=cant*umfactor;
		try {
			tv=peso;
			lblCantPeso.setText(mu.frmdecimal(tv,gl.peDecImp)+" "+gl.umpeso);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			lblCantPeso.setText("");
			mu.msgbox(e.getMessage());
		}

		if (mode) txtPeso.setText(mu.frmdecimal(cant*umfactor, gl.peDecImp));

		try {
			if (mu.emptystr(txtPeso.getText().toString())) return 2;
			vpeso=Double.parseDouble(txtPeso.getText().toString());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			if (porpeso) {
				mu.msgbox("Peso incorrecto");return 2;
			}

		}

		//if (porpeso && gl.rutatipo.equalsIgnoreCase("V")) {
		//	if (!checkLimits(vpeso,opeso)) return 2;
		//}

		if (porpeso ) {
			if (!checkLimits(vpeso,opeso)) return 2;
		}

		if (ajust) {
			msgAskAjust("Cantidad ajustada a : "+mu.frmdecimal(cant, gl.peDecImp)+". ¿Aplicar?");
			return 1;
		} else {
			return 0;
		}
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

	private void setPrecio() {
	    double cu,tv,corig,cround,fruni,frcant,adcant,ppeso=0;

        try {
            ppeso=Double.parseDouble(txtPeso.getText().toString());
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);

            if (porpeso) {
				lblTot.setText("***");
				if (!mu.emptystr(txtPeso.getText().toString())) mu.msgbox("Peso incorrecto");
				return;
			}
        }

        //prec=prc.precio(prodid,0,nivel,um,gl.umpeso,ppeso);

        try {
			if (porpeso) {
				//tv=prec*ppeso;
				tv=prec;
			} else {
				tv=prec*cant;
			}
        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            tv=0;mu.msgbox(e.getMessage());
        }

        lblTot.setText(mu.frmcur(tv));

        try {
            tv=umfactor*cant;
            lblCantPeso.setText(mu.frmdecimal(tv,gl.peDecImp)+" "+gl.umpeso);
        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            lblCantPeso.setText("");
            mu.msgbox(e.getMessage());
        }
    }
	
	private void parseCant(double c) {
		try{
			DecimalFormat frmdec = new DecimalFormat("#.####");
			double ub;

			ub=c;
			idle=false;
			if (ub>0) txtCant.setText(frmdec.format(ub));
			idle=true;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private double getDispInv(){
		Cursor DT;
		
		try {
			sql="SELECT CANT FROM P_STOCKINV WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();

			double disp=DT.getDouble(0);

			if(DT!=null) DT.close();

			return disp;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return 0;
	    }		
	}

    private void paramProd() {
        try {
            porpeso=app.ventaPeso(prodid);
            esbarra=app.prodBarra(prodid);
        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            porpeso=false;esbarra=false;
            msgbox(e.getMessage());
        }

        if (porpeso) {
            txtPeso.setVisibility(View.VISIBLE);
            lblPesoLbl.setVisibility(View.VISIBLE);
            lblFactor.setVisibility(View.VISIBLE);
			lblCantPeso.setVisibility(View.VISIBLE);
        }
    }

    private void forceClose() {
        super.finish();
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

    private double precioPreventaPres() {
		double pp=0;


			pp=prc.precio(prodid,1,nivel,umini,gl.umpeso,0,umini);
			if (prc.existePrecioEspecial(prodid,1,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
				if (prc.precioespecial>0) pp=prc.precioespecial;
			}

			if (porpeso)
				prevfact=(int) factorPres(prodid,um,umini);

		//um=umini;

		return pp;
	}

	public double factorPres(String cod,String umventa,String umstock) {
		Cursor DT;
		String sql;
		double val;

		try {
			if (!umini.equals(gl.umpeso)){
				sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
						"WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+umventa+"') AND (UNIDADMINIMA='"+umstock+"')";
				DT = Con.OpenDT(sql);

				if (DT.getCount()==0) {
					sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
							"WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+umventa+"')";
					DT = Con.OpenDT(sql);
				}

				DT.moveToFirst();

				val=DT.getDouble(0);
				if(DT!=null) DT.close();
			}else{
				val=1;
			}

			return  val;
		} catch (Exception e) {
			return 1;
		}
	}


	private boolean permitirCantidadMayores(String prodid, double cant){
		Cursor dt;
        boolean rslt=true;

		try{

			if (gl.iddespacho !=null ){

				if (!gl.iddespacho.isEmpty()) {

					sql="SELECT PRODUCTO " +
							" FROM DS_PEDIDOD " +
							" WHERE (PRODUCTO='"+prodid+"') " +
							" AND (COREL = '"+gl.iddespacho+"')" +
							" AND (CANT < "+cant+") ";
					dt=Con.OpenDT(sql);

					if (dt.getCount()>0) {
						//Validaremos si al cliente se le pueden vender cantidad mayores

						if (gl.permitir_cantidad_mayor) {
							//Si se le pueden vender cantidades mayores
							return true;
						} else {
							//No se le pueden vender cantidades mayores
							return false;
						}

					}
					if(dt!=null) dt.close();
				}
			}

		}catch (Exception ex){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
		}
		return rslt;
	}

	//endregion

    //region Msg

	private void msgSinPrecio(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					forceClose();
				}
			});
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	
	
	public void msgAskUpd(View view) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Actualizar disponible ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					updDisp();
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

	private void msgAskAjust(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					applyCant();
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

	private void msgFactor(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Advertencia");
			dialog.setMessage("¡" + msg + "!");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					forceClose();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//region Activity Events
	
	protected void onResume() {

		try{
			super.onResume();

			if (browse==1) {
				browse=0;
				lblDisp.setText(mu.frmdec(getDispInv()));
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

}
