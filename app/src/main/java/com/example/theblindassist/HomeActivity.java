package com.example.theblindassist;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    public final static String MESSAGE_KEY ="com.example.theblindassist.MESSAGE";
    private MediaPlayer selected_audio;
    private MediaPlayer select_audio;
    private int language;
    private Button btn_start_detection;
    private Button btn_history;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        Intent gotIntent = getIntent();
        language = gotIntent.getIntExtra(MESSAGE_KEY,1);
        btn_history = findViewById(R.id.button_HIS);
        btn_start_detection = findViewById(R.id.button_STD);
        btn_start_detection.setClickable(true);
        btn_history.setClickable(true);
        if(language == 0){
            btn_start_detection.setText("जांच शुरू करें");
            btn_history.setText("पुराना इतिहास देखें");
            select_audio = MediaPlayer.create(HomeActivity.this,R.raw.hindi_selected);
            select_audio.start();
            try {
                Thread.sleep(2000);
            }
            catch(Exception e){
                Log.e("HomeActivity", "Error creating sleep thread");
            }
            selected_audio = MediaPlayer.create(HomeActivity.this,R.raw.home_activity_welcome_hindi);
            selected_audio.start();
        }
        else{
            select_audio = MediaPlayer.create(HomeActivity.this,R.raw.english_selected);
            select_audio.start();
            try {
                Thread.sleep(2000);
            }
            catch(Exception e){
                Log.e("HomeActivity", "Error creating sleep thread");
            }
            selected_audio = MediaPlayer.create(HomeActivity.this,R.raw.home_activity_welcome_english);
            selected_audio.start();
        }
    }

    public void StartDetection(View view){
        view.setClickable(false);
        btn_history.setClickable(false);
        select_audio.stop();
        selected_audio.stop();
        Intent detectionIntent = new Intent(HomeActivity.this, DetectionActivity.class);
        detectionIntent.putExtra(MESSAGE_KEY,language);
        startActivity(detectionIntent);
    }

    public void History(View view){
        view.setClickable(false);
        btn_start_detection.setClickable(false);
        select_audio.stop();
        selected_audio.stop();
        Intent detectionIntent = new Intent(HomeActivity.this, HistoryWindow.class);
        detectionIntent.putExtra(MESSAGE_KEY,language);
        startActivity(detectionIntent);
    }

    @Override
    public void onBackPressed() {
        select_audio.stop();
        selected_audio.stop();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp(){
        select_audio.stop();
        selected_audio.stop();
        Intent backIntent = new Intent(HomeActivity.this, LanguageSelectActivity.class);
        startActivity(backIntent);
        finish();
        return true;
    }
}
