package com.dts.roadp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class BaseDatosVersion {

	public ArrayList<String> items=new ArrayList<String>();
	
	private Context cont;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private String sql;	
	
	private int aver,pver;
	
	public BaseDatosVersion(Context context,SQLiteDatabase dbase,BaseDatos dbCon) {
		cont=context;
		db=dbase;
		Con=dbCon;
	}
	
	public void checkVersion(int actversion) {
		
		items.clear();
		
		aver=actversion;
		pver=getDBVersion();
		
		if (aver==pver) return;
		
		for (int version = pver; version <=aver; version++) {
			updateVersion(version);	
		}	
		
		if (processDBUpdates()) Toast.makeText(cont,"La base de datos ha sido actualizada a versión "+aver+".", Toast.LENGTH_SHORT).show();;
		
	}
	
	
	// Private

	private int getDBVersion() {
		Cursor dt;
		int dbv=0;
		
		try {
			sql="SELECT dbver FROM Params";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			dbv=dt.getInt(0);
		} catch (Exception e) {
			return -1;
		}
		
		return dbv;
	}
	
	private void updateVersion(int vers) {
		
		switch (vers) {
		case 1:  
			ver1();break;
		case 0:  
			ver0();break;	
		}		
	}
	
	private boolean processDBUpdates() {
		int icount;
		
		try {
			
			icount=items.size();
			
			db.beginTransaction();
			
			if (icount>=0) {
				for (int i = 0; i < icount; i++) {
					sql=items.get(i);
					db.execSQL(sql);
				}	
			}
			
			sql="UPDATE Params SET dbver="+aver;
		    db.execSQL(sql);
			
			db.setTransactionSuccessful();
			db.endTransaction();
		
		} catch (Exception e) {
			db.endTransaction();
			msgbox(e.getMessage()+"\n"+sql);return false;
		}	
		
		return true;		
		
	}
	
	
	// Versiones
	
	private void ver1() {
		
	}
	
	private void ver0() {
		
	}
	
	
	// Aux
	
 	private void msgbox(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
					
		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {  }
    	});
		dialog.show();
	
	}   		
	
}
