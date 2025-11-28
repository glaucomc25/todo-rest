package todo;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;

public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String dbPath = context.getInitParameter("DB_PATH");
        String absolutePath = context.getRealPath(dbPath);

        context.setAttribute("DB_PATH", absolutePath);

        // Cria a factory
        ConnectionFactory factory = new ConnectionFactory("jdbc:sqlite:" + absolutePath);

        // Inicializa a tabela
        try (TaskDAO dao = new TaskDAO(factory)) {
            dao.createTableIfNotExists();
            System.out.println("Banco inicializado em: " + absolutePath);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela do banco", e);
        }

        // Salva a factory no contexto
        context.setAttribute("factory", factory);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Contexto destruído.");
    }
}