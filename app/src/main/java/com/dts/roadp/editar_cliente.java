package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class editar_cliente extends PBase {

    private EditText txtNombreCliente, txtNitCliente, txtCanalCliente, txtSubCanalCliente,
            txtDir, txtContacto, txtEmail, txtTelefono, txtProvincia, txtDistrito;
    private TextView lbCordenada;
    private ImageView btnBuscarCanal, imgGuardar, btnBuscarProv;
    private TableRow rowNombre, rowRuc;
    private String codigo, idCanal, idSubcanal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cliente);

        super.InitBase();
        lbCordenada = (TextView) findViewById(R.id.lbCoor);
        btnBuscarCanal = (ImageView) findViewById(R.id.btnBuscarCanal);
        btnBuscarProv = (ImageView) findViewById(R.id.btnBuscarProv);
        imgGuardar = (ImageView) findViewById(R.id.imgGuardar);

        txtNombreCliente = (EditText) findViewById(R.id.txtNombreCliente);
        txtNitCliente = (EditText) findViewById(R.id.txtNitCliente);
        txtDir = (EditText) findViewById(R.id.txtDireccion);
        txtContacto = (EditText) findViewById(R.id.txtContacto);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtTelefono = (EditText) findViewById(R.id.txtTelefono);
        txtProvincia = (EditText) findViewById(R.id.txtProvincia);
        txtDistrito = (EditText) findViewById(R.id.txtCorregimiento);
        txtCanalCliente = (EditText) findViewById(R.id.txtCanalCliente);
        txtSubCanalCliente = (EditText) findViewById(R.id.txtSubCanalCliente);


        rowNombre = (TableRow) findViewById(R.id.RowNombre);
        rowRuc = (TableRow) findViewById(R.id.RowRuc);

        codigo = gl.cliente;
        getDatosCLiente();
        setData();


       if (!gl.peEditarNombre) {
           rowNombre.setVisibility(View.GONE);
        }

       if (!gl.peEditarNit) {
           rowRuc.setVisibility(View.GONE);
       }

       setHandlers();
    }

    private void GuardarData() {
        String corel = gl.ruta+mu.getCorelBase();
        String tabla = "D_CLIENTE_MODIF";
        long f= du.getActDateTime();

        try {

            String txtCanal = txtCanalCliente.getText().toString();
            String txtSubCanal = txtSubCanalCliente.getText().toString();
            String pNitCliente = txtNitCliente.getText().toString();
            String pNombreCliente = txtNombreCliente.getText().toString();
            String pTelefono = txtTelefono.getText().toString();
            String pDir = txtDir.getText().toString();
            String pContacto = txtContacto.getText().toString();
            String pEmail = txtEmail.getText().toString();
            String pMuni = gl.IdMun;

            if (mu.emptystr(txtCanal) || mu.emptystr(txtSubCanal)) {
                toast("Los campos canal y subcanal son obligatorios");
                return;
            }

            if (rowRuc.getVisibility()!=View.GONE){
                if (mu.emptystr(pNitCliente) ) {
                    toast("Debe ingresar el RUC del cliente");
                    txtNitCliente.requestFocus();
                    return;
                }
            }

            if (rowNombre.getVisibility()!=View.GONE){
                if (mu.emptystr(pNombreCliente) ) {
                    toast("Debe ingresar el nombre del cliente");
                    txtNombreCliente.requestFocus();
                    return;
                }
            }

            if (mu.emptystr(pTelefono) ) {
                toast("Debe ingresar el teléfono del cliente");
                txtTelefono.requestFocus();
                return;
            }

            if (mu.emptystr(pDir) ) {
                toast("Debe ingresar la dirección del cliente");
                txtDir.requestFocus();
                return;
            }

            if (mu.emptystr(pContacto) ) {
                toast("Debe ingresar el contacto del cliente");
                txtContacto.requestFocus();
                return;
            }

            if (mu.emptystr(pEmail) ) {
                toast("Debe ingresar el correo del cliente");
                txtEmail.requestFocus();
                return;
            }

            if (mu.emptystr(pMuni) ) {
                toast("Debe ingresar el Municipio del cliente");
                return;
            }


            if (validaCliente() == 0) {

                ins.init(tabla);

                ins.add("COREL", corel);
                ins.add("CODIGO", codigo);
                ins.add("NOMBRE", txtNombreCliente.getText().toString());
                ins.add("CANAL", idCanal);
                ins.add("NIT", txtNitCliente.getText().toString());
                ins.add("SUBCANAL", idSubcanal);
                ins.add("BLOQUEADO", "0");
                ins.add("TIPONEG", "");
                ins.add("TIPO", "");
                ins.add("SUBTIPO", "");
                ins.add("NIVELPRECIO", "0");
                ins.add("MEDIAPAGO", "");
                ins.add("LIMITECREDITO", "0");
                ins.add("DIACREDITO", "0");
                ins.add("DESCUENTO", "");
                ins.add("BONIFICACION", "");
                ins.add("IMPSPEC", "0");
                ins.add("INVTIPO", "");
                ins.add("INVEQUIPO", "");
                ins.add("INV1", "");
                ins.add("INV2", "");
                ins.add("INV3", "");
                ins.add("MENSAJE", "");
                ins.add("TELEFONO", txtTelefono.getText().toString());
                ins.add("DIRTIPO", "");
                ins.add("DIRECCION", txtDir.getText().toString());
                ins.add("SUCURSAL", "");
                ins.add("COORX", gl.gpspx);
                ins.add("COORY", gl.gpspy);
                ins.add("FIRMADIG", "");
                ins.add("CODBARRA", "");
                ins.add("VALIDACREDITO", "");
                ins.add("PRECIO_ESTRATEGICO", "");
                ins.add("NOMBRE_PROPIETARIO", "");
                ins.add("NOMBRE_REPRESENTANTE", "");
                ins.add("BODEGA", "");
                ins.add("COD_PAIS", "");
                ins.add("FACT_VS_FACT", "");
                ins.add("CHEQUEPOST", "");
                ins.add("PERCEPCION", "0");
                ins.add("TIPO_CONTRIBUYENTE", "");
                ins.add("ID_DESPACHO", "0");
                ins.add("ID_FACTURACION", "0");
                ins.add("MODIF_PRECIO", "");
                ins.add("INGRESA_CANASTAS", "0");
                ins.add("PRIORIZACION", "");
                ins.add("STATCOM", "N");
                ins.add("FECHA_SISTEMA", f);
                ins.add("CONTACTO", txtContacto.getText().toString());
                ins.add("MUNICIPIO", gl.IdMun);
                ins.add("EMAIL", txtEmail.getText().toString());
                db.execSQL(ins.sql());

            } else {

                upd.init(tabla);
                upd.add("CANAL", idCanal);
                upd.add("SUBCANAL", idSubcanal);
                upd.add("STATCOM", "N");
                upd.add("FECHA_SISTEMA", f);
                upd.add("MUNICIPIO", gl.IdMun);
                upd.add("DIRECCION", txtDir.getText().toString());
                upd.add("TELEFONO", txtTelefono.getText().toString());
                upd.add("COORY", gl.gpspx);
                upd.add("COORY", gl.gpspy);
                upd.add("EMAIL", txtEmail.getText().toString());
                upd.add("CONTACTO", txtContacto.getText().toString());
                upd.Where("CODIGO='"+codigo+"'");

                db.execSQL(upd.SQL());
            }

            toast("Cliente actualizado correctamente");
            UpdClienteP();
            limpiar();

            Regresar();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            mu.msgbox(e.getMessage());
        }
    }

    private void UpdClienteP() {
        try {
            upd.init("P_CLIENTE");
            upd.add("CANAL", idCanal);
            upd.add("SUBCANAL", idSubcanal);
            upd.add("MUNICIPIO", gl.IdMun);
            upd.add("DIRECCION", txtDir.getText().toString());
            upd.add("TELEFONO", txtTelefono.getText().toString());
            upd.add("COORX",  gl.gpspx);
            upd.add("COORY",  gl.gpspy);
            upd.add("EMAIL", txtEmail.getText().toString());
            upd.add("CONTACTO", txtContacto.getText().toString());
            upd.Where("CODIGO='"+codigo+"'");

            db.execSQL(upd.SQL());

        } catch (Exception e) {
            addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            mu.msgbox(e.getMessage());
        }
    }

    public void getDatosCLiente() {
        Cursor DT;
        try {
            sql = "SELECT NOMBRE, NIT, DIRECCION, TELEFONO, COORX, COORY, EMAIL, CONTACTO FROM P_CLIENTE WHERE CODIGO='" + codigo + "'";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            txtNombreCliente.setText(DT.getString(0));
            txtNitCliente.setText(DT.getString(1));
            txtDir.setText(DT.getString(2));
            txtTelefono.setText(DT.getString(3));
            gl.gpspx = Double.valueOf(DT.getString(4));
            gl.gpspy = Double.valueOf(DT.getString(5));
            txtEmail.setText(DT.getString(6));
            txtContacto.setText(DT.getString(7));

            /*sql = "SELECT CONTACTO, EMAIL FROM D_CLINUEVOT WHERE CODIGO='" + codigo + "'";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();
            txtContacto.setText(DT.getString(0));
            txtEmail.setText(DT.getString(1));*/

            if (DT != null) DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
    }

    public int validaCliente() {
        Cursor DT;

        sql = "SELECT CODIGO FROM D_CLIENTE_MODIF WHERE CODIGO='"+codigo+"'";
        DT=Con.OpenDT(sql);

        if(DT!=null) DT.close();

        return  DT.getCount();

    }

    public void setGPS(View view) {
        try{
            browse=2;
            gl.gpsCliente = true;
            startActivity(new Intent(this,CliGPS.class));
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private  void VerCanales() {
        Intent intent = new Intent(this, CanalSubcanal.class);
        startActivity(intent);
    }

    private void VerProvincia() {
        Intent intent = new Intent(this, DepartamentoMun.class);
        startActivity(intent);
    }

    private void setHandlers() {
        btnBuscarCanal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerCanales();
            }
        });

        btnBuscarProv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerProvincia();
            }
        });

        imgGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuardarData();
            }
        });

    }

    private void setData() {
        idCanal = gl.IdCanal;
        idSubcanal = gl.IdSubcanal;

        lbCordenada.setText(gl.gpspx+" , "+ gl.gpspy);
        txtCanalCliente.setText(gl.EditarClienteCanal);
        txtSubCanalCliente.setText(gl.EditarClienteSubcanal);
        txtProvincia.setText(gl.CliProvincia);
        txtDistrito.setText(gl.CliDistrito);
    }

    private void limpiar() {
        gl.gpsCliente = false;
        gl.EditarClienteCanal = "";
        gl.EditarClienteSubcanal = "";
        gl.CliProvincia = "";
        gl.CliDistrito = "";
        gl.IdMun = "";
        gl.IdCanal = "";
        gl.IdSubcanal = "";
        gl.gpspx = 0.00;
        gl.gpspy = 0.00;
    }

    private  void Regresar() {
        limpiar();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    @Override
    public void onBackPressed() {
        try{
            Regresar();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }
}