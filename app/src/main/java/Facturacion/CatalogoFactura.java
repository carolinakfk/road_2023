package Facturacion;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.roadp.AppMethods;
import com.dts.roadp.BaseDatos;
import com.dts.roadp.PBase;
import com.dts.roadp.appGlobals;
import com.dts.roadp.clsClasses;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class CatalogoFactura extends PBase {

    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private String sql;
    private Context cont;
    private appGlobals gl;
    private AppMethods app;

    public CatalogoFactura(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        cont=context;
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
        gl = ((appGlobals) (((Activity) cont).getApplication()));

    }

    public void Reconectar(BaseDatos dbconnection, SQLiteDatabase dbase) {
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
    }

    public void InsertarFELControl(clsClasses.clsControlFEL ItemFEL) {
        Cursor dt;
        String vFechaAutorizacion = "";
        try {

            sql = "SELECT MAX(IdTablaControl) FROM D_FACTURA_CONTROL_CONTINGENCIA";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            ItemFEL.Id = dt.getInt(0) + 1;

            ins.init("D_FACTURA_CONTROL_CONTINGENCIA");

            ins.add("IdTablaControl", ItemFEL.Id);
            ins.add("Cufe", ItemFEL.Cufe);
            ins.add("TipoDocumento", ItemFEL.TipoDoc);
            ins.add("NumeroDocumento", StringUtils.right("0000000000" + ItemFEL.NumDoc,10));//FacturaFEL.gDGen.dNroDF
            ins.add("Sucursal", ItemFEL.Sucursal);
            ins.add("Caja", ItemFEL.Caja);
            ins.add("Estado", ItemFEL.Estado);
            ins.add("Mensaje", ItemFEL.Mensaje);
            ins.add("Valor_XML", ItemFEL.ValorXml);
            ins.add("FechaEnvio", ItemFEL.FechaEnvio);
            ins.add("TipoFactura", ItemFEL.TipFac);
            ins.add("Fecha_Agr", ItemFEL.FechaAgr);
            ins.add("QR", ItemFEL.QR);
            ins.add("COREL", ItemFEL.Corel);
            ins.add("RUTA", ItemFEL.Ruta);
            ins.add("VENDEDOR", ItemFEL.Vendedor);
            ins.add("HOST", ItemFEL.Host);
            ins.add("CODIGOLIQUIDACION", ItemFEL.CodLiquidacion);
            ins.add("CORELATIVO", ItemFEL.Correlativo);
            ins.add("QRIMAGE", ItemFEL.QRImg);

            if (ItemFEL.Fecha_Autorizacion!=null && ItemFEL.Fecha_Autorizacion.length() > 0){
                vFechaAutorizacion = ItemFEL.Fecha_Autorizacion.equals("0001-01-01T00:00:00")?
                        "1900-01-01T00:00:00":
                        ItemFEL.Fecha_Autorizacion.toString().substring(0,ItemFEL.Fecha_Autorizacion.length()-6);
            }else{
                vFechaAutorizacion = "1900-01-01T00:00:00";
            }

            ins.add("FECHA_AUTORIZACION",vFechaAutorizacion);
            ins.add("NUMERO_AUTORIZACION", ItemFEL.Numero_Autorizacion);

            db.execSQL(ins.sql());

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    public void ActualizaFELControl(clsClasses.clsControlFEL ItemFEL, String Corel) {
        String vFechaAutorizacion = "";
        try {

            upd.init("D_FACTURA_CONTROL_CONTINGENCIA");

            upd.add("Cufe", ItemFEL.Cufe);
            upd.add("Estado", ItemFEL.Estado);
            upd.add("Mensaje", ItemFEL.Mensaje);
            upd.add("Valor_XML", ItemFEL.ValorXml);
            upd.add("QR", ItemFEL.QR);
            upd.add("HOST", ItemFEL.Host);
            upd.add("CODIGOLIQUIDACION", ItemFEL.CodLiquidacion);
            upd.add("CORELATIVO", ItemFEL.Correlativo);
            upd.add("QRIMAGE", ItemFEL.QRImg);

            vFechaAutorizacion = "1900-01-01T00:00:00";

            if (ItemFEL.Fecha_Autorizacion!=null ){
                if (ItemFEL.Fecha_Autorizacion.length() > 0) {
                    vFechaAutorizacion = ItemFEL.Fecha_Autorizacion.equals("0001-01-01T00:00:00") ?
                            "1900-01-01T00:00:00" :
                            ItemFEL.Fecha_Autorizacion.toString().substring(0, ItemFEL.Fecha_Autorizacion.length() - 6);
                }
            }

            upd.add("FECHA_AUTORIZACION",vFechaAutorizacion);
            upd.add("NUMERO_AUTORIZACION", ItemFEL.Numero_Autorizacion);
            upd.Where("COREL ='"+ Corel +"' ");

            db.execSQL(upd.SQL());
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
    }

    public void InsertarNotaDebito(clsClasses.clsNotaCreditoEnc Item) {
        try {
            opendb();
            db.beginTransaction();

            ins.init("D_NOTACRED");

            ins.add("COREL",gl.dvcorelnd);
            ins.add("ANULADO","N");
            ins.add("FECHA",Item.Fecha);
            ins.add("RUTA", Item.Ruta);
            ins.add("VENDEDOR",Item.Vendedor);
            ins.add("CLIENTE", Item.Cliente);
            ins.add("TOTAL", Item.Total);
            ins.add("FACTURA",Item.Factura);
            ins.add("SERIE", Item.Serie);
            ins.add("CORELATIVO",gl.dvactualnd);
            ins.add("STATCOM",Item.Statcom);
            ins.add("CODIGOLIQUIDACION",Item.CodigoLiquidacion);
            ins.add("RESOLNC",Item.ResolNC);
            ins.add("SERIEFACT",Item.SerieFact);
            ins.add("CORELFACT",Item.CorelFact);
            ins.add("IMPRES",Item.Impres);
            ins.add("CERTIFICADA_DGI", 0);
            ins.add("TIPO_DOCUMENTO", Item.TipoDocumento);
            ins.add("COREL_REFERENCIA", Item.Cufe);
            ins.add("ES_ANULACION", Item.EsAnulacion);
            ins.add("CUFE_FACTURA", Item.CufeFactura);

            db.execSQL(ins.sql());

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    public clsClasses.clsMunicipio getMunicipio(String CodMuni) {
        clsClasses.clsMunicipio Municipio = clsCls.new clsMunicipio();
        Cursor dt;
        try {
            sql= "SELECT * FROM P_MUNI WHERE CODIGO = '"+CodMuni+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Municipio.codigo = dt.getString(0);
                Municipio.depar = dt.getString(1);
                Municipio.nombre = dt.getString(2);
            }

            if(dt!=null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Municipio;
    }

    public clsClasses.clsCiudad getCiudad(String CodCiudad) {
        clsClasses.clsCiudad Ciudad = null;
        Cursor dt;
        try {
            sql= "SELECT * FROM P_CIUDAD WHERE CODIGO = '"+CodCiudad+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Ciudad = clsCls.new clsCiudad();
                Ciudad.codigo = dt.getString(0);
                Ciudad.distrito = dt.getString(1);
                Ciudad.corregimiento = dt.getString(2);
                Ciudad.provincia = dt.getString(3);
            }

            if(dt!=null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Ciudad;
    }

    public clsClasses.clsDepartamento getDepartamento(String CodDep) {
        clsClasses.clsDepartamento Departamento = clsCls.new clsDepartamento();
        Cursor dt;
        try {
            sql= "SELECT * FROM P_DEPAR WHERE CODIGO = '"+CodDep+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Departamento.nombre = dt.getString(2);
            }

            if(dt!=null) dt.close();
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Departamento;
    }

    public clsClasses.clsCliente getCliente(String CodCliente) {
        clsClasses.clsCliente Cliente = clsCls.new clsCliente();
        Cursor dt;
        try {
            sql= "SELECT NOMBRE, TIPO_CONTRIBUYENTE, TIPORECEPTOR, EMAIL, TELEFONO, COD_PAIS, DIRECCION, CIUDAD, NIT, MUNICIPIO, MEDIAPAGO, DIACREDITO, CODIGO " +
                    " FROM P_CLIENTE WHERE CODIGO = '"+CodCliente+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Cliente.nombre = dt.getString(0);
                Cliente.codigo = dt.getString(12);
                Cliente.tipoContribuyente = dt.getString(1);
                Cliente.tipoRec = dt.getString(2);
                Cliente.email = dt.getString(3);
                Cliente.telefono = dt.getString(4);
                Cliente.codPais = dt.getString(5);
                Cliente.direccion = dt.getString(6).substring(0,(dt.getString(6).length()>=100?100:dt.getString(6).length()));
                Cliente.ciudad = dt.getString(7);
                Cliente.nit = dt.getString(8);
                Cliente.muni = dt.getString(9);
                Cliente.mediapago = Integer.valueOf(dt.getString(10));
                Cliente.diascredito = Integer.valueOf(dt.getString(11));
            }

            if(dt!=null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Cliente;
    }

    public clsClasses.clsSucursal getSucursal() {
        clsClasses.clsSucursal Sucursal = clsCls.new clsSucursal();
        Cursor dt;

        try {
            sql="SELECT * FROM P_SUCURSAL";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Sucursal.codigo = dt.getString(0);
                Sucursal.descripcion = dt.getString(2);
                Sucursal.nombre = dt.getString(3);
                Sucursal.direccion = dt.getString(4);
                Sucursal.telefono = dt.getString(5);
                Sucursal.nit = dt.getString(6);
                Sucursal.texto = dt.getString(7);
                Sucursal.tipoSucursal = dt.getString(8);
                Sucursal.correo = dt.getString(9);
                Sucursal.corx = dt.getString(10);
                Sucursal.cory = dt.getString(11);
                Sucursal.codubi = dt.getString(12);
                Sucursal.tipoRuc = dt.getString(13);
                Sucursal.codMuni = dt.getString(14);
                Sucursal.sitio_web = dt.getString(15);

            } else {
                Sucursal = null;
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Sucursal;
    }

    public clsClasses.clsProducto getProducto(String Codigo) {

        clsClasses.clsProducto Producto = clsCls.new clsProducto();
        Cursor dt;

        try {

            sql="SELECT DESCLARGA, UNIDBAS, SUBBODEGA  FROM P_PRODUCTO " +
                    " WHERE CODIGO = '"+Codigo+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Producto.codigo = Codigo;
                Producto.nombre = dt.getString(0);
                Producto.um = dt.getString(1);
                Producto.subBodega = dt.getString(2);
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Producto;
    }

    public String getUMDGI(String CodigoUM) {
        Cursor dt;
        String Codigo="";
        try {
            sql="SELECT CODIGO_CGI  FROM P_MEDIDA " +
                    " WHERE CODIGO = '"+CodigoUM+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Codigo = dt.getString(0);
            }

            if (dt != null) dt.close();
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Codigo;
    }

    public clsClasses.clsNotaCreditoEnc GetEncNotaCredito(String Corel) {
        Cursor dt;
        clsClasses.clsNotaCreditoEnc NotaCreditoEnc = clsCls.new clsNotaCreditoEnc();

        try {
            sql = "SELECT * FROM D_NOTACRED WHERE COREL = '"+Corel+"' AND TIPO_DOCUMENTO = 'NC' ";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();

                NotaCreditoEnc.Corel = dt.getString(0);
                NotaCreditoEnc.Anulado = dt.getString(1);
                NotaCreditoEnc.Fecha = dt.getLong(2);
                NotaCreditoEnc.Ruta = dt.getString(3);
                NotaCreditoEnc.Vendedor = dt.getString(4);
                NotaCreditoEnc.Cliente = dt.getString(5);
                NotaCreditoEnc.Total = dt.getDouble(6);
                NotaCreditoEnc.Factura = dt.getString(7);
                NotaCreditoEnc.Serie = dt.getString(8);
                NotaCreditoEnc.Correlativo = dt.getString(9);
                NotaCreditoEnc.Statcom = dt.getString(10);
                NotaCreditoEnc.CodigoLiquidacion = dt.getInt(11);
                NotaCreditoEnc.ResolNC = dt.getString(12);
                NotaCreditoEnc.SerieFact = dt.getString(13);
                NotaCreditoEnc.CorelFact = dt.getInt(14);
                NotaCreditoEnc.Impres = dt.getInt(15);
                NotaCreditoEnc.CertificadaDgi =  dt.getInt(16);
                NotaCreditoEnc.Cufe = dt.getString(17);
                NotaCreditoEnc.TipoDocumento = dt.getString(18);
                NotaCreditoEnc.CorelRef = dt.getString(19);
                NotaCreditoEnc.EsAnulacion = dt.getInt(20);
                NotaCreditoEnc.CufeFactura = dt.getString(21);
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return  NotaCreditoEnc;
    }

    public ArrayList<clsClasses.clsBeNotaCreditoDet> GetDetalleNT(String Corel) {
        ArrayList<clsClasses.clsBeNotaCreditoDet> lista = new ArrayList<>();
        clsClasses.clsBeNotaCreditoDet item;
        Cursor dt;

        try {
            lista. clear();

            sql = "SELECT A.*, B.DESCCORTA " +
                  " FROM D_NOTACREDD A " +
                  " INNER JOIN P_PRODUCTO B ON B.CODIGO = A.PRODUCTO " +
                  " WHERE A.COREL = '"+Corel+"' AND A.TIPO_DOCUMENTO = 'NC'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    item = clsCls.new clsBeNotaCreditoDet();

                    item.corel = dt.getString(0);
                    item.codigoProd = dt.getString(1);
                    item.precio = dt.getDouble(2);
                    item.precioAct = dt.getDouble(3);
                    item.cant = dt.getDouble(4);
                    item.peso = dt.getDouble(5);
                    item.porpeso = dt.getString(6);
                    item.umVenta = dt.getString(7);
                    item.umStock = dt.getString(8);
                    item.umpeso = dt.getString(9);
                    item.factor = dt.getDouble(10);
                    item.producto = dt.getString(11);


                    lista.add(item);
                    dt.moveToNext();
                }
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return lista;
    }

    public void UpdateEstadoNotaCredito(String Cufe, String CufeFact, String EstadoNC) {
        try {

            int CertificadaDGI = EstadoNC.equals("2") ? 1:0;

            sql = "UPDATE D_NOTACRED SET CUFE ='" + Cufe + "', CUFE_FACTURA ='" + CufeFact +"', CERTIFICADA_DGI=" + CertificadaDGI + "  WHERE COREL='" + gl.devcornc + "'";
            db.execSQL(sql);
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }
    }

    public void UpdateEstadoFactura(String Cufe, String EstadoFac, String corel) {

        try {

            int CertificadaDGI = EstadoFac.equals("2") ? 1:0;

            sql="UPDATE D_FACTURA SET CUFE ='"+Cufe+"', CERTIFICADA_DGI="+CertificadaDGI+"  WHERE COREL='"+corel+"'";
            db.execSQL(sql);

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }
    }

    public void UpdateEstadoNotaDebito(String Cufe, int EstadoFac, String corel) {
        try {
            sql="UPDATE D_NOTACRED SET CUFE ='"+Cufe+"', CERTIFICADA_DGI="+EstadoFac+"  WHERE COREL='"+corel+"'" +" AND TIPO_DOCUMENTO = 'ND'";
            db.execSQL(sql);
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }
    }

    public String FechaCredito(int diascredito) {
        String fechaNueva= "";
        try {
            SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, diascredito);

            fechaNueva = fecha.format(calendar.getTime())+"-05:00";

        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }

        return  fechaNueva;
    }

    public String ReplaceXML(String texto) {
        String Vocales = "ÁáÉéÍíÓóÚúÑñÜü", VocalesSinAcento = "AaEeIiOoUuNnUu";

        char[] array = texto.toCharArray();
        try {

            for (int index = 0; index < array.length; index++) {
                int pos = Vocales.indexOf(array[index]);
                if (pos > -1) {
                    array[index] = VocalesSinAcento.charAt(pos);
                }
            }

        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return new String(array);
    }

/*    public String obtienecorrel(String tipo){
        String correl="";
        Cursor DT;

        try {
            sql="SELECT SERIE,ACTUAL+1,FINAL,INICIAL FROM P_CORREL_OTROS WHERE RUTA='"+gl.ruta+"' AND TIPO='"+tipo+"'";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                correl=DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)), 6);

                gl.dvactualnd = String.valueOf(DT.getInt(1));

                if (tipo.equals("D")){
                    gl.dvactuald = String.valueOf(DT.getInt(1));
                }else{
                    gl.dvactualnd = String.valueOf(DT.getInt(1));
                }
            }else{
                if (!gl.peModal.equalsIgnoreCase("TOL")){
                    correl=gl.ruta+"_"+mu.getCorelBase();
                    if (tipo.equals("D")){
                        gl.dvactuald = String.valueOf(1);
                    }else{
                        gl.dvactualnd = String.valueOf(1);
                    }
                }
            }

            if (DT != null) DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return  correl;

    }
*/

    public String obtienecorrel(String tipo){
        String correl="",fserie;
        Cursor DT;
        int cactual,cfinal,ca1,ca2,fcorel;

        try{
            sql = " SELECT SERIE, ACTUAL, FINAL, INICIAL, TIPO " +
                    " FROM P_CORREL_OTROS " +
                    " WHERE RUTA='" + gl.ruta + "' AND TIPO='" + tipo + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                fserie = DT.getString(0);
                ca1 = DT.getInt(1);
                cfinal = DT.getInt(3);
            } else {
                fserie = "";
                fcorel = 0;
                if (gl.peModal.equalsIgnoreCase("TOL")){
                    mu.msgbox("No está definido correlativo de NC. No se puede continuar con la Nota de Crédito por devolución.\n");
                }else{
                    correl=gl.ruta+"_"+mu.getCorelBase();
                    if (tipo.equals("D")){
                        gl.dvactuald = String.valueOf(1);
                        gl.dvSeried = fserie;
                    }else if (tipo.equals("ND")){
                        gl.dvactualnd = String.valueOf(1);
                        gl.dvSeriend = fserie;
                    }else if (tipo.equals("NC")){
                        gl.dvactualnc = String.valueOf(1);
                        gl.dvSerienc = fserie;
                    }
                }
                return "";
            }

            sql = " SELECT MAX(COREL) FROM D_NOTACRED_LOG " +
                    " WHERE RUTA='" + gl.ruta + "' AND " +
                    "       SERIE='" + fserie + "' AND" +
                    "       TIPO='" + tipo + "'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();
                ca2 = DT.getInt(0);
            } else {
                ca2 = 0;
            }

            DT.close();

            cactual = ca1;
            if (ca2 > cactual) cactual = ca2;
            fcorel = cactual + 1;

            correl=fserie + StringUtils.right("000000" + Integer.toString(fcorel), 6);

            if (tipo.equals("D")){
                gl.dvactuald = String.valueOf(fcorel);
                gl.dvSeried = fserie;
            }else if (tipo.equals("ND")){
                gl.dvactualnd = String.valueOf(fcorel);
                gl.dvSeriend = fserie;
            }else if (tipo.equals("NC")){
                gl.dvactualnc = String.valueOf(fcorel);
                gl.dvSerienc = fserie;
            }

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return  correl;

    }

    public String ExisteFacturaDControl (String pCorel) {
        String corel = "";
        Cursor dt;
        try {
            sql = "SELECT COREL FROM D_FACTURA_CONTROL_CONTINGENCIA WHERE COREL = '"+pCorel+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                corel = dt.getString(0);
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return corel;
    }

    public String ValidaTelefono(String telefono) {
        String regex = "^[0-9]{3,4}?\\-[0-9]{4}$";
        String resultado = null;

        try {
            if (telefono != null) {
                if (!telefono.isEmpty()) {
                    resultado = telefono.trim().matches(regex) ? telefono : null;
                }
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

        return resultado;
    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    public clsClasses.clsRUC getRUC(String pNit){
        clsClasses.clsRUC pRUC=clsCls.new clsRUC();

        try{
            if (pNit.length()>0) {
                String[] DVRuc = pNit.split(" ");
                if (DVRuc.length > 1) {
                    pRUC.sRUC = DVRuc[0].trim();
                    if(DVRuc.length >= 2 ){
                        if (DVRuc[1].trim().equals("")) {
                            if (DVRuc.length >= 4) {
                                pRUC.sDV = StringUtils.right("00" + DVRuc[3].trim(), 2);
                            } else {
                                pRUC.sDV = "";
                                //progress.cancel();
                                //msgbox(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
                            }
                        }else{
                            if(DVRuc.length >= 3 ){
                                pRUC.sDV = StringUtils.right("00" + DVRuc[2].trim(),2);
                            }else{
                                pRUC.sDV = "";
                                //progress.cancel();
                                //msgbox(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
                            }
                        }
                    }

                }else{
                    pRUC.sRUC= pNit;
                    pRUC.sDV = "";
                }
            }else {
                pRUC.sRUC = "";
            }
        }catch (Exception e){
            msgbox("Error al validar el RUC " + pNit);
        }
        return  pRUC;
    }

}
