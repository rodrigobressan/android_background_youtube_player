package com.example.rodrigobresan.awesomeplayer.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.rodrigobresan.awesomeplayer.R;
import com.example.rodrigobresan.awesomeplayer.callback.TestMediaDrmCallback;
import com.example.rodrigobresan.awesomeplayer.player.DashRendererBuilder;
import com.example.rodrigobresan.awesomeplayer.player.DemoPlayer;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;

/**
 * Created by Rodrigo Bresan on 07/10/2015.
 */
public class VideoFragment extends Fragment implements SurfaceHolder.Callback, DemoPlayer.Listener, AudioCapabilitiesReceiver.Listener {

    private MediaController mediaController;
    private SurfaceView surfaceView;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private boolean enableBackgroundAudio;

    private Uri contentUri;
    private String contentId;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    public static VideoFragment newInstance(int index) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        // set a few data that will be used to display the video
        this.contentId = "Video";

        String videoUrl = getResources().getString(R.string.video_dash_url);

        contentUri = Uri.parse(videoUrl);

        // configure the Ui
        setVideoUi(view);

        // configure the audio in background
        setupAudioCapabilities();

        return view;
    }

    /*
     * This method is used to set all the UI components, such as the FrameLayout which contains
     * the SurfaceView, and the action of each item inside the view
     */
    private void setVideoUi(View view) {
        // find the video frame and set the actions for it (onTouchListener)
        View frameVideo = view.findViewById(R.id.frameVideo);
        frameVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleMediaControllerVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });

        frameVideo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceViewVideo);
        surfaceView.getHolder().addCallback(this);

        mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(frameVideo);
    }

    /*
     * This method is used to start our AudioCapabilitiesReceiver, which is responsible for
     * playing any media's audio in background
     */
    private void setupAudioCapabilities() {
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getActivity().getApplicationContext(), this);
        audioCapabilitiesReceiver.register();

        // set our media will have its audio playing on background
        enableBackgroundAudio = true;
    }

    /*
     * When we get back into our application, we must check if our player already exists, otherwise
     * we must prepare it
     */
    @Override
    public void onResume() {
        super.onResume();
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
    }

    /*
     * When we pause our app, we must check if we should keep playing the audio on background
     * or not
     */
    @Override
    public void onPause() {
        super.onPause();
        if (!enableBackgroundAudio) {
            releasePlayer();
        } else {
            player.setBackgrounded(true);
        }
    }

    /*
     * When we destroy the application, we must remove the audio in background (if there's any)
     * and finally release the player.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    /*
     * This listener is used to check when the audio capabilities had any change on it
     * Also does prepare the video for playing and set if the audio will be playing
     * on background or not
     */
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }

    /*
     * This method is used to render the media
     * We are using a Dash format type, so we call our DashRendererBuilder to render
     * our media and when its finished, our WideVideTestMediaDrmCallback is called
     */
    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(getActivity().getApplicationContext(), "BresanPlayer");
        return new DashRendererBuilder(getActivity().getApplicationContext(), userAgent, contentUri.toString(),
                new TestMediaDrmCallback(contentId));
    }

    /*
     * Prepare the player for being played
     * Add its listeners and set its media player
     */
    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    /*
     * This method is used when the player is no longer needed
     */
    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    /*
     * Check when the video is ended and then displays the media controller
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            mediaController.show(0);
        }
    }

    /*
     * If any error occurs, we check if it was because of DRM (Digital Rights Management)
     * failure, in this case whe should warn the user about the error
     */
    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // special cases for DRM failures
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId;
            if (Util.SDK_INT < 18) {  // in case the SDK is smaller than 18
                stringId = R.string.drm_error_not_supported;
            } else if (unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME) {
                stringId = R.string.drm_error_unsupported_scheme;
            } else {
                stringId = R.string.drm_error_unknown;
            }

            Toast.makeText(getActivity().getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        mediaController.show(0);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        // could create an aspect ration video here
    }

    private void toggleMediaControllerVisibility()  {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            mediaController.show(0);
        }
    }

    // pause the video execution
    public void pauseVideo() {
        player.setPlayWhenReady(false);
    }

    // continue the video execution from where it was paused
    public void continueVideo() {
        player.setPlayWhenReady(true);
    }

    // Callbacks for surface view implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }
}

