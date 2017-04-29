package com.example.sumit.buddy.gallery;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class DefaultImageLoader implements MediaLoader {

    private int mImageId;
    private Bitmap mImageBitmap;

    public DefaultImageLoader(int id) {
        mImageId = id;
    }

    public DefaultImageLoader(Bitmap bitmap) {
        mImageBitmap = bitmap;
    }

    @TargetApi(21)
    @Override
    public void loadThumbnail(Context context, ImageView thumbnailView, SuccessCallbacks successCallbacks) {
        if (mImageBitmap == null) {
            // This means we were given id, not the bitmap
            // so, get bitmap from that id.
            mImageBitmap = ((BitmapDrawable) context.getResources().getDrawable(mImageId, null)).getBitmap();
        }
        thumbnailView.setImageBitmap(mImageBitmap);
        successCallbacks.onSuccess();
    }
}
