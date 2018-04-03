package com.te.teassignment;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Formatter;
import java.util.Locale;

/**
 * Entry activity - Launcher
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {

    // Key
    private static final String CURRENT_POSITION = "current_position";
    private static final String CURRENT_TIME = "current_time";
    private static final String WAS_PLAYING = "was_playing";
    private static final String VIDEO_DURATION = "video_duration";
    private static final String IS_FULLSCREEN = "is_fullscreen";
    private static final String TAG = "toppers_edge";
    private static final String VIDEO_URL = "http://mirrors.standaloneinstaller.com/video-sample/lion-sample.mp4";

    // Video View
    private VideoView videoView;

    // ImageView for play button
    private ImageButton playView;
    // ImageView for fullscreen button
    private ImageButton fullScreenView;

    // SeekBar for Video progress
    private SeekBar videoSeekBar;

    // TextView for running video time
    private TextView runningTimeView;
    // TextView for total time of video
    private TextView totalTimeView;

    // StringBuilder to create time
    private StringBuilder formatBuilder;

    // Formatter to format date
    private Formatter formatter;

    // Progress bar to show video is loading
    private ProgressBar progressBar;

    // Handler to update seek bar and time
    private Handler handler;

    // Flag for full screen
    private Boolean isFullScreen = false;
    // flag for video was playing or not
    private Boolean wasPlaying = false;

    // Value of position where video stopped
    private int stoppedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());

        fullScreenView.setImageResource(R.drawable.ic_fullscreen);

        // Link for some video
        // https://ia802302.us.archive.org/27/items/Pbtestfilemp4videotestmp4/video_test_512kb.mp4
        // http://abhiandroid-8fb4.kxcdn.com/ui/wp-content/uploads/2016/04/videoviewtestingvideo.mp4
        // https://www.youtube.com/watch?v=KApiwGl33so
        // http://mirrors.standaloneinstaller.com/video-sample/P6090053.mp4
        // http://mirrors.standaloneinstaller.com/video-sample/lion-sample.mp4

        // Show progress bar while loading video
        progressBar.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(VIDEO_URL);
        // Set Uri of video to VideoView
        videoView.setVideoURI(uri);
        handler = new Handler();
    }

    /**
     * Init all Views
     */
    private void initViews() {
        videoView = findViewById(R.id.simpleVideoView);
        playView = findViewById(R.id.play_button);
        fullScreenView = findViewById(R.id.fullscreen_button);
        videoSeekBar = findViewById(R.id.video_progress_bar);
        runningTimeView = findViewById(R.id.running_time_view);
        totalTimeView = findViewById(R.id.total_time_view);
        progressBar = findViewById(R.id.loading_video);
    }

    @Override
    protected void onStart() {
        super.onStart();

        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);
        videoView.setOnPreparedListener(this);

        playView.setOnClickListener(this);
        fullScreenView.setOnClickListener(this);
        videoSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playView.setImageResource(R.drawable.ic_play);
        stoppedPosition = videoView.getCurrentPosition();
        wasPlaying = videoView.isPlaying();
        handler.removeCallbacks(updateProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(stoppedPosition);
        videoSeekBar.setProgress(stoppedPosition);
        if (wasPlaying) {
            videoView.start();
            handler.postDelayed(updateProgress, 0);
            playView.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        videoView.setOnCompletionListener(null);
        videoView.setOnErrorListener(null);
        videoView.setOnPreparedListener(null);

        playView.setOnClickListener(null);
        fullScreenView.setOnClickListener(null);
        videoSeekBar.setOnSeekBarChangeListener(null);

        handler.removeCallbacks(updateProgress);
    }

    /**
     * Thread to update video progress
     */
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            // Set Time progress
            int currentTime = videoView.getCurrentPosition();
            String current = getTime(currentTime);
            runningTimeView.setText(current);
            // Set SeekBar Progress
            videoSeekBar.setProgress(currentTime);
            handler.postDelayed(this, 10);
        }
    };

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playView.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateProgress);
                } else {
                    playView.setImageResource(R.drawable.ic_pause);
                    handler.postDelayed(updateProgress, 0);
                    videoView.start();
                }
                break;
            case R.id.fullscreen_button:
                if (!getIsFullScreen()) {
                    setIsFullScreen(true);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setIsFullScreen(false);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Notification that the progress level has changed. Clients can use the fromUser parameter
     * to distinguish user-initiated changes from those that occurred programmatically.
     *
     * @param seekBar  The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range min..max where min
     *                 and max were set by {@link android.widget.ProgressBar#setMin(int)} and
     *                 {@link android.widget.ProgressBar#setMax(int)}, respectively. (The default values for
     *                 min is 0 and max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            videoView.seekTo(progress);
            runningTimeView.setText(getTime(videoView.getCurrentPosition()));
        }
    }

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateProgress);
        videoView.pause();
    }

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.postDelayed(updateProgress, 0);
        videoView.start();
    }

    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        playView.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(updateProgress);
    }

    /**
     * Called to indicate an error.
     *
     * @param mp    the MediaPlayer the error pertains to
     * @param what  the type of error that has occurred:
     * @param extra an extra code, specific to the error.
     * @return True if the method handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_LONG).show();
        return false;
    }

    /**
     * Set Total length of video
     */
    private String getTime(int time) {
        int totalSeconds = time / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * Called when the media file is ready for playback.
     *
     * @param mp the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        progressBar.setVisibility(View.GONE);
        int totalLen = videoView.getDuration();
        String totalTime = getTime(totalLen);
        totalTimeView.setText(totalTime);
        videoSeekBar.setMax(totalLen);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_POSITION, videoView.getCurrentPosition());
        outState.putInt(VIDEO_DURATION, videoView.getDuration());
        outState.putString(CURRENT_TIME, runningTimeView.getText().toString());
        outState.putBoolean(WAS_PLAYING, videoView.isPlaying());
        outState.putBoolean(IS_FULLSCREEN, isFullScreen);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setIsFullScreen(savedInstanceState.getBoolean(IS_FULLSCREEN));
        runningTimeView.setText(savedInstanceState.getString(CURRENT_TIME));
        videoSeekBar.setMax(savedInstanceState.getInt(VIDEO_DURATION));
        stoppedPosition = savedInstanceState.getInt(CURRENT_POSITION);
        videoSeekBar.setProgress(stoppedPosition);
        videoView.seekTo(stoppedPosition);
        wasPlaying = savedInstanceState.getBoolean(WAS_PLAYING);
        if (savedInstanceState.getBoolean(WAS_PLAYING)) {
            playView.setImageResource(R.drawable.ic_pause);
            handler.postDelayed(updateProgress, 0);
            videoView.start();
        } else {
            playView.setImageResource(R.drawable.ic_play);
        }
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            fullScreenView.setImageResource(R.drawable.ic_fullscreen);
        } else {
            fullScreenView.setImageResource(R.drawable.ic_fullscreen_exit);
        }
    }

    /**
     * Set Full Screen Flag
     *
     * @param isFullScreen isFullScreen
     */
    private void setIsFullScreen(Boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
    }

    /**
     * Get full screen flag
     *
     * @return isFullScreen
     */
    private Boolean getIsFullScreen() {
        return isFullScreen;
    }
}
