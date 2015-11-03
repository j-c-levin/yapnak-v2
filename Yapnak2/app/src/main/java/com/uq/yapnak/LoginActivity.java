package com.uq.yapnak;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    ProgressDialog spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /**
     * Catch hardware back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Catch menu back button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home: {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
        }
        return false;
    }

    public void login(View view) {
        //Check internet status
        if (new ConnectionStatus(this).isConnected()) {
            email = (EditText) findViewById(R.id.Email);
            password = (EditText) findViewById(R.id.Password);
            spinner();
            new Login_Async(this).execute(password.getText().toString(), email.getText().toString());
        }
    }

    void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Logging in");
        spinner.setMessage("Just checking my records, won't be a mo'");
        spinner.setCancelable(false);
        spinner.show();
    }
}

