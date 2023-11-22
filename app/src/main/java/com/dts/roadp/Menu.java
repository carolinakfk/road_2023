package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.roadp.clsClasses.clsMenu;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Menu extends PBase {

	private GridView gridView;
	private RelativeLayout relbotpan;
	private TextView lblVendedor,lblRuta;
	
	private ArrayList<clsMenu> items= new ArrayList<clsMenu>();

	//#HS_20181207 Controles para seleccionar el ayudante y vehiculo.
	private Spinner Ayudante,Vehiculo;
	private TextView lblAyudante,lblVehiculo;
	private ArrayList<String> listIDAyudante = new ArrayList<String>();
	private ArrayList<String> listAyudante = new ArrayList<String>();
	private ArrayList<String> listIDVehiculo = new ArrayList<String>();
	private ArrayList<String> listVehiculo = new ArrayList<String>();

	private ListAdaptMenuGrid adaptergrid;
	private AlertDialog.Builder menudlg;
	
	private int selId,selIdx,menuid,iicon;
	private String rutatipo,sdoc;
	private boolean rutapos,horizpos;
	
	private final int mRequestCode = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) 	{

		try {

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_menu);

			super.InitBase();
			addlog("Menu",""+du.getActDateTime(),gl.vend);

			gridView = (GridView) findViewById(R.id.gridView1);
			relbotpan = (RelativeLayout) findViewById(R.id.relbotpan);

			//#HS_20181206_0945 Agregue lblVendedor que se muestra en el menú.
			lblVendedor = (TextView) findViewById(R.id.lblVendedor);
			lblRuta = (TextView) findViewById(R.id.textView9);

			Ayudante = new Spinner(this);
			Vehiculo = new Spinner(this);
			lblAyudante = new TextView(this);
			lblAyudante.setText("Ayudante:");
			lblVehiculo = new TextView(this);
			lblVehiculo.setText("Vehículo:");
			///

			vApp=this.getApplication();
			rutatipo=gl.rutatipog;
			gl.devfindia=false;
			rutapos=false;gl.rutapos=false;

			iicon=1;

			if (rutatipo.equalsIgnoreCase("T")) {
				sdoc="Factura + Pedido";
			} else if (rutatipo.equalsIgnoreCase("D")) {
				sdoc="Prefactura";iicon=102;
			} else if (rutatipo.equalsIgnoreCase("V")) {
				sdoc="Venta";iicon=102;
			}else if (rutatipo.equalsIgnoreCase("C")) {
				sdoc="Canastas";iicon=101;
			}else if (rutatipo.equalsIgnoreCase("P")) {
				sdoc="Preventa";iicon=101;
			}

			if (rutatipo.equalsIgnoreCase("R")) {
				sdoc="Venta";
				rutapos=true;
				gl.rutapos=true;
			}

			try {
				if (gl.peModal.equalsIgnoreCase("TOL")){

					gl.numSerie = getNumSerie();

					if (gl.numSerie.equals("")){
						gl.numSerie = gl.deviceId;
					}

				}else{
					gl.numSerie = gl.deviceId;
				}
			} catch (Exception e) {
				gl.numSerie="";
			}

			selId=-1;selIdx=-1;

			setHandlers();

			int ori=this.getResources().getConfiguration().orientation; // 1 - portrait , 2 - landscape
			horizpos=ori==2;

			if (horizpos) {
				gridView.setNumColumns(3);relbotpan.setVisibility(View.GONE);
			} else {
				gridView.setNumColumns(3);relbotpan.setVisibility(View.VISIBLE);
			}

			this.setTitle("ROAD");
			listItems();

			if (gl.peVehAyud) {
				AyudanteVehiculo();
			} else {
				gl.ayudanteID="";gl.vehiculoID="";
			}

			//#CKFK 20190423 Quité esta validación de configuración de impresora
			//ConfImpresora();

			/*sql = "UPDATE P_CLIENTE SET MEDIAPAGO = 4";
			db.execSQL(sql);
			sql = "UPDATE P_RUTA SET VENTA = 'V'";
			db.execSQL(sql);*/

            validaParametros();

		} catch (Exception e) 		{
			Log.e("Mnu", e.getMessage());
		}

	}

	//region  Main
	
	public void showClients(View view) {
		try{
			Intent intent;
			intent = new Intent(this,Clientes.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void listItems() {
		try{
			clsMenu item;

			items.clear();selIdx=-1;

			try {

				item = clsCls.new clsMenu();
				item.ID=1;item.Name=sdoc;item.Icon=iicon;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=2;item.Name="Comunicación";item.Icon=2;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=3;item.Name="Reimpresión";item.Icon=3;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=4;item.Name="Anulación";item.Icon=4;
				items.add(item);

				if (!rutapos) {
					item = clsCls.new clsMenu();
					item.ID=5;item.Name="Consultas";item.Icon=5;
					items.add(item);
				}

				item = clsCls.new clsMenu();
				item.ID=6;item.Name="Depósito";item.Icon=6;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=7;item.Name="Inventario";item.Icon=7;
				items.add(item);

			//#HS_20181211 Agregue nuevo boton en menu: Inicio de dia.
			//item = clsCls.new clsMenu();
			//item.ID=11;item.Name="Inicio de día";item.Icon=8;
			//items.add(item);

				item = clsCls.new clsMenu();
				item.ID=8;item.Name="Cierre del día";item.Icon=8;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=9;item.Name="Utilerias";item.Icon=9;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=10;item.Name="Cambio usuario";item.Icon=10;
				items.add(item);
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}

			adaptergrid=new ListAdaptMenuGrid(this, items);
			gridView.setAdapter(adaptergrid);
			adaptergrid.setSelectedIndex(selIdx);

			//#HS_20181206 agrega el vendedor en lblVendedor y la ruta.
			lblVendedor.setText(gl.vendnom);
			lblRuta.setText(gl.ruta);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
		
	public void setHandlers(){
	    try{
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					clsMenu vItem = (clsMenu) adaptergrid.getItem(position);
					menuid=vItem.ID;

					adaptergrid.setSelectedIndex(position);

					showMenuItem();
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}

	private void showMenuItem() {
		int prtype;
		boolean epssetflag = false;
		Float cantidad;
		//toast("menu id : "+menuid);

		try{
			prtype = getPrinterType();
			if (prtype == 2) {
				if (gl.mPrinterSet) epssetflag = false;
				else epssetflag = true;
			}

			if (menuid == 2) epssetflag = false;
			if (menuid == 9) epssetflag = false;

			if (epssetflag) {
				Intent intent = new Intent(this, UtilPrint.class);
				startActivity(intent);
				return;
			}


			switch (menuid) {

				case 1:
					getWSURLs();
					if (rutapos) {
						Intent intentp = new Intent(this, CliPos.class);
						startActivity(intentp);
					} else {

						if (!cierreRealizado() && !cierreZGenerado()){

							gl.filtrocli=-1;
							Intent intent = new Intent(this, Clientes.class);
							//Asigna conexión actual al siguiente activity.

							//#HS_201811 Verifica si hay existencias disponibles.

							if (rutatipo.equals("V") || rutatipo.equals("D")){
								cantidad = CantExistencias();
							}

							//#HS_20181206 Verifica el usuario si es DTS.
							if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")){
								mu.msgbox("No puede realizar esta acción");
							}else {

								if(gl.vnivel == 1){
									msgAskSupervisor1();
								}else {
									startActivity(intent);
								}
							}

						}
					}

					break;

				case 2:  // Comunicacion
					gl.enviaMov=false;
					getWSURLs();
					gl.findiaactivo=false;
					gl.tipo = 0;
					gl.autocom = 0;
					gl.modoadmin = false;
					Intent intent2 = new Intent(this, ComWS.class);
					startActivity(intent2);
					break;


				case 3:  // Reimpresion

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutatipo.equalsIgnoreCase("T")) {
							showPrintMenuTodo();
						} else {
							if (rutatipo.equalsIgnoreCase("V") || rutatipo.equalsIgnoreCase("D")) {
								if (gl.peAceptarCarga) {
									showPrintMenuVentaApr();
								} else {
									showPrintMenuVenta();
								}
							} else {
								showPrintMenuPreventa();
							}
						}
					}

					break;

				case 4:  // Anulacion

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutapos) {
							showVoidMenuVenta();
						} else {
							if (rutatipo.equalsIgnoreCase("T")) {
								showVoidMenuTodo();
							} else {
								if (rutatipo.equalsIgnoreCase("V") || rutatipo.equalsIgnoreCase("D")) {
									showVoidMenuVenta();
								} else {
									showVoidMenuPreventa();
								}
							}
						}
					}

					break;

				case 5:  // Consultas

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						showConsMenu();
					}

					break;

				case 6:  // Deposito

					getDepTipo();

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						Intent intent6 = new Intent(this, Deposito.class);
						startActivity(intent6);
					}

					break;

				case 7:  // Inventario

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutatipo.equalsIgnoreCase("P")) {
							showInvMenuPreventa();
						} if (rutatipo.equalsIgnoreCase("C")) {
							showInvMenuCanastas();
						} if (rutatipo.equalsIgnoreCase("T")) {
							showInvMenuTodas();
						}else if (rutatipo.equalsIgnoreCase("V")) {
							showInvMenuVenta();
						}else if (rutatipo.equalsIgnoreCase("D")) {
							showInvMenuVenta();
						}
					}

					break;

				case 8:  // Fin Dia

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						gl.findiaactivo = false;
						Intent intent8 = new Intent(this, FinDia.class);
						startActivity(intent8);
					}

					break;

				case 9:  // Utilerias
					showInvMenuUtils();
					break;

				case 10:  // Cambio usuario
					askCambUsuario();
					break;

				case 11:	// Inicio día
					Intent intent11 = new Intent(this, InicioDia.class);
					startActivity(intent11);
					break;

			}
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	//endregion

	//region Reimpresion

	private boolean cierreRealizado(){
		long fechaUltimoCierre;
		boolean rslt=false;
		clsFinDia claseFinDia;

		claseFinDia = new clsFinDia(this);

		try{

			fechaUltimoCierre = claseFinDia.ultimoCierreFecha();
			if (du.getActDate() == fechaUltimoCierre) {
				msgbox("Fin de Día ya fue efectuado el día de hoy, cargue datos nuevamente");

				rslt = true;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return rslt;
	}

	private boolean cierreZGenerado(){
		int CierreZ;
		boolean rslt=false;
		clsFinDia claseFinDia;

		claseFinDia = new clsFinDia(this);

		try{

			CierreZ = claseFinDia.getGeneroCierreZ();
			if (CierreZ == 6) {
				msgbox("Cierre  Z ya fue efectuado el día de hoy, comunique los datos y cargue datos nuevamente");

				rslt = true;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return rslt;
	}

	public void showPrintMenuTodo() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Pedido","Recibo","Deposito","Recarga","Devolución a bodega","Cierre de dia", "Nota crédito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(0);break;
						case 2:
							menuImprDoc(1);break;
						case 3:
							menuImprDoc(2);break;
						case 4:
							menuImprDoc(4);break;
						case 5:
							menuImprDoc(5);break;
						case 6:
							menuImprDoc(99);break;
						case 7:
							menuImprDoc(6);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuVenta() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega","Cierre de dia", "Nota de crédito", "Nota débito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
						case 3:
							menuImprDoc(4);break;
						case 4:
							menuImprDoc(5);break;
						case 5:
							menuImprDoc(99);break;
						case 6:
							menuImprDoc(6);break;
						case 7:
							menuImprDoc(7);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuVentaApr() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega","Cierre de dia","Nota de crédito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
						case 3:
							menuImprDoc(4);break;
						case 4:
							menuImprDoc(5);break;
						case 5:
							menuImprDoc(99);break;
						case 6:
							menuImprDoc(6);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuPreventa() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Pedido","Recibo","Deposito","Nota de crédito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(0);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
						case 3:
							menuImprDoc(6);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void menuImprDoc(int doctipo) {
		try{
			gl.tipo=doctipo;

			Intent intent = new Intent(this,Reimpresion.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	//endregion
	
	//region Anulacion
	
	public void showVoidMenuTodo() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Pedido","Recibo","Deposito","Recarga","Devolución a bodega", "Nota crédito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.anulacion48);
			menudlg.setTitle("ANULACIÓN");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							gl.tipo=3;break;
						case 1:
							gl.tipo=0;break;
						case 2:
							gl.tipo=1;break;
						case 3:
							gl.tipo=2;break;
						case 4:
							gl.tipo=4;break;
						case 5:
							gl.tipo=5;break;
						case 6:
							gl.tipo=6;break;
					}

					menuAnulDoc();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#FF4040"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void showVoidMenuVenta() {
		try{

			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega", "Nota crédito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.anulacion48);
			menudlg.setTitle("ANULACIÓN");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							gl.tipo=3;break;
						case 1:
							gl.tipo=1;break;
						case 2:
							gl.tipo=2;break;
						case 3:
							gl.tipo=4;break;
						case 4:
							gl.tipo=5;break;
						case 5:
							gl.tipo=6;break;
					}

					menuAnulDoc();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#FF4040"));
			nbutton.setTextColor(Color.WHITE);

		}catch(Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showVoidMenuPreventa() {

		try{
            final AlertDialog Dialog;
            final String[] selitems = {"Pedido","Recibo","Deposito", "Nota crédito"};

            menudlg = new AlertDialog.Builder(this);
            menudlg.setIcon(R.drawable.anulacion48);
            menudlg.setTitle("Anulación");

            menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    switch (item) {
                        case 0:
                            gl.tipo=0;break;
                        case 1:
                            gl.tipo=1;break;
                        case 2:
                            gl.tipo=2;break;
                        case 3:
                            gl.tipo=6;break;
                    }

                    menuAnulDoc();
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

            Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
            nbutton.setTextColor(Color.WHITE);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuAnulDoc() {
		try{
			Intent intent = new Intent(this,Anulacion.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
	
	//region Consultas
	
	public void showConsMenu() {

		try{
			final AlertDialog Dialog;
			int tmp= 0, itemcnt = 6;

			menudlg = new AlertDialog.Builder(this);
			menudlg.setTitle("Consultas");

			if (rutatipo.equalsIgnoreCase("D") || rutatipo.equalsIgnoreCase("T")) itemcnt = 8;

			final String[] selitems = new String[itemcnt];

			if (rutatipo.equalsIgnoreCase("D") || rutatipo.equalsIgnoreCase("T")) {
				selitems[tmp] = "Resumen de prefacturas"; tmp++;
				selitems[tmp] = "Resumen de productos en prefacturas"; tmp++;
			}

			selitems[tmp]="Objetivos por producto";tmp++;
			selitems[tmp]="Objetivos por familia";tmp++;
			selitems[tmp]="Objetivo por ruta";tmp++;
			selitems[tmp]="Objetivo por cobro";tmp++;
			selitems[tmp]="Inventario bodega";tmp++;
			selitems[tmp]="Consulta de precios";tmp++;

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String mt=selitems[item];

					if (mt.equalsIgnoreCase("Resumen de prefacturas")) menuPrefactura();
					if (mt.equalsIgnoreCase("Resumen de productos en prefacturas")) menuResumenProd();
					if (mt.equalsIgnoreCase("Objetivos por producto")) menuObjProd();
					if (mt.equalsIgnoreCase("Objetivos por familia")) menuObjFamilia();
					if (mt.equalsIgnoreCase("Objetivo por ruta")) menuObjRuta();
					if (mt.equalsIgnoreCase("Objetivo por cobro")) menuObjCobro();
					if (mt.equalsIgnoreCase("Inventario bodega")) menuInvBod();
					if (mt.equalsIgnoreCase("Consulta de precios")) menuPrecios();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjProd() {
		try{
			gl.tipo=0;

			Intent intent = new Intent(this,ObjProd.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjFamilia() {
		try{
			gl.tipo=1;

			Intent intent = new Intent(this,ObjProd.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjRuta() {
		try{
			gl.tipo=0;

			Intent intent = new Intent(this,ObjRuta.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjCobro() {
		try{
			gl.tipo=1;

			Intent intent = new Intent(this,ObjRuta.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuInvBod() {
		try{
			Intent intent = new Intent(this,InvBodega.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	private void menuPrecios() {
		try{
			Intent intent = new Intent(this,ConsPrecio.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// #AT 20211027
	private void menuPrefactura() {
		try{
			gl.repPrefactura = true;
			Intent intent = new Intent(this,ReportePrefactura.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void menuResumenProd() {
		try{

			Intent intent = new Intent(this,ResumenProductos.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	//endregion

	//region Inventario
	
	public void showInvMenuVenta() 	{

		try{
			final AlertDialog Dialog;
			int itemcnt=1,itempos=0;

			if (gl.peAceptarCarga) 	{
				itemcnt+=1;
			} else {
				if (gl.peStockItf) {
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						itemcnt+=1;
					} else {
						itemcnt+=1;
					}
				} else {
					itemcnt+=2;
				}

			}
			if (gl.peSolicInv) itemcnt++;

			final String[] selitems = new String[itemcnt];

			if (rutatipo.equals("V") || rutatipo.equals("D")){
				selitems[itempos]="Existencias";itempos++;
			}

		   if (rutatipo.equals("P")){
				selitems[itempos]="Existencias pedidos";itempos++;
			}

			if (rutatipo.equals("T")){
				selitems[itempos]="Existencias";itempos++;
				selitems[itempos]="Existencias pedidos";itempos++;
			}


			if (gl.peAceptarCarga) {
				selitems[itempos]="Aceptar Inventario";itempos++;
			} else {
				if (gl.peStockItf) {
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						//selitems[itempos]="Recarga manual";itempos++;
						selitems[itempos]="Devolucion a bodega";itempos++;
					} else {
						selitems[itempos]="Devolucion a bodega";itempos++;
					}
				} else {
					selitems[itempos]="Devolucion a bodega";itempos++;
					selitems[itempos]="Recarga manual";itempos++;
				}
			}

			//gl.peSolicInv=true;

			if (gl.peSolicInv) {
				selitems[itempos]="Solicitud de inventario";itempos++;
			}

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String mt=selitems[item];

					if (mt.equalsIgnoreCase("Existencias")) menuExist();
                    if (mt.equalsIgnoreCase("Existencias pedidos")) menuExistPed();
            		if (mt.equalsIgnoreCase("Devolucion a bodega")) menuDevBod();
					if (mt.equalsIgnoreCase("Recarga manual")) menuRecarga();
					if (mt.equalsIgnoreCase("Aceptar Inventario")) menuRecargaAuto();
					if (mt.equalsIgnoreCase("Solicitud de inventario")) menuSolicInv();

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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void showInvMenuPreventa() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Existencias pedidos","Devolucion a bodega"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
                            menuExistPed();break;
						case 1:
							menuDevBod();break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch	(Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showInvMenuTodas() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Existencias", "Existencias pedidos","Devolucion a bodega"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuExist();break;
						case 1:
							menuExistPed();break;
						case 2:
							menuDevBod();break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch	(Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showInvMenuCanastas() 	{

		try{
			final AlertDialog Dialog;
			int itemcnt=1,itempos=0;

			final String[] selitems = new String[itemcnt];

			selitems[itempos]="Devolucion a bodega";

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String mt=selitems[item];

					if (mt.equalsIgnoreCase("Devolucion a bodega")) menuDevBod();

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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showInvMenuPos() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Existencias","Devolucion","Recarga"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					//mu.msgbox(item);

					switch (item) {
						case 0:
							menuExist();break;
						case 1:
							menuDevBod();break;
						case 2:
							menuRecarga();break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuExist() {
		try{
			gl.tipo=0;
			Intent intent = new Intent(this, Exist.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

    private void menuExistPed() {
        try{
            gl.tipo=0;
            Intent intent = new Intent(this, ExistPed.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }
	
	private void menuRecarga() {

		try{
			Intent intent = new Intent(this,Recarga.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuRecargaAuto() {
		try{
			Intent intent = new Intent(this,RecargaAuto.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuDevBod() {
		try{

			gl.closeDevBod=false;

			if (gl.peModal.equalsIgnoreCase("TOL")) {

				if (existeDevolucion()){
					msgbox("Ya existe una devolución activa, no puede hacer otra");
					return;
				}

				if (tieneDevolucionTOL()) {
					startActivity(new Intent(this,DevolBodTol.class));
				} else {
					msgbox("La devolución está vacia, no se puede aplicar");return;
				}
			} else {
				startActivity(new Intent(this,DevolBod.class));
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuSolicInv() {
		Cursor dt;
		boolean newflag=false;
		String flag,corel;
		try{
			try {

				sql="SELECT STATCOM FROM D_SOLICINV";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0) {
					dt.moveToFirst();

					flag=dt.getString(0);
					if (flag.equalsIgnoreCase("S")) {
						msgbox("Falta enviar solicitud actual y realizar cierre del día antes de crear una nueva.");return;
					}
				} else {
					newflag=true;
				}

				if(dt!=null) dt.close();

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			}

			if (newflag) {

				try {
					db.beginTransaction();

					sql="DELETE FROM D_SOLICINVD";
					db.execSQL(sql);

					corel=gl.ruta+"_"+mu.getCorelBase();

					ins.init("D_SOLICINV");

					ins.add("COREL",corel);
					ins.add("ANULADO","N");
					ins.add("RUTA",gl.ruta);
					ins.add("FECHA",fecha);
					ins.add("USUARIO",gl.vend);
					ins.add("REFERENCIA","");
					ins.add("STATCOM","P");

					db.execSQL(ins.sql());

					db.setTransactionSuccessful();
					db.endTransaction();
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					db.endTransaction();msgbox("No se puede crear una nueva solicitud.\n"+e.getMessage());return;
				}

			}

			Intent intent = new Intent(this,SolicInv.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	//endregion

	//region Utilerias
	
	public void showInvMenuUtils() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Configuracion de impresora","Tablas","Correlativo CierreZ","Calculadora Kgs","Soporte","Serial del dipositivo","Impresión de barras", "Rating ROAD"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.utils48);
			menudlg.setTitle("Utilerias");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuConfImpres();break;
						case 1:
							startActivity(new Intent(Menu.this,Tablas.class));break;
						case 2:
							menuCorelZ();break;
						case 3:
							startActivity(new Intent(Menu.this, CalculadoraKgs.class));break;
						case 4:
							startActivity(new Intent(Menu.this,Soporte.class));break;
						case 5:
							msgbox("Serial# : "+gl.deviceId);break;
						case 6:
							startActivity(new Intent(Menu.this,imprime_barras.class));break;
						case 7:
							startActivity(new Intent(Menu.this,rating.class));break;

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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}		
	
	private void menuConfImpres() {
		try{
			Intent intent = new Intent(this,UtilPrint2.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	private void menuCorelZ() {
		Cursor DT;
		int coract;

		try{
			try {
				sql="SELECT Corel FROM FinDia";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				coract=DT.getInt(0);

				if(DT!=null) DT.close();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				mu.msgbox(e.getMessage());
				coract=0;
			}

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Nuevo correlativo");
			alert.setMessage("Actual : "+coract);

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER );
			input.setText("");
			input.requestFocus();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						String s=input.getText().toString();
						int icor=Integer.parseInt(s);

						if (icor<0) throw new Exception();
						askApplyCor(icor);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox("Correlativo incorrecto");
						return;
					}
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}


		
	}
	
	private void askApplyCor(int ncor) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Aplicar nuevo correlativo ?");

			final int fncor=ncor;

			dialog.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						sql="UPDATE FinDia SET Corel="+fncor;
						db.execSQL(sql);
					} catch (SQLException e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						mu.msgbox("Error : " + e.getMessage());
					}
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void askCambUsuario() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿Cambiar usuario?");

			dialog.setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Menu.super.finish();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	public float CantExistencias() {
		Cursor DT;
		float cantidad=0,cantb=0;

		try {

			sql = "SELECT P_STOCK.CODIGO,P_PRODUCTO.DESCLARGA,P_STOCK.CANT,P_STOCK.CANTM,P_STOCK.UNIDADMEDIDA,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.CENTRO,P_STOCK.STATUS " +
					"FROM P_STOCK INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=P_STOCK.CODIGO  WHERE 1=1 ";
			if (Con != null){
				DT = Con.OpenDT(sql);
				cantidad = DT.getCount();
			}else {
				cantidad = 0;
			}

			sql = "SELECT BARRA FROM P_STOCKB";
			if (Con != null){
				DT = Con.OpenDT(sql);
				cantb = DT.getCount();

				if(DT!=null) DT.close();
			}else {
				cantb = 0;
			}

			cantidad=cantidad+cantb;
		} catch (Exception e) {
			return 0;
		}

		return cantidad;
	}

	//endregion

	//region Supervisor ayudante

	private void msgAskSupervisor1() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Este usuario es ADMINISTRADOR. ¿Está seguro de realizar la venta con este usuario?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					msgAskSupervisor2();
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

	private void msgAskSupervisor2() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Usted no debería realizar ventas con el Rol de ADMINISTRADOR. ¿Esta 100% seguro de realizar la venta con este usuario?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Menu.this, Clientes.class));
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

	//#HS_20181207 Mensaje que muestra los ayudantes y vehiculos disponibles.

	private void AyudanteVehiculo() {

		try{

			/*inputAyudanteVehiculo();

			getlistAyudante();
			getlistVehiculo();
			getAyudante();
			getVehiculo();
            */

            startActivity(new Intent(this,ayudante_vehiculo.class));

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void inputAyudanteVehiculo() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("AYUDANTE Y VEHÍCULO");

			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);


		//layout.addView(new TextView(this));
			layout.addView(lblAyudante);
			layout.addView(Ayudante);
			//layout.addView(new TextView(this));
			layout.addView(lblVehiculo);
			layout.addView(Vehiculo);

			alert.setView(layout);

			alert.setPositiveButton("Asignar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					getAyudanteVehiculo();
				}
			});

			alert.setNegativeButton("Omitir", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					gl.ayudante = "";gl.ayudanteID = "";
					gl.vehiculo = "";gl.vehiculoID= "";
					closekeyb();
				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}



	private void getAyudante(){

		try{
			Ayudante.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

							gl.ayudanteID=listIDAyudante.get(position);
							gl.ayudante=listAyudante.get(position);

					} catch (Exception e) {
						mu.msgbox( e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	private void getVehiculo(){

		try{
			Vehiculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

						gl.vehiculoID=listIDVehiculo.get(position);
						gl.vehiculo=listVehiculo.get(position);

					} catch (Exception e) {
						mu.msgbox( e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	//#HS_20181207 Obtiene el ayudante y vehiculo asignado a la ruta.
	private void getAyudanteVehiculo(){

		try
		{
			if(gl.ayudanteID.equals("")){
				msgAskAyudante();
			}else if(gl.vehiculoID.equals("")){
				msgAskVehiculo();
			}else{
				mu.msgbox("Ayudante y Vehículo asignado a la ruta");
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Log.d("getAyudanteVehiculo error: ", e.getMessage());
		}
	}

	//#HS_20181207 Lista los ayudantes de la ruta.
	private void getlistAyudante(){
		Cursor DT;

		try {
			sql="SELECT CODIGO,NOMBRE FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 5 " +
			    "Union\n" +
					" Select '','Seleccione un ayudante' from P_VENDEDOR";

			DT=Con.OpenDT(sql);
			if(DT.getCount()>0) {

				DT.moveToFirst();

				while (!DT.isAfterLast())
				{
					listIDAyudante.add(DT.getString(0));
					listAyudante.add(DT.getString(1));
					DT.moveToNext();
				}

			} else if(DT.getCount() == 0){
				listIDVehiculo.add("");
				listVehiculo.add("No hay ayudantes disponibles");
			}

			if(DT!=null) DT.close();

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listAyudante);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			Ayudante.setAdapter(dataAdapter);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

	private void getlistVehiculo(){
		Cursor DT;

		try {
			sql="SELECT CODIGO, MARCA, PLACA FROM P_VEHICULO " +
			"Union \n" +
					" Select '','Seleccione un vehículo','' from P_VEHICULO";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0) {
				DT.moveToFirst();

				while (!DT.isAfterLast()) {
					listIDVehiculo.add(DT.getString(0));
					listVehiculo.add(DT.getString(1) + "-" + DT.getString(2));
					DT.moveToNext();
				}
			} else if(DT.getCount() == 0){
				listIDVehiculo.add("");
				listVehiculo.add("No hay vehiculos disponibles");
			}

			if(DT!=null) DT.close();

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listVehiculo);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			Vehiculo.setAdapter(dataAdapter);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

	private void msgAskAyudante() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Esta seguro de continuar sin asignar un ayudante?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//inputAyudanteVehiculo();

				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskVehiculo() {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Esta seguro de continuar sin asignar un vehículo?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

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

	//endregion

	//region Aux
	
	private void setPrintWidth() {
		Cursor DT;
		int prwd=32;
		
		try {
			sql="SELECT COL_IMP FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			prwd=DT.getInt(0);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			prwd=32;
		}
		
		gl.prw=prwd;
		
	}
	
	private void getDepTipo() {
		Cursor DT;
		
		try {
			sql="SELECT BOLETA_DEPOSITO,DEPOSITO_PARCIAL FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			gl.boldep=DT.getInt(0);
			gl.depparc=DT.getInt(1)==1;

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			gl.boldep=0;
			gl.depparc=false;
		}
		
	}	
		
	private int getPrinterType() {
		Cursor DT;
		String prtipo;
		int prid=0;
		
		
		try {
			sql="SELECT TIPO_IMPRESORA FROM P_ARCHIVOCONF";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			prtipo=DT.getString(0);
				
			if (prtipo.equalsIgnoreCase("DATAMAX")) prid=1;
			if (prtipo.equalsIgnoreCase("EPSON")) prid=2;

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			prid=0;
	    }
		
		return prid;
				
	}

	public void doWSTest(View view) {
		startActivity(new Intent(Menu.this,WSTest.class));
	}

	//#CKFK 20190705 Se creó función para obtener número de serie de P_COREL
	public String getNumSerie(){
		Cursor DT;
		String numSerie = "";

		try {

			sql = "SELECT NUMSERIE FROM P_HANDHELD" ;
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();
				numSerie = DT.getString(0);

				if (DT!=null) DT.close();

			} else {
				numSerie="";
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return numSerie;
	}

	private boolean tieneDevolucionTOL() {
		Cursor dt;
		long cantcan = 0, cantstock = 0, cantbolsa = 0;

		try {
			sql = "SELECT IFNULL(SUM(CANT),0) FROM P_STOCK WHERE CANT+CANTM>0";
			dt = Con.OpenDT(sql);
			cantstock = dt.getLong(0);

			sql = "SELECT IFNULL(COUNT(BARRA),0) FROM P_STOCKB";
			dt = Con.OpenDT(sql);
			cantbolsa = dt.getLong(0);

			sql = "SELECT IFNULL(SUM(CANT),0) FROM D_CXC E INNER JOIN D_CXCD D ON  E.COREL = D.COREL WHERE E.ANULADO = 'N'";
			dt = Con.OpenDT(sql);
			cantcan = dt.getLong(0);

			sql = "SELECT IFNULL(SUM(CANTREC),0) FROM D_CANASTA WHERE ANULADO = 0";
			dt = Con.OpenDT(sql);
			cantcan += dt.getLong(0);

			if(dt!=null) dt.close();

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			return false;
		}

		return (cantstock + cantbolsa + cantcan > 0);
	}

	private boolean existeDevolucion(){
		boolean vReturn = false;
		Cursor DT;

		try {

			sql	 = "SELECT * FROM D_MOV WHERE TIPO = 'D' AND ANULADO = 'N'";
			DT=Con.OpenDT(sql);

			vReturn = DT.getCount()>0;

		}catch (Exception e){
			msgbox( e.getMessage());
		}

		return vReturn;
	}
	public void getWSURLs() {
		Cursor dt;

		try {

			sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.URLLocal= dt.getString(0);
			gl.URLRemoto=dt.getString(1);

			if (gl.URLLocal.isEmpty()) msgbox("No existe configuración para transferencia de datos");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

    public void validaParametros() {
        Cursor dt;

        try {

            sql="SELECT IMPRESION,ENVIO_AUTO_PEDIDOS FROM P_RUTA";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getString(0).equalsIgnoreCase("N")) {
                msgbox("La ruta tiene deshabilitada la impresión");
            }

            if (dt.getInt(1)==1) {
                msgbox("Envio de pedidos habilitado");
            }

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


	//endregion

	//region Activity Events
	
	@Override
 	protected void onResume() {
		try{
			super.onResume();
			setPrintWidth();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	@Override
	public void onBackPressed() {
		try{

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
}
