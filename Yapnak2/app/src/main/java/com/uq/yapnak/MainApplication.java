package com.uq.yapnak;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Joshua on 02/11/2015.
 */
public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, getResources().getString(R.string.app_id), getResources().getString(R.string.key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this);
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.uq.yapnak", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("debug", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }
    }
}
