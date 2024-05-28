package com.example.spaceshooter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ComingSoonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coming_soon);
    }

    public void goBack(View view) {
        // Chuyển về MAIN ACTIVITY
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
