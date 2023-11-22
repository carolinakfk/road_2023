package com.dts.roadp;

import android.os.Bundle;
import android.widget.ListView;

public class ListaPromo extends PBase {

	private ListView listView;
	
	private ListAdaptPromo adapter;
	private clsClasses.clsPromoItem selitem;
	
	private clsDescLista clsDList;
	
	private String prodid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_promo);
		
		super.InitBase();
		addlog("ListaPromo",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		
		prodid=((appGlobals) vApp).gstr;
		
		clsDList=new clsDescLista(this,prodid);
		
		adapter=new ListAdaptPromo(this, clsDList.items);
		listView.setAdapter(adapter);
		
		closekeyb();
	}

	
}
