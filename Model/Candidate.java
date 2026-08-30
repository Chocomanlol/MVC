package Model;

public class Candidate {
    private String id;
    private String name;

    public Candidate(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }
}
