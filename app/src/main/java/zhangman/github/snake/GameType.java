package zhangman.github.snake;

/**
 * 游戏属性分类
 */

public interface GameType {
  int GRID = 0;
  int FOOD = 1;
  int SNAKE = 2;

  int LEFT = 1;
  int TOP = 2;
  int RIGHT = 3;
  int BOTTOM = 4;
}
