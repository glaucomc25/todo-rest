package todo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private final String dbUrl;

    // Construtor recebe a URL do banco
    public ConnectionFactory(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC"); // registra o driver no DriverManager
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite não encontrado", e);
        }        
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}