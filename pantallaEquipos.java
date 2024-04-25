import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class pantallaEquipos extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnActualizar, btnCrear, btnVerificarDisponibilidad, btnVerificarExistencia;

    public pantallaEquipos() {
        super("Datos de Equipos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[] { "ID Equipo", "Nombre", "Cantidad Disponible", "ID Categoría" }, 0);
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

        btnActualizar = new JButton("Actualizar Equipo");
        btnEliminar = new JButton("Eliminar Equipo");
        btnCrear = new JButton("Crear Equipo");
        // FUNCION
        btnVerificarDisponibilidad = new JButton("Verificar disponibilidad");
        btnVerificarExistencia = new JButton("Verificar Existencia");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnVerificarDisponibilidad);
        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarEquipo());
        btnEliminar.addActionListener(e -> eliminarEquipo());
        btnCrear.addActionListener(e -> crearEquipo());
        btnVerificarDisponibilidad.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int idCategoria = (Integer) model.getValueAt(row, 3);
                int cantidadDisponible = obtenerCantidadEquiposDisponibles(idCategoria);
                JOptionPane.showMessageDialog(this,
                        "Cantidad de equipos disponibles en esta categoría: " + cantidadDisponible, "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un equipo para verificar.", "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Cargar datos
        loadEquiposData();
    }

    private void abrirPantallaMat() {
        pantallaMat pantalla = new pantallaMat(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadEquiposData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (CallableStatement cstmt = conn
                    .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_equipos(?) }")) {
                cstmt.registerOutParameter(1, OracleTypes.CURSOR);
                cstmt.execute();

                try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                    while (rs.next()) {
                        int idEquipo = rs.getInt("Identificador de equipo");
                        String nombreEquipo = rs.getString("Nombre del equipo");
                        int cantidadDisponible = rs.getInt("Cantidad disponible");
                        int idCategoria = rs.getInt("Identificador de categoria");

                        model.addRow(new Object[] { idEquipo, nombreEquipo, cantidadDisponible, idCategoria });
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de equipos: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void crearEquipo() {

        JTextField nombreField = new JTextField();
        JTextField cantidadField = new JTextField();
        JTextField categoriaField = new JTextField();

        Object[] message = {

                "Nombre del Equipo:", nombreField,
                "Cantidad Disponible:", cantidadField,
                "ID Categoría:", categoriaField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Equipo", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            insertarEquipo(

                    nombreField.getText(),
                    Integer.parseInt(cantidadField.getText()),
                    Integer.parseInt(categoriaField.getText()));
        }
    }

    private void insertarEquipo(String nombre, int cantidad, int categoria) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_equipos(?, ?, ?, ?) }")) {

            cstmt.setString(1, nombre);
            cstmt.setInt(2, cantidad);
            cstmt.setInt(3, categoria);
            cstmt.registerOutParameter(4, OracleTypes.CURSOR); // Si tu procedimiento lo requiere

            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Equipo creado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadEquiposData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el equipo: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEquipo() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int idEquipo = (Integer) model.getValueAt(row, 0);
            String nombreActual = (String) model.getValueAt(row, 1);
            int cantidadActual = (Integer) model.getValueAt(row, 2);
            int idCategoriaActual = (Integer) model.getValueAt(row, 3);

            JTextField nombreField = new JTextField(nombreActual);
            JTextField cantidadField = new JTextField(String.valueOf(cantidadActual));
            JTextField idCategoriaField = new JTextField(String.valueOf(idCategoriaActual));

            Object[] message = {
                    "Nombre del Equipo:", nombreField,
                    "Cantidad Disponible:", cantidadField,
                    "ID Categoría:", idCategoriaField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Equipo",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nuevoNombre = nombreField.getText().trim();
                int nuevaCantidad = Integer.parseInt(cantidadField.getText().trim());
                int nuevaCategoria = Integer.parseInt(idCategoriaField.getText().trim());
                actualizarEquipoDB(idEquipo, nuevoNombre, nuevaCantidad, nuevaCategoria);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un equipo para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarEquipoDB(int idEquipo, String nuevoNombre, int nuevaCantidad, int nuevaCategoria) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_equipos(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, idEquipo);
            cstmt.setString(2, nuevoNombre);
            cstmt.setInt(3, nuevaCantidad);
            cstmt.setInt(4, nuevaCategoria);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR); // Si el procedimiento espera un cursor como salida

            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Equipo actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadEquiposData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el equipo: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEquipo() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int idEquipo = (Integer) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar el equipo con ID: " + idEquipo + "?",
                    "Eliminar Equipo",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                eliminarEquipoDB(idEquipo);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un equipo para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarEquipoDB(int idEquipo) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_equipos(?, ?) }")) {
            cstmt.setInt(1, idEquipo);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Equipo eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Eliminación imposible. Revise la gestión de recepciones o despachos relacionados y reintente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // FUNCION
    private int obtenerCantidadEquiposDisponibles(int idCategoria) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        int cantidadEquiposDisponibles = 0;

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{? = call obtener_cantidad_equipos_disponibles_categoria_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setInt(2, idCategoria);
            cstmt.execute();

            cantidadEquiposDisponibles = cstmt.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener la cantidad de equipos disponibles: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        return cantidadEquiposDisponibles;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new pantallaEquipos().setVisible(true));
    }
}
