package com.mycompany.sistemadeinventario.persistencia;

import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.UsuarioDuplicadoException;
import com.mycompany.sistemadeinventario.excepciones.UsuarioNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Usuario;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.bson.Document;

/** Acceso a la colección "usuarios" de MongoDB. */
public class UsuarioRepositorio {
    private static final String MENSAJE_DUPLICADO = "El nombre de usuario o el correo ya está registrado.";

    private final MongoCollection<Document> coleccion;

    public UsuarioRepositorio() throws ErrorBaseDatosException {
        try {
            coleccion = MongoConexion.getBaseDatos().getCollection("usuarios");
            IndexOptions unico = new IndexOptions().unique(true);
            coleccion.createIndex(Indexes.ascending("usuarioLower"), unico);
            coleccion.createIndex(Indexes.ascending("correoLower"), unico);
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al preparar la colección de usuarios: " + e.getMessage());
        }
    }

    private static Usuario aUsuario(Document doc) {
        return new Usuario(doc.getString("usuario"), doc.getString("correo"),
                doc.getString("salt"), doc.getString("passwordHash"), doc.getString("rol"));
    }

    public long contar() throws ErrorBaseDatosException {
        try {
            return coleccion.countDocuments();
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al consultar usuarios: " + e.getMessage());
        }
    }

    public List<Usuario> listarTodos() throws ErrorBaseDatosException {
        List<Usuario> lista = new ArrayList<>();
        try {
            for (Document d : coleccion.find().sort(Indexes.ascending("usuarioLower"))) {
                lista.add(aUsuario(d));
            }
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al leer los usuarios: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Usuario u) throws ErrorBaseDatosException, UsuarioDuplicadoException {
        Document doc = new Document("usuario", u.getNombreUsuario())
                .append("usuarioLower", u.getNombreUsuario().toLowerCase(Locale.ROOT))
                .append("correo", u.getCorreo())
                .append("correoLower", u.getCorreo().toLowerCase(Locale.ROOT))
                .append("salt", u.getSalt())
                .append("passwordHash", u.getPasswordHash())
                .append("rol", u.getRol())
                .append("fechaCreacion", new Date());
        try {
            coleccion.insertOne(doc);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new UsuarioDuplicadoException(MENSAJE_DUPLICADO);
            }
            throw new ErrorBaseDatosException("Error al guardar el usuario: " + e.getMessage());
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al guardar el usuario: " + e.getMessage());
        }
    }

    /** Reemplaza los datos del usuario identificado por su nombre original. El rol no se modifica. */
    public void actualizar(String nombreOriginal, Usuario u)
            throws ErrorBaseDatosException, UsuarioDuplicadoException, UsuarioNoEncontradoException {
        try {
            long coincidencias = coleccion.updateOne(
                    Filters.eq("usuarioLower", nombreOriginal.toLowerCase(Locale.ROOT)),
                    Updates.combine(
                            Updates.set("usuario", u.getNombreUsuario()),
                            Updates.set("usuarioLower", u.getNombreUsuario().toLowerCase(Locale.ROOT)),
                            Updates.set("correo", u.getCorreo()),
                            Updates.set("correoLower", u.getCorreo().toLowerCase(Locale.ROOT)),
                            Updates.set("salt", u.getSalt()),
                            Updates.set("passwordHash", u.getPasswordHash()))).getMatchedCount();
            if (coincidencias == 0) {
                throw new UsuarioNoEncontradoException("El usuario " + nombreOriginal + " ya no existe.");
            }
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new UsuarioDuplicadoException(MENSAJE_DUPLICADO);
            }
            throw new ErrorBaseDatosException("Error al actualizar el usuario: " + e.getMessage());
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    public void eliminar(String nombreUsuario) throws ErrorBaseDatosException, UsuarioNoEncontradoException {
        try {
            long borrados = coleccion.deleteOne(
                    Filters.eq("usuarioLower", nombreUsuario.toLowerCase(Locale.ROOT))).getDeletedCount();
            if (borrados == 0) {
                throw new UsuarioNoEncontradoException("El usuario " + nombreUsuario + " ya no existe.");
            }
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    /** Busca por nombre de usuario o correo (sin distinguir mayúsculas). */
    public Usuario buscarPorIdentificador(String identificador) throws ErrorBaseDatosException {
        String id = identificador.trim().toLowerCase(Locale.ROOT);
        try {
            Document doc = coleccion.find(Filters.or(
                    Filters.eq("usuarioLower", id),
                    Filters.eq("correoLower", id))).first();
            return doc == null ? null : aUsuario(doc);
        } catch (MongoException e) {
            throw new ErrorBaseDatosException("Error al consultar el usuario: " + e.getMessage());
        }
    }
}
