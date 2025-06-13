package notificaciones;

import modelo.CuentaBancaria;

public class NotificadorSMS implements Notificador {
    @Override
    public void enviarNotificacion(CuentaBancaria cuenta, String mensaje) {
        System.out.println("Enviando SMS a " + cuenta.getTitular() + ": " + mensaje);
    }
}