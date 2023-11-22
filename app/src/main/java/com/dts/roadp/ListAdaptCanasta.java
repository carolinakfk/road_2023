package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptCanasta extends BaseAdapter {

    private static ArrayList<clsClasses.clsCanasta> items;


    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptCanasta(Context context, ArrayList<clsClasses.clsCanasta> results) {
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
        ListAdaptTipoCanasta.ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.tipo_canasta, null);
            holder = new ListAdaptTipoCanasta.ViewHolder();

            holder.lblCodigo  = (TextView) convertView.findViewById(R.id.lblCanCodigo);
            holder.lblEntr = (TextView) convertView.findViewById(R.id.lblCanEnt);
            holder.lblRec = (TextView) convertView.findViewById(R.id.lblCanRec);
            holder.lblNombre = (TextView) convertView.findViewById(R.id.lblCanNombre);

            convertView.setTag(holder);
        } else {
            holder = (ListAdaptTipoCanasta.ViewHolder) convertView.getTag();
        }

        holder.lblCodigo.setText(String.valueOf(items.get(position).fechaFormato));
        holder.lblNombre.setText(items.get(position).desclarga);
        holder.lblEntr.setText(String.valueOf(items.get(position).cantentr));
        holder.lblRec.setText(String.valueOf(items.get(position).cantrec));

        convertView.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCodigo,lblNombre,lblEntr, lblRec;
    }

}