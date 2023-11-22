package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MercAct extends PBase {
	
	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();
	
	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado,pid;
	private int itempos,tipoid;
	private double icant,iprec;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merc);

		super.InitBase();
		addlog("MercAct",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		
		emp=((appGlobals) vApp).emp;
		estado=((appGlobals) vApp).devtipo;
		cliid=((appGlobals) vApp).cliente;
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDate();
		((appGlobals) vApp).devrazon="0";
			
		listResp();
		
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

						tipoid=vItem.id;
						pid=vItem.Sid;

						showRespDialog();

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
		Cursor DT,DTD;
		clsClasses.clsCFDV vItem;	
		String pid,s;
		int resp;
				
		items.clear();
				
		try {
			sql="SELECT P_MEREQTIPO.NOMBRE,P_MEREQUIPO.SERIAL,P_MEREQUIPO.TIPO " +
				 "FROM P_MEREQUIPO INNER JOIN P_MEREQTIPO ON P_MEREQTIPO.CODIGO=P_MEREQUIPO.TIPO  " +
				 "WHERE P_MEREQUIPO.CLIENTE='"+cliid+"' ORDER BY P_MEREQTIPO.NOMBRE,P_MEREQUIPO.SERIAL";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  vItem = clsCls.new clsCFDV();
			  
			  pid=DT.getString(1);
			  
			  try {
			
				  sql="SELECT ESTADO FROM D_MEREQUIPO WHERE CLIENTE='"+cliid+"' AND SERIAL='"+pid+"' AND FECHA="+fecha;
				  DTD=Con.OpenDT(sql);				  
				  if (DTD.getCount()>0) {
					  
					  DTD.moveToFirst();
					  resp=DTD.getInt(0);
					  DTD.close();
					  
					  sql="SELECT NOMBRE FROM P_MERESTADO WHERE CODIGO="+resp;
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
			  
			  vItem.Sid=pid;
			  vItem.Fecha=DT.getString(0)+" No: "+DT.getString(1);
			  vItem.Desc=s;
			  vItem.Valor="";
			  vItem.id=DT.getInt(2);
			 
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
	
	private void addItem(String pid,int rid,int tid){
		
		try {
			sql="DELETE FROM D_MEREQUIPO WHERE CLIENTE='"+cliid+"' AND SERIAL='"+pid+"' AND FECHA="+fecha;
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			ins.init("D_MEREQUIPO");
			
			ins.add("CLIENTE",cliid);
			ins.add("SERIAL",pid);
			ins.add("FECHA",fecha);
			ins.add("TIPO",tid);
			ins.add("ESTADO",rid);
			ins.add("CODBARRA","");
			ins.add("STATCOM","N");
			
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
			
			sql="SELECT Codigo,Nombre FROM P_MERESTADO ORDER BY Nombre";
		
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
		   	mu.msgbox( e.getMessage());
	    }
		
	}
	
	public void showRespDialog() {
		final AlertDialog Dialog;
		   
	    final String[] selitems = new String[lname.size()];
	    for (int i = 0; i < lname.size(); i++) {
	    	selitems[i] = lname.get(i);
	    }

		try{
			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Estado");

			mMenuDlg.setSingleChoiceItems(selitems , -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							try {
								String s=lcode.get(item);
								int resp=Integer.parseInt(s);

								addItem(pid,resp,tipoid);

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
