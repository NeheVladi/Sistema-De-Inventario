package com.mycompany.sistemadeinventario.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de categorías de los productos.
 * Para agregar o quitar categorías basta con editar la lista LISTA.
 */
public final class Categorias {
    /** Opción para los filtros de búsqueda y reportes (no es una categoría real). */
    public static final String TODAS = "Todas";
    public static final String DEFECTO = "General";

    public static final List<String> LISTA = List.of(
            DEFECTO, "Alimentos", "Bebidas", "Electrónica", "Hogar",
            "Herramientas", "Limpieza", "Papelería", "Ropa", "Otros");

    private Categorias() { }

    /** Opciones para el formulario de agregar o modificar. */
    public static String[] paraFormulario() {
        return LISTA.toArray(new String[0]);
    }

    /** Opciones para filtros: "Todas" más todas las categorías. */
    public static String[] paraFiltro() {
        List<String> opciones = new ArrayList<>();
        opciones.add(TODAS);
        opciones.addAll(LISTA);
        return opciones.toArray(new String[0]);
    }
}
