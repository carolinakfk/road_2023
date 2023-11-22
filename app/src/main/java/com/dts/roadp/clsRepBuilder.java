package com.dts.roadp;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class clsRepBuilder {
	
	public String fname,cursym;
	public int prw;
	
	public  ArrayList<String> items=new ArrayList<String>();
	private Context cont;
	private BufferedWriter writer = null;
	private FileWriter wfile;
	private DecimalFormat decfrm;
	
	private int seplen,prwq,prwt,prwh,decimp;
	private double aux;
	private String frmstr,ts;
	
	public clsRepBuilder(Context context,int printwidth,boolean regular,String cursymbol,int decimpres, String archivo) {
		
		cont=context;
		//AT 20211029 Quitar comentario luego de realizar pruebas
		//prw=printwidth
		prw=32;
		seplen=prw;
		cursym=cursymbol;
		decimp=decimpres;

		aux=prw;
		prwq=(int) Math.floor(aux/4);
		prwt=(int) Math.floor(aux/3);
		prwh=(int) Math.floor(aux/2);
		
		System.setProperty("line.separator","\r\n");
		if (regular) {
			if (!archivo.isEmpty()){
				fname = Environment.getExternalStorageDirectory()+"/"+archivo;
			}else{
				fname = Environment.getExternalStorageDirectory()+"/print.txt";
			}
		} else {
			fname = Environment.getExternalStorageDirectory()+"/SyncFold/findia.txt";	
		}
		
		decfrm = new DecimalFormat("#,##0.00");
	}	
	
		
	// Main
	
	public String build() {
		String s,ss="";
		
		if (items.size()==0) return "";
		
		try {
		    for (int i = 0; i < items.size(); i++) {
			   	s=items.get(i);
			   	ss=ss+s+"\r\n";
			}
		    
		    return ss;
		} catch(Exception e){
			return "";
		}

	}

	public boolean save(){
		return saverep(false);
	}

	public boolean saveappend(){
		return saverep(true);
	}

	public boolean saverep(boolean append){
		String s;
		int lns=0;

		if (items.size()==0) return true;

		try {

			wfile=new FileWriter(fname,append);
			writer = new BufferedWriter(wfile);

			if (append) {
				writer.write("\r\n");
				writer.write("\r\n");
			}

			for (int i = 0; i < items.size(); i++) {
				//#CKFK20221230 Agregar el QR
				try {
					s="";
					//if (!items.get(i).contains("QRCode:")){
						s = items.get(i).trim();
					//}
				     /*else{
							s = trim(items.get(i));
						}*/
				} catch (Exception e) {
					s = "";
				}

				//#CKFK20221228 Corrección para que salga completo el QR y la dirección del cliente
				if (s.length()>prw && !s.contains("QRCode:")){
					int largo=s.length();
					String sOriginal=s;
					while (largo>prw) {
						writer.write(s.substring(0,prw));
						writer.write("\r\n");
						s=sOriginal.substring(prw,sOriginal.length());
						sOriginal=s;
						largo=s.length();
					}
				}

				writer.write(s);writer.write("\r\n");lns++;
			}

			writer.close();
			items.clear();

		} catch(Exception e){
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public boolean save(int cnt){
		return saverep(cnt,false);
	}

	public boolean saveappend(int cnt){
		return saverep(cnt,true);
	}

	public boolean saverep(int cnt,boolean append){
		String s;
		int lns=0;

		if (items.size()==0) return true;

		try {

			wfile=new FileWriter(fname,append);
			writer = new BufferedWriter(wfile);

			if (append) {
				writer.write("\r\n");
				writer.write("\r\n");
			}

			for (int j = 0; j < cnt; j++) {

				for (int i = 0; i < items.size(); i++) {
					try {
						s="";
						//if (!items.get(i).contains("QRCode:")){
							s = items.get(i).trim();
						//}
						/*else{
							s = trim(items.get(i));
						}*/
					} catch (Exception e) {
						s = "";
					}

					//#CKFK20221228 Corrección para que salga completo el QR y la dirección del cliente
					if (s.length()>prw && !s.contains("QRCode:")){
						int largo=s.length();
						String sOriginal=s;
						while (largo>prw) {
							writer.write(s.substring(0,prw));
							writer.write("\r\n");
							s=sOriginal.substring(prw,sOriginal.length());
							sOriginal=s;
							largo=s.length();
						}
					}

					writer.write(s);
					writer.write("\r\n");
					lns++;
				}

				writer.write("\r\n");
				writer.write("\r\n");
				writer.write("\r\n");
				writer.write("\r\n");

			}

			writer.close();
			items.clear();

		} catch(Exception e){
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public void clear(){
		items.clear();
	}

	
	// Simple 
	
	public void empty() {
		items.add(" ");	
	}
	
	public void line() {
		char[] fill = new char[seplen];
		Arrays.fill(fill, '-');
		String s = new String(fill);
		
		items.add(s);
	}
	
	public String ltrim(String ss,int sw) {
		int l=ss.length();
		if (l>sw) {
			ss=ss.substring(0,sw);	
		} else {
			frmstr="%-"+sw+"s";	
			ss=String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	public String rtrim(String ss,int sw) {
		int sl,l;
		String sp="";

		ss=ss.trim();
        l=ss.length();

		if (l>=sw) {
			ss=ss.substring(0,sw);
		} else {
		    /*
		    sl=sw-l;
            for (int i = 0; i <sl; i++) {
                sp=sp+" ";
            }
            ss=sp+ss;  */
			frmstr="%"+sw+"s";
			ss=String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	public String ctrim(String ss) {
		int l=ss.length();
		if (l>prw) {
			ss=ss.substring(0,prw);	
		} else {
			int ldisp=(prw-l)/2;
			frmstr=fillempty(ldisp);	
			ss=frmstr+ss;
		}
		
		return ss;		
	}
	
	
	// Composed 

	public void add(String tn){
		items.add(tn);
	}
	
	public void addc(String tn){
		items.add(ctrim(tn));
	}
	
	public void add3lrr(String s1,String s2,String s3) {
		ts=ltrim(s1,prwt-2)+rtrim(s2,prwh-4)+rtrim(s3,prwh-4);
		items.add(ts);
	}
	
	public void add3lrr(String s1,String s2,double v3) {
		String s3=decfrm.format(v3);
		
		ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
		items.add(ts);
	}
	
	public void add3lrr(String s1,double v2,double v3) {
		String s2,s3;
		
		s2=cursym+decfrm.format(v2);
		s3=cursym+decfrm.format(v3);
		
		ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
		items.add(ts);
	}
	
	public void add3fact(String s1,double v2,double v3) {
		String s2,s3;
		
		s2=cursym+decfrm.format(v2);
		s3=cursym+decfrm.format(v3);
		
		ts=ltrim(s1,prwh)+rtrim(s2,prwq)+rtrim(s3,prwq);
		items.add(ts);
	}

	public void add3fact(String s1,String s2,String s3) {
		ts=ltrim(s1,prwh)+rtrim(s2,prwq)+rtrim(s3,prwq);
		items.add(ts);
	}

	public void add2string(String s1,String s2) {
		ts=ltrim(s1,prwh)+rtrim(s2,prwh);
		items.add(ts);
	}

	public void addtot(String s1,String val) {
		ts=ltrim(s1,prw-17)+" "+rtrim(val,12);
		items.add(ts);
	}

    public void add2ss1(String s1,String val) {
        ts=ltrim(s1,prw-3)+" "+rtrim(val,2);
        items.add(ts);
    }

    public void addtotD(String s1,double val) {
		String sval;
		sval = Double.toString(val);
		ts=ltrim(s1,prwh)+" "+rtrim(sval,prwh-1);
		items.add(ts);
	}

	public void addtot(String s1,double val) {
		String sval;
		
		sval=cursym+decfrm.format(val);
		ts=ltrim(s1,prw-13)+" "+rtrim(sval,12);
		items.add(ts);
	}

	public void addCanasta(String s1,double val) {
		String sval;

		sval = Double.toString(val);
		ts=ltrim(s1,prw-13)+" : "+rtrim(sval,8);
		items.add(ts);
	}

	public void addtotpeso(String s1,double val) {
		String sval;

		sval = Double.toString(val);
		ts=ltrim(s1,prw-13)+" "+rtrim(sval,12);
		items.add(ts);
	}

	public void addtotsp(String s1,double val) {
		String sval;
		
		sval=cursym+decfrm.format(val);
		ts=ltrim(s1,prw-14)+"  "+rtrim(sval,12)+" ";
		items.add(ts);
	}
	
	public void addtot(String s1,String val,int wid) {
		ts=ltrim(s1,prw-wid-1)+" "+rtrim(val,wid);
		items.add(ts);
	}
	
	public void addt(String tn){
		items.add("\t"+tn);	
	}
	
	public void add(String tn,int tbs){
		String ss="";
		
		for (int i = 0; i <tbs; i++) {
			ss+="\t";
		}
		
		items.add(ss+tn);	
	}
	
	public void addg(String s1,String s2,double val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,15);ss=s1+"\t";
		s2="("+s2+")";
		s2=StringUtils.leftPad(s2,6);ss=ss+s2+"\t";
		s3=decfrm.format(val);
		s3=StringUtils.leftPad(s3,12);ss+=s3;
		
		items.add(ss);
		
	}
	
	public void addg(String s1,String s2,String val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,15);ss=s1+"\t";
		s2=StringUtils.leftPad(s2,6);ss=ss+s2+"\t";
		s3=val;
		s3=StringUtils.leftPad(s3,12);ss+=s3;
		
		items.add(ss);
		
	}

	public void addp(String s1,String val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,16);ss=s1+"";
		s3=val;
		s3=StringUtils.leftPad(s3,16);ss+=s3;
		
		items.add(ss);
		
	}
	
	public void addpu(String s1,String s2,int mw) {
		String ss,st;
		
		ss=StringUtils.rightPad(s1,mw);
		st=StringUtils.rightPad(s2,mw);		
		items.add(ss+" "+st);		
	}
	
	
	
	// Aux
	
	public String frmdec(double val) {
		return cursym+decfrm.format(val);
	}

	//#CKFK20220201 Modifiqué el trim para el cliente
	private String trim(String ss) {
		int l=ss.length();
		if (!ss.contains("Cliente:")){
			if (l>prw) ss=ss.substring(0,prw);
		}
		return ss;
	}
	
	private String fillempty(int cn) {
		char[] fill = new char[cn];
		Arrays.fill(fill, ' ');
		String s = new String(fill);
		
		return s;
	}
	
}
