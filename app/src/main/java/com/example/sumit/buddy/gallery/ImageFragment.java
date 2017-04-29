package com.example.sumit.buddy.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.sumit.buddy.R;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ImageFragment extends Fragment {

    private int imageId;
    private static PhotoViewAttacher mAttacher;

    public static ImageFragment getInstance(int imgId) {
        ImageFragment fragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("IMAGE_ID", imgId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageId = getArguments().getInt("IMAGE_ID");


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.img);
        imageView.setImageResource(imageId);
        mAttacher = new PhotoViewAttacher(imageView);
        return view;
    }
}