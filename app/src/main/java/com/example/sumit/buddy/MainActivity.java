package com.example.sumit.buddy;

import android.content.Intent;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.utils.CircleTransform;
import com.example.sumit.buddy.utils.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final Object mProfileImageLock = new Object();

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView; // this is the ENTIRE navigation view
    private View mNavigationHeaderView; // this is ONLY navigation header view
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    private UserDetail mCurrentUserDetail;

    // DatabaseReference related to USER-PRESENCE functionality
    public static DatabaseReference myConnectionRef;
    public static DatabaseReference lastOnlineRef;
    public static DatabaseReference connectedRef;


    // nav header clicked
    private boolean mIsNavigationHeaderSelected;
    // navigation view item selected
    private boolean mIsNavigationViewItemSelected;
    private int mNavigationViewItemSelectedId;


    public static final String CHAT_LIST_ITEM_USER_ID = "uId"; // This is public, because this is used in ActivityChatDetail too

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OneSignal.startInit(this).init();

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    // Start of UPDATING USER INFO IN THE NAVIGATION DRAWER
                    mDatabase.getReference().child("users").child(mAuth.getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mCurrentUserDetail = dataSnapshot.getValue(UserDetail.class);
                                    Glide.with(MainActivity.this).load(mCurrentUserDetail.getProfileUrl())
                                            .centerCrop()
                                            .placeholder(R.drawable.empty_profile)
                                            .crossFade()
                                            .thumbnail(0.5f)
                                            .bitmapTransform(new CircleTransform(MainActivity.this))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into((ImageView) mNavigationHeaderView.findViewById(R.id.imageView));

                                    ((TextView) mNavigationHeaderView.findViewById(R.id.user_name)).setText(mCurrentUserDetail.getName());
                                    ((TextView) mNavigationHeaderView.findViewById(R.id.user_email)).setText(mCurrentUserDetail.getEmail());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                    // END OF UPDATING USER INFO IN THE NAVIGATION DRAWER

                    //START OF UPDATING USER PRESENCE
                        /*
                            -we can be connected from multiple device simultaneously, and we store each connections
                            -whenever we are offline, we remove this value, and update our last seen ref.
                            -If **myConnectionsRef** is null,
                                    it means we are offline,
                                    And in this case, we retrieve the value from **lastOnlineRef** to check the user's last presence
                         */
                    /*
                        Now we are not storing multiple device...

                    final DatabaseReference myConnectionsRef = mDatabase.getReference()
                            .child("users")
                            .child(firebaseAuth.getCurrentUser().getUid())
                            .child("connections");*/

                    myConnectionRef = mDatabase.getReference()
                            .child("users")
                            .child(firebaseAuth.getCurrentUser().getUid())
                            .child("is_connected");

                    lastOnlineRef = mDatabase.getReference()
                            .child("users")
                            .child(firebaseAuth.getCurrentUser().getUid())
                            .child("last_online");

                    connectedRef = mDatabase.getReference(".info/connected");
                    connectedRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean connected = dataSnapshot.getValue(Boolean.class);
                            if (connected) {
                                Log.v(TAG, "YOU ARE ONLINE");
                                DatabaseReference conn = myConnectionRef; // Earlier, we used to do "myConnectionsRef.push()"
                                conn.setValue(Boolean.TRUE);

                                conn.onDisconnect().setValue(Boolean.FALSE);
                                lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                            } else {
                                Log.v(TAG, "YOU ARE OFFLINE");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                } else {
                    startActivity(new Intent(MainActivity.this, ActivityAuthHome.class));
                }

            }
        };


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_closed) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mIsNavigationViewItemSelected) {
                    mIsNavigationViewItemSelected = false;
                    switch (mNavigationViewItemSelectedId) {
                        case R.id.nav_menu_settings:
                            Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.nav_menu_share:
                            Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                } else if (mIsNavigationHeaderSelected) {
                    mIsNavigationHeaderSelected = false;
                    Intent intent = new Intent(MainActivity.this, ActivitySettings.class);
                    intent.putExtra("CURRENT_USER", mCurrentUserDetail);
                    startActivity(intent);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationHeaderView = mNavigationView.getHeaderView(0);


        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        ChatListPagerAdapter adapter = new ChatListPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentChatList(), "CHATS");  //create fragment for chat list and contacts...
        adapter.addFragment(new FragmentContactList(), "CONTACTS");  //create fragment for chat list and contacts...
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                mIsNavigationViewItemSelected = true;
                mNavigationViewItemSelectedId = item.getItemId();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mNavigationHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsNavigationHeaderSelected = true;
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        Log.v(TAG, "continuing onStart by thread: id = " + Thread.currentThread().getId() + " name = " + Thread.currentThread().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
}
