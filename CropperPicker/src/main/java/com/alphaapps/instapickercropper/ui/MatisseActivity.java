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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alphaapps.instapickercropper.MessageEvent;
import com.alphaapps.instapickercropper.R;
import com.alphaapps.instapickercropper.internal.entity.Album;
import com.alphaapps.instapickercropper.internal.entity.Item;
import com.alphaapps.instapickercropper.internal.entity.SelectionSpec;
import com.alphaapps.instapickercropper.internal.model.AlbumCollection;
import com.alphaapps.instapickercropper.internal.model.MediaItem;
import com.alphaapps.instapickercropper.internal.model.SelectedItemCollection;
import com.alphaapps.instapickercropper.internal.ui.MediaSelectionFragment;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumMediaAdapter;
import com.alphaapps.instapickercropper.internal.ui.adapter.AlbumsAdapter;
import com.alphaapps.instapickercropper.internal.ui.widget.AlbumsSpinner;
import com.alphaapps.instapickercropper.internal.ui.widget.CheckRadioView;
import com.alphaapps.instapickercropper.internal.utils.MediaStoreCompat;
import com.alphaapps.instapickercropper.ui.ActionDialog.ActionDialog;
import com.alphaapps.instapickercropper.ui.ActionDialog.DialogOption;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.alphaapps.instapickercropper.SelectionCreator.IS_SINGLE_IMAGE_KEY;
import static com.alphaapps.instapickercropper.SelectionCreator.RECENT_URI_IMAGE_KEY;
import static com.alphaapps.instapickercropper.SelectionCreator.SHOW_Camera_ICON;


/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class MatisseActivity extends AppCompatActivity implements
        AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener,
        MediaSelectionFragment.SelectionProvider, View.OnClickListener,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener,
        AlbumMediaAdapter.OnPhotoCapture, UCropFragmentCallback {

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    private static final int REQUEST_CODE_PREVIEW = 23;
    private static final int REQUEST_CODE_CAPTURE = 24;
    public static final String CHECK_STATE = "checkState";
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;
    private SelectedItemCollection mSelectedCollection = SelectedItemCollection.getInstance();
    private SelectionSpec mSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;
    private TextView mButtonPreview;
    private TextView mButtonApply;
    private View mContainer;
    private View mEmptyView;

    private LinearLayout mOriginalLayout;
    private CheckRadioView mOriginal;
    private boolean mOriginalEnable;


    ////////////////
    public static final String EXTRA_RESULT_ORIGINAL_PATH = "extra_result_selection_path";
    public static final String CROP_ASPECT_RATIO = "CROP_ASPECT_RATIO";
    public static final String MEDIA_MODE = "MEDIA_MODE";
    public static final String MEDIA_ITEMS = "MEDIA_ITEMS";
    public static final String IS_EXPANDED = "IS_EXPANDED";


    private HashMap<String, float[]> createdFragmentsState = new HashMap<>();
    public ArrayList<String> originalPathName = new ArrayList<>();
    ArrayList<MediaItem> resultItems = new ArrayList<>();

    int croppedItemsCounter = 0;
    boolean isExpanded = false, isMultiple = false;
    public int viewHeight = 0;
    RectF mCrop = new RectF();
    boolean showCameraIcon = false;
    FrameLayout continaer;
    ImageView remove_imgIV;
    ImageView changeImgSizeIV;
    String lastSelectedUri = null;
    TextView nextTV;
    TextView backTV;
    ImageView openCamera;
    TextView albumNameTV;
    RecyclerView mediaThumbnailsRV;
    //    MediaThumbAdapter mediaThumbAdapter;
    AppBarLayout appBarLayout;
    final Handler handler = new Handler();
    UCropFragment fragment;
    FrameLayout cropperContainer;
    Random randomNumber;
    FragmentManager manager;
    ProgressDialog mProgressDialog;
    boolean isSingleImage = false;
    public ArrayList<Uri> recentImageUri = new ArrayList<>();
    float mediaAspectRatio;
    int mode = 0; // 0 -> Landscape 1-> portrait


    ActionDialog dialog;
    ActionDialog.OnClickListener actionDialogListener = new ActionDialog.OnClickListener() {
        @Override
        public void click(int action) {
            if (action == DialogOption.ACTION_PROCEED) {
                showProgressDialog();
                cropItemsSet();
            }
        }
    };


    ////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // programmatically set theme before super.onCreate()
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        super.onCreate(savedInstanceState);
//        if (!mSpec.hasInited) {
//            setResult(RESULT_CANCELED);
//            finish();
//            return;
//        }
        setContentView(R.layout.fragment_cropper_picker);

        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
            if (mSpec.captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        Drawable navigationIcon = toolbar.getNavigationIcon();
//        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
//        int color = ta.getColor(0, 0);
//        ta.recycle();
//        navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//
//        mButtonPreview = (TextView) findViewById(R.id.button_preview);
//        mButtonApply = (TextView) findViewById(R.id.button_apply);
//        mButtonPresetOnClickListener(this);
//        mButtonApply.setOnClickListener(this);
//        mContainer = findViewById(R.id.container);
//        mEmptyView = findViewById(R.id.empty_view);
//        mOriginalLayout = findViewById(R.id.originalLayout);
//        mOriginal = findViewById(R.id.original);
//        mOriginalLayout.setOnClickListener(this);

        mSelectedCollection.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        updateBottomToolbar();

        mAlbumsAdapter = new AlbumsAdapter(this, null, false);
        mAlbumsSpinner = new AlbumsSpinner(this);
        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView((TextView) findViewById(R.id.albumNameTV));
        mAlbumsSpinner.setPopupAnchorView(findViewById(R.id.toolbar));
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();
////////////////////////////

        randomNumber = new Random();
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        manager = getSupportFragmentManager();

        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }
        List<DialogOption> options = new ArrayList();
        options.add(new DialogOption(getString(R.string.proceed), getResources().getColor(R.color.black), DialogOption.ACTION_PROCEED));
        options.add(new DialogOption(getString(R.string.cancel), getResources().getColor(R.color.red), DialogOption.ACTION_CANCEL));
        dialog = ActionDialog.getInstance(options, actionDialogListener, ActionDialog.LENGTH_LONG, this, Color.WHITE);

        isSingleImage = getIntent().getBooleanExtra(IS_SINGLE_IMAGE_KEY, false);
        recentImageUri = (ArrayList<Uri>) getIntent().getSerializableExtra(RECENT_URI_IMAGE_KEY);

        showCameraIcon = getIntent().getBooleanExtra(SHOW_Camera_ICON, false);

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
            if (mSpec.captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }
        cropperContainer = findViewById(R.id.cropperContainer);

        cropperContainer.setClipToOutline(true);

        remove_imgIV = findViewById(R.id.remove_imgIV);
        changeImgSizeIV = findViewById(R.id.expand_collapseIV);

        remove_imgIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deSelectItem();
            }
        });


        changeImgSizeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isExpanded) {
                    changeImgSizeIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand));
                } else {
                    changeImgSizeIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_collapse));
                }
                isExpanded = !isExpanded;
                getRatio(lastSelectedUri);

                changeMediaSize(isExpanded);

            }
        });
//        rotateIV = findViewById(R.id.rotateIV);
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        ta.recycle();
        mContainer = findViewById(R.id.container);
        mEmptyView = findViewById(R.id.empty_view);
        albumNameTV = findViewById(R.id.albumNameTV);

        View collapseViewFL = findViewById(R.id.albumsAnchorFL);
        nextTV = findViewById(R.id.nextTV);
        nextTV.setEnabled(true);
        backTV = findViewById(R.id.backTV);
        openCamera = findViewById(R.id.openCamera);
        appBarLayout = findViewById(R.id.appbar);
        mediaThumbnailsRV = findViewById(R.id.mediaThumbnailsRV);
//        mediaThumbnailsRV.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
//        mediaThumbAdapter = new MediaThumbAdapter(this);
//        mediaThumbnailsRV.setAdapter(mediaThumbAdapter);

        continaer = (FrameLayout) mContainer;
        mSelectedCollection.onCreate(savedInstanceState);
        mAlbumsAdapter = new AlbumsAdapter(this, null, false);
        mAlbumsSpinner = new AlbumsSpinner(this);

        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView(albumNameTV);
        mAlbumsSpinner.setPopupAnchorView(collapseViewFL);
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        mAlbumCollection.onCreate(this, this);
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
                openCamera.setVisibility(GONE);
                nextTV.setVisibility(VISIBLE);
                backTV.setText(R.string.cancel);
            }
        } else {
            if (showCameraIcon)
                openCamera.setVisibility(VISIBLE);
            else
                openCamera.setVisibility(GONE);

            nextTV.setVisibility(GONE);
            backTV.setText(R.string.close);

        }

        //set the height of the image frame as rectangle (the width & height = screen width)
        setCropFrameSize();

        //end height setting

//        cropperContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight));
        cropperContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (cropperContainer.getChildAt(0) instanceof SimpleExoPlayerView) {
//                    SimpleExoPlayerView view1 = (SimpleExoPlayerView) cropperContainer.getChildAt(0);
//                    view1.getPlayer().setPlayWhenReady(!view1.getPlayer().getPlayWhenReady());
//                }
            }
        });

        nextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTV.setEnabled(false);
                if (mSelectedCollection.getItems().size() == 0) {
                    displayCustomToast(getString(R.string.error_no_count_default));
                    return;
                }
                showProgressDialog();

                cropItemsSet();

//                if (!(cropperContainer.getChildAt(0) instanceof SimpleExoPlayerView)) { // the view is an image
//                    CropperView cropperView = cropperContainer.findViewById(R.id.cropperView);
//                    if (cropperisCropping() || cropperisAdjusting() || cropperisEditing()) {
//                        displayCustomToast(getString(R.string.error_cannot_proceed_while_cropping));
//                        return;
//                    }
//                }

            }
        });
        backTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
            layoutParams.height = findViewById(R.id.maintoolbar).getLayoutParams().height;
            appBarLayout.setLayoutParams(layoutParams);
        }
        ///////////////
        cropperContainer = (FrameLayout) findViewById(R.id.cropperContainer);
        //////////
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
        outState.putBoolean("checkState", mOriginalEnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumCollection.onDestroy();
//        mSpec.onCheckedListener = null;
//        mSpec.onSelectedListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_PREVIEW) {
//            Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
//            ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
//            mOriginalEnable = data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
//            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
//                    SelectedItemCollection.COLLECTION_UNDEFINED);
//            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
//                Intent result = new Intent();
//                ArrayList<Uri> selectedUris = new ArrayList<>();
//                ArrayList<String> selectedPaths = new ArrayList<>();
//                if (selected != null) {
//                    for (Item item : selected) {
//                        selectedUris.add(item.getContentUri());
//                        selectedPaths.add(PathUtils.getPath(this, item.getContentUri()));
//                    }
//                }
//                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
//                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
//                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//                setResult(RESULT_OK, result);
//                finish();
//            } else {
//                mSelectedCollection.overwrite(selected, collectionType);
//                Fragment mediaSelectionFragment = getSupportFragmentManager().findFragmentByTag(
//                        MediaSelectionFragment.class.getSimpleName());
//                if (mediaSelectionFragment instanceof MediaSelectionFragment) {
//                    ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
//                }
//                updateBottomToolbar();
//            }
//        } else if (requestCode == REQUEST_CODE_CAPTURE) {
//            // Just pass the data back to previous calling Activity.
//            Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
//            String path = mMediaStoreCompat.getCurrentPhotoPath();
//            ArrayList<Uri> selected = new ArrayList<>();
//            selected.add(contentUri);
//            ArrayList<String> selectedPath = new ArrayList<>();
//            selectedPath.add(path);
//            Intent result = new Intent();
//            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
//            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
//            setResult(RESULT_OK, result);
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//                MatisseActivity.this.revokeUriPermission(contentUri,
//                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            finish();
        }
    }

    private void updateBottomToolbar() {
//
//        int selectedCount = mSelectedCollection.count();
//        if (selectedCount == 0) {
//            mButtonPresetEnabled(false);
//            mButtonApply.setEnabled(false);
//            mButtonApply.setText(getString(R.string.button_sure_default));
//        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
//            mButtonPresetEnabled(true);
//            mButtonApply.setText(R.string.button_sure_default);
//            mButtonApply.setEnabled(true);
//        } else {
//            mButtonPresetEnabled(true);
//            mButtonApply.setEnabled(true);
//            mButtonApply.setText(getString(R.string.button_sure, selectedCount));
//        }
//
//
//        if (mSpec.originalable) {
//            mOriginalLayout.setVisibility(VISIBLE);
//            updateOriginalState();
//        } else {
//            mOriginalLayout.setVisibility(INVISIBLE);
//        }
//
//
    }


    private void updateOriginalState() {
//        mOriginal.setChecked(mOriginalEnable);
//        if (countOverMaxSize() > 0) {
//
//            if (mOriginalEnable) {
//                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
//                        getString(R.string.error_over_count, mSpec.originalMaxSize));
//                incapableDialog.show(getSupportFragmentManager(),
//                        IncapableDialog.class.getName());
//
//                mOriginal.setChecked(false);
//                mOriginalEnable = false;
//            }
//        }
    }


    private int countOverMaxSize() {
        int count = 0;
//        int selectedCount = mSelectedCollection.count();
//        for (int i = 0; i < selectedCount; i++) {
//            Item item = mSelectedCollection.asList().get(i);
//
//            if (item.isImage()) {
//                float size = PhotoMetadataUtils.getSizeInMB(item.size);
//                if (size > mSpec.originalMaxSize) {
//                    count++;
//                }
//            }
//        }
        return count;
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
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                mAlbumsSpinner.setSelection(MatisseActivity.this,
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
        mAlbumsAdapter.swapCursor(null);
    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(GONE);
            mEmptyView.setVisibility(VISIBLE);
        } else {
            mContainer.setVisibility(VISIBLE);
            mEmptyView.setVisibility(GONE);
            Fragment fragment = MediaSelectionFragment.newInstance(album);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar();

//        if (mSpec.onSelectedListener != null) {
//            mSpec.onSelectedListener.onSelected(
//                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString(this));
//        }
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
//        Intent intent = new Intent(this, AlbumPreviewActivity.class);
//        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
//        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
//        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }

    @Override
    public void capture() {
        if (mMediaStoreCompat != null) {
            mMediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
        }
    }

    ////////////////////////


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCropperPickerMessageEvent(MessageEvent event) {

        if (event.getAction() != null) {
            if (originalPathName == null)
                originalPathName = new ArrayList<>();
            if (!originalPathName.contains(event.getAction()))
                originalPathName.add(event.getAction());

            lastSelectedUri = event.getAction();
            if (isSingleImage) {
                singleItemSelected(false, event.getAction());
            } else {
                ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
                layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                appBarLayout.setLayoutParams(layoutParams);
//            stopVideo();
                if (event.getAction() != null) {
                    appBarLayout.setExpanded(true);

                    if (showCameraIcon) {
                        openCamera.setVisibility(View.GONE);
                        nextTV.setVisibility(View.VISIBLE);
                    }

                    if (!event.isOld())
                        createNewCroppingFrame(Uri.parse(event.getAction()));
                    else
                        retrieveOldCroppingFrame(Uri.parse(event.getAction()));
                }
                if (mSelectedCollection.getItems().size() == 1) {
                    remove_imgIV.setVisibility(View.GONE);
                    changeImgSizeIV.setVisibility(View.VISIBLE);
                    getRatio(lastSelectedUri);
                    isMultiple = false;
                    if (fragment.mGestureCropImageView != null)
                        fragment.changeIsMultiple(isMultiple);
                } else {
                    remove_imgIV.setVisibility(View.VISIBLE);
                    changeImgSizeIV.setVisibility(View.GONE);
                    isMultiple = true;
                    if (fragment.mGestureCropImageView != null)
                        fragment.changeIsMultiple(isMultiple, mCrop);
                }
//
                if (mediaAspectRatio <= 1.05f && mediaAspectRatio >= 0.95f) //hide changeImgSizeIV for squared img
                    changeImgSizeIV.setVisibility(View.GONE);

            }
        }
    }

    void retrieveOldCroppingFrame(Uri selectedUri) {
        for (Item item : mSelectedCollection.getItems()) {
            if (!item.getContentUri().equals(selectedUri)) {

                //save the state of the value before rome it
                UCropFragment oldFragment = ((UCropFragment) getSupportFragmentManager().findFragmentByTag(item.getContentUri().toString()));
                oldFragment.mGestureCropImageView.setIsOldFragment(true, oldFragment.mGestureCropImageView.getCurrentImageMatrix());

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .hide(oldFragment)
                        .commit();
            }

        }

        fragment = (UCropFragment) getSupportFragmentManager().findFragmentByTag(selectedUri.toString());
        fragment.mGestureCropImageView.setIsOldFragment(true, fragment.mGestureCropImageView.getCurrentImageMatrix());

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(fragment)
                .commit();
    }


    void createNewCroppingFrame(Uri selectedUri) {

        String destinationFileName = System.currentTimeMillis() + ".jpg";
        UCrop uCrop = UCrop.of(selectedUri, Uri.fromFile(new File(this.getCacheDir(), destinationFileName)));

        uCrop = advancedConfig(uCrop);

        fragment = uCrop.getFragment(uCrop.getIntent(this).getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.cropperContainer, fragment, selectedUri.toString())
//                .commitAllowingStateLoss();
                .commitNowAllowingStateLoss();
    }

    void setCropFrameSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        viewHeight = Math.round((screenWidth - marginInPx()));

        //set the height of the image frame as rectangle (the width & height = screen width)

        cropperContainer.setMinimumHeight(viewHeight);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cropperContainer.getLayoutParams();
        params.height = viewHeight;
        cropperContainer.setLayoutParams(params);
        Log.d("###", viewHeight + "");

    }

    int marginInPx() {
        int marginInPx = 0;
        try {
            float density = this.getResources().getDisplayMetrics().density;
            marginInPx = Math.round((float) 32 * density);
        } catch (Exception ignored) {
        }
        return marginInPx;
    }


    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        uCrop = uCrop.useSourceImageAspectRatio();

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setShowCropGrid(false);

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);


        return uCrop.withOptions(options);

//        float[] vals = ((CropperPickerActivity) this).getCreatedFragmentsState(lastSelectedUri);
//        if (vals != null) {
//            options.AddOldMatrix(vals[0], vals[1], vals[2],
//
//                    vals[3], vals[4], vals[5],
//
//                    vals[6], vals[7], vals[8]);
//
//        }

    }

    @Override
    public void loadingProgress(boolean showLoader) {

    }

    @Override
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
            Log.e("### error", "handleCropError: ", cropError);
        } else {
        }
    }

    public static String reFormatPath(String unformattedPath) {
        String extraString = "file://";
        if (unformattedPath.contains(extraString)) {
            return unformattedPath.replace(extraString, "");
        }
        return unformattedPath;
    }


    private void handleCropResult(@NonNull Intent result) {

        final Uri resultUri = UCrop.getOutput(result);
        String reformattedUri = reFormatPath(resultUri.getPath());
        resultItems.add(new MediaItem(reformattedUri, true, false, false));

        returnPickerResult();

    }

    void returnPickerResult() {
        croppedItemsCounter += 1;

        if (croppedItemsCounter == originalPathName.size()) {

            Intent intentResult = new Intent();
            intentResult.putStringArrayListExtra(EXTRA_RESULT_ORIGINAL_PATH, originalPathName);
            intentResult.putExtra(CROP_ASPECT_RATIO, mediaAspectRatio);
            intentResult.putExtra(IS_EXPANDED, isExpanded);
            intentResult.putExtra(MEDIA_MODE, mode);

            intentResult.putExtra(MEDIA_ITEMS, MediaItem.toJsonArray(resultItems));

            hideProgressDialog();
            setResult(RESULT_OK, intentResult);
            finish();

        }


    }

    @Override
    public void setCreatedFragmentsState(String s, float[] vals) {
        createdFragmentsState.put(s, vals);


    }

    @Override
    public float[] getCreatedFragmentsState(String s) {
        return createdFragmentsState.get(s);
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
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mProgressDialog.show();
                mProgressDialog.getWindow().setLayout(getPXSize(124), getPXSize(124));
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


    public void displayCustomToast(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView tvToastText = (TextView) layout.findViewById(R.id.tvToastText);
        tvToastText.setText(message);

        ImageView ivToastImage = (ImageView) layout.findViewById(R.id.ivToastImage);

        ivToastImage.setColorFilter(ContextCompat.getColor(this, R.color.black));
        ivToastImage.setImageResource(R.drawable.warning);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 90);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        try {
            toast.show();
        } catch (Exception e) {
            Log.e("Toast Exception", e.getMessage());
        }
    }

    void getRatio(String imgPath) {
        Bitmap bitmap;
        try {
            int w, h;

            if (imgPath.toString().contains("video")) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                Bitmap bmp = null;

                retriever.setDataSource(this, Uri.parse(imgPath));
                bmp = retriever.getFrameAtTime();
                h = bmp.getHeight();
                w = bmp.getWidth();

//                View view = getLayoutInflater().inflate(R.layout.exoplayer_view, cropperContainer, false);
//                view.setLayoutParams(new ViewGroup.LayoutParams(w, h));
//                VideoItemVH videoItemVH = new VideoItemVH(view, Uri.parse(imgPath));
//                CroppingViewHolder croppingVH = new CroppingViewHolder(videoItemVH, imgPath);
//                croppingVH.getVideoView().getSimpleExoPlayerView().release();
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imgPath));
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

            }
        }
    }


    void deselect(Item item) {
        mSelectedCollection.setLastSelectedItem(null);
        mSelectedCollection.remove(item);

        //remove the path from the original selected paths
        originalPathName.remove(item.getContentUri().toString());
//        mediaSelectionFragment.refreshSelection();
//        mediaSelectionFragment.refreshMediaGrid();
    }

    public void cropItemsSet() {
        if (mSelectedCollection.getItems().isEmpty())
            return;
//        stopVideo();
////        releaseVideo();
//        selectedPaths = new ArrayList<>();
//        resultItems = new ArrayList<>();
//        keys = new ArrayList<>(mSelectedCollection.croppingItems.keySet());
        cropItem();
    }


    private void cropItem() {

        Log.d("#### finally the size", "" + originalPathName.size());
        for (String s : originalPathName) {
            if (!isVideo(s)) {
                UCropFragment uCropFragment = (UCropFragment) getSupportFragmentManager().findFragmentByTag(s);
                cropImageAsync(uCropFragment);
            } else {
//                croppingViewHolder.getVideoView().getSimpleExoPlayerView().release();
//                cropVideoAsync(croppingViewHolder);
            }
        }
    }

    private void cropImageAsync(UCropFragment f) {
        f.cropAndSaveImage(); // then call onCropFinish
    }

    boolean isVideo(String s) {
        if (s.contains("video"))
            return true;
        else return false;
    }


    private void changeMediaSize(boolean isExpanded) {

//        if (lastURLIsImg())
//            uCropFragment.changeExpandMode(isExpanded);
//        else {
//
//        }
    }

    public int getPXSize(int dp) {
        int px = dp;
        try {
            float density = getResources().getDisplayMetrics().density;
            px = Math.round((float) dp * density);
        } catch (Exception ignored) {
        }
        return px;
    }

    public void singleItemSelected(boolean backToCamera, String imgPath) {
    }
}
