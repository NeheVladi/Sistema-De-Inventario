package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.CodigoDuplicadoException;
import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ProductoNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

// Clase encargada de crear la lista de productos dentro del inventario.
public class Inventario {
    private List<Producto> productos = new ArrayList<>();
    private Producto productoSeleccionado;

    // Método encargado de registrar los productos y comprobar si está disponible el código.
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
    
    // Este método normaliza el código también válida si esta vacío.
    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.replaceAll("[\\p{javaWhitespace}\\p{Z}]", "");
    }
    
    // Método para evitar códigos duplicados.
    private static boolean codigosIguales(String primero, String segundo) {
        return normalizarCodigo(primero).equalsIgnoreCase(normalizarCodigo(segundo));
    }

    // Este método modifica los productos.
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
    // Elimina los productos a partir de su código.
    public void eliminarProducto(String codigo) throws ProductoNoEncontradoException {
        Producto p = buscarProducto(codigo);
        productos.remove(p);
        if (productoSeleccionado == p) {
            productoSeleccionado = null;
        }
    }
    // Buscar los productos a partir de su código.
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
    
    // Genera la lista de productos de inventario. 
    public List<Producto> consultarInventario() {
        return new ArrayList<>(productos);
    }
    
    // Retorna el producto seleccionado
    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }
}
