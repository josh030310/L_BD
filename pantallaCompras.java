import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaCompras extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrearCompra;
    private JButton btnActualizar, btnTotalComprasProveedor;

    public pantallaCompras() {
        super("Datos de Compras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(
                new Object[] { "Identificador de compra", "Identificador de proveedor", "Fecha de compra" }, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Configura el menú
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menuOpciones = new JMenu("Opciones");
        menuBar.add(menuOpciones);

        JMenuItem menuItemPrincipal = new JMenuItem("Regresar");
        menuOpciones.add(menuItemPrincipal);
        menuItemPrincipal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPantallaMat();
            }
        });

        btnActualizar = new JButton("Actualizar Compra");
        btnEliminar = new JButton("Eliminar Compra");
        btnCrearCompra = new JButton("Crear Compra");
        btnTotalComprasProveedor = new JButton("Total de Compras por Proveedor");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrearCompra);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnTotalComprasProveedor);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarCompra());
        btnEliminar.addActionListener(e -> eliminarCompra());
        btnCrearCompra.addActionListener(e -> crearCompra());
        btnTotalComprasProveedor.addActionListener(e -> mostrarTotalComprasPorProveedor());

        loadComprasData();
    }

    private void mostrarTotalComprasPorProveedor() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        JDialog dialog = new JDialog(this, "Total de Compras por Proveedor", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(new Object[] { "ID Proveedor", "Total Compras" }, 0);
        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table));

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{ ? = call obtener_total_compras_por_proveedor() }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    int idProveedor = rs.getInt("ID_PROVEEDOR");
                    int totalCompras = rs.getInt("TOTAL_COMPRAS");
                    model.addRow(new Object[] { idProveedor, totalCompras });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el total de compras por proveedor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        dialog.setVisible(true);
    }

    private void abrirPantallaMat() {
        pantallaMat pantalla = new pantallaMat(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void crearCompra() {
        JTextField idCompraField = new JTextField(10);
        JTextField idProveedorField = new JTextField(10);
        JTextField fechaCompraField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Identificador de compra:"));
        panel.add(idCompraField);
        panel.add(new JLabel("Identificador de proveedor:"));
        panel.add(idProveedorField);
        panel.add(new JLabel("Fecha de compra (YYYY-MM-DD):"));
        panel.add(fechaCompraField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Crear Compra", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int idCompra = Integer.parseInt(idCompraField.getText());
            int idProveedor = Integer.parseInt(idProveedorField.getText());
            Date fechaCompra = Date.valueOf(fechaCompraField.getText());
            insertarCompra(idCompra, idProveedor, fechaCompra);
        }
    }

    private void insertarCompra(int idCompra, int idProveedor, Date fechaCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_compras(?, ?, ?, ?) }")) {
            cstmt.setInt(1, idCompra);
            cstmt.setInt(2, idProveedor);
            cstmt.setDate(3, new java.sql.Date(fechaCompra.getTime()));
            cstmt.registerOutParameter(4, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Compra creada exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadComprasData(); // Recargar los datos de la tabla para mostrar la nueva compra
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear la compra: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCompra() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar esta compra?",
                    "Eliminar Compra",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int idCompra = (Integer) model.getValueAt(row, 0);
                eliminarCompraDB(idCompra);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una compra para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarCompraDB(int idCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_compras(?, ?) }")) {
            cstmt.setInt(1, idCompra);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Registra el segundo parámetro como cursor de salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Compra eliminada exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Asegúrate de actualizar la tabla en el hilo de despacho de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    model.removeRow(selectedRow);
                }
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eliminación imposible. Revise la gestión de detalle de compras relacionados y reintente", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCompra() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int idCompra = (Integer) model.getValueAt(row, 0);
            int idProveedor = (Integer) model.getValueAt(row, 1);
            Date fechaCompra = (Date) model.getValueAt(row, 2);

            JTextField idProveedorField = new JTextField(String.valueOf(idProveedor));
            JTextField fechaCompraField = new JTextField(fechaCompra.toString());

            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Identificador de proveedor:"));
            panel.add(idProveedorField);
            panel.add(new JLabel("Fecha de compra (YYYY-MM-DD):"));
            panel.add(fechaCompraField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Actualizar Compra", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                actualizarCompraDB(idCompra, Integer.parseInt(idProveedorField.getText()),
                        Date.valueOf(fechaCompraField.getText()));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una compra para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarCompraDB(int idCompra, int idProveedor, Date fechaCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_compras(?, ?, ?, ?) }")) {
            cstmt.setInt(1, idCompra);
            cstmt.setInt(2, idProveedor);
            cstmt.setDate(3, fechaCompra);
            cstmt.registerOutParameter(4, OracleTypes.CURSOR); // Si estás usando el cursor en tu PL/SQL
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Compra actualizada exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadComprasData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la compra: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadComprasData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_compras(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    int idCompra = rs.getInt("Identificador de compra");
                    int idProveedor = rs.getInt("Identificador de proveedor");
                    Date fechaCompra = rs.getDate("Fecha de compra");
                    model.addRow(new Object[] { idCompra, idProveedor, fechaCompra });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de compras: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pantallaCompras().setVisible(true));
    }
}
