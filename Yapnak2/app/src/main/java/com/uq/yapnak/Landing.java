package com.uq.yapnak;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Landing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Yapnak");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    /**Navigate to login screen*/
    public void toLogin(View view) {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toRegister(View view) {
        try {
            Intent intent = new Intent(this, Register.class);
            startActivityForResult(intent, 0);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void facebookLogin(View view) {

        List<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("debug", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("debug", "User signed up and logged in through Facebook!");
                } else {
                    Log.d("debug", "User logged in through Facebook!");
                    onFacebookLogin();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    void onFacebookLogin() {
        final Landing landing = this;
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("debug", object.toString());
                String email = "";
                String name = "";
                try {
                    name = object.getString("name");
                    email  = object.getString("email");
                } catch (JSONException e) {
                    e.printStackTrace();
                    email = name;
                }
                try {
                    new FacebookSignup_Async(landing).execute(String.valueOf(object.getInt("id")), email, name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

}
