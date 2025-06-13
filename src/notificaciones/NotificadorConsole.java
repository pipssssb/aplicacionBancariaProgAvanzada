
package notificaciones;

import modelo.CuentaBancaria;

public class NotificadorConsole implements Notificador {
    @Override
    public void enviarNotificacion(CuentaBancaria cuenta, String mensaje) {
        System.out.println("Notificación para " + cuenta.getTitular() + ": " + mensaje);
    }
}