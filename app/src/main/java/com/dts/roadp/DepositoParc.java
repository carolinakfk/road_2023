package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsDepos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class DepositoParc extends PBase {

	private Spinner  spinBanco;
	private EditText txtBol,txtEf;
	private TextView lblCheq,lblTot,lblLim;
	
	private ListView listView;
	private ImageView btnSave,btnCancel;
	
	private ArrayList<clsClasses.clsDepos> items = new ArrayList<clsClasses.clsDepos>();
	private ListAdaptDepos adapter;
	private clsClasses.clsDepos selitem;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinname = new ArrayList<String>();
	private ArrayList<String> spincuenta = new ArrayList<String>();
	
	private String bancoid="",cuenta="",bol,corel;
	private double tef,tcheq,ttot,limit;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deposito_parc);
		
		super.InitBase();
		addlog("DepositoParc",""+du.getActDateTime(),gl.vend);
		
		spinBanco = (Spinner) findViewById(R.id.spinner1);
		txtBol = (EditText) findViewById(R.id.txtMonto);
		txtEf = (EditText) findViewById(R.id.txtBoleta);txtEf.setText("");
		lblCheq = (TextView) findViewById(R.id.lblCheq);lblCheq.setText("0.00");
		lblTot = (TextView) findViewById(R.id.lblTot);lblTot.setText("0.00");
		lblLim = (TextView) findViewById(R.id.TextView01);lblLim.setText("0.00");
			
		setHandlers();
		
		fillSpinner();
		setLimit();
		fillDocList();
		
		if (gl.boldep==1) {
			txtBol.setText(du.sfecha(fecha)+" "+du.shora(fecha)+":"+du.sSecond());
			txtBol.setEnabled(false);
		} else {	
			txtBol.setText("");
			txtBol.setEnabled(true);
		}
		
		tef=0;tcheq=0;ttot=0;	
		txtBol.requestFocus();
	}
	
	
	// Events
	
	public void listaDoc(View view){
		try{
			showDocDialog();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void saveDepos(View view){
		try{
			if (checkValues()) msgAskSave("Guardar depósito");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void OpenDesglose(View view){
		try{
			gl.totDep = tef;
			startActivity(new Intent(this,desglose.class));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Main
	
	private void setHandlers(){
		    
		spinBanco.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TextView spinlabel;
		       	
		    	try {
		    		spinlabel=(TextView)parentView.getChildAt(0);
			    	spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(22);
				    
			    	bancoid=spincode.get(position);
			    	cuenta=spincuenta.get(position);
			    	
		        } catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				   	mu.msgbox( e.getMessage());
		        }			    	
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});	
			
	}	
	
	private void scanValues(){
		double val,vef=0,vcheq=0;
		String ss;

		try{
			try {
				for(int i = 0; i < items.size(); i++ ) {
					selitem=items.get(i);
					val=selitem.Chec*selitem.Bandera;
					vcheq+=val;
				}
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				return;
			}

			try {
				ss=txtEf.getText().toString();
				tef=Double.parseDouble(ss);
			} catch (NumberFormatException e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				tef=0;
			}

			tcheq=vcheq;ttot=tef+tcheq;

			lblCheq.setText(mu.frmdec(tcheq));
			lblTot.setText(mu.frmdec(ttot));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
	
	private void showDocDialog(){
		final Dialog dialog = new Dialog(this);
		final String rv;
		final int rvi;
		
		dialog.setContentView(R.layout.activity_depos_doc);
		dialog.setTitle("Documentos pendientes");

		try{
			listView =  (ListView) dialog.findViewById(R.id.listView1);
			btnSave   = (ImageView) dialog.findViewById(R.id.imageView2);
			btnCancel = (ImageView) dialog.findViewById(R.id.imageView1);

			adapter=new ListAdaptDepos(this, items);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
					int flag;
					try {
						clsClasses.clsDepos selitem = (clsClasses.clsDepos) adapter.getItem(position);
						flag=selitem.Bandera;

						if (flag==0) flag=1; else flag=0;
						selitem.Bandera=flag;

						adapter.refreshItems();
						adapter.setSelectedIndex(position);

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}
				};
			});

			btnSave.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					scanValues();
					dialog.dismiss();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	
	
	private void fillDocList(){
		Cursor DT;
		clsClasses.clsDepos item;	
		
		items.clear();
	
		try {
			
			sql="SELECT COREL,ITEM,VALOR,DESC1,DESC2 FROM D_COBROP WHERE TIPO='C' AND DEPOS<>'S' ";
			DT=Con.OpenDT(sql);
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
								
				item = clsCls.new clsDepos();
				
				item.Cod=DT.getString(0);
				item.Nombre=DT.getString(3);
				item.Valor=DT.getDouble(2);
				item.Total=DT.getDouble(2);
				item.Efect=0;
				item.Chec=DT.getDouble(2);
				item.NChec=DT.getInt(1);
				item.Tipo="C";
				item.Bandera=1;
				item.Banco=DT.getString(4);
				
				items.add(item);						
				 
				DT.moveToNext();
			}
				
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		try {
			
			sql="SELECT COREL,ITEM,VALOR,DESC1,DESC2 FROM D_FACTURAP WHERE TIPO='C' AND DEPOS<>'S' ";
			DT=Con.OpenDT(sql);
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
								
				item = clsCls.new clsDepos();
				
				item.Cod=DT.getString(0);
				item.Nombre=DT.getString(3);
				item.Valor=DT.getDouble(2);
				item.Total=DT.getDouble(2);
				item.Efect=0;
				item.Chec=DT.getDouble(2);
				item.NChec=DT.getInt(1);
				item.Tipo="F";
				item.Bandera=1;
				item.Banco=DT.getString(4);
				
				items.add(item);						
				 
				DT.moveToNext();
			}
				
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}		
		
	}
	
	private void saveDoc(){
		Cursor DT;
		clsClasses.clsDepos item;
		double tot=0,efect=0,chec=0,val;
		String doc,num,banco;
		int nchec=0,it=0,cp;
				
		try {
			tot=ttot;
			
			corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
			
			db.beginTransaction();
						
			ins.init("D_DEPOS");
			ins.add("COREL",corel);
			ins.add("EMPRESA",((appGlobals) vApp).emp);
			ins.add("FECHA",fecha);
			ins.add("RUTA",((appGlobals) vApp).ruta);
			ins.add("BANCO",bancoid);
			ins.add("CUENTA",cuenta);
			ins.add("REFERENCIA",txtBol.getText().toString());
			
			ins.add("TOTAL",tot);
			ins.add("TOTEFEC",efect);
			ins.add("TOTCHEQ",chec);
			ins.add("NUMCHEQ",nchec);
			
			ins.add("IMPRES",0);
			ins.add("STATCOM","N");
			ins.add("ANULADO","N");
			ins.add("CODIGOLIQUIDACION",0);
			
			db.execSQL(ins.sql());
			
			for (int i = 0; i < items.size(); i++ ) {
				
				item=items.get(i);
				
				if (item.Bandera==1) {
					
					doc=item.Cod;
					
					if (item.Efect>0) {
						
						it+=1;
						
						ins.init("D_DEPOSD");
						ins.add("COREL",corel);
						ins.add("DOCCOREL",doc);
						ins.add("ITEM",it);
						ins.add("TIPODOC",item.Tipo);
						ins.add("CODPAGO",1);
						ins.add("CHEQUE","N");
						ins.add("MONTO",item.Efect);
						ins.add("BANCO","");
						ins.add("NUMERO","");
						
						db.execSQL(ins.sql());	
						
					}
					
		 			if (item.Tipo.equalsIgnoreCase("F")) {
						sql="SELECT Valor,DESC1,DESC3,CODPAGO FROM D_FACTURAP WHERE (COREL='"+doc+"') AND  (TIPO='C')";
		 			} else {	
						sql="SELECT Valor,DESC1,DESC3,CODPAGO FROM D_COBROP WHERE (COREL='"+doc+"') AND  (TIPO='C')";
		 			}					
					DT=Con.OpenDT(sql);
						
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
							
						val=DT.getDouble(0);
						num=DT.getString(1);
						banco=DT.getString(2);
						cp=DT.getInt(3);
							
						it+=1;
						
						ins.init("D_DEPOSD");
						ins.add("COREL",corel);
						ins.add("DOCCOREL",doc);
						ins.add("ITEM",it);
						ins.add("TIPODOC",item.Tipo);
						ins.add("CODPAGO",cp);
						ins.add("CHEQUE","S");
						ins.add("MONTO",val);
						ins.add("BANCO",banco);
						ins.add("NUMERO",num);
						
						db.execSQL(ins.sql());	
									
						DT.moveToNext();
					}
					
		 			if (item.Tipo.equalsIgnoreCase("F")) {
		 				sql="UPDATE D_FACTURA SET DEPOS='S' WHERE (COREL='"+doc+"')";
		 			} else {	
		 				sql="UPDATE D_COBRO SET DEPOS='S' WHERE (COREL='"+doc+"')";
		 			}									
					db.execSQL(sql);	
					
				} // Bandera==1
				
			}

			//Inserta y válida Desglose de depósito.

			if (gl.peModal.equalsIgnoreCase("TOL")) {

				sql="SELECT * FROM T_DEPOSB";

				DT=Con.OpenDT(sql);

				if (DT.getCount()==0){
					msgbox("Por favor realice el desglose antes de continuar.");
					db.endTransaction();
					return;
				}else{

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						ins.init("D_DEPOSB");
						ins.add("COREL",corel);
						ins.add("DENOMINACION",DT.getDouble(0));
						ins.add("CANTIDAD",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("MONEDA",DT.getString(3));

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

					sql="Delete from T_DEPOSB";
					db.execSQL(sql);
				}

			}

			db.setTransactionSuccessful();
			db.endTransaction();
			
			Toast.makeText(this,"Depósito guardado", Toast.LENGTH_SHORT).show();
			doExit();
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox( e.getMessage());return;
		}
				
	}
	
	
	// Aux
	
	private void fillSpinner(){
		Cursor DT;
		  
		try {
			sql="SELECT CODIGO,NOMBRE,CUENTA FROM P_BANCO ORDER BY Nombre,Cuenta";
			DT=Con.OpenDT(sql);
					
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  spincode.add(DT.getString(0));
			  spinname.add(DT.getString(1)+" - "+DT.getString(2));
			  spincuenta.add(DT.getString(2));
			  
			  DT.moveToNext();
			}
					
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		   	mu.msgbox( e.getMessage());
	    }
					
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinname);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
		spinBanco.setAdapter(dataAdapter);
			
		bancoid="";
		
	}	
	
	private void setLimit() {
		Cursor dt;
		double limf,limc,depos;

		try {
			limf=0;limc=0;depos=0;
			
			sql="SELECT SUM(TOTAL) FROM D_FACTURA WHERE ANULADO='N'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				limf=dt.getDouble(0);
			}
			
			
			sql="SELECT SUM(TOTAL) FROM D_COBRO WHERE ANULADO='N'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				limc=dt.getDouble(0);
			}
				
			sql="SELECT SUM(TOTAL) FROM D_DEPOS WHERE ANULADO='N'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				depos=dt.getDouble(0);
			}				
		
			limit=limf+limc-depos;
			lblLim.setText(mu.frmdec(limit));
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			limit=0;
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}
	
	private boolean checkValues(){
		double val;

		try{
			try {
				ss=txtEf.getText().toString();
				val=Double.parseDouble(ss);
				if (val<=0) throw new Exception();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				mu.msgbox("Monto incorrecto");return false;
			}

			scanValues();

			if (ttot==0) {
				mu.msgbox("No está definido monto");return false;
			}

			if (ttot>limit) {
				mu.msgbox("Monto mayor que límite");return false;
			}

			if (mu.emptystr(bancoid)) {
				mu.msgbox("Falta definir banco");return false;
			}

			bol=txtBol.getText().toString();
			if (mu.emptystr(bol)) {
				mu.msgbox("Falta definir boleto");txtBol.requestFocus();return false;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
		return true;
	}
	
	private void msgAskSave(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveDoc();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	
	
	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

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
	
	private void doExit(){
		try{
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin guardar depósito");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
		
	
	
}
