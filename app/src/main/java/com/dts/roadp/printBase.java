package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import datamaxoneil.connection.ConnectionBase;
import datamaxoneil.printer.DocumentEZ;
import datamaxoneil.printer.DocumentLP;
import datamaxoneil.printer.ParametersEZ;

public class printBase {

	public String printerAddress = "Unknown";
	public Runnable printclose;
	public String errmsg;
	public int prwidth;
	public boolean exitprint=false;
	
	protected Context cont;
	 
	protected ConnectionBase prconn = null;
	protected String fname;
	protected DocumentLP docLP = new DocumentLP("!");
    protected DocumentEZ docEZ = new DocumentEZ("MF204");
    protected ParametersEZ paramEZ = new ParametersEZ();

	protected Thread prthread;
	protected Handler handler = new Handler(); 
	protected Runnable callback;
	
	protected byte[] printData = {0};
	protected byte[] printData2 = {0};
	
	protected boolean hasCallback;
	
	public printBase(Context context,String printerMAC) {
		cont=context;
		printerAddress=printerMAC;	
	}
	
	// Abstract Methods
	
	public void printask(Runnable callBackHook)
	{
		hasCallback=true;
		callback=callBackHook;
	}

	public void printask(Runnable callBackHook, String fName) {
		hasCallback=true;
		callback=callBackHook;
		fname=fName;
	}

	public void printnoask(Runnable callBackHook, String fName) {
		hasCallback=true;
		callback=callBackHook;
		fname=fName;
	}

	public void printask() {
		hasCallback=false;
	}

	public void printaskBarra(ArrayList<String> listitems) {
		hasCallback=false;
	}
		
	public boolean print() {
		return true;
	}

	public boolean printBarra(ArrayList<String> listitems) {
		return true;
	}

	public void printask(String fileName) {	
		hasCallback=false;
	}
			
	public boolean print(String fileName) {
		return true;
	}

	/** Muestra el documento generado y permite guardar una copia publica bajo demanda. */
	protected void showViewSaveDocument() {
		showViewSaveDocument(null);
	}

	/** Al cerrar la vista previa, reanuda la decision de impresion si fue solicitada. */
	protected void showViewSaveDocument(Runnable onClose) {
		File document = resolveDocumentFile();
		if (document == null || !document.exists()) {
			Toast.makeText(cont, "No se encontro el documento generado.", Toast.LENGTH_LONG).show();
			runContinuation(onClose);
			return;
		}

		String content = readDocument(document);
		if (content == null) {
			runContinuation(onClose);
			return;
		}

		TextView preview = new TextView(cont);
		int padding = (int) (16 * cont.getResources().getDisplayMetrics().density);
		preview.setPadding(padding, padding, padding, padding);
		preview.setText(content);
		preview.setTextIsSelectable(true);
		preview.setHorizontallyScrolling(true);
		preview.setMovementMethod(new ScrollingMovementMethod());
		preview.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		final File source = document;
		new AlertDialog.Builder(cont)
				.setTitle(document.getName())
				.setView(preview)
				.setPositiveButton("Guardar", (dialog, which) -> {
					saveDocumentToDownloads(source);
					runContinuation(onClose);
				})
				.setNegativeButton("Cerrar", (dialog, which) -> runContinuation(onClose))
				.show();
	}

	private void runContinuation(Runnable continuation) {
		if (continuation != null) new Handler(Looper.getMainLooper()).post(continuation);
	}

	private File resolveDocumentFile() {
		String fileName = (fname == null || fname.trim().isEmpty()) ? "print.txt" : fname;
		File file = new File(AppPaths.printDir(cont), fileName);
		if (file.exists()) return file;

		// Compatibilidad con impresiones legacy que quedaron directamente bajo ROAD.
		file = new File(AppPaths.road(cont), fileName);
		return file.exists() ? file : null;
	}

	private String readDocument(File document) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(document)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append('\n');
			}
			return content.toString();
		} catch (Exception e) {
			Toast.makeText(cont, "No se pudo abrir el documento: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
			return null;
		}
	}

	private void saveDocumentToDownloads(File source) {
		String originalName = source.getName();
		int extensionAt = originalName.lastIndexOf('.');
		String baseName = extensionAt > 0 ? originalName.substring(0, extensionAt) : originalName;
		String extension = extensionAt > 0 ? originalName.substring(extensionAt) : ".txt";
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
				.format(new Date());
		String displayName = baseName + "_" + timestamp + extension;

		Backups.exportFileToDownloads(cont, source, "ROAD/Documentos", displayName, "text/plain");
	}

}
