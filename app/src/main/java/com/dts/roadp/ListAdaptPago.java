package com.dts.roadp;

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

public class ListAdaptPago extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsPago> itemDetailsrrayList;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptPago(Context context, ArrayList<clsClasses.clsPago> results) {
		itemDetailsrrayList = results;
		l_Inflater = LayoutInflater.from(context);
		selectedIndex = -1;
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
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_pago, null);
			holder = new ViewHolder();
			
			holder.lblTipo  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblNum = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblPValor);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
					
		holder.lblTipo.setText(itemDetailsrrayList.get(position).Tipo);
		holder.lblNum.setText(itemDetailsrrayList.get(position).Num);
		holder.lblValor.setText(itemDetailsrrayList.get(position).Valor);
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblTipo,lblNum,lblValor;
	}
	
}