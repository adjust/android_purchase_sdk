package com.adjust.sdk.purchase;

/**
 * Created by uerceg on 09/12/15.
 */
public class ADJPConfig {
    private String appToken;
    private String environment;
    private ADJPLogLevel logLevel;

    public ADJPConfig(String appToken, String environment) {
        this.appToken = appToken;
        this.environment = environment;
        this.logLevel = ADJPLogLevel.INFO;
    }

    public String getAppToken() {
        return this.appToken;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public ADJPLogLevel getLogLevel() {
        return this.logLevel;
    }

    public void setLogLevel(ADJPLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isValid() {
        if (this.appToken == null) {
            ADJPLogger.getInstance().error("Invalid app token");
            return false;
        }

        if (this.appToken.length() != ADJPConstants.APP_TOKEN_SIZE) {
            ADJPLogger.getInstance().error("Invalid app token");
            return false;
        }

        if (this.environment == null) {
            ADJPLogger.getInstance().error("Invalid environment");
            return false;
        }

        if (this.environment.equalsIgnoreCase(ADJPConstants.ENVIRONMENT_SANDBOX) == false &&
                this.environment.equalsIgnoreCase(ADJPConstants.ENVIRONMENT_PRODUCTION) == false) {
            ADJPLogger.getInstance().error("Invalid environment");
            return false;
        }

        return true;
    }
}
