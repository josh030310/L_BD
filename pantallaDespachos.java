import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaDespachos extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar;
    private JButton btnCrear;
    private JButton btnActualizar;

    public pantallaDespachos() {
        super("Datos de Despachos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de despacho", "Identificador de equipo",
                "Identificador de departamento", "Fecha de despacho", "Hora de despacho", "Identificador de personal",
                "Nombre de personal", "Teléfono de personal", "Correo de personal" }, 0);
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

        btnEliminar = new JButton("Eliminar Despacho");
        btnCrear = new JButton("Crear Despacho");
        btnActualizar = new JButton("Actualizar Departamento");

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarDespacho());
        btnEliminar.addActionListener(e -> eliminarDespacho());
        btnCrear.addActionListener(e -> crearDespacho());
        loadDespachosData();

    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void eliminarDespacho() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este despacho?",
                    "Eliminar Despacho",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idDespachoStr = (String) model.getValueAt(row, 0); // Suponiendo que la columna 0 es el
                                                                          // identificador y es un String
                int idDespacho = Integer.parseInt(idDespachoStr); // Convierte el identificador a entero
                eliminarDespachoDB(idDespacho);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un despacho para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarDespachoDB(int idDespacho) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_despachos(?, ?) }")) {
            cstmt.setInt(1, idDespacho);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Registra el segundo parámetro como cursor de salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Despacho eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            SwingUtilities.invokeLater(() -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    model.removeRow(selectedRow);
                }
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el despacho: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearDespacho() {
        JTextField idDespachoField = new JTextField();
        JTextField idEquipoField = new JTextField();
        JTextField idDepartamentoField = new JTextField();
        JTextField fechaField = new JTextField();
        JTextField horaField = new JTextField();
        JTextField idPersonalField = new JTextField();

        Object[] message = {
                "ID del Despacho:", idDespachoField,
                "ID del Equipo:", idEquipoField,
                "ID del Departamento:", idDepartamentoField,
                "Fecha del Despacho (YYYY-MM-DD):", fechaField,
                "Hora del Despacho (HH:MM):", horaField,
                "ID del Personal:", idPersonalField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Despacho", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // Asumiendo que todos los campos deben ser parseados a enteros excepto la fecha
            // y la hora.
            int idDespacho = Integer.parseInt(idDespachoField.getText());
            int idEquipo = Integer.parseInt(idEquipoField.getText());
            int idDepartamento = Integer.parseInt(idDepartamentoField.getText());
            int idPersonal = Integer.parseInt(idPersonalField.getText());
            Date fecha = Date.valueOf(fechaField.getText()); // Utilizar java.sql.Date para la conversión
            Time hora = Time.valueOf(horaField.getText() + ":00"); // Asumiendo que la hora se ingresa en formato HH:MM

            insertarDespacho(idDespacho, idEquipo, idDepartamento, fecha, hora, idPersonal);
        }
    }

    private void insertarDespacho(int idDespacho, int idEquipo, int idDepartamento, Date fecha, Time hora,
            int idPersonal) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_despachos(?, ?, ?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, idDespacho);
            cstmt.setInt(2, idEquipo);
            cstmt.setInt(3, idDepartamento);
            cstmt.setDate(4, fecha);
            cstmt.setTime(5, hora);
            cstmt.setInt(6, idPersonal);
            cstmt.registerOutParameter(7, OracleTypes.CURSOR); // Registrar el parámetro OUT
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Despacho creado exitosamente.", "Creación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            loadDespachosData(); // Recargar datos de los despachos
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el despacho: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDespacho() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idDespacho = model.getValueAt(row, 0).toString();
            String idEquipo = model.getValueAt(row, 1).toString();
            String idDepartamento = model.getValueAt(row, 2).toString();
            String idPersonal = model.getValueAt(row, 5).toString();

            // Crear los JTextField con los valores actuales
            JTextField idEquipoField = new JTextField(idEquipo);
            JTextField idDepartamentoField = new JTextField(idDepartamento);
            JTextField idPersonalField = new JTextField(idPersonal);

            // Crear un JPanel con GridLayout para los labels y fields
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Identificador de Equipo:"));
            panel.add(idEquipoField);
            panel.add(new JLabel("Identificador de Departamento:"));
            panel.add(idDepartamentoField);
            panel.add(new JLabel("Identificador de Personal:"));
            panel.add(idPersonalField);

            // Muestra el diálogo
            int result = JOptionPane.showConfirmDialog(null, panel, "Actualizar Despacho",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                actualizarDespachoDB(idDespacho, idEquipoField.getText(),
                        idDepartamentoField.getText(), idPersonalField.getText());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un despacho para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarDespachoDB(String idDespacho, String nuevoIdEquipo, String nuevoIdDepartamento,
            String nuevoIdPersonal) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_despachos(?, ?, ?, ?, ?) }")) {

            cstmt.setString(1, idDespacho);
            cstmt.setString(2, nuevoIdEquipo);
            cstmt.setString(3, nuevoIdDepartamento);
            cstmt.setString(4, nuevoIdPersonal);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR); // si tu procedimiento lo requiere
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Despacho actualizado exitosamente.", "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            loadDespachosData(); // Actualizar los datos mostrados
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el despacho: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDespachosData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (CallableStatement cstmt = conn
                    .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_despachos(?) }")) {
                cstmt.registerOutParameter(1, OracleTypes.CURSOR);
                cstmt.execute();

                try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                    while (rs.next()) {
                        String idDespacho = rs.getString("Identificador de despacho");
                        String idEquipo = rs.getString("Identificador de equipo");
                        String idDepartamento = rs.getString("Identificador de departamento");
                        Date fechaDespacho = rs.getDate("Fecha de despacho");
                        Time horaDespacho = rs.getTime("Hora de despacho");
                        String idPersonal = rs.getString("Identificador de personal");
                        String nombrePersonal = rs.getString("Nombre de personal");
                        String telefonoPersonal = rs.getString("Telefono de personal");
                        String correoPersonal = rs.getString("Correo de personal");

                        model.addRow(new Object[] { idDespacho, idEquipo, idDepartamento, fechaDespacho, horaDespacho,
                                idPersonal, nombrePersonal, telefonoPersonal, correoPersonal });
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de despachos: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaDespachos frame = new pantallaDespachos();
            frame.setVisible(true);
        });
    }
}
