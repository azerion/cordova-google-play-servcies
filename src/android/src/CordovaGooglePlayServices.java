package com.azerion.cordova.plugin;

//Google Play Services for Games
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.content.Intent;

/**
 * Simple Cordova plugin for google play services games Contains Highscores,
 * Achievements and Events Based on
 * https://github.com/playgameservices/android-basic-samples/blob/master/TypeANumber/src/main/java/com/google/example/games/tanc/MainActivity.java
 */
public class CordovaGooglePlayServices extends CordovaPlugin {
    private AchievementsClient achievementsClient;
    private LeaderboardsClient leaderboardsClient;
    private EventsClient eventsClient;
    private PlayersClient playersClient;
    private GoogleSignInClient googleSignInClient;
    private CallbackContext connectionCallbackContext;

    private String displayName = "???";
    private Boolean signedIn = false;

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final String LOG_TAG = "CordovaPluginGooglePlayServices";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        connectionCallbackContext = null;
        // Create the client used to sign in to Google services.
        googleSignInClient = GoogleSignIn.getClient(cordova.getActivity(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
    }

    /**
     * 
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options;
        try {
            options = args.getJSONObject(0);
        } catch (JSONException e) {
            LOG.d(LOG_TAG, "Unable to parse json: " + e.getMessage());
            callbackContext.error("Error encountered: " + e.getMessage());
            return false;
        }

        if ("initialize".equals(action)) {
            connectionCallbackContext = callbackContext;
            return true;
        } else if ("login".equals(action)) {
            startSignInIntent(callbackContext);
            return true;
        } else if ("submitScore".equals(action)) {
            onSubmitScore(options.getString("leaderBoardId"), options.getInt("score"), callbackContext);
            return true;
        } else if ("unlockAchievement".equals(action)) {
            onUnlockAchievement(options.getString("achievementId"), callbackContext);
            return true;
        } else if ("showLeaderboard".equals(action)) {
            onShowLeaderboardsRequested(callbackContext);
            return true;
        } else if ("showAchievements".equals(action)) {
            onShowAchievementsRequested(callbackContext);
            return true;
        } else if ("isSignedIn".equals(action)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, signedIn.toString());
            callbackContext.sendPluginResult(result);
            return true;
        } else if ("getDisplayName".equals(action)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, displayName);
            callbackContext.sendPluginResult(result);
            return true;
        }

        return false;
    }

    @Override
    public void onStart() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(cordova.getActivity());
        if (null != account) {
            LOG.d(LOG_TAG, "Existing acount signed in before, setting up");
            onConnected(account);
        }
    }

    private void sendResult(Boolean success, String message, CallbackContext callbackContext) {
        LOG.d(LOG_TAG, "Sending result to JS: [" + message + "] Success: " + success.toString());
        PluginResult result;
        if (success) {
            result = new PluginResult(PluginResult.Status.OK, message);
        } else {
            result = new PluginResult(PluginResult.Status.ERROR, message);
        }
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    /**
     * Simple sign-in with UI to notify user which account to use
     */
    private void startSignInIntent(CallbackContext callbackContext) {
        LOG.d(LOG_TAG, "Starting Play Services signin intent");
        cordova.setActivityResultCallback(this);
        cordova.startActivityForResult(this, googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                sendResult(true, "GOOGLE_SIGNED_IN", connectionCallbackContext);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "GOOGLE_SIGNIN_FAIL";
                }
                sendResult(false, message, connectionCallbackContext);
                onDisconnected();
            }
        }

    }

    private void silentlySignin() {
        googleSignInClient.silentSignIn().addOnCompleteListener(cordova.getActivity(),
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            onConnected(task.getResult());
                            sendResult(true, "GOOGLE_SILENT_SIGNED_IN", connectionCallbackContext);
                        } else {
                            onDisconnected();
                            signedIn = false;
                            sendResult(true, "GOOGLE_SILENT_SIGNIN_FAILED", connectionCallbackContext);
                        }
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        LOG.d(LOG_TAG, "Connected");

        achievementsClient = Games.getAchievementsClient(cordova.getActivity(), googleSignInAccount);
        leaderboardsClient = Games.getLeaderboardsClient(cordova.getActivity(), googleSignInAccount);
        playersClient = Games.getPlayersClient(cordova.getActivity(), googleSignInAccount);

        signedIn = true;

        playersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
            @Override
            public void onComplete(@NonNull Task<Player> task) {
                if (task.isSuccessful()) {
                    displayName = task.getResult().getDisplayName();
                    if (null != connectionCallbackContext) {
                        sendResult(true, "GOOGLE_PLAYERNAME_RECEIVED", connectionCallbackContext);
                    }
                } else {
                    displayName = "???";
                }
            }
        });
    }

    private void onDisconnected() {
        LOG.d(LOG_TAG, "Disconnected");
        achievementsClient = null;
        leaderboardsClient = null;
        playersClient = null;
    }

    public void onShowAchievementsRequested(CallbackContext callbackContext) {
        if (!signedIn) {
            LOG.d(LOG_TAG, "Opened Achievements when not signed in, signing in instead.");
            startSignInIntent(callbackContext);
            return;
        }

        achievementsClient.getAchievementsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                cordova.getActivity().startActivityForResult(intent, RC_UNUSED);
                sendResult(true, "Showing Achievements", callbackContext);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sendResult(false, "Error Showing Achievements", callbackContext);
            }
        });
    }

    public void onShowLeaderboardsRequested(CallbackContext callbackContext) {
        if (!signedIn) {
            LOG.d(LOG_TAG, "Opened leaderboard when not signed in, signing in instead.");
            startSignInIntent(callbackContext);
            return;
        }

        leaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                cordova.getActivity().startActivityForResult(intent, RC_LEADERBOARD_UI);
                sendResult(true, "Showing All Leaderboards", callbackContext);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sendResult(false, "Error Showing All Leaderboards", callbackContext);
            }
        });
    }

    public void onSubmitScore(String scoreBoard, Integer score, CallbackContext callbackContext) {
        if (!signedIn) {
            LOG.d(LOG_TAG, "Submitting Score failed: " + score.toString() + " to: " + scoreBoard);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Score not submitted, player not signed in");
            callbackContext.sendPluginResult(result);
            return;
        }

        LOG.d(LOG_TAG, "Submitting Score: " + score.toString() + " to: " + scoreBoard);
        PluginResult result = new PluginResult(PluginResult.Status.OK, "Score submitted");
        callbackContext.sendPluginResult(result);
        leaderboardsClient.submitScore(scoreBoard, score);
    }

    public void onUnlockAchievement(String achievementId, CallbackContext callbackContext) {
        if (!signedIn) {
            LOG.d(LOG_TAG, "Unlocking achievement failed: " + achievementId);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Achievement not submitted, player not signed in");
            callbackContext.sendPluginResult(result);
            return;
        }
        LOG.d(LOG_TAG, "Unlocking achievement: " + achievementId);
        PluginResult result = new PluginResult(PluginResult.Status.OK, "Achievement submitted");
        callbackContext.sendPluginResult(result);
        achievementsClient.unlock(achievementId);
    }
}
