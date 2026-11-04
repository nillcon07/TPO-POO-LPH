package modelo.finanzas;

import java.io.Serializable;
import java.util.*;
import modelo.comunidad.Lote;
import modelo.comun.*;

public class Pago implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final String fecha;
    private final double importeTotal;
    private final MedioPago medioPago;
    private final Lote lote;
    private final List<ImputacionPago> imputaciones = new ArrayList<>();
    Pago(int numero, String fecha, double importeTotal, MedioPago medioPago, Lote lote) {
        Fechas.fecha(fecha);
        this.numero = numero;
        this.fecha = fecha;
        this.importeTotal = Validaciones.dinero(Validaciones.positivo(importeTotal, "Pago"));
        Validaciones.exigir(this.importeTotal > 0, "El pago mínimo es un centavo.");
        this.medioPago = Validaciones.requerido(medioPago, "Medio de pago");
        this.lote = Validaciones.requerido(lote, "Lote");
    }
    void registrarImputaciones(List<ImputacionPago> nuevas) { imputaciones.addAll(nuevas); }
    public int getNumero() { return numero; }
    public String getFecha() { return fecha; }
    public double getImporteTotal() { return importeTotal; }
    public MedioPago getMedioPago() { return medioPago; }
    public Lote getLote() { return lote; }
    public List<ImputacionPago> getImputaciones() { return new ArrayList<>(imputaciones); }
    @Override public String toString() { return "Pago " + numero + " | " + fecha + " | $" + importeTotal + " | " + medioPago; }
}
