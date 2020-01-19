package com.example.jack8.floatwindow;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Brickout {
    private Activity activity;
    private ScreenSize screenSize;

    private enum GameStatus{
        STOP,
        READY,
        START
    }
    private LinkedHashSet<Integer> windowsList = new LinkedHashSet<>();
    private WindowStruct gameWindow;
    private Handler handler = new Handler();
    private  GameStatus gameStatus = GameStatus.STOP;
    private Ball ball;
    private int ballSpeed;
    private  Paddle paddle;
    private ArrayList<Brick> bricks = new ArrayList<>();
    int score = 0, life = 0;
    private TextView scoreText, lifeText;
    private boolean showTitle;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> sounds = new HashMap<>();

    public Brickout(final Activity activity){
        this.activity = activity;
        this.screenSize = new ScreenSize(activity);
        ballSpeed = ((int)activity.getResources().getDisplayMetrics().density*5);
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        sounds.put(R.raw.bassdrum, soundPool.load(activity, R.raw.bassdrum, 1));
        sounds.put(R.raw.boing, soundPool.load(activity, R.raw.boing, 1));
        sounds.put(R.raw.padexplo, soundPool.load(activity, R.raw.padexplo, 1));
        sounds.put(R.raw.wowpulse, soundPool.load(activity, R.raw.wowpulse, 1));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onPlay(boolean showTitle){
        if(gameStatus != GameStatus.STOP)
            return;
        this.showTitle = showTitle;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        gameStatus = GameStatus.READY;
        life = 3;
        score = 0;
        View gameView = LayoutInflater.from(activity).inflate(R.layout.brickout,null);
        scoreText = gameView.findViewById(R.id.score);
        lifeText = gameView.findViewById(R.id.life);
        lifeText.setText(String.valueOf(life));
        scoreText.setText(String.valueOf(score));
        gameWindow = new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS)
                .windowPages(new View[]{gameView})
                .windowPageTitles(new String[]{activity.getString(R.string.brickout)})
                .windowAction(new WindowStruct.WindowAction() {
                    @Override
                    public void goHide(WindowStruct windowStruct) {

                    }

                    @Override
                    public void goClose(WindowStruct windowStruct) {
                        final int totalScore = score + life * 10;
                        windowsList.remove(windowStruct.getNumber());
                        bricks.clear();
                        if(gameStatus != GameStatus.STOP) {
                            gameStatus = GameStatus.STOP;
                            windowsList.add((new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                                    .windowPages(new int[]{R.layout.brickout_end_window})
                                    .windowPageTitles(new String[]{activity.getString(R.string.game_over)})
                                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                                    .left(screenSize.getWidth() / 2 - (int) (activity.getResources().getDisplayMetrics().density * 60))
                                    .top(screenSize.getHeight() / 2 - (int) (activity.getResources().getDisplayMetrics().density * 30))
                                    .width((int) (activity.getResources().getDisplayMetrics().density * 120))
                                    .height((int) (activity.getResources().getDisplayMetrics().density * (30 + WindowParameter.getWindowButtonsHeight(activity))))
                                    .windowButtonsHeight((int) (activity.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(activity)))
                                    .windowButtonsWidth((int) (activity.getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(activity)))
                                    .windowSizeBarHeight((int) (activity.getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(activity)))
                                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                        @Override
                                        public void Construction(Context context, View view, int i, Object[] objects, WindowStruct windowStruct) {
                                            ((TextView) view.findViewById(R.id.total_score)).setText(String.valueOf(totalScore));
                                        }

                                        @Override
                                        public void Deconstruction(Context context, View view, int i, WindowStruct windowStruct) {
                                            windowsList.remove(windowStruct.getNumber());
                                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                                        }

                                        @Override
                                        public void onResume(Context context, View view, int i, WindowStruct windowStruct) {

                                        }

                                        @Override
                                        public void onPause(Context context, View view, int i, WindowStruct windowStruct) {

                                        }
                                    })
                                    .show()).getNumber());
                        }else
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                    }
                })
                .show();
        gameWindow.max();
        windowsList.add(gameWindow.getNumber());
        windowsList.add((new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                .top(0)
                .left(screenSize.getWidth()-(int)(activity.getResources().getDisplayMetrics().density*30))
                .width((int)(activity.getResources().getDisplayMetrics().density*30))
                .height((int)(activity.getResources().getDisplayMetrics().density*30))
                .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                .parentWindow(gameWindow)
                .windowAction(new WindowStruct.WindowAction() {
                    @Override
                    public void goHide(WindowStruct windowStruct) {

                    }

                    @Override
                    public void goClose(WindowStruct windowStruct) {
                        windowsList.remove(windowStruct.getNumber());
                        gameWindow.close();
                    }
                })
                .show()).getNumber());
        paddle = new Paddle(
                screenSize.getWidth() / 2 - (int)(activity.getResources().getDisplayMetrics().density*60),
                screenSize.getHeight() - (int)(activity.getResources().getDisplayMetrics().density*60)
        );
        ball = new Ball(paddle.x + (int)(activity.getResources().getDisplayMetrics().density*70),paddle.y - (int)(activity.getResources().getDisplayMetrics().density*20),new Point(ballSpeed,-ballSpeed));
        int x = screenSize.getWidth() /  + (int)(activity.getResources().getDisplayMetrics().density * 80);
        int y = 3;
        for(int i = 0;i <= x;i++)
            for(int j = 0;j <= y;j++)
                bricks.add(
                        new Brick(
                                (int)(activity.getResources().getDisplayMetrics().density * (70 * i)) + (int)(activity.getResources().getDisplayMetrics().density * 10),
                                (int)(activity.getResources().getDisplayMetrics().density * (40 * j)) + (int)(activity.getResources().getDisplayMetrics().density * 80)
                        )
                );
    }

    public void onPause() {
        gameStatus = GameStatus.STOP;
        for(int number : windowsList) {
            WindowStruct.getWindowStruct(number).setTransitionsDuration(0);
            WindowStruct.getWindowStruct(number).close();
        }
    }
    public void onDestroy() {
        soundPool.unload(sounds.remove(R.raw.wowpulse));
        soundPool.unload(sounds.remove(R.raw.padexplo));
        soundPool.unload(sounds.remove(R.raw.boing));
        soundPool.unload(sounds.remove(R.raw.bassdrum));
    }

    class ScreenSize extends WindowStruct.ScreenSize{
        public ScreenSize(Context context) {
            super(context);
        }

        @Override
        public int getWidth() {
            return super.context.getResources().getDisplayMetrics().widthPixels;
        }

        @Override
        public int getHeight() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(displayMetrics);
                if(super.context.getResources().getDisplayMetrics().heightPixels > super.context.getResources().getDisplayMetrics().widthPixels)
                    return displayMetrics.heightPixels - getStatusBarHeight() - getNavigationBarHeight();
            }
            return super.context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight();
        }
    }

    static class Point{
        int x, y;
        Point(int x, int y){
            this.x = x;
            this.y = y;
        }
        void add(Point point){
            x += point.x;
            y += point.y;
        }
    }
    class Ball extends Point implements Runnable{
        Point a;
        WindowStruct _ball;
        Rect rect = new Rect();
        View view;
        Ball(int x, int y,Point a) {
            super(x, y);
            this.a = a;
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(R.drawable.ball);
            view = imageView;
            this._ball = new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                    .displayObject(WindowStruct.ALL_NOT_DISPLAY)
                    .windowPages(new View[]{imageView})
                    .top(y)
                    .left(x)
                    .parentWindow(gameWindow)
                    .width((int)(activity.getResources().getDisplayMetrics().density*20))
                    .height((int)(activity.getResources().getDisplayMetrics().density*20))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            windowsList.remove(windowStruct.getNumber());
                        }
                    })
                    .show();
            windowsList.add(_ball.getNumber());
        }

        Rect getRect(){
            rect.top = _ball.getPositionY();
            rect.bottom = _ball.getPositionY() + _ball.getHeight();
            rect.left = _ball.getPositionX();
            rect.right = _ball.getPositionX() + _ball.getWidth();
            return rect;
        }

        @Override
        void add(Point point){
            super.add(point);
            _ball.setPosition(_ball.getPositionX() + point.x,_ball.getPositionY() + point.y);
        }

        @Override
        public void run() {
            x += a.x;
            y += a.y;
            //與接球盤的碰撞判斷
            if(getRect().intersect(paddle.getRect())){
                soundPool.play(sounds.get(R.raw.boing), 1f, 1f, 1, 0, 1f);
                Point bp = new Point(_ball.getPositionX() + _ball.getWidth() / 2, _ball.getPositionY() + _ball.getHeight() /2);
                Point pp = new Point(paddle._paddle.getPositionX() + paddle._paddle.getWidth() / 2, paddle._paddle.getPositionY() + paddle._paddle.getHeight() /2);
                if(Math.abs(bp.x - pp.x) < paddle._paddle.getWidth()/2) {//撞擊上下
                    if (bp.y < pp.y)//上
                        a.y = -Math.abs(a.y);
                    else if(bp.y > pp.y)//下
                        a.y = Math.abs(a.y);
                } else if(Math.abs(bp.y - pp.y) < paddle._paddle.getHeight()/2) {//撞擊左右
                    if (bp.x < pp.x)//左
                        a.x = -Math.abs(a.x);
                    else if(bp.x > pp.x)//右
                        a.x = Math.abs(a.x);
                } else {//撞擊四個角
                    if(bp.x < pp.x){//左上下
                        if (bp.y < pp.y) {//上
                            a.x = -Math.abs(a.x);
                            a.y = -Math.abs(a.y);
                        } else if(bp.y > pp.y) {//下
                            a.x = -Math.abs(a.x);
                            a.y = Math.abs(a.y);
                        }
                    } else if(bp.x > pp.x){//右上下
                        if (bp.y < pp.y) {//上
                            a.x = Math.abs(a.x);
                            a.y = -Math.abs(a.y);
                        } else if(bp.y > pp.y) {//下
                            a.x = Math.abs(a.x);
                            a.y = Math.abs(a.y);
                        }
                    }
                }
            } else {
                if (x < 0) {
                    soundPool.play(sounds.get(R.raw.bassdrum), 1f, 1f, 1, 0, 1f);
                    a.x = Math.abs(a.x);
                } else if (x > screenSize.getWidth() - _ball.getWidth()) {
                    soundPool.play(sounds.get(R.raw.bassdrum), 1f, 1f, 1, 0, 1f);
                    a.x = -Math.abs(a.x);
                }
                if (y < 0) {
                    soundPool.play(sounds.get(R.raw.bassdrum), 1f, 1f, 1, 0, 1f);
                    a.y = Math.abs(a.y);
                } else if (y > screenSize.getHeight() - _ball.getHeight()){
                    soundPool.play(sounds.get(R.raw.padexplo), 1f, 1f, 1, 0, 1f);
                    gameStatus = GameStatus.READY;
                    a.x = ballSpeed;
                    a.y = -ballSpeed;
                    _ball.setPosition(x = paddle.x + (int)(activity.getResources().getDisplayMetrics().density*70),y = paddle.y - (int)(activity.getResources().getDisplayMetrics().density*20));
                    if(--life == 0)
                        gameWindow.close();
                    lifeText.setText(String.valueOf(life));
                }
            }
            _ball.setPosition(x, y);
            if(gameStatus == GameStatus.START)
                handler.post(this);
        }
    }
    class Paddle extends Point implements  View.OnTouchListener {

        WindowStruct _paddle;
        Rect rect = new Rect();
        View view;

        Paddle(int x, int y) {
            super(x, y);
            ImageView imageView = new ImageView(activity);
            view = imageView;
            imageView.setImageResource(R.drawable.paddle);
            imageView.setOnTouchListener(this);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gameStatus == GameStatus.START)
                        return;
                    gameStatus = GameStatus.START;
                    handler.post(ball);
                    for(Brick brick : bricks)
                        handler.post(brick);
                }
            });
            this._paddle = new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                    .displayObject(WindowStruct.ALL_NOT_DISPLAY)
                    .windowPages(new View[]{imageView})
                    .top(y)
                    .left(x)
                    .parentWindow(gameWindow)
                    .width((int)(activity.getResources().getDisplayMetrics().density*120))
                    .height((int)(activity.getResources().getDisplayMetrics().density*30))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            windowsList.remove(windowStruct.getNumber());
                        }
                    })
                    .show();
            windowsList.add(_paddle.getNumber());
        }

        Rect getRect(){
            rect.top = _paddle.getPositionY();
            rect.bottom = _paddle.getPositionY() + _paddle.getHeight();
            rect.left = _paddle.getPositionX();
            rect.right = _paddle.getPositionX() + _paddle.getWidth();
            return rect;
        }

        private float H =- 1;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (H == -1) {
                    H = event.getRawX();//取得點擊的Y座標到視窗頂點的距離
                    return true;
                }
                x = _paddle.getPositionX()-(int) (H-event.getRawX());
                x = Math.max(0, x);
                x = Math.min(screenSize.getWidth() - _paddle.getWidth(), x);
                _paddle.setPosition(x,_paddle.getPositionY());
                if(gameStatus == GameStatus.READY)
                    ball.add(new Point(
                            (x > 0 && x < screenSize.getWidth() - _paddle.getWidth())?-(int) (H-event.getRawX()):0,
                            0
                    ));
                H = event.getRawX();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                H = -1;
            }
            return false;
        }
    }
    class Brick implements Runnable{

        WindowStruct _brick;
        Rect rect = new Rect();
        View view;
        int life = 1;

        Brick(int x, int y) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(R.drawable.brick);
            view = imageView;
            this._brick = new WindowStruct.Builder(activity, (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                    .displayObject(showTitle ? WindowStruct.TITLE_BAR_AND_BUTTONS : WindowStruct.ALL_NOT_DISPLAY)
                    .windowPages(new View[]{imageView})
                    .windowPageTitles(new String[]{activity.getString(R.string.brick)})
                    .top(y)
                    .left(x)
                    .parentWindow(gameWindow)
                    .width((int)(activity.getResources().getDisplayMetrics().density*60))
                    .height((int)(activity.getResources().getDisplayMetrics().density*30))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            windowsList.remove(windowStruct.getNumber());
                        }
                    })
                    .show();
            windowsList.add(_brick.getNumber());
        }

        Rect getRect(){
            rect.top = _brick.getPositionY();
            rect.bottom = _brick.getPositionY() + _brick.getHeight();
            rect.left = _brick.getPositionX();
            rect.right = _brick.getPositionX() + _brick.getWidth();
            return rect;
        }

        @Override
        public void run() {
            //與接球盤的碰撞判斷
            if(getRect().intersect(ball.getRect())){
                soundPool.play(sounds.get(R.raw.wowpulse), 1f, 1f, 1, 0, 1f);
                Point bp = new Point(ball._ball.getPositionX() + ball._ball.getWidth() / 2, ball._ball.getPositionY() + ball._ball.getHeight() /2);
                Point pp = new Point(_brick.getPositionX() + _brick.getWidth() / 2, _brick.getPositionY() + _brick.getHeight() /2);
                if(Math.abs(bp.x - pp.x) < _brick.getWidth()/2) {//撞擊上下
                    if (bp.y < pp.y)//上
                        ball.a.y = -Math.abs(ball.a.y);
                    else if(bp.y > pp.y)//下
                        ball.a.y = Math.abs(ball.a.y);
                } else if(Math.abs(bp.y - pp.y) < _brick.getHeight()/2) {//撞擊左右
                    if (bp.x < pp.x)//左
                        ball.a.x = -Math.abs(ball.a.x);
                    else if(bp.x > pp.x)//右
                        ball.a.x = Math.abs(ball.a.x);
                } else {//撞擊四個角
                    if(bp.x < pp.x){//左上下
                        if (bp.y < pp.y) {//上
                            ball.a.x = -Math.abs(ball.a.x);
                            ball.a.y = -Math.abs(ball.a.y);
                        } else if(bp.y > pp.y) {//下
                            ball.a.x = -Math.abs(ball.a.x);
                            ball.a.y = Math.abs(ball.a.y);
                        }
                    } else if(bp.x > pp.x){//右上下
                        if (bp.y < pp.y) {//上
                            ball.a.x = Math.abs(ball.a.x);
                            ball.a.y = -Math.abs(ball.a.y);
                        } else if(bp.y > pp.y) {//下
                            ball.a.x = Math.abs(ball.a.x);
                            ball.a.y = Math.abs(ball.a.y);
                        }
                    }
                }
                if(--life == 0) {
                    score++;
                    scoreText.setText(String.valueOf(score));
                    _brick.close();
                    bricks.remove(this);
                    if(bricks.isEmpty()){
                        gameWindow.close();
                    }
                }
            }
            if(gameStatus == GameStatus.START && life > 0)
                handler.post(this);
        }
    }
}
