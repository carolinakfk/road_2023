package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.roadp.clsClasses.clsBonifItem;
import com.dts.roadp.clsClasses.clsBonifProd;

import java.util.ArrayList;

public class BonList extends PBase {

	private ListView listView;
	private RelativeLayout relMonto;
	private TextView lblMonto,lblFalta,lblTipo,lblMarg;
	private ImageView imgComp,imgAdd;
	
	private ArrayList<clsClasses.clsBonifProd> items = new ArrayList<clsClasses.clsBonifProd>();
	private ListAdaptBonif adapter;
	private clsBonifItem item;
	private clsBonifProd selitem;
	private Precio prc;

	private AppMethods app;
	private InputMethodManager keyboard;	
	
	private String lista,rutatipo,bonprodid,um; 
	private int pos,mpos,tipolista,selpos,poruni,nivel,dectipo;
	private double valor,bonprodcant,icant,falt,mul,marg;
	private boolean completo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bon_list);
		
		super.InitBase();
		addlog("BonList",""+du.getActDateTime(),gl.vend);
		
		listView =  (ListView) findViewById(R.id.listView1);
		relMonto =  (RelativeLayout) findViewById(R.id.relMonto);
		lblMonto =  (TextView) findViewById(R.id.textView5);
		lblFalta =  (TextView) findViewById(R.id.lblSel);
		lblTipo =  (TextView) findViewById(R.id.textView2);
		lblMarg =  (TextView) findViewById(R.id.textView8);
		imgComp =  (ImageView) findViewById(R.id.imageView2);
		imgAdd =  (ImageView) findViewById(R.id.imageView3);
		
		rutatipo=gl.rutatipo;
		bonprodid=gl.bonprodid;
		bonprodcant=gl.bonprodcant;
		nivel=gl.nivel;
		
		keyboard = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);	
		
		sethandlers();

		app = new AppMethods(this, gl, Con, db);
		prc=new Precio(this,mu,gl.peDec);
		um=gl.um;
		
		clearByProd();	
		marg=getMargin();
		
		mpos=gl.bonus.size();
		pos=0;
		setItem();
		
		//showkeyb();
	}
	
	
	// Events
	
	public void nextScreen(View view) {
		try{
			processNextScreen();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void selectProd(View view) {
		try{
			gl.gstr="";
			browse=1;

			if (rutatipo.equalsIgnoreCase("P")) gl.prodtipo=0;else gl.prodtipo=1;

			Intent intent = new Intent(this,Producto.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
		
	private void sethandlers() {

		try{
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			//		try {
						Object lvObj = listView.getItemAtPosition(position);
						selitem = (clsBonifProd)lvObj;

						adapter.setSelectedIndex(position);
						selpos=position;

						//if (tipolista!=2) msgAskDel("Eliminar producto de la lista ?");
						if (tipolista==2) setVarCant();
						if (tipolista==3) setVarAllCant();

				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;*/
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}

		
	}
	
	
	// Main
	
	private void showBonList() {
		double mmarg;
		
		lista=item.lista;
		tipolista=item.tipolista;
		valor=item.valor;mmarg=marg*valor;
		mul=item.mul;
		
		lblMarg.setText(" ");
		
		imgComp.setVisibility(View.INVISIBLE);
		imgAdd.setVisibility(View.INVISIBLE);

		try{
			switch (tipolista) {
				case 0:
					s="Producto";break;
				case 1:
					s="Lista fija";break;
				case 2:
					s="Lista variable";
					lblMarg.setText("+/- "+mu.frmdec(mmarg));
					break;
				case 3:
					s="Todos los productos";
					imgAdd.setVisibility(View.VISIBLE);
					lblMarg.setText("+/- "+mu.frmdec(mmarg));
					break;
			}

			lblTipo.setText(s);
			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		
	}
	
	private void listItems() {
		Cursor DT;
		clsClasses.clsBonifProd item;	
		double ddisp,bcant;
		try {

			items.clear();

			//mu.msgbox("Lista : " +lista+"  ,   TipoLista : "+tipolista);

			switch (tipolista) {
			case 0:  
				sql="SELECT CODIGO,DESCLARGA,1,0 FROM P_PRODUCTO WHERE (CODIGO='"+lista+"')";break;
			case 3:  
				sql="SELECT CODIGO,DESCLARGA,1,0 FROM P_PRODUCTO WHERE CODIGO=''";break;	
			default:  
				sql="SELECT P_BONLIST.PRODUCTO, P_PRODUCTO.DESCLARGA, P_BONLIST.CANT, P_BONLIST.CANTMIN " +
					"FROM P_PRODUCTO INNER JOIN P_BONLIST ON P_PRODUCTO.CODIGO = P_BONLIST.PRODUCTO " +
					"WHERE (P_BONLIST.CODIGO='"+lista+"') ORDER BY P_PRODUCTO.DESCLARGA";break;
			}
			
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
				  
			  item = clsCls.new clsBonifProd();
			  
			  item.id=DT.getString(0);
			  item.nombre=DT.getString(1);
			  
			  bcant=DT.getDouble(2)*valor;
			  if (tipolista==2) bcant=DT.getDouble(3)*mul;
			  item.cant=bcant;
			  item.cantmin=DT.getDouble(3)*mul;
			  
			  item.precio=prc.precio(item.id,bcant,nivel,um,gl.umpeso,0,um);
			  item.prstr=mu.frmdec(item.precio);
			  item.costo=prc.costo;
			  
			  if (rutatipo.equalsIgnoreCase("V")) {
				  ddisp=getDisp(item.id);
				  item.disp=ddisp;
				  if (ddisp>=bcant) item.flag=1;else item.flag=0;
			  } else {
				  item.flag=1;					
			  }
			  
			  items.add(item);	
			 
			  DT.moveToNext();
			}

			if(DT!=null) DT.close();

			adapter=new ListAdaptBonif(this,items);
			listView.setAdapter(adapter);

			showStat();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage()+"\n"+sql);
		}	

	}
	
	private void applyBon() {
		clsBonifProd item;
		double cant,disp,diff;

		try{
			for (int i = 0; i <items.size(); i++) {

				item=items.get(i);
				cant=item.cant;

				if (cant>0) {
					if (item.flag==0) {
						disp=item.disp;
						diff=cant-disp;

						addFalt(item.id,diff);

						s="Se registro faltante :\n"+mu.frmdecno(diff)+" - "+item.nombre;
						toast(s);

					} else {
						addItem(item);
					}
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
	
	private void addItem(clsBonifProd item) {
		Cursor DT;
		int iidx;
		double fact,peso;

		try {
			sql="SELECT MAX(ITEM) FROM T_BONITEM";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			iidx=DT.getInt(0);

			if(DT!=null) DT.close();

			iidx++;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			iidx=0;
		}
		
		try {

			fact=app.factorPeso(item.id);
			peso=fact*item.cant;

			ins.init("T_BONITEM");

			ins.add("ITEM",iidx);
			ins.add("PRODID",bonprodid);
			ins.add("BONIID",item.id);
			ins.add("CANT",item.cant);
			ins.add("PRECIO",item.precio);
			ins.add("COSTO",item.costo);
			ins.add("PESO",peso);
			ins.add("UMVENTA",app.umVenta(item.id));
			ins.add("UMSTOCK",app.umStock(item.id));
			ins.add("UMPESO",gl.umpeso);
			ins.add("FACTOR",fact);
			ins.add("POR_PESO",app.ventaPeso(item.id)?1:0);

	    	db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}		
	}
	
	private void addFalt(String prid,double prcant) {
			
		try {
			ins.init("T_BONIFFALT");
			
			ins.add("PRODID",bonprodid);
			ins.add("PRODUCTO",prid);
			ins.add("CANT",prcant);

	    	db.execSQL(ins.sql());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
		
	}
	
	private void showStat() {
		double btot=0,bmon=0,afalt,margval;


		try{
			if (tipolista<2) return;

			for (int i = 0; i <items.size(); i++) {
				btot+=items.get(i).cant;
				bmon+=items.get(i).cant*items.get(i).precio;
			}

			imgComp.setVisibility(View.INVISIBLE);completo=false;

			if (poruni==1) {
				falt=valor-btot;
				lblMonto.setText(mu.frmdecno(btot));
				lblFalta.setText(mu.frmdecno(falt));

				if (falt==0) {
					imgComp.setVisibility(View.VISIBLE);completo=true;
				}

			} else {

				falt=valor-bmon;
				lblMonto.setText(mu.frmdec(bmon));
				lblFalta.setText(mu.frmdec(falt));

				afalt=Math.abs(falt);
				margval=valor*marg;

				if (afalt<=margval) {
					imgComp.setVisibility(View.VISIBLE);completo=true;
				}

			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	
	}
	
	private void processNextScreen() {
		try{
			if (tipolista<2) {
				nextItem();
			} else {
				showStat();
				if (completo) {
					nextItem();
				} else {
					mu.msgbox("Bonificación incompleta.");
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void processItem() {
		clsClasses.clsBonifProd item;
		String pid,pname;
		double bcant,ddisp;
		
		pid=gl.gstr;
		pname=gl.pprodname;

		try{
			if (mu.emptystr(pid)) return;

			item = clsCls.new clsBonifProd();

			item.id=pid;
			item.nombre=pname;

			bcant=0;
			item.cant=bcant;
			item.cantmin=0;

			item.precio=prc.precio( item.id,1,nivel,um,gl.umpeso,0,um);
			item.prstr=mu.frmdec(item.precio);
			item.costo=prc.costo;

			if (rutatipo.equalsIgnoreCase("V")) {
				ddisp=getDisp(item.id);
				item.disp=ddisp;
				if (ddisp>0) item.flag=1;else item.flag=0;
			} else {
				item.flag=1;
			}

			items.add(item);

			adapter.notifyDataSetChanged();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
	// Aux
	
	private void setVarCant() {
		double val,ffalt,ppr,valmx,valmi;
		final double cmin,cmax;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		valmi=selitem.cantmin;
		val=selitem.disp;
		ffalt=falt+selitem.cant;

		try{
			if (val>ffalt) valmx=ffalt;else valmx=val;

			if (poruni==0) {

				ppr=selitem.precio;if (ppr<=0) ppr=1;

				val=valmi/ppr;
				valmi=Math.floor(val);

				val=valmx/ppr;
				valmx=Math.floor(val);
			}


			cmin=valmi;
			cmax=valmx;


			alert.setTitle("Cantidad");
			alert.setMessage("Entre : "+mu.frmdecno(cmin)+" y "+mu.frmdecno(cmax));

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(mu.frmdecno(selitem.cant));
			input.requestFocus();


			alert.setNegativeButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String s;

				//	try {
						s=input.getText().toString();
						icant=Double.parseDouble(s);

					//	if (icant<cmin) throw new Exception();
					//	if (icant>cmax) throw new Exception();

						if (icant<cmin || icant>cmax){
							mu.msgbox("Cantidad incorrecta");return;
						}else {
							inputCant();
						}

				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}
				*/
				}
			});

			alert.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			if (cmax>=cmin) {
				alert.show();
			} else {
				mu.msgbox("No hay existencia para aplicar");
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void setVarAllCant() {
		double val,ffalt,ppr,valmx,valmi;
		final double cmin,cmax;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		try{
			valmi=selitem.cantmin;
			val=selitem.disp;
			ffalt=falt+selitem.cant;
			if (val>ffalt) valmx=ffalt;else valmx=val;

			if (poruni==0) {

				ppr=selitem.precio;if (ppr<=0) ppr=1;

				val=valmi/ppr;
				valmi=Math.floor(val);

				val=valmx/ppr;
				valmx=Math.floor(val);
			}

			cmin=valmi;
			cmax=valmx;

			alert.setTitle("Cantidad");
			alert.setMessage("Entre : "+mu.frmdecno(cmin)+" y "+mu.frmdecno(cmax));

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(mu.frmdecno(selitem.cant));
			input.requestFocus();

			alert.setNegativeButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String s;

				//	try {
						s=input.getText().toString();
						icant=Double.parseDouble(s);

					/*	if (icant<cmin) throw new Exception();
						if (icant>cmax) throw new Exception();
					*/
						if(icant>cmin || icant>cmax){
							mu.msgbox("Cantidad incorrecta");
							return;
						}else{
							inputCant();
						}


				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox("Cantidad incorrecta");return;
					}
				*/
				}
			});

			alert.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});

			alert.setNeutralButton("Borrar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				//	try {
						items.remove(selpos);
						adapter.notifyDataSetChanged();
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}
				*/
				}
			});


			if (cmax>=cmin) {
				alert.show();
			} else {
				mu.msgbox("No hay existencia para aplicar");
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void inputCant() {
		try{
			selitem.cant=icant;

			adapter.notifyDataSetChanged();
			showStat();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void setItem() {
		try{
		//	try {
				item=gl.bonus.get(pos);

				if (gl.bonus.get(pos).porcant.equalsIgnoreCase("S")) poruni=1;else poruni=0;

				showBonList();
		/*	} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
		*/
			relMonto.setVisibility(View.GONE);dectipo=-1;

			if (tipolista>=2) {
				relMonto.setVisibility(View.VISIBLE);dectipo=0;
			}

			if (poruni==0) {
				relMonto.setVisibility(View.VISIBLE);dectipo=1;
			} else {
				lblMarg.setText(" ");
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		

	}
	
	private void nextItem() {

		try{
			applyBon();

			if (pos==mpos-1) {
				super.finish();
				return;
			}

			pos++;
			setItem();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void msgAskDel(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				//	try {
						items.remove(selpos);
						adapter.notifyDataSetChanged();
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}
				*/
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
	
	private double getDisp(String prodid) {
		Cursor DT;
		double sdisp,bcant,bpcant;

		/*if (rutatipo.equalsIgnoreCase("V")) {
			sql="SELECT SUM(CANT) FROM P_STOCK WHERE CODIGO='"+prodid+"'";
		} else {
			sql="SELECT CANT FROM P_STOCKINV WHERE CODIGO='"+prodid+"'";
		}

		try {
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			sdisp=DT.getDouble(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sdisp=0;
		}

		if (prodid.equalsIgnoreCase(bonprodid)) bpcant=bonprodcant; else bpcant=0;

		// Total en lista de bonoficaciones

		bcant=0;

		return sdisp-bcant-bpcant;*/

		try {

			if (rutatipo.equalsIgnoreCase("V")) {
				sql="SELECT SUM(CANT) FROM P_STOCK WHERE CODIGO='"+prodid+"'";
			} else {
				sql="SELECT CANT FROM P_STOCKINV WHERE CODIGO='"+prodid+"'";
			}

			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			sdisp=DT.getDouble(0);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sdisp=0;
		}

		if (prodid.equalsIgnoreCase(bonprodid)) bpcant=bonprodcant; else bpcant=0;
		// Total en lista de bonoficaciones
		bcant=0;

		return sdisp-bcant-bpcant;
	}
	
	private double getMargin() {
		Cursor DT;
		double sdisp;

		try {
			sql="SELECT BONVOLTOL FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			sdisp=DT.getDouble(0);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sdisp=25;
		}
		sdisp=sdisp/100;
		return sdisp;
	}
	
	private void toastcnt(String msg) {

		try{
			if (mu.emptystr(msg)) return;

			Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	private void clearByProd() {
		
		try {
			sql="DELETE FROM T_BONITEM WHERE PRODID='"+bonprodid+"'";
			db.execSQL(sql);
	/*	} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}		
		
		try {*/
			sql="DELETE FROM T_BONIFFALT WHERE PRODID='"+bonprodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
			
	}
	
	protected void showkeyb(){
		try{
			if (keyboard != null) keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}


	// Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			if (browse==1) {
				browse=0;
				processItem();return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
	
	@Override
	public void onBackPressed() {
		try{
			processNextScreen();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	

}
