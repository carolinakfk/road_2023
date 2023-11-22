package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class clsDocument {

	public String nombre,numero,serie,ruta,vendedor,cliente,nit,tipo,ref,medidapeso, rutapv;
	public String resol,resfecha,resvence,resrango,fsfecha,fsfechaent,modofact;
	public String tf1="",tf2="",tf3="",tf4="",tf5="",add1="",add2="",deviceid, qrCode,CUFE, Caja, FechaAutorizacion, NumAutorizacion;
	public clsRepBuilder rep;
	public boolean docfactura,docrecibo,docanul,docpedido,docdevolucion,doccanastabod,docdesglose;
	public int pendiente,diacred,condicionPago,impprecio;
	public long ffecha;

	public double SumaPeso = 0;
	public double SumaCant = 0;

    protected AppMethods app;
    protected appGlobals global;
	protected android.database.sqlite.SQLiteDatabase db;
	protected BaseDatos Con;
	protected String sql;
	
	protected ArrayList<String> lines= new ArrayList<String>();
	
	protected Context cont;
	protected DateUtils DU;
	protected DecimalFormat decfrm;
	
	protected String clicod,clidir,pemodo,vendcod;

	protected int prw;
	
	public clsDocument(Context context,int printwidth,String cursym,int decimpres, String archivo) {
		cont=context;
		prw=printwidth;
		
		rep=new clsRepBuilder(cont,prw,true,cursym,decimpres, archivo);
		DU=new DateUtils();
		decfrm = new DecimalFormat("#,##0.00");

	}

	public boolean buildPrint(String corel,int reimpres) {
		setAddlog("Build print",""+DU.getActDateTime(),"");

		modofact="*";
		rep.clear();
				
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}

	public boolean buildPrint(String corel,int reimpres,String modo) {
		int flag;

        modofact=modo;
		rep.clear();

		try{

			if (!buildHeader(corel,reimpres)) return false;
			if (!buildDetail()) return false;
			if (!buildFooter()) return false;

			flag=0;

			if (modofact.equalsIgnoreCase("TOL")) {
				if (docfactura && (reimpres==10)) flag=1;
				if (docfactura && (reimpres==4) || docdesglose) flag=0;
				if (doccanastabod){
					if (reimpres==1){
						flag=1;
					}else{
						flag=0;
					}
				}
				if (docrecibo && (reimpres==0)) flag=0;
				if (docrecibo && (reimpres==1)) flag=1;
				if (docdevolucion && (reimpres==1)) flag = 1;
				if (docpedido && (reimpres==1)) flag = 1;
			} else if(modofact.equalsIgnoreCase("*")) {
				if (doccanastabod) flag = 0;
				if (docdevolucion || docpedido) flag = 1;
			}

			if (flag==0) {
				if (!rep.save()) return false;
			} else if (flag==1){
				if  (docpedido) {
					if (global != null) {
						if (!rep.save(global.pCantImpresion)) return false;
					} else {
						if (!rep.save(2)) return false;
					}
				}else if  (docfactura || docdevolucion) {
					//#CKFK20230118 Las facturas y NC ya solo van a tener una copia
					if (!rep.save(2)) return false;
					//if (!rep.save(1)) return false;
				}else{
					if (!rep.save(2)) return false;
				}
			} else if (flag==2){
				if (!rep.save(3)) return false;
			} else if (flag==3){
				if (!rep.save(3)) return false;
			}

		}catch (Exception e){
		    String ss=e.getMessage();
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return true;
	}

	public boolean buildPrintAppend(String corel,int reimpres,String modo,Boolean append) {
		int flag;

		modofact=modo;
		rep.clear();

		try{

			if (!buildHeader(corel,reimpres)) return false;
			if (!buildDetail()) return false;
			if (!buildFooter()) return false;

			flag=0;

			if (modofact.equalsIgnoreCase("TOL")) {
				if (docfactura && (reimpres==10)) flag=1;
				if (docfactura && (reimpres==4) || docdesglose) flag=0;
				if (doccanastabod){
					if (reimpres==1){
						flag=1;
					}else{
						flag=0;
					}
				}
				if (docrecibo && (reimpres==0)) flag=0;
				if (docdevolucion && (reimpres==1)) flag = 1;
				if (docpedido && (reimpres==1)) flag = 1;
			} else if(modofact.equalsIgnoreCase("*")) {
				if (doccanastabod) flag = 0;
				if (docdevolucion || docpedido) flag = 1;
			}

			if (flag==0) {
				if (!rep.saverep(append)) return false;
			} else if (flag==1){
				if (!rep.saverep(2,append)) return false;
			} else if (flag==2){
				if (!rep.saverep(3,append)) return false;
			} else if (flag==3){
				if (!rep.saverep(3,append)) return false;
			}

		}catch (Exception e){
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return true;
	}

	public boolean buildPrint(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb ) {
		
		rep.clear();
				
		if (!buildHeader(corel,reimpres,pCon,pdb)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintExt(String corel,int reimpres,String modo) {
		
		pemodo=modo;
		
		rep.clear();
		
		if (!buildHeader(corel,0)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintSimple(String corel,int reimpres) {
		
		rep.clear();
		
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}

	// Methods Prototypes
	
	protected boolean buildDetail() {
		return true;
	}
	
	protected boolean buildFooter() {
		
		return true;
	}	
	
	protected boolean loadDocData(String corel) {
		return true;
	}
	
	protected boolean loadHeadData(String corel) {
		nombre="";numero="";serie="";ruta="";vendedor="";cliente="";
		
		return true;
	}

    protected void saveHeadLines(int reimpres) {
        String s;
		String mPago,dPago;
		boolean vAnulada = false;

        rep.empty();rep.empty();

		if (docfactura || docdevolucion){
			rep.addc("             DGI");
		}

        for (int i = 0; i <lines.size(); i++) 		{

            s=lines.get(i);
            s=encabezado(s);

            if (docpedido) {
                s=s.replace("Factura serie","Pedido");
                s=s.replace("numero : 0","");
            }

            if (docrecibo) s=s.replace("Factura","Recibo");

            if(docdevolucion) s=s.replace("Factura","Recibo");

			if(doccanastabod){
				s=s.replace("Factura","Recibo");
			}

            //if (residx==1) {

            /*    residx=0;
            }*/

			if(docfactura && s.contains("ANULADA")){
				s = s.replace("ANULADA","");
				vAnulada = true;
			}

			if (!s.equalsIgnoreCase("@@")) {
				rep.add(s);
			}

			if(docrecibo && s.contains("RECIBO")){
				rep.add("DOCUMENTO NO FISCAL");
			}

			if ((docfactura || docdevolucion) && vAnulada){
				rep.add("ANULADA");
				vAnulada = false;
			}

			if(i==7){

				if (docfactura && !modofact.equalsIgnoreCase("TOL")) {
					rep.add("");
					rep.add(resol);
					rep.add(resfecha);
					rep.add(resvence);
					rep.add(resrango);
				}
			}
        }

		try{
			if (nit.length()>0) {
				String[] DVRuc = nit.split(" ");
				String dDV="";
				String ruc="";
				String receptor = "";
				if (DVRuc.length > 1) {
					ruc = DVRuc[0].trim();
					if (DVRuc[1].trim().equals("")){
						try{
							dDV =  StringUtils.right("00" + DVRuc[3].trim(),2);
						}catch (Exception ex){
							dDV = "";
						}
						receptor = DVRuc[2].trim();
					}else{
						dDV =  StringUtils.right("00" + DVRuc[2].trim(),2);
						receptor = DVRuc[1].trim();
					}
					nit = ruc + " DV:" + dDV + " Rec." + receptor;
				}
			}
		}catch (Exception ex){
			toast("Error " + ex.getMessage());
		}

        if (!emptystr(nit)) {
        	rep.add("RUC: "+nit);
		}

		if (!emptystr(rutapv)) {
			rep.add("Ruta Preventa: "+rutapv);
		}

        if (!emptystr(clidir)) rep.add("Dir : "+clidir);
        if(docfactura){
			if(condicionPago==4){

				mPago= "Credito";

				if(diacred==1){
					dPago="dia";
				}else{
					dPago="dias";
				}
				rep.add("Condiciones de pago: "+mPago+" "+diacred+" "+dPago);

			}else{
				mPago= "Contado";
				rep.add("Condiciones de pago: "+mPago);
			}
		}

		if (docfactura || docdevolucion) {
			rep.add("");
			rep.add("Punto de facturacion: "+Caja);
			rep.add("Protocolo de autorizacion: "+(NumAutorizacion==null?"":NumAutorizacion)+" de "+
					(FechaAutorizacion!=null?(FechaAutorizacion.equals("1900-01-01T00:00:00")?"":FechaAutorizacion):""));
		}

		rep.add("");

        if (docpedido) {
            rep.add("Fecha Pedido  : "+fsfecha);
            rep.add("Fecha Entrega : "+fsfechaent);
        } else {
            rep.add("Fecha : "+fsfecha);
        }

		rep.add("");

        //if (!emptystr(clicod)) rep.add("Codigo: "+clicod);

       if (!modofact.equalsIgnoreCase("TOL")){
		   if (!emptystr(add1)) {
			   rep.add("");
				rep.add(add1);
				if (!emptystr(add2)) rep.add(add2);
				rep.add("");
			}
        }

        if (docfactura && !(modofact.equalsIgnoreCase("TOL"))){

			rep.add("");
			if (docfactura && (reimpres==1)) rep.add("-------  R E I M P R E S I O N  -------");
			if (docfactura && (reimpres==10)) rep.add("-------  R E I M P R E S I O N  -------");
			if (docfactura && (reimpres==2)) rep.add("------  C O P I A  ------");
			if (docfactura && (reimpres==3)) rep.add("------       A N U L A D O      ------");
			//#HS_20181212 condición para factura pendiente de pago
			if(docfactura && (reimpres==4)) {
				rep.add("- P E N D I E N T E  D E  P A G O -");
				pendiente = reimpres;
			}
			if (docfactura && (reimpres==5)) rep.add("------  C O N T A B I L I D A D  ------");
			rep.add("");

		}else if ((docdevolucion || docpedido) && !(modofact.equalsIgnoreCase("TOL"))){

            //CKFK 2019-04-23 Consultar con Aaron
			rep.add("");
			if ((docdevolucion && (reimpres==1)) || (docpedido && (reimpres==1))) rep.add("-------  R E I M P R E S I O N  -------");
			if ((docdevolucion && (reimpres==2)) || (docpedido && (reimpres==2))) rep.add("------  C O P I A  ------");
			if ((docdevolucion && (reimpres==3)) || (docpedido && (reimpres==3))) rep.add("------       A N U L A D O      ------");
			rep.add("");

		}

		if (doccanastabod && !(modofact.equalsIgnoreCase("TOL"))){
			rep.add("");
			if (doccanastabod && (reimpres==1)) rep.add("-------  R E I M P R E S I O N  -------");
			rep.add("");
		}

    }

    protected String encabezado(String l) {
        String s,lu,a;
        int idx;

        //residx=0;
		if (emptystr(l)) return "";
        //lu=l.toUpperCase().trim();
        lu=l.trim();

        if (lu.length()==1 && lu.equalsIgnoreCase("N")) {
            s=nombre;//s=rep.ctrim(s);
			return s;
        }

        if (l.indexOf("dd-MM-yyyy")>=0) {
            s=DU.sfecha(DU.getActDateTime());
            l=l.replace("dd-MM-yyyy",s);return l;
        }

        if (l.indexOf("HH:mm:ss")>=0) {
            s=DU.shora(DU.getActDateTime());
            l=l.replace("HH:mm:ss",s);return l;
        }

		if (l.indexOf("@Numero") >=0) {

			int index = l.indexOf("@Numero");

			String temp = l.substring(index + 7, index + 9);
			String temp1 = l.substring(index + 9, index + 10);

			int ctemp= Integer.parseInt(temp);

			String str=StringUtils.leftPad("", ctemp, temp1);

			if (!serie.isEmpty()) {
				numero = StringUtils.right(str + numero, Integer.parseInt(temp));

				if (!numero.isEmpty()){
                    int ctemp1= Integer.parseInt(numero);
                    if (ctemp1==0) numero = StringUtils.leftPad("", ctemp);
                }

            }

			if (!numero.isEmpty()) {
				if (l.length() > index + numero.length() ){
					l = l.substring(0, index) + numero + l.substring(index + 10);
				}else{
					l = l.substring(0, index) + numero;
				}
			}

			if (l.indexOf("@Numero")>=0) {
				l = StringUtils.replace(l,"@Numero","");
				if (temp.length()>0 && temp1.length()>0){
					l=StringUtils.replace(l,temp+temp1,"");
				}
			}
		}

		if (StringUtils.upperCase(l).indexOf("@SerNum") != -1) {

			int index = StringUtils.upperCase(l).indexOf("@SerNum");

			String temp = l.substring(index + 7, index + 9);
			String temp1 = l.substring(index + 10, index + 11);

			int ctemp= Integer.parseInt(temp);

			String str=StringUtils.leftPad("", ctemp, temp1);

			numero = StringUtils.right(str + numero, Integer.parseInt(temp));

            if (!numero.isEmpty()){
                int ctemp1= Integer.parseInt(numero);
                if (ctemp1==0) numero = StringUtils.leftPad("", ctemp);
            }

			if ((l.length()) > index + serie.length() + numero.length()) {
				l = l.substring(0, index) + serie + numero + l.substring(index + 1 + serie.length() + numero.length());
			}else{
				l = l.substring(0, index) + serie + numero;
			}

			if (l.indexOf("@SerNum")>=0) {
				l = StringUtils.replace(l,"@SerNum","");
			}
		}

		idx=l.indexOf("@Serie");
        if (idx>=0) {

			if (!serie.isEmpty()) {
				if (l.length() > idx + serie.length()) {
					l = l.substring(0, idx) + serie + l.substring(idx + 6);
					//l = l.substring(0, idx) + serie + l.substring(idx + 6, idx + l.length() - idx - 6); #CKFK 20190701
				}else{
					l = l.substring(0, idx) + serie;
				}
			}

			if (l.indexOf("@Serie")>=0) {
				l = StringUtils.replace(l,"@Serie","");
			}

        }

		if ((l.indexOf("No.:")>=0) && (l.trim().length()==4)) {
			l = StringUtils.replace(l,"No.:","@@");
			l=l.trim();
		}

        idx=lu.indexOf("@Vendedor");
        if (idx>=0) {
        	rep.addc("");
            if (emptystr(vendedor)) return "@@";
            l=l.replace("@Vendedor",vendcod+" - "+vendedor);return l;
        }

        idx=lu.indexOf("@Ruta");
        if (idx>=0) {
            if (emptystr(ruta)) return "@@";
            l=l.replace("@Ruta",ruta);return l;
        }

        idx=lu.indexOf("@Cliente");
        if (idx>=0) {
            if (emptystr(cliente)) return "@@";
            /*if(l.length()>20){
				l=l.replace("@Cliente",clicod+" - "+cliente);
				return l;
			}*/
			//#CKFK20220201 Modifiqué la forma de generar los datos del cliente
            l=l.replace("@Cliente",clicod+" ");

            if (cliente.length()>prw){
            	l=l+cliente.substring(0,prw)+" "+cliente.substring(prw,cliente.length());
			}else{
				l=l+cliente;
			}
            return l;
        }

        return l;
    }


    // Private

	private boolean buildHeader(String corel,int reimpres) {
		
		lines.clear();
		
		try {
			Con = new BaseDatos(cont);
			opendb();
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);
			}

			loadHeadLines();

			try {
				Con.close();   
			} catch (Exception e1) {

			}
			
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			Toast.makeText(cont,"buildheader: "+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}

		saveHeadLines(reimpres);
		
		return true;
	}

	private boolean buildHeader(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb) {
		
		lines.clear();
		
		try {
				
			Con=pCon;db=pdb;
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);
			}
			loadHeadLines();

			saveHeadLines(reimpres);
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}		

		return true;
	}

	
	// Aux
	
	private boolean loadHeadLines() {
		Cursor DT;	
		String s,sucur;
		
		try {

			sql = "SELECT SUCURSAL FROM P_RUTA";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();
			sucur = DT.getString(0);

			sql="SELECT TEXTO FROM P_ENCABEZADO_REPORTESHH WHERE SUCURSAL='"+sucur+"' ORDER BY CODIGO";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return false;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				s=DT.getString(0);	
				lines.add(s);	

				DT.moveToNext();
			}

			return true;
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	public boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}

	//#CKFK20220116 Modifiqué esta función para que se manejen los segundo también
	//#CKFK20220416 Agregué que se mostrara la hora
	public String sfecha(long f) {
		long vy,vm,vd;
		String s;
		String hora="";

		if (String.valueOf(f).length()==12){
			hora=DU.shoraseg(f);
			f = f/1000000;
		}else{
			f = f/10000;
		}

		vy=(long) f/10000;
		f=f % 10000;
		vm=(long) f/100;
		f=f % 100;
		vd=(long) f;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"/";} else {s=s+"0"+String.valueOf(vd)+"/";}
		if (vm>9) { s=s+String.valueOf(vm)+"/20";} else {s=s+"0"+String.valueOf(vm)+"/20";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s + " " + hora;
	}

	public String sfecha_original(long f) {
		long vy,vm,vd;
		String s;
		
		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;
		
		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"-";} else {s=s+"0"+String.valueOf(vd)+"-";}  
		if (vm>9) { s=s+String.valueOf(vm)+"-20";} else {s=s+"0"+String.valueOf(vm)+"-20";}  
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);} 
		
		return s;
	}

	public String sfecha_dos(long f) {

		long vy,vm,vd;
		String s;

		if (String.valueOf(f).length()==12){
			f = f/1000000;
		}else{
			f = f/10000;
		}

		vy=(long) f/10000;
		f=f % 10000;
		vm=(long) f/100;
		f=f % 100;
		vd=(long) f;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"/";} else {s=s+"0"+String.valueOf(vd)+"/";}
		if (vm>9) { s=s+String.valueOf(vm)+"/";} else {s=s+"0"+String.valueOf(vm)+"/";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s;
	}

	public String sfecha_dos_original(long f) {
		long vy,vm,vd;
		String s;

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"-";} else {s=s+"0"+String.valueOf(vd)+"-";}
		if (vm>9) { s=s+String.valueOf(vm)+"-";} else {s=s+"0"+String.valueOf(vm)+"-";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s;
	}

	public String shora(int vValue) {
		int h,m;
		String sh,sm;

		h=vValue % 10000;
		m=h % 100;if (m>9) {sm=String.valueOf(m);} else {sm="0"+String.valueOf(m);}
		h=(int) h/100;if (h>9) {sh=String.valueOf(h);} else {sh="0"+String.valueOf(h);}

		return sh+":"+sm;
	}
	
	public String frmdecimal(double val,int ndec) {
		String ss="",ff="#,##0.";
		DecimalFormat ffrmint = new DecimalFormat("#,##0"); 
		
		if (ndec<=0) {		
			ss=ffrmint.format((int) val);return ss;
		}
		
		for (int i = 1; i <ndec+1; i++) {
			ff=ff+"0";
		}
		
		DecimalFormat decim = new DecimalFormat(ff);
		ss=decim.format(val);
		
		return ss;
	}

	public void toast(String msg) {
		Toast.makeText(cont,msg, Toast.LENGTH_SHORT).show();	
	}

	public void setAddlog(String methodname,String msg,String info) {

		BufferedWriter writer = null;
		FileWriter wfile;

		try {

			String fname = Environment.getExternalStorageDirectory()+"/roadlog.txt";
			wfile=new FileWriter(fname,true);
			writer = new BufferedWriter(wfile);

			writer.write("Método: " + methodname + " Mensaje: " +msg + " Info: "+ info );
			writer.write("\r\n");

			writer.close();

		} catch (Exception e) {
			//msgbox("Error " + e.getMessage());
		}
	}

	private void opendb() {
		
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
	    } catch (Exception e) {
	    	//Toast.makeText(cont,"Opendb "+e.getMessage(), Toast.LENGTH_LONG).show();
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");

		}
	}	
		
}
