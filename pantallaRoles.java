import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class pantallaRoles extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrear, btnActualizar;

    public pantallaRoles() {
        super("Datos de Roles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesit
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de rol", "Nombre de rol", "Descripción de rol" },
                0);
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
                abrirPantallaAdmi();
            }
        });

        btnActualizar = new JButton("Actualizar Rol");
        btnEliminar = new JButton("Eliminar Rol");
        btnCrear = new JButton("Crear Rol");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarRol());
        btnEliminar.addActionListener(e -> eliminarRol());
        btnCrear.addActionListener(e -> crearRol());

        // Cargar datos
        loadRolesData();
    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void crearRol() {
        JTextField idField = new JTextField();
        JTextField nombreField = new JTextField();
        JTextField descripcionField = new JTextField();
        Object[] message = {
                "Identificador de rol:", idField,
                "Nombre del rol:", nombreField,
                "Descripción del rol:", descripcionField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Rol", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText().trim());
            String nombreRol = nombreField.getText().trim();
            String descripcionRol = descripcionField.getText().trim();
            insertarRol(id, nombreRol, descripcionRol);
        }
    }

    private void insertarRol(int id, String nombre, String descripcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_roles(?, ?, ?,?) }")) {
            // Aquí asumimos que el ID del rol se autoincrementa o se calcula
            // automáticamente en otro lado
            cstmt.setInt(1, id);
            cstmt.setString(2, nombre);
            cstmt.setString(3, descripcion);
            cstmt.registerOutParameter(4, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Rol creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadRolesData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eliminación imposible. Revise la gestión de permisos o usuarios y reintente", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRolesData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_roles(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) { // Recupera el cursor como un ResultSet
                while (rs.next()) {
                    String idRol = rs.getString("Identificador de rol");
                    String nombreRol = rs.getString("Nombre de rol");
                    String descripcionRol = rs.getString("Descripcion de rol");

                    model.addRow(new Object[] { idRol, nombreRol, descripcionRol }); // Añade fila al modelo de la
                                                                                     // tabla
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de almacenes: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarRol() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idRol = (String) model.getValueAt(row, 0);
            String nombreActual = (String) model.getValueAt(row, 1);
            String descripcionActual = (String) model.getValueAt(row, 2);

            JTextField nombreField = new JTextField(nombreActual);
            JTextField descripcionField = new JTextField(descripcionActual);
            Object[] message = {
                    "Identificador de rol:", idRol,
                    "Nuevo nombre de rol:", nombreField,
                    "Nueva descripción de rol:", descripcionField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Rol", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nuevoNombre = nombreField.getText().trim();
                String nuevaDescripcion = descripcionField.getText().trim();
                actualizarRolDB(idRol, nuevoNombre, nuevaDescripcion);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un rol para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarRolDB(String idRol, String nuevoNombre, String nuevaDescripcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_roles(?, ?, ?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idRol));
            cstmt.setString(2, nuevoNombre);
            cstmt.setString(3, nuevaDescripcion);
            cstmt.registerOutParameter(4, OracleTypes.CURSOR); // Asumiendo que el procedimiento espera un cursor de
                                                               // salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Rol actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadRolesData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el rol: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarRol() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este rol?",
                    "Eliminar Rol",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idRol = (String) model.getValueAt(row, 0);
                eliminarRolDB(idRol);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un rol para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarRolDB(String idRol) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_roles(?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idRol));
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Rol eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el rol: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaRoles frame = new pantallaRoles();
            frame.setVisible(true);
        });
    }
}
