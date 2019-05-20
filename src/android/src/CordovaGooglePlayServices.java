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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.content.Intent;

/**
 * Simple Cordova plugin for google play services games
 * Contains Highscores, Achievements and Events
 * Based on https://github.com/playgameservices/android-basic-samples/blob/master/TypeANumber/src/main/java/com/google/example/games/tanc/MainActivity.java 
 */
public class CordovaGooglePlayServices extends CordovaPlugin {

    private AchievementsClient achievementsClient;
    private LeaderboardsClient leaderboardsClient;
    private EventsClient eventsClient;
    private PlayersClient playersClient;
    private GoogleSignInClient googleSignInClient;
    private CallbackContext callback;

    private String displayName = "???";

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;  

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // Create the client used to sign in to Google services.
        googleSignInClient = GoogleSignIn.getClient(cordova.getActivity(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
    }

    /**
     * 
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject options;
        if (args.length() > 0) {
          try {
              options = args.getJSONObject(0);
          } catch (JSONException e) {
              callbackContext.error("Error encountered: " + e.getMessage());
              return false;
          }
        }

        if ("login".equals(action)) {
            callback = callbackContext;
            startSignInIntent();
            return true;
        } else if ("submitScore".equals(action)) {

        } else if ("unlockAchievement".equals(action)) {
          
        } else if ("showLeaderboard".equals(action)) {
          
        } else if ("showAchievements".equals(action)) {
          
        } else if ("getDisplayName".equals(action)) {
          PluginResult result = new PluginResult(PluginResult.Status.OK, displayName);           
          callbackContext.sendPluginResult(result);
          return true;
        }

        return false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        silentlySignin();
    }


    private void sendResult(Boolean success, CallbackContext callbackContext) {
        PluginResult result;
        if (success) {
            result = new PluginResult(PluginResult.Status.OK);    
        } else {
            result = new PluginResult(PluginResult.Status.ERROR);
        }
        
        callbackContext.sendPluginResult(result);
    }

    private void startSignInIntent() {
      cordova.setActivityResultCallback(this); 

      cordova.startActivityForResult(this, googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }
  
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
      if (requestCode == RC_SIGN_IN) {
        Task<GoogleSignInAccount> task =
            GoogleSignIn.getSignedInAccountFromIntent(data);
  
        try {
          GoogleSignInAccount account = task.getResult(ApiException.class);
          onConnected(account);
        } catch (ApiException apiException) {
          String message = apiException.getMessage();
          if (message == null || message.isEmpty()) {
            message = "sig-in failed";
          }
  
          onDisconnected();
        }
      }
  
    }

    private void silentlySignin(.0) {
        googleSignInClient.silentSignIn().addOnCompleteListener(cordova.getActivity(),
        new OnCompleteListener<GoogleSignInAccount>() {
          @Override
          public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
            if (task.isSuccessful()) {
              onConnected(task.getResult());
            } else {           
              onDisconnected();
            }
          }
        });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {   
        achievementsClient = Games.getAchievementsClient(cordova.getActivity(), googleSignInAccount);
        leaderboardsClient = Games.getLeaderboardsClient(cordova.getActivity(), googleSignInAccount);
        eventsClient = Games.getEventsClient(cordova.getActivity(), googleSignInAccount);
        playersClient = Games.getPlayersClient(cordova.getActivity(), googleSignInAccount);
    
        // Show sign-out button on main menu
        // mMainMenuFragment.setShowSignInButton(false);
    
        // Show "you are signed in" message on win screen, with no sign in button.
        // mWinFragment.setShowSignInButton(false);
    
        // Set the greeting appropriately on main menu
        playersClient.getCurrentPlayer()
            .addOnCompleteListener(new OnCompleteListener<Player>() {
              @Override
              public void onComplete(@NonNull Task<Player> task) {
                if (task.isSuccessful()) {
                  displayName = task.getResult().getDisplayName();
                } else {
                  displayName = "???";
              }
            }
        });
    
    
        // if we have accomplishments to push, push them
        // if (!mOutbox.isEmpty()) {
        //   pushAccomplishments();
        //   Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
        //       Toast.LENGTH_LONG).show();
        // }
    }

    private void onDisconnected() {    
        achievementsClient = null;
        leaderboardsClient = null;
        playersClient = null;
    
        // // Show sign-in button on main menu
        // mMainMenuFragment.setShowSignInButton(true);
    
        // // Show sign-in button on win screen
        // mWinFragment.setShowSignInButton(true);
    
        // mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
    }
    
}
