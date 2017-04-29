package com.example.sumit.buddy.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;

public class Utility {

    public static class BitmapTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<Bitmap> mBitmap;

        public BitmapTask(Bitmap bitmap) {
            mBitmap = new WeakReference<Bitmap>(bitmap);

        }

        @Override
        protected Bitmap doInBackground(String... params) {

            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            Bitmap response = null;

            try {
                URL url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                inputStream = httpURLConnection.getInputStream();
                response = BitmapFactory.decodeStream(inputStream);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Bitmap response) {

            if (mBitmap != null) {
                Bitmap bitmap = mBitmap.get();
                bitmap = response;
            }

        }
    }

    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager con_manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }




}

