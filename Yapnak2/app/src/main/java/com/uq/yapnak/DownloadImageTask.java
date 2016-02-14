package com.uq.yapnak;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Joshua on 29/10/2015.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    Context context;

    public DownloadImageTask(ImageView bmImage, Context context) {
        this.bmImage = bmImage;
        this.context = context;
    }

    protected Bitmap doInBackground(String... urls) {
        String fileUrl = urls[0];
        String offerId = urls[1];
        SharedPreferences prefs = context.getSharedPreferences("yapnak", 0);

        if (prefs.contains(offerId)) {

//            Log.d("debug", "offer " + offerId + " has a stored value: " + prefs.getString(offerId, "test"));
            //Check if stored image is the same as the one requested

//            Log.d("debug", "matches: " + fileUrl.split("/")[fileUrl.split("/").length - 1] + " : " + prefs.getString(offerId, "sample").equals(fileUrl.split("/")[fileUrl.split("/").length - 1]));

            if (prefs.getString(offerId, "sample").equals(fileUrl.split("/")[fileUrl.split("/").length - 1])) {

                //File is already downloaded
                Log.d("debug", "current image up to date");

            } else {
                Log.d("debug", "downloading new image");
                //Delete current image
                File file = new File(context.getFilesDir(), prefs.getString(offerId, "sample"));
                boolean deleted = file.delete();

                Log.d("debug", "deleted: " + deleted);

                //Download and store client image
                FileOutputStream out = null;
                try {
                    Bitmap newImage = null;
                    InputStream in = new java.net.URL(fileUrl).openStream();
                    newImage = BitmapFactory.decodeStream(in);
                    out = context.openFileOutput(fileUrl.split("/")[fileUrl.split("/").length - 1], Context.MODE_PRIVATE);
                    newImage.compress(Bitmap.CompressFormat.JPEG, 70, out);
                } catch (Exception e) {
                    Log.w("Error", e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (out != null)
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                //Insert the reference
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(offerId,fileUrl.split("/")[fileUrl.split("/").length - 1]);
                editor.commit();
                Log.d("debug", "new file path: " + prefs.getString(offerId, "test"));
            }
        } else {
            Log.d("debug", "no reference stored, downloading");
            //Download and store client image
            FileOutputStream out = null;
            try {
                Bitmap newImage = null;
                InputStream in = new java.net.URL(fileUrl).openStream();
                newImage = BitmapFactory.decodeStream(in);
                out = context.openFileOutput(fileUrl.split("/")[fileUrl.split("/").length - 1], Context.MODE_PRIVATE);
                newImage.compress(Bitmap.CompressFormat.JPEG, 70, out);
            } catch (Exception e) {
                Log.w("Error", e.getMessage());
                e.printStackTrace();
            } finally {
                if (out != null)
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            //Insert the reference
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(offerId,fileUrl.split("/")[fileUrl.split("/").length - 1]);
            editor.commit();
            Log.d("debug", "new file path: " + prefs.getString(offerId, "test"));
        }

        //Load image from file path
        File file = new File(context.getFilesDir(), fileUrl.split("/")[fileUrl.split("/").length - 1]);
        Bitmap response = BitmapFactory.decodeFile(file.getAbsolutePath());

        return response;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
