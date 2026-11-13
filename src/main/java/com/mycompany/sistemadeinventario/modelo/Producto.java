package com.mycompany.sistemadeinventario.modelo;

import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;

public class Producto {
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private double precio;
    private int stock;

    public Producto(String codigo, String nombre, String descripcion, String categoria, double precio, int stock) {
        this.codigo = codigo == null ? null : codigo.trim();
        this.nombre = nombre == null ? null : nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion;
        this.categoria = normalizarCategoria(categoria);
        this.precio = precio;
        this.stock = stock;
    }

    /** Constructor de compatibilidad: asigna la categoría por defecto. */
    public Producto(String codigo, String nombre, String descripcion, double precio, int stock) {
        this(codigo, nombre, descripcion, Categorias.DEFECTO, precio, stock);
    }

    private static String normalizarCategoria(String categoria) {
        return categoria == null || categoria.trim().isEmpty() ? Categorias.DEFECTO : categoria.trim();
    }

    public boolean validarDatos() {
        return codigo != null && !codigo.trim().isEmpty()
                && nombre != null && !nombre.trim().isEmpty()
                && Double.isFinite(precio) && precio >= 0
                && esTextoSeguro(codigo) && esTextoSeguro(nombre)
                && esTextoSeguro(descripcion) && esTextoSeguro(categoria)
                && stock >= 0;
    }

    public void actualizarStock(int cantidad) throws DatosInvalidosException {
        long nuevoStock = (long) stock + cantidad;
        if (nuevoStock < 0 || nuevoStock > Integer.MAX_VALUE) {
            throw new DatosInvalidosException("El stock resultante está fuera del rango permitido.");
        }
        this.stock = (int) nuevoStock;
    }

    private boolean esTextoSeguro(String texto) {
        return texto != null && !texto.contains("|")
                && !texto.contains("\n") && !texto.contains("\r");
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }

    public void setNombre(String nombre) { this.nombre = nombre == null ? null : nombre.trim(); }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion == null ? "" : descripcion; }
    public void setCategoria(String categoria) { this.categoria = normalizarCategoria(categoria); }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }

    // Formato para guardar en archivo: codigo|nombre|descripcion|categoria|precio|stock
    public String toLinea() {
        return codigo + "|" + nombre + "|" + descripcion + "|" + categoria + "|" + precio + "|" + stock;
    }

    @Override
    public String toString() {
        return String.format("Código: %s | Nombre: %s | Descripción: %s | Categoría: %s | Precio: $%.2f | Stock: %d",
                codigo, nombre, descripcion, categoria, precio, stock);
    }
}
