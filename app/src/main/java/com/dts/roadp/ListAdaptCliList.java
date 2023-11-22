package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsCDB;

import java.util.ArrayList;

public class ListAdaptCliList extends BaseAdapter {

	private static ArrayList<clsCDB> items;

	private int selectedIndex;

	private LayoutInflater l_Inflater;

	public ListAdaptCliList(Context context, ArrayList<clsCDB> results) {
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
		int val,cobro,ppago,iconid,despacho;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_clilist, null);
			holder = new ViewHolder();
			
			holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.imgBand = (ImageView) convertView.findViewById(R.id.imgNext);
			holder.imgCobro = (ImageView) convertView.findViewById(R.id.imageView8);
			holder.imgPPago = (ImageView) convertView.findViewById(R.id.imageView7);
			holder.imgDespacho = (ImageView) convertView.findViewById(R.id.imgDespacho);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
					
		holder.lblCod.setText(items.get(position).Cod+" "+items.get(position).Adds);
		holder.lblDesc.setText(items.get(position).Desc);
		
		val= items.get(position).Bandera;
		cobro= items.get(position).Cobro;
		ppago= items.get(position).ppend;
		despacho= items.get(position).prefacturas;

		iconid=R.drawable.blank24;
		if (val>0) iconid=R.drawable.disable24;
		if (val==0) iconid=R.drawable.icok24;
		holder.imgBand.setImageResource(iconid);

		if (cobro==1) holder.imgCobro.setImageResource(R.drawable.cobro48);else holder.imgCobro.setImageResource(R.drawable.blank24);
		if (ppago==1) holder.imgPPago.setImageResource(R.drawable.pago_pend);else holder.imgPPago.setImageResource(R.drawable.blank24);
		if (despacho==1) holder.imgDespacho.setImageResource(R.drawable.pedido);else holder.imgDespacho.setImageResource(R.drawable.blank24);

		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblCod,lblDesc;
		ImageView  imgBand,imgCobro,imgPPago,imgDespacho;
	}
	
}