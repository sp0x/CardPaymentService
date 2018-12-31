package bg.icafe;

class Payment
{
    private long id;
    private String description;

    public Payment()
    {
        description = "";
    }

    public String getDescription(){ return this.description;}
    public Payment setDescription(String desc) { this.description = desc; return this; }

    public long getId(){ return this.id; }
    public void setId(long id) { this.id = id; }
}