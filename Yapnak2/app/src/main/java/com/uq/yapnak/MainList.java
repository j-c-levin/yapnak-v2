package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    public void onCardClick(View card) {
        if (currentCard != null) {
            doFade(currentCard, R.id.offer_dark, 1, 0, 200);
            doFade(currentCard, R.id.offer_text, 0, 1, 200);
            doFade(currentCard, R.id.offer_distance, 0, 1, 200);
            doFade(currentCard, R.id.client_name, 0, 1, 200);
        }
        if (card.getTag() != 1) {
            card.setTag(1);
            doFade(card, R.id.offer_dark, 0, 1, 200);
            doFade(card, R.id.offer_text, 1, 0, 200);
            doFade(card, R.id.offer_distance, 1, 0, 200);
            doFade(card, R.id.client_name, 1, 0, 200);
            currentCard = card;
        } else {
            card.setTag(0);
            doFade(card, R.id.offer_dark, 1, 0, 200);
            doFade(card, R.id.offer_text, 0, 1, 200);
            doFade(card, R.id.offer_distance, 0, 1, 200);
            doFade(card, R.id.client_name, 0, 1, 200);
            currentCard = null;
        }
    }

    void doFade(View v, int vId, float start, float end, long duration) {
        final View card = v;
        final int viewId = vId;
        ValueAnimator animation = ValueAnimator.ofFloat(start, end);
        animation.setDuration(duration);
        animation.start();
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                card.findViewById(viewId).setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
    }
}
