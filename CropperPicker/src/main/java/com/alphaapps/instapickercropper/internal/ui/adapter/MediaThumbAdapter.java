package com.alphaapps.instapickercropper.internal.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alphaapps.instapickercropper.MessageEvent;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Item;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MediaThumbAdapter extends RecyclerView.Adapter {

    private SelectedItemCollection mSelectedCollection;
    private Context context;
    LayoutInflater layoutInflater;
    public int selectedPos = -1;
    int currentSize = 0;
    RequestOptions options;
    public MediaThumbAdapter(Context context) {
        mSelectedCollection = SelectedItemCollection.getInstance();
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        options = new RequestOptions().centerCrop().dontAnimate();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_media_thumbnail, parent, false);
        return new MediaThumbVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        List<Item> items = mSelectedCollection.asList();
        MediaThumbVH viewHolder = (MediaThumbVH) holder;
        Item item = items.get(position);
        viewHolder.bind(item);
        Glide.with(context)
                .load(item.getContentUri())
                .apply(options)
                .into(viewHolder.thumbnailIV);
        Log.d(MediaThumbAdapter.class.getCanonicalName(), "selectItem: " + selectedPos + "   " + position);
        if (selectedPos == position)
            viewHolder.selectedFL.setVisibility(View.VISIBLE);
        else
            viewHolder.selectedFL.setVisibility(View.GONE);

    }

    public void notifyAdapterDataSetChanged() {
        if (currentSize != mSelectedCollection.count())
        {
            selectedPos = mSelectedCollection.count() - 1;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mSelectedCollection != null)
            return mSelectedCollection.count();
        return 0;
    }

    public void selectItem(Uri uri) {

        List<Item> items = mSelectedCollection.asList();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getContentUri().equals(uri)) {
                selectedPos = i;
                break;
            }
        }
        Log.d(MediaThumbAdapter.class.getCanonicalName(), "selectItem: " + selectedPos + "   " + uri);
        notifyDataSetChanged();
    }


    class MediaThumbVH extends RecyclerView.ViewHolder {
        ImageView thumbnailIV;
        FrameLayout selectedFL;
        Item item;


        public MediaThumbVH(View view) {
            super(view);
            thumbnailIV = view.findViewById(R.id.thumbnailIV);
            selectedFL = view.findViewById(R.id.selectedFL);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedPos == getAdapterPosition()) {
//                        EventBus.getDefault().post(new DeSelectItemEvent(item));
//                        selectedPos = -1;
                    } else if (mSelectedCollection.count() > 1) {
                        selectedFL.setVisibility(View.VISIBLE);
                        selectedPos = getAdapterPosition();
                        currentSize = mSelectedCollection.count();

                        EventBus.getDefault().post(new MessageEvent("id", item.getContentUri().toString(), true));
                    }
                    currentSize = mSelectedCollection.count();
                }
            });
        }

        public void bind(Item item) {
            this.item = item;
        }
    }
}
