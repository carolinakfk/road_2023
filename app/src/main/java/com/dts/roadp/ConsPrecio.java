package com.dts.roadp;

import java.util.ArrayList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ConsPrecio extends PBase {
	
	private ListView listView;
	private EditText txtFilter;
	private Spinner  spinNivel;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();;
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();
	
	private int nivel=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cons_precio);
		
		super.InitBase();
		addlog("ConsPrecio",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		txtFilter = (EditText) findViewById(R.id.txtMonto);
		spinNivel = (Spinner) findViewById(R.id.spinner1);

		fillSpinner();
		
		setHandlers();
		
		listItems();
		
	}

	// Main
	
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

			spinNivel.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;
					String scod;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(16);

						scod=spincode.get(position);
						nivel=Integer.parseInt(scod);

						listItems();

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}

				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		
	}
	
	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		String vF,cod,name,um;
		double val;
				
		items.clear();
		
		vF=txtFilter.getText().toString().replace("'","");;
		
		try {
			/*
			sql="SELECT P_PRODUCTO.CODIGO,P_PRODUCTO.DESCCORTA,P_PRODPRECIO.PRECIO "+
			     "FROM P_PRODUCTO INNER JOIN P_PRODPRECIO ON P_PRODPRECIO.CODIGO=P_PRODUCTO.CODIGO  " +
			     " WHERE P_PRODPRECIO.NIVEL="+nivel+" ";
			if (vF.length()>0) {sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";}
			sql+="ORDER BY P_PRODUCTO.DESCCORTA";
			*/
			
			sql="SELECT  P_PRODPRECIO.CODIGO, P_PRODUCTO.DESCLARGA,P_PRODPRECIO.PRECIO, P_PRODPRECIO.UNIDADMEDIDA "+
				"FROM  P_PRODPRECIO INNER JOIN	P_PRODUCTO ON P_PRODPRECIO.CODIGO = P_PRODUCTO.CODIGO "+
				"WHERE (P_PRODPRECIO.NIVEL = "+nivel+") ";
			if (vF.length()>0) {sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";}					
			sql+="ORDER BY P_PRODUCTO.DESCLARGA, P_PRODPRECIO.UNIDADMEDIDA";	
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  cod=DT.getString(0);	
			  name=DT.getString(1);
			  val=DT.getDouble(2);
			  um=DT.getString(3);
			
			  vItem = clsCls.new clsCFDV();
			  
			  vItem.Fecha=cod+" - "+um;
			  vItem.Desc=name;
			  vItem.Valor=mu.frmdec(val);
			  
			  items.add(vItem);	
			 
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox(e.getMessage());
	    }
			 
		adapter=new ListAdaptCFDV(this,items);
		listView.setAdapter(adapter);
	}


	// Aux

	private void fillSpinner(){
		Cursor DT;
		String icode,iname;

		try {

			sql="SELECT Codigo,Nombre FROM P_NIVELPRECIO ORDER BY Nombre";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				icode=DT.getString(0);
				iname=DT.getString(1);

				spincode.add(icode);
				spinlist.add(iname);

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinNivel.setAdapter(dataAdapter);

		try {
			spinNivel.setSelection(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			nivel=0;
		}

	}		

}
