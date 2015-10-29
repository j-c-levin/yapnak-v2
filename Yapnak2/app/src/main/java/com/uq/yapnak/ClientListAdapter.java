package com.uq.yapnak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yapnak.gcmbackend.userEndpointApi.model.OfferEntity;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

import java.io.InputStream;

/**
 * Created by Joshua on 26/10/2015.
 */
public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolder> {

    private OfferListEntity offerList;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private ImageView offerImage;
        private TextView offerText;
        private TextView clientName;
        private TextView offerDistance;

        public ViewHolder(View v) {
            super(v);
            offerText = (TextView) v.findViewById(R.id.offer_text);
            clientName = (TextView) v.findViewById(R.id.client_name);
            offerDistance = (TextView) v.findViewById(R.id.offer_distance);
            offerImage = (ImageView) v.findViewById(R.id.offer_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ClientListAdapter(OfferListEntity offerList) {
        this.offerList = offerList;
    }


    @Override
    public ClientListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
// create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offer_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClientListAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        OfferEntity offer = offerList.getOfferList().get(position);
        holder.clientName.setText(offer.getClientName());
        holder.offerText.setText(offer.getOfferText());
        holder.offerDistance.setText(offer.getDistance());
        new DownloadImageTask(holder.offerImage).execute(offerList.getOfferList().get(position).getClientOfferPhoto());
    }

    @Override
    public int getItemCount() {
        return offerList.getOfferList().size();
    }
}

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.w("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
