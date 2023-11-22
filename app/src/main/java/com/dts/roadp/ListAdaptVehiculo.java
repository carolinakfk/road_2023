package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptVehiculo extends BaseAdapter {


    private static ArrayList<clsClasses.clsVehiculo> itemDetailsrrayList;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptVehiculo(Context context, ArrayList<clsClasses.clsVehiculo> results) {
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
            convertView  = l_Inflater.inflate(R.layout.activity_list_adapt_vehiculo, null);
            holder = new ViewHolder();

            holder.lblCod = (TextView)convertView.findViewById(R.id.lblIdVehiculo);
            holder.lblMarca = (TextView)convertView.findViewById(R.id.lblMarca);
            holder.lblPlaca = (TextView)convertView.findViewById(R.id.lblPlaca);

            convertView.setTag(holder);

        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.lblCod.setText(itemDetailsrrayList.get(position).idVehiculo);
        holder.lblMarca.setText(itemDetailsrrayList.get(position).marca);
        holder.lblPlaca.setText(itemDetailsrrayList.get(position).placa);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;

    }

    static class ViewHolder {
        TextView lblCod,lblMarca,lblPlaca;
    }

}
