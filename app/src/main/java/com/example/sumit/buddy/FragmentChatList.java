package com.example.sumit.buddy;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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


public class FragmentChatList extends Fragment {

    private static final String TAG = "FragmentChatList";

    private ListView mChatUserList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    private ChildEventListener mChildEventListener;

    final List<UserDetail> userDetails = new ArrayList<>();
    DatabaseReference databaseReference = null;
    ChatUserListAdapter adapter = null;

    private String myUid;

    public FragmentChatList() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        // Log.v(TAG, "fragment is being called...");
        mChatUserList = (ListView) view.findViewById(R.id.chat_list);
        registerForContextMenu(mChatUserList);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // user signed in

                    myUid = mAuth.getCurrentUser().getUid();


                    adapter = new ChatUserListAdapter(getActivity(), userDetails);
                    mChatUserList.setAdapter(adapter);

                    databaseReference = mDatabase.getReference().child("messages").child(myUid);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshots) {
                            adapter.clear();

                            for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                                // we are iterating over the users with whom current users has chatted...

                                final String uId = dataSnapshot.getKey();
                                final UserDetail userDetail = new UserDetail();
                                userDetail.setUId(uId);
                                mDatabase.getReference().child("users").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, String> info = (Map<String, String>) dataSnapshot.getValue();
                                        userDetail.setName(info.get("name"));
                                        userDetail.setEmail(info.get("email"));
                                        userDetail.setProfileUrl(info.get("profileUrl"));

                                        adapter.add(userDetail);
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

                } else {
                    // user not signed in
                    // TODO DO THIS THROUGH ACTIVITY
                    startActivity(new Intent(getActivity(), ActivityAuthHome.class));
                }
            }
        };


        /*
        FirebaseListAdapter<String> adapter = new FirebaseListAdapter<String>(MainActivity.this,
                String.class, android.R.layout.simple_list_item_1, databaseReference) {
            @Override
            protected void populateView(View v, String model, int position) {

                Log.v(TAG, "position = " + position + " model = " + model);
                ((TextView) v.findViewById(android.R.id.text1)).setText(model);
            }
        };
        */
        //  mChatUserList.setAdapter(adapter);

        mChatUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int positionInAdapter, long rowId) {

                // TODO DO THIS THROUGH ACTIVITY
                String uId = ((ChatUserListAdapter.ViewHolder) view.getTag()).mTvUid.getText().toString().trim();

                Intent intent = new Intent(getActivity(), ActivityChatDetail.class);
                intent.putExtra(MainActivity.CHAT_LIST_ITEM_USER_ID, uId);
                startActivity(intent);

            }
        });

        View emptyViewForChatList = inflater.inflate(R.layout.empty_chat_list, null);
        mChatUserList.setEmptyView(emptyViewForChatList);
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
        //TODO remove child event listener from database reference...
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.chat_list_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.chat_list_delete:

                mDatabase.getReference()
                        .child("messages")
                        .child(mAuth.getCurrentUser().getUid())
                        .child(((TextView) info.targetView.findViewById(R.id.uid)).getText().toString())
                        .removeValue();

                adapter.notifyDataSetChanged();

                return true;

            case R.id.chat_list_hide:

                return true;

            default:
                return super.onContextItemSelected(item);
        }
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









