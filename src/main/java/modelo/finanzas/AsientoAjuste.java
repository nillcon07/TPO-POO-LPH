package modelo.finanzas;

public final class AsientoAjuste implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String fecha, motivo;
    private final double montoAjuste;
    private final Pago pagoCorregido;
    AsientoAjuste(String fecha, double montoAjuste, String motivo, Pago pagoCorregido) {
        modelo.comun.Fechas.fecha(fecha);
        modelo.comun.Validaciones.exigir(modelo.comun.Fechas.compararFechas(fecha, pagoCorregido.getFecha()) >= 0, "El ajuste no puede preceder al pago.");
        this.fecha = fecha;
        this.montoAjuste = modelo.comun.Validaciones.dinero(montoAjuste);
        modelo.comun.Validaciones.exigir(this.montoAjuste != 0, "El ajuste debe ser distinto de cero.");
        this.motivo = modelo.comun.Validaciones.texto(motivo, "Motivo del ajuste");
        this.pagoCorregido = pagoCorregido;
    }
    public String getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public double getMontoAjuste() { return montoAjuste; }
    public Pago getPagoCorregido() { return pagoCorregido; }
}
