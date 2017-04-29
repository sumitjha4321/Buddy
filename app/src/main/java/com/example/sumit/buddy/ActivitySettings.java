package com.example.sumit.buddy;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.location.PermissionUtils;
import com.example.sumit.buddy.utils.CircleTransform;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class ActivitySettings extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PermissionUtils.PermissionAskListener, OnMapReadyCallback {

    private static final String TAG = "ActivitySettings";


    private Toolbar mToolbar;
    private TextView mTvEmail;
    private EditText mEtName;
    private Button mBtnLogout;

    private UserDetail mCurrentUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;

    // Location stuff
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private boolean mHasLocationPermission, mHasLocationSettings;
    private GoogleMap mGoogleMap;


    private static final int LOCATION_REQUEST_INTERVAL = 1000 * 3600;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 1000 * 3600;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    public static final int LOCATION_SETTINGS_REQUEST_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
        mDatabase = FirebaseDatabase.getInstance();


        mCurrentUser = (UserDetail) getIntent().getSerializableExtra("CURRENT_USER"); // we could have got this using firebase,
        //...but we have received it from intent here..

        Log.v(TAG, mCurrentUser.toString());

        mBtnLogout = (Button) findViewById(R.id.btn_logout);
        mTvEmail = (TextView) findViewById(R.id.tv_email_field);
        mTvEmail.setText(mCurrentUser.getEmail());
        mEtName = (EditText) findViewById(R.id.et_profile_name);
        mEtName.setText(mCurrentUser.getName());

        Glide.with(this).load(mCurrentUser.getProfileUrl())
                .centerCrop()
                .placeholder(R.drawable.empty_profile)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView) findViewById(R.id.img_profile_settings));

        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.myConnectionRef.setValue(Boolean.FALSE);
                MainActivity.lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                mAuth.signOut();
                startActivity(new Intent(ActivitySettings.this, MainActivity.class));
            }
        });

        init();

        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        final ScrollView rootScrollViewOfMap = (ScrollView) findViewById(R.id.root_scrollview);
        mapFragment.setOnTouchListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                rootScrollViewOfMap.requestDisallowInterceptTouchEvent(true);
            }
        });
        mapFragment.getMapAsync(this);
    }

    private void init() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkLocationPermission();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    // PermissionAskListener methods...
    @Override
    public void onPermissionGranted() {

    }

    @Override
    public void onPermissionRequest() {

    }

    @Override
    public void onPermissionPreviouslyDenied() {

    }

    @Override
    public void onPermissionDisabled() {

    }

    private void requestLocationPermission() {

        //We are not using ** ActivityCompat.requestPermission(...) ** here
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);

        //you will receive callback to ***onRequestPermissionsResult****

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mHasLocationPermission = true;
                    checkLocationSettings();


                } else {

                    //user did not grant the location permission, show a dialog to re-enable the permission or finish the activity

                    new AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage("This feature needs to location permission. Please enable location permission.")
                            .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    requestLocationPermission();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mHasLocationPermission = false;


                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }


    private void checkLocationPermission() {

        PermissionUtils.checkPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, new PermissionUtils.PermissionAskListener() {
            @Override
            public void onPermissionGranted() {
                Log.v(TAG, "Permission already granted");
                //Permission already granted, now check location settings...
                mHasLocationPermission = true;
                checkLocationSettings();
            }

            @Override
            public void onPermissionRequest() {
                /*
                    we don't have permission available
                    First, request for the permission, if the user allows,
                    ...then go for checkLocationSettings
                 */
                Log.v(TAG, "onPermissionRequest | Requesting permission...");


                requestLocationPermission();
                /*
                    *** Now, once we request the permission, based on user's action, we will recieve callback to
                        ...onRequestPermissionResult - from there check if the user allowed the permission or not
                        ...and then move ahead to check the location settings...
                 */

            }

            @Override
            public void onPermissionPreviouslyDenied() {

                Log.v(TAG, "onPermissionPreviouslyDenied | Requesting permission...");


                //User had denied the permission earlier, May be you want to show the dialog showing that this permission
                // ...is required
                //But here, I am simply requesting the permission again...

                requestLocationPermission();

            }

            @Override
            public void onPermissionDisabled() {
                Log.v(TAG, "onPermissionDisabled | Requesting permission...");


                //User has disabled the location permission...Prompt them to go to settings and enable the permission...


                new AlertDialog.Builder(ActivitySettings.this)
                        .setTitle("Location Permission Disabled")
                        .setMessage("You have disable the location permission.\n Please enable the permission.")
                        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                                //TODO:make it startActivityForresult and then again check if he/she enabled permission or not
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHasLocationPermission = false;
                                dialog.cancel();
                                finish();
                            }
                        })
                        .show();
            }
        });

    }


    private void checkLocationSettings() {
        //We have location permission
        //...now check if the location settings is on/off.


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                builder.build()
        );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        mHasLocationSettings = true;
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(ActivitySettings.this, LOCATION_SETTINGS_REQUEST_CODE);
                            //now you will receive callback on ******onActivityResult******
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });


    }


    private void startLocationUpdates() {

        if (!(mHasLocationPermission && mHasLocationSettings)) {
            return;
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_SETTINGS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mHasLocationSettings = true;
                    startLocationUpdates();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Location settings disabled")
                            .setMessage("This feature needs to access your location. Please turn on your location.")
                            .setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkLocationSettings();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mHasLocationSettings = false;
                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            default:
                break;
        }


    }


    @Override
    public void onLocationChanged(Location location) {

        updateLocationInMap(location);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

    }

    private void updateLocationInMap(Location location) {

        if (mGoogleMap != null) {

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("Your Location")
            );

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLatLng)      // Sets the center of the map to Mountain View
                    .zoom(15)                   // Sets the zoom
                    //      .bearing(90)                // Sets the orientation of the camera to east
                    //       .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }


    }
}
