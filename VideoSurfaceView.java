package com.zeonsolutions.b4app.webapi;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zeonsolutions.b4app.R;

/**
 * Created by Kastr
 * https://github.com/Kastr/VideoSurfaceView
 */

public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final int MODE_CENTER = 1;
    private static final int MODE_FIT_HORIZONTAL = 2;
    private static final int MODE_FIT_VERTICAL = 3;
    private static final int MODE_FIT_ALL = 4;

    private MediaPlayer mp;
    private int mVideoWidth;
    private int mVideoHeight;
    private @RawRes
    int videoResource;
    private int videoScaleType;

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupAttrs(context, attrs, defStyleAttr);
        init();
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAttrs(context, attrs, 0);
        init();
    }

    private void setupAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.VideoSurfaceView,
                defStyle, 0
        );

        try {
            videoResource = a.getResourceId(R.styleable.VideoSurfaceView_videoRawResource, 0);
            videoScaleType = a.getInteger(R.styleable.VideoSurfaceView_videoScaleType, 0);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        mp = new MediaPlayer();
        getHolder().addCallback(this);
    }

    synchronized public void releaseMediaPlayer() {
        if(mp == null) {
            return;
        }
        mp.release();
        mp = null;
    }

    private void startMediaPlayer(SurfaceHolder holder) {

        if(mp == null) {
            mp = new MediaPlayer();
        }

        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(videoResource);
            mp.setDisplay(holder);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mp.setLooping(true);
            mp.setOnPreparedListener(this);
            mp.setOnErrorListener(this);
            mp.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* SURFACE
     * ------------------------------------------------------------------------------------------------*/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startMediaPlayer(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseMediaPlayer();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureVideo(widthMeasureSpec, heightMeasureSpec);
    }


    /* MEDIA PLAYER
     * ------------------------------------------------------------------------------------------------*/
    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        this.mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(VideoSurfaceView.class.getSimpleName(), "Error occurred with MediaPlayer.\n(int)What: " + String.valueOf(what));
        return false;
    }


    /* Utils
     * ------------------------------------------------------------------------------------------------*/
    private void measureVideo(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        if (mVideoWidth == 0 && mVideoHeight == 0) {
            setMeasuredDimension(width, height);
            return;
        }

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (videoScaleType) {
            case MODE_CENTER:
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
                break;

            case MODE_FIT_HORIZONTAL:
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
                break;

            case MODE_FIT_VERTICAL:
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
                break;

            case MODE_FIT_ALL:
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //image too wide, correcting
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //image too tall, correcting
                    height = width * mVideoHeight / mVideoWidth;
                }
                break;
        }
        setMeasuredDimension(width, height);
    }
}
