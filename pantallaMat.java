import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class pantallaMat extends JFrame {
    public pantallaMat() {
        setTitle("Dashboard de Material Handler - MedSolutions");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Configura el logo y el título
        setLayout(new BorderLayout());
        JLabel labelTitulo = new JLabel("Bienvenido Material Handler", JLabel.CENTER);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(labelTitulo, BorderLayout.NORTH);

        // Configura el menú
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menuOpciones = new JMenu("Opciones");
        menuBar.add(menuOpciones);

        JMenuItem menuItemAlmacenes = new JMenuItem("Gestionar Almacenes");
        menuOpciones.add(menuItemAlmacenes);
        menuItemAlmacenes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaAlmacenes();
            }
        });

        JMenuItem menuItemCategorias = new JMenuItem("Gestionar Categorías");
        menuOpciones.add(menuItemCategorias);
        menuItemCategorias.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaCategorias();
            }
        });

        JMenuItem menuItemCompras = new JMenuItem("Gestionar Compras");
        menuOpciones.add(menuItemCompras);
        menuItemCompras.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaCompras();
            }
        });

        JMenuItem menuItemDetalleCompras = new JMenuItem("Gestionar Detalle de Compras");
        menuOpciones.add(menuItemDetalleCompras);
        menuItemDetalleCompras.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaDetalleCompras();
            }
        });

        JMenuItem menuItemEquipos = new JMenuItem("Gestionar Equipos");
        menuOpciones.add(menuItemEquipos);
        menuItemEquipos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaEquipos();
            }
        });

        JMenuItem menuItemProveedores = new JMenuItem("Gestionar Proveedores");
        menuOpciones.add(menuItemProveedores);
        menuItemProveedores.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaProveedores();
            }
        });

        JMenuItem menuItemRecepciones = new JMenuItem("Gestionar Recepciones");
        menuOpciones.add(menuItemRecepciones);
        menuItemRecepciones.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaRecepciones();
            }
        });

        JMenuItem menuItemSesion = new JMenuItem("Cerrar Sesión");
        menuOpciones.add(menuItemSesion);
        menuItemSesion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });

        // Panel con el logo
        JPanel logoPanel = new JPanel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("path_al_logo_de_MedSolutions.jpg"));
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(logoIcon);
        logoPanel.add(logoLabel);
        add(logoPanel, BorderLayout.CENTER);

    }

    private void cerrarSesion() {
        LoginPantalla pantalla = new LoginPantalla(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaAlmacenes() {
        pantallaAlmacenes pantalla = new pantallaAlmacenes(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaCategorias() {
        pantallaCategorias pantalla = new pantallaCategorias(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaCompras() {
        pantallaCompras pantalla = new pantallaCompras(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaDetalleCompras() {
        pantallaDetalleCompras pantalla = new pantallaDetalleCompras(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaEquipos() {
        pantallaEquipos pantalla = new pantallaEquipos(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaProveedores() {
        pantallaProveedores pantalla = new pantallaProveedores(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void abrirPantallaRecepciones() {
        pantallaRecepciones pantalla = new pantallaRecepciones(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new pantallaMat().setVisible(true);
        });
    }
}
