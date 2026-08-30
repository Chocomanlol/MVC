package Model;
public class Voter {
    private String id;
    private String name;
    private boolean active;

    public Voter(String id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }
    public String getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public boolean isActive() { 
        return active; 
    }
}
