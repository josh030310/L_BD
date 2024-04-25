import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaDepartamentos extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar;
    private JButton btnActualizar;
    private JButton btnCrear, btnContarDepartamentos;

    public pantallaDepartamentos() {
        super("Datos de Departamentos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[] { "Identificador de departamento", "Nombre del departamento" }, 0);
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

        btnActualizar = new JButton("Actualizar Departamento");
        btnEliminar = new JButton("Eliminar Departamento");
        btnCrear = new JButton("Crear Departamento");
        btnContarDepartamentos = new JButton("Contar Departamentos");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnContarDepartamentos);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarDepartamento());
        btnEliminar.addActionListener(e -> eliminarDepartamento());
        btnCrear.addActionListener(e -> crearDepartamento());
        btnContarDepartamentos.addActionListener(e -> mostrarTotalDepartamentos());

        loadDepartamentosData();
    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadDepartamentosData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (CallableStatement cstmt = conn
                    .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_departamentos(?) }")) {
                cstmt.registerOutParameter(1, OracleTypes.CURSOR);
                cstmt.execute();

                try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                    while (rs.next()) {
                        int idDepartamento = rs.getInt("Identificador de departamento");
                        String nombreDepartamento = rs.getString("Nombre del departamento");

                        model.addRow(new Object[] { idDepartamento, nombreDepartamento });
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de departamentos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarTotalDepartamentos() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall("{ ? = call contar_total_departamentos() }")) {
            cstmt.registerOutParameter(1, OracleTypes.NUMBER);
            cstmt.execute();
            int totalDepartamentos = cstmt.getInt(1);
            JOptionPane.showMessageDialog(this, "Total de Departamentos: " + totalDepartamentos, "Total Departamentos",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el total de departamentos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearDepartamento() {
        JTextField nombreField = new JTextField();
        JTextField idField = new JTextField();
        Object[] message = {
                "ID del Departamento:", idField,
                "Nombre del Departamento:", nombreField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Departamento", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText());
            String nombre = nombreField.getText();
            insertarDepartamento(id, nombre);
        }
    }

    private void insertarDepartamento(int id, String nombre) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_departamentos(?, ?, ?) }")) {
            cstmt.setInt(1, id);
            cstmt.setString(2, nombre);
            cstmt.registerOutParameter(3, OracleTypes.CURSOR); // Registrar el parámetro OUT
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Departamento creado exitosamente.", "Creación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            loadDepartamentosData(); // Recargar datos de los departamentos
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el departamento: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarDepartamento() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este departamento?",
                    "Eliminar Departamento",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int idDepartamento = (Integer) model.getValueAt(row, 0);
                eliminarDepartamentoDB(idDepartamento);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un departamento para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarDepartamentoDB(int idDepartamento) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_departamentos(?, ?) }")) {
            cstmt.setInt(1, idDepartamento);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Registra el segundo parámetro como cursor de salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Departamento eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            SwingUtilities.invokeLater(() -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    model.removeRow(selectedRow);
                }
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eliminación imposible. Revise la gestión de personal o despachos relacionados y reintente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDepartamento() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int idDepartamento = (Integer) model.getValueAt(row, 0);
            String nombreDepartamento = (String) model.getValueAt(row, 1);

            String nuevoNombre = JOptionPane.showInputDialog(this,
                    "Actualizar el nombre del departamento:", nombreDepartamento);

            if (nuevoNombre != null && !nuevoNombre.isEmpty()) {
                actualizarDepartamentoDB(idDepartamento, nuevoNombre);
                loadDepartamentosData();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un departamento para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarDepartamentoDB(int idDepartamento, String nuevoNombre) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_departamentos(?, ?, ?) }")) {
            cstmt.setInt(1, idDepartamento);
            cstmt.setString(2, nuevoNombre);
            cstmt.registerOutParameter(3, OracleTypes.CURSOR); // si tu procedimiento lo requiere
            cstmt.execute();

            // Actualizar la fila en el modelo de la tabla
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.setValueAt(nuevoNombre, row, 1); // Asumiendo que la columna 1 es la que contiene el nombre del
                                                       // departamento
            }

            JOptionPane.showMessageDialog(this, "Departamento actualizado exitosamente.", "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // No es necesario recargar todos los datos desde la base de datos
            // loadDepartamentosData(); // Comentado porque ya no es necesario
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el departamento: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pantallaDepartamentos().setVisible(true));
    }
}
