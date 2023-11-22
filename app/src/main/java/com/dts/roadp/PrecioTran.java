package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DecimalFormat;

public class PrecioTran {

	public double costo,descmon,imp,impval,tot,precsin,totsin,precdoc,precioespecial;

	private int active;

	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	private String sql;

	private Context cont;

	private DecimalFormat ffrmprec;
	private MiscUtils mu;

	private String prodid,um,umpeso,umventa;
	private double cant,desc,prec;
	private int nivel,ndec;
	private boolean porpeso;

	public PrecioTran(Context context, MiscUtils mutil, int numdec, BaseDatos dbconnection, SQLiteDatabase database) {
		
		cont=context;
		mu=mutil;
		ndec=numdec;

		Con=dbconnection;
		db=database;
		ins=Con.Ins;upd=Con.Upd;

		costo=0;descmon=0;imp=0;tot=0;
		
		ffrmprec = new DecimalFormat("#0.00");
		
	}

	public double precio(String prod,double pcant,int nivelprec,String unimedida,String unimedidapeso,double ppeso,String umven) {

		prodid=prod;cant=pcant;nivel=nivelprec;
		um=unimedida;umpeso=unimedidapeso;umventa=umven;
		prec=0;costo=0;descmon=0;imp=0;tot=0;precioespecial=0;

		clsDescuentoTran clsDesc=new clsDescuentoTran(cont,prodid,cant,Con,db);
		desc=clsDesc.getDesc();

		if (cant>0) prodPrecio(ppeso);else prodPrecioBase();

		return prec;
	}

	public boolean existePrecioEspecial(String prod,double pcant,String cliente,String clitipo,String unimedida,String unimedidapeso,double ppeso) {
		prodid=prod;cant=pcant;um=unimedida;
		umpeso=unimedidapeso;
		prec=0;costo=0;descmon=0;imp=0;tot=0;precioespecial=0;

		if (cant==0) return false;

		prodPrecioEsp(ppeso,cliente,clitipo);
		precioespecial=prec;
		return prec>0;
	}

	
	// Private
	
	private void prodPrecio(double ppeso) {
		Cursor DT;
		double pr,stot,pprec,tsimp;
		String sprec="";
	
		try {
			if (ppeso>0) {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+umpeso+"') ";
			} else {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+um+"') ";
			}

			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			pr=DT.getDouble(0);

			if(DT!=null) DT.close();

		} catch (Exception e) {
			pr=0;

			try {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+umventa+"') ";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				pr=DT.getDouble(0);
				if(DT!=null) DT.close();
			} catch (Exception ee) {
				pr=0;
			}
	    }
		
		totsin=pr*cant;tsimp=mu.round(totsin,ndec);
		
		//percep=0;
		
		imp=getImp();
		pr=pr*(1+imp/100);

		// total
		stot=pr*cant;stot=mu.round(stot,ndec);
		if (imp>0) impval=stot-tsimp; else impval=0;
		descmon=(double) (stot*desc/100);descmon=mu.round(descmon,ndec);
		tot=stot-descmon;

		if (cant>0) prec=(double) (tot/cant); else prec=pr;

		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			precdoc=mu.round(pprec,ndec);
		} catch (Exception e) {
			precdoc=prec;
		}

		if (ppeso>0) prec=prec*ppeso/cant;
			
		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=prec;
		}
		prec=pprec;

		// total
		stot=prec*cant;stot=mu.round(stot,ndec);
		if (imp>0) impval=stot-tsimp; else impval=0;
		descmon=(double) (stot*desc/100);descmon=mu.round(descmon,ndec);
		tot=stot-descmon;

		if (imp==0) precsin=prec; else precsin=prec/(1+imp/100);
		//Toast.makeText(cont,sprec+" - "+pprec+" / "+prec+" prec sin : "+precsin, Toast.LENGTH_SHORT).show();
		
		totsin=mu.round(precsin*cant,ndec);
		if (cant>0) precsin=(double) (totsin/cant);	
		
		try {
			sprec=ffrmprec.format(precsin);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=precsin;
		}
		precsin=pprec;

	}
	
	private void prodPrecioBase() {

		Cursor DT;
		double pr,stot,pprec,tsimp;
		String sprec="";
	
		try {

			sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+um+"') ";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			pr=DT.getDouble(0);
			if(DT!=null) DT.close();

		} catch (Exception e) {
			pr=0;
	    }	
		
		totsin=pr;tsimp=mu.round(totsin,ndec);
		
		imp=getImp();
		pr=pr*(1+imp/100);
		
		stot=pr;stot=mu.round(stot,ndec);
		
		if (imp>0) impval=stot-tsimp; else impval=0;
		
		descmon=0;
		
		tot=stot-descmon;
		prec=tot;
			
		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=prec;
		}
		prec=pprec;
		
		if (imp==0) precsin=prec; else precsin=prec/(1+imp/100);
		
		totsin=mu.round(precsin,ndec);
		precsin=totsin;	
		
		try {
			sprec=ffrmprec.format(precsin);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=precsin;
		}
		precsin=pprec;

	}
	
	private double getImp() {
		Cursor DT;
		double imv=0,im1=0,im2=0,im3=0;
		int ic1,ic2,ic3;
		
		try {
			sql="SELECT IMP1,IMP2,IMP3 FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			ic1=DT.getInt(0);
			ic2=DT.getInt(1);
			ic3=DT.getInt(2);

			if(DT!=null) DT.close();

			if (ic1>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic1;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im1=DT.getDouble(0);
				} catch (Exception e) {
					im1=0;
				}
				if(DT!=null) DT.close();
			}
			
			if (ic2>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic2;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im2=DT.getDouble(0);
				} catch (Exception e) {
					im2=0;
				}
				if(DT!=null) DT.close();
			}
			
			if (ic3>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic3;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im3=DT.getDouble(0);
				} catch (Exception e) {
					im3=0;
				}
				if(DT!=null) DT.close();
			}			
			
			
			imv=im1+im2+im3;
			
			return imv;
			
		} catch (Exception e) {
			return 0;
	    }		
		
	}

	private void prodPrecioEsp(double ppeso,String cliente,String clitipo) {
		Cursor dt;
		double pr,prr,stot,pprec,tsimp;
		String sprec="",vcod,vval;

		pr=0;
		try {
			if (ppeso>0) {
				sql=" SELECT PRECIO,CODIGO,VALOR FROM TMP_PRECESPEC "+
						" WHERE (PRODUCTO='"+prodid+"') AND (UNIDADMEDIDA='"+umpeso+"') "+
						" AND ((VALOR='" + cliente + "' AND CODIGO ='CLIENTE') " +
						" OR (CODIGO='RUTA' AND VALOR='" + cliente + "') OR  " +
						" (CODIGO ='TIPO' AND VALOR IN (SELECT TIPO FROM P_CLIENTE WHERE CODIGO='" + cliente + "')))" +
						" ORDER BY CODIGO ASC ";
			} else {
				sql=" SELECT PRECIO,CODIGO,VALOR FROM TMP_PRECESPEC "+
						" WHERE (PRODUCTO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"') "+
						" AND ((VALOR='" + cliente + "' AND CODIGO ='CLIENTE') " +
						" OR (CODIGO='RUTA' AND VALOR='" + cliente + "') OR  " +
						" (CODIGO ='TIPO' AND VALOR IN (SELECT TIPO FROM P_CLIENTE WHERE CODIGO='" + cliente + "')))" +
						" ORDER BY CODIGO ASC ";
			}
			dt=Con.OpenDT(sql);

			if (dt.getCount() > 0) {
				dt.moveToFirst();
				while (!dt.isAfterLast()) {
					prr = dt.getDouble(0);
					vcod = dt.getString(1);
					vval=dt.getString(2);

					if (vcod.equalsIgnoreCase("TIPO")) {
						if (vval.equalsIgnoreCase(clitipo)){
							pr=prr;break;
						}
					} else {
						if (vval.equalsIgnoreCase(cliente)){
							pr=prr;break;
						}
					}

					dt.moveToNext();
				}
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
			pr=0;
		}

		totsin=pr*cant;tsimp=mu.round(totsin,ndec);
		imp=0;
		pr=pr*(1+imp/100);

		// total
		stot=pr*cant;stot=mu.round(stot,ndec);
		if (imp>0) impval=stot-tsimp; else impval=0;
		descmon=(double) (stot*desc/100);descmon=mu.round(descmon,ndec);
		tot=stot-descmon;

		if (cant>0) prec=(double) (tot/cant); else prec=pr;

		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			precdoc=mu.round(pprec,ndec);
		} catch (Exception e) {
			precdoc=prec;
		}

		if (ppeso>0) prec=prec*ppeso/cant;

		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=prec;
		}
		prec=pprec;

		// total
		stot=prec*cant;stot=mu.round(stot,ndec);
		if (imp>0) impval=stot-tsimp; else impval=0;
		descmon=(double) (stot*desc/100);descmon=mu.round(descmon,ndec);
		tot=stot-descmon;

		if (imp==0) precsin=prec; else precsin=prec/(1+imp/100);
		totsin=mu.round(precsin*cant,ndec);
		if (cant>0) precsin=(double) (totsin/cant);

		try {
			sprec=ffrmprec.format(precsin);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=precsin;
		}

		precsin=pprec;

	}


	// Aux
	
	public double round2(double val){
		int ival;
		
		val=(double) (100*val);
		double rslt=Math.round(val);
		rslt=Math.floor(rslt);
		
		ival=(int) rslt;
		rslt=(double) ival;
		
		return (double) (rslt/100);
	}
	
 	private void opendb() {
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
			active=1;	
	    } catch (Exception e) {
	    	active= 0;
	    }
	}		

}
