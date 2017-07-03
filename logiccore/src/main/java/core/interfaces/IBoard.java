package core.interfaces;

public interface IBoard {
    void drawChess(int x,  int y, boolean host);
    void removeChess(int x, int y);
    void clearChess();
    void setLatticeClickListener(LatticeClickListener listener);
}
