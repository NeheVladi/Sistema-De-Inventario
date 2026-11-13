package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.ExcepcionGeneral;
import com.mycompany.sistemadeinventario.excepciones.UsuarioNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Usuario;
import com.mycompany.sistemadeinventario.persistencia.UsuarioRepositorio;
import com.mycompany.sistemadeinventario.seguridad.PasswordUtil;
import com.mycompany.sistemadeinventario.seguridad.SesionActual;

import java.util.Arrays;
import java.util.List;

/** Administración de usuarios (listar, crear, modificar, eliminar). Solo disponible para el administrador. */
public class ServicioUsuarios {
    private final UsuarioRepositorio repositorio;

    public ServicioUsuarios() throws ErrorBaseDatosException {
        this.repositorio = new UsuarioRepositorio();
    }

    private Usuario exigirAdministrador() throws DatosInvalidosException {
        Usuario actual = SesionActual.getUsuario();
        if (actual == null || !actual.esAdministrador()) {
            throw new DatosInvalidosException("Solo el administrador puede gestionar usuarios.");
        }
        return actual;
    }

    public List<Usuario> listar() throws ExcepcionGeneral {
        exigirAdministrador();
        return repositorio.listarTodos();
    }

    /** Los usuarios creados desde aquí tienen el rol USUARIO. */
    public Usuario crear(String nombre, String correo, char[] clave, char[] confirmacion)
            throws ExcepcionGeneral {
        try {
            exigirAdministrador();
            Usuario u = ValidadorUsuario.nuevoUsuario(nombre, correo, clave, confirmacion, Usuario.ROL_USUARIO);
            repositorio.insertar(u);
            return u;
        } finally {
            limpiar(clave);
            limpiar(confirmacion);
        }
    }

    /** Si la contraseña se deja vacía, se conserva la actual. El rol no cambia. */
    public Usuario modificar(String nombreOriginal, String nombre, String correo,
                             char[] clave, char[] confirmacion) throws ExcepcionGeneral {
        try {
            Usuario actual = exigirAdministrador();
            Usuario original = repositorio.buscarPorIdentificador(nombreOriginal);
            if (original == null) {
                throw new UsuarioNoEncontradoException("El usuario seleccionado ya no existe.");
            }
            String nuevoNombre = ValidadorUsuario.validarNombre(nombre);
            String nuevoCorreo = ValidadorUsuario.validarCorreo(correo);

            String sal = original.getSalt();
            String hash = original.getPasswordHash();
            boolean cambiaClave = (clave != null && clave.length > 0)
                    || (confirmacion != null && confirmacion.length > 0);
            if (cambiaClave) {
                ValidadorUsuario.validarClave(clave, confirmacion);
                sal = PasswordUtil.generarSal();
                hash = PasswordUtil.hash(clave, sal);
            }

            Usuario nuevo = new Usuario(nuevoNombre, nuevoCorreo, sal, hash, original.getRol());
            repositorio.actualizar(original.getNombreUsuario(), nuevo);

            // Si el administrador modificó su propia cuenta, la sesión debe reflejar los datos nuevos.
            if (actual.getNombreUsuario().equalsIgnoreCase(original.getNombreUsuario())) {
                SesionActual.iniciar(nuevo);
            }
            return nuevo;
        } finally {
            limpiar(clave);
            limpiar(confirmacion);
        }
    }

    public void eliminar(String nombreUsuario) throws ExcepcionGeneral {
        Usuario actual = exigirAdministrador();
        Usuario objetivo = repositorio.buscarPorIdentificador(nombreUsuario);
        if (objetivo == null) {
            throw new UsuarioNoEncontradoException("El usuario seleccionado ya no existe.");
        }
        if (actual.getNombreUsuario().equalsIgnoreCase(objetivo.getNombreUsuario())) {
            throw new DatosInvalidosException("No puede eliminar su propia cuenta mientras tiene la sesión iniciada.");
        }
        if (objetivo.esAdministrador()) {
            throw new DatosInvalidosException("Las cuentas de administrador no se pueden eliminar.");
        }
        repositorio.eliminar(objetivo.getNombreUsuario());
    }

    private static void limpiar(char[] datos) {
        if (datos != null) Arrays.fill(datos, '\0');
    }
}
