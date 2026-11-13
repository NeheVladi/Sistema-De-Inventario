package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.CodigoDuplicadoException;
import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ProductoNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    private List<Producto> productos = new ArrayList<>();
    private Producto productoSeleccionado;

    public void registrarProducto(Producto p) throws DatosInvalidosException, CodigoDuplicadoException {
        if (p == null || !p.validarDatos()) {
            throw new DatosInvalidosException("Los datos del producto son inválidos.");
        }
        validarCodigoDisponible(p.getCodigo());
        productos.add(p);
    }

    // Se usa antes de pedir los demás datos y también al registrar desde archivo.
    public void validarCodigoDisponible(String codigo) throws DatosInvalidosException, CodigoDuplicadoException {
        if (codigo == null || normalizarCodigo(codigo).isEmpty()
                || codigo.contains("|") || codigo.contains("\n") || codigo.contains("\r")) {
            throw new DatosInvalidosException("El código del producto es inválido.");
        }
        for (Producto existente : productos) {
            if (codigosIguales(existente.getCodigo(), codigo)) {
                throw new CodigoDuplicadoException("Producto ya registrado. Código: " + existente.getCodigo());
            }
        }
    }

    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.replaceAll("[\\p{javaWhitespace}\\p{Z}]", "");
    }

    private static boolean codigosIguales(String primero, String segundo) {
        return normalizarCodigo(primero).equalsIgnoreCase(normalizarCodigo(segundo));
    }

    public void modificarProducto(Producto p) throws ProductoNoEncontradoException, DatosInvalidosException {
        if (p == null || !p.validarDatos()) {
            throw new DatosInvalidosException("Los datos del producto son inválidos.");
        }
        Producto existente = buscarProducto(p.getCodigo());
        existente.setNombre(p.getNombre());
        existente.setDescripcion(p.getDescripcion());
        existente.setCategoria(p.getCategoria());
        existente.setPrecio(p.getPrecio());
        existente.setStock(p.getStock());
    }

    public void eliminarProducto(String codigo) throws ProductoNoEncontradoException {
        Producto p = buscarProducto(codigo);
        productos.remove(p);
        if (productoSeleccionado == p) {
            productoSeleccionado = null;
        }
    }

    public Producto buscarProducto(String codigo) throws ProductoNoEncontradoException {
        codigo = codigo == null ? "" : codigo.trim();
        for (Producto p : productos) {
            if (codigosIguales(p.getCodigo(), codigo)) {
                productoSeleccionado = p;
                return p;
            }
        }
        throw new ProductoNoEncontradoException("No existe un producto con el código: " + codigo);
    }

    public List<Producto> consultarInventario() {
        return new ArrayList<>(productos);
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }
}
