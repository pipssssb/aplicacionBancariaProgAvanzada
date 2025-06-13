package notificaciones;

import entidades.Procesador;
import modelo.CuentaBancaria;

public class NotificadorUnificado implements Procesador<CuentaBancaria> {
    private Notificador notificador = new NotificadorConsole();

    @Override
    public void configurar(Object configuracion) {
        if (configuracion instanceof String) {
            String tipo = (String) configuracion;
            switch (tipo.toUpperCase()) {
                case "EMAIL":
                    this.notificador = new NotificadorEmail();
                    break;
                case "SMS":
                    this.notificador = new NotificadorSMS();
                    break;
                default:
                    this.notificador = new NotificadorConsole();
            }
        }
    }

    @Override
    public void procesar(CuentaBancaria cuenta, String operacion, Object... parametros) {
        String mensaje = (String) parametros[0];
        notificador.enviarNotificacion(cuenta, mensaje);
    }
}