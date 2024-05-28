package com.example.spaceshooter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;

public class Explosion {

    public Bitmap[] frames;
    public int frameIndex;
    public long lastFrameTime;
    public int frameInterval;
    public int x, y;
    public boolean finished;

    public Explosion(Bitmap[] frames, int x, int y, int frameInterval) {
        this.frames = frames;
        this.x = x;
        this.y = y;
        this.frameInterval = frameInterval;
        this.frameIndex = 0;
        this.lastFrameTime = SystemClock.uptimeMillis();
        this.finished = false;
    }

    // update
    public void update() {
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - lastFrameTime > frameInterval) {
            frameIndex++;
            lastFrameTime = currentTime;
            if (frameIndex >= frames.length) {
                finished = true;
            }
        }
    }

    // check
    public void draw(Canvas canvas) {
        if (!finished) {
            canvas.drawBitmap(frames[frameIndex], x, y, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}