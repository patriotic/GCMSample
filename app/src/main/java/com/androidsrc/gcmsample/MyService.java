package com.androidsrc.gcmsample;

/**
 * Created by Ikhtiar on 10/31/2015.
 */
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyService extends Service {

    protected static final int MSG_SEND_NOTIFICATION_SUCCESS = 106;
    protected static final int MSG_SEND_NOTIFICATION_FAILURE = 107;
    protected static final int MSG_SEND_NOTIFICATION_REG_ID_ERROR = 108;
    private static final String SERVER_URL_SENDING_NOTIFICATION="http://testing-purpose.co.nf/gcm_sample/device_server_device.php";
    private String gcmRegId;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        gcmRegId =  getSharedPreferences().getString("PREF_GCM_REG_ID", "");

        if (TextUtils.isEmpty(gcmRegId)) {
            handler.sendEmptyMessage(MSG_SEND_NOTIFICATION_REG_ID_ERROR);
        }else{
            new SendNotificationTask().execute();
           }
        return START_NOT_STICKY;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SEND_NOTIFICATION_SUCCESS:
                    disableBroadcastReceiver(getApplicationContext());
                    Toast.makeText(getApplicationContext(),
                            "Notification Sent", Toast.LENGTH_LONG).show();
                    break;
                case MSG_SEND_NOTIFICATION_FAILURE:
                    Toast.makeText(getApplicationContext(),
                            "Notification Not Sent",
                            Toast.LENGTH_LONG).show();
                    break;
                case MSG_SEND_NOTIFICATION_REG_ID_ERROR:
                    Toast.makeText(getApplicationContext(),
                            "Register with gcm first",
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    stopSelf();

            }
        };
    };

    private class SendNotificationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            URL url = null;
            String NotificationMessage = "SAVE ME!!!!";

            try {
                url = new URL(SERVER_URL_SENDING_NOTIFICATION);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(MSG_SEND_NOTIFICATION_FAILURE);
            }
            Map<String, String> dataMap = new HashMap<String, String>();
            dataMap.put("regId", gcmRegId);
            dataMap.put("notificationMessage",NotificationMessage);

            StringBuilder postBody = new StringBuilder();
            Iterator iterator = dataMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry param = (Map.Entry) iterator.next();
                postBody.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            byte[] bytes = body.getBytes();

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");

                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();

                int status = conn.getResponseCode();
                if (status == 200) {
                    // Request success
                    handler.sendEmptyMessage(MSG_SEND_NOTIFICATION_SUCCESS);
                } else {
                    throw new IOException("Request failed with error code "
                            + status);
                }
            } catch (ProtocolException pe) {
                pe.printStackTrace();
                handler.sendEmptyMessage(MSG_SEND_NOTIFICATION_FAILURE);
            } catch (IOException io) {
                io.printStackTrace();
                handler.sendEmptyMessage(MSG_SEND_NOTIFICATION_FAILURE);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

        public void disableBroadcastReceiver(Context context) {
            PackageManager pm = context.getPackageManager();
            ComponentName compName =
                    new ComponentName(context.getApplicationContext(),
                            NetworkStateReceiver.class);
            pm.setComponentEnabledSetting(
                    compName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        private SharedPreferences getSharedPreferences() {
        if (prefs == null) {
            prefs = getApplicationContext().getSharedPreferences(
                    "AndroidSRCDemo", Context.MODE_PRIVATE);
        }
        return prefs;
    }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        }
    }