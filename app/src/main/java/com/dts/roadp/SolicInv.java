package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SolicInv extends PBase {

	private ListView listView;
	private TextView lblTot;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;
	
	private String itemid,prodid;
	private double cant,tot;
	private String emp,estado,ubas,corel;
	private int itempos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solic_inv);
		
		super.InitBase();
		addlog("SolicInv",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		lblTot = (TextView) findViewById(R.id.textView1);

		setHandlers();
		
		browse=0;fecha=du.getActDateTime();prodid="";
		listItems();
		getCorel();
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
				mu.msgbox("No se puede continuar, no ha agregado ninguno producto !");return;
			}

			msgAskComplete("Completar la solicitud y preparar para envio");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void doExit(View view){
		try{
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void doClear(View view){
		try{
			msgAskClear("Borrar la solicitud");
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

						updCant(vItem.Cod);

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
						clsClasses.clsCFDV item = (clsClasses.clsCFDV) lvObj;

						adapter.setSelectedIndex(position);
						prodid=item.Cod;

						msgAskDelete("Eliminar registro");
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}
					return true;
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
		int selidx=-1,pp=0;
				
		items.clear();tot=0;
		
		try {
			
			sql="SELECT D_SOLICINVD.PRODUCTO, D_SOLICINVD.CANT, D_SOLICINVD.TOTAL, P_PRODUCTO.DESCCORTA, 0 "+
			     "FROM D_SOLICINVD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_SOLICINVD.PRODUCTO "+
				 "ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()>0) DT.moveToFirst();
			
			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCFDV();

				vItem.Cod=DT.getString(0);if (vItem.Cod.equalsIgnoreCase(prodid)) selidx=pp;
				vItem.Desc=DT.getString(3);
				vItem.Valor=mu.frmdec(DT.getDouble(2));tot+=DT.getDouble(2);
				s=mu.frmdec(DT.getDouble(1));
				vItem.Fecha="";
				vItem.Valor=mu.frmint(DT.getDouble(1));
				vItem.id=DT.getInt(4);

				items.add(vItem);	

				DT.moveToNext();pp++;
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}
			 
		adapter=new ListAdaptDevCli(this,items);
		listView.setAdapter(adapter);
		
		lblTot.setText(mu.frmdec(tot));
		
		if (selidx>-1) {	
	        adapter.setSelectedIndex(selidx);
	        listView.smoothScrollToPosition(selidx);
		} 

		
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
			Intent intent = new Intent(this,RecargCant.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void updCant(String itemid){
		try{
			browse=2;
			prodid=itemid;
			((appGlobals) vApp).prod=itemid;
			startActivity(new Intent(this,RecargCant.class));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void processCant(){
		try{
			double cnt;
			String raz;

			cnt=((appGlobals) vApp).dval;
			if (cnt<0) return;

			//if (cnt==0) {
			//	toastcent("No puede ser guardada ni enviada solicitud de inventario en cero");return;
			//}

			raz=((appGlobals) vApp).devrazon;
			ubas=((appGlobals) vApp).ubas;
			cant=cnt;

			addItem(raz);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void addItem(String raz){
		
		try {
			sql="DELETE FROM D_SOLICINVD WHERE PRODUCTO='"+prodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	
		try {
			
			ins.init("D_SOLICINVD");
			
			ins.add("COREL",corel);
			ins.add("PRODUCTO",prodid);
			ins.add("COSTO",gl.costo);
			ins.add("CANT",cant);
			ins.add("TOTAL",cant*gl.costo);
			ins.add("UM",ubas);
					
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		try {
			sql="DELETE FROM D_SOLICINVD WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
		
		listItems();
				
	}
	
	private void saveDevol(){
			
		try {			
			sql="UPDATE D_SOLICINV SET STATCOM='N'";
			db.execSQL(sql);
			
			toastcent("Solicitud preparada para envio.");			
			super.finish();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
		}			
	}
	
		
	// Aux 
	
	private void clearData(){
		try {
			sql="DELETE FROM D_SOLICINVD";
			db.execSQL(sql);
			
			super.finish();
			toastcent("Solicitud borrada.");
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}	
	}
	
	private void msgAskClear(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					clearData();
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
	
	private void msgAskDelete(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						sql="DELETE FROM D_SOLICINVD WHERE PRODUCTO='"+prodid+"'";
						db.execSQL(sql);

						prodid="";
						listItems();
					} catch (SQLException e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						msgbox(e.getMessage());
					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

			
	}	

	private boolean hasProducts(){
		Cursor DT;
		
		try {
			sql="SELECT * FROM D_SOLICINVD";	
			DT=Con.OpenDT(sql);
				
			return DT.getCount()>0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}	
	}
	
	private void getCorel() {
		Cursor dt;
		
		try {
			sql="SELECT COREL FROM D_SOLICINV";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();		
			corel=dt.getString(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			corel="0";
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}
	
	
	// Activity Events
	
	@Override
	protected void onResume() {
		try{
			super.onResume();

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
}
