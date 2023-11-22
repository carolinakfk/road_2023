package com.dts.roadp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PicView extends PBase {

	private ImageView imgImg;
	
	private String imgpath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic_view);
		
		super.InitBase();
		addlog("PicView",""+du.getActDateTime(),gl.vend);
		
		imgImg=(ImageView) findViewById(R.id.imgImg);
		
		this.setTitle(((appGlobals) vApp).gstr);
		imgpath=((appGlobals) vApp).imgpath;
		
		try {
			Bitmap bmImg = BitmapFactory.decodeFile(imgpath);
			imgImg.setImageBitmap(bmImg);

			PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imgImg);
			photoViewAttacher.setZoomable(true);
			photoViewAttacher.update();

	    } catch (Exception e) {
	    	mu.msgbox(e.getMessage());
	    	imgImg.setVisibility(View.INVISIBLE);
		}
	
		closekeyb();
		
	}

}
