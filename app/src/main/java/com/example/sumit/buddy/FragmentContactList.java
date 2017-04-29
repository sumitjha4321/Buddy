package com.example.sumit.buddy;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.internal.LockOnGetVariable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentContactList extends Fragment {


    private static final String TAG = "FragmentContactList";

    private ListView mContactList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    private ChildEventListener mChildEventListener;

    final List<UserDetail> userDetails = new ArrayList<>();
    DatabaseReference databaseReference = null;
    ContactListAdapter adapter = null;

    private String myUid;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        mContactList = (ListView) view.findViewById(R.id.contact_list);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // user not signed in
                    // TODO DO THIS THROUGH ACTIVITY
                    startActivity(new Intent(getActivity(), ActivityAuthHome.class));
                } else {
                    myUid = mAuth.getCurrentUser().getUid();


                }
            }
        };


        adapter = new ContactListAdapter(getActivity(), userDetails);
        mContactList.setAdapter(adapter);

        // TODO: currently adding all users in the contact list
        // TODO : make a separate table in db which contains contact of this user
        databaseReference = mDatabase.getReference().child("users");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshots, String s) {

                UserDetail userDetail = new UserDetail();
                userDetail.setUId(dataSnapshots.getKey());
                Map<String, String> userInfo = (HashMap<String, String>) dataSnapshots.getValue();
                Log.v(TAG, userInfo.toString());
                userDetail.setName(userInfo.get("name"));
                userDetail.setEmail(userInfo.get("email"));
                userDetail.setProfileUrl(userInfo.get("profileUrl"));

                adapter.add(userDetail);

                // **userDetail.setDateOfBirth** not needed


                /*
                    Suppose we required only the childs i.e(email, name, and profile url)
                    Then we could directly do ** adapter.add(dataSnapshots.getValue(UserDetail.class)) **
                    But,
                        here we require the **UId** too, which is present as **key**,
                        so, we had to do this long method...
                 */
                //adapter.add(dataSnapshots.getValue(UserDetail.class));  [DEPRECATED, AS WE REQUIRE **UID** (i.e parent's key) too]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG, "onChildChanged " + dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.v(TAG, "onChildRemoved " + dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG, "onChildMoved " + dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final UserDetail userDetail = new UserDetail();

                String uId = ((TextView) view.findViewById(R.id.uid)).getText().toString(); // get this user's id from the textview
                userDetail.setUId(uId);
                mDatabase.getReference().child("users").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> info = (Map<String, String>) dataSnapshot.getValue();
                        userDetail.setName(info.get("name"));
                        userDetail.setProfileUrl(info.get("profileUrl"));
                        userDetail.setEmail(info.get("email"));

                        Intent intent = new Intent(getActivity(), ActivityProfile.class);
                        intent.putExtra("CURRENT_USER", userDetail);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        //TODO remove child event listerner from database reference...
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach");


    }

}
