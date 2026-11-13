package com.mycompany.sistemadeinventario.vista;

import com.mycompany.sistemadeinventario.excepciones.ErrorArchivoException;
import com.mycompany.sistemadeinventario.modelo.Categorias;
import com.mycompany.sistemadeinventario.modelo.Producto;
import com.mycompany.sistemadeinventario.servicios.ServicioReportes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/** Ventana del módulo de reportes: vista previa, filtro por categoría y exportación a CSV. */
public class ReportesDialog extends JDialog {
    private final ServicioReportes servicio = new ServicioReportes();
    private final List<Producto> productos;
    private List<Producto> actual;

    private final JComboBox<String> cmbTipo = new JComboBox<>(ServicioReportes.TIPOS);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(Categorias.paraFiltro());
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Categoría", "Precio", "Stock", "Valor"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JLabel lblResumen = new JLabel(" ");

    // Constructor.
    public ReportesDialog(Frame padre, List<Producto> productos) {
        super(padre, "Reportes de inventario", true);
        this.productos = productos;

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        norte.add(new JLabel("Tipo de reporte:"));
        norte.add(cmbTipo);
        norte.add(new JLabel("Categoría:"));
        norte.add(cmbCategoria);

        JTable tabla = new JTable(modelo);
        tabla.getTableHeader().setReorderingAllowed(false);

        JButton btnExportar = new JButton("Exportar a CSV");
        JButton btnCerrar = new JButton("Cerrar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnExportar);
        botones.add(btnCerrar);

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.add(lblResumen, BorderLayout.NORTH);
        sur.add(botones, BorderLayout.SOUTH);

        setLayout(new BorderLayout(6, 6));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        cmbTipo.addActionListener(e -> generar());
        cmbCategoria.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> exportar());
        btnCerrar.addActionListener(e -> dispose());

        generar();
        setSize(820, 460);
        setLocationRelativeTo(padre);
    }

    // Genera la tabla de reportes donde se listan todos los productos.
    private void generar() {
        String categoria = (String) cmbCategoria.getSelectedItem();
        actual = servicio.generar(cmbTipo.getSelectedIndex(), categoria, productos);
        modelo.setRowCount(0);
        for (Producto p : actual) {
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(),
                    String.format("$%.2f", p.getPrecio()), p.getStock(),
                    String.format("$%.2f", ServicioReportes.valorDe(p))});
        }
        lblResumen.setText(servicio.resumen(actual));
    }

    // Método para exportar en csv.
    private void exportar() {
        if (actual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El reporte no tiene registros para exportar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("reporte_inventario.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        if (archivo.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo ya existe. ¿Desea reemplazarlo?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Path destino = archivo.toPath();
            servicio.exportarCsv(actual, destino);
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente:\n" + destino,
                    "Sistema de Inventario", JOptionPane.INFORMATION_MESSAGE);
        } catch (ErrorArchivoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
