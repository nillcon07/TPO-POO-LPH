package modelo.comunidad;

import java.io.Serializable;
import java.util.*;
import modelo.comun.Validaciones;
import modelo.personas.*;
import modelo.finanzas.CuentaLote;

public class Lote implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final double superficie;
    private Persona titular;
    private final List<Persona> personasAutorizadasReclamo = new ArrayList<>();
    private final List<Residente> residentes = new ArrayList<>();
    private final CuentaLote cuenta;
    public Lote(int numero, double superficie, Persona titular) {
        Validaciones.exigir(numero > 0, "El número de lote debe ser positivo.");
        this.numero = numero;
        this.superficie = Validaciones.positivo(superficie, "Superficie");
        this.titular = Validaciones.requerido(titular, "Titular");
        this.cuenta = new CuentaLote(this);
    }
    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public Persona getTitular() { return titular; }
    public CuentaLote getCuenta() { return cuenta; }
    public List<Residente> getResidentes() { return new ArrayList<>(residentes); }
    public List<Persona> getPersonasAutorizadasReclamo() { return new ArrayList<>(personasAutorizadasReclamo); }
    public boolean puedeReclamar(Persona persona) { return titular.equals(persona) || personasAutorizadasReclamo.contains(persona); }
    public boolean puedeAutorizarVisita(Persona persona) { return titular.equals(persona) || residentes.contains(persona); }
    public void autorizarReclamos(Persona solicitante, Persona persona) {
        Validaciones.exigir(titular.equals(solicitante), "Solo el titular puede autorizar reclamos.");
        Validaciones.requerido(persona, "Persona");
        if (!personasAutorizadasReclamo.contains(persona)) personasAutorizadasReclamo.add(persona);
    }
    public void revocarReclamos(Persona solicitante, Persona persona) {
        Validaciones.exigir(titular.equals(solicitante), "Solo el titular puede revocar permisos.");
        personasAutorizadasReclamo.remove(persona);
    }
    public Residente agregarResidente(Persona persona, TipoResidente tipo) {
        Validaciones.exigir(!residentes.contains(persona), "La persona ya reside en este lote.");
        Residente residente = new Residente(persona, this, tipo);
        residentes.add(residente);
        return residente;
    }
    public void quitarResidente(Persona persona) {
        Validaciones.exigir(residentes.remove(persona), "La persona no reside en este lote.");
    }
    public void cambiarTitular(Persona titular) {
        this.titular = Validaciones.requerido(titular, "Titular");
        personasAutorizadasReclamo.clear(); // Los permisos del titular anterior caducan.
    }
    @Override public String toString() { return "Lote " + numero + " — " + titular; }
}
