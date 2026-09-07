package com.exchange.me_core.config;

public class Environment {
    public enum ENV {
        PROD, STAGING, LOCAL
    }

    private static Environment instance;
    private final ENV env;

    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }

        return instance;
    }

    private Environment() {
        env = ENV.LOCAL;
        loadProperties();
    }

    public ENV getEnv() {
        return env;
    }

    private void loadProperties() {
        //Todo: load application & os properties;
    }

    public String getConfig(String key) {
        throw new RuntimeException("Not implemented yet");
    }

    public Boolean getBoolConfig(String key) {
        throw new RuntimeException("Not implemented yet");
    }

    public int getIntConfig(String key) {
        throw new RuntimeException("Not implemented yet");
    }

    public long getLongConfig(String key) {
        throw new RuntimeException("Not implemented yet");
    }
}
