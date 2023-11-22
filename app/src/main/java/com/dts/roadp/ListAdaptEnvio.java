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

public class ListAdaptEnvio extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsEnvio> itemDetailsrrayList;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptEnvio(Context context, ArrayList<clsClasses.clsEnvio> results) {
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
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_envio, null);
			holder = new ViewHolder();
			
			holder.lblName  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblEnv = (TextView) convertView.findViewById(R.id.lblEEnv);
			holder.lblPend = (TextView) convertView.findViewById(R.id.lblEPend);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
					
		holder.lblName.setText(itemDetailsrrayList.get(position).Nombre);
		holder.lblEnv.setText(""+itemDetailsrrayList.get(position).env);
		holder.lblPend.setText(""+itemDetailsrrayList.get(position).pend);
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(0, 128, 0));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblName,lblEnv,lblPend;
	}
	
}