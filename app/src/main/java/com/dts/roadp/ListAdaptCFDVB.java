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

public class ListAdaptCFDVB extends BaseAdapter {

	private static ArrayList<clsClasses.clsCFDV> items;

	private int selectedIndex;

	private LayoutInflater l_Inflater;

	public ListAdaptCFDVB(Context context, ArrayList<clsClasses.clsCFDV> results) {
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

			convertView = l_Inflater.inflate(R.layout.activity_list_view_cfdvb, null);
			holder = new ViewHolder();

			holder.lblFecha  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblPValor);
            holder.img1 =  convertView.findViewById(R.id.imageView27);
			holder.txtCufe = (TextView) convertView.findViewById(R.id.textCufe);
			holder.txtCertificada = (TextView) convertView.findViewById(R.id.textCertificada);
			holder.txtEstadoDGI = (TextView) convertView.findViewById(R.id.txtEstado);
			holder.lblCufe = (TextView) convertView.findViewById(R.id.lblCufe);
			holder.lblCertificada = (TextView) convertView.findViewById(R.id.lblCertificada);
			holder.lblEstadoDGI = (TextView) convertView.findViewById(R.id.lblEstadoDGI);
			holder.lblmontominimo= (TextView) convertView.findViewById(R.id.textView108);


			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.lblFecha.setText(items.get(position).Fecha);
		holder.lblDesc.setText(items.get(position).Desc);
		holder.lblValor.setText(items.get(position).Valor);

		if (items.get(position).tipodoc==3
			|| items.get(position).tipodoc==6
			|| items.get(position).tipodoc==7) {

			holder.txtCufe.setText(items.get(position).Cufe);
			holder.txtCertificada.setText(items.get(position).Certificada_DGI);
			holder.txtEstadoDGI.setText(items.get(position).Estado);

			holder.txtCufe.setVisibility(View.VISIBLE);
			holder.txtCertificada.setVisibility(View.VISIBLE);
			holder.txtEstadoDGI.setVisibility(View.VISIBLE);
			holder.lblCufe.setVisibility(View.VISIBLE);
			holder.lblCertificada.setVisibility(View.VISIBLE);
			holder.lblEstadoDGI.setVisibility(View.VISIBLE);

		} else {
			holder.txtCufe.setText("");
			holder.txtCertificada.setText("");
			holder.txtEstadoDGI.setText("");

			holder.txtCufe.setVisibility(View.GONE);
			holder.txtCertificada.setVisibility(View.GONE);
			holder.txtEstadoDGI.setVisibility(View.GONE);
			holder.lblCufe.setVisibility(View.GONE);
			holder.lblCertificada.setVisibility(View.GONE);
			holder.lblEstadoDGI.setVisibility(View.GONE);

			if (items.get(position).tipodoc==0) {
				if (items.get(position).banderamonto>0) {
					if (items.get(position).banderamonto==1) {
						holder.lblmontominimo.setTextColor(Color.RED);
						holder.lblmontominimo.setText("Monto mínimo pendiente");
					}
					if (items.get(position).banderamonto==2){
						holder.lblmontominimo.setTextColor(Color.BLUE);
						holder.lblmontominimo.setText("ANULADO");
					}
					holder.lblmontominimo.setVisibility(View.VISIBLE);
				} else {
					holder.lblmontominimo.setVisibility(View.GONE);
				}
			}
		}

		if (items.get(position).bandera==1) {
            holder.img1.setVisibility(View.VISIBLE);
        } else {
            holder.img1.setVisibility(View.INVISIBLE);
        }

		if (selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
		} else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}

		return convertView;
	}


	static class ViewHolder {
		TextView  lblFecha,lblDesc,lblValor, txtCufe, txtEstadoDGI, txtCertificada,
				  lblCufe, lblEstadoDGI,lblCertificada,lblmontominimo;
		ImageView img1;
	}

}