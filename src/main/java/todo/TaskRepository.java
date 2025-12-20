package todo;

import java.sql.SQLException;
import java.util.List;

public interface TaskRepository {
    int insert(String description) throws SQLException;
    Task getById(int id) throws SQLException;
    List<Task> listAll() throws SQLException;
    boolean update(int id, String description) throws SQLException;
    boolean delete(int id) throws SQLException;   
}
