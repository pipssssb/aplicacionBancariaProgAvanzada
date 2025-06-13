// NUEVAS FUNCIONALIDADES: 4) SISTEMA DE FAVORITOS PARA TRANSFERENCIAS

package entidades;

public class ContactoFavorito {
    private String nombre;
    private String numeroCuenta;
    private String alias;
    private String banco;

    public ContactoFavorito(String nombre, String numeroCuenta, String alias, String banco) {
        this.nombre = nombre;
        this.numeroCuenta = numeroCuenta;
        this.alias = alias;
        this.banco = banco;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getAlias() { return alias; }
    public String getBanco() { return banco; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setBanco(String banco) { this.banco = banco; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s - Cuenta: %s", nombre, alias, banco, numeroCuenta);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContactoFavorito that = (ContactoFavorito) obj;
        return alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        return alias.hashCode();
    }
}

