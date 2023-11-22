package com.dts.roadp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class CliNuevoAprEdit extends PBase {

	private Spinner sprel,spgru,spesc,spest,spgen,sphij;
	private EditText txtcui,txtNom,txtDir,txtTel,txtNit;
	private TextView lblFecha,lblMuni;
	private ScrollView scrView;

	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();

	private int cyear,cmonth,cday,dweek;
	
	private String cli,cui,idmun="*",idrel,idgru,idesc,idest,idgen;
	private int numhij;
	private long fechanac=0;

	final String[] itemrel = {"CATOLICA", "EVANGELICA", "MORMON", "TESTIGO JEHOVA", "OTROS"}; 
	final String[] itemgru = {"LADINO", "INDIGENA", "MORENO DE COLOR", "NO APLICA"};	
	final String[] itemesc = {"ANALFABETA", "PRIMARIA", "BASICO", "UNIVERSIDAD","CARRERA TECNICA UNIVERSITARIA", "MAESTRIA"};	
	final String[] itemest = {"CASADO/A", "UNIDO/A", "DIVORCIADO/A", "SOLTERO/A","VIUDO/A", "NO APLICA"};	
	final String[] itemgen = {"MASCULINO", "FEMENINO", "TERCER GENERO"};	
	final String[] itemhij = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};	

	//Expresión regular para CUI
	/* ^ Indica el inicio de la Cadena
	[0-9]{4} Debe de iniciar con cuatro digitos del 0 al 9
	\\s? Puede Contener un espacio en blanco o no
	[0-9]{5} Seguido de cinco dígitos
	\\s? Puede Contener un espacio en blanco o no
	[0-9]{4} Debe de Finalizar con cuatro digitos del 0 al 9
	$ Indica el final de la cadena */
	private static final String PATTERN_CUI = "^[0-9]{4}\\s?[0-9]{5}\\s?[0-9]{4}$";
	private String cuimsg;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_nuevo_apr_edit);
		
		super.InitBase();
		addlog("CliNuevoAprEdit",""+du.getActDateTime(),gl.vend);

		sprel= (Spinner) findViewById(R.id.spinner2);
		spgru= (Spinner) findViewById(R.id.spinner3);
		spesc= (Spinner) findViewById(R.id.spinner4);
		spest= (Spinner) findViewById(R.id.spinner5);
		spgen= (Spinner) findViewById(R.id.spinner6);
		sphij= (Spinner) findViewById(R.id.spinner7);
		
		cyear=0;
		lblFecha= (TextView) findViewById(R.id.textView2);
		lblMuni= (TextView) findViewById(R.id.textView11);lblMuni.setText("");
		
		txtcui= (EditText) findViewById(R.id.editText1);
		txtNom = (EditText) findViewById(R.id.txtCNNom);
		txtDir = (EditText) findViewById(R.id.txtCNDir);
		txtTel = (EditText) findViewById(R.id.txtCNTel);
		txtNit = (EditText) findViewById(R.id.txtCNNit);
		
		scrView=(ScrollView) findViewById(R.id.scrollView1);
				
		cli=gl.tcorel;
				
		fillSpinMuni();	
		setHandlers();	   
		
		loadItem();
		fillSpinners();
	}

	   
    // Events 
   
	
	public void doSave(View view) {

		try{
			if (mu.emptystr(txtNom.getText().toString())) {
				msgScroll("Falta nombre");txtNom.requestFocus();return ;
			}

			if (mu.emptystr(txtDir.getText().toString())) {
				msgScroll("Falta dirección");txtDir.requestFocus();return ;
			}

			if (mu.emptystr(txtNit.getText().toString())) {
				msgScroll("Falta NIT");txtNit.requestFocus();return ;
			}

			cui=txtcui.getText().toString().trim();
			if (mu.emptystr(cui)) {
				msgScroll("Falta definir CUI / DPI");return ;
			}

			if (cui.length()!=13) {
				msgScroll("CUI / DPI incorrecto");return ;
			}

			if (validaCUI(cui)) {
				if (parseSpinValues()) save();
			} else {
				msgAskCUI("CUI / DPI incorrecto. Continuar");
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
 
	public void doFecha(View view) {

		try{
			fechaNac();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
   
	public void doMuni(View view) {
		try{
			browse=1;
			Intent intent = new Intent(this,Municipio.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void setHandlers(){

		try{
			sprel.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});

			spgru.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});

			spesc.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});

			spest.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});

			spgen.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});

			sphij.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

				//	try {
						TextView spinlabel=(TextView) parentView.getChildAt(0);
						spinlabel.setTypeface(lblFecha.getTypeface());
				/*	} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) { return;}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}	  
   	

	// Main 

	private void loadItem() {
		try{
			loadPart1();
			loadPart2();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void loadPart1() {
		Cursor dt;

		try {

			sql="SELECT NOMBRE,DIRECCION,TELEFONO,NIT FROM D_CLINUEVO WHERE CODIGO='"+cli+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return;

			dt.moveToFirst();

			txtNom.setText(dt.getString(0));
			txtDir.setText(dt.getString(1));
			txtTel.setText(dt.getString(2));
			txtNit.setText(dt.getString(3));

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	private void loadPart2() {
		Cursor dt;

		try {

			sql="SELECT FECHANAC,CUI,MuniID,Religion,Etnico,Escolaridad,Estado,Genero,Hijos FROM D_CLINUEVO_APR WHERE CODIGO='"+cli+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return;

			dt.moveToFirst();
			
			fechanac=dt.getLong(0);
			cui=dt.getString(1);
			idmun=dt.getString(2);
			idrel=dt.getString(3);
			idgru=dt.getString(4);
			idesc=dt.getString(5);
			idest=dt.getString(6);
			idgen=dt.getString(7);
			numhij=dt.getInt(8);

			txtcui.setText(cui);
			parseDateValue();
			nombreMuni();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private void save() {
		
		Cursor dt;
		String iddep;

		try {
			sql="SELECT DEPAR FROM P_MUNI WHERE CODIGO='"+idmun+"'";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			iddep=dt.getString(0);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());return;
		}

		
		try {*/

			db.beginTransaction();

			upd.init("D_CLINUEVO");		
			upd.add("NOMBRE",txtNom.getText().toString());
			upd.add("DIRECCION",txtDir.getText().toString()+"");
			upd.add("TELEFONO",txtTel.getText().toString()+"");
			upd.add("NIT",txtNit.getText().toString()+"");
			upd.Where("CODIGO='"+cli+"'");
			db.execSQL(upd.SQL());
			
			
			upd.init("P_CLIENTE");		
			upd.add("NOMBRE",txtNom.getText().toString());
			upd.Where("CODIGO='"+cli+"'");
			db.execSQL(upd.SQL());
						
			
			upd.init("D_CLINUEVO_APR");
			upd.add("FECHANAC",fechanac);
			upd.add("CUI",cui);
			upd.add("DepID",iddep);
			upd.add("MuniID",idmun);
			upd.add("Religion",idrel);
			upd.add("Etnico",idgru);
			upd.add("Escolaridad",idesc);
			upd.add("Estado",idest);
			upd.add("Genero",idgen);
			upd.add("Hijos",numhij);
			upd.add("STATCOM","N");
			upd.Where("CODIGO='"+cli+"'");
			db.execSQL(upd.SQL());
			

			db.setTransactionSuccessful();				
			db.endTransaction();			

			toast("Cliente nuevo actualizado");				
			super.finish();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox(e.getMessage());return;
		}		   

	}

	private boolean parseSpinValues() {
		String dbgv=".";

		if (fechanac==0) {
			msgFecha("Falta definir fecha de nacimiento");return false;
		}

		if (idmun.equalsIgnoreCase("*")) {
			msgMuni("Falta definir municipio");return false;
		}

		try {
			dbgv="1";
			idrel=itemrel[sprel.getSelectedItemPosition()];idrel=idrel.substring(0,2);
			if (idrel.equalsIgnoreCase("TE")) idrel="TJ";

			dbgv="2";
			idgru=itemgru[spgru.getSelectedItemPosition()];idgru=idgru.substring(0,2);
			dbgv="3";
			idesc=itemesc[spesc.getSelectedItemPosition()];idesc=idesc.substring(0,2);
			if (idesc.equalsIgnoreCase("CA")) idesc="CT";
			dbgv="4";
			idest=itemest[spest.getSelectedItemPosition()];idest=idest.substring(0,2);
			dbgv="5";
			idgen=itemgen[spgen.getSelectedItemPosition()];idgen=idgen.substring(0,2);
			if (idgen.equalsIgnoreCase("MA")) idgen="M";
			if (idgen.equalsIgnoreCase("FE")) idgen="F";
			if (idgen.equalsIgnoreCase("TE")) idgen="3";
			dbgv="6";
			numhij=mu.CInt(itemhij[sphij.getSelectedItemPosition()]);

			return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage()+":"+dbgv);return false;
		}

	}

	private boolean validaCUI(String Cui) {
		boolean valido=true;
		cuimsg="";

		// Ac� se compara la expresi�n regular con el string ingresado
		Pattern pattern = Pattern.compile(PATTERN_CUI);
		// Match the given input against this pattern
		Matcher matcher = pattern.matcher(Cui);
		boolean validCui= matcher.matches();

		// Se reemplazan los espacios en blanco en la cadena (si tiene)
		Cui = Cui.replace(" ", "");

		try{
			//Si la cadena cumple con la expresion regular debemos de verificar que sea un CUI v�lido
			if(validCui ==true){

				// Extraemos el numero del DPI
				String no = Cui.substring(0, 8);
				// Extraemos el numero de Departamento
				int depto = Integer.parseInt(Cui.substring(9, 11));
				// Extraemos el numero de Municipio
				int muni = Integer.parseInt(Cui.substring(11,13));

				// Se extra el numero validador
				int ver = Integer.parseInt(Cui.substring(8,9));

				// Array con la cantidad de municipios que contiene cada departamento.

				int munisPorDepto[] = {
						/* 01 - Guatemala tiene:      */ 17 /* municipios. */,
						/* 02 - El Progreso tiene:    */  8 /* municipios. */,
						/* 03 - Sacatep�quez tiene:   */ 16 /* municipios. */,
						/* 04 - Chimaltenango tiene:  */ 16 /* municipios. */,
						/* 05 - Escuintla tiene:      */ 13 /* municipios. */,
						/* 06 - Santa Rosa tiene:     */ 14 /* municipios. */,
						/* 07 - Solol� tiene:         */ 19 /* municipios. */,
						/* 08 - Totonicap�n tiene:    */  8 /* municipios. */,
						/* 09 - Quetzaltenango tiene: */ 24 /* municipios. */,
						/* 10 - Suchitep�quez tiene:  */ 21 /* municipios. */,
						/* 11 - Retalhuleu tiene:     */  9 /* municipios. */,
						/* 12 - San Marcos tiene:     */ 30 /* municipios. */,
						/* 13 - Huehuetenango tiene:  */ 32 /* municipios. */,
						/* 14 - Quich� tiene:         */ 21 /* municipios. */,
						/* 15 - Baja Verapaz tiene:   */  8 /* municipios. */,
						/* 16 - Alta Verapaz tiene:   */ 17 /* municipios. */,
						/* 17 - Pet�n tiene:          */ 14 /* municipios. */,
						/* 18 - Izabal tiene:         */  5 /* municipios. */,
						/* 19 - Zacapa tiene:         */ 11 /* municipios. */,
						/* 20 - Chiquimula tiene:     */ 11 /* municipios. */,
						/* 21 - Jalapa tiene:         */  7 /* municipios. */,
						/* 22 - Jutiapa tiene:        */ 17 /* municipios. */
				};



				//Verificamos que no se haya ingresado 0 en la posicion de depto o municipio
				if((muni==0 || depto==0) || (muni==0 && depto==0)){
					valido=false;
					cuimsg+="CUI no válido";
				}

				else{
					//Si el numero de depto ingresado en la cadena es mayor 22 es cui invalido
					cuimsg+="munixdepto: " + munisPorDepto.length;
					if(depto > munisPorDepto.length ){
						valido=false;
						cuimsg+="CUI no válido Departamento fuera de rango";
					}
					else{
						//si depto es menor o igual a 22

						cuimsg+="Municipios maximos: " +  munisPorDepto[depto -1];
						//se valida que el municipio ingresado en la cadena este dentro del rango del depto
						if(muni > munisPorDepto[depto -1]){
							valido=false;
							cuimsg+="CUI no válido municipio fuera de rango";
						}

						else{

							// si es valido
							int total=0;
							//Se realiza la siguiente Ooperaci�n
							for(int i=0; i<no.length(); i++){
								cuimsg+="-" +no.substring(i,i+1);
								total += (Integer.parseInt(no.substring(i,i+1)))*(i + 2);
							}

							// al total de la anterior operaci�n se le saca el mod 11

							int modulo=total%11;
							cuimsg+="CUI con modulo" + modulo;

							// Si el mod es igual al numero verificador el cui es valido , sino es invalido
							if(modulo!=ver){
								valido=false;
							}
						}
					}
				}

			} else{
				valido=false;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		//se retorna el booleano que indica si el cui es v�lido o no
		return valido;
	}


	// Date

    /*
	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			if (fechanac==0) {
				cyear=2000;
				cmonth=1;
				cday=1;
			}

			final Calendar c = Calendar.getInstance();
			int year = cyear;
			int month = cmonth-1;
			int day = cday;

			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			try {
				cyear=year;cmonth=month+1;cday=day;
				fechanac=cyear*10000+cmonth*100+cday;

				applyDate();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				msgbox(e.getMessage());
			}
		}

	}
	*/

	private void applyDate() {
		try{
			String s;

			if (cyear==0) return;

			s="";
			if (cday>9)   s=s+String.valueOf(cday)+"/"; else s=s+"0"+String.valueOf(cday)+"/";
			if (cmonth>9) s=s+String.valueOf(cmonth)+"/"; else s=s+"0"+String.valueOf(cmonth)+"/";
			s=s+cyear;

			lblFecha.setText(s);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// Aux 

	private void fillSpinMuni(){
		Cursor DT;

		try {

			sql="SELECT Codigo,Nombre FROM P_MUNI ORDER BY Nombre";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				spincode.add(DT.getString(0));
				spinlist.add(DT.getString(1));
				DT.moveToNext();
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			//spmun.setAdapter(dataAdapter);
			//spmun.setSelection(0);			

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}
	}

	private void fillSpinners(){
		String cod,cc;
		int sidx=0;

		try { // TE=TJ	
			ArrayAdapter<String> darel = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemrel);
			darel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sidx=0;cod=idrel.substring(0,2);if (cod.equalsIgnoreCase("TJ")) cod="TE";
			for (int i = 0; i <itemrel.length; i++) {
				if (itemrel[i].substring(0,2).equalsIgnoreCase(cod)) sidx=i;
			}
			sprel.setAdapter(darel);sprel.setSelection(sidx);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try {*/
			ArrayAdapter<String> dagru = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemgru);
			dagru.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sidx=0;cod=idgru.substring(0,2);
			for (int i = 0; i <itemgru.length; i++) {
				if (itemgru[i].substring(0,2).equalsIgnoreCase(cod)) sidx=i;
			}
			spgru.setAdapter(dagru);spgru.setSelection(sidx);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try { */// CA=CT
			ArrayAdapter<String> daesc = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemesc);
			daesc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sidx=0;cod=idesc.substring(0,2);if (cod.equalsIgnoreCase("CA")) cod="CT";
			for (int i = 0; i <itemesc.length; i++) {
				if (itemesc[i].substring(0,2).equalsIgnoreCase(cod)) sidx=i;
			}
			spesc.setAdapter(daesc);spesc.setSelection(sidx);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try { */
			ArrayAdapter<String> daest = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemest);
			daest.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			try {
				sidx=0;cod=idest;
				for (int i = 0; i <itemest.length; i++) {
					cc=itemest[i];if (cc.length()>1) cc=cc.substring(0,2);
					//if (cc.equalsIgnoreCase(cod)) sidx=i;
				}
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				sidx=0;
			}
			
			spest.setAdapter(daest);spest.setSelection(sidx);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try { *///MA = M, FE=F, TE=3
			ArrayAdapter<String> dagen = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemgen);
			dagen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sidx=0;cod=idgen;
			if (cod.equalsIgnoreCase("M")) cod="MA";
			if (cod.equalsIgnoreCase("F")) cod="FE";
			if (cod.equalsIgnoreCase("3")) cod="TE";
			for (int i = 0; i <itemgen.length; i++) {
				if (itemgen[i].substring(0,2).equalsIgnoreCase(cod)) sidx=i;
			}
			spgen.setAdapter(dagen);spgen.setSelection(sidx);
	/*	} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try { */
			ArrayAdapter<String> dahij = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemhij);
			dahij.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sphij.setAdapter(dahij);sphij.setSelection(numhij);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskCUI(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (parseSpinValues()) save();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					txtcui.requestFocus();
					scrView.scrollTo(0,0);
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void doExitNoSave(){

		try {

			db.beginTransaction();

			sql="DELETE FROM D_CLINUEVO WHERE (CODIGO='"+gl.tcorel+"') AND (RUTA='"+gl.ruta+"')";
			db.execSQL(sql);

			sql="DELETE FROM P_CLIENTE WHERE (CODIGO='"+gl.tcorel+"') ";
			db.execSQL(sql);

			sql="DELETE FROM P_CLIRUTA WHERE (CLIENTE='"+gl.tcorel+"') ";
			db.execSQL(sql);

			db.setTransactionSuccessful();		
			db.endTransaction();		

			super.finish();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private void msgScroll(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					txtcui.requestFocus();
					scrView.scrollTo(0,0);
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgMuni(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doMuni(null);
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgFecha(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					fechaNac();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void fechaNac() {
		try{
			//DialogFragment newFragment = new DatePickerFragment();
			//newFragment.show(getSupportFragmentManager(), "datePicker");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void aplicaMuni() {	

		try {
			if (mu.emptystr(((appGlobals) vApp).gstr)) return;
			idmun=((appGlobals) vApp).gstr;  		
			lblMuni.setText(((appGlobals) vApp).pprodname);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			idmun="*";lblMuni.setText("");
			toast(e.getMessage());
		}
	}

	private void parseDateValue() {

		try{
			cyear=(int) fechanac/10000;fechanac=fechanac % 10000;
			cmonth=(int) fechanac/100;
			cday=(int) fechanac % 100;

			applyDate();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void nombreMuni() {
		Cursor dt;

		try {

			sql="SELECT NOMBRE FROM P_MUNI WHERE CODIGO='"+idmun+"'";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			lblMuni.setText(dt.getString(0));		
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}


	// Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			if (browse==1) {
				browse=0;
				aplicaMuni();return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


}

