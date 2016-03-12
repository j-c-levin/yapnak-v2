package com.uq.yapnak;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Landing extends AppCompatActivity {

    public Context context;
    ProgressDialog spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Yapnak");
        context = this;
        testStripe();
    }

    void testStripe() {
        Card card = new Card("4242-4242-4242-4242", 12, 2017, "123");
        Log.d("debug", "card is: " + card.validateCard());
        if ( !card.validateCard()) {
            // Show errors
            return;
        }

        Stripe stripe = null;
        try {
            stripe = new Stripe("pk_test_l54Tnxt1Zue4lJJPHZ4Vpu93");
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            Log.d("debug", token.toString());
                            new StripeCharge_Async().execute(token.getId());
                        }

                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(context,
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
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
                    new Alert_Dialog(context).faceBookFailed();
                } else if (user.isNew()) {
                    Log.d("debug", "User signed up and logged in through Facebook!");
                    spinner();
                    onFacebookLogin();
                } else {
                    Log.d("debug", "User logged in through Facebook!");
                    spinner();
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

    void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Facebook login success");
        spinner.setMessage("Awesome, just finishing the process...");
        spinner.setCancelable(false);
        spinner.show();
    }

}
