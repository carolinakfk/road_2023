package com.dts.roadp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class printZebraZPL extends printBase {

    private static final long PRINT_TIMEOUT_MS=15000;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS=4104;

    private Connection connection;

    private ZebraPrinter printer;

    private ArrayList<String> lines = new ArrayList<String>();
    private String qrCode="";

    private int cantQR = 0, marca = 0;

    private String ss,statstr,dbg;
    private boolean status,validprint;
    private volatile boolean diagnosticErrorShown=false;
    private volatile boolean printInProgress=false;
    private volatile int printAttemptId=0;

    // ZQ320 AC:3F:A4:C8:5F:D9


    public printZebraZPL(Context context, String printerMAC, boolean validprinter) {
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

    public boolean printBarra(ArrayList<String> listitems) {
        hasCallback=false;

        errmsg="";
        try {
            if (loadFileBarra(listitems))	doStartPrintBarra();else return false;
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());return false;
        }

        return true;
    }

    public void printask(Runnable callBackHook,String fileName) {
        hasCallback=true;
        callback=callBackHook;
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

    public void printnoask(Runnable callBackHook,String fileName) {
        hasCallback=true;
        callback=callBackHook;
        fname=fileName;	errmsg="";exitprint=false;
        try {
            if (loadFile())	doStartPrint();
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());
        }
    }


    // Private

    private boolean loadFile() {
        File ffile;
        BufferedReader dfile;
        String ss;
        int linea = 0;

        cantQR = 0;

        try {

            //File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
            File file1 = new File(AppPaths.printDir(appGlobals.app()), fname);
            ffile = new File(file1.getPath());

            FileInputStream fIn = new FileInputStream(ffile);
            dfile = new BufferedReader(new InputStreamReader(fIn));

        } catch (Exception e) {
            showDiagnosticError("Abrir archivo de impresión", e);
            return false;
        }

        try {

            lines.clear();
            linea = 0;

            while ((ss = dfile.readLine()) != null) {

                linea += 1;

                if (ss.contains("QRCode:")){
                    qrCode = ss.substring(7,ss.length());
                    cantQR +=1;
                    if (cantQR==1){
                        marca = linea;
                    }
                }else{
                    lines.add(ss);
                }
            }

            //lines.add("");
            //lines.add("");

            dfile.close();

            showDiagnosticProgress("Documento preparado: "+lines.size()+" líneas. Conectando...");

            //printData = docLP.getDocumentData();

            return true;
        } catch (Exception e) {
            try {
                dfile.close();
            } catch (Exception e1) {}

            showDiagnosticError("Leer archivo de impresión", e);

            return false;
        }
    }

    private boolean loadFileBarra(ArrayList<String> listitems) {
        File ffile;
        BufferedReader dfile;
        String ss;

        try {

            //lines.clear();

            lines = listitems;

            showmsg("Lines : "+lines.size());

            return true;

        } catch (Exception e) {
            try {

            } catch (Exception e1) {}

            showmsg("Error: " + e.getMessage());

            return false;
        }
    }

    private byte[] printData() {
        byte[] prdata = null;
        int ccnt,dlen;
        String ps,ss;
        int altolinea,anchopapel,psx,altoQRPapel=0,altoQR=0;

        try {

            altolinea=20;

            if (qrCode.length()>0){
                altoQR = 280;
                if (cantQR>1){
                    altoQRPapel = 280 * cantQR;
                }else{
                    altoQRPapel = altoQR;
                }
            }

            psx = 0;
            anchopapel=500;
            if (prwidth>35) anchopapel=350; //#CKFK 20190614 Agregué esta condición para el ancho del papel
            if (prwidth>40) anchopapel=300;
            if (prwidth>60) anchopapel=400;

            ccnt=lines.size();
            dlen=(ccnt*altolinea) + altoQRPapel +60;

            ps="";
            anchopapel=430;
            ps+="^XA";
            ps+="^PW"+anchopapel;
            ps+="^LL"+dlen;

            //Prueba imprimir así, haber como sale..
            for (int i = 0; i <ccnt; i++) {

                if (marca!=0 && marca == i && altoQR!=0){
                    if (qrCode.length()>0){
                        ps+="^FO,"+psx+",1";
                        ps+="^BQN,2,3^";
                        ps+="^FD"+"   "+qrCode;
                        ps+="^FS";

                        ps+="^FO,"+psx+",0 ^A 0, 40 ";
                        ps+="^ADN,5,1";
                        //ps+="^A@N,20,20,TT0003M_^FH\\^CI28";
                        //ps+="^A0N,20,20^FH\\^CI28^";
                        ps+="^FD";
                        ss=lines.get(i);
                        ps+=ss;
                        //ps+="^FS^CI27";
                        ps+="^FS";
                    }
                }else{

                    ps+="^FO,"+psx+",0 ^A 0, 40 ";
                    ps+="^ADN,5,1";
                    //ps+="^A@N,20,20,TT0003M_^FH\\^CI28";
                    //ps+="^A0N,20,20^FH\\^CI28^";
                    ps+="^FD";
                    ss=lines.get(i);
                    ps+=ss;
                    //ps+="^FS^CI27";
                    ps+="^FS";

                }

                if (marca!=0 && i==marca && cantQR>1){
                    psx =psx + altoQR;
                }else{
                    psx =psx + altolinea;
                }
            }

            if (qrCode.length()>0){
                ps+="^FO,"+psx+",1";
                ps+="^BQN,2,3^";
                ps+="^FD"+"   "+qrCode;
                ps+="^FS";
            }

            ps+="^XZ";

            prdata =ps.getBytes();

        } catch (Exception e) {
            setStatus(e.getMessage());
            showDiagnosticError("Generar documento ZPL", e);
        }

        return prdata;
    }

    private byte[] printDataBarra() {
        byte[] prdata = null;
        int ccnt,dlen;
        String ps,ss;
        int altobarra,anchopapel,psx;
        int anchobarra;

        try {

            altobarra=80;//#CKFK 20190614 Modifiqué el alto de la barra a 80 antes era 100
            anchobarra = 1;//#CKFK 20190614 Modifiqué el ancho de la barra a 1 antes era 2
            psx = 20;
            anchopapel=500;
            if (prwidth>35) anchopapel=350;//#CKFK 20190614 Agregué esta condición para el ancho del papel
            if (prwidth>40) anchopapel=300;
            if (prwidth>60) anchopapel=400;

            ccnt=lines.size();
            dlen=ccnt*(altobarra+62);

            ps="";

            ps+="^XA";
            ps+="^PW"+anchopapel;
            ps+="^LL"+dlen;
            for (int i = 0; i <ccnt; i++) {
                ps+="^BY"+anchobarra+",2,"+altobarra+"";
                ps+="^FO,"+psx+",62";
                ps+="^BC";
                ps+="^FD";
                ss=lines.get(i);
                ps+=ss;
                ps+="^FS";
                psx =psx+55 + altobarra;
            }

            ps+="^XZ";

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
        printInProgress=true;
        final int currentAttempt=++printAttemptId;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (printInProgress && printAttemptId==currentAttempt) {
                    showDiagnosticError("Tiempo de espera de impresión ZPL",
                            new Exception("La impresora no respondió en 15 segundos. Verifique Bluetooth y vuelva a intentar."));
                    closeTimedOutConnection(currentAttempt);
                }
            }
        }, PRINT_TIMEOUT_MS);

        new Thread(new Runnable() {
            public void run() {
                try {
                    doPrint();
                } catch (Throwable error) {
                    showDiagnosticError("Ejecutar impresión ZPL", error);
                } finally {
                    if (printAttemptId==currentAttempt) printInProgress=false;
                }
            }
        }).start();

    }

    private void doStartPrintBarra() {
        if (!validprint) {
            showmsg("¡La impresora no está autorizada!");return;
        }

        showmsg("Imprimiendo ..." );
        status=true;

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();

                doPrintBarra();

                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    private void doPrint() {

        showDiagnosticProgress("Abriendo conexión Bluetooth con la impresora ZPL...");
        printer = connect();

        if (printer != null) {
            processPrint();
        } else {
            disconnect();
        }

        doCallBack();
    }

    private void doPrintBarra() {

        printer = connect();

        if (printer != null) {
            processPrintBarra();
        } else {
            disconnect();
        }

        doCallBack();
    }

    private void doCallBack() {

        int i=1;

        if (!hasCallback) return;

        try {
            final Handler cbhandler = new Handler(Looper.getMainLooper());
            cbhandler.postDelayed(() -> {
                try {
                    callback.run();
                } catch (Exception ee) {
                    String ss=ee.getMessage();
                }
            }, 500);
        } catch (Exception e) {
            String ss=e.getMessage();
        }

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

            PrinterStatus printerStatus = printer.getCurrentStatus();

            if (printerStatus.isReadyToPrint) {
                byte[] configLabel = printData();
                if (configLabel==null || configLabel.length==0) {
                    showDiagnosticError("Generar documento ZPL",
                            new IllegalStateException("El documento de impresión está vacío."));
                    return;
                }
                if (connection==null || !connection.isConnected()) {
                    showDiagnosticError("Enviar documento ZPL",
                            new IllegalStateException("La conexión Bluetooth no está disponible."));
                    return;
                }
                connection.write(configLabel);
            } else if (printerStatus.isHeadOpen) {
                setStatus("Impresora abierta");
            } else if (printerStatus.isPaused) {
                setStatus("Impresora detenida");
            } else if (printerStatus.isPaperOut) {
                setStatus("Papel afuera");
            }
        } catch (Exception e) {
            setStatus(e.getMessage());
            showDiagnosticError("Consultar o enviar datos a la impresora ZPL", e);
        } finally {
          disconnect();
        }
    }

    private void processPrintBarra() {

        try {

            PrinterStatus printerStatus = printer.getCurrentStatus();

            if (printerStatus.isReadyToPrint) {
                byte[] configLabel = printDataBarra();
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

    private void processPrintQRCode() {

        try {

            PrinterStatus printerStatus = printer.getCurrentStatus();

            if (printerStatus.isReadyToPrint) {
                byte[] configLabel = printDataQRCode();
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

    private byte[]printDataQRCode(){
        byte[] prdata = null;
        int ccnt,dlen;
        String ps,qr_code="";
        int altobarra,anchopapel,psx,psy;

        try {

            altobarra=80;//#CKFK 20190614 Modifiqué el alto de la barra a 80 antes era 100
            psx = 20;
            psy = 250;
            anchopapel=500;
            if (prwidth>35) anchopapel=350;//#CKFK 20190614 Agregué esta condición para el ancho del papel
            if (prwidth>40) anchopapel=300;
            if (prwidth>60) anchopapel=400;

            ps="";

            ps+="^XA";
            ps+="^PW"+anchopapel;
            ps+="^FO,"+psx+","+psy+"";
            ps+="^BQN,2,5^";
            ps+="^FD"+qr_code;
            ps+="^FS";
            ps+="^XZ";

            prdata =ps.getBytes();

        } catch (Exception e) {
            setStatus(e.getMessage());
        }

        return prdata;
    }

    public ZebraPrinter connect() {

        connection = null;

        if (!ensureBluetoothPermissions()) return null;

        try {
            connection = new BluetoothConnection(printerAddress);
            connection.open();
        } catch (Exception e) {
            setStatus("Comm Error : "+e.getMessage());
            showDiagnosticError("Abrir conexión Bluetooth ZPL", e);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (connection != null && connection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(connection);
                PrinterLanguage pcLanguage = printer.getPrinterControlLanguage();
                String pl = SGD.GET("device.languages", connection);
                SGD.SET("device.languages", "hybrid_xml_zpl", connection);
            } catch (ConnectionException e) {
                setStatus("Unknown Printer Language");
                showDiagnosticError("Detectar lenguaje de la impresora ZPL", e);
                printer = null;
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Unknown Printer Language");
                showDiagnosticError("Detectar lenguaje de la impresora ZPL", e);
                printer = null;
                disconnect();
            }
        }

        return printer;
    }

    private boolean ensureBluetoothPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;

        final ArrayList<String> missingPermissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(cont, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (ContextCompat.checkSelfPermission(cont, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (missingPermissions.isEmpty()) return true;

        setStatus("Falta permiso de Dispositivos cercanos para imprimir por Bluetooth.");
        showDiagnosticProgress("Conceda el permiso Dispositivos cercanos y vuelva a intentar la impresión.");

        if (cont instanceof Activity) {
            final Activity activity = (Activity) cont;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(activity,
                            missingPermissions.toArray(new String[0]),
                            REQUEST_BLUETOOTH_PERMISSIONS);
                }
            });
        } else {
            showDiagnosticError("Validar permisos Bluetooth ZPL",
                    new SecurityException("No fue posible solicitar el permiso Dispositivos cercanos desde esta pantalla."));
        }

        return false;
    }

    private void closeTimedOutConnection(final int timedOutAttempt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (printAttemptId==timedOutAttempt && connection!=null) connection.close();
                } catch (Exception ignored) {
                } finally {
                    if (printAttemptId==timedOutAttempt) printInProgress=false;
                }
            }
        }).start();
    }

    public void disconnect() {
        sleep(300);
        try {
            if (connection != null) connection.close();

            if (status) {
                //showmsg("Impresora lista");
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
					diagnosticErrorShown=false;
					showDiagnosticProgress("Preparando documento para imprimir en ZPL...");
                    if (loadFile())	doStartPrint();
                } catch (Exception e) {
					showDiagnosticError("Iniciar impresión ZPL", e);
                }
            }

        });

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Handler cbhandler = new Handler(Looper.getMainLooper());
                cbhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        msgAskRePrint();
                        //printclose.run();
                    }
                }, 200);
            }
		});
		dialog.setNeutralButton("Ver/Guardar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				showViewSaveDocument(printZebraZPL.this::msgAskPrint);
			}
		});

		dialog.show();

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
                final Handler cbhandler = new Handler(Looper.getMainLooper());
                cbhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
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

    private void showDiagnosticProgress(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast=Toast.makeText(cont, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private synchronized void showDiagnosticError(final String stage, final Throwable error) {
        if (diagnosticErrorShown) return;
        diagnosticErrorShown=true;

        final String type=error==null?"Desconocido":error.getClass().getSimpleName();
        final String message=error==null || error.getMessage()==null || error.getMessage().trim().isEmpty()
                ?"Sin detalle proporcionado":error.getMessage();
        final String detail="Etapa: "+stage+"\nTipo: "+type+"\nMensaje: "+message;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(cont)
                            .setTitle("Diagnóstico de impresión ZPL")
                            .setMessage(detail)
                            .setPositiveButton("Aceptar", null)
                            .show();
                } catch (Exception ignored) {
                    Toast toast=Toast.makeText(cont, detail, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
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
