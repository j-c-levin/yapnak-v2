package com.uq.yapnak;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class About extends AppCompatActivity {

    TextView aboutText;
    String about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutText = (TextView) findViewById(R.id.about_text);
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        about = data.getString("about", null);
        aboutText.setText(about);
    }


}
