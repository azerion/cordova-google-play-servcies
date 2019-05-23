Cordova Plugin Google Play Services
===================================
This is a simple Cordova plugin that allows you to implement google play services on Android. It has a minimalist API and will allow you to handle leaderboards and achievements.

Getting started
---------------
Releases are deployed to Cordova Plugin Registry. You only have to install the desired plugins using Cordova CLI.
```
    cordova plugin add cordova-plugin-google-play-services --variable APP_ID=the_app_id;
```
An additional parameter is required:
* **APP_ID:** The id provided by google play games.

Example
-------
```
// Can pass boolean true to enable debug logging
const social = new Azerion.GooglePlayServices();

// Make sure you can log in
social.login();

//Get the player name when logged in
social.getPlayerName().then((name) => {
    console.log(name);

    // Update UI to show playername
});

// Check if somebody is logged in
social.isSignedIn((signedIn) => {
    console.log(signedIn);
    if (signedIn) {
        // update ui
    }
});

/**
 * Highscores
 */
social.submitScore(leadboardId, score);
social.showLeaderboard();

/**
 * Achievements
 */
social.unlockAchievement();
social.showAchievements();
```

F.A.Q.
------
### But why?
There are some libraries, but they are out-dated, this cordova plugin supports google play services version 17:+

Disclaimer
----------
We at Azerion just love playing and creating awesome games. We aren't affiliated with Cordova. We just needed some awesome google play services. Feel free to use it for creating apps for your own awesome games!
Cordova Plugin Google Play Services is distributed under the MIT license. All 3rd party libraries and components are distributed under their
respective license terms.
