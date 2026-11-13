package com.mycompany.sistemadeinventario.excepciones;
// Esta clase captura los codigos duplicados para mostrar un mensaje
public class CodigoDuplicadoException extends ExcepcionGeneral {
    public CodigoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
