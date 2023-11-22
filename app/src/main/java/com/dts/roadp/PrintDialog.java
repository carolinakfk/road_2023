package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class PrintDialog extends PBase {

	private printer prn;
	private Runnable printcallback,printclose;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 	{
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_print_dialog);

			super.InitBase();
			addlog("PrintDialog",""+du.getActDateTime(),gl.vend);

			printcallback= new Runnable() {
				public void run() {
					askPrint();
				}
			};

			printclose= new Runnable() {
				public void run() {
					PrintDialog.super.finish();
				}
			};

			prn=new printer(this,printclose,gl.validimp);

			final Handler shandler = new Handler();
			shandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					singlePrint();
				}
			}, 500);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			Log.e("print",e.getMessage());
		}

	}
	
	// Events
	
	// Main
	
 	public void singlePrint() {
 		prn.printask(printcallback);
 	}	
 	
	private void askPrint() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Road");
			dialog.setMessage("Impresion correcta ?");
			dialog.setNegativeButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gl.closeCliDet=true;
					gl.closeVenta=true;

					try{
						db.execSQL("UPDATE FinDia SET val7=7");
					} catch (Exception e){
					}

					PrintDialog.super.finish();
				}
			});

			dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					singlePrint();
				}
			});
			dialog.show();

		}catch (Exception ex)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			Log.d("AskPrint",ex.getMessage());
		}

	}	

	// Aux
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		try{
			super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
