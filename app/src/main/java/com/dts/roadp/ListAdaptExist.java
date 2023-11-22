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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListAdaptExist extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsExist> items;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;

	public ListAdaptExist(Context context, ArrayList<clsClasses.clsExist> results) {
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
		int ic;
		String lote;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_exist, null);
			holder = new ViewHolder();

			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblCod = (TextView) convertView.findViewById(R.id.textView18);
			holder.lblLote  = (TextView) convertView.findViewById(R.id.lblETipo);
            holder.lblLotem  = (TextView) convertView.findViewById(R.id.textView19);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblPValor);
			holder.lblValorM = (TextView) convertView.findViewById(R.id.lblValorM);
            holder.lblValorT = (TextView) convertView.findViewById(R.id.lblValorT);
			holder.lblPeso = (TextView) convertView.findViewById(R.id.lblepeso);
			holder.lblPesoM = (TextView) convertView.findViewById(R.id.lblepesom);
            holder.lblPesoT = (TextView) convertView.findViewById(R.id.lblepesoT);

			holder.reltitle= (RelativeLayout) convertView.findViewById(R.id.relexitit);
			holder.relbueno= (RelativeLayout) convertView.findViewById(R.id.relexist);
			holder.relmalo = (RelativeLayout) convertView.findViewById(R.id.relexistm);
			holder.reltot = (RelativeLayout) convertView.findViewById(R.id.relexistt);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.lblDesc.setText(items.get(position).Cod);
		holder.lblCod.setText(items.get(position).Desc);

		holder.lblLote.setText(items.get(position).Lote);
        holder.lblLotem.setText(items.get(position).Lote);
		holder.lblPeso.setText(items.get(position).Valor);
		holder.lblValor.setText(items.get(position).Peso);

		holder.lblPesoM.setText(items.get(position).ValorM);
		holder.lblValorM.setText(items.get(position).PesoM);

        holder.lblPesoT.setText(items.get(position).ValorT);
		holder.lblValorT.setText(items.get(position).PesoT);

		holder.reltitle.setVisibility(View.GONE);
		holder.relbueno.setVisibility(View.GONE);
		holder.relmalo.setVisibility(View.GONE);
		holder.reltot.setVisibility(View.GONE);

		ic=items.get(position).items;
		lote=items.get(position).Lote;

		switch (items.get(position).flag) {
			case 0:
				holder.reltitle.setVisibility(View.VISIBLE);
				holder.lblCod.setVisibility(View.VISIBLE);break;
			case 1:
				holder.relbueno.setVisibility(View.VISIBLE);break;
            case 2:
                holder.relmalo.setVisibility(View.VISIBLE);break;
            case 3:
                holder.reltot.setVisibility(View.VISIBLE);break;
		}
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblLote,lblLotem,lblCod,lblDesc,lblValor,lblValorM,lblValorT,lblPeso,lblPesoM,lblPesoT;
		RelativeLayout reltitle,relbueno,relmalo,reltot;
	}
	
}