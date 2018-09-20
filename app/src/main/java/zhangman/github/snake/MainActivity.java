package zhangman.github.snake;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SnakePanelView.OnEatFoodListener {

    private SnakePanelView mSnakePanelView;
    private boolean gamePaused = false;
    private boolean gameStarted = false;
    private Button mBtStart;
    private TextView mTvScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSnakePanelView = findViewById(R.id.snake_view);
        mSnakePanelView.setOnEatFoodListener(this);

        findViewById(R.id.left_btn).setOnClickListener(this);
        findViewById(R.id.right_btn).setOnClickListener(this);
        findViewById(R.id.top_btn).setOnClickListener(this);
        findViewById(R.id.bottom_btn).setOnClickListener(this);
        mBtStart = findViewById(R.id.start_btn);
        mTvScore = findViewById(R.id.tv_score);
        mBtStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                mSnakePanelView.setSnakeDirection(GameType.LEFT);
                break;
            case R.id.right_btn:
                mSnakePanelView.setSnakeDirection(GameType.RIGHT);
                break;
            case R.id.top_btn:
                mSnakePanelView.setSnakeDirection(GameType.TOP);
                break;
            case R.id.bottom_btn:
                mSnakePanelView.setSnakeDirection(GameType.BOTTOM);
                break;
            case R.id.start_btn:
                if (!gameStarted) {
                    mSnakePanelView.reStartGame();
                    mBtStart.setText("PAUSE");
                    gameStarted = true;
                    gamePaused = false;
                } else {
                    if (gamePaused) {
                        mSnakePanelView.resumeGame();
                        mBtStart.setText("PAUSE");
                    } else {
                        mSnakePanelView.pauseGame();
                        mBtStart.setText("RESUME");
                    }
                    gamePaused = !gamePaused;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onEatFood(int foodCount) {
        mTvScore.setText(String.format(Locale.CHINA, "分数：%1$d", foodCount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSnakePanelView!=null) {
            mSnakePanelView.release();
            mSnakePanelView=null;
        }
    }
}
