package notificaciones;

import modelo.CuentaBancaria;

public class NotificadorEmail implements Notificador {
    @Override
    public void enviarNotificacion(CuentaBancaria cuenta, String mensaje) {
        System.out.println("Enviando correo a " + cuenta.getTitular() + ": " + mensaje);
    }
}