package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class BaseDatosScript {

	private Context vcontext;

	public BaseDatosScript(Context context) {
		vcontext=context;
	}

	public int scriptDatabase(SQLiteDatabase database) {
		String vSQL;

		try {

			vSQL="CREATE TABLE [Params] ("+
					"ID integer NOT NULL primary key,"+
					"EmpID     INTEGER  NOT NULL,"+
					"dbver     INTEGER  NOT NULL,"+
					"param1    INTEGER  NOT NULL,"+
					"param2    INTEGER  NOT NULL,"+
					"prn       TEXT     NOT NULL,"+
					"prnparam  TEXT     NOT NULL,"+
					"prnserie  TEXT     NOT NULL,"+
					"lic       TEXT     NOT NULL,"+
					"licparam  TEXT     NOT NULL);";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [FinDia] ("+
					"ID integer NOT NULL primary key,"+
					"Corel INTEGER  NOT NULL,"+
					"val1  INTEGER  NOT NULL,"+  // Día del cierre
					"val2  INTEGER  NOT NULL,"+	 // Comunicacion
					"val3  INTEGER  NOT NULL,"+	 // Impresión Depósito
					"val4  INTEGER  NOT NULL,"+	 // Depósito
					"val5  INTEGER  NOT NULL,"+	 // Devolución bodega y canastas
					"val6  INTEGER  NOT NULL,"+  // Generación Cierre Z
					"val7  INTEGER  NOT NULL,"+  // Impresión de Cierre Z
					"val8  REAL  NOT NULL);";    // GrandTotal
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [LIC_CLIENTE] ("+
					"ID      TEXT NOT NULL primary key,"+
					"LTIPO   TEXT NOT NULL,"+
					"IDKEY   TEXT NOT NULL,"+
					"NOMBRE  TEXT NOT NULL,"+
					"BINKEY  TEXT NOT NULL);";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_PARAMEXT] ("+
					"ID integer NOT NULL primary key,"+
					"Nombre TEXT NOT NULL,"+
					"Valor  TEXT);";
			database.execSQL(vSQL);


			if (scriptTablasD(database)==0) {return 0;}

			if (scriptTablasO(database)==0) {return 0;}

			if (scriptTablasP(database)==0) {return 0;}

			if (scriptTablasT(database)==0) {return 0;}

			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}

	}

	private int scriptTablasD(SQLiteDatabase database) {
		String vSQL;

		try {

			vSQL="CREATE TABLE [D_PEDIDO] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[KILOMETRAJE] REAL NOT NULL,"+
					"[FECHAENTR] INTEGER NOT NULL,"+
					"[DIRENTREGA] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[DESMONTO] REAL NOT NULL,"+
					"[IMPMONTO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[BANDERA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[CALCOBJ] TEXT NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[ADD1] TEXT NOT NULL,"+
					"[ADD2] TEXT NOT NULL,"+
					"[ADD3] TEXT NOT NULL,"+
					"[STATPROC] TEXT NOT NULL,"+
					"[RECHAZADO] INTEGER NOT NULL,"+
					"[RAZON_RECHAZADO] TEXT NOT NULL,"+
					"[INFORMADO] INTEGER NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"[ID_DESPACHO] INTEGER NOT NULL,"+
					"[ID_FACTURACION] INTEGER NOT NULL,"+
					"[RUTASUPER] TEXT NOT NULL,"+
                    "[FECHA_SISTEMA] INTEGER NOT NULL,"+
					"[ANULADO_POR_MONTO_MINIMO] INTEGER NOT NULL,"+
					"[CUMPLE_MONTO_MINIMO] INTEGER NOT NULL,"+
					"[TIPO_PEDIDO] TEXT NOT NULL,"+
					"[TOTAL_MONTO_MINIMO] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_PEDIDO_idx1 ON D_PEDIDO(COREL)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_PEDIDO_idx2 ON D_PEDIDO(ANULADO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_PEDIDO_idx3 ON D_PEDIDO(FECHA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_PEDIDO_idx4 ON D_PEDIDO(STATCOM)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_PEDIDO_idx5 ON D_PEDIDO(CALCOBJ)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_PEDIDOD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[IMP] REAL NOT NULL,"+
					"[DES] REAL NOT NULL,"+
					"[DESMON] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIODOC] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[VAL1] REAL NOT NULL,"+
					"[VAL2] TEXT NOT NULL,"+
					"[CANTPROC] REAL NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[SIN_EXISTENCIA] INTEGER NOT NULL,"+  //JP20210614
					"PRIMARY KEY ([COREL],[PRODUCTO],[SIN_EXISTENCIA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_PEDIDOD_idx1 ON D_PEDIDOD(COREL)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_PEDIDOD_idx2 ON D_PEDIDOD(ANULADO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_CxC] ("+
					"[COREL] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[SUPERVISOR] TEXT NOT NULL,"+
					"[AYUDANTE] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[ESTADO] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_CxC_idx1 ON D_CxC(ANULADO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_CxCD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CODDEV] TEXT NOT NULL,"+
					"[ESTADO] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PRECLISTA] REAL NOT NULL,"+
					"[REF] TEXT NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[FECHA_CAD] INTEGER NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"PRIMARY KEY ([COREL],[ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_CxCD_idx1 ON D_CxCD(COREL)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_DEVOL] ("+
					"[COREL] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_DEVOL_idx1 ON D_DEVOL(ANULADO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_DEVOLD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CODDEV] TEXT NOT NULL,"+
					"[ESTADO] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PRECLISTA] REAL NOT NULL,"+
					"[REF] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[ITEM])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MERPROPIO] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[PRODUCTO],[FECHA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MERCOMP] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[DESCUENTO] REAL NOT NULL,"+
					"[DIAVISITA] INTEGER NOT NULL,"+
					"[FRECUENCIA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[PRODUCTO],[FECHA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MEREQUIPO] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[SERIAL] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[TIPO] INTEGER NOT NULL,"+
					"[ESTADO] INTEGER NOT NULL,"+
					"[CODBARRA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[SERIAL],[FECHA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MERFALTA] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[PRODUCTO],[FECHA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MERPREGUNTA] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[CODIGO] INTEGER NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[RESP] INTEGER NOT NULL,"+
					"[STATCOM] INTEGER NOT NULL,"+
					"[FOTO] TEXT NOT NULL,"+
					"[GRABACION] TEXT NOT NULL,"+
					"[STATCOMG] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[CODIGO],[FECHA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_COBRO] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[KILOMETRAJE] REAL NOT NULL,"+
					"[CORELC] TEXT NOT NULL,"+
					"[BANDERA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[DEPOS] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[CALCOBJ] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[CORELATIVO] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_COBRO_idx1 ON D_COBRO(ANULADO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_COBRO_idx2 ON D_COBRO(CLIENTE)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_COBRO_idx3 ON D_COBRO(STATCOM)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_COBRO_idx4 ON D_COBRO(CALCOBJ)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_COBROD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[TIPODOC] TEXT NOT NULL,"+
					"[MONTO] REAL NOT NULL,"+
					"[PAGO] REAL NOT NULL,"+
					"[CONTRASENA] TEXT NOT NULL,"+
					"[ID_TRANSACCION] INTEGER NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[ASIGNACION] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[DOCUMENTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_COBROD_idx1 ON D_COBROD(ANULADO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_COBROD_SR] ("+
					"[COREL] TEXT NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[TIPODOC] TEXT NOT NULL,"+
					"[MONTO] REAL NOT NULL,"+
					"[PAGO] REAL NOT NULL,"+
					"[CONTRASENA] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[DOCUMENTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_COBROP] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[CODPAGO] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[DESC1] TEXT NOT NULL,"+
					"[DESC2] TEXT NOT NULL,"+
					"[DESC3] TEXT NOT NULL,"+
					"[DEPOS] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_COBROP_idx1 ON D_COBROP(ANULADO)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CLINUEVO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[NEGOCIO] TEXT NOT NULL,"+
					"[DIRECCION] TEXT NOT NULL,"+
					"[TELEFONO] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[TIPONEG] TEXT NOT NULL,"+
					"[NIVPRECIO] INTEGER NOT NULL,"+
					"[DIA1] TEXT NOT NULL,"+
					"[DIA2] TEXT NOT NULL,"+
					"[DIA3] TEXT NOT NULL,"+
					"[DIA4] TEXT NOT NULL,"+
					"[DIA5] TEXT NOT NULL,"+
					"[DIA6] TEXT NOT NULL,"+
					"[DIA7] TEXT NOT NULL,"+
					"[ORDVIS] INTEGER NOT NULL,"+
					"[BAND1] TEXT NOT NULL,"+
					"[BAND2] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
                    "[IMAGEN] TEXT NOT NULL,"+
                    "[CODIGO_ERP] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[RUTA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CLINUEVOT] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[NEGOCIO] TEXT NOT NULL,"+
					"[DIRECCION] TEXT NOT NULL,"+
					"[TELEFONO] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[TIPONEG] TEXT NOT NULL,"+
					"[NIVPRECIO] INTEGER NOT NULL,"+
					"[DIA1] TEXT NOT NULL,"+
					"[DIA2] TEXT NOT NULL,"+
					"[DIA3] TEXT NOT NULL,"+
					"[DIA4] TEXT NOT NULL,"+
					"[DIA5] TEXT NOT NULL,"+
					"[DIA6] TEXT NOT NULL,"+
					"[DIA7] TEXT NOT NULL,"+
					"[ORDVIS] INTEGER NOT NULL,"+
					"[BAND1] TEXT NOT NULL,"+
					"[BAND2] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[IMAGEN] TEXT NOT NULL,"+
					"[CODIGO_ERP] TEXT NOT NULL,"+
					"[COORX] REAL NOT NULL,"+
					"[COORY] REAL NOT NULL,"+
					"[DEPARTAMENTO] TEXT NOT NULL,"+
					"[MUNICIPIO] TEXT NOT NULL,"+
					"[CIUDAD] TEXT NOT NULL,"+
					"[EMAIL] TEXT NOT NULL,"+
					"[CONTACTO] TEXT NOT NULL,"+
					"[CANAL] TEXT NOT NULL,"+
					"[SUBCANAL] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CSPOLLO] REAL NOT NULL,"+
					"[CSEMBUTIDO] REAL NOT NULL,"+
					"[CSHUEVO] REAL NOT NULL,"+
					"[CSRES] REAL NOT NULL,"+
					"[CSCERDO] REAL NOT NULL,"+
					"[CSCONGELADOS] REAL NOT NULL,"+
					"[CSSALSAS] REAL NOT NULL,"+
					"[TIPOLOGIA] TEXT NOT NULL,"+
					"[RUC_VALIDADO] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[RUTA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_CLINUEVO_idx1 ON D_CLINUEVO(STATCOM)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CLINUEVOT_IMAGEN] ("+
					"[CODIMAGEN] INTEGER NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[OBSERVACIONES] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIMAGEN])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_CLINUEVOT_IMAGEN_idx1 ON D_CLINUEVOT_IMAGEN(STATCOM)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [TMP_D_CLINUEVOT_IMAGEN] ("+
					"[CODIMAGEN] INTEGER NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIMAGEN])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CLINUEVO_APR] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHANAC] INTEGER NOT NULL,"+
					"[CUI] TEXT NOT NULL,"+
					"[DepID] TEXT NOT NULL,"+
					"[MuniID] TEXT NOT NULL,"+
					"[Religion] TEXT NOT NULL,"+
					"[Etnico] TEXT NOT NULL,"+
					"[Escolaridad] TEXT NOT NULL,"+
					"[Estado] TEXT NOT NULL,"+
					"[Genero] TEXT NOT NULL,"+
					"[Hijos] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[RUTA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_DEPOS] ("+
					"[COREL] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[BANCO] TEXT NOT NULL,"+
					"[CUENTA] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[TOTEFEC] REAL NOT NULL,"+
					"[TOTCHEQ] REAL NOT NULL,"+
					"[NUMCHEQ] INTEGER NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_DEPOS_idx1 ON D_DEPOS(STATCOM)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_DEPOS_idx2 ON D_DEPOS(ANULADO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_DEPOSD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[DOCCOREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[TIPODOC] TEXT NOT NULL,"+
					"[CODPAGO] INTEGER NOT NULL,"+
					"[CHEQUE] TEXT NOT NULL,"+
					"[MONTO] REAL NOT NULL,"+
					"[BANCO] TEXT NOT NULL,"+
					"[NUMERO] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[DOCCOREL],[ITEM])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_DEPOSB] ("+
					"[COREL] TEXT NOT NULL,"+
					"[DENOMINACION] REAL NOT NULL,"+
					"[CANTIDAD] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[MONEDA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL],[DENOMINACION],[TIPO],[MONEDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_FACT_LOG] ("+
					"[ITEM] INTEGER NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[COREL] INTEGER NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"PRIMARY KEY ([ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_NOTACRED_LOG] ("+
					"[ITEM] INTEGER NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[COREL] INTEGER NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"PRIMARY KEY ([ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_FACTURA] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[KILOMETRAJE] REAL NOT NULL,"+
					"[FECHAENTR] INTEGER NOT NULL,"+
					"[FACTLINK] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[DESMONTO] REAL NOT NULL,"+
					"[IMPMONTO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[BANDERA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[CALCOBJ] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[CORELATIVO] INTEGER NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[ADD1] TEXT NOT NULL,"+
					"[ADD2] TEXT NOT NULL,"+
					"[ADD3] TEXT NOT NULL,"+
					"[DEPOS] TEXT NOT NULL,"+
					"[PEDCOREL] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[ASIGNACION] TEXT NOT NULL,"+
					"[SUPERVISOR] TEXT NOT NULL,"+
					"[AYUDANTE] TEXT NOT NULL,"+
					"[VEHICULO] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[RAZON_ANULACION] TEXT NOT NULL,"+
					"[CODIGO_RUTA_PEDIDO] TEXT,"+
					"[DESPCOREL] TEXT,"+
					"[CERTIFICADA_DGI] INTEGER NOT NULL,"+
					"[CUFE] TEXT,"+
					"[ORDEN_COMPRA] TEXT,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_FACTURA_idx1 ON D_FACTURA(ANULADO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_FACTURA_idx2 ON D_FACTURA(FECHA)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_FACTURAD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[IMP] REAL NOT NULL,"+
					"[DES] REAL NOT NULL,"+
					"[DESMON] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIODOC] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[VAL1] REAL NOT NULL,"+
					"[VAL2] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_FACTURAP] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[CODPAGO] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[DESC1] TEXT NOT NULL,"+
					"[DESC2] TEXT NOT NULL,"+
					"[DESC3] TEXT NOT NULL,"+
					"[DEPOS] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[ITEM])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_FACTURAF] ("+
					"[COREL] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[DIRECCION] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_FACTURAD_LOTES] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[CANTIDAD] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[LOTE])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_FACTURA_STOCK] ("+
					"[COREL] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[plibra] REAL NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[CENTRO] TEXT NOT NULL,"+
					"[STATUS] TEXT NOT NULL,"+
					"[ENVIADO] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[COREL_D_MOV] TEXT NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([COREL],[CODIGO],[LOTE],[DOCUMENTO],[STATUS],[UNIDADMEDIDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_FACTURA_STOCK_idx1 ON D_FACTURA_STOCK(COREL)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_FACTURA_BARRA] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[BARRA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[COREL] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[CENTRO] TEXT NOT NULL,"+
					"[STATUS] TEXT NOT NULL,"+
					"[ENVIADO] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[COREL_D_MOV] TEXT NOT NULL,"+
					"[UNIDADMEDIDA] TEXT NOT NULL,"+
					"[DOC_ENTREGA] TEXT NOT NULL,"+
					"[RESERVADO] INTEGER DEFAULT 0 NOT NULL,"+
					"PRIMARY KEY ([RUTA],[BARRA],[DOCUMENTO],[STATUS])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_FACTURA_BARRA_idx1 ON D_FACTURA_BARRA(COREL)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_FACTURAD_MODIF]("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANTSOLICITADA] REAL,"+
					"[UMVENTASOLICITADA] TEXT,"+
					"[PESOSOLICITADO] REAL,"+
					"[CANTENTREGADA] REAL,"+
					"[UMVENTAENTREGADA] TEXT,"+
					"[PESOENTREGADO] REAL,"+
					"[IDRAZON] TEXT,"+
					"[PEDCOREL] TEXT,"+
					"[STATCOM] TEXT,"+
					"[DESPCOREL] TEXT,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_FACTURAD_MODIF_IDX1 ON D_FACTURAD_MODIF(PRODUCTO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_FACTURAD_MODIF_IDX2 ON D_FACTURAD_MODIF(ANULADO)";
			database.execSQL(vSQL);

			vSQL = "CREATE TABLE  [D_FACTURA_CONTROL_CONTINGENCIA](" +
					"[IdTablaControl] INTEGER NOT NULL," +
					"[Cufe] TEXT," +
					"[TipoDocumento] TEXT," +
					"[NumeroDocumento] TEXT," +
					"[Sucursal] TEXT," +
					"[Caja] TEXT," +
					"[Estado] TEXT," +
					"[Mensaje] TEXT," +
					"[Valor_XML] TEXT," +
					"[FechaEnvio] TEXT," +
					"[TipoFactura] TEXT," +
					"[Fecha_Agr] TEXT," +
					"[QR] TEXT," +
					"[COREL] TEXT," +
					"[RUTA] TEXT," +
					"[VENDEDOR] TEXT," +
					"[HOST] TEXT," +
					"[CODIGOLIQUIDACION] INTEGER," +
					"[CORELATIVO] TEXT," +
					"[QRIMAGE] TEXT," +
					"[FECHA_AUTORIZACION] TEXT," +
					"[NUMERO_AUTORIZACION] TEXT," +
					" PRIMARY KEY ([IdTablaControl])" +
					" );";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_MOV] ("+
					"[COREL] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[USUARIO] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_MOV_idx1 ON D_MOV(ANULADO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_MOV_idx2 ON D_MOV(TIPO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX D_MOV_idx3 ON D_MOV(STATCOM)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_MOVD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[PESOM] REAL NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[LOTE],[UNIDADMEDIDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_MOVDB] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[BARRA] TEXT NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[BARRA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_MOVDPALLET] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[BARRAPALLET] TEXT NOT NULL,"+
					"[BARRAPRODUCTO] TEXT NOT NULL,"+
					"[LOTEPRODUCTO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[BARRAPALLET],[BARRAPRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_MOVDCAN] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[PESOM] REAL NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[BARRA] TEXT NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[PASEANTE] INTEGER NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_ATENCION] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[HORALLEG] TEXT NOT NULL,"+
					"[HORASAL] TEXT NOT NULL,"+
					"[TIEMPO] INTEGER NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[DIAACT] INTEGER NOT NULL,"+
					"[DIA] INTEGER NOT NULL,"+
					"[DIAFLAG] TEXT NOT NULL,"+
					"[SECUENCIA] INTEGER NOT NULL,"+
					"[SECUENACT] INTEGER NOT NULL,"+
					"[CODATEN] TEXT NOT NULL,"+
					"[KILOMET] REAL NOT NULL,"+
					"[VALORVENTA] REAL NOT NULL,"+
					"[VALORNEXT] REAL NOT NULL,"+
					"[CLIPORDIA] INTEGER NOT NULL,"+
					"[CODOPER] TEXT NOT NULL,"+
					"[COREL] TEXT NOT NULL,"+
					"[SCANNED] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[LLEGO_COMPETENCIA_ANTES] INTEGER NOT NULL,"+
					"[CoorX] REAL NOT NULL,"+
					"[CoorY] REAL NOT NULL,"+
					"[CliCoorX] REAL NOT NULL,"+
					"[CliCoorY] REAL NOT NULL,"+
					"[Dist] REAL NOT NULL,"+
					"PRIMARY KEY ([RUTA],[FECHA],[HORALLEG])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_BONIF] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ITEM] INTEGER NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[VENPED] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[COSTO] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"PRIMARY KEY ([COREL],[ITEM])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_BONIF_BARRA] ("+
					"[COREL] TEXT NOT NULL," +
					"[BARRA] TEXT NOT NULL," +
					"[PESO] REAL NOT NULL," +
					"[PRODUCTO] TEXT NOT NULL," +
					"[UMVENTA] TEXT NOT NULL," +
					"[UMSTOCK] TEXT NOT NULL," +
					"[UMPESO] TEXT NOT NULL," +
					"[FACTOR] REAL NOT NULL," +
					"PRIMARY KEY ([COREL],[BARRA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_BONIF_LOTES] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[LOTE])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_BONIF_STOCK] ("+
					"[COREL] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[plibra] REAL NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[CENTRO] TEXT NOT NULL,"+
					"[STATUS] TEXT NOT NULL,"+
					"[ENVIADO] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[COREL_D_MOV] TEXT NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([COREL],[CODIGO],[LOTE],[DOCUMENTO],[STATUS],[UNIDADMEDIDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_BONIF_STOCK_idx1 ON D_BONIF_STOCK(COREL)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_BONIFFALT] ("+
					"[COREL] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_REL_PROD_BON] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[BONIFICADO] TEXT NOT NULL,"+
					"[CANTIDAD] REAL NOT NULL,"+
					"[CONSECUTIVO] INTEGER NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO],[BONIFICADO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_CLICOORD] ("+
					"[CODIGO]  TEXT NOT NULL,"+
					"[STAMP]   REAL NOT NULL,"+
					"[COORX]   REAL NOT NULL,"+
					"[COORY]   REAL NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[STAMP])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_REPFINDIA] ("+
					"[RUTA]  TEXT NOT NULL,"+
					"[LINEA] INTEGER NOT NULL,"+
					"[TEXTO] TEXT NOT NULL,"+
					"PRIMARY KEY ([RUTA],[LINEA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_SOLICINV] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[USUARIO] TEXT NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_SOLICINV_idx1 ON D_SOLICINV(STATCOM)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_SOLICINVD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[COSTO] REAL NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[UM] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_NOTACRED] ("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[TOTAL]  REAL NOT NULL,"+
					"[FACTURA] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[CORELATIVO] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[RESOLNC] TEXT NOT NULL,"+
					"[SERIEFACT] TEXT NOT NULL,"+
					"[CORELFACT] INTEGER NOT NULL,"+
					"[IMPRES] INTEGER NOT NULL,"+
					"[CERTIFICADA_DGI] INTEGER NOT NULL,"+
					"[CUFE] TEXT,"+
					"[TIPO_DOCUMENTO] TEXT NOT NULL,"+
					"[COREL_REFERENCIA] TEXT,"+
					"[ES_ANULACION] INTEGER,"+
					"[CUFE_FACTURA] TEXT,"+
					"PRIMARY KEY ([COREL], [TIPO_DOCUMENTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_NOTACREDD] ("+
					"[COREL] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PRECIO_ORIG] REAL NOT NULL,"+
					"[PRECIO_ACT] REAL NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[POR_PESO] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[TIPO_DOCUMENTO] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO], [TIPO_DOCUMENTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX D_NOTACREDD_idx1 ON D_NOTACREDD(COREL)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [D_STOCKB_DEV] ("+
					"[BARRA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[COREL] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"PRIMARY KEY ([BARRA],[RUTA],[VENDEDOR],[CODIGO],[COREL])"+
					");";
			database.execSQL(vSQL);

			vSQL= "CREATE TABLE [D_RATING]("+
					"[IDRATING] INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[RATING] REAL NOT NULL,"+
					"[COMENTARIO] TEXT NOT NULL,"+
					"[IDTRANSERROR] INTEGER NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[STATCOM] TEXT NOT NULL"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CANASTA] ("+
					"IDCANASTA INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"RUTA TEXT NOT NULL,"+
					"FECHA INTEGER NOT NULL,"+
					"CLIENTE TEXT NOT NULL,"+
					"PRODUCTO TEXT NOT NULL,"+
					"CANTREC INTEGER,"+
					"CANTENTR INTEGER,"+
					"STATCOM TEXT DEFAULT 'N',"+
					"CORELTRANS TEXT,"+
					"PESOREC REAL DEFAULT 0,"+
					"PESOENTR REAL DEFAULT 0,"+
					"ANULADO INTEGER DEFAULT 0,"+
					"UNIDBAS TEXT DEFAULT 'UN'," +
					"CODIGOLIQUIDACION INTEGER DEFAULT 0,"+
					"VENDEDOR TEXT "+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_DESPACHOD_NO_ENTREGADO](" +
					"[COREL] TEXT NOT NULL," +
					"[ANULADO] INTEGER NOT NULL," +
					"[PRODUCTO] TEXT NOT NULL," +
					"[CANTSOLICITADA] REAL," +
					"[UMVENTASOLICITADA] TEXT," +
					"[PESOSOLICITADO] REAL," +
					"[CANTENTREGADA] REAL," +
					"[UMVENTAENTREGADA] TEXT," +
					"[PESOENTREGADO] REAL," +
					"[IDRAZON] TEXT NOT NULL," +
					"[STATCOM] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [D_CLIENTE_MODIF] ("+
					"[COREL] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[BLOQUEADO] TEXT NOT NULL,"+
					"[TIPONEG] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[SUBTIPO] TEXT NOT NULL,"+
					"[CANAL] TEXT NOT NULL,"+
					"[SUBCANAL] TEXT NOT NULL,"+
					"[NIVELPRECIO] INTEGER NOT NULL,"+
					"[MEDIAPAGO] TEXT NOT NULL,"+
					"[LIMITECREDITO] REAL NOT NULL,"+
					"[DIACREDITO] INTEGER NOT NULL,"+
					"[DESCUENTO] TEXT NOT NULL,"+
					"[BONIFICACION] TEXT NOT NULL,"+
					"[IMPSPEC] REAL NOT NULL,"+
					"[INVTIPO] TEXT NOT NULL,"+
					"[INVEQUIPO] TEXT NOT NULL,"+
					"[INV1] TEXT NOT NULL,"+
					"[INV2] TEXT NOT NULL,"+
					"[INV3] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[MENSAJE] TEXT NOT NULL,"+
					"[TELEFONO] TEXT NOT NULL,"+
					"[DIRTIPO] TEXT NOT NULL,"+
					"[DIRECCION] TEXT NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"[COORX] REAL NOT NULL,"+
					"[COORY] REAL NOT NULL,"+
					"[FIRMADIG] TEXT NOT NULL,"+
					"[CODBARRA] TEXT NOT NULL,"+
					"[VALIDACREDITO] TEXT NOT NULL,"+
					"[PRECIO_ESTRATEGICO] TEXT NOT NULL,"+
					"[NOMBRE_PROPIETARIO] TEXT NOT NULL,"+
					"[NOMBRE_REPRESENTANTE] TEXT NOT NULL,"+
					"[BODEGA] TEXT NOT NULL,"+
					"[COD_PAIS] TEXT NOT NULL,"+
					"[FACT_VS_FACT] TEXT NOT NULL,"+
					"[CHEQUEPOST] TEXT NOT NULL,"+
					"[PERCEPCION] REAL NOT NULL,"+
					"[TIPO_CONTRIBUYENTE] TEXT NOT NULL,"+
					"[ID_DESPACHO] INTEGER NOT NULL,"+
					"[ID_FACTURACION] INTEGER NOT NULL,"+
					"[MODIF_PRECIO] INTEGER NOT NULL,"+
					"[INGRESA_CANASTAS] INTEGER DEFAULT 0 NOT NULL,"+
					"[PRIORIZACION] TEXT NOT NULL,"+
					"[STATCOM] TEXT NOT NULL,"+
					"[FECHA_SISTEMA] TEXT NOT NULL,"+
					"[CONTACTO] TEXT NOT NULL,"+
					"[MUNICIPIO] TEXT NOT NULL,"+
					"[EMAIL] TEXT NOT NULL,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private int scriptTablasO(SQLiteDatabase database) {
		String vSQL;

		try {

			vSQL="CREATE TABLE [O_RUTA] ("+
					"[OBJANO] INTEGER NOT NULL,"+
					"[OBJMES] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[METAV] REAL NOT NULL,"+
					"[METAU] REAL NOT NULL,"+
					"[ACUMV] REAL NOT NULL,"+
					"[ACUMU] REAL NOT NULL,"+
					"PRIMARY KEY ([OBJANO],[OBJMES],[RUTA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX O_RUTA_idx1 ON O_RUTA(OBJANO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_RUTA_idx2 ON O_RUTA(OBJMES)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_RUTA_idx3 ON O_RUTA(RUTA)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [O_COBRO] ("+
					"[OBJANO] INTEGER NOT NULL,"+
					"[OBJMES] INTEGER NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[METAV] REAL NOT NULL,"+
					"[METAU] REAL NOT NULL,"+
					"[ACUMV] REAL NOT NULL,"+
					"[ACUMU] REAL NOT NULL,"+
					"PRIMARY KEY ([OBJANO],[OBJMES],[RUTA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX O_COBRO_idx1 ON O_COBRO(OBJANO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_COBRO_idx2 ON O_COBRO(OBJMES)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_COBRO_idx3 ON O_COBRO(RUTA)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [O_PROD] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[METAV] REAL NOT NULL,"+
					"[METAU] REAL NOT NULL,"+
					"[ACUMV] REAL NOT NULL,"+
					"[ACUMU] REAL NOT NULL,"+
					"PRIMARY KEY ([RUTA],[CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX O_PROD_idx1 ON O_PROD(RUTA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_PROD_idx2 ON O_PROD(CODIGO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [O_LINEA] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[METAV] REAL NOT NULL,"+
					"[METAU] REAL NOT NULL,"+
					"[ACUMV] REAL NOT NULL,"+
					"[ACUMU] REAL NOT NULL,"+
					"PRIMARY KEY ([RUTA],[CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX O_LINEA_idx1 ON O_LINEA(RUTA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX O_LINEA_idx2 ON O_LINEA(CODIGO)";
			database.execSQL(vSQL);


			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private int scriptTablasP(SQLiteDatabase database) {
		String vSQL;

		try {

			vSQL="CREATE TABLE [P_BANCO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[CUENTA] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[TIPO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_BANCO_idx1 ON P_BANCO(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_CLIENTE] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[BLOQUEADO] TEXT NOT NULL,"+
					"[TIPONEG] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[SUBTIPO] TEXT NOT NULL,"+
					"[CANAL] TEXT NOT NULL,"+
					"[SUBCANAL] TEXT NOT NULL,"+
					"[NIVELPRECIO] INTEGER NOT NULL,"+
					"[MEDIAPAGO] TEXT NOT NULL,"+
					"[LIMITECREDITO] REAL NOT NULL,"+
					"[DIACREDITO] INTEGER NOT NULL,"+
					"[DESCUENTO] TEXT NOT NULL,"+
					"[BONIFICACION] TEXT NOT NULL,"+
					"[ULTVISITA] INTEGER NOT NULL,"+
					"[IMPSPEC] REAL NOT NULL,"+
					"[INVTIPO] TEXT NOT NULL,"+
					"[INVEQUIPO] TEXT NOT NULL,"+
					"[INV1] TEXT NOT NULL,"+
					"[INV2] TEXT NOT NULL,"+
					"[INV3] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[MENSAJE] TEXT NOT NULL,"+
					"[EMAIL] TEXT NOT NULL,"+
					"[TELEFONO] TEXT NOT NULL,"+
					"[DIRTIPO] TEXT NOT NULL,"+
					"[DIRECCION] TEXT NOT NULL,"+
                    "[MUNICIPIO] TEXT NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"[COORX] REAL NOT NULL,"+
					"[COORY] REAL NOT NULL,"+
					"[FIRMADIG] TEXT NOT NULL,"+
					"[CODBARRA] TEXT NOT NULL,"+
					"[VALIDACREDITO] TEXT NOT NULL,"+
					"[PRECIO_ESTRATEGICO] TEXT NOT NULL,"+
					"[NOMBRE_PROPIETARIO] TEXT NOT NULL,"+
					"[NOMBRE_REPRESENTANTE] TEXT NOT NULL,"+
					"[BODEGA] TEXT NOT NULL,"+
					"[COD_PAIS] TEXT NOT NULL,"+
					"[FACT_VS_FACT] TEXT NOT NULL,"+
					"[CHEQUEPOST] TEXT NOT NULL,"+
					"[PERCEPCION] REAL NOT NULL,"+
					"[TIPO_CONTRIBUYENTE] TEXT NOT NULL,"+
					"[ID_DESPACHO] INTEGER NOT NULL,"+
					"[ID_FACTURACION] INTEGER NOT NULL,"+
					"[MODIF_PRECIO] INTEGER NOT NULL,"+
					"[INGRESA_CANASTAS] INTEGER DEFAULT 0 NOT NULL,"+
                    "[PRIORIZACION] TEXT NOT NULL,"+
					"[CONTACTO] TEXT,"+
					"[GEOREFERENCIA_CANASTA] INTEGER DEFAULT 0 NOT NULL,"+
					"[GEOREFERENCIA_PREFACTURA] INTEGER DEFAULT 0 NOT NULL,"+
					"[GEOREFERENCIA_PREVENTA] INTEGER DEFAULT 0 NOT NULL,"+
					"[GEOREFERENCIA_AUTOVENTA] INTEGER DEFAULT 0 NOT NULL,"+
					"[TIPORECEPTOR] TEXT,"+
					"[CIUDAD] TEXT,"+
					"[DESCRIPCION_PAGO] TEXT,"+
					"[PERMITIR_PEDIDO_EXTRA_RUTA] INTEGER DEFAULT 0 NOT NULL,"+
					"[TIPOLOGIA] TEXT ,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CLIENTE_idx1 ON P_CLIENTE(NOMBRE)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CLIENTE_FACHADA] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[IMAGEN]  TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CLIDIR] ("+
					"[CODIGO_CLIENTE] TEXT NOT NULL,"+
					"[DIRECCION_ENTREGA] TEXT NOT NULL"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CLIDIR_idx1 ON P_CLIDIR(CODIGO_CLIENTE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_CLIRUTA] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[SEMANA] INTEGER NOT NULL,"+
					"[DIA] INTEGER NOT NULL,"+
					"[SECUENCIA] INTEGER NOT NULL,"+
					"[BANDERA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([RUTA],[CLIENTE],[SEMANA],[DIA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CLIRUTA_idx1 ON P_CLIRUTA(SEMANA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_CLIRUTA_idx2 ON P_CLIRUTA(DIA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_CLIRUTA_idx3 ON P_CLIRUTA(SECUENCIA)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_COBRO] ("+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"[TIPODOC] TEXT NOT NULL,"+
					"[VALORORIG] REAL NOT NULL,"+
					"[SALDO] REAL NOT NULL,"+
					"[CANCELADO] REAL NOT NULL,"+
					"[FECHAEMIT] INTEGER NOT NULL,"+
					"[FECHAV] INTEGER NOT NULL,"+
					"[CONTRASENA] TEXT NOT NULL,"+
					"[ID_TRANSACCION] INTEGER NOT NULL,"+
					"[REFERENCIA] TEXT NOT NULL,"+
					"[ASIGNACION] TEXT NOT NULL,"+
					"PRIMARY KEY ([DOCUMENTO],[EMPRESA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_COBRO_idx1 ON P_COBRO(CLIENTE)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_COBRO_idx2 ON P_COBRO(SALDO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_COBRO_idx3 ON P_COBRO(FECHAEMIT)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_COBRO_idx4 ON P_COBRO(FECHAV)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_IMPUESTO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[VALOR]  REAL NOT NULL,"+
					"PRIMARY  KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_NIVELPRECIO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[DECIMALES] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_LINEA] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[MARCA] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_LINEA_idx1 ON P_LINEA(NOMBRE)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_LIQUIDACION] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[ESTADO] TEXT NOT NULL,"+
					"PRIMARY KEY ([RUTA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_PRODPRECIO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NIVEL] INTEGER NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[NIVEL],[UNIDADMEDIDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_PRODPRECIO_idx1 ON P_PRODPRECIO(CODIGO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_PRODPRECIO_idx2 ON P_PRODPRECIO(NIVEL)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [TMP_PRECESPEC] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[VALOR] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[VALOR],[PRODUCTO],[UNIDADMEDIDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX TMP_PRECESPEC_idx1 ON TMP_PRECESPEC(PRODUCTO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_PRODUCTO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[LINEA] TEXT NOT NULL,"+
					"[SUBLINEA] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[MARCA] TEXT NOT NULL,"+
					"[CODBARRA] TEXT NOT NULL,"+
					"[DESCCORTA] TEXT NOT NULL,"+
					"[DESCLARGA] TEXT NOT NULL,"+
					"[COSTO] REAL NOT NULL,"+
					"[FACTORCONV] REAL NOT NULL,"+
					"[UNIDBAS] TEXT NOT NULL,"+
					"[UNIDMED] TEXT NOT NULL,"+
					"[UNIMEDFACT] REAL NOT NULL,"+
					"[UNIGRA] TEXT NOT NULL,"+
					"[UNIGRAFACT] REAL NOT NULL,"+
					"[DESCUENTO] TEXT NOT NULL,"+
					"[BONIFICACION] TEXT NOT NULL,"+
					"[IMP1] REAL NOT NULL,"+
					"[IMP2] REAL NOT NULL,"+
					"[IMP3] REAL NOT NULL,"+
					"[VENCOMP] TEXT NOT NULL,"+
					"[DEVOL] TEXT NOT NULL,"+
					"[OFRECER] TEXT NOT NULL,"+
					"[RENTAB] TEXT NOT NULL,"+
					"[DESCMAX] TEXT NOT NULL,"+
					"[PESO_PROMEDIO] REAL NOT NULL,"+
					"[MODIF_PRECIO] INTEGER NOT NULL,"+
					"[IMAGEN] TEXT NOT NULL,"+
                    "[VIDEO] TEXT NOT NULL,"+
                    "[VENTA_POR_PESO] INTEGER NOT NULL,"+
                    "[ES_PROD_BARRA] INTEGER NOT NULL,"+
                    "[UNID_INV] TEXT NOT NULL,"+
                    "[VENTA_POR_PAQUETE] INTEGER NOT NULL,"+
                    "[VENTA_POR_FACTOR_CONV] INTEGER NOT NULL,"+
                    "[ES_SERIALIZADO] INTEGER NOT NULL,"+
                    "[PARAM_CADUCIDAD] INTEGER NOT NULL,"+
                    "[PRODUCTO_PADRE] TEXT NOT NULL,"+
                    "[FACTOR_PADRE] REAL NOT NULL,"+
                    "[TIENE_INV] INTEGER NOT NULL,"+
                    "[TIENE_VINETA_O_TUBO] INTEGER NOT NULL,"+
                    "[PRECIO_VINETA_O_TUBO] REAL NOT NULL,"+
                    "[ES_VENDIBLE] INTEGER NOT NULL,"+
                    "[UNIGRASAP] REAL NOT NULL,"+
                    "[UM_SALIDA] TEXT NOT NULL," +
					"[ES_CANASTA] INTEGER NOT NULL,"+
					"[SUBBODEGA] TEXT,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_PRODUCTO_idx1 ON P_PRODUCTO(LINEA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_PRODUCTO_idx2 ON P_PRODUCTO(CODBARRA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_PRODUCTO_idx3 ON P_PRODUCTO(DESCCORTA)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_DESCUENTO] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[CTIPO] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PTIPO] INTEGER NOT NULL,"+
					"[TIPORUTA] INTEGER NOT NULL,"+
					"[RANGOINI] REAL NOT NULL,"+
					"[RANGOFIN] REAL NOT NULL,"+
					"[DESCTIPO] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[GLOBDESC] TEXT NOT NULL,"+
					"[PORCANT] TEXT NOT NULL,"+
					"[FECHAINI] INTEGER NOT NULL,"+
					"[FECHAFIN] INTEGER NOT NULL,"+
					"[CODDESC] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[CTIPO],[PRODUCTO],[PTIPO],[TIPORUTA],[RANGOINI])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_DESCUENTO_idx1 ON P_DESCUENTO(CLIENTE)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_DESCUENTO_idx2 ON P_DESCUENTO(CTIPO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_DESCUENTO_idx3 ON P_DESCUENTO(FECHAINI)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_DESCUENTO_idx4 ON P_DESCUENTO(FECHAFIN)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_EMPRESA] ("+
					"[EMPRESA] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[INITPATH] TEXT NOT NULL,"+
					"[FTPPATH] TEXT NOT NULL,"+
					"[NUMREIMPRES] INTEGER NOT NULL,"+
					"[MODDESC] TEXT NOT NULL,"+
					"[USARPESO] TEXT NOT NULL,"+
					"[DEVCONPREC] TEXT NOT NULL,"+
					"[ACUMDESC] TEXT NOT NULL,"+
					"[DESCMAX] REAL NOT NULL,"+
					"[BONVOLTOL] REAL NOT NULL,"+
					"[COD_PAIS] TEXT NOT NULL,"+
					"[BOLETA_DEPOSITO] INTEGER NOT NULL,"+
					"[EDITAR_DIRECCION] INTEGER NOT NULL,"+
					"[DEPOSITO_PARCIAL] INTEGER NOT NULL,"+
					"[COL_IMP] INTEGER NOT NULL,"+
					"[INV_ENLINEA] INTEGER NOT NULL,"+
					"[FIN_DIA] INTEGER NOT NULL,"+
					"[PRESENTACION_MULTIPLE] INTEGER NOT NULL,"+
					"[PRECIOS_ESPECIALES] INTEGER NOT NULL,"+
					"[AUTORIZ_MODIF_DESCBON] INTEGER NOT NULL,"+
					"[CAMBIO_POR_CAMBIO] INTEGER NOT NULL,"+
					"[DEVOLUCION_MERCANCIA] INTEGER NOT NULL,"+
					"[COBROS_SIN_REFERENCIA] INTEGER NOT NULL,"+
					"[PORCENTAJE_NC] REAL NOT NULL,"+
					"[PORC_MERMA] REAL NOT NULL,"+
					"[PRODUCTO_ERROR_SUMA] TEXT NOT NULL,"+
					"[UNIDAD_MEDIDA_PESO] TEXT NOT NULL,"+
					"[LOTE_POR_DEFECTO] TEXT NOT NULL,"+
					"[INCIDENCIA_NO_LECTURA] INTEGER NOT NULL," +
					"[IMPRIMIR_TOTALES_PEDIDO] INTEGER,"+
					"[URL_AUTENTICACION] TEXT,"+
					"[USUARIO_API] TEXT,"+
			        "[CLAVE_API] TEXT,"+
			        "[URL_ANULACION] TEXT,"+
					"[URL_CONSULTA_DOCUMENTOS_POR_CRITERIO] TEXT,"+
					"[URL_EMISION_FACTURA_B2C] TEXT,"+
					"[URL_EMISION_NC_B2C] TEXT,"+
					"[URL_EMISION_ND_B2C] TEXT,"+
					"[URL_BASE] TEXT,"+
					"[URL_TOKEN] TEXT,"+
					"[QR_API] TEXT,"+
					"[ARCHIVO_P12] TEXT,"+
					"[QR_CLAVE] TEXT,"+
					"[URL_B2C_HH] TEXT,"+
					"[URL_DOC] TEXT,"+
					"[URL_EMISION_NC_B2B_HH] TEXT,"+
					"[URL_EMISION_ND_B2B_HH] TEXT,"+
					"[UNIDAD_MEDIDA_DEFECTO] TEXT,"+
					"[AMBIENTE] TEXT,"+
					"[URL_CONSULTAR_DOCUMENTO_POR_RUTA] TEXT,"+
					"PRIMARY KEY ([EMPRESA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_SUCURSAL] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[DESCRIPCION] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[DIRECCION] INTEGER NOT NULL,"+
					"[TELEFONO] TEXT NOT NULL,"+
					"[NIT] TEXT NOT NULL,"+
					"[TEXTO] TEXT NOT NULL,"+
					"[TIPO_SUCURSAL] INTEGER,"+
			        "[CORREO] TEXT,"+
			        "[COORDENADA_X] REAL NOT NULL,"+
			        "[COORDENADA_Y] REAL NOT NULL,"+
			        "[CODUBI] TEXT NOT NULL,"+
			        "[TIPORUC] TEXT,"+
					"[CODMUNI] TEXT,"+
					"[SITIO_WEB] TEXT,"+
					"[MM_INCLUYE_CERRADOS] INTEGER,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_MEDIDA](" +
					"[CODIGO] TEXT NOT NULL," +
					"[DESCRIPCION] TEXT," +
					"[CODIGO_CGI] TEXT," +
					" PRIMARY KEY([CODIGO])" +
					" );";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_STOCK] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[plibra] REAL NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[CENTRO] TEXT NOT NULL,"+
					"[STATUS] TEXT NOT NULL,"+
					"[ENVIADO] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[COREL_D_MOV] TEXT NOT NULL,"+
					"[UNIDADMEDIDA] TEXT DEFAULT 'UN' NOT NULL,"+
					"[RESERVADO] INTEGER DEFAULT 0 NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[LOTE],[DOCUMENTO],[STATUS],[UNIDADMEDIDA],[RESERVADO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_STOCKB] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[BARRA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[COREL] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[CENTRO] TEXT NOT NULL,"+
					"[STATUS] TEXT NOT NULL,"+
					"[ENVIADO] INTEGER NOT NULL,"+
					"[CODIGOLIQUIDACION] INTEGER NOT NULL,"+
					"[COREL_D_MOV] TEXT NOT NULL,"+
					"[UNIDADMEDIDA] TEXT NOT NULL,"+
					"[DOC_ENTREGA] TEXT NOT NULL,"+
					"[RESERVADO] INTEGER DEFAULT 0 NOT NULL,"+
					"PRIMARY KEY ([RUTA],[BARRA],[DOCUMENTO],[STATUS])"+
					");";
			database.execSQL(vSQL);

			//#CKFK 20190304 10:48 Se agregó esta tabla para poder importar los pallets
			vSQL = "CREATE TABLE [P_STOCK_PALLET](" +
					"[DOCUMENTO] TEXT NOT NULL," +
					"[RUTA] TEXT NOT NULL," +
					"[BARRAPALLET] TEXT NOT NULL, " +
					"[CODIGO] TEXT NOT NULL, " +
					"[BARRAPRODUCTO] TEXT NOT NULL," +
					"[LOTEPRODUCTO] TEXT NOT NULL," +
					"[CANT] REAL NOT NULL," +
					"[COREL] TEXT NOT NULL," +
					"[PRECIO] REAL NOT NULL," +
					"[PESO] REAL NOT NULL," +
					"[FECHA] INTEGER NOT NULL," +
					"[ANULADO] INTEGER NOT NULL," +
					"[CENTRO] TEXT NOT NULL," +
					"[STATUS] TEXT NOT NULL," +
					"[ENVIADO] INTEGER NOT NULL," +
					"[CODIGOLIQUIDACION] INTEGER NOT NULL," +
					"[COREL_D_MOV] TEXT NOT NULL," +
					"[FECHA_SISTEMA] INTEGER NOT NULL," +
					"[UNIDADMEDIDA] TEXT NOT NULL," +
					"[DOC_ENTREGA] TEXT NOT NULL," +
					" PRIMARY KEY ([DOCUMENTO],[RUTA],[BARRAPALLET],[CODIGO]," +
					"[BARRAPRODUCTO],[LOTEPRODUCTO],[UNIDADMEDIDA]));";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_STOCK_APR] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_STOCKINV] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_STOCKINV_idx1 ON P_STOCKINV(CANT)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_CODATEN] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CODATEN_idx1 ON P_CODATEN(CODIGO)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CODNOLEC] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CODDEV] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[DESCRIPCION] TEXT NOT NULL,"+
					"[ESTADO] TEXT NOT NULL,"+
					"[PORCENTAJE] REAL NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[ESTADO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CODDEV_idx1 ON P_CODDEV(CODIGO)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_RAZON_DESP_INCOMP]("+
					"[IDRAZON] TEXT NOT NULL,"+
					"[DESCRIPCION] TEXT NOT NULL,"+
					"PRIMARY KEY ([IDRAZON])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_RAZON_DESP_INCOMP_IDX1 ON P_RAZON_DESP_INCOMP(IDRAZON)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_MERMARCACOMP] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MERMARCACOMP_idx1 ON P_MERMARCACOMP(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MERPRODCOMP] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[MARCA] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MERPRODCOMP_idx1 ON P_MERPRODCOMP(MARCA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_MERPRODCOMP_idx2 ON P_MERPRODCOMP(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MERPREGUNTA] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MERPREGUNTA_idx1 ON P_MERPREGUNTA(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MERRESP] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[PREGUNTA] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MERRESP_idx1 ON P_MERRESP(PREGUNTA)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_MERRESP_idx2 ON P_MERRESP(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MEREQTIPO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MEREQTIPO_idx1 ON P_MEREQTIPO(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MEREQUIPO] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[SERIAL] TEXT NOT NULL,"+
					"[TIPO] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[SERIAL])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MERESTADO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MERESTADO_idx1 ON P_MERESTADO(NOMBRE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_NIVELMEDIAPAGO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[DESCRIPCION] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_MEDIAPAGO] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[ACTIVO] TEXT NOT NULL,"+
					"[NIVEL] INTEGER NOT NULL,"+
					"[PORCOBRO] TEXT NOT NULL,"+
					"[VIA_PAGO_DGI] TEXT,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_MEDIAPAGO_idx1 ON P_MEDIAPAGO(NIVEL)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_MEDIAPAGO_idx2 ON P_MEDIAPAGO(PORCOBRO)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_RUTA] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[ACTIVO] TEXT NOT NULL,"+
					"[VENDEDOR] TEXT NOT NULL,"+
					"[VENTA] TEXT NOT NULL,"+
					"[FORANIA] TEXT NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[SUBTIPO] TEXT NOT NULL,"+
					"[BODEGA] TEXT NOT NULL,"+
					"[SUBBODEGA] TEXT NOT NULL,"+
					"[DESCUENTO] TEXT NOT NULL,"+
					"[BONIF] TEXT NOT NULL,"+
					"[KILOMETRAJE] TEXT NOT NULL,"+
					"[IMPRESION] TEXT NOT NULL,"+
					"[RECIBOPROPIO] TEXT NOT NULL,"+
					"[CELULAR] TEXT NOT NULL,"+
					"[RENTABIL] TEXT NOT NULL,"+
					"[OFERTA] TEXT NOT NULL,"+
					"[PERCRENT] REAL NOT NULL,"+
					"[PASARCREDITO] TEXT NOT NULL,"+
					"[TECLADO] TEXT NOT NULL,"+
					"[EDITDEVPREC] TEXT NOT NULL,"+
					"[EDITDESC] TEXT NOT NULL,"+
					"[PARAMS] TEXT NOT NULL,"+
					"[SEMANA] INTEGER NOT NULL,"+
					"[OBJANO] INTEGER NOT NULL,"+
					"[OBJMES] INTEGER NOT NULL,"+
					"[SYNCFOLD] TEXT NOT NULL,"+
					"[WLFOLD] TEXT NOT NULL,"+
					"[FTPFOLD] TEXT NOT NULL,"+
					"[EMAIL] TEXT NOT NULL,"+
					"[LASTIMP] INTEGER NOT NULL,"+
					"[LASTCOM] INTEGER NOT NULL,"+
					"[LASTEXP] INTEGER NOT NULL,"+
					"[IMPSTAT] TEXT NOT NULL,"+
					"[EXPSTAT] TEXT NOT NULL,"+
					"[COMSTAT] TEXT NOT NULL,"+
					"[PARAM1] TEXT NOT NULL,"+
					"[PARAM2] TEXT NOT NULL,"+
					"[PESOLIM] REAL NOT NULL,"+
					"[INTERVALO_MAX] INTEGER NOT NULL,"+
					"[LECTURAS_VALID] INTEGER NOT NULL,"+
					"[INTENTOS_LECT] INTEGER NOT NULL,"+
					"[HORA_INI] INTEGER NOT NULL,"+
					"[HORA_FIN] INTEGER NOT NULL,"+
					"[APLICACION_USA] INTEGER NOT NULL,"+
					"[PUERTO_GPS] INTEGER NOT NULL,"+
					"[ES_RUTA_OFICINA] INTEGER NOT NULL,"+
					"[DILUIR_BON] INTEGER NOT NULL,"+
					"[PREIMPRESION_FACTURA] INTEGER NOT NULL,"+
					"[MODIFICAR_MEDIA_PAGO] INTEGER NOT NULL,"+
					"[IDIMPRESORA] TEXT NOT NULL,"+
					"[NUMVERSION] TEXT NOT NULL,"+
					"[FECHAVERSION] INTEGER NOT NULL,"+
					"[ARQUITECTURA] TEXT NOT NULL,"+
					"[PERMITIR_PRODUCTO_NUEVO] INTEGER NOT NULL,"+
					"[PERMITIR_CANTIDAD_MAYOR] INTEGER NOT NULL,"+
                    "[ENVIO_AUTO_PEDIDOS] INTEGER NOT NULL,"+
                    "[PEDIDOS_CLINUEVO] INTEGER NOT NULL,"+
					"[VALIDAR_POSICION_GEOREFERENCIAL] INTEGER NOT NULL,"+
					"[CONTROL_CANASTA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_CLIGRUPO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[CLIENTE] TEXT NOT NULL,"+
					"PRIMARY KEY (CODIGO,CLIENTE)"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_CLIGRUPO_idx1 ON P_CLIGRUPO(CODIGO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX P_CLIGRUPO_idx2 ON P_CLIGRUPO(CLIENTE)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_PRODGRUP] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_COREL] ("+
					"[RESOL] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[CORELINI] INTEGER NOT NULL,"+
					"[CORELFIN] INTEGER NOT NULL,"+
					"[CORELULT] INTEGER NOT NULL,"+
					"[FECHARES] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHAVIG] INTEGER NOT NULL,"+
					"[RESGUARDO] INTEGER NOT NULL,"+
					"[VALOR1] INTEGER NOT NULL,"+
					"PRIMARY KEY (RESOL,SERIE)"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CORELNC] ("+
					"[RESOL] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[CORELINI] INTEGER NOT NULL,"+
					"[CORELFIN] INTEGER NOT NULL,"+
					"[CORELULT] INTEGER NOT NULL,"+
					"[FECHARES] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHAVIG] INTEGER NOT NULL,"+
					"[RESGUARDO] INTEGER NOT NULL,"+
					"[VALOR1] INTEGER NOT NULL,"+
					"PRIMARY KEY (RESOL,SERIE)"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CORRELREC] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[INICIAL] INTEGER NOT NULL,"+
					"[FINAL] INTEGER NOT NULL,"+
					"[ACTUAL] INTEGER NOT NULL,"+
					"[ENVIADO] TEXT NOT NULL,"+
					"PRIMARY KEY (RUTA,SERIE)"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_CORREL_OTROS] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[SERIE] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[INICIAL] INTEGER NOT NULL,"+
					"[FINAL] INTEGER NOT NULL,"+
					"[ACTUAL] INTEGER NOT NULL,"+
					"[ENVIADO] TEXT NOT NULL,"+
					"PRIMARY KEY (RUTA,SERIE,TIPO)"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_ARCHIVOCONF] ("+
					"[RUTA] TEXT NOT NULL,"+
					"[TIPO_HH] TEXT NOT NULL,"+
					"[IDIOMA] TEXT  NOT NULL,"+
					"[TIPO_IMPRESORA] TEXT  NOT NULL,"+
					"[SERIAL_HH] TEXT  NOT NULL,"+
					"[MODIF_PESO] TEXT NOT NULL,"+
					"[PUERTO_IMPRESION] TEXT NOT NULL,"+
					"[LBS_O_KGS] TEXT NOT NULL,"+
					"[NOTA_CREDITO] INTEGER  NOT NULL,"+
					"PRIMARY KEY ([RUTA])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_ENCABEZADO_REPORTESHH] ("+
					"[CODIGO] INTEGER NOT NULL,"+
					"[TEXTO] TEXT NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_ENCABEZADO_REPORTESHH_idx1 ON P_ENCABEZADO_REPORTESHH(SUCURSAL)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_BONIF] ("+
					"[CLIENTE] TEXT NOT NULL,"+
					"[CTIPO] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PTIPO] INTEGER NOT NULL,"+
					"[TIPORUTA] INTEGER NOT NULL,"+
					"[TIPOBON] TEXT NOT NULL,"+
					"[RANGOINI] REAL NOT NULL,"+
					"[RANGOFIN] REAL NOT NULL,"+
					"[TIPOLISTA] INTEGER NOT NULL,"+
					"[TIPOCANT] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[LISTA] TEXT NOT NULL,"+
					"[CANTEXACT] TEXT NOT NULL,"+
					"[GLOBBON] TEXT NOT NULL,"+
					"[PORCANT] TEXT NOT NULL,"+
					"[FECHAINI] INTEGER NOT NULL,"+
					"[FECHAFIN] INTEGER NOT NULL,"+
					"[CODDESC] INTEGER NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[EMP] TEXT NOT NULL,"+
					"[UMPRODUCTO] TEXT NOT NULL,"+
					"[UMBONIFICACION] TEXT NOT NULL,"+
					"PRIMARY KEY ([CLIENTE],[CTIPO],[PRODUCTO],[PTIPO],[TIPORUTA],[TIPOBON],[RANGOINI])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_BONLIST] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT] INTEGER NOT NULL,"+
					"[CANTMIN] REAL NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_VENDEDOR] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[CLAVE] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[NIVEL] INTEGER NOT NULL,"+
					"[NIVELPRECIO] INTEGER NOT NULL,"+
					"[BODEGA] TEXT NOT NULL,"+
					"[SUBBODEGA] TEXT NOT NULL,"+
					"[COD_VEHICULO] TEXT NOT NULL,"+
					"[LIQUIDANDO] TEXT NOT NULL,"+
					"[BLOQUEADO] INTEGER NOT NULL,"+
					"[DEVOLUCION_SAP] INTEGER NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			//#HS_20181207 AGREGUE TABLA P_VEHICULO.
			vSQL="CREATE TABLE [P_VEHICULO] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[MARCA]  TEXT NOT NULL,"+
					"[PLACA] TEXT NOT NULL,"+
					"[PESO] INTEGER NOT NULL,"+
					"[KM_MILLAS] TEXT NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_MUNI] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[DEPAR]  TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_REF1] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_REF2] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_REF3] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_FACTORCONV] ("+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[UNIDADSUPERIOR] TEXT NOT NULL,"+
					"[FACTORCONVERSION] REAL NOT NULL,"+
					"[UNIDADMINIMA] TEXT NOT NULL,"+
					"PRIMARY KEY ([PRODUCTO],[UNIDADSUPERIOR],[UNIDADMINIMA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_PORCMERMA] ("+
					"[EMPRESA] TEXT NOT NULL,"+
					"[SUCURSAL] TEXT NOT NULL,"+
					"[RUTA] TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PORCENTAJEMERMA] REAL NOT NULL,"+
					"[PORCMINIMO] REAL NOT NULL,"+
					"[PORCMAXIMO] REAL NOT NULL,"+
					"PRIMARY KEY ([EMPRESA],[SUCURSAL],[RUTA],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			//Jaros modifico la tabla P_IMPRESORA
			vSQL="CREATE TABLE [P_IMPRESORA]("+
					"[IDIMPRESORA] TEXT NOT NULL,"+
					"[NUMSERIE] TEXT NOT NULL,"+
					"[MARCA] TEXT NOT NULL,"+
					"[MODELO] TEXT NOT NULL,"+
					"[CREADA] TEXT NOT NULL,"+
					"[MODIFICADA] TEXT NOT NULL,"+
					"[FECHA_CREADA] INTEGER NOT NULL,"+
					"[FECHA_MODIFICADA] INTEGER NOT NULL,"+
					"[MACADDRESS] TEXT NOT NULL,"+
					"PRIMARY KEY ([IDIMPRESORA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_FECHA]("+
					"[RUTA] TEXT NOT NULL,"+
					"[FECHA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([RUTA])"+
					");";
			database.execSQL(vSQL);

			vSQL= "CREATE TABLE [P_HANDHELD]("+
					"[NUMPLACA] TEXT NOT NULL,"+
					"[NUMSERIE] TEXT NOT NULL,"+
					"[MARCA] TEXT NOT NULL,"+
					"[CREADA] TEXT NOT NULL,"+
					"[MODIFICADA] TEXT NOT NULL,"+
					"[FECHA_CREADA] INTEGER NOT NULL,"+
					"[FECHA_MODIFICADA] INTEGER NOT NULL,"+
					"[CORELZ] INT NOT NULL,"+
					"[GRANDTOTAL] REAL NOT NULL,"+
					"PRIMARY KEY ([NUMPLACA])"+
					");";
			database.execSQL(vSQL);

			vSQL= "CREATE TABLE [P_TRANSERROR]("+
					"[IDTRANSERROR] INTEGER NOT NULL,"+
					"[TRANSERROR] TEXT NOT NULL,"+
					"PRIMARY KEY ([IDTRANSERROR])"+
					");";
			database.execSQL(vSQL);

			//#CKFK 20190619 Agregué la tabla P_GLOBPARAM para poder obtener los datos de los clientes nuevos
			vSQL= "CREATE TABLE [P_GLOBPARAM]("+
					"[EMPID] TEXT NOT NULL,"+
					"[COMSERVER] TEXT NOT NULL,"+
					"[FTPSERVER] TEXT NOT NULL,"+
					"[VERFACTURA] TEXT NOT NULL,"+
					"[VERPEDIDO] TEXT NOT NULL,"+
					"[VERCOBRO] TEXT NOT NULL,"+
					"[VALORN1] REAL NOT NULL,"+
					"PRIMARY KEY ([EMPID])"+
					");";
			database.execSQL(vSQL);

			//#CKFK 20190619 Agregué la tabla P_CONFIGBARRA para poder configurar la lectura de las barras
			vSQL = "CREATE TABLE [P_CONFIGBARRA](" +
					"[IDCONFIGBARRA] INT NOT NULL," +
					"[LONGITUDBARRA] INT NOT NULL," +
					"[PREFIJO] TEXT NOT NULL,"+
					"PRIMARY KEY ([IDCONFIGBARRA])"+
					");";
			database.execSQL(vSQL);

			//#CKFK 20201106 Agregué la tabla DS_PEDIDO para el encabezado de los pedidos para despachar
			vSQL = "CREATE TABLE [DS_PEDIDO](" +
					"[COREL] TEXT NOT NULL," +
					"[ANULADO] TEXT ," +
					"[FECHA] INT," +
					"[EMPRESA] TEXT," +
					"[RUTA] TEXT," +
					"[VENDEDOR] TEXT," +
					"[CLIENTE] TEXT," +
					"[KILOMETRAJE] TEXT," +
					"[FECHAENTR] INT," +
					"[DIRENTREGA] TEXT," +
					"[TOTAL] REAL," +
					"[DESMONTO] REAL," +
					"[IMPMONTO] REAL," +
					"[PESO] REAL," +
					"[BANDERA] TEXT," +
					"[STATCOM] TEXT," +
					"[CALCOBJ] TEXT," +
					"[IMPRES] INT," +
					"[ADD1] TEXT," +
					"[ADD2] TEXT," +
					"[ADD3] TEXT,"+
					"[ORDEN_COMPRA] TEXT,"+
					"PRIMARY KEY ([COREL])"+
					");";
			database.execSQL(vSQL);

			//#CKFK 20201106 Agregué la tabla DS_PEDIDOD para el detalle de los pedidos a despachar
			vSQL = "CREATE TABLE [DS_PEDIDOD](" +
					"[COREL] TEXT NOT NULL," +
					"[PRODUCTO] TEXT NOT NULL," +
					"[EMPRESA] TEXT," +
					"[ANULADO] TEXT," +
					"[CANT] REAL," +
					"[PRECIO] REAL," +
					"[IMP] REAL," +
					"[DES] REAL," +
					"[DESMON] REAL," +
					"[TOTAL] REAL," +
					"[PRECIODOC] REAL," +
					"[PESO] REAL," +
					"[VAL1] REAL," +
					"[VAL2] TEXT," +
					"[RUTA] TEXT,"+
					"[UMVENTA] TEXT," +
					"[UMSTOCK] TEXT," +
					"[UMPESO] TEXT," +
					"[CANT_ORIGINAL] REAL," +
					"[PESO_ORIGINAL] REAL," +
					"PRIMARY KEY ([COREL]," +
					"[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

            vSQL="CREATE TABLE [P_STOCK_PV] ("+
                    "RUTA TEXT NOT NULL,"+
                    "CODIGO TEXT NOT NULL,"+
                    "CANT REAL NOT NULL,"+
                    "PESO REAL NOT NULL,"+
                    "CANT_SOL REAL NOT NULL,"+
                    "PESO_SOL REAL NOT NULL,"+
                    "UNIDADMEDIDA TEXT NOT NULL,"+
                    "DOCUMENTO TEXT NOT NULL,"+
                    "FECHA INTEGER NOT NULL,"+
                    "ANULADO INTEGER NOT NULL,"+
                    "CENTRO TEXT NOT NULL,"+
                    "ESTADO TEXT NOT NULL,"+
                    "ENVIADO INTEGER NOT NULL,"+
                    "PRIMARY KEY ([RUTA],[CODIGO],[UNIDADMEDIDA],[DOCUMENTO])"+
                    ");";
            database.execSQL(vSQL);

            vSQL="CREATE INDEX P_stock_pv_idx1 ON P_stock_pv(FECHA)";database.execSQL(vSQL);
            vSQL="CREATE INDEX P_stock_pv_idx2 ON P_stock_pv(ANULADO)";database.execSQL(vSQL);
            vSQL="CREATE INDEX P_stock_pv_idx3 ON P_stock_pv(ESTADO)";database.execSQL(vSQL);
            vSQL="CREATE INDEX P_stock_pv_idx4 ON P_stock_pv(ENVIADO)";database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_STOCK_PVC] ("+
					"CODIGO TEXT NOT NULL,"+
					"CANT REAL NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			//#JP 20210831
            vSQL = "CREATE TABLE [P_CANAL](" +
                    "[CODIGO] TEXT NOT NULL," +
                    "[NOMBRE] TEXT NOT NULL,"+
                    "PRIMARY KEY ([CODIGO])"+
                    ");";
            database.execSQL(vSQL);

            //#JP 20210831
            vSQL = "CREATE TABLE [P_CANALSUB](" +
                    "[CODIGO] TEXT NOT NULL," +
                    "[CANAL] TEXT NOT NULL," +
                    "[NOMBRE] TEXT NOT NULL,"+
                    "PRIMARY KEY ([CODIGO])"+
                    ");";
            database.execSQL(vSQL);

            //#JP 20210831
            vSQL = "CREATE TABLE [P_PRIORIZACION](" +
                    "[CODIGO] TEXT NOT NULL," +
                    "PRIMARY KEY ([CODIGO])"+
                    ");";
            database.execSQL(vSQL);

            //#JP 20210913
            vSQL = "CREATE TABLE [P_DEPAR](" +
                    "[CODIGO] TEXT NOT NULL," +
                    "[AREA] TEXT NOT NULL," +
                    "[NOMBRE] TEXT NOT NULL,"+
                    "PRIMARY KEY ([CODIGO])"+
                    ");";
            database.execSQL(vSQL);

			vSQL = "CREATE TABLE [P_CIUDAD](" +
					"[CODIGO] TEXT NOT NULL," +
					"[DISTRITO] TEXT," +
					"[CORREGIMIENTO] TEXT," +
					"[PROVINCIA] TEXT,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [P_tipologia] ("+
					"CODIGO TEXT NOT NULL,"+
					"CANALSUB TEXT NOT NULL,"+
					"NOMBRE TEXT NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [P_monto_minimo_cliente] ("+
					"CLIENTE TEXT NOT NULL,"+
					"MM_ESTANDAR REAL NOT NULL,"+
					"MM_EXTRARUTA REAL NOT NULL,"+
					"PRIMARY KEY ([CLIENTE])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX P_monto_minimo_cliente_idx1 ON P_monto_minimo_cliente(CLIENTE)";database.execSQL(vSQL);

			return 1;

		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private int scriptTablasT(SQLiteDatabase database) {
		String vSQL;

		try {

			vSQL="CREATE TABLE [T_VENTA] ("+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[UM] TEXT NOT NULL,"+
					"[SIN_EXISTENCIA] INTEGER NOT NULL,"+ //JP20210614
					"[CANT] REAL NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[IMP] REAL NOT NULL,"+
					"[DES] REAL NOT NULL,"+
					"[DESMON] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIODOC] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[VAL1] REAL NOT NULL,"+
					"[VAL2] TEXT NOT NULL,"+
					"[VAL3] REAL NOT NULL,"+
					"[VAL4] TEXT NOT NULL,"+
					"[PERCEP] REAL NOT NULL,"+
					"[CANTORIGINAL] REAL,"+
					"[PESOORIGINAL] REAL,"+
					"PRIMARY KEY ([PRODUCTO],[UM],[SIN_EXISTENCIA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX T_VENTA_idx1 ON T_VENTA(PRODUCTO)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_VENTA_DESPACHO] ("+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[EMPRESA] TEXT NOT NULL,"+
					"[UM] TEXT NOT NULL,"+
					"[CANTSOL] REAL NOT NULL,"+
					"[CANTREC] REAL NOT NULL,"+
					"[CANTDIF] REAL NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[IMP] REAL NOT NULL,"+
					"[DES] REAL NOT NULL,"+
					"[DESMON] REAL NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIODOC] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[VAL1] REAL NOT NULL,"+
					"[VAL2] TEXT NOT NULL,"+
					"[VAL3] REAL NOT NULL,"+
					"[VAL4] TEXT NOT NULL,"+
					"[CANTORIGINAL] REAL NOT NULL,"+
					"[PESOORIGINAL] REAL NOT NULL,"+
					"PRIMARY KEY ([PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX T_VENTA_DESPACHO_idx1 ON T_VENTA_DESPACHO(PRODUCTO)";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [T_DESC] ("+
					"[ID] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PTIPO] INTEGER NOT NULL,"+
					"[RANGOINI] REAL NOT NULL,"+
					"[RANGOFIN] REAL NOT NULL,"+
					"[DESCTIPO] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[GLOBDESC] TEXT NOT NULL,"+
					"[PORCANT] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"PRIMARY KEY ([ID])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX T_DESC_idx1 ON T_DESC(PRODUCTO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX T_DESC_idx2 ON T_DESC(PTIPO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX T_DESC_idx3 ON T_DESC(RANGOINI)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX T_DESC_idx4 ON T_DESC(RANGOFIN)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_BONIF] ("+
					"[ID] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[PTIPO] INTEGER NOT NULL,"+
					"[TIPOBON] TEXT NOT NULL,"+
					"[RANGOINI] REAL NOT NULL,"+
					"[RANGOFIN] REAL NOT NULL,"+
					"[TIPOLISTA] INTEGER NOT NULL,"+
					"[TIPOCANT] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[LISTA] TEXT NOT NULL,"+
					"[CANTEXACT] TEXT NOT NULL,"+
					"[GLOBBON] TEXT NOT NULL,"+
					"[PORCANT] TEXT NOT NULL,"+
					"[NOMBRE] TEXT NOT NULL,"+
					"[EMP] TEXT NOT NULL,"+
					"[UMPRODUCTO] TEXT NOT NULL,"+
					"[UMBONIFICACION] TEXT NOT NULL,"+
					"PRIMARY KEY ([ID])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_CxCD] ("+
					"[Item] INTEGER NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CODDEV] TEXT NOT NULL,"+
					"[TOTAL] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PRECLISTA] REAL NOT NULL,"+
					"[REF] TEXT NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[FECHA_CAD] INTEGER NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[POR_PESO] TEXT NOT NULL,"+
					"[TIENE_LOTE] INTEGER,"+
					"PRIMARY KEY ([Item])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX T_CxCD_idx1 ON T_CxCD(CODIGO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX T_CxCD_idx2 ON T_CxCD(CANT)";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_DEVOL] ("+
					"[CODIGO] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[CANTM] REAL NOT NULL,"+
					"PRIMARY KEY ([CODIGO])"+
					");";
			database.execSQL(vSQL);


			vSQL="CREATE TABLE [T_PAGO] ("+
					"[ITEM] INTEGER NOT NULL,"+
					"[CODPAGO] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[VALOR] REAL NOT NULL,"+
					"[DESC1] TEXT NOT NULL,"+
					"[DESC2] TEXT NOT NULL,"+
					"[DESC3] TEXT NOT NULL,"+
					"PRIMARY KEY ([ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_DEPOSB] ("+
					"[DENOMINACION] REAL NOT NULL,"+
					"[CANTIDAD] INTEGER NOT NULL,"+
					"[TIPO] TEXT NOT NULL,"+
					"[MONEDA] INTEGER NOT NULL,"+
					"PRIMARY KEY ([DENOMINACION],[TIPO],[MONEDA])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_PAGOD] ("+
					"[ITEM] INTEGER NOT NULL,"+
					"[DOCUMENTO] TEXT NOT NULL,"+
					"[TIPODOC] TEXT NOT NULL,"+
					"[MONTO] REAL NOT NULL,"+
					"[PAGO] REAL NOT NULL,"+
					"PRIMARY KEY ([ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_LOTES] ("+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[LOTE] TEXT NOT NULL,"+
					"[CANTIDAD] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"PRIMARY KEY ([PRODUCTO],[LOTE])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_BONITEM] ("+
					"[ITEM] INTEGER NOT NULL,"+
					"[PRODID] TEXT NOT NULL,"+
					"[BONIID] TEXT NOT NULL,"+
					"[CANT] REAL NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[COSTO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[UMVENTA] TEXT NOT NULL,"+
					"[UMSTOCK] TEXT NOT NULL,"+
					"[UMPESO] TEXT NOT NULL,"+
					"[FACTOR] REAL NOT NULL,"+
					"[POR_PESO] TEXT NOT NULL,"+
					"PRIMARY KEY ([ITEM])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_BARRA] ("+
					"[BARRA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[PESOORIG] REAL NOT NULL,"+
					"[CANTIDAD] REAL NOT NULL,"+
					"PRIMARY KEY ([BARRA],[CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_BARRA_BONIF] ("+
					"[BARRA] TEXT NOT NULL,"+
					"[CODIGO] TEXT NOT NULL,"+
					"[PRECIO] REAL NOT NULL,"+
					"[PESO] REAL NOT NULL,"+
					"[PESOORIG] REAL NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"PRIMARY KEY ([BARRA],[CODIGO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_BONIFFALT] ("+
					"[PRODID]   TEXT NOT NULL,"+
					"[PRODUCTO] TEXT NOT NULL,"+
					"[CANT]     REAL NOT NULL,"+
					"PRIMARY KEY ([PRODID],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_CANASTA] ("+
					"IDCANASTA INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"RUTA TEXT NOT NULL,"+
					"FECHA INTEGER NOT NULL,"+
					"CLIENTE TEXT NOT NULL,"+
					"PRODUCTO TEXT NOT NULL,"+
					"CANTREC INTEGER,"+
					"CANTENTR INTEGER,"+
					"STATCOM TEXT DEFAULT 'N',"+
					"CORELTRANS TEXT,"+
					"PESOREC REAL DEFAULT 0,"+
					"PESOENTR REAL DEFAULT 0,"+
					"ANULADO INTEGER DEFAULT 0,"+
					"UNIDBAS TEXT DEFAULT 'UN'," +
					"CODIGOLIQUIDACION INTEGER DEFAULT 0,"+
					"VENDEDOR TEXT "+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE TABLE [T_FACTURAD_MODIF]("+
					"[COREL] TEXT NOT NULL,"+
					"[ANULADO] INTEGER NOT NULL,"+
					"[PRODUCTO] TEXT (15) NOT NULL,"+
					"[CANTSOLICITADA] REAL,"+
					"[UMVENTASOLICITADA] TEXT,"+
					"[PESOSOLICITADO] REAL,"+
					"[CANTENTREGADA] REAL,"+
					"[UMVENTAENTREGADA] TEXT,"+
					"[PESOENTREGADO] REAL,"+
					"[IDRAZON] TEXT,"+
					"[PEDCOREL] TEXT,"+
					"[STATCOM] TEXT,"+
					"[DESPCOREL] TEXT,"+
					"PRIMARY KEY ([COREL],[PRODUCTO])"+
					");";
			database.execSQL(vSQL);

			vSQL="CREATE INDEX T_FACTURAD_MODIF_IDX1 ON T_FACTURAD_MODIF(PRODUCTO)";
			database.execSQL(vSQL);
			vSQL="CREATE INDEX T_FACTURAD_MODIF_IDX2 ON T_FACTURAD_MODIF(ANULADO)";
			database.execSQL(vSQL);

			return 1;

		} catch (SQLiteException e) {
			msgbox(e.getMessage());return 0;
		}
	}

	public int scriptData(SQLiteDatabase db) {

		try {
			db.execSQL("INSERT INTO Params VALUES (0,0,0,0,0,'','','','','');");
			db.execSQL("INSERT INTO FinDia VALUES (0,0, 0,0,0,0, 0,0,0,0);");
			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private void msgbox(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);

		dialog.setIcon(R.drawable.ic_error);

		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Toast.makeText(getApplicationContext(), "Yes button pressed",Toast.LENGTH_SHORT).show();
			}
		});
		dialog.show();

	}

}