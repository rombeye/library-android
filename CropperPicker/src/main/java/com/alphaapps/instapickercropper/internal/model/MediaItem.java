package com.alphaapps.instapickercropper.internal.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by dali on 3/8/2018.
 */

public class MediaItem  {

    public static ArrayList<MediaItem> fromJsonArray(JSONArray jsonArray) {
        Type listType = new TypeToken<ArrayList<MediaItem>>() {
        }.getType();
        ArrayList<MediaItem> MediaItems = new Gson().fromJson(jsonArray.toString(), listType);
        return MediaItems;
    }

    public static ArrayList<MediaItem> fromJsonArray(String jsonArray) {
        Type listType = new TypeToken<ArrayList<MediaItem>>() {
        }.getType();
        ArrayList<MediaItem> MediaItems = new Gson().fromJson(jsonArray.toString(), listType);
        return MediaItems;
    }

    public static MediaItem fromJson(String json) {
        return new Gson().fromJson(json, MediaItem.class);
    }


    public static String toJsonArray(ArrayList<MediaItem> MediaItemsArrayList) {
        return new Gson().toJson(MediaItemsArrayList);
    }


    String uri;
    boolean isImage;
    boolean isMuted;
    boolean fromCamera = false;
    List<String> videoCropCommand;
    String croppedVideoUri;

    public MediaItem() {
    }

    public MediaItem(String uri, boolean isImage, boolean isMuted) {
        this.uri = uri;
        this.isImage = isImage;
        this.isMuted = isMuted;
    }

    public MediaItem(String uri, boolean isImage, boolean isMuted, boolean fromCamera) {
        this.uri = uri;
        this.isImage = isImage;
        this.isMuted = isMuted;
        this.fromCamera = fromCamera;
    }

    public String getUri() {
        return uri;
    }

    public MediaItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public boolean isImage() {
        return isImage;
    }

    public MediaItem setImage(boolean image) {
        isImage = image;
        return this;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public MediaItem setMuted(boolean muted) {
        isMuted = muted;
        return this;
    }

    public void setFromCamera(boolean fromCamera) {
        this.fromCamera = fromCamera;
    }

    public boolean isFromCamera() {
        return fromCamera;
    }

    public List<String> getVideoCropCommand() {
        return videoCropCommand;
    }

    public void setVideoCropCommand(List<String> videoCropCommand) {
        this.videoCropCommand = videoCropCommand;
        this.croppedVideoUri = videoCropCommand.get(videoCropCommand.size() - 1);
    }

    public String getCroppedVideoUri() {
        return croppedVideoUri;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "uri='" + uri + '\'' +
                ", isImage=" + isImage +
                ", isMuted=" + isMuted +
                ", fromCamera=" + fromCamera +
                ", videoCropCommand=" + videoCropCommand +
                '}';
    }
}
