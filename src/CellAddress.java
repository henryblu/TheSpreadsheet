public class CellAddress {
    private int row;
    private int column;

    public CellAddress(int row, int column){
        this.row = row;
        this.column = column;
    }
    public int getRow(){return row;}
    public int getColumn(){return column;}

    public int hashCode(){
        return 31* row + column;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (!(obj instanceof CellAddress)) return false; 
        
        CellAddress other = (CellAddress) obj;
        return this.row == other.row && this.column == other.column;
    }
}
