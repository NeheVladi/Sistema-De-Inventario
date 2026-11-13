package com.mycompany.sistemadeinventario.vista;

import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.ExcepcionGeneral;
import com.mycompany.sistemadeinventario.excepciones.ProductoNoEncontradoException;
import com.mycompany.sistemadeinventario.modelo.Categorias;
import com.mycompany.sistemadeinventario.modelo.Producto;
import com.mycompany.sistemadeinventario.modelo.Usuario;
import com.mycompany.sistemadeinventario.persistencia.ProductoRepositorio;
import com.mycompany.sistemadeinventario.seguridad.SesionActual;
import com.mycompany.sistemadeinventario.servicios.Inventario;
import com.mycompany.sistemadeinventario.servicios.ServicioAutenticacion;
import com.mycompany.sistemadeinventario.servicios.ServicioReportes;
import com.mycompany.sistemadeinventario.servicios.ServicioUsuarios;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** Ventana principal protegida: gestión del inventario. Requiere sesión activa. */
public class MainFrame extends JFrame {
    private static final int STOCK_BAJO = ServicioReportes.STOCK_BAJO;

    private final ServicioAutenticacion servicio;
    private final ProductoRepositorio repositorio;
    private Inventario inventario = new Inventario();
    private String codigoSeleccionado;
    private boolean actualizandoTabla;

    private final JLabel lblUsuario = new JLabel();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Descripción", "Categoría", "Precio", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    // Formulario (agregar y modificar)
    private final JTextField txtCodigo = new JTextField(15);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtDescripcion = new JTextField(15);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(Categorias.paraFormulario());
    private final JTextField txtPrecio = new JTextField(15);
    private final JTextField txtStock = new JTextField(15);

    // Búsqueda y filtros
    private final JTextField txtBuscar = new JTextField(12);
    private final JComboBox<String> cmbFiltroCategoria = new JComboBox<>(Categorias.paraFiltro());
    private final JComboBox<String> cmbFiltro = new JComboBox<>(new String[]{
            "Todos", "Con stock", "Stock bajo (< " + STOCK_BAJO + ")", "Sin stock"});
    private final JLabel lblResumen = new JLabel(" ");

    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnStock = new JButton("Ajustar stock");
    private final JButton btnLimpiar = new JButton("Limpiar");

    // Constructor.
    public MainFrame(ServicioAutenticacion servicio) throws ErrorBaseDatosException {
        super("Sistema de Inventario");
        if (!SesionActual.estaActiva()) {
            throw new IllegalStateException("Debe iniciar sesión para acceder al sistema.");
        }
        this.servicio = servicio;
        this.repositorio = new ProductoRepositorio();

        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirFormulario(), BorderLayout.EAST);

        configurarEventos();
        recargar();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 560);
        setLocationRelativeTo(null);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel construirEncabezado() {
        Usuario u = SesionActual.getUsuario();
        JPanel p = new JPanel(new BorderLayout());
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD));
        actualizarEncabezado();
        p.add(lblUsuario, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnReportes = new JButton("Reportes");
        btnReportes.addActionListener(e ->
                new ReportesDialog(this, inventario.consultarInventario()).setVisible(true));
        botones.add(btnReportes);
        if (u.esAdministrador()) {
            JButton btnUsuarios = new JButton("Usuarios");
            btnUsuarios.addActionListener(e -> abrirUsuarios());
            botones.add(btnUsuarios);
        }
        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.addActionListener(e -> cerrarSesion());
        botones.add(btnSalir);
        p.add(botones, BorderLayout.EAST);
        return p;
    }

    // Método que construye un panel general donde se ingresan los datos del producto.
    private JPanel construirCentro() {
        JPanel p = new JPanel(new BorderLayout(0, 6));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("Buscar (código o nombre):"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Categoría:"));
        filtros.add(cmbFiltroCategoria);
        filtros.add(new JLabel("Stock:"));
        filtros.add(cmbFiltro);
        p.add(filtros, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        p.add(lblResumen, BorderLayout.SOUTH);
        return p;
    }

    // Método que crea el formulario para la tabla.
    private JPanel construirFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Producto"));
        p.setPreferredSize(new Dimension(310, 0));

        agregarFila(p, 0, "Código:", txtCodigo);
        agregarFila(p, 1, "Nombre:", txtNombre);
        agregarFila(p, 2, "Descripción:", txtDescripcion);
        agregarFila(p, 3, "Categoría:", cmbCategoria);
        agregarFila(p, 4, "Precio ($):", txtPrecio);
        agregarFila(p, 5, "Stock:", txtStock);

        JPanel botones = new JPanel(new GridLayout(3, 2, 6, 6));
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnStock);
        botones.add(btnLimpiar);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(12, 4, 4, 4);
        p.add(botones, c);

        c.gridy = 7;
        c.weighty = 1;
        p.add(new JLabel(), c);
        return p;
    }

    // Agrega la fila dentro de la tabla.
    private void agregarFila(JPanel p, int fila, String etiqueta, Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        p.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(campo, c);
    }

    // Método que captura los eventos de los botones en general.
    private void configurarEventos() {
        btnRegistrar.addActionListener(e -> registrarProducto());
        btnModificar.addActionListener(e -> modificarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnStock.addActionListener(e -> ajustarStock());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizandoTabla) {
                cargarSeleccion();
            }
        });
        cmbFiltro.addActionListener(e -> refrescarTabla());
        cmbFiltroCategoria.addActionListener(e -> refrescarTabla());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refrescarTabla(); }
            public void removeUpdate(DocumentEvent e) { refrescarTabla(); }
            public void changedUpdate(DocumentEvent e) { refrescarTabla(); }
        });
        actualizarEstadoBotones();
    }

    // ---------- Operaciones ----------

    private void registrarProducto() {
        try {
            if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()) {
                aviso("El código y el nombre son obligatorios.");
                return;
            }
            // Se valida el código antes de procesar el resto de los datos.
            inventario.validarCodigoDisponible(txtCodigo.getText());
            Producto p = leerFormulario(txtCodigo.getText());
            inventario.registrarProducto(p);
            repositorio.insertar(p);
            limpiarFormulario();
            refrescarTabla();
            info("Producto registrado correctamente.");
        } catch (NumberFormatException ex) {
            aviso("El precio debe ser un número y el stock un número entero.");
        } catch (ErrorBaseDatosException ex) {
            errorBD(ex);
        } catch (ExcepcionGeneral ex) {
            aviso(ex.getMessage());
        }
    }

    // Válida la modificaciones del producto.
    private void modificarProducto() {
        if (codigoSeleccionado == null) {
            aviso("Seleccione un producto de la tabla.");
            return;
        }
        try {
            Producto p = leerFormulario(codigoSeleccionado);
            inventario.modificarProducto(p);
            repositorio.actualizar(p);
            refrescarTabla();
            info("Producto modificado correctamente.");
        } catch (NumberFormatException ex) {
            aviso("El precio debe ser un número y el stock un número entero.");
        } catch (ErrorBaseDatosException ex) {
            errorBD(ex);
        } catch (ExcepcionGeneral ex) {
            aviso(ex.getMessage());
        }
    }

    // Método para eliminar productos
    private void eliminarProducto() {
        if (codigoSeleccionado == null) {
            aviso("Seleccione un producto de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto " + codigoSeleccionado + "? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            inventario.eliminarProducto(codigoSeleccionado);
            repositorio.eliminar(codigoSeleccionado);
            limpiarFormulario();
            refrescarTabla();
            info("Producto eliminado correctamente.");
        } catch (ErrorBaseDatosException ex) {
            errorBD(ex);
        } catch (ExcepcionGeneral ex) {
            aviso(ex.getMessage());
        }
    }

    // Modifica la cantidad de productos dentro del inventario.
    private void ajustarStock() {
        if (codigoSeleccionado == null) {
            aviso("Seleccione un producto de la tabla.");
            return;
        }
        String entrada = JOptionPane.showInputDialog(this,
                "Cantidad a sumar (positiva) o restar (negativa) al stock:", "Ajustar stock",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null) {
            return;
        }
        try {
            int cantidad = Integer.parseInt(entrada.trim());
            Producto p = inventario.buscarProducto(codigoSeleccionado);
            p.actualizarStock(cantidad);
            repositorio.actualizar(p);
            refrescarTabla();
            txtStock.setText(String.valueOf(p.getStock()));
            info("Stock actualizado. Nuevo stock: " + p.getStock());
        } catch (NumberFormatException ex) {
            aviso("Ingrese un número entero válido.");
        } catch (ErrorBaseDatosException ex) {
            errorBD(ex);
        } catch (ExcepcionGeneral ex) {
            aviso(ex.getMessage());
        }
    }

    // Método muestra el nombre del usuario dentro de la interfaz.
    private void abrirUsuarios() {
        try {
            new UsuariosDialog(this, new ServicioUsuarios()).setVisible(true);
            actualizarEncabezado(); // por si el administrador modificó su propia cuenta
        } catch (ErrorBaseDatosException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método cerrar sesión.
    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        servicio.cerrarSesion();
        dispose();
        try {
            new LoginFrame(servicio).setVisible(true);
        } catch (ErrorBaseDatosException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // ---------- Apoyo leer formulario----------

    private Producto leerFormulario(String codigo) {
        double precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
        int stock = Integer.parseInt(txtStock.getText().trim());
        return new Producto(codigo, txtNombre.getText(), txtDescripcion.getText(),
                (String) cmbCategoria.getSelectedItem(), precio, stock);
    }

    /** Vuelve a cargar el inventario desde MongoDB (fuente de verdad). */
    private void recargar() {
        codigoSeleccionado = null;
        inventario = new Inventario();
        int omitidos = 0;
        try {
            List<Producto> lista = repositorio.listarTodos();
            for (Producto p : lista) {
                try {
                    inventario.registrarProducto(p);
                } catch (ExcepcionGeneral e) {
                    omitidos++;
                }
            }
        } catch (ErrorBaseDatosException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
        refrescarTabla();
        if (omitidos > 0) {
            aviso(omitidos + " registro(s) de la base de datos tienen datos inválidos y se omitieron.");
        }
    }

    /** Reconstruye la tabla aplicando la búsqueda y los filtros de categoría y stock. */
    private void refrescarTabla() {
        actualizandoTabla = true;
        try {
            modelo.setRowCount(0);
            String texto = txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
            int filtroStock = cmbFiltro.getSelectedIndex();
            String categoria = (String) cmbFiltroCategoria.getSelectedItem();
            boolean todasLasCategorias = categoria == null || Categorias.TODAS.equals(categoria);
            double valorTotal = 0;
            int total = 0;
            int filaSeleccionada = -1;
            for (Producto p : inventario.consultarInventario()) {
                total++;
                valorTotal += p.getPrecio() * p.getStock();
                boolean coincideTexto = texto.isEmpty()
                        || p.getCodigo().toLowerCase(Locale.ROOT).contains(texto)
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(texto);
                boolean coincideCategoria = todasLasCategorias || p.getCategoria().equalsIgnoreCase(categoria);
                boolean coincideStock = switch (filtroStock) {
                    case 1 -> p.getStock() > 0;
                    case 2 -> p.getStock() > 0 && p.getStock() < STOCK_BAJO;
                    case 3 -> p.getStock() == 0;
                    default -> true;
                };
                if (coincideTexto && coincideCategoria && coincideStock) {
                    if (codigoSeleccionado != null && p.getCodigo().equalsIgnoreCase(codigoSeleccionado)) {
                        filaSeleccionada = modelo.getRowCount();
                    }
                    modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getDescripcion(),
                            p.getCategoria(), String.format("$%.2f", p.getPrecio()), p.getStock()});
                }
            }
            lblResumen.setText(String.format("Mostrando %d de %d productos  |  Valor total del inventario: $%.2f",
                    modelo.getRowCount(), total, valorTotal));
            // Mantiene resaltado el producto en edición sin volver a cargar el formulario.
            if (filaSeleccionada >= 0) {
                int vista = tabla.convertRowIndexToView(filaSeleccionada);
                tabla.setRowSelectionInterval(vista, vista);
            }
        } finally {
            actualizandoTabla = false;
        }
        actualizarEstadoBotones();
    }

    // Método para cargar la selección de la tabla.
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String codigo = (String) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        try {
            Producto p = inventario.buscarProducto(codigo);
            codigoSeleccionado = p.getCodigo();
            txtCodigo.setText(p.getCodigo());
            txtNombre.setText(p.getNombre());
            txtDescripcion.setText(p.getDescripcion());
            seleccionarCategoria(p.getCategoria());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            txtStock.setText(String.valueOf(p.getStock()));
            actualizarEstadoBotones();
        } catch (ProductoNoEncontradoException ex) {
            aviso(ex.getMessage());
        }
    }

    /** Selecciona la categoría en el formulario; si no está en el catálogo, la agrega para no perderla. */
    private void seleccionarCategoria(String categoria) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).equalsIgnoreCase(categoria)) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
        cmbCategoria.addItem(categoria);
        cmbCategoria.setSelectedItem(categoria);
    }

    // Método limpiar formulario.
    private void limpiarFormulario() {
        tabla.clearSelection();
        codigoSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtPrecio.setText("");
        txtStock.setText("");
        actualizarEstadoBotones();
        txtCodigo.requestFocusInWindow();
    }

    // Métodos para actualizar los estados del boton. 
    private void actualizarEstadoBotones() {
        boolean haySeleccion = codigoSeleccionado != null;
        txtCodigo.setEditable(!haySeleccion); // el código no se modifica
        btnRegistrar.setEnabled(!haySeleccion);
        btnModificar.setEnabled(haySeleccion);
        btnStock.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion && SesionActual.getUsuario() != null
                && SesionActual.getUsuario().esAdministrador());
    }

    // Método actualiza el usuario.
    private void actualizarEncabezado() {
        Usuario u = SesionActual.getUsuario();
        if (u != null) {
            lblUsuario.setText("Usuario: " + u.getNombreUsuario() + "  (" + u.getRol() + ")");
        }
    }

    // Método que avisa si hay desconexión.
    private void errorBD(ErrorBaseDatosException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        recargar(); // mantiene la vista sincronizada con MongoDB
        limpiarFormulario();
    }
    
    // Avisos.
    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // Información.
    private void info(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Sistema de Inventario", JOptionPane.INFORMATION_MESSAGE);
    }
}
