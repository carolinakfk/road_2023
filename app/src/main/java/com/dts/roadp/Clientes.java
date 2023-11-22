package com.dts.roadp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsCDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Clientes extends PBase {

	private ListView listView;
	private Spinner spinList, spinFilt;
	private EditText txtFiltro;
	private TextView lblCant;
	private ImageView cliNuevo;

	private ArrayList<clsCDB> items = new ArrayList<clsCDB>();
	private ArrayList<String> cobros = new ArrayList<String>();
	private ArrayList<String> ppago = new ArrayList<String>();
	private ArrayList<String> prefacturas = new ArrayList<String>();

	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> listcode = new ArrayList<String>();
	private ArrayList<String> listname = new ArrayList<String>();

	private ListAdaptCliList adapter;
	private clsCDB selitem;
	private AppMethods app;

	private int selidx, fecha, dweek, browse;
	private String selid, bbstr, bcode;
	private boolean scanning = false;

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
		setContentView(R.layout.activity_clientes);

		super.InitBase();
		addlog("Clientes", "" + du.getActDateTime(), gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		spinList = (Spinner) findViewById(R.id.spinner1);
		spinFilt = (Spinner) findViewById(R.id.spinner8);
		txtFiltro = (EditText) findViewById(R.id.txtMonto);
		lblCant = (TextView) findViewById(R.id.lblCant);
		cliNuevo = (ImageView) findViewById(R.id.imgImg);

		app = new AppMethods(this, gl, Con, db);
		gl.validimp = app.validaImpresora();
		if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

		setHandlers();

		selid = "";
		selidx = -1;

		dweek = mu.dayofweek();

		if (gl.tolsuper) spinList.setVisibility(View.INVISIBLE);
		fillSpinners();

		if (gl.rutatipog.equals("C")){
			cliNuevo.setVisibility(View.INVISIBLE);
		}else{
			cliNuevo.setVisibility(View.VISIBLE);
		}

		listItems();

		closekeyb();

		gl.escaneo = "N";

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			bcode = txtFiltro.getText().toString().trim();
			barcodeClient();
		}
		return super.dispatchKeyEvent(e);
	}


	// Events

	public void applyFilter(View view) {
		try {
			//listItems();
			//hidekeyb();
			txtFiltro.setText("");
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void showVenta(View view) {

		try {
			gl.tcorel = mu.getCorelBase();//gl.ruta/

			//#AT20220422 Elimina datos de la tabla TMP_D_CLINUEVOT_IMAGEN
			sql="DELETE FROM TMP_D_CLINUEVOT_IMAGEN";
			db.execSQL(sql);

			if(gl.peModal.equalsIgnoreCase("TOL")) {
				Intent intent = new Intent(this, CliNuevoT.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, CliNuevo.class);
				startActivity(intent);
			}
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void orderDist(View view) {
		msgAskDist("Ordenar los clientes por distancia aérea");
	}

	private void setHandlers() {

		try {

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					Object lvObj = listView.getItemAtPosition(position);
					clsCDB sitem = (clsCDB) lvObj;
					selitem = sitem;

					selid = sitem.Cod;
					selidx = position;
					adapter.setSelectedIndex(position);

					//#HS_20181211 Carga la lista de incidencias por no lectura y muestra el dialogo
					if (gl.incNoLectura == true) {
						listNoLectura();
					} else {
						showCliente();
					}

				}

				;
			});

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					boolean pedit, pbor;
					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsClasses.clsCDB item = (clsClasses.clsCDB) lvObj;

						selid = item.Cod;
						selidx = position;
						adapter.setSelectedIndex(position);

						pedit = puedeeditarse();
						pbor = puedeborrarse();

						if (pbor && pedit) {
							showItemMenu();
						} else {
							if (pbor) msgAskBor("Eliminar cliente nuevo");
							if (pedit) msgAskEdit("Cambiar datos de cliente nuevo");
						}
					} catch (Exception e) {
						addlog(new Object() {
						}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
					}
					return true;
				}
			});

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					finish();
				}

				public void onSwipeLeft() {
				}
			});

			spinList.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;
					String scod;

					//	try {
					spinlabel = (TextView) parentView.getChildAt(0);
					spinlabel.setTextColor(Color.BLACK);
					spinlabel.setPadding(5, 0, 0, 0);
					spinlabel.setTextSize(18);

					dweek = position;

					listItems();

				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}*/

				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});

			spinFilt.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					listItems();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});

			txtFiltro.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {
					int tl = txtFiltro.getText().toString().length();
					if (tl > 1) gl.escaneo = "S";
					if (tl == 0 || tl > 1) listItems();
				}
			});

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

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			mu.msgbox(e.getMessage());
		}

	}


	// Main

	public void listItems() {
		Cursor DT;
		clsCDB vItem;
		int vP;
		String id, filt, ss;

		items.clear();

		selidx = -1;
		vP = 0;
		filt = txtFiltro.getText().toString().replace("'", "");

		try {

			cobros.clear();
			sql = "SELECT DISTINCT CLIENTE FROM P_COBRO ";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				for (int i = 0; i < DT.getCount(); i++) {
					cobros.add(DT.getString(0));
					DT.moveToNext();
				}
			}

			ppago.clear();
			sql = "SELECT D_FACTURA.CLIENTE " +
					"FROM D_FACTURA INNER JOIN  D_FACTURAP ON  D_FACTURA.COREL = D_FACTURAP.COREL " +
					"GROUP BY  D_FACTURA.CLIENTE, D_FACTURA.COREL, D_FACTURA.ANULADO " +
					"HAVING  (D_FACTURA.ANULADO='N') AND (SUM(D_FACTURAP.VALOR=0))";

			sql = "SELECT DISTINCT CLIENTE FROM D_FACTURA WHERE (ANULADO='N') AND (COREL NOT IN " +
					"  (SELECT DISTINCT D_FACTURA_1.COREL " +
					"   FROM D_FACTURA AS D_FACTURA_1 INNER JOIN " +
					"   D_FACTURAP ON D_FACTURA_1.COREL=D_FACTURAP.COREL))";

			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				for (int i = 0; i < DT.getCount(); i++) {
					ss = DT.getString(0);
					if (!ppago.contains(ss)) ppago.add(ss);
					DT.moveToNext();
				}
			}

			//#CKFK 20210803 Agregué filtro por prefacturas
			prefacturas.clear();
			sql = "SELECT DISTINCT CLIENTE " +
				  "FROM DS_PEDIDO " +
				  "WHERE ANULADO = 'N' AND BANDERA = 'N'";

			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				for (int i = 0; i < DT.getCount(); i++) {
					ss = DT.getString(0);
					if (!prefacturas.contains(ss)) {
						prefacturas.add(ss);
					}
					DT.moveToNext();
				}
			}

			sql = "SELECT DISTINCT P_CLIRUTA.CLIENTE,P_CLIENTE.NOMBRE,P_CLIRUTA.BANDERA,P_CLIENTE.COORX,P_CLIENTE.COORY " +
					"FROM P_CLIRUTA INNER JOIN P_CLIENTE ON P_CLIRUTA.CLIENTE=P_CLIENTE.CODIGO " +
					"WHERE (1=1) ";

			if (mu.emptystr(filt)) {
				if (!gl.tolsuper) {
					if (dweek != 0) sql += "AND (P_CLIRUTA.DIA =" + dweek + ") ";
				} else {
					sql += "AND (P_CLIRUTA.DIA =1) ";
				}
			}

			if (!mu.emptystr(filt)) {
				sql += "AND ((P_CLIRUTA.CLIENTE LIKE '%" + filt + "%') OR (P_CLIENTE.NOMBRE LIKE '%" + filt + "%')) ";
			}
			sql += "ORDER BY P_CLIRUTA.SECUENCIA,P_CLIENTE.NOMBRE";

			DT = Con.OpenDT(sql);

			lblCant.setText("" + DT.getCount() + "");

			if (DT.getCount() > 0) {

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					id = DT.getString(0);

					vItem = clsCls.new clsCDB();

					vItem.Cod = DT.getString(0);
					ss = DT.getString(0);
					vItem.Desc = DT.getString(1);
					vItem.Bandera = DT.getInt(2);
					vItem.Adds = "";
					vItem.coorx = DT.getDouble(3);
					vItem.coory = DT.getDouble(4);

					if (cobros.contains(ss)) vItem.Cobro = 1;
					else vItem.Cobro = 0;
					if (ppago.contains(ss)) vItem.ppend = 1;
					else vItem.ppend = 0;
					if (prefacturas.contains(ss)) vItem.prefacturas = 1;
					else vItem.prefacturas = 0;

					switch (spinFilt.getSelectedItemPosition()) {
						case 0:
							items.add(vItem);
							break;
						case 1:
							if (vItem.Cobro == 1) items.add(vItem);
							break;
						case 2:
							if (vItem.ppend == 1) items.add(vItem);
							break;
						case 3:
							if (vItem.prefacturas == 1) items.add(vItem);
							break;
					}

					if (id.equalsIgnoreCase(selid)) selidx = vP;
					vP += 1;

					DT.moveToNext();
				}

				if(DT!=null) DT.close();
			}else{
				spinList.setSelection(0);
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			mu.msgbox(e.getMessage());
		}

		adapter = new ListAdaptCliList(this, items);
		listView.setAdapter(adapter);

		if (selidx > -1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}

	}

	public void showCliente() {
		Cursor DT;
		float[] results = new float[1];
		double cx=0,cy=0;

		gl.gpsdist=-1;

		try {

			sql="SELECT COORX,COORY FROM P_CLIENTE WHERE CODIGO='"+selid+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			cx=DT.getDouble(0);	cy=DT.getDouble(1);
			if (cx+cy==0) throw new Exception();

			getLocation();
			if (latitude+longitude==0) throw new Exception();
			Location.distanceBetween(cx,cy,latitude,longitude, results);

			gl.gpsdist=(int) results[0];

			if (gl.tolsuper) spinList.setVisibility(View.INVISIBLE);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			gl.gpsdist=-1;
		}

		try {
			gl.cliente = selid;

			gl.closeCliDet = false;
			gl.closeVenta = false;

			Intent intent;
			intent = new Intent(this, CliDet.class);
			startActivity(intent);
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void editCliente() {
		try {
			gl.tcorel = selid;
			startActivity(new Intent(this, CliNuevoAprEdit.class));
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void barcodeClient() {
		Cursor dt;

		try {
			sql = "SELECT Codigo FROM P_CLIENTE WHERE CODBARRA='" + bcode + "'";
			dt = Con.OpenDT(sql);

			if (dt.getCount() == 0) {
				msgbox("Cliente no existe " + bcode + " ");
				txtFiltro.setText("");
				txtFiltro.requestFocus();
				return;
			}

			dt.moveToFirst();
			selid = dt.getString(0);
			showCliente();

			txtFiltro.setText("");
			txtFiltro.requestFocus();

			if(dt!=null) dt.close();

		} catch (Exception e) {
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}
	}


	// Distancia

	private void ordenarPorDistancia() {
		float[] results = new float[1];

		if (items.size() == 0) return;

		latitude = 0;longitude = 0;

		try {
			getLocation();
			if (latitude + longitude == 0) throw new Exception();
		} catch (Exception e) {
			toast("No se puede definir posición actual");return;
		}

		for (int i = 0; i < items.size(); i++) {
			try {
				if (items.get(i).coorx+items.get(i).coory==0) {
					items.get(i).valor=1000000;
				} else {
					Location.distanceBetween(items.get(i).coorx,items.get(i).coory,latitude,longitude, results);
					items.get(i).valor=results[0];
					items.get(i).Adds=" [ "+mu.frmint(items.get(i).valor)+"m ]";
				}
			} catch (Exception e) {
				items.get(i).valor=1000000;
			}

			if (items.get(i).valor>=1000000) items.get(i).Adds="";
		}

		Collections.sort(items, new distanceComparator());

		adapter = new ListAdaptCliList(this, items);
		listView.setAdapter(adapter);

		adapter.setSelectedIndex(0);
		listView.setSelection(0);

	}

	public class distanceComparator implements Comparator<clsCDB> {
		public int compare(clsCDB left, clsCDB right) {
				 return (int)left.valor-(int)right.valor;
		}
	}

	public Location getLocation() {

		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!isGPSEnabled) 	toastcent("¡GPS Deshabilitado!");

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				this.canGetLocation = false;
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void showItemMenu() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Eliminar cliente","Cambiar datos"};

			AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
			menudlg.setTitle("Cliente nuevo");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
						case 0:
							msgAskBor("Eliminar cliente");break;
						case 1:
							editCliente();break;
					}

					dialog.cancel();
				}
			});

			menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			Dialog = menudlg.create();
			Dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

    }

	private void fillSpinners(){
		try{
			List<String> dlist = new ArrayList<String>();

			dlist.add("Todos");
			dlist.add("Lunes");
			dlist.add("Martes");
			dlist.add("Miercoles");
			dlist.add("Jueves");
			dlist.add("Viernes");
			dlist.add("Sabado");
			dlist.add("Domingo");

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dlist);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spinList.setAdapter(dataAdapter);
			spinList.setSelection(dweek);


			List<String> flist = new ArrayList<String>();
			flist.add("Todos");
			flist.add("Con cobros");
			flist.add("Pago pendiente");
			if (gl.rutatipog.equals("T") || gl.rutatipog.equals("D")){
				flist.add("Prefacturas");
			}

			ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, flist);
			dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spinFilt.setAdapter(dataAdapter2);

			if (gl.filtrocli==-1) {
				spinFilt.setSelection(0);
			} else {
				spinFilt.setSelection(gl.filtrocli);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private boolean puedeborrarse() {
		Cursor dt;
		String sql = "";

		try {
			sql="SELECT * FROM D_CLINUEVO WHERE CODIGO='"+selid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

			sql="SELECT * FROM D_FACTURA WHERE CLIENTE='"+selid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) return false;

			if(dt!=null) dt.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private boolean puedeeditarse() {
		Cursor dt;
		String sql = "";

		try {
			sql="SELECT * FROM D_CLINUEVO WHERE CODIGO='"+selid+"' AND STATCOM='N'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

			if(dt!=null) dt.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private void msgAskBor(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setMessage("¿" + msg + "?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					borraCliNuevo();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskEdit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setMessage("¿" + msg + "?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					editCliente();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskDist(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setMessage("¿" + msg + "?");
			dialog.setTitle("Clientes");
			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ordenarPorDistancia();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void doSSend(View view) {
	    //gl.URLtemp="http://192.168.1.137/wsAndr/wsAndr.asmx";

		if (getWSURL()) startActivity(new Intent(this,ComWSSend.class));
	}

	private void msgAskSend(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Pedido");
		dialog.setMessage("¿" + msg + "?");

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Clientes.this,ComWSSend.class));
			}
		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});

		dialog.show();

	}

	private void borraCliNuevo() {
		try {
			db.beginTransaction();

			db.execSQL("DELETE FROM D_CLINUEVO WHERE CODIGO='"+selid+"'");
			db.execSQL("DELETE FROM P_CLIRUTA WHERE CLIENTE='"+selid+"'");
					
			db.setTransactionSuccessful();
			db.endTransaction();
			
			listItems();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox(e.getMessage());
		}
	}

	private void listNoLectura(){
		Cursor DT;
		String code,name;

		listcode.clear();listname.clear();

		try {

			sql="SELECT Codigo,Nombre FROM P_CODNOLEC ORDER BY Nombre";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				code = String.valueOf(DT.getInt(0));
				name = DT.getString(1);

				listcode.add(code);
				listname.add(name);
				DT.moveToNext();
			}
			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());return;
		}

		showIncNoLectura();

	}

	public void showIncNoLectura() {
		try{
			final AlertDialog Dialog;

			final String[] selitems = new String[listname.size()];

			for (int i = 0; i < listname.size(); i++) {
				selitems[i] = listname.get(i);
			}

			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Incidencia de no lectura");

			mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
						showCliente();
				}
			});

			mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void enviaPedido() {
		boolean autoenvio=false;

		try {
			sql = "SELECT ENVIO_AUTO_PEDIDOS FROM P_RUTA";
			Cursor DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				autoenvio = DT.getInt(0)==1;
			}
		} catch (Exception e) {
			autoenvio=false;
		}

		if (gl.tolsuper && autoenvio) {
			gl.tolpedsend=false;

			//if (getWSURL())
			Intent intent=new Intent(this,ComWSSend.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			//msgAskSend("Enviar pedido");
		}
	}

	public boolean getWSURL() {

		try {
			if (gl.isOnWifi == 1) {
				gl.URLtemp = gl.URLLocal;
			} else {
				gl.URLtemp = gl.URLRemoto;
			}

			if (gl.URLtemp.isEmpty()) {
				msgbox("No existe configuración para transferencia de datos");
				return false;
			}

			return true;
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			mu.msgbox(e.getMessage());
			return false;
		}
	}

	// Activity Events
	
	protected void onResume() {
		try{
			super.onResume();
			listItems();
			enviaPedido();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
