package entidades;

public interface Procesador<T> {
    void procesar(T objeto, String operacion, Object... parametros);
    void configurar(Object configuracion);
}