package Interfaz;

import Facturacion.Token;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ServicioToken {

    @GET("Autenticacion/Api/ServicioEDOC?Id=1")
    Call<Token> getToken(@Header("Authorization") String credenciales);
}
