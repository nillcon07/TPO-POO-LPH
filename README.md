
# Sistema de Gestión de Barrios

## Modelo de entidades

### Barrio
- **Atributos:** nombre, lotes, personal, equipos, administrador.

### Lote
- **Atributos:** número, superficie (en metros cuadrados), persona (titular del lote).

### Persona
- **Atributos:** nombre, apellido, documento, teléfono, correo electrónico.

### Residente
- **Hereda de:** Persona.
- **Atributos:** lote en el que vive, tipo de residente (familiar o inquilino).

#### Familiar
- **Hereda de:** Residente.

#### Inquilino
- **Hereda de:** Residente.

### Visitante o Proveedor
- **Representación:** persona o referencia a un visitante/proveedor.

### Personal
- **Tipo:** clase abstracta.
- **Hereda de:** Persona.
- **Atributos:** legajo, fecha de ingreso.

#### Guardia
- **Hereda de:** Personal.
- **Atributos:** turno, puesto asignado (portería, ronda o monitoreo).

#### EmpleadoMantenimiento
- **Hereda de:** Personal.
- **Atributos:** especialidad (electricidad, plomería o jardinería).

### Administrador
- **Hereda de:** Personal.
- **Atributos:** lista de personal a cargo.
- **Responsabilidades:** coordinar al personal, asignar reclamos y aprobar órdenes de mantenimiento.

### Reclamo
- **Atributos:** número, fecha, tipo (seguridad, mantenimiento, convivencia o administrativo), descripción, prioridad, estado, lote, persona que lo presentó, empleado asignado, lista de cambios de estado y lista de comentarios.

### CambioEstado
- **Atributos:** fecha, nuevo estado, observación.

### Comentario
- **Atributos:** fecha, autor (Persona), texto.

### Expensa
- **Atributos:** período, importe base, fecha de vencimiento, estado, lote asignado, lista de pagos.

### Pago
- **Atributos:** fecha, importe total, monto imputado a interés, monto imputado a capital, medio de pago (efectivo, transferencia o tarjeta).

### AsientoAjuste
- **Atributos:** fecha, monto de ajuste, motivo, pago corregido.

### Sector
- **Atributos:** nombre del sector o ubicación dentro del barrio.

### Equipo
- **Tipo:** clase abstracta.
- **Atributos:** código, sector asignado, fecha de instalación, estado (operativo, con falla, en reparación o fuera de servicio).

#### Cámara
- **Hereda de:** Equipo.
- **Atributos:** resolución, días de grabación.

#### Luminaria
- **Hereda de:** Equipo.
- **Atributos:** tipo de lámpara, potencia (en watts).

### OrdenMantenimiento
- **Atributos:** número, fecha de apertura, fecha de cierre, prioridad, descripción del trabajo, detalle de la reparación, equipo afectado, empleado asignado.

### AutorizacionAcceso
- **Atributos:** fecha de inicio, fecha de fin, franja horaria, es recurrente, residente que autoriza, persona visitante/proveedor autorizada, lote que visita.

### RegistroAcceso
- **Atributos:** fecha y hora de ingreso, fecha y hora de egreso, autorización asociada, guardia de portería que registra.

## Clases de soporte y patrones GRASP

### GestorBarrioControlador
- **Patrón GRASP:** Controller.
- **Responsabilidad:** orquestar la lógica principal de los casos de uso.

### GeneradorReportes
- **Patrón GRASP:** Pure Fabrication.
- **Responsabilidades:**
  - Generar reportes de reclamos pendientes.
  - Generar reportes de morosidad por lote.
  - Generar reportes de equipos fuera de servicio.
