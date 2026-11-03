import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import controlador.GestorBarrioControlador;
import modelo.archivos.*;
import principal.DatosDemo;
import modelo.comunidad.*;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.finanzas.*;
import modelo.mantenimiento.*;
import modelo.accesos.*;
import modelo.comun.*;
import modelo.repositorios.*;

/** Pruebas ejecutables sin descargas ni framework; una falla produce código de salida distinto de cero. */
public final class PruebasAceptacion {
    private static int correctas, fallidas, comprobaciones;
    private static String informe = "";
    private interface Prueba { void ejecutar() throws Exception; }
    private static void probar(String nombre, Prueba prueba) {
        try { prueba.ejecutar(); correctas++; registrar("OK  " + nombre); }
        catch (Exception error) { fallidas++; registrar("ERROR  " + nombre + ": " + error); error.printStackTrace(); }
    }
    private static void registrar(String linea) { System.out.println(linea); informe += linea + "\n"; }
    private static void exigir(boolean condicion, String mensaje) { comprobaciones++; if (!condicion) throw new IllegalStateException(mensaje); }
    private static void igual(double esperado, double real) {
        double diferencia = esperado - real;
        exigir(diferencia > -0.00001 && diferencia < 0.00001, "Esperado " + esperado + ", recibido " + real);
    }
    private static void rechaza(Runnable operacion) {
        comprobaciones++;
        try { operacion.run(); } catch (ReglaNegocioException e) { return; }
        throw new IllegalStateException("Se esperaba un rechazo de regla de negocio.");
    }

    private static Path prepararArchivo(String nombre) throws IOException {
        Path carpeta = Paths.get("target", "pruebas");
        Files.createDirectories(carpeta);
        Path archivo = Paths.get(carpeta.toString(), nombre);
        if (Files.exists(archivo)) Files.delete(archivo);
        return archivo;
    }
    private static final class Contexto {
        Contexto() { Fechas.establecerMomento("2026-11-03 10:00"); }
        final RepositorioPersonas personas = new RepositorioPersonasMemoria(new RepositorioPersonasCsv(Paths.get("datos/personas-demo.csv")).listar());
        final Barrio barrio = DatosDemo.crear(personas);
        final Administrador admin = barrio.getAdministrador();
        final Persona titular = personas.buscarPorDocumento("101"), residente = personas.buscarPorDocumento("103");
        final Guardia guardia = (Guardia) barrio.buscarPersonal("G001");
        final EmpleadoMantenimiento tecnico = (EmpleadoMantenimiento) barrio.buscarPersonal("M001");
        final Lote lote = barrio.buscarLote(1);
        final GestorBarrioControlador c = new GestorBarrioControlador(barrio, new RepositorioReclamosMemoria(barrio), personas, new RepositorioBarrio() {
            public Barrio cargar() { return barrio; }
            public void guardar(Barrio valor) { }
        });
        Reclamo reclamo(TipoReclamo tipo) { return c.registrarReclamo(titular, 1, tipo, "Descripción de prueba", "2026-01-01 09:00"); }
        void resolver(Reclamo r, String dia) {
            c.asignarReclamo(admin, r.getNumero(), "M001", dia + " 09:00");
            c.actualizarEstado(tecnico, r.getNumero(), EstadoReclamo.EN_CURSO, dia + " 10:00", "Se inicia la atención");
            c.actualizarEstado(tecnico, r.getNumero(), EstadoReclamo.RESUELTO, dia + " 11:00", "Se reparó el problema");
        }
    }
    public static void main(String[] args) throws Exception {
        probar("Identidad de personas y colecciones protegidas", () -> {
            Contexto x = new Contexto();
            exigir(x.guardia.equals(x.personas.buscarPorDocumento("104")), "Identidad por documento");
            List<Lote> copia = x.barrio.getLotes();
            copia.clear();
            exigir(x.barrio.getLotes().size() == 2, "Modificar la copia no altera la colección interna.");
            List<Persona> catalogo = x.c.listarPersonas();
            catalogo.clear();
            exigir(x.c.listarPersonas().size() == 9, "El catálogo del controlador devuelve una copia.");
            List<Persona> personasFuente = x.personas.listar();
            personasFuente.clear();
            exigir(x.personas.listar().size() == 9, "La fuente de personas devuelve una copia.");
            exigir(x.residente.equals(x.lote.getResidentes().get(0)), "Herencia de Persona");
            exigir(x.lote.getResidentes().get(0).getLote() == x.lote, "Relación bidireccional del residente");
        });
        probar("Residente no recibe permiso de reclamo automáticamente", () -> {
            Contexto x = new Contexto();
            rechaza(() -> x.c.registrarReclamo(x.residente, 1, TipoReclamo.MANTENIMIENTO, "Lámpara", "2026-01-01 10:00"));
            x.c.permisoReclamos(x.titular, 1, "103", true);
            x.c.registrarReclamo(x.residente, 1, TipoReclamo.MANTENIMIENTO, "Lámpara", "2026-01-01 10:00");
            x.c.permisoReclamos(x.titular, 1, "103", false);
            rechaza(() -> x.c.permisoReclamos(x.admin, 1, "103", true));
            rechaza(() -> x.c.registrarReclamo(x.residente, 1, TipoReclamo.SEGURIDAD, "Puerta", "2026-01-02 10:00"));
            igual(1, x.barrio.getReclamos().size());
        });
        probar("Lotes: titular múltiple, unicidad y límite de 150", () -> {
            Contexto x = new Contexto();
            for (int n = 3; n <= 150; n++) x.c.agregarLote(x.admin, n, 100, "101");
            rechaza(() -> x.c.agregarLote(x.admin, 151, 100, "101"));
            rechaza(() -> x.c.agregarLote(x.admin, 1, 100, "101"));
            igual(150, x.barrio.getLotes().size());
            Contexto y = new Contexto();
            rechaza(() -> y.c.agregarLote(y.admin, 3, -10, "101"));
            rechaza(() -> y.c.agregarLote(y.admin, 3, 100, "INEXISTENTE"));
            rechaza(() -> y.c.agregarLote(y.titular, 3, 100, "101"));
        });
        probar("Cambio de titular revoca permisos del titular anterior", () -> {
            Contexto x = new Contexto(); x.c.permisoReclamos(x.titular, 1, "103", true);
            x.c.cambiarTitular(x.admin, 1, "102");
            exigir(!x.lote.puedeReclamar(x.titular) && !x.lote.puedeReclamar(x.residente), "Permisos revocados");
            exigir(x.lote.getResidentes().size() == 1, "Los residentes se conservan");
        });
        probar("Registro, prioridad e historial inicial de reclamo", () -> {
            Contexto x = new Contexto();
            for (TipoReclamo tipo : TipoReclamo.values()) {
                Reclamo r = x.reclamo(tipo); exigir(r.getEstado() == EstadoReclamo.ABIERTO, "Estado inicial");
                exigir(r.getPrioridad() == tipo.getPrioridad(), "Prioridad por tipo"); igual(1, r.getCambiosEstado().size());
            }
            rechaza(() -> x.c.registrarReclamo(x.titular, 1, TipoReclamo.MANTENIMIENTO, " ", "2026-01-01 10:00"));
        });
        probar("Transiciones, asignación por especialidad y autorización de actor", () -> {
            Contexto x = new Contexto(); Reclamo r = x.reclamo(TipoReclamo.MANTENIMIENTO);
            rechaza(() -> x.c.asignarReclamo(x.admin, r.getNumero(), "G001", "2026-01-01 10:00"));
            rechaza(() -> x.c.actualizarEstado(x.admin, r.getNumero(), EstadoReclamo.RESUELTO, "2026-01-01 10:00", "Salto"));
            x.c.asignarReclamo(x.admin, r.getNumero(), "M001", "2026-01-01 10:00");
            rechaza(() -> x.c.actualizarEstado(x.guardia, r.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-01 11:00", "Sin permiso"));
            rechaza(() -> x.c.actualizarEstado(x.tecnico, r.getNumero(), EstadoReclamo.EN_CURSO, "2025-12-31 11:00", "Fecha anterior"));
            exigir(r.getEstado() == EstadoReclamo.ASIGNADO, "El rechazo no cambia el estado");
        });
        probar("Máximo cinco en curso y reasignación con capacidad", () -> {
            Contexto x = new Contexto();
            for (int n = 0; n < 5; n++) {
                Reclamo r = x.reclamo(TipoReclamo.MANTENIMIENTO); x.c.asignarReclamo(x.admin, r.getNumero(), "M001", "2026-01-01 10:00");
                x.c.actualizarEstado(x.tecnico, r.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-01 11:00", "Atención");
            }
            Reclamo sexto = x.reclamo(TipoReclamo.MANTENIMIENTO); x.c.asignarReclamo(x.admin, sexto.getNumero(), "M001", "2026-01-01 10:00");
            rechaza(() -> x.c.actualizarEstado(x.tecnico, sexto.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-01 11:00", "Atención"));
            EmpleadoMantenimiento otro = x.c.agregarEmpleado(x.admin, "108", "M002", "2026-01-01", Especialidad.PLOMERIA);
            x.c.asignarReclamo(x.admin, sexto.getNumero(), "M002", "2026-01-01 12:00");
            x.c.actualizarEstado(otro, sexto.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-01 13:00", "Atención");
            rechaza(() -> x.c.asignarReclamo(x.admin, sexto.getNumero(), "M001", "2026-01-01 14:00"));
            exigir(sexto.getResponsableAsignado() == otro, "Se conserva el responsable al rechazar");
        });
        probar("Reapertura en el límite y cierre desde la última resolución", () -> {
            Contexto x = new Contexto(); Reclamo r = x.reclamo(TipoReclamo.MANTENIMIENTO); x.resolver(r, "2026-01-01");
            rechaza(() -> x.c.actualizarEstado(x.admin, r.getNumero(), EstadoReclamo.CERRADO, "2026-01-06 11:00", "Cierre"));
            x.c.reabrirReclamo(x.titular, r.getNumero(), "2026-01-06 11:00", "Persistió el problema");
            exigir(r.getEstado() == EstadoReclamo.ASIGNADO && r.getResponsableAsignado() == x.tecnico, "Reapertura conserva responsable");
            x.c.actualizarEstado(x.tecnico, r.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-06 12:00", "Nueva atención");
            x.c.actualizarEstado(x.tecnico, r.getNumero(), EstadoReclamo.RESUELTO, "2026-01-06 13:00", "Nueva solución");
            x.barrio.verificarControles("2026-01-07 14:00"); exigir(r.getEstado() == EstadoReclamo.RESUELTO, "Se usa la última resolución");
            x.barrio.verificarControles("2026-01-11 13:01"); exigir(r.getEstado() == EstadoReclamo.CERRADO, "Cierre automático fuera del plazo");
            rechaza(() -> x.c.reabrirReclamo(x.titular, r.getNumero(), "2026-01-12 14:00", "Fuera de plazo"));
        });
        probar("Alerta de 24 horas única y atención efectiva", () -> {
            Contexto x = new Contexto(); Reclamo r = x.reclamo(TipoReclamo.SEGURIDAD);
            x.c.asignarReclamo(x.admin, r.getNumero(), "G001", "2026-01-01 10:00");
            x.barrio.verificarControles("2026-01-02 08:59"); igual(0, x.barrio.getNotificaciones().size());
            x.barrio.verificarControles("2026-01-02 09:00"); x.barrio.verificarControles("2026-01-03 09:00"); igual(1, x.barrio.getNotificaciones().size());
            exigir(r.getEstado() == EstadoReclamo.ASIGNADO, "La alerta no altera estado");
            Contexto y = new Contexto(); Reclamo atendido = y.reclamo(TipoReclamo.SEGURIDAD);
            y.c.asignarReclamo(y.admin, atendido.getNumero(), "G001", "2026-01-01 10:00");
            y.c.actualizarEstado(y.guardia, atendido.getNumero(), EstadoReclamo.EN_CURSO, "2026-01-01 11:00", "Atendido");
            y.barrio.verificarControles("2026-01-03 09:00"); igual(0, y.barrio.getNotificaciones().size());
        });
        probar("Comentarios registran autor y rechazan contenido vacío", () -> {
            Contexto x = new Contexto(); Reclamo r = x.reclamo(TipoReclamo.ADMINISTRATIVO);
            x.c.agregarComentario(x.titular, r.getNumero(), "2026-01-01 10:00", "Se adjunta información");
            rechaza(() -> x.c.agregarComentario(x.titular, r.getNumero(), "2026-01-01 10:00", " "));
            igual(1, r.getComentarios().size()); exigir(r.getComentarios().get(0).getAutor().equals(x.titular), "Autor conservado");
        });
        probar("Liquidación por superficie y corrección de centavos por número de lote", () -> {
            Contexto x = new Contexto(); new LiquidadorExpensas().emitir(x.barrio.getLotes(), "2026-01", 1000);
            igual(400, x.lote.getCuenta().getExpensas().get(0).getImporteBase());
            igual(600, x.barrio.buscarLote(2).getCuenta().getExpensas().get(0).getImporteBase());
            Barrio b = new Barrio("Prueba", x.admin); for (int n = 1; n <= 3; n++) b.agregarLote(n, 1, x.titular);
            List<Expensa> emitidas = new LiquidadorExpensas().emitir(b.getLotes(), "2026-02", 0.05);
            double total = 0;
            for (Expensa expensa : emitidas) total += expensa.getImporteBase();
            igual(0.05, total); igual(0.01, emitidas.get(0).getImporteBase());
        });
        probar("Emisión duplicada se rechaza antes de modificar otros lotes", () -> {
            Contexto x = new Contexto(); x.lote.getCuenta().emitir("2026-01", 100);
            rechaza(() -> x.c.emitirExpensas(x.admin, "2026-01", 1000)); igual(0, x.barrio.buscarLote(2).getCuenta().getExpensas().size());
            rechaza(() -> x.c.emitirExpensas(x.admin, "2026-13", 1000));
            rechaza(() -> x.c.emitirExpensas(x.admin, "2026-02", Double.NaN));
        });
        probar("Vencimiento: día 10 sin mora, día 11 con un día de interés", () -> {
            Contexto x = new Contexto(); x.lote.getCuenta().emitir("2026-01", 1000);
            igual(0, x.lote.getCuenta().consultar("2026-01-10").getSaldos().get(0).getIntereses());
            igual(1, x.lote.getCuenta().consultar("2026-01-11").getSaldos().get(0).getIntereses());
        });
        probar("Pago parcial aplica intereses primero y calcula el siguiente tramo", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 1000);
            Pago pago = cuenta.registrarPago("2026-01-20", 510, MedioPago.TRANSFERENCIA);
            igual(10, pago.getImputaciones().get(0).getMontoInteres()); igual(500, pago.getImputaciones().get(0).getMontoCapital());
            igual(505, cuenta.consultar("2026-01-30").getDeudaTotal());
            exigir(cuenta.consultar("2026-01-30").getSaldos().get(0).getEstado() == EstadoExpensa.VENCIDA, "Pago parcial vencido");
        });
        probar("Pagos empiezan por el período más antiguo, sin capitalizar intereses", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 100); cuenta.emitir("2026-02", 100);
            Pago p = cuenta.registrarPago("2026-02-11", 150, MedioPago.EFECTIVO);
            exigir(p.getImputaciones().get(0).getExpensa().getPeriodo().equals("2026-01"), "Período antiguo primero");
            igual(3.2, p.getImputaciones().get(0).getMontoInteres()); igual(100, p.getImputaciones().get(0).getMontoCapital());
            igual(53.3, cuenta.consultar("2026-02-11").getDeudaTotal());
        });
        probar("Estados PARCIAL, VENCIDA y PAGADA se calculan al consultar", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 1000);
            cuenta.registrarPago("2026-01-05", 500, MedioPago.TARJETA);
            exigir(cuenta.consultar("2026-01-09").getSaldos().get(0).getEstado() == EstadoExpensa.PARCIAL, "Parcial antes del vencimiento");
            exigir(cuenta.consultar("2026-01-11").getSaldos().get(0).getEstado() == EstadoExpensa.VENCIDA, "Se actualiza el estado al vencer");
            cuenta.registrarPago("2026-01-11", 500.5, MedioPago.EFECTIVO);
            exigir(cuenta.consultar("2026-03-10").getSaldos().get(0).getEstado() == EstadoExpensa.PAGADA, "Una deuda cancelada no genera interés nuevo");
        });
        probar("Pagos inválidos y anticipos no dejan movimientos parciales", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-02", 100);
            rechaza(() -> cuenta.registrarPago("2026-01-10", 1, MedioPago.EFECTIVO));
            rechaza(() -> cuenta.registrarPago("2026-02-10", 101, MedioPago.EFECTIVO));
            rechaza(() -> cuenta.registrarPago("2026-02-10", -1, MedioPago.EFECTIVO));
            rechaza(() -> cuenta.registrarPago("2026-02-10", Double.POSITIVE_INFINITY, MedioPago.EFECTIVO));
            igual(0, cuenta.getPagos().size()); igual(0, cuenta.getExpensas().get(0).getImputaciones().size());
        });
        probar("Ajuste retroactivo conserva pago e imputaciones originales", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 1000);
            Pago original = cuenta.registrarPago("2026-01-20", 510, MedioPago.EFECTIVO);
            cuenta.ajustar(1, "2026-01-30", -100, "Corrección de importe");
            igual(606, cuenta.consultar("2026-01-30").getDeudaTotal()); igual(502.5, cuenta.consultar("2026-01-25").getDeudaTotal());
            igual(510, original.getImporteTotal()); igual(500, original.getImputaciones().get(0).getMontoCapital()); igual(1, cuenta.getAjustes().size());
        });
        probar("Ajuste reconstruye pagos posteriores e intereses por tramos", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 1000);
            cuenta.registrarPago("2026-01-20", 510, MedioPago.EFECTIVO);
            Pago posterior = cuenta.registrarPago("2026-01-25", 100, MedioPago.EFECTIVO);
            cuenta.ajustar(1, "2026-01-30", -100, "Corrección");
            igual(505.52, cuenta.consultar("2026-01-30").getDeudaTotal()); igual(97.5, posterior.getImputaciones().get(0).getMontoCapital());
            ImputacionPago encontrada = null;
            for (ImputacionPago imputacion : cuenta.consultar("2026-01-30").getImputacionesEfectivas()) {
                if (imputacion.getPago() == posterior) { encontrada = imputacion; break; }
            }
            exigir(encontrada != null, "Existe imputación efectiva del pago posterior");
            igual(97, encontrada.getMontoCapital());
        });
        probar("Ajuste rechaza negativos efectivos y sobrepagos posteriores", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta(); cuenta.emitir("2026-01", 200);
            cuenta.registrarPago("2026-01-10", 100, MedioPago.EFECTIVO); cuenta.registrarPago("2026-01-10", 100, MedioPago.EFECTIVO);
            rechaza(() -> cuenta.ajustar(1, "2026-01-11", -101, "Inválido"));
            rechaza(() -> cuenta.ajustar(1, "2026-01-11", 1, "Sobrepaga el segundo movimiento"));
            igual(0, cuenta.getAjustes().size()); igual(0, cuenta.consultar("2026-01-11").getDeudaTotal());
        });
        probar("Morosidad desde cuatro períodos, continuidad y notificación única", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta();
            for (String periodo : List.of("2026-01", "2026-02", "2026-03")) cuenta.emitir(periodo, 100);
            x.barrio.verificarControles("2026-04-11 10:00"); exigir(!cuenta.isMoroso(), "Tres períodos no bastan");
            cuenta.emitir("2026-04", 100); x.barrio.verificarControles("2026-04-11 11:00"); exigir(cuenta.isMoroso(), "Cuatro períodos generan morosidad");
            x.barrio.verificarControles("2026-04-11 12:00"); igual(1, x.barrio.getNotificaciones().size());
            double primera = cuenta.consultar("2026-04-11").getSaldos().get(0).getTotal(); cuenta.registrarPago("2026-04-11", primera, MedioPago.EFECTIVO);
            x.barrio.verificarControles("2026-04-11 13:00"); exigir(cuenta.isMoroso(), "Se conserva el episodio con tres períodos");
            cuenta.registrarPago("2026-04-11", cuenta.consultar("2026-04-11").getDeudaTotal(), MedioPago.EFECTIVO);
            x.barrio.verificarControles("2026-04-11 14:00"); exigir(!cuenta.isMoroso(), "Se quita al cancelar toda la deuda vencida"); igual(1, x.barrio.getNotificaciones().size());
        });
        probar("Fechas estrictas, bisiestos, horas y períodos", () -> {
            Fechas.fecha("2024-02-29"); comprobaciones++;
            rechaza(() -> Fechas.fecha("2026-02-29")); rechaza(() -> Fechas.fecha("2026-13-01"));
            rechaza(() -> Fechas.hora("24:00")); rechaza(() -> Fechas.periodo("2026-1"));
            igual(2, Fechas.dias("2024-02-28", "2024-03-01"));
        });
        probar("Informante de falla debe ser vecino o guardia", () -> {
            Contexto x = new Contexto();
            rechaza(() -> x.c.registrarFalla(x.personas.buscarPorDocumento("108"), "CAM-01", "2026-01-01 10:00", "", "Falla"));
            Falla f = x.c.registrarFalla(x.guardia, "CAM-01", "2026-01-01 10:00", "", "Sin imagen");
            exigir(f.getDescripcion().contains("estimado"), "Se informa la estimación de inicio");
            igual(1, x.barrio.getFallas().size());
        });
        probar("Prioridad polimórfica y umbral por luminarias distintas", () -> {
            Contexto x = new Contexto(); Sector sector = new Sector("Sector norte");
            Falla camara = x.barrio.registrarFalla("CAM-01", x.guardia, "2026-01-01 10:00", "", "Falla");
            exigir(x.barrio.generarOrden(camara.getNumero(), "2026-01-01 11:00", "Reparar").getPrioridad() == Prioridad.ALTA, "Cámara ALTA");
            List<OrdenMantenimiento> ordenes = new ArrayList<>();
            for (int n = 1; n <= 3; n++) {
                x.barrio.agregarEquipo(new Luminaria("N" + n, sector, "2026-01-01", "LED", 20));
                Falla f = x.barrio.registrarFalla("N" + n, x.guardia, "2026-01-01 10:00", "", "Falla");
                ordenes.add(x.barrio.generarOrden(f.getNumero(), "2026-01-01 11:00", "Reparar"));
            }
            x.barrio.registrarFalla("N1", x.guardia, "2026-01-01 12:00", "", "Segundo reporte");
            for (OrdenMantenimiento orden : ordenes) exigir(orden.getPrioridad() == Prioridad.MEDIA, "Repetir un reporte no aumenta la cantidad de equipos");
            x.barrio.agregarEquipo(new Luminaria("N4", sector, "2026-01-01", "LED", 20));
            Falla cuarta = x.barrio.registrarFalla("N4", x.guardia, "2026-01-01 12:00", "", "Falla");
            for (OrdenMantenimiento orden : ordenes) exigir(orden.getPrioridad() == Prioridad.ALTA, "Se elevan las órdenes abiertas");
            exigir(x.barrio.generarOrden(cuarta.getNumero(), "2026-01-01 13:00", "Reparar").getPrioridad() == Prioridad.ALTA, "Nueva orden ALTA");
        });
        probar("Orden requiere técnico y reparación y no se duplica", () -> {
            Contexto x = new Contexto(); Falla f = x.c.registrarFalla(x.guardia, "CAM-01", "2026-01-01 10:00", "", "Falla");
            OrdenMantenimiento o = x.c.generarOrden(x.admin, f.getNumero(), "2026-01-01 11:00", "Reparar cámara");
            rechaza(() -> x.c.generarOrden(x.admin, f.getNumero(), "2026-01-01 12:00", "Duplicada"));
            rechaza(() -> x.c.cerrarOrden(x.admin, o.getNumero(), "2026-01-01 12:00", "Reparada"));
            x.c.asignarOrden(x.admin, o.getNumero(), "M001");
            rechaza(() -> x.c.cerrarOrden(x.tecnico, o.getNumero(), "2026-01-01 12:00", " "));
            rechaza(() -> x.c.cerrarOrden(x.titular, o.getNumero(), "2026-01-01 12:00", "Reparada"));
            x.c.cerrarOrden(x.tecnico, o.getNumero(), "2026-01-01 13:00", "Se reemplazó fuente");
            exigir(f.getFechaRestablecimiento().equals(o.getFechaCierre()), "Restablecimiento registrado");
            exigir(o.getEquipoAfectado().getEstado() == EstadoEquipo.OPERATIVO, "Equipo operativo");
        });
        probar("Alerta de cámara por interrupción, sin duplicar reportes", () -> {
            Contexto x = new Contexto();
            Falla a = x.barrio.registrarFalla("CAM-01", x.guardia, "2026-01-01 10:00", "", "Falla");
            Falla b = x.barrio.registrarFalla("CAM-01", x.guardia, "2026-01-01 11:00", "", "Otro reporte");
            x.barrio.verificarControles("2026-01-03 10:00"); igual(0, x.barrio.getNotificaciones().size());
            x.barrio.verificarControles("2026-01-03 10:01"); x.barrio.verificarControles("2026-01-04 10:01"); igual(1, x.barrio.getNotificaciones().size());
            for (Falla f : List.of(a, b)) { OrdenMantenimiento o = x.barrio.generarOrden(f.getNumero(), "2026-01-04 11:00", "Reparar"); o.asignar(x.tecnico); o.cerrar("2026-01-04 12:00", "Restablecido"); }
            x.barrio.registrarFalla("CAM-01", x.guardia, "2026-01-05 10:00", "", "Nueva interrupción");
            x.barrio.verificarControles("2026-01-07 10:01"); igual(2, x.barrio.getNotificaciones().size());
        });
        probar("Titular no residente y residente pueden autorizar; terceros no", () -> {
            Contexto x = new Contexto(); Persona visita = x.personas.buscarPorDocumento("106");
            x.c.autorizarVisita(x.titular, new VisitanteProveedor(visita, TipoVisitante.VISITA, false), 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            x.c.autorizarVisita(x.residente, new VisitanteProveedor(visita, TipoVisitante.VISITA, false), 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            rechaza(() -> x.c.autorizarVisita(x.personas.buscarPorDocumento("108"), new VisitanteProveedor(visita, TipoVisitante.VISITA, false), 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false));
        });
        probar("Proveedor habitual registrado por administrador; máximo treinta días inclusivos", () -> {
            Contexto x = new Contexto();
            rechaza(() -> x.c.identificarVisitante(x.titular, "107", TipoVisitante.PROVEEDOR, true));
            x.c.identificarVisitante(x.admin, "107", TipoVisitante.PROVEEDOR, true);
            AutorizacionAcceso a = x.c.autorizarVisita(x.titular, "107", TipoVisitante.PROVEEDOR, 1, "2026-01-01", "2026-01-30", "08:00", "20:00", true);
            exigir(a.estaVigente("2026-01-30 20:00"), "Límite incluido");
            rechaza(() -> x.c.autorizarVisita(x.titular, "107", TipoVisitante.PROVEEDOR, 1, "2026-01-01", "2026-01-31", "08:00", "20:00", true));
            rechaza(() -> x.c.autorizarVisita(x.titular, "106", TipoVisitante.VISITA, 1, "2026-01-01", "2026-01-02", "08:00", "20:00", true));
        });
        probar("Vigencia de autorización valida fechas y franja horaria", () -> {
            Contexto x = new Contexto(); AutorizacionAcceso a = x.c.autorizarVisita(x.titular, "106", TipoVisitante.VISITA, 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            exigir(a.estaVigente("2026-01-02 08:00") && a.estaVigente("2026-01-02 20:00"), "Extremos incluidos");
            exigir(!a.estaVigente("2026-01-02 07:59") && !a.estaVigente("2026-01-03 10:00"), "Fuera de vigencia");
            rechaza(() -> x.c.autorizarVisita(x.titular, "106", TipoVisitante.VISITA, 1, "2026-01-02", "2026-01-02", "20:00", "08:00", false));
        });
        probar("Ingreso global único, egreso cronológico y consulta por documento", () -> {
            Contexto x = new Contexto(); AutorizacionAcceso a = x.c.autorizarVisita(x.titular, "106", TipoVisitante.VISITA, 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            AutorizacionAcceso b = x.c.autorizarVisita(x.personas.buscarPorDocumento("102"), "106", TipoVisitante.VISITA, 2, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            igual(2, x.c.buscarAutorizaciones(x.guardia, "106", "2026-01-02 10:00").size());
            RegistroAcceso r = x.c.registrarIngreso(x.guardia, a.getNumero(), "2026-01-02 10:00");
            rechaza(() -> x.c.registrarIngreso(x.guardia, b.getNumero(), "2026-01-02 11:00"));
            rechaza(() -> x.c.registrarEgreso(x.guardia, "106", "2026-01-02 09:00"));
            x.c.registrarEgreso(x.guardia, "106", "2026-01-02 21:00"); exigir(r.getFechaHoraEgreso() != null, "Puede salir luego de terminar la franja");
            rechaza(() -> x.c.registrarEgreso(x.guardia, "106", "2026-01-02 22:00"));
            igual(1, x.barrio.getRegistrosAcceso().size());
        });
        probar("Guardia de ronda no puede operar portería", () -> {
            Contexto x = new Contexto(); Guardia ronda = x.c.agregarGuardia(x.admin, "108", "G002", "2026-01-01", Turno.TARDE, PuestoGuardia.RONDA);
            AutorizacionAcceso a = x.c.autorizarVisita(x.titular, "106", TipoVisitante.VISITA, 1, "2026-01-02", "2026-01-02", "08:00", "20:00", false);
            rechaza(() -> x.c.registrarIngreso(ronda, a.getNumero(), "2026-01-02 10:00"));
        });
        probar("Controlador rechaza administrador externo aunque comparta documento", () -> {
            Contexto x = new Contexto(); Administrador falso = new Administrador(x.admin, "A001", "2026-01-01");
            rechaza(() -> x.c.emitirExpensas(falso, "2026-01", 1000));
            rechaza(() -> x.c.registrarPago(x.titular, 1, "2026-01-01", 10, MedioPago.EFECTIVO));
        });
        probar("Persistencia completa conserva identidades, movimientos e historiales", () -> {
            Contexto x = new Contexto(); Reclamo reclamo = x.reclamo(TipoReclamo.MANTENIMIENTO); x.resolver(reclamo, "2026-01-01");
            x.lote.getCuenta().emitir("2026-01", 1000); x.lote.getCuenta().registrarPago("2026-01-20", 510, MedioPago.EFECTIVO); x.lote.getCuenta().ajustar(1, "2026-01-30", -100, "Corrección");
            Path ruta = prepararArchivo("barrio.dat");
            RepositorioBarrioArchivo archivo = new RepositorioBarrioArchivo(ruta); exigir(archivo.cargar() == null, "Archivo ausente"); archivo.guardar(x.barrio);
            Barrio cargado = archivo.cargar(); exigir(cargado != null, "Se recuperó el barrio"); Lote lote = cargado.buscarLote(1);
            exigir(cargado.getReclamos().get(0).getLote() == lote, "Identidad del lote conservada");
            exigir(cargado.getReclamos().get(0).getResponsableAsignado() == cargado.buscarPersonal("M001"), "Identidad del responsable conservada");
            exigir(lote.getResidentes().get(0).getLote() == lote, "Relación bidireccional conservada");
            exigir(lote.getCuenta().getPagos().get(0) == lote.getCuenta().getAjustes().get(0).getPagoCorregido(), "Ajuste vinculado al pago original");
            igual(606, lote.getCuenta().consultar("2026-01-30").getDeudaTotal()); igual(4, cargado.getReclamos().get(0).getCambiosEstado().size());
        });
        probar("Repositorio de reclamos en archivo recupera cambios y comentarios", () -> {
            Contexto x = new Contexto(); Reclamo r = x.reclamo(TipoReclamo.SEGURIDAD); x.c.agregarComentario(x.titular, r.getNumero(), "2026-01-01 10:00", "Prueba");
            Path ruta = prepararArchivo("reclamos.dat");
            RepositorioReclamos archivo = new RepositorioReclamosArchivo(ruta); archivo.guardar(r);
            RepositorioReclamos nuevo = new RepositorioReclamosArchivo(ruta); igual(1, nuevo.listarTodos().size()); igual(1, nuevo.buscarPorNumero(r.getNumero()).getComentarios().size());
        });
        probar("Liquidar un período atrasado no reimputa pagos anteriores a su emisión", () -> {
            Contexto x = new Contexto(); CuentaLote cuenta = x.lote.getCuenta();
            cuenta.emitir("2026-02", 100, "2026-02-01");
            Pago pago = cuenta.registrarPago("2026-02-10", 100, MedioPago.EFECTIVO);
            cuenta.emitir("2026-01", 100, "2026-03-01");
            igual(0, cuenta.consultar("2026-02-10").getDeudaTotal());
            exigir(cuenta.consultar("2026-03-01").getImputacionesEfectivas().get(0).getExpensa() == pago.getImputaciones().get(0).getExpensa(), "No se reasigna el pago a una factura inexistente al pagar");
            exigir(cuenta.consultar("2026-03-01").getDeudaTotal() > 100, "La liquidación atrasada conserva su vencimiento");
        });
        probar("Fechas por componentes: siglos, años bisiestos y cambio de día", () -> {
            igual(366, Fechas.dias("2000-01-01", "2001-01-01"));
            igual(365, Fechas.dias("1900-01-01", "1901-01-01"));
            igual(2, Fechas.minutosEntre("2026-12-31 23:59", "2027-01-01 00:01"));
            igual(-2, Fechas.minutosEntre("2027-01-01 00:01", "2026-12-31 23:59"));
            igual(0, Fechas.compararFechas("2024-02-29", "2024-02-29"));
            rechaza(() -> Fechas.fecha("1900-02-29"));
            rechaza(() -> Fechas.fecha("0000-01-01"));
            rechaza(() -> Fechas.momento("2026-11-03 24:00"));
        });
        probar("Redondeo con String y enteros: signos, empates y exponentes", () -> {
            igual(1.01, Validaciones.dinero(1.005));
            igual(-1.01, Validaciones.dinero(-1.005));
            igual(2.68, Validaciones.dinero(2.675));
            igual(-2.68, Validaciones.dinero(-2.675));
            igual(0.01, Validaciones.dinero(5e-3));
            igual(0, Validaciones.dinero(1e-8));
            igual(123, Validaciones.centavos(1.2345));
            igual(-124, Validaciones.centavos(-1.235));
            rechaza(() -> Validaciones.dinero(Double.NaN));
        });
        probar("CSV: lectura de caracteres y rechazo de filas inválidas", () -> {
            Path ruta = prepararArchivo("personas.csv");
            String cabecera = "documento;nombre;apellido;telefono;correoElectronico\n";
            String persona = "101;Ana;Pérez;123;ana@ejemplo.com";
            Files.writeString(ruta, cabecera + persona);
            RepositorioPersonas personas = new RepositorioPersonasCsv(ruta);
            exigir(personas.listar().size() == 1, "Se recupera la persona.");
            exigir(personas.buscarPorDocumento("101").getApellido().equals("Pérez"), "Se conservan las tildes.");
            String[] filasInvalidas = {
                "101;Ana;Pérez;123",
                persona + ";extra",
                "101;Ana;Pérez;123;",
                persona + "\n" + persona,
                persona + "\n 101 ;Otra;Persona;456;otra@ejemplo.com"
            };
            for (String fila : filasInvalidas) {
                Files.writeString(ruta, cabecera + fila);
                boolean rechazada = false;
                try { new RepositorioPersonasCsv(ruta); }
                catch (IllegalArgumentException e) { rechazada = true; }
                exigir(rechazada, "Se rechaza una fila inválida o duplicada.");
            }
        });
        probar("Alta de persona crea el CSV y se conserva al volver a leer", () -> {
            Path ruta = prepararArchivo("personas-nuevas.csv");
            RepositorioPersonas personas = new RepositorioPersonasCsv(ruta);
            exigir(personas.listar().isEmpty(), "El archivo nuevo contiene solo la cabecera.");
            Persona nueva = new Persona("Lucía", "Pérez", "109", "000-109", "lucia@example.invalid");
            personas.agregarPersona(nueva);
            exigir(personas.buscarPorDocumento("109") == nueva, "Está disponible sin reiniciar.");
            RepositorioPersonas recuperadas = new RepositorioPersonasCsv(ruta);
            exigir(recuperadas.listar().size() == 1, "Se recupera una sola persona.");
            exigir(recuperadas.buscarPorDocumento("109").getNombre().equals("Lucía"), "Se conservan las tildes.");
            exigir(recuperadas.buscarPorDocumento("109").getCorreoElectronico().equals("lucia@example.invalid"), "Se conservan los campos.");
        });
        probar("Alta rechaza duplicados y campos incompatibles sin modificar el CSV", () -> {
            Path ruta = prepararArchivo("personas-validacion.csv");
            RepositorioPersonas personas = new RepositorioPersonasCsv(ruta);
            personas.agregarPersona(new Persona("Ana", "Pérez", "109", "123", "ana@example.invalid"));
            String original = Files.readString(ruta);
            rechaza(() -> personas.agregarPersona(new Persona("Otra", "Persona", " 109 ", "456", "otra@example.invalid")));
            rechaza(() -> personas.agregarPersona(new Persona("Ana;Otra", "Pérez", "110", "123", "ana@example.invalid")));
            rechaza(() -> personas.agregarPersona(new Persona("Ana\nOtra", "Pérez", "110", "123", "ana@example.invalid")));
            rechaza(() -> personas.agregarPersona(new Persona("Ana", "Pérez", "110", "123\r456", "ana@example.invalid")));
            rechaza(() -> personas.agregarPersona(null));
            rechaza(() -> personas.agregarPersona(new Persona(" ", "Pérez", "110", "123", "ana@example.invalid")));
            exigir(Files.readString(ruta).equals(original), "Los rechazos no escriben en el archivo.");
            exigir(personas.listar().size() == 1, "Los rechazos no agregan personas en memoria.");
        });
        probar("El alta admite un CSV existente sin salto de línea al final", () -> {
            Path ruta = prepararArchivo("personas-sin-salto.csv");
            Files.writeString(ruta, "documento;nombre;apellido;telefono;correoElectronico\n101;Ana;Pérez;123;ana@example.invalid");
            RepositorioPersonas personas = new RepositorioPersonasCsv(ruta);
            personas.agregarPersona(new Persona("Bruno", "Prueba", "102", "456", "bruno@example.invalid"));
            exigir(new RepositorioPersonasCsv(ruta).listar().size() == 2, "Las filas quedan separadas.");
        });
        probar("Un fallo al guardar una persona no la agrega en memoria", () -> {
            Path ruta = prepararArchivo("personas-error.csv");
            Path respaldo = prepararArchivo("personas-error-respaldo.csv");
            RepositorioPersonas personas = new RepositorioPersonasCsv(ruta);
            Files.move(ruta, respaldo);
            Files.createDirectories(ruta);
            boolean fallo = false;
            try { personas.agregarPersona(new Persona("Ana", "Pérez", "109", "123", "ana@example.invalid")); }
            catch (IllegalStateException e) { fallo = true; }
            exigir(fallo, "Se informa el fallo de escritura.");
            exigir(personas.listar().isEmpty(), "No se registra un alta que no se pudo guardar.");
            Files.delete(ruta);
            Files.move(respaldo, ruta);
        });
        probar("Solo el administrador da de alta y la nueva persona se puede asignar inmediatamente", () -> {
            Contexto x = new Contexto();
            rechaza(() -> x.c.agregarPersona(x.guardia, "109", "Lucía", "Pérez", "123", "lucia@example.invalid"));
            exigir(x.c.listarPersonas().size() == 9, "El rechazo de permisos no agrega personas.");
            Persona nueva = x.c.agregarPersona(x.admin, "109", "Lucía", "Pérez", "123", "lucia@example.invalid");
            exigir(x.c.listarPersonas().size() == 10 && x.c.listarActores().size() == 10, "Se actualizan las consultas sin reiniciar.");
            Lote lote = x.c.agregarLote(x.admin, 3, 250, "109");
            exigir(lote.getTitular() == nueva, "La nueva persona se asigna como titular.");
            x.c.agregarGuardia(x.admin, "109", "G002", "2026-11-03", Turno.MANANA, PuestoGuardia.PORTERIA);
            exigir(x.c.listarActores().size() == 10, "Asignar un rol no duplica a la persona.");
            rechaza(() -> x.c.agregarPersona(x.admin, "109", "Otra", "Persona", "456", "otra@example.invalid"));
            exigir(x.c.listarPersonas().size() == 10, "Se rechaza el documento duplicado.");
        });
        probar("Archivo corrupto se informa y conserva sus bytes", () -> {
            Path ruta = prepararArchivo("corrupto.dat"); Files.writeString(ruta, "NO ES UN BARRIO");
            boolean rechazado = false;
            try { new RepositorioBarrioArchivo(ruta).cargar(); }
            catch (IllegalStateException e) { rechazado = true; }
            exigir(rechazado, "El archivo corrupto debe ser rechazado.");
            exigir(Files.readString(ruta).equals("NO ES UN BARRIO"), "No se reemplaza por datos demo");
        });
        registrar("\nResultado: " + correctas + " escenarios correctos, " + fallidas + " fallidos, " + comprobaciones + " comprobaciones.");
        Files.writeString(Paths.get("target/pruebas-aceptacion.txt"), informe);
        if (fallidas > 0) throw new IllegalStateException("Fallaron " + fallidas + " escenarios.");
    }
}
