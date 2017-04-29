package com.example.sumit.buddy.gallery;


import android.content.Context;
import android.widget.ImageView;

public interface MediaLoader {

    public void loadThumbnail(Context context, ImageView thumbnailView, SuccessCallbacks successCallbacks);

    interface SuccessCallbacks {
        void onSuccess();
    }
}
