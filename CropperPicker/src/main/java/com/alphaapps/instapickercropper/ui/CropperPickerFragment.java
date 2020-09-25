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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphaapps.instapickercropper.CustomTextView;
import com.alphaapps.instapickercropper.MessageEvent;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Album;
import com.alphaapps.instapickercropper.internal.entity.Item;
import com.alphaapps.instapickercropper.internal.entity.SelectionSpec;
import com.alphaapps.instapickercropper.internal.model.AlbumCollection;
import com.alphaapps.instapickercropper.internal.model.CroppingViewHolder;
import com.alphaapps.instapickercropper.internal.model.MediaItem;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.alphaapps.instapickercropper.internal.model.VideoItemVH;
import com.alphaapps.instapickercropper.internal.ui.MediaSelectionFragment;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumMediaAdapter;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumsAdapter;
import com.alphaapps.instapickercropper.internal.ui.adapter.MediaThumbAdapter;
import com.alphaapps.instapickercropper.internal.ui.widget.AlbumsSpinner;
import com.alphaapps.instapickercropper.internal.utils.MediaStoreCompat;
import com.alphaapps.instapickercropper.ui.ActionDialog.ActionDialog;
import com.alphaapps.instapickercropper.ui.ActionDialog.DialogOption;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class CropperPickerFragment extends Fragment implements
        AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener,
        MediaSelectionFragment.SelectionProvider, View.OnClickListener,
        AlbumMediaAdapter.OnPhotoCapture {
    public static final String USER_NAME_KEY = "USER_NAME";
    public static final String IS_SINGLE_IMAGE_KEY = "IS_SINGLE_IMAGE_KEY";
    public static final String RECENT_URI_IMAGE_KEY = "RECENT_URI_IMAGE";
    public static final String ASPECT_RATIO = "ASPECT_RATIO";
    public static final String SHOW_Camera_ICON = "SHOW_Camera_ICON";
    private static final String TAG = "CropperPickerFragment";
    private static final int MAX_IMAGE_DIMENSION = 100000;

    boolean isExpanded = false, isMultiple = false;
    RectF mCrop = new RectF();
    int screenWidth;
    FrameLayout continaer;
    ImageView remove_imgIV;
    ImageView changeImgSizeIV;
    String lastSelectedUri = null;
    TextView nextTV;
    TextView backTV;
    ImageView openCamera;
    CustomTextView userNameTV;
    RecyclerView mediaThumbnailsRV;
    MediaThumbAdapter mediaThumbAdapter;
    AppBarLayout appBarLayout;
    final Handler handler = new Handler();
    UCropFragment uCropFragment;
    VideoFragment videoFragment;
    int croppedItemsCounter = 0;

    ProgressDialog mProgressDialog;


    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_ORIGINAL_PATH = "extra_result_selection_path";
    public static final String CROP_ASPECT_RATIO = "CROP_ASPECT_RATIO";
    public static final String MEDIA_MODE = "MEDIA_MODE";
    public static final String MEDIA_ITEMS = "MEDIA_ITEMS";
    public static final String IS_EXPANDED = "IS_EXPANDED";
    public static final String FRAGMENTS_STATUS = "FRAGMENTS_STATUS";


    private static final int REQUEST_CODE_PREVIEW = 23;
    private static final int REQUEST_CODE_CAPTURE = 24;
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;
    private SelectedItemCollection mSelectedCollection;

    private SelectionSpec mSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;
    private View mContainer;
    private View mEmptyView;
    Random randomNumber;
    public static int viewHeight = 0;
    private FrameLayout cropperContainer;
    ArrayList<Uri> recentImageUri;
    float mediaAspectRatio;
    int mode = 0; // 0 -> Landscape 1-> portrait
    boolean showCameraIcon = false;
    FragmentManager manager;
    boolean isSingleImage = false;
    CropperPickerActivity context;

    ActionDialog dialog;
    ActionDialog.OnClickListener actionDialogListener = new ActionDialog.OnClickListener() {
        @Override
        public void click(int action) {
            if (action == DialogOption.ACTION_PROCEED) {
                ((CropperPickerActivity) getActivity()).showProgressDialog();
                cropItemsSet();
            }
        }
    };
    private MediaSelectionFragment mediaSelectionFragment;

    public static CropperPickerFragment newInstance(String userName, String userAvatar, boolean isSingleImage, ArrayList<Uri> recentImageURI, boolean showGalleryIcon) {
        Bundle args = new Bundle();
        CropperPickerFragment fragment = new CropperPickerFragment();
        args.putString(USER_NAME_KEY, userName);
        args.putBoolean(IS_SINGLE_IMAGE_KEY, isSingleImage);
        args.putSerializable(RECENT_URI_IMAGE_KEY, recentImageURI);
        args.putBoolean(SHOW_Camera_ICON, showGalleryIcon);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cropper_picker, container, false);
        // programmatically set theme before super.onCreate()
        randomNumber = new Random();
        mSpec = SelectionSpec.getInstance();
        getActivity().setTheme(mSpec.themeId);
        manager = getChildFragmentManager();
        mSelectedCollection = SelectedItemCollection.getInstance();

        context = (CropperPickerActivity) getActivity();
        if (mSpec.needOrientationRestriction()) {
            getActivity().setRequestedOrientation(mSpec.orientation);
        }
        List<DialogOption> options = new ArrayList();
        options.add(new DialogOption(getString(R.string.proceed), getResources().getColor(R.color.new_black), DialogOption.ACTION_PROCEED));
        options.add(new DialogOption(getString(R.string.cancel), getResources().getColor(R.color.red), DialogOption.ACTION_CANCEL));
        dialog = ActionDialog.getInstance(options, actionDialogListener, ActionDialog.LENGTH_LONG, getActivity(), Color.WHITE);

        String userName = getArguments().getString(USER_NAME_KEY);
        isSingleImage = getArguments().getBoolean(IS_SINGLE_IMAGE_KEY, false);
        recentImageUri = (ArrayList<Uri>) getArguments().getSerializable(RECENT_URI_IMAGE_KEY);

        showCameraIcon = getArguments().getBoolean(SHOW_Camera_ICON);

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(getActivity());
            if (mSpec.captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }
        cropperContainer = view.findViewById(R.id.cropperContainer);

        cropperContainer.setClipToOutline(true);

        remove_imgIV = view.findViewById(R.id.remove_imgIV);
        changeImgSizeIV = view.findViewById(R.id.expand_collapseIV);

        remove_imgIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deSelectItem();
            }
        });


        changeImgSizeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateExpandMode();

            }
        });
//        rotateIV = view.findViewById(R.id.rotateIV);
        TypedArray ta = getActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        ta.recycle();
        mContainer = view.findViewById(R.id.container);
        mEmptyView = view.findViewById(R.id.empty_view);
        userNameTV = view.findViewById(R.id.albumNameTV);
        userNameTV.setText(userName);

        View collapseViewFL = view.findViewById(R.id.albumsAnchorFL);
        nextTV = view.findViewById(R.id.nextTV);
        nextTV.setEnabled(true);
        backTV = view.findViewById(R.id.backTV);
        openCamera = view.findViewById(R.id.openCamera);
        appBarLayout = view.findViewById(R.id.appbar);
        mediaThumbnailsRV = view.findViewById(R.id.mediaThumbnailsRV);
//        mediaThumbnailsRV.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        mediaThumbAdapter = new MediaThumbAdapter(getActivity());
        mediaThumbnailsRV.setAdapter(mediaThumbAdapter);

        continaer = (FrameLayout) mContainer;
        mSelectedCollection.onCreate(savedInstanceState);
        mAlbumsAdapter = new AlbumsAdapter(getActivity(), null, false);
        mAlbumsSpinner = new AlbumsSpinner(getActivity());

        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView(userNameTV);
        mAlbumsSpinner.setPopupAnchorView(collapseViewFL);
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        mAlbumCollection.onCreate(getActivity(), this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singleItemSelected(true, "");
            }
        });


        if (recentImageUri != null) {
            if (recentImageUri.size() > 0) {
                openCamera.setVisibility(View.GONE);
                nextTV.setVisibility(View.VISIBLE);
                backTV.setText(R.string.cancel);
            }
        } else {
            if (showCameraIcon)
                openCamera.setVisibility(View.VISIBLE);
            else
                openCamera.setVisibility(View.GONE);

            nextTV.setVisibility(View.GONE);
            backTV.setText(R.string.close);

        }

        //set the height of the image frame as rectangle (the width & height = screen width)
        setLayoutParams();
        cropperContainer.setMinimumHeight(viewHeight);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cropperContainer.getLayoutParams();
        params.height = viewHeight;
        cropperContainer.setLayoutParams(params);

        //end height setting

        cropperContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cropperContainer.getChildAt(0) instanceof SimpleExoPlayerView) {
                    SimpleExoPlayerView view1 = (SimpleExoPlayerView) cropperContainer.getChildAt(0);
                    view1.getPlayer().setPlayWhenReady(!view1.getPlayer().getPlayWhenReady());
                }
            }
        });

        nextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTV.setEnabled(false);
                ((CropperPickerActivity) getActivity()).showProgressDialog();
                if (mSelectedCollection.getItems().size() == 0) {
                    displayCustomToast(getString(R.string.error_no_count_default));
                    return;
                }
                cropItemsSet();

//                if (!(cropperContainer.getChildAt(0) instanceof SimpleExoPlayerView)) { // the view is an image
//                    CropperView cropperView = cropperContainer.findViewById(R.id.cropperView);
//                    if (cropperView.isCropping() || cropperView.isAdjusting() || cropperView.isEditing()) {
//                        displayCustomToast(getString(R.string.error_cannot_proceed_while_cropping));
//                        return;
//                    }
//                }

            }
        });
        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        // Disable "Drag" for AppBarLayout (i.e. User can't scroll appBarLayout by directly touching appBarLayout - User can only scroll appBarLayout by only using scrollContent)
        if (appBarLayout.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();

            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
        if (recentImageUri == null) {
            ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
            layoutParams.height = view.findViewById(R.id.maintoolbar).getLayoutParams().height;
            appBarLayout.setLayoutParams(layoutParams);
        }

        return view;
    }

    private void changeMediaSize(boolean isExpanded) {
        if (!isVideo(lastSelectedUri))
            uCropFragment.changeExpandMode(isExpanded);
        else {
            videoFragment.setVideoLayoutParams(videoLayoutParams(isExpanded));
        }
    }

    FrameLayout.LayoutParams videoLayoutParams(boolean isExpanded) {
        FrameLayout.LayoutParams params;
        if (isExpanded)
            params = new FrameLayout.LayoutParams((int) cropperContainer.getWidth(), (int) cropperContainer.getHeight(), Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        else {
            RectF rectF = calculateVideoSize();
            params = new FrameLayout.LayoutParams((int) rectF.width(), (int) rectF.height(), (Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL));
        }
        params.gravity = Gravity.CENTER;

        return params;
    }

    RectF calculateVideoSize() {

        if (!isMultiple) {
            //CropRec: left - top - right - bottom
            int height = (int) (cropperContainer.getHeight() / mediaAspectRatio);
            RectF mCropRect = new RectF();
            if (height > cropperContainer.getHeight()) {
                int width = (int) (cropperContainer.getHeight() * mediaAspectRatio);
                int halfDiff = (cropperContainer.getWidth() - width) / 2;
                mCropRect.set(halfDiff, 0, width + halfDiff, cropperContainer.getHeight());
            } else {
                int halfDiff = (cropperContainer.getHeight() - height) / 2;

                mCropRect.set(0, halfDiff, cropperContainer.getWidth(), height + halfDiff);
            }

            return mCropRect;
        } else {
            return mCrop;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCropperPickerMessageEvent(MessageEvent event) {

        if (event.getAction() != null)
            if (!context.originalPathName.contains(event.getAction()))
                context.originalPathName.add(event.getAction().toString());
        if (isSingleImage) {
            singleItemSelected(false, event.getAction());
        } else {
            ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
            layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            appBarLayout.setLayoutParams(layoutParams);
            stopVideo();
            if (event.getAction() != null) {
                appBarLayout.setExpanded(true);
                Uri mediaUri = Uri.parse(event.getAction());

                if (showCameraIcon) {
                    openCamera.setVisibility(View.GONE);
                    nextTV.setVisibility(View.VISIBLE);
                }
                lastSelectedUri = mediaUri.toString();

                Log.d("received ", lastSelectedUri);
                //video item
                if (isVideo(lastSelectedUri)) { // selected item already been added to the list, so just reselect it

                    if (!event.isOld())
                        createNewVideoView(Uri.parse(event.getAction()));
                    else
                        retrieveOldVideoFrame(Uri.parse(event.getAction()));

                } else { // Image item

                    ////////////////insert the new cropper

                    if (!event.isOld())
                        createNewCroppingFrame(Uri.parse(event.getAction()));
                    else
                        retrieveOldCroppingFrame(Uri.parse(event.getAction()));
                }

                mediaThumbAdapter.selectItem(Uri.parse(lastSelectedUri));
            }

            if (mSelectedCollection.getItems().size() == 1) {
                remove_imgIV.setVisibility(View.GONE);
                changeImgSizeIV.setVisibility(View.VISIBLE);
                getRatio();
                isMultiple = false;
                if (!isVideo(lastSelectedUri))
                    uCropFragment.changeIsMultiple(isMultiple);
                else
                    videoFragment.setVideoLayoutParams(videoLayoutParams(isExpanded));


            } else {
                remove_imgIV.setVisibility(View.VISIBLE);
                changeImgSizeIV.setVisibility(View.GONE);
                isMultiple = true;
                if (!isVideo(lastSelectedUri))
                    uCropFragment.changeIsMultiple(isMultiple, mCrop);
                else
                    videoFragment.setVideoLayoutParams(videoLayoutParams(isExpanded));
            }

            if (mediaAspectRatio <= 1.05f && mediaAspectRatio >= 0.95f && !isExpanded) //hide changeImgSizeIV for squared img
                changeImgSizeIV.setVisibility(View.GONE);

//            mediaThumbAdapter.notifyAdapterDataSetChanged();
        }
    }

    public void updateExpandMode() {
        if (isExpanded) {
            changeImgSizeIV.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_expand));
        } else {
            changeImgSizeIV.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_collapse));
        }
        isExpanded = !isExpanded;
        getRatio();

        changeMediaSize(isExpanded);
    }

    public void getRatio() {
        String imgPath = lastSelectedUri;
        Bitmap bitmap;
        try {
            int w, h;

            if (imgPath.toString().contains("video")) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                Bitmap bmp = null;

                retriever.setDataSource(getContext(), Uri.parse(imgPath));
                bmp = retriever.getFrameAtTime();
                h = bmp.getHeight();
                w = bmp.getWidth();

            } else {
//                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imgPath));
                bitmap = scaleImage(context, Uri.parse(imgPath));
                h = bitmap.getHeight();
                w = bitmap.getWidth();
            }

            mediaAspectRatio = (float) w / h;

//
            if (mediaAspectRatio > 1.923f)
                mediaAspectRatio = 1.923f;
            if (mediaAspectRatio < 0.8f)
                mediaAspectRatio = 0.8f;

            if (isExpanded)
                mediaAspectRatio = 1.0f;


            if (w > h)
                mode = 0;
            else
                mode = 1;

            //CropRec: left - top - right - bottom
            int height = (int) (viewHeight / mediaAspectRatio);
            if (height > viewHeight) {
                int width = (int) (viewHeight * mediaAspectRatio);
                int halfDiff = (viewHeight - width) / 2;
                mCrop.set(halfDiff, 0, width + halfDiff, viewHeight);
            } else {
                int halfDiff = (viewHeight - height) / 2;

                mCrop.set(0, halfDiff, viewHeight, height + halfDiff);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //     deselect the thumbnail
    void deSelectItem() {
        List<Item> items = mSelectedCollection.asList();
        for (Item i : items) {
            if (i.uri.toString().equals(lastSelectedUri)) {

                deselect(i);
                return;
//                if (mSelectedCollection.remove(i)) {
////                mSelectedCollection.croppingItems.remove(i.getContentUri().toString());
//
//                    originalPathName.remove(lastSelectedUri);
//
//                    mediaSelectionFragment.refreshSelection();
//                    mediaSelectionFragment.refreshMediaGrid();
//
//                    mediaThumbAdapter.notifyAdapterDataSetChanged();
//                    return;
//                }
            }
        }
    }


    void deselect(Item item) {
        mSelectedCollection.setLastSelectedItem(null);

        if (mSelectedCollection.remove(item)) {
            manager
                    .beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .remove(manager.findFragmentByTag(item.getContentUri().toString()))
                    .commitNowAllowingStateLoss();
            //remove the path from the original selected paths
            context.originalPathName.remove(item.getContentUri().toString());
            mediaSelectionFragment.refreshSelection();
            mediaSelectionFragment.refreshMediaGrid();
        }
    }

    void deSelectAllItem() {
        List<Item> items = mSelectedCollection.asList();
        for (Item i : items) {

            deselect(i);

        }

    }

    void stopVideo() {
        for (Item item : mSelectedCollection.getItems()) {
            if (manager.findFragmentByTag(item.getContentUri().toString()) instanceof VideoFragment) {
                ((VideoFragment) manager.findFragmentByTag(item.getContentUri().toString())).stopVideo();
            }
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj,
                null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public static String getImageUri(Bitmap inImage, int randomNumber) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage = Bitmap.createBitmap(inImage, 0, 0, inImage.getWidth(), inImage.getHeight(), null, false);
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        inImage.recycle();
        // generate a unique name for image to save in device temp dir
        String fileName = System.currentTimeMillis() + "" + randomNumber;
        return reFormatPath(bitmapToUri(fileName, bytes.toByteArray()).toString());
    }

    public static String reFormatPath(String unformattedPath) {
        String extraString = "file://";
        if (unformattedPath.contains(extraString)) {
            return unformattedPath.replace(extraString, "");
        }
        return unformattedPath;
    }

    public static File bitmapToUri(String fileName, byte[] imageBytes) {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        tempDir.mkdir();
        //write the bytes in file
        FileOutputStream fos = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile(fileName, ".jpg", tempDir);
            fos = new FileOutputStream(tempFile);
            fos.write(imageBytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVideo();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSelectedCollection.getItems().clear();
        mAlbumCollection.onDestroy();
    }


    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAlbumCollection.setStateCurrentSelection(position);
        mAlbumsAdapter.getCursor().moveToPosition(position);
        Album album = Album.valueOf(mAlbumsAdapter.getCursor());
        if (album.isAll() && SelectionSpec.getInstance().capture) {
            album.addCaptureCount();
        }
        onAlbumSelected(album);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onAlbumLoad(final Cursor cursor) {
        mAlbumsAdapter.swapCursor(cursor);
        // select default album.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {

                if (recentImageUri == null)
                    appBarLayout.setExpanded(false);
                else
                    appBarLayout.setExpanded(true);
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                mAlbumsSpinner.setSelection(getActivity(),
                        mAlbumCollection.getCurrentSelection());
                Album album = Album.valueOf(cursor);
                if (album.isAll() && SelectionSpec.getInstance().capture) {
                    album.addCaptureCount();
                }
                onAlbumSelected(album);
            }
        });
    }

    @Override
    public void onAlbumReset() {
        //  mAlbumsAdapter.swapCursor(null);
    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            mediaSelectionFragment = MediaSelectionFragment.newInstance(album);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, mediaSelectionFragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }

    @Override
    public void capture() {
        if (mMediaStoreCompat != null) {
            mMediaStoreCompat.dispatchCaptureIntent(getContext(), REQUEST_CODE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    return;
                } else {

                    Toast.makeText(getContext(), "Permission denied, can't perform this operation.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    ArrayList<MediaItem> resultItems = new ArrayList<>();

    public void cropItemsSet() {
        if (mSelectedCollection.getItems().isEmpty())
            return;
        stopVideo();
        cropItem();
    }

    private void cropItem() {
        for (String s : context.originalPathName) {
            if (!isVideo(s)) {
                UCropFragment uCropFragment = (UCropFragment) manager.findFragmentByTag(s);
                cropImageAsync(uCropFragment);
            } else {
                VideoFragment videoFragment = (VideoFragment) manager.findFragmentByTag(s);
//                ((SimpleExoPlayerView) (videoFragment).videoItemVH.mView).getPlayer().release();

                final VideoItemVH videoItemVH = videoFragment.videoItemVH;
                resultItems.add(videoItemVH.getMediaItem());
                returnPickerResult();
            }
        }
    }

    boolean isVideo(String s) {
        if (s.contains("video"))
            return true;
        else return false;
    }


    private void cropImageAsync(UCropFragment f) {
        f.cropAndSaveImage(); // save cropping result in UCrop

    }

    public void displayCustomToast(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
        TextView tvToastText = (TextView) layout.findViewById(R.id.tvToastText);
        tvToastText.setText(message);

        ImageView ivToastImage = (ImageView) layout.findViewById(R.id.ivToastImage);

        ivToastImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.black));
        ivToastImage.setImageResource(R.drawable.warning);

        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 90);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        try {
            toast.show();
        } catch (Exception e) {
            Log.e("Toast Exception", e.getMessage());
        }
    }

    void setLayoutParams() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        viewHeight = Math.round((screenWidth - marginInPx()));

        //set the height of the image frame as rectangle (the width & height = screen width)

        viewHeight = Math.round((screenWidth - marginInPx()));
    }

    int marginInPx() {
        int marginInPx = 0;
        try {
            float density = getActivity().getResources().getDisplayMetrics().density;
            marginInPx = Math.round((float) 32 * density);
        } catch (Exception ignored) {
        }
        return marginInPx;
    }


    //this function is used when we need to prevent the use from selecting more than one item, so that we close the gallery once the user select an img
    public void singleItemSelected(boolean backToCamera, String imgPath) {
        Intent result = new Intent();
        if (backToCamera)
            result.putStringArrayListExtra(EXTRA_RESULT_ORIGINAL_PATH, null);
        else {
            result.putStringArrayListExtra(EXTRA_RESULT_ORIGINAL_PATH, context.originalPathName);
            try {
                Bitmap bitmap;
                resultItems = new ArrayList<>();

                if (imgPath.toString().contains("video")) {

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    Bitmap bmp = null;

                    retriever.setDataSource(getContext(), Uri.parse(imgPath));
                    bmp = retriever.getFrameAtTime();
                    int videoHeight = bmp.getHeight();
                    int videoWidth = bmp.getWidth();

                    View view = getLayoutInflater().inflate(R.layout.exoplayer_view, cropperContainer, false);
                    view.setLayoutParams(new ViewGroup.LayoutParams(videoWidth, videoHeight));
                    VideoItemVH videoItemVH = new VideoItemVH(view, Uri.parse(imgPath));
                    CroppingViewHolder croppingVH = new CroppingViewHolder(videoItemVH, imgPath);
                    croppingVH.getVideoView().getSimpleExoPlayerView().release();
                    MediaItem item = croppingVH.getVideoView().getMediaItem();
                    resultItems.add(item);
                    mediaAspectRatio = (float) videoHeight / videoWidth;

                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imgPath));

                    mediaAspectRatio = ((float) bitmap.getHeight()) / bitmap.getWidth();
                    String mediaUri = getImageUri(bitmap, randomNumber.nextInt());

                    resultItems.add(new MediaItem(mediaUri, true, false, false));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        result.putExtra(CROP_ASPECT_RATIO, mediaAspectRatio);
        result.putExtra(MEDIA_ITEMS, MediaItem.toJsonArray(resultItems));
        result.putExtra(IS_EXPANDED, isExpanded);
        result.putExtra(MEDIA_MODE, mode);

        if (isAdded() && getActivity() != null) {
            ((CropperPickerActivity) getActivity()).hideProgressDialog();
            getActivity().setResult(RESULT_OK, result);
            getActivity().finish();
        }
    }


    void removeAllThumbnails() {
        mSelectedCollection.removeAllItems();
        mediaThumbAdapter.notifyAdapterDataSetChanged();
    }


    //////////////////////

    void hideAllViews() {
        String itemToBeRemove = "";

        if (mSelectedCollection.getItems().size() != context.originalPathName.size()) {

            for (String s : context.originalPathName) {

                if (mSelectedCollection.getSingleItem(s) == null) {
                    itemToBeRemove = s;
                }
            }
            manager
                    .beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .remove(manager.findFragmentByTag(itemToBeRemove))
                    .commitNowAllowingStateLoss();
            //remove the path from the original selected paths
            context.originalPathName.remove(itemToBeRemove);
        }
        for (Item item : mSelectedCollection.getItems()) {
            if (manager.findFragmentByTag(item.getContentUri().toString()) instanceof UCropFragment) {

                //save the state of the value before rome it
                UCropFragment oldFragment = ((UCropFragment) manager.findFragmentByTag(item.getContentUri().toString()));
                oldFragment.mGestureCropImageView.setIsOldFragment(true, oldFragment.mGestureCropImageView.getCurrentImageMatrix());

            }
            if (!item.getContentUri().toString().equals(lastSelectedUri)) {
                manager
                        .beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .hide(manager.findFragmentByTag(item.getContentUri().toString()))
                        .commit();
            }

        }
    }

    void retrieveOldCroppingFrame(Uri selectedUri) {
        hideAllViews();

        uCropFragment = (UCropFragment) manager.findFragmentByTag(selectedUri.toString());
        manager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(manager.findFragmentByTag(selectedUri.toString()))
                .commit();
    }


    void retrieveOldVideoFrame(Uri selectedUri) {
        hideAllViews();

        videoFragment = (VideoFragment) manager.findFragmentByTag(selectedUri.toString());
        videoFragment.startVideo();
        manager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(videoFragment)
                .commit();
    }


    void createNewVideoView(Uri selectedUri) {

        videoFragment = VideoFragment.newInstance(selectedUri.toString());

        manager
                .beginTransaction()
                .add(R.id.cropperContainer, videoFragment, selectedUri.toString())
                .commitNowAllowingStateLoss();
    }

    void createNewCroppingFrame(Uri selectedUri) {

        String destinationFileName = System.currentTimeMillis() + ".jpg";
        UCrop uCrop = UCrop.of(selectedUri, Uri.fromFile(new File(getActivity().getCacheDir(), destinationFileName)));

        uCrop = advancedConfig(uCrop);

        uCropFragment = uCrop.getFragment(uCrop.getIntent(getActivity()).getExtras(), getActivity());

        manager
                .beginTransaction()
                .add(R.id.cropperContainer, uCropFragment, selectedUri.toString())
                .commitNowAllowingStateLoss();
    }

    ///////////////


    public void onCropFinish(UCropFragment.UCropResult result) {
        switch (result.mResultCode) {
            case RESULT_OK:
                handleCropResult(result.mResultData);
                break;
            case UCrop.RESULT_ERROR:
                handleCropError(result.mResultData);
                break;
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
        } else {
        }
    }

    void saveStates() {
        List<Item> items = mSelectedCollection.asList();
        for (Item i : items) {
            if (!isVideo(i.getContentUri().toString())) {
                float[] vals = new float[9];
                ((UCropFragment) manager.findFragmentByTag(i.getContentUri().toString())).mGestureCropImageView.getCurrentImageMatrix().getValues(vals);
                ((CropperPickerActivity) context).setCreatedFragmentsState(i.getContentUri().toString(), vals);
            }
        }
    }

    void returnPickerResult() {
        croppedItemsCounter += 1;

        if (croppedItemsCounter == mSelectedCollection.getItems().size()) {
            saveStates();


            Intent intentResult = new Intent();
            intentResult.putStringArrayListExtra(EXTRA_RESULT_ORIGINAL_PATH, context.originalPathName);
            Log.d("sssssssssss", mediaAspectRatio + "");

            intentResult.putExtra(CROP_ASPECT_RATIO, mediaAspectRatio);
            intentResult.putExtra(IS_EXPANDED, isExpanded);
            intentResult.putExtra(FRAGMENTS_STATUS, CropperPickerActivity.toJsonArray(context.getCreatedFragmentsState()));

            intentResult.putExtra(MEDIA_MODE, mode);

            intentResult.putExtra(MEDIA_ITEMS, MediaItem.toJsonArray(resultItems));

            if (isAdded() && getActivity() != null) {
                ((CropperPickerActivity) getActivity()).hideProgressDialog();
                getActivity().setResult(RESULT_OK, intentResult);
                getActivity().finish();
            }
        }


    }


    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        String reformattedUri = reFormatPath(resultUri.getPath());
        resultItems.add(new MediaItem(reformattedUri, true, false, false));

        returnPickerResult();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        uCrop = uCrop.useSourceImageAspectRatio();

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setShowCropGrid(false);

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);

        float[] vals = ((CropperPickerActivity) getActivity()).getCreatedFragmentsState(lastSelectedUri);
        if (vals != null) {
            options.AddOldMatrix(vals[0], vals[1], vals[2],

                    vals[3], vals[4], vals[5],

                    vals[6], vals[7], vals[8]);

        }


//        options.setMaxBitmapSize(10000);
//        options.withMaxResultSize(1000, 1000);
//        options.setFreeStyleCropEnabled(true);
//        options.setShowCropFrame(false);
//        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);

        return uCrop.withOptions(options);
    }


    public static Bitmap scaleImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

}