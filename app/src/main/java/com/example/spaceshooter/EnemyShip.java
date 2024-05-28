package com.example.spaceshooter;

import android.graphics.Paint;
import android.os.SystemClock;

import java.util.ArrayList;

public class EnemyShip {

    public int enemyX, enemyY, enemySpeedX, enemySpeedY, width, height;
    public long lastShotTime;
    public long shotInterval = 1300; // thời gian chở mỗi lần bắn

    public EnemyShip() {
        resetEnemyShip();
        lastShotTime = SystemClock.uptimeMillis();
    }

    public void shoot(ArrayList<EnemyBullet> bullets, Paint paint) {
        long currentTime = SystemClock.uptimeMillis();
        if ((currentTime - lastShotTime) > shotInterval) {
            int bulletStartX = this.enemyX + this.width / 2;
            int bulletStartY = this.enemyY + this.height;
            GameView.playEnemySound();
            bullets.add(new EnemyBullet(bulletStartX, bulletStartY, paint));
            lastShotTime = currentTime;
        }
    }

    public void resetEnemyShip() {
        if (Math.random() < 0.5) {
            enemyX = -200;
            enemySpeedX = 5 + (int)(Math.random() * 11);
        } else {
            enemyX = GameView.screenWidth + 200;
            enemySpeedX = -1 * (5 + (int)(Math.random() * 11));
        }
        enemyY = (int)(Math.random() * 200);
        if (Math.random() < 0.5) {
            enemySpeedY = 2 + (int)(Math.random() * 3);
        } else {
            enemySpeedY = -1 * (2 + (int)(Math.random() * 3));
        }
    }

    // getter
    public int getWidth() {
        return GameView.enemyShip.getWidth();
    }

    public int getHeight() {
        return GameView.enemyShip.getHeight();
    }
}
