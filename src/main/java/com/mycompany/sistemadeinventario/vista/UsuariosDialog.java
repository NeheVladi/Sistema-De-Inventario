package com.mycompany.sistemadeinventario.vista;

import com.mycompany.sistemadeinventario.excepciones.ExcepcionGeneral;
import com.mycompany.sistemadeinventario.modelo.Usuario;
import com.mycompany.sistemadeinventario.servicios.ServicioUsuarios;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Administración de usuarios: tabla de registros con agregar, modificar y eliminar. */
public class UsuariosDialog extends JDialog {
    private final ServicioUsuarios servicio;
    private String usuarioSeleccionado;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Usuario", "Correo", "Rol"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblResumen = new JLabel(" ");

    private final JPanel panelForm = new JPanel(new GridBagLayout());
    private final JTextField txtUsuario = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);
    private final JPasswordField txtConfirmar = new JPasswordField(15);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    // Constructor. 
    public UsuariosDialog(Frame padre, ServicioUsuarios servicio) {
        super(padre, "Gestión de usuarios", true);
        this.servicio = servicio;

        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Administración de usuarios", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        add(titulo, BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.EAST);

        btnAgregar.addActionListener(e -> agregar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        actualizarEstado();
        recargar();
        setSize(840, 430);
        setLocationRelativeTo(padre);
    }

    // ---------- Interfaz---------

    // Construir la tabla.
    private JPanel construirTabla() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        p.add(lblResumen, BorderLayout.SOUTH);
        return p;
    }

    // Construir el formulario.
    private JPanel construirFormulario() {
        panelForm.setPreferredSize(new Dimension(330, 0));
        agregarFila(panelForm, 0, "Usuario:", txtUsuario);
        agregarFila(panelForm, 1, "Correo:", txtCorreo);
        agregarFila(panelForm, 2, "Contraseña:", txtClave);
        agregarFila(panelForm, 3, "Confirmar:", txtConfirmar);

        JLabel ayuda = new JLabel("<html><small>Al modificar, deje la contraseña vacía para conservar la actual.<br>"
                + "Nueva contraseña: mínimo 8 caracteres, con letras y números.</small></html>");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 4, 4, 4);
        panelForm.add(ayuda, c);

        JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        c.gridy = 5;
        panelForm.add(botones, c);

        c.gridy = 6;
        c.weighty = 1;
        panelForm.add(new JLabel(), c);
        return panelForm;
    }

    // Agrega las filas dentró de las tablas.
    private void agregarFila(JPanel panel, int fila, String etiqueta, Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    // ---------- Operaciones ----------

    // Método que agregar los usuarios.
    private void agregar() {
        try {
            servicio.crear(txtUsuario.getText(), txtCorreo.getText(),
                    txtClave.getPassword(), txtConfirmar.getPassword());
            limpiarFormulario();
            recargar();
            info("Usuario creado correctamente.");
        } catch (ExcepcionGeneral ex) {
            txtClave.setText("");
            txtConfirmar.setText("");
            aviso(ex.getMessage());
        }
    }

    // Método que modifica los usuarios.
    private void modificar() {
        if (usuarioSeleccionado == null) {
            aviso("Seleccione un usuario de la tabla.");
            return;
        }
        try {
            servicio.modificar(usuarioSeleccionado, txtUsuario.getText(), txtCorreo.getText(),
                    txtClave.getPassword(), txtConfirmar.getPassword());
            limpiarFormulario();
            recargar();
            info("Usuario modificado correctamente.");
        } catch (ExcepcionGeneral ex) {
            txtClave.setText("");
            txtConfirmar.setText("");
            aviso(ex.getMessage());
        }
    }

    // Método que elimina un usuario seleccionado.
    private void eliminar() {
        if (usuarioSeleccionado == null) {
            aviso("Seleccione un usuario de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario " + usuarioSeleccionado + "? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(usuarioSeleccionado);
            limpiarFormulario();
            recargar();
            info("Usuario eliminado correctamente.");
        } catch (ExcepcionGeneral ex) {
            aviso(ex.getMessage());
        }
    }

    // ---------- Apoyo ----------

    // Método para recargar la tabla.
    private void recargar() {
        try {
            List<Usuario> lista = servicio.listar();
            modelo.setRowCount(0);
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{u.getNombreUsuario(), u.getCorreo(),
                        u.esAdministrador() ? "Administrador" : "Usuario"});
            }
            lblResumen.setText("Usuarios registrados: " + lista.size());
        } catch (ExcepcionGeneral ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método que extrae la selección de la tabla.
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        usuarioSeleccionado = (String) modelo.getValueAt(m, 0);
        txtUsuario.setText(usuarioSeleccionado);
        txtCorreo.setText((String) modelo.getValueAt(m, 1));
        txtClave.setText("");
        txtConfirmar.setText("");
        actualizarEstado();
    }

    // Limpia el formulario.
    private void limpiarFormulario() {
        tabla.clearSelection();
        usuarioSeleccionado = null;
        txtUsuario.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
        txtConfirmar.setText("");
        actualizarEstado();
        txtUsuario.requestFocusInWindow();
    }

    // Actualiza los usuarios dentro de la tabla.
    private void actualizarEstado() {
        boolean haySeleccion = usuarioSeleccionado != null;
        btnAgregar.setEnabled(!haySeleccion);
        btnModificar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
        panelForm.setBorder(BorderFactory.createTitledBorder(
                haySeleccion ? "Modificación de usuario" : "Nuevo usuario"));
        panelForm.repaint();
    }

    // Avisos.
    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // Avisos.
    private void info(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Sistema de Inventario", JOptionPane.INFORMATION_MESSAGE);
    }
}
