package todo;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TaskApiServlet extends HttpServlet {

    private ConnectionFactory factory;
    private Gson gson;
    private static final Logger logger = Logger.getLogger(TaskApiServlet.class.getName());

    @Override
    public void init() throws ServletException {
        gson = new GsonBuilder().setPrettyPrinting().create();

        // Banco fora da aplicação
        this.factory = new ConnectionFactory("jdbc:sqlite:" + URL.getDatabasePath());

        // Apenas valida se o banco é acessível
        try (Connection c = factory.getConnection()) {
            logger.info("Banco conectado com sucesso em: " + URL.getDatabasePath());
        } catch (SQLException e) {
            throw new ServletException("Não foi possível conectar ao banco", e);
        }
    }

    // ---------- GET /tasks ou /tasks/{id} ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        try (PrintWriter out = resp.getWriter()) {
            // GET /tasks → lista todas as tarefas
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /tasks → lista todas as tarefas
                try (TaskDAO dao = new TaskDAO(factory)) {
                    List<Task> tasks = dao.listAll();
                    out.println(gson.toJson(tasks));
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erro ao listar tasks", e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"error\":\"Erro ao listar tasks\"}");
                }
                return;
            } 
            // GET /tasks/{id} → busca tarefa específica
            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"ID must be a number\"}");
                return;
            }

            try (TaskDAO dao = new TaskDAO(factory)) {
                Task task = dao.getById(id);
                if (task != null) {
                    out.println(gson.toJson(task));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"error\":\"Task not found\"}");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao buscar task", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\":\"Erro ao buscar task\"}");
            }
        }
    }

    // ---------- POST /tasks ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws  IOException {

        resp.setContentType("application/json;charset=UTF-8");

        // Lê JSON do corpo
        JsonObject json;
        try {
            json = gson.fromJson(req.getReader(), JsonObject.class); 
        } catch (Exception e) {
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             resp.getWriter().println("{\"error\":\"Invalid JSON\"}");
            return;
        }
        if (!json.has("description") || json.get("description").getAsString().isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Description is required\"}");
            return;
        }
        String description = json.get("description").getAsString();

        PrintWriter out = resp.getWriter();
        try (TaskDAO dao = new TaskDAO(factory)) {
            int id = dao.insert(description);
            resp.setStatus(HttpServletResponse.SC_CREATED);

            JsonObject response = new JsonObject();
            response.addProperty("id", id);
            response.addProperty("description", description);
            out.println(gson.toJson(response));

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir task", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Erro ao inserir task\"}");
        }
    }

    // ---------- PUT /tasks/{id} ----------
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws  IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"ID is required in URL\"}");
            }
            return;
        }

        int id;
        try {
            id = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"ID must be a number\"}");
            }
            return;
        }

        // Lê JSON do corpo
        JsonObject json;
        try {
            json = gson.fromJson(req.getReader(), JsonObject.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\" \"Invalid JSON\" \"}");
            return;
        }

        if (!json.has("description") || json.get("description").getAsString().isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\" \"Description is required\" \"}");            
            return;
        }

        String description = json.get("description").getAsString();

        PrintWriter out = resp.getWriter();
        try (TaskDAO dao = new TaskDAO(factory)) {
            boolean updated = dao.update(id, description);
            if (updated) {
                JsonObject response = new JsonObject();
                response.addProperty("id", id);
                response.addProperty("description", description);
                out.println(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\":\"Task not found\"}");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar task", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Erro ao atualizar task\"}");
        }
    }

    // ---------- DELETE /tasks/{id} ----------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"ID is required in URL\"}");
            }
            return;
        }

        int id;
        try {
            id = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"ID must be a number\"}");
            }
            return;
        }
        PrintWriter out = resp.getWriter();
        try (TaskDAO dao = new TaskDAO(factory)) {
            boolean deleted = dao.delete(id);
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\":\"Task not found\"}");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar task", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Erro ao deletar task\"}");
        }
    }
}