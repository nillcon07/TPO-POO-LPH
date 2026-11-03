package modelo.comun;

/** Fechas en String y cálculos con enteros: calendario gregoriano, sin bibliotecas de fechas. */
public final class Fechas {
    private static String momentoActual;

    private Fechas() { }

    private static boolean digitos(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) < '0' || texto.charAt(i) > '9') return false;
        }
        return true;
    }

    private static boolean bisiesto(int anio) {
        return anio % 400 == 0 || anio % 4 == 0 && anio % 100 != 0;
    }

    private static int diasDelMes(int anio, int mes) {
        if (mes == 2) return bisiesto(anio) ? 29 : 28;
        if (mes == 4 || mes == 6 || mes == 9 || mes == 11) return 30;
        return 31;
    }

    public static String periodo(String valor) {
        Validaciones.exigir(valor != null && valor.length() == 7 && valor.charAt(4) == '-',
                "Período inválido; use aaaa-MM.");
        Validaciones.exigir(digitos(valor.substring(0, 4)) && digitos(valor.substring(5, 7)),
                "Período inválido; use aaaa-MM.");
        int anio = Integer.parseInt(valor.substring(0, 4));
        int mes = Integer.parseInt(valor.substring(5, 7));
        Validaciones.exigir(anio >= 1 && mes >= 1 && mes <= 12, "Período inválido; use aaaa-MM.");
        return valor;
    }

    public static String fecha(String valor) {
        Validaciones.exigir(valor != null && valor.length() == 10 && valor.charAt(7) == '-',
                "Fecha inválida; use aaaa-MM-dd.");
        periodo(valor.substring(0, 7));
        Validaciones.exigir(digitos(valor.substring(8, 10)), "Día inválido.");
        int anio = Integer.parseInt(valor.substring(0, 4));
        int mes = Integer.parseInt(valor.substring(5, 7));
        int dia = Integer.parseInt(valor.substring(8, 10));
        Validaciones.exigir(dia >= 1 && dia <= diasDelMes(anio, mes), "El día no existe en ese mes.");
        return valor;
    }

    public static String hora(String valor) {
        Validaciones.exigir(valor != null && valor.length() == 5 && valor.charAt(2) == ':',
                "Hora inválida; use HH:mm.");
        Validaciones.exigir(digitos(valor.substring(0, 2)) && digitos(valor.substring(3, 5)),
                "Hora inválida; use HH:mm.");
        int horas = Integer.parseInt(valor.substring(0, 2));
        int minutos = Integer.parseInt(valor.substring(3, 5));
        Validaciones.exigir(horas <= 23 && minutos <= 59, "Hora inválida; use HH:mm.");
        return valor;
    }

    public static String momento(String valor) {
        Validaciones.exigir(valor != null && valor.length() == 16 && valor.charAt(10) == ' ',
                "Fecha y hora inválidas; use aaaa-MM-dd HH:mm.");
        fecha(valor.substring(0, 10));
        hora(valor.substring(11, 16));
        return valor;
    }

    public static String dia(String valor) {
        momento(valor);
        return valor.substring(0, 10);
    }

    public static String inicioPeriodo(String valor) { return periodo(valor) + "-01"; }
    public static String vencimientoPeriodo(String valor) { return periodo(valor) + "-10"; }

    private static long numeroDia(String valor) {
        fecha(valor);
        int anio = Integer.parseInt(valor.substring(0, 4));
        int mes = Integer.parseInt(valor.substring(5, 7));
        int dia = Integer.parseInt(valor.substring(8, 10));
        long anteriores = anio - 1;
        long total = anteriores * 365 + anteriores / 4 - anteriores / 100 + anteriores / 400;
        for (int m = 1; m < mes; m++) total += diasDelMes(anio, m);
        return total + dia - 1;
    }

    private static int minutoDelDia(String valor) {
        hora(valor);
        return Integer.parseInt(valor.substring(0, 2)) * 60 + Integer.parseInt(valor.substring(3, 5));
    }

    public static long dias(String desde, String hasta) { return numeroDia(hasta) - numeroDia(desde); }

    public static long minutosEntre(String desde, String hasta) {
        momento(desde);
        momento(hasta);
        long dias = dias(desde.substring(0, 10), hasta.substring(0, 10));
        return dias * 24 * 60 + minutoDelDia(hasta.substring(11)) - minutoDelDia(desde.substring(11));
    }

    public static int compararFechas(String primera, String segunda) {
        long diferencia = dias(segunda, primera);
        if (diferencia < 0) return -1;
        if (diferencia > 0) return 1;
        return 0;
    }

    public static int compararHoras(String primera, String segunda) {
        return minutoDelDia(primera) - minutoDelDia(segunda);
    }

    public static void noAnterior(String anterior, String nuevo) {
        Validaciones.exigir(minutosEntre(anterior, nuevo) >= 0,
                "La fecha no puede ser anterior al último evento.");
    }

    public static void establecerMomento(String valor) {
        momento(valor);
        momentoActual = valor;
    }

    public static String ahora() {
        Validaciones.exigir(momentoActual != null, "Primero indique la fecha y hora de la sesión.");
        return momentoActual;
    }

    public static String hoy() { return dia(ahora()); }
}
