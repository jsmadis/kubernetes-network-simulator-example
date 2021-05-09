package io.patriot_framework.network_simulator.kubernetes.utils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents class used for making HTTP requests.
 * It provides methods used in the testing of deployed devices
 */
public class HttpClient {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client =  new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();


    /**
     * Makes get request to the HTTP_COAP_TESTING_APP interface (specified by the url)
     * The interface requires hostname, port and path of deployed device and
     * it will make CoAP request and it will return the response of such call
     *
     * @param url URL of HTTP_COAP_TESTING_APP container
     * @param hostname hostname of deployed data generator
     * @param port exposed port for CoAP communication on the deployed data generator
     * @param path specific path where is deplyed data generator accessible with CoAP protocol
     * @return response body of request to the HTTP_COAP_TESTING_APP
     */
    public String get(String url, String hostname, int port, String path) throws IOException {
        JSONObject json = new JSONObject();
        json.put("hostname", hostname + ":" + port);
        json.put("path", path);
        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder().post(body).url(url).build();

        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }
}
