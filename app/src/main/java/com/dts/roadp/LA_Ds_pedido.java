package com.dts.roadp;

import android.content.Context;
import java.util.ArrayList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LA_Ds_pedido  extends BaseAdapter {

    private MiscUtils mu;
    private DateUtils du;
    private AppMethods app;

    private ArrayList<clsClasses.clsDs_pedido> items= new ArrayList<clsClasses.clsDs_pedido>();
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_Ds_pedido(Context context, PBase owner, ArrayList<clsClasses.clsDs_pedido> results) {
        items = results;
        l_Inflater = LayoutInflater.from(context);
        selectedIndex = -1;

        mu=owner.mu;
        du=owner.du;
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

            convertView = l_Inflater.inflate(R.layout.lv_ds_pedido, null);
            holder = new ViewHolder();

            holder.lbl1 = (TextView) convertView.findViewById(R.id.lblV1);
            holder.lbl10 = (TextView) convertView.findViewById(R.id.lblV10);
            holder.lbl11 = (TextView) convertView.findViewById(R.id.lblV11);
            holder.lbl19 = (TextView) convertView.findViewById(R.id.lblV19);
            holder.lbl20 = (TextView) convertView.findViewById(R.id.lblV20);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lbl1.setText(""+items.get(position).corel);
        holder.lbl10.setText(""+items.get(position).direntrega);
        holder.lbl11.setText(mu.frmcur(items.get(position).total));
        holder.lbl19.setText(""+items.get(position).fechaentr);
        holder.lbl20.setText(""+items.get(position).add2);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lbl1,lbl10,lbl11,lbl19,lbl20;
    }

}

