package com.example.spaceshooter;

import android.graphics.Bitmap;

public class Bullet {

    int bulletX, bulletY; // tọa độ

    int bulletVelocity; // tốc độ
    int width, height; // kích thước


    //constructor
    public Bullet(int startX, int startY, int width, int height) {
        bulletX = startX;
        bulletY = startY;
        bulletVelocity = 50; // đặt tốc độ = 50
        this.width = width;
        this.height = height;
    }

    // mỗi lần màn hình được cập nhật, để di chuyển đạn
    public void update() {
        bulletY -= bulletVelocity; // Đạn di chuyển lên trên màn hình
    }

    // getter
    public int getBulletX() {
        return bulletX;
    }

    public int getBulletY() {
        return bulletY;
    }
}
