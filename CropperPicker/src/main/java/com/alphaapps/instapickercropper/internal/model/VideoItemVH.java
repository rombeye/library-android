package com.alphaapps.instapickercropper.internal.model;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.utils.PathUtils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VideoItemVH implements Serializable {
    private final int mFileHeight, mFileWidth;
    public Uri mediaUri;

    boolean clickedVideo = false;
    public View mView;

    String urimp4;
    String newPath;//= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Video/new.mp4";
    String originalPath;

    public VideoItemVH(View view, Uri mediaUri) {
        mView = view;
        mView.setClipToOutline(true);
        SimpleExoPlayer player;
        SimpleExoPlayerView simpleExoPlayerView;
        final ExtractorMediaSource videoSource;

        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl

        // 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(view.getContext(), trackSelector);
        simpleExoPlayerView = view.findViewById(R.id.player);

        //Set media controller
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.requestFocus();

        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);

        // I. ADJUST HERE:
        //CHOOSE CONTENT: LiveStream / SdCard


        //VIDEO FROM SD CARD: (2 steps. set up file and path, then change videoSource to get the file)
        urimp4 = PathUtils.getPath(mView.getContext(), mediaUri);//mediaUri.toString();//"/Video/111.mp4"; //upload file to device and add path/name.mp4
        Uri mp4VideoUri = Uri.parse(urimp4);

        originalPath = PathUtils.getPath(mView.getContext(), mp4VideoUri);

//        Random randomNumber = new Random();
//        newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Video/" + System.currentTimeMillis() + "" + randomNumber.nextInt() + ".mp4";


        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(urimp4);
//        metaRetriever.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + urimp4);

        Bitmap bitmap = metaRetriever.getFrameAtTime();

//        displayCustomToast("Video resolution is "+ videoHeight +"/"+ videoWidth, CropperPickerActivity.ToastType.WARNING);
        mFileHeight = bitmap.getHeight();
        mFileWidth = bitmap.getWidth();
        bitmap.recycle();


        //Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        //Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), "exoplayer2example"), bandwidthMeterA);
        //Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        //FOR SD CARD SOURCE:
        videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);


        // Prepare the player with the source.
        player.prepare(videoSource);
//        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setPlayWhenReady(true); //run file/link when ready to play.
    }

    public void play() {
        ((SimpleExoPlayerView) mView).getPlayer().setPlayWhenReady(true);
    }

    public void stop() {
        ((SimpleExoPlayerView) mView).getPlayer().setPlayWhenReady(false);
    }

    public SimpleExoPlayer getSimpleExoPlayerView() {
        return ((SimpleExoPlayerView) mView.findViewById(R.id.player)).getPlayer();
    }

    public void toggleVideoState() {
        if (!clickedVideo)
            play();
        else
            stop();
        clickedVideo = true;
    }

    public String getOutputVideoPath() {
        return newPath;
    }

    public MediaItem getMediaItem() {
        MediaItem mediaItem = new MediaItem();
        mediaItem.setFromCamera(false);
        mediaItem.setUri(urimp4);
        mediaItem.setMuted(false);
        mediaItem.setImage(false);

        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        tempDir.mkdir();
        File tempFile ;
        Random randomNumber = new Random();
        String fileName = System.currentTimeMillis() + "" + randomNumber.nextInt();
        try {
            tempFile = File.createTempFile(fileName, ".mp4", tempDir);
            newPath = tempFile.getAbsolutePath();
            List<String> cropCommand ;
            if (mFileWidth > mFileHeight) {
                int widhtToHeightDef = mFileWidth - mFileHeight;
                cropCommand = getFullFfpmegCommand(urimp4, newPath, widhtToHeightDef / 2, 0, mFileHeight, mFileHeight, 0);
            } else {
                int widhtToHeightDef = mFileHeight - mFileWidth;
                cropCommand = getFullFfpmegCommand(urimp4, newPath, 0, widhtToHeightDef / 2, mFileWidth, mFileWidth, 0);
            }
            mediaItem.setVideoCropCommand(cropCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaItem;
    }

    private List<String> getFullFfpmegCommand(String originalVideoPath, String croppedVideoPath
            , int xCoordinate, int yCoordinate, int imageWidth, int imageHeight, int angle) {
        List<String> command = new ArrayList<>();
        command.add("-y");
        command.add("-i");
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        String rotate = "";
        switch (angle) {
            case 0:
                x = xCoordinate;
                y = yCoordinate;
                w = imageWidth;
                h = imageHeight;
                break;
            case 90:
                x = yCoordinate;
                y = mFileWidth - imageWidth - xCoordinate;
                w = imageHeight;
                h = imageWidth;
                rotate = ",transpose=1";
                break;
            case 180:
                x = mFileHeight - imageWidth - xCoordinate;
                y = mFileWidth - imageHeight - yCoordinate;
                w = imageWidth;
                h = imageHeight;
                rotate = ",vflip";
                break;
            case -90:
                x = mFileHeight - imageHeight - yCoordinate;
                y = xCoordinate;
                w = imageHeight;
                h = imageWidth;
                rotate = ",transpose=2";
                break;
        }

        command.add(originalVideoPath);
        command.add("-s");
        command.add("480x480");

        command.add("-vf");
//        ffmpegCommand.add("scale=");
//        ffmpegCommand.add(imageWidth*compression/100);
//        ffmpegCommand.add(":");
//        ffmpegCommand.add(imageHeight*compression/100);
        //ffmpegCommand.add("-filter:v");
//        ffmpegCommand.add(" ");
        command.add("crop=" + w + ":" + h + ":" + x + ":" + y);

        command.add("-preset");
        command.add("ultrafast");
        command.add("-strict");
        command.add("-2");
        command.add("-c:v");
        command.add("libx264");
        command.add("-c:a");
        command.add("copy");

        if (!rotate.isEmpty()) {
            command.add(rotate);
            //ffmpegCommand.add(" -c:a copy");
        }
        command.add(croppedVideoPath);
        return command;
    }

    public void startCropping(ExecuteBinaryResponseHandler executeBinaryResponseHandler) {


        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        tempDir.mkdir();
        File tempFile = null;
        Random randomNumber = new Random();
        String fileName = System.currentTimeMillis() + "" + randomNumber.nextInt();
        try {
            tempFile = File.createTempFile(fileName, ".mp4", tempDir);
            newPath = tempFile.getAbsolutePath();

            if (mFileWidth > mFileHeight) {
                int widhtToHeightDef = mFileWidth - mFileHeight;
                cropVideo(executeBinaryResponseHandler, urimp4, newPath, widhtToHeightDef / 2, 0, mFileHeight, mFileHeight, 0);
            } else {
                int widhtToHeightDef = mFileHeight - mFileWidth;
                cropVideo(executeBinaryResponseHandler, urimp4, newPath, 0, widhtToHeightDef / 2, mFileWidth, mFileWidth, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    List<String> ffmpegCommand;

    private void cropVideo(ExecuteBinaryResponseHandler executeBinaryResponseHandler, String originalVideoPath, String croppedVideoPath, int xCoordinate,
                           int yCoordinate, int imageWidth, int imageHeight, int croppedAngle) {
        String command;
        ffmpegCommand = new ArrayList<>();
        ffmpegCommand.add("-y");
        ffmpegCommand.add("-i");
        getFFmpegCommand(ffmpegCommand, imageWidth, imageHeight,
                xCoordinate, yCoordinate, originalVideoPath, croppedVideoPath, croppedAngle);
        command = ffmpegCommand.toString();

        Log.i("sss", command);
        String[] cmd = new String[ffmpegCommand.size()];
        ffmpegCommand.toArray(cmd);

        cropVideoTask(cmd, executeBinaryResponseHandler);

    }

    private void getFFmpegCommand(List<String> ffmpegCommand, int imageWidth,
                                  int imageHeight, int xCoordinate, int yCoordinate,
                                  String originalVideoPath, String croppedVideoPath, int angle) {
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        String rotate = "";
        switch (angle) {
            case 0:
                x = xCoordinate;
                y = yCoordinate;
                w = imageWidth;
                h = imageHeight;
                break;
            case 90:
                x = yCoordinate;
                y = mFileWidth - imageWidth - xCoordinate;
                w = imageHeight;
                h = imageWidth;
                rotate = ",transpose=1";
                break;
            case 180:
                x = mFileHeight - imageWidth - xCoordinate;
                y = mFileWidth - imageHeight - yCoordinate;
                w = imageWidth;
                h = imageHeight;
                rotate = ",vflip";
                break;
            case -90:
                x = mFileHeight - imageHeight - yCoordinate;
                y = xCoordinate;
                w = imageHeight;
                h = imageWidth;
                rotate = ",transpose=2";
                break;
        }

        ffmpegCommand.add(originalVideoPath);
        ffmpegCommand.add("-s");
        ffmpegCommand.add("480x480");

        ffmpegCommand.add("-vf");
//        ffmpegCommand.add("scale=");
//        ffmpegCommand.add(imageWidth*compression/100);
//        ffmpegCommand.add(":");
//        ffmpegCommand.add(imageHeight*compression/100);
        //ffmpegCommand.add("-filter:v");
//        ffmpegCommand.add(" ");
        ffmpegCommand.add("crop=" + w + ":" + h + ":" + x + ":" + y);

        ffmpegCommand.add("-preset");
        ffmpegCommand.add("ultrafast");
        ffmpegCommand.add("-strict");
        ffmpegCommand.add("-2");
        ffmpegCommand.add("-c:v");
        ffmpegCommand.add("libx264");
        ffmpegCommand.add("-c:a");
        ffmpegCommand.add("copy");

        if (!rotate.isEmpty()) {
            ffmpegCommand.add(rotate);
            //ffmpegCommand.add(" -c:a copy");
        }
        ffmpegCommand.add(croppedVideoPath);

    }

    private void cropVideoTask(String[] cmd, ExecuteBinaryResponseHandler executeBinaryResponseHandler) {
        try {
            FFmpeg ffmpeg;

            //prepare cropper
            ffmpeg = FFmpeg.getInstance(mView.getContext());
            try {
                //Load the binary
                ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure() {
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            } catch (FFmpegNotSupportedException e) {
                // Handle if FFmpeg is not supported by device
                e.printStackTrace();
            }


            ffmpeg.execute(cmd, executeBinaryResponseHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.i("aaa", "doInBackground: Exception + Device is not supported");
        }
    }
}