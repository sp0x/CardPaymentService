package bg.icafe;

class Payment
{
    private long id;
    private String description;

    public Payment(){

    }

    public String getDescription(){ return this.description;}

    public long getId(){ return this.id; }
    public void setId(long id) { this.id = id; }
}