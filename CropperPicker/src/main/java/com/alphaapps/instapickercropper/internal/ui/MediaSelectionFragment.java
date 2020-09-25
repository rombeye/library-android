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
package com.alphaapps.instapickercropper.internal.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alphaapps.instapickercropper.MessageEvent;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Album;
import com.alphaapps.instapickercropper.internal.entity.Item;
import com.alphaapps.instapickercropper.internal.entity.SelectionSpec;
import com.alphaapps.instapickercropper.internal.model.AlbumMediaCollection;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumMediaAdapter;
import com.alphaapps.instapickercropper.internal.ui.widget.MediaGridInset;
import com.alphaapps.instapickercropper.internal.utils.UIUtils;
import com.alphaapps.instapickercropper.ui.CropperPickerActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class MediaSelectionFragment extends Fragment implements
        AlbumMediaCollection.AlbumMediaCallbacks, AlbumMediaAdapter.CheckStateListener,
        AlbumMediaAdapter.OnMediaClickListener {

    public static final String EXTRA_ALBUM = "extra_album";

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private RecyclerView mRecyclerView;
    private AlbumMediaAdapter mAdapter;
    private SelectionProvider mSelectionProvider;
    private AlbumMediaAdapter.CheckStateListener mCheckStateListener;
    private AlbumMediaAdapter.OnMediaClickListener mOnMediaClickListener;

    public static MediaSelectionFragment newInstance(Album album) {
        MediaSelectionFragment fragment = new MediaSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionProvider) {
            mSelectionProvider = (SelectionProvider) context;
        } else {
            throw new IllegalStateException("Context must implement SelectionProvider.");
        }
        if (context instanceof AlbumMediaAdapter.CheckStateListener) {
            mCheckStateListener = (AlbumMediaAdapter.CheckStateListener) context;
        }
        if (context instanceof AlbumMediaAdapter.OnMediaClickListener) {
            mOnMediaClickListener = (AlbumMediaAdapter.OnMediaClickListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Album album = getArguments().getParcelable(EXTRA_ALBUM);

        mAdapter = new AlbumMediaAdapter(getContext(),
                mSelectionProvider.provideSelectedItemCollection(), mRecyclerView);
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        int spanCount;
        SelectionSpec selectionSpec = SelectionSpec.getInstance();
        if (selectionSpec.gridExpectedSize > 0) {
            spanCount = UIUtils.spanCount(getContext(), selectionSpec.gridExpectedSize);
        } else {
            spanCount = selectionSpec.spanCount;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);

        // select first item on load
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    ArrayList<Uri> selectedImageUri = ((CropperPickerActivity) getActivity()).recentImageUri;

                    if (selectedImageUri != null)
                        if (selectedImageUri.size() != 0) {
                            for (Uri oldSelectedUri : selectedImageUri) {
                                int selectedImageIndex = 0;
                                String selectedImage = oldSelectedUri.toString();
                                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                                    String uri = mAdapter.getItemUri(i);

                                    if (uri.equals(selectedImage)) {
                                        selectedImageIndex = i;
                                        break;
                                    }
                                }
                                SelectedItemCollection mSelectedCollection = SelectedItemCollection.getInstance();
                                Item item = mAdapter.getItem(selectedImageIndex);
                                mSelectedCollection.add(item);
                                mSelectedCollection.setLastSelectedItem(item);
                                mAdapter.notifyItemChanged(selectedImageIndex);
                                EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), false));
                            }
//                        mRecyclerView.findViewHolderForAdapterPosition(selectedImageIndex).itemView.performClick();
                        }
//                    else
//                        mRecyclerView.findViewHolderForAdapterPosition(0).itemView.performClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
        mAlbumMediaCollection.onCreate(getActivity(), this);
        mAlbumMediaCollection.load(album, selectionSpec.capture);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAlbumMediaCollection.onDestroy();
    }

    public void refreshMediaGrid() {
        mAdapter.notifyDataSetChanged();
    }

    public void refreshSelection() {
        mAdapter.refreshSelection();
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onAlbumMediaReset() {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onUpdate() {
        // notify outer Activity that check state changed
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
//        if (mOnMediaClickListener != null) {
//            mOnMediaClickListener.onMediaClick((Album) getArguments().getParcelable(EXTRA_ALBUM),
//                    item, adapterPosition);
//        }
        Log.v("media", "Media is clicked");
    }

    public interface SelectionProvider {
        SelectedItemCollection provideSelectedItemCollection();
    }
}
