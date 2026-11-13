package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando el usuario esta duplicado.
public class UsuarioDuplicadoException extends ExcepcionGeneral {
    public UsuarioDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
