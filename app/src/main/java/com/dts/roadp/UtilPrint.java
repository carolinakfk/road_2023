package com.dts.roadp;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class UtilPrint extends PBase {
	
	private Spinner  spinPrint;
	private EditText txtPar;
	private TextView txtBut1;
	
	private ArrayList<String> spincode= new ArrayList<String>();
    private String prtipo,prpar;
    private int pridx;

	private AppMethods app;
    private printer prn;
    private Runnable printclose,printcallback;
   
    // Datamax "00:03:19:8E:76:7E"
    // Zebra "00:22:58:01:04:28"
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_util_print);
		
		super.InitBase();
		addlog("UtilPrint",""+du.getActDateTime(),gl.vend);
		
		spinPrint = (Spinner) findViewById(R.id.spinner1);
		txtPar = (EditText) findViewById(R.id.txtMonto);
		txtBut1 = (TextView) findViewById(R.id.textView1);txtBut1.setVisibility(View.INVISIBLE);

		app = new AppMethods(this, gl, Con, db);

		setHandlers();
		
		loadItem();
				
		fillSpinner();
		
		buildFile();

		printcallback= new Runnable() {
		    public void run() {
		    }
		};
				
		printclose= new Runnable() {
		    public void run() {
		    	//UtilPrint.super.finish();
		    }
		};
		
		prn=new printer(this,printclose,true);

		
	}
	
	
	// Events
	
	public void doApply(View view) {
		try{
			//if (!app.validaImpresora(txtPar.getText().toString().trim())) {
			//	msgbox("¡La impresora no está autorizada!");return;
			//}

			updateItem();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void doAction(View view) {
	}
	
	public void doTestPrint(View view) {

		try{
			if (prtipo.equalsIgnoreCase("SIN IMPRESORA")) return;

			prpar=txtPar.getText().toString().trim();

			if (mu.emptystr(prpar)) {
				mu.msgbox("Parámetro de impresión incorrecto");return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
		
	private void setHandlers(){
		    
		spinPrint.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TextView spinlabel;
		       	
		    	try {
		    		spinlabel=(TextView)parentView.getChildAt(0);
			    	spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(18);
				    
			    	prtipo=spincode.get(position);
				    pridx=position;	
			    	
			    	txtBut1.setVisibility(View.INVISIBLE);
			    	//if (pridx==2) {
			    	//	txtBut1.setText("Conectar");txtBut1.setVisibility(View.VISIBLE);
			    	//}
			    	
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
	
	
	// Main
	
	private void loadItem() {
		Cursor DT;
		
		try {

			sql="SELECT TIPO_IMPRESORA,PUERTO_IMPRESION FROM P_ARCHIVOCONF";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			//#EJC20181127: Se agregó validación de RowCount
			if (DT.getCount()>0)
			{
				prtipo=DT.getString(0);
				prpar=DT.getString(1);
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			prtipo="";prpar="";
		   	mu.msgbox( e.getMessage());
	    }	
		
		txtPar.setText(prpar);
		
	}
	
	private void updateItem() {
		
		try {
			
			prpar=txtPar.getText().toString().trim();

			if(gl.impresora.equalsIgnoreCase("S")){
				if(prtipo.equalsIgnoreCase("SIN IMPRESORA")){
					msgbox("Debe seleccionar una impresora.");
				}else {
					sql="UPDATE P_ARCHIVOCONF SET TIPO_IMPRESORA='"+prtipo+"',PUERTO_IMPRESION='"+prpar+"'";
					db.execSQL(sql);

					Toast.makeText(this,"Configuración guardada.", Toast.LENGTH_SHORT).show();
					super.finish();
				}
			}else{
				sql="UPDATE P_ARCHIVOCONF SET TIPO_IMPRESORA='"+prtipo+"',PUERTO_IMPRESION='"+prpar+"'";
				db.execSQL(sql);

				Toast.makeText(this,"Configuración guardada.", Toast.LENGTH_SHORT).show();
				super.finish();
			}

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}
		
	}


	// Aux
	
	private void fillSpinner() 	{
		int sp=0;
	
		try {

			spincode.clear();
			
			s="SIN IMPRESORA"; spincode.add(s); if (prtipo.equalsIgnoreCase(s)) sp=0;
			s="DATAMAX";       spincode.add(s); if (prtipo.equalsIgnoreCase(s)) sp=1;
			s="ZEBRA CPCL";    spincode.add(s); if (prtipo.equalsIgnoreCase(s)) sp=2;
							
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spincode);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				
			spinPrint.setAdapter(dataAdapter);
			spinPrint.setSelection(sp);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			Log.d("e",e.getMessage());
		}
	}	
	
	private void buildFile() {

		BufferedWriter writer = null;
		FileWriter wfile;
		String fname;	
		
		fname = Environment.getExternalStorageDirectory()+"/"+"printtest.txt";
		
		try {

			wfile=new FileWriter(fname,false);
			writer = new BufferedWriter(wfile);
			
			writer.write(" ");writer.write("\r\n");
			writer.write("Impresion linea 1");writer.write("\r\n");
			writer.write("Impresion linea 2");writer.write("\r\n");
			writer.write("Impresion linea 3");writer.write("\r\n");
			writer.write(" ");writer.write("\r\n");
			writer.write(" ");writer.write("\r\n");
			
		    writer.close();
		    
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("No se puede crear archivo de impresion : "+e.getMessage());
		}
		
	}
	

}
