package gestores;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import entidades.Validador;

interface EstrategiaValidacion {
    boolean validar(Object... parametros);
    String obtenerMensajeError();
}

class ValidacionRetiro implements EstrategiaValidacion {
    private static final double SALDO_MINIMO = 100.0;
    private String mensajeError = "";

    @Override
    public boolean validar(Object... parametros) {
        double monto = (Double) parametros[0];
        double saldoActual = (Double) parametros[1];

        if (monto <= 0) {
            mensajeError = "El monto debe ser mayor a 0";
            return false;
        }
        if (saldoActual - monto < SALDO_MINIMO) {
            mensajeError = "No se puede retirar. Saldo mínimo requerido: $" + SALDO_MINIMO;
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

class ValidacionDeposito implements EstrategiaValidacion {
    private String mensajeError = "";

    @Override
    public boolean validar(Object... parametros) {
        double monto = (Double) parametros[0];

        if (monto <= 0) {
            mensajeError = "El monto a depositar debe ser mayor a 0";
            return false;
        }
        if (monto > 10000) {
            mensajeError = "Monto máximo por depósito: $10,000";
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

class ValidacionHorario implements EstrategiaValidacion {
    private static final int HORA_INICIO = 6;
    private static final int HORA_FIN = 23;
    private String mensajeError = "";

    @Override
    public boolean validar(Object... parametros) {
        int horaActual = LocalDateTime.now().getHour();
        if (horaActual < HORA_INICIO || horaActual >= HORA_FIN) {
            mensajeError = "Operaciones disponibles de " + HORA_INICIO + ":00 a " + HORA_FIN + ":00";
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

class ValidacionLimiteDiario implements EstrategiaValidacion {
    private static final double LIMITE_DIARIO = 5000.0;
    private String mensajeError = "";

    @Override
    public boolean validar(Object... parametros) {
        double montoOperacion = (Double) parametros[0];
        double montoGastadoHoy = (Double) parametros[1];

        if (montoGastadoHoy + montoOperacion > LIMITE_DIARIO) {
            mensajeError = "Límite diario excedido. Límite: $" + LIMITE_DIARIO +
                    ", Gastado hoy: $" + montoGastadoHoy;
            return false;
        }
        return true;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }
}

public class ValidadorOperacionesImpl implements Validador {
    private Map<String, EstrategiaValidacion> estrategias;
    private String mensajeError = "";

    public ValidadorOperacionesImpl() {
        inicializarEstrategias();
    }

    private void inicializarEstrategias() {
        estrategias = new HashMap<>();
        estrategias.put("RETIRO", new ValidacionRetiro());
        estrategias.put("DEPOSITO", new ValidacionDeposito());
        estrategias.put("HORARIO", new ValidacionHorario());
        estrategias.put("LIMITE_DIARIO", new ValidacionLimiteDiario());
    }

    @Override
    public boolean validar(String tipo, Object... parametros) {
        EstrategiaValidacion estrategia = estrategias.get(tipo.toUpperCase());
        if (estrategia == null) {
            mensajeError = "Tipo de validación no válido";
            return false;
        }

        boolean resultado = estrategia.validar(parametros);
        if (!resultado) {
            mensajeError = estrategia.obtenerMensajeError();
        }
        return resultado;
    }

    @Override
    public String obtenerMensajeError() {
        return mensajeError;
    }

    public void agregarEstrategiaValidacion(String tipo, EstrategiaValidacion estrategia) {
        estrategias.put(tipo.toUpperCase(), estrategia);
    }
}
