// NUEVAS FUNCIONALIDADES: 2) VALIDADOR DE OPERACIONES

package entidades;

public interface ValidadorOperaciones {
    boolean validarRetiro(double monto, double saldoActual);
    boolean validarDeposito(double monto);
    boolean validarHorario();
    boolean validarLimiteDiario(double montoOperacion, double montoGastadoHoy);
    String obtenerMensajeError();
}

