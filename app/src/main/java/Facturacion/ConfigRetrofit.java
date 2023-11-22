package Facturacion;

import android.app.Activity;
import android.content.Context;
import com.dts.roadp.appGlobals;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigRetrofit {
    private appGlobals gl;
    private static Retrofit retrofit = null;

    public ConfigRetrofit (Context cont) {
        gl = ((appGlobals) (((Activity) cont).getApplication()));
    }
    public  <S> S CrearServicio(Class<S> claseServicio) {
        getCliente();
        return retrofit.create(claseServicio);
    }

    public void getCliente() {

        OkHttpClient cliente = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(gl.url_base)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(cliente)
                    .build();
        }

    }
}
