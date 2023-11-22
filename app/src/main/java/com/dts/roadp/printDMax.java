package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.qrcode.encoder.QRCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import datamaxoneil.connection.Connection_Bluetooth;
import datamaxoneil.printer.Document;
import datamaxoneil.printer.DocumentEZ;
import datamaxoneil.printer.ParametersEZ;

// 00:17:AC:15:EC:C3

public class printDMax extends printBase {
	
	private String ss,ess;
	private appGlobals appG;
	private PBase clsPBase;
	private boolean validprint;

	private ArrayList<String> lines = new ArrayList<String>();
	private ArrayList<Document> documentlist = new  ArrayList<Document>();

	private String qrCode="";
	private int cantQR=0;
	
	public printDMax(Context context,String printerMAC,boolean validprinter) {
		super(context,printerMAC);
		validprint=validprinter;
		appG = new appGlobals();
		clsPBase=new PBase();
	}
	
	
	// Main
	
	public void printask(Runnable callBackHook) {
			
		hasCallback=true;
		callback=callBackHook;
		
		fname="print.txt";errmsg="";exitprint=false;
		msgAskPrint();
	}
	
	public void printask() {
		hasCallback=false;
		
		fname="print.txt";errmsg="";exitprint=false;
		msgAskPrint();				
	}

	public void printask(Runnable callBackHook, String fileName){
		hasCallback=true;
		callback=callBackHook;

		fname=fileName;errmsg="";exitprint=false;
		msgAskPrint();
	}

	public void printnoask(Runnable callBackHook, String fileName){
		hasCallback=true;
		callback=callBackHook;

		fname=fileName;errmsg="";
		try {
			if (loadFile())  doStartPrint();
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
		}
	}
	
	public boolean print() 	{
		hasCallback=false;
		
		fname="print.txt";errmsg="";

		try	{
			if (loadFile())	doStartPrint();	else return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}

	public boolean printBarra(ArrayList<String> listitems) 	{
		hasCallback=false;

		try	{
			if (loadFileBarra(listitems)){
				doStartPrintBarra();
			}
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}

		return true;
	}
	
	public void printask(String fileName) {
		hasCallback=false;
		
		fname=fileName;	errmsg="";
		exitprint=false;
		msgAskPrint();				
	}
		
	public boolean print(String fileName) {
		hasCallback=false;
		
		fname=fileName;errmsg="";
		
		try 		{
			if (loadFile())	{
				doStartPrint();
			} else {
				return false;
			}
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	

	// Private
	
	private boolean loadFile() {

		File ffile;
		BufferedReader dfile;
		String ss;
		
		try {
			
			File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
			ffile = new File(file1.getPath());
					
			FileInputStream fIn = new FileInputStream(ffile);
			dfile = new BufferedReader(new InputStreamReader(fIn));
			
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
			return false;
		}			
		
		try {
			docLP.clear();
			cantQR = 0;

			while ((ss = dfile.readLine()) != null) {
				//#CKFK20230127 Agregué esto para imprimir el QR en las datamax
				if (ss.contains("QRCode:")){
					cantQR  += 1;
					qrCode = ss.substring(7,ss.length());

					if (cantQR==1){

						docLP.writeText("");
						docLP.writeText("");

						printData = docLP.getDocumentData();

						docLP.clear();

					}else if(cantQR==2){

						docLP.writeText("");
						docLP.writeText("");

						printData2 = docLP.getDocumentData();

					}

				}else{
					docLP.writeText(ss);
				}
			}

			dfile.close();

			docLP.writeText("");
			docLP.writeText("");

			if (cantQR==0){

				printData = docLP.getDocumentData();

			}
			return true;

		} catch (Exception e) {
			try {
				dfile.close();
			} catch (Exception e1) {}	
			
			showmsg("Error: " + e.getMessage());
			
			return false;
		}			
	}

	private boolean loadFileBarra(ArrayList<String> listitems) {

		try {

			int ccnt;
			String txt;
           documentlist.clear();

			lines = listitems;

			showmsg("Lines : "+lines.size());

			ccnt=lines.size();

			for (int i = 0; i <ccnt; i++) {
				docEZ = new DocumentEZ("MF204");
				txt="";
				ss=lines.get(i);
				txt=lines.get(i);
				paramEZ.setHorizontalMultiplier(2);
				paramEZ.setVerticalMultiplier(14);
				docEZ.writeText(txt, 120, 30);
				docEZ.writeBarCode("BC128",ss,40,20,paramEZ);
				documentlist.add(docEZ);

			}

			return true;

		} catch (Exception e) {
			try {

			} catch (Exception e1) {}

			showmsg("Error: " + e.getMessage());

			return false;
		}
	}

	private boolean loadFileQRCode() {

		try {

			showmsg("Lines : "+lines.size());

			documentlist.clear();

			if (qrCode.length()>0){
				docEZ = new DocumentEZ("MF204");

				docEZ.writeBarCodeQRCode(qrCode,1,0,0,0,0);
				documentlist.add(docEZ);
			}

			return true;

		} catch (Exception e) {
			try {

			} catch (Exception e1) {}

			showmsg("Error: " + e.getMessage());

			return false;
		}
	}

	private void doStartPrint() {
		if (!validprint) {
			showmsg("¡La impresora no está autorizada!");return;
		}

		appG.endPrint = true;
		showmsg("Imprimiendo ..." );
		AsyncPrintCall wsRtask = new AsyncPrintCall();
		wsRtask.execute();
	}

	private void doStartPrintBarra() {
		if (!validprint) {
			showmsg("¡La impresora no está autorizada!");return;
		}

		appG.endPrint = true;
		showmsg("Imprimiendo ..." );
		AsyncPrintBarraCall wsRtask = new AsyncPrintBarraCall();
		wsRtask.execute();
	}

	private class AsyncPrintCall extends AsyncTask<String, Void, Void> {

		@Override
	    protected Void doInBackground(String... params) {
			try {
				processPrint();
			} catch (Exception e) {
				ss=ss + e.getMessage();
				Log.d("Err_Impr",e.getMessage());
			}
	            
	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
	    	try {
	    		doCallBack();
			} catch (Exception e) {
	    		ss=ss+e.getMessage();
				clsPBase.addlog("onPostExecute", "" , ss);
			}
	    }
	 
        @Override
        protected void onPreExecute() {}
	 
        @Override
        protected void onProgressUpdate(Void... values) {}
	 
    }

	private class AsyncPrintBarraCall extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				processPrintBarra();
			} catch (Exception e) {
				ss=ss + e.getMessage();
				Log.d("Err_Impr",e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				doCallBack();
			} catch (Exception e) {
				ss=ss+e.getMessage();
				clsPBase.addlog("onPostExecute", "" , ss);
			}
		}

		@Override
		protected void onPreExecute() {}

		@Override
		protected void onProgressUpdate(Void... values) {}

	}

	private void doCallBack() {

		if (!hasCallback) return;

        try {
            final Handler cbhandler = new Handler();
            cbhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.run();
                    } catch (Exception ee) {}
                }
            }, 500);
        } catch (Exception e) {
        	ess=e.getMessage();
        	ss=ss+e.getMessage();
		}

	}

	public void processPrintBarra() {

		ss="p1..";

		try {

			prconn = null;

			//Looper.prepare();

			prconn = Connection_Bluetooth.createClient(printerAddress);

			if (!prconn.getIsOpen()) prconn.open();

			for (Document doc: documentlist){

				printData = doc.getDocumentData();

				int intento =0;

				while (!prconn.getIsOpen() && intento <10) {
					try{

						prconn.open();

						intento+=1;

					} catch (Exception e) {
						ss = ss + "Error : " + e.getMessage()+ ", intento " + intento;
						Log.d("processPrint_ERR: ", ss);
						clsPBase.addlog("processPrint", "" , ss);}
				}

				if(!prconn.getIsOpen() && intento ==10){
					showmsg("No fue posible abrir la conexión con la impresora, se intentó: " + intento);
				}else{
					prconn.write(printData,0,printData.length);

					prthread.sleep(500);

					prconn.clearWriteBuffer();
				}

			}

			prconn.close();

		} catch (Exception e) {
			ss = ss + "Error : " + e.getMessage();
			Log.d("processPrint_ERR: ", ss);

			try {
				if (prconn != null) prconn.close();
			} catch (Exception ee) {
				ss=ss + ee.getMessage();
			}
		}

	}

	public void processPrint() {

		ss="p1..";

		try {

			prconn = null;

			//Looper.prepare();

			prconn = Connection_Bluetooth.createClient(printerAddress);

			//CKFK 20190630 06:11 PM agregué a este proceso que intente la conexión con la impresora 10 veces
			// y si no logra abrir la conexión de mensaje de que no pudo establecer conexión
			//Además reduje el sleep a 500
			int intento =0;

			while (!prconn.getIsOpen() && intento <10) {
				try{

					prconn.open();

					intento+=1;

				} catch (Exception e) {
					ss = ss + "Error : " + e.getMessage()+ ", intento " + intento;
					Log.d("processPrint_ERR: ", ss);
					clsPBase.addlog("processPrint", "" , ss);}
			}

			if(!prconn.getIsOpen() && intento ==10){
				showmsg("No fue posible abrir la conexión con la impresora, se intentó: " + intento);
				return;
			}else{
				prconn.write(printData);

				prthread.sleep(500);

				if (qrCode.length()>0){
					documentlist.clear();

					docEZ = new DocumentEZ("MF204");

					docEZ.writeBarCodeQRCode(qrCode,1,0,0,0,0);
					documentlist.add(docEZ);
					for (Document doc: documentlist){
						printData = doc.getDocumentData();
					}
					prconn.write(printData,0,printData.length);
				}

				prthread.sleep(500);

				if (cantQR==2){

					printData = printData2;

					prconn.write(printData);

					prthread.sleep(500);

					if (qrCode.length()>0){
						documentlist.clear();

						docEZ = new DocumentEZ("MF204");

						docEZ.writeBarCodeQRCode(qrCode,1,0,0,0,0);
						documentlist.add(docEZ);
						for (Document doc: documentlist){
							printData = doc.getDocumentData();
						}
						prconn.write(printData,0,printData.length);
					}
					prthread.sleep(500);
				}

				prconn.clearWriteBuffer();
				//printclose.run();
				prconn.close();

			}

			//	if (!prconn.getIsOpen()) prconn.open();

		} catch (Exception e) {
			ss = ss + "Error : " + e.getMessage();
			Log.d("processPrint_ERR: ", ss);

			try {
				if (prconn != null) prconn.close();
			} catch (Exception ee) {
				ss=ss + ee.getMessage();
			}
		}

	}

	private void msgAskRePrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿Quiere volver a intentar la impresión?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				msgAskPrint();
			}

		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
                            appG.devprncierre=true;
  							printclose.run();
						} catch (Exception e) {
							//showmsg(e.getMessage());
						}

					}
				}, 200);
			}
		});

		dialog.show();

	}

	// Aux
	
	private void msgAskPrint() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿La impresora está lista?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	try {
					if (loadFile())
						doStartPrint();
				} catch (Exception e) {
					showmsg("Error: " + e.getMessage());
				}
		    }

		});

		//#EJC20181130:Se comentarió por solicitud de auditor de SAT.
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						msgAskRePrint();
					}
				}, 200);
		    }
		});

		dialog.show();
			
	}
	
	public void showmsg(String MsgStr) {
		errmsg=MsgStr;
			handler.post(new Runnable() {
			public void run() {
				Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	

}
