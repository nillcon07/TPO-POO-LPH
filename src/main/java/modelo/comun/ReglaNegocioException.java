package modelo.comun;

/** Una operación rechazada no debe modificar el estado del dominio. */
public class ReglaNegocioException extends IllegalArgumentException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
