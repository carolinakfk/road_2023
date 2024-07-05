package com.dts.roadp;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import org.apache.http.client.HttpClient;

import Facturacion.ConfigRetrofit;
import Facturacion.HttpClientAPI;
import Facturacion.Token;
import Interfaz.ServicioToken;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class RUCprueba extends PBase {


    private ConfigRetrofit retrofit;
    private Token token = new Token();
    private HttpClientAPI htclient;
    //ServicioToken client;
    private Runnable rnToken;
    private Gson gson;

    private clsClasses.clsEmpresa Empresa = clsCls.new clsEmpresa();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rucprueba);

            super.InitBase();

            rnToken= () -> { cbToken() ;};

            htclient = new HttpClientAPI();
            gson = new Gson();
            //retrofit = new ConfigRetrofit(this);

            // Asegúrate de que la instancia Retrofit está correctamente configurada
            //client = retrofit.CrearServicio(ServicioToken.class);

            getDatosEmpresa();

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    //region Events

    public void doToken(View view) {
        try {
            //AsyncGetToken Token = new AsyncGetToken();
            //Token.execute();

            //getAPIToken();

            getHttpToken();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Main

    private void getHttpToken() {
        try {

            String base = Empresa.usuarioApi + ":" + Empresa.claveApi;
            String credenciales = "Basic "+ encodeToString(base.getBytes(), NO_WRAP);

            Request request =new Request.Builder()
                    .url("https://labpa.guru-soft.com/EdocPanama/4.0/Autenticacion/Api/ServicioEDOC?Id=1")
                    .get()
                    .addHeader("Accept", "*/*")
                    .addHeader("Accept-Encoding", "gzip,deflate,br")
                    .addHeader("Authorization", credenciales)
                    .build();

            htclient.makeGetRequest(request, rnToken);



        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    private void cbToken() {
        try {

            if (htclient.retcode!=1) {
                toast("Error: "+htclient.data);return;
            }

            String rs= htclient.data;
            try {
                token = gson.fromJson(rs, Token.class);
            } catch (JsonSyntaxException e) {
                msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            }

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    //endregion

    //region Tasks


    public class AsyncGetToken extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... vd) {
            getAPIToken();
            return null;
        }

        @Override
        protected void onPostExecute(String vdata){
            super.onPostExecute(vdata);
        }
    }

    //endregion

    //region Aux

//    private void getAPIToken() {
//        Call<Token> call=null;
//        try {
//
//            String base = Empresa.usuarioApi + ":" + Empresa.claveApi;
//            String credenciales = "Basic "+ encodeToString(base.getBytes(), NO_WRAP);
//
//            ServicioToken client = retrofit.CrearServicio(ServicioToken.class);
//
//            try {
//                call = client.getToken(credenciales);
//            } catch (Exception e) {
//                msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
//            }
//
//            String ss="";
//
//            call.enqueue(new Callback<Token>() {
//
//                @Override
//                public void onResponse(Call<Token> call, Response<Token> response) {
//                    if (response.isSuccessful()) {
//                        String se="";
//                        //MyResponse myResponse = response.body();
//                        // Handle the response
//                    } else {
//                        String se="";
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Token> call, Throwable t) {
//                    String se="";
//                    // Handle the failure
//                }
//
//
//            });
//
//            /*
//            try {
//                Response<Token> response = call.execute();
//                if (response.isSuccessful()) {
//                    token = response.body();
//                } else {
//                    toastlong("Error en respuesta: " + getClass());
//                }
//            } catch (Exception ex) {
//                mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" Error en respuesta "+ ex.getMessage());
//            }
//
//             */
//
//        } catch (Exception e) {
//            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
//        }
//    }

    private void getAPIToken() {
        Call<Token> call = null;
        try {
            String base = Empresa.usuarioApi + ":" + Empresa.claveApi;
            String credenciales = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

            try {
                //call = client.getToken(credenciales);
            } catch (Exception e) {
                msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
                return; // Salir si hay un error al crear la llamada
            }

            call.enqueue(new Callback<Token>() {
                @Override
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (response.isSuccessful()) {
                        Token token = response.body();
                        // Manejar la respuesta exitosa aquí
                        // Actualiza la UI o guarda el token
                    } else {
                        msgbox("Respuesta no exitosa: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Token> call, Throwable t) {
                    msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . onFailure: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }


    private void getDatosEmpresa() {
        Cursor DT;

        try	{
            sql = "SELECT URL_AUTENTICACION, URL_ANULACION, USUARIO_API, CLAVE_API FROM P_EMPRESA";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            if (DT.getCount() > 0) {
                Empresa.urlToken = DT.getString(0);
                Empresa.urlAnulacion = DT.getString(1);
                Empresa.usuarioApi = DT.getString(2);
                Empresa.claveApi = DT.getString(3);
            } else {
                return;
            }

            if(DT!=null) DT.close();

        } catch (Exception e) {
            mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    //endregion


}