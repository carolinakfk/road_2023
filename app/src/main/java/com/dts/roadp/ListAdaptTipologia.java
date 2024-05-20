package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptTipologia extends BaseAdapter {
    private static ArrayList<clsClasses.clsP_tipologia> items;


    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptTipologia(Context context, ArrayList<clsClasses.clsP_tipologia> results) {
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
        ListAdaptTipologia.ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.activity_list_adapt_subcanal, null);
            holder = new ListAdaptTipologia.ViewHolder();

            holder.lblCodigo  = (TextView) convertView.findViewById(R.id.lbCodigo);
            holder.lblNombre = (TextView) convertView.findViewById(R.id.lbNombre);

            convertView.setTag(holder);
        } else {
            holder = (ListAdaptTipologia.ViewHolder) convertView.getTag();
        }

        holder.lblCodigo.setText(String.valueOf(items.get(position).codigo));
        holder.lblNombre.setText(items.get(position).nombre);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCodigo,lblNombre;
    }
}
