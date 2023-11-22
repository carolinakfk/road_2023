package com.dts.roadp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DevolBod extends PBase {

	private ListView listView;

	private ArrayList<clsClasses.clsExist> items= new ArrayList<clsClasses.clsExist>();
	private ListAdaptDevBod adapter;
	private clsClasses.clsExist selitem;

	private String prodid,savecant;
	private double cant,disp,dispm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devol_bod);

		super.InitBase();
		addlog("DevolBod",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);

		browse=0;
		fecha=du.getFechaActual();

		setHandlers();

		fillData();
		listItems();
	}

	// Events

	public void showProd(View view) {
		try{
			((appGlobals) vApp).gstr="";
			((appGlobals) vApp).tipo=1;
			browse=1;

			Intent intent = new Intent(this, Exist.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void finishDevol(View view){
		try{
			if (!hasProducts()) {
				mu.msgbox("¡No puede continuar, no ha agregado ninguno producto!");return;
			}

			msgAskComplete("Aplicar devolución");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Main

	private void setHandlers(){

		try{
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsClasses.clsExist vItem = (clsClasses.clsExist)lvObj;

						prodid=vItem.Cod;

						adapter.setSelectedIndex(position);

						savecant="";
						setCant();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void listItems(){
		Cursor DT;
		clsClasses.clsExist vItem;
		String cod,name;
		double val,valm;

		items.clear();

		try {

			sql="SELECT T_DEVOL.CODIGO,P_PRODUCTO.DESCCORTA,T_DEVOL.CANT,T_DEVOL.CANTM "+
					"FROM T_DEVOL INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_DEVOL.CODIGO  WHERE 1=1 ";
			sql+="ORDER BY P_PRODUCTO.DESCCORTA";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod=DT.getString(0);
				name=DT.getString(1);
				val=DT.getDouble(2);
				valm=DT.getDouble(3);

				vItem = clsCls.new clsExist();

				vItem.Cod=cod;
				vItem.Fecha=cod;
				vItem.Desc=name;
				vItem.Valor=mu.frmdec(val)+" B";

				if (valm==0) {
					vItem.ValorM=" ";
				} else {
					vItem.ValorM=mu.frmdec(valm)+" M";
				}

				items.add(vItem);

				DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}

		adapter=new ListAdaptDevBod(this,items);
		listView.setAdapter(adapter);
	}

	private void processItem(){
		try{
			String pid;

			pid=((appGlobals) vApp).gstr;
			if (mu.emptystr(pid)) {return;}

			prodid=pid;

			savecant="";
			setCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setCant(){

		try{
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			getDisp();
			alert.setTitle("Ingrese la cantidad ");
			alert.setMessage("Existencias :  "+disp+" (B) / "+dispm+" (M)");

			final EditText input = new EditText(this);
			input.setText(savecant);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);

			alert.setView(input);

			alert.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
				}
			});

			alert.setNeutralButton("Estado Bueno",  new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
					setCant("B",input.getText().toString());
				}
			});

			alert.setPositiveButton("Estado Malo", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
					setCant("M",input.getText().toString());
				}
			});

			final AlertDialog dialog = alert.create();
			dialog.show();

			showkeyb();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setCant(String est,String s){
		double val;

		try{
			try {
				val=Double.parseDouble(s);
				if (val<0) throw new Exception();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				mu.msgbox("Cantidad incorrecta");return;
			}

			if (est.equalsIgnoreCase("B")) {
				if (val>disp) {
					savecant=s;
					setCant();
					mu.msgbox("Cantidad mayor que existencia : "+disp);
					return;
				}
			} else {
				if (val>dispm) {
					savecant=s;
					setCant();
					mu.msgbox("Cantidad mayor que existencia : "+dispm);
					return;
				}
			}

			cant=val;

			addItem(est);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void addItem(String est){

		try {
			sql="INSERT INTO T_DEVOL VALUES('"+prodid+"',0,0)";
			db.execSQL(sql);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		try {
			if (est.equalsIgnoreCase("B")) {
				sql="UPDATE T_DEVOL SET CANT="+cant+" WHERE CODIGO='"+prodid+"'";
			} else {
				sql="UPDATE T_DEVOL SET CANTM="+cant+" WHERE CODIGO='"+prodid+"'";
			}
			db.execSQL(sql);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return;
		}

		try {
			sql="DELETE FROM T_DEVOL WHERE CANT=0 AND CANTM=0";
			db.execSQL(sql);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		listItems();

	}

	private void clearProd() {

		try {
			sql="DELETE FROM T_DEVOL";
			db.execSQL(sql);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		items.clear();
		adapter=new ListAdaptDevBod(this,items);
		listView.setAdapter(adapter);

	}

	private void saveDevol(){
		Cursor DT;
		String corel,pcod;
		Double pcant,pcantm;
		int i;

		corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();

		try {

			db.beginTransaction();

			ins.init("D_MOV");

			ins.add("COREL",corel);
			ins.add("RUTA",((appGlobals) vApp).ruta);
			ins.add("ANULADO","N");
			ins.add("FECHA",fecha);
			ins.add("TIPO","D");
			ins.add("USUARIO",((appGlobals) vApp).vend);
			ins.add("REFERENCIA","");
			ins.add("STATCOM","N");
			ins.add("IMPRES",0);
			ins.add("CODIGOLIQUIDACION",0);

			db.execSQL(ins.sql());


			sql="SELECT CODIGO,CANT,CANTM FROM T_DEVOL WHERE CANT+CANTM>0";
			DT=Con.OpenDT(sql);
			i=1;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				pcod=DT.getString(0);
				pcant=DT.getDouble(1);
				pcantm=DT.getDouble(2);

				ins.init("D_MOVD");

				ins.add("COREL",corel);
				ins.add("PRODUCTO",pcod);
				ins.add("CANT",pcant);
				ins.add("CANTM",pcantm);
				ins.add("PESO",0);
				ins.add("PESOM",0);
				ins.add("LOTE",pcod);
				ins.add("CODIGOLIQUIDACION",0);

				db.execSQL(ins.sql());

				if (pcant>0) {
					sql="UPDATE P_STOCK SET CANT=CANT-"+pcant+" WHERE CODIGO='"+pcod+"'";
					db.execSQL(sql);
					i+=1;
				}

				if (pcantm>0) {
					sql="UPDATE P_STOCK SET CANTM=CANTM-"+pcant+" WHERE CODIGO='"+pcod+"'";
					db.execSQL(sql);
					i+=1;
				}

				DT.moveToNext();
			}

			db.setTransactionSuccessful();
			db.endTransaction();

			Toast.makeText(this,"Devolución aplicada", Toast.LENGTH_SHORT).show();
			super.finish();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox( e.getMessage());
		}

	}

	// Aux

	private void getDisp(){
		Cursor DT;

		try {
			sql="SELECT CANT,CANTM FROM P_STOCK WHERE CODIGO='"+prodid+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {
				disp=0;dispm=0;	return;
			}

			DT.moveToFirst();

			disp=DT.getDouble(0);
			dispm=DT.getDouble(1);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}
	}

	private void fillData(){
		Cursor DT;
		String cod;
		Double cant,cantm;

		try {
			sql="DELETE FROM T_DEVOL";
			db.execSQL(sql);

			sql="DELETE FROM P_STOCK WHERE CANT=0 AND CANTM=0";
			db.execSQL(sql);

			sql="SELECT CODIGO,CANT,CANTM FROM P_STOCK";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod=DT.getString(0);
				cant=DT.getDouble(1);
				cantm=DT.getDouble(2);

				sql="INSERT INTO T_DEVOL VALUES('"+cod+"',"+cant+","+cantm+")";
				db.execSQL(sql);

				DT.moveToNext();
			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}
	}

	private void msgAskExit(String msg) {
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

	}

	private void msgAskComplete(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveDevol();
					//doExit();
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

	public void msgAskLimpiar(View view) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿Eliminar todos los productos de la lista?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					clearProd();
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

	private boolean hasProducts(){
		Cursor DT;

		try {
			sql="SELECT CODIGO FROM T_DEVOL";
			DT=Con.OpenDT(sql);

			return DT.getCount()>0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
	}

	private void doExit(){
		try{
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			//if (((appGlobals) vApp).closeVenta) super.finish();

			if (browse==1) {
				browse=0;
				processItem();return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin terminar devolución");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
