package hzj.github.snake;

/**
 * 网格中的位置x,y，不是屏幕的x,y
 */

public class GridPosition {
  private int x;
  private int y;

  public GridPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }
}
