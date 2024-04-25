import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class pantallaRecepciones extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrear, btnActualizar, btnCantidadEquiposRecibidos;

    public pantallaRecepciones() {
        super("Datos de Recepciones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de recepción", "Identificador de equipo",
                "Identificador de almacén", "Fecha de recepción", "Hora de recepción" }, 0);
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

        btnActualizar = new JButton("Actualizar Recepción");
        btnEliminar = new JButton("Eliminar Recepción");
        btnCrear = new JButton("Crear Recepción");
        btnCantidadEquiposRecibidos = new JButton("Cantidad Equipos Recibidos");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCantidadEquiposRecibidos);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarRecepcion());
        btnEliminar.addActionListener(e -> eliminarRecepcion());
        btnCrear.addActionListener(e -> crearRecepcion());
        btnCantidadEquiposRecibidos.addActionListener(e -> verificarCantidadEquiposRecibidos());

        // Cargar datos
        loadRecepcionesData();
    }

    private void verificarCantidadEquiposRecibidos() {
        int row = table.getSelectedRow();
        if (row != -1) {
            try {
                // Asegúrate de que obtienes una cadena y la conviertes a un entero
                int idAlmacen = Integer.parseInt((String) model.getValueAt(row, 2));
                int cantidadRecibida = obtenerCantidadEquiposRecibidosAlmacen(idAlmacen);
                JOptionPane.showMessageDialog(this,
                        "Cantidad de equipos recibidos en el almacén " + idAlmacen + ": " + cantidadRecibida,
                        "Cantidad Equipos Recibidos",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El ID del almacén debe ser un número.", "Error de formato",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un almacén para verificar la cantidad recibida.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int obtenerCantidadEquiposRecibidosAlmacen(int idAlmacen) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        int cantidadRecibida = -1;

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{? = call obtener_cantidad_equipos_recibidos_almacen_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setInt(2, idAlmacen);
            cstmt.execute();

            cantidadRecibida = cstmt.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener la cantidad de equipos recibidos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return cantidadRecibida;
    }

    private void abrirPantallaMat() {
        pantallaMat pantalla = new pantallaMat(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void crearRecepcion() {

        JTextField idEquipoField = new JTextField();
        JTextField idAlmacenField = new JTextField();
        JTextField fechaRecepcionField = new JTextField(); // Podrías usar un DatePicker para seleccionar la fecha
        JTextField horaRecepcionField = new JTextField(); // y un TimePicker para la hora

        Object[] message = {

                "Identificador de equipo:", idEquipoField,
                "Identificador de almacén:", idAlmacenField,
                "Fecha de recepción (yyyy-mm-dd):", fechaRecepcionField,
                "Hora de recepción (HH:MM):", horaRecepcionField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Recepción", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            int idEquipo = Integer.parseInt(idEquipoField.getText().trim());
            int idAlmacen = Integer.parseInt(idAlmacenField.getText().trim());
            Date fechaRecepcion = Date.valueOf(fechaRecepcionField.getText().trim()); // Convierte String a Date
            Timestamp horaRecepcion = Timestamp
                    .valueOf(fechaRecepcionField.getText().trim() + " " + horaRecepcionField.getText().trim() + ":00"); // Convierte
                                                                                                                        // String
                                                                                                                        // a
                                                                                                                        // Timestamp

            insertarRecepcion(idEquipo, idAlmacen, fechaRecepcion, horaRecepcion);
        }
    }

    private void insertarRecepcion(int idEquipo, int idAlmacen, Date fechaRecepcion,
            Timestamp horaRecepcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_recepciones( ?, ?, ?, ?, ?) }")) {

            cstmt.setInt(1, idEquipo);
            cstmt.setInt(2, idAlmacen);
            cstmt.setDate(3, fechaRecepcion);
            cstmt.setTimestamp(4, horaRecepcion);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR);

            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Recepción creada exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadRecepcionesData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear la recepción: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarRecepcion() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idRecepcion = (String) model.getValueAt(row, 0);
            String idEquipoActual = (String) model.getValueAt(row, 1);
            String idAlmacenActual = (String) model.getValueAt(row, 2);
            Date fechaRecepcionActual = (Date) model.getValueAt(row, 3);
            Time horaRecepcionActual = (Time) model.getValueAt(row, 4);

            JTextField idEquipoField = new JTextField(idEquipoActual);
            JTextField idAlmacenField = new JTextField(idAlmacenActual);
            JTextField fechaRecepcionField = new JTextField(fechaRecepcionActual.toString());
            JTextField horaRecepcionField = new JTextField(horaRecepcionActual.toString().substring(0, 5)); // Solo la
                                                                                                            // hora y
                                                                                                            // minutos

            Object[] message = {
                    "Identificador de equipo:", idEquipoField,
                    "Identificador de almacén:", idAlmacenField,
                    "Fecha de recepción (YYYY-MM-DD):", fechaRecepcionField,
                    "Hora de recepción (HH:MM):", horaRecepcionField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Recepción",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int idEquipo = Integer.parseInt(idEquipoField.getText().trim());
                int idAlmacen = Integer.parseInt(idAlmacenField.getText().trim());
                Date fechaRecepcion = Date.valueOf(fechaRecepcionField.getText().trim()); // Convierte String a Date
                Timestamp horaRecepcion = Timestamp.valueOf(
                        fechaRecepcionField.getText().trim() + " " + horaRecepcionField.getText().trim() + ":00"); // Asegura
                                                                                                                   // el
                                                                                                                   // formato
                                                                                                                   // correcto

                actualizarRecepcionDB(idRecepcion, idEquipo, idAlmacen, fechaRecepcion, horaRecepcion);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una recepción para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarRecepcionDB(String idRecepcion, int idEquipo, int idAlmacen, Date fechaRecepcion,
            Timestamp horaRecepcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall(
                                "{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_recepciones(?, ?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idRecepcion));
            cstmt.setInt(2, idEquipo);
            cstmt.setInt(3, idAlmacen);
            cstmt.setDate(4, fechaRecepcion);
            cstmt.setTimestamp(5, horaRecepcion);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR);

            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Recepción actualizada exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadRecepcionesData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la recepción: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarRecepcion() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar esta recepción?",
                    "Eliminar Recepción",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idRecepcion = (String) model.getValueAt(row, 0);
                eliminarRecepcionDB(idRecepcion);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una recepción para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarRecepcionDB(String idRecepcion) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_recepciones(?, ?) }")) {
            cstmt.setString(1, idRecepcion);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Recepción eliminada exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar la recepción: ", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRecepcionesData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_recepciones(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    String idRecepcion = rs.getString("Identificador de recepcion");
                    String idEquipo = rs.getString("Identificador de equipo");
                    String idAlmacen = rs.getString("Identificador de almacen");
                    Date fechaRecepcion = rs.getDate("Fecha de recepcion");
                    Time horaRecepcion = rs.getTime("Hora de recepcion");

                    model.addRow(new Object[] { idRecepcion, idEquipo, idAlmacen, fechaRecepcion, horaRecepcion });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de almacenes: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaRecepciones frame = new pantallaRecepciones();
            frame.setVisible(true);
        });
    }
}
