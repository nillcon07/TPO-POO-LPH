# Sistema de Gestión de Barrios

# Entidades del Sistema de Gestión de Barrios

## 1. Barrio
- **Atributos:** nombre, lotes, personal, equipos, administrador.

## 2. Lote
- **Atributos:** numero, superficie (en metros cuadrados), persona (titular del lote).

## 3. Persona
- **Atributos:** nombre, apellido, documento, teléfono, correo electrónico.

## 4. Residente
- **Hereda de:** Persona.
- **Atributos:** lote (lote en el que vive), tipoResidente (familiar o inquilino).

### 4.1. Familiar
- **Hereda de:** Residente.

### 4.2. Inquilino
- **Hereda de:** Residente.

## 5. Visitante o Proveedor
- **Representación:** persona o referencia a un visitante/proveedor.

## 6. Administrador
- **Hereda de:** Personal.
- **Atributos:** lista de personal a cargo.
- **Responsabilidades:** coordinar al personal, asignar reclamos y aprobar órdenes de mantenimiento.

## 7. Personal
- **Tipo:** clase abstracta.
- **Hereda de:** Persona.
- **Atributos:** legajo, fecha de ingreso.

### 7.1. Guardia
- **Hereda de:** Personal.
- **Atributos:** turno, puesto asignado (portería, ronda o monitoreo).

### 7.2. EmpleadoMantenimiento
- **Hereda de:** Personal.
- **Atributos:** especialidad (electricidad, plomería o jardinería).

## 8. Reclamo
- **Atributos:** número, fecha, tipo (seguridad, mantenimiento, convivencia o administrativo), descripción, prioridad, estado, lote, persona que lo presentó, empleado asignado, lista de cambios de estado, lista de comentarios.

## 9. CambioEstado
- **Atributos:** fecha, nuevo estado, observación.

## 10. Comentario
- **Atributos:** fecha, autor (Persona), texto.

## 11. Expensa
- **Atributos:** período, importe base, fecha de vencimiento, estado, lote asignado, lista de pagos.

## 12. Pago
- **Atributos:** fecha, importe total, monto imputado a interés, monto imputado a capital, medio de pago (efectivo, transferencia o tarjeta).

## 13. AsientoAjuste
- **Atributos:** fecha, monto de ajuste, motivo, pago corregido.

## 14. Sector
- **Atributos:** nombre del sector o ubicación dentro del barrio.

## 15. Equipo
- **Tipo:** clase abstracta.
- **Atributos:** código, sector asignado, fecha de instalación, estado (operativo, con falla, en reparación o fuera de servicio).

### 15.1. Cámara
- **Hereda de:** Equipo.
- **Atributos:** resolución, días de grabación.

### 15.2. Luminaria
- **Hereda de:** Equipo.
- **Atributos:** tipo de lámpara, potencia (en watts).

## 16. OrdenMantenimiento
- **Atributos:** número, fecha de apertura, fecha de cierre, prioridad, descripción del trabajo, detalle de la reparación, equipo afectado, empleado asignado.

## 17. AutorizacionAcceso
- **Atributos:** fecha de inicio, fecha de fin, franja horaria, es recurrente, residente que autoriza, persona visitante/proveedor autorizada, lote que visita.

## 18. RegistroAcceso
- **Atributos:** fecha y hora de ingreso, fecha y hora de egreso, autorización asociada, guardia de portería que registra.

## 19. GestorBarrioControlador
- **Patrón GRASP:** Controller.
- **Responsabilidad:** orquestar la lógica principal de los casos de uso.

## 20. GeneradorReportes
- **Patrón GRASP:** Pure Fabrication.
- **Responsabilidades:**
  - Generar reportes de reclamos pendientes.
  - Generar reportes de morosidad por lote.
  - Generar reportes de equipos fuera de servicio.
