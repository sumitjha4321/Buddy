package com.example.sumit.buddy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.utils.CircleTransform;

import java.util.List;


public class ContactListAdapter extends ArrayAdapter<UserDetail> {

    private final LayoutInflater mLayoutInflater;
    private List<UserDetail> mUserDetails;

    public ContactListAdapter(Context context, List<UserDetail> objects) {
        super(context, -1, objects);
        mLayoutInflater = LayoutInflater.from(context);
        mUserDetails = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.chat_contact_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mProfileImage = (ImageView) convertView.findViewById(R.id.img_user);
            viewHolder.mTvName = (TextView) convertView.findViewById(R.id.username);
            viewHolder.mTvEmail = (TextView) convertView.findViewById(R.id.user_email);
            viewHolder.mTvUid = (TextView) convertView.findViewById(R.id.uid);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        UserDetail userDetail = mUserDetails.get(position);

        viewHolder.mTvName.setText(userDetail.getName());
        viewHolder.mTvEmail.setText(userDetail.getEmail());
        viewHolder.mTvUid.setText(userDetail.getUId());

        Glide.with(getContext()).load(userDetail.getProfileUrl())
                .centerCrop()
                .placeholder(R.drawable.empty_profile)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.mProfileImage);

        return convertView;
    }

    public class ViewHolder {

        public ImageView mProfileImage;
        public TextView mTvName;
        public TextView mTvEmail;
        public TextView mTvUid;
    }
}