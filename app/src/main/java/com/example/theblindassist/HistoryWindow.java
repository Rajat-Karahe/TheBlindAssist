package com.example.theblindassist;

//https://www.youtube.com/watch?v=9t8VVWebRFM
//https://www.allcodingtutorials.com/post/insert-delete-update-and-view-data-in-sqlite-database-android-studio

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class HistoryWindow extends AppCompatActivity {
    LinearLayout layoutList;
    public final static String MESSAGE_KEY ="com.example.theblindassist.MESSAGE";
    private int language;

    DBHelper DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_window);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        layoutList = findViewById(R.id.layout_list);
        Intent gotIntent = getIntent();
        language = gotIntent.getIntExtra(MESSAGE_KEY,1);

        DB = new DBHelper(this);
        refresh();
    }

    private void refresh(){

        Cursor res = DB.getdata();
        if(res.getCount()==0){
            Toast.makeText(HistoryWindow.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
            return;
        }

        if(res.getCount() > 40){
            DB.deleteLastTable();
        }

        layoutList.removeAllViews();
        if(language==0){
            addViewList("दिनांक","समय","प्रकार",true);
        }
        else{
            addViewList("Date","Time","Type",true);
        }
        while(res.moveToNext()){
            addViewList(res.getString(1),res.getString(2),res.getString(3) + " Rs",false);
        }
    }

    private void addViewList(String givendate,String giventime,String denomination,boolean header){
        final View myview;
        if(!header)
            myview = getLayoutInflater().inflate(R.layout.row_add_history,null,false);
        else
            myview = getLayoutInflater().inflate(R.layout.hedder_for_history,null,false);

        layoutList.addView(myview);
        TextView date = myview.findViewById(R.id.date);
        TextView time1 = myview.findViewById(R.id.time);
        TextView deno = myview.findViewById(R.id.denomination);

        date.setText(givendate);
        time1.setText(giventime);
        deno.setText(denomination);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent backIntent = new Intent(HistoryWindow.this, HomeActivity.class);
        backIntent.putExtra(MESSAGE_KEY,language);
        startActivity(backIntent);
        finish();
        return true;
    }
}