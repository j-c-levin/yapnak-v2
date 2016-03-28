package com.uq.yapnak;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferEntity;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Register extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    int stage = 2;
    FrameLayout registerFrame;
    TextView registerInstruction;
    TextView registerDataName;
    EditText registerField;
    TextView registerContinue;
    ImageView registerBackground;
    ImageView qrImage;

    FrameLayout cardForm;
    EditText cardNumber;
    EditText cvc;
    EditText endMonth;
    EditText endYear;


    private View currentCard;
    int[] initialViewIds = {R.id.offer_text, R.id.offer_distance, R.id.client_name};
    int[] fadedViewIds = {R.id.offer_dark, R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    int[] clickableViewIds = {R.id.get, R.id.favourite, R.id.rate, R.id.location, R.id.recommend};
    String email;
    String mobileNumber;
    String password;
    ProgressDialog spinner;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFrame = (FrameLayout) findViewById(R.id.register_frame);
        registerField = (EditText) findViewById(R.id.register_field);
        registerInstruction = (TextView) findViewById(R.id.register_instruction);
        registerDataName = (TextView) findViewById(R.id.register_data_name);
        registerBackground = (ImageView) findViewById(R.id.register_background);
        qrImage = (ImageView) findViewById(R.id.QRCodeImage);
        registerField.setTag(registerField.getKeyListener());
        registerField.setKeyListener(null);

        cardForm = (FrameLayout) findViewById(R.id.card_form);
        cardNumber = (EditText) findViewById(R.id.CardNumber);
        cvc = (EditText) findViewById(R.id.CVC);
        endMonth = (EditText) findViewById(R.id.EndMonth);
        endYear = (EditText) findViewById(R.id.EndYear);

        cardForm.setVisibility(View.INVISIBLE);
        cardNumber.setTag(cardNumber.getKeyListener());

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_hide);
        registerFrame.startAnimation(anim);

        registerContinue = (TextView) findViewById(R.id.register_continue);
        registerContinue.setClickable(false);

        setupRecycler();
//        new GetRegistrationOffers_Async(this, "josh5721").execute(51.517273, -0.061330);
        OfferListEntity e = new OfferListEntity();
        List<OfferEntity> l = new ArrayList();
        OfferEntity o = new OfferEntity();
        o.setDistance("1 minute");
        o.setOfferId((long) 1);
        o.setOfferText("Nutella Banana with hot drink or coke");
        o.setClientName("The Crepe Shop and Art Cafe");
        o.setFavourite(false);
        o.setClientOfferPhoto("https://yapnak-app.appspot.com/images/4.jpg");
        l.add(o);
        o = new OfferEntity();
        o.setDistance("1 minute");
        o.setOfferId((long) 2);
        o.setOfferText("Chicken donor wrap and soft drink");
        o.setClientName("Efes Bricklane");
        o.setFavourite(false);
        o.setClientOfferPhoto("https://yapnak-app.appspot.com/images/3.jpg");
        l.add(o);
        e.setOfferList(l);
        loadOffers(e);
    }

    public void loadOffers(OfferListEntity response) {
        mAdapter = new RegisterListAdapter(response, this);
        mRecyclerView.setAdapter(mAdapter);
        registrationProgress();
    }

    void setupRecycler() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    void registrationProgress() {
        Log.d("debug", "at " + stage);
        switch (stage) {
            case 1:
                //Ask to tap a card
                caseOne();
                break;
            case 2:
                //Ask for email
                caseTwo();
                break;
            case 3:
                //Hide then ask to click 'get'
                caseThree();
                break;
            case 4:
                //Ask for mobile number
                caseFour();
                break;
            case 5:
                //Hide, show top of the box and then ask for password
                caseFive();
                break;
            case 6:
                caseSix();
                break;
        }
        stage += 1;
    }

    void caseOne() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_up_second);
        registerFrame.startAnimation(anim);
    }

    void caseTwo() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_up_second);
        registerFrame.startAnimation(anim);
        registerField.setKeyListener((KeyListener) registerField.getTag());
        registerContinue.setClickable(true);
    }

    void caseThree() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_down);
        final Context context = this;
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.registration_up_first);
//                Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.registration_up_second);
                //Show scan image here

                registerInstruction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                registerInstruction.setText("2: Tap \"GET\" for a QR code that can be scanned in store.");
                registerDataName.setText("Enter Mobile Number");
                registerField.setInputType(InputType.TYPE_CLASS_NUMBER);
//                registerFrame.startAnimation(anim2);
                registrationProgress();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        registerFrame.startAnimation(anim);
        registerField.setKeyListener(null);
        registerContinue.setClickable(false);
    }

    void caseFour() {
        Editable e = registerField.getText();
        email = e.toString();
        registerField.setText(null);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_up_second);
        registerFrame.startAnimation(anim);
        registerField.setKeyListener((KeyListener) registerField.getTag());
        qrImage.setVisibility(View.VISIBLE);
        registerContinue.setClickable(true);
    }

    void caseFive() {
        registerField.setKeyListener(null);
        registerContinue.setClickable(false);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_down);
        final Context context = this;
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Editable e = registerField.getText();
                mobileNumber = e.toString();
                registerField.setText(null);
                registerInstruction.setText("3: Pay £5 in-store and enjoy your meal!");
                registerInstruction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                registerDataName.setText("Enter Password");
                Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.registration_up_second);
                registerField.setKeyListener((KeyListener) registerField.getTag());
                registerField.setInputType(InputType.TYPE_CLASS_TEXT);
                registerContinue.setClickable(true);
                registerFrame.startAnimation(anim2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        registerFrame.startAnimation(anim);
    }

    void caseSix() {
        registerField.setKeyListener(null);
        registerContinue.setClickable(false);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.registration_down);
        final Context context = this;

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Editable e = registerField.getText();
                password = e.toString();

                Animation anim2 = AnimationUtils.loadAnimation(context, R.anim.registration_up_second);
                cardForm.setVisibility(View.VISIBLE);
                cardNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Debug", "called");
                    }
                });
                cardForm.startAnimation(anim2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        registerFrame.startAnimation(anim);
    }

    public void Test(View view) {
        Log.d("Debug", "Called");
    }

    public void registerCard(View view) {
        Card card = null;
        try {
            card = new Card(cardNumber.getText().toString(), Integer.parseInt(endMonth.getText().toString()), Integer.parseInt(endYear.getText().toString()), cvc.getText().toString());
            if (!card.validateCard()) {
                new Alert_Dialog(this).cardValidationFailed();
                return;
            }
        } catch (Exception e) {
            new Alert_Dialog(this).cardValidationFailed();
            return;
        }

        Stripe stripe = null;
        try {
            stripe = new Stripe("pk_test_l54Tnxt1Zue4lJJPHZ4Vpu93");
            final Context context = this;
            final Register register = this;
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            Log.d("Debug", "Got token: " + token.toString());
                            register.token = token.getId();
                            spinner();
                            new Registration_Async(register).execute(password, mobileNumber, email);
                        }
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(context,
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    public void registerEmail(View view) {
        hideSoftKeyboard();
        registrationProgress();
    }

    public void onCardClick(View card) {
//        if (stage == 2) {
//            if (card != currentCard && currentCard != null) {
//                //Another card is already selected, deselect
//                currentCard.setTag(0);
//                for (int i : initialViewIds) {
//                    unFade(currentCard, i);
//                }
//                for (int i : fadedViewIds) {
//                    doFade(currentCard, i);
//                }
//                for (int i : clickableViewIds) {
//                    currentCard.findViewById(i).setEnabled(false);
//                    currentCard.findViewById(i).setClickable(false);
//                }
//            }
//            if (card.getTag() != 1) {
//                //Fade selected card
//                card.setTag(1);
//                for (int i : initialViewIds) {
//                    doFade(card, i);
//                }
//                for (int i : fadedViewIds) {
//                    unFade(card, i);
//                }
//                for (int i : clickableViewIds) {
//                    card.findViewById(i).setEnabled(true);
//                    card.findViewById(i).setClickable(true);
//                }
//                currentCard = card;
//            } else {
//                //Unfade selected card
//                card.setTag(0);
//                for (int i : initialViewIds) {
//                    unFade(card, i);
//                }
//                for (int i : fadedViewIds) {
//                    doFade(card, i);
//                }
//                for (int i : clickableViewIds) {
//                    card.findViewById(i).setEnabled(false);
//                    card.findViewById(i).setClickable(false);
//                }
//                currentCard = null;
//            }
//            registrationProgress();
//        }
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
        if (stage == 4) {
            Intent intent = new Intent(this, QRCodeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            JSONObject json = new JSONObject();
            try {
                json.put("id", "josh5721");
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
            registrationProgress();
        }
    }

    public void onRateClick(View card) {

    }

    public void onFavouriteClick(View card) {
        if (card.getTag() == 0) {
            //Add card to favourites
            ((ImageButton) card).setImageResource(R.drawable.heart_filled);
            card.setTag(1);
        } else {
            //Unfavourite card
            ((ImageButton) card).setImageResource(R.drawable.heart);
            card.setTag(0);
        }
    }

    public void onLocationClick(View card) {

    }

    public void onRecommendClick(View card) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home: {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            }
        }
        return false;
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Signing up");
        spinner.setMessage("Fetching my pen and paper, just a mo'");
        spinner.setCancelable(false);
        spinner.show();
    }

}
