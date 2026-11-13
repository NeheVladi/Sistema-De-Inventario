package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando las credenciales no coinciden con los datos del usuario.
public class CredencialesInvalidasException extends ExcepcionGeneral {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
