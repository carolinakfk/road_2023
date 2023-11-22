package com.dts.roadp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	
	public DateUtils() {
	}

	//#CKFK20220116 Modifiqué esta función para que se manejen los segundo también
	public String sfecha(long f) {
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
		if (vm>9) { s=s+String.valueOf(vm)+"/20";} else {s=s+"0"+String.valueOf(vm)+"/20";}  
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);} 
		
		return s;
	}
	
	public String sfechash(long f) {
		int vy,vm,vd;
		String s;
		
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		
		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"/";} else {s=s+"0"+String.valueOf(vd)+"/";}  
		if (vm>9) { s=s+String.valueOf(vm);} else {s=s+"0"+String.valueOf(vm);}  
		
		return s;
	}
	
	public String shora(long vValue) {
		long h,m;
		String sh,sm;
			
		h=vValue % 10000;
		m=h % 100;if (m>9) {sm=String.valueOf(m);} else {sm="0"+String.valueOf(m);}
		h=(int) h/100;if (h>9) {sh=String.valueOf(h);} else {sh="0"+String.valueOf(h);}
			
		return sh+":"+sm;
	}

	public String shoraseg(long vValue) {
		long h,m,s;
		String sh,sm,ss;

		h=vValue % 1000000;
		m=h % 10000 / 100;if (m>9) {sm=String.valueOf(m);} else {sm="0"+String.valueOf(m);}
		s=h % 100;if (s>9) {ss=String.valueOf(s);} else {ss="0"+String.valueOf(s);}
		h=(int) h/10000;if (h>9) {sh=String.valueOf(h);} else {sh="0"+String.valueOf(h);}

		return sh+":"+sm+":"+ss;
	}

    public String sfechalocal(long f) {

        if (f==0) return "";

        String s=sfecha(f);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(s);
            s = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } catch (Exception e) {
            s="";
        }

        return s;
    }

	public String geActTimeStr(){
		long f,ch,cm,cs;
		String s,ss;
		
		final Calendar c = Calendar.getInstance();
		
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs = c.get(Calendar.SECOND);
		
		s=""+ch;if (ch<10) s="0"+s;
		ss=""+cm;if (cm<10) ss="0"+ss;s=s+":"+ss;
		ss=""+cs;if (cs<10) ss="0"+ss;s=s+":"+ss;
		
		return s;
	}
	
	public String sSecond(){
		long cs;
		String sss;
		
		final Calendar c = Calendar.getInstance();
		cs=c.get(Calendar.SECOND);
				
		sss=""+cs;
		if (cs<10) sss="0"+sss;
		
		return sss;
	}

	//#CKFK20220117 Lo modifiqué porque la fecha ahora se maneja con segundos
	public String univfecha(long f) {
		long vy,vm,vd,m,h;
		String s;
		
		//yyyyMMdd hh:mm:ss

		if (String.valueOf(f).length()==12){
			f  = Long.valueOf( String.valueOf(f).substring(0,10));
		}

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;
		h= (long) f/100;
		m= f % 100;
		
		s="20";
		if (vy>9) s=s+vy; else s=s+"0"+vy; 
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;  
		s=s+" ";  
		if (h>9)  s=s+h;  else s=s+"0"+h;
		s=s+":";
		if (m>9)  s=s+m;  else s=s+"0"+m;
		s=s+":00";
		
		return s;
	}

	//#CKFK 20190509 Modifiqué para quitale el 00 que le puso Henry
	public String univfechasinhora(long f) {
		int vy,vm,vd;
		String s;

		//yyyyMMdd

		vy=(int) f/10000;f=f % 10000;
		vm=(int) f/100;f=f % 100;
		vd=(int) f;

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s=vy+" "+vm+":"+vd;

		return s;
	}
	
	public String univfechaseg() {

		long f,fecha, vy,vm,vd;;
		int cyear,cmonth,cday,ch,cm, cs;
		String s;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		//yyyyMMdd hh:mm:ss
		
		vy=cyear;
		vm=cmonth;
		vd=cday;
			
		s=""+vy; 
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s = s + " ";
		if (ch>9) s=s+ch; else s=s+"0"+ch; s=s+ ":";
		if (cm>9) s=s+cm; else s=s+"0"+cm; s=s+ ":";
		if (cs>9) s=s+cs; else s=s+"0"+cs;

		return s;
	}

	//#CKFK20220216 Modifiqué esta función para que se manejen los segundos también
	public String univfechaext(long f) {
		long vy,vm,vd;
		long vFecha,vhora,vmin,vseg;
		String s, ss, sm, sh;

		//yyyyMMdd hh:mm:ss

		if (String.valueOf(f).length()==12){
			vFecha = f/1000000;
			vhora  = Long.valueOf( String.valueOf(f).substring(6,12));
			vmin=vhora % 10000 / 100;if (vmin>9) {sm=String.valueOf(vmin);} else {sm="0"+String.valueOf(vmin);}
			vseg=vhora % 100;if (vseg>9) {ss=String.valueOf(vseg);} else {ss="0"+String.valueOf(vseg);}
			vhora=(int) vhora/10000;if (vhora>9) {sh=String.valueOf(vhora);} else {sh="0"+String.valueOf(vhora);}

			s =  vFecha + " " + sh+":"+sm+":"+ss;

		}else{

			vy=(long) f/10000;
			f=f % 10000;
			vm=(long) f/100;
			f=f % 100;
			vd=(long) f;

			s=""+vy;
			if (vm>9) s=s+vm; else s=s+"0"+vm;
			if (vd>9) s=s+vd; else s=s+"0"+vd;
			s=vy+" "+vm+":"+vd+":00"; //#HS_20181128_1102 Agregue " "+vm+":"+vd+":00" para que devolviera la hora.

		}

		return s;
	}

	public String univfechaext_original(long f) {
		long vy,vm,vd;
		String s;

		//yyyyMMdd hh:mm:ss

		vy=(long) f/10000;f=f % 10000;
		vm=(long) f/100;f=f % 100;
		vd=(long) f;

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s=vy+" "+vm+":"+vd+":00"; //#HS_20181128_1102 Agregue " "+vm+":"+vd+":00" para que devolviera la hora.

		return s;
	}

	//#CKFK20220116 Modifiqué esta función para los casos en que la fecha se maneja con segundos
	public String univfechasql(long f) {
		long vy,vm,vd;
		String sy,sm,sd;

		//yyyy-MM-dd

		if (String.valueOf(f).length()==12){
			f  = Long.valueOf( String.valueOf(f).substring(0,10));
		}

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		if (vy>9) sy="20"+vy; else sy="200"+vy;
		if (vm>9) sm=""+vm; else sm="0"+vm;
		if (vd>9) sd=""+vd; else sd="0"+vd;

		return sy+sm+sd;
	}

	public String univfechasql_original(long f) {
		long vy,vm,vd;
		String sy,sm,sd;

		//yyyy-MM-dd

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		if (vy>9) sy="20"+vy; else sy="200"+vy;
		if (vm>9) sm=""+vm; else sm="0"+vm;
		if (vd>9) sd=""+vd; else sd="0"+vd;

		return sy+sm+sd;
	}

	public long ffecha00(long f) {
		f=(long) f/10000;
		f=f*10000;
		return f;
	}
	
	public long ffecha24(long f) {
		f=(long) f/10000;
		f=f*10000+2359;
		return f;
	}

	//#HS_20181121_1008 Funcion para fecha sin hora.
	public long cfechaSinHora(int year,int month, int day) {
		long c;
		c=year % 100;
		c=c*10000+month*100+day;

		//return	c;
		return c; //*10000;
	}
	  
	public long cfecha(int year,int month, int day) {
		long c;
		c=year % 100;
		c=c*10000+month*100+day;

		return c*1000000;
	}
	
	public long parsedate(long date,int hour,int min,int seg) {
		long f;
		f=date+10000*hour+100*min+seg;
		return f;
	}
		
	public int getyear(long f) {
		int vy;
		long tmp;
				
		tmp=(long) f/1000000;
		f=tmp / 10000;
		vy=(int)f+2000;
		
		return vy;
	}
	
	public int getmonth(long f) {
		long vy;
		int vm;
				
		vy=(long) f/1000000;
		f=vy % 1000000;
		f=(int) f%10000;
		vm=(int)f /100;
				
		return vm;
	}
	
	public int getday(long f) {
		long vy;
		int vm,vd;

		vy=(long) f/1000000;
		f=vy % 1000000;
		vm=(int) f%10000;
		vd=(int)f %100;
		
		return vd;
	}
	
	public int LastDay(int year,int month) {
		int m,y,ld;
		
		m=month % 2;
		if (m==1) {
			ld=31;
		} else {
			ld=30;
		}
		
		if (month==2) {
			ld=28;
			if (year % 4==0) {ld=29;}
		}

		return ld;
		
	}
	
	public long timeDiff(long v1,long v2) {
		long h1,m1,h2,m2,vm1,vm2,dif;

		h1=v1 % 10000;m1=h1 % 100;h1=(int) h1/100;
		vm1=h1*60+m1;
		
		h2=v2 % 10000;m2=h2 % 100;h2=(int) h2/100;
		vm2=h2*60+m2;
		
		dif=vm1-vm2;
		if (dif<0) dif=-dif;
		
		return dif;
		
	}
	
	public long dayofweek(long f) {
		long y,m,d,dw;
	     
		final Calendar c = Calendar.getInstance();
		
		c.set(getyear(f), getmonth(f)-1, getday(f));
			
	    dw=c.get(Calendar.DAY_OF_WEEK);
	    
	    if (dw==1) dw=7;else dw=dw-1;
	    
	    return dw;
	}
	
	public long getActDate(){
		long f;
		int cyear,cmonth,cday;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		
		f=cfecha(cyear,cmonth,cday);
		
		return f;
	}

	//#HS_20181121_1008 Funcion para obtener la fecha sin hora.
	public long getFechaActual(){
		long f,fecha;
		int cyear,cmonth,cday,ch,cm;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		//#HS_20181120_1725 Campo de fecha sin hora.
		f=cfechaSinHora(cyear,cmonth,cday);
		fecha=f;

		return fecha;
	}
	
	public long getActDateTime(){
		long f,fecha;
		int cyear,cmonth,cday,ch,cm,cs;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		f=cfecha(cyear,cmonth,cday);
		fecha=f+ch*10000+cm*100+cs;

		return fecha;
	}

	public String getActDateStr(){
		long f;
		int cyear,cmonth,cday;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		
		f=cfecha(cyear,cmonth,cday);
		
		return sfecha(f);
	}
	
	public long getCorelBase(){
		long f;
		int cyear,cmonth,cday,ch,cm,cs,vd,vh;
		
		final Calendar c = Calendar.getInstance();
		
		cyear = c.get(Calendar.YEAR);cyear=cyear % 10;
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);
				
		vd=cyear*384+cmonth*32+cday;
		vh=ch*3600+cm*60+cs;
		
		f=vd*100000+vh;
		f=f*100;
		
		return f;
	}

	public String getFechaCompleta() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String strDate = sdf.format(c.getTime());

		return strDate;
	}
	
}