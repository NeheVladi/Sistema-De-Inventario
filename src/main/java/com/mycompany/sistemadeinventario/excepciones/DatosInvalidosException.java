package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje si los datos ingresados son invalidos(datos vacios, tipos de datos).
public class DatosInvalidosException extends ExcepcionGeneral {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
