package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class WSTest extends PBase {

    private GridView dgrid;
    private ProgressBar pbar;
    private EditText txt1;

    private ArrayList<String> dvalues=new ArrayList<String>();
    private ListAdaptTablas2 adapter;

    private int cw;
    private String URL;

    private WebService ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wstest);

        super.InitBase();

        dgrid = (GridView) findViewById(R.id.gridview2);
        pbar=(ProgressBar) findViewById(R.id.progressBar3);pbar.setVisibility(View.INVISIBLE);
        txt1 = (EditText) findViewById(R.id.editText9);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        URL=getURL();

        ws=new WebService(WSTest.this,URL);

        txt1.setText("SELECT CODIGO,FTPFOLD,EMAIL FROM P_RUTA WHERE CODIGO='8001-1'");

    }


    // Events

    public void doClear(View view) {
        String qs;

        try{
            qs=txt1.getText().toString();
            if (!mu.emptystr(qs)) {
                pbar.setVisibility(View.VISIBLE);
                //ws.openDT("SELECT * FROM "+qs);
                ws.openDT(qs);
            } else {
                toast("Falta nombre de tabla");
            }
        } catch (Exception e){
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    // Main

    @Override
    protected void wsCallBack(Boolean throwing,String errmsg) {

        pbar.setVisibility(View.INVISIBLE);

        try {
            super.wsCallBack(throwing, errmsg);

            toast("Rows : " + ws.openDTCursor.getCount());
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        showData(ws.openDTCursor);
    }

    private void showData(Cursor dt) {
        String n,ss;
        int cc=1,j;

        dvalues.clear();

        try {
            if (dt.getCount() > 0) {
                cc = dt.getColumnCount();

                ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
                dlayoutParams.width = ((int) (cw * cc)) + 25;
                dgrid.setLayoutParams(dlayoutParams);

                dgrid.setColumnWidth(cw);
                dgrid.setStretchMode(GridView.NO_STRETCH);
                dgrid.setNumColumns(cc);

                dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    for (int i = 0; i < cc; i++) {
                        try {
                            ss = dt.getString(i) + "";
                            if (ss == null || ss.isEmpty()) ss = " ";
                        } catch (Exception e) {
                            ss = " ";
                        }
                        dvalues.add(ss);
                    }
                    dt.moveToNext();
                }
              }

        } catch (Exception e) {
            addlog(new Object() {  }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        } finally {
            adapter = new ListAdaptTablas2(this, dvalues);
            dgrid.setAdapter(adapter);
            pbar.setVisibility(View.INVISIBLE);
        }
    }


    // Aux

    public String getURL() {
        Cursor DT;

        try {
            sql = "SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            return DT.getString(0);

        } catch (Exception e) {
            return "*";
        }
    }



}
