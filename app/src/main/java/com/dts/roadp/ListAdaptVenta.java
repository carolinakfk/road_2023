package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsVenta;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdaptVenta extends BaseAdapter {
	
	public String cursym;
	
	private static ArrayList<clsVenta> items;
		
	private int selectedIndex;
	private LayoutInflater l_Inflater;
	private DecimalFormat frmdec; 

	public ListAdaptVenta(Context context, ArrayList<clsVenta> results) {
		items = results;
		l_Inflater = LayoutInflater.from(context);
		
		selectedIndex = -1;
		frmdec = new DecimalFormat("#,##0.00");
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
		double val;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_venta, null);
			holder = new ViewHolder();
			
			holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblNombre = (TextView) convertView.findViewById(R.id.lblCFact);
			holder.lblCant = (TextView) convertView.findViewById(R.id.lblCant);
			holder.lblPrec = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblFecha);
			holder.lblTot = (TextView) convertView.findViewById(R.id.lblTot);
			holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPeso);
			holder.lblPrecio = (TextView) convertView.findViewById(R.id.lblPrecio);
            holder.lblPE = (TextView) convertView.findViewById(R.id.textView97);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
							
		holder.lblCod.setText(items.get(position).Nombre);
		holder.lblNombre.setText(items.get(position).Cod);
		
		//val=items.get(position).Cant;
		//holder.lblCant.setText(frmdec.format(val)+" "+items.get(position).um);
		holder.lblCant.setText(items.get(position).val);
		//val=items.get(position).Prec;
		//holder.lblPrec.setText(frmdec.format(val));
		holder.lblPrec.setText("");
		holder.lblDesc.setText(items.get(position).sdesc);
		val=items.get(position).Total;
		holder.lblTot.setText(cursym+" "+frmdec.format(val));
		holder.lblPrecio.setText(String.valueOf(items.get(position).precio));

		holder.lblPeso.setText(items.get(position).valp);
		if (items.get(position).valp.equalsIgnoreCase(".")) holder.lblPeso.setVisibility(View.GONE);
		if (items.get(position).Peso==0) holder.lblPeso.setVisibility(View.GONE);
        holder.lblPE.setText(items.get(position).PE);

		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblCod, lblNombre,lblCant,lblPrec,lblDesc,lblTot,lblPeso,lblPrecio,lblPE;
	}
	
}