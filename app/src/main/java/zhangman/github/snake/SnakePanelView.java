package zhangman.github.snake;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 绘制 舞台
 */
public class SnakePanelView extends View {
    private final static String TAG = SnakePanelView.class.getSimpleName();
    public static boolean DEBUG = true;
    //默认速度
    private static final int DEFAULT_SPEED = 5;
    //蛇默认长度
    private static final int DEFAULT_SNAKE_LENGTH = 3;

    //舞台
    private List<List<GridSquare>> mGridSquare = new ArrayList<>();
    //存储蛇的头部位置
    private List<GridPosition> mSnakePositions = new ArrayList<>();

    private GridPosition mSnakeHeader;//蛇头部位置
    private GridPosition mFoodPosition;//食物的位置
    private int mSnakeLength = DEFAULT_SNAKE_LENGTH;
    private long mSpeed = DEFAULT_SPEED;
    private int mSnakeDirection = GameType.RIGHT;
    //标记游戏是否已结束
    private boolean mIsEndGame = false;
    //网格行列数量
    private int mGridSize = 20;
    //绘制方块的画笔
    private Paint mGridPaint = new Paint();
    //绘制网格线的画笔
    private Paint mStrokePaint = new Paint();
    //每个方格尺寸
    private int mRectSize = dp2px(getContext(), 15);
    private int mStartX, mStartY;
    private GameMainThread mThread;

    public SnakePanelView(Context context) {
        this(context, null);
    }

    public SnakePanelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnakePanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //默认保存所有的网格
        List<GridSquare> squares;
        for (int i = 0; i < mGridSize; i++) {
            squares = new ArrayList<>();
            for (int j = 0; j < mGridSize; j++) {
                squares.add(new GridSquare(GameType.GRID));
            }
            mGridSquare.add(squares);
        }
        //初始化蛇头位置
        mSnakeHeader = new GridPosition(10, 10);
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        //初始化 食物位置
        mFoodPosition = new GridPosition(0, 0);
        mIsEndGame = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStartX = w / 2 - mGridSize * mRectSize / 2;
        mStartY = dp2px(getContext(), 40);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mStartY * 2 + mGridSize * mRectSize;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制白色背景
        canvas.drawColor(Color.WHITE);
        //格子画笔
        mGridPaint.reset();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Paint.Style.FILL);
        mGridPaint.setAntiAlias(true);

        //网格线画笔
        mStrokePaint.reset();
        mStrokePaint.setColor(Color.BLACK);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);


        for (int i = 0; i < mGridSize; i++) {
            for (int j = 0; j < mGridSize; j++) {
                int left = mStartX + i * mRectSize;
                int top = mStartY + j * mRectSize;
                int right = left + mRectSize;
                int bottom = top + mRectSize;
                //根据网格行列数量绘制网格（正方形）
                canvas.drawRect(left, top, right, bottom, mStrokePaint);
                //绘制方块
                mGridPaint.setColor(mGridSquare.get(i).get(j).getColor());
                canvas.drawRect(left, top, right, bottom, mGridPaint);
            }
        }
    }

    /**
     * 更新食物方块
     *
     * @param foodPosition
     */
    private void refreshFood(GridPosition foodPosition) {
        mGridSquare.get(foodPosition.getX()).get(foodPosition.getY()).setType(GameType.FOOD);
    }

    //=================================开放api=========================================================

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        if (mThread != null) {
            mThread.mySuspend();
        }
    }

    /**
     * 继续游戏
     */
    public void resumeGame() {
        if (mThread != null) {
            mThread.myResume();
        }
    }

    /**
     * 终止游戏
     */
    public void stopGame() {
        if (mThread != null) {
            mThread.myStop();
        }
    }

    /**
     * 释放资源，关闭线程
     */
    public void release(){
        if (mThread!=null) {
            mThread.status=mThread.STOP;
            mThread.interrupt();
            mThread=null;
        }
    }

    //设置蛇的移动速度，值越大越快
    public void setSpeed(long speed) {
        mSpeed = speed;
    }

    //设置网格尺寸数量
    public void setGridSize(int gridSize) {
        mGridSize = gridSize;
    }

    //改变蛇的运动方向
    public void setSnakeDirection(int snakeDirection) {
        if (mSnakeDirection == GameType.RIGHT && snakeDirection == GameType.LEFT) return;
        if (mSnakeDirection == GameType.LEFT && snakeDirection == GameType.RIGHT) return;
        if (mSnakeDirection == GameType.TOP && snakeDirection == GameType.BOTTOM) return;
        if (mSnakeDirection == GameType.BOTTOM && snakeDirection == GameType.TOP) return;
        mSnakeDirection = snakeDirection;
    }

    public interface OnEatFoodListener {
        void onEatFood(int foodCount);
    }

    public OnEatFoodListener mOnEatFoodListener;

    /**
     * 吃到的食物的数量回调
     *
     * @param onEatFoodListener
     */
    public void setOnEatFoodListener(OnEatFoodListener onEatFoodListener) {
        mOnEatFoodListener = onEatFoodListener;
    }

    //============================================================================================

    //创建线程动态绘制
    private class GameMainThread extends Thread {

        private final int STOP = -1;
        private final int SUSPEND = 0;
        private final int RUNNING = 1;
        private int status = 1;

        @Override
        public void run() {
            while (!mIsEndGame && status != STOP) {
                if (status == SUSPEND) {
                    synchronized (mThread) {
                        try {
                            // 若线程挂起则阻塞自己
                            wait();
                            Log.d(TAG, "run: 我被挂起了");
                        } catch (InterruptedException e) {
                            Log.d(TAG, "线程异常终止...");
                            e.printStackTrace();
                            break;//捕获到异常之后，执行break跳出循环。
                        }
                    }
                } else {
                    moveSnake(mSnakeDirection);
                    checkCollision();
                    refreshGridSquare();
                    handleSnakeTail();
                    postInvalidate();//重绘界面
                    handleSpeed();
                    Log.d(TAG, "run: 我再跑啊");
                }
            }
        }

        private void handleSpeed() {
            try {
                sleep(1000 / mSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 恢复
         */
        public synchronized void myResume() {
            // 修改状态
            status = RUNNING;
            // 唤醒
            notifyAll();
        }

        /**
         * 挂起
         */
        public void mySuspend() {
            // 修改状态
            status = SUSPEND;
        }

        /**
         * 停止
         */
        public void myStop() {
            // 修改状态
            status = STOP;
        }
    }

    //检测碰撞
    private void checkCollision() {
        //检测是否咬到自己，即判断最后的位置是否与已经过的位置重复
        GridPosition headerPosition = mSnakePositions.get(mSnakePositions.size() - 1);
        for (int i = 0; i < mSnakePositions.size() - 2; i++) {
            GridPosition position = mSnakePositions.get(i);
            if (headerPosition.getX() == position.getX() && headerPosition.getY() == position.getY()) {
                //咬到自己 停止游戏
                mIsEndGame = true;
                showMessageDialog();
                return;
            }
        }

        //判断是否吃到食物
        if (headerPosition.getX() == mFoodPosition.getX()
                && headerPosition.getY() == mFoodPosition.getY()) {
            mSnakeLength++;
            post(new Runnable() {
                @Override
                public void run() {
                    //只能在主线程回调
                    if (mOnEatFoodListener != null) {
                        mOnEatFoodListener.onEatFood(mSnakeLength - DEFAULT_SNAKE_LENGTH);
                    }
                }
            });
            generateFood();
        }
    }

    private void showMessageDialog() {
        post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getContext()).setMessage("Game Over!\n你咬到自己了!")
                        .setCancelable(false)
                        .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                reStartGame();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    /**
     * 重新开始游戏
     * 此方法已经在主线程回调
     */
    public void reStartGame() {
        if (!mIsEndGame) {
            return;
        }
        for (List<GridSquare> squares : mGridSquare) {
            for (GridSquare square : squares) {
                square.setType(GameType.GRID);
            }
        }
        if (mSnakeHeader != null) {
            mSnakeHeader.setX(10);
            mSnakeHeader.setY(10);
        } else {
            mSnakeHeader = new GridPosition(10, 10);//蛇的初始位置
        }
        mSnakePositions.clear();
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        mSnakeLength = DEFAULT_SNAKE_LENGTH;//蛇的长度
        if (mOnEatFoodListener != null) {
            mOnEatFoodListener.onEatFood(0);
        }
        mSnakeDirection = GameType.RIGHT;
        mSpeed = DEFAULT_SPEED;//速度，越小速度越慢
        if (mFoodPosition != null) {
            mFoodPosition.setX(0);
            mFoodPosition.setY(0);
        } else {
            mFoodPosition = new GridPosition(0, 0);
        }
        refreshFood(mFoodPosition);
        mIsEndGame = false;
        mThread = new GameMainThread();
        mThread.start();
    }

    //生成food
    private void generateFood() {
        Random random = new Random();
        int foodX = random.nextInt(mGridSize - 1);
        int foodY = random.nextInt(mGridSize - 1);
        for (int i = 0; i < mSnakePositions.size() - 1; i++) {
            if (foodX == mSnakePositions.get(i).getX() && foodY == mSnakePositions.get(i).getY()) {
                //不能生成在蛇身上
                foodX = random.nextInt(mGridSize - 1);
                foodY = random.nextInt(mGridSize - 1);
                //重新循环
                i = 0;
            }
        }
        mFoodPosition.setX(foodX);
        mFoodPosition.setY(foodY);
        refreshFood(mFoodPosition);
    }

    /**
     * 移动蛇的位置
     *
     * @param snakeDirection
     */
    private void moveSnake(int snakeDirection) {
        switch (snakeDirection) {
            case GameType.LEFT:
                if (mSnakeHeader.getX() - 1 < 0) {//边界判断：如果到了最左边 让他穿过屏幕到最右边
                    mSnakeHeader.setX(mGridSize - 1);
                } else {
                    mSnakeHeader.setX(mSnakeHeader.getX() - 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.TOP:
                if (mSnakeHeader.getY() - 1 < 0) {
                    mSnakeHeader.setY(mGridSize - 1);
                } else {
                    mSnakeHeader.setY(mSnakeHeader.getY() - 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.RIGHT:
                if (mSnakeHeader.getX() + 1 >= mGridSize) {
                    mSnakeHeader.setX(0);
                } else {
                    mSnakeHeader.setX(mSnakeHeader.getX() + 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.BOTTOM:
                if (mSnakeHeader.getY() + 1 >= mGridSize) {
                    mSnakeHeader.setY(0);
                } else {
                    mSnakeHeader.setY(mSnakeHeader.getY() + 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
        }
    }

    /**
     * 刷新网格，模拟蛇移动
     */
    private void refreshGridSquare() {
        for (GridPosition position : mSnakePositions) {
            mGridSquare.get(position.getX()).get(position.getY()).setType(GameType.SNAKE);
        }
    }

    /**
     * 刷新蛇尾
     */
    private void handleSnakeTail() {
        int snakeLength = mSnakeLength;
        for (int i = mSnakePositions.size() - 1; i >= 0; i--) {
            if (snakeLength > 0) {
                snakeLength--;
            } else {
                //将超过长度的格子 置为 GameType.GRID
                GridPosition position = mSnakePositions.get(i);
                mGridSquare.get(position.getX()).get(position.getY()).setType(GameType.GRID);
            }
        }
        snakeLength = mSnakeLength;
        for (int i = mSnakePositions.size() - 1; i >= 0; i--) {
            if (snakeLength > 0) {
                snakeLength--;
            } else {
                //将最底部的位置移除
                mSnakePositions.remove(i);
            }
        }
    }

    /**
     * dp转px
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
    }
}
