package com.dts.roadp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PedidoRes extends PBase {

	private static final String CERO = "0";
	private static final String BARRA = "/";
	public final Calendar c = Calendar.getInstance();
	final int mes = c.get(Calendar.MONTH);
	final int dia = c.get(Calendar.DAY_OF_MONTH);
	final int anio = c.get(Calendar.YEAR);

	private ListView listView;
	private EditText txtDir;
	private TextView lblFecha;
	private ImageView imgBon;
	private Spinner spinList;
	private ImageView btnSave;
	
	private List<String> spname = new ArrayList<String>();
	private ArrayList<clsClasses.clsCDB> items= new ArrayList<clsClasses.clsCDB>();
	private ListAdaptTotals adapter;
	
	private Runnable printcallback,printclose,printexit;
	
	private clsDescGlob clsDesc;
    private clsDataBuilder dbld;
	private printer prn;
	private clsDocPedido pdoc;
    private AppMethods app;
	
	private long fecha,fechae;
	private String itemid,cliid,corel;
	private int cyear, cmonth, cday,dweek,impres, presday;
	
	private double dmax,dfinmon,descpmon,descg,descgmon,tot,stot0,stot,descmon,totimp,totperc,dispventa;
	private boolean acum,cleandprod,toledano,porpeso,prodstandby,impprecio;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pedido_res);
		
		super.InitBase();
		addlog("PedidoRes",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		txtDir = (EditText) findViewById(R.id.txtMonto);
		lblFecha = (TextView) findViewById(R.id.lblpSaldo);
		imgBon = (ImageView) findViewById(R.id.imageView1);
		
		spinList = (Spinner) findViewById(R.id.spinner1);
		btnSave = (ImageView) findViewById(R.id.imgPFoto);
		
		cliid=gl.cliente;
		gl.tolpedsend=false;
        toledano=gl.peModal.equalsIgnoreCase("TOL");

		setActDate();
		dweek=mu.dayofweek();

		setActDate2();
		fechae=fecha;
		lblFecha.setText(du.sfecha(fechae));
        app = new AppMethods(this, gl, Con, db);

		clsDesc=new clsDescGlob(this);

		adjustSpinner();
		fillSpinner();
		
		descpmon=totalDescProd();
		
		dmax=clsDesc.dmax;
		acum=clsDesc.acum;

        dispventa = gl.dvdispventa;dispventa=mu.round(dispventa,2);

		processFinalPromo();
		
	    processFinalPromo();
		
		printcallback= new Runnable() {
		    public void run() {
		    	askPrint();
		    }
		};
		
		printclose= new Runnable() {
		    public void run() {
		    }
		};

		printexit= new Runnable() {
			public void run() {
				gl.tolpedsend=true;
				PedidoRes.super.finish();
			}
		};
		
		prn=new printer(this,printexit,gl.validimp);
		pdoc=new clsDocPedido(this,prn.prw,gl.peMon,gl.peDecImp, "");
		pdoc.global=gl;
		pdoc.deviceid =gl.numSerie;
	}
		
	
	//region Events
	
	public void showBon(View view) {
		Intent intent = new Intent(this,BonVenta.class);
		startActivity(intent);	
	}

	//endregion
	
	//region Main
	
	private void processFinalPromo(){
		
		descg=gl.descglob;
		
		descgmon=(double) (stot0*descg/100);
		totalOrder();
		
		if (descg>0) {
			try{
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						showPromo();
					}
				}, 300);

			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
		}
	}
	
	public void showPromo(){
		try {
			browse=1;
			gl.promprod="";
			gl.promcant=0;
			gl.promdesc=descg;
			
			Intent intent = new Intent(this,DescBon.class);
			startActivity(intent);	
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}
		
	}
	
	private void updDesc(){
		try{
			descg=gl.promdesc;
			descgmon=(double) (stot0*descg/100);
			totalOrder();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void totalOrder(){
		double dmaxmon;
		
		cleandprod=false;

		try{
			if (acum) {
				dfinmon=descpmon+descgmon;
				cleandprod=false;
			} else {
				if (descpmon>=descgmon) {
					dfinmon=descpmon;
					cleandprod=false;
				} else {
					dfinmon=descgmon;
					cleandprod=true;
				}
			}

			dmaxmon=(double) (stot0*dmax/100);
			if (dmax>0) {
				if (dfinmon>dmaxmon) dfinmon=dmax;
			}

			descmon=mu.round2(dfinmon);
			stot=mu.round2(stot0);

			fillTotals();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void fillTotals() {
		clsClasses.clsCDB item;	
		
		items.clear();
		
		try {
			
			if (gl.sinimp) {
				
				totimp=mu.round2(totimp);
				stot=stot-totimp;
				
				totperc=stot*(gl.percepcion/100);
				totperc=mu.round2(totperc);
				
				tot=stot+totimp-descmon+totperc;
				tot=mu.round2(tot);
				
				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="Impuesto";item.Desc=mu.frmcur(totimp);item.Bandera=0;
				items.add(item);				
		
				if (gl.contrib.equalsIgnoreCase("C")) {
					item = clsCls.new clsCDB();
					item.Cod="Percepcion";item.Desc=mu.frmcur(totperc);item.Bandera=0;
					items.add(item);
				}
				
				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);					
				
			} else {
								
				totimp=mu.round2(totimp);
				tot=stot-descmon;
				tot=mu.round2(tot);
				
				
				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);			
				
			}
					
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		adapter=new ListAdaptTotals(this,items);
		listView.setAdapter(adapter);
	}
	
 	private void finishOrder(){
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

		try {

			fecha=du.cfechaSinHora(Integer.parseInt(lblFecha.getText().toString().substring(8,10)),
					Integer.parseInt(lblFecha.getText().toString().substring(3,5)),
                    Integer.parseInt(lblFecha.getText().toString().substring(0,2)));

			if (!fechaValida()){
				mu.msgbox("La fecha de entrega ingresada no es válida, no se puede guardar el pedido");
				return;
			}

			if (!saveOrder()) return;

			clsBonifSave bonsave=new clsBonifSave(this,corel,"P");

			bonsave.ruta=gl.ruta;
			bonsave.cliente=gl.cliente;
			bonsave.fecha=fecha;
			bonsave.emp=gl.emp;

			bonsave.save();

			btnSave.setVisibility(View.INVISIBLE);

			if (gl.impresora.equalsIgnoreCase("S")) {
				String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
				pdoc.buildPrint(corel,0, vModo);
				prn.printask(printcallback);
			}

            if (toledano && autoenvio) enviaPedido();

            if (prodstandby) toastcent("EL PEDIDO CONTIENE PRODUCTO CERRADO");

			gl.closeCliDet=true;
			gl.closeVenta=true;

			if (!gl.impresora.equalsIgnoreCase("S")) {
				gl.tolpedsend=true;
				super.finish();
			}

		} catch (Exception e){
			mu.msgbox("Error " + e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void enviaPedido() {
        Cursor dt;
        String wsurl,psql="";

        try {
            sql="SELECT FTPFOLD FROM P_RUTA";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();
            wsurl=dt.getString(0);
            if (wsurl.isEmpty()) {
                toastlong("No se puede enviar pedido, no está definida la URL de web service");return;
            }

            dbld = new clsDataBuilder(this);
            dbld.insert("D_PEDIDO", "WHERE COREL='" + corel + "'");
            dbld.insert("D_PEDIDOD", "WHERE COREL='" + corel + "'");

            for (int i = 0; i < dbld.size(); i++) {
                psql=psql+dbld.items.get(i)+"\n";
            }

            Intent intent = new Intent(PedidoRes.this, srvEnvPedido.class);
            intent.putExtra("URL",wsurl);
            intent.putExtra("command",psql);
            intent.putExtra("correlativo",corel);
            startService(intent);

        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }
 	
	private void singlePrint() {
 		prn.printask(printcallback);
 	}
	
	private boolean saveOrder(){
		Cursor DT;
		double tot,desc,imp,peso,vcant,vpeso,vfactor,factpres,cantinv;
        String vprod,vumstock,vumventa,bandisp, vumstockaux = "";
        int dev_ins=1;
        int ncItem=0;

        corel=gl.ruta+"_"+mu.getCorelBase();
		fechae=du.ffecha00(fechae);
        prodstandby=false;

		try {

			sql="SELECT MAX(ITEM) FROM D_NOTACRED_LOG ";
			DT=Con.OpenDT(sql);

			if (DT!=null){
				if(DT.getCount()>0){
					DT.moveToFirst();
					ncItem=DT.getInt(0);
				}else{
					ncItem=0;
				}

				DT.close();

				ncItem++;
			}

			sql="SELECT SUM(TOTAL),SUM(DESMON),SUM(IMP),SUM(PESO) FROM T_VENTA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			tot=DT.getDouble(0);
			desc=DT.getDouble(1);
			imp=DT.getDouble(2);
			peso=DT.getDouble(3);
			
			db.beginTransaction();

			if (!gl.modpedid.isEmpty()) {
                anulaPedidoExistente();
            }
			
			ins.init("D_PEDIDO");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",du.getActDate());
			ins.add("EMPRESA",gl.emp);
			if (gl.tolsuper) {
				ins.add("RUTA",gl.rutasup);ins.add("RUTASUPER",gl.ruta);
			}else {
				ins.add("RUTA",gl.ruta);ins.add("RUTASUPER","");
			}
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("KILOMETRAJE",0);
			ins.add("FECHAENTR",fechae);
			ins.add("DIRENTREGA",txtDir.getText().toString());
			ins.add("TOTAL",tot);
			ins.add("DESMONTO",descmon);
			ins.add("IMPMONTO",imp);
			ins.add("PESO",peso);
			ins.add("BANDERA","");
			ins.add("STATCOM","N");
			ins.add("CALCOBJ","N");
			ins.add("IMPRES",0);
			ins.add("ADD1","");
			ins.add("ADD2","");
			ins.add("ADD3","");
			ins.add("STATPROC","");
			ins.add("RECHAZADO",0);
			ins.add("RAZON_RECHAZADO","");  // valor de percepcion 0 o xxx.xx
			ins.add("INFORMADO",0);
			ins.add("SUCURSAL",gl.sucur);
			ins.add("ID_DESPACHO",0);
			ins.add("ID_FACTURACION",0);
            ins.add("RUTASUPER","");
            ins.add("FECHA_SISTEMA",du.getActDateTime());

			db.execSQL(ins.sql());
          		
			sql="SELECT PRODUCTO,CANT,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2,UM,FACTOR,UMSTOCK," +
					"SIN_EXISTENCIA,VAL3 FROM T_VENTA";
			DT=Con.OpenDT(sql);
	
			DT.moveToFirst();
			while (!DT.isAfterLast()) {

                if (DT.getInt(14)==1) bandisp="S";else bandisp="F";

                ins.init("D_PEDIDOD");

                ins.add("COREL", corel);
                ins.add("PRODUCTO", DT.getString(0));
                ins.add("EMPRESA", gl.emp);
                ins.add("ANULADO", "N");
                ins.add("CANT", DT.getDouble(1));
                ins.add("PRECIO", DT.getDouble(2));
                ins.add("IMP", DT.getDouble(3));
                ins.add("DES", DT.getDouble(4));
                ins.add("DESMON", DT.getDouble(5));
                ins.add("TOTAL", DT.getDouble(6));
                ins.add("PRECIODOC", DT.getDouble(7));
                ins.add("PESO", DT.getDouble(8));
                ins.add("VAL1", DT.getDouble(9));
                ins.add("VAL2", bandisp); //DT.getString(10));

                if ((DT.getInt(15)==1) && (DT.getInt(14)==0)) {
                    cantinv=DT.getDouble(1);
                } else {
                    cantinv=0;
                }
                ins.add("CANTPROC",cantinv);

                ins.add("UMVENTA", DT.getString(11));
                ins.add("FACTOR", DT.getDouble(12));

                //#CKFK 20211221 Agregué la validacion de que si se está guardando la UMSTOCK incorrecta  se reemplace
				vumstockaux="";
                if (DT.getString(13).equals("KG")) {
					vumstockaux=app.umSalida(DT.getString(0));
					ins.add("UMSTOCK", vumstockaux);
				}else{
                	ins.add("UMSTOCK", DT.getString(13));
				}

                ins.add("UMPESO", gl.umpeso);
                ins.add("SIN_EXISTENCIA", DT.getInt(14)); //JP20210614

                String ss=ins.sql();
                db.execSQL(ins.sql());

                if (DT.getInt(15)==1) prodstandby=true;

                //if (toledano) {
                if (cantinv>0) {

                    vprod = DT.getString(0);
                    vumstock = DT.getString(13);
                    vcant = cantinv; // DT.getDouble(1);
                    vpeso = DT.getDouble(8);
                    factpres = DT.getDouble(12);
                    if (app.esRosty(vprod)) factpres=1;
                    vfactor = vpeso / (vcant * factpres);
                    vumventa = DT.getString(11);
                    porpeso = prodPorPeso(DT.getString(0));

                    rebajaStockUM(vprod, vumstock, vcant, vfactor, vumventa, factpres, peso);
                }
				
			    DT.moveToNext();
			}

			if(DT!=null) DT.close();

         	if (prodstandby)  db.execSQL("UPDATE D_PEDIDO SET BANDERA='S' WHERE (COREL='"+corel+"')");

            //region Devolución de  producto.
            if (gl.dvbrowse!=0) {

                String pcod;
                Double pcant;

                if (dev_ins==1){

                    ins.init("D_CxC");

                    ins.add("COREL",gl.dvcorreld);
                    ins.add("RUTA",gl.ruta);
                    ins.add("CLIENTE",gl.cliente);
                    ins.add("FECHA",fecha);
                    ins.add("ANULADO","N");
                    ins.add("EMPRESA",gl.emp);
                    ins.add("TIPO", gl.dvestado);
                    ins.add("REFERENCIA",corel);
                    ins.add("IMPRES",0);
                    ins.add("STATCOM","N");
                    ins.add("VENDEDOR",gl.vend);
                    ins.add("TOTAL",dispventa);
                    ins.add("SUPERVISOR",gl.codSupervisor);
                    ins.add("AYUDANTE",gl.ayudanteID);
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("ESTADO","S");

                    db.execSQL(ins.sql());

                    ins.init("D_NOTACRED");

                    ins.add("COREL",gl.dvcorrelnc);
                    ins.add("ANULADO","N");
                    ins.add("FECHA",fecha);
                    ins.add("RUTA",gl.ruta);
                    ins.add("VENDEDOR",gl.vend);
                    ins.add("CLIENTE",gl.cliente);
                    ins.add("TOTAL",dispventa);
                    ins.add("FACTURA",corel);
                    ins.add("SERIE","0");
                    ins.add("CORELATIVO",gl.dvactualnc);
                    ins.add("STATCOM","N");
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("RESOLNC","N");
                    ins.add("SERIEFACT",0);
                    ins.add("CORELFACT",0);
                    ins.add("IMPRES",0);

                    db.execSQL(ins.sql());

                    sql="SELECT Item,CODIGO,CANT,CODDEV,TOTAL,PRECIO,PRECLISTA,REF,PESO,LOTE,UMVENTA,UMSTOCK,UMPESO,FACTOR,POR_PESO FROM T_CxCD WHERE CANT>0";
                    DT=Con.OpenDT(sql);

                    DT.moveToFirst();
                    while (!DT.isAfterLast()) {

                        pcod=DT.getString(1);
                        pcant=DT.getDouble(2);

                        ins.init("D_CxCD");

                        ins.add("COREL",gl.dvcorreld);
                        ins.add("ITEM",DT.getInt(0));
                        ins.add("CODIGO",DT.getString(1));
                        ins.add("CANT",DT.getDouble(2));
                        ins.add("CODDEV",DT.getString(3));
                        ins.add("ESTADO",gl.dvestado);
                        ins.add("TOTAL",DT.getDouble(4));
                        ins.add("PRECIO",DT.getDouble(5));
                        ins.add("PRECLISTA",DT.getDouble(6));
                        ins.add("REF",DT.getString(7));
                        ins.add("PESO",DT.getDouble(8));
                        ins.add("FECHA_CAD",0);
                        ins.add("LOTE",DT.getString(9));
                        ins.add("UMVENTA",DT.getString(10));
                        ins.add("UMSTOCK",DT.getString(11));
                        ins.add("UMPESO",DT.getString(12));
                        ins.add("FACTOR",DT.getDouble(13));
                        db.execSQL(ins.sql());

                        ins.init("D_NOTACREDD");

                        ins.add("COREL",gl.dvcorrelnc);
                        ins.add("PRODUCTO",DT.getString(1));
                        ins.add("PRECIO_ORIG",DT.getDouble(5));
                        ins.add("PRECIO_ACT",0);
                        ins.add("CANT",DT.getDouble(2));
                        ins.add("PESO",DT.getDouble(8));
                        ins.add("POR_PESO", DT.getString(14));
                        ins.add("UMVENTA",DT.getString(10));
                        ins.add("UMSTOCK",DT.getString(11));
                        ins.add("UMPESO",DT.getString(12));
                        ins.add("FACTOR",DT.getDouble(13));
                        db.execSQL(ins.sql());

                        DT.moveToNext();
                    }

                    sql="UPDATE D_CxC SET REFERENCIA='"+corel+"' WHERE COREL='"+gl.dvcorreld+"'";
                    db.execSQL(sql);

                    sql="UPDATE D_NOTACRED SET FACTURA='"+corel+"' WHERE COREL='"+gl.dvcorrelnc+"'";
                    db.execSQL(sql);

                    gl.devtotal=0;

                    sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactuald+" WHERE RUTA='"+gl.ruta+"' AND TIPO='D'";
                    db.execSQL(sql);

					ins.init("D_NOTACRED_LOG");
					ins.add("ITEM",ncItem);
					ins.add("SERIE",gl.dvSeried);
					ins.add("COREL",gl.dvactuald);
					ins.add("FECHA",du.getActDateTime());
					ins.add("RUTA",gl.ruta);
					ins.add("TIPO","ND");
					db.execSQL(ins.sql());

					ncItem +=1;

                    sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactualnc+" WHERE RUTA='"+gl.ruta+"' AND TIPO='NC'";
                    db.execSQL(sql);

					ins.init("D_NOTACRED_LOG");
					ins.add("ITEM",ncItem);
					ins.add("SERIE",gl.dvSeriend);
					ins.add("COREL",gl.dvactualnc);
					ins.add("FECHA",du.getActDateTime());
					ins.add("RUTA",gl.ruta);
					ins.add("TIPO","NC");
					db.execSQL(ins.sql());

					ncItem +=1;

                    Toast.makeText(this,"Devolución guardada", Toast.LENGTH_SHORT).show();

                    sql="DELETE FROM T_CxCD";
                    db.execSQL(sql);

                    dev_ins = 0;

                }

            }

            //endregion


            db.setTransactionSuccessful();
			db.endTransaction();
			 
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox( e.getMessage());
		   	return false;
		}
		
		try {

			upd.init("P_CLIRUTA");
			upd.add("BANDERA",0);

			if (gl.tolsuper) {
				upd.Where("CLIENTE='"+cliid+"'");
			}else{
				upd.Where("CLIENTE='"+cliid+"' AND DIA="+dweek);
			}
	
			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		saveAtten();
		
		return true;
	}

    private void rebajaStockUM(String prid,String umstock,double cant,double factor, String umventa,double factpres,double ppeso) {
        Cursor dt;
        double cantapl,dispcant,actcant,pesoapl,disppeso,actpeso,speso;

        if (porpeso) {
            actcant=cant;actpeso=ppeso;
        } else {
            actcant=cant*factpres;actpeso=cant*factor;
        }

        try {
            sql="SELECT CANT,PESO FROM P_STOCK_PV WHERE (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"')";
            dt=Con.OpenDT(sql);
            if (dt.getCount()==0) return;

            dt.moveToFirst();
            while (!dt.isAfterLast()) {

                cant=dt.getDouble(0);
                speso=dt.getDouble(1);

                if (actcant>cant) cantapl=cant;else cantapl=actcant;
                dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
                actcant=actcant-cantapl;

                if (porpeso) {
                    if (actpeso>speso) pesoapl=speso;else pesoapl=actpeso;
                    actpeso=actpeso-pesoapl;
                } else {
                    pesoapl=cantapl*factor;
                }
                disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

                sql="UPDATE P_STOCK_PV SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') ";
                db.execSQL(sql);

                dt.moveToNext();
            }

            return;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("rebajaStockUM: "+e.getMessage());
            return;
        }
    }

    private void anulaPedidoExistente() {
        Cursor dt;
        double dcant,dpeso;
        String prid;

        corel=gl.modpedid;

        sql="SELECT PRODUCTO,CANTPROC,PESO FROM D_PEDIDOD WHERE COREL='"+corel+"'";
        dt=Con.OpenDT(sql);

        dt.moveToFirst();
        while (!dt.isAfterLast()) {

            prid=dt.getString(0);
            dcant=dt.getDouble(1);
            dpeso=dt.getDouble(2);

            if (dcant>0) {
                sql="UPDATE P_STOCK_PV SET CANT=CANT+"+dcant+",PESO=PESO+"+dpeso+" WHERE (CODIGO='"+prid+"') ";
                db.execSQL(sql);
            }

            dt.moveToNext();
        }

        db.execSQL("DELETE FROM D_PEDIDO WHERE COREL='"+corel+"'");
        db.execSQL("DELETE FROM D_PEDIDOD WHERE COREL='"+corel+"'");

    }

    private double totalDescProd(){
		Cursor DT;
		
		try {
			sql="SELECT SUM(DESMON),SUM(TOTAL),SUM(IMP) FROM T_VENTA";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			tot=DT.getDouble(1);
			stot0=tot+DT.getDouble(0);
			
			totimp=DT.getDouble(2);
			
			return DT.getDouble(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tot=0;
			mu.msgbox( e.getMessage());return 0;
		}	
		
	}
 	
	private void saveAtten() {
		long ti,tf,td;
		
		ti=gl.atentini;tf=du.getActDateTime();
		td=du.timeDiff(tf,ti);if (td<1) td=1;
		
		try {
			ins.init("D_ATENCION");
		
			ins.add("RUTA",gl.ruta);
			ins.add("FECHA",ti);
			ins.add("HORALLEG",gl.ateninistr);
			//ins.add("HORALLEG",DU.shora(ti)+":00");
			ins.add("HORASAL",du.shoraseg(tf));
			ins.add("TIEMPO",td);
			
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("DIAACT",du.dayofweek(ti));
			ins.add("DIA",du.dayofweek(ti));
			ins.add("DIAFLAG","S");
			
			ins.add("SECUENCIA",1);
			ins.add("SECUENACT",1);
			ins.add("CODATEN","");
			ins.add("KILOMET",0);
			
			ins.add("VALORVENTA",tot);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",0);
			ins.add("CODOPER","X");
			ins.add("COREL",corel);

			ins.add("CoorX",gl.gpspx);
			ins.add("CoorY",gl.gpspy);
			ins.add("CliCoorX",gl.gpscpx);
			ins.add("CliCoorY",gl.gpscpy);
			ins.add("Dist",gl.gpscdist);

			ins.add("SCANNED",gl.escaneo);
			ins.add("STATCOM","N");
			ins.add("LLEGO_COMPETENCIA_ANTES",0);
	
			db.execSQL(ins.sql());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//MU.msgbox("Error : " + e.getMessage());
		}	
		
	}


    //endregion

	//region Date

	public void showDateDialog(View view) {
		try{
		/*DialogFragment newFragment = new DatePickerFragment();
	    //newFragment.show(getSupportFragmentManager(), "datePicker");*/
			obtenerFecha();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void obtenerFecha(){
		try{
			DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
					final int mesActual = month + 1;
					String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
					String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
					lblFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
				}
			},anio, mes, dia);

			recogerFecha.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void setActDate(){
		try{
			final Calendar c = Calendar.getInstance();

			cyear = c.get(Calendar.YEAR);
			cmonth = c.get(Calendar.MONTH)+1;
			cday = c.get(Calendar.DAY_OF_MONTH);
			fecha=du.cfecha(cyear,cmonth,cday);

			presday = c.get(Calendar.DAY_OF_WEEK);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void setActDate2(){
		try{
			final Calendar c = Calendar.getInstance();

			if (presday == 7)
				c.add(Calendar.DATE,2);
			else
				c.add(Calendar.DATE,1);

			cyear = c.get(Calendar.YEAR);
			cmonth = c.get(Calendar.MONTH)+1;
			cday = c.get(Calendar.DAY_OF_MONTH);
			fecha=du.cfecha(cyear,cmonth,cday);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

    //endregion

	//region Aux
	
	private void adjustSpinner(){

		try{
			spinList.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;
					String scod;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

						scod=spname.get(position);
						txtDir.setText(scod);

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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

	private void fillSpinner(){
		Cursor DT;
		
		spname.clear();
			
		try {
			sql="SELECT DIRECCION FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			spname.add(DT.getString(0));

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			spname.add("");txtDir.setText("");
			mu.msgbox( e.getMessage());
	    }		
		
		
		try {
			
			sql="SELECT DIRECCION_ENTREGA FROM P_CLIDIR WHERE CODIGO_CLIENTE='"+cliid+"' ORDER BY DIRECCION_ENTREGA";
			DT=Con.OpenDT(sql);
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
			  spname.add(DT.getString(0));
			  DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spname);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		try {
			spinList.setAdapter(dataAdapter);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	
	}
	
	public void askSave(View view) {
        String ss;

		try {

            prodstandby=validaStandby();
            if (prodstandby) ss="Guardar pedido con producto cerrado?";else ss="Guardar pedido ?";

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage(ss);

			dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finishOrder();
				}
			});

			dialog.setNegativeButton("Salir", null);

			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public  boolean fechaValida(){
		boolean vFechaValida = false;

		try{

			if (fecha<du.getFechaActual()){
				vFechaValida= false;
			}else{
				fechae=fecha * 10000;
				vFechaValida= true;
			}

		}catch (Exception e){
			mu.msgbox("Ocurrió un error " + e.getMessage());
		}

		return  vFechaValida;
	}

	public void setDate(){
		lblFecha.setText(du.sfecha(fecha));
	}
	
	private void checkPromo() {
		Cursor DT;
		
		imgBon.setVisibility(View.INVISIBLE);
		
		try {
			sql="SELECT ITEM FROM T_BONITEM";
           	DT=Con.OpenDT(sql);
			if (DT.getCount()>0) imgBon.setVisibility(View.VISIBLE);
			if(DT!=null) DT.close();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }			
	}
	
	private void askPrint() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Impresion correcta ?");
            dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					impres++;

					try {
						sql="UPDATE D_PEDIDO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					if (impres>1) {

						try {
							sql="UPDATE D_PEDIDO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						}

						gl.brw=0;
						gl.tolpedsend=true;
						finish();

					} else {
						String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
						pdoc.buildPrint(corel,1,vModo);

						prn.printask(printcallback);

					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//singlePrint();
					prn.printask(printcallback);
					//if (gl.tolsuper) startActivity(new Intent(PedidoRes.this,ComWSSend.class));
					//finish();
				}
			});


			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}

    private boolean prodPorPeso(String prodid) {
        try {
            return app.ventaPeso(prodid);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validaStandby(){
        Cursor DT;

        prodstandby=false;

        try {
            sql="SELECT VAL3 FROM T_VENTA";
            DT=Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                if (DT.getInt(0)==1) prodstandby=true;
                DT.moveToNext();
            }

            if(DT!=null) DT.close();
        } catch (Exception e) {
            mu.msgbox( e.getMessage());return false;
        }

        return prodstandby;
    }

    //endregion

	//region Activity Events
	
	@Override
	protected void onResume() {
		try{
			super.onResume();

			checkPromo();

			if (browse==1) {
				browse=0;
				if (gl.promapl) updDesc();
				return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	
	}

    //endregion

}
