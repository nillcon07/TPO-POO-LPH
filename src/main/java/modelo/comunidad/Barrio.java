package main.java.modelo.comunidad;

import java.io.Serializable;
import java.util.*;
import modelo.personas.*;
import modelo.comun.*;
import modelo.reclamos.*;
import modelo.mantenimiento.*;
import modelo.accesos.*;


/** Raíz del dominio: conserva las identidades y relaciones del barrio. */
public class Barrio implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final List<Lote> lotes = new ArrayList<>();
    private final List<Personal> personal = new ArrayList<>();
    private final List<Equipo> equipos = new ArrayList<>();
    private final List<Reclamo> reclamos = new ArrayList<>();
    private final List<AutorizacionAcceso> autorizaciones = new ArrayList<>();
    private final List<RegistroAcceso> registrosAcceso = new ArrayList<>();
    private final List<Notificacion> notificaciones = new ArrayList<>();
    private final Set<String> proveedoresHabituales = new HashSet<>();
    private final Administrador administrador;

    public Barrio(String nombre, Administrador administrador) {
        this.nombre = Validaciones.texto(nombre, "Nombre del barrio");
        this.administrador = Validaciones.requerido(administrador, "Administrador");
        personal.add(administrador);
    }

    public void exigirAdministrador(Persona actor) {
        Validaciones.exigir(actor instanceof Administrador && administrador == actor, "Se requiere el administrador del barrio.");
    }

    public void exigirPersonal(Personal actor) {
        for (Personal integrante : personal) {
            if (integrante == actor) return;
        }
        throw new ReglaNegocioException("El personal no pertenece al barrio.");
    }

    public Lote agregarLote(int numero, double superficie, Persona titular) {
        Validaciones.exigir(lotes.size() < 150, "El barrio admite un máximo de 150 lotes.");
        for (Lote lote : lotes) Validaciones.exigir(lote.getNumero() != numero, "Número de lote duplicado.");
        Lote lote = new Lote(numero, superficie, titular);
        lotes.add(lote);
        return lote;
    }

    private void validarPersonal(Persona persona, String legajo) {
        Validaciones.requerido(persona, "Persona");
        for (Personal integrante : personal) {
            Validaciones.exigir(!integrante.getLegajo().equals(legajo) && !integrante.equals(persona),
                    "Legajo o persona ya registrados como personal.");
        }
    }

    public Guardia agregarGuardia(Persona persona, String legajo, String fecha, Turno turno, PuestoGuardia puesto) {
        validarPersonal(persona, legajo);
        Guardia guardia = new Guardia(persona, legajo, fecha, turno, puesto);
        personal.add(guardia);
        return guardia;
    }

    public EmpleadoMantenimiento agregarEmpleado(Persona persona, String legajo, String fecha, Especialidad especialidad) {
        validarPersonal(persona, legajo);
        EmpleadoMantenimiento empleado = new EmpleadoMantenimiento(persona, legajo, fecha, especialidad);
        personal.add(empleado);
        return empleado;
    }

    public Equipo agregarEquipo(Equipo equipo) {
        Validaciones.requerido(equipo, "Equipo");
        for (Equipo existente : equipos) Validaciones.exigir(!existente.getCodigo().equalsIgnoreCase(equipo.getCodigo()), "Código de equipo duplicado.");
        equipos.add(equipo);
        return equipo;
    }

    public Lote buscarLote(int numero) {
        for (Lote lote : lotes) if (lote.getNumero() == numero) return lote;
        throw new ReglaNegocioException("Lote inexistente.");
    }
    public Personal buscarPersonal(String legajo) {
        for (Personal integrante : personal) if (integrante.getLegajo().equals(legajo)) return integrante;
        throw new ReglaNegocioException("Legajo inexistente.");
    }
    public Equipo buscarEquipo(String codigo) {
        for (Equipo equipo : equipos) if (equipo.getCodigo().equalsIgnoreCase(codigo)) return equipo;
        throw new ReglaNegocioException("Equipo inexistente.");
    }
    public Falla buscarFalla(int numero) {
        for (Falla falla : getFallas()) if (falla.getNumero() == numero) return falla;
        throw new ReglaNegocioException("Falla inexistente.");
    }
    public OrdenMantenimiento buscarOrden(int numero) {
        for (OrdenMantenimiento orden : getOrdenes()) if (orden.getNumero() == numero) return orden;
        throw new ReglaNegocioException("Orden inexistente.");
    }

    public void guardarReclamo(Reclamo reclamo) {
        Validaciones.exigir(lotes.contains(reclamo.getLote()), "El lote del reclamo no pertenece al barrio.");
        for (Reclamo existente : reclamos) {
            if (existente.getNumero() == reclamo.getNumero()) {
                Validaciones.exigir(existente == reclamo, "Número de reclamo duplicado.");
                return;
            }
        }
        reclamos.add(reclamo);
    }

    public boolean esVecino(Persona persona) {
        for (Lote lote : lotes) if (lote.puedeAutorizarVisita(persona)) return true;
        return false;
    }
    public void registrarProveedorHabitual(String documento) { proveedoresHabituales.add(Validaciones.texto(documento, "Documento")); }
    public boolean esProveedorHabitual(String documento) { return proveedoresHabituales.contains(documento); }

    public Falla registrarFalla(String codigo, Persona informante, String fecha, String inicio, String descripcion) {
        boolean habilitado = esVecino(informante);
        if (informante instanceof Guardia) {
            for (Personal integrante : personal) if (integrante == informante) habilitado = true;
        }
        Validaciones.exigir(habilitado, "Solo un titular, residente o guardia puede informar fallas.");
        int numero = 1;
        for (Falla existente : getFallas()) {
            if (existente.getNumero() >= numero) numero = existente.getNumero() + 1;
        }
        Falla falla = buscarEquipo(codigo).registrarFalla(numero, fecha, inicio, descripcion, informante);
        actualizarPrioridadSector(falla.getEquipo().getSector());
        return falla;
    }

    public OrdenMantenimiento generarOrden(int fallaNumero, String fecha, String trabajo) {
        Falla falla = buscarFalla(fallaNumero);
        int numero = 1;
        for (OrdenMantenimiento existente : getOrdenes()) {
            if (existente.getNumero() >= numero) numero = existente.getNumero() + 1;
        }
        OrdenMantenimiento orden = falla.generarOrden(numero, fecha, trabajo);
        actualizarPrioridadSector(falla.getEquipo().getSector());
        return orden;
    }

    private void actualizarPrioridadSector(Sector sector) {
        int cantidad = 0;
        for (Equipo equipo : equipos) {
            if (equipo instanceof Luminaria && equipo.getSector().equals(sector) && equipo.tieneFallaActiva()) cantidad++;
        }
        if (cantidad > 3) {
            for (OrdenMantenimiento orden : getOrdenes()) {
                if (orden.getEquipoAfectado() instanceof Luminaria && orden.getEquipoAfectado().getSector().equals(sector)
                        && orden.getFallaOrigen().getFechaRestablecimiento() == null) orden.elevarPrioridad();
            }
        }
    }

    public AutorizacionAcceso autorizarVisita(Persona vecino, VisitanteProveedor visitante, int lote, String inicio, String fin, String desde, String hasta, boolean recurrente) {
        AutorizacionAcceso nueva = new AutorizacionAcceso(autorizaciones.size() + 1, vecino, visitante, buscarLote(lote), inicio, fin, desde, hasta, recurrente);
        autorizaciones.add(nueva);
        return nueva;
    }

    public List<AutorizacionAcceso> autorizacionesVigentes(String documento, String fecha) {
        List<AutorizacionAcceso> vigentes = new ArrayList<>();
        for (AutorizacionAcceso autorizacion : autorizaciones) {
            if (autorizacion.getPersonaAutorizada().getDocumento().equals(documento) && autorizacion.estaVigente(fecha)) vigentes.add(autorizacion);
        }
        return vigentes;
    }

    public RegistroAcceso registrarIngreso(Guardia guardia, int numeroAutorizacion, String fecha) {
        exigirPersonal(guardia);
        AutorizacionAcceso autorizacion = null;
        for (AutorizacionAcceso candidata : autorizaciones) {
            if (candidata.getNumero() == numeroAutorizacion) { autorizacion = candidata; break; }
        }
        Validaciones.exigir(autorizacion != null, "Autorización inexistente.");
        Persona visitante = autorizacion.getPersonaAutorizada();
        for (RegistroAcceso registro : registrosAcceso) {
            if (registro.getAutorizacion().getPersonaAutorizada().equals(visitante)) {
                Validaciones.exigir(registro.getFechaHoraEgreso() != null, "La persona ya tiene un ingreso sin egreso.");
                Fechas.noAnterior(registro.getFechaHoraEgreso(), fecha);
            }
        }
        RegistroAcceso nuevo = new RegistroAcceso(fecha, autorizacion, guardia);
        registrosAcceso.add(nuevo);
        return nuevo;
    }

    public RegistroAcceso registrarEgreso(Guardia guardia, String documento, String fecha) {
        exigirPersonal(guardia);
        Validaciones.exigir(guardia.getPuestoAsignado() == PuestoGuardia.PORTERIA, "El guardia debe pertenecer a portería.");
        for (RegistroAcceso registro : registrosAcceso) {
            if (registro.getFechaHoraEgreso() == null && registro.getAutorizacion().getPersonaAutorizada().getDocumento().equals(documento)) {
                registro.registrarEgreso(fecha);
                return registro;
            }
        }
        throw new ReglaNegocioException("La persona no tiene un ingreso abierto.");
    }

    private void notificar(Notificacion notificacion) {
        if (notificacion == null) return;
        for (Notificacion anterior : notificaciones) {
            if (anterior.getClave().equals(notificacion.getClave())) return;
        }
        notificaciones.add(notificacion);
    }

    public void verificarControles(String fecha) {
        Fechas.momento(fecha);
        for (Reclamo reclamo : reclamos) {
            if (reclamo.requiereEscalamiento(fecha)) notificar(new Notificacion("RECLAMO-" + reclamo.getNumero(), fecha, administrador, "Reclamo ALTA sin atención en 24 horas: " + reclamo.getNumero(), 0));
            if (reclamo.puedeCerrar(fecha)) reclamo.actualizarEstado(administrador, EstadoReclamo.CERRADO, fecha, "Cierre automático: venció el plazo de reapertura", reclamos);
        }
        for (Equipo equipo : equipos) {
            if (equipo.requiereAlertaInterrupcion(fecha)) notificar(new Notificacion("CAMARA-" + equipo.getCodigo() + "-" + equipo.getNumeroInterrupcion(), fecha, administrador, "Cámara sin funcionar por más de 48 horas: " + equipo.getCodigo(), 0));
        }
        for (Lote lote : lotes) notificar(lote.getCuenta().verificarMorosidad(fecha));
    }

    public String getNombre() { return nombre; }
    public Administrador getAdministrador() { return administrador; }
    public List<Lote> getLotes() { return new ArrayList<>(lotes); }
    public List<Personal> getPersonal() { return new ArrayList<>(personal); }
    public List<Equipo> getEquipos() { return new ArrayList<>(equipos); }
    public List<Reclamo> getReclamos() { return new ArrayList<>(reclamos); }
    public List<AutorizacionAcceso> getAutorizaciones() { return new ArrayList<>(autorizaciones); }
    public List<RegistroAcceso> getRegistrosAcceso() { return new ArrayList<>(registrosAcceso); }
    public List<Notificacion> getNotificaciones() { return new ArrayList<>(notificaciones); }

    public List<Falla> getFallas() {
        List<Falla> resultado = new ArrayList<>();
        for (Equipo equipo : equipos) resultado.addAll(equipo.getFallas());
        return resultado;
    }

    public List<OrdenMantenimiento> getOrdenes() {
        List<OrdenMantenimiento> resultado = new ArrayList<>();
        for (Falla falla : getFallas()) {
            if (falla.getOrden() != null) resultado.add(falla.getOrden());
        }
        return resultado;
    }
}
