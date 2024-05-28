package com.example.spaceshooter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // chạy nhạc nền
    @Override
    protected void onStart() {
        super.onStart();
        if (backgroundMusic == null) {
            backgroundMusic = MediaPlayer.create(this, R.raw.universe);
            backgroundMusic.setLooping(true);
            backgroundMusic.start();
        }
    }

    // dừng nhạc nền
    @Override
    protected void onStop() {
        super.onStop();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    // hủy nhạc nền
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    // bắt đầu game và dừng nhạc nền
    public void startGame(View view) {
        // Stop the background music
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    // dialog kết thúc game
    public void exitGame(View view) {
        // Create and show a confirmation dialog
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to exit ?")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Exit the game
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // chuyển tới màn hình coming soon
    public void navigateToComingSoon(View view) {
        Intent intent = new Intent(this, ComingSoonActivity.class);
        startActivity(intent);
    }
}
