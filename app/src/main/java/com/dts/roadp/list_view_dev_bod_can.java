package com.dts.roadp;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class list_view_dev_bod_can extends BaseAdapter {

    private static ArrayList<clsClasses.clsDevCan> items;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public list_view_dev_bod_can(Context context, ArrayList<clsClasses.clsDevCan> results) {
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
        list_view_dev_bod_can.ViewHolder holder;
        int ic;
        String lote;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.activity_list_view_dev_bod_can, null);
            holder = new list_view_dev_bod_can.ViewHolder();

            holder.lblDesc = (TextView) convertView.findViewById(R.id.lblDesc);
            holder.lblCod = (TextView) convertView.findViewById(R.id.lblspace);
            holder.lblLote  = (TextView) convertView.findViewById(R.id.lblCodT);
            holder.lblLotem  = (TextView) convertView.findViewById(R.id.lblEstado);
            holder.lblValor = (TextView) convertView.findViewById(R.id.lblValr);
            holder.lblValorM = (TextView) convertView.findViewById(R.id.lblCdEstado);
            holder.lblValorT = (TextView) convertView.findViewById(R.id.lblVrlT);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPesoT);
            holder.lblPesoM = (TextView) convertView.findViewById(R.id.lblPesoEstado);
            holder.lblPesoT = (TextView) convertView.findViewById(R.id.lblPesott);

            holder.reltitle= (RelativeLayout) convertView.findViewById(R.id.rltit);
            holder.relbueno= (RelativeLayout) convertView.findViewById(R.id.rlDet);
            holder.relmalo = (RelativeLayout) convertView.findViewById(R.id.rlEstados);
            holder.reltot = (RelativeLayout) convertView.findViewById(R.id.rlTot);

            convertView.setTag(holder);
        } else {
            holder = (list_view_dev_bod_can.ViewHolder) convertView.getTag();
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
        TextView lblLote,lblLotem,lblCod,lblDesc,lblValor,lblValorM,lblValorT,lblPeso,lblPesoM,lblPesoT;
        RelativeLayout reltitle,relbueno,relmalo,reltot;
    }

}
