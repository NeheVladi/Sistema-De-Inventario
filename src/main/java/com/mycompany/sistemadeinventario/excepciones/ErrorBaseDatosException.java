package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando la clase de datos no responde.
public class ErrorBaseDatosException extends ExcepcionGeneral {
    public ErrorBaseDatosException(String mensaje) {
        super(mensaje);
    }
}
