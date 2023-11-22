package com.dts.roadp;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dts.roadp.BaseDatos;
import com.dts.roadp.clsClasses;

public class clsDs_pedidodObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Ds_pedidod";
    private String sql;
    public ArrayList<clsClasses.clsDs_pedidod> items= new ArrayList<clsClasses.clsDs_pedidod>();

    public clsDs_pedidodObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsDs_pedidod item) {
        addItem(item);
    }

    public void update(clsClasses.clsDs_pedidod item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsDs_pedidod item) {
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

    public clsClasses.clsDs_pedidod first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsDs_pedidod item) {

        ins.init("Ds_pedidod");

        ins.add("COREL",item.corel);
        ins.add("PRODUCTO",item.producto);
        ins.add("EMPRESA",item.empresa);
        ins.add("ANULADO",item.anulado);
        ins.add("CANT",item.cant);
        ins.add("PRECIO",item.precio);
        ins.add("IMP",item.imp);
        ins.add("DES",item.des);
        ins.add("DESMON",item.desmon);
        ins.add("TOTAL",item.total);
        ins.add("PRECIODOC",item.preciodoc);
        ins.add("PESO",item.peso);
        ins.add("VAL1",item.val1);
        ins.add("VAL2",item.val2);
        ins.add("Ruta",item.ruta);
        ins.add("UMVENTA",item.umventa);
        ins.add("UMSTOCK",item.umstock);
        ins.add("UMPESO",item.umpeso);
        ins.add("CANTORIGINAL", item.cantOriginal);
        ins.add("PESOORIGINAL", item.pesoOriginal);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsDs_pedidod item) {

        upd.init("Ds_pedidod");

        upd.add("EMPRESA",item.empresa);
        upd.add("ANULADO",item.anulado);
        upd.add("CANT",item.cant);
        upd.add("PRECIO",item.precio);
        upd.add("IMP",item.imp);
        upd.add("DES",item.des);
        upd.add("DESMON",item.desmon);
        upd.add("TOTAL",item.total);
        upd.add("PRECIODOC",item.preciodoc);
        upd.add("PESO",item.peso);
        upd.add("VAL1",item.val1);
        upd.add("VAL2",item.val2);
        upd.add("Ruta",item.ruta);
        upd.add("UMVENTA",item.umventa);
        upd.add("UMSTOCK",item.umstock);
        upd.add("UMPESO",item.umpeso);
        upd.add("CANTORIGINAL", item.cantOriginal);
        upd.add("PESOORIGINAL", item.pesoOriginal);

        upd.Where("(COREL='"+item.corel+"') AND (PRODUCTO='"+item.producto+"')");

        db.execSQL(upd.SQL());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsDs_pedidod item) {
        sql="DELETE FROM Ds_pedidod WHERE (COREL='"+item.corel+"') AND (PRODUCTO='"+item.producto+"')";
        db.execSQL(sql);
    }

    private void deleteItem(String id) {
        sql="DELETE FROM Ds_pedidod WHERE id='" + id+"'";
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsDs_pedidod item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsDs_pedidod();

            item.corel=dt.getString(0);
            item.producto=dt.getString(1);
            item.empresa=dt.getString(2);
            item.anulado=dt.getString(3);
            item.cant=dt.getDouble(4);
            item.precio=dt.getDouble(5);
            item.imp=dt.getDouble(6);
            item.des=dt.getDouble(7);
            item.desmon=dt.getDouble(8);
            item.total=dt.getDouble(9);
            item.preciodoc=dt.getDouble(10);
            item.peso=dt.getDouble(11);
            item.val1=dt.getDouble(12);
            item.val2=dt.getString(13);
            item.ruta=dt.getString(14);
            item.umventa=dt.getString(15);
            item.umstock=dt.getString(16);
            item.umpeso=dt.getString(17);
            item.cantOriginal=dt.getDouble(18);
            item.pesoOriginal=dt.getDouble(19);;

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

    public String addItemSql(clsClasses.clsDs_pedidod item) {

        ins.init("Ds_pedidod");

        ins.add("COREL",item.corel);
        ins.add("PRODUCTO",item.producto);
        ins.add("EMPRESA",item.empresa);
        ins.add("ANULADO",item.anulado);
        ins.add("CANT",item.cant);
        ins.add("PRECIO",item.precio);
        ins.add("IMP",item.imp);
        ins.add("DES",item.des);
        ins.add("DESMON",item.desmon);
        ins.add("TOTAL",item.total);
        ins.add("PRECIODOC",item.preciodoc);
        ins.add("PESO",item.peso);
        ins.add("VAL1",item.val1);
        ins.add("VAL2",item.val2);
        ins.add("Ruta",item.ruta);
        ins.add("UMVENTA",item.umventa);
        ins.add("UMSTOCK",item.umstock);
        ins.add("UMPESO",item.umpeso);
        ins.add("CANTORIGINAL", item.cantOriginal);
        ins.add("PESOORIGINAL", item.pesoOriginal);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsDs_pedidod item) {

        upd.init("Ds_pedidod");

        upd.add("EMPRESA",item.empresa);
        upd.add("ANULADO",item.anulado);
        upd.add("CANT",item.cant);
        upd.add("PRECIO",item.precio);
        upd.add("IMP",item.imp);
        upd.add("DES",item.des);
        upd.add("DESMON",item.desmon);
        upd.add("TOTAL",item.total);
        upd.add("PRECIODOC",item.preciodoc);
        upd.add("PESO",item.peso);
        upd.add("VAL1",item.val1);
        upd.add("VAL2",item.val2);
        upd.add("Ruta",item.ruta);
        upd.add("UMVENTA",item.umventa);
        upd.add("UMSTOCK",item.umstock);
        upd.add("UMPESO",item.umpeso);
        upd.add("CANTORIGINAL", item.cantOriginal);
        upd.add("PESOORIGINAL", item.pesoOriginal);
        upd.Where("(COREL='"+item.corel+"') AND (PRODUCTO='"+item.producto+"')");

        return upd.SQL();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

