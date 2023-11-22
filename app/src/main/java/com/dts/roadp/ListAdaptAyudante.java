package com.dts.roadp;

import android.content.Context;
import java.util.ArrayList;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsAyudante;

public class ListAdaptAyudante extends BaseAdapter {

    private static ArrayList<clsAyudante> itemDetailsrrayList;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptAyudante(Context context, ArrayList<clsAyudante> results) {
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

            convertView  = l_Inflater.inflate(R.layout.activity_list_adapt_ayudante, null);
            holder = new ViewHolder();

            holder.lblCod = (TextView) convertView.findViewById(R.id.lblIdAyudante);
            holder.lblDesc = (TextView) convertView.findViewById(R.id.lblAyudante);

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.lblCod.setText(itemDetailsrrayList.get(position).idayudante);
        holder.lblDesc.setText(itemDetailsrrayList.get(position).nombreayudante);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;

    }

    static class ViewHolder {
        TextView lblCod,lblDesc;
    }

}
