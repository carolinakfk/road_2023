package com.dts.roadp;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsBarras;
import java.util.ArrayList;

public class list_view_barras extends BaseAdapter {

    private static ArrayList<clsBarras> items;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public list_view_barras(Context context, ArrayList<clsBarras> results) {
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
            convertView = l_Inflater.inflate(R.layout.activity_list_view_barras, null);
            holder = new ViewHolder();

            holder.lblCod  = (TextView) convertView.findViewById(R.id.lblCBarra);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPesoBarra);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblCod.setText(items.get(position).barra);
        holder.lblPeso.setText(items.get(position).peso);

       /* if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }*/

        return convertView;

    }

    static class ViewHolder {
        TextView lblCod,lblPeso;
    }

}
