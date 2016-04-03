package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.yapnak.gcmbackend.userEndpointApi.model.FeedbackEntity;

import java.io.IOException;

/**
 * Created by Joshua on 03/11/2015.
 */
public class Feedback_Async extends AsyncTask<String, Void, FeedbackEntity> {

    RatingActivity ratingActivity;
    public Feedback_Async(RatingActivity ratingActivity) {
        this.ratingActivity = ratingActivity;
    }

    @Override
    protected FeedbackEntity doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        FeedbackEntity response = new FeedbackEntity();
        String s = "";
        for (String x : strings) {
            s += x;
            s += " ";
        }
        Log.d("debug", s);
        try {
            //clientID,isAccepted,offerId, rating, userID, comment
            response = UserEndpoint.userEndpointApi.feedback(Integer.decode(strings[0]), Boolean.parseBoolean(strings[1]), Integer.decode(strings[2]), Integer.valueOf(strings[3]), strings[4]).setComment(strings[5]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(FeedbackEntity response) {
        if (Boolean.parseBoolean(response.getStatus())) {
            Log.d("debug", "Feedback submitted successfully");
        } else {
            Log.d("debug", "Feedback ERROR");
        }
        ratingActivity.finish();
        ratingActivity.spinner.hide();
    }
}
