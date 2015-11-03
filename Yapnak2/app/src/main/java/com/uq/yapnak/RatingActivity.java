package com.uq.yapnak;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;

public class RatingActivity extends AppCompatActivity {

    ProgressDialog spinner;
    boolean isChecked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Switch isAccepted = (Switch) findViewById(R.id.isAccepted);
        isAccepted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isChecked = b;
            }
        });
    }

    public void submit(View view) {
        //Do submitting
        Log.d("debug", "submitting");
        Bundle extras = getIntent().getExtras();
        RatingBar rating = (RatingBar) findViewById(R.id.rating);
        EditText comment = (EditText) findViewById(R.id.comment);
        Editable text = comment.getText();
        if (new ConnectionStatus(this).isConnected()) {
            spinner();
            new Feedback_Async(this).execute(extras.getString("clientId"), String.valueOf(isChecked), extras.getString("offerId"), String.valueOf(rating.getRating()).substring(0,1), extras.getString("userId"), (text.toString().equals("")) ? "none given" : text.toString());
        }
    }

    public void close(View view) {
        finish();
    }

    void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Submitting feedback");
        spinner.setMessage("Releasing the carrier pigeons...");
        spinner.setCancelable(false);
        spinner.show();
    }
}
