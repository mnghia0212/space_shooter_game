package com.example.spaceshooter;

import static android.text.format.DateUtils.formatElapsedTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends View {

    int points = 0; // điểm
    static int screenWidth, screenHeight;
    int shipX, shipY; // Tọa độ của ship
    int bgY1, bgY2; // Tọa độ y của 2 background
    boolean draggingShip = false; // cờ kéo tàu
    boolean shipMoved = false; // cờ tàu di chuyển
    long UPDATE_MILLIS = 30; // thời gian giữa các lần vẽ lại giao diện

    // 2 background
    Bitmap map1;
    Bitmap map2;

    // bitmap ship và enemy ship
    static Bitmap ship;
    static Bitmap enemyShip;
    static Bitmap enemyShipPlus;

    // bitmap heart
    Bitmap heart;

    //bitmap explosion
    Bitmap[] explosionFrames;

    // sound
    static SoundPool soundPool;
    int shipFireSound;
    static int enemyFireSound;
    int shipExplosionSound;
    int enemyExplosionSound;
    int bulletHitSound;
    int gameOverSound;
    float shipFireVolume = 0.8f;
    static float enemyFireVolume = 0.1f;

    // shot
    private long lastShotTime = 0;
    private static final long SHOT_INTERVAL = 300; // 300 milliseconds

    // life
    int lifeCount = 5;  //nhân vật có 5 mạng
    int heartWidth, heartHeight;  // Kích thước của trái tim
    int heartMargin = -100;  // Khoảng cách giữa các trái tim

    // Timer
    long startTime;
    long elapsedTime;
    String elapsedTimeText;

    Runnable runnable; // update game và trả về UI
    Handler handler;
    Context context;

    // mảng chứa đạn, enemy ship, đạn enemy, explosion
    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<EnemyShip> enemyShips = new ArrayList<>();
    ArrayList<EnemyShipPlus> enemyShipsPlus = new ArrayList<>();
    ArrayList<EnemyBullet> enemyBullets = new ArrayList<>();
    List<Explosion> explosions = new ArrayList<>();

    // Paint vẽ đạn, điểm, đạn enemy, timer
    Paint bulletPaint, scorePaint, enemyBulletPaint,timerPaint;
    Rect dest;
    final int TEXT_SIZE = 60;


    public GameView(Context context) {
        super(context);
        this.context = context;

        //khởi tạo soundpool
        AudioAttributes audioAttributes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        //khởi tạo scrolling background
        map1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.map1);
        map2 = map1; // Gán bg t2 và t1 cùng 1 ảnh
        bgY1 = 0; // Đặt background thứ nhất ở đầu màn hình
        bgY2 = -screenHeight; // Đặt bg t2 ở ngay trên bg t1

        // khởi tạo ship và enemy ship
        ship = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
        enemyShip = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemyship);
        enemyShipPlus = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemyshipplus);

        //khởi tạo heart
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        heartWidth = heart.getWidth();
        heartHeight = heart.getHeight();

        //khởi tạo sound
        shipFireSound = soundPool.load(context, R.raw.shipfire, 1);
        enemyFireSound = soundPool.load(context, R.raw.enemyfire, 1);
        shipExplosionSound = soundPool.load(context, R.raw.shipexplosion, 1);
        enemyExplosionSound = soundPool.load(context, R.raw.enemyexplosion, 1);
        bulletHitSound = soundPool.load(context, R.raw.bullethit, 1);
        gameOverSound = soundPool.load(context, R.raw.gameover, 1);

        // khởi tạo explosion
        explosionFrames = new Bitmap[30];
        for (int i = 0; i < explosionFrames.length; i++) {
            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier("img_" + i, "drawable", context.getPackageName());
            explosionFrames[i] = BitmapFactory.decodeResource(context.getResources(), resId);
        }

        // lấy kích thước màn hình
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;

        dest = new Rect(0, 0, screenWidth, screenHeight);
        handler = new Handler();
        runnable = this::invalidate;

        // đặt vị trí ban đầu của ship
        shipX = screenWidth / 2 - ship.getWidth() / 2;
        shipY = screenHeight - ship.getHeight() - 350;

        // 1 màn hình chứa 3 ship enemy và 3 ship enemy plus
        for (int i = 0; i < 3; i++) {
            EnemyShip enemyShip = new EnemyShip();
            enemyShips.add(enemyShip);
        }

        for (int i = 0; i < 3; i++) {
            EnemyShipPlus enemyShipPlus = new EnemyShipPlus();
            enemyShipsPlus.add(enemyShipPlus);
        }
        // vẽ đạn
        bulletPaint = new Paint();
        bulletPaint.setColor(Color.WHITE);

        // vẽ điểm
        scorePaint = new Paint();
        scorePaint.setColor(Color.LTGRAY);
        scorePaint.setTextSize(TEXT_SIZE);
        scorePaint.setTextAlign(Paint.Align.LEFT);

        // vẽ timer
        timerPaint = new Paint();
        timerPaint.setColor(Color.LTGRAY);
        timerPaint.setTextSize(TEXT_SIZE);
        timerPaint.setTextAlign(Paint.Align.RIGHT);

        // Timer
        startTime = SystemClock.uptimeMillis();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Update timer
        elapsedTime = SystemClock.uptimeMillis() - startTime;
        elapsedTimeText = formatElapsedTime(elapsedTime);

        // Tốc độ cuộn nền và enemy tăng dần qua các mốc thời gian
        float speedMultiplier = 1;
        if (elapsedTime >= 120000) {
            speedMultiplier = 2;
        } else if (elapsedTime >= 90000) {
            speedMultiplier = 1.8F;
        } else if (elapsedTime >= 60000) {
            speedMultiplier = 1.5F;
        } else if (elapsedTime >= 30000) {
            speedMultiplier = 1.2F;
        }

        // Vẽ 2 nền cuộn nhau
        int backgroundY1 = bgY1 % map1.getHeight();
        canvas.drawBitmap(map1, 0, backgroundY1, null);
        canvas.drawBitmap(map1, 0, backgroundY1 - map1.getHeight(), null);

        int backgroundY2 = bgY2 % map2.getHeight();
        canvas.drawBitmap(map2, 0, backgroundY2, null);
        canvas.drawBitmap(map2, 0, backgroundY2 - map2.getHeight(), null);

        // tốc độ cuộn nền
        bgY1 += 20 * speedMultiplier;
        bgY2 += 20 * speedMultiplier;

        if (bgY1 >= map1.getHeight()) {
            bgY1 = 0;
        }
        if (bgY2 >= map2.getHeight()) {
            bgY2 = 0;
        }

        // Vẽ heart
        for (int i = 0; i < lifeCount; i++) {
            int x = 10 + (heartWidth + heartMargin) * i;  // Tính toán vị trí x của trái tim, với khoảng cách mới
            canvas.drawBitmap(heart, x, screenHeight - heartHeight - 10, null);  // 10 là margin từ dưới cùng của màn hình
        }

        // Cập nhật vị trí và vẽ tàu địch
        for (int i = 0; i < enemyShips.size(); i++) {
            enemyShips.get(i).enemyX += enemyShips.get(i).enemySpeedX * speedMultiplier;
            enemyShips.get(i).enemyY += enemyShips.get(i).enemySpeedY * speedMultiplier;


            if (enemyShips.get(i).enemyY > 250) {
                enemyShips.get(i).enemySpeedY *= -1;
            }
            if (enemyShips.get(i).enemyY < 0) {
                enemyShips.get(i).enemySpeedY *= -1;
            }
            if (enemyShips.get(i).enemyX < -enemyShip.getWidth() - 200 || enemyShips.get(i).enemyX > screenWidth + 200) {
                enemyShips.get(i).resetEnemyShip();
            }

            enemyShips.get(i).shoot(enemyBullets, enemyBulletPaint); // Gọi phương thức bắn
        }

        for (int i = 0; i < enemyShipsPlus.size(); i++) {
            enemyShipsPlus.get(i).enemyX += enemyShipsPlus.get(i).enemySpeedX * speedMultiplier;
            enemyShipsPlus.get(i).enemyY += enemyShipsPlus.get(i).enemySpeedY * speedMultiplier;

            if (enemyShipsPlus.get(i).enemyY > 350) {
                enemyShipsPlus.get(i).enemySpeedY *= -1;
            }
            if (enemyShipsPlus.get(i).enemyY < 0) {
                enemyShipsPlus.get(i).enemySpeedY *= -1;
            }
            if (enemyShipsPlus.get(i).enemyX < -enemyShipPlus.getWidth() - 200 || enemyShipsPlus.get(i).enemyX > screenWidth + 200) {
                enemyShipsPlus.get(i).resetEnemyShip();
            }
            enemyShipsPlus.get(i).shoot(enemyBullets, enemyBulletPaint); // Gọi phương thức bắn
        }

        for (int i = 0; i < enemyShips.size(); i++) {
            canvas.drawBitmap(enemyShip, enemyShips.get(i).enemyX, enemyShips.get(i).enemyY, null);
        }
        for (int i = 0; i < enemyShipsPlus.size(); i++) {
            canvas.drawBitmap(enemyShipPlus, enemyShipsPlus.get(i).enemyX, enemyShipsPlus.get(i).enemyY, null);
        }

        // Vẽ đạn của tàu địch
        Iterator<EnemyBullet> it = enemyBullets.iterator();
        while (it.hasNext()) {
            EnemyBullet bullet = it.next();
            bullet.update(elapsedTime);

            // Vẽ đổ bóng
            Paint shadowPaint = new Paint();
            shadowPaint.setColor(Color.argb(255, 200, 100, 200)); // Màu tím sáng
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(bullet.getBulletX() + 3, bullet.getBulletY() + 3, bullet.radius + 3, shadowPaint);

            // Vẽ đạn với gradient
            canvas.drawCircle(bullet.getBulletX(), bullet.getBulletY(), bullet.radius, bullet.bulletPaint);
            if (bullet.getBulletY() > screenHeight) {
                it.remove();
            }
        }

        // Vẽ đạn của tàu người chơi
        Paint laserPaint = new Paint();
        laserPaint.setColor(Color.YELLOW); // Màu của laser
        for (int i = 0; i < bullets.size(); ) {
            Bullet bullet = bullets.get(i);
            bullet.update();

            if (bullet.getBulletY() < -bullet.height) {
                bullets.remove(i);
            } else {
                // Vẽ hình chữ nhật laser từ tọa độ (x, y) đến (x+width, y+height)
                canvas.drawRect(bullet.getBulletX(), bullet.getBulletY(),
                        bullet.getBulletX() + bullet.width,
                        bullet.getBulletY() + bullet.height, laserPaint);
                i++;
            }
        }

        // xử lý va chạm
        handleCollisions();

        // vẽ explosions
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();
            explosion.draw(canvas);
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }

        // Vẽ đạn của tàu người chơi
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            boolean bulletHit = false;  // đánh dấu đạn có trúng mục tiêu không

            // kiểm tra va chạm với tàu địch
            for (int j = 0; j < enemyShips.size() && !bulletHit; j++) {
                EnemyShip ship = enemyShips.get(j);
                if (bullet.getBulletX() >= ship.enemyX && bullet.getBulletX() <= ship.enemyX + enemyShip.getWidth() &&
                        bullet.getBulletY() >= ship.enemyY && bullet.getBulletY() <= ship.enemyY + enemyShip.getHeight()) {
                    // Thêm vụ nổ tại vị trí chính xác của tàu địch
                    int explosionX = ship.enemyX + enemyShip.getWidth() / 2 - explosionFrames[0].getWidth() / 2;
                    int explosionY = ship.enemyY + enemyShip.getHeight() / 2 - explosionFrames[0].getHeight() / 2;
                    explosions.add(new Explosion(explosionFrames, explosionX, explosionY, 7)); // Add explosion
                    playSound(enemyExplosionSound, 0.8f);
                    points += 1;
                    bulletHit = true;
                    ship.resetEnemyShip();
                    break; // Kết thúc kiểm tra va chạm cho viên đạn này khi đã trúng tàu
                }
            }

            // kiểm tra va chạm với tàu địch loại Plus
            for (int j = 0; j < enemyShipsPlus.size(); j++) {
                EnemyShipPlus shipPlus = enemyShipsPlus.get(j);
                if (bullet.getBulletX() >= shipPlus.enemyX && bullet.getBulletX() <= shipPlus.enemyX + enemyShipPlus.getWidth() &&
                        bullet.getBulletY() >= shipPlus.enemyY && bullet.getBulletY() <= shipPlus.enemyY + enemyShipPlus.getHeight()) {
                    // Thêm vụ nổ tại vị trí chính xác của tàu địch
                    int explosionX = shipPlus.enemyX + enemyShipPlus.getWidth() / 2 - explosionFrames[0].getWidth() / 2;
                    int explosionY = shipPlus.enemyY + enemyShipPlus.getHeight() / 2 - explosionFrames[0].getHeight() / 2;
                    explosions.add(new Explosion(explosionFrames, explosionX, explosionY, 7)); // Add explosion
                    playSound(enemyExplosionSound, 0.8f);
                    points += 2;
                    bulletHit = true;
                    shipPlus.resetEnemyShip();
                    break; // Kết thúc kiểm tra va chạm cho viên đạn này khi đã trúng tàu
                }
            }

            if (bulletHit) {
                bulletIterator.remove(); // Xóa đạn khỏi danh sách nếu nó trúng tàu
            }
        }

        canvas.drawBitmap(ship, shipX, shipY, null);

        // vẽ point and timer
        canvas.drawText("Point : " + points, 0, TEXT_SIZE, scorePaint);
        canvas.drawText("Time : " + elapsedTimeText, screenWidth - 10, TEXT_SIZE, timerPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        boolean insideShip = x >= shipX && x < (shipX + ship.getWidth()) &&
                y >= shipY && y < (shipY + ship.getHeight());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (insideShip) {
                    // Chạm vào tàu
                    draggingShip = true; // Đánh dấu bắt đầu kéo tàu
                    shipMoved = false; // Lúc đầu tàu chưa di chuyển
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (draggingShip) {
                    // Di chuyển tàu khi đang kéo tàu
                    shipX = x - ship.getWidth() / 2;
                    shipY = y - ship.getHeight() / 2;
                    shipMoved = true; // Đánh dấu tàu đã di chuyển

                    // Bắn tự động khi kéo tàu
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastShotTime >= SHOT_INTERVAL) {
                        lastShotTime = currentTime;
                        if (bullets.size() < 5) {
                            int laserWidth = 19; // Dài của laser
                            int laserHeight = 37; // Cao của laser
                            playSound(shipFireSound, shipFireVolume);
                            Bullet bullet = new Bullet(shipX + ship.getWidth() / 2 - laserWidth / 2,
                                    shipY - laserHeight, laserWidth, laserHeight);
                            bullets.add(bullet);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (draggingShip) {
                    // Kết thúc kéo tàu
                    draggingShip = false;
                }
                break;
        }
        return true;
    }


    public void handleCollisions() {
        // Kiểm tra va chạm giữa đạn địch và tàu người chơi
        Iterator<EnemyBullet> it = enemyBullets.iterator();
        while (it.hasNext()) {
            EnemyBullet bullet = it.next();
            if (Math.sqrt(Math.pow((bullet.getBulletX() - (shipX + (double) ship.getWidth() / 2)), 2) +
                    Math.pow((bullet.getBulletY() - (shipY + (double) ship.getHeight() / 2)), 2))
                    <= (bullet.radius + (double) Math.max(ship.getWidth(), ship.getHeight()) / 2)) {
                it.remove(); // Bỏ đạn ra khỏi list nếu trúng
                lifeCount--; // Giảm 1 lifecount nếu đạn trúng
                playSound(bulletHitSound, 4.0f);
            }
        }

        // Kiểm tra va chạm giữa tàu địch và tàu người chơi
        for (EnemyShip enemyShip : enemyShips) {
            if (Rect.intersects(new Rect(shipX, shipY, shipX + ship.getWidth(), shipY + ship.getHeight()),
                    new Rect(enemyShip.enemyX, enemyShip.enemyY, enemyShip.enemyX + enemyShip.getWidth(), enemyShip.enemyY + enemyShip.getHeight()))) {
                int explosionX = enemyShip.enemyX + enemyShip.getWidth() / 2 - explosionFrames[0].getWidth() / 2;
                int explosionY = enemyShip.enemyY + enemyShip.getHeight() / 2 - explosionFrames[0].getHeight() / 2;
                explosions.add(new Explosion(explosionFrames, explosionX, explosionY, 7)); // Add explosion
                playSound(bulletHitSound, 4.0f); // sound dính đạn
                lifeCount--; // Giảm 1 lifecount nếu đạn trún
                enemyShip.resetEnemyShip(); // Đặt lại vị trí tàu địch
            }
        }

        // Kiểm tra va chạm giữa tàu địch loại Plus và tàu người chơi
        for (EnemyShipPlus enemyShipPlus : enemyShipsPlus) {
            if (Rect.intersects(new Rect(shipX, shipY, shipX + ship.getWidth(), shipY + ship.getHeight()),
                    new Rect(enemyShipPlus.enemyX, enemyShipPlus.enemyY, enemyShipPlus.enemyX + enemyShipPlus.getWidth(), enemyShipPlus.enemyY + enemyShipPlus.getHeight()))) {
                int explosionX = enemyShipPlus.enemyX + enemyShip.getWidth() / 2 - explosionFrames[0].getWidth() / 2;
                int explosionY = enemyShipPlus.enemyY + enemyShip.getHeight() / 2 - explosionFrames[0].getHeight() / 2;
                explosions.add(new Explosion(explosionFrames, explosionX, explosionY, 7)); // Add explosion
                playSound(bulletHitSound, 4.0f);
                lifeCount--;
                enemyShipPlus.resetEnemyShip();
            }
        }

        if (lifeCount <= 0) {
            saveHighScore(points, elapsedTime); // Save high score
            Intent intent = new Intent(getContext(), GameOver.class);
            intent.putExtra("points", points);
            intent.putExtra("time", elapsedTime);
            intent.putExtra("highScore", getHighScore());
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }


    public static void playSound(int sound, float volume) {
        soundPool.play(sound, volume, volume, 1, 0, 1);
    }

    public static void playEnemySound() {
        playSound(enemyFireSound, enemyFireVolume);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @SuppressLint("DefaultLocale")
    public String formatElapsedTime(long elapsedTime) {
        int minutes = (int) (elapsedTime / 60000);
        int seconds = (int) (elapsedTime % 60000 / 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void saveHighScore(int points, long time) {
        // Lưu điểm cao nhất vào SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("high_score_prefs", Context.MODE_PRIVATE);
        int highScore = prefs.getInt("high_score", 0);
        long bestTime = prefs.getLong("best_time", 0);
        if (points > highScore || (points == highScore && time < bestTime)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("high_score", points);
            editor.putLong("best_time", time);
            editor.apply();
        }
    }

    public int getHighScore() {
        // Lấy điểm cao nhất từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("high_score_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("high_score", 0);
    }
}