import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class pantallaDetalleCompras extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JButton btnEliminar, btnCrear, btnActualizar, btnCantidadTotalMateriales;

    public pantallaDetalleCompras() {
        super("Detalle de Compras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Ajusta el tamaño según necesites
        setLocationRelativeTo(null); // Centrar ventana

        model = new DefaultTableModel(new Object[] { "Identificador de detalle de compra", "Material",
                "Cantidad", "Precio unitario", "Identificador de compra", "Identificador de proveedor",
                "Fecha de compra" }, 0);
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

        btnActualizar = new JButton("Actualizar Detalle de Compra");
        btnEliminar = new JButton("Eliminar Detalle de Compra");
        btnCrear = new JButton("Crear Detalle de Compras");
        btnCantidadTotalMateriales = new JButton("Cantidad Total de Materiales");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnCantidadTotalMateriales);

        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> actualizarDetalleCompra());
        btnEliminar.addActionListener(e -> eliminarDetalleCompra());
        btnCrear.addActionListener(e -> crearDetalleCompra());
        btnCantidadTotalMateriales.addActionListener(e -> mostrarCantidadTotalMateriales());

        loadDetalleComprasData();
    }

    private void mostrarCantidadTotalMateriales() {
        int row = table.getSelectedRow();
        if (row != -1) {
            try {
                // Suponiendo que la columna 4 es "Identificador de compra"
                int idCompra = Integer.parseInt(model.getValueAt(row, 4).toString());
                int cantidadTotalMateriales = obtenerCantidadTotalMateriales(idCompra);

                JOptionPane.showMessageDialog(this,
                        "Cantidad total de materiales para la compra ID " + idCompra + ": " + cantidadTotalMateriales,
                        "Cantidad Total de Materiales",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Formato de ID de compra incorrecto.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un detalle de compra para ver la cantidad total de materiales.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int obtenerCantidadTotalMateriales(int idCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";
        int cantidadTotalMateriales = -1;

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{? = call obtener_total_cantidad_materiales_detalle_compras_fun(?)}")) {

            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setInt(2, idCompra);
            cstmt.execute();

            cantidadTotalMateriales = cstmt.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener la cantidad total de materiales: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return cantidadTotalMateriales;
    }

    private void abrirPantallaMat() {
        pantallaMat pantalla = new pantallaMat(); // Pasamos `this` para mantener la referencia
        pantalla.setVisible(true);
        this.setVisible(false);
    }

    private void loadDetalleComprasData() {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        // Limpiar las filas existentes en el modelo
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_OBTENER_PKG.obtener_detalle_compras(?) }")) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    String idDetalleCompra = rs.getString("Identificador de detalle de compra");
                    String material = rs.getString("Material");
                    int cantidad = rs.getInt("Cantidad");
                    BigDecimal precioUnitario = rs.getBigDecimal("Precio unitario");
                    String idCompra = rs.getString("Identificador de compra");
                    String idProveedor = rs.getString("Identificador de proveedor");
                    Date fechaCompra = rs.getDate("Fecha de compra");

                    // Asegúrate de que estás añadiendo las columnas en el mismo orden que se define
                    // en el modelo de la tabla
                    model.addRow(new Object[] {
                            idDetalleCompra,
                            material,
                            cantidad,
                            precioUnitario,
                            idCompra,
                            idProveedor,
                            fechaCompra
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos de detalle de compras: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearDetalleCompra() {
        JTextField idDetalleCompraField = new JTextField();
        JTextField materialField = new JTextField();
        JTextField cantidadField = new JTextField();
        JTextField precioUnitarioField = new JTextField();
        JTextField idCompraField = new JTextField();

        Object[] message = {
                "ID del Detalle de Compra:", idDetalleCompraField,
                "Material:", materialField,
                "Cantidad:", cantidadField,
                "Precio Unitario:", precioUnitarioField,
                "ID de Compra:", idCompraField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Crear Detalle de Compra",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int idDetalleCompra = Integer.parseInt(idDetalleCompraField.getText());
                String material = materialField.getText();
                int cantidad = Integer.parseInt(cantidadField.getText());
                BigDecimal precioUnitario = new BigDecimal(precioUnitarioField.getText());
                int idCompra = Integer.parseInt(idCompraField.getText());

                insertarDetalleCompra(idDetalleCompra, material, cantidad, precioUnitario, idCompra);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos.", "Error de formato",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void insertarDetalleCompra(int idDetalleCompra, String material, int cantidad, BigDecimal precioUnitario,
            int idCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_CREAR_PKG.crear_detalle_compras(?, ?, ?, ?, ?, ?) }")) {

            cstmt.setInt(1, idDetalleCompra);
            cstmt.setString(2, material);
            cstmt.setInt(3, cantidad);
            cstmt.setBigDecimal(4, precioUnitario);
            cstmt.setInt(5, idCompra);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR); // Registrar el parámetro OUT como un cursor

            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Detalle de compra creado exitosamente.", "Creación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            loadDetalleComprasData(); // Recargar los datos de la tabla

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al crear el detalle de compra: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDetalleCompra() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idDetalleCompra = model.getValueAt(row, 0).toString();
            String materialActual = model.getValueAt(row, 1).toString();
            int cantidadActual = (Integer) model.getValueAt(row, 2);
            BigDecimal precioUnitarioActual = (BigDecimal) model.getValueAt(row, 3);
            String idCompraActual = model.getValueAt(row, 4).toString();

            JTextField materialField = new JTextField(materialActual);
            JTextField cantidadField = new JTextField(String.valueOf(cantidadActual));
            JTextField precioUnitarioField = new JTextField(precioUnitarioActual.toPlainString());
            JTextField idCompraField = new JTextField(idCompraActual);

            Object[] message = {
                    "Material:", materialField,
                    "Cantidad:", cantidadField,
                    "Precio Unitario:", precioUnitarioField,
                    "ID de Compra:", idCompraField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Actualizar Detalle de Compra",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                actualizarDetalleCompraDB(
                        idDetalleCompra,
                        materialField.getText(),
                        Integer.parseInt(cantidadField.getText()),
                        new BigDecimal(precioUnitarioField.getText()),
                        idCompraField.getText());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un detalle de compra para actualizar.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarDetalleCompraDB(String idDetalleCompra, String material, int cantidad,
            BigDecimal precioUnitario, String idCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn.prepareCall(
                        "{ call INVENTARIO_MGMT_ACTUALIZAR_PKG.actualizar_detalle_compras(?, ?, ?, ?, ?, ?) }")) {

            // Asegúrate de que el ID del detalle de compra sea un entero si así lo requiere
            // la base de datos
            int idDetalleCompraInt = Integer.parseInt(idDetalleCompra);

            cstmt.setInt(1, idDetalleCompraInt);
            cstmt.setString(2, material);
            cstmt.setInt(3, cantidad);
            cstmt.setBigDecimal(4, precioUnitario);
            cstmt.setString(5, idCompra);
            cstmt.registerOutParameter(6, OracleTypes.CURSOR); // Registrar el parámetro OUT para el cursor

            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Detalle de compra actualizado exitosamente.", "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            loadDetalleComprasData(); // Actualizar los datos de la tabla

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el detalle de compra: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarDetalleCompra() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres eliminar este detalle de compra?",
                    "Eliminar Detalle de Compra",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String idDetalleCompra = (String) model.getValueAt(row, 0);
                eliminarDetalleCompraDB(idDetalleCompra);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un detalle de compra para eliminar.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarDetalleCompraDB(String idDetalleCompra) {
        String url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user = "inventario";
        String password = "DBFide1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                CallableStatement cstmt = conn
                        .prepareCall("{ call INVENTARIO_MGMT_ELIMINAR_PKG.eliminar_detalle_compras(?, ?) }")) {
            // Asegúrate de que el idDetalleCompra sea un entero si así lo requiere la base
            // de datos
            int idDetalleCompraInt = Integer.parseInt(idDetalleCompra);

            cstmt.setInt(1, idDetalleCompraInt);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Registrar el parámetro OUT si es necesario

            cstmt.execute();

            JOptionPane.showMessageDialog(this, "Detalle de compra eliminado exitosamente.", "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de la eliminación
            model.removeRow(table.getSelectedRow());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el detalle de compra: ", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            pantallaDetalleCompras frame = new pantallaDetalleCompras();
            frame.setVisible(true);
        });
    }
}
