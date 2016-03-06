package com.uq.yapnak;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yapnak.gcmbackend.userEndpointApi.model.OfferEntity;
import com.yapnak.gcmbackend.userEndpointApi.model.OfferListEntity;

/**
 * Created by Joshua on 13/11/2015.
 */
public class RegisterListAdapter extends RecyclerView.Adapter<RegisterListAdapter.ViewHolder> {

    private OfferListEntity offerList;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private ImageView offerImage;
        private TextView offerText;
        private TextView clientName;
        private TextView offerDistance;
        private TextView clientId;
        private TextView offerId;
        private TextView gps;
        private ImageButton favourite;

        public ViewHolder(View v) {
            super(v);
            offerText = (TextView) v.findViewById(R.id.offer_text);
            clientName = (TextView) v.findViewById(R.id.client_name);
            offerDistance = (TextView) v.findViewById(R.id.offer_distance);
            offerImage = (ImageView) v.findViewById(R.id.offer_image);
            clientId = (TextView) v.findViewById(R.id.clientId);
            offerId = (TextView) v.findViewById(R.id.offerId);
            gps = (TextView) v.findViewById(R.id.gps);
            favourite = (ImageButton) v.findViewById(R.id.favourite);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RegisterListAdapter(OfferListEntity offerList, Context context) {
        this.offerList = offerList;
        this.context = context;
    }

    @Override
    public RegisterListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
// create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offer_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RegisterListAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        OfferEntity offer = offerList.getOfferList().get(position);
        holder.clientName.setText(offer.getClientName());
        holder.offerText.setText(offer.getOfferText());
        holder.offerDistance.setText(offer.getDistance());
        holder.clientId.setText(String.valueOf(offer.getClientId()));
        holder.offerId.setText(String.valueOf(offer.getOfferId()));
        holder.gps.setText(String.valueOf(offer.getLatitude() + "," + offer.getLongitude()));
        new DownloadImageTask(holder.offerImage, context).execute(offer.getClientOfferPhoto());
        if (offer.getFavourite()) {
            holder.favourite.setImageResource(R.drawable.heart_filled);
            holder.favourite.setTag(1);
        } else {
            holder.favourite.setTag(0);
        }
    }

    @Override
    public int getItemCount() {
        return offerList.getOfferList().size();
    }
}
