import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaPersonal extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnActualizar, btnCrear, btnCantidadPorDepartamento;

    public pantallaPersonal() {
        super("Datos de Personal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesite
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de personal", "Nombre", "Teléfono", "Correo",
                "Identificador de departamento" }, 0);
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

        btnActualizar = new JButton("Actualizar Personal");
        btnEliminar = new JButton("Eliminar Personal");
        btnCrear = new JButton("Crear Personal");
        btnCantidadPorDepartamento = new JButton("Cantidad de Personal por Departamento");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCantidadPorDepartamento);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarPersonal());
        btnEliminar.addActionListener(e -> eliminarPersonal());
        btnCrear.addActionListener(e -> crearPersonal());
        btnCantidadPorDepartamento.addActionListener(e -> mostrarCantidadPersonalPorDepartamento());

        loadPersonalData();

    }

    private void mostrarCantidadPersonalPorDepartamento() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        JDialog dialog = new JDialog(this, "Cantidad de Personal por Departamento", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(new Object[] { "ID Departamento", "Cantidad de Personal" }, 0);
        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table));

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{ ? = call obtener_personal_por_departamento() }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    int idDepartamento = rs.getInt("ID_DEPARTAMENTO");
                    int cantidadPersonal = rs.getInt("TOTAL_PERSONAL");
                    model.addRow(new Object[] { idDepartamento, cantidadPersonal });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar la cantidad de personal por departamento: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        dialog.setVisible(true);
    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadPersonalData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_personal(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    String idPersonal = rs.getString("Identificador de personal");
                    String nombre = rs.getString("Nombre");
                    String telefono = rs.getString("Telefono");
                    String correo = rs.getString("Correo");
                    String idDepartamento = rs.getString("Identificador de departamento");

                    model.addRow(new Object[] { idPersonal, nombre, telefono, correo, idDepartamento });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos del personal: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearPersonal() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField departmentIdField = new JTextField();

        Object[] message = {
                "Identificador de personal:", idField,
                "Nombre:", nameField,
                "Teléfono:", phoneField,
                "Correo:", emailField,
                "Identificador de departamento:", departmentIdField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Personal", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int idPersonal = Integer.parseInt(idField.getText().trim());
            String nombre = nameField.getText().trim();
            String telefono = phoneField.getText().trim();
            String correo = emailField.getText().trim();
            int idDepartamento = Integer.parseInt(departmentIdField.getText().trim());
            insertarPersonal(idPersonal, nombre, telefono, correo, idDepartamento);
        }
    }

    private void insertarPersonal(int idPersonal, String nombre, String telefono, String correo, int idDepartamento) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_personal(?, ?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, idPersonal);
            cstmt.setString(2, nombre);
            cstmt.setString(3, telefono);
            cstmt.setString(4, correo);
            cstmt.setInt(5, idDepartamento);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Personal creado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPersonalData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el personal: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPersonal() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idPersonal = (String) model.getValueAt(row, 0);
            String nombreActual = (String) model.getValueAt(row, 1);
            String telefonoActual = (String) model.getValueAt(row, 2);
            String correoActual = (String) model.getValueAt(row, 3);
            String idDepartamentoActual = (String) model.getValueAt(row, 4);

            JTextField nameField = new JTextField(nombreActual);
            JTextField phoneField = new JTextField(telefonoActual);
            JTextField emailField = new JTextField(correoActual);
            JTextField departmentIdField = new JTextField(idDepartamentoActual);

            Object[] message = {
                    "Identificador de personal:", idPersonal,
                    "Nombre:", nameField,
                    "Teléfono:", phoneField,
                    "Correo:", emailField,
                    "Identificador de departamento:", departmentIdField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Personal",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nuevoNombre = nameField.getText().trim();
                String nuevoTelefono = phoneField.getText().trim();
                String nuevoCorreo = emailField.getText().trim();
                int nuevoIdDepartamento = Integer.parseInt(departmentIdField.getText().trim());
                actualizarPersonalDB(idPersonal, nuevoNombre, nuevoTelefono, nuevoCorreo, nuevoIdDepartamento);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un personal para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarPersonalDB(String idPersonal, String nuevoNombre, String nuevoTelefono, String nuevoCorreo,
            int nuevoIdDepartamento) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_personal(?, ?, ?, ?, ?, ?) }")) {
            cstmt.setString(1, idPersonal);
            cstmt.setString(2, nuevoNombre);
            cstmt.setString(3, nuevoTelefono);
            cstmt.setString(4, nuevoCorreo);
            cstmt.setInt(5, nuevoIdDepartamento);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR); // Asumiendo que tu procedimiento espera un cursor de
                                                               // salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Personal actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPersonalData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el personal: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPersonal() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este personal?",
                    "Eliminar Personal",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idPersonal = (String) model.getValueAt(row, 0);
                eliminarPersonalDB(idPersonal);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un personal para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarPersonalDB(String idPersonal) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_personal(?, ?) }")) {
            cstmt.setString(1, idPersonal);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Personal eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eliminación imposible. Revise la gestión de despachos o usuarios relacionados y reintente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaPersonal frame = new pantallaPersonal();
            frame.setVisible(true);
        });
    }
}
