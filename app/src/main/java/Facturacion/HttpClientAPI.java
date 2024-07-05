package Facturacion;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientAPI {

    public int retcode = 0;
    public String data;

    private OkHttpClient client;
    private Runnable rnCallback;


    public HttpClientAPI() {
        //client = new OkHttpClient();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(5000, TimeUnit.SECONDS);
        builder.readTimeout(10000, TimeUnit.SECONDS);
        builder.writeTimeout(5000, TimeUnit.SECONDS);

        client = builder.build();

    }

    public void makeGetRequest(Request request, Runnable callback) {
        rnCallback = callback;

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        data = response.body().string();
                        retcode = 1;
                    } else {
                        data = "Error: " + response.code()+" "+response.message();
                        retcode = 0;
                    }
                } catch (Exception e) {
                    data = "Error: " + e.getMessage();
                    retcode = -1;
                } finally {
                    if (response.body() != null) response.body().close();
                }
                rnCallback.run();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                data = "Error: " + e.getMessage();
                retcode = -1;
                rnCallback.run();
            }

        });
    }

}