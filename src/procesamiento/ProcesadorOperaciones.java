package procesamiento;

import modelo.CuentaBancaria;

public interface ProcesadorOperaciones {
    void depositar(CuentaBancaria cuenta, double monto);
    void retirar(CuentaBancaria cuenta, double monto);
}