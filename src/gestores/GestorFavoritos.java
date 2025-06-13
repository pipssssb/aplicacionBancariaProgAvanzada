package gestores;
import entidades.*;
import java.util.ArrayList;
import java.util.function.Predicate;

interface EstrategiaValidacionFavorito {
    boolean esValido(ContactoFavorito favorito, ArrayList<ContactoFavorito> favoritosExistentes);
    String obtenerMensajeError();
}

class ValidacionAliasUnico implements EstrategiaValidacionFavorito {
    private String mensajeError = "";

    @Override
    public boolean esValido(ContactoFavorito favorito, ArrayList<ContactoFavorito> favoritosExistentes) {
        boolean existeAlias = favoritosExistentes.stream()
                .anyMatch(f -> f.getAlias().equalsIgnoreCase(favorito.getAlias()));

        if (existeAlias) {
            mensajeError = "Ya existe un contacto con ese alias";
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

interface EstrategiaBusqueda<T> {
    ArrayList<T> buscar(ArrayList<T> lista, String criterio);
}

class BusquedaPorNombre implements EstrategiaBusqueda<ContactoFavorito> {
    @Override
    public ArrayList<ContactoFavorito> buscar(ArrayList<ContactoFavorito> lista, String criterio) {
        return lista.stream()
                .filter(f -> f.getNombre().toLowerCase().contains(criterio.toLowerCase()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}

class BusquedaPorBanco implements EstrategiaBusqueda<ContactoFavorito> {
    @Override
    public ArrayList<ContactoFavorito> buscar(ArrayList<ContactoFavorito> lista, String criterio) {
        return lista.stream()
                .filter(f -> f.getBanco().toLowerCase().contains(criterio.toLowerCase()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}

public class GestorFavoritos implements Gestor<ContactoFavorito> {
    private ArrayList<ContactoFavorito> favoritos;
    private EstrategiaValidacionFavorito estrategiaValidacion;

    public GestorFavoritos() {
        this(new ValidacionAliasUnico());
    }

    public GestorFavoritos(EstrategiaValidacionFavorito estrategia) {
        this.favoritos = new ArrayList<>();
        this.estrategiaValidacion = estrategia;
    }

    @Override
    public boolean agregar(ContactoFavorito favorito) {
        if (!estrategiaValidacion.esValido(favorito, favoritos)) {
            System.out.println(estrategiaValidacion.obtenerMensajeError());
            return false;
        }
        favoritos.add(favorito);
        System.out.println("Contacto favorito agregado exitosamente");
        return true;
    }

    @Override
    public boolean eliminar(String alias) {
        ContactoFavorito contacto = buscar(alias);
        if (contacto != null) {
            favoritos.remove(contacto);
            System.out.println("Contacto eliminado exitosamente");
            return true;
        }
        System.out.println("No se encontró un contacto con ese alias");
        return false;
    }

    @Override
    public ArrayList<ContactoFavorito> listar() {
        return new ArrayList<>(favoritos);
    }

    @Override
    public ContactoFavorito buscar(String alias) {
        return favoritos.stream()
                .filter(f -> f.getAlias().equalsIgnoreCase(alias))
                .findFirst()
                .orElse(null);
    }

    public boolean agregarFavorito(String nombre, String numeroCuenta, String alias, String banco) {
        ContactoFavorito nuevo = new ContactoFavorito(nombre, numeroCuenta, alias, banco);
        return agregar(nuevo);
    }

    public boolean eliminarFavorito(String alias) {
        return eliminar(alias);
    }

    public ContactoFavorito buscarPorAlias(String alias) {
        return buscar(alias);
    }

    public ArrayList<ContactoFavorito> buscarPorNombre(String nombre) {
        EstrategiaBusqueda<ContactoFavorito> busquedaNombre = new BusquedaPorNombre();
        return busquedaNombre.buscar(favoritos, nombre);
    }

    public ArrayList<ContactoFavorito> buscarConEstrategia(EstrategiaBusqueda<ContactoFavorito> estrategia, String criterio) {
        return estrategia.buscar(favoritos, criterio);
    }

    public ArrayList<ContactoFavorito> buscarConPredicado(Predicate<ContactoFavorito> predicado) {
        return favoritos.stream()
                .filter(predicado)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void listarFavoritos() {
        if (favoritos.isEmpty()) {
            System.out.println("No hay contactos favoritos");
            return;
        }
        System.out.println("\n=== CONTACTOS FAVORITOS ===");
        for (int i = 0; i < favoritos.size(); i++) {
            System.out.println((i + 1) + ". " + favoritos.get(i));
        }
    }

    public ArrayList<ContactoFavorito> obtenerFavoritos() {
        return listar();
    }

    public void cambiarEstrategiaValidacion(EstrategiaValidacionFavorito nuevaEstrategia) {
        this.estrategiaValidacion = nuevaEstrategia;
    }
}
