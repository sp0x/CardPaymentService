package bg.icafe.payment;

public class Payment
{
    private String id;
    private String description;

    public Payment()
    {
        description = "";
    }
    public Payment(String id)
    {
        description = "";
        this.setId(id);
    }

    public String getDescription(){ return this.description;}
    public Payment setDescription(String desc) {
        this.description = desc;
        if(this.description.length()>125){
            this.description = this.description.substring(0, 124);
        }
        return this;
    }

    public String getId(){ return this.id; }
    public void setId(String id) { this.id = id; }
}