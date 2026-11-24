## Integrantes

- Valentin Alejandro Nillcon — 1213259
- Benjamin Sitzerman — 1208780
- Marcos Lopez Quevedo — 1207164

## Descripción y Objetivo del proyecto

## Instrucciones de ejecución 

### 1. [Requisitos previos para ejecutar el proyecto.]
    - Java Development Kit (JDK) versión 17 o superior.
    - Entorno de desarrollo integrado (IDE) compatible con proyectos Maven, como IntelliJ IDEA (recomendado).

### 2. [Pasos para configurar el entorno.]
    1. Clonar o descargar el repositorio del proyecto en tu máquina local.
    2. Abrir el IDE (IntelliJ IDEA) y seleccionar "Open", buscando la carpeta raíz del proyecto (`TPO-POO-LPh` o equivalente) que contiene el archivo `pom.xml`.
    3. Esperar a que Maven importe y resuelva las dependencias automáticamente de forma correcta.
    4. Verificar que las carpetas principales (`src/main/java` para el código fuente y `src/main/resources` o `datos` para la persistencia) estén configuradas como fuentes raíz en el IDE.

### 3. [Pasos para ejecutar el programa.]
    El sistema cuenta con una interfaz de consola interactiva principal y clases de prueba configuradas:
    
    - Ejecución de la Interfaz por Terminal (`VistaConsola`):
      1. Navegar dentro del proyecto hacia la clase principal de ejecución (`principal.Main`).
      2. Ejecutar el método `main` desde el IDE.
      3. El sistema iniciará en la terminal solicitando la fecha y hora de la sesión (en formato `aaaa-MM-dd HH:mm`) para inicializar el reloj del sistema de simulación.
      4. Utilizar las opciones numéricas del menú principal para interactuar con los diferentes módulos como Administrador, Guardia o Empleado.
    
    - Ejecución de la Interfaz Gráfica (JavaFX):
      1. Desde IntelliJ: ejecutar el método `main` de `principal.LanzadorFX` (no hace falta configurar módulos; JavaFX llega por Maven).
      2. Desde la terminal: `mvn javafx:run`.
      3. En la pantalla de ingreso elegir el usuario (Administrador, Titular, Residente, Guardia o Técnico) y la fecha y hora de la sesión.
      4. Cada usuario ve solo las pestañas que su rol permite; aun así, cada operación se vuelve a validar en el controlador y el modelo.
      5. Los datos se guardan en `datos/barrio.dat` al pulsar "Guardar" o al cerrar la ventana.

    - Ejecución de la Interfaz por Terminal: ejecutar el `main` de `principal.Main`.

    - Ejecución de Pruebas de Aceptación:
      1. Ubicar la clase `PruebasAceptacion.java` dentro del directorio de código fuente.
      2. Hacer clic derecho sobre la clase y seleccionar "Run 'PruebasAceptacion'" para validar de forma automatizada los casos de uso y reglas de negocio del sistema.
