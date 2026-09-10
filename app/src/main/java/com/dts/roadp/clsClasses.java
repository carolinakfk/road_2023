package com.dts.roadp;

import android.graphics.Bitmap;

public class clsClasses {
	
	public class clsCD {
		public String Cod,Desc,Text,um;
		public boolean bandera,disp, es_despacho;
		public double faltante, cantOriginal, pesoOriginal;
		public int promocion = 0;
	}
	
	public class clsCDB {
		public String Cod,Desc,Adds;
		public int Bandera,Cobro,ppend, prefacturas;
		public double valor,coorx,coory;
	}

    //JP20210614
	public class clsCFDV {
		public int id,flag,bandera,tipodoc,banderamonto=0;
		public String Cod,Desc,Fecha,Valor,Sid, Cufe, Numero_Autorizacion, Certificada_DGI, Estado, CufeFactura ="", Statcom="";
		public double val;
	}
	
	public class clsExist {
		public String Cod,Desc,Fecha,Valor,ValorM,ValorT,Peso,PesoM,PesoT,Lote,Doc,Centro,Stat,Estado;
		public double cant,cantm;
		public int id,flag,items, promocion = 0;
	}

	public class clsDevCan {
		public String Cod,Desc,Fecha,Valor,ValorM,ValorT,Peso,PesoM,PesoT,Lote,Doc,Centro,Stat;
		public double cant,cantm;
		public int id,flag,items;
	}

	public class clsMenu {
		public int ID,Icon;
		public String Name;
	}	
	
	public class clsVenta {
		public String Cod,Nombre,um,val,valp,sdesc,ums,PE;
		public double Cant,Peso,Prec,Desc,Total,imp,percep, precio,factor;

	}
	
	public class clsPromoItem {
		public String Prod,Nombre,Bon,Tipo;
		public double RIni,RFin,Valor;
		public boolean Porrango,Porprod,Porcant;
	}
	
	public class clsObj {
		public String Nombre,Cod,Meta,Acum,Falta,Perc;
	}
	
	public class clsDepos {
		public String Nombre,Cod,Tipo,Banco;
		public double Valor,Total,Efect,Chec;
		public int Bandera,NChec;
	}
	
	public class clsCobro {
		public String Factura,Tipo,fini,ffin;
		public double Valor,Saldo,Pago;
		public int id,flag;
	}
	
	public class clsPago {
		public String Tipo,Num,Valor;
		public int id;
	}
	
	public class clsEnvio {
		public String Nombre;
		public int id,env,pend;
	}
	
	public class clsBonifItem {
		public String prodid,lista,cantexact,globbon,tipocant,porcant;
		public int tipolista;
		public double valor,mul,monto;
	}	
	
	public class clsBonifProd {
		public String id,nombre,prstr;
		public int flag;
		public double cant,cantmin,disp,precio,costo;
	}	
	
	public class clsDemoDlg {
		public int tipo,flag;
		public String Cod,Desc;
		public double val;
	}

	//#HS_20181121_1546 Se agregó clsFinDia y los campos de la tabla FinDia.
	public class clsFinDiaItems {
		public int id, corel, val1, val2, val3, val4, val5, val6, val7, val8;
	}

	public class clsRepes {
		public int id,bol;
		public double peso,can;
		public String sid,speso,sbol,scan,stot;
	}

	public class  clsAyudante{
		public String idayudante;
		public String nombreayudante;
	}

	public class clsVehiculo{
		public String idVehiculo,marca,placa;
	}

	public class clsBarras{
		public String barra,peso;
	}

	public class clsDs_pedido {
		public String corel;
		public String anulado;
		public long fecha;
		public String empresa;
		public String ruta;
		public String vendedor;
		public String cliente;
		public double kilometraje;
		public long fechaentr;
		public String direntrega;
		public double total;
		public double desmonto;
		public double impmonto;
		public double peso;
		public String bandera;
		public String statcom;
		public String calcobj;
		public int  impres;
		public String add1;
		public String add2;
		public String add3;
		public String orden_compra;
		public long fechaPrecio;
	}

	public class clsDs_pedidod {
		public String corel;
		public String producto;
		public String empresa;
		public String anulado;
		public double cant;
		public double precio;
		public double imp;
		public double des;
		public double desmon;
		public double total;
		public double preciodoc;
		public double peso;
		public double val1;
		public String val2;
		public String ruta;
		public String umventa;
		public String umstock;
		public String umpeso;
		public double cantOriginal;
		public double pesoOriginal;
	}


	public class clsTipoCanastas {
		public String codigo, desccorta, desclarga;
		public int tenregado, trecibido;
		public Boolean totales=true;
	}

	public class clsCanasta {
		public String ruta, cliente, producto, vendedor;
		public int idCanasta, cantrec, cantentr;
		public String codigo, desccorta, desclarga, fechaFormato;
		public boolean editar;
		public long fecha;
	}

    public class clsPedItem {
        public String producto;
        public double cant;
	}

	public class clsCanal {
		public String codigo;
		public String nombre;
	}

	public class clsSubCanal {
		public String codigo;
		public String nombre;
		public String canal;
	}

	public class clsP_tipologia {
		public String codigo;
		public String canalsub;
		public String nombre;
	}

	public class clsDepartamento {
		public String codigo;
		public String area;
		public String nombre;
	}

	public class clsMunicipio {
		public String codigo;
		public String depar;
		public String nombre;
	}

	public class clsCiudad {
		public String codigo;
		public String distrito;
		public String corregimiento;
		public String provincia;
	}

	public class clsResProducto {
		public String codigo, nombre;
		public String cantidad;
		public String peso;
	}

	public class clsResPrefactura {
		public String codigoCli, codigoProd, nombreCli, nombreProd, Prefact, rutapreventa, cantidad, peso;
		public int flag;
	}

	public class clsDocumentoImg {
		public String nombre;
		public Bitmap img;
	}

	public class clsSucursal {
		public String codigo, empresa, descripcion, nombre, telefono, nit, direccion, texto, correo, corx, cory,
				codubi, tipoRuc, codMuni = "", tipoSucursal, sitio_web;
	}

	public class clsRUC{
		public String sRUC, sDV,sTipoReceptor;
	}

	public class clsCliente {
		public String  codigo, tipoContribuyente, tipoRec, email, telefono, codPais, nombre, direccion, ciudad, muni = "", nit;
		public int diascredito, mediapago;
	}

	public class clsProducto {
		public String  codigo, nombre, um, subBodega;
	}

	public class clsControlFEL {
		public int Id, CodLiquidacion;
		public String Cufe = "", TipoDoc = "", NumDoc = "", Sucursal = "", Caja = "", Estado = "", Mensaje = "",
				ValorXml = "", FechaEnvio = "", TipFac = "", FechaAgr = "", QR = "", Corel = "",Ruta = "",
				Vendedor = "", Host = "", Correlativo = "", QRImg = "",
				Numero_Autorizacion = "", Fecha_Autorizacion = "";	}

	public class clsEmpresa {
		public String urlToken, urlAnulacion, usuarioApi, claveApi;
	}

	public class clsBeNotaCreditoDet {
		public String corel, producto, codigoProd, umpeso, porpeso, umVenta, umStock;
		public double cant, factor, precio, precioAct, peso;
	}

	public class clsNotaCreditoEnc {
		public String Corel, Anulado, Ruta, Vendedor, Cliente, Factura, Serie, Correlativo, Statcom, ResolNC, SerieFact, Cufe, TipoDocumento, CorelRef, CufeFactura;
		public int CodigoLiquidacion, CorelFact, Impres, CertificadaDgi, EsAnulacion;
		public long Fecha;
		public Double Total;
	}

	public class  clsMmCliente {
		public double mm_estandar, mm_extaruta;
		public int setup;
	}

	public class clsBeDescuento {
		public String porPorcentaje = "";
		public double valor = 0;
		//#EJC20260721 refactor(hh-desc-result): conserva genealogía de la condición elegida.
		public int codDesc = 0;
		public int prioridad = 0;
		public int prioridadDescuento = 0;
		public int pTipo = 0;
		public String descTipo = "";
		public String producto = "";
		public String umVenta = "";
		public double rangoIni = 0;
		public double rangoFin = 0;
	}

	public class clsBeP_DESCUENTO {
		public String cliente = "";
		public String ctipo = "";
		public String producto = "";
		public String ptipo = "";
		public String tiporuta = "";
		public double rangoIni = 0;
		public double rangoFin = 0;
		public String descTipo = "";
		public double valor = 0;
		public String globDesc = "";
		public double porcAnt = 0;
		public String fechaIni = "";
		public String fechaFin = "";
		public int codDesc = 0;
		public String nombre = "";
		public boolean esRecargo = false;
		public String porPorcentaje = "";
		public int prioridad = 0;
		public int prioridadDescuento = 0;
		public String umVenta = "";
		public String sucursal = "";
		public String tipologia = "";
	}

	public class clsBeP_DESCUENTO_COMBO_DET {
		public int codDesc = 0;
		public int secuencia = 0;
		public String producto = "";
		public double cantidad = 0;
		public String grupo = "";
		public String umStock = "";
		public String umVenta = "";
		public boolean obligatorio = true;
		public String empresa = "";
		public String tipoParticipacion = "";
	}

	public class VentaLinea {
		public String lineKey;
		public String producto;
		public String empresa;
		public String um;
		public double sinExistencia;
		public double cant;
		public String umStock;
		public String umPeso;
		public double factor;
		public double precio;
		public double precioBase;
		public double totalBase;
		public int codDescAplicado;
		public int codRecargoAplicado;
		public double imp;
		public double des;
		public double desMon;
		public double total;
		public double precioDoc;
		public double peso;
		public double val1;
		public double val2;
		public double val3;
		public double val4;
		public double percep;
		public double cantOriginal;
		public double pesoOriginal;
		public double recargo;
		public double recargoMonto;
		public boolean promoEligible = true;
		//#EJC20260724 state(hh-combo-individual-fallback): conserva el resultado individual
		//para restaurarlo si un combo queda incompleto o resulta ambiguo.
		public boolean individualSnapshot;
		public double individualPrecio;
		public double individualTotal;
		public double individualDes;
		public double individualDesMon;
		public double individualRecargo;
		public double individualRecargoMonto;
		public int individualCodDesc;
		public int individualCodRecargo;
	}
}
