import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaProveedores extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrear, btnActualizar, btnVerificarExistencia;

    public pantallaProveedores() {
        super("Datos de Proveedores");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de proveedor", "Nombre de proveedor",
                "Teléfono de proveedor", "Correo de proveedor" }, 0);
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

        btnActualizar = new JButton("Actualizar Proveedor");
        btnEliminar = new JButton("Eliminar Proveedor");
        btnCrear = new JButton("Crear Proveedor");
        btnVerificarExistencia = new JButton("Verificar Existencia");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnVerificarExistencia);
        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarProveedor());
        btnEliminar.addActionListener(e -> eliminarProveedor());
        btnCrear.addActionListener(e -> crearProveedor());
        btnVerificarExistencia.addActionListener(e -> verificarExistenciaProveedor());

        loadProveedoresData();
    }

    private void verificarExistenciaProveedor() {
        String nombreProveedor = JOptionPane.showInputDialog(this, "Introduce el nombre del proveedor para verificar:");

        if (nombreProveedor != null && !nombreProveedor.isEmpty()) {
            String resultado = verificaExistenciaProveedor(nombreProveedor);

            if ("true".equals(resultado)) {
                JOptionPane.showMessageDialog(this, "El proveedor existe en la base de datos.", "Resultado",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if ("false".equals(resultado)) {
                JOptionPane.showMessageDialog(this, "El proveedor NO existe en la base de datos.", "Resultado",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ocurrió un error al verificar la existencia del proveedor.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String verificaExistenciaProveedor(String nombreProveedor) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        String resultado = "error";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{? = call verifica_existencia_proveedor_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.VARCHAR);
            cstmt.setString(2, nombreProveedor);
            cstmt.execute();

            resultado = cstmt.getString(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al verificar la existencia del proveedor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return resultado;
    }

    private void abrirPantallaMat() {
        pantallaMat pantalla = new pantallaMat(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadProveedoresData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_proveedores(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) { // Recupera el cursor como un ResultSet
                while (rs.next()) {
                    String idProveedor = rs.getString("Identificador de proveedor");
                    String nombreProveedor = rs.getString("Nombre de proveedor");
                    String telefonoProveedor = rs.getString("Telefono de proveedor");
                    String correoProveedor = rs.getString("Correo de proveedor");

                    model.addRow(new Object[] { idProveedor, nombreProveedor, telefonoProveedor, correoProveedor });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Eliminación imposible. Revise la gestión de compras y reintente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearProveedor() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] message = {
                "Identificador de proveedor:", idField,
                "Nombre de proveedor:", nameField,
                "Teléfono de proveedor:", phoneField,
                "Correo de proveedor:", emailField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Proveedor", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int idProveedor = Integer.parseInt(idField.getText().trim());
            String nombreProveedor = nameField.getText().trim();
            String telefonoProveedor = phoneField.getText().trim();
            String correoProveedor = emailField.getText().trim();
            insertarProveedor(idProveedor, nombreProveedor, telefonoProveedor, correoProveedor);
        }
    }

    private void insertarProveedor(int idProveedor, String nombreProveedor, String telefonoProveedor,
            String correoProveedor) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_proveedores(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, idProveedor);
            cstmt.setString(2, nombreProveedor);
            cstmt.setString(3, telefonoProveedor);
            cstmt.setString(4, correoProveedor);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Proveedor creado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadProveedoresData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el proveedor: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProveedor() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idProveedor = (String) model.getValueAt(row, 0);
            String nombreActual = (String) model.getValueAt(row, 1);
            String telefonoActual = (String) model.getValueAt(row, 2);
            String correoActual = (String) model.getValueAt(row, 3);

            JTextField nameField = new JTextField(nombreActual);
            JTextField phoneField = new JTextField(telefonoActual);
            JTextField emailField = new JTextField(correoActual);
            Object[] message = {
                    "Identificador de proveedor:", idProveedor,
                    "Nuevo nombre de proveedor:", nameField,
                    "Nuevo teléfono de proveedor:", phoneField,
                    "Nuevo correo de proveedor:", emailField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Proveedor",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nuevoNombre = nameField.getText().trim();
                String nuevoTelefono = phoneField.getText().trim();
                String nuevoCorreo = emailField.getText().trim();
                actualizarProveedorDB(idProveedor, nuevoNombre, nuevoTelefono, nuevoCorreo);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proveedor para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarProveedorDB(String idProveedor, String nuevoNombre, String nuevoTelefono,
            String nuevoCorreo) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_proveedores(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idProveedor));
            cstmt.setString(2, nuevoNombre);
            cstmt.setString(3, nuevoTelefono);
            cstmt.setString(4, nuevoCorreo);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Proveedor actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadProveedoresData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el proveedor: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProveedor() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este proveedor?",
                    "Eliminar Proveedor",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idProveedor = (String) model.getValueAt(row, 0);
                eliminarProveedorDB(idProveedor);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proveedor para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarProveedorDB(String idProveedor) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_proveedores(?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idProveedor));
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Proveedor eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el proveedor: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaProveedores frame = new pantallaProveedores();
            frame.setVisible(true);
        });
    }
}
