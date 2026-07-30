package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ConsPromociones extends PBase {

    private RecyclerView recyclerView;
    private EditText txtFiltro;
    private EditText txtCliente;
    private TextView lblSinPromociones;
    private TextView lblCliente;
    private PromocionesAdapter adapter;
    private final ArrayList<PromocionItem> promociones=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cons_promociones);
        super.InitBase();
        addlog("ConsPromociones",""+du.getActDateTime(),gl.vend);

        recyclerView=findViewById(R.id.rvPromociones);
        txtFiltro=findViewById(R.id.txtFiltroPromocion);
        txtCliente=findViewById(R.id.txtClientePromocion);
        lblSinPromociones=findViewById(R.id.lblSinPromociones);
        lblCliente=findViewById(R.id.lblClientePromocion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        adapter=new PromocionesAdapter(promociones);
        recyclerView.setAdapter(adapter);

        txtFiltro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s == null ? "" : s.toString());
                actualizarEstadoVacio();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        cargarPromociones();
    }

    // #EJC20260730 feat(hh-promotion-query): consulta local eficiente con detalle
    // de combos precargado para evitar consultas SQLite desde cada ViewHolder.
    private void cargarPromociones() {
        Cursor cursor=null;
        promociones.clear();
        try {
            Map<Integer,String> detallesCombo=cargarDetallesCombo();
            Map<String,PromocionItem> agrupadas=new LinkedHashMap<>();
            String cliente=txtCliente.getText().toString().trim().replace("'","''");
            sql="SELECT CASE WHEN IFNULL(D.PTIPO,0)=6 "+
                    "THEN COALESCE(NULLIF(D.CODCOMBO,''),D.PRODUCTO) ELSE D.PRODUCTO END,"+
                    "COALESCE(NULLIF(P.DESCLARGA,''),"+
                    "NULLIF(P.DESCCORTA,''),'Todos los productos / Combo'),"+
                    "IFNULL(D.NOMBRE,''),D.RANGOINI,D.RANGOFIN,D.VALOR,"+
                    "IFNULL(D.PORCANT,''),IFNULL(D.PORPORCENTAJE,''),"+
                    "IFNULL(D.ES_RECARGO,0),IFNULL(D.PTIPO,0),IFNULL(D.CODDESC,0),"+
                    "IFNULL(D.DESCTIPO,''),D.FECHAINI,D.FECHAFIN "+
                    "FROM P_DESCUENTO D LEFT JOIN P_PRODUCTO P ON P.CODIGO=D.PRODUCTO ";
            if (!cliente.isEmpty()) sql+=filtroAplicabilidadCliente(cliente);
            sql+=
                    "ORDER BY D.PRODUCTO,D.ES_RECARGO,D.PRIORIDAD_DESCUENTO,D.PRIORIDAD";
            cursor=Con.OpenDT(sql);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String codigo=texto(cursor.getString(0));
                    int codDesc=cursor.getInt(10);
                    int pTipo=cursor.getInt(9);
                    boolean esRecargo=cursor.getInt(8)==1;
                    String descTipo=texto(cursor.getString(11));
                    String clave;
                    if (pTipo==6 || "R".equalsIgnoreCase(descTipo)) {
                        clave=codDesc+"|"+codigo+"|"+esRecargo+"|"+pTipo+"|"+descTipo;
                    } else {
                        clave=codDesc+"|"+codigo+"|"+esRecargo+"|"+pTipo+"|"+descTipo+
                                "|"+cursor.getDouble(3)+"|"+cursor.getDouble(5);
                    }
                    PromocionItem item=agrupadas.get(clave);
                    if (item==null) {
                        item=new PromocionItem();
                        item.codigo=codigo;
                        item.producto=texto(cursor.getString(1));
                        item.nombre=texto(cursor.getString(2));
                        item.porCantidad=texto(cursor.getString(6));
                        item.porPorcentaje=texto(cursor.getString(7));
                        item.esRecargo=esRecargo;
                        item.pTipo=pTipo;
                        item.codDesc=codDesc;
                        item.descTipo=descTipo;
                        item.fechaIni=cursor.getLong(12);
                        item.fechaFin=cursor.getLong(13);
                        item.detalleCombo=detallesCombo.get(item.codDesc);
                        agrupadas.put(clave,item);
                    }
                    EscalaItem escala=new EscalaItem();
                    escala.rangoIni=cursor.getDouble(3);
                    escala.rangoFin=cursor.getDouble(4);
                    escala.valor=cursor.getDouble(5);
                    escala.porPorcentaje=texto(cursor.getString(7));
                    item.escalas.add(escala);
                } while (cursor.moveToNext());
            }
            promociones.addAll(agrupadas.values());
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),
                    e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        adapter.reemplazar(promociones);
        actualizarEstadoVacio();
    }

    private String filtroAplicabilidadCliente(String cliente) {
        int dia=Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        dia=dia==Calendar.SUNDAY ? 7 : dia-1;
        long fecha=fechaActualSap();
        String ruta=gl.ruta == null ? "" : gl.ruta.replace("'","''");
        return "INNER JOIN P_CLIENTE C ON C.CODIGO='"+cliente+"' WHERE ("+
                "D.CTIPO=0 OR "+
                "(D.CTIPO=1 AND D.CLIENTE=C.CODIGO) OR "+
                "(D.CTIPO=2 AND D.CLIENTE=C.TIPONEG) OR "+
                "(D.CTIPO=3 AND D.CLIENTE=C.TIPO) OR "+
                "(D.CTIPO=4 AND D.CLIENTE=C.SUBTIPO) OR "+
                "(D.CTIPO=5 AND D.CLIENTE=C.CANAL) OR "+
                "(D.CTIPO=6 AND D.CLIENTE=C.SUBCANAL) OR "+
                "(D.CTIPO=8 AND D.CLIENTE=C.SUCURSAL) OR "+
                "(D.CTIPO=9 AND D.CLIENTE=CAST(C.NIVELPRECIO AS TEXT)) OR "+
                "(D.CTIPO=10 AND D.CLIENTE IN (SELECT G.CODIGO FROM P_CLIGRUPO G "+
                "WHERE G.CLIENTE=C.CODIGO)) OR "+
                "(D.CTIPO=11 AND EXISTS (SELECT 1 FROM P_CLIRUTA R "+
                "WHERE R.CLIENTE=C.CODIGO AND R.RUTA=D.CLIENTE AND R.DIA="+dia+") "+
                "AND D.CLIENTE='"+ruta+"') OR "+
                "(D.CTIPO=12 AND D.CLIENTE=IFNULL(C.TIPOLOGIA,'')) OR "+
                "(D.CTIPO=13 AND D.CLIENTE=IFNULL(C.PRIORIZACION,'') "+
                "AND IFNULL(D.TIPOLOGIA,'')=IFNULL(C.TIPOLOGIA,'') "+
                "AND IFNULL(D.SUCURSAL,'')=IFNULL(C.SUCURSAL,'')) OR "+
                "(D.CTIPO=14 AND D.CLIENTE=IFNULL(C.SUBTIPOLOGIA,''))) "+
                "AND D.FECHAINI<="+fecha+" AND D.FECHAFIN>="+fecha+" "+
                "AND NOT EXISTS (SELECT 1 FROM P_CLIENTE_PROD_EXCLUIDOS E "+
                "WHERE E.CLIENTE=C.CODIGO AND E.PRODUCTO=D.PRODUCTO AND E.ACTIVO=1 "+
                "AND E.FECHAINI<="+fecha+" AND E.FECHAFIN>="+fecha+") ";
    }

    // #EJC20260730 fix(hh-promotion-query-date): P_DESCUENTO guarda vigencia
    // yyyyMMddHHmmss; DateUtils legacy devuelve yyMMddHHmmss.
    private long fechaActualSap() {
        Calendar actual=Calendar.getInstance();
        return actual.get(Calendar.YEAR)*10000000000L+
                (actual.get(Calendar.MONTH)+1)*100000000L+
                actual.get(Calendar.DAY_OF_MONTH)*1000000L+
                actual.get(Calendar.HOUR_OF_DAY)*10000L+
                actual.get(Calendar.MINUTE)*100L+
                actual.get(Calendar.SECOND);
    }

    public void filtrarPorCliente(View view) {
        String cliente=txtCliente.getText().toString().trim();
        if (cliente.isEmpty()) {
            lblCliente.setText("Mostrando todas las promociones activas");
            cargarPromociones();
            return;
        }
        Cursor cursor=null;
        try {
            cursor=Con.OpenDT("SELECT NOMBRE FROM P_CLIENTE WHERE CODIGO='"+
                    cliente.replace("'","''")+"'");
            if (cursor==null || !cursor.moveToFirst()) {
                mu.msgbox("El cliente no existe en la HH.");
                return;
            }
            lblCliente.setText(cliente+" - "+texto(cursor.getString(0)));
        } finally {
            if (cursor!=null) cursor.close();
        }
        cargarPromociones();
    }

    private Map<Integer,String> cargarDetallesCombo() {
        Map<Integer,StringBuilder> builders=new HashMap<>();
        Cursor cursor=null;
        try {
            String comboSql="SELECT C.CODDESC,C.PRODUCTO,"+
                    "COALESCE(NULLIF(P.DESCLARGA,''),NULLIF(P.DESCCORTA,''),''),"+
                    "C.CANTIDAD,IFNULL(C.UMVENTA,''),IFNULL(C.OBLIGATORIO,1),"+
                    "IFNULL(C.TIPO_PARTICIPACION_COMBO,'') "+
                    "FROM P_DESCUENTO_COMBO_DET C "+
                    "LEFT JOIN P_PRODUCTO P ON P.CODIGO=C.PRODUCTO "+
                    "ORDER BY C.CODDESC,C.SECUENCIA";
            cursor=Con.OpenDT(comboSql);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int codDesc=cursor.getInt(0);
                    StringBuilder detalle=builders.get(codDesc);
                    if (detalle==null) {
                        detalle=new StringBuilder();
                        builders.put(codDesc,detalle);
                    }
                    if (detalle.length()>0) detalle.append("\n");
                    detalle.append("• ").append(cursor.getString(1)).append(" - ")
                            .append(texto(cursor.getString(2)))
                            .append("\n  Cantidad: ")
                            .append(formato(cursor.getDouble(3)));
                    String unidad=texto(cursor.getString(4));
                    if (!unidad.isEmpty()) detalle.append(" ").append(unidad);
                    detalle.append(cursor.getInt(5)==1 ? " · Obligatorio" : " · Opcional");
                    String participacion=texto(cursor.getString(6));
                    if (!participacion.isEmpty()) {
                        detalle.append(" · ").append(participacion);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),
                    e.getMessage(),"P_DESCUENTO_COMBO_DET");
        } finally {
            if (cursor != null) cursor.close();
        }
        Map<Integer,String> resultado=new HashMap<>();
        for (Map.Entry<Integer,StringBuilder> entry : builders.entrySet()) {
            resultado.put(entry.getKey(),entry.getValue().toString());
        }
        return resultado;
    }

    private void actualizarEstadoVacio() {
        boolean vacio=adapter == null || adapter.getItemCount()==0;
        lblSinPromociones.setVisibility(vacio ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    public void cerrarPromociones(View view) {
        finish();
    }

    static String formato(double value) {
        if (value==Math.rint(value)) return String.format(Locale.US,"%.0f",value);
        return String.format(Locale.US,"%.2f",value);
    }

    private static String texto(String value) {
        return value == null ? "" : value;
    }

    static final class PromocionItem {
        String codigo;
        String producto;
        String nombre;
        String porCantidad;
        String porPorcentaje;
        String detalleCombo;
        String descTipo;
        double rangoIni;
        double rangoFin;
        double valor;
        long fechaIni;
        long fechaFin;
        int pTipo;
        int codDesc;
        boolean esRecargo;
        boolean expandido;
        final ArrayList<EscalaItem> escalas=new ArrayList<>();

        boolean esCombo() {
            return pTipo==6;
        }

        boolean tieneEscalas() {
            return "R".equalsIgnoreCase(descTipo) && escalas.size()>1;
        }

        String textoBusqueda() {
            return (codigo+" "+producto+" "+nombre+" "+codDesc).toLowerCase(Locale.US);
        }
    }

    static final class EscalaItem {
        double rangoIni;
        double rangoFin;
        double valor;
        String porPorcentaje;
    }
}
