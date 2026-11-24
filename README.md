## Integrantes

- Valentin Alejandro Nillcon — 1213259
- Benjamin Sitzerman — 1208780
- Marcos Lopez Quevedo — 1207164

## Descripción y Objetivo del proyecto

Sistema de gestión del barrio privado **"Los Aromos"**, desarrollado en Java aplicando el Paradigma Orientado a Objetos.

El sistema permite administrar:
- **Comunidad:** lotes (hasta 150), titulares, residentes y personal (administrador, guardias y técnicos de mantenimiento).
- **Reclamos:** registro, asignación según especialidad, cambios de estado, comentarios, reapertura dentro de los 5 días y alerta de reclamos de prioridad ALTA sin atención en 24 horas.
- **Expensas y pagos:** liquidación por superficie, pagos parciales, intereses por mora, ajustes y detección de morosidad.
- **Equipos y mantenimiento:** cámaras y luminarias, fallas, órdenes de trabajo y alertas por cámaras fuera de servicio.
- **Visitas y accesos:** autorizaciones de visitas y proveedores habituales, registro de ingresos y egresos en portería.
- **Reportes:** reclamos pendientes, morosidad, equipos fuera de servicio y notificaciones.

Cada usuario solo puede realizar las operaciones que corresponden a su rol. Los permisos se validan en el controlador y en el modelo, por lo que se cumplen en cualquier interfaz.

**Objetivo:** aplicar los contenidos de la materia (encapsulamiento, herencia, polimorfismo, clases abstractas, interfaces, enumerados, colecciones, excepciones, manejo de archivos, patrones GRASP, principios SOLID, arquitectura MVC, interfaces gráficas con JavaFX y el patrón Singleton) en un sistema completo con dos interfaces, terminal y gráfica, sobre el mismo modelo y controlador.

### Estructura
- `modelo`: entidades y reglas de negocio, organizadas por módulo (`comunidad`, `personas`, `reclamos`, `finanzas`, `mantenimiento`, `accesos`, `reportes`, `comun`) y la persistencia (`repositorios`, `archivos`).
- `controlador`: `GestorBarrioControlador`, que coordina los casos de uso y valida los permisos.
- `vista`: `VistaConsola` (terminal) y `vista.javafx` (interfaz gráfica).
- `principal`: puntos de entrada independientes (`Main` para la terminal y `MainJavaFX` para la interfaz gráfica), `CreadorControlador` y `DatosDemo`.
- `datos`: `personas.csv` (padrón de personas) y `barrio.dat` (se genera al guardar).

## Instrucciones de ejecución 

### 1. Requisitos previos para ejecutar el proyecto
    - Java Development Kit (JDK) versión 25 o superior (lo requiere el `pom.xml`).
    - Entorno de desarrollo integrado (IDE) compatible con proyectos Maven, como IntelliJ IDEA (recomendado).

### 2. Pasos para configurar el entorno
    1. Clonar o descargar el repositorio del proyecto en tu máquina local.
    2. Abrir el IDE (IntelliJ IDEA) y seleccionar "Open", buscando la carpeta raíz del proyecto (`TPO-POO-LPh` o equivalente) que contiene el archivo `pom.xml`.
    3. Esperar a que Maven importe y resuelva las dependencias automáticamente de forma correcta.
    4. Verificar que `src/main/java` esté marcada como carpeta de fuentes (en azul) y que el SDK del proyecto sea JDK 25 o superior.

### 3. Pasos para ejecutar el programa
    El sistema cuenta con dos interfaces independientes (terminal y gráfica) y pruebas de aceptación:
    
    - Ejecución de la Interfaz por Terminal (`VistaConsola`):
      1. Navegar dentro del proyecto hacia la clase principal de ejecución (`principal.Main`).
      2. Ejecutar el método `main` desde el IDE.
      3. El sistema iniciará en la terminal solicitando la fecha y hora de la sesión (en formato `aaaa-MM-dd HH:mm`) para inicializar el reloj del sistema de simulación.
      4. Utilizar las opciones numéricas del menú principal para interactuar con los diferentes módulos como Administrador, Guardia o Empleado.
    
    - Ejecución de la Interfaz Gráfica (JavaFX):
      1. Desde IntelliJ: ejecutar el método `main` de `principal.MainJavaFX` (no hace falta configurar módulos; JavaFX llega por Maven).
      2. Desde la terminal: `mvn javafx:run`.
      3. En la pantalla de ingreso elegir el usuario (Administrador, Titular, Residente, Guardia o Técnico) y la fecha y hora de la sesión.
      4. Cada usuario ve solo las pestañas que su rol permite; aun así, cada operación se vuelve a validar en el controlador y el modelo.
      5. Los datos se guardan en `datos/barrio.dat` después de cada operación y al cerrar la ventana.


    - Ejecución de Pruebas de Aceptación:
      1. Ubicar la clase `PruebasAceptacion.java` dentro del directorio de código fuente.
      2. Hacer clic derecho sobre la clase y seleccionar "Run 'PruebasAceptacion'" para validar de forma automatizada los casos de uso y reglas de negocio del sistema.

    - Datos de ejemplo:
      Si no existe `datos/barrio.dat`, el sistema arranca con un barrio de ejemplo (`DatosDemo`) y lo guarda en ese archivo con las modificaciones. Para volver a los datos de ejemplo, borrar `datos/barrio.dat`.
