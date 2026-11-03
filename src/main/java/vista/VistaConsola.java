package vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.comun.Validaciones;
import modelo.comunidad.Lote;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.finanzas.*;
import modelo.mantenimiento.*;
import modelo.accesos.*;

/** Menús con Scanner, while, switch e if. Las reglas se delegan al controlador. */
public final class VistaConsola {
    private final GestorBarrioControlador controlador;
    private final Scanner entrada = new Scanner(System.in);
    private Persona actor;
    private boolean finEntrada;

    public VistaConsola(GestorBarrioControlador controlador) {
        this.controlador = Validaciones.requerido(controlador, "Controlador");
        for (Personal integrante : controlador.listarPersonal()) {
            if (integrante instanceof Administrador) { actor = integrante; break; }
        }
        Validaciones.requerido(actor, "Administrador");
    }

    public void iniciar() {
        System.out.println("LOS AROMOS — MENÚ DE TERMINAL");
        System.out.println("El cambio de actor permite recorrer los permisos del trabajo práctico.");
        System.out.println("La fecha y hora de la sesión se ingresan como texto y se pueden avanzar con la opción 10.");
        solicitarFechaHora(false);
        while (!finEntrada) {
            try {
                controlador.verificarControles(Fechas.ahora());
                actualizarActor();
                System.out.println("\nActor actual: " + actor + " — " + descripcionActor(actor));
                menu("Menú principal", new String[] {"Cambiar actor", "Comunidad y personal", "Reclamos",
                        "Expensas y pagos", "Equipos y mantenimiento", "Visitas y accesos",
                        "Reportes y alertas", "Guardar", "Verificar plazos y alertas", "Actualizar fecha y hora de la sesión"});
                int opcion = entero("Opción (0 para guardar y salir): ");
                if (finEntrada) break;
                switch (opcion) {
                    case 0:
                        controlador.guardar();
                        System.out.println("Datos guardados. Hasta luego.");
                        return;
                    case 1: cambiarActor(); break;
                    case 2: comunidad(); break;
                    case 3: reclamos(); break;
                    case 4: finanzas(); break;
                    case 5: mantenimiento(); break;
                    case 6: accesos(); break;
                    case 7: System.out.println(controlador.generarReportes(actor, Fechas.ahora())); break;
                    case 8: controlador.guardar(); completada(); break;
                    case 9: controlador.verificarControles(Fechas.ahora()); completada(); break;
                    case 10:
                        solicitarFechaHora(true);
                        if (!finEntrada) completada();
                        break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        controlador.guardar();
        System.out.println("\nFin de entrada. Datos guardados.");
        // No se cierran System.in ni System.out: pertenecen al programa que inició la vista.
    }

    private void actualizarActor() {
        for (Persona persona : controlador.listarActores()) {
            if (persona.equals(actor)) { actor = persona; return; }
        }
    }

    private void solicitarFechaHora(boolean comprobarAvance) {
        while (!finEntrada) {
            String valor = leer("Fecha y hora de la sesión (aaaa-MM-dd HH:mm): ");
            if (finEntrada) return;
            try {
                if (comprobarAvance) Fechas.noAnterior(Fechas.ahora(), valor);
                Fechas.establecerMomento(valor);
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void completada() {
        controlador.verificarControles(Fechas.ahora());
        System.out.println("Operación completada. Datos guardados.");
    }

    private void cambiarActor() {
        Persona seleccionado = seleccionar("Actor", controlador.listarActores());
        if (seleccionado == null) return;
        actor = seleccionado;
        System.out.println("Actor seleccionado: " + actor + " — " + descripcionActor(actor));
    }

    private String descripcionActor(Persona persona) {
        if (persona instanceof Administrador) return "Administrador";
        if (persona instanceof Guardia) return "Guardia";
        if (persona instanceof EmpleadoMantenimiento) return "EmpleadoMantenimiento";
        if (persona instanceof Residente) return "Residente";
        return "Persona";
    }

    private void menu(String titulo, String[] opciones) {
        System.out.println("\n=== " + titulo + " ===");
        for (int i = 0; i < opciones.length; i++) System.out.println((i + 1) + ") " + opciones[i]);
        System.out.println("0) " + (titulo.equals("Menú principal") ? "Guardar y salir" : "Volver"));
    }

    private void comunidad() {
        while (!finEntrada) {
            menu("Comunidad y personal", new String[] {"Consultar lotes y personal", "Agregar lote",
                    "Agregar residente", "Quitar residente", "Cambiar titular", "Autorizar o revocar permiso de reclamo",
                    "Registrar guardia", "Registrar empleado de mantenimiento", "Consultar personas disponibles", "Registrar persona"});
            int opcion = entero("Opción (0 para volver): ");
            if (opcion == 0 || finEntrada) return;
            try {
                switch (opcion) {
                    case 1: listarComunidad(); break;
                    case 2: agregarLote(); break;
                    case 3: agregarResidente(); break;
                    case 4: quitarResidente(); break;
                    case 5: cambiarTitular(); break;
                    case 6: permisoReclamos(); break;
                    case 7: agregarGuardia(); break;
                    case 8: agregarEmpleado(); break;
                    case 9: listar(controlador.listarPersonas()); break;
                    case 10: agregarPersona(); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void listarComunidad() {
        System.out.println("LOTES (" + controlador.listarLotes().size() + "/150)");
        for (Lote lote : controlador.listarLotes()) {
            System.out.println(lote + " | " + lote.getSuperficie() + " m²");
            System.out.println("  Residentes: " + lote.getResidentes());
            System.out.println("  Permisos de reclamo: " + lote.getPersonasAutorizadasReclamo());
        }
        System.out.println("PERSONAL");
        for (Personal persona : controlador.listarPersonal()) {
            System.out.println(persona.getLegajo() + " | " + descripcionActor(persona) + " | " + persona);
        }
    }

    private void agregarPersona() {
        String documento = leer("Documento: ");
        String nombre = leer("Nombre: ");
        String apellido = leer("Apellido: ");
        String telefono = leer("Teléfono: ");
        String correo = leer("Correo electrónico: ");
        if (finEntrada) return;
        System.out.println("Persona registrada: " + controlador.agregarPersona(actor, documento, nombre, apellido, telefono, correo));
        completada();
    }

    private void agregarLote() {
        int numero = entero("Número del lote: ");
        double superficie = decimal("Superficie en m²: ");
        Persona titular = persona();
        if (titular == null || finEntrada) return;
        controlador.agregarLote(actor, numero, superficie, titular.getDocumento());
        completada();
    }

    private void agregarResidente() {
        int lote = lote(); if (lote == 0) return;
        Persona persona = persona(); if (persona == null) return;
        TipoResidente tipo = seleccionar("TipoResidente", TipoResidente.values());
        if (tipo == null || finEntrada) return;
        controlador.agregarResidente(actor, lote, persona.getDocumento(), tipo);
        completada();
    }

    private void quitarResidente() {
        int lote = lote(); if (lote == 0) return;
        Persona persona = persona(); if (persona == null || finEntrada) return;
        controlador.quitarResidente(actor, lote, persona.getDocumento());
        completada();
    }

    private void cambiarTitular() {
        int lote = lote(); if (lote == 0) return;
        Persona persona = persona(); if (persona == null || finEntrada) return;
        controlador.cambiarTitular(actor, lote, persona.getDocumento());
        completada();
    }

    private void permisoReclamos() {
        int lote = lote(); if (lote == 0) return;
        Persona persona = persona(); if (persona == null) return;
        boolean autorizar = siNo("¿Autorizar? (s para autorizar, n para revocar): ");
        if (finEntrada) return;
        controlador.permisoReclamos(actor, lote, persona.getDocumento(), autorizar);
        completada();
    }

    private void agregarGuardia() {
        Persona persona = persona(); if (persona == null) return;
        String legajo = leer("Legajo: "), fecha = fecha("Fecha de ingreso");
        Turno turno = seleccionar("Turno", Turno.values()); if (turno == null) return;
        PuestoGuardia puesto = seleccionar("PuestoGuardia", PuestoGuardia.values());
        if (puesto == null || finEntrada) return;
        controlador.agregarGuardia(actor, persona.getDocumento(), legajo, fecha, turno, puesto);
        completada();
    }

    private void agregarEmpleado() {
        Persona persona = persona(); if (persona == null) return;
        String legajo = leer("Legajo: "), fecha = fecha("Fecha de ingreso");
        Especialidad especialidad = seleccionar("Especialidad", Especialidad.values());
        if (especialidad == null || finEntrada) return;
        controlador.agregarEmpleado(actor, persona.getDocumento(), legajo, fecha, especialidad);
        completada();
    }

    private void reclamos() {
        while (!finEntrada) {
            menu("Reclamos", new String[] {"Listar reclamos", "Consultar detalle e historial", "Registrar reclamo",
                    "Asignar o reasignar responsable", "Actualizar estado / cerrar", "Reabrir reclamo", "Agregar comentario"});
            int opcion = entero("Opción (0 para volver): ");
            if (opcion == 0 || finEntrada) return;
            try {
                switch (opcion) {
                    case 1: listar(controlador.listarReclamos(actor)); break;
                    case 2: detalleReclamo(); break;
                    case 3: registrarReclamo(); break;
                    case 4: asignarReclamo(); break;
                    case 5: actualizarEstado(); break;
                    case 6: reabrirReclamo(); break;
                    case 7: comentar(); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void detalleReclamo() {
        Reclamo elegido = reclamo(); if (elegido == null) return;
        Reclamo reclamo = controlador.consultarReclamo(actor, elegido.getNumero());
        System.out.println(reclamo + "\nPrioridad: " + reclamo.getPrioridad() + "\nResponsable: " + reclamo.getResponsableAsignado());
        System.out.println("HISTORIAL"); listar(reclamo.getCambiosEstado());
        System.out.println("COMENTARIOS"); listar(reclamo.getComentarios());
    }

    private void registrarReclamo() {
        int lote = lote(); if (lote == 0) return;
        TipoReclamo tipo = seleccionar("TipoReclamo", TipoReclamo.values()); if (tipo == null) return;
        String descripcion = leer("Descripción: "); if (finEntrada) return;
        Reclamo nuevo = controlador.registrarReclamo(actor, lote, tipo, descripcion, Fechas.ahora());
        System.out.println("Reclamo registrado: " + nuevo);
        completada();
    }

    private void asignarReclamo() {
        Reclamo reclamo = reclamo(); if (reclamo == null) return;
        List<Personal> habilitados = new ArrayList<>();
        for (Personal persona : controlador.listarPersonal()) {
            if (persona.puedeAtender(reclamo.getTipo())) habilitados.add(persona);
        }
        Personal responsable = seleccionar("Responsable", habilitados);
        if (responsable == null || finEntrada) return;
        controlador.asignarReclamo(actor, reclamo.getNumero(), responsable.getLegajo(), Fechas.ahora());
        completada();
    }

    private void actualizarEstado() {
        Reclamo reclamo = reclamo(); if (reclamo == null) return;
        EstadoReclamo estado = seleccionar("EstadoReclamo", EstadoReclamo.values()); if (estado == null) return;
        String observacion = leer("Observación: "); if (finEntrada) return;
        controlador.actualizarEstado(actor, reclamo.getNumero(), estado, Fechas.ahora(), observacion);
        completada();
    }

    private void reabrirReclamo() {
        Reclamo reclamo = reclamo(); if (reclamo == null) return;
        String observacion = leer("Motivo de reapertura: "); if (finEntrada) return;
        controlador.reabrirReclamo(actor, reclamo.getNumero(), Fechas.ahora(), observacion);
        completada();
    }

    private void comentar() {
        Reclamo reclamo = reclamo(); if (reclamo == null) return;
        String texto = leer("Comentario: "); if (finEntrada) return;
        controlador.agregarComentario(actor, reclamo.getNumero(), Fechas.ahora(), texto);
        completada();
    }

    private void finanzas() {
        while (!finEntrada) {
            menu("Expensas y pagos", new String[] {"Emitir expensas", "Consultar cuenta, pagos y ajustes",
                    "Registrar pago", "Corregir un pago mediante ajuste"});
            int opcion = entero("Opción (0 para volver): ");
            if (opcion == 0 || finEntrada) return;
            try {
                switch (opcion) {
                    case 1: emitirExpensas(); break;
                    case 2: consultarCuenta(); break;
                    case 3: registrarPago(); break;
                    case 4: ajustarPago(); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void emitirExpensas() {
        String actual = Fechas.hoy().substring(0, 7);
        String periodo = leer("Período aaaa-MM [" + actual + "]: ", actual);
        double presupuesto = decimal("Presupuesto mensual: "); if (finEntrada) return;
        System.out.println("Expensas emitidas: " + controlador.emitirExpensas(actor, periodo, presupuesto).size());
        completada();
    }

    private void consultarCuenta() {
        int lote = lote(); if (lote == 0) return;
        String fecha = fecha("Fecha de consulta"); if (finEntrada) return;
        EstadoCuenta cuenta = controlador.consultarCuenta(actor, lote, fecha);
        System.out.println(cuenta);
        System.out.println("PAGOS ORIGINALES"); listar(controlador.consultarPagos(actor, lote, fecha));
        System.out.println("AJUSTES");
        for (AsientoAjuste ajuste : controlador.consultarAjustes(actor, lote, fecha)) {
            System.out.println(ajuste.getFecha() + " | pago " + ajuste.getPagoCorregido().getNumero()
                    + " | $" + ajuste.getMontoAjuste() + " | " + ajuste.getMotivo());
        }
        System.out.println("IMPUTACIONES EFECTIVAS");
        for (ImputacionPago imputacion : cuenta.getImputacionesEfectivas()) {
            System.out.println("Pago " + imputacion.getPago().getNumero() + " | período " + imputacion.getExpensa().getPeriodo()
                    + " | intereses $" + imputacion.getMontoInteres() + " | capital $" + imputacion.getMontoCapital());
        }
    }

    private void registrarPago() {
        int lote = lote(); if (lote == 0) return;
        String fecha = fecha("Fecha del pago"); double importe = decimal("Importe: ");
        MedioPago medio = seleccionar("MedioPago", MedioPago.values());
        if (medio == null || finEntrada) return;
        System.out.println(controlador.registrarPago(actor, lote, fecha, importe, medio));
        completada();
    }

    private void ajustarPago() {
        int lote = lote(); if (lote == 0) return;
        Pago pago = seleccionar("Pago a corregir", controlador.consultarPagos(actor, lote, Fechas.hoy()));
        if (pago == null) return;
        String fecha = fecha("Fecha del ajuste"); double monto = decimal("Importe del ajuste con signo: ");
        String motivo = leer("Motivo: "); if (finEntrada) return;
        controlador.ajustarPago(actor, lote, pago.getNumero(), fecha, monto, motivo);
        completada();
    }

    private void mantenimiento() {
        while (!finEntrada) {
            menu("Equipos y mantenimiento", new String[] {"Consultar equipos, fallas y órdenes", "Agregar cámara",
                    "Agregar luminaria", "Informar falla", "Generar orden", "Asignar técnico", "Cerrar orden",
                    "Marcar equipo fuera de servicio"});
            int opcion = entero("Opción (0 para volver): ");
            if (opcion == 0 || finEntrada) return;
            try {
                switch (opcion) {
                    case 1: listarEquipos(); break;
                    case 2: agregarCamara(); break;
                    case 3: agregarLuminaria(); break;
                    case 4: informarFalla(); break;
                    case 5: generarOrden(); break;
                    case 6: asignarTecnico(); break;
                    case 7: cerrarOrden(); break;
                    case 8: fueraServicio(); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void listarEquipos() {
        System.out.println("EQUIPOS"); listar(controlador.listarEquipos());
        System.out.println("FALLAS"); listar(controlador.listarFallas());
        System.out.println("ÓRDENES");
        for (OrdenMantenimiento orden : controlador.listarOrdenes()) {
            System.out.println(orden + " | técnico " + orden.getEmpleadoAsignado() + " | reparación " + orden.getDetalleReparacion());
        }
    }

    private void agregarCamara() {
        String codigo = leer("Código: "), sector = leer("Sector: "), fecha = fecha("Fecha de instalación");
        String resolucion = leer("Resolución: "); int dias = entero("Días de grabación: ");
        if (finEntrada) return;
        controlador.agregarCamara(actor, codigo, sector, fecha, resolucion, dias);
        completada();
    }

    private void agregarLuminaria() {
        String codigo = leer("Código: "), sector = leer("Sector: "), fecha = fecha("Fecha de instalación");
        String lampara = leer("Tipo de lámpara: "); double potencia = decimal("Potencia W: ");
        if (finEntrada) return;
        controlador.agregarLuminaria(actor, codigo, sector, fecha, lampara, potencia);
        completada();
    }

    private void informarFalla() {
        Equipo equipo = seleccionar("Equipo", controlador.listarEquipos()); if (equipo == null) return;
        String inicio = leer("Inicio aaaa-MM-dd HH:mm (vacío si se desconoce): "), descripcion = leer("Descripción: ");
        if (finEntrada) return;
        System.out.println(controlador.registrarFalla(actor, equipo.getCodigo(), Fechas.ahora(), inicio, descripcion));
        completada();
    }

    private void generarOrden() {
        Falla falla = seleccionar("Falla", controlador.listarFallas()); if (falla == null) return;
        String trabajo = leer("Trabajo a realizar: "); if (finEntrada) return;
        System.out.println(controlador.generarOrden(actor, falla.getNumero(), Fechas.ahora(), trabajo));
        completada();
    }

    private void asignarTecnico() {
        OrdenMantenimiento orden = seleccionar("Orden", controlador.listarOrdenes()); if (orden == null) return;
        List<Personal> tecnicos = new ArrayList<>();
        for (Personal persona : controlador.listarPersonal()) {
            if (persona instanceof EmpleadoMantenimiento) tecnicos.add(persona);
        }
        Personal tecnico = seleccionar("Técnico", tecnicos); if (tecnico == null || finEntrada) return;
        controlador.asignarOrden(actor, orden.getNumero(), tecnico.getLegajo());
        completada();
    }

    private void cerrarOrden() {
        OrdenMantenimiento orden = seleccionar("Orden", controlador.listarOrdenes()); if (orden == null) return;
        String detalle = leer("Detalle de reparación: "); if (finEntrada) return;
        controlador.cerrarOrden(actor, orden.getNumero(), Fechas.ahora(), detalle);
        completada();
    }

    private void fueraServicio() {
        Equipo equipo = seleccionar("Equipo", controlador.listarEquipos()); if (equipo == null || finEntrada) return;
        controlador.marcarEquipoFueraServicio(actor, equipo.getCodigo());
        completada();
    }

    private void accesos() {
        while (!finEntrada) {
            menu("Visitas y accesos", new String[] {"Consultar autorizaciones, ingresos y egresos",
                    "Registrar proveedor habitual", "Autorizar visita o proveedor", "Consultar autorización por documento",
                    "Registrar ingreso", "Registrar egreso"});
            int opcion = entero("Opción (0 para volver): ");
            if (opcion == 0 || finEntrada) return;
            try {
                switch (opcion) {
                    case 1: listarAccesos(); break;
                    case 2: proveedorHabitual(); break;
                    case 3: autorizarVisita(); break;
                    case 4: consultarAutorizacion(); break;
                    case 5: registrarIngreso(); break;
                    case 6: registrarEgreso(); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void listarAccesos() {
        System.out.println("AUTORIZACIONES"); listar(controlador.listarAutorizaciones());
        System.out.println("INGRESOS Y EGRESOS"); listar(controlador.listarAccesos());
    }

    private void proveedorHabitual() {
        Persona persona = persona(); if (persona == null || finEntrada) return;
        controlador.identificarVisitante(actor, persona.getDocumento(), TipoVisitante.PROVEEDOR, true);
        completada();
    }

    private void autorizarVisita() {
        int lote = lote(); if (lote == 0) return;
        Persona persona = persona(); if (persona == null) return;
        TipoVisitante tipo = seleccionar("TipoVisitante", TipoVisitante.values()); if (tipo == null) return;
        String inicio = fecha("Fecha de inicio"), fin = fecha("Fecha de fin");
        String desde = leer("Hora desde HH:mm: "), hasta = leer("Hora hasta HH:mm: ");
        boolean recurrente = siNo("¿Autorización recurrente? (s/n): "); if (finEntrada) return;
        System.out.println(controlador.autorizarVisita(actor, persona.getDocumento(), tipo, lote, inicio, fin, desde, hasta, recurrente));
        completada();
    }

    private Guardia guardia() {
        if (!(actor instanceof Guardia)) throw new IllegalArgumentException("Cambie el actor a un guardia de portería.");
        return (Guardia) actor;
    }

    private void consultarAutorizacion() {
        Guardia guardia = guardia(); String documento = leer("Documento del visitante: "); if (finEntrada) return;
        listar(controlador.buscarAutorizaciones(guardia, documento, Fechas.ahora()));
    }

    private void registrarIngreso() {
        Guardia guardia = guardia(); String documento = leer("Documento del visitante: "); if (finEntrada) return;
        AutorizacionAcceso autorizacion = seleccionar("Autorización vigente", controlador.buscarAutorizaciones(guardia, documento, Fechas.ahora()));
        if (autorizacion == null || finEntrada) return;
        System.out.println(controlador.registrarIngreso(guardia, autorizacion.getNumero(), Fechas.ahora()));
        completada();
    }

    private void registrarEgreso() {
        Guardia guardia = guardia(); String documento = leer("Documento del visitante: "); if (finEntrada) return;
        System.out.println(controlador.registrarEgreso(guardia, documento, Fechas.ahora()));
        completada();
    }

    private Persona persona() { return seleccionar("Persona existente", controlador.listarPersonas()); }
    private Reclamo reclamo() { return seleccionar("Reclamo", controlador.listarReclamos(actor)); }
    private int lote() {
        Lote seleccionado = seleccionar("Lote", controlador.listarLotes());
        if (seleccionado == null) return 0;
        return seleccionado.getNumero();
    }

    private <T> T seleccionar(String nombre, T[] opciones) {
        List<T> lista = new ArrayList<>();
        for (T opcion : opciones) lista.add(opcion);
        return seleccionar(nombre, lista);
    }

    private <T> T seleccionar(String nombre, List<T> opciones) {
        if (finEntrada) return null;
        if (opciones.isEmpty()) throw new IllegalArgumentException("No hay opciones disponibles para " + nombre + ".");
        System.out.println("\n" + nombre + ":");
        for (int i = 0; i < opciones.size(); i++) System.out.println((i + 1) + ") " + opciones.get(i));
        while (!finEntrada) {
            int opcion = entero("Seleccione una opción (0 para cancelar): ");
            if (opcion == 0 || finEntrada) { System.out.println("Operación cancelada."); return null; }
            if (opcion >= 1 && opcion <= opciones.size()) return opciones.get(opcion - 1);
            System.out.println("Opción inválida.");
        }
        return null;
    }

    private void listar(List<?> elementos) {
        if (elementos.isEmpty()) System.out.println("Sin registros.");
        else for (Object elemento : elementos) System.out.println(elemento);
    }

    private String leer(String mensaje) {
        if (finEntrada) return "";
        System.out.print(mensaje);
        System.out.flush();
        if (!entrada.hasNextLine()) { finEntrada = true; return ""; }
        return entrada.nextLine().trim();
    }

    private String leer(String mensaje, String predeterminado) {
        String texto = leer(mensaje);
        if (texto.isEmpty()) return predeterminado;
        return texto;
    }

    private String fecha(String nombre) {
        return leer(nombre + " aaaa-MM-dd [" + Fechas.hoy() + "]: ", Fechas.hoy());
    }

    private int entero(String mensaje) {
        while (!finEntrada) {
            String texto = leer(mensaje); if (finEntrada) return 0;
            try { return Integer.parseInt(texto); }
            catch (NumberFormatException e) { System.out.println("Ingrese un número entero válido."); }
        }
        return 0;
    }

    private double decimal(String mensaje) {
        while (!finEntrada) {
            String texto = leer(mensaje); if (finEntrada) return 0;
            try {
                double numero = Double.parseDouble(texto.replace(',', '.'));
                if (Validaciones.esFinito(numero)) return numero;
            } catch (NumberFormatException e) {
                System.out.println("El valor ingresado no es un número.");
            }
            System.out.println("Ingrese un número válido; puede usar coma o punto decimal.");
        }
        return 0;
    }

    private boolean siNo(String mensaje) {
        while (!finEntrada) {
            String respuesta = leer(mensaje); if (finEntrada) return false;
            if (respuesta.equalsIgnoreCase("s")) return true;
            if (respuesta.equalsIgnoreCase("n")) return false;
            System.out.println("Responda s o n.");
        }
        return false;
    }
}
