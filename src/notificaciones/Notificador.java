package notificaciones;

import modelo.CuentaBancaria;

public interface Notificador {
    void enviarNotificacion(CuentaBancaria cuenta, String mensaje);
}