package com.dts.roadp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class Deposito extends PBase {

	private Spinner  spinBanco;
	private EditText txtBol;
	private TextView lblEf,lblCheq,lblTot,lblBol;

	private ListView listView;
	private ImageView btnSave,btnCancel,btnSelAll,btnSelNone;

	private ArrayList<clsClasses.clsDepos> items = new ArrayList<clsClasses.clsDepos>();
	private ListAdaptDepos adapter;
	private clsClasses.clsDepos selitem;
	private clsFinDia claseFinDia;
	private DateUtils claseDateUtils;

	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinname = new ArrayList<String>();
	private ArrayList<String> spincuenta = new ArrayList<String>();

	private Runnable printcallback,printclose;
	private printer prn;
	private clsDocDepos ddoc;

	private String bancoid="",cuenta="",bol,corel;
	private int boldep;
	private double tef,tcheq,ttot;
	private boolean depparc; //#HS_20181120_1625 Se cambio el tipo a la variable de entero a boolean.

	private AppMethods app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deposito);

		super.InitBase();
		addlog("Deposito",""+du.getActDateTime(),gl.vend);

		spinBanco = (Spinner) findViewById(R.id.spinner1);
		txtBol = (EditText) findViewById(R.id.txtBoleta);
		lblEf = (TextView) findViewById(R.id.lblpSaldo);lblEf.setText(mu.frmcur(0));
		lblCheq = (TextView) findViewById(R.id.lblCheq);lblCheq.setText(mu.frmcur(0));
		lblTot = (TextView) findViewById(R.id.lblTot);lblTot.setText(mu.frmcur(0));
		lblBol = (TextView) findViewById(R.id.textView2);

		setHandlers();

		fillSpinner();
		fillDocList();

		if (gl.boldep==0) {
			//txtBol.setText(du.sfecha(fecha)+" "+du.shora(fecha)+":"+du.sSecond());
			txtBol.setText("");
			lblBol.setVisibility(View.INVISIBLE);
			txtBol.setVisibility(View.INVISIBLE);
		} else {
			txtBol.setText("");
			lblBol.setVisibility(View.VISIBLE);
			txtBol.setVisibility(View.VISIBLE);
		}

		tef=0;tcheq=0;ttot=0;
		scanValues();

		printcallback= new Runnable() {
			public void run() {
				askPrint();
			}
		};

		printclose= new Runnable() {
			public void run() {
				Deposito.super.finish();
			}
		};

		app = new AppMethods(this, gl, Con, db);
		gl.validimp=app.validaImpresora();
		if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

		prn=new printer(this,printclose,gl.validimp);
		ddoc=new clsDocDepos(this,prn.prw,gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp, "");

		boldep=gl.boldep;
		depparc=gl.depparc;

	}


	// Events

	public void listaDoc(View view){
		try{
			showDocDialog();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void saveDepos(View view){
		try{

			Cursor DT;

			if (gl.peModal.equalsIgnoreCase("TOL")) {

				sql="SELECT * FROM T_DEPOSB";

				DT=Con.OpenDT(sql);

				if (DT.getCount()==0) {
					if (tef>0){
						msgbox("Por favor realice el desglose antes de continuar.");

						if(DT!=null) DT.close();

						return;}
				}

				if(DT!=null) DT.close();

			}

			msgAskSave("Guardar depósito");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void OpenDesglose(View view){
		try{
			gl.totDep =Double.parseDouble( mu.frmcur_sm(tef).replace(",",""));
			startActivity(new Intent(this,desglose.class));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Main

	private void setHandlers(){

		spinBanco.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				TextView spinlabel;

				try {
					spinlabel=(TextView)parentView.getChildAt(0);
					spinlabel.setTextColor(Color.BLACK);
					spinlabel.setPadding(5, 0, 0, 0);
					spinlabel.setTextSize(18);

					bancoid=spincode.get(position);
					cuenta=spincuenta.get(position);

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

	}

	private void scanValues(){
		double val,vef=0,vcheq=0;

		try {
			for(int i = 0; i < items.size(); i++ ) {
				selitem=items.get(i);
				val=selitem.Efect*selitem.Bandera;
				vef+=val;
				val=selitem.Chec*selitem.Bandera;
				vcheq+=val;
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return;
		}

		tef=vef;tcheq=vcheq;ttot=tef+tcheq;

		lblEf.setText(mu.frmcur(tef));
		lblCheq.setText(mu.frmcur(tcheq));
		lblTot.setText(mu.frmcur(ttot));

	}

	private void showDocDialog(){
		final Dialog dialog = new Dialog(this);
		final String rv;
		final int rvi;

		dialog.setContentView(R.layout.activity_depos_doc);
		dialog.setTitle("Documentos pendientes");

		try{
			listView   = (ListView)  dialog.findViewById(R.id.listView1);
			btnSave    = (ImageView) dialog.findViewById(R.id.imageView2);
			btnCancel  = (ImageView) dialog.findViewById(R.id.imageView1);
			btnSelAll  = (ImageView) dialog.findViewById(R.id.imageView10);
			btnSelNone = (ImageView) dialog.findViewById(R.id.imageView11);

			if (depparc==true) {
				btnSelAll.setVisibility(View.VISIBLE);btnSelNone.setVisibility(View.VISIBLE);
			} else {
				btnSelAll.setVisibility(View.INVISIBLE);btnSelNone.setVisibility(View.INVISIBLE);
			}

			adapter=new ListAdaptDepos(this, items);adapter.cursym=gl.peMon;
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
					int flag;
					try {
						clsClasses.clsDepos selitem = (clsClasses.clsDepos) adapter.getItem(position);
						flag=selitem.Bandera;

						if (depparc==true) {
							if (flag==0) flag=1; else flag=0;
							selitem.Bandera=flag;

							adapter.refreshItems();
							adapter.setSelectedIndex(position);
						}

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}
				};
			});

			btnSave.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					scanValues();
					dialog.dismiss();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btnSelAll.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					try {
						for(int i = 0; i < items.size(); i++ ) items.get(i).Bandera=1;
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					adapter.notifyDataSetChanged();
					scanValues();
				}
			});

			btnSelNone.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					try {
						for(int i = 0; i < items.size(); i++ ) items.get(i).Bandera=0;
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					adapter.notifyDataSetChanged();
					scanValues();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void fillDocList(){
		Cursor DT,DTD;
		clsClasses.clsDepos item;
		double val,efect,chec;
		int nchec;

		items.clear();

		try {

			sql="SELECT D_COBRO.COREL,P_CLIENTE.NOMBRE,D_COBRO.TOTAL "+
					"FROM D_COBRO INNER JOIN P_CLIENTE ON P_CLIENTE.CODIGO=D_COBRO.CLIENTE " +
					"WHERE D_COBRO.DEPOS<>'S' AND D_COBRO.ANULADO = 'N'";
			DT=Con.OpenDT(sql);
			//if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				sql="SELECT SUM(Valor)	FROM D_COBROP WHERE (COREL='"+DT.getString(0)+"') AND  (TIPO='E')";
				DTD=Con.OpenDT(sql);
				try {
					DTD.moveToFirst();
					efect=DTD.getDouble(0);
				} catch (Exception ee) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ee.getMessage(),"");
					efect=0;
				}

				sql="SELECT SUM(Valor),Count(Valor) FROM D_COBROP WHERE (COREL='"+DT.getString(0)+"') AND  (TIPO='C')";
				DTD=Con.OpenDT(sql);
				try {
					DTD.moveToFirst();
					chec=DTD.getDouble(0);nchec=DTD.getInt(1);
				} catch (Exception ee) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ee.getMessage(),"");
					chec=0;nchec=0;
				}

				val=efect+chec;

				if (val>0) {
					item = clsCls.new clsDepos();

					item.Cod=DT.getString(0);
					item.Nombre=DT.getString(1);
					item.Valor=val;
					item.Total=DT.getDouble(2);
					item.Efect=efect;
					item.Chec=chec;
					item.NChec=nchec;
					item.Tipo="C";
					item.Bandera=1;
					item.Banco="Cobro";

					items.add(item);
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		try {

			sql="SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.TOTAL,D_FACTURA.SERIE,D_FACTURA.CORELATIVO "+
					"FROM D_FACTURA INNER JOIN P_CLIENTE ON P_CLIENTE.CODIGO=D_FACTURA.CLIENTE " +
					"WHERE D_FACTURA.ANULADO='N' AND D_FACTURA.DEPOS<>'S' ";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				sql="SELECT SUM(Valor)	FROM D_FACTURAP WHERE (COREL='"+DT.getString(0)+"') AND  (TIPO='E')";
				DTD=Con.OpenDT(sql);
				try {
					DTD.moveToFirst();
					efect=DTD.getDouble(0);
				} catch (Exception ee) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ee.getMessage(),"");
					efect=0;
				}

				sql="SELECT SUM(Valor),Count(Valor) FROM D_FACTURAP WHERE (COREL='"+DT.getString(0)+"') AND  (TIPO='C')";
				DTD=Con.OpenDT(sql);
				try {
					DTD.moveToFirst();
					chec=DTD.getDouble(0);nchec=DTD.getInt(1);
				} catch (Exception ee) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ee.getMessage(),"");
					chec=0;nchec=0;
				}

				val=efect+chec;

				if (val>0) {
					item = clsCls.new clsDepos();

					item.Cod=DT.getString(0);
					item.Nombre=DT.getString(3)+"-"+DT.getString(4);
					item.Valor=val;
					item.Total=DT.getDouble(2);
					item.Efect=efect;
					item.Chec=chec;
					item.NChec=nchec;
					item.Tipo="F";
					item.Bandera=1;
					item.Banco="Factura";

					items.add(item);
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private boolean saveDoc(){
		Cursor DT;
		clsClasses.clsDepos item;
		double tot=0,efect=0,chec=0,val;
		String doc,num,banco;
		int nchec=0,it=0,cp;

		if (!checkValues()) return false;

		fecha=du.getActDateTime();
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());

		try {

			for (int i = 0; i < items.size(); i++ ) {

				item=items.get(i);

				if (item.Bandera==1) {
					tot+=item.Valor;
					efect+=item.Efect;
					chec+=item.Chec;
					nchec+=item.NChec;
				}

			}

			tot=mu.round(tot,2);
			efect=mu.round(efect,2);
			chec=mu.round(chec,2);

			corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();

			db.beginTransaction();

			ins.init("D_DEPOS");
			ins.add("COREL",corel);
			ins.add("EMPRESA",((appGlobals) vApp).emp);
			ins.add("FECHA",fecha);
			ins.add("RUTA",((appGlobals) vApp).ruta);
			ins.add("BANCO",bancoid);
			ins.add("CUENTA",cuenta);
			ins.add("REFERENCIA",txtBol.getText().toString());

			ins.add("TOTAL",tot);
			ins.add("TOTEFEC",efect);
			ins.add("TOTCHEQ",chec);
			ins.add("NUMCHEQ",nchec);

			ins.add("IMPRES",0);
			ins.add("STATCOM","N");
			ins.add("ANULADO","N");
			ins.add("CODIGOLIQUIDACION",0);


			db.execSQL(ins.sql());

			for (int i = 0; i < items.size(); i++ ) {

				item=items.get(i);

				if (item.Bandera==1) {

					doc=item.Cod;

					if (item.Efect>0) {

						it+=1;
						item.Efect=mu.round(item.Efect,2);

						ins.init("D_DEPOSD");
						ins.add("COREL",corel);
						ins.add("DOCCOREL",doc);
						ins.add("ITEM",it);
						ins.add("TIPODOC",item.Tipo);
						ins.add("CODPAGO",1);
						ins.add("CHEQUE","N");
						ins.add("MONTO",item.Efect);
						ins.add("BANCO","");
						ins.add("NUMERO","");

						db.execSQL(ins.sql());

					}

					if (item.Tipo.equalsIgnoreCase("F")) {
						sql="SELECT Valor,DESC1,DESC3,CODPAGO FROM D_FACTURAP WHERE (COREL='"+doc+"') AND  (TIPO='C')";
					} else {
						sql="SELECT Valor,DESC1,DESC3,CODPAGO FROM D_COBROP WHERE (COREL='"+doc+"') AND  (TIPO='C')";
					}
					DT=Con.OpenDT(sql);

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						val=DT.getDouble(0);
						num=DT.getString(1);
						banco=DT.getString(2);
						cp=DT.getInt(3);

						it+=1;

						ins.init("D_DEPOSD");
						ins.add("COREL",corel);
						ins.add("DOCCOREL",doc);
						ins.add("ITEM",it);
						ins.add("TIPODOC",item.Tipo);
						ins.add("CODPAGO",cp);
						ins.add("CHEQUE","S");
						ins.add("MONTO",val);
						ins.add("BANCO",banco);
						ins.add("NUMERO",num);

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

					if (item.Tipo.equalsIgnoreCase("F")) {
						sql="UPDATE D_FACTURA SET DEPOS='S' WHERE (COREL='"+doc+"')";
					} else {
						sql="UPDATE D_COBRO SET DEPOS='S' WHERE (COREL='"+doc+"')";
					}
					db.execSQL(sql);

				} // Bandera==1

			}

			//Inserta y válida Desglose de depósito.
			if (gl.peModal.equalsIgnoreCase("TOL")) {

				sql="SELECT * FROM T_DEPOSB";

				DT=Con.OpenDT(sql);

				if (DT.getCount()==0){
					if (tef>0){msgbox("Por favor realice el desglose antes de continuar.");
						db.endTransaction();
						return false;}
				} else {

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						ins.init("D_DEPOSB");
						ins.add("COREL",corel);
						ins.add("DENOMINACION",DT.getDouble(0));
						ins.add("CANTIDAD",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("MONEDA",DT.getString(3));

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

					//sql="Delete from T_DEPOSB";
					//db.execSQL(sql);
				}

				if(DT!=null) DT.close();

			}

			db.execSQL("UPDATE FinDia SET val3=0");

			db.setTransactionSuccessful();
			db.endTransaction();

			return true;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox( e.getMessage());return false;
		}

	}

	private void finishDoc(){
		claseFinDia = new clsFinDia(this);
		try{
			if (!saveDoc()) return;

			if (prn.isEnabled()) {
				ddoc.buildPrint(corel,0,gl.peModal);
				prn.printask(printcallback);
				claseFinDia.updateImpDeposito(0);
			} else if(!prn.isEnabled()){
				ddoc.buildPrint(corel, 0,gl.peModal);
				Toast.makeText(this,"Depósito guardado", Toast.LENGTH_SHORT).show();
				super.finish();
			}
			claseFinDia.updateDeposito(4);
			sql="Delete from T_DEPOSB";
			db.execSQL(sql);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Aux

	private void fillSpinner(){
		Cursor DT;

		try {
			sql="SELECT CODIGO,NOMBRE,CUENTA FROM P_BANCO WHERE (TIPO='D') AND CUENTA = '"+gl.sucur+"' ORDER BY Nombre,Cuenta";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				spincode.add(DT.getString(0));
				spinname.add(DT.getString(1)+" - "+DT.getString(2));
				spincuenta.add(DT.getString(2));

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinname);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinBanco.setAdapter(dataAdapter);

		bancoid="";

	}

	private boolean checkValues(){
		try{
			if (ttot==0) {
				mu.msgbox("No está seleccionado ninguno documento");return false;
			}

			if (mu.emptystr(bancoid)) {
				mu.msgbox("Falta definir banco");return false;
			}

			bol=txtBol.getText().toString();
			if (boldep==1) {
				if (mu.emptystr(bol)) {
					mu.msgbox("Falta definir boleto");txtBol.requestFocus();return false;
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return true;
	}

	private void msgAskSave(String msg) {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finishDoc();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
				}
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

	private void doExit(){
		try {
			super.finish();
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
					try {
						db.execSQL("UPDATE FinDia SET val3=1");
					} catch (Exception e) {
						msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
					}
					finish();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						db.execSQL("UPDATE FinDia SET val3=0");
					} catch (Exception e) {
						msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
					}
					prn.printask(printcallback);
					//finish();
				}
			});

			dialog.show();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Activity Events

	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin guardar depósito");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


}
