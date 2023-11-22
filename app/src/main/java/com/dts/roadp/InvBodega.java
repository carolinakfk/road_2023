package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCD;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

public class InvBodega extends PBase {

	private ListView listView;
	private EditText txtFilter;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inv_bodega);
		
		super.InitBase();
		addlog("InvBodega",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		txtFilter = (EditText) findViewById(R.id.txtMonto);

		setHandlers();
		
		listItems();
		browse=0;
		
	}
	
	
	// Events
	
	public void limpiaFiltro(View view) {
		txtFilter.setText("");	
	}

	private void setHandlers(){

		try{
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
					try {
						adapter.setSelectedIndex(position);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
				};
			});


			txtFilter.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {
					int tl;

					tl=txtFilter.getText().toString().length();

					if (tl==0 || tl>1) listItems();
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
	
	
	// Main
		
	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		String vF,cod,name;
		double val;
				
		items.clear();
		
		vF=txtFilter.getText().toString();
		
		try {
			
			sql="SELECT P_STOCKINV.CODIGO,P_PRODUCTO.DESCCORTA,P_STOCKINV.CANT "+
			     "FROM P_STOCKINV INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCKINV.CODIGO  WHERE 1=1 ";
			if (vF.length()>0) {sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";}
			sql+="ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  cod=DT.getString(0);	
			  name=DT.getString(1);
			  val=DT.getDouble(2);
			
			  vItem = clsCls.new clsCFDV();
			  
			  vItem.Fecha=cod;
			  vItem.Desc=name;
			  vItem.Valor=mu.frmdec(val);
			  
			  items.add(vItem);	
			 
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptCFDV(this,items);
		listView.setAdapter(adapter);
	}

	// Actualizacon
	
	public void updDisp(){

		try{
			((appGlobals) vApp).gstr="*";
			browse=1;

			Intent intent = new Intent(this,ActDisp.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void msgAskUpd(View view) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Actualizar inventario ?");

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
	
	// Activity Events
	
	protected void onResume() {
		String rslt;

		try{
			super.onResume();

			if (browse==1) {
				browse=0;
				rslt=((appGlobals) vApp).gstr;

				if (rslt.equalsIgnoreCase("&")) {
					listItems();
					Toast.makeText(this,"Inventario actualizado.", Toast.LENGTH_SHORT).show();
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
}
