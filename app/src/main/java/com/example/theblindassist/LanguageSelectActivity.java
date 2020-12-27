package com.example.theblindassist;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LanguageSelectActivity extends AppCompatActivity {
//        hindi = 0
//      english = 1
    public final static String MESSAGE_KEY ="com.example.theblindassist.MESSAGE";
    private MediaPlayer mediaPlayer;
    private Button hindi_btn;
    private Button english_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_select);
        hindi_btn = findViewById(R.id.button_Hindi);
        english_btn = findViewById(R.id.button_English);
        hindi_btn.setClickable(true);
        english_btn.setClickable(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.welcome);
        mediaPlayer.start();
    }

    public void hindiSelected(View v){
        v.setClickable(false);
        english_btn.setClickable(false);
        mediaPlayer.stop();
        Intent homeIn = new Intent(LanguageSelectActivity.this, HomeActivity.class);
        homeIn.putExtra(MESSAGE_KEY,0);
        startActivity(homeIn);
    }

    public void englishSelected(View v){
        v.setClickable(false);
        hindi_btn.setClickable(false);
        mediaPlayer.stop();
        Intent homeIn = new Intent(LanguageSelectActivity.this,HomeActivity.class);
        homeIn.putExtra(MESSAGE_KEY,1);
        startActivity(homeIn);
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        super.onBackPressed();
    }
}
