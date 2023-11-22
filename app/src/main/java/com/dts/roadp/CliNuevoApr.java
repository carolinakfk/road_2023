package com.dts.roadp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class CliNuevoApr extends PBase {
	
	private Spinner sprel,spgru,spesc,spest,spgen,sphij;
	private EditText txtcui;
	private TextView lblFecha,lblMuni;
	private ScrollView scrView;

	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();

	private int cyear,cmonth,cday,dweek;
	
	private String cui,idmun="*",idrel,idgru,idesc,idest,idgen;
	private int numhij,fechanac=0;
		
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
	[0-9]{5} Seguido de cinco d�gitos
	\\s? Puede Contener un espacio en blanco o no
	[0-9]{4} Debe de Finalizar con cuatro digitos del 0 al 9
	$ Indica el final de la cadena */
	private static final String PATTERN_CUI = "^[0-9]{4}\\s?[0-9]{5}\\s?[0-9]{4}$";
	private String cuimsg;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_nuevo_apr);
		
		super.InitBase();
		addlog("CliNuevoApr",""+du.getActDateTime(),gl.vend);

		//spmun= (Spinner) findViewById(R.id.spinner1);
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
		
		scrView=(ScrollView) findViewById(R.id.scrollView1);
		
		
		fillSpinMuni();
		fillSpinners();
		setHandlers();	   
		
	}
	   
	   
    // Events 
	
	public void doSave(View view) {

		try{
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

		/*
		spmun.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

		    	try {
		    		TextView spinlabel=(TextView)parentView.getChildAt(0);
		  	    	spinlabel.setTypeface(lblFecha.getTypeface());
		        } catch (Exception e) {
		        }

		    	try {
		    		idmun=spincode.get(position);
		        } catch (Exception e) {
		        	idmun="";
		        }

		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});
		*/
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}	  
   

	// Main 

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
	
			ins.init("D_CLINUEVO_APR");

			ins.add("CODIGO",gl.tcorel);
			ins.add("RUTA",gl.ruta);
			ins.add("FECHANAC",fechanac);
			ins.add("CUI",cui);
			ins.add("DepID",iddep);
			ins.add("MuniID",idmun);
			ins.add("Religion",idrel);
			ins.add("Etnico",idgru);
			ins.add("Escolaridad",idesc);
			ins.add("Estado",idest);
			ins.add("Genero",idgen);
			ins.add("Hijos",numhij);
			ins.add("STATCOM","N");
		
			db.execSQL(ins.sql());

			toastcent("Cliente nuevo creado");

			super.finish();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox(e.getMessage());
		//	return;
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

		try {
			// Acá se compara la expresión regular con el string ingresado
			Pattern pattern = Pattern.compile(PATTERN_CUI);
			// Match the given input against this pattern
			Matcher matcher = pattern.matcher(Cui);
			boolean validCui = matcher.matches();

			// Se reemplazan los espacios en blanco en la cadena (si tiene)
			Cui = Cui.replace(" ", "");

			//Si la cadena cumple con la expresion regular debemos de verificar que sea un CUI válido
			if (validCui == true) {

				// Extraemos el numero del DPI
				String no = Cui.substring(0, 8);
				// Extraemos el numero de Departamento
				int depto = Integer.parseInt(Cui.substring(9, 11));
				// Extraemos el numero de Municipio
				int muni = Integer.parseInt(Cui.substring(11, 13));

				// Se extra el numero validador
				int ver = Integer.parseInt(Cui.substring(8, 9));

				// Array con la cantidad de municipios que contiene cada departamento.

				int munisPorDepto[] = {
						/* 01 - Guatemala tiene:      */ 17 /* municipios. */,
						/* 02 - El Progreso tiene:    */  8 /* municipios. */,
						/* 03 - Sacatepéquez tiene:   */ 16 /* municipios. */,
						/* 04 - Chimaltenango tiene:  */ 16 /* municipios. */,
						/* 05 - Escuintla tiene:      */ 13 /* municipios. */,
						/* 06 - Santa Rosa tiene:     */ 14 /* municipios. */,
						/* 07 - Sololá tiene:         */ 19 /* municipios. */,
						/* 08 - Totonicapán tiene:    */  8 /* municipios. */,
						/* 09 - Quetzaltenango tiene: */ 24 /* municipios. */,
						/* 10 - Suchitepéquez tiene:  */ 21 /* municipios. */,
						/* 11 - Retalhuleu tiene:     */  9 /* municipios. */,
						/* 12 - San Marcos tiene:     */ 30 /* municipios. */,
						/* 13 - Huehuetenango tiene:  */ 32 /* municipios. */,
						/* 14 - Quiché tiene:         */ 21 /* municipios. */,
						/* 15 - Baja Verapaz tiene:   */  8 /* municipios. */,
						/* 16 - Alta Verapaz tiene:   */ 17 /* municipios. */,
						/* 17 - Petén tiene:          */ 14 /* municipios. */,
						/* 18 - Izabal tiene:         */  5 /* municipios. */,
						/* 19 - Zacapa tiene:         */ 11 /* municipios. */,
						/* 20 - Chiquimula tiene:     */ 11 /* municipios. */,
						/* 21 - Jalapa tiene:         */  7 /* municipios. */,
						/* 22 - Jutiapa tiene:        */ 17 /* municipios. */
				};


				//Verificamos que no se haya ingresado 0 en la posicion de depto o municipio
				if ((muni == 0 || depto == 0) || (muni == 0 && depto == 0)) {
					valido = false;
					cuimsg += "CUI no válido";
				} else {
					//Si el numero de depto ingresado en la cadena es mayor 22 es cui invalido
					cuimsg += "munixdepto: " + munisPorDepto.length;
					if (depto > munisPorDepto.length) {
						valido = false;
						cuimsg += "CUI no válido Departamento fuera de rango";
					} else {
						//si depto es menor o igual a 22

						cuimsg += "Municipios maximos: " + munisPorDepto[depto - 1];
						//se valida que el municipio ingresado en la cadena este dentro del rango del depto
						if (muni > munisPorDepto[depto - 1]) {
							valido = false;
							cuimsg += "CUI no válido municipio fuera de rango";
						} else {

							// si es valido
							int total = 0;
							//Se realiza la siguiente Ooperación
							for (int i = 0; i < no.length(); i++) {
								cuimsg += "-" + no.substring(i, i + 1);
								total += (Integer.parseInt(no.substring(i, i + 1))) * (i + 2);
							}

							// al total de la anterior operación se le saca el mod 11

							int modulo = total % 11;
							cuimsg += "CUI con módulo" + modulo;

							// Si el mod es igual al numero verificador el cui es valido , sino es invalido
							if (modulo != ver) {
								valido = false;
							}
						}
					}
				}

			} else {
				valido = false;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		//se retorna el booleano que indica si el cui es válido o no
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
		String s;

		try{
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

	   try { // TE=TJ	
		   ArrayAdapter<String> darel = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemrel);
		   darel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   sprel.setAdapter(darel);sprel.setSelection(0);
	/*   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	   
	   try { */
		   ArrayAdapter<String> dagru = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemgru);
		   dagru.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   spgru.setAdapter(dagru);spgru.setSelection(0);
	/*   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	   
	   try { */// CA=CT
		   ArrayAdapter<String> daesc = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemesc);
		   daesc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   spesc.setAdapter(daesc);spesc.setSelection(0);
	/*   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	   
	   try { */
		   ArrayAdapter<String> daest = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemest);
		   daest.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   spest.setAdapter(daest);spest.setSelection(0);
	/*   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	   
	   try { *///MA = M, FE=F, TE=3
		   ArrayAdapter<String> dagen = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemgen);
		   dagen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   spgen.setAdapter(dagen);spgen.setSelection(0);
	/*   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	   
	   try { */
		   ArrayAdapter<String> dahij = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, itemhij);
		   dahij.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		   sphij.setAdapter(dahij);sphij.setSelection(0);
	   } catch (Exception e) {
		   addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
	   }
	  
   }
   
	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExitNoSave();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { }
			});

			dialog.show();
		}catch (Exception e){
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
	
	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin crear cliente");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
}
