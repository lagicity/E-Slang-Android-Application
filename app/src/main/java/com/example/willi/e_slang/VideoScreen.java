package com.example.willi.e_slang;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;

import static com.example.willi.e_slang.Config.YOUTUBE_API_KEY;

/**
 * VideoScreen shows the videos
 */

public class VideoScreen extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_REQUEST = 1;
    String url;
    DbManager dbm;
    Cursor cursor;
    String flag;
    String word;
    Spinner videoUrl;

    ArrayList<String> videoListArray;
    int option;
    YouTubePlayer mYoutubePlayer;
    private YouTubePlayerView youTubeView;
    private MyPlayerStateChangeListener playerStateChangeListener;
    ///////////////////
    private MyPlaybackEventListener playbackEventListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_screen);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        youTubeView.initialize(YOUTUBE_API_KEY, this);
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();

        YouTubePlayerFragment youTubePlayerFragment = YouTubePlayerFragment.newInstance();
        youTubePlayerFragment.initialize(YOUTUBE_API_KEY, this);
    }

    protected void onStart() {
        super.onStart();

        dbm = DbManager.getInstance();
        dbm.mCtx = this;
        dbm.open();

        Intent intent = getIntent();
        flag = intent.getStringExtra("flag");
        word = intent.getStringExtra("word");

        cursor = dbm.getOneWord(flag, word);
        if (cursor.moveToFirst()) {
            do {
                url = (cursor.getString(cursor.getColumnIndex("video_url")));
            } while (cursor.moveToNext());
        }

        Log.d(VideoScreen.class.getName(), "urls =" + url);

        videoUrl = (Spinner) findViewById(R.id.spinner);
        videoListArray = dbm.getAllType(word, 5);
        initialiseVideoUrlSpinner(videoUrl, videoListArray);

        videoUrl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                url = videoListArray.get(position).toString();
                if (url.contains("youtube") && url.contains("=")) {
                    String[] parts = url.split("=");
                    url = parts[1];

                } else if (url.contains("youtu.be")) {
                    String[] parts = url.split("/");
                    url = parts[parts.length - 1];

                }
                //initialise();
                Log.d(VideoScreen.class.getName(), "item selected = " + url);

                if (mYoutubePlayer != null) {
                    mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    mYoutubePlayer.loadVideo(url);
                    mYoutubePlayer.play();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        cursor.close();
        dbm.close();
    }

    private void initialise() {
        youTubeView.initialize(YOUTUBE_API_KEY, this);
        Log.d(VideoScreen.class.getName(), "initialising");
    }

    //initialise type of def spinner
    private void initialiseVideoUrlSpinner(Spinner v, ArrayList videoList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, videoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        v.setAdapter(adapter);
    }

    public void onBackPressed(View v) {
        super.onBackPressed();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        mYoutubePlayer = player;
        if (!wasRestored) {
            //player.cueVideo("B8J8FuNs300"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
            //  if (url.contains("youtube") && url.contains("=")) {
            //  String[] parts=url.split("=");
            player.cueVideo(url);

            // } else if (url.contains("youtu.be")) {
            //  String[] parts=url.split("/");
            // player.cueVideo(url);

            // } else {
            //    Toast.makeText(this, "Youtube URL is invalid", Toast.LENGTH_LONG).show();
            //  }
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(YOUTUBE_API_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            // Called when playback starts, either due to user action or call to play().
            showMessage("Playing");
        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            showMessage("Paused");
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
            // showMessage("Stopped");
        }

        @Override
        public void onBuffering(boolean b) {
            // Called when buffering starts or ends.
        }

        @Override
        public void onSeekTo(int i) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        @Override
        public void onLoaded(String s) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
        }

        @Override
        public void onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        @Override
        public void onVideoStarted() {
            // Called when playback of the video starts.
        }

        @Override
        public void onVideoEnded() {
            // Called when the video reaches its end.
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            // Called when an error occurs.
        }
    }

}
