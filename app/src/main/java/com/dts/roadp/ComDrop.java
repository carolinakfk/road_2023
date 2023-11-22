package com.dts.roadp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ComDrop extends Activity {

	private TextView lblInfo,lblParam;
	private ProgressBar barInfo;
	private TextView btnRec,btnSend;
	
	private int active;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private String vSQL;
	
	private Application vApp;
	private MiscUtils MU;
	private DateUtils DU;
	
	private int isbusy,lin;
	private long fecha;
	private String err;
	private boolean fFlag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_drop);
		
		lblInfo= (TextView) findViewById(R.id.lblETipo);
		lblParam= (TextView) findViewById(R.id.lblProd);
		barInfo= (ProgressBar) findViewById(R.id.progressBar2);
		btnSend= (TextView) findViewById(R.id.btnRec);
		btnRec= (TextView) findViewById(R.id.btnSend);
		
		isbusy=0;
		
		lblInfo.setText("");
		barInfo.setVisibility(View.INVISIBLE);
		
		Con = new BaseDatos(this);
	    opendb();
	    ins=Con.Ins;upd=Con.Upd;
		
		vApp=this.getApplication();
		MU=new MiscUtils(this);
		DU=new DateUtils();fecha=DU.getActDateTime();
		
		readDataHeader();
	}

	// Events
	
	public void syncDropBox(View view){
		try {
		    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.ttxapps.dropsync");
		    this.startActivity(intent);
		} catch (Exception e) {
			MU.msgbox("No se puede ejecutar DropSync");
		}
	}
	
	public void sendSMS(View view){
		try {
		    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
	        i.setType("vnd.android-dir/mms-sms");
            startActivity(i);
		} catch (Exception e) {
            MU.msgbox("No se puede enviar mensaje");
		}
	}
	
	public void callPhone(View view){
		try {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			startActivity(intent);
		} catch (Exception e) {
            MU.msgbox("No se puede llamar");
		}
		  
	}
	
	
	// Main
	
	private void runRecep() {
		
		if (isbusy==1) {return;}
		
		if (fFlag) {
			
			isbusy=1;
			
			barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
			lblInfo.setText("Procesando ...");
			btnSend.setVisibility(View.INVISIBLE);
			btnRec.setVisibility(View.INVISIBLE);
			
			AsyncCallSync wstask = new AsyncCallSync();
			wstask.execute();
			
		} else {	
			isbusy=0;
			MU.msgbox("No hay parámetros");
			btnSend.setVisibility(View.VISIBLE);
			btnRec.setVisibility(View.VISIBLE);
		}
	}
	
	private void runSend(){
		
		btnSend.setVisibility(View.INVISIBLE);
		btnRec.setVisibility(View.INVISIBLE);
		
		if (genData()) askSendOK();
		
		btnSend.setVisibility(View.VISIBLE);
		btnRec.setVisibility(View.VISIBLE);
	}
	
	private boolean genData(){
		Cursor DT;
		clsDataBuilder dbld;
		String s,fruta,hora;
		int ccorel;
		long fecha;
		
		try {

			dbld=new clsDataBuilder(this);
			
			dbld.clear();
			
			// FACTURAS
			try {
				vSQL="SELECT COREL,RUTA,CORELATIVO FROM D_FACTURA WHERE STATCOM='N' ORDER BY CORELATIVO";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						fruta=DT.getString(1);
						ccorel=DT.getInt(2);  
						
						dbld.insert("D_FACTURA" ,"WHERE COREL='"+s+"'");
						dbld.insert("D_FACTURAD","WHERE COREL='"+s+"'");
						dbld.insert("D_FACTURAP","WHERE COREL='"+s+"'");
						dbld.add("UPDATE P_COREL SET CORELULT="+ccorel+"  WHERE RUTA='"+fruta+"'");	
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}			

			// PEDIDOS
			try {
				vSQL="SELECT COREL FROM D_PEDIDO WHERE STATCOM<>'S'";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						
						dbld.insert("D_PEDIDO","WHERE COREL='"+s+"'");
						dbld.insert("D_PEDIDOD","WHERE COREL='"+s+"'");
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}
			
			// COBROS
			try {
				vSQL="SELECT COREL FROM D_COBRO WHERE STATCOM<>'S'";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						
						dbld.insert("D_COBRO","WHERE COREL='"+s+"'");
						dbld.insert("D_COBROD","WHERE COREL='"+s+"'");
						dbld.insert("D_COBROP","WHERE COREL='"+s+"'");
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}
			
			
			// DEPOSITOS
			try {
				vSQL="SELECT COREL FROM D_DEPOS WHERE STATCOM='N'";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						
						dbld.insert("D_DEPOS" ,"WHERE COREL='"+s+"'");
						dbld.insert("D_DEPOSD","WHERE COREL='"+s+"'");
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}			
			
			// MOVIMIENTOS
			try {
				vSQL="SELECT COREL FROM D_MOV WHERE STATCOM='N'";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						
						dbld.insert("D_MOV" ,"WHERE COREL='"+s+"'");
						dbld.insert("D_MOVD","WHERE COREL='"+s+"'");
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}				
			
			// ATENCION
			try {
				vSQL="SELECT RUTA,FECHA,HORALLEG FROM D_ATENCION WHERE STATCOM='N'";
				DT=Con.OpenDT(vSQL);
				
				if (DT.getCount()>0) {
				
					DT.moveToFirst();
					while (!DT.isAfterLast()) {
					  
						s=DT.getString(0);
						fecha=DT.getLong(1);
						hora=DT.getString(2);  
						
						dbld.insert("D_ATENCION" ,"WHERE (RUTA='"+s+"') AND (FECHA="+fecha+") AND (HORALLEG='"+hora+"') ");
						
						DT.moveToNext();
					}	
				}
				
			} catch (Exception ee) {
				MU.msgbox(ee.getMessage());	
			}				
								
			
			dbld.save();
			
			return true;
			
		} catch (Exception e) {
			MU.msgbox(e.getMessage());return false;
		}
		
		
	}
	
	private void setFlags(){	
		try {
			vSQL="UPDATE D_FACTURA SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}
		
		try {
			vSQL="UPDATE D_PEDIDO SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}		
		
		try {
			vSQL="UPDATE D_COBRO SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}			
		
		try {
			vSQL="UPDATE D_DEPOS SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}			
		
		try {
			vSQL="UPDATE D_MOV SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}	
			
		try {
			vSQL="UPDATE D_ATENCION SET STATCOM='S'";
		    db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}

		try {
			vSQL="UPDATE D_DESPACHOD_NO_ENTREGADO SET STATCOM='S'";
			db.execSQL(vSQL);
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}

	}
	
	
	// ASync Call
	
	private class AsyncCallSync extends AsyncTask<String, Void, Void> {

		@Override
        protected Void doInBackground(String... params) {
			
			File myFile;
			BufferedReader myReader = null;
			
			err="#";lin=0;
				
			try {
				lin=1;
				File file = new File(Environment.getExternalStorageDirectory(), "/SyncFold/rd_param.txt");
				myFile = new File(file.getPath());
					
				FileInputStream fIn = new FileInputStream(myFile);
				myReader = new BufferedReader(new InputStreamReader(fIn));
				
				String aDataRow = "";
				aDataRow = myReader.readLine();
						
				while ((aDataRow = myReader.readLine()) != null) {
					vSQL=aDataRow;
					db.execSQL(vSQL);
						
					//Thread.sleep(50);
					
					lin+=1;if (lin % 10==0) publishProgress();
				}
					
				err="OK";
			} catch (Exception e) {
				err=e.getMessage()+"\n"+vSQL;
			}
				
			try {
				myReader.close();
			} catch (Exception e) {
			}
            
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
        	
        	barInfo.setVisibility(View.INVISIBLE);
        	lblInfo.setText(""+lin+"");
        	
        	if (err.equalsIgnoreCase("OK")) {
        		MU.msgbox("Comunicación terminada");
        	} else {	
        		showmsg(err);
        	}
        	
        	isbusy=0;
        	
        	btnSend.setVisibility(View.VISIBLE);
			btnRec.setVisibility(View.VISIBLE);
        }
 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {
    		}
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(""+lin+"");
    		} catch (Exception e) {
    		}
        }
 
    }	
	
	// Aux
	
	private void readDataHeader() {
		File myFile;
		BufferedReader myReader = null;
		
		lblParam.setText("No hay parámetros");
		fFlag=false;
		
		try {
	 		File file = new File(Environment.getExternalStorageDirectory(), "/SyncFold/rd_param.txt");
			myFile = new File(file.getPath());
			
			FileInputStream fIn = new FileInputStream(myFile);
			myReader = new BufferedReader(new InputStreamReader(fIn));
		
			String aDataRow = "";
			aDataRow = myReader.readLine();
			
			lblParam.setText(aDataRow);
			
		} catch (Exception e) {
		}
		
		try {
			myReader.close();
			fFlag=true;
		} catch (Exception e) {
		}
		
	}
	
	public void showmsg(String s){
		Intent intent;
		((appGlobals) vApp).gstr=s;
		intent = new Intent(this,MessageBox.class);
		startActivity(intent);
	}
	
	public void askRec(View view) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("¿Recibir datos nuevos?");
					
		dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	runRecep();
		    }
		});
		
		dialog.setNegativeButton("Cancelar", null);
		
		dialog.show();
			
	}	
	
	public void askSend(View view) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("¿Enviar datos?");
					
		dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	runSend();
		    }
		});
		
		dialog.setNegativeButton("Cancelar", null);
		
		dialog.show();
			
	}	
	
	public void askSendOK() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("¿Envio correcto?");
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	askSendOK2();
		    }
		});
		
		dialog.setNegativeButton("No", null);
		
		dialog.show();
			
	}	
	
	public void askSendOK2() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("¿Está seguro?");
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	setFlags();
		    }
		});
		
		dialog.setNegativeButton("No", null);
		
		dialog.show();
			
	}	
	
	
	// Activity Events
	
	protected void onResume() {
		opendb();
	    super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			Con.close();  
		} catch (Exception e) { }
		active= 0;
	    super.onPause();
	}	
	
	@Override
	public void onBackPressed() {
	   if (isbusy==0) {
		   super.onBackPressed();
	   }
	}
	
	private void opendb() {
		
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
			active=1;	
	    } catch (Exception e) {
	    	MU.msgbox(e.getMessage());
	    	active= 0;
	    }
	}				

}
