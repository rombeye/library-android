package com.alphaapps.instapickercropper;

/**
 * Created by Juad on 11/1/2017.
 */

public class MessageEvent {
    private String id, action;
    boolean isOld = false;

    public MessageEvent(String id, String action, boolean isOld) {
        this.id = id;
        this.action = action;
        this.isOld=isOld;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isOld() {
        return isOld;
    }
}
