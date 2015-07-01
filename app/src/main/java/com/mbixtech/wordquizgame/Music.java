package com.mbixtech.wordquizgame;

import android.content.Context;
import android.media.MediaPlayer;

public class Music {
    private static MediaPlayer mPlayer = null;

    public static void play(Context context, int resId) {
        stop();
        mPlayer = MediaPlayer.create(context, resId);
        mPlayer.setVolume(0.3f, 0.3f);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public static void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
