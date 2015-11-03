package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.ParsePush;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;
import com.yapnak.gcmbackend.userEndpointApi.model.UserDetailsEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainList extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, UserDetailsListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View currentCard;
    int[] initialViewIds = {R.id.offer_text, R.id.offer_distance, R.id.client_name};
    int[] fadedViewIds = {R.id.offer_dark, R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    int[] clickableViewIds = {R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    private String userId;
    ProgressDialog spinner;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    SwipeRefreshLayout swipeRefreshLayout;
    UserDetailsEntity userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        //Save userId locally
        userId = (String) getIntent().getExtras().get("userId");

        //Get user details
        new UserDetails_Async(this).execute(userId);

        //Setup the recycler view
        setupRecycler();

        //Set up top-only refresh
        refreshOnlyAtTop();
        swipeRefreshSetup();

        if (new ConnectionStatus(this).isConnected()) {
            //Start a progress dialog whilst initial loading
            initialSpinner();
            buildGoogleApiClient();
        }
    }

    public void onTaskComplete(UserDetailsEntity userDetails) {
        this.userDetails = userDetails;
        if (userDetails.getLoyaltyPoints() < 5) {
            ParsePush.subscribeInBackground("new");
        } else if (userDetails.getLoyaltyPoints() < 10) {
            ParsePush.subscribeInBackground("once");
            ParsePush.unsubscribeInBackground("new");
        } else {
            ParsePush.subscribeInBackground("repeat");
            ParsePush.unsubscribeInBackground("new");
            ParsePush.unsubscribeInBackground("once");
        }
    }

    public void refreshOnlyAtTop() {
        final RecyclerView r = (RecyclerView) findViewById(R.id.recycler_list);
        final LinearLayoutManager l = (LinearLayoutManager) r.getLayoutManager();
        r.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                boolean enable = false;
                if (l != null && l.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = l.findFirstCompletelyVisibleItemPosition() == 0;
                    enable = firstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
    }

    public void setupRecycler() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void initialSpinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Loading");
        spinner.setMessage("Sniffing for food nearby...");
        spinner.setCancelable(false);
        spinner.show();
    }

    public void swipeRefreshSetup() {
        //Swipe refresh listener
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("debug", "refreshing");
                if (new ConnectionStatus(getBaseContext()).isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        });
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
        startActivity(intent);
    }

    public void onRateClick(View card) {
        View parent = (View) card.getParent();
        Intent intent = new Intent(this, RatingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("userId", userId);
        TextView clientId = (TextView) parent.findViewById(R.id.clientId);
        intent.putExtra("clientId", clientId.getText());
        TextView offerId = (TextView) parent.findViewById(R.id.offerId);
        intent.putExtra("offerId", offerId.getText());
        startActivity(intent);
    }

    public void onFavouriteClick(View card) {
        View parent = (View) card.getParent();
        TextView offerId = (TextView) parent.findViewById(R.id.offerId);
        final String offer = "offer" + offerId.getText().toString();
        ParsePush.subscribeInBackground(offer);
        new FavouriteOffer_Async().execute(offerId.getText().toString(), userId);
        ((ImageButton) card).setImageResource(R.drawable.heart_filled);
    }

    public void onLocationClick(View card) {
        View parent = (View) card.getParent();
        TextView gps = (TextView) parent.findViewById(R.id.gps);

        String mapURL = "http://maps.google.com/maps?daddr=" + gps.getText() + "&dirflg=w";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mapURL));
        startActivity(intent);
    }

    public void onRecommendClick(View card) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.d("debug", "connecting");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("debug", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Log.d("debug", "null");
        } else {
            Log.d("debug", "executing get clients");
            new GetClients_Async(this).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        new Alert_Dialog(this).connectionError(connectionResult);
    }
}
