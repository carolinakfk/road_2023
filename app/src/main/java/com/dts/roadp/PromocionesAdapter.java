package com.dts.roadp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PromocionesAdapter
        extends RecyclerView.Adapter<PromocionesAdapter.PromocionViewHolder> {

    private final ArrayList<ConsPromociones.PromocionItem> todos=new ArrayList<>();
    private final ArrayList<ConsPromociones.PromocionItem> visibles=new ArrayList<>();

    PromocionesAdapter(List<ConsPromociones.PromocionItem> items) {
        reemplazar(items);
    }

    void reemplazar(List<ConsPromociones.PromocionItem> items) {
        todos.clear();
        todos.addAll(items);
        visibles.clear();
        visibles.addAll(items);
        notifyDataSetChanged();
    }

    void filtrar(String filtro) {
        String buscar=filtro == null ? "" : filtro.trim().toLowerCase(Locale.US);
        visibles.clear();
        if (buscar.isEmpty()) {
            visibles.addAll(todos);
        } else {
            for (ConsPromociones.PromocionItem item : todos) {
                if (item.textoBusqueda().contains(buscar)) visibles.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromocionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promocion,parent,false);
        return new PromocionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromocionViewHolder holder, int position) {
        ConsPromociones.PromocionItem item=visibles.get(position);
        holder.codigo.setText(item.codigo+" · CODDESC "+item.codDesc);
        holder.producto.setText(item.producto);
        holder.nombre.setText(item.nombre);
        holder.tipo.setText(item.esRecargo ? "RECARGO" : "DESCUENTO");

        String valor=ConsPromociones.formato(item.valor);
        if ("S".equalsIgnoreCase(item.porPorcentaje)) valor+="%";
        holder.valores.setText("Rango: "+ConsPromociones.formato(item.rangoIni)+" - "+
                ConsPromociones.formato(item.rangoFin)+"   Valor: "+valor);
        holder.modalidad.setText("Por cantidad: "+valorSiNo(item.porCantidad)+
                "   Por porcentaje: "+valorSiNo(item.porPorcentaje));
        holder.vigencia.setText("Vigencia: "+formatoFecha(item.fechaIni)+" - "+
                formatoFecha(item.fechaFin));

        boolean combo=item.esCombo();
        holder.verCombo.setVisibility(combo ? View.VISIBLE : View.GONE);
        holder.detalleCombo.setVisibility(combo && item.expandido ? View.VISIBLE : View.GONE);
        holder.detalleCombo.setText(item.detalleCombo == null || item.detalleCombo.isEmpty()
                ? "El combo no tiene productos sincronizados." : item.detalleCombo);
        holder.verCombo.setText(item.expandido
                ? "Ocultar productos del combo ▲" : "Ver productos del combo ▼");

        holder.itemView.setOnClickListener(combo ? view -> {
            item.expandido=!item.expandido;
            int adapterPosition=holder.getAdapterPosition();
            if (adapterPosition!=RecyclerView.NO_POSITION) notifyItemChanged(adapterPosition);
        } : null);
        holder.itemView.setClickable(combo);
    }

    @Override
    public int getItemCount() {
        return visibles.size();
    }

    private String valorSiNo(String value) {
        return "S".equalsIgnoreCase(value) ? "Sí" : "No";
    }

    private String formatoFecha(long value) {
        String fecha=String.valueOf(value);
        if (fecha.length()<8) return fecha;
        return fecha.substring(6,8)+"/"+fecha.substring(4,6)+"/"+fecha.substring(0,4);
    }

    static final class PromocionViewHolder extends RecyclerView.ViewHolder {
        final TextView codigo;
        final TextView producto;
        final TextView nombre;
        final TextView tipo;
        final TextView valores;
        final TextView modalidad;
        final TextView vigencia;
        final TextView verCombo;
        final TextView detalleCombo;

        PromocionViewHolder(@NonNull View itemView) {
            super(itemView);
            codigo=itemView.findViewById(R.id.lblPromoCodigo);
            producto=itemView.findViewById(R.id.lblPromoProducto);
            nombre=itemView.findViewById(R.id.lblPromoNombre);
            tipo=itemView.findViewById(R.id.lblPromoTipo);
            valores=itemView.findViewById(R.id.lblPromoValores);
            modalidad=itemView.findViewById(R.id.lblPromoModalidad);
            vigencia=itemView.findViewById(R.id.lblPromoVigencia);
            verCombo=itemView.findViewById(R.id.lblVerCombo);
            detalleCombo=itemView.findViewById(R.id.lblDetalleCombo);
        }
    }
}
