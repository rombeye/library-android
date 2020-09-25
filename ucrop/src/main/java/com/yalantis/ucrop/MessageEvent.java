package com.yalantis.ucrop;

public class MessageEvent {

    boolean  isExpanded=false;


    public MessageEvent(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public Boolean getIsExpanded () {
        return isExpanded;
    }


    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
