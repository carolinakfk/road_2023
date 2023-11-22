package com.dts.roadp;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RecargaAuto extends PBase {

	private ListView listView;

	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;

	private String itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recarga_auto);

		super.InitBase();
		addlog("RecargaAuto",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);

		emp=((appGlobals) vApp).emp;
		estado="R";

		setHandlers();

		browse=0;
		fecha=du.getActDateTime();
		((appGlobals) vApp).devrazon="0";

		listItems();

	}


	// Events

	public void finishDevol(View view){
		try{
			if (!hasProducts()) {
				mu.msgbox("No puede continuar, la recarga esta vacia !");return;
			}

			msgAskComplete("Aplicar la recarga");
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
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						adapter.setSelectedIndex(position);
					} catch (Exception e) {
						mu.msgbox( e.getMessage());
					}
				};
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		String s;

		items.clear();

		try {

			sql="SELECT P_STOCK_APR.CODIGO, P_STOCK_APR.CANT, ' ', P_PRODUCTO.DESCCORTA, P_STOCK_APR.PESO "+
					"FROM P_STOCK_APR INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK_APR.CODIGO "+
					"ORDER BY P_PRODUCTO.DESCCORTA";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCFDV();

				vItem.Cod=DT.getString(0);
				vItem.Desc=DT.getString(3);
				vItem.Valor=DT.getString(2);
				s=mu.frmdec(DT.getDouble(1));
				vItem.Fecha=s;
				vItem.id=DT.getInt(4);

				items.add(vItem);	

				DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}

		adapter=new ListAdaptDevCli(this,items);
		listView.setAdapter(adapter);
	}

	private void saveRecarga(){
		Cursor DT;
		String corel,pcod;
		Double pcant;

		corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();

		try {

			db.beginTransaction();
			
			db.execSQL("DELETE FROM P_STOCK");

			ins.init("D_MOV");

			ins.add("COREL",corel);
			ins.add("RUTA",((appGlobals) vApp).ruta);
			ins.add("ANULADO","N");
			ins.add("FECHA",fecha);
			ins.add("TIPO","R");
			ins.add("USUARIO",((appGlobals) vApp).vend);
			ins.add("REFERENCIA","");
			ins.add("STATCOM","N");
			ins.add("IMPRES",0);
			ins.add("CODIGOLIQUIDACION",0);

			db.execSQL(ins.sql());

			sql="SELECT PESO,CODIGO,CANT FROM P_STOCK_APR WHERE CANT>0";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				pcod=DT.getString(1);
				pcant=DT.getDouble(2);

				ins.init("D_MOVD");

				ins.add("COREL",corel);
				ins.add("PRODUCTO",pcod);
				ins.add("CANT",pcant);
				ins.add("CANTM",0);
				ins.add("PESO",0);
				ins.add("PESOM",0);
				ins.add("LOTE",pcod);
				ins.add("CODIGOLIQUIDACION",0);

				db.execSQL(ins.sql());

				updateStock(corel,pcod,pcant);

				DT.moveToNext();
			}
			
			db.execSQL("DELETE FROM P_STOCK_APR");

			db.setTransactionSuccessful();

			db.endTransaction();

			//Toast.makeText(this,"Recarga guardada", Toast.LENGTH_SHORT).show();
			//super.finish();
			
			msgExit("Recarga aplicada");
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox( e.getMessage());
		}	

	}

	private void updateStock(String corel,String pcod,double pcant) {
		Cursor DT;

		try {

			ins.init("P_STOCK");

			ins.add("CODIGO",pcod);
			ins.add("CANT",0);
			ins.add("CANTM",0);
			ins.add("PESO",0);
			ins.add("plibra",0);
			ins.add("LOTE",pcod);
			ins.add("DOCUMENTO","1");

			ins.add("FECHA",fecha);
			ins.add("ANULADO",0);
			ins.add("CENTRO","");
			ins.add("STATUS","A");
			ins.add("ENVIADO",0);
			ins.add("CODIGOLIQUIDACION",0);
			ins.add("COREL_D_MOV",corel);

			//msgbox(ins.sql());

			db.execSQL(ins.sql());

			sql="SELECT * FROM P_STOCK  WHERE CODIGO='"+pcod+"' ";	
			DT=Con.OpenDT(sql);
			//msgbox(""+DT.getCount());

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox(e.getMessage());
		}

		sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"' ";	
		db.execSQL(sql);

	}


	// Aux 

	private void msgAskComplete(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					msgAskSave("¿Está seguro?");
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
	
	private void msgAskSave(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveRecarga();
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
	
	private void msgExit(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();

					((appGlobals) vApp).tipo=0;
					Intent intent = new Intent(RecargaAuto.this, Exist.class);
					startActivity(intent);
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
			sql="SELECT CODIGO FROM P_STOCK_APR";	
			DT=Con.OpenDT(sql);

			return DT.getCount()>0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}	
	}

	private void doExit(){
		super.finish();
	}
	

}
