package com.example.sumit.buddy.gallery;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.sumit.buddy.R;

import java.util.ArrayList;


public class ScrollGalleryView extends LinearLayout {

    private static final String TAG = "ScrollGalleryView";

    private LinearLayout mThumbnailsContainer;
    private Context mContext;
    private HorizontalScrollView mHorizontalScrollView;
    private int mThumbnailSize;
    private ArrayList<Integer> mListOfMedia = new ArrayList<>();
    private ViewPager mViewPager;
    private FragmentManager mFragmentManager;
    private ScrollSlidePagerAdapter adapter;
    private Point mDisplayProps;
    private int mViewPagerPreviousItem = 0;
    private ActionBar mSupportActionBar;


    public ScrollGalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        Log.v(TAG, attrs.toString());

        LayoutInflater.from(mContext).inflate(R.layout.scroll_gallery_view, this, true);
        mDisplayProps = getDisplaySize();
        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.thumbnails_scroll_view);
        mThumbnailsContainer = (LinearLayout) findViewById(R.id.thumbnails_container);
    }

    private final OnClickListener mThumbnailClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            mViewPager.setCurrentItem((int) view.getTag());
            scroll(view);


        }
    };


    private Point getDisplaySize() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }


    public ScrollGalleryView setThumbnailSize(int size) {
        mThumbnailSize = size;
        return this;
    }

    public ScrollGalleryView addMedia(int imageId) {
        return addMedia(new int[]{imageId});
    }

    public ScrollGalleryView addMedia(int[] imageIds) {

        for (int imageId : imageIds) {
            mListOfMedia.add(imageId);

            final ImageView thumbnail = addThumbnail(getDefaultThumbnail());
            new DefaultImageLoader(imageId).loadThumbnail(mContext, thumbnail, new MediaLoader.SuccessCallbacks() {
                @Override
                public void onSuccess() {
                    thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            });
        }
        adapter.notifyDataSetChanged();
        mThumbnailsContainer.getChildAt(0).setBackgroundColor(Color.RED);
        return this;
    }

    @TargetApi(21)
    private Bitmap getDefaultThumbnail() {
        return ((BitmapDrawable) getResources().getDrawable(R.drawable.placeholder_image, null)).getBitmap();
    }

    private ImageView addThumbnail(Bitmap bitmap) {

        ImageView thumbnailView = new ImageView(mContext);
        thumbnailView.setLayoutParams(new LayoutParams(mThumbnailSize, mThumbnailSize));
        thumbnailView.setPadding(4, 4, 4, 4);
        thumbnailView.setImageBitmap(createThumbnail(bitmap));
        thumbnailView.setTag(mListOfMedia.size() - 1);
        thumbnailView.setOnClickListener(mThumbnailClickListener);
        thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
        mThumbnailsContainer.addView(thumbnailView);
        return thumbnailView;
    }

    private Bitmap createThumbnail(Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap, mThumbnailSize, mThumbnailSize);
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                scroll(mThumbnailsContainer.getChildAt(position));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter = new ScrollSlidePagerAdapter(mFragmentManager, mListOfMedia);
        mViewPager.setAdapter(adapter);
    }

    public ScrollGalleryView setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        initViewPager();
        return this;
    }


    private void scroll(View thumbnail) {
        if (mSupportActionBar != null) {
            mSupportActionBar.setTitle((mViewPager.getCurrentItem() + 1) + " of " + mListOfMedia.size());
        }

        mThumbnailsContainer.getChildAt(mViewPagerPreviousItem).setBackgroundColor(Color.TRANSPARENT);
        thumbnail.setBackgroundColor(Color.RED);
        mViewPagerPreviousItem = mViewPager.getCurrentItem();

        int[] thumbnailCoords = new int[2];
        thumbnail.getLocationInWindow(thumbnailCoords);
        Log.v(TAG, "Screen center = " + mDisplayProps.x / 2 + " thumbnail center = " + (thumbnailCoords[0] + mThumbnailSize / 2));

        int dx = (thumbnailCoords[0] + mThumbnailSize / 2) - mDisplayProps.x / 2;
        Log.v(TAG, "Scrolling by " + dx);
        mHorizontalScrollView.smoothScrollBy(dx, 0);
    }

    public ScrollGalleryView addActionBar(ActionBar actionBar) {
        mSupportActionBar = actionBar;
        mSupportActionBar.setTitle("1 of " + mListOfMedia.size());
        mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        return this;
    }
}