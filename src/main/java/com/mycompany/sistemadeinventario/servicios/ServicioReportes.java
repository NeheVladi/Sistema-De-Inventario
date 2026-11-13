package com.mycompany.sistemadeinventario.servicios;

import com.mycompany.sistemadeinventario.excepciones.ErrorArchivoException;
import com.mycompany.sistemadeinventario.modelo.Categorias;
import com.mycompany.sistemadeinventario.modelo.Producto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Módulo de reportes: genera listados del inventario y los exporta a CSV. */
public class ServicioReportes {
    public static final int STOCK_BAJO = 10;

    public static final String[] TIPOS = {
        "Inventario general",
        "Stock bajo (< " + STOCK_BAJO + ")",
        "Productos sin stock",
        "Valorización (mayor a menor valor)"
    };

    /**
     * Devuelve los productos que corresponden al tipo de reporte (índice de TIPOS)
     * y a la categoría indicada (null o Categorias.TODAS incluye todas).
     */
    public List<Producto> generar(int tipo, String categoria, List<Producto> productos) {
        boolean todas = categoria == null || Categorias.TODAS.equals(categoria);
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (!todas && !p.getCategoria().equalsIgnoreCase(categoria)) {
                continue;
            }
            boolean incluir = switch (tipo) {
                case 1 -> p.getStock() > 0 && p.getStock() < STOCK_BAJO;
                case 2 -> p.getStock() == 0;
                default -> true;
            };
            if (incluir) {
                resultado.add(p);
            }
        }
        if (tipo == 3) {
            resultado.sort(Comparator.comparingDouble(ServicioReportes::valorDe).reversed());
        } else {
            resultado.sort(Comparator.comparing(p -> p.getCodigo().toLowerCase(Locale.ROOT)));
        }
        return resultado;
    }

    public static double valorDe(Producto p) {
        return p.getPrecio() * p.getStock();
    }

    public String resumen(List<Producto> lista) {
        long unidades = 0;
        double valor = 0;
        for (Producto p : lista) {
            unidades += p.getStock();
            valor += valorDe(p);
        }
        return String.format("Productos: %d  |  Unidades en stock: %d  |  Valor total: $%.2f",
                lista.size(), unidades, valor);
    }

    public void exportarCsv(List<Producto> lista, Path destino) throws ErrorArchivoException {
        List<String> lineas = new ArrayList<>();
        lineas.add("Codigo,Nombre,Descripcion,Categoria,Precio,Stock,Valor");
        for (Producto p : lista) {
            lineas.add(String.join(",",
                    celda(p.getCodigo()), celda(p.getNombre()), celda(p.getDescripcion()),
                    celda(p.getCategoria()),
                    String.format(Locale.ROOT, "%.2f", p.getPrecio()),
                    String.valueOf(p.getStock()),
                    String.format(Locale.ROOT, "%.2f", valorDe(p))));
        }
        try {
            Files.write(destino, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorArchivoException("No se pudo guardar el reporte: " + e.getMessage());
        }
    }

    /** Escapa comillas y evita que Excel interprete el texto como fórmula. */
    private static String celda(String texto) {
        String t = texto == null ? "" : texto;
        if (!t.isEmpty() && "=+-@".indexOf(t.charAt(0)) >= 0) {
            t = "'" + t;
        }
        return "\"" + t.replace("\"", "\"\"") + "\"";
    }
}
