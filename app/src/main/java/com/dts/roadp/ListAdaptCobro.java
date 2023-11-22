package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdaptCobro extends BaseAdapter {
	
	public String cursym;
	
	private static ArrayList<clsClasses.clsCobro> items;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;
	
	private DecimalFormat ffrmdec; 

	public ListAdaptCobro(Context context, ArrayList<clsClasses.clsCobro> results) {
		items = results;
		l_Inflater = LayoutInflater.from(context);
		selectedIndex = -1;
		ffrmdec = new DecimalFormat("#,##0.00"); 
	}

	public void setSelectedIndex(int ind) {
	    selectedIndex = ind;
	    notifyDataSetChanged();
	}
	
	public void refreshItems() {
	    notifyDataSetChanged();
	}	
	
	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		int val,iconid;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_cobro, null);
			holder = new ViewHolder();
			
			holder.lblFact  = (TextView) convertView.findViewById(R.id.lblCFact);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblCValor);
			holder.lblSaldo  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblPago = (TextView) convertView.findViewById(R.id.lblCPago);
			holder.lblFIni  = (TextView) convertView.findViewById(R.id.lblPag);
			holder.lblFFin = (TextView) convertView.findViewById(R.id.lblCVence);
			
			holder.imgBand = (ImageView) convertView.findViewById(R.id.imgImg);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
					
		holder.lblFact.setText(items.get(position).Factura+" "+items.get(position).Tipo);
		holder.lblValor.setText(cursym+" "+ffrmdec.format(items.get(position).Valor));
		holder.lblSaldo.setText(cursym+" "+ffrmdec.format(items.get(position).Saldo));
		holder.lblPago.setText(cursym+" "+ffrmdec.format(items.get(position).Pago));
		holder.lblFIni.setText(items.get(position).fini);
		holder.lblFFin.setText(items.get(position).ffin);
		
		val=items.get(position).flag;
		iconid=R.drawable.blank24;
		if (val==1) {iconid=R.drawable.icok24;}
		holder.imgBand.setImageResource(iconid);
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblFact,lblValor,lblSaldo,lblPago,lblFIni,lblFFin;
		ImageView imgBand;
	}
	
}