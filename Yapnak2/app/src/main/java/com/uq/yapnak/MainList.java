package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
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
    DrawerLayout lMenu;
    ActionBarDrawerToggle lMenuToggle;
    SwipeRefreshLayout content;
    FrameLayout lMenuLayout;
    ListView lMenuList;
    String[] lMenuListContent = {"Meals", "Favourites", "Loyalty", "Profile", "", "About", "Settings", "Help", "Sign Out"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        lMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        content = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        lMenuLayout = (FrameLayout) findViewById(R.id.content_frame);
//        lMenuList = (ListView) findViewById(R.id.left_drawer);

        //Save userId locally
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        userId = data.getString("userID", null);

        //Setup the recycler view
        setupRecycler();

        //Set up top-only refresh
        refreshOnlyAtTop();
        swipeRefreshSetup();

        //Setup side drawer menu
        drawerSetup();

        if (new ConnectionStatus(this).isConnected() && new GpsStatus(this).isEnabled()) {
            //Start a progress dialog whilst initial loading
            buildGoogleApiClient();

            //Get user details
            new UserDetails_Async(this).execute(userId);
        }
    }

    public void drawerSetup() {
//        lMenuList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, lMenuListContent));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lMenuToggle = new ActionBarDrawerToggle(this, lMenu, R.string.app_name, R.string.app_id) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                lMenu.bringToFront();
//                lMenuLayout.requestLayout();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        lMenu.setDrawerListener(lMenuToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (lMenuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        lMenuToggle.syncState();
    }

    public void drawerMeals(View item) {
        getClients();
    }

    public void drawerFavourites(View item) {
        getFavourites();
    }

    public void drawerLoyalty(View item) {

    }

    public void drawerProfile(View item) {

    }

    public void drawerAbout(View item) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    public void drawerSettings(View item) {
        Intent intent = new Intent(this, AppSettings.class);
        startActivity(intent);
    }

    public void drawerHelp(View item) {

    }

    public void drawerSignOut(View item) {
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        SharedPreferences.Editor editor = data.edit();
        editor.putString("userID", null);
        editor.commit();
        Intent intent = new Intent(this, Landing.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    public void swipeRefreshSetup() {
        //Swipe refresh listener
        final Context context = MainList.this;
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (new ConnectionStatus(context).isConnected() && new GpsStatus(context).isEnabled()) {
                    if (mGoogleApiClient != null) {
                        if (mLastLocation == null) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            getClients();
                        } else {
                            getClients();
                        }
                    } else {
                        buildGoogleApiClient();
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    public void getClients() {
        if (new ConnectionStatus(this).isConnected() && new GpsStatus(this).isEnabled()) {
            if (mGoogleApiClient != null) {
                if (mLastLocation == null) {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    swipeRefreshLayout.setRefreshing(true);
                    new GetOffers_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                    new GetOffers_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            } else {
                buildGoogleApiClient();
            }
            lMenu.closeDrawers();
        }
    }

    public void getFavourites() {
        if (new ConnectionStatus(this).isConnected() && new GpsStatus(this).isEnabled()) {
            if (mGoogleApiClient != null) {
                if (mLastLocation == null) {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    swipeRefreshLayout.setRefreshing(true);
                    new GetFavourites_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                    new GetFavourites_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            } else {
                buildGoogleApiClient();
            }
            lMenu.closeDrawers();
        }
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
        if (card.getTag() == 0) {
            //Add card to favourites
            View parent = (View) card.getParent();
            TextView offerId = (TextView) parent.findViewById(R.id.offerId);
            final String offer = "offer" + offerId.getText().toString();
            ParsePush.subscribeInBackground(offer);
            new FavouriteOffer_Async().execute(offerId.getText().toString(), userId);
            ((ImageButton) card).setImageResource(R.drawable.heart_filled);
            card.setTag(1);
        } else {
            //Unfavourite card
            View parent = (View) card.getParent();
            TextView offerId = (TextView) parent.findViewById(R.id.offerId);
            final String offer = "offer" + offerId.getText().toString();
            ParsePush.unsubscribeInBackground(offer);
            new RemoveFavouriteOffer_Async().execute(offerId.getText().toString(), userId);
            ((ImageButton) card).setImageResource(R.drawable.heart);
            card.setTag(0);
        }
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
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
        } else {
            getClients();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
