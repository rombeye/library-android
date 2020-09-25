package com.linkedin.android.spyglass.ui;

/**
 * Created by Abdulaziz on 9/27/17.
 */

class Hashtag {
    String text;
    Integer startsAt;
    Integer endsAt;

    public Hashtag() {
    }

    public Hashtag(String text, Integer startsAt, Integer endsAt) {
        this.text = text;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Integer startsAt) {
        this.startsAt = startsAt;
    }

    public Integer getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Integer endsAt) {
        this.endsAt = endsAt;
    }

}


