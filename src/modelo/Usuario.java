// NUEVAS FUNCIONALIDADES: 5) SISTEMA DE AUTENTICACIÓN BÁSICA

package modelo;

import java.time.LocalDateTime;

interface EstrategiaBloqueo {
    boolean debeBloquear(int intentosFallidos);
    LocalDateTime calcularTiempoBloqueo();
    String obtenerMensajeBloqueo();
}

class BloqueoTemporal implements EstrategiaBloqueo {
    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 5;

    @Override
    public boolean debeBloquear(int intentosFallidos) {
        return intentosFallidos >= MAX_INTENTOS;
    }

    @Override
    public LocalDateTime calcularTiempoBloqueo() {
        return LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
    }

    @Override
    public String obtenerMensajeBloqueo() {
        return "Usuario bloqueado por " + MINUTOS_BLOQUEO + " minutos debido a intentos fallidos";
    }
}

interface ValidadorPin {
    boolean esValido(String pin);
    String obtenerMensajeError();
}

class ValidadorPin4Digitos implements ValidadorPin {
    @Override
    public boolean esValido(String pin) {
        return pin != null && pin.length() == 4 && pin.matches("\\d+");
    }

    @Override
    public String obtenerMensajeError() {
        return "El PIN debe tener exactamente 4 dígitos";
    }
}

interface EstrategiaAutenticacion {
    boolean autenticar(String pinAlmacenado, String pinIngresado);
    void onAutenticacionExitosa();
    void onAutenticacionFallida();
}

class AutenticacionSimple implements EstrategiaAutenticacion {
    @Override
    public boolean autenticar(String pinAlmacenado, String pinIngresado) {
        return pinAlmacenado.equals(pinIngresado);
    }

    @Override
    public void onAutenticacionExitosa() {
        System.out.println("Autenticación exitosa");
    }

    @Override
    public void onAutenticacionFallida() {
        // Manejo delegado al Usuario
    }
}

public class Usuario {
    private String nombreUsuario;
    private String pin;
    private int intentosFallidos;
    private LocalDateTime tiempoBloqueo;
    private EstrategiaBloqueo estrategiaBloqueo;
    private ValidadorPin validadorPin;
    private EstrategiaAutenticacion estrategiaAuth;

    public Usuario(String nombreUsuario, String pin) {
        this(nombreUsuario, pin, new BloqueoTemporal(), new ValidadorPin4Digitos(), new AutenticacionSimple());
    }

    public Usuario(String nombreUsuario, String pin, EstrategiaBloqueo estrategiaBloqueo,
                   ValidadorPin validadorPin, EstrategiaAutenticacion estrategiaAuth) {
        this.nombreUsuario = nombreUsuario;
        this.pin = pin;
        this.intentosFallidos = 0;
        this.tiempoBloqueo = null;
        this.estrategiaBloqueo = estrategiaBloqueo;
        this.validadorPin = validadorPin;
        this.estrategiaAuth = estrategiaAuth;
    }

    public boolean autenticar(String pinIngresado) {
        if (estaBloqueado()) {
            System.out.println("Usuario bloqueado. Intente más tarde.");
            return false;
        }

        if (estrategiaAuth.autenticar(pin, pinIngresado)) {
            intentosFallidos = 0;
            estrategiaAuth.onAutenticacionExitosa();
            return true;
        } else {
            intentosFallidos++;
            estrategiaAuth.onAutenticacionFallida();
            System.out.println("PIN incorrecto. Intento " + intentosFallidos);

            if (estrategiaBloqueo.debeBloquear(intentosFallidos)) {
                bloquearUsuario();
            }
            return false;
        }
    }

    private void bloquearUsuario() {
        tiempoBloqueo = estrategiaBloqueo.calcularTiempoBloqueo();
        System.out.println(estrategiaBloqueo.obtenerMensajeBloqueo());
    }

    private boolean estaBloqueado() {
        if (tiempoBloqueo == null) return false;

        if (LocalDateTime.now().isBefore(tiempoBloqueo)) {
            return true;
        } else {
            // Desbloquear usuario
            tiempoBloqueo = null;
            intentosFallidos = 0;
            return false;
        }
    }

    public boolean cambiarPin(String pinActual, String pinNuevo) {
        if (!estrategiaAuth.autenticar(pin, pinActual)) {
            System.out.println("PIN actual incorrecto");
            return false;
        }

        if (!validadorPin.esValido(pinNuevo)) {
            System.out.println(validadorPin.obtenerMensajeError());
            return false;
        }

        this.pin = pinNuevo;
        System.out.println("PIN cambiado exitosamente");
        return true;
    }

    public void cambiarEstrategiaBloqueo(EstrategiaBloqueo nuevaEstrategia) {
        this.estrategiaBloqueo = nuevaEstrategia;
    }

    public void cambiarValidadorPin(ValidadorPin nuevoValidador) {
        this.validadorPin = nuevoValidador;
    }

    public void cambiarEstrategiaAutenticacion(EstrategiaAutenticacion nuevaEstrategia) {
        this.estrategiaAuth = nuevaEstrategia;
    }

    // Getters
    public String getNombreUsuario() { return nombreUsuario; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public String getPin() { return pin; }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
