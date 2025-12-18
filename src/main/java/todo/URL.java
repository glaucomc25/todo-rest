package todo;

public class URL {

    private static final String DB_PATH = "/home/glauco/Projects/todo-rest/src/main/webapp/WEB-INF/db/todo.db";

    public static String getDatabasePath() {
        return DB_PATH;
    }
}