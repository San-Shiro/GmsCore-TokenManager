package org.microg.gms.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TokenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GmsCoreMod", "Universal Broadcast Received");

        // Extract arguments from ADB command
        String email = intent.getStringExtra("email");
        String pkg = intent.getStringExtra("package");
        String sig = intent.getStringExtra("signature");
        String scope = intent.getStringExtra("scope");

        // Pass everything to the spoofer
        TokenSpoofer.fetchToken(context, email, pkg, sig, scope);
    }
}