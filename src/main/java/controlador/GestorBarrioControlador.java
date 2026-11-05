package controlador;

import java.util.*;

import modelo.personas.Persona;
import modelo.comunidad.*;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.finanzas.*;
import modelo.mantenimiento.*;
import modelo.accesos.*;
import modelo.comun.*;
import modelo.repositorios.*;
import modelo.reportes.GeneradorReportes;


/** GRASP Controller: coordina; las reglas viven en el modelo y la E/S en repositorios. */
public class GestorBarrioControlador {
    private final Barrio barrio;
    private final RepositorioReclamos reclamos;
    private final RepositorioPersonas personas;
    private final RepositorioBarrio almacenamiento;
    private final LiquidadorExpensas liquidador = new LiquidadorExpensas();
    private final GeneradorReportes reportes = new GeneradorReportes();
    public GestorBarrioControlador(Barrio barrio, RepositorioReclamos reclamos, RepositorioPersonas personas, RepositorioBarrio almacenamiento) {
        this.barrio = Validaciones.requerido(barrio, "Barrio"); this.reclamos = Validaciones.requerido(reclamos, "Repositorio de reclamos");
        this.personas = Validaciones.requerido(personas, "Fuente de personas"); this.almacenamiento = Validaciones.requerido(almacenamiento, "Almacenamiento");
    }
    private <T> T confirmar(T resultado) { guardar(); return resultado; }
    private void admin(Persona actor) { barrio.exigirAdministrador(actor); }
    private void validarActor(Persona actor) {
        Validaciones.requerido(actor, "Actor");
        if (actor instanceof Personal) barrio.exigirPersonal((Personal) actor);
        else buscarPersona(actor.getDocumento());
    }
    private Persona buscarPersona(String documento) {
        return personas.buscarPorDocumento(documento);
    }
    public void guardar() { almacenamiento.guardar(barrio); }
    public String getNombreBarrio() { return barrio.getNombre(); }
    public List<Persona> listarPersonas() { return new ArrayList<>(personas.listar()); }
    public Persona agregarPersona(Persona actor, String documento, String nombre, String apellido, String telefono, String correo) {
        admin(actor);
        Persona nueva = new Persona(nombre, apellido, documento, telefono, correo);
        personas.agregarPersona(nueva);
        return nueva;
    }
    public List<Persona> listarActores() {
        Map<String, Persona> porDocumento = new LinkedHashMap<>();
        for (Persona p : personas.listar()) porDocumento.put(p.getDocumento(), p);
        for (Personal p : barrio.getPersonal()) porDocumento.put(p.getDocumento(), p);
        return new ArrayList<>(porDocumento.values());
    }
    public List<Lote> listarLotes() { return barrio.getLotes(); }
    public List<Personal> listarPersonal() { return barrio.getPersonal(); }
    public List<Equipo> listarEquipos() { return barrio.getEquipos(); }
    public List<Falla> listarFallas() { return barrio.getFallas(); }
    public List<OrdenMantenimiento> listarOrdenes() { return barrio.getOrdenes(); }
    public List<AutorizacionAcceso> listarAutorizaciones() { return barrio.getAutorizaciones(); }
    public List<RegistroAcceso> listarAccesos() { return barrio.getRegistrosAcceso(); }
    public List<Notificacion> listarNotificaciones(Persona actor) { admin(actor); return barrio.getNotificaciones(); }
    public Lote agregarLote(Persona actor, int numero, double superficie, String documentoTitular) {
        admin(actor); return confirmar(barrio.agregarLote(numero, superficie, buscarPersona(documentoTitular)));
    }
    public Residente agregarResidente(Persona actor, int lote, String documento, TipoResidente tipo) {
        admin(actor); return confirmar(barrio.buscarLote(lote).agregarResidente(buscarPersona(documento), tipo));
    }
    public void quitarResidente(Persona actor, int lote, String documento) {
        admin(actor); barrio.buscarLote(lote).quitarResidente(buscarPersona(documento)); guardar();
    }
    public void cambiarTitular(Persona actor, int lote, String documento) {
        admin(actor); barrio.buscarLote(lote).cambiarTitular(buscarPersona(documento)); guardar();
    }
    public void permisoReclamos(Persona titular, int lote, String documento, boolean autorizar) {
        validarActor(titular);
        Persona persona = buscarPersona(documento);
        if (autorizar) barrio.buscarLote(lote).autorizarReclamos(titular, persona);
        else barrio.buscarLote(lote).revocarReclamos(titular, persona);
        guardar();
    }
    public Guardia agregarGuardia(Persona actor, String documento, String legajo, String fecha, Turno turno, PuestoGuardia puesto) {
        admin(actor); return confirmar(barrio.agregarGuardia(buscarPersona(documento), legajo, fecha, turno, puesto));
    }
    public EmpleadoMantenimiento agregarEmpleado(Persona actor, String documento, String legajo, String fecha, Especialidad especialidad) {
        admin(actor); return confirmar(barrio.agregarEmpleado(buscarPersona(documento), legajo, fecha, especialidad));
    }
    public Reclamo registrarReclamo(Persona vecino, int lote, TipoReclamo tipo, String descripcion, String fecha) {
        validarActor(vecino);
        int numero = 1;
        for (Reclamo existente : reclamos.listarTodos()) {
            if (existente.getNumero() >= numero) numero = existente.getNumero() + 1;
        }
        Reclamo nuevo = new Reclamo(numero, fecha, tipo, descripcion, barrio.buscarLote(lote), vecino);
        reclamos.guardar(nuevo); barrio.guardarReclamo(nuevo);
        return confirmar(nuevo);
    }
    public List<Reclamo> listarReclamos(Persona actor) {
        validarActor(actor);
        List<Reclamo> resultado = new ArrayList<>();
        for (Reclamo reclamo : reclamos.listarTodos()) {
            if (actor instanceof Personal || reclamo.getLote().puedeReclamar(actor)) resultado.add(reclamo);
        }
        return resultado;
    }
    public Reclamo consultarReclamo(Persona actor, int numero) {
        validarActor(actor);
        Reclamo reclamo = reclamos.buscarPorNumero(numero);
        Validaciones.exigir(actor instanceof Personal || reclamo.getLote().puedeReclamar(actor), "No puede consultar ese reclamo.");
        return reclamo;
    }
    public void asignarReclamo(Persona actor, int numero, String legajo, String fecha) {
        admin(actor);
        Reclamo reclamo = reclamos.buscarPorNumero(numero);
        reclamo.asignar((Administrador) actor, barrio.buscarPersonal(legajo), fecha, reclamos.listarTodos());
        reclamos.guardar(reclamo); guardar();
    }
    public void actualizarEstado(Persona actor, int numero, EstadoReclamo estado, String fecha, String observacion) {
        validarActor(actor);
        Reclamo reclamo = reclamos.buscarPorNumero(numero);
        reclamo.actualizarEstado(actor, estado, fecha, observacion, reclamos.listarTodos());
        reclamos.guardar(reclamo); guardar();
    }
    public void reabrirReclamo(Persona vecino, int numero, String fecha, String observacion) {
        validarActor(vecino);
        Reclamo reclamo = reclamos.buscarPorNumero(numero);
        reclamo.reabrir(vecino, fecha, observacion); reclamos.guardar(reclamo); guardar();
    }
    public void agregarComentario(Persona actor, int numero, String fecha, String comentario) {
        validarActor(actor);
        Reclamo reclamo = reclamos.buscarPorNumero(numero);
        reclamo.agregarComentario(actor, fecha, comentario); reclamos.guardar(reclamo); guardar();
    }
    public List<Expensa> emitirExpensas(Persona actor, String periodo, double presupuesto) {
        admin(actor); return confirmar(liquidador.emitir(barrio.getLotes(), periodo, presupuesto, Fechas.hoy()));
    }
    public EstadoCuenta consultarCuenta(Persona actor, int numeroLote, String fecha) {
        validarActor(actor);
        Lote lote = barrio.buscarLote(numeroLote);
        Validaciones.exigir(actor instanceof Administrador || lote.getTitular().equals(actor), "Solo el titular o el administrador puede consultar la cuenta.");
        return lote.getCuenta().consultar(fecha);
    }
    public List<Pago> consultarPagos(Persona actor, int lote, String fecha) {
        consultarCuenta(actor, lote, fecha);
        List<Pago> resultado = new ArrayList<>();
        for (Pago pago : barrio.buscarLote(lote).getCuenta().getPagos()) {
            if (Fechas.compararFechas(pago.getFecha(), fecha) <= 0) resultado.add(pago);
        }
        return resultado;
    }
    public List<AsientoAjuste> consultarAjustes(Persona actor, int lote, String fecha) {
        consultarCuenta(actor, lote, fecha);
        List<AsientoAjuste> resultado = new ArrayList<>();
        for (AsientoAjuste ajuste : barrio.buscarLote(lote).getCuenta().getAjustes()) {
            if (Fechas.compararFechas(ajuste.getFecha(), fecha) <= 0) resultado.add(ajuste);
        }
        return resultado;
    }
    public Pago registrarPago(Persona actor, int lote, String fecha, double importe, MedioPago medio) {
        admin(actor);
        Pago pago = barrio.buscarLote(lote).getCuenta().registrarPago(fecha, importe, medio);
        barrio.verificarControles(Fechas.ahora()); return confirmar(pago);
    }
    public AsientoAjuste ajustarPago(Persona actor, int lote, int pago, String fecha, double monto, String motivo) {
        admin(actor);
        AsientoAjuste ajuste = barrio.buscarLote(lote).getCuenta().ajustar(pago, fecha, monto, motivo);
        barrio.verificarControles(Fechas.ahora()); return confirmar(ajuste);
    }
    public Equipo agregarCamara(Persona actor, String codigo, String sector, String fecha, String resolucion, int dias) {
        admin(actor); return confirmar(barrio.agregarEquipo(new Camara(codigo, new Sector(sector), fecha, resolucion, dias)));
    }
    public Equipo agregarLuminaria(Persona actor, String codigo, String sector, String fecha, String lampara, double potencia) {
        admin(actor); return confirmar(barrio.agregarEquipo(new Luminaria(codigo, new Sector(sector), fecha, lampara, potencia)));
    }
    public void marcarEquipoFueraServicio(Persona actor, String codigo) { admin(actor); barrio.buscarEquipo(codigo).marcarFueraServicio(); guardar(); }
    public Falla registrarFalla(Persona actor, String equipo, String fecha, String inicio, String descripcion) {
        validarActor(actor); return confirmar(barrio.registrarFalla(equipo, actor, fecha, inicio, descripcion));
    }
    public OrdenMantenimiento generarOrden(Persona actor, int falla, String fecha, String trabajo) {
        admin(actor); return confirmar(barrio.generarOrden(falla, fecha, trabajo));
    }
    public void asignarOrden(Persona actor, int numero, String legajo) {
        admin(actor); Personal personal = barrio.buscarPersonal(legajo);
        Validaciones.exigir(personal instanceof EmpleadoMantenimiento, "Debe asignar un empleado de mantenimiento.");
        barrio.buscarOrden(numero).asignar((EmpleadoMantenimiento) personal); guardar();
    }
    public void cerrarOrden(Persona actor, int numero, String fecha, String detalle) {
        validarActor(actor); OrdenMantenimiento orden = barrio.buscarOrden(numero);
        Validaciones.exigir(actor instanceof Administrador || actor == orden.getEmpleadoAsignado(), "Solo el técnico asignado o el administrador puede cerrar la orden.");
        orden.cerrar(fecha, detalle); guardar();
    }
    public VisitanteProveedor identificarVisitante(Persona actor, String documento, TipoVisitante tipo, boolean habitual) {
        validarActor(actor);
        if (habitual) admin(actor);
        VisitanteProveedor visitante = new VisitanteProveedor(buscarPersona(documento), tipo, habitual);
        if (habitual) { barrio.registrarProveedorHabitual(documento); guardar(); }
        return visitante;
    }
    public AutorizacionAcceso autorizarVisita(Persona actor, String documento, TipoVisitante tipo, int lote, String inicio, String fin, String desde, String hasta, boolean recurrente) {
        validarActor(actor);
        boolean habitual = barrio.esProveedorHabitual(documento);
        VisitanteProveedor visitante = new VisitanteProveedor(buscarPersona(documento), tipo, habitual && tipo == TipoVisitante.PROVEEDOR);
        return confirmar(barrio.autorizarVisita(actor, visitante, lote, inicio, fin, desde, hasta, recurrente));
    }
    public AutorizacionAcceso autorizarVisita(Persona actor, VisitanteProveedor visitante, int lote, String inicio, String fin, String desde, String hasta, boolean recurrente) {
        validarActor(actor); buscarPersona(visitante.getDocumento());
        Validaciones.exigir(!visitante.isProveedorHabitual() || barrio.esProveedorHabitual(visitante.getDocumento()), "El proveedor habitual debe ser registrado por el administrador.");
        return confirmar(barrio.autorizarVisita(actor, visitante, lote, inicio, fin, desde, hasta, recurrente));
    }
    public List<AutorizacionAcceso> buscarAutorizaciones(Guardia actor, String documento, String fecha) { barrio.exigirPersonal(actor); return barrio.autorizacionesVigentes(documento, fecha); }
    public RegistroAcceso registrarIngreso(Guardia actor, int autorizacion, String fecha) { return confirmar(barrio.registrarIngreso(actor, autorizacion, fecha)); }
    public RegistroAcceso registrarEgreso(Guardia actor, String documento, String fecha) { return confirmar(barrio.registrarEgreso(actor, documento, fecha)); }
    public void verificarControles(String fecha) { barrio.verificarControles(fecha); guardar(); }
    public String generarReportes(Persona actor, String fecha) {
        admin(actor); verificarControles(fecha);
        String texto = "RECLAMOS PENDIENTES\n";
        for (Reclamo reclamo : reportes.reclamosPendientes(barrio)) texto += reclamo + "\n";
        texto += "\nMOROSIDAD POR LOTE\n";
        for (Lote lote : reportes.morosidad(barrio)) {
            texto += "Lote " + lote.getNumero() + " | " + lote.getTitular() + " | deuda vencida $"
                    + lote.getCuenta().consultar(Fechas.dia(fecha)).getDeudaVencida() + "\n";
        }
        texto += "\nEQUIPOS FUERA DE SERVICIO\n";
        for (Equipo equipo : reportes.equiposFueraServicio(barrio)) texto += equipo + "\n";
        texto += "\nPromedio de resolución de mantenimiento (horas): " + Validaciones.dinero(reportes.promedioHorasResolucion(barrio));
        texto += "\n\nNOTIFICACIONES REGISTRADAS\n";
        for (Notificacion notificacion : barrio.getNotificaciones()) texto += notificacion + "\n";
        return texto;
    }
}
