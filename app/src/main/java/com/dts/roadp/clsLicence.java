package com.dts.roadp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class clsLicence {

	private Context cont;

	private int appid = 103157;
	private int maskid = 491387;

	public clsLicence(Context context) {
		cont=context; 
	}

	public String getMac() {		
		WifiManager manager = (WifiManager) cont.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}

	public String getDeviceName() {		
		return android.os.Build.MODEL;
	}

	public int getLicKey(String mac) {
		String s1,s2,s3;
		String[] sp;
		int val,v1,v2,v3;

		try {
			sp=mac.split(":");

			s1=sp[0];v1=Integer.parseInt(s1,16);
			s2=sp[4];v2=Integer.parseInt(s2,16);
			s3=sp[5];v3=Integer.parseInt(s3,16);		
		} catch (Exception e) {
			return 0;
		}

		val=v3*65536+256*v2+v1;

		return val;
	}

	public String getLKey(int lickey) {
		int val, mval;

		try {
			mval = lickey % 957;
			val=lickey+appid+mval;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			return "*";
		}
	}

	public String encodeLicence(int lickey) {
		int val,mval;

		try {
			mval = lickey % 957;
			val=lickey+appid+mval;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			return "*";
		}
	}

	public String encodeValue(int lickey,int val) {
		try {
			val = val+lickey + maskid;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			return "*";
		}  
	}
	
	public int decodeValue(String hexval) {
		int val;
		
		try {
			val=Integer.parseInt(hexval,16);
			val=val-maskid;
		} catch (NumberFormatException e) {
			val=0;
		}
		
		return val;
	}
	
	
}
