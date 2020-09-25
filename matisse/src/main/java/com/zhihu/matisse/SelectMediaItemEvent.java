package com.zhihu.matisse;

import com.zhihu.matisse.internal.model.SelectedItemCollection;

public class SelectMediaItemEvent {
    private SelectedItemCollection mSelectedCollection ;


    public SelectMediaItemEvent(SelectedItemCollection mSelectedItemCollection) {
        this.mSelectedCollection = mSelectedItemCollection;
    }

    public  SelectedItemCollection  getSelectedItemCollection() {
        return mSelectedCollection;
    }

    public void setSelectedItemCollection(SelectedItemCollection mSelectedCollection) {
        this.mSelectedCollection = mSelectedCollection;
    }


}
