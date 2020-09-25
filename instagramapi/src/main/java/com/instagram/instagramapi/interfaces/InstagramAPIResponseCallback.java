package com.instagram.instagramapi.interfaces;

import com.instagram.instagramapi.exceptions.InstagramException;
import com.instagram.instagramapi.objects.IGPagInfo;

/**
 * Created by Sayyam on 3/18/16.
 */
public interface InstagramAPIResponseCallback<T> {

    public void onResponse(T responseObject, IGPagInfo pageInfo);

    public void onFailure(InstagramException exception);
}
