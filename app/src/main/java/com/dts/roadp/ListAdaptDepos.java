package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCDB;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdaptDepos extends BaseAdapter {
	
	public String cursym;
	
	private static ArrayList<clsClasses.clsDepos> items;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;
	
	private DecimalFormat ffrmdec; 

	public ListAdaptDepos(Context context, ArrayList<clsClasses.clsDepos> results) {
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
		double vval,vtot;       
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_depos, null);
			holder = new ViewHolder();
			
			holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblCFact);
			holder.lblTot = (TextView) convertView.findViewById(R.id.lblDVTot);
			holder.imgBand = (ImageView) convertView.findViewById(R.id.imgImg);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
				
		vval=items.get(position).Valor;
		vtot=items.get(position).Total;
		
		holder.lblCod.setText(cursym+" "+ffrmdec.format(vval));
		holder.lblDesc.setText(items.get(position).Nombre);
		//holder.lblTot.setText(ffrmdec.format(vtot));
		holder.lblTot.setText(items.get(position).Banco);
		
		val=items.get(position).Bandera;
		iconid=R.drawable.blank48;
		if (val==1) {iconid=R.drawable.ok48;}
		holder.imgBand.setImageResource(iconid);
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblCod,lblDesc,lblTot;
		ImageView  imgBand;
	}
	
}