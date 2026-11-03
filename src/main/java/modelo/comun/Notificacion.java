package modelo.comun;

import java.io.Serializable;
import modelo.personas.Persona;

/** Constancia interna: no envía correos ni mensajes externos. */
public final class Notificacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String clave, fecha, motivo;
    private final Persona destinatario;
    private final double deudaInformada;
    public Notificacion(String clave, String fecha, Persona destinatario, String motivo, double deudaInformada) {
        Fechas.momento(fecha);
        this.clave = Validaciones.texto(clave, "Clave");
        this.fecha = fecha;
        this.destinatario = Validaciones.requerido(destinatario, "Destinatario");
        this.motivo = Validaciones.texto(motivo, "Motivo");
        this.deudaInformada = Validaciones.dinero(deudaInformada);
    }
    public String getClave() { return clave; }
    public String getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public Persona getDestinatario() { return destinatario; }
    public double getDeudaInformada() { return deudaInformada; }
    @Override public String toString() { return fecha + " | " + destinatario + " | " + motivo + (deudaInformada > 0 ? " | $" + deudaInformada : ""); }
}
