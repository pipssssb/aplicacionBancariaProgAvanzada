package procesamiento;

import modelo.CuentaBancaria;

public class ProcesadorDeposito implements ProcesadorOperaciones {
    private double tasaComision;

    public ProcesadorDeposito(double tasaComision) {
        this.tasaComision = tasaComision;
    }

    @Override
    public void depositar(CuentaBancaria cuenta, double monto) {
        double montoNeto = monto * (1 - tasaComision);
        cuenta.setSaldo(cuenta.getSaldo() + montoNeto);
        if (tasaComision > 0) {
            System.out.println("Depositado: $" + montoNeto + " (comisión: $" + (monto - montoNeto) + ")");
        } else {
            System.out.println("Depositado: $" + monto);
        }
    }

    @Override
    public void retirar(CuentaBancaria cuenta, double monto) {
        double montoTotal = monto * (1 + tasaComision);
        if (cuenta.getSaldo() >= montoTotal) {
            cuenta.setSaldo(cuenta.getSaldo() - montoTotal);
            if (tasaComision > 0) {
                System.out.println("Retirado: $" + monto + " (comisión: $" + (montoTotal - monto) + ")");
            } else {
                System.out.println("Retirado: $" + monto);
            }
        } else {
            System.out.println("¡Saldo insuficiente!");
        }
    }
}