package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.parse.ParsePush;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View currentCard;
    int[] initialViewIds = {R.id.offer_text, R.id.offer_distance, R.id.client_name};
    int[] fadedViewIds = {R.id.offer_dark, R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    int[] clickableViewIds = {R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        userId = (String) getIntent().getExtras().get("userId");
        new GetClients_Async(this).execute();
    }

    public void loadOffers(OfferListEntity response) {
        mAdapter = new ClientListAdapter(response);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onCardClick(View card) {
        if (card != currentCard && currentCard != null) {
            //Another card is already selected, deselect
            currentCard.setTag(0);
            for (int i : initialViewIds) {
                unFade(currentCard, i);
            }
            for (int i : fadedViewIds) {
                doFade(currentCard, i);
            }
            for (int i : clickableViewIds) {
                currentCard.findViewById(i).setEnabled(false);
                currentCard.findViewById(i).setClickable(false);
            }
        }
        if (card.getTag() != 1) {
            //Fade selected card
            card.setTag(1);
            for (int i : initialViewIds) {
                doFade(card, i);
            }
            for (int i : fadedViewIds) {
                unFade(card, i);
            }
            for (int i : clickableViewIds) {
                card.findViewById(i).setEnabled(true);
                card.findViewById(i).setClickable(true);
            }
            currentCard = card;
        } else {
            //Unfade selected card
            card.setTag(0);
            for (int i : initialViewIds) {
                unFade(card, i);
            }
            for (int i : fadedViewIds) {
                doFade(card, i);
            }
            for (int i : clickableViewIds) {
                card.findViewById(i).setEnabled(false);
                card.findViewById(i).setClickable(false);
            }
            currentCard = null;
        }
    }

    void doFade(View v, int vId) {
        final View card = v;
        final int viewId = vId;
        long duration = 200;
        ValueAnimator animation = ValueAnimator.ofFloat(1, 0);
        animation.setDuration(duration);
        animation.start();
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                card.findViewById(viewId).setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
    }

    void unFade(View v, int vId) {
        final View card = v;
        final int viewId = vId;
        long duration = 200;
        ValueAnimator animation = ValueAnimator.ofFloat(0, 1);
        animation.setDuration(duration);
        animation.start();
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                card.findViewById(viewId).setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
    }

    public void onGetClick(View card) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        JSONObject json = new JSONObject();
        try {
            json.put("id", userId);
            json.put("isReward", false);
            View parent = (View) card.getParent();
            TextView clientId = (TextView) parent.findViewById(R.id.clientId);
            json.put("client", clientId.getText());
            TextView offerId = (TextView) parent.findViewById(R.id.offerId);
            json.put("offer", offerId.getText());
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
            String rightNow = s.format(new Date());
            json.put("datetime", rightNow);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra("qrCode", json.toString());
        this.startActivity(intent);
    }

    public void onRateClick(View card) {

    }

    public void onFavouriteClick(View card) {
        View parent = (View) card.getParent();
        TextView offerId = (TextView) parent.findViewById(R.id.offerId);
        final String offer = "offer" + offerId.getText().toString();
        ParsePush.subscribeInBackground(offer);
        new FavouriteOffer_Async().execute(offerId.getText().toString(), userId);
    }

    public void onLocationClick(View card) {

    }

    public void onRecommendClick(View card) {

    }
}
