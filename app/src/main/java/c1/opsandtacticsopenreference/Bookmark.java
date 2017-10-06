package c1.opsandtacticsopenreference;

/**
 * Created by c1user on 10/5/17.
 */

public class Bookmark {
    private int id;
    private String text;
    private String link;
    private String type;
    public Bookmark()
    {
    }
    public Bookmark(String text, String link, String type)
    {
        this.id=id;
        this.text=text;
        this.link=link;
        this.type=type;
    }
    public Bookmark(int id, String text, String link, String type)
    {
        this.id=id;
        this.text=text;
        this.link=link;
        this.type=type;
    }
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setType(String type){
        this.type = type;
    }
    // Getters
    public int getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public String getLink() {
        return link;
    }
    public String getType() {
        return type;
    }
}