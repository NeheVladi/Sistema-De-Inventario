package com.mycompany.sistemadeinventario.excepciones;

public class ExcepcionGeneral extends Exception {
    private String mensaje;

    public ExcepcionGeneral(String mensaje) {
        super(mensaje);
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
