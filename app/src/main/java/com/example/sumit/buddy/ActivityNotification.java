package com.example.sumit.buddy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityNotification extends AppCompatActivity {

    private static final String TAG = "ActivityNotification";

    private Button mBtn;
    private Button mBtnOneSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OneSignal.startInit(this).init();

        setContentView(R.layout.activity_notification);

        mBtn = (Button) findViewById(R.id.button);
        mBtnOneSignal = (Button) findViewById(R.id.btn_one_signal);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ActivityNotification.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("First title")
                        .setContentText("first text");

                Intent intent = new Intent(ActivityNotification.this, ActivityNotification.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ActivityNotification.this);
                stackBuilder.addParentStack(ActivityNotification.class);
                stackBuilder.addNextIntent(intent);

                //PendingIntent pIntent = PendingIntent.getActivity(ActivityNotification.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pIntent);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
            }
        });

        mBtnOneSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    String message = "hello man how are you";
                    String[] receipients = new String[]{"a5ec3b08-d18c-4227-bc7e-8580b76eb79b"};
                    JSONObject object = new JSONObject();
                    object.put("contents", new JSONObject().put("en", message));

                    object.put("include_player_ids", new JSONArray(receipients));
                    Log.v(TAG, object.toString());
                    String notification = "{'contents': {'en':'" + message + "'}, 'include_player_ids': ['" + "a5ec3b08-d18c-4227-bc7e-8580b76eb79b" + "']}";
                    Log.v(TAG, new JSONObject(notification).toString());
                    // OneSignal.postNotification(new JSONObject(notification), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /*
                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                    @Override
                    public void idsAvailable(String userId, String registrationId) {
                        Log.d("debug", "User:" + userId);
                        if (registrationId != null)
                            Log.d("debug", "registrationId:" + registrationId);
                    }
                });
                */

            }
        });
    }
}
