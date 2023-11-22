package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCD;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdaptPromo extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsPromoItem> itemDetailsrrayList;
		
	private int selectedIndex;
	private DecimalFormat fnfrmd,fnfrm;
	
	private LayoutInflater l_Inflater;

	public ListAdaptPromo(Context context, ArrayList<clsClasses.clsPromoItem> results) {
		itemDetailsrrayList = results;
		l_Inflater = LayoutInflater.from(context);
		selectedIndex = -1;
		
		fnfrm = new DecimalFormat("#,##0");
		fnfrmd = new DecimalFormat("#,##0.00");
	}

	public void setSelectedIndex(int ind) {
	    selectedIndex = ind;
	    notifyDataSetChanged();
	}
	
	public void refreshItems() {
	    notifyDataSetChanged();
	}	
	
	public int getCount() {
		return itemDetailsrrayList.size();
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		double val;
		String tipo,perc,s;
		boolean vb;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_promo, null);
			holder = new ViewHolder();
			
			holder.lblNombre  = (TextView) convertView.findViewById(R.id.lblCFact);
			holder.lblRIni = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblRFin = (TextView) convertView.findViewById(R.id.lblRFin);
			holder.lblVal = (TextView) convertView.findViewById(R.id.lblVal);
			holder.lblTipo = (TextView) convertView.findViewById(R.id.lblTipo);
			holder.lblBon = (TextView) convertView.findViewById(R.id.lblPBon);
			holder.lblTotal = (TextView) convertView.findViewById(R.id.lblVolumen);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
			
		
		tipo=itemDetailsrrayList.get(position).Tipo;
		holder.lblTipo.setText(tipo);
		if (tipo.equalsIgnoreCase("D")) {
			perc=" %";
			holder.lblBon.setText("");
			holder.lblBon.setVisibility(View.GONE);
		} else {
			perc=""; 
			holder.lblBon.setVisibility(View.VISIBLE);
			holder.lblBon.setText(itemDetailsrrayList.get(position).Bon);
		}
		
		vb=itemDetailsrrayList.get(position).Porrango;
		
		holder.lblNombre.setText(itemDetailsrrayList.get(position).Nombre);
		
		val=itemDetailsrrayList.get(position).RIni;
		s=fnfrm.format(val);if (!vb) s="de "+s;
		holder.lblRIni.setText(s);
		
		val=itemDetailsrrayList.get(position).RFin;
		s=fnfrm.format(val);if (!vb) s="por cada "+s;
		holder.lblRFin.setText(s);
		
		val=itemDetailsrrayList.get(position).Valor;
		holder.lblVal.setText(fnfrmd.format(val)+perc);
		
		vb=itemDetailsrrayList.get(position).Porprod;
		if (vb) {
			holder.lblTotal.setVisibility(View.GONE);
		} else {
			holder.lblTotal.setVisibility(View.VISIBLE);
		}
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(0, 128, 0));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView lblNombre,lblRIni,lblRFin,lblVal;
		TextView lblTipo,lblBon,lblTotal;  
	}
	
}