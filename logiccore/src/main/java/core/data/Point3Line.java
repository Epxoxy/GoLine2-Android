package core.data;

public class Point3Line {
    private int x1;
    private int y1;

    private int x2;
    private int y2;

    private int x3;
    private int y3;

    public  Point3Line(){}
    public Point3Line(int x1, int y1, int x2, int y2, int x3, int y3){
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.y1 = y1;
        this.y2 = y2;
        this.y3 = y3;
    }

    public boolean isInLine(int x, int y){
        return (x1 == x && y1 == y) ||
                (x2 == x && y2 == y) ||
                (x3 == x && y3 == y);
    }
    public IntPoint[] toPoints(){
        return new IntPoint[]{
                new IntPoint(x1,y1),
                new IntPoint(x2,y2),
                new IntPoint(x3,y3)
        };
    }
}