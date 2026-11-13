package com.mycompany.sistemadeinventario;

import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.persistencia.MongoConexion;
import com.mycompany.sistemadeinventario.servicios.ServicioAutenticacion;
import com.mycompany.sistemadeinventario.vista.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada: conecta con MongoDB y abre la pantalla de acceso. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Se usa el aspecto por defecto de Swing.
        }
        SwingUtilities.invokeLater(() -> {
            try {
                ServicioAutenticacion servicio = new ServicioAutenticacion();
                new LoginFrame(servicio).setVisible(true);
            } catch (ErrorBaseDatosException ex) {
                JOptionPane.showMessageDialog(null,
                        ex.getMessage() + "\n\nVerifique que MongoDB esté en ejecución "
                        + "(o defina la variable de entorno MONGO_URI).",
                        "Error de conexión", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(MongoConexion::cerrar));
    }
}
