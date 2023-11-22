package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class Municipio extends PBase {

	private ListView listView;
	private EditText txtFilter;
	
	private ArrayList<clsCD> items = new ArrayList<clsCD>();
	private ListAdaptCD adapter;

	private String itemid,prname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_municipio);
		
		super.InitBase();
		addlog("Municipio",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		txtFilter = (EditText) findViewById(R.id.txtMonto);

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
						Object lvObj = listView.getItemAtPosition(position);
						clsCD vItem = (clsCD)lvObj;

						itemid=vItem.Text;
						prname=vItem.Desc;
						gl.pprodname=prname;
						adapter.setSelectedIndex(position);

						appProd();
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
						clsCD vItem = (clsCD)lvObj;

						itemid=vItem.Text;
						prname=vItem.Desc;
						gl.pprodname=prname;
						adapter.setSelectedIndex(position);

						appProd();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;
				}
			});

			txtFilter.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {
					int tl;

					tl=txtFilter.getText().toString().length();

					if (tl==0 || tl>1) {listItems();}
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void listItems(){
		Cursor DT;
		clsCD vItem;	
		String vF,cod,name;

		items.clear();itemid="*";

		vF=txtFilter.getText().toString();

		try {

			sql="SELECT Codigo,Nombre FROM P_muNI ";
			if (vF.length()>0) sql=sql+"WHERE (Nombre LIKE '%" + vF + "%') ";
			sql+="ORDER BY Nombre";	

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
		
			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod=DT.getString(0);	
				name=DT.getString(1);

				vItem = clsCls.new clsCD();

				vItem.Cod="";
				vItem.Desc=name;
				vItem.Text=cod;

				items.add(vItem);	

				DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//	mu.msgbox( e.getMessage());
		}

		adapter=new ListAdaptCD(this,items);
		listView.setAdapter(adapter);
	}

	private void appProd(){
		try{
			gl.gstr=itemid;
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
}
