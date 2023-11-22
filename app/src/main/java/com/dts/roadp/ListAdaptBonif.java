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

public class ListAdaptBonif extends BaseAdapter {
	
	private static ArrayList<clsClasses.clsBonifProd> items;
		
	private int selectedIndex;
	
	private LayoutInflater l_Inflater;
	
	private DecimalFormat ffrmdec; 
	
	public ListAdaptBonif(Context context, ArrayList<clsClasses.clsBonifProd> results) {
		items = results;
		l_Inflater = LayoutInflater.from(context);
		selectedIndex = -1;
		ffrmdec = new DecimalFormat("#,##0.##");
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
		int flag;
	
		if (convertView == null) {
			
			convertView = l_Inflater.inflate(R.layout.activity_list_view_bonific, null);
			holder = new ViewHolder();
			
			holder.lblCod  = (TextView) convertView.findViewById(R.id.textView2);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.textView1);
			holder.lblTot = (TextView) convertView.findViewById(R.id.textView3);
			holder.lblPrec = (TextView) convertView.findViewById(R.id.textView4);
			holder.imgBand = (ImageView) convertView.findViewById(R.id.imageView1);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
				
		holder.lblDesc.setText(items.get(position).nombre);
		holder.lblCod.setText(items.get(position).id);
		holder.lblTot.setText(ffrmdec.format(items.get(position).cant));
		holder.lblPrec.setText(items.get(position).prstr);
		
		flag=items.get(position).flag;		
		if (flag==1) holder.imgBand.setVisibility(View.INVISIBLE);else holder.imgBand.setVisibility(View.VISIBLE);

		if (flag==-1) {

			holder.imgBand.setVisibility(View.INVISIBLE);
			holder.lblDesc.setText("");
			holder.lblCod.setText("");	
			holder.lblTot.setText("");
			holder.lblPrec.setText("FALTANTE");
		}
		
		
		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(0, 128, 0));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
		
		return convertView;
	}
	
	
	static class ViewHolder {
		TextView  lblCod,lblDesc,lblTot,lblPrec;
		ImageView  imgBand;
	}
	
}