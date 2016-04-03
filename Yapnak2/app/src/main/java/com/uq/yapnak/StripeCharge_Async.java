package com.uq.yapnak;

import android.os.AsyncTask;
import android.util.Log;

import com.yapnak.gcmbackend.userEndpointApi.model.VoidEntity;

import java.io.IOException;

/**
 * Created by Joshua on 13/11/2015.
 */
public class StripeCharge_Async extends AsyncTask<String, Void, VoidEntity> {

    QRCodeActivity qrActivity;

    public StripeCharge_Async(QRCodeActivity qrActivity) {
        this.qrActivity = qrActivity;
    }

    @Override
    protected VoidEntity doInBackground(String... strings) {
//        UserEndpointApi userApi = new UserEndpointApi(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        VoidEntity response = new VoidEntity();
        try {
            response = UserEndpoint.userEndpointApi.stripeCharge(Integer.parseInt(strings[1]),strings[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected void onPostExecute(VoidEntity response) {
      Log.d("debug", "Charged? : " + response.toString());
        qrActivity.spinner.hide();
        if (Boolean.parseBoolean(response.getStatus())) {
            qrActivity.purchaseSuccess();
        } else {
            qrActivity.purchaseFailed(response.getMessage());
        }
    }
}
