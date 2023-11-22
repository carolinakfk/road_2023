package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptResProd extends BaseAdapter {
    private static ArrayList<clsClasses.clsResProducto> items;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptResProd (Context context, ArrayList<clsClasses.clsResProducto> results) {
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
        ListAdaptResProd.ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.list_resumen_prod, null);
            holder = new ListAdaptResProd.ViewHolder();

            holder.lblCodigo  = (TextView) convertView.findViewById(R.id.lbCodigo);
            holder.lblNombre = (TextView) convertView.findViewById(R.id.lbNombre);
            holder.lblCant = (TextView) convertView.findViewById(R.id.lblCant);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPeso);

            convertView.setTag(holder);
        } else {
            holder = (ListAdaptResProd.ViewHolder) convertView.getTag();
        }

        holder.lblCodigo.setText(String.valueOf(items.get(position).codigo));
        holder.lblNombre.setText(items.get(position).nombre);
        holder.lblCant.setText("Cantidad: " + items.get(position).cantidad);
        holder.lblPeso.setText("Peso: "+ items.get(position).peso);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCodigo,lblNombre,lblCant, lblPeso;
    }
}
