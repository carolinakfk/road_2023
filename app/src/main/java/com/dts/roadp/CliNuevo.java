package com.dts.roadp;

import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class CliNuevo extends PBase {

	private EditText txtNom, txtDir, txtTel, txtNit;
	private Spinner spinList;
	private CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7;

	private ArrayList<String> spincode = new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();

	private int nivel = 0;
	private int d1, d2, d3, d4, d5, d6, d7;

	// Location
	private LocationManager locationManager;
	private Location location;

	private LocationListener locationListener;

	private boolean isGPSEnabled, isNetworkEnabled, canGetLocation;
	private double latitude, longitude;
	private String cod;

	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MIN_TIME_BW_UPDATES = 1000; // in Milliseconds

	@Override

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_nuevo);

		super.InitBase();
		addlog("CliNuevo", "" + du.getActDateTime(), gl.vend);

		spinList = (Spinner) findViewById(R.id.spinner1);
		txtNom = (EditText) findViewById(R.id.txtCNNom);
		txtDir = (EditText) findViewById(R.id.txtCNDir);
		txtTel = (EditText) findViewById(R.id.txtCNTel);
		txtNit = (EditText) findViewById(R.id.txtCNNit);

		cb1 = (CheckBox) findViewById(R.id.checkBox1);
		cb2 = (CheckBox) findViewById(R.id.checkBox2);
		cb3 = (CheckBox) findViewById(R.id.checkBox3);
		cb4 = (CheckBox) findViewById(R.id.checkBox4);
		cb5 = (CheckBox) findViewById(R.id.checkBox5);
		cb6 = (CheckBox) findViewById(R.id.checkBox6);
		cb7 = (CheckBox) findViewById(R.id.checkBox7);

		fillSpinner();
		setHandlers();

		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location arg0) {
			}

			@Override
			public void onProviderDisabled(String arg0) {
			}

			@Override
			public void onProviderEnabled(String arg0) {
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}

		};

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				lastKnowPos();
			}
		}, 500);

	}

	// Events

	public void doSave(View view) {

		try {
			if (!checkValues()) return;

			msgAskSave("Crear cliente nuevo");
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}


	// Main

	private void setHandlers() {

		// Spin

		spinList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				TextView spinlabel;
				String scod;

				try {
					spinlabel = (TextView) parentView.getChildAt(0);
					spinlabel.setTextColor(Color.BLACK);
					spinlabel.setPadding(5, 0, 0, 0);
					spinlabel.setTextSize(16);
				/*} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
				}

				try {*/
					scod = spincode.get(position);
					nivel = Integer.parseInt(scod);
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
					nivel = 0;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				return;
			}

		});

	}

	private void saveData() {

		String corel = mu.getCorelBase();

		try {

			db.beginTransaction();

			ins.init("D_CLINUEVO");

			ins.add("CODIGO", corel);
			ins.add("RUTA", gl.ruta);
			ins.add("FECHA", fecha);
			ins.add("NOMBRE", txtNom.getText().toString());
			if (gl.peModal.equalsIgnoreCase("TOL")) {
				ins.add("NEGOCIO", gl.cuentaCliNuevo);
			}else{
				ins.add("NEGOCIO", "");
			}
			ins.add("DIRECCION", txtDir.getText().toString() + "");
			ins.add("TELEFONO", txtTel.getText().toString() + "");
			ins.add("NIT", txtNit.getText().toString() + "");
			ins.add("TIPONEG", "");
			ins.add("NIVPRECIO", nivel);

			ins.add("DIA1", valToStr(d1));
			ins.add("DIA2", valToStr(d2));
			ins.add("DIA3", valToStr(d3));
			ins.add("DIA4", valToStr(d4));
			ins.add("DIA5", valToStr(d5));
			ins.add("DIA6", valToStr(d6));
			ins.add("DIA7", valToStr(d7));

			ins.add("ORDVIS", 0);
			ins.add("BAND1", "");
			ins.add("BAND2", "");
			ins.add("STATCOM", "N");
            ins.add("IMAGEN", "");
            ins.add("CODIGO_ERP", "");

			db.execSQL(ins.sql());

			// P_CLIENTE

			ins.init("P_CLIENTE");

			ins.add("CODIGO", corel);
			ins.add("NOMBRE", txtNom.getText().toString());
			ins.add("BLOQUEADO", "N");
			ins.add("TIPONEG", "");
			ins.add("TIPO", "NUEVO");
			if (gl.peModal.equalsIgnoreCase("TOL")) {
				ins.add("SUBTIPO", gl.cuentaCliNuevo);
			}else{
				ins.add("SUBTIPO", "PRE");
			}
			ins.add("CANAL", "PRE");
			ins.add("SUBCANAL", "PRE");
			ins.add("NIVELPRECIO", nivel);

			ins.add("MEDIAPAGO", "1");
			ins.add("LIMITECREDITO", 0);
			ins.add("DIACREDITO", 0);
			ins.add("DESCUENTO", "N");
			ins.add("BONIFICACION", "N");
			ins.add("ULTVISITA", fecha);

			ins.add("IMPSPEC", 0);
			ins.add("INVTIPO", "N");
			ins.add("INVEQUIPO", "N");
			ins.add("INV1", "N");
			ins.add("INV2", "N");
			ins.add("INV3", "N");

			ins.add("NIT", txtNit.getText().toString() + "");
			ins.add("MENSAJE", "N");
			ins.add("TELEFONO", txtTel.getText().toString() + "");
			ins.add("DIRTIPO", "N");
			ins.add("DIRECCION", txtDir.getText().toString() + "");
			ins.add("SUCURSAL", "1");

			ins.add("COORX", 0);
			ins.add("COORY", 0);
			ins.add("FIRMADIG", "N");
			ins.add("CODBARRA", "");
			ins.add("VALIDACREDITO", "N");
			ins.add("PRECIO_ESTRATEGICO", "N");
			ins.add("NOMBRE_PROPIETARIO", "");
			ins.add("NOMBRE_REPRESENTANTE", "");

			ins.add("BODEGA", "");
			ins.add("COD_PAIS", "");
			ins.add("FACT_VS_FACT", "0");
			ins.add("CHEQUEPOST", "N");

			ins.add("DESCUENTO", "N");
			ins.add("BONIFICACION", "N");

			ins.add("PERCEPCION", 0);
			ins.add("TIPO_CONTRIBUYENTE", "");
			ins.add("ID_DESPACHO", 0);
			ins.add("ID_FACTURACION", 0);
			ins.add("MODIF_PRECIO", 0);
            ins.add("PRIORIZACION", "");

			db.execSQL(ins.sql());

			// P_CLIRUTA
			int dv;
			for (int i = 0; i < 8; i++) {

				dv = 0;
				switch (i) {
					case 1:
						dv = d1;
						break;
					case 2:
						dv = d2;
						break;
					case 3:
						dv = d3;
						break;
					case 4:
						dv = d4;
						break;
					case 5:
						dv = d5;
						break;
					case 6:
						dv = d6;
						break;
					case 7:
						dv = d7;
						break;
				}

				if (dv == 1) {
					ins.init("P_CLIRUTA");

					ins.add("RUTA", ((appGlobals) vApp).ruta);
					ins.add("CLIENTE", corel);
					ins.add("SEMANA", 0);
					ins.add("DIA", i);
					ins.add("SECUENCIA", -1);
					ins.add("BANDERA", -1);

					db.execSQL(ins.sql());
				}

			}

		//	try {
				ins.init("D_CLICOORD");

				ins.add("CODIGO", corel);
				ins.add("STAMP", du.getCorelBase());
				ins.add("COORX", latitude);
				ins.add("COORY", longitude);
				ins.add("STATCOM", "N");

				db.execSQL(ins.sql());
		/*	} catch (SQLException e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				msgbox(e.getMessage());
			}
		*/
			db.setTransactionSuccessful();
			db.endTransaction();

			Toast.makeText(this, "Cliente nuevo creado", Toast.LENGTH_SHORT).show();
			finish();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			db.endTransaction();
			mu.msgbox(e.getMessage());
		}
	}

	// Location

	private void lastKnowPos() {

		latitude = 0;
		longitude = 0;

		try {
			getLocation();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			latitude = 0;
			longitude = 0;
		}

		toastcent(latitude + " , " + longitude);

	}

	public Location getLocation() {

		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!isGPSEnabled) toastcent("¡GPS Deshabilitado!");

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				this.canGetLocation = false;
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
						//return TODO;
					}
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return null;
		}

		return location;
	}


	// Aux
	
	private void fillSpinner(){
		Cursor DT;

		try {

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				sql = "SELECT Codigo,Nombre FROM P_NIVELPRECIO WHERE NOMBRE = 'GENERALES'";
			} else {
				sql = "SELECT Codigo,Nombre FROM P_NIVELPRECIO ORDER BY Codigo";
			}

			DT = Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				spincode.add(DT.getString(0));
				spinlist.add(DT.getString(1));

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinlist);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spinList.setAdapter(dataAdapter);

			spinList.setSelection(0);


		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			mu.msgbox(e.getMessage());
		}
		
	}		
	
	private boolean checkValues(){
		final Calendar c = Calendar.getInstance();
		int sc,dw;
		String s;


		try{
			s=txtNom.getText().toString();
			if (mu.emptystr(s)) {
				mu.msgbox("Falta nombre de cliente");return false;
			}

			s=txtDir.getText().toString();
			if (mu.emptystr(s)) {
				mu.msgbox("Falta la dirección");return false;
			}

			s=txtNit.getText().toString();
			if (mu.emptystr(s)) {
				mu.msgbox("Falta el NIT");return false;
			}

			dw = c.get(Calendar.DAY_OF_WEEK);
			if (dw==0) dw=7; else dw-=1;

			d1=0;d2=0;d3=0;d4=0;d5=0;d6=0;d7=0;

			if (cb1.isChecked()) d1=1;
			if (cb2.isChecked()) d2=1;
			if (cb3.isChecked()) d3=1;
			if (cb4.isChecked()) d4=1;
			if (cb5.isChecked()) d5=1;
			if (cb6.isChecked()) d6=1;
			if (cb7.isChecked()) d7=1;

			sc=d1+d2+d3+d4+d5+d6+d7;

			if (sc==0) {
				switch (dw) {
					case 1:d1=1;break;
					case 2:d2=1;break;
					case 3:d3=1;break;
					case 4:d4=1;break;
					case 5:d5=1;break;
					case 6:d6=1;break;
					case 7:d7=1;break;
				}
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return true;
	}
	
	private String valToStr(int val){
		if (val==1) return "S"; else return "N";
	}
	
	private void msgAskSave(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveData();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	
	
	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	
	
	private void doExit(){
		try{
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		try{
			msgAskExit("¿Salir sin guardar datos?");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
}
