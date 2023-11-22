package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class Recarga extends PBase {

	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;

	private printer prn;
	private Runnable printclose;
	private clsDocMov mdoc;
	
	private String itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recarga);

		super.InitBase();
		addlog("Recarga",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);

		emp=((appGlobals) vApp).emp;
		estado="R";
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDateTime();
		((appGlobals) vApp).devrazon="0";
		
		clearData();
		
		printclose= new Runnable() {
		    public void run() {
		    	Recarga.super.finish();
		    }
		};
		
		prn=new printer(this,printclose,gl.validimp);
		
		mdoc=new clsDocMov(this,prn.prw,"Recarga",gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp, "");
	}


	// Events
	
	public void showProd(View view) {
		try{
			((appGlobals) vApp).gstr="";
			browse=1;
			itempos=-1;
			Intent intent = new Intent(this,Producto.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void finishDevol(View view){
		try{
			if (!hasProducts()) {
				mu.msgbox("No puede continuar, no ha agregado ninguno producto !");return;
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

						//updCant(vItem.id);
						updCantCod(vItem.Cod);

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
			
			sql="SELECT T_CxCD.CODIGO, T_CxCD.CANT, ' ', P_PRODUCTO.DESCCORTA, T_CxCD.ITEM "+
			     "FROM T_CxCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_CxCD.CODIGO "+
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
	
	private void processItem(){
		String pid;

		try{
			pid=((appGlobals) vApp).gstr;
			if (mu.emptystr(pid)) return;

			prodid=pid;

			setCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void setCant(){
		try{
			browse=2;

			itempos=-1;
			((appGlobals) vApp).prod=prodid;
			((appGlobals) vApp).gstr="";
			((appGlobals) vApp).gstr2="";
			Intent intent = new Intent(this,RecargCant.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void updCant(int item){
		Cursor DT;
		String prid,rz;
		double pcant=0;

		try{
			try {
				sql="SELECT CODIGO,CODDEV,CANT FROM T_CxCD WHERE Item="+item;
				DT=Con.OpenDT(sql);
				DT.moveToFirst();

				prid=DT.getString(0);
				rz=DT.getString(1);

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				return;
			}

			browse=2;

			itempos=item;

		/*
		((appGlobals) vApp).prod=prid;
		((appGlobals) vApp).gstr=rz;
		//((appGlobals) vApp).dval=pcant;

		Intent intent = new Intent(this,DevCliCant.class);
		*/


			((appGlobals) vApp).prod=prid;
			((appGlobals) vApp).gstr="";
			((appGlobals) vApp).gstr2=""+pcant;
			Intent intent = new Intent(this,RecargCant.class);

			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void updCantCod(String item){
		Cursor DT;
		String prid,rz;
		double pcant=0;

		try{
			browse=2;
			prodid=item;

			((appGlobals) vApp).prod=item;
			((appGlobals) vApp).gstr="";
			((appGlobals) vApp).gstr2=""+pcant;
			Intent intent = new Intent(this,RecargCant.class);

			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void processCant(){
		double cnt;
		String raz;

		try{
			cnt=((appGlobals) vApp).dval;
			if (cnt<0) return;

			raz=((appGlobals) vApp).devrazon;
			cant=cnt;

			addItem(raz);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void addItem(String raz){
		Cursor DT;
		int id;
		
		/*
		try {
			vSQL="DELETE FROM T_CxCD WHERE item="+itempos;
			db.execSQL(vSQL);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		*/
		
		try {
			sql="DELETE FROM T_CxCD WHERE CODIGO='"+prodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			sql="SELECT MAX(Item) FROM T_CxCD";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			id=DT.getInt(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			id=0;
		}	
		
		id+=1;

		try {
			
			ins.init("T_CxCD");
			
			ins.add("Item",id);
			ins.add("CODIGO",prodid);
			ins.add("CANT",cant);
			ins.add("CODDEV",raz);
			ins.add("TOTAL",0);
			ins.add("PRECIO",0);
			ins.add("PRECLISTA",0);
			ins.add("REF","");
			
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		try {
			sql="DELETE FROM T_CxCD WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
		
		listItems();
				
	}
	
	private void saveDevol(){
		Cursor DT;
		String corel,pcod;
		Double pcant;
		
		corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
		
		try {
			
			db.beginTransaction();
			
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
			
			sql="SELECT Item,CODIGO,CANT,CODDEV FROM T_CxCD WHERE CANT>0";
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

			db.setTransactionSuccessful();				
			db.endTransaction();
			
			//Toast.makeText(this,"Recarga guardada", Toast.LENGTH_SHORT).show();
			
			//super.finish();			
			
			try {
				if (mdoc.buildPrint(corel,1)) prn.printask(printclose);
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				mu.msgbox(e.getMessage());
			}
			
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
	    
	    //vSQL="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"' AND LOTE='"+pcod+"' AND DOCUMENTO='1'";	
	    //db.execSQL(vSQL);	
	    
	    sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"' ";	
	    db.execSQL(sql);
	    
	}
	
	
	// Aux 
	
	private void clearData(){
		try {
			sql="DELETE FROM T_CxCD";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}	
	}
	
	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

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
	
	private void msgAskComplete(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveDevol();
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
			sql="SELECT CODIGO FROM T_CxCD";	
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

			if (browse==2) {
				browse=0;
				processCant();return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	
	}

	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin terminar recarga");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	


}
