public class Cell {
    private CellAddress address;
    private String content;

    public Cell(CellAddress address, String content){
        this.content = content;
        this.address = address;
    }

    public CellAddress getAddress(){ return address; }
    
    public String getContent(){ return content; }

    public void setContent(String content) {
        if (content == null){
            this.content = "";
        } else {
            this.content = content;
        }
    }

}
