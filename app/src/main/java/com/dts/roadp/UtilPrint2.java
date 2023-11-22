package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UtilPrint2 extends PBase {

    private ListView listView;
    private EditText txtFil;
    private TextView lblPrn;
    private ImageView imgBlank;

    private ArrayList<clsClasses.clsCD> items = new ArrayList<clsClasses.clsCD>();
    private ListAdaptCD adapter;

    private AppMethods app;
    private CryptUtil cu=new CryptUtil();

    private String printerid,printersn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_util_print2);

        super.InitBase();

        addlog("UtilPrint2",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        txtFil = (EditText) findViewById(R.id.editText7);
        lblPrn = (TextView) findViewById(R.id.textView79); lblPrn.setText("SIN IMPRESORA");
        imgBlank = (ImageView) findViewById(R.id.imageView16);

        app = new AppMethods(this, gl, Con, db);

        setHandlers();

        defPrinter();
        listItems();
    }


    // Events

    private void setHandlers() {

        try {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsCD item = (clsClasses.clsCD) lvObj;

                        adapter.setSelectedIndex(position);
                        printerid=item.um;
                        printersn=item.Desc;

                        msgAsk("Definir como impresora predeterminada");
                    } catch (Exception e) {
                        addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                        mu.msgbox(e.getMessage());
                    }
                }
            });

            txtFil.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) { }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                     int tl = txtFil.getText().toString().length();
                    if (tl == 0 || tl > 1) listItems();
                }
            });

            View.OnClickListener clickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    txtFil.setText("");
                }
            };
            imgBlank.setOnClickListener(clickListener);

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }


    // Main

    private void listItems() {
        Cursor DT;
        clsClasses.clsCD item;
        String filt,sn;

        items.clear();

        filt=txtFil.getText().toString().toUpperCase();

        try {

            sql="SELECT IDIMPRESORA,NUMSERIE,MARCA,MACADDRESS FROM P_IMPRESORA ";
            sql+="ORDER BY IDIMPRESORA";

            DT=Con.OpenDT(sql);
            if (DT.getCount()==0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                item = clsCls.new clsCD();

                item.Cod=DT.getString(2);
                item.Desc=cu.decrypt(DT.getString(1));sn= item.Desc.toUpperCase();
                item.Text=cu.decrypt(DT.getString(3));
                item.um=DT.getString(0);

                if (mu.emptystr(filt)) {
                    items.add(item);
                } else {
                    if (sn.contains(filt)) items.add(item);
                }

                DT.moveToNext();
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        try {
            Collections.sort(items, new Comparator<clsClasses.clsCD>() {
                @Override
                public int compare(clsClasses.clsCD item, clsClasses.clsCD t1) {
                    String s1 = item.Desc;
                    String s2 = t1.Desc;
                    return s1.compareToIgnoreCase(s2);
                }
            });
        } catch (Exception ee) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ee.getMessage(),"");
            mu.msgbox(ee.getMessage());
        }

        adapter=new ListAdaptCD(this,items);
        listView.setAdapter(adapter);
    }

    private void applyPrinter() {
         try {

            printersn=cu.encrypt(printersn);
            sql="UPDATE Params SET prn='"+printerid+"',prnserie='"+printersn+"' ";
            db.execSQL(sql);

            toast("Impresora definida");
            finish();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    // Aux

    private void defPrinter() {
        Cursor dt;
        String prs,prnid,prsn,prmac,prclass;

        try {

            sql="SELECT prn FROM Params";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();
            prnid=dt.getString(0);

            sql="SELECT NUMSERIE FROM P_IMPRESORA WHERE IDIMPRESORA='"+prnid+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                prsn=cu.decrypt(dt.getString(0));
            } else {
                prsn="";
            }

            prmac=app.impresParam();
            prclass=app.impresTipo();

            if (prclass.equalsIgnoreCase("SIN IMPRESORA")) {
                prs="SIN IMPRESORA";
            } else {
                prs=prclass+" : "+prmac+"\n"+prsn;
            }
            lblPrn.setText(prs);
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }


    // Dialogs

    private void msgAsk(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Impresora");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                applyPrinter();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }



    // Activity Events


}
