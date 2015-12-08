package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.KeyListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    SwipeRefreshLayout swipeRefreshLayout;
    UserDetailsEntity userDetails;
    DrawerLayout lMenu;
    ActionBarDrawerToggle lMenuToggle;
    SwipeRefreshLayout content;
    FrameLayout lMenuLayout;
    ViewAnimator viewAnimator;
    TextView aboutText;
    String about;
    View current;
    SharedPreferences prefs;
    boolean searchVisible = false;
    Menu actionBarMenu;
    EditText searchText;
    ImageView searchButton;
    static final String wip = "You've caught us early, we're still working on this.";
    boolean boot = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        lMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        content = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        lMenuLayout = (FrameLayout) findViewById(R.id.content_frame);
        viewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
        current = findViewById(R.id.menu_meals);
        searchText = (EditText) findViewById(R.id.search_location);
        searchButton = (ImageView) findViewById(R.id.search_arrow);

        //Enable text scrolling in the about us page
        aboutText = (TextView) findViewById(R.id.about_text);
        aboutText.setMovementMethod(new ScrollingMovementMethod());

        //Share userId locally
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        userId = data.getString("userID", null);

        //Hide search bar
        initialToggleSearch();

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lMenuToggle = new ActionBarDrawerToggle(this, lMenu, R.string.menu_closed, R.string.menu_open) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        lMenu.setDrawerListener(lMenuToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (lMenuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == Integer.valueOf(R.id.action_search)) {
            toggleSearch();
        }

        return super.onOptionsItemSelected(item);
    }

    void initialToggleSearch() {
        FrameLayout searchBar = (FrameLayout) findViewById(R.id.search_bar);
        Animation showSearch = AnimationUtils.loadAnimation(this, R.anim.search_hide);
        showSearch.setFillAfter(true);
        showSearch.setDuration(0);
        searchBar.startAnimation(showSearch);
        searchText.setTag(searchText.getKeyListener());
        searchText.setKeyListener(null);
        searchButton.setClickable(false);
    }

    void toggleSearch() {
        //Hide keyboard
        hideSoftKeyboard();

        if (searchVisible) {

            //Hide
            FrameLayout searchBar = (FrameLayout) findViewById(R.id.search_bar);
            Animation hideSearch = AnimationUtils.loadAnimation(this, R.anim.search_hide);
            hideSearch.setFillAfter(true);
            searchBar.startAnimation(hideSearch);

            RecyclerView recycler = (RecyclerView) content.findViewById(R.id.recycler_list);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            ViewGroup.LayoutParams params = recycler.getLayoutParams();
            params.height = height;
            recycler.setLayoutParams(params);
            searchText.setKeyListener(null);
            searchText.setText("");
            searchButton.setClickable(false);
            //Revert to nearby clients
            if (mLastLocation != null) {
                getClients();
            } else {
                swipeRefreshLayout.setRefreshing(true);
                createLocationRequest();
            }
        } else {
            //Show
            FrameLayout searchBar = (FrameLayout) findViewById(R.id.search_bar);
            Animation showSearch = AnimationUtils.loadAnimation(this, R.anim.search_show);
            showSearch.setFillAfter(true);
            searchBar.startAnimation(showSearch);
            RecyclerView recycler = (RecyclerView) content.findViewById(R.id.recycler_list);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;
            height = (int) (height * Double.parseDouble((String) recycler.getTag()));
            ViewGroup.LayoutParams params = recycler.getLayoutParams();
            params.height = height;
            recycler.setLayoutParams(params);
//            Animation moveContent = AnimationUtils.loadAnimation(this, R.anim.main_list_down);
//            moveContent.setFillAfter(true);
//            content.findViewById(R.id.recycler_list).startAnimation(moveContent);
            searchText.setKeyListener((KeyListener) searchText.getTag());
            searchButton.setClickable(true);
        }
        searchVisible = !searchVisible;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        lMenuToggle.syncState();
    }

    public void search(View view) {
        if (new ConnectionStatus(this).isConnected()) {
            hideSoftKeyboard();
            swipeRefreshLayout.setRefreshing(true);
            View parent = (View) view.getParent();
            EditText location = (EditText) parent.findViewById(R.id.search_location);
            new Search_Async(this, userId).execute(location.getText().toString());
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void noOffers() {
        new Alert_Dialog(this).noOffersFound();
        mRecyclerView.setVisibility(View.GONE);
    }

    public void noFavourites() {
        new Alert_Dialog(this).noFavouritesFound();
        mRecyclerView.setVisibility(View.GONE);
    }

    void drawerItemClicked(View item) {
        if (current != null) {
            current.setBackgroundResource(0);
        }
        current = item;
        item.setBackgroundResource(R.drawable.underline);
        lMenu.closeDrawers();
    }

    public void drawerMeals(View item) {
        drawerItemClicked(item);
        getClients();
        getSupportActionBar().setTitle("£5 Meals");
        viewAnimator.setDisplayedChild(0);
        actionBarMenu.findItem(R.id.action_search).setVisible(true);
    }

    public void drawerFavourites(View item) {
        drawerItemClicked(item);
        getFavourites();
        viewAnimator.setDisplayedChild(0);
        getSupportActionBar().setTitle("Favourites");
        actionBarMenu.findItem(R.id.action_search).setVisible(false);
    }

    public void drawerLoyalty(View item) {
        Toast.makeText(this, wip, Toast.LENGTH_SHORT).show();
    }

    public void drawerProfile(View item) {
        Toast.makeText(this, wip, Toast.LENGTH_SHORT).show();
    }

    public void drawerAbout(View item) {
        drawerItemClicked(item);
        getSupportActionBar().setTitle("About us");
        SharedPreferences data = getSharedPreferences("Yapnak", 0);
        about = data.getString("about", null);
        aboutText.setText(about);
        viewAnimator.setDisplayedChild(1);
        actionBarMenu.findItem(R.id.action_search).setVisible(false);
    }

    public void drawerSettings(View item) {
        drawerItemClicked(item);
        getSupportActionBar().setTitle("Settings");
        viewAnimator.setDisplayedChild(2);
        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new PrefsFragment()).commit();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    final Context context = MainList.this;

                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        Log.d("debug", "changing " + key);
                        if (key.equals("password")) {
                            if (prefs.getBoolean(key, false)) {
                                Intent intent = new Intent(context, NewPassword.class);
                                startActivity(intent);
                            }
                        }
                    }
                };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        actionBarMenu.findItem(R.id.action_search).setVisible(false);
    }

    public void drawerHelp(View item) {
        Toast.makeText(this, wip, Toast.LENGTH_SHORT).show();
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
        if (userDetails.getUserImage() != null) {
            //Do something with the user image
//            Log.d("debug", userDetails.getUserImage().substring(0, 20));
//            String imageDataBytes = userDetails.getUserImage().substring(userDetails.getUserImage().indexOf(",")+1);
//            InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));
//            Bitmap bitmap = BitmapFactory.decodeStream(stream);
//            ImageView i = (ImageView) findViewById(R.id.user_image);
//            i.setImageBitmap(bitmap);
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
                            createLocationRequest();
                        } else {
                            getClients();
                            offerRefreshAnalytics(mLastLocation);
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
                    createLocationRequest();
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                    new GetOffers_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            } else {
                buildGoogleApiClient();
            }
        }
    }

    public void getFavourites() {
        if (new ConnectionStatus(this).isConnected() && new GpsStatus(this).isEnabled()) {
            if (mGoogleApiClient != null) {
                if (mLastLocation == null) {

                } else {
                    swipeRefreshLayout.setRefreshing(true);
                    new GetFavourites_Async(this, userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            } else {
                buildGoogleApiClient();
            }
        }
    }

    public void loadOffers(OfferListEntity response) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mAdapter = new ClientListAdapter(response);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onCardClick(View card) {
        TextView t = (TextView) card.findViewById(R.id.offerId);
        cardClickedAnalytics(Integer.parseInt(String.valueOf(t.getText())), (int) card.findViewById(R.id.offerId).getTag());
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
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
            String rightNow = s.format(new Date());
            Log.d("debug", "Right now: " + rightNow);
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
        Toast.makeText(this, wip, Toast.LENGTH_SHORT).show();
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
            Log.d("debug", "mLastLocation unknown");
            swipeRefreshLayout.setRefreshing(true);
            createLocationRequest();
        } else {
            if (boot) {
                swipeRefreshLayout.setRefreshing(true);
                createLocationRequest();
            } else {
                getClients();
            }
        }
    }

    protected void createLocationRequest() {
        Log.d("debug", "createLocationRequest");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                onConnected(new Bundle());
                Log.d("debug", "found: " + location.getLatitude() + " " + location.getLongitude());
                if (boot) {
                    boot = false;
                    userLoginAnalytics();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            }
        });
    }

    void userLoginAnalytics() {
        new userLoginAnalytics_Async(userId).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
    }

    void offerRefreshAnalytics(Location location) {
        new offerRefreshAnalytics_Async(userId).execute(location.getLatitude(), location.getLongitude());
    }

    void cardClickedAnalytics(int offerId, int position) {
        //offerID, pos, userID
        new cardClickedAnalytics_Async(userId).execute(offerId, position);
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
