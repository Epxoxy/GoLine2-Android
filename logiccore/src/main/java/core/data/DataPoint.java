package core.data;

public class DataPoint extends IntPoint {
    private int data;
    public DataPoint(int x, int y){
        super(x, y);
    }
    public DataPoint(int x, int y, int data){
        super(x, y);
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}