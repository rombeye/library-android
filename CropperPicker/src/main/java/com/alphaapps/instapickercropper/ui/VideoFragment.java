/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alphaapps.instapickercropper.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.model.VideoItemVH;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;


public class VideoFragment extends Fragment {

    SimpleExoPlayerView player;

    public static String VIDEO_URI = "VIDEO_URI";
    VideoItemVH videoItemVH;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exoplayer_view, container, false);

        player = (SimpleExoPlayerView) view.findViewById(R.id.player);

        String selectedUri = getArguments().getString(VIDEO_URI);
        videoItemVH = new VideoItemVH(view, Uri.parse(selectedUri));
        videoItemVH.play();

        return view;
    }

    public void startVideo() {
        if (videoItemVH != null)
            videoItemVH.play();
    }

    public void stopVideo() {
        ((SimpleExoPlayerView) videoItemVH.mView).getPlayer().setPlayWhenReady(false);
    }

    @Override
    public void onDestroy() {
        releaseVideo();
        super.onDestroy();
    }

    public void releaseVideo() {
        ((SimpleExoPlayerView) videoItemVH.mView).getPlayer().release();

    }

    public static VideoFragment newInstance(String uri) {
        Bundle args = new Bundle();
        VideoFragment fragment = new VideoFragment();
        args.putString(VIDEO_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    public void setVideoLayoutParams(FrameLayout.LayoutParams params) {
        player.setLayoutParams(params);
        player.invalidate();
    }

}