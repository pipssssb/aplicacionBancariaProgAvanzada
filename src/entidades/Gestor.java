package entidades;
import java.util.ArrayList;

public interface Gestor<T> {
    boolean agregar(T item);
    boolean eliminar(String identificador);
    ArrayList<T> listar();
    T buscar(String criterio);
}