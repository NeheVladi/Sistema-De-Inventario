package com.mycompany.sistemadeinventario.vista;

import com.mycompany.sistemadeinventario.excepciones.CredencialesInvalidasException;
import com.mycompany.sistemadeinventario.excepciones.DatosInvalidosException;
import com.mycompany.sistemadeinventario.excepciones.ErrorBaseDatosException;
import com.mycompany.sistemadeinventario.excepciones.UsuarioDuplicadoException;
import com.mycompany.sistemadeinventario.seguridad.SesionActual;
import com.mycompany.sistemadeinventario.servicios.ServicioAutenticacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Pantalla de acceso. Si la base de datos no tiene usuarios, solicita las credenciales
 * del administrador (primer usuario); en caso contrario muestra el inicio de sesión.
 */
public class LoginFrame extends JFrame {
    private static final String CARD_LOGIN = "login";
    private static final String CARD_REGISTRO = "registro";

    private final ServicioAutenticacion servicio;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JButton btnIngresar = new JButton("Iniciar sesión");

    private final JTextField txtRegUsuario = new JTextField(20);
    private final JTextField txtRegCorreo = new JTextField(20);
    private final JPasswordField txtRegClave = new JPasswordField(20);
    private final JPasswordField txtRegConfirmar = new JPasswordField(20);
    private final JButton btnRegistrar = new JButton("Crear administrador");

    public LoginFrame(ServicioAutenticacion servicio) throws ErrorBaseDatosException {
        super("Sistema de Inventario - Acceso");
        this.servicio = servicio;

        contenedor.add(construirPanelLogin(), CARD_LOGIN);
        contenedor.add(construirPanelRegistro(), CARD_REGISTRO);
        add(contenedor);

        cartas.show(contenedor, servicio.requiereRegistroAdministrador() ? CARD_REGISTRO : CARD_LOGIN);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JPanel construirPanelLogin() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        p.add(titulo("Iniciar sesión"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        fila(form, 0, "Usuario o correo:", txtUsuario);
        fila(form, 1, "Contraseña:", txtClave);
        p.add(form, BorderLayout.CENTER);

        p.add(btnIngresar, BorderLayout.SOUTH);
        btnIngresar.addActionListener(e -> iniciarSesion());
        txtClave.addActionListener(e -> iniciarSesion());
        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());
        return p;
    }

    private JPanel construirPanelRegistro() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.add(titulo("Registro del administrador"), BorderLayout.NORTH);
        JLabel info = new JLabel("<html><center>No hay usuarios registrados.<br>"
                + "El primer usuario será el administrador del sistema.</center></html>", SwingConstants.CENTER);
        norte.add(info, BorderLayout.CENTER);
        p.add(norte, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        fila(form, 0, "Usuario:", txtRegUsuario);
        fila(form, 1, "Correo electrónico:", txtRegCorreo);
        fila(form, 2, "Contraseña:", txtRegClave);
        fila(form, 3, "Confirmar contraseña:", txtRegConfirmar);
        p.add(form, BorderLayout.CENTER);

        JLabel regla = new JLabel("<html><small>Mínimo 8 caracteres, con letras y números, sin espacios.</small></html>");
        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.add(regla, BorderLayout.NORTH);
        sur.add(btnRegistrar, BorderLayout.SOUTH);
        p.add(sur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> registrarAdministrador());
        txtRegConfirmar.addActionListener(e -> registrarAdministrador());
        return p;
    }

    private JLabel titulo(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        return l;
    }

    private void fila(JPanel panel, int fila, String etiqueta, java.awt.Component campo) {
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

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtClave.getPassword();
        if (usuario.isEmpty() || clave.length == 0) {
            java.util.Arrays.fill(clave, '\0');
            mensaje("Ingrese su usuario y contraseña.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            servicio.iniciarSesion(usuario, clave);
            MainFrame principal = new MainFrame(servicio);
            txtClave.setText("");
            dispose();
            principal.setVisible(true);
        } catch (CredencialesInvalidasException ex) {
            txtClave.setText("");
            mensaje(ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (ErrorBaseDatosException ex) {
            SesionActual.cerrar();
            mensaje(ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarAdministrador() {
        String usuario = txtRegUsuario.getText().trim();
        String correo = txtRegCorreo.getText().trim();
        char[] clave = txtRegClave.getPassword();
        char[] confirmacion = txtRegConfirmar.getPassword();
        if (usuario.isEmpty() || correo.isEmpty() || clave.length == 0 || confirmacion.length == 0) {
            java.util.Arrays.fill(clave, '\0');
            java.util.Arrays.fill(confirmacion, '\0');
            mensaje("Complete todos los campos.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            servicio.registrarAdministrador(usuario, correo, clave, confirmacion);
            txtRegUsuario.setText("");
            txtRegCorreo.setText("");
            txtRegClave.setText("");
            txtRegConfirmar.setText("");
            mensaje("Administrador creado correctamente. Ahora inicie sesión.", JOptionPane.INFORMATION_MESSAGE);
            txtUsuario.setText(usuario);
            cartas.show(contenedor, CARD_LOGIN);
            txtClave.requestFocusInWindow();
        } catch (DatosInvalidosException | UsuarioDuplicadoException | ErrorBaseDatosException ex) {
            txtRegClave.setText("");
            txtRegConfirmar.setText("");
            mensaje(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mensaje(String texto, int tipo) {
        JOptionPane.showMessageDialog(this, texto, "Sistema de Inventario", tipo);
    }
}
