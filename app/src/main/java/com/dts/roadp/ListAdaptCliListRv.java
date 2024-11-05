package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dts.roadp.clsClasses.clsCDB;

import java.util.ArrayList;

interface ClickListener {
	void onItemClick(int position, View v);
	void onItemLongClick(int position, View v);
}

public class ListAdaptCliListRv extends RecyclerView.Adapter<ListAdaptCliListRv.ViewHolder> {

	private ArrayList<clsCDB> items;
	private Context context;
	private int selectedIndex;
	private ClickListener listener;

	public ListAdaptCliListRv(Context context, ArrayList<clsCDB> items) {
		this.context = context;
		this.items = items;
		this.selectedIndex = -1;
	}

	public void setSelectedIndex(int ind) {
		selectedIndex = ind;
		notifyDataSetChanged();
	}

	public void refreshItems() {
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.activity_list_view_clilist, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		clsCDB item = items.get(position);

		holder.lblCod.setText(item.Cod + " " + item.Adds);
		holder.lblDesc.setText(item.Desc);

		int iconId = (item.Bandera > 0) ? R.drawable.disable24 : (item.Bandera == 0 ? R.drawable.icok24 : R.drawable.blank24);
		holder.imgBand.setImageResource(iconId);

		holder.imgCobro.setImageResource(item.Cobro == 1 ? R.drawable.cobro48 : R.drawable.blank24);
		holder.imgPPago.setImageResource(item.ppend == 1 ? R.drawable.pago_pend : R.drawable.blank24);
		holder.imgDespacho.setImageResource(item.prefacturas == 1 ? R.drawable.pedido : R.drawable.blank24);

		holder.itemView.setBackgroundColor(selectedIndex == position ? Color.rgb(26, 138, 198) : Color.WHITE);

		holder.itemView.setOnClickListener(v -> {
			if (listener != null) {
				listener.onItemClick(position, v);
			}
		});

		holder.itemView.setOnLongClickListener(v -> {
			if (listener != null) {
				listener.onItemLongClick(position, v);
			}
			return true;
		});
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setOnItemClickListener(ClickListener l) {
		listener = l;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView lblCod, lblDesc;
		ImageView imgBand, imgCobro, imgPPago, imgDespacho;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			lblCod = itemView.findViewById(R.id.lblETipo);
			lblDesc = itemView.findViewById(R.id.lblPNum);
			imgBand = itemView.findViewById(R.id.imgNext);
			imgCobro = itemView.findViewById(R.id.imageView8);
			imgPPago = itemView.findViewById(R.id.imageView7);
			imgDespacho = itemView.findViewById(R.id.imgDespacho);
		}
	}

	public ArrayList<clsCDB> getItems() {
		return items;
	}

	public clsCDB getItem(int position) {
		return items.get(position);
	}
}
