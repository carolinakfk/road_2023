package com.dts.roadp;

import java.util.ArrayList;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;

public class ObjProd extends PBase {

	private ListView listView;
	private RadioButton rbUni,rbMonto;

	private ArrayList<clsClasses.clsObj> items= new ArrayList<clsClasses.clsObj>();
	private ListAdaptObj adapter;
	private clsClasses.clsObj selitem;

	private String rutatipo;
	private int fecha,tipo,objtipo;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_obj_prod);

		super.InitBase();
		addlog("ObjProd",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		rbUni = (RadioButton) findViewById(R.id.radioButton1);
		rbMonto = (RadioButton) findViewById(R.id.radioButton2);

		tipo=((appGlobals) vApp).tipo;
		rutatipo=((appGlobals) vApp).rutatipog;
		if (tipo==0) {this.setTitle("Objetivos por producto");} else {this.setTitle("Objetivos por familia");}	

		objtipo=0;// Cantidad

		listItems();
	}


	// Events

	public void setUnits(View view){
		try{
			rbUni.setChecked(true);
			rbMonto.setChecked(false);
			objtipo=0;
			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void setValue(View view){
		try{
			rbUni.setChecked(false);
			rbMonto.setChecked(true);
			objtipo=1;
			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Main

	private void listItems(){
		Cursor DT,DTV;
		clsClasses.clsObj vItem;
		double meta,acum,perc,falta,venta;
		String cod,name,codigo;

		items.clear();		

		try {

			if (tipo==0) {
				if (objtipo==0) {
					sql="SELECT O_PROD.CODIGO,P_PRODUCTO.DESCCORTA,O_PROD.METAU,O_PROD.ACUMU "+
							"FROM O_PROD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=O_PROD.CODIGO ";
				} else {	
					sql="SELECT O_PROD.CODIGO,P_PRODUCTO.DESCCORTA,O_PROD.METAV,O_PROD.ACUMV "+
							"FROM O_PROD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=O_PROD.CODIGO ";
				}
				sql+="WHERE (O_PROD.RUTA='"+((appGlobals) vApp).ruta+"') ORDER BY P_PRODUCTO.DESCCORTA";
			} else {
				if (objtipo==0) {
					sql="SELECT O_LINEA.CODIGO,P_LINEA.NOMBRE,O_LINEA.METAU,O_LINEA.ACUMU "+
							"FROM O_LINEA INNER JOIN P_LINEA ON P_LINEA.CODIGO=O_LINEA.CODIGO ";
				} else {	
					sql="SELECT O_LINEA.CODIGO,P_LINEA.NOMBRE,O_LINEA.METAV,O_LINEA.ACUMV "+
							"FROM O_LINEA INNER JOIN P_LINEA ON P_LINEA.CODIGO=O_LINEA.CODIGO ";
				}
				sql+="WHERE (O_LINEA.RUTA='"+((appGlobals) vApp).ruta+"') ORDER BY P_LINEA.NOMBRE";
			}


			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod=DT.getString(0);codigo=cod;if (tipo==1) cod="";	
				name=DT.getString(1);
				meta=DT.getDouble(2);
				acum=DT.getDouble(3);

				venta=0;
				try {

					if (tipo==0) {
						if (objtipo==0) {

							if (rutatipo.equalsIgnoreCase("P")) {
								sql="SELECT SUM(D_PEDIDOD.CANT) "+
										"FROM D_PEDIDOD INNER JOIN D_PEDIDO ON D_PEDIDO.COREL=D_PEDIDOD.COREL "+
										"WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') AND (D_PEDIDO.RUTA='"+((appGlobals) vApp).ruta+"') AND (D_PEDIDOD.PRODUCTO='"+codigo+"')";		
							} else {	
								sql="SELECT SUM(D_FACTURAD.CANT) "+
										"FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURA.COREL=D_FACTURAD.COREL "+
										"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') AND (D_FACTURA.RUTA='"+((appGlobals) vApp).ruta+"') AND (D_FACTURAD.PRODUCTO='"+codigo+"')";		
							}

						} else {	

							if (rutatipo.equalsIgnoreCase("P")) {
								sql="SELECT SUM(D_PEDIDOD.TOTAL) "+
										"FROM D_PEDIDOD INNER JOIN D_PEDIDO ON D_PEDIDO.COREL=D_PEDIDOD.COREL "+
										"WHERE (D_PEDIDO.ANULADO='N')  AND (D_PEDIDO.STATCOM='N') AND (D_PEDIDO.RUTA='"+((appGlobals) vApp).ruta+"') AND (D_PEDIDOD.PRODUCTO='"+codigo+"')";			
							} else {	
								sql="SELECT SUM(D_FACTURAD.TOTAL) "+
										"FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURA.COREL=D_FACTURAD.COREL "+
										"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') AND (D_FACTURA.RUTA='"+((appGlobals) vApp).ruta+"') AND (D_FACTURAD.PRODUCTO='"+codigo+"')";			
							}

						}
					} else {
						if (objtipo==0) {

							if (rutatipo.equalsIgnoreCase("P")) {
								sql="SELECT SUM(D_PEDIDOD.CANT) "+
										"FROM D_PEDIDOD INNER JOIN D_PEDIDO ON D_PEDIDO.COREL=D_PEDIDOD.COREL INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_PEDIDOD.PRODUCTO "+
										"WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') AND (D_PEDIDO.RUTA='"+((appGlobals) vApp).ruta+"') AND (P_PRODUCTO.LINEA='"+codigo+"')";
							} else {	
								sql="SELECT SUM(D_FACTURAD.CANT) "+
										"FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURA.COREL=D_FACTURAD.COREL INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_FACTURAD.PRODUCTO "+
										"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') AND (D_FACTURA.RUTA='"+((appGlobals) vApp).ruta+"') AND (P_PRODUCTO.LINEA='"+codigo+"')";
							}

						} else {	

							if (rutatipo.equalsIgnoreCase("P")) {
								sql="SELECT SUM(D_PEDIDOD.TOTAL) "+
										"FROM D_PEDIDOD INNER JOIN D_PEDIDO ON D_PEDIDO.COREL=D_PEDIDOD.COREL INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_PEDIDOD.PRODUCTO "+
										"WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') AND (D_PEDIDO.RUTA='"+((appGlobals) vApp).ruta+"') AND (P_PRODUCTO.LINEA='"+codigo+"')";
							} else {	
								sql="SELECT SUM(D_FACTURAD.TOTAL) "+
										"FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURA.COREL=D_FACTURAD.COREL INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_FACTURAD.PRODUCTO "+
										"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') AND (D_FACTURA.RUTA='"+((appGlobals) vApp).ruta+"') AND (P_PRODUCTO.LINEA='"+codigo+"')";
							}

						}
					}

					DTV=Con.OpenDT(sql);
					if (DTV.getCount()>0) {
						DTV.moveToFirst();venta=DTV.getDouble(0);
					}

					DTV.close();

				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					mu.msgbox( e.getMessage());	  
				}

				acum=acum+venta;

				if (meta>0) {
					falta=meta-acum;if (falta<0) falta=0;
					perc=100*acum/meta;
				} else {
					falta=0;perc=0;acum=0;
				}

				if (meta>0) {
					vItem = clsCls.new clsObj();

					vItem.Cod=cod;
					vItem.Nombre=name;
					if (objtipo==0) {
						vItem.Meta=mu.frmdec(meta);
						vItem.Acum=mu.frmdec(acum);		
					} else {	
						vItem.Meta=mu.frmcur(meta);
						vItem.Acum=mu.frmcur(acum);		
					}
					  
					vItem.Perc=mu.frmdec(perc);
					vItem.Falta=mu.frmdec(falta);

					items.add(vItem);					  
				}

				DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}

		adapter=new ListAdaptObj(this,items);
		listView.setAdapter(adapter);
	}


}
