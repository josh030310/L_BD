import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaPermisos extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnActualizar, btnCrear, btnObtenerDescripcionPermiso;

    public pantallaPermisos() {
        super("Datos de Permisos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de permisos", "Nombre de permiso",
                "Identificador de rol", "Descripción de permiso" }, 0);
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

        btnActualizar = new JButton("Actualizar Permiso");
        btnEliminar = new JButton("Eliminar Permiso");
        btnCrear = new JButton("Crear Permiso");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarPermiso());
        btnEliminar.addActionListener(e -> eliminarPermiso());
        btnCrear.addActionListener(e -> crearPermiso());

        // Cargar datos
        loadPermisosData();
    }

    private String obtenerDescripcionPermisoPorRol(int idRol) {
        String descripcionPermiso = "";
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{? = call obtener_descripcion_permiso_por_rol_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.VARCHAR);
            cstmt.setInt(2, idRol);
            cstmt.execute();

            descripcionPermiso = cstmt.getString(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener la descripción del permiso: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return descripcionPermiso;
    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadPermisosData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (CallableStatement cstmt = conn
                    .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_permisos(?) }")) {
                cstmt.registerOutParameter(1, OracleTypes.CURSOR);
                cstmt.execute();

                try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                    while (rs.next()) {
                        int idPermisos = rs.getInt("Identificador de permisos");
                        String nombrePermiso = rs.getString("Nombre de permiso");
                        int idRol = rs.getInt("Identificador de rol");
                        String descripcionPermiso = rs.getString("Descripcion de permiso");

                        model.addRow(new Object[] { idPermisos, nombrePermiso, idRol, descripcionPermiso });
                    }
                }
            }
        }

        catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de permisos: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void crearPermiso() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField roleField = new JTextField();
        JTextField descField = new JTextField();

        Object[] message = {
                "Identificador de permisos:", idField,
                "Nombre de permiso:", nameField,
                "Identificador de rol:", roleField,
                "Descripción de permiso:", descField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Permiso", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText().trim());
            String nombre = nameField.getText().trim();
            int idRol = Integer.parseInt(roleField.getText().trim());
            String descripcion = descField.getText().trim();
            insertarPermiso(id, nombre, idRol, descripcion);
        }
    }

    private void insertarPermiso(int id, String nombre, int idRol, String descripcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_permisos(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, id);
            cstmt.setString(2, nombre);
            cstmt.setInt(3, idRol);
            cstmt.setString(4, descripcion);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Permiso creado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPermisosData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el permiso: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPermiso() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int idPermiso = (Integer) model.getValueAt(row, 0);
            String nombreActual = (String) model.getValueAt(row, 1);
            int idRolActual = (Integer) model.getValueAt(row, 2);
            String descripcionActual = (String) model.getValueAt(row, 3);

            JTextField nameField = new JTextField(nombreActual);
            JTextField roleField = new JTextField(Integer.toString(idRolActual));
            JTextField descField = new JTextField(descripcionActual);
            Object[] message = {
                    "Identificador de permiso:", idPermiso,
                    "Nuevo nombre de permiso:", nameField,
                    "Nuevo identificador de rol:", roleField,
                    "Nueva descripción de permiso:", descField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Permiso",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nuevoNombre = nameField.getText().trim();
                int nuevoIdRol = Integer.parseInt(roleField.getText().trim());
                String nuevaDescripcion = descField.getText().trim();
                actualizarPermisoDB(idPermiso, nuevoNombre, nuevoIdRol, nuevaDescripcion);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un permiso para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarPermisoDB(int idPermiso, String nuevoNombre, int nuevoIdRol, String nuevaDescripcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_permisos(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, idPermiso);
            cstmt.setString(2, nuevoNombre);
            cstmt.setInt(3, nuevoIdRol);
            cstmt.setString(4, nuevaDescripcion);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR); // Asumiendo que tu procedimiento espera un cursor de
                                                               // salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Permiso actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPermisosData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el permiso: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPermiso() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este permiso?",
                    "Eliminar Permiso",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int idPermiso = (Integer) model.getValueAt(row, 0);
                eliminarPermisoDB(idPermiso);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un permiso para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarPermisoDB(int idPermiso) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_permisos(?, ?) }")) {
            cstmt.setInt(1, idPermiso);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Permiso eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el permiso: ", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaPermisos frame = new pantallaPermisos();
            frame.setVisible(true);
        });
    }
}
