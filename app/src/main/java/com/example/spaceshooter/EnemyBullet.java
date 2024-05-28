package com.example.spaceshooter;

import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Color;

public class EnemyBullet {

    int bulletX, bulletY; // tọa độ
    float bulletVelocity; // tốc độ
    int radius = 13; // bán kính
    Paint bulletPaint; // paint vẽ đạn
    float speedMultiplier = 1; // hệ số độ khó

    public EnemyBullet(int startX, int startY, Paint paint) {
        this.bulletX = startX;
        this.bulletY = startY;

        //gradient vàng sáng cho đạn
        bulletPaint = new Paint();
        bulletPaint.setShader(new RadialGradient(0, 0, radius,
                new int[]{Color.RED, Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP));
        bulletPaint.setStyle(Paint.Style.FILL);

        // Initialize bulletVelocity
        updateVelocity(0); // Ban đầu không có nhân tốc độ
    }

    // tăng tốc độ đạn theo hệ số độ khó
    public void updateVelocity(long elapsedTime) {
        if (elapsedTime >= 120000) { // Sau 2 phút
            speedMultiplier = 2.5F;
        } else if (elapsedTime >= 90000) { // Sau 1 phút 30 giây
            speedMultiplier = 2.2F;
        } else if (elapsedTime >= 60000) { // Sau 1 phút
            speedMultiplier = 1.8F;
        } else if (elapsedTime >= 30000) { // Sau 30 giây
            speedMultiplier = 1.5F;
        } else {
            speedMultiplier = 1;
        }

        bulletVelocity = 40 * speedMultiplier;
    }

    // cập nhật vị trí đạn
    public void update(long elapsedTime) {
        updateVelocity(elapsedTime);
        bulletY += bulletVelocity;
    }

    // getter
    public int getBulletX() {
        return bulletX;
    }

    public int getBulletY() {
        return bulletY;
    }
}
