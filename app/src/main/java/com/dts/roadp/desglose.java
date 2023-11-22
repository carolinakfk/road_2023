package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class desglose extends PBase {

    private int tot, impres;
    private String tipo = "", corel = "";
    private double val1 = 0.0, val2 = 0.0, val5 = 0.0, val10 = 0.0, val20 = 0.0, val50 = 0.0, val100 = 0.0, val005 = 0.0, val050 = 0.0, val025 = 0.0, val010 = 0.0, val001 = 0.0, valtotc1 = 0.0, valtotc2 = 0.0, valtot = 0.0, falta = 0.0;
    private boolean editando = false;

    private printer prn;
    private Runnable printcallback, printclose;
    private clsDesglose fdesg;

    private EditText txtcien, txtcint, txtveint, txtdies, txtcinco, txtdos, txtuno, txtCntCvs, txtVCCvs, txtDiezCvs, txtCincoCvs, txtUnCv;
    private TextView lblTotal, Totcien, Totcnt, Totvein, Totdies, Totcinco, Totdos, Totuno, TotCntCvs, totVCCvs, totDiezCvs, totCincoCvs, totUnCv, lblTotC1, lblTotC2, lblFaltantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose);

        super.InitBase();
        addlog("Desglose", "" + du.getActDateTime(), gl.vend);

        txtcien = (EditText) findViewById(R.id.txtcien);
        txtcint = (EditText) findViewById(R.id.txtcint);
        txtveint = (EditText) findViewById(R.id.txtveint);
        txtdies = (EditText) findViewById(R.id.txtdies);
        txtcinco = (EditText) findViewById(R.id.txtcinco);
        txtdos = (EditText) findViewById(R.id.txtdos);
        txtuno = (EditText) findViewById(R.id.txtuno);
        txtCntCvs = (EditText) findViewById(R.id.txtCntCvs);
        txtVCCvs = (EditText) findViewById(R.id.txtVCCvs);
        txtDiezCvs = (EditText) findViewById(R.id.txtDiezCvs);
        txtCincoCvs = (EditText) findViewById(R.id.txtCincoCvs);
        txtUnCv = (EditText) findViewById(R.id.txtUnCv);

        lblTotal = (TextView) findViewById(R.id.lblTotal);
        lblTotal.setText(mu.frmcur(0));
        Totcien = (TextView) findViewById(R.id.Totcien);
        Totcien.setText(mu.frmcur(0));
        Totcnt = (TextView) findViewById(R.id.Totcnt);
        Totcnt.setText(mu.frmcur(0));
        Totvein = (TextView) findViewById(R.id.Totvein);
        Totvein.setText(mu.frmcur(0));
        Totdies = (TextView) findViewById(R.id.Totdies);
        Totdies.setText(mu.frmcur(0));
        Totcinco = (TextView) findViewById(R.id.Totcinco);
        Totcinco.setText(mu.frmcur(0));
        Totdos = (TextView) findViewById(R.id.Totdos);
        Totdos.setText(mu.frmcur(0));
        Totuno = (TextView) findViewById(R.id.Totuno);
        Totuno.setText(mu.frmcur(0));
        TotCntCvs = (TextView) findViewById(R.id.TotCntCvs);
        TotCntCvs.setText(mu.frmcur(0));
        totVCCvs = (TextView) findViewById(R.id.totVCCvs);
        totVCCvs.setText(mu.frmcur(0));
        totDiezCvs = (TextView) findViewById(R.id.totDiezCvs);
        totDiezCvs.setText(mu.frmcur(0));
        totCincoCvs = (TextView) findViewById(R.id.totCincoCvs);
        totCincoCvs.setText(mu.frmcur(0));
        totUnCv = (TextView) findViewById(R.id.totUnCv);
        totUnCv.setText(mu.frmcur(0));
        lblTotC1 = (TextView) findViewById(R.id.lblTotC1);
        lblTotC2 = (TextView) findViewById(R.id.lblTotC2);
        lblFaltantes = (TextView) findViewById(R.id.lblFaltantes);
        lblFaltantes.setText(mu.frmcur(0));

        ShowData();
        setHandles();

        txtcien.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtcint.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtveint.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtdies.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtcinco.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtdos.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtuno.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtCntCvs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtVCCvs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtDiezCvs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtCincoCvs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});
        txtUnCv.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0)});

        printcallback = new Runnable() {
            public void run() {
                askPrint();
            }
        };

        printclose = new Runnable() {
            public void run() {
                desglose.super.finish();
            }
        };

        prn=new printer(this,printclose,gl.validimp);
        fdesg=new clsDesglose(this,prn.prw,gl.ruta,0, "");
    }

    private void ShowData() {
        Cursor DT;
        int c100 = 0, c50 = 0, c20 = 0, c10 = 0, c5 = 0, c2 = 0, c1 = 0, c050 = 0, c025 = 0, c010 = 0, c005 = 0, c001 = 0;
        double flt = 0;

        try {

        /*   JP20190508 - causo que en segundo desposito se mostraba desglose da la anterior
            sql = "SELECT DENOMINACION,CANTIDAD,TIPO,MONEDA " +
                    "FROM D_DEPOSB";

            DT = Con.OpenDT(sql);

            if (DT.getCount() == 0) {
                sql = "SELECT * FROM T_DEPOSB";
                DT = Con.OpenDT(sql);
            } else {
                editando = true;
            }
            */

            sql = "SELECT * FROM T_DEPOSB";
            DT = Con.OpenDT(sql);

            if (DT.getCount() == 0) {

                lblTotal.setText(String.valueOf(mu.frmcur(gl.totDep)));
                lblFaltantes.setText(String.valueOf(mu.frmcur(gl.totDep)));

            } else {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    //corel = DT.getString(0);

                    tipo = DT.getString(0);

                    if (tipo.equals("100")) {
                        c100 += DT.getInt(1);
                        val100 += (Double.parseDouble(tipo) * c100);
                    }

                    if (tipo.equals("50")) {
                        c50 += DT.getInt(1);
                        val50 += (Double.parseDouble(tipo) * c50);
                    }

                    if (tipo.equals("20")) {
                        c20 += DT.getInt(1);
                        val20 += (Double.parseDouble(tipo) * c20);
                    }

                    if (tipo.equals("10")) {
                        c10 += DT.getInt(1);
                        val10 += (Double.parseDouble(tipo) * c10);
                    }

                    if (tipo.equals("5")) {
                        c5 += DT.getInt(1);
                        val5 += (Double.parseDouble(tipo) * c5);
                    }

                    if (tipo.equals("2")) {
                        c2 += DT.getInt(1);
                        val2 += (Double.parseDouble(tipo) * c2);
                    }

                    if (tipo.equals("1")) {
                        c1 += DT.getInt(1);
                        val1 += (Double.parseDouble(tipo) * c1);
                    }

                    if (tipo.equals("0.5")) {
                        c050 += DT.getInt(1);
                        val050 += (Double.parseDouble(tipo) * c050);
                    }

                    if (tipo.equals("0.25")) {
                        c025 += DT.getInt(1);
                        val025 += (Double.parseDouble(tipo) * c025);
                    }

                    if (tipo.equals("0.1")) {
                        c010 += DT.getInt(1);
                        val010 += (Double.parseDouble(tipo) * c010);
                    }

                    if (tipo.equals("0.05")) {
                        c005 += DT.getInt(1);
                        val005 += (Double.parseDouble(tipo) * c005);
                    }

                    if (tipo.equals("0.01")) {
                        c001 += DT.getInt(1);
                        val001 += (Double.parseDouble(tipo) * c001);
                    }

                    DT.moveToNext();

                }

                valtotc1 = val100 + val50 + val20 + val10 + val5 + val2 + val1;

                valtotc2 = val050 + val025 + val010 + val005 + val001;

                valtot = valtotc1 + valtotc2;

                if (gl.totDep == 0) {
                    gl.totDep = valtot;
                }

                flt = gl.totDep - valtot;

                if (gl.depparc) {

                    if (!editando) {
                        if (gl.totDep < valtot) {
                            LimpiaValores();
                            lblTotal.setText(String.valueOf(mu.frmcur(mu.round(gl.totDep, gl.peDec))));
                            lblFaltantes.setText(String.valueOf(mu.frmcur(mu.round(gl.totDep, gl.peDec))));
                            return;
                        }
                    } else {
                        LimpiaValores();
                        lblTotal.setText(String.valueOf(mu.frmcur(mu.round(gl.totDep, gl.peDec))));
                        lblFaltantes.setText(String.valueOf(mu.frmcur(mu.round(gl.totDep, gl.peDec))));
                        return;
                    }


                }

                Totcien.setText(String.valueOf(mu.frmcur(val100)));
                Totcnt.setText(String.valueOf(mu.frmcur(val50)));
                Totvein.setText(String.valueOf(mu.frmcur(val20)));
                Totdies.setText(String.valueOf(mu.frmcur(val10)));
                Totcinco.setText(String.valueOf(mu.frmcur(val5)));
                Totdos.setText(String.valueOf(mu.frmcur(val2)));
                Totuno.setText(String.valueOf(mu.frmcur(val1)));

                lblTotC1.setText(String.valueOf(mu.frmcur(mu.round(valtotc1, gl.peDec))));

                TotCntCvs.setText(String.valueOf(mu.frmcur(val050)));
                totVCCvs.setText(String.valueOf(mu.frmcur(val025)));
                totDiezCvs.setText(String.valueOf(mu.frmcur(val010)));
                totCincoCvs.setText(String.valueOf(mu.frmcur(val005)));
                totUnCv.setText(String.valueOf(mu.frmcur(val001)));

                lblTotC2.setText(String.valueOf(mu.frmcur(mu.round(valtotc2, gl.peDec))));

                lblTotal.setText(String.valueOf(mu.frmcur(mu.round(gl.totDep, gl.peDec))));
                lblFaltantes.setText(String.valueOf(mu.frmcur(mu.round(flt, gl.peDec))));

                txtcien.setText(String.valueOf(c100));
                txtcint.setText(String.valueOf(c50));
                txtveint.setText(String.valueOf(c20));
                txtdies.setText(String.valueOf(c10));
                txtcinco.setText(String.valueOf(c5));
                txtdos.setText(String.valueOf(c2));
                txtuno.setText(String.valueOf(c1));
                txtCntCvs.setText(String.valueOf(c050));
                txtVCCvs.setText(String.valueOf(c025));
                txtDiezCvs.setText(String.valueOf(c010));
                txtCincoCvs.setText(String.valueOf(c005));
                txtUnCv.setText(String.valueOf(c001));

            }


        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

    }

    public void clearVals(View view) {
        LimpiaValores();
    }

    private void LimpiaValores() {

        val100 = 0;
        val50 = 0;
        val20 = 0;
        val10 = 0;
        val5 = 0;
        val2 = 0;
        val1 = 0;
        val050 = 0;
        val025 = 0;
        val010 = 0;
        val005 = 0;
        val001 = 0;

        Totcien.setText(String.valueOf(mu.frmcur(0)));
        Totcnt.setText(String.valueOf(mu.frmcur(0)));
        Totvein.setText(String.valueOf(mu.frmcur(0)));
        Totdies.setText(String.valueOf(mu.frmcur(0)));
        Totcinco.setText(String.valueOf(mu.frmcur(0)));
        Totdos.setText(String.valueOf(mu.frmcur(0)));
        Totuno.setText(String.valueOf(mu.frmcur(0)));
        lblTotC1.setText(String.valueOf(mu.frmcur(mu.round(0, gl.peDec))));

        txtcien.setText("");
        txtcint.setText("");
        txtveint.setText("");
        txtdies.setText("");
        txtcinco.setText("");
        txtdos.setText("");
        txtuno.setText("");
        txtCntCvs.setText("");
        txtVCCvs.setText("");
        txtDiezCvs.setText("");
        txtCincoCvs.setText("");
        txtUnCv.setText("");

        TotCntCvs.setText(String.valueOf(mu.frmcur(0)));
        totVCCvs.setText(String.valueOf(mu.frmcur(0)));
        totDiezCvs.setText(String.valueOf(mu.frmcur(0)));
        totCincoCvs.setText(String.valueOf(mu.frmcur(0)));
        totUnCv.setText(String.valueOf(mu.frmcur(0)));

        lblTotC2.setText(String.valueOf(mu.frmcur(mu.round(0, gl.peDec))));

    }

    private void setHandles() {

        try {

            //EditText Valor 100
            txtcien.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {
                        double txtS = 0;

                        if (txtcien.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcien.getText().toString());
                        }

                        val100 = 0;
                        val100 = 100 * txtS;
                        Totcien.setText(String.valueOf(mu.frmcur(mu.round(val100, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtcien.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtcien.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtcien.getText().toString()) &&
                                (!txtcien.getText().toString().equals("0"))) {

                            val100 = 0;
                            val100 = 100 * Double.parseDouble(txtcien.getText().toString());
                            Totcien.setText(String.valueOf(mu.frmcur(mu.round(val100, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtcien.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtcien.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcien.getText().toString());
                        }

                        val100 = 0;
                        val100 = 100 * txtS;
                        Totcien.setText(String.valueOf(mu.frmcur(mu.round(val100, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 50

            txtcint.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtcint.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcint.getText().toString());
                        }

                        val50 = 0;
                        val50 = 50 * txtS;
                        Totcnt.setText(String.valueOf(mu.frmcur(mu.round(val50, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtcint.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtcint.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtcint.getText().toString()) &&
                                (!txtcint.getText().toString().equals("0"))) {

                            val50 = 0;
                            val50 = 50 * Double.parseDouble(txtcint.getText().toString());
                            Totcnt.setText(String.valueOf(mu.frmcur(mu.round(val50, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtcint.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtcint.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcint.getText().toString());
                        }

                        val50 = 0;
                        val50 = 50 * txtS;
                        Totcnt.setText(String.valueOf(mu.frmcur(mu.round(val50, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 20

            txtveint.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtveint.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtveint.getText().toString());
                        }

                        val20 = 0;
                        val20 = 20 * txtS;
                        Totvein.setText(String.valueOf(mu.frmcur(mu.round(val20, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtveint.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtveint.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtveint.getText().toString()) &&
                                (!txtveint.getText().toString().equals("0"))) {

                            val20 = 0;
                            val20 = 20 * Double.parseDouble(txtveint.getText().toString());
                            Totvein.setText(String.valueOf(mu.frmcur(mu.round(val20, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtveint.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtveint.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtveint.getText().toString());
                        }

                        val20 = 0;
                        val20 = 20 * txtS;
                        Totvein.setText(String.valueOf(mu.frmcur(mu.round(val20, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 10

            txtdies.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtdies.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtdies.getText().toString());
                        }

                        val10 = 0;
                        val10 = 10 * txtS;
                        Totdies.setText(String.valueOf(mu.frmcur(mu.round(val10, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtdies.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtdies.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtdies.getText().toString()) &&
                                (!txtdies.getText().toString().equals("0"))) {

                            val10 = 0;
                            val10 = 10 * Double.parseDouble(txtdies.getText().toString());
                            Totdies.setText(String.valueOf(mu.frmcur(mu.round(val10, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtdies.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtdies.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtdies.getText().toString());
                        }

                        val10 = 0;
                        val10 = 10 * txtS;
                        Totdies.setText(String.valueOf(mu.frmcur(mu.round(val10, gl.peDec))));

                        CalculaTotales();


                    }

                    return false;
                }
            });

            //EditText de valor 5

            txtcinco.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtcinco.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcinco.getText().toString());
                        }

                        val5 = 0;
                        val5 = 5 * txtS;
                        Totcinco.setText(String.valueOf(mu.frmcur(mu.round(val5, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtcinco.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtcinco.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtcinco.getText().toString()) &&
                                (!txtcinco.getText().toString().equals("0"))) {

                            val5 = 0;
                            val5 = 5 * Double.parseDouble(txtcinco.getText().toString());
                            Totcinco.setText(String.valueOf(mu.frmcur(mu.round(val5, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtcinco.setText("");
                            }

                        }

                        return true;

                    } else {

                        double txtS = 0;

                        if (txtcinco.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtcinco.getText().toString());
                        }

                        val5 = 0;
                        val5 = 5 * txtS;
                        Totcinco.setText(String.valueOf(mu.frmcur(mu.round(val5, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 2

            txtdos.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtdos.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtdos.getText().toString());
                        }

                        val2 = 0;
                        val2 = 2 * txtS;
                        Totdos.setText(String.valueOf(mu.frmcur(mu.round(val2, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtdos.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtdos.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtdos.getText().toString()) &&
                                (!txtdos.getText().toString().equals("0"))) {

                            val2 = 0;
                            val2 = 2 * Double.parseDouble(txtdos.getText().toString());
                            Totdos.setText(String.valueOf(mu.frmcur(mu.round(val2, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtdos.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtdos.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtdos.getText().toString());
                        }

                        val2 = 0;
                        val2 = 2 * txtS;
                        Totdos.setText(String.valueOf(mu.frmcur(mu.round(val2, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 1

            txtuno.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtuno.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtuno.getText().toString());
                        }

                        val1 = 0;
                        val1 = 1 * txtS;
                        Totuno.setText(String.valueOf(mu.frmcur(mu.round(val1, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtuno.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtuno.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtuno.getText().toString()) &&
                                (!txtuno.getText().toString().equals("0"))) {

                            val1 = 0;
                            val1 = 1 * Double.parseDouble(txtuno.getText().toString());
                            Totuno.setText(String.valueOf(mu.frmcur(mu.round(val1, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtuno.setText("");
                            }

                        } else {

                            double txtS = 0;

                            if (txtuno.getText().toString().equals("")) {
                                txtS = 0;
                            } else {
                                txtS = Double.parseDouble(txtuno.getText().toString());
                            }

                            val1 = 0;
                            val1 = 1 * txtS;
                            Totuno.setText(String.valueOf(mu.frmcur(mu.round(val1, gl.peDec))));

                            CalculaTotales();

                        }

                        return true;
                    }

                    return false;
                }
            });

            //EditText de valor 0.50

            txtCntCvs.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtCntCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtCntCvs.getText().toString());
                        }

                        val050 = 0;
                        val050 = 0.50 * txtS;
                        TotCntCvs.setText(String.valueOf(mu.frmcur(mu.round(val050, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtCntCvs.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtCntCvs.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtCntCvs.getText().toString()) &&
                                (!txtcien.getText().toString().equals("0"))) {

                            val050 = 0;
                            val050 = 0.50 * Double.parseDouble(txtCntCvs.getText().toString());
                            TotCntCvs.setText(String.valueOf(mu.frmcur(mu.round(val050, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtCntCvs.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtCntCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtCntCvs.getText().toString());
                        }

                        val050 = 0;
                        val050 = 0.50 * txtS;
                        TotCntCvs.setText(String.valueOf(mu.frmcur(mu.round(val050, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 0.25

            txtVCCvs.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtVCCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtVCCvs.getText().toString());
                        }

                        val025 = 0;
                        val025 = 0.25 * txtS;
                        totVCCvs.setText(String.valueOf(mu.frmcur(mu.round(val025, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtVCCvs.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtVCCvs.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtVCCvs.getText().toString()) &&
                                (!txtVCCvs.getText().toString().equals("0"))) {

                            val025 = 0;
                            val025 = 0.25 * Double.parseDouble(txtVCCvs.getText().toString());
                            totVCCvs.setText(String.valueOf(mu.frmcur(mu.round(val025, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtVCCvs.setText("");
                            }

                        }

                        return true;

                    } else {

                        double txtS = 0;

                        if (txtVCCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtVCCvs.getText().toString());
                        }

                        val025 = 0;
                        val025 = 0.25 * txtS;
                        totVCCvs.setText(String.valueOf(mu.frmcur(mu.round(val025, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 0.10

            txtDiezCvs.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtDiezCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtDiezCvs.getText().toString());
                        }

                        val010 = 0;
                        val010 = 0.10 * txtS;
                        totDiezCvs.setText(String.valueOf(mu.frmcur(mu.round(val010, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtDiezCvs.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtDiezCvs.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtDiezCvs.getText().toString()) &&
                                (!txtDiezCvs.getText().toString().equals("0"))) {

                            val010 = 0;
                            val010 = 0.10 * Double.parseDouble(txtDiezCvs.getText().toString());
                            totDiezCvs.setText(String.valueOf(mu.frmcur(mu.round(val010, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtDiezCvs.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtDiezCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtDiezCvs.getText().toString());
                        }

                        val010 = 0;
                        val010 = 0.10 * txtS;
                        totDiezCvs.setText(String.valueOf(mu.frmcur(mu.round(val010, gl.peDec))));

                        CalculaTotales();

                    }
                    return false;
                }
            });

            //EditText de valor 0.05

            txtCincoCvs.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtCincoCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtCincoCvs.getText().toString());
                        }

                        val005 = 0;
                        val005 = 0.05 * txtS;
                        totCincoCvs.setText(String.valueOf(mu.frmcur(mu.round(val005, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtCincoCvs.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtCincoCvs.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtCincoCvs.getText().toString()) &&
                                (!txtCincoCvs.getText().toString().equals("0"))) {

                            val005 = 0;
                            val005 = 0.05 * Double.parseDouble(txtCincoCvs.getText().toString());
                            totCincoCvs.setText(String.valueOf(mu.frmcur(mu.round(val005, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtCincoCvs.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtCincoCvs.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtCincoCvs.getText().toString());
                        }

                        val005 = 0;
                        val005 = 0.05 * txtS;
                        totCincoCvs.setText(String.valueOf(mu.frmcur(mu.round(val005, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });

            //EditText de valor 0.01

            txtUnCv.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try {

                        double txtS = 0;

                        if (txtUnCv.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtUnCv.getText().toString());
                        }

                        val001 = 0;
                        val001 = 0.01 * txtS;
                        totUnCv.setText(String.valueOf(mu.frmcur(mu.round(val001, gl.peDec))));

                        CalculaTotales();

                        if (falta < 0) {
                            msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                            txtUnCv.setText("");
                        }

                    } catch (Exception ex) {
                        msgbox(ex.getMessage());
                    }

                }

            });

            txtUnCv.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((keyCode == KeyEvent.KEYCODE_ENTER) &&
                            (event.getAction() == KeyEvent.ACTION_DOWN)) {

                        if (!mu.emptystr(txtUnCv.getText().toString()) &&
                                (!txtUnCv.getText().toString().equals("0"))) {

                            val001 = 0;
                            val001 = 0.01 * Double.parseDouble(txtUnCv.getText().toString());
                            totUnCv.setText(String.valueOf(mu.frmcur(mu.round(val001, gl.peDec))));

                            CalculaTotales();

                            if (falta < 0) {
                                msgbox("Cantidad incorrecta,por favor ingrese una cantidad válida.");
                                txtUnCv.setText("");
                            }

                        }

                        return true;
                    } else {

                        double txtS = 0;

                        if (txtUnCv.getText().toString().equals("")) {
                            txtS = 0;
                        } else {
                            txtS = Double.parseDouble(txtUnCv.getText().toString());
                        }

                        val001 = 0;
                        val001 = 0.01 * txtS;
                        totUnCv.setText(String.valueOf(mu.frmcur(mu.round(val001, gl.peDec))));

                        CalculaTotales();

                    }

                    return false;
                }
            });


        } catch (Exception ex) {
            msgbox(ex.getMessage());
        }

    }

    private void CalculaTotales() {

        try {

            valtotc1 = val100 + val50 + val20 + val10 + val5 + val2 + val1;

            lblTotC1.setText(String.valueOf(mu.frmcur(mu.round(valtotc1, gl.peDec))));

            valtotc2 = val050 + val025 + val010 + val005 + val001;

            lblTotC2.setText(String.valueOf(mu.frmcur(mu.round(valtotc2, gl.peDec))));

            valtot = valtotc1 + valtotc2;

            falta = gl.totDep - valtot;

            //lblTotal.setText(String.valueOf(valtot));
            lblFaltantes.setText(String.valueOf(mu.frmcur(mu.round(falta, gl.peDec))));

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    public void save(View view){
        if (SaveDesglose()) desglose.super.finish();
    }

    public boolean SaveDesglose() {
        String sql = "";
        double fdif;

        try {

            if (gl.depparc) editando = false;

            if (!editando) {

                CalculaTotales();

                fdif=mu.round(falta, 2);
                if ( fdif!= 0) {
                    msgbox("Cantidad faltante distinta a 0"); return false;
                }

                sql = "DELETE FROM T_DEPOSB";
                db.execSQL(sql);

                db.beginTransaction();

                this.sql = "";

                if (val100 > 0) {
                    int cant = 0;
                    if (txtcien.getText().toString() != "") {
                        cant = Integer.parseInt(txtcien.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 100);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val50 > 0) {
                    int cant = 0;
                    if (txtcint.getText().toString() != "") {
                        cant = Integer.parseInt(txtcint.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 50);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val20 > 0) {
                    int cant = 0;
                    if (txtveint.getText().toString() != "") {
                        cant = Integer.parseInt(txtveint.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 20);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val10 > 0) {
                    int cant = 0;
                    if (txtdies.getText().toString() != "") {
                        cant = Integer.parseInt(txtdies.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 10);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val5 > 0) {
                    int cant = 0;
                    if (txtcinco.getText().toString() != "") {
                        cant = Integer.parseInt(txtcinco.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 5);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val2 > 0) {
                    int cant = 0;
                    if (txtdos.getText().toString() != "") {
                        cant = Integer.parseInt(txtdos.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 2);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val1 > 0) {
                    int cant = 0;
                    if (txtuno.getText().toString() != "") {
                        cant = Integer.parseInt(txtuno.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 1);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "B");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val050 > 0) {
                    int cant = 0;
                    if (txtCntCvs.getText().toString() != "") {
                        cant = Integer.parseInt(txtCntCvs.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 0.50);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "M");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val025 > 0) {
                    int cant = 0;
                    if (txtVCCvs.getText().toString() != "") {
                        cant = Integer.parseInt(txtVCCvs.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 0.25);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "M");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val010 > 0) {
                    int cant = 0;
                    if (txtDiezCvs.getText().toString() != "") {
                        cant = Integer.parseInt(txtDiezCvs.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 0.10);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "M");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val005 > 0) {
                    int cant = 0;
                    if (txtCincoCvs.getText().toString() != "") {
                        cant = Integer.parseInt(txtCincoCvs.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 0.05);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "M");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }

                if (val001 > 0) {
                    int cant = 0;
                    if (txtUnCv.getText().toString() != "") {
                        cant = Integer.parseInt(txtUnCv.getText().toString());
                    }
                    ins.init("T_DEPOSB");
                    ins.add("DENOMINACION", 0.01);
                    ins.add("CANTIDAD", cant);
                    ins.add("TIPO", "M");
                    ins.add("MONEDA", "B");
                    db.execSQL(ins.sql());
                }


                db.setTransactionSuccessful();

                db.endTransaction();

                Toast.makeText(this, "Desglose guardado correctamente", Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
        }

        return true;
    }

    private void impresDesglose() {
        Cursor DT;
        impres = 0;


        if (SaveDesglose()) {

            if (prn.isEnabled()) {
                fdesg.buildPrint(corel, 0, "TOL");
                prn.printask(printcallback);
            } else {
                finish();
            }
        }

        return;
    }

    private void askPrint() {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Road");
            dialog.setMessage("¿Impresión correcta?");
            dialog.setCancelable(false);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    gl.closeCliDet = true;
                    gl.closeVenta = true;

                    impres++;
                    toast("Impres " + impres);

                    try {
                        sql = "UPDATE D_DESPOSB SET IMPRES=IMPRES+1 WHERE COREL='" + corel + "'";
                        db.execSQL(sql);
                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    }

                    /*if (impres > 1) {

                        try {
                            sql = "UPDATE D_DEPOSB SET IMPRES=IMPRES+1 WHERE COREL='" + corel + "'";
                            db.execSQL(sql);
                        } catch (Exception e) {
                            addlog(new Object() {
                            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                        }

                        gl.brw = 0;
                        desglose.super.finish();
                    } else {

                        prn.printask(printcallback);

                    }
                    */

                    finish();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //singlePrint();
                    prn.printask(printcallback);
                    //finish();
                }
            });

            dialog.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    protected void onResume() {

        try {
            super.onResume();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }
}
//fin de clase