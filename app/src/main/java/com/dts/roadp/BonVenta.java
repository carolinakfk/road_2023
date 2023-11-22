package com.dts.roadp;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BonVenta extends PBase {

	private ListView listView;
	private TextView lblTipo;
	
	private ArrayList<clsClasses.clsBonifProd> items = new ArrayList<clsClasses.clsBonifProd>();
	private ListAdaptBonif adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bon_venta);
		
		super.InitBase();
		addlog("BonVenta",""+du.getActDateTime(),gl.vend);
		
		listView =  (ListView) findViewById(R.id.listView1);
		lblTipo =  (TextView) findViewById(R.id.textView2);
	
		sethandlers();
		
		listItems();
	}
	
	// Events
	
	public void nextScreen(View view) {
		try{
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void sethandlers() {

		try{
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					adapter.setSelectedIndex(position);
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}

		
	}
	
	
	// Main
	
	
	private void listItems() {
		Cursor DT;
		clsClasses.clsBonifProd item;
		double ddisp,bcant;
		
		items.clear();
		
	
		try {
							
			sql="SELECT T_BONITEM.BONIID, P_PRODUCTO.DESCLARGA, SUM(T_BONITEM.CANT) " +
				"FROM P_PRODUCTO INNER JOIN T_BONITEM ON P_PRODUCTO.CODIGO = T_BONITEM.BONIID " +
				"GROUP BY T_BONITEM.BONIID, P_PRODUCTO.DESCLARGA  ORDER BY P_PRODUCTO.DESCLARGA";
			DT=Con.OpenDT(sql);
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  item = clsCls.new clsBonifProd();
			  
			  item.id=DT.getString(0);
			  item.nombre=DT.getString(1);
			  
			  bcant=DT.getDouble(2);
			  item.cant=bcant;
			  item.cantmin=0;
			  
			  item.precio=0;
			  item.prstr="";
			  item.costo=0;
			  item.flag=1;	
			  
			  items.add(item);	
			 
			  DT.moveToNext();
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage()+"\n"+sql);
		}	
		
		
		// Faltante
		
		try {

			sql="SELECT T_BONIFFALT.PRODUCTO, P_PRODUCTO.DESCLARGA, SUM(T_BONIFFALT.CANT) " +
				"FROM P_PRODUCTO INNER JOIN T_BONIFFALT ON P_PRODUCTO.CODIGO = T_BONIFFALT.PRODUCTO " +
				"GROUP BY T_BONIFFALT.PRODUCTO, P_PRODUCTO.DESCLARGA  ORDER BY P_PRODUCTO.DESCLARGA";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				item = clsCls.new clsBonifProd();

				item.id="0000";item.nombre="";
				item.cant=0;item.cantmin=0;
				item.precio=0;item.prstr="";
				item.costo=0;item.flag=-1;	

				items.add(item);					
			}
			
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				item = clsCls.new clsBonifProd();

				item.id=DT.getString(0);
				item.nombre=DT.getString(1);

				bcant=DT.getDouble(2);
				item.cant=bcant;
				item.cantmin=0;

				item.precio=0;
				item.prstr="";
				item.costo=0;
				item.flag=0;	

				items.add(item);	

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage()+"\n"+sql);
		}			
		
		
		adapter=new ListAdaptBonif(this,items);
		listView.setAdapter(adapter);
		
	}
	

	// Aux
	

}
