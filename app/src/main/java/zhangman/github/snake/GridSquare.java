package zhangman.github.snake;

import android.graphics.Color;

/**
 * 根据元素类型 返回不同颜色
 */

public class GridSquare {
    //元素类型
    private int mType;

    public GridSquare(int type) {
        mType = type;
    }

    public int getColor() {
        switch (mType) {
            //空格子
            case GameType.GRID:
                return Color.WHITE;
            //食物
            case GameType.FOOD:
                return Color.BLUE;
            //蛇
            case GameType.SNAKE:
                return Color.parseColor("#FF4081");
            default:
                break;
        }
        return Color.WHITE;
    }

    public void setType(int type) {
        mType = type;
    }
}
