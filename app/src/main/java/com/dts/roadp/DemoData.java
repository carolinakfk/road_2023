package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsDemoDlg;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DemoData extends PBase {

	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDemo adapter;
	private clsClasses.clsCFDV selitem;

	private String sprid="0001000000";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_data);
		
		super.InitBase();
		addlog("DemoData",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		
		gl.clsDemo =clsCls.new clsDemoDlg();
		
		setHandlers();
	}
		
	
	public void prevScreen(View view) {
		super.InitBase();	
	}
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				try {
					Object lvObj = listView.getItemAtPosition(position);
					selitem = (clsClasses.clsCFDV) lvObj;
		           	
					adapter.setSelectedIndex(position);
		    		
					sprid=selitem.Cod;					
					
					gl.clsDemo.tipo=selitem.id;
					gl.clsDemo.Cod=selitem.Cod;
					gl.clsDemo.Desc=selitem.Desc;
					gl.clsDemo.val=selitem.val;
					gl.clsDemo.flag=0;
					
					if (selitem.id==0) browse=1; else browse=2;
					
					Intent intent = new Intent(DemoData.this,DemoDataDlg.class);
					startActivity(intent);
					
		        } catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			   	   mu.msgbox(e.getMessage());
		        }
			};
	    });
	    
	}	
	
	// 0001000000  0001 D  0002 B
	
	private void listItems() {
		Cursor DT;
		clsClasses.clsCFDV item ;	
		String s;
		int iid,idx=0;
				
		items.clear();
		
		try {

			sql="SELECT CODIGO,NOMBRE FROM P_CLIENTE WHERE CODIGO='0001000000'";		
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;

			DT.moveToFirst();iid=0;

			item = clsCls.new clsCFDV();

			if (DT.getString(0).equalsIgnoreCase(sprid)) idx=0;
			item.Cod=DT.getString(0);
			item.Desc=DT.getString(1);
			item.val=-1;

			item.Valor="Cliente predeterminado"; 
			item.id=0;

			items.add(item);	

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}		

		try {
			
			sql="SELECT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA, P_PRODPRECIO.PRECIO "+
			     "FROM P_PRODUCTO INNER JOIN P_PRODPRECIO ON P_PRODPRECIO.CODIGO=P_PRODUCTO.CODIGO "+
				 "WHERE ((P_PRODUCTO.CODIGO='0001') OR (P_PRODUCTO.CODIGO)='0002') AND (P_PRODPRECIO.NIVEL=1)  ORDER BY P_PRODUCTO.CODIGO";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();iid=1;
			
			
			while (!DT.isAfterLast()) {

				item = clsCls.new clsCFDV();
		
				if (DT.getString(0).equalsIgnoreCase(sprid)) idx=iid;
				item.Cod=DT.getString(0);
				item.Desc=DT.getString(1);
				item.val=DT.getDouble(2);
				
				if (iid==1) item.Valor="Producto con descuento"; else item.Valor="Producto con bonificación";
				item.id=iid;iid++;
				
				items.add(item);	
			 
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }		
		
		
		try {
			
			sql="SELECT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA, P_PRODPRECIO.PRECIO "+
			     "FROM P_PRODUCTO INNER JOIN P_PRODPRECIO ON P_PRODPRECIO.CODIGO=P_PRODUCTO.CODIGO "+
				 "WHERE ((P_PRODUCTO.CODIGO<>'0001') AND (P_PRODUCTO.CODIGO)<>'0002') AND (P_PRODPRECIO.NIVEL=1)  ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();iid=3;
			
			
			while (!DT.isAfterLast()) {

				item = clsCls.new clsCFDV();
		
				if (DT.getString(0).equalsIgnoreCase(sprid)) idx=iid;
				item.Cod=DT.getString(0);
				item.Desc=DT.getString(1);
				item.val=DT.getDouble(2);
				item.Valor="";
				item.id=iid;iid++;
				
				items.add(item);	
			 
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptDemo(this,items);
		listView.setAdapter(adapter);		
		
		try {
			listView.setSelection(idx);
			adapter.setSelectedIndex(idx);
			listView.smoothScrollToPosition(idx);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	
	}
	
	private void updateClient() {
		if (gl.clsDemo.flag==0) return;
		
		gl.clsDemo.flag=0;
		
		try {
			upd.init("P_CLIENTE");
			upd.add("NOMBRE",gl.clsDemo.Desc);
			upd.Where("CODIGO='0001000000'");
	
			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());return;
		}	
		
		selitem.Desc=gl.clsDemo.Desc;
		adapter.notifyDataSetChanged();
		
	}
	
	private void updateProd() {
		if (gl.clsDemo.flag==0) return;
		
		gl.clsDemo.flag=0;
		
		try {
			db.beginTransaction();
			
			upd.init("P_PRODUCTO");
			upd.add("DESCCORTA",gl.clsDemo.Desc);
			upd.add("DESCLARGA",gl.clsDemo.Desc);
			upd.Where("CODIGO='"+gl.clsDemo.Cod+"'");
	
			db.execSQL(upd.SQL());
			
			upd.init("P_PRODPRECIO");
			upd.add("PRECIO",gl.clsDemo.val);
			upd.Where("CODIGO='"+gl.clsDemo.Cod+"' AND (NIVEL=1)");
	
			db.execSQL(upd.SQL());
			
			db.setTransactionSuccessful();
			db.endTransaction();
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox( e.getMessage());return;
		}
		
		selitem.Desc=gl.clsDemo.Desc;
		selitem.val=gl.clsDemo.val;
		adapter.notifyDataSetChanged();	
	}
	
	
	// Activity Events
	
	@Override
 	protected void onResume() {
		try{
			super.onResume();

			if (browse==1) {
				browse=0;
				updateClient();
				return;
			}

			if (browse==2) {
				browse=0;
				updateProd();
				return;
			}

			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	
	/*
	try {
		vSQL="SELECT INITPATH FROM P_EMPRESA WHERE EMPRESA='"+emp+"'";
		DT=Con.OpenDT(vSQL);
		DT.moveToFirst();
	
		String sim=DT.getString(0);
		sinimp=sim.equalsIgnoreCase("S");
		
	} catch (Exception e) {
		sinimp=false;
    }	
	*/
	
	
}
