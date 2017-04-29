package com.example.sumit.buddy.utils;


import android.widget.ImageView;
import android.widget.TextView;

import com.example.sumit.buddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserAvailability {

    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    public static void updateUserStatus(final String uId, final Object[] fields) {

        final ImageView userAvailabilityImgV = (ImageView) fields[0];
        final TextView userAvailabilityTV = (TextView) fields[1];


        DatabaseReference offsetRef = mDatabase.getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                // We got SERVER'S CURRENT TIME
                final long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                // now, check user is online or not, if not, find the last online time
                final DatabaseReference reference = mDatabase.getReference()
                        .child("users")
                        .child(uId);

                reference.child("is_connected")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean connected = dataSnapshot.getValue(Boolean.class);
                                if (connected) {
                                    userAvailabilityImgV.setImageResource(R.drawable.chat_online);
                                    userAvailabilityTV.setText("Online");
                                } else {
                                    reference.child("last_online").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Long lastSeenTimeMs = dataSnapshot.getValue(Long.class);
                                            userAvailabilityImgV.setImageResource(R.drawable.chat_offline);
                                            userAvailabilityTV.setText(getLastSeenString(estimatedServerTimeMs, lastSeenTimeMs));
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
    }

    private static String getLastSeenString(long server, long last_seen) {
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
}
