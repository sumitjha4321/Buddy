package com.example.sumit.buddy;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.utils.CircleTransform;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ChatUserListAdapter extends ArrayAdapter<UserDetail> {

    private static final String TAG = "ChatUserListAdapter";
    private List<UserDetail> userDetails;


    public ChatUserListAdapter(Context context, List<UserDetail> userDetails) {
        super(context, -1, userDetails);
        this.userDetails = userDetails;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.chat_user_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTvName = (TextView) convertView.findViewById(R.id.username);
            viewHolder.mProfileImage = (ImageView) convertView.findViewById(R.id.img_user);
            viewHolder.mLastChatDate = (TextView) convertView.findViewById(R.id.date);
            viewHolder.mTvUid = (TextView) convertView.findViewById(R.id.uid);

            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        UserDetail chatUserListItem = userDetails.get(position);

        Log.v(TAG, "setting for position = " + position + " name = " + chatUserListItem.getName() + " email = " + chatUserListItem.getEmail());

        viewHolder.mTvName.setText(chatUserListItem.getName());
        viewHolder.mTvUid.setText(chatUserListItem.getUId());
        viewHolder.mLastChatDate.setText("10:10 AM");

        Glide.with(getContext()).load(chatUserListItem.getProfileUrl())
                .centerCrop()
                .placeholder(R.drawable.empty_profile)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.mProfileImage);

        // TODO fill last chat date
        return convertView;
    }


    @Override
    public void add(UserDetail object) {
        super.add(object);
    }

    public class ViewHolder {
        public TextView mTvName;
        public ImageView mProfileImage;
        public TextView mLastChatDate;
        public TextView mTvUid; // this TextView has visibility=gone in xml | will be used to pass this info in
        //...list item click
    }
}