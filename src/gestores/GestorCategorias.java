package gestores;
import entidades.*;
import java.util.ArrayList;
import java.util.HashMap;

interface ProveedorCategoriasIniciales {
    ArrayList<CategoriaGasto> obtenerCategoriasIniciales();
}

class ProveedorCategoriasDefault implements ProveedorCategoriasIniciales {
    @Override
    public ArrayList<CategoriaGasto> obtenerCategoriasIniciales() {
        ArrayList<CategoriaGasto> categorias = new ArrayList<>();
        categorias.add(new CategoriaGasto("ALIMENTOS", "Gastos en comida y restaurantes", 1000.0));
        categorias.add(new CategoriaGasto("TRANSPORTE", "Gastos en transporte público y combustible", 500.0));
        categorias.add(new CategoriaGasto("ENTRETENIMIENTO", "Cine, juegos, salidas", 300.0));
        categorias.add(new CategoriaGasto("SERVICIOS", "Luz, agua, internet, teléfono", 800.0));
        categorias.add(new CategoriaGasto("OTROS", "Gastos varios", 200.0));
        return categorias;
    }
}

interface EstrategiaControlGasto {
    boolean puedeRegistrarGasto(CategoriaGasto categoria, double monto, double gastoActual);
    String obtenerMensajeError();
}

class ControlGastoLimiteMaximo implements EstrategiaControlGasto {
    private String mensajeError = "";

    @Override
    public boolean puedeRegistrarGasto(CategoriaGasto categoria, double monto, double gastoActual) {
        if (gastoActual + monto > categoria.getLimiteMaximo()) {
            mensajeError = "Límite máximo excedido para la categoría " + categoria.getNombre();
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

public class GestorCategorias implements Gestor<CategoriaGasto> {
    private ArrayList<CategoriaGasto> categorias;
    private HashMap<String, Double> gastoPorCategoria;
    private ProveedorCategoriasIniciales proveedorCategorias;
    private EstrategiaControlGasto estrategiaControl;

    public GestorCategorias() {
        this(new ProveedorCategoriasDefault(), new ControlGastoLimiteMaximo());
    }

    public GestorCategorias(ProveedorCategoriasIniciales proveedor, EstrategiaControlGasto estrategia) {
        this.categorias = new ArrayList<>();
        this.gastoPorCategoria = new HashMap<>();
        this.proveedorCategorias = proveedor;
        this.estrategiaControl = estrategia;
        inicializarCategorias();
    }

    private void inicializarCategorias() {
        ArrayList<CategoriaGasto> categoriasIniciales = proveedorCategorias.obtenerCategoriasIniciales();
        for (CategoriaGasto categoria : categoriasIniciales) {
            agregar(categoria);
        }
    }

    @Override
    public boolean agregar(CategoriaGasto categoria) {
        categorias.add(categoria);
        return true;
    }

    @Override
    public boolean eliminar(String nombre) {
        return categorias.removeIf(c -> c.getNombre().equalsIgnoreCase(nombre));
    }

    @Override
    public ArrayList<CategoriaGasto> listar() {
        return new ArrayList<>(categorias);
    }

    @Override
    public CategoriaGasto buscar(String nombre) {
        return categorias.stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }

    public boolean registrarGasto(String categoria, double monto) {
        CategoriaGasto cat = buscar(categoria);
        if (cat == null) return false;

        double gastoActual = gastoPorCategoria.getOrDefault(categoria, 0.0);

        if (!estrategiaControl.puedeRegistrarGasto(cat, monto, gastoActual)) {
            return false;
        }

        gastoPorCategoria.put(categoria, gastoActual + monto);
        return true;
    }

    public CategoriaGasto buscarCategoria(String nombre) {
        return buscar(nombre);
    }

    public ArrayList<CategoriaGasto> obtenerCategorias() {
        return listar();
    }

    public void mostrarResumenGastos() {
        System.out.println("\n=== RESUMEN DE GASTOS POR CATEGORÍA ===");
        for (CategoriaGasto categoria : categorias) {
            double gastado = gastoPorCategoria.getOrDefault(categoria.getNombre(), 0.0);
            double disponible = categoria.getLimiteMaximo() - gastado;
            System.out.printf("%s: $%.2f gastado de $%.2f (Disponible: $%.2f)\n",
                    categoria.getNombre(), gastado, categoria.getLimiteMaximo(), disponible);
        }
    }

    public void cambiarEstrategiaControl(EstrategiaControlGasto nuevaEstrategia) {
        this.estrategiaControl = nuevaEstrategia;
    }

    public void cambiarProveedorCategorias(ProveedorCategoriasIniciales nuevoProveedor) {
        this.proveedorCategorias = nuevoProveedor;
    }

    public String obtenerMensajeError() {
        return estrategiaControl.obtenerMensajeError();
    }
}
