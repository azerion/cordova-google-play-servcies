package com.azerion.cordova.plugin.googleplay;

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

    private String displayName;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        connectionCallbackContext = null;

        // Create the client used to sign in to Google services.
        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
    }

    /**
     * 
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    
    }

    private void signin() {
        googleSignInClient.silentSignIn().addOnCompleteListener(this,
        new OnCompleteListener<GoogleSignInAccount>() {
          @Override
          public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
            if (task.isSuccessful()) {
              Log.d(TAG, "signInSilently(): success");
              onConnected(task.getResult());
            } else {
              Log.d(TAG, "signInSilently(): failure", task.getException());
              onDisconnected();
            }
          }
        });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {   
        achievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        leaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        eventsClient = Games.getEventsClient(this, googleSignInAccount);
        playersClient = Games.getPlayersClient(this, googleSignInAccount);
    
        // Show sign-out button on main menu
        // mMainMenuFragment.setShowSignInButton(false);
    
        // Show "you are signed in" message on win screen, with no sign in button.
        // mWinFragment.setShowSignInButton(false);
    
        // Set the greeting appropriately on main menu
        playersClient.getCurrentPlayer()
            .addOnCompleteListener(new OnCompleteListener<Player>() {
              @Override
              public void onComplete(@NonNull Task<Player> task) {
                String displayName;
                if (task.isSuccessful()) {
                  displayName = task.getResult().getDisplayName();
                } else {
                  Exception e = task.getException();
                  handleException(e, getString(R.string.players_exception));
                  displayName = "???";
              }
            }
        });
    
    
        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
          pushAccomplishments();
          Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
              Toast.LENGTH_LONG).show();
        }
    
        loadAndPrintEvents();
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