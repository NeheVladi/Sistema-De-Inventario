package com.mycompany.sistemadeinventario.seguridad;

import com.mycompany.sistemadeinventario.modelo.Usuario;

/** Mantiene el usuario autenticado. Sin sesión activa no se pueden abrir las funciones protegidas. */
public final class SesionActual {
    private static Usuario usuario;

    private SesionActual() { }

    public static synchronized void iniciar(Usuario u) { usuario = u; }
    public static synchronized void cerrar() { usuario = null; }
    public static synchronized boolean estaActiva() { return usuario != null; }
    public static synchronized Usuario getUsuario() { return usuario; }
}
