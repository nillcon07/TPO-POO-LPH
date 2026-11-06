package modelo.comun;

public final class Validaciones {
    private Validaciones() { }

    //exigir -> base de varias validaciones ya que es la encargada de lanzar el error//
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new ReglaNegocioException(mensaje);
    }

    public static String validarDigito(String valor, String campo){
        String dato = texto(valor, campo);
        for (int i = 0; i < dato.length(); i++) {
            char caracter = dato.charAt(i);
            exigir(caracter >= '0' && caracter <= '9', campo + " debe contener solo dígitos.");
        }
        return dato;
    }

    public static String validarCaracter(String valor, String campo){
        String dato = texto(valor, campo);
        for (int i = 0; i < dato.length(); i++) {
            char caracter = dato.charAt(i);
            exigir(Character.isLetter(caracter) || caracter == ' ', campo + " debe contener caracteres.");
        }
        return  dato;
    }

    public static String validarCorreo(String valor, String campo) {
        String correo = texto(valor, campo);
        exigir(correo.contains("@"), campo + " debe contener @.");
        return correo;
    }

    public static String texto(String valor, String campo) {
        exigir(valor != null && !valor.trim().isEmpty(), campo + " es obligatorio.");
        return valor.trim();
    }

    public static <T> T requerido(T valor, String campo) {
        exigir(valor != null, campo + " es obligatorio.");
        return valor;
    }

    public static boolean esFinito(double valor) {
        return !Double.isNaN(valor) && !Double.isInfinite(valor);
    }

    public static double positivo(double valor, String campo) {
        exigir(esFinito(valor) && valor > 0, campo + " debe ser positivo y finito.");
        return valor;
    }

    public static double dinero(double valor) { return centavos(valor) / 100.0; }

    /** Redondeo decimal a centavos, alejando de cero los empates, con String y enteros. */
    public static long centavos(double valor) {
        exigir(esFinito(valor), "Importe no finito.");
        boolean negativo = valor < 0;
        String texto = Double.toString(negativo ? -valor : valor);
        int exponente = 0;
        int posicionE = texto.indexOf('E');
        if (posicionE >= 0) {
            exponente = Integer.parseInt(texto.substring(posicionE + 1));
            texto = texto.substring(0, posicionE);
        }
        int punto = texto.indexOf('.');
        if (punto < 0) punto = texto.length();
        String digitos = "";
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) != '.') digitos += texto.charAt(i);
        }
        int cantidad = punto + exponente + 2;
        long resultado = 0;
        for (int i = 0; i < cantidad; i++) {
            int digito = 0;
            if (i < digitos.length()) digito = digitos.charAt(i) - '0';
            exigir(resultado <= (Long.MAX_VALUE - digito) / 10, "Importe fuera de rango.");
            resultado = resultado * 10 + digito;
        }
        if (cantidad >= 0 && cantidad < digitos.length() && digitos.charAt(cantidad) >= '5') {
            exigir(resultado < Long.MAX_VALUE, "Importe fuera de rango.");
            resultado++;
        }
        return negativo ? -resultado : resultado;
    }
}
