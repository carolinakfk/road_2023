package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsMenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdaptMenu extends BaseAdapter {
	private static ArrayList<clsMenu> items;
	
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptMenu(Context context, ArrayList<clsMenu> results) {
		items = results;
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
		int iconid;

		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.activity_list_view_menu, null);
			holder = new ViewHolder();
			
			holder.imgEst = (ImageView) convertView.findViewById(R.id.imgNext);
			holder.lblName = (TextView) convertView.findViewById(R.id.lblTrat);
		
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
			
		holder.lblName.setText(items.get(position).Name);
			
		holder.imgEst.setImageResource(R.drawable.ok48);
		
		if (items.get(position).Icon==1) holder.imgEst.setImageResource(R.drawable.mnupedido);
		if (items.get(position).Icon==2) holder.imgEst.setImageResource(R.drawable.comunicacion);
		if (items.get(position).Icon==3) holder.imgEst.setImageResource(R.drawable.printer48);
		if (items.get(position).Icon==4) holder.imgEst.setImageResource(R.drawable.anular);
		if (items.get(position).Icon==5) holder.imgEst.setImageResource(R.drawable.consulta);
		if (items.get(position).Icon==6) holder.imgEst.setImageResource(R.drawable.deposito);
		if (items.get(position).Icon==7) holder.imgEst.setImageResource(R.drawable.inventario);
		if (items.get(position).Icon==8) holder.imgEst.setImageResource(R.drawable.cierre_dia);
		if (items.get(position).Icon==9) holder.imgEst.setImageResource(R.drawable.utils);

		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(0, 128, 0));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		
		return convertView;
	}

	static class ViewHolder {
		ImageView imgEst;
		TextView  lblName;
	}
	
}
