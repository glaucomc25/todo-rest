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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;


public class TaskApiServlet extends HttpServlet {

    private ConnectionFactory factory;
    private Gson gson;
    private static final Logger logger = Logger.getLogger(TaskApiServlet.class.getName());

    @Override
    public void init() throws ServletException {

        gson = new GsonBuilder().setPrettyPrinting().create();

        // Pega a factory que o AppListener colocou no ServletContext
        factory = (ConnectionFactory) getServletContext().getAttribute("factory");
        if (factory == null) {
            throw new ServletException("ConnectionFactory não encontrada no ServletContext");
        }
    }

    // ---------- GET /tasks ou /tasks?id=1 ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String pathInfo = req.getPathInfo(); // pega o que vem depois de /tasks

        try (PrintWriter out = resp.getWriter()) {

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /tasks → lista todas as tarefas
                try (TaskDAO dao = new TaskDAO(factory)) {
                    List<Task> tasks = dao.listAll();
                    String json = gson.toJson(tasks);
                    out.println(json);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erro ao listar tasks", e); // registra stacktrace no log
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"error\":\"Erro ao listar tasks\"}");
                }
            } else {
                // GET /tasks/{id} → busca tarefa específica
                int id;
                try {
                    id = Integer.parseInt(pathInfo.substring(1)); // remove a barra inicial
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
                    logger.log(Level.SEVERE, "Erro ao listar tasks", e); // stacktrace vai para o log                    
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"error\":\"Erro ao buscar task\"}");
                }
            }
        }
    }

        
    // ---------- POST /tasks ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        // Lê JSON do request
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        String description = json.get("description").getAsString();

         try (TaskDAO dao = new TaskDAO(factory);
             PrintWriter out = resp.getWriter()) {

            int id = dao.insert(description);

            JsonObject response = new JsonObject();
            response.addProperty("id", id);
            response.addProperty("description", description);

            out.println(gson.toJson(response));

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir task", e); // registra stacktrace no log
            throw new ServletException("Erro ao inserir task", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo(); // "/1"
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"ID is required in URL\"}");
            return;
        }
        //obtem o parametro apos o "/" como em "/1" pega o 1
        int id = Integer.parseInt(pathInfo.substring(1));

        // Lê JSON do corpo
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
       
        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        String description = json.get("description").getAsString();

        try (TaskDAO dao = new TaskDAO(factory);
             PrintWriter out = resp.getWriter()) {

            boolean updated = dao.update(id, description);

            if (updated) {
                out.println("{\"status\":\"Task updated\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\":\"Task not found\"}");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar task", e); // registra stacktrace no log
            throw new ServletException("Erro ao atualizar task", e);
        }
    }

  // ---------- DELETE /tasks/{id} ----------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8"); // UTF-8 garante caracteres especiais

        String pathInfo = req.getPathInfo(); // "/1"

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"ID is required in URL\"}");
            return;
        }

        int id = Integer.parseInt(pathInfo.substring(1));

        try (TaskDAO dao = new TaskDAO(factory);
            PrintWriter out = resp.getWriter()) {

            boolean deleted = dao.delete(id);

            if (deleted) {
                resp.getWriter().println("{\"status\":\"Task deleted\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("{\"error\":\"Task not found\"}");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar task", e); // registra stacktrace no log
            throw new ServletException("Erro ao deletar task", e);
        }
    }
}
