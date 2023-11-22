package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MercProp extends PBase {
	
	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;
	
	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos;
	private double icant,iprec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merc_prop);
		
		super.InitBase();
		addlog("MercProp",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		
		emp=((appGlobals) vApp).emp;
		estado=((appGlobals) vApp).devtipo;
		cliid=((appGlobals) vApp).cliente;
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDateTime();
		((appGlobals) vApp).devrazon="0";
		
		clearData();	
		
	}


	// Events
	
	public void showProd(View view) {
		try{
			((appGlobals) vApp).gstr="";
			browse=1;
			itempos=-1;

			((appGlobals) vApp).prodtipo=2;

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

			msgAskComplete("Guardar mercadeo");
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
						prodid=vItem.Cod;
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
		clsClasses.clsCFDV vItem;	
		String s;
				
		items.clear();
		
		try {
			
			sql="SELECT T_CxCD.CODIGO, T_CxCD.CANT,T_CxCD.CODDEV, P_PRODUCTO.DESCCORTA, T_CxCD.ITEM "+
			     "FROM T_CxCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_CxCD.CODIGO "+
			     "ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  vItem = clsCls.new clsCFDV();
			  
			  vItem.Cod=DT.getString(0);
			  vItem.Desc=DT.getString(3);
			  vItem.Valor="Precio : "+DT.getString(2);
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
			if (mu.emptystr(pid)) {return;}

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
			//Intent intent = new Intent(this,DevCliCant.class);
			//startActivity(intent);

			inputCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void inputCant() {
		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Cantidad");//	alert.setMessage("Serial");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText("");
			input.requestFocus();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String s;

					try {
						s=input.getText().toString();
						icant=Double.parseDouble(s);

						if (icant<0) throw new Exception();

						inputPrec();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox("Cantidad incorrecta");
						return;
					}

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
	
	private void updCant(int item){
		Cursor DT;
		String prid,rz;
		double pcant;

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
			((appGlobals) vApp).prod=prid;
			((appGlobals) vApp).gstr=rz;
			//((appGlobals) vApp).dval=pcant;

			Intent intent = new Intent(this,DevCliCant.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}
	
	private void processCant(){
		double cnt;
		String raz;

		try{
			//cnt=((appGlobals) vApp).dval;
			cnt=icant;
			if (cnt<0) return;

			//raz=((appGlobals) vApp).devrazon;
			raz=mu.frmdec(iprec);
			cant=cnt;

			addItem(raz);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void addItem(String raz){
		Cursor DT;
		int id;
		
		try {
			sql="DELETE FROM T_CxCD WHERE item="+itempos;
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			//vSQL="DELETE FROM T_CxCD WHERE CODIGO='"+prodid+"' AND CODDEV='"+raz+"'";
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
			ins.add("PESO",0);
			ins.add("FECHA_CAD",0);
			ins.add("LOTE","");
			ins.add("UMVENTA","");
			ins.add("UMSTOCK","");
			ins.add("UMPESO","");
			ins.add("FACTOR",0);
			ins.add("POR_PESO","");
			ins.add("TIENE_LOTE",0);
			
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
		long f=du.getActDate();
		Double pprec;
		
		corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
		
		try {
			
			db.beginTransaction();
			
			sql="SELECT CODIGO,CANT,CODDEV FROM T_CxCD WHERE CANT>0";
			DT=Con.OpenDT(sql);
	
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
			
				pcod=DT.getString(0);
			   
				try {
					pprec=Double.parseDouble(DT.getString(2));
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					db.endTransaction();
				   	mu.msgbox( e.getMessage());return;
				}				
				
				sql="DELETE FROM D_MERPROPIO WHERE CLIENTE='"+cliid+"' AND PRODUCTO='"+pcod+"' AND FECHA="+f;
				db.execSQL(sql);
				
			  	ins.init("D_MERPROPIO");
				ins.add("CLIENTE",cliid);
				ins.add("PRODUCTO",pcod);
				ins.add("FECHA",f);
				ins.add("CANT",DT.getDouble(1));
				ins.add("PRECIO",pprec);
			
			    db.execSQL(ins.sql());
			    
			    DT.moveToNext();
			}

			db.setTransactionSuccessful();
				
			db.endTransaction();
			
			Toast.makeText(this,"Mercadeo guardado", Toast.LENGTH_SHORT).show();
			
			super.finish();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox( e.getMessage());
		}	
		
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
	
	private void inputPrec() {
		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Precio");//	alert.setMessage("Serial");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText("");
			input.requestFocus();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String s;

					try {
						s=input.getText().toString();
						iprec=Double.parseDouble(s);

						if (iprec<0) throw new Exception();

						processCant();

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						mu.msgbox("Precio incorrecto");return;
					}
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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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

			if (((appGlobals) vApp).closeVenta) super.finish();

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
			if (hasProducts()){
				msgAskExit("Salir sin guardar ");
			} else {
				doExit();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
}
