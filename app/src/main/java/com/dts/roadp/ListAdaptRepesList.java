package com.dts.roadp;

import android.widget.BaseAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class ListAdaptRepesList extends BaseAdapter {

    private static ArrayList<clsCD> items;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptRepesList(Context context, ArrayList<clsCD> results) {
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

            convertView = l_Inflater.inflate(R.layout.activity_list_view_repes_list, null);
            holder = new ViewHolder();

            holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPNum);
            holder.lblPrec = (TextView) convertView.findViewById(R.id.lblPValor);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblCod.setText(items.get(position).Cod);
        holder.lblPeso.setText(items.get(position).Desc);
        holder.lblPrec.setText(items.get(position).Text);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCod,lblPeso,lblPrec;
    }

}
