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

public class ListAdaptProd extends BaseAdapter {
	
	private static ArrayList<clsCD> items;


	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptProd(Context context, ArrayList<clsCD> results) {
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
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_cd2, null);
			holder = new ViewHolder();
			
			holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblExtra = (TextView) convertView.findViewById(R.id.textView1);
			holder.img1 = convertView.findViewById(R.id.imageView17);
			holder.lblFaltante = (TextView) convertView.findViewById(R.id.lblFaltante);
			holder.lblCantOriginal = (TextView) convertView.findViewById(R.id.lblCantOriginal);
			holder.lblPesoOriginal = (TextView) convertView.findViewById(R.id.lblPesoOriginal);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

        //JP20210614
		holder.lblCod.setText(items.get(position).Cod);
		//holder.lblCod.setText(items.get(position).Cod+"  "+items.get(position).um);
		holder.lblDesc.setText(items.get(position).Desc);
		holder.lblExtra.setText(items.get(position).Text);
		holder.lblFaltante.setText("Cant entregar: " + items.get(position).faltante);
		holder.lblCantOriginal.setText("Cant pedida: " + items.get(position).cantOriginal);
		holder.lblPesoOriginal.setText("Peso pedido: " + items.get(position).pesoOriginal);

		if (items.get(position).bandera) {
            holder.img1.setVisibility(View.VISIBLE);
        } else {
            holder.img1.setVisibility(View.INVISIBLE);
        }

		if (items.get(position).faltante>0) {
			holder.lblFaltante.setVisibility(View.VISIBLE);
		} else {
			holder.lblFaltante.setVisibility(View.GONE);
		}

		if (items.get(position).es_despacho) {
			holder.lblCantOriginal.setVisibility(View.VISIBLE);
			holder.lblPesoOriginal.setVisibility(View.VISIBLE);
		} else {
			holder.lblCantOriginal.setVisibility(View.GONE);
			holder.lblPesoOriginal.setVisibility(View.GONE);
		}

		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblCod,lblDesc,lblExtra,lblFaltante,lblCantOriginal,lblPesoOriginal;
		ImageView img1;
	}
	
}