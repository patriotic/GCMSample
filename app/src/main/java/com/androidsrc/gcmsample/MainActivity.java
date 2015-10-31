package com.androidsrc.gcmsample;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	// Resgistration Id from GCM
	private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
	private SharedPreferences prefs;
	// Your project number and web server url. 
	private static final String GCM_SENDER_ID = "59853228716";
	private static final String WEB_SERVER_URL = "http://testing-purpose.co.nf/gcm_sample/register_user.php";

	GoogleCloudMessaging gcm;
	Button registerBtn;
	Button send_notification;
	TextView regIdView;

	private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
	protected static final int MSG_REGISTER_WITH_GCM = 101;
	protected static final int MSG_REGISTER_WEB_SERVER = 102;
	protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
	protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
	private String gcmRegId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		registerBtn = (Button) findViewById(R.id.register_gcmserver);
		send_notification = (Button) findViewById(R.id.send_notification);
		registerBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Check device for Play Services APK.
				if (isGoogelPlayInstalled()) {
					gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

					// Read saved registration id from shared preferences.
					gcmRegId = getSharedPreferences().getString(PREF_GCM_REG_ID, "");

					if (TextUtils.isEmpty(gcmRegId)) {
						handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
					}else{
						regIdView.setText(gcmRegId);
//						handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER);
						Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
//		send_notification.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Context context =  MainActivity.this;
//
//				if(CheckConnection.isAvailable(context)) {
//					context.startService(new Intent(context, MyService.class));
//				}else {
//					enableBroadcastReceiver();
//				}
//			}
//		});
		
		regIdView = (TextView) findViewById(R.id.regId);
	}

	private void enableBroadcastReceiver() {
		PackageManager pm = getPackageManager();
		ComponentName compName =
				new ComponentName(getApplicationContext(),
						NetworkStateReceiver.class);
		pm.setComponentEnabledSetting(
				compName,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	private boolean isGoogelPlayInstalled() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						ACTION_PLAY_SERVICES_DIALOG).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Google Play Service is not installed",
						Toast.LENGTH_SHORT).show();
				finish();
			}
			return false;
		}
		return true;

	}

	private SharedPreferences getSharedPreferences() {
		if (prefs == null) {
			prefs = getApplicationContext().getSharedPreferences(
					"AndroidSRCDemo", Context.MODE_PRIVATE);
		}
		return prefs;
	}

	public void saveInSharedPref(String result) {
		// TODO Auto-generated method stub
		Editor editor = getSharedPreferences().edit();
		editor.putString(PREF_GCM_REG_ID, result);
		editor.commit();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_WITH_GCM:
				new GCMRegistrationTask().execute();
				break;
			case MSG_REGISTER_WEB_SERVER:
				new WebServerRegistrationTask().execute();
				break;
			case MSG_REGISTER_WEB_SERVER_SUCCESS:
				Toast.makeText(getApplicationContext(),
						"registered with web server", Toast.LENGTH_LONG).show();
				break;
			case MSG_REGISTER_WEB_SERVER_FAILURE:
				Toast.makeText(getApplicationContext(),
						"registration with web server failed",
						Toast.LENGTH_LONG).show();
				break;
			}
		};
	};

	private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (gcm == null && isGoogelPlayInstalled()) {
				gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			}
			try {
				gcmRegId = gcm.register(GCM_SENDER_ID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return gcmRegId;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				Toast.makeText(getApplicationContext(), "registered with GCM",
						Toast.LENGTH_LONG).show();
				regIdView.setText(result);
				saveInSharedPref(result);
				handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER);
			}
		}

	}

	private class WebServerRegistrationTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			URL url = null;
			try {
				url = new URL(WEB_SERVER_URL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
			}
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("regId", gcmRegId);

			StringBuilder postBody = new StringBuilder();
			Iterator iterator = dataMap.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry param = (Entry) iterator.next();
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
					handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_SUCCESS);
				} else {
					throw new IOException("Request failed with error code "
							+ status);
				}
			} catch (ProtocolException pe) {
				pe.printStackTrace();
				handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
			} catch (IOException io) {
				io.printStackTrace();
				handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			return null;
		}
	}

}
