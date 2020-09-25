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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphaapps.instapickercropper.App;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Album;
import com.alphaapps.instapickercropper.internal.entity.SelectionSpec;
import com.alphaapps.instapickercropper.internal.model.AlbumCollection;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.alphaapps.instapickercropper.internal.ui.MediaSelectionFragment;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumsAdapter;
import com.alphaapps.instapickercropper.internal.ui.widget.AlbumsSpinner;
import com.alphaapps.instapickercropper.internal.utils.MediaStoreCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yalantis.ucrop.callback.ChangeRatioCallback;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class CropperPickerActivity extends AppCompatActivity implements MediaSelectionFragment.SelectionProvider, UCropFragmentCallback , ChangeRatioCallback, Serializable {

    private HashMap<String, float[]> createdFragmentsState = new HashMap<>();
    public ArrayList<String> originalPathName = new ArrayList<>();


    FrameLayout fragmentContainer;
    ProgressDialog mProgressDialog;
    public ArrayList<Uri> recentImageUri = new ArrayList<>();
    public float mediaAspectRation = 1;
    boolean showCameraIcon = false;

    public enum ToastType {RIGHT, WARNING, WRONG}

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
    public static final String SHOW_CAMERA = "SHOW_CAMERA";
    public static final String USER_NAME_KEY = "USER_NAME_KEY";
    public static final String USER_AVATAR_KEY = "USER_AVATAR_KEY";
    public static final String IS_SINGLE_IMAGE_KEY = "IS_SINGLE_IMAGE_KEY";
    public static final String RECENT_IMAGE_PATH_KEY = "RECENT_IMAGE_PATH_KEY";
    public static final String ASPECT_RATIO = "ASPECT_RATIO";
    public static final String SHOW_Camera_ICON = "SHOW_Camera_ICON";
    private static final int REQUEST_CODE_PREVIEW = 23;
    private static final int REQUEST_CODE_CAPTURE = 24;
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;
    private SelectedItemCollection mSelectedCollection = SelectedItemCollection.getInstance();
    private SelectionSpec mSpec;
    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;
    private View mContainer;
    private View mEmptyView;
    CropperPickerFragment fragment;

    static final int REQUEST_IMAGE_CAPTURE = 1;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // programmatically set theme before super.onCreate()
        mSpec = SelectionSpec.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper_picker);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        String userName = getIntent().getStringExtra(USER_NAME_KEY);
        String userAvatar = getIntent().getStringExtra(USER_AVATAR_KEY);
        boolean isSingleImage = getIntent().getBooleanExtra(IS_SINGLE_IMAGE_KEY, false);
        recentImageUri = (ArrayList<Uri>) getIntent().getExtras().get(RECENT_IMAGE_PATH_KEY);
        mediaAspectRation = getIntent().getFloatExtra(ASPECT_RATIO, 1);
        showCameraIcon = getIntent().getBooleanExtra(SHOW_Camera_ICON, false);
        String fragmnetStatesAsString = getIntent().getStringExtra(CropperPickerFragment.FRAGMENTS_STATUS);
        if (fragmnetStatesAsString != null && !fragmnetStatesAsString.equals("")) {

            HashMap<String, float[]> temp = fromJsonArray(fragmnetStatesAsString);
            for(String s : temp.keySet()){
                setCreatedFragmentsState(s,temp.get(s));
            }

        }
        fragment = CropperPickerFragment.newInstance(userName, userAvatar, isSingleImage, recentImageUri, showCameraIcon);
        getSupportFragmentManager().beginTransaction().add(fragmentContainer.getId(), fragment)
                .commit();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }

        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (data.getData() != null)
                Log.v("media", data.getData().toString());
//                mSelectedCollection.removeAllItems();
//                mSelectedCollection.setLatestId(getLatestId());
            //mAlbumCollection.loadAlbums();

        }

        if (requestCode == REQUEST_CODE_CAPTURE) {
            // Just pass the data back to previous calling Activity.
            Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
            String path = mMediaStoreCompat.getCurrentPhotoPath();
            ArrayList<Uri> selected = new ArrayList<>();
            selected.add(contentUri);
            ArrayList<String> selectedPath = new ArrayList<>();
            selectedPath.add(path);
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
            setResult(RESULT_OK, result);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                CropperPickerActivity.this.revokeUriPermission(contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            finish();
        }
    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            Fragment fragment = MediaSelectionFragment.newInstance(album);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return null;
    }

    public void showProgressDialog() {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this, R.style.CustomDialogTheme);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
                if (mProgressDialog.isShowing())
                    return;
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                mProgressDialog.show();
                mProgressDialog.getWindow().setLayout(App.getPXSize(124), App.getPXSize(124));
                mProgressDialog.setContentView(R.layout.custom_progress_dialog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        super.onPause();
    }

    public void displayCustomToast(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView tvToastText = (TextView) layout.findViewById(R.id.tvToastText);
        tvToastText.setText(message);

        ImageView ivToastImage = (ImageView) layout.findViewById(R.id.ivToastImage);

        ivToastImage.setColorFilter(ContextCompat.getColor(this, R.color.black));
        ivToastImage.setImageResource(R.drawable.warning);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 90);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        try {
            toast.show();
        } catch (Exception e) {
            Log.e("Toast Exception", e.getMessage());
        }
    }

    @Override
    public void loadingProgress(boolean showLoader) {
    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        fragment.onCropFinish(result);
    }

    @Override

    public void setCreatedFragmentsState(String s, float[] vals) {
        this.createdFragmentsState.put(s, vals);

    }

    @Override
    public float[] getCreatedFragmentsState(String s) {
        return createdFragmentsState.get(s);
    }

    public HashMap<String, float[]> getCreatedFragmentsState() {
        return this.createdFragmentsState;
    }

    public static HashMap<String, float[]> fromJsonArray(String jsonArray) {
        Type listType = new TypeToken<HashMap<String, float[]>>() {
        }.getType();

        HashMap<String, float[]> fragmentsStates = new Gson().fromJson(jsonArray.toString(), listType);
        return fragmentsStates;
    }

    public static String toJsonArray(HashMap<String, float[]> createdFragmentsState) {
        return new Gson().toJson(createdFragmentsState);
    }

    @Override
    public void updateExpandMode() {
    fragment.updateExpandMode();
    }

}