
package com.dts.roadp;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dts.roadp.BaseDatos;
import com.dts.roadp.clsClasses;

public class clsDs_pedidoObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Ds_pedido";
    private String sql;
    public ArrayList<clsClasses.clsDs_pedido> items= new ArrayList<clsClasses.clsDs_pedido>();

    public clsDs_pedidoObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        cont=context;
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
        count = 0;
    }

    public void reconnect(BaseDatos dbconnection, SQLiteDatabase dbase) {
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
    }

    public void add(clsClasses.clsDs_pedido item) {
        addItem(item);
    }

    public void update(clsClasses.clsDs_pedido item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsDs_pedido item) {
        deleteItem(item);
    }

    public void delete(String id) {
        deleteItem(id);
    }

    public void fill() {
        fillItems(sel);
    }

    public void fill(String specstr) {
        fillItems(sel+ " "+specstr);
    }

    public void fillSelect(String sq) {
        fillItems(sq);
    }

    public clsClasses.clsDs_pedido first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsDs_pedido item) {

        ins.init("Ds_pedido");

        ins.add("COREL",item.corel);
        ins.add("ANULADO",item.anulado);
        ins.add("FECHA",item.fecha);
        ins.add("EMPRESA",item.empresa);
        ins.add("RUTA",item.ruta);
        ins.add("VENDEDOR",item.vendedor);
        ins.add("CLIENTE",item.cliente);
        ins.add("KILOMETRAJE",item.kilometraje);
        ins.add("FECHAENTR",item.fechaentr);
        ins.add("DIRENTREGA",item.direntrega);
        ins.add("TOTAL",item.total);
        ins.add("DESMONTO",item.desmonto);
        ins.add("IMPMONTO",item.impmonto);
        ins.add("PESO",item.peso);
        ins.add("BANDERA",item.bandera);
        ins.add("STATCOM",item.statcom);
        ins.add("CALCOBJ",item.calcobj);
        ins.add("IMPRES",item.impres);
        ins.add("ADD1",item.add1);
        ins.add("ADD2",item.add2);
        ins.add("ADD3",item.add3);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsDs_pedido item) {

        upd.init("Ds_pedido");

        upd.add("ANULADO",item.anulado);
        upd.add("FECHA",item.fecha);
        upd.add("EMPRESA",item.empresa);
        upd.add("RUTA",item.ruta);
        upd.add("VENDEDOR",item.vendedor);
        upd.add("CLIENTE",item.cliente);
        upd.add("KILOMETRAJE",item.kilometraje);
        upd.add("FECHAENTR",item.fechaentr);
        upd.add("DIRENTREGA",item.direntrega);
        upd.add("TOTAL",item.total);
        upd.add("DESMONTO",item.desmonto);
        upd.add("IMPMONTO",item.impmonto);
        upd.add("PESO",item.peso);
        upd.add("BANDERA",item.bandera);
        upd.add("STATCOM",item.statcom);
        upd.add("CALCOBJ",item.calcobj);
        upd.add("IMPRES",item.impres);
        upd.add("ADD1",item.add1);
        upd.add("ADD2",item.add2);
        upd.add("ADD3",item.add3);

        upd.Where("(COREL='"+item.corel+"')");

        db.execSQL(upd.SQL());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    public void updateBandera(clsClasses.clsDs_pedido item) {

        upd.init("Ds_pedido");

        upd.add("BANDERA",item.bandera);
        upd.Where("(COREL='"+item.corel+"')");

        db.execSQL(upd.SQL());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsDs_pedido item) {
        sql="DELETE FROM Ds_pedido WHERE (COREL='"+item.corel+"')";
        db.execSQL(sql);
    }

    private void deleteItem(String id) {
        sql="DELETE FROM Ds_pedido WHERE id='" + id+"'";
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsDs_pedido item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsDs_pedido();

            item.corel=dt.getString(0);
            item.anulado=dt.getString(1);
            item.fecha=dt.getLong(2);
            item.empresa=dt.getString(3);
            item.ruta=dt.getString(4);
            item.vendedor=dt.getString(5);
            item.cliente=dt.getString(6);
            item.kilometraje=dt.getDouble(7);
            item.fechaentr=dt.getLong(8);
            item.direntrega=dt.getString(9);
            item.total=dt.getDouble(10);
            item.desmonto=dt.getDouble(11);
            item.impmonto=dt.getDouble(12);
            item.peso=dt.getDouble(13);
            item.bandera=dt.getString(14);
            item.statcom=dt.getString(15);
            item.calcobj=dt.getString(16);
            item.impres=dt.getInt(17);
            item.add1=dt.getString(18);
            item.add2=dt.getString(19);
            item.add3=dt.getString(20);

            items.add(item);

            dt.moveToNext();
        }

        if (dt!=null) dt.close();

    }

    public int newID(String idsql) {
        Cursor dt=null;
        int nid;

        try {
            dt=Con.OpenDT(idsql);
            dt.moveToFirst();
            nid=dt.getInt(0)+1;
        } catch (Exception e) {
            nid=1;
        }

        if (dt!=null) dt.close();

        return nid;
    }

    public String addItemSql(clsClasses.clsDs_pedido item) {

        ins.init("Ds_pedido");

        ins.add("COREL",item.corel);
        ins.add("ANULADO",item.anulado);
        ins.add("FECHA",item.fecha);
        ins.add("EMPRESA",item.empresa);
        ins.add("RUTA",item.ruta);
        ins.add("VENDEDOR",item.vendedor);
        ins.add("CLIENTE",item.cliente);
        ins.add("KILOMETRAJE",item.kilometraje);
        ins.add("FECHAENTR",item.fechaentr);
        ins.add("DIRENTREGA",item.direntrega);
        ins.add("TOTAL",item.total);
        ins.add("DESMONTO",item.desmonto);
        ins.add("IMPMONTO",item.impmonto);
        ins.add("PESO",item.peso);
        ins.add("BANDERA",item.bandera);
        ins.add("STATCOM",item.statcom);
        ins.add("CALCOBJ",item.calcobj);
        ins.add("IMPRES",item.impres);
        ins.add("ADD1",item.add1);
        ins.add("ADD2",item.add2);
        ins.add("ADD3",item.add3);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsDs_pedido item) {

        upd.init("Ds_pedido");

        upd.add("ANULADO",item.anulado);
        upd.add("FECHA",item.fecha);
        upd.add("EMPRESA",item.empresa);
        upd.add("RUTA",item.ruta);
        upd.add("VENDEDOR",item.vendedor);
        upd.add("CLIENTE",item.cliente);
        upd.add("KILOMETRAJE",item.kilometraje);
        upd.add("FECHAENTR",item.fechaentr);
        upd.add("DIRENTREGA",item.direntrega);
        upd.add("TOTAL",item.total);
        upd.add("DESMONTO",item.desmonto);
        upd.add("IMPMONTO",item.impmonto);
        upd.add("PESO",item.peso);
        upd.add("BANDERA",item.bandera);
        upd.add("STATCOM",item.statcom);
        upd.add("CALCOBJ",item.calcobj);
        upd.add("IMPRES",item.impres);
        upd.add("ADD1",item.add1);
        upd.add("ADD2",item.add2);
        upd.add("ADD3",item.add3);

        upd.Where("(COREL='"+item.corel+"')");

        return upd.SQL();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();


    }

}

