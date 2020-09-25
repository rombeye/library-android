package com.alphaapps.instapickercropper.internal.model;

import com.alphaapps.instapickercropper.internal.entity.Item;
import com.yalantis.ucrop.UCropFragment;

import java.io.Serializable;

/**
 * Created by dali on 11/7/2017.
 */

public class CroppingViewHolder implements Serializable{

    public enum MediaType {Video, Image}
    Item item;
    UCropFragment imageViewLayout;
    String videoURI;
    MediaType type;
    VideoItemVH videoView;


    public CroppingViewHolder() {
    }

    public CroppingViewHolder(UCropFragment imageViewLayout) {
        this.imageViewLayout = imageViewLayout;
        this.type = MediaType.Image;
    }

    public Item getItem() {
        return item;
    }

    public CroppingViewHolder setItem(Item item) {
        this.item = item;
        return this;
    }

    public CroppingViewHolder(VideoItemVH video, String videoURI) {
        this.videoURI = videoURI;
        this.videoView = video;
        this.type = MediaType.Video;
    }

    public UCropFragment getimageViewLayout() {
        return imageViewLayout;
    }

    public CroppingViewHolder setimageViewLayout(UCropFragment imageViewLayout) {
        this.imageViewLayout = imageViewLayout;
        return this;
    }

    public MediaType getType() {
        return type;
    }

    public CroppingViewHolder setType(MediaType type) {
        this.type = type;
        return this;
    }

    public String getVideoURI() {
        return videoURI;
    }

    public CroppingViewHolder setVideoURI(String videoURI) {
        this.videoURI = videoURI;
        return this;
    }

    public VideoItemVH getVideoView() {
        return videoView;
    }

    public CroppingViewHolder setVideoView(VideoItemVH videoView) {
        this.videoView = videoView;
        return this;
    }

//    public CropperView getCropperView(){
//        return imageViewLayout.findViewById(R.id.cropperView);
//    }
}
