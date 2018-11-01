package ie.moses.youtubeplaybackeventlistener;

import android.support.annotation.CallSuper;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;

@SuppressWarnings("WeakerAccess")
public abstract class YouTubePlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

    private static final String TAG = YouTubePlaybackEventListener.class.getSimpleName();

    private final YouTubePlayer _youTubePlayer;
    private volatile int _lastKnownPositionMillis;

    public YouTubePlaybackEventListener(YouTubePlayer youTubePlayer) {
        _youTubePlayer = youTubePlayer;
    }

    @CallSuper
    @Override
    public void onPlaying() {
        new Thread() {

            private int _retryCnt;

            @Override
            public void run() {
                try {
                    while (_youTubePlayer.isPlaying()) {
                        _lastKnownPositionMillis = _youTubePlayer.getCurrentTimeMillis();
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "playback position thread interrupted", e);
                    if (_retryCnt < 3) {
                        Log.i(TAG, "retrying...");
                        run();
                        _retryCnt++;
                    } else {
                        Log.w(TAG, "already retried 3 times");
                        _retryCnt = 0;
                    }
                }
            }
        }.start();
    }

    @Override
    public final void onSeekTo(int newPositionMillis) {
        onSeekTo(_lastKnownPositionMillis, newPositionMillis);
    }

    public abstract void onSeekTo(int oldPositionMillis, int newPositionMillis);

}
