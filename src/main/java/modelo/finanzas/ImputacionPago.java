package modelo.finanzas;

public final class ImputacionPago implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Pago pago;
    private final Expensa expensa;
    private final double montoInteres, montoCapital;
    ImputacionPago(Pago pago, Expensa expensa, double montoInteres, double montoCapital) {
        modelo.comun.Validaciones.exigir(pago.getLote() == expensa.getLote(), "Pago y expensa deben pertenecer al mismo lote.");
        this.pago = pago;
        this.expensa = expensa;
        this.montoInteres = montoInteres;
        this.montoCapital = montoCapital;
    }
    public Pago getPago() { return pago; }
    public Expensa getExpensa() { return expensa; }
    public double getMontoInteres() { return montoInteres; }
    public double getMontoCapital() { return montoCapital; }
}
