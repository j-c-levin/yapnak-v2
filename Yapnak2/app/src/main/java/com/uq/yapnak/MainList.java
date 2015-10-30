package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

public class MainList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View currentCard;


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
        new GetClients_Async(this).execute();
    }

    public void loadOffers(OfferListEntity response) {
        mAdapter = new ClientListAdapter(response);
        mRecyclerView.setAdapter(mAdapter);
    }

    int[] initialViewIds = {R.id.offer_text, R.id.offer_distance, R.id.client_name};
    int[] fadedViewIds = {R.id.offer_dark, R.id.get, R.id.heart, R.id.favourite, R.id.location, R.id.recommend};

    public void onCardClick(View card) {
        if (card != currentCard && currentCard != null) {
            //Another card is already selected, deselect
            currentCard.setTag(0);
            for (int i : initialViewIds) {
                unFade(currentCard, i);
            }
            for (int i : fadedViewIds) {
                doFade(currentCard, i);
                currentCard.findViewById(i).setEnabled(false);
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
                card.findViewById(i).setEnabled(true);
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
                card.findViewById(i).setEnabled(false);
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
        Log.d("debug", "Get");
        if (card.getTag() == 1) {
            Log.d("debug", "qrcode");
            Intent intent = new Intent(this, QRCodeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("qrCode", "testing");
            this.startActivity(intent);
        } else {
            Log.d("debug", "other");
            onCardClick(card);
        }
    }

    public void onHeartClick(View card) {
        Log.d("debug", "Heart");
        if (card.getTag() != 1) {

        } else {
            onCardClick(card);
        }
    }

    public void onFavouriteClick(View card) {
        Log.d("debug", "Favourite");
        if (card.getTag() != 1) {

        } else {
            onCardClick(card);
        }
    }

    public void onLocationClick(View card) {
        Log.d("debug", "Location");
        if (card.getTag() != 1) {

        } else {
            onCardClick(card);
        }
    }

    public void onRecommendClick(View card) {
        Log.d("debug", "Recommend");
        if (card.getTag() != 1) {

        } else {
            onCardClick(card);
        }
    }
}
