package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando el sistema de archivo no responde.
public class ErrorArchivoException extends ExcepcionGeneral {
    public ErrorArchivoException(String mensaje) {
        super(mensaje);
    }
}
