package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando el usuario ingresado no existe.
public class UsuarioNoEncontradoException extends ExcepcionGeneral {
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
