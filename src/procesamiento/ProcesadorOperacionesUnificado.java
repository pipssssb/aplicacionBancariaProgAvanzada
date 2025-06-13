package procesamiento;

import entidades.Procesador;
import modelo.CuentaBancaria;

public class ProcesadorOperacionesUnificado implements Procesador<CuentaBancaria> {
    private ProcesadorOperaciones procesadorOperaciones;

    @Override
    public void configurar(Object configuracion) {
        if (configuracion instanceof Double) {
            double tasaComision = (Double) configuracion;
            this.procesadorOperaciones = new ProcesadorDeposito(tasaComision);
        }
    }

    @Override
    public void procesar(CuentaBancaria cuenta, String operacion, Object... parametros) {
        double monto = (Double) parametros[0];

        switch (operacion.toUpperCase()) {
            case "DEPOSITAR":
                procesadorOperaciones.depositar(cuenta, monto);
                break;
            case "RETIRAR":
                procesadorOperaciones.retirar(cuenta, monto);
                break;
        }
    }
}