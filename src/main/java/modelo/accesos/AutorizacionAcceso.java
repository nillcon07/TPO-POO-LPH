package modelo.accesos;

import java.io.Serializable;

import modelo.personas.Persona;
import modelo.personas.TipoVisitante;
import modelo.personas.VisitanteProveedor;
import modelo.comun.*;
import modelo.comunidad.Lote;
import modelo.personas.*;

//Representa una autorización de acceso emitida para una visita o proveedor a un lote y permite validar la vigencia temporal y las reglas de negocio del barrio.

public class AutorizacionAcceso implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final String fechaInicio, fechaFin, horaDesde, horaHasta;
    private final boolean esRecurrente;
    private final Persona personaAutoriza;
    private final VisitanteProveedor personaAutorizada;
    private final Lote loteVisitado;
    public AutorizacionAcceso(int numero, Persona autoriza, VisitanteProveedor visitante,
                              Lote lote, String inicio, String fin,
                              String desde, String hasta, boolean recurrente) {
        modelo.comun.Validaciones.exigir(numero > 0, "Número de autorización inválido.");
        modelo.comun.Validaciones.requerido(autoriza, "Autorizante");
        modelo.comun.Validaciones.requerido(visitante, "Visitante");
        modelo.comun.Validaciones.requerido(lote, "Lote");

        modelo.comun.Validaciones.exigir(lote.puedeAutorizarVisita(autoriza),
                "Solo el titular o un residente registrado puede autorizar visitas.");

        long dias = modelo.comun.Fechas.dias(inicio, fin);
        modelo.comun.Validaciones.exigir(dias >= 0, "La vigencia de la autorización está invertida.");
        modelo.comun.Validaciones.exigir(modelo.comun.Fechas.compararHoras(desde, hasta) <= 0, "La franja horaria no puede cruzar la medianoche.");

        if (recurrente) {
            modelo.comun.Validaciones.exigir(visitante.getTipo() == TipoVisitante.PROVEEDOR && visitante.isProveedorHabitual(),
                    "Una autorización recurrente requiere un proveedor habitual.");
            modelo.comun.Validaciones.exigir(dias + 1 <= 30, "La vigencia recurrente máxima es de treinta días inclusive.");
        } else {
            modelo.comun.Validaciones.exigir(dias == 0, "Una visita no recurrente debe corresponder a un solo día.");
        }

        this.numero = numero;
        this.personaAutoriza = autoriza;
        this.personaAutorizada = visitante;
        this.loteVisitado = lote;
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        this.horaDesde = desde;
        this.horaHasta = hasta;
        this.esRecurrente = recurrente;
    }
    // Evalúa si la autorización se encuentra activa para una fecha y hora específicas. param fechaHora Cadena con formato 'YYYY-MM-DD HH:MM'. return true si la autorización cumple con el rango de fechas, horas y permisos actuales.
    public boolean estaVigente(String fechaHora) {
        modelo.comun.Fechas.momento(fechaHora);
        String fecha = fechaHora.substring(0, 10);
        String hora = fechaHora.substring(11, 16);
        boolean recurrenteValida = !esRecurrente || personaAutorizada.getTipo() == TipoVisitante.PROVEEDOR && personaAutorizada.isProveedorHabitual();
        return recurrenteValida && loteVisitado.puedeAutorizarVisita(personaAutoriza)
                && modelo.comun.Fechas.compararFechas(fecha, fechaInicio) >= 0 && modelo.comun.Fechas.compararFechas(fecha, fechaFin) <= 0
                && modelo.comun.Fechas.compararHoras(hora, horaDesde) >= 0 && modelo.comun.Fechas.compararHoras(hora, horaHasta) <= 0;
    }
    public int getNumero() { return numero; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public String getHoraDesde() { return horaDesde; }
    public String getHoraHasta() { return horaHasta; }
    public boolean isRecurrente() { return esRecurrente; }
    public Persona getPersonaAutoriza() { return personaAutoriza; }
    public VisitanteProveedor getPersonaAutorizada() { return personaAutorizada; }
    public Lote getLoteVisitado() { return loteVisitado; }
    @Override public String toString() { return "Autorización " + numero + " | " + personaAutorizada + " | lote " + loteVisitado.getNumero() + " | " + fechaInicio + " a " + fechaFin + " " + horaDesde + "–" + horaHasta; }
}
