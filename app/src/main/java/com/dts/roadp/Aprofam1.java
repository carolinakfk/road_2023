package com.dts.roadp;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Aprofam1 extends PBase {

	private Spinner spin1,spin2;
	private TextView lblFecha;
	private EditText txtNit,txtNom,txtDir;
		
	private ArrayList<String> code1= new ArrayList<String>();
	private ArrayList<String> code2= new ArrayList<String>();
	private ArrayList<String> list1 = new ArrayList<String>();
	private ArrayList<String> list2 = new ArrayList<String>();
	
	private String ref1,ref2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aprofam1);
		
		super.InitBase();
		addlog("Aprofam1",""+du.getActDateTime(),gl.vend);
		
		spin1= (Spinner) findViewById(R.id.spinner1);
		spin2= (Spinner) findViewById(R.id.Spinner01);
		lblFecha= (TextView) findViewById(R.id.textView2);
		txtNit= (EditText) findViewById(R.id.editText1);txtNit.setText(gl.fnit);
		txtNom= (EditText) findViewById(R.id.editText2);txtNom.setText(gl.fnombre);
		txtDir= (EditText) findViewById(R.id.editText3);txtDir.setText(gl.fdir);
		
		gl.ref1="";gl.ref2="";gl.ref3="";ref1="*";ref2="*";
		
		setHandlers();
		
		fillSpin1();	
		fillSpin2();
	}
	   
	   
    // Events 

	public void doVenta(View view) {

		try{
			if (mu.emptystr(txtNit.getText().toString())) {
				msgbox("NIT incorrecto");return;
			}

			if (mu.emptystr(txtNom.getText().toString())) {
				msgbox("Nombre incorrecto");return;
			}

			if (ref1.equalsIgnoreCase("*")) {
				msgbox("Debe seleccionar una aseguradora");return;
			}

			if (ref2.equalsIgnoreCase("*")) {
				msgbox("Debe seleccionar una organización");return;
			}

			gl.ref1=ref1;gl.ref2=ref2;
			gl.fnit=txtNit.getText().toString();
			gl.fnombre=txtNom.getText().toString();
			gl.fdir=txtDir.getText().toString();

			startActivity(new Intent(this,Venta.class));
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void doExit(View view) {
		try{
			super.finish();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
   
	private void setHandlers() {

		spin1.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

		    	try {
		    		TextView spinlabel=(TextView)parentView.getChildAt(0);
		    		spinlabel.setTypeface(lblFecha.getTypeface());
		    		spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(22);

					ref1=code1.get(position);
		        } catch (Exception e) {
		    		addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					ref1="*";
		        }

		    /*	try {
		    		ref1=code1.get(position);
		        } catch (Exception e) {
		    		addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		        	ref1="*";
		        }
			*/
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});

		spin2.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

		    	try {
		    		TextView spinlabel=(TextView)parentView.getChildAt(0);
		  	    	spinlabel.setTypeface(lblFecha.getTypeface());
		  	    	spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(22);

					ref2=code2.get(position);
		        } catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					ref2="*";
		        }

		    /*	try {
		    		ref2=code2.get(position);
		        } catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		        	ref2="*";
		        }
			*/
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});
			
	}	  
   

    // Main 
   
   
    // Aux 
   
	private void fillSpin1(){
		Cursor DT;

		code1.clear();list1.clear();
		code1.add("*");list1.add("");
		
		try {

			sql="SELECT Codigo,Nombre FROM P_REF1 ORDER BY Nombre";
			DT=Con.OpenDT(sql);
		
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				code1.add(DT.getString(0));
				list1.add(DT.getString(1));
				DT.moveToNext();
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list1);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spin1.setAdapter(dataAdapter);
			spin1.setSelection(-1);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}
	}
	
	private void fillSpin2(){
		Cursor DT;

		code2.clear();list2.clear();
		code2.add("*");list2.add("");
		
		try {

			sql="SELECT Codigo,Nombre FROM P_REF2 ORDER BY Nombre";
			DT=Con.OpenDT(sql);
		
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				code2.add(DT.getString(0));
				list2.add(DT.getString(1));
				DT.moveToNext();
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list2);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spin2.setAdapter(dataAdapter);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}
	}
	
	
}
