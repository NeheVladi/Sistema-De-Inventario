package com.mycompany.sistemadeinventario.persistencia;

import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

/**
 * Administra la conexión única a MongoDB.
 * La URI se toma de la variable de entorno MONGO_URI (por defecto, MongoDB local).
 */
public final class MongoConexion {
    public static final String NOMBRE_BD = "inventario_db";
    private static final String URI_POR_DEFECTO = "mongodb://localhost:27017";

    private static MongoClient cliente;
    private static MongoDatabase baseDatos;

    private MongoConexion() { }

    // Establece la conexion
    public static synchronized MongoDatabase getBaseDatos() throws ErrorBaseDatosException {
        if (baseDatos != null) {
            return baseDatos;
        }
        try {
            String uri = System.getenv("MONGO_URI");
            if (uri == null || uri.isBlank()) {
                uri = URI_POR_DEFECTO;
            }
            cliente = MongoClients.create(MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .applyToClusterSettings(b -> b.serverSelectionTimeout(5, TimeUnit.SECONDS))
                    .build());
            MongoDatabase bd = cliente.getDatabase(NOMBRE_BD);
            bd.runCommand(new Document("ping", 1)); // verifica que el servidor responde
            baseDatos = bd;
            return baseDatos;
        } catch (MongoException | IllegalArgumentException e) {
            cerrar();
            throw new ErrorBaseDatosException("No se pudo conectar con MongoDB: " + e.getMessage());
        }
    }

    public static synchronized void cerrar() {
        if (cliente != null) {
            cliente.close();
        }
        cliente = null;
        baseDatos = null;
    }
}
