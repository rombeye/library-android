package com.alphaapps.instapickercropper.ui.ActionDialog;

/**
 * Created by dali on 1/11/2018.
 */

public class DialogOption {
    public static final int ACTION_CANCEL = 1;
    public static final int ACTION_PROCEED = 2;


    String optionText;
    int textColor;
    int action;

    /**
     * Object holder for options of Action dialog.
     *
     * @param optionText represents option text.
     * @param textColor
     * @param action     returned by click listener to handle specific option.
     */
    public DialogOption(String optionText, int textColor, int action) {
        this.optionText = optionText;
        this.textColor = textColor;
        this.action = action;
    }


    public String getOptionText() {
        return optionText;
    }

    public DialogOption setOptionText(String optionText) {
        this.optionText = optionText;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public DialogOption setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getAction() {
        return action;
    }

    public DialogOption setAction(int action) {
        this.action = action;
        return this;
    }

}
