package todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO implements AutoCloseable {

    private ConnectionFactory factory;
    private Connection conn;

    public TaskDAO(ConnectionFactory factory) {
        this.factory = factory;
    }

    // ---------- Abre a conexão se ainda não estiver aberta ----------
    private void open() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = factory.getConnection();
        }
    }

    // ---------- Fecha a conexão ----------
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // ---------- Cria a tabela se não existir ----------
    public void createTableIfNotExists() throws SQLException {
        open();
        String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "description TEXT NOT NULL)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    // ---------- Insere uma nova task ----------
    public int insert(String description) throws SQLException {
        open();
        String sql = "INSERT INTO tasks(description) VALUES(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, description);
            ps.executeUpdate();

            // Retorna ID gerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("ID da task não retornado");
                }
            }
        }
    }

    // ---------- Busca uma task pelo ID ----------
    public Task getById(int id) throws SQLException {
        open();
        String sql = "SELECT * FROM tasks WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                        rs.getInt("id"),
                        rs.getString("description")
                    );
                } else {
                    return null; // não encontrou
                }
            }
        }
    }

    // ---------- Lista todas as tasks ----------
    public List<Task> listAll() throws SQLException {
        open();
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("description")
                ));
            }
        }
        return tasks;
    }

 // ---------- Atualiza uma task existente ----------
    public boolean update(int id, String description) throws SQLException {
        open();
        String sql = "UPDATE tasks SET description=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, description);
            ps.setInt(2, id);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    // ---------- Deleta uma task pelo ID ----------
    public boolean delete(int id) throws SQLException {
        open();
        String sql = "DELETE FROM tasks WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }
}