package com.alphaapps.instapickercropper.ui.ActionDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.alphaapps.instapickercropper.CustomTextView;
import com.alphaapps.instapickercropper.R;

import java.util.List;


/**
 * Created by dali on 1/11/2018.
 */

public class ActionDialogAdapter extends BaseAdapter {

    List<DialogOption> mList;
    Context mContext;
    private static LayoutInflater inflater = null;

    public ActionDialogAdapter(Context mContext, List<DialogOption> mList) {
        this.mList = mList;
        this.mContext = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<DialogOption> getmList() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mview = convertView;
        if (mview == null)
            mview = inflater.inflate(R.layout.cropper_action_dialog_item, null);
        mview.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(mContext, 48)));
        CustomTextView option = mview.findViewById(R.id.optionTV);
        option.setText(mList.get(position).getOptionText());
        option.setTextColor(mList.get(position).getTextColor());
//        option.setBackgroundColor(mList.get(position).getBackgroundColor());

        return mview;
    }

    public static int dip2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
