package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptDocs extends BaseAdapter  {
    private static ArrayList<clsClasses.clsDocumentoImg> items;


    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptDocs(Context context, ArrayList<clsClasses.clsDocumentoImg> results) {
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

            convertView = l_Inflater.inflate(R.layout.documento, null);
            holder = new ViewHolder();

            holder.imgDocumento  = (ImageView) convertView.findViewById(R.id.imgDocumento);
            holder.lblNomImg = (TextView) convertView.findViewById(R.id.lblNomImg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblNomImg.setText(items.get(position).nombre);
        holder.imgDocumento.setImageBitmap(items.get(position).img);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        ImageView imgDocumento;
        TextView lblNomImg;
    }
}
