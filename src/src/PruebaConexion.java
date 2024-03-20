public class PruebaConexion {
    public static void main(String[] args) {
        try {
            // Intenta obtener una conexión
            java.sql.Connection conn = Conexion.getConnection();

            // Si la conexión es exitosa, ciérrala e imprime un mensaje
            if (conn != null) {
                System.out.println("Conexión establecida exitosamente.");
                conn.close(); // Es importante cerrar la conexión cuando hayas terminado
            }
        } catch (Exception e) {
            // Si hay una excepción, imprime el mensaje de error
            e.printStackTrace();
        }
    }

}
