package com.mycompany.sistemadeinventario.persistencia;

import com.mycompany.sistemadeinventario.excepciones.CodigoDuplicadoException;
import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ErrorArchivoException;
import com.mycompany.sistemadeinventario.modelo.Categorias;
import com.mycompany.sistemadeinventario.modelo.Producto;
import com.mycompany.sistemadeinventario.servicios.Inventario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Persistencia {
    private Path archivo;
    private String formato = "TXT delimitado por |";

    public Persistencia(String rutaArchivo) {
        this.archivo = Paths.get(rutaArchivo);
    }

    public boolean verificarExistencia() {
        return Files.exists(archivo);
    }

    public void guardar(Inventario inventario) throws ErrorArchivoException {
        if (inventario == null) {
            throw new ErrorArchivoException("No hay un inventario para guardar.");
        }
        List<String> lineas = new ArrayList<>();
        for (Producto p : inventario.consultarInventario()) {
            if (!p.validarDatos()) {
                throw new ErrorArchivoException("El producto contiene datos inválidos: " + p.getCodigo());
            }
            lineas.add(p.toLinea());
        }
        try {
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorArchivoException("Error durante la escritura del archivo: " + e.getMessage());
        }
    }

    public Inventario recuperar() throws ErrorArchivoException {
        Inventario inventario = new Inventario();

        // Archivo inexistente: se inicia con inventario vacío
        if (!verificarExistencia()) {
            System.out.println("[Aviso] El archivo no existe. Se inicia con un inventario vacío.");
            return inventario;
        }

        List<String> lineas;
        try {
            lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorArchivoException("Error durante la lectura del archivo: " + e.getMessage());
        }

        // Archivo vacío
        if (lineas.isEmpty()) {
            System.out.println("[Aviso] El archivo está vacío. Se inicia con un inventario vacío.");
            return inventario;
        }

        int numeroLinea = 0;
        for (String linea : lineas) {
            numeroLinea++;
            if (linea.trim().isEmpty()) continue;

            String[] partes = linea.split("\\|", -1);
            // 6 campos (con categoría) o 5 campos (formato anterior, sin categoría)
            if (partes.length != 5 && partes.length != 6) {
                throw new ErrorArchivoException("Formato incorrecto en la línea " + numeroLinea);
            }
            try {
                boolean conCategoria = partes.length == 6;
                Producto p = new Producto(partes[0], partes[1], partes[2],
                        conCategoria ? partes[3] : Categorias.DEFECTO,
                        Double.parseDouble(partes[conCategoria ? 4 : 3]),
                        Integer.parseInt(partes[conCategoria ? 5 : 4]));
                inventario.registrarProducto(p);
            } catch (NumberFormatException e) {
                throw new ErrorArchivoException("Número inválido en la línea " + numeroLinea);
            } catch (DatosInvalidosException | CodigoDuplicadoException e) {
                throw new ErrorArchivoException("Dato inválido en la línea " + numeroLinea + ": " + e.getMessage());
            }
        }
        return inventario;
    }
}
