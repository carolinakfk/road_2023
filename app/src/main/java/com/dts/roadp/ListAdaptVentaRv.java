package com.dts.roadp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dts.roadp.clsClasses.clsVenta;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListAdaptVentaRv extends RecyclerView.Adapter<ListAdaptVentaRv.VentaViewHolder> {

	private ArrayList<clsVenta> items;
	private Context context;
	private DecimalFormat frmdec;
	private int selectedPosition = -1;
	public String cursym;

	// Interfaces para manejar clicks y long clicks
	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	public interface OnItemLongClickListener {
		boolean onItemLongClick(int position);
	}

	private OnItemClickListener clickListener;
	private OnItemLongClickListener longClickListener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.clickListener = listener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.longClickListener = listener;
	}

	public ListAdaptVentaRv(Context context, ArrayList<clsVenta> items) {
		this.context = context;
		this.items = items;
		this.frmdec = new DecimalFormat("#,##0.00");
	}

	@NonNull
	@Override
	public VentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.activity_list_view_venta, parent, false);
		return new VentaViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull VentaViewHolder holder, int position) {
		clsVenta item = items.get(position);
		double val;

		holder.lblCod.setText(item.Nombre);
		holder.lblNombre.setText(item.Cod);
		holder.lblCant.setText(item.val);
		holder.lblPrec.setText("");
		holder.lblDesc.setText(item.sdesc);

		val = item.Total;
		holder.lblTot.setText(cursym + " " + frmdec.format(val));
		holder.lblPrecio.setText(String.valueOf(item.precio));

		holder.lblPeso.setText(item.valp);
		if (item.valp.equalsIgnoreCase(".")) {
			holder.lblPeso.setVisibility(View.GONE);
		}
		if (item.Peso == 0) {
			holder.lblPeso.setVisibility(View.GONE);
		}
		holder.lblPE.setText(item.PE);

		// Manejo de la selección
		if (selectedPosition == position) {
			holder.itemView.setBackgroundColor(Color.rgb(26, 138, 198));
		} else {
			holder.itemView.setBackgroundColor(Color.TRANSPARENT);
		}

		// Configurar el click listener
		holder.itemView.setOnClickListener(v -> {
			if (clickListener != null) {
				clickListener.onItemClick(position);
			}
			setSelectedPosition(position);
		});

		// Configurar el long click listener
		holder.itemView.setOnLongClickListener(v -> {
			if (longClickListener != null) {
				return longClickListener.onItemLongClick(position);
			}
			return false;
		});
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setSelectedPosition(int position) {
		int previousSelected = selectedPosition;
		selectedPosition = position;
		notifyItemChanged(previousSelected);
		notifyItemChanged(position);
	}

	public void refreshItems() {
		notifyDataSetChanged();
	}

	public clsVenta getItem(int position) {
		return items.get(position);
	}

	// ViewHolder class
	public static class VentaViewHolder extends RecyclerView.ViewHolder {
		TextView lblCod, lblNombre, lblCant, lblPrec, lblDesc, lblTot, lblPeso, lblPrecio, lblPE;

		public VentaViewHolder(@NonNull View itemView) {
			super(itemView);

			lblCod = itemView.findViewById(R.id.lblETipo);
			lblNombre = itemView.findViewById(R.id.lblCFact);
			lblCant = itemView.findViewById(R.id.lblCant);
			lblPrec = itemView.findViewById(R.id.lblPNum);
			lblDesc = itemView.findViewById(R.id.lblFecha);
			lblTot = itemView.findViewById(R.id.lblTot);
			lblPeso = itemView.findViewById(R.id.lblPeso);
			lblPrecio = itemView.findViewById(R.id.lblPrecio);
			lblPE = itemView.findViewById(R.id.textView97);
		}
	}
}