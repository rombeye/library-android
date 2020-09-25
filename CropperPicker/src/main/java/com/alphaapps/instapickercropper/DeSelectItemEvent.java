package com.alphaapps.instapickercropper;

import com.alphaapps.instapickercropper.internal.entity.Item;

public class DeSelectItemEvent {
    Item item;

    public DeSelectItemEvent(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
