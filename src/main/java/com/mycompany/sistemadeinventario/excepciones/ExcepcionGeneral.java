package com.mycompany.sistemadeinventario.excepciones;
// Esta clse envia mensajes cuando ocurren errores de tipo....
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
