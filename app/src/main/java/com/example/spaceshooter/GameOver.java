package com.example.spaceshooter;

import static android.text.format.DateUtils.formatElapsedTime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOver extends AppCompatActivity {

    private MediaPlayer gameOverSound;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_over);

        // lấy point, time, highscore từ gameview
        Intent intent = getIntent();
        int points = intent.getIntExtra("points", 0);
        long time = intent.getLongExtra("time", 0);
        int highScore = intent.getIntExtra("highScore", 0);

        TextView pointsTextView = findViewById(R.id.points);
        TextView timeTextView = findViewById(R.id.time);
        TextView highScoreTextView = findViewById(R.id.high_score);

        // hiển thị point, time, highscore lên màn hình game over
        pointsTextView.setText("Points: " + points);
        timeTextView.setText("Time: " + formatElapsedTime(time));
        highScoreTextView.setText("High Score: " + highScore);

        // phát âm thanh gameover
        gameOverSound = MediaPlayer.create(this, R.raw.gameover);
        gameOverSound.setVolume(10, 10); // tăng âm lượng
        gameOverSound.start();
    }

    // hủy gameover sound
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameOverSound != null) {
            gameOverSound.release();
            gameOverSound = null;
        }
    }

    // chuyển về main activity
    public void restart(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // đổi thời gian
    @SuppressLint("DefaultLocale")
    public String formatElapsedTime(long elapsedTime) {
        int minutes = (int) (elapsedTime / 60000);
        int seconds = (int) (elapsedTime % 60000 / 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
