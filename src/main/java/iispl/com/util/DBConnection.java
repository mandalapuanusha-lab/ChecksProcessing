package iispl.com.util;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/checkdb",
            "postgres",
            "Imageinfo@123"
        );
    }
}