package io.patriot_framework.network_simulator.kubernetes.utils;

/**
 * Represents once object retrieved from request bin.
 */
public class RequestBinResult {
    private String body;

    public RequestBinResult(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
