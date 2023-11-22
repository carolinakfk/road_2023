package com.dts.roadp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

import java.util.ArrayList;

public class CliDocumentos extends PBase {

    private ListAdaptDocs adapter;
    private ArrayList<clsClasses.clsDocumentoImg> documentos = new ArrayList<clsClasses.clsDocumentoImg>();
    private clsClasses.clsDocumentoImg clsImg;

    private GridView gridDocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_documentos);

        super.InitBase();

        gridDocs = findViewById(R.id.gridDocs);

        CargarDocumentos();
        setHandlers();

    }

    private void setHandlers() {

    }

    private void CargarDocumentos() {
        Cursor DT;
        int codimagen = 0;
        String path = "";

        try {
            sql = "SELECT * FROM TMP_D_CLINUEVOT_IMAGEN";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsImg = clsCls.new clsDocumentoImg();

                codimagen = DT.getInt(0);
                path = Environment.getExternalStorageDirectory() + "/RoadFotos/clidocs/" + codimagen + ".jpg";

                BitmapFactory.Options opciones = new BitmapFactory.Options();
                opciones.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opciones);

                opciones.inSampleSize = calculateInSampleSize(opciones, 200, 200);
                opciones.inJustDecodeBounds = false;
                Bitmap bmImagen = BitmapFactory.decodeFile(path, opciones);
                bmImagen.getByteCount();

                clsImg.img = bmImagen;
                clsImg.nombre = path.split("/")[6];

                documentos.add(clsImg);
                DT.moveToNext();
            }

            if (DT != null) DT.close();

            adapter = new ListAdaptDocs(this, documentos);
            gridDocs.setAdapter(adapter);

        } catch (Exception e) {
            mu.msgbox("CargarDocumentos: " + e.getMessage() + sql);
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}