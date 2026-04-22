package com.dts.roadp;

import static org.apache.commons.io.FileUtils.copyFile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BaseDatos extends SQLiteOpenHelper {
	
	  public SQLiteDatabase vDatabase;	
	  public Context vcontext;
	  public int Created;
	  public Insert Ins;
	  public Update Upd;
	  private BaseDatosScript DBScript;

	  public static final String DB_NAME = "road.db";
	  private static final int DATABASE_VERSION = 1;

	  // ☆ Utilidad: ruta del DB nuevo (solo informativa/log)
	  public static File getNewDbFile(Context ctx) {
		return ctx.getDatabasePath(DB_NAME);
		// Si usas la opción ROAD interna:
		// return new File(resolveInternalRoadPath(ctx));
	  }

	// ☆ Ejecuta esto ANTES de abrir la BD por primera vez
	public static void ensureMigration(Context ctx) {
		try {
			File newDb = getNewDbFile(ctx);
			if (newDb.exists()) return; // ya migrado

			// Posibles ubicaciones antiguas (ajusta si usabas otras)
			File[] oldCandidates = new File[] {
					new File(Environment.getExternalStorageDirectory(), "road.db"),
					new File("/sdcard/road.db"),
					new File("/sdcard/ROAD/road.db")
			};

			for (File oldDb : oldCandidates) {
				if (oldDb.exists() && oldDb.canRead()) {
					File parent = newDb.getParentFile();
					if (parent != null && !parent.exists()) parent.mkdirs();
					copyFile(oldDb, newDb);
					// oldDb.delete(); // opcional
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ☆ Carpeta para documentos de impresión (app-specific external)
	public static File getPrintDocsDir(Context ctx) {
		File dir = new File(ctx.getExternalFilesDir(null), "ROAD");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	// ☆ Migrar documentos de impresión desde raíz a /Android/data/.../files/ROAD/
	public static void migrateLegacyPrintDocs(Context ctx) {
		File legacy = new File("/sdcard/ROAD");
		File dest = getPrintDocsDir(ctx);
		if (legacy.exists() && legacy.isDirectory()) {
			File[] files = legacy.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isFile()) {
						File target = new File(dest, f.getName());
						if (!target.exists()) {
							copyFile(f, target);
						}
						// f.delete(); // opcional
					}
				}
			}
		}
	}

	private static void copyFile(File src, File dst) {
		FileChannel in = null, out = null;
		try {
			in = new FileInputStream(src).getChannel();
			if (!dst.getParentFile().exists()) dst.getParentFile().mkdirs();
			out = new FileOutputStream(dst).getChannel();
			long size = in.size();
			long pos = 0;
			while (pos < size) pos += in.transferTo(pos, size - pos, out);
			out.force(true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (in != null) in.close(); } catch (Exception ignored) {}
			try { if (out != null) out.close(); } catch (Exception ignored) {}
		}
	}

	public BaseDatos(Context context) {
	    super(context, DB_NAME, null, DATABASE_VERSION);
	    Ins=new Insert();
	    Upd=new Update();
	    DBScript=new BaseDatosScript(context);
	    Created=0;
	    vcontext=context;
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
		 		 	   
		Created=1;
					
		if (scriptDatabase(database)==0) {
			Created=-1;return;
		}
	    
		if (scriptData(database)==0) {
			Created=-1;return;
		}
		
		dbCreated();
		
	  }

	  private int scriptDatabase(SQLiteDatabase database) {
		 return DBScript.scriptDatabase(database);
	  }
	 
	  private int scriptData(SQLiteDatabase database) {
		 return DBScript.scriptData(database);
	  } 

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		  Toast.makeText(vcontext,"UPDATE DB", Toast.LENGTH_SHORT).show();
	  }

	  // ☆ Conveniencia: abre y guarda en vDatabase
	  public void open() {
		if (vDatabase == null || !vDatabase.isOpen()) {
			vDatabase = this.getWritableDatabase();
		}
	  }

	  public Cursor OpenDT(String pSQL) {
	  	Cursor vCursor = null;
        String vError="";

		  try {
			  vCursor = vDatabase.rawQuery(pSQL, null);
			  vCursor.moveToLast();
		  }catch(Exception ex){
		  	//msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + ex.getMessage());
			  vError = ex.getMessage();
		  }

		  return vCursor;
	  }
	  
	  // Public class Insert
	  
	  public class Insert {
			
		  private List<String> clFList = new ArrayList<String>();
		  private List<String> clVList = new ArrayList<String>();
		  private String clTable;
		  
		  public Insert() {
			  clFList.clear(); 
		      clVList.clear();
		      clTable = "";
		  }
		  
		  public void init(String TableName) {
			  clFList.clear(); 
		      clVList.clear();
		      clTable = TableName;
		  }

		  public void add(String pField, String pValue , String pTipo) {
			  String SV;
			  
			  try 
			  {

				  if (pField == "") return;
				  if (pTipo == "") return;
					
				  pValue=pValue.replace("'", "");
				  SV="'" + pValue + "'";
				  
				  if (pTipo == "S") SV="'" +  pValue + "'";
				  if (pTipo == "N") SV= pValue;
				  				  
				  clFList.add(pField);
		          clVList.add(SV);
				  
			  } catch (Exception e) { }
			  
		  }
		  
		  public void add(String pField, String pValue ) {
			  String SV;
			  
			  try  {
				  if (pField == "") return;

				  pValue=pValue.replace("'", "");

				  if (pValue.equalsIgnoreCase("null")) {
					  SV = pValue;
				  } else {
					  SV = "'" + pValue + "'";
				  }

				  clFList.add(pField);
		          clVList.add(SV);
				  
			  } catch (Exception e) { }
			  
		  }

		  public void addXML(String pField, String pValue ) {
			  String SV="";

			  try  {
				  if (pField == "") return;

				  SV = pValue;

				  clFList.add(pField);
				  clVList.add(SV);

			  } catch (Exception e) { }

		  }

		  public void add(String pField, long pValue) {
			  String SV;
			  
			  try 
			  {
				  if (pField == "") {return;}
				  SV= String.valueOf(pValue);
				  				  
				  clFList.add(pField);
		          clVList.add(SV);
				  
			  } catch (Exception e) { }
			  
		  }

		  public void add(String pField, double pValue) {
			  String SV;
			  
			  try 
			  {
				  if (pField == "") {return;}
				  SV= String.valueOf(pValue);
				  				  
				  clFList.add(pField);
		          clVList.add(SV);
				  
			  } catch (Exception e) { }
			  
		  }
		   
		  public String sql() {
			  String sVal, S, SF, SV;
			  			  
			  if (clTable == "") {return "";}
			  if (clFList.isEmpty()) {return "";}
			  
			  try 
			  {

				  SV="";SF="";S="INSERT INTO " + clTable + " (";
				  for(int I = 0; I < clFList.size() ; I = I+1) {
					  sVal=clFList.get(I);
					  SF=SF + sVal; 
					  if (I < clFList.size()-1) {SF=SF + ",";}
					  
					  sVal=clVList.get(I);
					  SV=SV + sVal; 
					  if (I < clFList.size()-1) {SV=SV + ",";}
				  }
				  
				  S = S + SF + ") VALUES (" + SV + ")";

				 /* clFList.clear();
				  clVList.clear();*/

				  return S;
				 				  
			  } catch (Exception e) { 
				  return "";
			  }
			  
		  }
		  
	} 

	  // Public class Update
	  
	  public class Update {
			
		  private List<String> clFList = new ArrayList<String>();
		  private String clTable,vWhere;
		  
		  public Update() {
			  clFList.clear(); 
		      clTable = "";
		  }
		  
		  public void init(String TableName) {
			  clFList.clear(); 
		      clTable = "UPDATE " + TableName + " SET ";
		  }
	 
		  
		  public void add(String pField, String pValue , String pTipo) {
			  String SV;
			  
			  try  {

				  if (pField == "") return;
				  if (pTipo == "") return;
					
				  pValue=pValue.replace("'", "");
				  SV="'" + pValue + "'";
				  
				  if (pTipo == "S") SV="'" +  pValue + "'";
				  if (pTipo == "N") SV= pValue;
				  
				  clFList.add(pField + " = "+ SV);
				  
			  } catch (Exception e) { }
			  
		  }
		  
		  public void add(String pField, String pValue) {
			  String SV;
			  
			  try 
			  {
				  if (pField == "") return;
				  
				  pValue=pValue.replace("'", "");
				  SV="'" + pValue + "'";
				  clFList.add(pField + " = "+ SV);
			  } catch (Exception e) { }
			  
		  }

		  public void add(String pField, int pValue ) {
			  String SV;
			  
			  try  {
				  if (pField == "") return;
				  SV= String.valueOf(pValue);
				  clFList.add(pField + " = "+ SV);
			  } catch (Exception e) { }
			  
		  }
		  
		  public void add(String pField, double pValue ) {
			  String SV;
			  
			  try  {
				  if (pField == "") return;
				  SV= String.valueOf(pValue);
				  clFList.add(pField + " = "+ SV);
			  } catch (Exception e) { }
			  
		  }
		  
		  public void Where(String pWhere) {
			  vWhere = " WHERE " + pWhere;
		  }
	      	  
		  public String SQL() {
			  String sVal,vUpDate;
			  			  
			  if (clTable == "") return "";
			  if (clFList.isEmpty()) return "";
			  
			  try  {

				  vUpDate = clTable;
						  
				  for(int I = 0; I < clFList.size() ; I = I+1) {
					  sVal=clFList.get(I);
					  vUpDate=vUpDate + sVal; 
					  if (I < clFList.size()-1) {vUpDate=vUpDate + ",";}
				  }
				  
				  vUpDate=vUpDate + vWhere;
				  
				  return vUpDate;
				 				  
			  } catch (Exception e) { 
				  return "";
			  }
			  
		  }
	      
	}
	  
	private void msgbox(String msg) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);
	    	
			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_error);
			
			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {			      	
	    	    	//Toast.makeText(getApplicationContext(), "Yes button pressed",Toast.LENGTH_SHORT).show();
	    	    }
	    	});
			dialog.show();
		
		}   
	  
	    private void dbCreated() {
			AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);
	    	
			dialog.setTitle(R.string.app_name);
			dialog.setMessage("La base de datos ha sido creada.");
			dialog.setIcon(R.drawable.ic_info);
			
			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    }
	    	});
			dialog.show();
		
		}   	  
}