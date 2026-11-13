package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.CredencialesInvalidasException;
import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.UsuarioDuplicadoException;
import com.mycompany.sistemadeinventario.modelo.Usuario;
import com.mycompany.sistemadeinventario.persistencia.UsuarioRepositorio;
import com.mycompany.sistemadeinventario.seguridad.PasswordUtil;
import com.mycompany.sistemadeinventario.seguridad.SesionActual;

import java.util.Arrays;

/** Registro del primer administrador, inicio y cierre de sesión. */
public class ServicioAutenticacion {
    private static final String MENSAJE_LOGIN = "Usuario o contraseña incorrectos.";
    // Hash ficticio para que el tiempo de respuesta no revele si el usuario existe.
    private static final String SAL_FICTICIA = PasswordUtil.generarSal();

    private final UsuarioRepositorio repositorio;

    public ServicioAutenticacion() throws ErrorBaseDatosException {
        this.repositorio = new UsuarioRepositorio();
    }

    public boolean requiereRegistroAdministrador() throws ErrorBaseDatosException {
        return repositorio.contar() == 0;
    }

    /** Solo funciona mientras no exista ningún usuario: el primero queda como ADMIN. */
    public Usuario registrarAdministrador(String nombre, String correo, char[] clave, char[] confirmacion)
            throws DatosInvalidosException, UsuarioDuplicadoException, ErrorBaseDatosException {
        try {
            if (!requiereRegistroAdministrador()) {
                throw new DatosInvalidosException("Ya existe un administrador registrado.");
            }
            Usuario u = ValidadorUsuario.nuevoUsuario(nombre, correo, clave, confirmacion, Usuario.ROL_ADMIN);
            repositorio.insertar(u);
            return u;
        } finally {
            limpiar(clave);
            limpiar(confirmacion);
        }
    }

    public Usuario iniciarSesion(String identificador, char[] clave)
            throws CredencialesInvalidasException, ErrorBaseDatosException {
        try {
            Usuario u = repositorio.buscarPorIdentificador(identificador);
            if (u == null) {
                PasswordUtil.hash(clave, SAL_FICTICIA);
                throw new CredencialesInvalidasException(MENSAJE_LOGIN);
            }
            if (!PasswordUtil.verificar(clave, u.getSalt(), u.getPasswordHash())) {
                throw new CredencialesInvalidasException(MENSAJE_LOGIN);
            }
            SesionActual.iniciar(u);
            return u;
        } finally {
            limpiar(clave);
        }
    }

    public void cerrarSesion() {
        SesionActual.cerrar();
    }

    private static void limpiar(char[] datos) {
        if (datos != null) Arrays.fill(datos, '\0');
    }
}
