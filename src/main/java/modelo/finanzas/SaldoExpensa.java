package modelo.finanzas;

import modelo.comun.*;

/** Resultado de una consulta; nunca reemplaza los movimientos originales. */
public final class SaldoExpensa {
    private final Expensa expensa;
    private final double capital, intereses, reconocido;
    private final String fecha;
    SaldoExpensa(Expensa expensa, double capital, double intereses, double reconocido, String fecha) {
        this.expensa = expensa;
        this.capital = Validaciones.dinero(capital);
        this.intereses = Validaciones.dinero(intereses);
        this.reconocido = reconocido;
        this.fecha = fecha;
    }
    public Expensa getExpensa() { return expensa; }
    public double getCapital() { return capital; }
    public double getIntereses() { return intereses; }
    public double getTotal() { return Validaciones.dinero(capital + intereses); }
    public EstadoExpensa getEstado() {
        if (getTotal() == 0) return EstadoExpensa.PAGADA;
        if (Fechas.compararFechas(fecha, expensa.getFechaVencimiento()) > 0) return EstadoExpensa.VENCIDA;
        return reconocido > 0 ? EstadoExpensa.PARCIAL : EstadoExpensa.PENDIENTE;
    }
    @Override public String toString() { return expensa.getPeriodo() + " | " + getEstado() + " | capital $" + capital + " | intereses $" + intereses + " | total $" + getTotal(); }
}
