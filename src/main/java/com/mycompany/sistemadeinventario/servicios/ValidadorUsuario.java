package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.modelo.Usuario;
import com.mycompany.sistemadeinventario.seguridad.PasswordUtil;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Reglas de validación compartidas para crear y modificar usuarios. */
public final class ValidadorUsuario {
    private static final Pattern PATRON_USUARIO = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidadorUsuario() { }

    /** Devuelve el nombre sin espacios en los extremos, o lanza la excepción si no es válido. */
    public static String validarNombre(String nombre) throws DatosInvalidosException {
        String n = nombre == null ? "" : nombre.trim();
        if (!PATRON_USUARIO.matcher(n).matches()) {
            throw new DatosInvalidosException(
                    "El usuario debe tener de 3 a 30 caracteres (letras, números, punto, guion o guion bajo).");
        }
        return n;
    }
    
    // Método para válidar el correo electrónico.
    public static String validarCorreo(String correo) throws DatosInvalidosException {
        String c = correo == null ? "" : correo.trim();
        if (!PATRON_CORREO.matcher(c).matches()) {
            throw new DatosInvalidosException("El correo electrónico no tiene un formato válido.");
        }
        return c;
    }

    // Validar clave.
    public static void validarClave(char[] clave, char[] confirmacion) throws DatosInvalidosException {
        if (clave == null || clave.length < 8) {
            throw new DatosInvalidosException("La contraseña debe tener al menos 8 caracteres.");
        }
        boolean letra = false;
        boolean numero = false;
        for (char c : clave) {
            if (Character.isWhitespace(c)) {
                throw new DatosInvalidosException("La contraseña no puede contener espacios.");
            }
            if (Character.isLetter(c)) letra = true;
            if (Character.isDigit(c)) numero = true;
        }
        if (!letra || !numero) {
            throw new DatosInvalidosException("La contraseña debe incluir al menos una letra y un número.");
        }
        if (!Arrays.equals(clave, confirmacion)) {
            throw new DatosInvalidosException("Las contraseñas no coinciden.");
        }
    }

    /** Valida todos los datos y construye el usuario con la contraseña ya convertida en hash. */
    public static Usuario nuevoUsuario(String nombre, String correo, char[] clave, char[] confirmacion, String rol)
            throws DatosInvalidosException {
        String n = validarNombre(nombre);
        String c = validarCorreo(correo);
        validarClave(clave, confirmacion);
        String sal = PasswordUtil.generarSal();
        return new Usuario(n, c, sal, PasswordUtil.hash(clave, sal), rol);
    }
}
