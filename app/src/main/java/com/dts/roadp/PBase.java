package com.dts.roadp;

import android.app.Activity;
import android.os.Bundle;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PBase extends Activity {

	protected int active;
	protected SQLiteDatabase db;
	protected BaseDatos Con;
	protected BaseDatos.Insert ins;
	protected BaseDatos.Update upd;
	protected String sql;
	
	protected Application vApp;
	protected appGlobals gl;
	protected MiscUtils mu;
	protected DateUtils du;
	protected clsClasses clsCls = new clsClasses();
	protected InputMethodManager keyboard;	
	
	protected int itemid,browse,mode;
	protected int selid,selidx,deposito;
	protected long fecha;
	protected String s,ss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plist_base);

		// Log de ciclo de vida para detectar cuándo “se esconde”
		registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			@Override public void onActivityCreated(Activity a, Bundle b) {}
			@Override public void onActivityStarted(Activity a) {}
			@Override public void onActivityResumed(Activity a)  { writeLife("RESUMED", a.getClass().getSimpleName()); }
			@Override public void onActivityPaused(Activity a)   { writeLife("PAUSED",  a.getClass().getSimpleName()); }
			@Override public void onActivityStopped(Activity a)  { writeLife("STOPPED", a.getClass().getSimpleName()); }
			@Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
			@Override public void onActivityDestroyed(Activity a) {}
		});
	}

	// Escribe en: /sdcard/Android/data/com.dts.roadp/files/ROAD/Logs/roadlog.txt
	private void writeLife(String event, String activity) {
		try {
			File base = getExternalFilesDir(null); // carpeta de la app
			if (base == null) return;
			File logsDir = new File(new File(base, "ROAD"), "Logs");
			if (!logsDir.exists()) logsDir.mkdirs();
			File logFile = new File(logsDir, "roadlog.txt");

			String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
					.format(new Date());

			try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
				pw.println(ts + " | " + event + " | " + activity);
			}
		} catch (Exception ignored) {}
	}

	public void InitBase(){
		
		Con = new BaseDatos(this);

	    opendb();

	    ins=Con.Ins;upd=Con.Upd;
		
		vApp=this.getApplication();
		gl=((appGlobals) this.getApplication());
		
		mu=new MiscUtils(this,gl.peMon);
		du=new DateUtils();fecha=du.getActDateTime();
		
		keyboard = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);

	    browse=0;
	}
	
	// Web service call back
	public void wsCallBack(int callmode, Boolean throwing, String errmsg) throws Exception {
		if (throwing) throw new Exception(errmsg);
	}

	protected void wsCallBack(Boolean throwing,String errmsg) throws Exception {
		if (throwing) throw new Exception(errmsg);
	}

	// Aux
	
	protected void closekeyb(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
		
	protected void showkeyb(){
		if (keyboard != null) {
			keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
	}
		
	protected void hidekeyb() {
		keyboard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}	
	
	protected void msgbox(String msg){
		try{
			mu.msgbox(msg);
		}catch (Exception ex){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
		}
	}

	protected void toast(String msg) {
		toastcent(msg);
	}
	
	protected void toast(double val) {
		toastcent(""+val);
	}
	
	protected void toastlong(String msg) {
		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected void toastlongd(String msg) {
		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
		toast.show();
	}

	protected void toastcent(String msg) {

		if (mu.emptystr(msg)) return;

		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}	
	
	protected void addlog(final String methodname, String msg, String info) {

		final String vmethodname = methodname;
		final String vmsg = msg;
		final String vinfo = info;

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setAddlog(vmethodname,vmsg, vinfo);
			}
		}, 500);

	}

	// Escribe en: /sdcard/Android/data/com.dts.roadp/files/ROAD/Logs/roadlog.txt
	protected void setAddlog(String methodname, String msg, String info) {
		java.io.BufferedWriter writer = null;

		try {
			// Carpeta segura (app-specific external)
			java.io.File logsDir = new java.io.File(AppPaths.road(this), "Logs");
			if (!logsDir.exists()) logsDir.mkdirs();

			java.io.File logFile = new java.io.File(logsDir, "roadlog.txt");

			String nl = System.getProperty("line.separator");
			String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					java.util.Locale.getDefault()).format(new java.util.Date());

			try (java.io.FileWriter fw = new java.io.FileWriter(logFile, true);
				 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

				bw.write(ts + " | Método: " + methodname + " | Mensaje: " + msg + " | Info: " + info);
				bw.write(nl);
			}

		} catch (Exception e) {
			msgbox("Error " + e.getMessage());
		}
	}


	protected String iif(boolean condition,String valtrue,String valfalse) {
		if (condition) return valtrue;else return valfalse;
	}

	protected double iif(boolean condition,double valtrue,double valfalse) {
		if (condition) return valtrue;else return valfalse;
	}

	protected double iif(boolean condition,int valtrue,int valfalse) {
		if (condition) return valtrue;else return valfalse;
	}


	// Activity Events

	@Override
 	protected void onResume() {
		try {
			opendb();
		} catch(Exception ex) {}
		super.onResume();
		lifeLog("RESUMED");
	}

	@Override
	protected void onPause() {
		try {
			Con.close();
		} catch (Exception e) { }
		active= 0;
	    super.onPause();
		lifeLog("RESUMED");
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	public void opendb() {
		try {
			db = Con.getWritableDatabase();
			if (db!= null) {
				Con.vDatabase=db;
				active=1;
			} else {
				active = 0;
			}
	    } catch (Exception e) {
	    	//mu.msgbox(e.getMessage());
			Log.w("Error",e.getMessage());
	    	active= 0;
	    }
	}

	@Override
	protected void onStop() {
		super.onStop();
		lifeLog("STOPPED");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		lifeLog("DESTROYED");
	}

	// Muy útil para saber si “se fue al Home”
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		lifeLog("USER_LEAVE_HINT");
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		lifeLog("WINDOW_FOCUS=" + hasFocus);
	}

	private void lifeLog(String event) {
		try {
			java.io.File base = getExternalFilesDir(null); // /sdcard/Android/data/<pkg>/files
			if (base == null) return;
			java.io.File logsDir = new java.io.File(new java.io.File(base, "ROAD"), "Logs");
			if (!logsDir.exists()) logsDir.mkdirs();
			java.io.File logFile = new java.io.File(logsDir, "roadlog.txt");

			String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					java.util.Locale.getDefault()).format(new java.util.Date());
			try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(logFile, true))) {
				pw.println(ts + " | " + event + " | " + getClass().getSimpleName());
			}
		} catch (Exception ignored) {}
	}
	
}
