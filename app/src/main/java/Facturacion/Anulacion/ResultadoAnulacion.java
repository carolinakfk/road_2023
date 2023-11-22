package Facturacion.Anulacion;

public class ResultadoAnulacion {

    private String estado = "";
    private String FechaAutorizacion = "";
    private String codigoRespuestaDGI = "";
    private String MensajeRespuesta = "";
    private String EstadoEmisionEDOC = "";
    private String CodigoError = "";

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaAutorizacion() {
        return FechaAutorizacion;
    }

    public void setFechaAutorizacion(String fechaAutorizacion) {
        this.FechaAutorizacion = fechaAutorizacion;
    }

    public String getCodigoRespuestaDGI() {
        return codigoRespuestaDGI;
    }

    public void setCodigoRespuestaDGI(String codigoRespuestaDGI) {
        this.codigoRespuestaDGI = codigoRespuestaDGI;
    }

    public String getMensajeRespuesta() {
        return MensajeRespuesta;
    }

    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.MensajeRespuesta = mensajeRespuesta;
    }

    public String getEstadoEmisionEDOC() {
        return EstadoEmisionEDOC;
    }

    public void setEstadoEmisionEDOC(String value) {
        this.EstadoEmisionEDOC = value;
    }
    public String getCodigoError() {
        return CodigoError;
    }
    public void setCodigoError(String value) {
        this.CodigoError = value;
    }
}
