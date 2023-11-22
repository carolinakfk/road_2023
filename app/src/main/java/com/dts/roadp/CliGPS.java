package com.dts.roadp;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class CliGPS extends PBase {

	private TextView lblPos, lblGPS;
	private ProgressBar pbar;
	private ImageView imgMap;

	private boolean idle = true;

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
		setContentView(R.layout.activity_cli_gps);

		super.InitBase();
		addlog("CliGPS", "" + du.getActDateTime(), gl.vend);

		lblPos = (TextView) findViewById(R.id.textView3);
		lblGPS = (TextView) findViewById(R.id.textView1);

		imgMap = (ImageView) findViewById(R.id.imageView2);
		pbar = (ProgressBar) findViewById(R.id.progressBar1);

		initSession();

	}


	// Events

	public void lastScreen(View view) {
		try {
			super.finish();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	public void applyGPS(View view) {
		try {
			if (latitude == 0 && longitude == 0 && !gl.gpsCliente) {
				askApply();
			} else if(gl.gpsCliente){
				gl.gpspx = latitude;
				gl.gpspy = longitude;

				gl.gpsCliente = false;
				super.finish();
			} else {
				updateItem(latitude, longitude);
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
		}

	}


	// Main

	private void initSession() {

		try {
			cod = gl.cliente;

			lblPos.setText("0.00000000 , 0.00000000");
			lblGPS.setText("Conectando ...");

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
			handler.postDelayed(() -> lastKnowPos(), 500);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void showStaticMap() {

		try {
			if (latitude == 0 && longitude == 0) {
				toastcent("¡Posición desconicida, no se puede mostrar mapa!");
				imgMap.setImageResource(R.drawable.blank48);
				return;
			}


			final String URL = "https://maps.googleapis.com/maps/api/staticmap?" +
					"center=" + latitude + "," + longitude + "&" +
					"zoom=16&size=400x400&" +
					"markers=color:red%7Clabel:C%7C" + latitude + "," + longitude + "&" +
					"key=AIzaSyAeDWYjkjRi9vQVk7ITRgvAzV3ktpWyhP0";

			AsyncTask<Void, Void, Bitmap> setImageFromUrl = new AsyncTask<Void, Void, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Void... params) {
					Bitmap bmp = null;

					try {
						HttpClient httpclient = new DefaultHttpClient();
						HttpGet request = new HttpGet(URL);

						InputStream in = null;

						HttpResponse response = httpclient.execute(request);
						in = response.getEntity().getContent();
						bmp = BitmapFactory.decodeStream(in);
						in.close();
					} catch (Exception e) {
						return null;
					}

					return bmp;
				}

				protected void onPostExecute(Bitmap bmp) {

					try {
						if (bmp != null) {
							imgMap.setImageBitmap(bmp);
						} else {
							imgMap.setImageResource(R.drawable.blank48);
						}
					} catch (Exception e) {
						imgMap.setImageResource(R.drawable.blank48);
					}

					pbar.setVisibility(View.INVISIBLE);
				}

			};

			pbar.setVisibility(View.VISIBLE);
			setImageFromUrl.execute();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
		}

	}

	private void updateItem(double px, double py) {
		try {
			sql = "UPDATE P_CLIENTE SET coorx=" + px + ",coory=" + py + " WHERE CODIGO='" + cod + "'";
			db.execSQL(sql);
		/*} catch (SQLException e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox(e.getMessage());
		}

		try {*/
			ins.init("D_CLICOORD");

			ins.add("CODIGO", cod);
			ins.add("STAMP", du.getCorelBase());
			ins.add("COORX", px);
			ins.add("COORY", py);
			ins.add("STATCOM", "N");

			db.execSQL(ins.sql());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), sql);
			msgbox(e.getMessage());
		}

		super.finish();
	}


	// Location

	private void lastKnowPos() {

		idle = false;
		pbar.setVisibility(View.VISIBLE);

		latitude = 0;
		longitude = 0;

		try {
			getLocation();
		/*} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


		try {*/
			idle = true;
			pbar.setVisibility(View.INVISIBLE);

			lblPos.setText(latitude + " , " + longitude);

			if (latitude + longitude != 0) {
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//showStaticMap();
					}
				}, 500);
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
			latitude = 0;
			longitude = 0;
		}

	}

	public Location getLocation() {

		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGPSEnabled) {
				lblGPS.setText("GPS Activo");
			} else {
				toastcent("¡GPS Deshabilitado!");
				lblGPS.setText("GPS Deshabilitado");
			}

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

	private void askApply() {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿Borrar la posición?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					updateItem(0,0);
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

}
