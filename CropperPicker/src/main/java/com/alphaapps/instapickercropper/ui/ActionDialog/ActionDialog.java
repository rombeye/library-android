package com.alphaapps.instapickercropper.ui.ActionDialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alphaapps.instapickercropper.CustomTextView;
import com.alphaapps.instapickercropper.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dali on 1/11/2018.
 */

public class ActionDialog extends BottomSheetDialog {

    public static final long LENGTH_LONG = 2750;
    public static final long LENGTH_SHORT = 1500;
    ActionDialogAdapter actionDialogAdapter;
    ListView listView;
    int currentAdapterPosition;
    int backgroundColor;

    public ActionDialog(@NonNull Context context) {
        super(context);
    }


    /**
     * Builds an action dialog
     *
     * @param items           options list with specific attributes.
     * @param listener        listener to handle click events.
     * @param length          length before dialog disappears, pass 0 to disable this functionality.
     * @param context
     * @param backGroundColor
     * @return ActionDialog , Call show(fragment manager, fragment tag) to show the dialog.
     */
    public static ActionDialog getInstance(List<DialogOption> items, OnClickListener listener, long length, Context context, int backGroundColor) {
        ActionDialog fragment = new ActionDialog(context);
        fragment.setItems(items).setmListener(listener).setLength(length).setContext(context).setBackgroundColor(Color.parseColor("#00FFFFFF"));
        return fragment;
    }

    private boolean isAnimation = false;
    private View mRootView;
    private OnClickListener mListener;
    private OnDismissListener dismissListener;
    private long length = 2750;
    private boolean dismissed = true;
    private Context context;
    private String message = null;

    List<DialogOption> items;

    public ActionDialog setmListener(OnClickListener mListener) {
        this.mListener = mListener;
        return this;
    }

    public ActionDialog setItems(List<DialogOption> items) {
        this.items = new ArrayList<>();
        this.items.addAll(items);
        return this;
    }

    public ActionDialog setDismissListener(OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
        return this;
    }

    public ActionDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public ActionDialog setLength(long length) {
        this.length = length;
        return this;
    }

    public ActionDialog setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void setCurrentAdapterPosition(int adapterPosition) {
        this.currentAdapterPosition = adapterPosition;
    }

    public int getCurrentAdapterPosition() {
        return currentAdapterPosition;
    }

    public void setDialogOptionItems(List<DialogOption> optionItems) {
        if (this.items == null)
            items = new ArrayList<>();
        this.items.clear();
        this.items.addAll(optionItems);
//        this.items = optionItems;
        if (actionDialogAdapter != null)
            actionDialogAdapter.notifyDataSetChanged();
        else if (listView != null) {
            actionDialogAdapter = new ActionDialogAdapter(context, items);
            listView.setAdapter(actionDialogAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dismiss();
                }
                return true;
            }
        });

    }

    public boolean isDismissed() {
        return dismissed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropper_action_dialog);
        initData();
        dismissed = false;
        setCancelable(true);

//        if (length != 0)
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ActionDialog.this.dismiss();
//                }
//            }, length);

        listView = findViewById(R.id.bottom_lib_listView);
        ViewGroup messageLL = findViewById(R.id.messageLL);
        actionDialogAdapter = new ActionDialogAdapter(context, items);
        listView.setAdapter(actionDialogAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.click(items.get(position).getAction());
                }
                dismiss();
            }
        });
        listView.setBackgroundColor(backgroundColor);
        if (message != null) {
            messageLL.setVisibility(View.VISIBLE);
            ((CustomTextView) findViewById(R.id.messageTV)).setText(message);
        }
    }

    public void updateMessage(String message) {
        this.message = message;
        ((CustomTextView) findViewById(R.id.messageTV)).setText(message);
    }

    private void initData() {
        if (items == null)
            items = new ArrayList<>();
    }


    @Override
    public void dismiss() {
        if (dismissListener != null)
            dismissListener.dialogDismissed();
        ActionDialog.super.dismiss();

    }

    public interface OnClickListener {
        void click(int action);
    }

    public interface OnDismissListener {
        void dialogDismissed();
    }

}
