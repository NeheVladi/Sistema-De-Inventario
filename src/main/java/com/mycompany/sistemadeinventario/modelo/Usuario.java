package com.mycompany.sistemadeinventario.modelo;
// Estructura del usuario
public class Usuario {
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_USUARIO = "USUARIO";

    // Atributos
    private final String nombreUsuario;
    private final String correo;
    private final String salt;
    private final String passwordHash;
    private final String rol;

    // Constructor
    public Usuario(String nombreUsuario, String correo, String salt, String passwordHash, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    // Getters
    public String getNombreUsuario() { return nombreUsuario; }
    public String getCorreo() { return correo; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }

    // Valida si el administrador existe
    public boolean esAdministrador() {
        return ROL_ADMIN.equals(rol);
    }
}
