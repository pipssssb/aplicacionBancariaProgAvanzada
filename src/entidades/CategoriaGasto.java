// NUEVAS FUNCIONALIDADES: 3) SISTEMA DE CATEGORIZACIÓN DE GASTOS

package entidades;

public class CategoriaGasto {
    private String nombre;
    private String descripcion;
    private double limiteMaximo;
    private double gastoActual;

    public CategoriaGasto(String nombre, String descripcion, double limiteMaximo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.limiteMaximo = limiteMaximo;
        this.gastoActual = 0.0;
    }

    public boolean puedeGastar(double monto) {
        return (gastoActual + monto) <= limiteMaximo;
    }

    public void registrarGasto(double monto) {
        gastoActual += monto;
    }

    public double getDisponible() {
        return Math.max(0, limiteMaximo - gastoActual);
    }

    public void reiniciarGastos() {
        gastoActual = 0.0;
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getLimiteMaximo() { return limiteMaximo; }
    public double getGastoActual() { return gastoActual; }

    public void setLimiteMaximo(double limiteMaximo) {
        this.limiteMaximo = limiteMaximo;
    }

    @Override
    public String toString() {
        return nombre + " (" + descripcion + ") - Límite: $" + limiteMaximo +
                " - Disponible: $" + getDisponible();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategoriaGasto that = (CategoriaGasto) obj;
        return nombre.equals(that.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }
}

