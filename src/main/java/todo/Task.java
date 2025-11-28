package todo;

public class Task {
    private int id;
    private String description;

    // Construtor vazio necessário para Gson
    public Task() {}

    // Construtor usado no código para criar Task completa
    public Task(int id, String description) {
        this.id = id;
        this.description = description;
    }

    // Getters (necessários para serialização JSON)
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}