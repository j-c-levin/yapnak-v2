package com.uq.yapnak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QRCodeActivity extends Activity {

    ProgressDialog spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        TextView title = (TextView) findViewById(R.id.offer_title);
        title.setText(getIntent().getStringExtra("OfferName"));
    }

    public void purchaseMeal(View view) {
        spinner();
        new StripeCharge_Async(this).execute(
                getIntent().getStringExtra("UserID"),
                getIntent().getStringExtra("OfferID")
        );
    }

    public void close(View view) {
        finish();
    }

    public void purchaseSuccess() {
        TextView price = (TextView) findViewById(R.id.price);
        price.setText("Order confirmed");
        Button confirm = (Button) findViewById(R.id.confirm);
        confirm.setEnabled(false);
        confirm.setBackgroundColor(getResources().getColor(R.color.colorDeactivated));
    }

    public void purchaseFailed(String error) {
        new Alert_Dialog(this).purchaseFailed(error);
        TextView price = (TextView) findViewById(R.id.price);
        price.setText(":(");
        Button confirm = (Button) findViewById(R.id.confirm);
        confirm.setEnabled(false);
        confirm.setBackgroundColor(getResources().getColor(R.color.colorDeactivated));
    }

    void spinner() {
        spinner = new ProgressDialog(this);
        spinner.setIndeterminate(true);
        spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        spinner.setTitle("Processing order");
        spinner.setMessage("Food so close we can almost taste it");
        spinner.setCancelable(false);
        spinner.show();
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_qrcode);
//        //Find screen size
//        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        Display display = manager.getDefaultDisplay();
//        Point point = new Point();
//        display.getSize(point);
//        int width = point.x;
//        int height = point.y;
//        int smallerDimension = width < height ? width : height;
//        smallerDimension = smallerDimension * 9/10;
//        //Encode with a QR Code image
//        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(getIntent().getStringExtra("qrCode"),
//                null,
//                Contents.Type.TEXT,
//                BarcodeFormat.QR_CODE.toString(),
//                smallerDimension);
//        try {
//            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
//            ImageView myImage = (ImageView) findViewById(R.id.qr_code);
//            myImage.setImageBitmap(bitmap);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
