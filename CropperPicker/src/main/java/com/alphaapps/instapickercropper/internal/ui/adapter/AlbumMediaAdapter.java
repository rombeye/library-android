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
package com.alphaapps.instapickercropper.internal.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphaapps.instapickercropper.MessageEvent;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Album;
import com.alphaapps.instapickercropper.internal.entity.IncapableCause;
import com.alphaapps.instapickercropper.internal.entity.Item;
import com.alphaapps.instapickercropper.internal.entity.SelectionSpec;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.alphaapps.instapickercropper.internal.ui.widget.CheckView;
import com.alphaapps.instapickercropper.internal.ui.widget.CropperPickerMediaGrid;
import com.alphaapps.instapickercropper.ui.CropperPickerActivity;

import org.greenrobot.eventbus.EventBus;

public class AlbumMediaAdapter extends
        RecyclerViewCursorAdapter<RecyclerView.ViewHolder> implements
        CropperPickerMediaGrid.OnMediaGridClickListener {

    private static final int VIEW_TYPE_CAPTURE = 0x01;
    private static final int VIEW_TYPE_MEDIA = 0x02;
    private final static SelectedItemCollection mSelectedCollection = SelectedItemCollection.getInstance();
    private final Drawable mPlaceholder;
    private static SelectionSpec mSelectionSpec;
    private CheckStateListener mCheckStateListener;
    private OnMediaClickListener mOnMediaClickListener;
    private RecyclerView mRecyclerView;
    private int mImageResize;
    Context context ;


    public AlbumMediaAdapter(Context context, SelectedItemCollection selectedCollection, RecyclerView recyclerView) {
        super(null);
        this.context = context;
        mSelectionSpec = SelectionSpec.getInstance();
//        mSelectedCollection = SelectedItemCollection.getInstance();
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.item_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();

        mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CAPTURE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cropper_picker_photo_capture_item, parent, false);
            CaptureViewHolder holder = new CaptureViewHolder(v);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getContext() instanceof OnPhotoCapture) {
                        ((OnPhotoCapture) v.getContext()).capture();
                    }
                }
            });
            return holder;
        } else if (viewType == VIEW_TYPE_MEDIA) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cropper_picker_media_grid_item, parent, false);
            return new MediaViewHolder(v);
        }
        return null;
    }

    @Override
    protected void onBindViewHolder(final RecyclerView.ViewHolder holder, Cursor cursor) {
        if (holder instanceof CaptureViewHolder) {
            CaptureViewHolder captureViewHolder = (CaptureViewHolder) holder;
            Drawable[] drawables = captureViewHolder.mHint.getCompoundDrawables();
            TypedArray ta = holder.itemView.getContext().getTheme().obtainStyledAttributes(
                    new int[]{R.attr.capture_textColor});
            int color = ta.getColor(0, 0);
            ta.recycle();
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
        } else if (holder instanceof MediaViewHolder) {
            MediaViewHolder mediaViewHolder = (MediaViewHolder) holder;

            final Item item = Item.valueOf(cursor);
            mediaViewHolder.mCropperPickerMediaGrid.preBindMedia(new CropperPickerMediaGrid.PreBindInfo(
                    getImageResize(mediaViewHolder.mCropperPickerMediaGrid.getContext()),
                    mPlaceholder,
                    mSelectionSpec.countable,
                    holder
            ));
            if (!mSelectionSpec.countable)
                if (item.getId() == mSelectedCollection.getLatestId()) {
                    mSelectedCollection.removeAllItems();
                    mSelectedCollection.add(item);
                }
            mediaViewHolder.mCropperPickerMediaGrid.bindMedia(item);
            mediaViewHolder.mCropperPickerMediaGrid.setOnMediaGridClickListener(this);
            setCheckStatus(item, mediaViewHolder.mCropperPickerMediaGrid);
        }
    }

    private void setCheckStatus(Item item, CropperPickerMediaGrid mediaGrid) {
        if (mSelectionSpec.countable) {
            int checkedNum = mSelectedCollection.checkedNumOf(item);
            if (checkedNum > 0) {
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setCheckedNum(checkedNum);
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false);
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setCheckedNum(checkedNum);
                }
            }
        } else {
            boolean selected = mSelectedCollection.isSelected(item);
            if (selected) {
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setChecked(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mediaGrid.getThumbnail().setImageAlpha(170);
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setChecked(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        mediaGrid.getThumbnail().setImageAlpha(255);
                } else {
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setChecked(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        mediaGrid.getThumbnail().setImageAlpha(255);
                }
            }
        }
    }

    @Override
    public void onThumbnailClicked(ImageView thumbnail, Item item, RecyclerView.ViewHolder holder) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(null, item, holder.getAdapterPosition());
        }
    }

    @Override
    public void onCheckViewClicked(CheckView checkView, Item item, RecyclerView.ViewHolder holder) {
        if (mSelectionSpec.countable) {
            int checkedNum = mSelectedCollection.checkedNumOf(item);
            if (checkedNum == CheckView.UNCHECKED) {
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    //new item -> select it
                    mSelectedCollection.add(item);
                    mSelectedCollection.setLastSelectedItem(item);
                    notifyCheckStateChanged();
                    EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), false));
                }
            } else {
                if (mSelectedCollection.getLastSelectedItem() != null && mSelectedCollection.getLastSelectedItem().equals(item) && mSelectedCollection.getItems().size() > 1) {
                    //old item  and already selected -> deselect it
                    mSelectedCollection.setLastSelectedItem(null);
                    mSelectedCollection.remove(item);

                    //remove the path from the original selected paths
                    ( (CropperPickerActivity)context).originalPathName.remove(item.getContentUri().toString());
                    notifyCheckStateChanged();

                } else if (mSelectedCollection.getItems().size() > 1) {
                    //old item  but not already selected -> select it
                    mSelectedCollection.setLastSelectedItem(item);
                    EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), true));
                } else mSelectedCollection.setLastSelectedItem(item);
            }
        } else {

            //////////////////////
            if (mSelectedCollection.isSelected(item)) {
                if (mSelectedCollection.getLastSelectedItem() != null && mSelectedCollection.getLastSelectedItem().equals(item)) {
                    mSelectedCollection.setLastSelectedItem(null);
                    mSelectedCollection.remove(item);
                    notifyCheckStateChanged();
                } else {
                    mSelectedCollection.setLastSelectedItem(item);
                    EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), true));
                }

            } else {
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    mSelectedCollection.removeAllItems();
                    mSelectedCollection.add(item);
                    mSelectedCollection.setLatestId(0);
                    mSelectedCollection.setLastSelectedItem(item);
                    EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), false));

                    notifyCheckStateChanged();


                }
            }
        }

    }

    private void notifyCheckStateChanged() {
        notifyDataSetChanged();
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    @Override
    public int getItemViewType(int position, Cursor cursor) {
        return Item.valueOf(cursor).isCapture() ? VIEW_TYPE_CAPTURE : VIEW_TYPE_MEDIA;
    }

    @Override
    public String getItemUri(int position, Cursor cursor) {
        return Item.valueOf(cursor).uri.toString();
    }

    private boolean assertAddSelection(Context context, Item item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item, context);
        IncapableCause.handleCause(context, cause);
        return cause == null;
    }

    public void registerCheckStateListener(CheckStateListener listener) {
        mCheckStateListener = listener;
    }

    public void unregisterCheckStateListener() {
        mCheckStateListener = null;
    }

    public void registerOnMediaClickListener(OnMediaClickListener listener) {
        mOnMediaClickListener = listener;
    }

    public void unregisterOnMediaClickListener() {
        mOnMediaClickListener = null;
    }

    public void refreshSelection() {
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        if (first == -1 || last == -1) {
            return;
        }
        Cursor cursor = getCursor();
        for (int i = first; i <= last; i++) {
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(first);
            if (holder instanceof MediaViewHolder) {
                if (cursor.moveToPosition(i)) {
                    setCheckStatus(Item.valueOf(cursor), ((MediaViewHolder) holder).mCropperPickerMediaGrid);
                }
            }
        }
    }

    private int getImageResize(Context context) {
        if (mImageResize == 0) {
            RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
            int spanCount = ((GridLayoutManager) lm).getSpanCount();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int availableWidth = screenWidth - context.getResources().getDimensionPixelSize(
                    R.dimen.media_grid_spacing) * (spanCount - 1);
            mImageResize = availableWidth / spanCount;
            mImageResize = (int) (mImageResize * mSelectionSpec.thumbnailScale);
        }
        return mImageResize;
    }

    public interface CheckStateListener {
        void onUpdate();
    }

    public interface OnMediaClickListener {
        void onMediaClick(Album album, Item item, int adapterPosition);
    }

    public interface OnPhotoCapture {
        void capture();
    }

    private static class MediaViewHolder extends RecyclerView.ViewHolder {

        private CropperPickerMediaGrid mCropperPickerMediaGrid;

        MediaViewHolder(final View itemView) {
            super(itemView);
            mCropperPickerMediaGrid = (CropperPickerMediaGrid) itemView;
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    EventBus.getDefault().post(new MessageEvent("id", mCropperPickerMediaGrid.getMedia().getContentUri().toString()));
//                    if (!mSelectionSpec.countable)
//                        mCropperPickerMediaGrid.setChecked(true);
//                    else {
//                        mCropperPickerMediaGrid.onClick(itemView);
//                    }
//                }
//            });
        }
    }

    private static class CaptureViewHolder extends RecyclerView.ViewHolder {

        private TextView mHint;

        CaptureViewHolder(View itemView) {
            super(itemView);

            mHint = (TextView) itemView.findViewById(R.id.hint);
        }
    }
}
