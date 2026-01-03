package todo;

import java.io.File;

public class URL {

    public static String getDatabasePath() {
        // Pega o caminho absoluto do diretório WEB-INF/db
        String path = "/home/glauco/databases/todo.db"; // ou use um path relativo à sua estrutura
        File dbFile = new File(path);
        return dbFile.getAbsolutePath();
    }
}