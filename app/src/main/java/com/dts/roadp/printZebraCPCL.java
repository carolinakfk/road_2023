package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

// Zebra "00:22:58:01:04:28"

public class printZebraCPCL extends printBase {

	private Connection connection;

	private ZebraPrinter printer;
	
	private ArrayList<String> lines = new ArrayList<String>();
		
	private String ss,statstr,dbg;
	private boolean status,validprint;;

	// ZQ320 AC:3F:A4:C8:5F:D9


	public printZebraCPCL(Context context,String printerMAC,boolean validprinter) {
		super(context,printerMAC);
		validprint=validprinter;
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

	public boolean print() {
		hasCallback=false;

		fname="print.txt";errmsg="";
		try {
			if (loadFile())	doStartPrint();else return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			

		return true;
	}

	public void printask(String fileName) {	
		hasCallback=false;

		fname=fileName;	errmsg="";exitprint=false;
		msgAskPrint();				
	}

	public boolean print(String fileName) {
		hasCallback=false;

		fname=fileName;errmsg="";

		try {
			if (loadFile())	doStartPrint();else return false;
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


			lines.clear();

			while ((ss = dfile.readLine()) != null) {
				lines.add(ss);
			}
			
			//lines.add("");
			//lines.add("");

			dfile.close();	

			showmsg("Lines : "+lines.size());
			
			//printData = docLP.getDocumentData();

			return true;
		} catch (Exception e) {
			try {
				dfile.close();
			} catch (Exception e1) {}	

			showmsg("Error: " + e.getMessage());

			return false;
		}			
	}
	
	private byte[] printData() {
		byte[] prdata = null;
		int ccnt,dlen;
		String ps,ss;
		int altolinea,anchopapel;
		
		try {
			
			altolinea=30;
			
			anchopapel=200;
			if (prwidth>40) anchopapel=300;
			if (prwidth>60) anchopapel=400;
					
			ccnt=lines.size();
			dlen=ccnt*altolinea+60;
			
			ps="";
			ps+="! 0 "+anchopapel+" "+anchopapel+" "+dlen+" 1\r\n";dbg=ps;
			ps+="ML "+altolinea+"\r\n" + "TEXT 0 2 10 0\r\n";
			
			for (int i = 0; i <ccnt; i++) {
				ss=lines.get(i)+"\r\n";
				ps+=ss;
			}
		
			ps+="ENDML\r\n";
			//ps+="FORM\r\n";
			ps+="PRINT\r\n";

			prdata =ps.getBytes();

		} catch (Exception e) {
			setStatus(e.getMessage());
		}

		return prdata;
	}

	private void doStartPrint() {
		if (!validprint) {
			showmsg("¡La impresora no está autorizada!");return;
		}

		showmsg("Imprimiendo ..." );
		status=true;
		
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();

				doPrint();

				Looper.loop();
				Looper.myLooper().quit();
			}
		}).start();
		
	}
	
	private void doPrint() {
		printer = connect();

		if (printer != null) {
			processPrint();
		} else {
			disconnect();
		}
		
		doCallBack();
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
        } catch (Exception e) {}

        /*
        try {
            //#EJC201800: Llamar el close de la printer
            final Handler cbhandler1 = new Handler();
            cbhandler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        printclose.run();
                    } catch (Exception ee) {}
                }
            }, 200);
        } catch (Exception e) {}
		*/
	}

	
	// Zebra
	
	private void processPrint() {
		try {
			ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

			PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

			if (printerStatus.isReadyToPrint) {
				byte[] configLabel = printData();
				connection.write(configLabel);
			} else if (printerStatus.isHeadOpen) {
				setStatus("Impresora abierta");
			} else if (printerStatus.isPaused) {
				setStatus("Impresora detenida");
			} else if (printerStatus.isPaperOut) {
				setStatus("Papel afuera");
			}
		} catch (ConnectionException e) {
			setStatus(e.getMessage());
		} finally {
			disconnect();
		}
	}
	
	public ZebraPrinter connect() {
		
		connection = null;
		
		try {
			connection = new BluetoothConnection(printerAddress);
			connection.open();
		} catch (ConnectionException e) {
			setStatus("Comm Error : "+e.getMessage());
			disconnect();
		}

		ZebraPrinter printer = null;

		if (connection.isConnected()) {
			try {
				printer = ZebraPrinterFactory.getInstance(connection);
				String pl = SGD.GET("device.languages", connection);
			} catch (ConnectionException e) {
				setStatus("Unknown Printer Language");
				printer = null;				
				disconnect();
			} catch (ZebraPrinterLanguageUnknownException e) {
				setStatus("Unknown Printer Language");
				printer = null;
				disconnect();
			}
		}

		return printer;
	}
	
	public void disconnect() {
		sleep(300);
		try {
			if (connection != null) connection.close();
			
			if (status) {
				showmsg("Impresora lista");
			} else {	
				showmsg("Error : "+statstr);
			}
			//showmsg("dbg - "+dbg);
		} catch (ConnectionException e) {
			setStatus("COMM Error : "+e.getMessage());
		}
	}


	// Aux

	private void msgAskPrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("Impresora está lista ?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			      	
				try {
					if (loadFile())	doStartPrint();
				} catch (Exception e) {
					showmsg("Error: " + e.getMessage());
				}
			}

		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						printclose.run();
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
				Toast toast= Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT);  
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		});

	}	

	private void setStatus(final String statusMessage) {
		status=false;
		statstr=statusMessage;
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
	
}
