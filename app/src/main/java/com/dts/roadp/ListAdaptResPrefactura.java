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

public class ListAdaptResPrefactura extends BaseAdapter {
    private static ArrayList<clsClasses.clsResPrefactura> items;
    private static ArrayList<clsClasses.clsResProducto> itemsP;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptResPrefactura (Context context, ArrayList<clsClasses.clsResPrefactura> results) {
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
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdaptResPrefactura.ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.list_resumen_prefactura, null);
            holder = new ListAdaptResPrefactura.ViewHolder();

            holder.lblCodigo  = (TextView) convertView.findViewById(R.id.lblCodCli);
            holder.lblNombre = (TextView) convertView.findViewById(R.id.lblNomCli);
            holder.lblCant = (TextView) convertView.findViewById(R.id.lblCantProd);
            holder.lblPeso = (TextView) convertView.findViewById(R.id.lblPesoProd);
            holder.lblNomProd = (TextView) convertView.findViewById(R.id.lblNomProd);
            holder.lblCodProd = (TextView) convertView.findViewById(R.id.lblCodProd);
            holder.lblCodPrefact = (TextView) convertView.findViewById(R.id.lblCodPrefact);
            holder.lblCantProd = (TextView) convertView.findViewById(R.id.lblCantProd);
            holder.lblPesoProd = (TextView) convertView.findViewById(R.id.lblPesoProd);
            holder.lblRuta = (TextView) convertView.findViewById(R.id.lblRuta);
            holder.relEncabezado = (RelativeLayout) convertView.findViewById(R.id.relEncabezado);
            holder.relDet2 = (RelativeLayout) convertView.findViewById(R.id.relDet2);
            holder.relDetProd = (RelativeLayout) convertView.findViewById(R.id.relDetProd);

            convertView.setTag(holder);
        } else {
            holder = (ListAdaptResPrefactura.ViewHolder) convertView.getTag();
        }

        holder.lblCodigo.setText(String.valueOf(items.get(position).codigoCli));
        holder.lblNombre.setText(items.get(position).nombreCli);
        holder.lblCodPrefact.setText("Pref. No. "+items.get(position).Prefact);
        holder.lblRuta.setText("Ruta Preventa: "+items.get(position).rutapreventa);
        holder.lblCodProd.setText("Cod. Producto: " + items.get(position).codigoProd);
        holder.lblNomProd.setText(items.get(position).nombreProd);
        holder.lblCantProd.setText("Cant: "+String.valueOf(items.get(position).cantidad));
        holder.lblPesoProd.setText("Peso: " + items.get(position).peso);

        holder.relEncabezado.setVisibility(View.GONE);
        holder.relDet2.setVisibility(View.GONE);
        holder.relDetProd.setVisibility(View.GONE);

        if (items.get(position).flag == 0) {
            holder.relEncabezado.setVisibility(View.VISIBLE);
        }

        if (items.get(position).flag == 1) {
            holder.relDet2.setVisibility(View.VISIBLE);
            holder.relDetProd.setVisibility(View.VISIBLE);
        }

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblCodigo,lblNombre,lblCant, lblPeso, lblNomProd, lblCodProd, lblCodPrefact, lblCantProd, lblPesoProd, lblRuta;
        RelativeLayout relEncabezado, relDet2, relDetProd;
    }
}
