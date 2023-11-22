package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class ListAdaptRepes extends BaseAdapter {

    private static ArrayList<clsClasses.clsRepes> items;

    private int selectedIndex;
    private String serr;

    private LayoutInflater l_Inflater;

    public ListAdaptRepes(Context context, ArrayList<clsClasses.clsRepes> results) {
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

            convertView = l_Inflater.inflate(R.layout.activity_list_view_repesaje, null);
            holder = new ViewHolder();

            holder.lblCod  = (TextView) convertView.findViewById(R.id.lblETipo);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPNum);
            holder.lblBolsas = (TextView) convertView.findViewById(R.id.textView62);
            holder.lblCan = (TextView) convertView.findViewById(R.id.lblPNum3);
            holder.lblTot = (TextView) convertView.findViewById(R.id.lblPNum4);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.lblCod.setText(items.get(position).sid);
            holder.lblPeso.setText(items.get(position).speso);
            holder.lblBolsas.setText(items.get(position).sbol);
            holder.lblCan.setText(items.get(position).scan);
            holder.lblTot.setText(items.get(position).stot);
        } catch (Exception e) {
            serr=e.getMessage();
        }

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCod,lblPeso,lblCan,lblTot,lblBolsas;
    }

}
