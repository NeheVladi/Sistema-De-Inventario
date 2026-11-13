package com.mycompany.sistemadeinventario.excepciones;
// Esta clase envia un mensaje cuando el producto ingresado no se encuentra o no existe.
public class ProductoNoEncontradoException extends ExcepcionGeneral {
    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
