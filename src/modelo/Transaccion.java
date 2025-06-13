// NUEVAS FUNCIONALIDADES: 1) SISTEMA DE HISTORIAL DE TRANSACCIONES

package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

interface FormateadorTransaccion {
    String formatear(Transaccion transaccion);
}

class FormateadorCompleto implements FormateadorTransaccion {
    @Override
    public String formatear(Transaccion transaccion) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] %s - $%.2f | Saldo: $%.2f -> $%.2f | %s",
                transaccion.getFecha().format(formatter),
                transaccion.getTipo(),
                transaccion.getMonto(),
                transaccion.getSaldoAnterior(),
                transaccion.getSaldoPosterior(),
                transaccion.getDescripcion());
    }
}

class FormateadorSimple implements FormateadorTransaccion {
    @Override
    public String formatear(Transaccion transaccion) {
        return String.format("%s: $%.2f",
                transaccion.getTipo(),
                transaccion.getMonto());
    }
}

class FormateadorResumen implements FormateadorTransaccion {
    @Override
    public String formatear(Transaccion transaccion) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return String.format("%s | %s: $%.2f",
                transaccion.getFecha().format(formatter),
                transaccion.getTipo(),
                transaccion.getMonto());
    }
}

class Transaccion {
    private String tipo;
    private double monto;
    private LocalDateTime fecha;
    private String descripcion;
    private double saldoAnterior;
    private double saldoPosterior;
    private static FormateadorTransaccion formateadorDefault = new FormateadorCompleto();

    public Transaccion(String tipo, double monto, String descripcion, double saldoAnterior, double saldoPosterior) {
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
    }

    public String toString(FormateadorTransaccion formateador) {
        return formateador.formatear(this);
    }

    @Override
    public String toString() {
        return formateadorDefault.formatear(this);
    }

    public static void setFormateadorDefault(FormateadorTransaccion formateador) {
        formateadorDefault = formateador;
    }

    // Getters
    public String getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public double getSaldoAnterior() { return saldoAnterior; }
    public double getSaldoPosterior() { return saldoPosterior; }
}