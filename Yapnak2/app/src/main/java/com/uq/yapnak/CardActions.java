package com.uq.yapnak;

import android.util.Log;
import android.view.View;

/**
 * Created by Joshua on 29/10/2015.
 */
abstract class CardActions {
    public void onGetClick(View card) {
        Log.d("debug", "Get");
    }

    public void onHeartClick(View card) {
        Log.d("debug", "Heart");
    }

    public void onFavouriteClick(View card) {
        Log.d("debug", "Favourite");
    }

    public void onLocationClick(View card) {
        Log.d("debug", "Location");
    }

    public void onRecommendClick(View card) {
        Log.d("debug", "Recommend");
    }
}
