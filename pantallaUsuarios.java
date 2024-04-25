import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class pantallaUsuarios extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrear, btnActualizar, btnBuscarUsuario;

    public pantallaUsuarios() {
        super("Datos de Usuarios");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "ID_USUARIO", "ID_PERSONAL",
                "USERNAME", "PASSWORD", "ID_ROL", "NOMBRE_ROL", "DESCRIPCION_ROL" }, 0);
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

        btnActualizar = new JButton("Actualizar Usuario");
        btnEliminar = new JButton("Eliminar Usuario");
        btnCrear = new JButton("Crear Usuario");
        // FUNCION
        btnBuscarUsuario = new JButton("Buscar Username");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);

        // fUNCION
        buttonPanel.add(btnBuscarUsuario);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnCrear.addActionListener(e -> crearUsuario());
        btnBuscarUsuario.addActionListener(e -> buscarUsuarioPorNombre());

        loadUsuariosData();
    }

    private void buscarUsuarioPorNombre() {
        String nombreUsuario = JOptionPane.showInputDialog(this, "Introduce el nombre de usuario:");

        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            int idUsuario = obtenerIdUsuarioPorNombre(nombreUsuario);

            if (idUsuario >= 0) {
                JOptionPane.showMessageDialog(this, "El ID del usuario es: " + idUsuario, "Resultado de la Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (idUsuario == -1) {
                JOptionPane.showMessageDialog(this, "No se encontró el usuario.", "Resultado de la Búsqueda",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ocurrió un error al buscar el usuario.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // FUNCION
    private int obtenerIdUsuarioPorNombre(String nombreUsuario) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String passwordDB = "DBFide1";
        int idUsuario = -2; // Valor inicial para indicar error

        try (Connection conn = DriverManager.getConnection(url, user, passwordDB);
                CallableStatement cstmt = conn.prepareCall("{? = call obtener_id_usuario_por_nombre_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, nombreUsuario);
            cstmt.execute();

            idUsuario = cstmt.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el ID del usuario: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return idUsuario;
    }

    private void abrirPantallaAdmi() {
        pantallaAdmi pantalla = new pantallaAdmi(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void crearUsuario() {
        JTextField idField = new JTextField();
        JTextField idPersonalField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField idRolField = new JTextField();
        Object[] message = {
                "Identificador de Usuario:", idField,
                "ID Personal:", idPersonalField,
                "Nombre de usuario:", usernameField,
                "Contraseña:", passwordField,
                "ID Rol:", idRolField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText().trim());
            int idPersonal = Integer.parseInt(idPersonalField.getText().trim());
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            int idRol = Integer.parseInt(idRolField.getText().trim());
            insertarUsuario(id, idPersonal, username, password, idRol);
        }
    }

    private void insertarUsuario(int id, int idPersonal, String username, String password, int idRol) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String passwordDB = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, passwordDB);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_usuarios(?,?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, id);
            cstmt.setInt(2, idPersonal);
            cstmt.setString(3, username);
            cstmt.setString(4, password);
            cstmt.setInt(5, idRol);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR);
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsuariosData(); // Recargar datos después de la inserción
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el usuario: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarUsuario() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idUsuario = (String) model.getValueAt(row, 0);
            String idPersonalActual = (String) model.getValueAt(row, 1);
            String usernameActual = (String) model.getValueAt(row, 2);
            // No es recomendable mostrar o editar contraseñas directamente en la interfaz
            // de usuario
            String idRolActual = (String) model.getValueAt(row, 4);

            JTextField idPersonalField = new JTextField(idPersonalActual);
            JTextField usernameField = new JTextField(usernameActual);
            // Deberías pedir la contraseña de otra forma, quizás con un JPasswordField si
            // necesitas cambiarla
            JTextField idRolField = new JTextField(idRolActual);
            Object[] message = {
                    "ID Personal:", idPersonalField,
                    "Nombre de usuario:", usernameField,
                    // Aquí agregarías el campo para la contraseña si fuera necesario
                    "ID Rol:", idRolField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Usuario",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int idPersonal = Integer.parseInt(idPersonalField.getText().trim());
                String username = usernameField.getText().trim();
                // Aquí capturarías la nueva contraseña
                int idRol = Integer.parseInt(idRolField.getText().trim());
                actualizarUsuarioDB(idUsuario, idPersonal, username, idRol);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para actualizar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarUsuarioDB(String idUsuario, int idPersonal, String username, int idRol) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String passwordDB = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, passwordDB);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_usuarios(?, ?, ?, ?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idUsuario));
            cstmt.setInt(2, idPersonal);
            cstmt.setString(3, username);
            cstmt.setInt(4, idRol);
            cstmt.registerOutParameter(5, OracleTypes.CURSOR); // Asumiendo que tu procedimiento espera un cursor de
                                                               // salida
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsuariosData(); // Recargar datos después de la actualización
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el usuario: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUsuariosData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_usuarios(?) }")) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    String idUsuario = rs.getString("ID_USUARIO");
                    String idPersonal = rs.getString("ID_PERSONAL");
                    String nombreUsuario = rs.getString("USERNAME");
                    String contrasena = rs.getString("PASSWORD");
                    String idRol = rs.getString("ID_ROL");
                    String nombreRol = rs.getString("NOMBRE_ROL");
                    String descripcionRol = rs.getString("DESCRIPCION_ROL");

                    model.addRow(new Object[] { idUsuario, idPersonal, nombreUsuario, contrasena, idRol, nombreRol,
                            descripcionRol });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de usuarios: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este usuario?",
                    "Eliminar Usuario",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idUsuario = (String) model.getValueAt(row, 0);
                eliminarUsuarioDB(idUsuario);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para eliminar.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarUsuarioDB(String idUsuario) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String passwordDB = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, passwordDB);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_usuarios(?, ?) }")) {
            cstmt.setInt(1, Integer.parseInt(idUsuario));
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Asumiendo que el procedimiento devuelve un cursor
            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el usuario: ", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaUsuarios frame = new pantallaUsuarios();
            frame.setVisible(true);
        });
    }
}
