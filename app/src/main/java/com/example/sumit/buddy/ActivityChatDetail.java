package com.example.sumit.buddy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sumit.buddy.utils.CircleTransform;
import com.example.sumit.buddy.utils.Constants;
import com.example.sumit.buddy.utils.Utility;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


public class ActivityChatDetail extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ActivityChatDetail";

    private Toolbar mToolbar;
    private ListView mListMessage;
    private ImageView mBtnEmoji;
    private ImageView mBtnSend;
    private EmojiconEditText mEtMessage;
    private ImageButton mUploadImage;
    private LinearLayout mEmojiRootView;
    private EmojIconActions mEmojIconActions;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference1;
    private DatabaseReference mReference2;
    private Query mQuery;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Location
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private String myUId;
    private String friendUId;
    private String myProfileUrl;
    private String friendProfileUrl;

    private static final int RC_UPLOAD_IMAGE = 10;


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mListMessage = (ListView) findViewById(R.id.msg_list);
        registerForContextMenu(mListMessage);
        mEtMessage = (EmojiconEditText) findViewById(R.id.message);
        mBtnSend = (ImageView) findViewById(R.id.send);
        mBtnEmoji = (ImageView) findViewById(R.id.btn_emoji);
        mEmojiRootView = (LinearLayout) findViewById(R.id.emoji_root_view);
        mUploadImage = (ImageButton) findViewById(R.id.upload_image);


        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        // Getting mine and friend's user id...
        myUId = mAuth.getCurrentUser().getUid();
        friendUId = getIntent().getStringExtra(MainActivity.CHAT_LIST_ITEM_USER_ID);

        // Setting action bar title as friend's name
        mDatabase.getReference().child("users").child(friendUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDetail userDetail = dataSnapshot.getValue(UserDetail.class);
                getSupportActionBar().setTitle(userDetail.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // setting action bar's subtitle as last online
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                // We got SERVER'S CURRENT TIME
                final long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                // now, check user is online or not, if not, find the last online time
                final DatabaseReference friendRef = mDatabase.getReference()
                        .child("users")
                        .child(friendUId);

                friendRef.child("is_connected")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean connected = dataSnapshot.getValue(Boolean.class);
                                if (connected) {
                                    getSupportActionBar().setSubtitle("Online");
                                } else {
                                    friendRef.child("last_online").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Long lastSeenTimeMs = dataSnapshot.getValue(Long.class);
                                            getSupportActionBar().setSubtitle(getLastSeenString(estimatedServerTimeMs, lastSeenTimeMs));
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });


        // Getting mine and friend's profile url from user id...
        DatabaseReference reference = mDatabase.getReference().child("users");

        reference.child(myUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserDetail userDetail = dataSnapshot.getValue(UserDetail.class);
                myProfileUrl = userDetail.getProfileUrl();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reference.child(friendUId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserDetail userDetail = dataSnapshot.getValue(UserDetail.class);
                friendProfileUrl = userDetail.getProfileUrl();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mReference1 = mDatabase.getReference().child("messages").child(friendUId)
                .child(myUId);
        mReference2 = mDatabase.getReference().child("messages").child(myUId)
                .child(friendUId);


        mQuery = mReference2.orderByChild("messageTimestamp");

        Log.v(TAG, mQuery.toString());

        FirebaseListAdapter<ChatMessage> adapter = new FirebaseListAdapter<ChatMessage>(this,
                ChatMessage.class,
                -1,
                mQuery) {

            @Override
            public int getItemViewType(int position) {
                ChatMessage model = getItem(position);
                if (model.getSender().equals(friendUId)) {
                    if (model.getMessageType().equals(Constants.TEXT)) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (model.getMessageType().equals(Constants.TEXT)) {
                        return 2;
                    } else {
                        return 3;
                    }
                }
            }

            @Override
            public int getViewTypeCount() {
                return 4;
            }

            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {

                ChatMessage model = getItem(position);
                switch (getItemViewType(position)) {
                    case 0:
                        if (view == null) {
                            view = mActivity.getLayoutInflater().inflate(R.layout.message_item_1, viewGroup, false);
                        }
                        break;
                    case 1:
                        if (view == null) {
                            view = mActivity.getLayoutInflater().inflate(R.layout.message_item_1_media, viewGroup, false);
                        }
                        break;
                    case 2:
                        if (view == null) {
                            view = mActivity.getLayoutInflater().inflate(R.layout.message_item_2, viewGroup, false);
                        }
                        break;
                    case 3:
                        if (view == null) {
                            view = mActivity.getLayoutInflater().inflate(R.layout.message_item_2_media, viewGroup, false);
                        }
                        break;
                }
                // Call out to subclass to marshall this model into the provided view
                populateView(view, model, position);
                return view;
            }

            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                ImageView userImg = (ImageView) v.findViewById(R.id.img_user);

                ImageView ivMessageMedia = (ImageView) v.findViewById(R.id.message_media);
                TextView tvMessageText = (TextView) v.findViewById(R.id.message_text);
                TextView tvMessageTimestamp = (TextView) v.findViewById(R.id.message_timestamp);
                TextView tvMessageId = (TextView) v.findViewById(R.id.message_id);

                if (model.getMessageType().equals(Constants.TEXT)) {
                    tvMessageText.setText(model.getMessage());
                } else {
                    Glide.with(ActivityChatDetail.this).load(model.getMessage())
                            // .centerCrop()
                            // .placeholder(R.drawable.empty_profile)
                            .crossFade()
                            .thumbnail(0.5f)
                            //.bitmapTransform(new CircleTransform(ActivityChatDetail.this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ivMessageMedia);
                }

                if (model.getSender().equals(myProfileUrl)) {
                    Glide.with(ActivityChatDetail.this).load(myProfileUrl)
                            .centerCrop()
                            .placeholder(R.drawable.empty_profile)
                            .crossFade()
                            .thumbnail(0.5f)
                            .bitmapTransform(new CircleTransform(ActivityChatDetail.this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userImg);
                } else {
                    Glide.with(ActivityChatDetail.this).load(friendProfileUrl)
                            .centerCrop()
                            .placeholder(R.drawable.empty_profile)
                            .crossFade()
                            .thumbnail(0.5f)
                            .bitmapTransform(new CircleTransform(ActivityChatDetail.this))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userImg);
                }
                tvMessageTimestamp.setText(DateFormat.format("HH:mm", model.getMessageTimestamp()));
                tvMessageId.setText(model.getId());
                mListMessage.setSelection(position - 1);
            }
        };

        mListMessage.setAdapter(adapter);

        mEmojIconActions = new EmojIconActions(this, mEmojiRootView, mEtMessage, mBtnEmoji);
        mEmojIconActions.ShowEmojIcon();

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEtMessage.getText().toString().trim();
                if (message.equals("")) {
                    return;
                }

                // TODO : THIS IS STORING KEY AGAIN IN CHILD NODE
                // BECAUSE IN FIREBASE LIST ADAPTER, WE DON'T HAVE ACCESS TO PARENT'S KEY
                // ALSO YOU CAN'T EXTEND THE FIREBASE LIST ADAPTER AND GET THE KEY, BECAUSE the arraylist of keys has private access
                // So, copy the code of firebase list adapter from github and get the key too

                //As of now, I am just storing the key again
                DatabaseReference ref1 = mReference1.push();
                ref1.setValue(new ChatMessage(myUId, message, ref1.getKey(), Constants.TEXT));
                DatabaseReference ref2 = mReference2.push();
                ref2.setValue(new ChatMessage(myUId, message, ref2.getKey(), Constants.TEXT));

                mEtMessage.setText("");
                sendNotification(message);
            }
        });

        mUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, RC_UPLOAD_IMAGE);
            }
        });

        // Location stuffs
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_UPLOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    CropImage
                            .activity(uri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri croppedUri = result.getUri();
                    // Uploading cropped image
                    final StorageReference reference = mStorage
                            .getReference()
                            .child("media_messages")
                            .child(croppedUri.getLastPathSegment());
                    reference.putFile(croppedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Upload successful, now store the download url in the realtime database...
                            Log.v(TAG, taskSnapshot.getDownloadUrl().toString());

                            DatabaseReference ref1 = mReference1.push();
                            ref1.setValue(new ChatMessage(myUId, taskSnapshot.getDownloadUrl().toString(), ref1.getKey(), Constants.IMAGE));
                            DatabaseReference ref2 = mReference2.push();
                            ref2.setValue(new ChatMessage(myUId, taskSnapshot.getDownloadUrl().toString(), ref2.getKey(), Constants.IMAGE));


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ActivityChatDetail.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mListMessage.setSelection(mListMessage.getCount() - 1);
        //Log.v(TAG, String.valueOf(mListMessage.getCount() - 1));
        mListMessage.setStackFromBottom(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_detail_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_location:

                // Blund mera handle
                mGoogleApiClient.connect();
                return true;

            case R.id.delete_selection:
                //TODO SHOW DELETE SELECTION
                return true;

            case R.id.delete_conversation:
                new AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage("Entire conversation will be deleted")
                        .setCancelable(true)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDatabase.getReference().child("messages").child(myUId).child(friendUId).removeValue();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.chat_detail_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.chat_detail_context_menu_copy:
                ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", ((TextView) info.targetView.findViewById(R.id.message_text)).getText().toString());
                clipboardManager.setPrimaryClip(clip);
                return true;

            case R.id.chat_detail_context_menu_delete_myself:
                String messageId = ((TextView) info.targetView.findViewById(R.id.message_id)).getText().toString();
                Log.v(TAG, "Deleting message, id = " + messageId);
                mDatabase.getReference().child("messages").child(myUId).child(friendUId).child(messageId).removeValue();
                return true;

            case R.id.chat_detail_context_menu_delete_everyone:
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            mEtMessage.setText(mLocation.toString());
        } else {
            mEtMessage.setText("failed to get your location ");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mEtMessage.setText("connection failed ");


    }

    private String getLastSeenString(long server, long last_seen) {
        long diff = server - last_seen;
        diff /= 1000;
        if (diff <= 86400 || true) {
            int hour = (int) (diff / 3600);
            diff = diff % 3600;
            int min = (int) (diff / 60);
            if (hour != 0) {
                return "Last seen " + hour + (hour == 1 ? " hour " : " hours ") + min + (min == 1 ? " min " : " mins ") + "ago";
            } else {
                return "Last seen " + min + (min == 1 ? " min " : " mins ") + "ago";
            }
        }
        return "Blund mera";
    }


    private void sendNotification(final String message) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(friendUId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserDetail friendDetail = dataSnapshot.getValue(UserDetail.class);
                        try {
                            Log.v(TAG, "Sending message to " + friendDetail.getOneSignalId());
                            String[] recipients = new String[]{friendDetail.getOneSignalId()};
                            JSONObject object = new JSONObject();
                            object.put("contents", new JSONObject().put("en", message));
                            object.put("headings", new JSONObject().put("en", myUId));
                            object.put("include_player_ids", new JSONArray(recipients));

                            OneSignal.postNotification(object, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }


}
