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

public class ListAdaptObj extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsObj> itemDetailsrrayList;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptObj(Context context, ArrayList<clsClasses.clsObj> results) {
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
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_obj, null);
			holder = new ViewHolder();
			
			holder.lblNombre  = (TextView) convertView.findViewById(R.id.lblCFact);
			holder.lblCod = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblMeta = (TextView) convertView.findViewById(R.id.lblMeta);
			holder.lblAcum = (TextView) convertView.findViewById(R.id.lblpSaldo);
			holder.lblPerc = (TextView) convertView.findViewById(R.id.lblPerc);
			holder.lblFalta = (TextView) convertView.findViewById(R.id.lblFalta);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
					
		holder.lblNombre.setText(itemDetailsrrayList.get(position).Nombre);
		holder.lblCod.setText(itemDetailsrrayList.get(position).Cod);
		holder.lblMeta.setText(itemDetailsrrayList.get(position).Meta);
		holder.lblAcum.setText(itemDetailsrrayList.get(position).Acum);
		holder.lblPerc.setText(itemDetailsrrayList.get(position).Perc);
		holder.lblFalta.setText(itemDetailsrrayList.get(position).Falta);
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(0, 128, 0));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
		
	static class ViewHolder {
		TextView  lblNombre,lblCod,lblMeta,lblAcum,lblPerc,lblFalta;
	}
	
}