package entidades;

public interface Validador {
    boolean validar(String tipo, Object... parametros);
    String obtenerMensajeError();
}