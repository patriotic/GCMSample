package com.androidsrc.gcmsample;

/**
 * Created by Ikhtiar on 10/31/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(CheckConnection.isAvailable(context)) {
            context.startService(new Intent(context, MyService.class));
        }
    }
}
