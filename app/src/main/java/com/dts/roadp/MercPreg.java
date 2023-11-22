package com.dts.roadp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MercPreg extends PBase {

	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();
	
	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos,pid;
	private double icant,iprec;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merc_preg);
		
		super.InitBase();
		addlog("MercPreg",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		
		emp=((appGlobals) vApp).emp;
		estado=((appGlobals) vApp).devtipo;
		cliid=((appGlobals) vApp).cliente;
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDate();
		((appGlobals) vApp).devrazon="0";
			
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
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						adapter.setSelectedIndex(position);

						pid=vItem.id;
						listResp();

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
				};
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		

	    
	}
	
	private void listItems(){
		Cursor DT,DTD;
		clsClasses.clsCFDV vItem;	
		String s;
		int pid,resp;
				
		items.clear();
				
		try {
			sql="SELECT CODIGO,NOMBRE FROM P_MERPREGUNTA ORDER BY NOMBRE";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  vItem = clsCls.new clsCFDV();
			  
			  pid=DT.getInt(0);
			  
			  try {
			
				  sql="SELECT RESP FROM D_MERPREGUNTA WHERE CLIENTE='"+cliid+"' AND CODIGO="+pid+" AND FECHA="+fecha;
				  DTD=Con.OpenDT(sql);				  
				  if (DTD.getCount()>0) {
					  
					  DTD.moveToFirst();
					  resp=DTD.getInt(0);
					  DTD.close();
					  
					  sql="SELECT NOMBRE FROM P_MERRESP WHERE CODIGO="+resp;
					  DTD=Con.OpenDT(sql);	
					  DTD.moveToFirst();
					  s=DTD.getString(0);
					
				  } else {
					  s=".....";
				  }
			  
			  } catch (Exception ex) {
				  addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
				  s=".....";
		      }
			  
			  vItem.id=pid;
			  vItem.Fecha=DT.getString(1).toUpperCase();
			  vItem.Desc=s;
			  vItem.Valor="";
			 
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
	
	private void addItem(int pid,int rid){
		
		try {
			sql="DELETE FROM D_MERPREGUNTA WHERE CLIENTE='"+cliid+"' AND CODIGO="+pid+" AND FECHA="+fecha;
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			ins.init("D_MERPREGUNTA");
			
			ins.add("CLIENTE",cliid);
			ins.add("CODIGO",pid);
			ins.add("FECHA",fecha);
			ins.add("RESP",rid);
			ins.add("STATCOM","N");
			ins.add("FOTO","");
			ins.add("GRABACION","");
			ins.add("STATCOMG","N");
			
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		listItems();	
				
	}
	
	
	// Respuesta
	
	private void listResp(){
		Cursor DT;
		String code,name;
			
		lcode.clear();lname.clear();
		
		try {
			
			sql="SELECT Codigo,Nombre FROM P_MERRESP WHERE PREGUNTA="+pid+" ORDER BY Nombre";
		
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  try {
				  code=String.valueOf(DT.getInt(0));
				  name=DT.getString(1);
				  
				  lcode.add(code);
				  lname.add(name);
			  } catch (Exception e) {
				  addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				  mu.msgbox(e.getMessage()); 
			  }
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());return;
	    }
			
		showRespDialog();
		
	}
	
	public void showRespDialog() {
		final AlertDialog Dialog;
		   
	    final String[] selitems = new String[lname.size()];
	    for (int i = 0; i < lname.size(); i++) {
	    	selitems[i] = lname.get(i);
	    }

		try{
			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Respuesta");

			mMenuDlg.setSingleChoiceItems(selitems , -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							try {
								String s=lcode.get(item);
								int resp=Integer.parseInt(s);

								addItem(pid,resp);

								dialog.dismiss();
							} catch (Exception e) {
								addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
							}
						}
					});

			mMenuDlg.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
