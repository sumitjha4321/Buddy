package com.example.sumit.buddy;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.location.PermissionUtils;
import com.example.sumit.buddy.utils.CircleTransform;
import com.example.sumit.buddy.utils.UserAvailability;
import com.google.android.gms.auth.TokenData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.w3c.dom.Text;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class ActivityProfile extends AppCompatActivity {

    private static final String TAG = "ActivityProfile";

    private UserDetail mCurrentUser;

    private TextView mTvName;
    private ImageView mProfileImage;
    private ImageView mUserAvailabilityIcon;
    private TextView mUserAvailabilityText;
    private FloatingActionButton mFab;
    private LinearLayout mMediaContainer;
    private String mCurrentUserUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCurrentUser = (UserDetail) getIntent().getSerializableExtra("CURRENT_USER");
        mCurrentUserUid = mCurrentUser.getUId();

        mTvName = (TextView) findViewById(R.id.user_name);
        mProfileImage = (ImageView) findViewById(R.id.user_image);
        mUserAvailabilityIcon = (ImageView) findViewById(R.id.user_availability);
        mUserAvailabilityText = (TextView) findViewById(R.id.user_availability_text);
        mMediaContainer = (LinearLayout) findViewById(R.id.media_container);

        // name
        mTvName.setText(mCurrentUser.getName());
        // profile pic
        Glide.with(this).load(mCurrentUser.getProfileUrl())
                // .centerCrop()
                .placeholder(R.drawable.empty_profile)
                .crossFade()
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mProfileImage);

        // user-availability text and icon
        // @params: passing fields(ImageView and TextView) as parameter.
        Object[] fields = new Object[2];
        fields[0] = mUserAvailabilityIcon;
        fields[1] = mUserAvailabilityText;
        UserAvailability.updateUserStatus(mCurrentUserUid, fields);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityProfile.this, ActivityChatDetail.class);
                intent.putExtra(MainActivity.CHAT_LIST_ITEM_USER_ID, mCurrentUser.getUId());
                startActivity(intent);
            }
        });

        mMediaContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityProfile.this, ActivityMedia.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
}
