package com.adjust.sdk.purchase;

import java.util.HashMap;

/**
 * Created by uerceg on 03/12/15.
 */
public class ADJPVerificationPackage {
    private HashMap<String, String> parameters;
    private OnADJPVerificationFinished callback;

    public ADJPVerificationPackage(HashMap<String, String> parameters,
                                   OnADJPVerificationFinished callback) {
        this.callback = callback;
        this.parameters = parameters;
    }

    public HashMap<String, String> getParameters() {
        return this.parameters;
    }

    public OnADJPVerificationFinished getCallback() {
        return this.callback;
    }
}
