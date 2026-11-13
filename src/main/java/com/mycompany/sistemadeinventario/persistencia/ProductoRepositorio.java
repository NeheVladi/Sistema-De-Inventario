package com.mycompany.sistemadeinventario.persistencia;

import com.mycompany.sistemadeinventario.excepciones.CodigoDuplicadoException;
import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.ProductoNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Producto;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bson.Document;

/** Acceso a la colección "productos" de MongoDB. */
public class ProductoRepositorio {
    private final MongoCollection<Document> coleccion;

    public ProductoRepositorio() throws ErrorBaseDatosException {
        try {
            coleccion = MongoConexion.getBaseDatos().getCollection("productos");
            coleccion.createIndex(Indexes.ascending("codigoNorm"), new IndexOptions().unique(true));
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al preparar la colección de productos: " + e.getMessage());
        }
    }

    private static String normalizar(String codigo) {
        return codigo.replaceAll("[\\p{javaWhitespace}\\p{Z}]", "").toLowerCase(Locale.ROOT);
    }

    public List<Producto> listarTodos() throws ErrorBaseDatosException {
        List<Producto> lista = new ArrayList<>();
        try {
            for (Document d : coleccion.find().sort(Indexes.ascending("codigoNorm"))) {
                // Los productos guardados antes de existir la categoría no la tienen: se toma "General".
                lista.add(new Producto(
                        d.getString("codigo"),
                        d.getString("nombre"),
                        d.getString("descripcion"),
                        d.getString("categoria"),
                        ((Number) d.get("precio")).doubleValue(),
                        ((Number) d.get("stock")).intValue()));
            }
        } catch (MongoException | NullPointerException | ClassCastException e) {
            throw new ErrorBaseDatosException("Error al leer los productos: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Producto p) throws ErrorBaseDatosException, CodigoDuplicadoException {
        Document doc = new Document("codigo", p.getCodigo())
                .append("codigoNorm", normalizar(p.getCodigo()))
                .append("nombre", p.getNombre())
                .append("descripcion", p.getDescripcion())
                .append("categoria", p.getCategoria())
                .append("precio", p.getPrecio())
                .append("stock", p.getStock());
        try {
            coleccion.insertOne(doc);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new CodigoDuplicadoException("Producto ya registrado. Código: " + p.getCodigo());
            }
            throw new ErrorBaseDatosException("Error al guardar el producto: " + e.getMessage());
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al guardar el producto: " + e.getMessage());
        }
    }

    public void actualizar(Producto p) throws ErrorBaseDatosException, ProductoNoEncontradoException {
        try {
            long coincidencias = coleccion.updateOne(
                    Filters.eq("codigoNorm", normalizar(p.getCodigo())),
                    Updates.combine(
                            Updates.set("nombre", p.getNombre()),
                            Updates.set("descripcion", p.getDescripcion()),
                            Updates.set("categoria", p.getCategoria()),
                            Updates.set("precio", p.getPrecio()),
                            Updates.set("stock", p.getStock()))).getMatchedCount();
            if (coincidencias == 0) {
                throw new ProductoNoEncontradoException("No existe un producto con el código: " + p.getCodigo());
            }
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al actualizar el producto: " + e.getMessage());
        }
    }

    public void eliminar(String codigo) throws ErrorBaseDatosException, ProductoNoEncontradoException {
        try {
            long borrados = coleccion.deleteOne(Filters.eq("codigoNorm", normalizar(codigo))).getDeletedCount();
            if (borrados == 0) {
                throw new ProductoNoEncontradoException("No existe un producto con el código: " + codigo);
            }
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al eliminar el producto: " + e.getMessage());
        }
    }
}
