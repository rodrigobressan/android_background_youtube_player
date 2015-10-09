package com.example.rodrigobresan.awesomeplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rodrigobresan.awesomeplayer.R;
import com.example.rodrigobresan.awesomeplayer.callback.VideoControlCallback;

/**
 * Created by Rodrigo Bresan on 08/10/2015.
 */
public class LyricsFragment extends Fragment {

    private boolean isVideoPlaying;

    private VideoControlCallback mCallback;


    /*
     * When the fragment is attached we must also assign our callback to play/pause the video
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (VideoControlCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ButtonClicked");
        }
    }

    public static LyricsFragment newInstance(int index) {
        LyricsFragment fragment = new LyricsFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics, container, false);

        setUiComponents(view);

        isVideoPlaying = true;

        return view;
    }

    private void setUiComponents(View view) {

        final Button btnVideoControl = (Button) view.findViewById(R.id.btnVideoControl);
        btnVideoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoPlaying) {
                    btnVideoControl.setText(R.string.text_continue_video);
                    mCallback.pauseVideo();
                    isVideoPlaying = false;
                } else {
                    btnVideoControl.setText(R.string.text_pause_video);
                    mCallback.continueVideo();
                    isVideoPlaying = true;
                }
            }
        });
    }
}
