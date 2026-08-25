package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DevCliCant extends PBase {

	private EditText txtCant,lblPrec,txtLote,txtkgs,txtPrecio;
	private RelativeLayout rlCant;
	private TextView lblDesc,lblBU,lblPrecVenta;
	private Spinner spin,cmbum;
	private CheckBox chkTieneLote;

	private AppMethods app;

	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();

	private ArrayList<Double> cmbumfact= new ArrayList<Double>();
	private ArrayList<String> cmbumlist = new ArrayList<String>();

	private String prodid,estado,razon,devrazon,raz;
	private double cant,icant,factor=0.0,precioventa=0.0,pesoprom=0.0,clcpeso=0.0,precioBasePromocion=0.0;
	private int codDescFinal=0,codRecargoFinal=0;
	private double descuentoFinal=0.0,recargoFinal=0.0;
	private  String um="", ummin="",umcambiar="";
	private Precio prc;

	private boolean vEditando=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_cli_cant);

		super.InitBase();
		addlog("DevCliCant",""+du.getActDateTime(),gl.vend);

		setControls();

		prodid=gl.prod;
		estado=gl.devtipo;
		raz=gl.devrazon;
		gl.tienelote = 0;
		gl.dpeso=0;

		prc=new Precio(this,mu,2);

		gl.dval=-1;

		showkeyb();

		app = new AppMethods(this, gl, Con, db);

		gl.dvporpeso = prodPorPeso(prodid);

		fillSpinner();
		fillcmbUM();

		devrazon=gl.devrazon;
		razon=devrazon;

		ummin = getUMminima();

		showData();

		setHandlers();

		txtkgs.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		txtPrecio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		//Limita cantidad de decimales para los EditText.
		txtPrecio.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
		//txtCant.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(gl.peDec)});
		txtkgs.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(gl.peDec)});

	}

	// Events

	public void sendCant(View view) {

		try{

			gl.dvError = false;

			setCant();

			if (umcambiar.equalsIgnoreCase("Seleccione UM....")){
				gl.dvError = true;
				mu.msgbox("Debe definir unidad de medida a devolver.");return;
			}

			if (cant<=0){
				gl.dvError = true;
				mu.msgbox("Cantidad incorrecta");return;
			}

			if (estado.equalsIgnoreCase("M")) {
				if (razon.equalsIgnoreCase("0")){
					gl.dvError = true;
					mu.msgbox("Debe definir una razón de devolución.");return;
				}
			}

			gl.dval=cant;
			gl.devrazon=razon;

			if (app.ventaPeso(prodid)){

				if(txtkgs.getText().toString().isEmpty()){
					gl.dvpeso = 0.0;
				}else{
					gl.dvpeso = Double.parseDouble(txtkgs.getText().toString());
				}

				try{
					if (gl.dvpeso<=0){
						gl.dvError = true;
						throw new Exception();
					}
				}catch (Exception e){
					gl.dvpeso =0.0;
					gl.dvError = true;
					mu.msgbox("Peso incorrecto, debe ser mayor a 0");
					return;
				}

			}else{

				try{
					gl.dvpeso = Double.parseDouble(txtkgs.getText().toString());
					if (gl.dvpeso<=0) {
						throw new Exception("El peso debe ser mayor que 0");
					}
				}catch (Exception e){
					gl.dvpeso=app.pesoProm(prodid)*cant;
				}
			}

			gl.dvumpeso = gl.umpeso;
			//#CKFK20230904 Cambié al if para que sea en base a si el producto se vende por peso o no.
			if (gl.dvporpeso){
				//#CKFK20230710 Modifiqué esto para que se guarde la unidad de medida de venta correctamente
				gl.dvumstock =  umcambiar;    //#CKFK20230710  antes um;
				gl.dvumventa = um;           //#CKFK20230710  antes umcambiar;
			}else{
				//#CKFK20230823 Modifiqué esto para que se guarde la unidad de medida de venta correctamente
				//cuando no se vende por peso
				gl.dvumstock =  um;
				gl.dvumventa = umcambiar;
			}

			if (unidadVacia(gl.dvumstock) || unidadVacia(gl.dvumventa) || unidadVacia(gl.dvumpeso)) {
				gl.dvError = true;
				mu.msgbox("No se puede agregar el producto " + prodid +
						" porque UMSTOCK, UMVENTA y UMPESO son obligatorias.");
				return;
			}

			if(txtLote.getText().toString().trim().equalsIgnoreCase("")){
				gl.dvError = true;
				mu.msgbox("Lote no puede ser vacío, por favor ingrese un lote.");return;
			}else {
				gl.dvlote =txtLote.getText().toString();
			}

			if(txtPrecio.getText().toString().trim().equalsIgnoreCase("")){
				gl.dvprec = 0.0;
				gl.dvpreclista = 0.0;
			}else{
				gl.dvprec =Double.parseDouble(txtPrecio.getText().toString());
				//#EJC20260724 state(hh-return-promotion-base): evita encadenar el combo
				//sobre el precio individual ya ajustado de la devolucion.
				// #EJC20260730 fix(hh-return-combo-unit-price): la base del combo debe
				// persistirse en la UM seleccionada (ej. CA), no en la UM minima.
				gl.dvpreclista = obtenerPrecioBaseUnidadSeleccionada();
			}

			gl.dvfactor = factor;

			if(gl.dvporpeso==true){
				gl.dvtotal = gl.dvpeso*gl.dvprec;
			}else{
				gl.dvtotal = cant*gl.dvprec;
			}

			if (chkTieneLote.isChecked()==true){
				gl.tienelote = 0;
			}else{
				gl.tienelote = 1;
			}

			if(gl.dvprec==0){
				gl.dvError = true;
				mu.msgbox("El precio no puede ser 0");return;
			}

			if(gl.dvtotal==0){
				gl.dvError = true;
				mu.msgbox("El total no puede ser 0");return;
			}

			gl.dvprec = mu.round2(gl.dvprec);
			gl.dvtotal= mu.round2(gl.dvtotal);
			gl.dvpreclista = mu.round2(gl.dvpreclista);

			//hidekeyb();
			super.finish();

		}catch (Exception e){
			gl.dvError = true;
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private boolean unidadVacia(String unidad) {
		return unidad == null || unidad.trim().isEmpty();
	}

	// Main

	private void setHandlers(){

		try{
			spin.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

						razon=spincode.get(position);

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}

				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});

			cmbum.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					TextView cmblabel;

					try {

						cmblabel=(TextView)parent.getChildAt(0);
						cmblabel.setTextColor(Color.BLACK);
						cmblabel.setPadding(5, 0, 0, 0);
						cmblabel.setTextSize(18);

						factor=cmbumfact.get(position);
						umcambiar = cmbumlist.get(position);

						clcpeso=app.pesoProm(prodid)*factor;

						if (mu.emptystr(txtCant.getText().toString())) txtCant.setText("0");

						if (gl.dvporpeso==false){
							lblPrec.setText(String.valueOf(mu.round(getPrecio(),2)));
							txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
						} else {
							if (!vEditando){
								lblPrec.setText(String.valueOf(precioventa));
								txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
							}
							vEditando = false;
						}
						actualizarPromocionProvisional();

						txtCant.requestFocus();

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}

				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			chkTieneLote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (chkTieneLote.isChecked()==false){
						txtLote.setEnabled(true);
					}else{
						txtLote.setText(gl.lotedf);
						txtLote.setEnabled(false);
					}
				}
			});

			txtCant.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {

					try{

						if (!vEditando) {

							if (!mu.emptystr(txtCant.getText().toString())&&
									(!txtCant.getText().toString().equals("0"))){

								if (gl.dvporpeso==false){
									if (clcpeso!=0){
										txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
									}else{
										txtkgs.setText(String.valueOf(mu.round(pesoprom*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
									}

								}else{
									txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
								}
							}

						}
						actualizarPromocionProvisional();

					}catch (Exception ex){
						msgbox(ex.getMessage());
					}

				}

			});

			txtCant.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {

					vEditando = false;

					if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
							(event.getAction() == KeyEvent.ACTION_DOWN)) {

						if (!mu.emptystr(txtCant.getText().toString())&&
								(!txtCant.getText().toString().equals("0"))){

							if (gl.dvporpeso==false){
								if (clcpeso!=0){
									txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
								}else{
									txtkgs.setText(String.valueOf(mu.round(pesoprom*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
								}

							}else{
								txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
							}

						}

						return true;
					}
					return false;
				}
			});


		}catch (Exception ex){
			msgbox(ex.getMessage());
		}

	}

	private void showData() {
		Cursor DT;
		String ubas;

		int ex=0;

		try {

			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,PESO_PROMEDIO,UNID_INV "+
					" FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				ubas=DT.getString(9);
				pesoprom = DT.getDouble(8);
				//lblBU.setText(ubas);
				gl.ubas=ubas;
				lblDesc.setText(prodid + " " + DT.getString(7));
			}

			gl.dvPromoElegible=true;
			if (prodPorPeso(prodid)) {
				precioventa = prc.precio(prodid, cant, gl.nivel, um, gl.umpeso, gl.dpeso,um);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
					if (prc.precioespecial>0) {
						precioventa=prc.precioespecial;
						gl.dvPromoElegible=false;
					}
				}
			} else {
				precioventa = prc.precio(prodid, cant, gl.nivel, um, gl.umpeso, 0,um);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
					if (prc.precioespecial>0) {
						precioventa=prc.precioespecial;
						gl.dvPromoElegible=false;
					}
				}
			}
			precioBasePromocion=gl.dvPromoElegible && prc.precioBase>0
					? prc.precioBase : precioventa;
			//#CKFK 20190329_08:37AM Agregué esta validación cuando el precio es 0.
			if (precioventa==0) {
				hidekeyb();
				msgSinPrecio("El producto no tiene definido precio");return;
			}

			precioventa = mu.round(precioventa,2);

			try { db.execSQL("ALTER TABLE T_CxCD ADD COLUMN PROMO_ELEGIBLE INTEGER DEFAULT 1 NOT NULL"); }
			catch (Exception ignored) { }
			asegurarTrazabilidadPromocionTemporal();
			sql="SELECT CANT,PESO,PRECIO,UMVENTA,LOTE,TIENE_LOTE,PRECLISTA,"+
					"IFNULL(PROMO_ELEGIBLE,1),IFNULL(PRECIO_BASE,0),"+
					"IFNULL(DESMON,0),IFNULL(RECARGOMONTO,0),"+
					"IFNULL(CODDESC_APLICADO,0),IFNULL(CODRECARGO_APLICADO,0) "+
					"FROM T_CxCD WHERE CODIGO='"+prodid+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){

				DT.moveToFirst();
				icant=DT.getDouble(0);
				razon=raz;
				umcambiar = DT.getString(3);
				txtPrecio.setText(String.valueOf(DT.getDouble(2)));
				txtkgs.setText(String.valueOf(DT.getDouble(1)));
				gl.tienelote = DT.getInt(5);
				precioBasePromocion=DT.getDouble(8)>0?DT.getDouble(8):
						(DT.getDouble(6)>0?DT.getDouble(6):precioBasePromocion);
				gl.dvPromoElegible=DT.getInt(7)==1;
				descuentoFinal=DT.getDouble(9);
				recargoFinal=DT.getDouble(10);
				codDescFinal=DT.getInt(11);
				codRecargoFinal=DT.getInt(12);
				precioventa=DT.getDouble(2);
				txtLote.setText(DT.getString(4));

				if(gl.tienelote==1){
					chkTieneLote.setChecked(false);
					txtLote.setEnabled(true);
				}else{
					chkTieneLote.setChecked(true);
					txtLote.setEnabled(false);
				}

				setSpinVal(razon);
				setComboValor(umcambiar);

				if (icant>0) parseCant(icant);

				lblPrecVenta.setText(umcambiar);

				vEditando = true;

			}else{

				setSpinVal(devrazon);
				setComboValor(gl.ubas);

				lblPrecVenta.setText(um);
				txtLote.setEnabled(false);

				if (gl.dvporpeso==true){
					lblPrec.setText(String.valueOf(precioventa));
				}

			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			icant=0;
		}

		if (estado.equals("B")){
			spin.setEnabled(false);
		}

	}

	private void forceClose() {
		super.finish();
	}

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

	private void setCant(){
		try {
			cant=Double.parseDouble(txtCant.getText().toString());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cant=-1;
		}
	}

	private void parseCant(double c) {
		try{
			DecimalFormat frmdec = new DecimalFormat("#####");
			double ub;

			ub=c;
			if (ub>0) txtCant.setText(frmdec.format(ub));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Aux

	private void fillSpinner(){
		Cursor DT;
		String icode,iname;

		spincode.add("0");
		spinlist.add("Seleccione una razón ....");

		try {

			sql="SELECT CODIGO,DESCRIPCION FROM P_CODDEV WHERE ESTADO='"+estado+"'  ORDER BY DESCRIPCION";
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

		spin.setAdapter(dataAdapter);

		try {
			spin.setSelection(0);
		} catch (Exception e) {

			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			spin.setSelection(0);
		}

	}

	private void fillcmbUM() {
		Cursor DT;
		String ifactor, iname;

		cmbumfact.add(0.0);
		cmbumlist.add("Seleccione UM....");

		getUMCliente();

		try {
			sql = "SELECT UNIDADSUPERIOR,FACTORCONVERSION FROM P_FACTORCONV WHERE PRODUCTO='" + prodid + "' AND UNIDADSUPERIOR<>'" + gl.umpeso + "'  ORDER BY UNIDADSUPERIOR";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					iname = DT.getString(0);
					ifactor = DT.getString(1);

					cmbumfact.add(Double.parseDouble(ifactor));
					cmbumlist.add(iname);

					DT.moveToNext();
				}

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cmbumlist);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				cmbum.setAdapter(dataAdapter);
			}

			if (gl.tiponcredito == 1) {
				txtPrecio.setEnabled(true);
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cmbum.setSelection(0);
		}

	}

	private void setSpinVal(String vc){
		int pos=0;
		String s;

		for(int i = 0; i < spincode.size(); i++ ) {
			s=spincode.get(i);
			if (s.equalsIgnoreCase(vc)) {
				pos=i;break;
			}
		}

		try {
			spin.setSelection(pos);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			spin.setSelection(0);
		}
	}

	public void setComboValor(String um){
		int pos=0;
		String s;

		for(int i = 0; i <cmbumlist.size(); i++ ) {
			s=cmbumlist.get(i);
			if (s.equalsIgnoreCase((um==gl.umpeso?gl.ubas:um))) {
				pos=i;break;
			}
		}

		try {
			cmbum.setSelection(pos);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cmbum.setSelection(0);
		}
	}

	private void setControls() {

		try{

			txtCant= (EditText) findViewById(R.id.txtMonto);
			rlCant= (RelativeLayout) findViewById(R.id.rlCant);
			lblPrec=(EditText) findViewById(R.id.txtPrecio);
			txtLote = (EditText) findViewById(R.id.txtLote);
			txtkgs = (EditText) findViewById(R.id.txtkgs);
			txtPrecio = (EditText) findViewById(R.id.txtPrecio);

			lblDesc=(TextView) findViewById(R.id.lblFecha);
			lblPrecVenta = (TextView)findViewById(R.id.lblPrecioVenta);
			lblBU=(TextView) findViewById(R.id.lblBU);
			spin = (Spinner) findViewById(R.id.spinner1);
			cmbum = (Spinner) findViewById(R.id.cmbUM);

			prc=new Precio(this,mu,2);

			chkTieneLote = (CheckBox) findViewById(R.id.chkTieneLote);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// #EJC20260730 fix(hh-return-promo-price): reevalua el precio al cambiar
	// cantidad o UM; la informacion visual se muestra solo en la lista principal.
	private void actualizarPromocionProvisional() {
		if (vEditando || txtCant == null || txtkgs == null || lblPrec == null) return;
		double cantidadActual;
		double pesoActual;
		try {
			cantidadActual=Double.parseDouble(txtCant.getText().toString());
			pesoActual=mu.emptystr(txtkgs.getText().toString())
					? 0 : Double.parseDouble(txtkgs.getText().toString());
		} catch (Exception ignored) {
			return;
		}
		if (cantidadActual<=0) return;

		double pesoPrecio=gl.dvporpeso ? pesoActual : 0;
		precioventa=prc.precio(prodid,cantidadActual,gl.nivel,um,gl.umpeso,pesoPrecio,umcambiar);
		precioBasePromocion=prc.precioBase>0 ? prc.precioBase : precioventa;
		codDescFinal=0;
		codRecargoFinal=0;
		descuentoFinal=0;
		recargoFinal=0;
		lblPrec.setText(String.valueOf(mu.round(precioventa,2)));
	}

	private double obtenerPrecioBaseUnidadSeleccionada() {
		double base=precioBasePromocion>0 ? precioBasePromocion : gl.dvprec;
		if (gl.dvporpeso || umcambiar == null || um == null ||
				umcambiar.equalsIgnoreCase(um)) {
			return base;
		}
		double factorSeleccionado=getFactor(umcambiar);
		double factorPrecio=getFactor(um);
		if (factorSeleccionado>0 && factorPrecio>0) {
			base=base*(factorSeleccionado/factorPrecio);
		}
		return base>0 ? base : gl.dvprec;
	}

	private void asegurarTrazabilidadPromocionTemporal() {
		String[] columnas = {
				"PRECIO_BASE REAL DEFAULT 0 NOT NULL", "TOTAL_BASE REAL DEFAULT 0 NOT NULL",
				"DES REAL DEFAULT 0 NOT NULL", "DESMON REAL DEFAULT 0 NOT NULL",
				"RECARGO REAL DEFAULT 0 NOT NULL", "RECARGOMONTO REAL DEFAULT 0 NOT NULL",
				"CODDESC_APLICADO INTEGER DEFAULT 0 NOT NULL",
				"CODRECARGO_APLICADO INTEGER DEFAULT 0 NOT NULL"
		};
		for (String columna : columnas) {
			try { db.execSQL("ALTER TABLE T_CxCD ADD COLUMN "+columna); }
			catch (Exception ignored) { }
		}
	}

	private void getUMCliente(){

		Cursor dt;

		try {

			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();
				um=dt.getString(0);
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("1-"+ e.getMessage());
		}

	}

	private String getUMminima(){
		Cursor dt;
		String umbas="";

		try{

			sql="SELECT UNIDBAS FROM P_PRODUCTO WHERE CODIGO = '" + prodid +"' AND ES_PROD_BARRA = 0";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){

				dt.moveToFirst();
				umbas=dt.getString(0);

			}else {

				sql = " SELECT UM_SALIDA FROM P_PRODUCTO "+
						" WHERE CODIGO = '" + prodid + "' AND ES_PROD_BARRA = 1 ";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0){

					dt.moveToFirst();
					umbas=dt.getString(0);

				}
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return umbas;

	}

	private Double getPrecio(){
		Double prec=0.0,proprecio=0.0;
		Double fact1,fact2;

		try{

			if (!umcambiar.equalsIgnoreCase(um)){
				fact1 = getFactor(umcambiar);
				fact2 = getFactor(um);

				if (fact2>0) proprecio=fact1/fact2;

			} proprecio = (umcambiar.equals(um)?1:proprecio);

			prec = precioventa * proprecio;

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return	prec;
	}

	private Double getFactor(String vUM){
		Cursor DT;
		Double fact=0.0;

		try{

			ummin=gl.ubas;
			sql = "SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE UNIDADSUPERIOR = '" + vUM + "' AND PRODUCTO = '"+ prodid +"' AND UNIDADMINIMA='"+ummin+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {
				DT.moveToFirst();
				fact = Double.parseDouble(DT.getString(0));
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return  fact;
	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		try{
			super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
