package com.dts.roadp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ComWS extends PBase {
//Esto es de Kelvyn
	private TextView lblInfo, lblParam, lblRec, lblEnv, lblEnvM;
	private ProgressBar barInfo;
	private EditText txtRuta, txtWS, txtEmp;
	private ImageView imgRec, imgEnv, imgEnvM;
	private RelativeLayout ralBack;
	private RelativeLayout relExist, relPedidos;
	private RelativeLayout relPrecio;
	private RelativeLayout relStock, relClientes;
	private TextView lblUser, lblPassword, txtVersion;
	private EditText txtUser, txtPassword;
	private CheckBox cbSuper;

	private int isbusy, fecha, lin, reccnt, ultcor, ultcor_ant, licResult, licResultRuta, iRutaSupervisor;
	private String err, ruta, rutatipo, sp, docstock, ultSerie, ultSerie_ant, rrs,sRutaSupervisor,rrss;
	private String licSerial, licRuta, licSerialEnc, licRutaEnc, parImprID;
	private boolean fFlag, showprogress, pendientes, envioparcial, findiaactivo, errflag,
            esEnvioManual = false,pedidos, cargastockpv,errorFlag;
	private String clienteImg= "", rutaImg ="", fechaImg="", statcomImg ="", path="";

	private SQLiteDatabase dbT;
	private DatabaseErrorHandler er;
	private BaseDatos ConT;
	private BaseDatos.Insert insT;
	private AppMethods clsAppM;

	private ArrayList<String> listItems = new ArrayList<>();
	private ArrayList<String> results = new ArrayList<>();
	private ArrayList<String> listDocs = new ArrayList<>();

	private ArrayList<clsClasses.clsEnvio> items = new ArrayList<clsClasses.clsEnvio>();
	private ListAdaptEnvio adapter;

	private clsDataBuilder dbld;
	private clsLicence lic;
	private clsFinDia claseFindia;
	private DateUtils DU;
	private String jsonWS, updCxC;
	private CryptUtil cu = new CryptUtil();

	protected PowerManager.WakeLock wakeLock;

	// Web Service -

	public AsyncCallRec wsRtask;
	public AsyncCallSend wsStask;
	public AsyncCallConfirm wsCtask;

	private static String sstr, fstr, fprog, finf, ferr, fterr, idbg, dbg, ftmsg, esql, ffpos;
	private int scon, running, pflag, stockflag, conflag;
	private String ftext, slsync, senv, gEmpresa, ActRuta, mac, rootdir;
	private String fsql, fsqli, fsqlf, strliqid,argstr,xmlresult, hEnvio;
	private boolean rutapos, ftflag, esvacio, liqid, cargasuper, autoenvio, resultado;

	private final String NAMESPACE = "http://tempuri.org/";
	private String METHOD_NAME, URL, URL_Remota;

	protected PowerManager.WakeLock wakelock;

	private HttpTransportSE transport;
	private XMLObject xobj;

	private boolean vDBVacia= false;

	//Web service adicional

	private String nombretabla;
	private int indicetabla,modo_recepcion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_ws);

		super.InitBase();
		//addlog("ComWS", "" + du.getActDateTime(), gl.vend);

		System.setProperty("line.separator", "\r\n");
		rootdir = Environment.getExternalStorageDirectory() + "/RoadFotos/";

		dbld = new clsDataBuilder(this);
		claseFindia = new clsFinDia(this);
		clsAppM = new AppMethods(this, gl, Con, db);

		lblInfo = (TextView) findViewById(R.id.lblETipo);
		lblParam = (TextView) findViewById(R.id.lblProd);
		txtVersion = (TextView) findViewById(R.id.txtVersion);
		barInfo = (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta = (EditText) findViewById(R.id.txtRuta);
		txtRuta.setEnabled(false);
		txtWS = (EditText) findViewById(R.id.txtWS);
		txtWS.setEnabled(false);
		txtEmp = (EditText) findViewById(R.id.txtEmp);
		txtEmp.setEnabled(false);

		lblRec = (TextView) findViewById(R.id.btnRec);
		lblEnv = (TextView) findViewById(R.id.btnSend);
		lblEnvM = (TextView) findViewById(R.id.btnSenHand);

		imgEnv = (ImageView) findViewById(R.id.imageView6);
		imgEnvM = (ImageView) findViewById(R.id.imageView23);
		imgRec = (ImageView) findViewById(R.id.imageView5);

		relPedidos = (RelativeLayout) findViewById(R.id.relPedidos);

		ralBack = (RelativeLayout) findViewById(R.id.relwsmail);
		relExist = (RelativeLayout) findViewById(R.id.relExist);
		relPrecio = (RelativeLayout) findViewById(R.id.relPrecio);
		relStock = (RelativeLayout) findViewById(R.id.relStock);
		RelativeLayout relPedidos = (RelativeLayout) findViewById(R.id.relPedidos);
		relClientes = (RelativeLayout) findViewById(R.id.relClientes);

		relPedidos.setVisibility(View.INVISIBLE);

		cbSuper = (CheckBox) findViewById(R.id.checkBox8);

		isbusy = 0;

		lblInfo.setText("");
		lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);

		lblUser = new TextView(this, null);
		lblPassword = new TextView(this, null);
		//txtVersion=new TextView(this, null);

		txtUser = new EditText(this, null);
		txtPassword = new EditText(this, null);

		txtVersion.setText(gl.parNumVer + gl.parFechaVer);

		if (gl.ruta.isEmpty()) {
			ruta = txtRuta.getText().toString();
			gl.ruta = ruta;
		} else {
			ruta = gl.ruta;
		}

		ActRuta = ruta;
		gEmpresa = gl.emp;
		rutatipo = gl.rutatipog;
		rutapos = gl.rutapos;
		vDBVacia = dbVacia();

		if (gl.tipo == 0) {
			this.setTitle("Comunicación");
		} else {
			this.setTitle("Comunicación Local");
		}

		licSerial = gl.deviceId;
		licRuta = ruta;

		try {
			licSerialEnc = cu.encrypt(licSerial);
			licRutaEnc = cu.encrypt(licRuta);
		} catch (Exception e) {
			licSerialEnc = "";
			licRutaEnc = "";
		}

		gl.isOnWifi = clsAppM.isOnWifi();

		getWSURL();

		xobj = new XMLObject();

		//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
		if (gl.debug) {
			if (mu.emptystr(txtRuta.getText().toString())) {
				txtRuta.setText("M102-1");
				//txtRuta.setText("6055-5");
				//txtRuta.setText("0050-1");
				//txtRuta.setText("8001-1");
				txtEmp.setText("03");
			}

			//txtWS.setText("http://200.46.46.104:8001/RDC7_SAP_PRD_ANDR/wsAndr.asmx");
            //txtWS.setText("http://200.46.46.104:8001/RDC7_SAP_QAS_ANDR/wsAndr.asmx");
			txtWS.setText("http://movil.toledano.com/RDC7_SAP_QAS_ANDR/wsAndr.asmx");

            //txtRuta.setText("8001-1");
			//txtEmp.setText("03");
			//txtWS.setText("http://192.168.1.137/wsAndr/wsandr.asmx");
		}

		//txtRuta.setText("8001-1");
		//txtEmp.setText("03");
		//txtWS.setText("http://192.168.1.10/wsAndr/wsandr.asmx");

		mac = getMac();
		fsql = du.univfechasql(du.getActDate());
		fsqli = du.univfechasql(du.ffecha00(du.getActDate())) + " 00:00:00";
		fsqlf = du.univfechasql(du.ffecha24(du.getActDate())) + " 23:59:59";

		parImprID = clsAppM.getPrintId();

		lic = new clsLicence(this);

		pendientes = validaPendientes();

		envioparcial = gl.peEnvioParcial;

		gl.enviaPedidosParcial = false;
		gl.enviaClientes = false;

		if (!gl.enviaMov){
			visibilidadBotones();
		}

		//if (gl.autocom==1) runSend();

		//relExist.setVisibility(View.VISIBLE);

		if (esvacio) txtWS.setEnabled(true);

		setHandlers();

		if (gl.modoadmin) {
			relPedidos.setVisibility(View.VISIBLE);
		} else {
			if (gl.tolsuper) relPedidos.setVisibility(View.VISIBLE);
		}
		if (!autoenvio && (rutatipo.equals("P") || rutatipo.equals("T"))) {
            relPedidos.setVisibility(View.VISIBLE);
        }

		if (gl.ruta.isEmpty()) {
		    relPedidos.setVisibility(View.INVISIBLE);
		    relClientes.setVisibility(View.INVISIBLE);
        }

        pedidos=rutatipo.equals("P");

		try {
			final PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
			this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:mywakelocktag");
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "wakeLock");
		}

        actualizaEstadoPedidos();

        if (gl.enviaMov){
			lblRec.setVisibility(View.INVISIBLE);
			imgRec.setVisibility(View.INVISIBLE);
			lblEnv.setVisibility(View.INVISIBLE);
			imgEnv.setVisibility(View.INVISIBLE);
			lblEnvM.setVisibility(View.INVISIBLE);
			imgEnvM.setVisibility(View.INVISIBLE);
			relPrecio.setVisibility(View.INVISIBLE);
			relExist.setVisibility(View.INVISIBLE);
			relStock.setVisibility(View.INVISIBLE);

			runSend();

		}
	}

	private boolean dbVacia() {
		Cursor dt;

		try {
			sql = "SELECT CODIGO,ENVIO_AUTO_PEDIDOS FROM P_RUTA";
			dt = Con.OpenDT(sql);

			if (dt.getCount()==0) {
                autoenvio = dt.getInt(1)==1;
            } else autoenvio=false;

			return dt.getCount() == 0;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			return true;
		}
	}

	//region Events

	public void askRec(View view) {

		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

        modo_recepcion=1;

		lblRec.setVisibility(View.INVISIBLE);
		imgRec.setVisibility(View.INVISIBLE);

		if (gl.ruta.isEmpty()) {
			ruta = txtRuta.getText().toString();
			gl.ruta = ruta;
		} else {
			ruta = gl.ruta;
		}

		licSerial = gl.deviceId;
		licRuta = ruta;

		try {
			licSerialEnc = cu.encrypt(licSerial);
			licRutaEnc = cu.encrypt(licRuta);
		} catch (Exception e) {
			licSerialEnc = "";
			licRutaEnc = "";
		}

		//CKFK 20190222 Se agregó esta validación para no sobreescribir los datos si ya se importaron
		if (!gl.modoadmin) {

			if (gl.banderafindia) {

				long fechaUltimoCierre;

				fechaUltimoCierre = claseFindia.ultimoCierreFecha();

				if ((du.getActDate() == fechaUltimoCierre) && ExistenDatos()) {
					//claseFindia.
					claseFindia.eliminarTablasD();
					claseFindia.eliminarTablasD();
				}

			}

			if (ExistenDatos()) {
				BorraDatosAnteriores("¿Tiene facturas, pedidos, cobros, devoluciones o inventario, está seguro de borrar los datos?");
			} else {
				msgAskConfirmaRecibido();
			}
		} else {
			claseFindia.eliminarTablasD();
			msgAskConfirmaRecibido();
		}

	}

	public void askSend(View view) {
		try {

			gl.enviaPedidosParcial = false;
			gl.enviaMov = false;
			gl.enviaClientes = false;

			if (isbusy == 1) {
				toastcent("Por favor, espere que se termine la tarea actual.");return;
			}

			lblEnv.setVisibility(View.INVISIBLE);
			imgEnv.setVisibility(View.INVISIBLE);

			if (!gl.debug) {
				if (!validaLicencia()) {
					mu.msgbox("Licencia inválida!");
					lblEnv.setVisibility(View.VISIBLE);
					imgEnv.setVisibility(View.VISIBLE);
					return;
				}
			}

			if (gl.banderafindia) {
				if (!puedeComunicar()) {
					mu.msgbox("No ha hecho fin de día, no puede comunicar datos");
					lblEnv.setVisibility(View.VISIBLE);
					imgEnv.setVisibility(View.VISIBLE);
					return;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Enviar datos?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runSend();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					lblEnv.setVisibility(View.VISIBLE);
					imgEnv.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	public void askSendPedidos(View view) {
		try {

			if (isbusy == 1) {
				toastcent("Por favor, espere que se termine la tarea actual.");return;
			}

			relPedidos.setVisibility(View.INVISIBLE);

			if (!gl.debug) {
				if (!validaLicencia()) {
					mu.msgbox("Licencia inválida!");
					relPedidos.setVisibility(View.VISIBLE);
					return;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envío Pedidos");
			dialog.setMessage("¿Enviar Pedidos?");
			dialog.setCancelable(false);

			gl.enviaPedidosParcial = true;

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runSend();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					relPedidos.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	public void askSendClientes(View view) {
		try {

			if (isbusy == 1) {
				toastcent("Por favor, espere que se termine la tarea actual.");return;
			}

			relClientes.setVisibility(View.INVISIBLE);

			if (!gl.debug) {
				if (!validaLicencia()) {
					mu.msgbox("Licencia inválida!");
					relClientes.setVisibility(View.VISIBLE);
					return;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envío Clientes");
			dialog.setMessage("¿Enviar Clientes Nuevos?");
			dialog.setCancelable(false);

			gl.enviaClientes = true;

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runSend();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					relClientes.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	public void askSendManual(View view) {

		try {
			if (isbusy == 1) {
				toastcent("Por favor, espere que se termine la tarea actual.");
				return;
			}

			lblEnvM.setVisibility(View.INVISIBLE);
			imgEnvM.setVisibility(View.INVISIBLE);

			if (!gl.debug) {
				if (!validaLicencia()) {
					mu.msgbox("Licencia inválida!");
					lblEnvM.setVisibility(View.VISIBLE);
					imgEnvM.setVisibility(View.VISIBLE);
					return;
				}
			}

			if (gl.banderafindia) {
				if (!puedeComunicar()) {
					mu.msgbox("No ha hecho fin de dia, no puede comunicar datos");
					lblEnvM.setVisibility(View.VISIBLE);
					imgEnvM.setVisibility(View.VISIBLE);
					return;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Va a realizar la COMUNICACIÓN MANUAL, está seguro?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					DatosSupervisor();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					lblEnvM.setVisibility(View.VISIBLE);
					imgEnvM.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	public void askSendContinue() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("La COMUNICACIÓN MANUAL no envía los datos directamente a liquidación,¿está seguro?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (sendDataManual()) {
						askOk();
					}
				}
			});

			dialog.setNegativeButton("No", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	public void askSendCorrect() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Comunicación correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					askOkCom();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					askIncorrect();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void askIncorrect() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Está seguro de que la comunicacion NO fue correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Eliminatablas();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void askOk() {

		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Envio");//	alert.setMessage("Serial");

			final TextView input = new TextView(this);
			alert.setView(input);

			input.setText("Archivo de datos creado conecte el dispotivo al ordenador");
			input.requestFocus();

			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					askSendCorrect();
				}
			});

			alert.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void askOkCom() {

		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Envio");//	alert.setMessage("Serial");

			final TextView input = new TextView(this);
			alert.setView(input);

			input.setText("Está seguro de que la comunicación fue correcta");
			input.requestFocus();

			alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Eliminatablas();
				}
			});

			alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					return;
				}
			});

			alert.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void DatosSupervisor() {

		try {

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Envío");

			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if (lblUser.getParent() != null) {
				((ViewGroup) lblUser.getParent()).removeView(lblUser);
			}

			if (lblPassword.getParent() != null) {
				((ViewGroup) lblPassword.getParent()).removeView(lblPassword);
			}

			if (txtUser.getParent() != null) {
				((ViewGroup) txtUser.getParent()).removeView(txtUser);
			}

			if (txtPassword.getParent() != null) {
				((ViewGroup) txtPassword.getParent()).removeView(txtPassword);
			}

			lblUser.setText("Usuario: ");
			lblPassword.setText("Contraseña: ");
			txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

			txtUser.setText("");
			txtPassword.setText("");

			layout.addView(lblUser);
			layout.addView(txtUser);
			layout.addView(lblPassword);
			layout.addView(txtPassword);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					String usr, pwd;
					boolean dtCorrectos;

					usr = txtUser.getText().toString().trim();
					pwd = txtPassword.getText().toString().trim();

					if (mu.emptystr(usr)) {
						toast("Usuario incorrecto.");
						lblEnvM.setVisibility(View.VISIBLE);
						imgEnvM.setVisibility(View.VISIBLE);
						return;
					}

					if (mu.emptystr(pwd)) {
						toast("Contraseña incorrecta.");
						lblEnvM.setVisibility(View.VISIBLE);
						imgEnvM.setVisibility(View.VISIBLE);
						return;
					}

					dtCorrectos = validaDatos(usr, pwd);

					lblEnvM.setVisibility(View.VISIBLE);
					imgEnvM.setVisibility(View.VISIBLE);

					if (dtCorrectos) {
						askSendContinue();
					} else {
						layout.removeAllViews();

						return;
					}

				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					layout.removeAllViews();
					lblEnvM.setVisibility(View.VISIBLE);
					imgEnvM.setVisibility(View.VISIBLE);
				}
			});

			alert.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private boolean validaDatos(String user, String psw) {

		Cursor DT;
		boolean correctos = false;
		String dpsw;
		try {

			if (gl.tolsuper) {
				sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + user + "' AND NIVEL=2";
			} else {
				sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + user + "' AND NIVEL=1";
			}
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				mu.msgbox("Usuario incorrecto !");
				return false;
			}

			DT.moveToFirst();
			dpsw = DT.getString(1);
			if (!psw.equalsIgnoreCase(dpsw)) {
				mu.msgbox("Contraseña incorrecta !");
				return false;
			}

			if (DT != null) DT.close();

			correctos = true;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			return false;
		}

		return correctos;

	}

	private boolean sendDataManual() {

		errflag = false;

		senv = "Envío terminado \n \n";

		items.clear();
		dbld.clearlog();

		try {

			esEnvioManual = true;

			envioFacturas();

			envioCanastas();

			envioClienteModificados();

			envioPedidos();

			envioNotasCredito();

			envioNotasDevolucion();

			envioCobros();

			envioDepositos();

			envio_D_MOV();

			envioCli();

			envioAtten();

			envioCoord();

			envioSolicitud();

			envioRating();

			updateAcumulados();

			updateInventario();

			update_Corel_GrandTotal();

			envioFinDia();

			dbld.saveArchivo(du.getActDateStr());

			esEnvioManual = false;

			errflag = true;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			esEnvioManual = false;
		}

		return errflag;
	}

	private boolean generaArchivoBck() {

		errflag = false;

		items.clear();
		dbld.clearlog();

		try {

			//esEnvioManual = true;

			envioFacturas();

			envioCanastas();

			envioClienteModificados();

			envioPedidos();

			envioNotasCredito();

			envioNotasDevolucion();

			envioCobros();

			envioDepositos();

			envio_D_MOV();

			envioCli();

			envioAtten();

			envioCoord();

			envioSolicitud();

			envioRating();

			updateAcumulados();

			updateInventario();

			update_Corel_GrandTotal();

			envioFinDia();

			dbld.saveArchivo_bck(du.getActDateStr());

			//errflag = true;


		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			esEnvioManual = false;
		}

		return errflag;
	}

	private void Eliminatablas() {
		boolean Eliminadas = false;
		try {

			claseFindia.updateFinDia(du.getActDate());
			claseFindia.updateComunicacion(2);

			ActualizaStatcom();
			Eliminadas = claseFindia.eliminarTablasD();

			if (Eliminadas) {
				mu.msgbox("Envío de datos correcto");
			}

			visibilidadBotones();

		} catch (Exception e) {

		}
	}

	private boolean puedeComunicar() {

		boolean vPuedeCom = false;

		try {

			//#CKFK 20190304 Agregué validación para verificar si ya se realizó la comunicación de los datos.
			if (gl.banderafindia) {

				return ((claseFindia.getImprimioCierreZ() == 7));

			} else {
				return true;
			}

		} catch (Exception ex) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ex.getMessage(), "");
		}

		return vPuedeCom;
	}

	public void askExist(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			relExist.setVisibility(View.INVISIBLE);

			dialog.setTitle("Existencias bodega");
			dialog.setMessage("¿Actualizar existencias?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runExist();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					relExist.setVisibility(View.VISIBLE);
				}
			});


			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

	}

	public void askPrecios(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			relPrecio.setVisibility(View.INVISIBLE);

			dialog.setTitle("Precios");
			dialog.setMessage("¿Actualizar precios?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runPrecios();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					relPrecio.setVisibility(View.VISIBLE);
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

	}

	public void askRecarga(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			relStock.setVisibility(View.INVISIBLE);

			dialog.setTitle("Recarga de inventario");
			dialog.setMessage("¿Recargar inventario?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					long vfecha = clsAppM.fechaFactTol(du.getActDate());
					long aFecha = du.getActDate();
					String fechav = du.sfecha(vfecha);
					String fechaa = du.sfecha(aFecha);

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

					Date strDate = null;
					Date strDatea =null;
					try {
						strDate = sdf.parse(fechav);
						strDatea = sdf.parse(fechaa);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (strDatea.getTime() == strDate.getTime()) {
						runRecarga();
					}else{
						msgbox("Las recargas de inventario deben corresponnder al mismo día de la carga inicial del inventario");
					}
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					relStock.setVisibility(View.VISIBLE);
				}
			});


			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere hasta que se termine la tarea actual.");
			return;
		}

	}

	public void doFotos(View view) {
		startActivity(new Intent(this, ComWSFotos.class));
	}

	//#CKFK 20211114 Cambié esto por la función askSendPedidos
/*	public void doPedidos(View view) {
        //startActivity(new Intent(this, ComWSSend.class));
	}*/

	private void setHandlers() {
		ralBack.setOnTouchListener(new SwipeListener(this) {
			public void onSwipeRight() {
				onBackPressed();
			}

			public void onSwipeLeft() {
			}
		});
	}

	//endregion

	//region Main

	// AT 20211019
	private boolean tieneCatalogo() {
		Cursor DT;
		boolean TieneRuta = false;
		boolean TieneClientes = false;
		boolean TieneProd = false;

		try {
			String ntablas[] = {"P_RUTA", "P_CLIENTE", "P_PRODUCTO"};

			for (int i = 0; i < ntablas.length; i++) {
				sql ="SELECT CODIGO FROM "+ntablas[i];
				DT=Con.OpenDT(sql);

                if (DT.getCount() > 0) {
                    if (ntablas[i].equals("P_RUTA")){
                        TieneRuta = true;
                    } else if (ntablas[i].equals("P_CLIENTE")) {
                        TieneClientes = true;
                    } else if (ntablas[i].equals("P_PRODUCTO")) {
                        TieneProd = true;
                    }
                }
			}

			if (TieneRuta && TieneClientes && TieneProd) {
				return true;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}

		return false;
	}

    // JP20211018
    private void runRecep() {
		modo_recepcion=1;
		runRecepion();
    }

    // JP20211018
	private void runExist() {
		if (tieneCatalogo()) {
			modo_recepcion=2;
			runRecepion();
		} else {
			msgbox("No tiene datos de la ruta, clientes y productos, debe hacer una carga de datos completa");
		}
	}

	// AT 20211019
	private void runPrecios() {
		if(tieneCatalogo()) {
			modo_recepcion = 3;
			runRecepion();
		} else {
			msgbox("No tiene datos de la ruta, clientes y productos, debe hacer una carga de datos completa");
		}
	}

	// AT 20211019
	private void runRecarga() {
		if(tieneCatalogo()) {
			modo_recepcion = 4;
			runRecepion();
		} else {
			msgbox("No tiene datos de la ruta, clientes y productos, debe hacer una carga de datos completa");
		}
	}

    private void runRecepion() {
        try {
            if (isbusy == 1) return;

            if (!setComParams()) return;

            try{
                //#CKFK 20190313 Agregué esto para ocultar el teclado durante la carga de los datos
                View view = this.getCurrentFocus();
                if (view != null) {
                    view.clearFocus();
                    keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }catch (Exception e){ }

            isbusy = 1;

            if (!esvacio) {
                ultcor_ant = ultCorel();
                ultSerie_ant = ultSerie();
            }

            barInfo.setVisibility(View.VISIBLE);
            barInfo.invalidate();
            lblInfo.setText("Iniciando proceso de carga..");

            lblInfo.setText("Conectando ...");

            wsRtask = new AsyncCallRec();
            wsRtask.execute();

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private void runSend() {
        long dia = du.dayofweek(du.getActDate());

        try {
            if (isbusy == 1) return;

            if (!setComParams()) return;

            try {
                File f1 = new File(Environment.getExternalStorageDirectory() + "/road.db");
                File f2 = new File(Environment.getExternalStorageDirectory() + "/road" + dia + ".db");
                FileUtils.copyFile(f1, f2);
            } catch (Exception e) {
                msgbox("No se puede generar respaldo : " + e.getMessage());
            }

            isbusy = 1;

            barInfo.setVisibility(View.VISIBLE);
            barInfo.invalidate();
            lblInfo.setText("Conectando ...");

            showprogress = true;
            wsStask = new AsyncCallSend();
            wsStask.execute();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    public void writeData(View view) {

		try {
			dbld.clear();
			dbld.insert("D_PEDIDO", "WHERE 1=1");
			dbld.insert("D_PEDIDOD", "WHERE 1=1");
			dbld.save();
			dbld.saveArchivo(du.getActDateStr());
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}


	}

	private boolean validaPendientes() {
		int pend = 0;

		sp = "";

		try {

			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_FACTURA WHERE STATCOM<>'S'", "Fact: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_PEDIDO WHERE (STATCOM='N') AND ((ANULADO='S') OR (CUMPLE_MONTO_MINIMO=1))", "Ped: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_COBRO WHERE STATCOM<>'S'", "Rec: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_DEPOS WHERE STATCOM<>'S'", "Dep: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_MOV WHERE STATCOM<>'S'", "Inv : ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(IDCANASTA),0) FROM D_CANASTA WHERE STATCOM<>'S'", "Can : ");

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return pend > 0;
	}

	//endregion

    //region Modos de recepcion

    private void cargaTodasTablas() {
        try {
            listItems.clear();
            indicetabla=-5;nombretabla="P_PARAMEXT";
            executaTabla();
        } catch (Exception e) {
            String ss=e.getMessage();
            visibilidadBotones();
        }
    }

    // JP20211018
    private void cargaTablasExist() {
        try {
            listItems.clear();
            indicetabla=0; nombretabla="P_STOCKINV";
            executaTabla();
        } catch (Exception e) {
            String ss=e.getMessage();
            visibilidadBotones();
        }
    }

    // AT20211019
	private void cargaTablasPrecio() {
		try	{
			listItems.clear();
			indicetabla=0; nombretabla="TMP_PRECESPEC";
			executaTabla();
		} catch (Exception e) {
			String ss = e.getMessage();
			visibilidadBotones();
		}

	}

	// AT20211019
	private void cargaTablasRec() {
		try	{
			listItems.clear();
			indicetabla=2; nombretabla="TMP_PRECESPEC";
			executaTabla();
		} catch (Exception e) {
			String ss = e.getMessage();
			visibilidadBotones();
		}
	}
    //endregion

	//region Web Service Methods

	public int fillTable(String value, String delcmd) {
		int rc;
		String s, ss, tabla;
		int retFillTable = 0;

		METHOD_NAME = "getIns";
		sstr = "OK";

		try {
			idbg = idbg + " filltable ";

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(value);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;
			idbg = idbg + " rec " + rc + "  ";

			s = "";
			if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
				if (rc == 1) stockflag=0; else stockflag=1;
			}

            if (cargastockpv) {
                if (rc == 1) {
                    stockflag=0;
                } else {
                    stockflag=1;
                }
            }

			tabla = delcmd.substring(12);
			switch (tabla) {

				case "P_RUTA":
					if (rc == 1) {
						borraDatos();
						throw new Exception("La ruta ingresada no es válida, ruta: " + ruta + ", no se puede continuar la carga de datos");
					}
					break;

				case "P_CLIENTE":
					if (!cargasuper) {
						if (rc == 1) {
							borraDatos();
							throw new Exception("No hay clientes definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
						}
					}
					break;

				case "P_PRODUCTO":
					if (rc == 1) {
						borraDatos();
						throw new Exception("No hay productos definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
					}
					break;
				case "P_PRODPRECIO":
					if (rc == 1) {
						borraDatos();
						throw new Exception("No hay precios definidos para los productos de esta ruta:" + ruta + ", no se puede continuar la carga de datos");
					}
					break;
				case "P_COREL":
					if (rc == 1 && gl.rutatipo.equals("V")) {
						borraDatos();
                        if (!pedidos)  throw new Exception("No hay correlativos definidos para esta ruta:" + ActRuta + ", no se puede continuar la carga de datos");
					}
					break;
				case "CANTDOCPEND":
					if (rc == 2 && gl.rutatipo.equals("V")) {
						throw new Exception("Tiene datos pendientes de subir al BOF, va a tener que facturar manual el día de hoy o subir los datos pendientes");
					}else{
						throw new Exception("No hay documentos pendientes");
					}
					//break;
			}

			for (int i = 0; i < rc; i++) {
				String str = "";
				try {
					str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);
					//s=s+str+"\n";
				} catch (Exception e) {
					mu.msgbox("error: " + e.getMessage());
				}

				if (i == 0) {

					idbg = idbg + " ret " + str + "  ";

					if (str.equalsIgnoreCase("#")) {
						listItems.add(delcmd);
					} else {
						idbg = idbg + str;
						ftmsg = ftmsg + "\n" + str;
						ftflag = true;
						sstr = str;
						return 0;
					}
				} else {
					try {
						sql = str;
						listItems.add(sql);
						sstr = str;
					} catch (Exception e) {
						addlog(new Object() {
						}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
						sstr = e.getMessage();
					}
				}
			}

			retFillTable = 1;

		} catch (Exception e) {

			//#EJC20190226: Evitar que se muestre OK después del nombre de la tabla cuando da error de timeOut.
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			retFillTable = 0;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, value);

		}

		return retFillTable;
	}

	public int fillTableImpresora() {

		int rc;
		String s, ss, delcmd = "DELETE FROM P_IMPRESORA";

		METHOD_NAME = "getInsImpresora";

		sstr = "OK";

		try {

			idbg = idbg + " filltableImpresora ";

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			/*
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(value);
			request.addProperty(param);
			*/
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;
			idbg = idbg + " rec " + rc + "  ";

			s = "";

			for (int i = 0; i < rc; i++) {
				String str = "";
				try {
					str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);
					//s=s+str+"\n";
				} catch (Exception e) {
					mu.msgbox("error: " + e.getMessage());
				}

				if (i == 0) {

					idbg = idbg + " ret " + str + "  ";

					if (str.equalsIgnoreCase("#")) {
						listItems.add(delcmd);
					} else {
						idbg = idbg + str;
						ftmsg = ftmsg + "\n" + str;
						ftflag = true;
						sstr = str;
						return 0;
					}
				} else {
					try {
						sql = str;
						listItems.add(sql);
						sstr = str;
					} catch (Exception e) {
						addlog(new Object() {
						}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
						sstr = e.getMessage();
					}
				}
			}

			return 1;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			return 0;
		}
	}

	public int fillTable2(String value, String delcmd) {
		int rc,retFillTable = 0;
		String str, ss, tabla;
		String[] sitems;

		String xr;

		try {
			sstr = "OK";

			if (nombretabla.contains("P_IMPRESORA")) {

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("getInsImpresora");
				xr = getXMLRegionSingle("getInsImpresoraResult");
			}else if (nombretabla.contains("CARGA_SUPERVISOR")){

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("RutaSupervisor");
				xr = getXMLRegionSingle("RutaSupervisorResult");
				sRutaSupervisor = (String) getSingle(xr,"checkLicenceResult",String.class);
				iRutaSupervisor =  0;
				if (sRutaSupervisor.equalsIgnoreCase("S")) {
					iRutaSupervisor = 1;
				} else if (sRutaSupervisor.equalsIgnoreCase("N")) {
					iRutaSupervisor= 0;
				}
				cargasuper = iRutaSupervisor == 1;
			}else if (nombretabla.contains("checkLicenceRuta")){

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("checkLicenceRuta", "Ruta",ruta);
				xr=getXMLRegionSingle("checkLicenceRutaResult");
				licResultRuta=(Integer) getSingle(xr,"checkLicenceRutaResult",Integer.class);

			}else if (nombretabla.contains("checkLicence")){

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("checkLicence","Serial",licSerial,"Name", gl.devicename, "Ruta",ruta);

				xr=getXMLRegionSingle("checkLicenceResult");
				licResult=(Integer) getSingle(xr,"checkLicenceResult",Integer.class);

			}else if (nombretabla.contains("commitSQL")){

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("Commit", "SQL", value);
				xr=getXMLRegionSingle("CommitResult");
				xr=(String) getSingle(xr,"CommitResult",String.class);

			} else if (nombretabla.contains("CommitEnvio")) {

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				String fecha = du.getActDateStr();

				callMethod("CommitEnvio", "SQL", value, "pHash", hEnvio, "pFechaHH", fecha, "pRuta", gl.ruta, "pVendedor", gl.vend);
				xr = getXMLRegionSingle("CommitEnvioResult");
				xr = (String) getSingle(xr, "CommitEnvioResult", String.class);

			} else {

				value=value.replace("&", "&amp;");
				value=value.replace("\"", "&quot;");
				value=value.replace("'", "&apos;");
				value=value.replace("<", "&lt;");
				value=value.replace(">", "&gt;");

				callMethod("getIns", "SQL", value);
				xr=getXMLRegionSingle("getInsResult");
			}

			sitems=xr.split("\n");
			rc=sitems.length;

			s = "";

			if (pedidos) {
                if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK_PV")) {
                    if (rc == 1) {
                        stockflag = 0;
                    } else {
                        stockflag = 1;
                    }
                }
            } else {
                if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK") || delcmd.equalsIgnoreCase("DELETE FROM P_STOCKB")) {
                    if (rc == 1) {
                        stockflag = 0;
                    } else {
                        stockflag = 1;
                    }
                }
            }

			if (!delcmd.equals("commitSQL") && !delcmd.contains("CommitEnvio")){
				tabla=delcmd.substring(12);
				switch (tabla){

					case "P_RUTA":
						if (rc==1){
							borraDatos();
							throw new Exception("La ruta ingresada no es válida, ruta: " + ruta + ", no se puede continuar la carga de datos");
						}
						break;

					case "P_CLIENTE":
						if (!cargasuper) {
							if (rc == 1) {
								borraDatos();
								throw new Exception("No hay clientes definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
							}
						}
						break;

					case "P_PRODUCTO":
						if (rc==1){
							borraDatos();
							throw new Exception("No hay productos definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
						}
						break;
					case "P_PRODPRECIO":
						if (rc==1){
							borraDatos();
							throw new Exception("No hay precios definidos para los productos de esta ruta:" + ruta + ", no se puede continuar la carga de datos");
						}
						break;
					case "P_COREL":
						if (rc==1 && gl.rutatipo.equals("V")){
							borraDatos();
							if (!pedidos) throw new Exception("No hay correlativos definidos para esta ruta:" + ruta + ", no se puede continuar la carga de datos");
						}
						break;
				}

				for (int i=1; i < rc-2; i++) {

					try {
						ss=sitems[i];
						ss=ss.replace("<string>","");
						str=ss.replace("</string>","");
						str=str.replace("&amp;", "&");
						str=str.replace("&quot;", "\"");
						str=str.replace("&apos;", "'");
						str=str.replace("&lt;", "<");
						str=str.replace("&gt;", ">");
					} catch (Exception e) {
						str="";
					}

					if (i == 1) {

						idbg = idbg + " ret " + str + "  ";

						if (str.equalsIgnoreCase("#")) {
							listItems.add(delcmd);
						} else {
							idbg = idbg + str;
							ftmsg = ftmsg + "\n" + str;
							ftflag = true;
							sstr = str;
							return 0;
						}
					} else {
						try {
							sql = str;
							listItems.add(sql);
							sstr = str;
						} catch (Exception e) {
							addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
							sstr = e.getMessage();
                            return 0;
						}
					}
				}
			} else if (delcmd.contains("commitSQL") || delcmd.contains("CommitEnvio") ){
				//Corregir esto
				str=xr;
				if (str.equalsIgnoreCase("#")) {
					listItems.add(delcmd);
				} else {
					idbg = idbg + str;
					ftmsg = ftmsg + "\n" + str;
					ftflag = true;
					sstr = str;
					return 0;
				}
			}

			retFillTable= 1;

		} catch (Exception e) {
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			retFillTable= 0;
		}

		return  retFillTable;
	}

	public String getXMLRegionSingle(String nodename) throws Exception {
		String st,ss,sv,en,sxml;
		Node xmlnode;

		try {

			InputStream istream = new ByteArrayInputStream( xmlresult.getBytes() );
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(istream);

			Element root=doc.getDocumentElement();

			NodeList children=root.getChildNodes();
			Node bodyroot=children.item(0);
			NodeList body=bodyroot.getChildNodes();
			Node responseroot=body.item(0);
			NodeList response=responseroot.getChildNodes();

			ss="";
			for(int i =0;i<response.getLength();i++) {
				ss+=response.item(i).getNodeName()+",\n";

				if (response.item(i).getNodeName().equalsIgnoreCase(nodename)) {
					xmlnode=response.item(i);
					sxml=nodeToString(xmlnode);
					return sxml;
				}
			}
		} catch (Exception e) {
			throw new Exception(" XMLObject getXMLRegion : "+ e.getMessage());
		}
		return "";
	}

	private String nodeToString(Node node)  throws Exception {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (Exception te) {
			throw new Exception("XMLObject nodeToString : "+te.getMessage());
		}
		return sw.toString();
	}

	public Object getSingle( String body, String name, Class<?> cl)  throws Exception {

		int start = body.indexOf("<" + name + ">");
		if (start>-1)  start += name.length() + 2;else start=0;//with <and > char
		int end = body.indexOf("</" + name + ">");
		if (end == -1) body = "";else body = body.substring(start, end);

		String gname = cl.getName();

		if (cl.getName().toLowerCase().contains("string")) {
			return body;
		}
		if (cl.getName().toLowerCase().contains("double")) {
			if (body.isEmpty()) return 0; else return
					Double.parseDouble(body);
		}
		if (cl.getName().toLowerCase().contains("int")) {
			if (body.isEmpty()) return 0; else return
					Integer.parseInt(body);
		}

		if (cl.getName().toLowerCase().contains("boolean")) {
			return Boolean.parseBoolean(body);
		}

		return null;
	}

	public void callMethod(String methodName, Object... args) throws Exception {
		int mTimeOut=5000;
		String mResult,line="";
		URL mUrl = new URL(URL);
		errorFlag=false;

		try{
			mResult = "";xmlresult="";

			URLConnection conn = mUrl.openConnection();
			conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			conn.addRequestProperty("SOAPAction", "http://tempuri.org/" + methodName);

			//#EJC 20200601: Set Timeout
			conn.setConnectTimeout(mTimeOut);
			conn.setReadTimeout(mTimeOut);

			conn.setDoOutput(true);

			OutputStream ostream = conn.getOutputStream();

			OutputStreamWriter wr = new OutputStreamWriter(ostream);

			String body = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
					"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:" +
					"xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:" +
					"soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
					"<soap:Body>" +
					"<" + methodName + " xmlns=\"http://tempuri.org/\">";

			body += buildArgs(args);
			body += "</" + methodName + ">" +
					"</soap:Body>" +
					"</soap:Envelope>";
			wr.write(body);
			wr.flush();

			int responsecode = ((HttpURLConnection) conn).getResponseCode();
			String responsemsg = ((HttpURLConnection) conn).getResponseMessage();

			//#EJC20200702:Capturar excepcion de SQL (No se sabe el error pero sabemos que no se proceso)
			if (responsecode==500) {
				throw new Exception("Error 500: Esto es poco usual pero algún problema ocurrió del lado del motor de BD al ejecutar sentencia SQL: \n" +
						"\n" + args[1].toString());
			}else if (responsecode!=299 && responsecode!=404) {

				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = rd.readLine()) != null) mResult += line;
				rd.close();rd.close();

				mResult=mResult.replace("ñ","n");
				xmlresult=mResult;

			} if (responsecode==299) {

				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = rd.readLine()) != null) mResult += line;
				rd.close();rd.close();

				mResult=mResult.replace("ñ","n");
				xmlresult=mResult;

				throw new Exception("Error al procesar la solicitud :\n " );

			} if (responsecode==404) {
				throw new Exception("Error 404: No se obtuvo acceso a: \n" + mUrl.toURI() +
						"\n" + "Verifique que el WS Existe y es accesible desde el explorador.");
			}

		} catch (Exception e) {
			sstr= "Error al conectarse a " + mUrl.toURI() + " " + e.getMessage();
			errorFlag=true;fterr=e.getMessage();
			throw new Exception(sstr);
		}
	}

	private String buildArgs(Object... args) throws IllegalArgumentException, IllegalAccessException    {
		String result = "";
		String argName = "";
		String valor = "";

		for (int i = 0; i < args.length; i++)   {
			if (i % 2 == 0) {
				argName = args[i].toString();
			} else {
				result += "<" + argName + ">";
				argstr = result;

				result += buildArgValue(args[i]);
				argstr = result;
				result += "</" + argName + ">";
				argstr = result;
			}
		}
		return result;
	}

	private String buildArgValue(Object obj) throws IllegalArgumentException, IllegalAccessException   {

		Class<?> cl = null;

		try  {
			cl = obj.getClass();
		} catch (Exception e) {
			return "";
		}

		String result = "";

		if (cl.isPrimitive()) return obj.toString();
		if (cl.getName().contains("java.lang.")) return obj.toString();
		if (cl.getName().equals("java.util.Date"))  {
			DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			return dfm.format((Date) obj);
		}

		if (cl.isArray())  {
			String xmlName = cl.getName().substring(cl.getName().lastIndexOf(".") + 1);
			xmlName = xmlName.replace(";", "");
			Object[] arr = (Object[]) obj;

			for (int i = 0; i < arr.length; i++) {
				result += "<" + xmlName + ">";
				result += buildArgValue(arr[i]);
				result += "</" + xmlName + ">";
			}

			return result;
		}

		Field[] fields = cl.getDeclaredFields();

		for (int i = 0; i < fields.length - 1; i++) {
			result += "<" + fields[i].getName() + ">";
			result += buildArgValue(fields[i].get(obj));
			result += "</" + fields[i].getName() + ">";
		}

		return result;
	}

	// #AT 20220131 Valida si existe hash
	public boolean validaHash() {
		String xr;
		METHOD_NAME = "Existe_Hash_Envio";
		sstr = "OK";

		try {

			callMethod("Existe_Hash_Envio", "Hash", hEnvio);
			xr = getXMLRegionSingle("Existe_Hash_EnvioResult");
			resultado = (boolean) getSingle(xr, "Existe_Hash_EnvioResult", Boolean.class);

			return resultado;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			sstr = e.getMessage();
		}

		return false;
	}

	public int commitSQL() {
		int rc;
		String s, ss="";
		//#CKFK 20190429 Creé esta variable para retornar si la comunicación fue correcta o no
		//e hice modificaciones en la función para garantizar esta funcionalidad
		int vCommit=0;

		METHOD_NAME = "CommitEnvio";
		sstr = "OK";

		if (dbld.size() == 0) vCommit =1;//return 1

		s = "";
		for (int i = 0; i < dbld.size(); i++) {
			ss = dbld.items.get(i);
			s = s + ss + "\n";
		}
		if (showprogress) {
			fprog = "Enviando ...";
			wsStask.onProgressUpdate();
		}

		s=s.replace("&","&amp;");
		s=s.replace("\"", "&quot;");
		s=s.replace("'","&apos;");
		s=s.replace("<", "&lt;");
		s=s.replace(">", "&gt;");

		nombretabla = "CommitEnvio";

		hEnvio = md5(s);

		//#AT 20220202 Si ya existe el Hash es porque ya se enviaron datos
		//retorna vCommit 1; si no hace el proceso de envió
		if (validaHash()) {
			vCommit = 1;
		} else {
			vCommit=fillTable2(s,"CommitEnvio");
		}

		return vCommit;

	}

    public int commitSQLs() {
        int rc;
        String s, ss;
        int vCommit = 0;

        METHOD_NAME = "Commit";
        sstr = "OK";

        if (dbld.size() == 0) vCommit = 1;//return 1

        s = "";
        for (int i = 0; i < dbld.size(); i++) {
            ss = dbld.items.get(i);
            s = s + ss + "\n";
        }

        if (showprogress) {
            fprog = "Enviando ...";
            wsStask.onProgressUpdate();
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(s);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL, 60000);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) vCommit = 1;// return 1;

            sstr = s;
            //return 0;

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            sstr = e.getMessage();
            vCommit = 0;
        }

        return vCommit;
    }

    public int OpenDTt(String sql) {
		int rc;

		METHOD_NAME = "OpenDT";

		results.clear();

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(sql);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;

			for (int i = 0; i < rc + 1; i++) {
				String str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

				if (i == 0) {
					sstr = str;
					if (!str.equalsIgnoreCase("#")) {
						sstr = str;
						return 0;
					}
				} else {
					results.add(str);
				}
			}

			return 1;
		} catch (Exception e) {
			sstr = e.getMessage();
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return 0;

	}

	public int getTest() {

		METHOD_NAME = "TestWS";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Value");
			param.setValue("OK");

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 3000);

			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject response = null;
			//response = (SoapPrimitive) envelope.getResponse();
			response = (SoapObject) envelope.bodyIn;

			sstr = response.toString() + "..";

			return 1;

		} catch (Exception e) {
			fterr ="Causa: " + e.getCause() + "-Mensaje: " + e.getMessage();
		}

		return 1;
	}

	//#HS_20181219 Funcion para enviar JSON al Web Service.
	public int envioFachada() {
		String METHOD_NAME = "GuardaFachada";
		s = "";

		fprog = "Enviando ...";
		wsStask.onProgressUpdate();

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("JSONFachadas");
			param.setValue(jsonWS);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			sstr = e.getMessage();
		}

		return 0;

	}

	public int checkLicence(String serial) {
		int rc;
		String s, ss;

		METHOD_NAME = "checkLicence";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Serial");
			param.setValue(serial);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();
			if (s.equalsIgnoreCase("1")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			sstr = e.getMessage();
		}

		return 0;
	}

	public int checkLicenceRuta(String ruta) {
		int rc;
		String s, ss;

		METHOD_NAME = "checkLicenceRuta";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Ruta");
			param.setValue(ruta);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();
			if (s.equalsIgnoreCase("1")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			sstr = e.getMessage();
		}

		return 0;
	}

	/*
	public int guardaImagen(String idprod) {
		int rc;
		String s, ss, resstr;

		METHOD_NAME = "getImage";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("idprod");
			param.setValue(idprod);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			resstr = response.toString();

			try {
				//byte[] imgbytes = resstr.getBytes();

				byte[] imgbytes = Base64.decode(resstr, Base64.DEFAULT);

				int bs = imgbytes.length;

				FileOutputStream fos = new FileOutputStream(rootdir + "0006.jpg");
				BufferedOutputStream outputStream = new BufferedOutputStream(fos);
				outputStream.write(imgbytes);
				outputStream.close();

			} catch (Exception ee) {
				sstr = ee.getMessage();
				return 0;
			}

			sstr = "" + resstr.length();
			return resstr.length();
		} catch (Exception e) {
			sstr = e.getMessage();
		}

		return 0;
	}
    */
	public int rutaSupervisor(String ruta) {
		int rc;
		String s, ss;

		METHOD_NAME = "RutaSupervisor";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Ruta");
			param.setValue(ruta);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			s = response.toString();

			if (s.equalsIgnoreCase("S")) {
				return 1;
			} else if (s.equalsIgnoreCase("N")) {
				return 0;
			} else {
				sstr = s;
				return 0;
			}
		} catch (Exception e) {
			sstr = e.getMessage();
			return 0;
		}
	}

	//endregion

	//region WS Recepcion Methods

	private boolean getDataOriginal() {
		Cursor DT;
		BufferedWriter writer = null;
		FileWriter wfile;
		int rc, scomp, prn, jj;
		String s, val = "";

		try {

			if (TieneInventarioSinVentas()) {
				return false;
			}

			String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
			wfile = new FileWriter(fname, false);
			writer = new BufferedWriter(wfile);

			db.execSQL("DELETE FROM P_LIQUIDACION");

			sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				val = DT.getString(0);
			} else {
				val = "N";
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			val = "N";
		}

		if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
		else gl.peStockItf = false;

		listItems.clear();
		scomp = 0;
		idbg = "";
		stockflag = 0;
		ftmsg = "";
		ftflag = false;
		rrs = "...";

		try {

			cargasuper = rutaSupervisor(ActRuta) == 1;

			if (!AddTable("P_PARAMEXT")) return false;
			procesaParamsExt();

			listItems.clear();
			if (!AddTable("P_RUTA")) return false;
			procesaRuta();

			listItems.clear();
			if (!AddTable("P_COREL")) return false;
			if (!AddTable("P_CLIENTE")) return false;
			if (!AddTable("P_PRODUCTO")) return false;
			if (!AddTable("P_PRODPRECIO")) return false;
			if (!AddTable("P_NIVELPRECIO")) return false;
			//if (!AddTable("P_CLIENTE_FACHADA")) return false;
			if (!AddTable("P_CLIRUTA")) return false;
			if (!AddTable("P_CLIDIR")) return false;
			if (!AddTable("P_FACTORCONV")) return false;
			if (!AddTable("P_LINEA")) return false;
			if (!AddTable("TMP_PRECESPEC")) return false;
			if (!AddTable("P_DESCUENTO")) return false;
			if (!AddTable("P_EMPRESA")) return false;
			if (!AddTable("P_SUCURSAL")) return false;
			if (!AddTable("P_BANCO")) return false;
			if (!AddTable("P_STOCKINV")) return false;

			if (!AddTable("P_CODATEN")) return false;
			if (!AddTable("P_CODDEV")) return false;
			if (!AddTable("P_CODNOLEC")) return false;
			if (!AddTable("P_NIVELPRECIO")) return false;
			if (!AddTable("P_CORELNC")) return false;
			if (!AddTable("P_CORRELREC")) return false;
			if (!AddTable("P_CORREL_OTROS")) return false;
			if (!AddTable("P_STOCK_APR")) return false;
			if (!AddTable("P_STOCK")) return false;
			if (!AddTable("P_STOCKB")) return false;
			if (!AddTable("P_STOCK_PALLET"))
				return false;//#CKFK 20190304 10:48 Se agregó esta tabla para poder importar los pallets
			if (!AddTable("P_COBRO")) return false;
			if (!AddTable("P_CLIGRUPO")) return false;
			if (!AddTable("P_MEDIAPAGO")) return false;
			if (!AddTable("P_BONIF")) return false;
			if (!AddTable("P_BONLIST")) return false;
			if (!AddTable("P_PRODGRUP")) return false;
			if (!AddTable("P_IMPUESTO")) return false;
			if (!AddTable("P_VENDEDOR")) return false;
			if (!AddTable("P_MUNI")) return false;
			if (!AddTable("P_VEHICULO")) return false;
			if (!AddTable("P_HANDHELD")) return false;
			if (!AddTable("P_TRANSERROR")) return false;
			if (!AddTable("P_GLOBPARAM")) return false;
			if (!AddTable("P_CONFIGBARRA")) return false;
            if (!AddTable("P_STOCK_PV")) return false;
			if (!AddTable("P_TIPOLOGIA")) return false;

			licResult = checkLicence(licSerial);
			licResultRuta = checkLicenceRuta(licRuta);

			fillTableImpresora();

			if (!AddTable("P_REF1")) return false;
			if (!AddTable("P_REF2")) return false;
			if (!AddTable("P_REF3")) return false;

			if (!AddTable("P_ARCHIVOCONF")) return false;
			if (!AddTable("P_ENCABEZADO_REPORTESHH")) return false;
			if (!AddTable("P_PORCMERMA")) return false;

			// Objetivos

			if (!AddTable("O_RUTA")) return false;
			if (!AddTable("O_COBRO")) return false;
			if (!AddTable("O_PROD")) return false;
			if (!AddTable("O_LINEA")) return false;

			// Mercadeo
			if (!AddTable("P_MEREQTIPO")) return false;
			if (!AddTable("P_MEREQUIPO")) return false;
			if (!AddTable("P_MERESTADO")) return false;
			if (!AddTable("P_MERPREGUNTA")) return false;
			if (!AddTable("P_MERRESP")) return false;
			if (!AddTable("P_MERMARCACOMP")) return false;
			if (!AddTable("P_MERPRODCOMP")) return false;
			if (!AddTable("D_CANASTA")) return false;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, fstr);
			return false;
		}

		ferr = "";

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return true;

			fprog = "Procesando ...";
			wsRtask.onProgressUpdate();

			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			prn = 0;
			jj = 0;
			Log.d("M", "So far so good");

			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);
				esql = sql;
				sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
				sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
				sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

				try {
					writer.write(sql);
					writer.write("\r\n");
				} catch (Exception e) {
					Log.d("M", "Write Something happend there " + e.getMessage());
				}

				try {
					dbT = ConT.getWritableDatabase();
					dbT.execSQL(sql);
				} catch (Exception e) {
					Log.d("M", "Something happend there " + e.getMessage());
					/*addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage() + "EJC", "Yo fui " + sql);*/
					Log.e("z", e.getMessage());
				}

				try {

					if (i % 10 == 0) {
						fprog = "Procesando: " + i + " de: " + (rc - 1);
						wsRtask.onProgressUpdate();
						SystemClock.sleep(20);
					}
				} catch (Exception e) {
					Log.e("z", e.getMessage());
					ferr += " " + e.getMessage();
					/*addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);*/
				}
			}

			fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
			wsRtask.onProgressUpdate();

			Actualiza_FinDia();
			encodePrinters();
			encodeLicence();
			//encodeLicenceRuta();

			SetStatusRecToTrans("1");

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			fprog = "Documento de inventario recibido en BOF...";
			wsRtask.onProgressUpdate();

			//fechaCarga();
			//Actualiza_Documentos();

			fprog = "Fin de actualización";
			wsRtask.onProgressUpdate();

			scomp = 1;

			try {
				ConT.close();
			} catch (Exception e) {
				//addlog(new Object() {	}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				Log.e("Error", e.getMessage());
			}

			try {
				writer.close();
			} catch (Exception e) {
				//addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
				//msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
				Log.e("Error", e.getMessage());
			}

			return true;

		} catch (Exception e) {
			fprog = "Actualización incompleta";
			wsRtask.onProgressUpdate();

			Log.e("Error", e.getMessage());
			try {
				ConT.close();
			} catch (Exception ee) {
				sstr = e.getMessage();
				ferr += " " + sstr;
			}

			sstr = e.getMessage();
			ferr += " " + sstr + "\n" + sql;
			esql = sql;

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ferr, esql);

			return false;

		}

	}

	private boolean getData() {
		Cursor DT;
		BufferedWriter writer = null;
		FileWriter wfile;
		int rc, scomp, prn, jj;
		int ejecutarhh = 0;
		String s, val = "";

		try {

		    if (!pedidos) {
                if (TieneInventarioSinVentas()) return false;
            }

			String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
			wfile = new FileWriter(fname, false);
			writer = new BufferedWriter(wfile);

			db.execSQL("DELETE FROM P_LIQUIDACION");

			sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				val = DT.getString(0);
			} else {
				val = "N";
			}

			DT.close();

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			val = "N";
		}

		if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
		else gl.peStockItf = false;

		listItems.clear();
		scomp = 0;idbg = "";stockflag = 0;ftmsg = "";ftflag = false;rrs="...";

		try {

		    /* temporalmente deshabilitado, cuando va a funcionar carga de todas las tablas, habilitar otra vez
			if (!AddTable("P_PARAMEXT")) return false;
			procesaParamsExt();

			listItems.clear();
			if (!AddTable("P_RUTA")) return false;
			procesaRuta();
            */

			ferr = "";
			return true;
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(),idbg, fstr);
			return false;
		}

	}

	private void executaTabla() {
		try {
			lblInfo.setText(nombretabla);
			WSRec wsrec = new WSRec();
			wsrec.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean procesaDatos() {
		Cursor DT;
		BufferedWriter writer = null;
		FileWriter wfile;
		int rc, scomp, prn, jj;
		int ejecutarhh = 0;
		String s, val = "";

    	ferr = "";
		lblInfo.setText("Procesando tablas . . .");

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return true;

            try {
                String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
                wfile = new FileWriter(fname, false);
                writer = new BufferedWriter(wfile);
            } catch (IOException e) {}


            fprog = "Procesando ...";
			wsRtask.onProgressUpdate();

			ConT=new BaseDatos(this);
			dbT=ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			prn = 0;jj = 0;

			try{

				for (int i = 0; i < rc; i++) {

					sql = listItems.get(i);
					esql = sql;
					sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
					sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
					sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

					try {
						writer.write(sql);writer.write("\r\n");
					} catch (Exception e) {
					}
				}

				writer.close();

			} catch (Exception ex){

			}


			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);esql = sql;
				sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
				sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
				sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

				try {
					dbT = ConT.getWritableDatabase();
					dbT.execSQL(sql);
				} catch (Exception e) {
					ferr += " " +e.getMessage();
					addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				}

				try {
					if (i % 10 == 0) {
						fprog = "Procesando: " + i + " de: " + (rc - 1);
						wsRtask.onProgressUpdate();SystemClock.sleep(20);
					}
				} catch (Exception e) {
					ferr += " " +e.getMessage();
					addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				}
			}

            try {
                writer.close();
            } catch (Exception e) {
                String ss=e.getMessage();
            }

			fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
			wsRtask.onProgressUpdate();

			try {
				dbT.execSQL("DELETE FROM P_STOCK_PVC");
				sql = "INSERT INTO P_STOCK_PVC (CODIGO,CANT) SELECT CODIGO,CANT FROM P_STOCK_PV";
				dbT.execSQL(sql);
			} catch (Exception e) {
				msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			}

			if (modo_recepcion==1) {
				Actualiza_FinDia();
				encodeLicence();
				//encodeLicenceRuta();
				encodePrinters();
				fechaCarga();
				//SetStatusRecToTrans("1");
			}

			if (modo_recepcion==1 || modo_recepcion==2){
				SetStatusRecToTrans("1");
			}

			if (modo_recepcion!=3){
				fprog = "Documento de inventario recibido en BOF...";
				wsRtask.onProgressUpdate();

				//fechaCarga();
				//Actualiza_Documentos();

			}

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			fprog = "Fin de actualización";wsRtask.onProgressUpdate();

			scomp = 1;

			try {
				ConT.close();
			} catch (Exception e) {
				//addlog(new Object() {	}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			}

			lblInfo.setText(" ");
			s = "Recepción completa.";

			if (modo_recepcion!=3 ){
				try {
					Cursor dt1 = Con.OpenDT(sql);
					sql = "SELECT VENTA FROM P_RUTA";
					dt1 = Con.OpenDT(sql);
					dt1.moveToFirst();
					val = dt1.getString(0);

					if (dt1 != null) dt1.close();

				} catch (Exception e) {
					val = "V";
				}

				gl.rutatipo = val;
				rutatipo = gl.rutatipo;
				pedidos=rutatipo.equals("P");

				// if (stockflag == 1) s = s + "\nSe actualizó inventario.";

				if (pedidos) {
					sql = "SELECT Codigo FROM P_STOCK_PV ";
				} else {
					sql = "SELECT Codigo FROM P_STOCK UNION SELECT Codigo FROM P_STOCKB ";
				}
				Cursor dt = Con.OpenDT(sql);
				if (dt.getCount() > 0){
					stockflag = 1;
					s = s + "\nSe actualizó inventario.";
				}

				clsAppM.estandartInventario();
				clsAppM.estandartInventarioPedido();

				if (stockflag == 1) {
					sendConfirm();
				}
			}

			if (modo_recepcion==1 ){
				validaDatos(true);
				comparaCorrel();
				otrosParametros();
			}

			isbusy = 0;

			visibilidadBotones();

			msgAskExit(s);

			barInfo.setVisibility(View.INVISIBLE);
			lblParam.setVisibility(View.INVISIBLE);

			lblRec.setVisibility(View.VISIBLE);
			imgRec.setVisibility(View.VISIBLE);

			return true;

		} catch (Exception e) {
			fprog = "Actualización incompleta";
			wsRtask.onProgressUpdate();

			try {
				ConT.close();
			} catch (Exception ee) {
				sstr = e.getMessage();ferr += " " + sstr;
			}

			sstr = e.getMessage();ferr += " " + sstr + "\n" + sql;esql = sql;
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), ferr, esql);

			return false;

		}finally {
			visibilidadBotones();
		}

	}

	private void procesaParamsExt() {
		Cursor dt;
		String sql, val = "";
		int ival, rc;

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return;

			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);
				esql = sql;
				dbT.execSQL(sql);

				try {
					if (i % 10 == 0) {

						SystemClock.sleep(20);
					}
				} catch (Exception e) {
					Log.e("z", e.getMessage());
				}
			}

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			try {
				sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				val = dt.getString(0);
			} catch (Exception e) {
				val = "N";
			}
			if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
			else gl.peStockItf = false;

			try {
				sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=3";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				gl.peModal = dt.getString(0).toUpperCase();

				if (dt != null) dt.close();

			} catch (Exception e) {
				gl.peModal = "-";
			}

			try {
				ConT.close();
			} catch (Exception e) {
			}

		} catch (Exception e) {
			try {
				ConT.close();
			} catch (Exception ee) {
			}
		}

	}

	private void procesaRuta() {
		Cursor dt;
		String sql, val = "";
		int ival, rc;

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return;

			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);
				esql = sql;

				if (sql.contains("p_empresa")){
					msgbox("Si");
				}
				dbT.execSQL(sql);

				try {
					if (i % 10 == 0) SystemClock.sleep(20);
				} catch (Exception e) {}
			}

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			try {
				sql = "SELECT VENTA FROM P_RUTA";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				val = dt.getString(0);

				if (dt != null) dt.close();

			} catch (Exception e) {
				val = "V";
			}
			gl.rutatipo = val;

		} catch (Exception e) {
			try {
				ConT.close();
			} catch (Exception ee) {
			}
		}

	}

	private boolean borraDatos() {

		try {

			db.beginTransaction();

			sql = "DELETE FROM P_RUTA";
			db.execSQL(sql);
			sql = "DELETE FROM P_PRODUCTO";
			db.execSQL(sql);
			sql = "DELETE FROM P_COREL";
			db.execSQL(sql);
			sql = "DELETE FROM P_PARAMEXT";
			db.execSQL(sql);
			sql = "DELETE FROM P_PRODPRECIO";
			db.execSQL(sql);
			sql = "DELETE FROM P_CLIENTE";
			db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

		} catch (SQLException e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			db.endTransaction();
			//mu.msgbox("Error : " + e.getMessage());
			return false;
		}

		return true;
	}

	//#EJC20181120: Inserta los documentos que bajaron a la HH
	private boolean Actualiza_Documentos() {
		DateUtils DU = new DateUtils();
		long Now = du.getFechaActual();

		String ruta = txtRuta.getText().toString().trim();

		try {

			String SQL = " INSERT INTO P_DOC_ENVIADOS_HH " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCKB WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK_PALLET WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)";

			dbld.clear();
			dbld.add(SQL);

			if (commitSQL() == 1) {
				return true;
			} else {
				fterr += "\n" + sstr;
				dbg = sstr;
				return false;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			Log.e("Error", e.getMessage());
			return false;
		}

	}

	private boolean Actualiza_FinDia() {

		Cursor DT1;
		int vCorelZ = 0;
		float vGrandTotal = 0;

		boolean vActualizaFD = true;

		try {

			sql = "SELECT CORELZ, GRANDTOTAL FROM P_HANDHELD";
			DT1 = ConT.OpenDT(sql);

			if (DT1.getCount() > 0 ) {

				DT1.moveToFirst();

				vCorelZ = DT1.getInt(0);
				vGrandTotal = DT1.getFloat(1);

				sql = "UPDATE FINDIA SET COREL = " + vCorelZ + ", VAL1=0, VAL2=0, VAL3=0, VAL4=0,VAL5=0, VAL6=0, VAL7=0, VAL8 = " + vGrandTotal;
				dbT.execSQL(sql);
			}else if (gl.rutatipo.equals("P") || gl.rutatipo.equals("C")){
				sql = "UPDATE FINDIA SET COREL = 0, VAL1=0, VAL2=0, VAL3=0, VAL4=0,VAL5=0, VAL6=0, VAL7=0, VAL8 =0";
				dbT.execSQL(sql);
			}

			DT1.close();

			if (DT1 != null) DT1.close();


		} catch (Exception ex) {
			vActualizaFD = false;
		}

		return vActualizaFD;

	}

	private boolean update_Corel_GrandTotal() {

		Cursor DT1;
		int vCorelZ = 0;
		float vGrandTotal = 0;
		String vNumPlaca;

		boolean vActualizaFD = true;

		try {

			sql = "SELECT NUMPLACA, CORELZ, GRANDTOTAL FROM P_HANDHELD";
			DT1 = Con.OpenDT(sql);

			if (DT1.getCount() > 0) {

				DT1.moveToFirst();

				vNumPlaca = DT1.getString(0);
				vCorelZ = DT1.getInt(1);
				vGrandTotal = DT1.getFloat(2);

				sql = "UPDATE P_HANDHELD SET CORELZ = " + vCorelZ + ", GRANDTOTAL = " + vGrandTotal + " WHERE NUMPLACA = '" + vNumPlaca + "'";
				dbld.add(sql);
			}

			DT1.close();

		} catch (Exception ex) {
			vActualizaFD = false;
			fstr = ex.getMessage();
		}

		return vActualizaFD;

	}

	private boolean AddTable(String TN) {
		String SQL;
		String sqlDel= "DELETE FROM "+TN;

		try {

			fprog = TN;	if (cargastockpv) fprog="Procesando datos . . .";
         	idbg = TN;
			wsRtask.onProgressUpdate();

			if (modo_recepcion == 4 || modo_recepcion == 2) {
				String ntablas[] = {"P_STOCK", "P_STOCK_PALLET", "P_STOCKB"};

				for (int i = 0; i < ntablas.length; i++) {
					if (TN.equals(ntablas[i])) {
						sqlDel = "DELETE FROM "+TN+" WHERE 1 = 0 ";
					}
				}
			}

			SQL = getTableSQL(TN);
			if (fillTable2(SQL, sqlDel) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tabla:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tabla:" + TN;
			idbg = idbg;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, fstr);
			return false;
		}
	}

	private boolean AddTableVL_Original(String TN) {
		String SQL;

		try {

			fprog = TN;
			idbg = TN;
			//wsRtask.onProgressUpdate();
			SQL = getTableSQL(TN);

			if (fillTable(SQL, "DELETE FROM " + TN) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tabla:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tabla:" + TN + ", " + e.getMessage();
			idbg = idbg + e.getMessage();
			return false;
		}
	}

	private boolean AddTableVL(String TN) {
		String SQL;

		try {

			nombretabla = TN;
			fprog = TN;
			idbg = TN;
			//wsRtask.onProgressUpdate();
			SQL = getTableSQL(TN);

			if (fillTable2(SQL, "DELETE FROM " + TN) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tabla:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tabla:" + TN + ", " + e.getMessage();
			idbg = idbg + e.getMessage();
			return false;
		}
	}

	private String getTableSQL(String TN) {
		String SQL = "";
		long fi, ff;

		fi = du.ffecha00(du.getActDate());
		ff = du.ffecha24(du.getActDate());
		long ObjAno = du.getyear(du.getActDate());
		long ObjMes = du.getmonth(du.getActDate());

		if (TN.equalsIgnoreCase("P_STOCK")) {

			if (modo_recepcion==1){
				if (gl.peModal.equalsIgnoreCase("TOL")) {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + ActRuta + "'  AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
							"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)";
				} else if (gl.peModal.equalsIgnoreCase("APR")) {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsql + "') ";
				} else {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsql + "') ";
				}

			}else{
				if (gl.peModal.equalsIgnoreCase("TOL")) {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
							"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND  (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
							"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)" +
							"AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
							"AND (FECHA<='" + fsqlf + "'))";
				} else if (gl.peModal.equalsIgnoreCase("APR")) {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
							"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
				} else {
					SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
							"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, 0 " +
							"FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
				}
			}

			esql = SQL;
			return SQL;
		}

		//CKFK 20190222 Agregué a la consulta el AND (ENVIADO = 0)
		if (TN.equalsIgnoreCase("P_STOCKB")) {
			if (modo_recepcion==1){
				SQL = "SELECT RUTA, BARRA, CODIGO, CANT, COREL, PRECIO, PESO, DOCUMENTO,dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
						"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, DOC_ENTREGA, 0 " +
						"FROM P_STOCKB WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
						"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			}else{
				SQL = "SELECT RUTA, BARRA, CODIGO, CANT, COREL, PRECIO, PESO, DOCUMENTO,dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
						"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, DOC_ENTREGA, 0 " +
						"FROM P_STOCKB WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
						"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) " +
						"AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
						"AND (FECHA<='" + fsqlf + "'))";
			}
			return SQL;
		}

		//CKFK 20190304 Agregué la consulta para obtener los datos de P_STOCK_PALLET
		if (TN.equalsIgnoreCase("P_STOCK_PALLET")) {
			if(modo_recepcion==1){
				SQL = "SELECT DOCUMENTO, RUTA, BARRAPALLET, CODIGO, BARRAPRODUCTO, LOTEPRODUCTO, CANT, COREL, PRECIO, PESO, " +
						"UNIDADMEDIDA,dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, DOC_ENTREGA, 0  " +
						"FROM P_STOCK_PALLET WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
						"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			}else {
				SQL = "SELECT DOCUMENTO, RUTA, BARRAPALLET, CODIGO, BARRAPRODUCTO, LOTEPRODUCTO, CANT, COREL, PRECIO, PESO, " +
						"UNIDADMEDIDA,dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, DOC_ENTREGA, 0  " +
						"FROM P_STOCK_PALLET WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
						"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) " +
						"AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE (FECHA>='" + fsqli + "') " +
						"AND (FECHA<='" + fsqlf + "'))";
			}
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_BONIF")) {
			//SQL = "SELECT  CLIENTE, CTIPO, PRODUCTO, PTIPO, TIPORUTA, TIPOBON, RANGOINI, RANGOFIN, TIPOLISTA, TIPOCANT, VALOR," +
			//"LISTA, CANTEXACT, GLOBBON, PORCANT, dbo.AndrDate(FECHAINI), dbo.AndrDate(FECHAFIN), CODDESC, NOMBRE, EMP, UMPRODUCTO , UMBONIFICACION " +
			//"FROM P_BONIF WHERE ((dbo.AndrDate(FECHAINI)<=" + ff + ") AND (dbo.AndrDate(FECHAFIN)>=" + fi + "))";

			SQL = "SELECT  CLIENTE, CTIPO, PRODUCTO, PTIPO, TIPORUTA, TIPOBON, RANGOINI, RANGOFIN, TIPOLISTA, TIPOCANT, VALOR," +
					"LISTA, CANTEXACT, GLOBBON, PORCANT, dbo.AndrDate(FECHAINI), dbo.AndrDate(FECHAFIN), CODDESC, NOMBRE, EMP, UMPRODUCTO , UMBONIFICACION " +
					"FROM P_BONIF WHERE ((FECHAINI<='" + fsqlf + "') AND (FECHAFIN>='" + fsqli + "'))";
			esql = SQL;
			return SQL;
		}

		//23-7-2019 se adiciono la opcion de clientes por supervisor en base de las rutas
		if (TN.equalsIgnoreCase("P_CLIRUTA")) {
			if (!cargasuper) {
				SQL = "SELECT RUTA,CLIENTE,SEMANA,DIA,SECUENCIA,-1 AS BANDERA FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'";
			} else {
				SQL = "SELECT DISTINCT RUTA,CLIENTE,1 AS SEMANA,1 AS DIA,1 AS SECUENCIA,-1 AS BANDERA FROM P_CLIRUTA WHERE RUTA IN ( " +
						"SELECT DISTINCT RUTA FROM VENDEDORES WHERE Codigo IN ( " +
						"SELECT VENDEDOR FROM P_RUTA WHERE CODIGO='" + ActRuta + "'))";
			}
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIENTE")) {

			if (!cargasuper) {

				SQL = " SELECT CODIGO,NOMBRE,BLOQUEADO,TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL, ";
				SQL += "NIVELPRECIO,MEDIAPAGO,LIMITECREDITO,DIACREDITO,DESCUENTO,BONIFICACION, ";
				SQL += "dbo.AndrDate(ULTVISITA),IMPSPEC,INVTIPO,INVEQUIPO,INV1,INV2,INV3, NIT, MENSAJE, EMAIL, ";
				SQL += "TELEFONO,DIRTIPO, DIRECCION,MUNICIPIO, SUCURSAL,COORX, COORY, FIRMADIG, CODBARRA, VALIDACREDITO, ";
				SQL += "PRECIO_ESTRATEGICO, NOMBRE_PROPIETARIO, NOMBRE_REPRESENTANTE, ";
				SQL += "BODEGA, COD_PAIS, FACT_VS_FACT, CHEQUEPOST, PERCEPCION, TIPO_CONTRIBUYENTE, ID_DESPACHO, ID_FACTURACION," +
						"MODIF_PRECIO, INGRESA_CANASTAS, PRIORIZACION, CONTACTO, GEOREFERENCIA_CANASTA," +
						"GEOREFERENCIA_PREFACTURA,GEOREFERENCIA_PREVENTA, GEOREFERENCIA_AUTOVENTA, TIPORECEPTOR, CIUDAD, DESCRIPCION_PAGO," +
						"PERMITIR_PEDIDO_EXTRA_RUTA,TIPOLOGIA ";
				SQL += "FROM P_CLIENTE ";
				SQL += "WHERE (CODIGO IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') )) ";

			} else {

				SQL = " SELECT CODIGO,NOMBRE,BLOQUEADO,TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL, ";
				SQL += "NIVELPRECIO,MEDIAPAGO,LIMITECREDITO,DIACREDITO,DESCUENTO,BONIFICACION, ";
				SQL += "dbo.AndrDate(ULTVISITA),IMPSPEC,INVTIPO,INVEQUIPO,INV1,INV2,INV3, NIT, MENSAJE, EMAIL, ";
				SQL += "TELEFONO,DIRTIPO, DIRECCION,SUCURSAL,COORX, COORY, FIRMADIG, CODBARRA, VALIDACREDITO, ";
				SQL += "PRECIO_ESTRATEGICO, NOMBRE_PROPIETARIO, NOMBRE_REPRESENTANTE, ";
				SQL += "BODEGA, COD_PAIS, FACT_VS_FACT, CHEQUEPOST, PERCEPCION, TIPO_CONTRIBUYENTE, ID_DESPACHO, " +
						"ID_FACTURACION,MODIF_PRECIO, INGRESA_CANASTAS, PRIORIZACION, CONTACTO, GEOREFERENCIA_CANASTA," +
						"GEOREFERENCIA_PREFACTURA,GEOREFERENCIA_PREVENTA, GEOREFERENCIA_AUTOVENTA, " +
						"TIPORECEPTOR, CIUDAD ";
				SQL += "FROM P_CLIENTE WHERE CODIGO IN ( ";
				SQL += "SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA IN ( ";
				SQL += "SELECT DISTINCT RUTA FROM VENDEDORES WHERE Codigo IN ( ";
				SQL += "SELECT VENDEDOR FROM P_RUTA WHERE CODIGO='" + ActRuta + "'))) ";

			}

			return SQL;
		}

		//#HS_20181220 Tabla de Fachadas cliente.
		if (TN.equalsIgnoreCase("P_CLIENTE_FACHADA")) {
			SQL = " SELECT * FROM P_CLIENTE_FACHADA ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIDIR")) {
			SQL = " SELECT * FROM P_CLIDIR ";
			SQL += " WHERE (P_CLIDIR.CODIGO_CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') ))";
			return SQL;
		}
       /*       if (TN.equalsIgnoreCase("P_PRODUCTO")) {
           SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
           SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, DESCUENTO,BONIFICACION, ";
           SQL += "IMP1, IMP2, IMP3, VENCOMP, DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN ";
           SQL += "FROM P_PRODUCTO ";
           return SQL;
       }
	   */

		if (TN.equalsIgnoreCase("P_PRODUCTO")) {

			if (!cargasuper) {
				//#KM20210802 Agregué OR ES_CANASTA = 1
				SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
				SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, ISNULL(DESCUENTO,'N') AS DESCUENTO, ISNULL(BONIFICACION,'N') AS BONIFICACION, ";
				SQL += "IMP1, IMP2, IMP3, VENCOMP, ISNULL(DEVOL,'S') AS DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN, ";
				SQL += "VIDEO,VENTA_POR_PESO,ES_PROD_BARRA,UNID_INV,VENTA_POR_PAQUETE,VENTA_POR_FACTOR_CONV,ES_SERIALIZADO,PARAM_CADUCIDAD, ";
				SQL += "PRODUCTO_PADRE,FACTOR_PADRE,TIENE_INV,TIENE_VINETA_O_TUBO,PRECIO_VINETA_O_TUBO,ES_VENDIBLE,UNIGRASAP,UM_SALIDA,ES_CANASTA, SUBBODEGA ";
				SQL += "FROM P_PRODUCTO WHERE (ES_VENDIBLE=1 OR ES_CANASTA = 1) AND ((CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
						" OR CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "'))" +
						" OR LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "') AND EMPRESA = '" + gEmpresa + "') OR UNIDMED='CAN' )" +
						" AND CODIGO IN ( " +
						" SELECT CODIGO FROM P_PRODPRECIO WHERE (NIVEL IN ( SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE " +
						" WHERE (CODIGO IN ( SELECT DISTINCT CLIENTE FROM DS_PEDIDO WHERE (RUTA ='" + ActRuta + "') AND (BANDERA='D')))))OR " +
						" NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE  " +
						" WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "')))" +
						" OR TIENE_VINETA_O_TUBO = 1 ";
			} else {

				SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
				SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, ISNULL(DESCUENTO,'N') AS DESCUENTO, ISNULL(BONIFICACION,'N') AS BONIFICACION, ";
				SQL += "IMP1, IMP2, IMP3, VENCOMP, ISNULL(DEVOL,'S') AS DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN, ";
				SQL += "VIDEO,VENTA_POR_PESO,ES_PROD_BARRA,UNID_INV,VENTA_POR_PAQUETE,VENTA_POR_FACTOR_CONV,ES_SERIALIZADO,PARAM_CADUCIDAD, ";
				SQL += "PRODUCTO_PADRE,FACTOR_PADRE,TIENE_INV,TIENE_VINETA_O_TUBO,PRECIO_VINETA_O_TUBO,ES_VENDIBLE,UNIGRASAP,UM_SALIDA, ES_CANASTA, SUBBODEGA ";
				SQL += "FROM P_PRODUCTO WHERE (ES_VENDIBLE=1) ";

			}

			return SQL;
		}

		if (TN.equalsIgnoreCase("P_FACTORCONV")) {
			//#EJC20181112
			//SQL = "SELECT PRODUCTO,UNIDADSUPERIOR,FACTORCONVERSION,UNIDADMINIMA FROM P_FACTORCONV ";
			if (!cargasuper) {
				SQL = " SELECT * FROM P_FACTORCONV WHERE PRODUCTO IN (SELECT CODIGO " +
						" FROM P_PRODUCTO WHERE LINEA IN (SELECT DISTINCT LINEA FROM P_LINEARUTA " +
						" WHERE RUTA = '" + ActRuta + "')) " +
						" OR ((PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
						" OR PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "')))";
			} else {
				SQL = " SELECT * FROM P_FACTORCONV WHERE PRODUCTO IN (SELECT CODIGO " +
						" FROM P_PRODUCTO WHERE LINEA IN (SELECT DISTINCT LINEA FROM P_LINEARUTA " +
						" WHERE RUTA IN ( SELECT DISTINCT RUTA FROM VENDEDORES WHERE Codigo IN ( " +
						" SELECT VENDEDOR FROM P_RUTA WHERE CODIGO='" + ActRuta + "')))) ";
			}

			return SQL;
		}

		if (TN.equalsIgnoreCase("P_LINEA")) {
			SQL = "SELECT CODIGO,MARCA,NOMBRE FROM P_LINEA ";
			SQL += "WHERE (CODIGO IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PRODPRECIO")) {
			if (!cargasuper) {
				SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO ";
				SQL += " WHERE ( (CODIGO IN ( SELECT CODIGO FROM P_PRODUCTO WHERE (LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE RUTA='" + ActRuta + "')) ) ) ";
				SQL += " OR  (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "')) ) ";
				SQL += " AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "')))";
			} else {
				SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO ";
			}

			return SQL;
		}

		if (TN.equalsIgnoreCase("TMP_PRECESPEC")) {

			if (!cargasuper) {
				SQL = "SELECT CODIGO,VALOR,PRODUCTO,PRECIO,UNIDADMEDIDA FROM TMP_PRECESPEC ";
				SQL += " WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
			} else {
				SQL = "SELECT DISTINCT CODIGO,VALOR,PRODUCTO,PRECIO,UNIDADMEDIDA FROM TMP_PRECESPEC ";
				SQL += "WHERE (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') AND ";
				SQL += "RUTA IN (SELECT DISTINCT RUTA FROM VENDEDORES WHERE Codigo IN ( ";
				SQL += "SELECT VENDEDOR FROM P_RUTA WHERE CODIGO='" + ActRuta + "')) ";
			}

			return SQL;
		}

		if (TN.equalsIgnoreCase("P_DESCUENTO")) {
			SQL = "SELECT  CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,DESCTIPO,VALOR,GLOBDESC,PORCANT,dbo.AndrDateIni(FECHAINI),dbo.AndrDateFin(FECHAFIN),CODDESC,NOMBRE ";
			SQL += "FROM P_DESCUENTO WHERE DATEDIFF(D, FECHAINI,GETDATE()) >=0 AND DATEDIFF(D,GETDATE(), FECHAFIN) >=0";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_EMPRESA")) {
			SQL = "SELECT EMPRESA, NOMBRE, INITPATH, FTPPATH, NUMREIMPRES, MODDESC, USARPESO, DEVCONPREC, ACUMDESC, DESCMAX, " +
				  " BONVOLTOL, COD_PAIS, BOLETA_DEPOSITO, EDITAR_DIRECCION, DEPOSITO_PARCIAL, COL_IMP, INV_ENLINEA, FIN_DIA," +
				  " PRESENTACION_MULTIPLE, PRECIOS_ESPECIALES, AUTORIZ_MODIF_DESCBON, CAMBIO_POR_CAMBIO, DEVOLUCION_MERCANCIA," +
				  " COBROS_SIN_REFERENCIA, PORCENTAJE_NC, PORC_MERMA, PRODUCTO_ERROR_SUMA, UNIDAD_MEDIDA_PESO, LOTE_POR_DEFECTO," +
				  " INCIDENCIA_NO_LECTURA, IMPRIMIR_TOTALES_PEDIDO, URL_AUTENTICACION, USUARIO_API, CLAVE_API, URL_ANULACION, " +
				  " URL_CONSULTA_DOCUMENTOS_POR_CRITERIO,URL_EMISION_FACTURA_B2C, URL_EMISION_NC_B2C,URL_EMISION_ND_B2C," +
				  " URL_BASE,URL_TOKEN, QR_API,ARCHIVO_P12, QR_CLAVE, URL_B2C_HH, URL_DOC, URL_EMISION_NC_B2B_HH, URL_EMISION_ND_B2B_HH," +
				  " UNIDAD_MEDIDA_DEFECTO, AMBIENTE, URL_CONSULTAR_DOCUMENTO_POR_RUTA " +
				  " FROM P_EMPRESA WHERE EMPRESA = '" + gEmpresa + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_RUTA")) {
			SQL = "SELECT CODIGO, NOMBRE, ACTIVO, VENDEDOR, VENTA, FORANIA, SUCURSAL, TIPO, SUBTIPO, " +
					"BODEGA, SUBBODEGA, DESCUENTO, BONIF, KILOMETRAJE, IMPRESION, RECIBOPROPIO, CELULAR, RENTABIL, OFERTA, " +
					"PERCRENT, PASARCREDITO, TECLADO, EDITDEVPREC, EDITDESC, PARAMS, SEMANA, OBJANO, OBJMES, SYNCFOLD, WLFOLD, " +
					"FTPFOLD, EMAIL, LASTIMP, LASTCOM, LASTEXP, IMPSTAT, EXPSTAT, COMSTAT, PARAM1, " +
					"PARAM2, PESOLIM, INTERVALO_MAX, LECTURAS_VALID, INTENTOS_LECT, HORA_INI, HORA_FIN, " +
					"APLICACION_USA, PUERTO_GPS, ES_RUTA_OFICINA, DILUIR_BON, PREIMPRESION_FACTURA, MODIFICAR_MEDIA_PAGO, " +
					"IDIMPRESORA, NUMVERSION,0 AS FECHAVERSION, ARQUITECTURA, PERMITIR_PRODUCTO_NUEVO, " +
					"PERMITIR_CANTIDAD_MAYOR, ENVIO_AUTO_PEDIDOS, PEDIDOS_CLINUEVO, VALIDAR_POSICION_GEOREFERENCIAL, " +
					"CONTROL_CANASTA " +
					"FROM P_RUTA WHERE CODIGO = '" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_BANCO")) {
			SQL = "SELECT * FROM P_BANCO WHERE EMPRESA = '" + gEmpresa + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_STOCKINV")) {
			SQL = "SELECT * FROM P_STOCKINV";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODATEN")) {
			SQL = "SELECT * FROM P_CODATEN";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODNOLEC")) {
			SQL = "SELECT CODIGO, DESCRIPCION AS NOMBRE  FROM P_RAZONNOSCAN";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODDEV")) {
			SQL = "SELECT CODIGO, DESCRIPCION, ESTADO, PORCENTAJE FROM P_CODDEV";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_RAZON_DESP_INCOMP")) {
			SQL = "SELECT IDRAZON, DESCRIPCION FROM P_RAZON_DESP_INCOMP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_NIVELPRECIO")) {
			SQL = "SELECT CODIGO, NOMBRE, ISNULL(DECIMALES,0) AS DECIMALES FROM P_NIVELPRECIO ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIGRUPO")) {
			SQL = "SELECT CODIGO,CLIENTE FROM P_CLIGRUPO WHERE (CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_STOCK_APR")) {
			SQL = "SELECT CODIGO, CANT, PESO " +
					"FROM P_STOCK_APR WHERE RUTA='" + ActRuta + "' ";
			//SQL = "SELECT CODIGO,CANT,0 AS CANTM,PESO FROM P_STOCK WHERE RUTA='" + ActRuta + "'";
			//idbg=SQL;
			return SQL;
		}

		//#HS_20181212 Agregue campos ID_TRANSACCION, REFERENCIA, ASIGNACION.
		if (TN.equalsIgnoreCase("P_COBRO")) {
			SQL = "SELECT  DOCUMENTO, EMPRESA, RUTA, CLIENTE, TIPODOC, VALORORIG, SALDO, CANCELADO, dbo.AndrDate(FECHAEMIT),dbo.AndrDate(FECHAV),'' AS CONTRASENA, ID_TRANSACCION, REFERENCIA, ASIGNACION ";
			SQL += "FROM P_COBRO WHERE (RUTA='" + ActRuta + "') AND CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "')) ";
			//idbg=SQL;
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_COREL")) {
			SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_COREL WHERE RUTA='" + ActRuta + "' AND ACTIVA = 'S'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORELNC")) {
			SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_CORELNC WHERE RUTA='" + ActRuta + "' AND ACTIVA = 'S'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORRELREC")) {
			SQL = "SELECT RUTA,SERIE,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORRELREC WHERE RUTA='" + ActRuta + "' AND ENVIADO = 'N'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORREL_OTROS")) {
			SQL = "SELECT RUTA,SERIE,TIPO,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORREL_OTROS WHERE RUTA='" + ActRuta + "' AND ENVIADO = 'N'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MEDIAPAGO")) {
			SQL = "SELECT CODIGO,NOMBRE,ACTIVO,NIVEL,PORCOBRO, VIA_PAGO_DGI FROM P_MEDIAPAGO WHERE ACTIVO='S'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_ARCHIVOCONF")) {
			SQL = "SELECT RUTA,TIPO_HH,IDIOMA,TIPO_IMPRESORA,SERIAL_HH,MODIF_PESO,PUERTO_IMPRESION,LBS_O_KGS,NOTA_CREDITO FROM P_ARCHIVOCONF WHERE (RUTA='" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_ENCABEZADO_REPORTESHH")) {
			SQL = "SELECT CODIGO,TEXTO,SUCURSAL FROM P_ENCABEZADO_REPORTESHH_II WHERE SUCURSAL IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_BONLIST")) {
			SQL = "SELECT CODIGO,PRODUCTO,CANT,CANTMIN,NOMBRE FROM P_BONLIST " +
					"	WHERE CODIGO IN (SELECT LISTA FROM P_BONIF WHERE TIPOLISTA in (1,2) " +
					"	AND DATEDIFF(D, FECHAINI,GETDATE()) >=0 AND DATEDIFF(D,GETDATE(), FECHAFIN) >=0  AND EMP = '" + gEmpresa + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PRODGRUP")) {
			SQL = "SELECT CODIGO,PRODUCTO,NOMBRE FROM P_PRODGRUP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_IMPUESTO")) {
			SQL = "SELECT CODIGO,VALOR FROM P_IMPUESTO";
			return SQL;
		}

		//#HS_20181206 Agregue Ruta.
		if (TN.equalsIgnoreCase("P_VENDEDOR")) {

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				SQL = "SELECT CODIGO,NOMBRE,CLAVE,RUTA,NIVEL,NIVELPRECIO,ISNULL(BODEGA,0) AS BODEGA,ISNULL(SUBBODEGA,0) AS SUBBODEGA,'' AS COD_VEHICULO,0 AS LIQUIDANDO,0 AS BLOQUEADO,0 AS DEVOLUCION_SAP  " +
						"FROM VENDEDORES  WHERE (RUTA='" + ActRuta + "') ";
			} else {
				SQL = "SELECT CODIGO,NOMBRE,CLAVE,RUTA,NIVEL,NIVELPRECIO,ISNULL(BODEGA,0) AS BODEGA,ISNULL(SUBBODEGA,0) AS SUBBODEGA,COD_VEHICULO,LIQUIDANDO,BLOQUEADO,DEVOLUCION_SAP  " +
						"FROM P_VENDEDOR  WHERE (RUTA='" + ActRuta + "') OR (NIVEL=1) ";
			}

			return SQL;
		}

		//#HS_20181207 Agregue campos de P_VEHICULO.
		if (TN.equalsIgnoreCase("P_VEHICULO")) {
			SQL = "SELECT CODIGO,MARCA,PLACA,PESO,KM_MILLAS,TIPO FROM P_VEHICULO";
			return SQL;
		}

		//#CKFK_20190319 Agregué tabla P_HANDHELD
		if (TN.equalsIgnoreCase("P_HANDHELD")) {
			SQL = " SELECT NUMPLACA, NUMSERIE, TIPO, ISNULL(CREADA,'') AS CREADA, " +
					" ISNULL(MODIFICADA,'') AS MODIFICADA, ISNULL(FECHA_CREADA, GETDATE()) AS FECHA_CREADA," +
					"	ISNULL(FECHA_MODIFICADA, GETDATE()) AS FECHA_MODIFICADA, CORELZ, GRANDTOTAL FROM P_HANDHELD" +
					" WHERE NUMPLACA IN (SELECT HANDHELD FROM P_COREL WHERE RUTA = '" + ActRuta + "' AND ACTIVA = 'S')";
			return SQL;
		}

		//#CKFK_20190319 Agregué tabla P_IMPRESORA
		if (TN.equalsIgnoreCase("P_IMPRESORA")) {
			SQL = " SELECT IDIMPRESORA, NUMSERIE, MARCA, ISNULL(CREADA,'') AS CREADA, " +
					" ISNULL(MODIFICADA,'') AS MODIFICADA, " +
					"0 AS FECHA_CREADA,0 AS FECHA_MODIFICADA,MACADDRESS FROM P_IMPRESORA";
			return SQL;
		}

		//#CKFK_20190522 Agregué tabla P_TRANSERROR
		if (TN.equalsIgnoreCase("P_TRANSERROR")) {
			SQL = " SELECT IDTRANSERROR, TRANSERROR FROM P_TRANSERROR";
			return SQL;
		}

		//#CKFK_20190707 Agregué tabla P_GLOBPARAM
		if (TN.equalsIgnoreCase("P_GLOBPARAM")) {
			SQL = " SELECT EMPID, COMSERVER, FTPSERVER, VERFACTURA, VERPEDIDO, VERCOBRO, VALORN1 FROM P_GLOBPARAM";
			return SQL;
		}

		//#CKFK_20190522 Agregué tabla P_CONFIGBARRA
		if (TN.equalsIgnoreCase("P_CONFIGBARRA")) {
			SQL = " SELECT IDCONFIGBARRA, LONGITUDBARRA, PREFIJO FROM P_CONFIGBARRA";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MUNI")) {
			SQL = "SELECT * FROM P_MUNI";
			return SQL;
		}

        if (TN.equalsIgnoreCase("P_DEPAR")) {
            SQL = "SELECT * FROM P_DEPAR";
            return SQL;
        }

		if (TN.equalsIgnoreCase("P_CIUDAD")) {
			SQL = "SELECT * FROM P_CIUDAD";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF1")) {
			SQL = "SELECT * FROM P_REF1";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF2")) {
			SQL = "SELECT * FROM P_REF2";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF3")) {
			SQL = "SELECT * FROM P_REF3";
			return SQL;
		}

        if (TN.equalsIgnoreCase("P_CANAL")) {
            SQL = "SELECT * FROM P_CANAL";
            return SQL;
        }

        if (TN.equalsIgnoreCase("P_CANALSUB")) {
            SQL = "SELECT * FROM P_CANALSUB";
            return SQL;
        }

        if (TN.equalsIgnoreCase("P_PRIORIZACION")) {
            SQL = "SELECT * FROM P_PRIORIZACION";
            return SQL;
        }

		if (TN.equalsIgnoreCase("P_TIPOLOGIA")) {
			SQL = "SELECT CODIGO, CANALSUB, NOMBRE FROM P_TIPOLOGIA";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MONTO_MINIMO_CLIENTE")) {
			SQL = "SELECT CLIENTE,MM_ESTANDAR,MM_EXTRARUTA FROM P_MONTO_MINIMO_CLIENTE";
			return SQL;
		}


		// Objetivos

		if (TN.equalsIgnoreCase("O_PROD")) {
			SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_PROD WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_LINEA")) {
			SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_LINEA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_RUTA")) {
			SQL = "SELECT * FROM O_RUTA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_COBRO")) {
			SQL = "SELECT * FROM O_COBRO WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}


		// Mercadeo

		if (TN.equalsIgnoreCase("P_MEREQTIPO")) {
			SQL = "SELECT * FROM P_MEREQTIPO";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MEREQUIPO")) {
			SQL = "SELECT * FROM P_MEREQUIPO ";
			SQL = SQL + "WHERE (CLIENTE IN  (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "' ) )";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERESTADO")) {
			SQL = "SELECT * FROM P_MERESTADO";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERPREGUNTA")) {
			SQL = "SELECT * FROM P_MERPREGUNTA";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERRESP")) {
			SQL = "SELECT * FROM P_MERRESP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERMARCACOMP")) {
			SQL = "SELECT * FROM P_MERMARCACOMP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERPRODCOMP")) {
			SQL = "SELECT * FROM P_MERPRODCOMP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PORCMERMA")) {
			SQL = "SELECT EMPRESA,SUCURSAL,RUTA,PRODUCTO,PORCENTAJEMERMA,PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE (RUTA='" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("LIC_CLIENTE")) {
			SQL = "SELECT * FROM LIC_CLIENTE WHERE ID='" + mac + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PARAMEXT")) {
			SQL = "SELECT ID,Nombre,Valor FROM P_PARAMEXT WHERE ((idRuta='" + ActRuta + "') OR (ISNULL(idRuta,'')=''))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_LIQUIDACION")) {
			SQL = "SELECT RUTA,ESTADO FROM P_LIQUIDACION WHERE (RUTA='" + ActRuta + "') AND (FECHA>='" + fsql + "') ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_SUCURSAL")) {
			SQL = " SELECT CODIGO, EMPRESA, DESCRIPCION, NOMBRE, DIRECCION, TELEFONO, NIT, TEXTO, TIPO_SUCURSAL, CORREO, COORDENADA_X, COORDENADA_Y, CODUBI, TIPORUC, CODMUNI, SITIO_WEB, MM_INCLUYE_CERRADOS " +
					" FROM P_SUCURSAL WHERE CODIGO IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MEDIDA")) {
			SQL = " SELECT CODIGO, DESCRIPCION, CODIGO_CGI" +
				  " FROM P_MEDIDA";
			return SQL;
		}

        if (TN.equalsIgnoreCase("P_STOCK_PV")) {
            //"FROM P_STOCK WHERE RUTA='" + ActRuta + "'  AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
            //        "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)";

            SQL = "SELECT  RUTA, CODIGO, CANT, PESO, CANT_SOL, PESO_SOL, UNIDADMEDIDA, DOCUMENTO, " +
                    "0 , ANULADO, CENTRO, ESTADO, ENVIADO " +
                    "FROM P_STOCK_PV WHERE (RUTA ='"+ActRuta+"') AND (ANULADO=0) " +
                    "AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
            return SQL;
        }

		if (TN.equalsIgnoreCase("DS_PEDIDO")) {
			SQL = " SELECT COREL, ANULADO, dbo.AndrDate(FECHA), EMPRESA, RUTA, VENDEDOR, CLIENTE, KILOMETRAJE, dbo.AndrDate(FECHAENTR), DIRENTREGA, " +
					" TOTAL, DESMONTO, IMPMONTO, PESO, BANDERA, STATCOM, CALCOBJ, IMPRES, ADD1, ADD2, ADD3, ORDEN_COMPRA "+
					" FROM DS_PEDIDO " +
					" WHERE RUTA='" + ActRuta + "' AND (ANULADO='N') AND (STATCOM = 'N') " +
					"AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("DS_PEDIDOD")) {
			SQL = " SELECT COREL, PRODUCTO, EMPRESA, ANULADO, CANT, PRECIO, IMP, DES, DESMON, TOTAL, PRECIODOC, " +
					"PESO, VAL1, VAL2, RUTA, UMVENTA, UMSTOCK, UMPESO, CANT_ORIGINAL, PESO_ORIGINAL "+
					" FROM DS_PEDIDOD " +
					" WHERE COREL IN (SELECT COREL FROM DS_PEDIDO WHERE RUTA = '" + ActRuta + "'  AND (ANULADO='N') " +
					"AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') )";
			return SQL;
		}

        if (TN.equalsIgnoreCase("D_CANASTA")) {
        	SQL = "SELECT RUTA, FECHA, CLIENTE, PRODUCTO, CANTREC, CANTENTR, STATCOM, CORELTRANS, PESOREC, PESOENTR, ANULADO, UNIDBAS, CODIGOLIQUIDACION " +
					"FROM D_CANASTA " +
					"WHERE RUTA = '" + ActRuta + "' " +
					"AND FECHA >= '"+fsqli+"'  " +
					"AND ANULADO = 0;";
		}
		return SQL;
	}

	private void comparaCorrel() {
		Cursor DT;
		String ss;
		try {
			try {
				sql = "SELECT VENTA FROM P_RUTA";
				DT = Con.OpenDT(sql);

				if (DT.getCount() == 0) {
					msgbox("La ruta no existe, por favor informe a su supervisor!");
				}

				DT.moveToFirst();
				ss = DT.getString(0);
				if (ss.equalsIgnoreCase("T")) ss = "V";

				if (DT != null) DT.close();

			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				ss = "X";
			}

			if (!ss.equalsIgnoreCase("V")) return;

			ultcor = ultCorel();
			if (ultcor == 0) {
				//msgbox("No está definido correlativo de las facturas!\n Por favor informe a su supervisor.");
			}

			ultSerie = ultSerie(); //#HS_20181129_1005 Agregue ultSerie.
			if (ultcor_ant != ultcor) {
				//#HS_20181129_1005 Agregue comparacion para las series.
				if (ultSerie_ant != ultSerie) {
					msgbox("Nueva serie de facturación");
				} else if (ultcor_ant > 0) {
					msgbox("El último correlativo actualizado ( " + ultcor + " ) no coincide con último emitido ( " + ultcor_ant + " )!\n Por favor infore a su supervisor.");
					return;
				}
			}
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

	}

	private boolean validaDatos(boolean completo) {
		Cursor dt;

		try {

			if (!rutatipo.equalsIgnoreCase("P") && !rutatipo.equalsIgnoreCase("C")) {
				sql = "SELECT RESOL FROM P_COREL";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
				    if (!pedidos) {
                        toastlong("No está definido correlativo de facturas");return false;
                    }
				}
			}

			sql = "SELECT Codigo FROM P_CLIENTE";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
                toastlong("Lista de clientes está vacia");
				return false;
			}

			sql = "SELECT Ruta FROM P_CLIRUTA";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
                toastlong("Lista de clientes por ruta está vacia");
				return false;
			}

			sql = "SELECT Codigo FROM P_PRODUCTO";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
                toastlong("Lista de productos está vacia");
				return false;
			}

			if (completo) {

				sql = "SELECT Nivel FROM P_PRODPRECIO ";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
					msgbox("Lista de precios está vacia");
					return false;
				}

				sql = "SELECT Producto FROM P_FACTORCONV ";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
                    toastlong("Lista de conversiones está vacia");
					return false;
				}

                if (pedidos) {
                    sql = "SELECT Codigo FROM P_STOCK_PV ";
                } else {
                    sql = "SELECT Codigo FROM P_STOCK UNION SELECT Codigo FROM P_STOCKB";
                }

                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                	if (!rutatipo.equals("C")){
						toastlong("La de carga inventario de productos está vacia");
						return false;
					}
                }

			}

			if (dt != null) dt.close();

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			Log.d("ValidaDatos", e.getMessage());
		}

		return true;
	}

	//#HS_20181123_1623 Agregue funcion FinDia para el commit y update de tablas.
	private boolean FinDia() {

		try {

			if (commitSQL() == 1) {

				db.beginTransaction();
				db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
				db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
				db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
				db.execSQL("UPDATE D_CXC SET STATCOM='S'");
				db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
				db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
				db.execSQL("UPDATE D_MOV SET STATCOM='S'");
				db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
				db.execSQL("UPDATE D_CLIENTE_MODIF SET STATCOM='S'");
				db.execSQL("UPDATE D_CLINUEVOT SET STATCOM='S'");
				db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
				db.execSQL("UPDATE D_CANASTA SET STATCOM='S'");
				db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
				db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
				db.execSQL("UPDATE D_RATING SET STATCOM='S'");
				db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
				db.execSQL("UPDATE D_DESPACHOD_NO_ENTREGADO SET STATCOM='S'");
				db.execSQL("UPDATE FINDIA SET VAL5=0, VAL4=0,VAL3=0, VAL2=0");
				db.setTransactionSuccessful();
				db.endTransaction();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox("FinDia(): " + e.getMessage());
			return false;
		}
		return true;
	}

	//#CKFK_20190325 Agregué funcion ActualizaStatcom que es una copia de FinDia pero sin el CommitSQL
	private boolean ActualizaStatcom() {

		try {

			db.beginTransaction();
			db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
			db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
			db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
			db.execSQL("UPDATE D_CXC SET STATCOM='S'");
			db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
			db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
			db.execSQL("UPDATE D_MOV SET STATCOM='S'");
			db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
			db.execSQL("UPDATE D_CLIENTE_MODIF SET STATCOM='S'");
			db.execSQL("UPDATE D_CLINUEVOT SET STATCOM='S'");
			db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
			db.execSQL("UPDATE D_CANASTA SET STATCOM='S'");
			db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
			db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
			db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
			db.execSQL("UPDATE D_RATING SET STATCOM='S'");
			db.execSQL("UPDATE D_DESPACHOD_NO_ENTREGADO SET STATCOM='S'");
			db.execSQL("UPDATE P_RUTA SET PARAM2 = ''");
			db.setTransactionSuccessful();
			db.endTransaction();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox("ActualizaStatcom(): " + e.getMessage());
			return false;
		}
		return true;
	}

	private void encodePrinters() {
		Cursor dt;
		String prid, ser, mac, se, sm;

		try {
			sql = "SELECT IDIMPRESORA,NUMSERIE,MACADDRESS FROM P_IMPRESORA";
			dt = ConT.OpenDT(sql);

			if (dt.getCount() > 0) dt.moveToFirst();
			while (!dt.isAfterLast()) {

				prid = dt.getString(0);
				ser = dt.getString(1);
				mac = dt.getString(2);

				se = cu.encrypt(ser);
				sm = cu.encrypt(mac);

				sql = "UPDATE P_IMPRESORA SET NUMSERIE='" + se + "',MACADDRESS='" + sm + "' WHERE IDIMPRESORA='" + prid + "'";
				dbT.execSQL(sql);

				dt.moveToNext();
			}

			if (dt != null) dt.close();

		} catch (Exception e) {
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}
	}

	private void encodeLicence() {
		String licD, LicR;

		try {

			if (licResult == 1) {
				licD = licSerialEnc;
				sql = "UPDATE Params SET lic='" + lic + "'";
				dbT.execSQL(sql);
			}else{
				if (licResult == 2) {
					LicR = licRutaEnc;
					licD = licSerialEnc;
					sql = "UPDATE Params SET lic='" + licD + "', licparam='" + LicR + "'";
					dbT.execSQL(sql);

				}else{
					LicR = "";
					licD = "";
				}
			}

		} catch (Exception e) {
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}

	}

	private void encodeLicenceRuta() {
		String lic;

		try {
			if (licResultRuta == 1) lic = licRutaEnc;
			else lic = "";
			sql = "UPDATE Params SET licparam='" + lic + "'";
			dbT.execSQL(sql);
		} catch (Exception e) {
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}

	}

	private boolean validaLicencia() {
		CryptUtil cu = new CryptUtil();
		Cursor dt;
		String lic, lickey, licruta, rutaencrypt;
		Integer msgLic = 0;

		if (gl.debug) {
			return true;
		}


		try {
			lickey = cu.encrypt(gl.deviceId);
			rutaencrypt = cu.encrypt(gl.ruta);

			sql = "SELECT lic, licparam FROM Params";
			dt = Con.OpenDT(sql);
			dt.moveToFirst();
			lic = dt.getString(0);
			licruta = dt.getString(1);
			String ss = cu.decrypt(licruta);

			if (dt != null) dt.close();

			if (lic.equalsIgnoreCase(lickey) && licruta.equalsIgnoreCase(rutaencrypt)) return true;
			else if (!lic.equalsIgnoreCase(lickey) && !licruta.equalsIgnoreCase(rutaencrypt)) {
				msgLic = 1;
			} else if (!lic.equalsIgnoreCase(lickey)) {
				msgLic = 2;
			} else if (!licruta.equalsIgnoreCase(rutaencrypt)) {
				msgLic = 3;
			}
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			mu.msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " : " + e.getMessage());
		}

		if (msgLic == 1)
			toastlong("El dispositivo no tiene licencia válida de handheld, ni de ruta");
		else if (msgLic == 2) {
			toastlong("El dispositivo no tiene licencia valida de handheld");
		} else if (msgLic == 3) {
			toastlong("El dispositivo no tiene licencia valida de ruta");
		}

		return false;
	}

	private void fechaCarga() {

		try {
			//dbT.beginTransaction();

			dbT.execSQL("DELETE FROM P_FECHA");

			sql = "INSERT INTO P_FECHA VALUES('" + gl.ruta + "'," + du.getActDate() + ")";
			dbT.execSQL(sql);

			//dbT.setTransactionSuccessful();
			//dbT.endTransaction();
		} catch (Exception e) {
			//dbT.endTransaction();
		}

	}

	//endregion

	//region WS Recepcion Handling Methods

	public void wsExecute() {

		running = 1;
		fstr = "No connect";
		scon = 0;

		try {
			if (getTest() == 1) {
				scon = 1;
			} else {
				URL = URL_Remota;
				if (getTest() == 1) scon = 1;
			}

			idbg = idbg + sstr;

			if (scon == 1) {
				fstr = "Sync OK";
				if (!getData()) fstr = "Recepcion incompleta : " + fstr;
			} else {
				fstr = "No se puede conectar al web service : " + sstr;
			}
		} catch (Exception e) {
			scon = 0;
			fstr = "Error importando los datos: " + fstr;
			Log.d("E", fstr + " " + sstr);
		}

	}

	public void wsFinished() {

        running = 0;

        try {
            if (fstr.equalsIgnoreCase("Sync OK")) {

                // JP20211018
                if (modo_recepcion==1) {
                    cargaTodasTablas();
                } else if (modo_recepcion==2) {
                    cargaTablasExist();
                } else if(modo_recepcion == 3) {
					cargaTablasPrecio();
				} else if(modo_recepcion == 4) {
					cargaTablasRec();
                }

            } else {
                lblInfo.setText(fstr);
                mu.msgbox("Ocurrió error : \n" + fstr + " " + idbg + " (Registro: " + reccnt + ") ");
                //mu.msgbox("::" + esql);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                visibilidadBotones();
                addlog("Recepcion", fstr, idbg);
                return;
            }
            if (ftflag) msgbox(ftmsg);
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }
    }

    private void setValoresRuta() {
		Cursor DT;
		try	{
			sql="SELECT PERMITIR_CANTIDAD_MAYOR, PERMITIR_PRODUCTO_NUEVO, VALIDAR_POSICION_GEOREFERENCIAL FROM P_RUTA";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				gl.permitir_cantidad_mayor = (DT.getInt(0) == 1 ? true : false);
				gl.permitir_producto_nuevo = (DT.getInt(1) == 1 ? true : false);
				gl.validar_posicion_georef = (DT.getInt(2) == 1 ? true : false);
			}

		} catch (Exception e){
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}
	}

    private void confImpresora() {
        try {

            //sql = "UPDATE Params SET prn='" + clsAppM.getPrintId_Ruta() + "',prnserie='" + clsAppM.impresTipo_Ruta() + "' ";
            //db.execSQL(sql);

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private class AsyncCallRec extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsExecute();
			} catch (Exception e) {
				if (scon == 0) {
					fstr = "No se puede conectar al web service : " + sstr;
					visibilidadBotones();
				}
				//msgbox(fstr);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				wsFinished();
			} catch (Exception e) {
				Log.d("onPostExecute", e.getMessage());
			}

		}

		@Override
		protected void onPreExecute() {
			try {
				SetStatusRecTo("");
			} catch (Exception e) {
				Log.d("onPreExecute", e.getMessage());
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
				synchronized (this) {
					if (!lblInfo.getText().toString().matches("")) lblInfo.setText(fprog);
				}
			} catch (Exception e) {
				Log.d("onProgressUpdate", e.getMessage());
			}
		}

	}

    //endregion

	//region WS Recepcion por tabla

	private class WSRec extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsCargaTabla();
			} catch (Exception e) {
				if (scon == 0) fstr = "No se puede conectar al web service : " + sstr;
				Log.d("onPostExecute", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
                // JP20211018
			    if (modo_recepcion==1) {
                    wsCallback();
                } else if (modo_recepcion==2) {
                    wsCallbackExist();
                } else if (modo_recepcion == 3) {
					wsCallbackPrecio();
				} else if (modo_recepcion == 4) {
			    	wsCallbackExist();
				}
			} catch (Exception e) {
				Log.d("onPostExecute", e.getMessage());
			}
		}

		@Override
		protected void onPreExecute() {  }

		@Override
		protected void onProgressUpdate(Void... values) { }

	}

	public void wsCargaTabla() {
		try {
//        	if (nombretabla.contains("P_IMPRESORA")){
//				fillTableImpresora();
//			}else{
			AddTable(nombretabla);
//			}
		} catch (Exception e) {
			String ee=e.getMessage();
		}
	}

	public void wsCallback() {
		boolean ejecutar=true;

        cargastockpv=false;

		try {
			indicetabla++;

			switch (indicetabla) {
				case -4:
					nombretabla="CARGA_SUPERVISOR";break;
				case -3:
					nombretabla="P_PARAMEXT";break;
				case -2:
					procesaParamsExt();
					nombretabla ="";
					break;
				case -1:
					nombretabla="P_RUTA";break;
				case 0:
					procesaRuta();
					nombretabla ="";
					break;
				case 1:
					nombretabla="P_EMPRESA";break;
				case 2:
					nombretabla="P_COREL";break;
				case 3:
					nombretabla="P_PRODUCTO";break;
				case 4:
					nombretabla="P_CLIENTE";break;
				case 5:
					nombretabla="P_PRODUCTO";break;
				case 6:
					nombretabla="P_PRODPRECIO";break;
				case 7:
					nombretabla="P_NIVELPRECIO";break;
				case 8:
					nombretabla="P_TIPONEG";break;
				case 9:
					nombretabla="P_CLIRUTA";break;
				case 10:
					nombretabla="P_CLIDIR";break;
				case 11:
					nombretabla="P_FACTORCONV";break;
				case 12:
					nombretabla="P_LINEA";break;
				case 13:
					nombretabla="TMP_PRECESPEC";break;
				case 14:
					nombretabla="P_DESCUENTO";break;
				case 15:
					nombretabla="P_EMPRESA";break;
				case 16:
					nombretabla="P_SUCURSAL";break;
				case 17:
					nombretabla="P_BANCO";break;
				case 18:
					nombretabla="P_STOCKINV";break;
				case 19:
					nombretabla="P_CODATEN";break;
				case 20:
					nombretabla="P_CODDEV";break;
				case 21:
					nombretabla="P_CODNOLEC";break;
				case 22:
					nombretabla="P_CORELNC";break;
				case 23:
					nombretabla="P_CORRELREC";break;
				case 24:
					nombretabla="P_CORREL_OTROS";break;
				case 25:
					nombretabla="P_STOCK_APR";break;
				case 26:
					nombretabla="P_STOCK";break;
				case 27:
					nombretabla="P_STOCKB";break;
				case 28:
					nombretabla="P_STOCK_PALLET";break;//#CKFK 20190304 10:48 Se agregó esta tabla para poder importar los pallets
				case 29:
					nombretabla="P_COBRO";break;
				case 30:
					nombretabla="P_CLIGRUPO";break;
				case 31:
					nombretabla="P_MEDIAPAGO";break;
				case 32:
					nombretabla="P_BONIF";break;
				case 33:
					nombretabla="P_BONLIST";break;
				case 34:
					nombretabla="P_PRODGRUP";break;
				case 35:
					nombretabla="P_IMPUESTO";break;
				case 36:
					nombretabla="P_VENDEDOR";break;
				case 37:
					nombretabla="P_MUNI";break;
				case 38:
					nombretabla="P_VEHICULO";break;
				case 39:
					nombretabla="P_HANDHELD";break;
				case 40:
					nombretabla="P_TRANSERROR";break;
				case 41:
					nombretabla="P_CATALOGO_PRODUCTO";break;
				case 42:
					nombretabla="P_GLOBPARAM";break;
				case 43:
					nombretabla="P_CONFIGBARRA";break;
				case 44:
					nombretabla="P_REF1";break;
				case 45:
					nombretabla="P_REF2";break;
				case 46:
					nombretabla="P_REF3";break;
				case 47:
					nombretabla="P_ARCHIVOCONF";break;
				case 48:
					nombretabla="P_ENCABEZADO_REPORTESHH";break;
				case 49:
					nombretabla="P_PORCMERMA";break;

				// Objetivos

				case 50:
					nombretabla="O_RUTA";break;
				case 51:
					nombretabla="O_COBRO";break;
				case 52:
					nombretabla="O_PROD";break;
				case 53:
					nombretabla="O_LINEA";break;

				// Mercadeo

				case 54:
					nombretabla="P_MEREQTIPO";break;
				case 55:
					nombretabla="P_MEREQUIPO";break;
				case 56:
					nombretabla="P_MERESTADO";break;
				case 57:
					nombretabla="P_MERPREGUNTA";break;
				case 58:
					nombretabla="P_MERRESP";break;
				case 59:
					nombretabla="P_MERMARCACOMP";break;
				case 60:
					nombretabla="P_MERPRODCOMP";break;
				case 61:
					nombretabla="P_PEDIDO_RECHAZADO";break;
				case 62:
					nombretabla="DS_PEDIDO";
					break;
				case 63:
					nombretabla="DS_PEDIDOD";
					break;
				case 64:
					dbld.clear();
					dbld.add("EXEC SP_GENERA_PEDIDO_SUGERIDO_POR_RUTA '" + ruta + "'");
					dbld.add("EXEC SP_ULTIMOSPRECIOS '" + ruta + "'");
					commitSQL();
					nombretabla = "";
					break;
				case 65:
					nombretabla="P_PEDSUG";break;
				case 66:
					nombretabla="P_ULTIMOPRECIO";break;
				case 67:
					nombretabla="P_IMPRESORA";break;
				case 68:
					//licResult=checkLicence(licSerial);
					nombretabla = "checkLicence";break;
				case 69:
					//licResultRuta=checkLicenceRuta(licRuta);
					nombretabla = "checkLicenceRuta";
					break;
				case 70:
					nombretabla="P_RAZON_DESP_INCOMP";
					break;
                case 71:
                    nombretabla = "P_STOCK_PV";break;
				case 72:
					nombretabla="P_CANAL";break;
                case 73:
                    nombretabla="P_CANALSUB";break;
                case 74:
                    nombretabla="P_DEPAR";break;
				case 75:
					nombretabla="P_MEDIDA";break;
				case 76:
					nombretabla="P_CIUDAD";break;
				case 77:
					nombretabla="P_TIPOLOGIA";break;
				case 78:
					nombretabla="P_MONTO_MINIMO_CLIENTE";break;

                case 79://#CKFK 20210813 Cambié esto para el final
                    nombretabla="Procesando tablas ...";break;

                case 80:
					procesaDatos();
					//#AT 20220322 Se cambia el valor de las variables
					//gl.permitir_cantidad_mayor, gl.permitir_producto_nuevo, gl.validar_posicion_georef
					setValoresRuta();

					ejecutar = false;
                    break;

			}

			if (ejecutar) executaTabla();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// JP20211018
    public void wsCallbackExist() {
        boolean ejecutar=true;

        cargastockpv=false;

        try {
            indicetabla++;

            switch (indicetabla) {
                case 1:
                	nombretabla="P_STOCKINV";break;
                case 2:
                    nombretabla="TMP_PRECESPEC";break;
                case 3:
                    nombretabla="P_PRODPRECIO";break;
                case 4:
                    nombretabla="P_STOCK";break;
                case 5:
                    nombretabla="P_STOCK_PALLET";break;
                case 6:
                    nombretabla="P_STOCKB";break;
                case 7:
                    nombretabla="P_FACTORCONV";break;
                case 8:
                    procesaDatos();
                    ejecutar = false;
                    break;
            }

            if (ejecutar) executaTabla();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // AT20211019
	public void wsCallbackPrecio() {
		boolean ejecutar=true;

		cargastockpv=false;

		try {
			indicetabla++;

			switch (indicetabla) {
				case 1:
					nombretabla = "TMP_PRECESPEC"; break;
				case 2:
					nombretabla = "P_PRODPRECIO"; break;
				case 3:
					nombretabla = "P_FACTORCONV"; break;
				case 4:
					procesaDatos();
					ejecutar = false;
					break;
			}

			if (ejecutar) executaTabla();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    //endregion

	//region WS Envio Methods

	private boolean sendData() {
		int rslv;

		errflag = false;

		if (getTest() == 0) {

			URL = URL_Remota;

			if (getTest() == 0) {
				errflag = true;
				return false;
			}
		}

		senv = "Envío terminado \n \n";

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			rslv = validaLiquidacion();
			if (rslv != 1) {
				liqid = false;
				if (rslv == 0) {
					senv = "La liquidación no está cerrada, no se puede enviar datos";
				} else {
					senv = "No se puede determinar estado de la liquidación.";
				}
				errflag = true;
				return false;
			} else {
				liqid = true;
			}
		} else {
			liqid = true;
		}

		items.clear();
		dbld.clear();
		dbld.clearlog();

		generaArchivoBck();

		dbld.clearlog();
		dbld.clear();
		senv = "";
		errflag=false;

		try {
			envioFacturas();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioCanastas();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioClienteModificados();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioDespachosNoEntregados();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioPedidos();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioNotasCredito();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioNotasDevolucion();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioCobros();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioDepositos();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envio_D_MOV();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioCli();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioAtten();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioCoord();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioSolicitud();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioRating();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			updateAcumulados();
			//if (!fstr.equals("Sync OK")) {
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			updateInventario();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			updateDespachos();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			if (!update_Corel_GrandTotal()) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioFinDia();

			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			//listaFachada();
            //envioFotos();

			dbld.savelog();
			dbld.saveArchivo(du.getActDateStr());

			//#CKFK 20190429 Saqué esto de envioFinDia para que se guarde bien el log y luego se realice el envío.
			if (!envioparcial) {

				if (getTest() == 1) {
					scon = 1;
				} else {
					URL = URL_Remota;
					if (getTest() == 1) {
						scon = 1;
					}
				}

				if (scon == 1) {
					if (commitSQL() == 1) {
						errflag = false;
						envioFotos();
						envioDocsCliente();
						return true;
					} else {
						errflag = true;
						fterr += "\n" + sstr;
						return false;
					}
				} else {
					errflag = true;
					fstr = "No se puede conectar al web service : " + sstr;
					fterr += "\n" + fstr;
					return false;
				}
			} else {
				return true;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return true;
	}

	private boolean sendMov() {
		int rslv;

		errflag = false;

		if (getTest() == 0) {

			URL = URL_Remota;

			if (getTest() == 0) {
				errflag = true;
				return false;
			}
		}

		senv = "Envío terminado \n \n";

		try {

			envio_D_MOV_Parcial();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return true;
	}

	private boolean sendPedidosParcial() {
		int rslv;

		errflag = false;

		if (getTest() == 0) {

			URL = URL_Remota;

			if (getTest() == 0) {
				errflag = true;
				return false;
			}
		}

		senv = "Envío terminado \n \n";

		try {

			envioPedidosParcial();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return true;
	}

	private boolean sendClientes() {
		int rslv;

		errflag = false;

		if (getTest() == 0) {

			URL = URL_Remota;

			if (getTest() == 0) {
				errflag = true;
				return false;
			}
		}

		senv = "Envío terminado \n \n";

		try {

			envioClientesParcial();
			if (errflag) {
				dbld.savelog();
				addlog(new Object() {}.getClass().getEnclosingMethod().getName(), fstr, "Error envío");
				return false;
			}

			envioDocsCliente();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return true;
	}

	public String sfecha(long f) {
		long vy,vm,vd;
		String s;

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"/";} else {s=s+"0"+String.valueOf(vd)+"/";}
		if (vm>9) { s=s+String.valueOf(vm)+"/";} else {s=s+"0"+String.valueOf(vm)+"/";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s;
	}

	private int validaLiquidacion() {
		Cursor DT;
		String ss;

		try {

			db.execSQL("DELETE FROM P_LIQUIDACION");

			AddTableVL("P_LIQUIDACION");

			if (listItems.size() < 2) return 1;

			sql = listItems.get(0);
			db.execSQL(sql);

			sql = listItems.get(1);
			db.execSQL(sql);

			sql = "SELECT ESTADO FROM P_LIQUIDACION";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				ss = DT.getString(0);
				if (ss.equalsIgnoreCase("Cerrada")) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return 1;
			}
		} catch (Exception e) {
			strliqid = e.getMessage();
			return -1;
		}
	}

	public void envioFacturas() {
		Cursor DT;
		String cor, fruta, tt;
		int i, pc = 0, pcc = 0, ccorel;

		fterr = "";
		err = "";

		try {

			sql = "SELECT COREL,RUTA,CORELATIVO FROM D_FACTURA WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Facturas : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();

			dbld.clear();

			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);
				ccorel = DT.getInt(2);

				dbg = "::";

				try {

					i += 1;
					fprog = "Factura " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_FACTURA", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAP", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD_MODIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURA_CONTROL_CONTINGENCIA", "WHERE COREL = '"+ cor +"'");

					dbld.insert("D_STOCKB_DEV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_BARRA", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_COREL SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "' " +
							"AND CORELULT<" + ccorel);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_FACTURA SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\nFactura : " + sstr;
							dbg = sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("facturas.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
					dbg = e.getMessage();
				}

				DT.moveToNext();
			}

			DT.close();

			if (envioparcial && (fterr.isEmpty()) && !esEnvioManual) {
				sql = "DELETE FROM D_FACTURA_BARRA";
				db.execSQL(sql);
				sql = "DELETE FROM D_STOCKB_DEV";
				db.execSQL(sql);
			}

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("facturas.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			dbg = fstr;
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Facturas : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Facturas : " + pc + "\n";
		}
		//}

	}

	public void envioCanastas() {
		Cursor DT;
		int i, pc = 0, pcc = 0, idCanasta;

		try {
			sql = "SELECT IDCANASTA FROM D_CANASTA WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Canastas : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				idCanasta = DT.getInt(0);

				try {

					i += 1;
					fprog = "Canasta " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_CANASTA", "WHERE IDCANASTA="+idCanasta);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CANASTA SET STATCOM='S' WHERE IDCANASTA="+idCanasta;
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("canastas.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("canastas.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Canastas : " + pc + " , NO ENVIADO : " + pf + " \n";
		} else {
			senv += "Canastas : " + pc + "\n";
		}
	}

	public void envioClienteModificados() {
		Cursor DT;
		int i, pc = 0, pcc = 0;
		String Corel;

		try {
			sql = "SELECT COREL FROM D_CLIENTE_MODIF WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "ClientesModificados : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				Corel = DT.getString(0);

				try {

					i += 1;
					fprog = "ClientesModificados " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_CLIENTE_MODIF", "WHERE COREL='"+Corel+"'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CLIENTE_MODIF SET STATCOM='S' WHERE COREL='"+Corel+"'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("clientesmodificados.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("clientesmodificados.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Clientesmodificados : " + pc + " , NO ENVIADO : " + pf + " \n";
		} else {
			senv += "Clientesmodificados : " + pc + "\n";
		}
	}

	public void envioDespachosNoEntregados() {
		Cursor DT;
		int i, pc = 0, pcc = 0;
		String Corel, Producto;

		try {
			sql = "SELECT COREL, PRODUCTO FROM D_DESPACHOD_NO_ENTREGADO WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Prefacturas : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				Corel = DT.getString(0);
				Producto = DT.getString(1);

				try {

					i += 1;
					fprog = "Prefactura " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_DESPACHOD_NO_ENTREGADO", "WHERE COREL= '" + Corel + "'" +
							     " AND PRODUCTO = '" + Producto + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_DESPACHOD_NO_ENTREGADO SET STATCOM='S' WHERE COREL= '" + Corel + "'" +
									" AND PRODUCTO = '" + Producto + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("despachos.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("despachos.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Prefacturas : " + pc + " , NO ENVIADO : " + pf + " \n";
		} else {
			senv += "Prefacturas : " + pc + "\n";
		}
	}

	public void envioPedidos() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;
		String Aux;

		try {
			sql = "SELECT COREL FROM D_PEDIDO WHERE (STATCOM='N') AND ((ANULADO='S') OR (CUMPLE_MONTO_MINIMO=1))";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Pedidos : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Pedido " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					if (cor.equals("P804-1_220116032118")){
						Aux =  "Es aquí";
					}
					dbld.insert("D_PEDIDO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_PEDIDOD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_PEDIDO SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							//Toast.makeText(this, "Envio correcto", Toast.LENGTH_SHORT).show();
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("pedidos.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}
				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("pedidos.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Pedidos : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Pedidos : " + pc + "\n";
		}
		//}
	}

	public void envioCobros() {
		Cursor DT;
		String cor, fruta;
		int i, pc = 0, pcc = 0, corult;

		try {
			sql = "SELECT COREL,CORELATIVO,RUTA FROM D_COBRO WHERE STATCOM='N' ORDER BY COREL";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Cobros : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				corult = DT.getInt(1);
				fruta = DT.getString(2);

				try {

					i += 1;
					fprog = "Cobro " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_COBRO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD_SR", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROP", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_CORRELREC SET Actual=" + corult + "  WHERE RUTA='" + fruta + "' " +
							"AND Actual<" + corult);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_COBRO SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\nCobro: " + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("cobros.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("cobros.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Cobros : " + pc + " , NO ENVIADO : " + pf + " \n";
		} else {
			senv += "Cobros : " + pc + "\n";
		}
		//}
	}

	public void envioNotasCredito() {
		Cursor DT;
		String cor, fruta = "", tipodocumento, tipoDGIR = "", tipoDGISR = "", query = "";
		int i, pc = 0, pcc = 0, ccorel;

		try {

			sql = "SELECT COREL,RUTA,CORELATIVO, TIPO_DOCUMENTO FROM D_NOTACRED WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas crédito : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);
				ccorel = DT.getInt(2);
				tipodocumento = DT.getString(3);

				//Tipo de documento (04:Nota de Crédito  referente a facturas, 06:Nota de crédito genérica)
				//(05:Nota de debito  referente a facturas, 07:Nota de debito genérica )
				if	(tipodocumento.equals("ND")) {
					tipoDGISR = "05";
					tipoDGIR ="07";
				} else if (tipodocumento.equals("NC")) {
					tipoDGISR = "06";
					tipoDGIR = "04";
				}

				try {

					i += 1;
					fprog = "Nota crédito " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_NOTACRED", "WHERE COREL='" + cor + "' AND TIPO_DOCUMENTO = '"+tipodocumento+"'");
					dbld.insert("D_NOTACREDD", "WHERE COREL='" + cor + "' AND TIPO_DOCUMENTO = '"+tipodocumento+"'");

					query = "(TIPODOCUMENTO = '" + tipoDGISR + "' OR TIPODOCUMENTO = '" + tipoDGIR + "')";
					dbld.insert("D_FACTURA_CONTROL_CONTINGENCIA", "WHERE COREL = '"+ cor +"' AND "+query+"");

					//dbld.add("UPDATE P_CORELNC SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "'");
					if	(tipodocumento.equals("NC")) {
						dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'NC' " +
								"AND ACTUAL<" + ccorel);
					} else if (tipodocumento.equals("ND")) {
						dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'ND' " +
								"AND ACTUAL <" + ccorel);
					}

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_NOTACRED SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("notascredito.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("notascredito.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Notas crédito : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Notas crédito : " + pc + "\n";
		}
		//}
	}

	public void envioNotasDevolucion() {
		Cursor DT;
		String cor, fruta = "", serie = "";
		int i, pc = 0, pcc = 0, ccorel = 0;

		try {

			sql = "SELECT COREL,RUTA FROM D_CXC WHERE STATCOM='N' ORDER BY COREL";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas de devolución : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);

				try {
					i += 1;
					fprog = "Nota de devolución " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_CXC", "WHERE COREL='" + cor + "'");
					dbld.insert("D_CXCD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CXC SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("notasdevolucion.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			sql = "SELECT ACTUAL, SERIE FROM P_CORREL_OTROS WHERE TIPO = 'D'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				ccorel = DT.getInt(0);
				serie = DT.getString(1);

				//dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND SERIE = '"+serie +"' AND TIPO = 'D' " +
				//		" AND ACTUAL =< "+ccorel+1);

				dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'D' " +
						" AND ACTUAL < " + ccorel);
				//updCxC="UPDATE P_CORREL_OTROS SET ACTUAL="+ccorel+ "  WHERE RUTA='"+fruta+"' AND TIPO='D' " ;

				//ss = "exec actualizaCorelDev '" + gl.ruta + "'," + ccorel;
				//dbld.add(ss);

			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("notasdevolucion.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Notas devolución : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Notas devolución : " + pc + "\n";
		}
		//}
	}

	public void envioDepositos() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			sql = "SELECT COREL FROM D_DEPOS WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Depósitos : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Depósito " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_DEPOS", "WHERE COREL='" + cor + "'");
					dbld.insert("D_DEPOSD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_DEPOS SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("depositos.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("depositos.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Depósitos : " + pc + " , NO ENVIADO : " + pf + " \n";
		} else {
			senv += "Depósitos : " + pc + "\n";
		}
		//}
	}

	public String Get_Corel_D_Mov() {
		Cursor DT;
		String cor = "";

		try {

			sql = "SELECT COREL FROM D_MOV WHERE (TIPO='D') ORDER BY COREL DESC ";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				cor = DT.getString(0);
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox(e.getMessage());
		}
		return cor;
	}

	public void envio_D_MOV() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			sql = "SELECT COREL FROM D_MOV WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Inventario : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();

			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Inventario " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.add("IF NOT EXISTS(SELECT COREL FROM D_MOV WHERE COREL = '" + cor + "')  " +
							" BEGIN ");
					dbld.insert("D_MOV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDB", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDCAN", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDPALLET", "WHERE COREL='" + cor + "'");

					//#CKFK 20190412 Corregido error
					dbld.add("INSERT INTO P_DEVOLUCIONES_SAP " +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,'', D.LOTE, 'N', GETDATE(), D.CANT, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVD D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'" +
							" UNION" +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,D.BARRA, '', 'N', GETDATE(), 1, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVDB D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'");
					dbld.add(" END ");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							pc += 1;

						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("inventario.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("inventarios.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Inventario : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Inventario : " + pc + "\n";
			}
		}
	}

	public void envio_D_MOV_Parcial() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			senv = "Envío terminado \n \n";

			sql = "SELECT COREL FROM D_MOV WHERE STATCOM='N' AND ANULADO = 'N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Inventario : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();

			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Inventario " + i;

					dbld.clear();
					dbld.add("IF NOT EXISTS(SELECT COREL FROM D_MOV WHERE COREL = '" + cor + "')  " +
							" BEGIN ");
					dbld.insert("D_MOV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVD", "WHERE COREL='" + cor + "' ");
					dbld.insert("D_MOVDB", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDCAN", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDPALLET", "WHERE COREL='" + cor + "'");

					//#CKFK 20190412 Corregido error
					dbld.add("INSERT INTO P_DEVOLUCIONES_SAP " +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,'', D.LOTE, 'N', GETDATE(), D.CANT, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVD D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'" +
							" UNION" +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,D.BARRA, '', 'N', GETDATE(), 1, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVDB D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'");
					dbld.add(" END ");

					if (commitSQL() == 1) {
							sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							pc += 1;

					} else {
						errflag = true;
						fterr += "\n" + sstr;
					}

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("inventario.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("inventarios.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Inventario: " + pc + " , NO ENVIADO : " + pf + ", se enviará en el fin de día \n";
		} else {
			senv += "Inventario: " + pc + "\n";
		}

/*		mu.toast(senv);
		finish();*/
	}

	public void envioPedidosParcial() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			sql = "SELECT COREL FROM D_PEDIDO WHERE (STATCOM='N') AND ((ANULADO='S') OR (CUMPLE_MONTO_MINIMO=1))";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Pedidos : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Pedido " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					dbld.clear();

					dbld.insert("D_PEDIDO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_PEDIDOD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");

					if (commitSQL() == 1) {
						sql = "UPDATE D_PEDIDO SET STATCOM='S' WHERE COREL='" + cor + "'";
						db.execSQL(sql);
						//Toast.makeText(this, "Envio correcto", Toast.LENGTH_SHORT).show();
						pc += 1;
					} else {
						errflag = true;
						fterr += "\n" + sstr;
					}

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("pedidos.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}
				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("pedidos.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Pedidos : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Pedidos : " + pc + "\n";
		}
		//}
	}

	public void envioClientesParcial() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			if (gl.peModal.equalsIgnoreCase("TOL")) {
				sql = "SELECT CODIGO FROM D_CLINUEVOT WHERE STATCOM ='N'";
			} else {
				sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
			}
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Clientes : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Cliente " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					dbld.clear();

					if (gl.peModal.equalsIgnoreCase("TOL")) {
						dbld.insert("D_CLINUEVOT", "WHERE CODIGO='" + cor + "'");
					} else {
						dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");
					}

					if (commitSQL() == 1) {
						if (gl.peModal.equalsIgnoreCase("TOL")) {
							sql = "UPDATE D_CLINUEVOT SET STATCOM='S' WHERE CODIGO='" + cor + "'";
						} else {
							sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
						}

						db.execSQL(sql);
						//Toast.makeText(this, "Envio correcto", Toast.LENGTH_SHORT).show();
						pc += 1;
					} else {
						errflag = true;
						fterr += "\n" + sstr;
					}

				} catch (Exception e) {
					errflag = true;
					dbld.savelog("clientes.txt");
					addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}
				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("Clientes.txt");

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Clientes : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Clientes : " + pc + "\n";
		}
	}

	//#AT20220426 Envió de documentos por cliente
	public void envioDocsCliente() {
		Cursor DT;
		String resultado ="", xr="";
		try {

			sql = "SELECT * FROM D_CLINUEVOT_IMAGEN WHERE STATCOM = 'N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();

				while (!DT.isAfterLast()) {
					int codigoImg = DT.getInt(0);
					clienteImg = DT.getString(1);
					rutaImg = DT.getString(2);
					fechaImg = DT.getString(3);
					statcomImg = DT.getString(5);

					path = Environment.getExternalStorageDirectory() + "/RoadFotos/clidocs/" + codigoImg+ ".jpg";

					Bitmap bmp = BitmapFactory.decodeFile(path);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG,75, out);
					byte[] imagebyte = out.toByteArray();
					String strBase64 = Base64.encodeBytes(imagebyte);

					callMethod("GuardaDocumentosCliente",
							"cliente", clienteImg,
								"ruta", rutaImg,
								"fecha", fechaImg,
								"statcom", statcomImg,
								"img",strBase64);
					xr = getXMLRegionSingle("GuardaDocumentosClienteResult");
					resultado = (String) getSingle(xr, "GuardaDocumentosClienteResult", String.class);

					if (resultado.contains("#")) {
						sql = "UPDATE D_CLINUEVOT_IMAGEN SET STATCOM ='S' WHERE CODIMAGEN = '"+codigoImg+"'";
						db.execSQL(sql);
					}

					DT.moveToNext();
				}
			}

		} catch (Exception e) {
			mu.msgbox("EnvioDocumentos: "+ e.getMessage());
		}
	}

	public void envioCli() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			if (gl.peModal.equalsIgnoreCase("TOL")) {
				sql = "SELECT CODIGO FROM D_CLINUEVOT WHERE STATCOM ='N'";
			} else {
				sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
			}
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Cliente Nuevo : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Cliente Nuevo " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) {
						dbld.clear();
					}

					//dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");
					if (gl.peModal.equalsIgnoreCase("APR")) {
						dbld.insert("D_CLINUEVO_APR", "WHERE CODIGO='" + cor + "'");
					} else if(gl.peModal.equalsIgnoreCase("TOL")) {
						dbld.insert("D_CLINUEVOT", "WHERE CODIGO ='"+cor+"'");
					} else {
						dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");
					}

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {

							//sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
							//db.execSQL(sql);
							if (gl.peModal.equalsIgnoreCase("APR")) {
								sql = "UPDATE D_CLINUEVO_APR SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							} else if (gl.peModal.equalsIgnoreCase("TOL")){
								sql = "UPDATE D_CLINUEVOT SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							} else {
								sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							}

							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Cli. nuevos : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Cli. nuevos : " + pc + "\n";
			}
		}
	}

	public void envioAtten() {
		Cursor DT;
		String cor, hora;
		long fecha;

		fprog = " ";
		if (!esEnvioManual) {
			wsStask.onProgressUpdate();
		}

		try {
			sql = "SELECT RUTA,FECHA,HORALLEG FROM D_ATENCION WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fecha = DT.getLong(1);
				hora = DT.getString(2);

				try {

					if (envioparcial) dbld.clear();

					dbld.insert("D_ATENCION", "WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_ATENCION SET STATCOM='S' WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ";
							db.execSQL(sql);
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void envioCoord() {
		Cursor DT;
		String cod, ss;
		int stp;
		double px, py;
		fprog = " ";
		if (!esEnvioManual) {
			wsStask.onProgressUpdate();
		}

		try {
			sql = "SELECT CODIGO,COORX,COORY,STAMP FROM D_CLICOORD WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod = DT.getString(0);
				px = DT.getDouble(1);
				py = DT.getDouble(2);
				stp = DT.getInt(3);

				try {

					if (envioparcial) dbld.clear();

					ss = "UPDATE P_CLIENTE SET COORX=" + px + ",COORY=" + py + " WHERE (CODIGO='" + cod + "')";
					dbld.add(ss);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CLICOORD SET STATCOM='S' WHERE (CODIGO='" + cod + "') AND (STAMP=" + stp + ") ";
							db.execSQL(sql);
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void envioSolicitud() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			sql = "SELECT * FROM D_SOLICINVD";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			sql = "SELECT COREL FROM D_SOLICINV WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Solicitud " + i;
					if (!esEnvioManual) {
						wsStask.onProgressUpdate();
					}

					if (envioparcial) dbld.clear();

					dbld.insert("D_SOLICINV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_SOLICINVD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_SOLICINV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Solicitud : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Solicitud : " + pc + "\n";
			}
		}
	}

	public void envioFinDia() {

		int pc = 0, pcc = 3;

		fprog = " ";
		if (!esEnvioManual) {
			wsStask.onProgressUpdate();
		}

		try {

			pc = 0;

			if (envioparcial) dbld.clear();

			ss = "UPDATE P_RUTA SET IDIMPRESORA='" + parImprID + "',NUMVERSION='" + gl.parNumVer + "',ARQUITECTURA='ANDR' WHERE CODIGO='" + gl.ruta + "'";
			dbld.add(ss);

			dbld.add("DELETE FROM D_REPFINDIA WHERE RUTA='" + gl.ruta + "'");
			dbld.insert("D_REPFINDIA", "WHERE (LINEA>=0)");

			String vFechaFD = "";
			vFechaFD = String.valueOf(dbld.fechaFactTol(du.getActDate())).substring(0,6);

			dbld.add("IF NOT EXISTS(SELECT RUTA FROM D_FINDIA WHERE RUTA = '" + gl.ruta + "' " +
					"AND fecha = '" + vFechaFD + "')  " +
					" BEGIN ");
			dbld.add("INSERT INTO D_FINDIA (RUTA, FECHA, FINDIA)" +
					" VALUES ('" + gl.ruta + "','" + vFechaFD + "',1)");
			dbld.add(" END ");

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 1) {
					pc = 3;
				} else {
					errflag = true;
					fterr += "\nFinDia : " + sstr;
					dbg = sstr;
				}
			} else pc = 3;

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Envío Fin día : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Envío Fin día : " + pc + "\n";
		}
	}

	//#CKFK 20190522 Función creada para enviar los rating
	public void envioRating() {
		Cursor DT;
		String ruta, vendedor, comentario, fecha;
		int id, idtranserror;
		float rating;

		if (!esEnvioManual) {
			wsStask.onProgressUpdate();
		}

		try {
			sql = " SELECT IDRATING, RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM " +
					" FROM D_RATING WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				id = DT.getInt(0);
				ruta = DT.getString(1);
				vendedor = DT.getString(2);
				rating = DT.getFloat(3);
				comentario = DT.getString(4);
				idtranserror = DT.getInt(5);
				fecha = DT.getString(6);

				try {

					if (envioparcial) dbld.clear();

					ss = "INSERT INTO D_RATING (RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM)" +
							" VALUES('" + ruta + "','" + vendedor + "'," + rating + ",'" + comentario + "'," +
							"" + idtranserror + ",'" + fecha + "','N')";
					dbld.add(ss);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_RATING SET STATCOM='S' WHERE IDRATING='" + id + "'";
							db.execSQL(sql);
						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void updateInventario() {
		DU = new DateUtils();
		String sFecha;
		int rslt;
		long vfecha = clsAppM.fechaFactTol(du.getActDate());
		sFecha = DU.univfechasql(vfecha);
		String corel_d_mov = Get_Corel_D_Mov();

		try {

			if (envioparcial) dbld.clear();

			ss = " UPDATE P_STOCK SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND (FECHA ='" + sFecha + "') AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' )";
			dbld.add(ss);

			ss = " UPDATE P_STOCKB SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "')";
			dbld.add(ss);

			ss = " UPDATE P_STOCK_PALLET SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "')";
			dbld.add(ss);

			ss = " UPDATE P_STOCK_PV SET ENVIADO = 1 " +
				 " WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' AND ENVIADO = 0 " +
				 " AND DOCUMENTO IN (SELECT DISTINCT DOCUMENTO FROM P_STOCK_PV WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "')";
			dbld.add(ss);

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 0) {
					fterr += "\n" + sstr;
					errflag = true;
				}
			}

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			//fterr=fterr+fstr;
		}

	}

	public void updateDespachos() {
		DU = new DateUtils();
		String sFecha;
		int rslt;
		long vfecha = clsAppM.fechaFactTol(du.getActDate());
		sFecha = DU.univfechasql(vfecha);
		String corel_d_mov = Get_Corel_D_Mov();

		try {

			if (envioparcial) dbld.clear();

			ss = " UPDATE DS_PEDIDO SET STATCOM = 'S' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND (FECHA ='" + sFecha + "') AND STATCOM = 'N' " ;
			dbld.add(ss);

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 0) {
					fterr += "\n" + sstr;
					errflag = true;
				}
			}

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			//fterr=fterr+fstr;
		}

	}

	public void updateAcumulados() {
		long ff;
		long oyear, omonth, rslt;

		ff = du.getActDate();
		oyear = du.getyear(ff);
		omonth = du.getmonth(ff);

		try {

			if (envioparcial) dbld.clear();

			ss = "exec AcumuladoObjetivos '" + gl.ruta + "'," + oyear + "," + omonth;
			dbld.add(ss);

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 0) {
					fterr += "\n" + sstr;
					errflag = true;
				}
			}

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			fterr = fterr + fstr;
		}
	}

	public void updateLicence() {
		String SQL;
		String TN = "LIC_CLIENTE";

		try {

			fprog = TN;
			idbg = TN;
			listItems.clear();


			SQL = "SELECT * FROM LIC_CLIENTE WHERE ID='" + mac + "'";
			if (fillTable(SQL, "DELETE FROM LIC_CLIENTE") == 1) {
				idbg = idbg + SQL + "#" + "PASS OK";
			} else {
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = sstr;
			}
			idbg = idbg + " :: " + listItems.size();
		} catch (Exception e) {
			fstr = "Tabla:" + TN + ", " + e.getMessage();
			idbg = idbg + " " + e.getMessage();
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, fstr);
		}
	}

	public void addItem(String nombre, int env, int pend) {
		clsClasses.clsEnvio item;

		try {
			item = clsCls.new clsEnvio();

			item.Nombre = nombre;
			item.env = env;
			item.pend = pend;

			items.add(item);

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}
	}

	public void updateLicencePush() {
		String ss;

		try {
			ss = listItems.get(1);
			if (mu.emptystr(ss)) return;
			db.execSQL(ss);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			//msgbox(e.getMessage());
		}
	}

    public boolean envioFotos() {
        String trid,fname;
        int pp;

        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/clinue");

            for (File imagen : dir.listFiles()) {

                try {
                    trid=imagen.getName();pp=trid.indexOf(".");
                    trid=trid.substring(0,pp);
                    fname=imagen.getAbsolutePath();

                    if (sendFoto(trid,ruta,fname, gl.peModal)==1) imagen.delete();
                } catch (Exception e) {
                }
            }

            return true;
        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }
        return false;
    }

    public int sendFoto(String codigo,String ruta,String fname, String modal) {
        String resstr="";

        METHOD_NAME = "saveImageCN";

        try {

            Bitmap bmp = BitmapFactory.decodeFile(fname);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG,75, out);
            byte[] imagebyte = out.toByteArray();
            String strBase64 = Base64.encodeBytes(imagebyte);

            int iv1=strBase64.length();

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("codigo");
            param.setValue(codigo);
            request.addProperty(param);

            PropertyInfo param2 = new PropertyInfo();
            param2.setType(String.class);
            param2.setName("ruta");
            param2.setValue(ruta);
            request.addProperty(param2);

            PropertyInfo param3 = new PropertyInfo();
            param3.setType(String.class);
            param3.setName("imgdata");
            param3.setValue(strBase64);
            request.addProperty(param3);

			PropertyInfo param4 = new PropertyInfo();
			param4.setType(String.class);
			param4.setName("modal");
			param4.setValue(modal);
			request.addProperty(param4);

            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            resstr = response.toString();

            if (resstr.equalsIgnoreCase("#")) {
                return 1;
            } else {
                throw new Exception(resstr);
            }
        } catch (Exception e) {
            String ss= e.getMessage();
            //addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage());
        }

        return 0;
    }

    /*
    //#HS_20181219 funcion para crear JSON de fotos fachada.
	public void listaFachada() {

		Cursor DT;
		String codigo, imagen64, strImagen;
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		JSONArray json_Array = new JSONArray();

		System.setProperty("line.separator", "\r\n");

		try {
			sql = "SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA = '" + gl.ruta + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				while (!DT.isAfterLast()) {

					codigo = DT.getString(0);

					String paht = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + codigo + ".jpg");
					File archivo = new File(paht);

					if (archivo.exists()) {


						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						Bitmap bitmap = BitmapFactory.decodeFile(paht);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						byte[] imageBytes = baos.toByteArray();
						imagen64 = Base64.encodeToString(imageBytes, Base64.NO_PADDING);

						json = new JSONObject();
						json.put("CODIGO", codigo);
						json.put("IMAGEN", imagen64);
						json_Array.put(json);

					}

					DT.moveToNext();
				}

				if (DT != null) DT.close();

				json2.put("P_CLIENTE_FACHADA", json_Array);

			}

			jsonWS = json2.toString();

			//#HS_20181221 Se envian las fotos.
			if (envioFachada() == 1) {
				String paht = (Environment.getExternalStorageDirectory() + "/RoadFotos");
				File archivo = new File(paht);
				EliminarArchivos(archivo);
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			mu.msgbox("listaFachada: " + e.getMessage());
		}
	}
    */

	//#HS_20181221 Elimina las fotos de ROADFOTOS
	public void EliminarArchivos(File ArchivoDirectorio) {
		try {
			if (ArchivoDirectorio.isDirectory()) {
				for (File hijo : ArchivoDirectorio.listFiles())
					EliminarArchivos(hijo);
			} else {
				ArchivoDirectorio.delete();

			}
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	//endregion

	//region WS Envio Handling Methods

	public void wsSendExecute() {

		running = 1;
		scon = 0;
		fstr = "Envio incompleto  ";
		errflag = false;

		try {

			msgbox("Envio de pedidos parciales " + gl.enviaPedidosParcial);

			if (getTest() == 1) {
				scon = 1;
			} else {
				URL = URL_Remota;
				if (getTest() == 1) scon = 1;
			}

			if (scon == 1) {
				fstr = "Sync OK";
				if (gl.enviaMov){
					if (!sendMov()) {
						fstr = "Envio incompleto : " + sstr;
					}
				}else if (gl.enviaPedidosParcial){
					if (!sendPedidosParcial()) {
						fstr = "Envio incompleto : " + sstr;
					}
				} else if (gl.enviaClientes) {
					if (!sendClientes()) {
						fstr = "Envio incompleto : " + sstr;
					}
				}else{
					if (!sendData()) {
						fstr = "Envio incompleto : " + sstr;
					}
				}

			} else {
				fstr = "No se puede conectar al web service : " + sstr;
			}

		} catch (Exception e) {
			scon = 0;
			fstr = "No se puede conectar al web service. " + e.getMessage() + " " + fterr;
			Log.d("E", fstr + sstr + " " + fterr);
		}
	}

	public void wsSendFinished() {

		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);

		if(!gl.enviaMov && !gl.enviaPedidosParcial && !gl.enviaClientes){
			lblEnv.setVisibility(View.VISIBLE);
			imgEnv.setVisibility(View.VISIBLE);

			running = 0;

			//senv="Envio completo\n";

			try {
				if (scon == 0) {
					lblInfo.setText(fstr);
					writeErrLog(fstr);
					mu.msgbox(fstr);
					lblInfo.setText(fstr);
					isbusy = 0;
					barInfo.setVisibility(View.INVISIBLE);
					addlog("Envío", fterr + " " + fstr, esql);
					return;
				}

				if (!errflag) {
					lblInfo.setText(" ");

					if (!envioparcial) {

						claseFindia.updateComunicacion(2);
						claseFindia.updateFinDia(du.getActDate());

						findiaactivo = gl.findiaactivo;
						if (ultimoCierreFecha() == du.getActDate()) findiaactivo = true;

						if (findiaactivo) {
							ActualizaStatcom();
							claseFindia.eliminarTablasD();
						}
					}

					fstr = "Envio completo ";
					msgResultEnvio(senv);

				} else {
					lblInfo.setText(fstr);
					isbusy = 0;
					barInfo.setVisibility(View.INVISIBLE);
					visibilidadBotones();
					mu.msgbox("Ocurrió error : \n" + fstr);
					addlog("Envío", fstr, esql);
					return;
				}

				if (envioparcial) {

					findiaactivo = gl.findiaactivo;
					if (ultimoCierreFecha() == du.getActDate()) findiaactivo = true;

					if (findiaactivo) {
						FinDia();
						claseFindia.eliminarTablasD();
					}

				}

				visibilidadBotones();
				//if (!errflag) ComWS.super.finish();

				isbusy = 0;

			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}

		}else{

			relPedidos.setVisibility(View.VISIBLE);
			relClientes.setVisibility(View.VISIBLE);

			if (scon == 0) {

				lblInfo.setText(fstr);
				writeErrLog(fstr);
				mu.msgbox("Ocurrió error con la comunicación al Web Service, \n" +
						"el protocolo de seguridad no permitió el acceso: \n" + fstr);
				lblInfo.setText(fstr);
				isbusy = 0;
				barInfo.setVisibility(View.INVISIBLE);
				addlog("Envío", fterr + " " + fstr, esql);
				return;
			}

			if (!errflag){

				if (gl.enviaPedidosParcial) {
					gl.enviaPedidosParcial = false;
				}

				if (gl.enviaClientes) {
					gl.enviaClientes = false;
				}

				if (gl.enviaMov) gl.enviaMov = false;

				fstr = "Envio completo ";
				msgResultEnvioParcial(senv);
				return;

			} else {

				lblInfo.setText(fstr);
				isbusy = 0;

				barInfo.setVisibility(View.INVISIBLE);
				if (scon == 1) {
					mu.msgbox("Ocurrió error con la comunicación al Web Service, \n" +
							"si se pudo establecer conexión al servidor pero  \n" +
							"el protocolo de seguridad no permitió el acceso: \n" + fstr);
				} else if (scon == 1) {
					mu.msgbox("Ocurrió error no se pudo establecer conexión al servidor \n" + fstr);
			    }

				addlog("Envío", fstr, esql);
				return;

			}

		}

	}

	private class AsyncCallSend extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				//#AT 20211124 Se ejecuta si es necesario crear una cola de mensajes
				if (!gl.enviaMov && !gl.enviaPedidosParcial && !gl.enviaClientes){
					if (Looper.myLooper() == null)
					{
						Looper.prepare();
					}
					//Looper.prepare();
				}
				//Looper.prepare();
				wsSendExecute();
			} catch (Exception e) {
				if (scon == 0) {
					fstr = "No se puede conectar al web service : " + sstr;
					//lblInfo.setText(fstr);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				wsSendFinished();
				Looper.loop();
				/*if (gl.enviaMov || gl.enviaPedidosParcial){
					Looper.loop();
				}*/
			} catch (Exception e) {
				Log.d("onPostExecute", e.getMessage());
			}
		}

		@Override
		protected void onPreExecute() {
			try {
			} catch (Exception e) {
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
				lblInfo.setText(fprog);
			} catch (Exception e) {
			}
		}

	}

	//endregion

	//region WS Confirm Methods

	private void sendConfirm() {
		Cursor dt;

		try {
			try {

				listDocs.clear();
				sql = "SELECT DISTINCT DOCUMENTO FROM P_STOCK " +
					  "UNION " +
					  "SELECT DISTINCT DOCUMENTO FROM P_STOCKB ";
				dt = Con.OpenDT(sql);

				if (dt!=null){
					if (dt.getCount() > 0) {
						dt.moveToFirst();
						for (int i = 0; i < dt.getCount(); i++) {
							docstock = dt.getString(0);
							listDocs.add(docstock);
							dt.moveToNext();
						}
					}
				}

				if (dt != null) dt.close();

			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				msgbox(new Object() {
				}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			}

			//#HS_20181126_1603 Cambie el getActDate por getFechaActual
			sql = "UPDATE P_RUTA SET EMAIL='" + du.getActDate() + "'";
			db.execSQL(sql);

			Handler mtimer = new Handler();
			Runnable mrunner = new Runnable() {
				@Override
				public void run() {
					showprogress = false;
					wsCtask = new AsyncCallConfirm();
					wsCtask.execute();
				}
			};
			mtimer.postDelayed(mrunner, 500);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	//endregion

	//region WS Confirm Handling Methods

	public void wsConfirmExecute() {
		String univdate = du.univfecha(du.getActDate());
		isbusy = 1;

		try {
			conflag = 0;

			dbld.clear();

			if (listDocs.size()>0){
				for (int i = 0; i < listDocs.size(); i++) {
					docstock = listDocs.get(i);
					dbld.add("DELETE FROM P_DOC_ENVIADOS_HH WHERE DOCUMENTO='" + docstock + "'");
					dbld.add("INSERT INTO P_DOC_ENVIADOS_HH VALUES ('" + docstock + "','" + ActRuta + "','" + univdate + "',1)");
				}
			}

			dbld.add("UPDATE P_RUTA SET IDIMPRESORA='" + parImprID + "',NUMVERSION='" + gl.parNumVer + "',ARQUITECTURA='ANDR', FECHAVERSION='" + gl.parFechaVer + "' WHERE CODIGO='" + gl.ruta + "'");
			dbld.add("INSERT INTO P_BITACORA_VERSIONHH (RUTA,FECHA,NUMVERSION,ARQUITECTURA) " +
					"VALUES('" + gl.ruta + "','" + du.univfechaseg() + "','" + gl.parNumVer + "','ANDR')");

			if (commitSQL() == 1){
				conflag = 1;
			}else {
				conflag = 0;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fterr += "\n" + e.getMessage();
			dbg = e.getMessage();
		}
	}

	public void wsConfirmFinished() {
		try {
			isbusy = 0;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	private class AsyncCallConfirm extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsConfirmExecute();
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				wsConfirmFinished();
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}

		}

		@Override
		protected void onPreExecute() {
			try {
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
			} catch (Exception e) {
			}
		}

	}

	//endregion

	//region Aux

	public void comManual(View view) {
		try {
			Intent intent = new Intent(this, ComDrop.class);
			startActivity(intent);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void getWSURL() {
		Cursor DT;
		String wsurl;

		if (!gl.debug) {
			txtRuta.setText(ruta);
			txtEmp.setText(gEmpresa);
		}

		try {

			sql = "SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='" + ruta + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();

				URL = DT.getString(0);
				URL_Remota = DT.getString(1);

				gl.URLtemp = URL;

				if (!URL.isEmpty()) {
					txtWS.setText(URL);
				} else if (!URL_Remota.isEmpty()) {
					txtWS.setText(URL);
				} else {
					toast("No hay configurada URL para transferencia de datos");
				}

			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			URL = "*";
			txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");

		}

	}

	private boolean setComParams() {
		String ss;

		ss = txtRuta.getText().toString().trim();

		try {
			if (mu.emptystr(ss)) {
				mu.msgbox("La ruta no esta definida.");
				return false;
			}
			ActRuta = ss;

			ss = txtEmp.getText().toString().trim();
			if (mu.emptystr(ss)) {
				mu.msgbox("La empresa no esta definida.");
				return false;
			}
			gEmpresa = ss;

			ss = txtWS.getText().toString().trim();
			//ss="http://192.168.1.142/wsAndr/wsandr.asmx";
			if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
				mu.msgbox("La dirección de Web service no esta definida.");
				return false;
			}
			URL = ss;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		return true;
	}

	private int getDocCount(String ss, String pps) {

		Cursor DT;
		int cnt = 0;
		String st;

		try {

			sql = ss;
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				cnt = DT.getInt(0);
			}

			if (DT != null) DT.close();

			if (cnt > 0) {
				st = pps + " " + cnt;
				sp = sp + st + ", ";
			}

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox(sql+"\n"+e.getMessage());
		}

		return cnt;

	}

	private String getMac() {
		WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}

	private int ultCorel() {
		Cursor DT;
		int crl = 0;

		try {
			sql = "SELECT CORELULT FROM P_COREL";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				crl = DT.getInt(0);
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return crl;
	}

	//#HS_20181129_1006 Agregue funcion para obtener la serie.
	private String ultSerie() {
		Cursor DT;
		String serie = "";

		try {
			sql = "SELECT SERIE FROM P_COREL";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				serie = DT.getString(0);
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox("ultSerie(): " + e.getMessage());
		}

		return serie;
	}

	//#HS_20181121_1048 Se creo la funcion Get_Fecha_Inventario().
	private long Get_Fecha_Inventario() {
		Cursor DT;
		long fecha = 0;

		try {

			sql = "SELECT IFNULL(EMAIL,0) AS FECHA FROM P_RUTA";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				fecha = DT.getLong(0);
				if (fecha == 0) {
					fecha = 1001010000;//#HS_20181129_0945 Cambie los valores de fecha porque deben se yymmdd hhmm
				}
			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}
		return fecha;
	}

	private long ultimoCierreFecha() {
		Cursor DT;
		long rslt = 0;

		try {
			sql = "SELECT val1 FROM FinDia";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();
			rslt = DT.getLong(0);

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			rslt = 0;
		}

		return rslt;
	}

	private void visibilidadBotones() {
		Cursor dt;

		boolean recep = false;

		esvacio = false;

		try {
			try {
				sql = "SELECT * FROM P_RUTA";
				dt = Con.OpenDT(sql);
				esvacio = dt.getCount() == 0;
			} catch (Exception e) {
				//msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
				esvacio = true;
			}

			//Inicializa estos layout en invisible
			relExist.setVisibility(View.INVISIBLE);
			relPrecio.setVisibility(View.INVISIBLE);
			relStock.setVisibility(View.INVISIBLE);

			//Si entra en modo administración, habilita los botones y se va
			if (gl.modoadmin || gl.debug) {

				txtRuta.setEnabled(true);
				txtWS.setEnabled(true);
				txtEmp.setEnabled(true);

				if (esvacio) {
					lblEnv.setVisibility(View.INVISIBLE);
					imgEnv.setVisibility(View.INVISIBLE);
					lblEnvM.setVisibility(View.INVISIBLE);
					imgEnvM.setVisibility(View.INVISIBLE);
					lblRec.setVisibility(View.VISIBLE);
					imgRec.setVisibility(View.VISIBLE);
				}

				return;
			}

			//#HS_20181121_0910 Se creó la funcion Get_Fecha_Inventario().
			if (!esvacio) {
				long fc = Get_Fecha_Inventario();
				recep = fc == du.getActDate();
			}

			//Invisible botón y texto de envío
			lblEnv.setVisibility(View.INVISIBLE);
			imgEnv.setVisibility(View.INVISIBLE);

			//Invisible botón y texto de envío manual
			lblEnvM.setVisibility(View.INVISIBLE);
			imgEnvM.setVisibility(View.INVISIBLE);

			//Invisible botón y texto de recepción
			lblRec.setVisibility(View.INVISIBLE);
			imgRec.setVisibility(View.INVISIBLE);

			//Tiene documentos
			boolean TieneFact, TienePedidos, TieneCobros, TieneDevol, YaComunico, TieneInventario,
					TieneCanastas, TieneOtros, TieneAtenciones;

			if (!envioparcial) {
				TieneFact = (clsAppM.getDocCountTipo("Facturas", false) > 0 ? true : false);
				TienePedidos = (clsAppM.getDocCountTipo("Pedidos", false) > 0 ? true : false);
				TieneCobros = (clsAppM.getDocCountTipo("Cobros", false) > 0 ? true : false);
				TieneDevol = (clsAppM.getDocCountTipo("Devoluciones", false) > 0 ? true : false);
				YaComunico = (claseFindia.getComunicacion() == 2 ? true : false);
				TieneInventario = (clsAppM.getDocCountTipo("Inventario", false) > 0 ? true : false);
				TieneCanastas = (clsAppM.getDocCountTipo("Canastas", false) > 0 ? true : false);
				TieneAtenciones = (clsAppM.getDocCountTipo("Atenciones", false) > 0 ? true : false);
			} else {
				TieneFact = (clsAppM.getDocCountTipo("Facturas", true) > 0 ? true : false);
				TienePedidos = (clsAppM.getDocCountTipo("Pedidos", true) > 0 ? true : false);
				TieneCobros = (clsAppM.getDocCountTipo("Cobros", true) > 0 ? true : false);
				TieneDevol = (clsAppM.getDocCountTipo("Devoluciones", true) > 0 ? true : false);
				YaComunico = (claseFindia.getComunicacion() == 2 ? true : false);
				TieneInventario = (clsAppM.getDocCountTipo("Inventario", true) > 0 ? true : false);
				TieneCanastas = (clsAppM.getDocCountTipo("Canastas", true) > 0 ? true : false);
				TieneAtenciones = (clsAppM.getDocCountTipo("Atenciones", true) > 0 ? true : false);
			}

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				if (claseFindia.yaHizoFindeDia()) {
					if (YaComunico) {
						if ((rutatipo.equalsIgnoreCase("V") && !TieneInventario) || (!rutatipo.equalsIgnoreCase("V"))) {
							lblRec.setVisibility(View.VISIBLE);
							imgRec.setVisibility(View.VISIBLE);
							lblEnv.setVisibility(View.INVISIBLE);
							imgEnv.setVisibility(View.INVISIBLE);
							lblEnvM.setVisibility(View.INVISIBLE);
							imgEnvM.setVisibility(View.INVISIBLE);
							if (StringUtils.equals(GetStatusRec(), "1")) {
								relExist.setVisibility(gl.peBotInv && !TieneFact ? View.VISIBLE : View.INVISIBLE);
								relStock.setVisibility(gl.peBotStock && TieneFact ? View.VISIBLE : View.INVISIBLE);
								relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
							} else {
								relExist.setVisibility(View.INVISIBLE);
								relStock.setVisibility(View.INVISIBLE);
								relPrecio.setVisibility(View.INVISIBLE);
							}
						} else if ((rutatipo.equalsIgnoreCase("V") && TieneInventario &&
								(TieneFact || TieneCobros || TieneDevol || TienePedidos || TieneCanastas)) ||
								(!rutatipo.equalsIgnoreCase("V"))) {
							lblRec.setVisibility(View.INVISIBLE);
							imgRec.setVisibility(View.INVISIBLE);
							lblEnv.setVisibility(View.VISIBLE);
							imgEnv.setVisibility(View.VISIBLE);
							lblEnvM.setVisibility(View.VISIBLE);
							imgEnvM.setVisibility(View.VISIBLE);
							relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
							relExist.setVisibility(View.INVISIBLE);
							relStock.setVisibility(gl.peBotStock ? View.VISIBLE : View.INVISIBLE);
						}
					}
				} else {
					if ((!YaComunico) && !(TieneFact || TienePedidos || TieneCanastas) && !TieneCobros && !TieneDevol
					    && !(rutatipo.equalsIgnoreCase("P") && !TieneAtenciones)) {
						lblRec.setVisibility(View.VISIBLE);
						imgRec.setVisibility(View.VISIBLE);
						lblEnv.setVisibility(View.INVISIBLE);
						imgEnv.setVisibility(View.INVISIBLE);
						lblEnvM.setVisibility(View.INVISIBLE);
						imgEnvM.setVisibility(View.INVISIBLE);
						if (StringUtils.equals(GetStatusRec(), "1")) {
							relExist.setVisibility(gl.peBotInv && !TieneFact ? View.VISIBLE : View.INVISIBLE);
							relStock.setVisibility(gl.peBotStock && TieneFact ? View.VISIBLE : View.INVISIBLE);
							relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
						} else {
							relExist.setVisibility(View.INVISIBLE);
							relStock.setVisibility(View.INVISIBLE);
							relPrecio.setVisibility(View.INVISIBLE);
						}
					} else {
						if (YaComunico) {
							lblRec.setVisibility(View.VISIBLE);
							imgRec.setVisibility(View.VISIBLE);
							lblEnv.setVisibility(View.INVISIBLE);
							imgEnv.setVisibility(View.INVISIBLE);
							lblEnvM.setVisibility(View.INVISIBLE);
							imgEnvM.setVisibility(View.INVISIBLE);

							if (StringUtils.equals(GetStatusRec(), "1")) {
								relExist.setVisibility(gl.peBotInv ? View.VISIBLE : View.INVISIBLE);
								relStock.setVisibility(gl.peBotStock ? View.VISIBLE : View.INVISIBLE);
								relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
							} else {
								relExist.setVisibility(View.INVISIBLE);
								relStock.setVisibility(View.INVISIBLE);
								relPrecio.setVisibility(View.INVISIBLE);
							}
						} else {
							lblRec.setVisibility(View.INVISIBLE);
							imgRec.setVisibility(View.INVISIBLE);
							lblEnv.setVisibility(View.VISIBLE);
							imgEnv.setVisibility(View.VISIBLE);
							lblEnvM.setVisibility(View.VISIBLE);
							imgEnvM.setVisibility(View.VISIBLE);
							relExist.setVisibility(View.INVISIBLE);
							relStock.setVisibility(gl.peBotStock ? View.VISIBLE : View.INVISIBLE);
							relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
						}
					}
				}
			} else {
				if (((rutatipo.equalsIgnoreCase("V")) || (rutatipo.equalsIgnoreCase("D")) && !TieneInventario)
						|| ((!rutatipo.equalsIgnoreCase("V")) && (!rutatipo.equalsIgnoreCase("D")))) {

					lblRec.setVisibility(View.VISIBLE);
					imgRec.setVisibility(View.VISIBLE);
					lblEnv.setVisibility(View.INVISIBLE);
					imgEnv.setVisibility(View.INVISIBLE);
					lblEnvM.setVisibility(View.INVISIBLE);
					imgEnvM.setVisibility(View.INVISIBLE);

					if (StringUtils.equals(GetStatusRec(), "1")) {
						relExist.setVisibility(gl.peBotInv ? View.VISIBLE : View.INVISIBLE);
						relStock.setVisibility(gl.peBotStock ? View.VISIBLE : View.INVISIBLE);
						relPrecio.setVisibility(gl.peBotPrec ? View.VISIBLE : View.INVISIBLE);
					} else {
						relExist.setVisibility(View.INVISIBLE);
						relStock.setVisibility(View.INVISIBLE);
						relPrecio.setVisibility(View.INVISIBLE);
					}

				} else {
					if (((((rutatipo.equalsIgnoreCase("V")) || (rutatipo.equalsIgnoreCase("D"))) && TieneInventario
							&& (TieneFact || TieneCobros || TienePedidos || TieneCanastas) || TieneDevol)) || ((!rutatipo.equalsIgnoreCase("V"))
							&& (!rutatipo.equalsIgnoreCase("D")))) {
						lblRec.setVisibility(View.INVISIBLE);
						imgRec.setVisibility(View.INVISIBLE);
						lblEnv.setVisibility(View.VISIBLE);
						imgEnv.setVisibility(View.VISIBLE);
						lblEnvM.setVisibility(View.VISIBLE);
						imgEnvM.setVisibility(View.VISIBLE);
						relExist.setVisibility(View.INVISIBLE);
						relStock.setVisibility(gl.peBotStock ? View.VISIBLE : View.INVISIBLE);
						relPrecio.setVisibility(View.INVISIBLE);
					}
				}
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}


	}

	private void otrosParametros() {
		try {
			AppMethods app = new AppMethods(this, gl, Con, db);
			app.parametrosExtra();
			app.parametrosGlobales();
			app.parametrosBarras();
			app.confImpresora();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			msgbox(e.getMessage());
		}
	}

	private void writeErrLog(String errstr) {
		BufferedWriter writer = null;
		FileWriter wfile;

		try {
			String fname = Environment.getExternalStorageDirectory() + "/roaderror.txt";

			wfile = new FileWriter(fname, false);
			writer = new BufferedWriter(wfile);
			writer.write(errstr);
			writer.write("\r\n");
			writer.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}

	}

	private void restartApp() {
		try {
			PackageManager packageManager = this.getPackageManager();
			Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
			ComponentName componentName = intent.getComponent();
			Intent mainIntent = Intent.makeRestartActivityTask(componentName);
			//Intent mainIntent = IntentCompat..makeRestartActivityTask(componentName);
			this.startActivity(mainIntent);
			System.exit(0);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
	public boolean ExistenDatosSinEnviar() {

		try {

			int cantFact, CantPedidos, CantCobros, CantDevol, CantInventario;

			clsAppM = new AppMethods(this, gl, Con, db);

			cantFact = clsAppM.getDocCountTipo("Facturas", true);
			CantPedidos = clsAppM.getDocCountTipo("Pedidos", true);
			CantCobros = clsAppM.getDocCountTipo("Cobros", true);
			CantDevol = clsAppM.getDocCountTipo("Devoluciones", true);
			CantInventario = clsAppM.getDocCountTipo("Inventario", true);

			return ((cantFact > 0) || (CantCobros > 0) || (CantDevol > 0) || (CantPedidos > 0) || (CantInventario > 0));

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			msgbox(e.getMessage());
			return false;
		}

	}

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
	public boolean ExisteInventario() {

		try {

			int CantInventario;

			clsAppM = new AppMethods(this, gl, Con, db);

			CantInventario = clsAppM.getDocCountTipo("Inventario", false);

			return ((CantInventario > 0));

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			msgbox(e.getMessage());
			return false;
		}

	}

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
	public boolean ExistenDatos() {

		try {

			int cantFact, CantPedidos, CantCobros, CantDevol, CantInventario;

			clsAppM = new AppMethods(this, gl, Con, db);

			cantFact = clsAppM.getDocCountTipo("Facturas", false);
			CantPedidos = clsAppM.getDocCountTipo("Pedidos", false);
			CantCobros = clsAppM.getDocCountTipo("Cobros", false);
			CantDevol = clsAppM.getDocCountTipo("Devoluciones", false);
			CantInventario = clsAppM.getDocCountTipo("Inventario", false);

			return ((cantFact > 0) || (CantCobros > 0) || (CantDevol > 0) || (CantPedidos > 0) || (CantInventario > 0));

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			msgbox(e.getMessage());
			return false;
		}

	}

	private void msgResultEnvio(String msg) {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(ComWS.this, rating.class));
					ComWS.super.finish();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	private void msgResultEnvioParcial(String msg) {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ComWS.super.finish();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void msgAskExit(String msg) {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					if (gl.modoadmin) {
						restartApp();
					} else {
						finish();
					}
					;
				}
			});

			dialog.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	// #JP corregido 20190226
	private void BorraDatosAnteriores(String msg) {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					claseFindia.eliminarTablasD();
					msgAskConfirmaRecibido();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	private void msgAskExitComplete() {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Está seguro de salir de la aplicación?");
			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void msgAskConfirmaRecibido() {
		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Recepción");
			dialog.setMessage("¿Recibir datos nuevos?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runRecep();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					lblRec.setVisibility(View.VISIBLE);
					imgRec.setVisibility(View.VISIBLE);
				}
			});
			dialog.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

	private void msgAskSinLicencia() {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Licencia");
			dialog.setMessage("El dispositivo no tiene licencia válida");
			dialog.setCancelable(false);

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					restartApp();
				}
			});

			dialog.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	public void SetStatusRecTo(String estado) {
		try {
			sql = "UPDATE P_RUTA SET PARAM2='" + StringUtils.trim(estado) + "'";
			db.execSQL(sql);
		} catch (Exception ex) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ex.getMessage(), "");
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + ex.getMessage());
		}

	}

	public void SetStatusRecToTrans(String estado) {
		try {
			sql = "UPDATE P_RUTA SET PARAM2='" + StringUtils.trim(estado) + "'";
			dbT.execSQL(sql);
		} catch (Exception ex) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ex.getMessage(), "");
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + ex.getMessage());
		}
	}

	public String GetStatusRec() {
		Cursor DT;
		String vGetStatusRec = "";

		try {
			sql = "SELECT PARAM2 FROM P_RUTA ";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				vGetStatusRec = DT.getString(0);
			}

			if (DT != null) DT.close();

		} catch (Exception ex) {
			Log.d("GetStatusRec", "Something happend here " + ex.getMessage());
			msgbox(new Object() {
			}.getClass().getEnclosingMethod().getName() + " " + ex.getMessage());
		}

		return vGetStatusRec;
	}

	//#CKFK 20190619 Agregué esta condición a la consulta AND DATEDIFF(D, FECHA, Getdate())<6
	private boolean TieneInventarioSinVentas(){

		boolean vTieneInventarioSinVentas = false;
		int rslv=0;
		String msg;

		try	{

			msg = "";

			sql =" SELECT DOCUMENTO " +
					" FROM P_STOCKB " +
					" WHERE (RUTA='" + gl.ruta + "') AND DATEDIFF(D, FECHA, Getdate())>0 " +
					" AND DATEDIFF(D, FECHA, Getdate())<6 " +
					" AND ANULADO = 0 AND STATUS = 'A' AND ENVIADO = 0 AND CODIGOLIQUIDACION = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH) " +
					" UNION SELECT DOCUMENTO " +
					" FROM P_STOCK " +
					" WHERE (RUTA='" + gl.ruta + "') AND CANT > 0 AND DATEDIFF(D, FECHA, Getdate())>0  " +
					" AND DATEDIFF(D, FECHA, Getdate())<6 " +
					" AND ANULADO = 0 AND STATUS = 'A' AND ENVIADO = 0  AND CODIGOLIQUIDACION = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH) ";

			rslv = fillTable(sql,"DELETE FROM CANTDOCPEND");

			if (rslv == 1) {
				msg = "Tiene datos pendientes de subir al BOF, va a tener que facturar manual el día de hoy o subir los datos pendientes";
				ferr += msg;
				vTieneInventarioSinVentas = true;
			} else {
				vTieneInventarioSinVentas = false;
			}

		}catch(Exception ex){
			ferr += "Ocurrió un error validando existencia de inventario sin ventas " + ex.getMessage();
		}

		return  vTieneInventarioSinVentas;
	}

    private void actualizaEstadoPedidos() {
        int pp;
        String fname,cor;

        try {
            String path = Environment.getExternalStorageDirectory().getPath() + "/RoadPedidos";
            File directory = new File(path);
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                try {
                    fname = files[i].getName();
                    pp = fname.indexOf(".txt");
                    if (pp > 0) {
                        cor=fname.substring(0,pp);

                        db.execSQL("UPDATE D_PEDIDO SET STATCOM='S' WHERE COREL='"+cor+"'");

                        new File(path+"/"+cor+".txt").delete();
                    }
                } catch (Exception e) {
                    msgbox("actualizaEstadoPedidos1 : "+e.getMessage());
                }
            }
        } catch (Exception e) {
            msgbox("actualizaEstadoPedidos2 : "+e.getMessage());
        }
    }

	//endregionF

	public String md5(String s) {
		MessageDigest digest;

		try
		{
			digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
			byte[] magnitude = digest.digest();
			BigInteger bi = new BigInteger(1, magnitude);
			String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
			return hash;
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	public void DesencriptaImpresora(View view) {

		try {
			askDecodePrinter();
		} catch (Exception e) {

		}
	}
	public void askDecodePrinter() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Desencriptar");
			dialog.setMessage("¿Va a realizar la desencripción de las impresoras, está seguro?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					DatosSupervisorPrinters();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			dialog.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void DatosSupervisorPrinters() {

		try {

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Desencriptar impresoras");

			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if (lblUser.getParent() != null) {
				((ViewGroup) lblUser.getParent()).removeView(lblUser);
			}

			if (lblPassword.getParent() != null) {
				((ViewGroup) lblPassword.getParent()).removeView(lblPassword);
			}

			if (txtUser.getParent() != null) {
				((ViewGroup) txtUser.getParent()).removeView(txtUser);
			}

			if (txtPassword.getParent() != null) {
				((ViewGroup) txtPassword.getParent()).removeView(txtPassword);
			}

			lblUser.setText("Usuario: ");
			lblPassword.setText("Contraseña: ");
			txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

			txtUser.setText("");
			txtPassword.setText("");

			layout.addView(lblUser);
			layout.addView(txtUser);
			layout.addView(lblPassword);
			layout.addView(txtPassword);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					String usr, pwd;
					boolean dtCorrectos;

					usr = txtUser.getText().toString().trim();
					pwd = txtPassword.getText().toString().trim();

					if (mu.emptystr(usr)) {
						toast("Usuario incorrecto.");
						lblEnvM.setVisibility(View.VISIBLE);
						imgEnvM.setVisibility(View.VISIBLE);
						return;
					}

					if (mu.emptystr(pwd)) {
						toast("Contraseña incorrecta.");
						lblEnvM.setVisibility(View.VISIBLE);
						imgEnvM.setVisibility(View.VISIBLE);
						return;
					}

					dtCorrectos = validaDatos(usr, pwd);

					if (dtCorrectos) {
						decodePrintersC();
					} else {
						layout.removeAllViews();

						return;
					}

				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					layout.removeAllViews();
				}
			});

			alert.show();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void decodePrintersC() {
		Cursor dt;
		String prid, ser, mac, se, sm;

		try {

			/*sql = "UPDATE P_RUTA SET VENTA = 'V'";
			db.execSQL(sql);*/

			sql = "SELECT IDIMPRESORA,NUMSERIE,MACADDRESS FROM P_IMPRESORA";
			dt = Con.OpenDT(sql);

			if (dt.getCount() > 0) dt.moveToFirst();
			while (!dt.isAfterLast()) {

				prid = dt.getString(0);
				ser = dt.getString(1);
				mac = dt.getString(2);

				se = cu.decrypt(ser);
				sm = cu.decrypt(mac);

				sql = "UPDATE P_IMPRESORA SET NUMSERIE='" + se + "',MACADDRESS='" + sm + "' WHERE IDIMPRESORA='" + prid + "'";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if (dt != null) dt.close();

			msgbox("Finalizó proceso de desencripción de las impresoras");

		} catch (Exception e) {
			try{

				sql = "SELECT IDIMPRESORA,NUMSERIE,MACADDRESS FROM P_IMPRESORA";
				dt = Con.OpenDT(sql);

				if (dt.getCount() > 0) dt.moveToFirst();
				while (!dt.isAfterLast()) {

					prid = dt.getString(0);
					ser = dt.getString(1);
					mac = dt.getString(2);

					se = cu.encrypt(ser);
					sm = cu.encrypt(mac);

					sql = "UPDATE P_IMPRESORA SET NUMSERIE='" + se + "',MACADDRESS='" + sm + "' WHERE IDIMPRESORA='" + prid + "'";
					db.execSQL(sql);

					dt.moveToNext();
				}

				if (dt != null) dt.close();

				msgbox("Finalizó proceso de encripción de las impresoras");

			}catch (Exception ex){
				msgbox(new Object() {
				}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());

			}
		}
	}

	//region Activity Events

	@Override
	public void onBackPressed() {
		try{
			if (isbusy==0) {
				if (gl.modoadmin) {
					msgAskExitComplete();
				} else {
					super.onBackPressed();
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	/*@Override
	protected void onResume() {
		super.onResume();
		try {
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
	}

	@Override
	protected void onPause() {
		try {
			this.wakeLock.release();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		this.wakelock.release();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		this.wakelock.release();
	}
	*/

	//endregion

}
