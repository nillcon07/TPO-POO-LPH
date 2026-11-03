package modelo.finanzas;

import java.util.*;
import modelo.comun.Validaciones;

public final class EstadoCuenta {
    private final List<SaldoExpensa> saldos;
    private final List<ImputacionPago> imputacionesEfectivas;

    EstadoCuenta(List<SaldoExpensa> saldos, List<ImputacionPago> imputaciones) {
        this.saldos = new ArrayList<>(saldos);
        this.imputacionesEfectivas = new ArrayList<>(imputaciones);
    }
    public List<SaldoExpensa> getSaldos() { return new ArrayList<>(saldos); }
    public List<ImputacionPago> getImputacionesEfectivas() { return new ArrayList<>(imputacionesEfectivas); }

    public double getDeudaTotal() {
        double total = 0;
        for (SaldoExpensa saldo : saldos) total += saldo.getTotal();
        return Validaciones.dinero(total);
    }
    public double getDeudaVencida() {
        double total = 0;
        for (SaldoExpensa saldo : saldos) {
            if (saldo.getEstado() == EstadoExpensa.VENCIDA) total += saldo.getTotal();
        }
        return Validaciones.dinero(total);
    }
    public long getPeriodosImpagos() {
        long cantidad = 0;
        for (SaldoExpensa saldo : saldos) {
            if (saldo.getEstado() == EstadoExpensa.VENCIDA) cantidad++;
        }
        return cantidad;
    }
    @Override public String toString() {
        String texto = "";
        for (SaldoExpensa saldo : saldos) texto += saldo + "\n";
        return texto + "Deuda total: $" + getDeudaTotal();
    }
}
