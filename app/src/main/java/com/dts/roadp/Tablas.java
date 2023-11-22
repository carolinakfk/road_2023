package com.dts.roadp;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Tablas extends PBase {

    private GridView grid,dgrid;
    private Spinner spin,spinf;
    private TextView lblCant;
    private ProgressBar pbar;
    private EditText txt1;

    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private ListAdaptTablas adapter;
    private ListAdaptTablas2 dadapter;

    private int cw;
    private String scod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablas);

        super.InitBase();

        addlog("Tablas",""+du.getActDateTime(),gl.vend);

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        spin = (Spinner) findViewById(R.id.spinner);
        pbar=(ProgressBar) findViewById(R.id.progressBar3);pbar.setVisibility(View.INVISIBLE);
        txt1 = (EditText) findViewById(R.id.editText1);
        lblCant = (TextView) findViewById(R.id.textView86);lblCant.setText("");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        setHandlers();

        fillSpinner();

    }


    // Events

    public void doClear(View view) {
        try{
            txt1.setText("");txt1.requestFocus();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void setHandlers(){

        try{
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    TextView spinlabel;

                    try {
                        spinlabel = (TextView) parentView.getChildAt(0);
                        spinlabel.setTextColor(Color.BLACK);
                        spinlabel.setPadding(5, 0, 0, 0);
                        spinlabel.setTextSize(18);
                        spinlabel.setTypeface(spinlabel.getTypeface(), Typeface.BOLD);

                        scod = spinlist.get(position);
                        if (!scod.equalsIgnoreCase(" ")) {
                            //txt1.setText("");
                            processTable();
                        }
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox(e.getMessage());
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    return;
                }

            });

            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = grid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        dadapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        msgbox(item);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }
                    return true;
                }
            });

            txt1.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                processTable();
                                return true;
                        }
                    }
                    return false;
                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }


    // Main

    private void processTable() {
        try{
            pbar.setVisibility(View.VISIBLE);

            Handler mmtimer = new Handler();
            Runnable mmrunner = new Runnable() {
                @Override
                public void run() {
                    pbar.setVisibility(View.VISIBLE);

                    values.clear();
                    dvalues.clear();

                    adapter = new ListAdaptTablas(Tablas.this, values);
                    grid.setAdapter(adapter);

                    dadapter = new ListAdaptTablas2(Tablas.this, dvalues);
                    dgrid.setAdapter(dadapter);
                }
            };
            mmtimer.postDelayed(mmrunner, 50);

            Handler mtimer = new Handler();
            Runnable mrunner = new Runnable() {
                @Override
                public void run() {
                    showData(scod);
                }
            };
            mtimer.postDelayed(mrunner, 1000);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void showData(String tn) {
        Cursor PRG,dt;
        String n,flt,ss = "";
        int cc=1,j;

        lblCant.setText("");

        try {
            ss="SELECT ";

            sql = "PRAGMA table_info('"+tn+"')";
            PRG=db.rawQuery(sql, null);
            cc=PRG.getCount();

            PRG.moveToFirst();j=0;

            while (!PRG.isAfterLast()) {
                n=PRG.getString(PRG.getColumnIndex("name"));
                // t=PRG.getString(PRG.getColumnIndex("type"));// INTEGER , TEXT , REAL

                values.add(n);
                ss=ss+n;if (j<cc-1) ss=ss+",";
                PRG.moveToNext();j++;
            }

            ss=ss+" FROM "+tn;

            flt=txt1.getText().toString();
            if (!mu.emptystr(flt)) ss=ss+" WHERE "+flt+" COLLATE NOCASE";

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
           // err=e.getMessage();
        }

        ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
        layoutParams.width =((int) (cw*cc))+25;
        grid.setLayoutParams(layoutParams);

        grid.setColumnWidth(cw);
        grid.setStretchMode(GridView.NO_STRETCH);
        grid.setNumColumns(cc);

        adapter=new ListAdaptTablas(this,values);
        grid.setAdapter(adapter);


        ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
        dlayoutParams.width =((int) (cw*cc))+25;
        dgrid.setLayoutParams(dlayoutParams);

        dgrid.setColumnWidth(cw);
        dgrid.setStretchMode(GridView.NO_STRETCH);
        dgrid.setNumColumns(cc);

        try {
            dt=Con.OpenDT(ss);
            if (dt.getCount()==0) {
                lblCant.setText("0");
                pbar.setVisibility(View.INVISIBLE);return;
            }

            dt.moveToFirst();
            while (!dt.isAfterLast()) {

                for (int i = 0; i < cc; i++) {
                    try {
                        ss=dt.getString(i);
                    } catch (Exception e) {
                        ss="?";
                    }
                    dvalues.add(ss);
                }
                dt.moveToNext();
            }
            lblCant.setText(""+dt.getCount());
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        dadapter=new ListAdaptTablas2(this,dvalues);
        dgrid.setAdapter(dadapter);

        pbar.setVisibility(View.INVISIBLE);

    }

    // Aux

    private void fillSpinner() {
        Cursor DT;

        spinlist.clear();spinlist.add(" ");

        try {
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name<>'android_metadata' order by name";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                spinlist.add(DT.getString(0));
                DT.moveToNext();
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinlist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(dataAdapter);
    }

}
