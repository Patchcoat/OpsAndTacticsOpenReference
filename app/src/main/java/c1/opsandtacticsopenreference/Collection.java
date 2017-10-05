package c1.opsandtacticsopenreference;

/**
 * Created by c1user on 10/5/17.
 */

public class Collection {
    private int id;
    private String collection;
    public Collection(){

    }
    public Collection(int id, String collection){
        this.id=id;
        this.collection=collection;
    }
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setCollection(String collection){
        this.collection = collection;
    }
    // Getters
    public int getId() {
        return id;
    }
    public String getCollection(){
        return collection;
    }
}
