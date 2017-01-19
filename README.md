# VideoSurfaceView
Android view to play videos from raw resources included in the project.

How to use:

```
<package.VideoSurfaceView
        android:id="@+id/video_surface"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_centerInParent="true"
        app:videoRawResource="@raw/video_file"
        app:videoScaleType="stretch"/>
```

* videoRawResource: the video you copied to your res/raw/
* videoScaleType: some modes to scale the video (stretch, center, fitHorizontally, fitVertically, fitAll)
