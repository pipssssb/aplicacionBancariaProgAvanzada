package modelo;

import entidades.*;
import gestores.*;
import java.util.*;
import java.time.LocalDate;

interface EstrategiaOperacion {
    boolean ejecutar(CuentaBancaria cuenta, Object... parametros);
    String obtenerTipoOperacion();
    String obtenerMensajeError();
}

class OperacionDeposito implements EstrategiaOperacion {
    private String mensajeError = "";

    @Override
    public boolean ejecutar(CuentaBancaria cuenta, Object... parametros) {
        double monto = (Double) parametros[0];
        String pin = (String) parametros[1];

        if (!cuenta.autenticar(pin)) {
            mensajeError = "PIN incorrecto";
            return false;
        }

        if (!cuenta.getValidador().validar("DEPOSITO", monto) ||
                !cuenta.getValidador().validar("HORARIO")) {
            mensajeError = cuenta.getValidador().obtenerMensajeError();
            return false;
        }

        double saldoAnterior = cuenta.getSaldo();
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuenta.registrarTransaccion("DEPOSITO", monto, "Depósito en efectivo", saldoAnterior);
        System.out.println("Depósito exitoso. Nuevo saldo: $" + cuenta.getSaldo());
        return true;
    }

    @Override
    public String obtenerTipoOperacion() {
        return "DEPOSITO";
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

class OperacionRetiro implements EstrategiaOperacion {
    private String mensajeError = "";

    @Override
    public boolean ejecutar(CuentaBancaria cuenta, Object... parametros) {
        double monto = (Double) parametros[0];
        String pin = (String) parametros[1];
        String categoria = parametros.length > 2 ? (String) parametros[2] : null;

        if (!cuenta.autenticar(pin)) {
            mensajeError = "PIN incorrecto";
            return false;
        }

        cuenta.actualizarGastosDiarios();

        if (!cuenta.getValidador().validar("RETIRO", monto, cuenta.getSaldo()) ||
                !cuenta.getValidador().validar("HORARIO") ||
                !cuenta.getValidador().validar("LIMITE_DIARIO", monto, cuenta.getGastosDiarios())) {
            mensajeError = cuenta.getValidador().obtenerMensajeError();
            return false;
        }

        if (categoria != null && !categoria.isEmpty()) {
            if (!cuenta.getGestorCategorias().registrarGasto(categoria, monto)) {
                mensajeError = "Límite de categoría '" + categoria + "' excedido";
                return false;
            }
        }

        double saldoAnterior = cuenta.getSaldo();
        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuenta.setGastosDiarios(cuenta.getGastosDiarios() + monto);

        String descripcion = categoria != null ? "Retiro - Categoría: " + categoria : "Retiro en efectivo";
        cuenta.registrarTransaccion("RETIRO", monto, descripcion, saldoAnterior);
        System.out.println("Retiro exitoso. Nuevo saldo: $" + cuenta.getSaldo());
        return true;
    }

    @Override
    public String obtenerTipoOperacion() {
        return "RETIRO";
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

class OperacionTransferencia implements EstrategiaOperacion {
    private String mensajeError = "";

    @Override
    public boolean ejecutar(CuentaBancaria cuenta, Object... parametros) {
        String alias = (String) parametros[0];
        double monto = (Double) parametros[1];
        String pin = (String) parametros[2];

        if (!cuenta.autenticar(pin)) {
            mensajeError = "PIN incorrecto";
            return false;
        }

        ContactoFavorito favorito = cuenta.getGestorFavoritos().buscarPorAlias(alias);
        if (favorito == null) {
            mensajeError = "No se encontró el contacto favorito: " + alias;
            return false;
        }

        cuenta.actualizarGastosDiarios();

        if (!cuenta.getValidador().validar("RETIRO", monto, cuenta.getSaldo()) ||
                !cuenta.getValidador().validar("HORARIO") ||
                !cuenta.getValidador().validar("LIMITE_DIARIO", monto, cuenta.getGastosDiarios())) {
            mensajeError = cuenta.getValidador().obtenerMensajeError();
            return false;
        }

        double saldoAnterior = cuenta.getSaldo();
        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuenta.setGastosDiarios(cuenta.getGastosDiarios() + monto);

        String descripcion = "Transferencia a " + favorito.getNombre() + " (" + favorito.getBanco() + ")";
        cuenta.registrarTransaccion("TRANSFERENCIA", monto, descripcion, saldoAnterior);
        System.out.println("Transferencia exitosa a " + favorito.getNombre());
        return true;
    }

    @Override
    public String obtenerTipoOperacion() {
        return "TRANSFERENCIA";
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

interface FactoryGestores {
    GestorFavoritos crearGestorFavoritos();
    GestorCategorias crearGestorCategorias();
    Validador crearValidador();
}

class FactoryGestoresDefault implements FactoryGestores {
    @Override
    public GestorFavoritos crearGestorFavoritos() {
        return new GestorFavoritos();
    }

    @Override
    public GestorCategorias crearGestorCategorias() {
        return new GestorCategorias();
    }

    @Override
    public Validador crearValidador() {
        return new ValidadorOperacionesImpl();
    }
}

public class CuentaBancaria {
    private String numeroCuenta;
    private String titular;
    private double saldo;
    private ArrayList<Transaccion> historialTransacciones;
    private GestorFavoritos gestorFavoritos;
    private GestorCategorias gestorCategorias;
    private Usuario usuario;
    private Validador validador;
    private double gastosDiarios;
    private LocalDate fechaUltimosGastos;
    private Map<String, EstrategiaOperacion> operaciones;

    public CuentaBancaria(String numeroCuenta, String titular, double saldoInicial, String pin) {
        this(numeroCuenta, titular, saldoInicial, pin, new FactoryGestoresDefault());
    }

    public CuentaBancaria(String numeroCuenta, String titular, double saldoInicial, String pin, FactoryGestores factory) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldoInicial;
        this.historialTransacciones = new ArrayList<>();
        this.gestorFavoritos = factory.crearGestorFavoritos();
        this.gestorCategorias = factory.crearGestorCategorias();
        this.validador = factory.crearValidador();
        this.usuario = new Usuario(titular, pin);
        this.gastosDiarios = 0.0;
        this.fechaUltimosGastos = LocalDate.now();

        inicializarOperaciones();

        if (saldoInicial > 0) {
            registrarTransaccion("DEPOSITO_INICIAL", saldoInicial, "Saldo inicial de la cuenta", 0.0);
        }
    }

    private void inicializarOperaciones() {
        operaciones = new HashMap<>();
        operaciones.put("DEPOSITO", new OperacionDeposito());
        operaciones.put("RETIRO", new OperacionRetiro());
        operaciones.put("TRANSFERENCIA", new OperacionTransferencia());
    }

    public boolean autenticar(String pin) {
        return usuario.autenticar(pin);
    }

    public boolean ejecutarOperacion(String tipoOperacion, Object... parametros) {
        EstrategiaOperacion operacion = operaciones.get(tipoOperacion.toUpperCase());
        if (operacion == null) {
            System.out.println("Tipo de operación no válido: " + tipoOperacion);
            return false;
        }

        boolean resultado = operacion.ejecutar(this, parametros);
        if (!resultado) {
            System.out.println("Error: " + operacion.obtenerMensajeError());
        }
        return resultado;
    }

    public boolean depositar(double monto, String pin) {
        return ejecutarOperacion("DEPOSITO", monto, pin);
    }

    public boolean retirar(double monto, String pin, String categoria) {
        return ejecutarOperacion("RETIRO", monto, pin, categoria);
    }

    public boolean transferirAFavorito(String alias, double monto, String pin) {
        return ejecutarOperacion("TRANSFERENCIA", alias, monto, pin);
    }

    public void agregarOperacion(String tipo, EstrategiaOperacion operacion) {
        operaciones.put(tipo.toUpperCase(), operacion);
    }

    public void registrarTransaccion(String tipo, double monto, String descripcion, double saldoAnterior) {
        Transaccion transaccion = new Transaccion(tipo, monto, descripcion, saldoAnterior, saldo);
        historialTransacciones.add(transaccion);
    }

    public void mostrarHistorial() {
        if (historialTransacciones.isEmpty()) {
            System.out.println("No hay transacciones registradas");
            return;
        }

        System.out.println("\n=== HISTORIAL DE TRANSACCIONES ===");
        for (Transaccion t : historialTransacciones) {
            System.out.println(t);
        }
    }

    public void mostrarHistorialPorTipo(String tipo) {
        System.out.println("\n=== HISTORIAL - " + tipo.toUpperCase() + " ===");
        historialTransacciones.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase(tipo))
                .forEach(System.out::println);
    }

    public void actualizarGastosDiarios() {
        LocalDate hoy = LocalDate.now();
        if (!hoy.equals(fechaUltimosGastos)) {
            gastosDiarios = 0.0;
            fechaUltimosGastos = hoy;
        }
    }

    // Getters y setters
    public GestorFavoritos getGestorFavoritos() { return gestorFavoritos; }
    public GestorCategorias getGestorCategorias() { return gestorCategorias; }
    public Usuario getUsuario() { return usuario; }
    public Validador getValidador() { return validador; }
    public double getSaldo() { return saldo; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getTitular() { return titular; }
    public double getGastosDiarios() { return gastosDiarios; }

    public void setSaldo(double saldo) { this.saldo = saldo; }
    public void setGastosDiarios(double gastosDiarios) { this.gastosDiarios = gastosDiarios; }
}