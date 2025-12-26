package org.microg.gms.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TokenSpoofer {

    private static final String TAG = "GmsCoreMod";

    // Defaults (Google Photos) - used if arguments are missing
    private static final String DEF_PKG = "com.google.android.apps.photos";
    private static final String DEF_SIG = "24bb24c05e47e0aefa68a58a766179d9b613a600";
    private static final String DEF_SCOPE = "oauth2:https://www.googleapis.com/auth/photos.native";

    public static void fetchToken(Context context, String email, String pkgName, String signature, String scope) {
        new Thread(() -> {
            try {
                // 1. Validation & Defaults
                final String finalPkg = (pkgName != null) ? pkgName : DEF_PKG;
                final String finalSig = (signature != null) ? signature : DEF_SIG;
                final String finalScope = (scope != null) ? scope : DEF_SCOPE;

                Log.d(TAG, "Fetching token for: " + finalPkg);

                // 2. Find Account
                AccountManager am = AccountManager.get(context);
                Account[] accounts = am.getAccountsByType("com.google");
                Account selectedAccount = null;

                if (email != null && !email.isEmpty()) {
                    for (Account acc : accounts) {
                        if (acc.name.equalsIgnoreCase(email)) {
                            selectedAccount = acc;
                            break;
                        }
                    }
                }
                if (selectedAccount == null && accounts.length > 0) selectedAccount = accounts[0];

                if (selectedAccount == null) {
                    notifyUser(context, "Error: No Google Account found!");
                    return;
                }

                // 3. Universal Spoof Request
                AuthRequest request = new AuthRequest()
                        .email(selectedAccount.name)
                        .token(selectedAccount.name)
                        .service(finalScope)
                        .app(finalPkg, finalSig)      // <--- DYNAMIC
                        .caller(finalPkg, finalSig);  // <--- DYNAMIC

                AuthResponse response = request.getResponse();

                if (response.auth != null) {
                    // 4. Save to JSON (Include metadata so you know what this token is for)
                    String jsonOutput = String.format(
                            "{\"email\": \"%s\", \"package\": \"%s\", \"scope\": \"%s\", \"token\": \"%s\", \"androidId\": \"%s\"}",
                            selectedAccount.name, finalPkg, finalScope, response.auth, response.androidId
                    );

                    // Save to a generic file, or use pkgName in filename to avoid overwrites
                    saveToFile(jsonOutput, "token_" + finalPkg + ".json");
                    notifyUser(context, "Token Generated for " + finalPkg);
                    Log.d(TAG, "SUCCESS: " + response.auth);
                } else {
                    notifyUser(context, "Auth Failed: " + response.issue);
                    Log.e(TAG, "Auth Failed: " + response.issue);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void saveToFile(String data, String filename) {
        try {
            File file = new File("/sdcard/" + filename);
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void notifyUser(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}