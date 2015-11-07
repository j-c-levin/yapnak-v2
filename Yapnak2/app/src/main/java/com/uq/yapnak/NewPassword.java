package com.uq.yapnak;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class NewPassword extends AppCompatActivity {

    ProgressDialog spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
    }

    public void newPasswordSubmit(View view) {
        View parent = (View) view.getParent();
        EditText current = (EditText) parent.findViewById(R.id.current_password);
        EditText password = (EditText) parent.findViewById(R.id.new_password);
        EditText confirm = (EditText) parent.findViewById(R.id.confirm_password);
        if (current.getText().toString().equals("") || password.getText().toString().equals("") || confirm.getText().toString().equals("")) {
            new Alert_Dialog(this).passwordDetailsMissing();
        } else {
            if (password.getText().toString().equals(confirm.getText().toString())) {
                SharedPreferences data = getSharedPreferences("Yapnak", 0);
                String userId = data.getString("userID", null);
                spinner();
                new NewPassword_Async(this).execute(current.getText().toString(), password.getText().toString(), userId);
            } else {
                new Alert_Dialog(this).passwordMismatch();
            }
        }
    }

    public void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Setting new password");
        spinner.setMessage("Letting our code monkeys in on a secret...");
        spinner.setCancelable(false);
        spinner.show();
    }

    public void passwordResetFail() {
        new Alert_Dialog(this).passwordResetFail();
    }

    public void passwordResetSuccess() {
        new Alert_Dialog(this).passwordResetSuccess();
    }

    public void close(View view) {
        finish();
    }
}
