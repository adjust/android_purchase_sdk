package com.adjust.sdk.purchase;

/**
 * Created by uerceg on 04/12/15.
 */
public class ADJPMerchantItem {
    private String itemSku;
    private String itemToken;
    private String developerPayload;
    private OnADJPVerificationFinished callback;

    public ADJPMerchantItem(String itemSku, String itemToken, String developerPayload,
                            OnADJPVerificationFinished callback) {
        this.itemSku = itemSku;
        this.itemToken = itemToken;
        this.developerPayload = developerPayload;
        this.callback = callback;
    }

    public String getItemSku() {
        return this.itemSku;
    }

    public String getItemToken() {
        return this.itemToken;
    }

    public String getDeveloperPayload() {
        return this.developerPayload;
    }

    public OnADJPVerificationFinished getCallback() {
        return this.callback;
    }

    public boolean isValid() {
        if (this.itemSku == null) {
            ADJPLogger.getInstance().error("SKU not set");
            return false;
        }

        if (this.itemSku.isEmpty()) {
            ADJPLogger.getInstance().error("SKU not valid");
            return false;
        }

        if (this.itemToken == null) {
            ADJPLogger.getInstance().error("Token not set");
            return false;
        }

        if (this.itemToken.isEmpty()) {
            ADJPLogger.getInstance().error("Token not valid");
            return false;
        }

        if (this.developerPayload == null) {
            ADJPLogger.getInstance().error("Developer payload not set");
            return false;
        }

        if (this.developerPayload.isEmpty()) {
            ADJPLogger.getInstance().error("Developer payload not valid");
            return false;
        }

        return true;
    }
}
