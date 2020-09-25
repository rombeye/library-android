package com.yalantis.ucrop;

public interface UCropFragmentCallback {

    /**
     * Return loader status
     *
     * @param showLoader
     */
    void loadingProgress(boolean showLoader);

    /**
     * Return cropping result or error
     *
     * @param result
     */
    void onCropFinish(UCropFragment.UCropResult result);


    void setCreatedFragmentsState(String s, float[] vals);

    float[] getCreatedFragmentsState(String s);


}
