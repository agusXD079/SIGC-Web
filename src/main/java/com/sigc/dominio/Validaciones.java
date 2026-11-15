package com.sigc.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validaciones reutilizables del dominio; todas lanzan {@link ReglaNegocioException}. */
public final class Validaciones {
    private Validaciones() {}

    /**
     * @param valor texto recibido
     * @param campo nombre para el mensaje
     * @param max largo maximo
     * @return texto sin espacios laterales
     */
    public static String texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank())
            throw new ReglaNegocioException(campo + " es obligatorio.");
        String limpio = valor.strip();
        if (limpio.length() > max)
            throw new ReglaNegocioException(campo + " admite hasta " + max + " caracteres.");
        return limpio;
    }

    /**
     * @param valor texto recibido, posiblemente vacio
     * @param campo nombre para el mensaje
     * @param max largo maximo
     * @return texto limpio o cadena vacia
     */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) return "";
        String limpio = valor.strip();
        if (limpio.length() > max)
            throw new ReglaNegocioException(campo + " admite hasta " + max + " caracteres.");
        return limpio;
    }

    /**
     * @param valor numero recibido
     * @param campo nombre para el mensaje
     * @param min minimo inclusivo
     * @param max maximo inclusivo
     * @return el mismo valor si es valido
     */
    public static int entero(int valor, String campo, int min, int max) {
        if (valor < min || valor > max)
            throw new ReglaNegocioException(
                    campo + " debe estar entre " + min + " y " + max + ".");
        return valor;
    }

    /**
     * @param valor importe recibido
     * @param campo nombre para el mensaje
     * @param maxInclusivo importe maximo admitido
     * @return importe mayor que cero con dos decimales
     */
    public static BigDecimal importe(BigDecimal valor, String campo, BigDecimal maxInclusivo) {
        if (valor == null) throw new ReglaNegocioException(campo + " es obligatorio.");
        BigDecimal redondeado = valor.setScale(2, RoundingMode.HALF_UP);
        if (redondeado.signum() <= 0)
            throw new ReglaNegocioException(campo + " debe ser mayor que cero.");
        if (redondeado.compareTo(maxInclusivo) > 0)
            throw new ReglaNegocioException(campo + " supera el máximo permitido.");
        return redondeado;
    }

    /**
     * @param valor objeto recibido
     * @param campo nombre para el mensaje
     * @param <T> tipo del objeto
     * @return el mismo objeto si no es nulo
     */
    public static <T> T requerido(T valor, String campo) {
        if (valor == null) throw new ReglaNegocioException(campo + " es obligatorio.");
        return valor;
    }

    /**
     * @param valor DNI con separadores opcionales (puntos, espacios o guiones)
     * @return DNI normalizado de 7 u 8 digitos
     */
    public static String dni(String valor) {
        if (valor == null || valor.isBlank()) throw new ReglaNegocioException("El DNI es obligatorio.");
        String digitos = valor.replaceAll("[.\\s-]", "");
        if (!digitos.matches("[0-9]{7,8}"))
            throw new ReglaNegocioException("El DNI debe tener 7 u 8 dígitos.");
        return digitos;
    }

    /**
     * @param valor correo opcional
     * @return correo limpio o cadena vacia
     */
    public static String email(String valor) {
        String limpio = textoOpcional(valor, "El correo", 120);
        if (!limpio.isEmpty() && !limpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new ReglaNegocioException("El correo no tiene un formato válido.");
        return limpio;
    }
}
