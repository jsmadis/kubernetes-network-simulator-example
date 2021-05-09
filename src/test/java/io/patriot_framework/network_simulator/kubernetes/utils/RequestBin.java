package io.patriot_framework.network_simulator.kubernetes.utils;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Class representing request bin object of https://requestbin.io website.
 * Request bin is used for webhook testing and in the scenario provides a service
 * where we can send HTTP requests and collect them.
 */
public class RequestBin {
    private static final String REQUEST_BIN_URL = "https://requestbin.io";
    private static final String API_SUFFIX = "/api/v1/bins";
    private final Gson gson = new Gson();
    OkHttpClient client = new OkHttpClient.Builder().build();
    private String name;

    public RequestBin() throws IOException {
        this.name = createRequestBinInstance();
    }

    public String url() {
        return REQUEST_BIN_URL + "/" + name;
    }


    /**
     * Returns list of received webhooks
     * @return List of RequestBinResults
     */
    public List<RequestBinResult> getLatestResults() throws IOException {
        Request request = new Request
                .Builder()
                .get()
                .url(String.format("%s%s/%s/requests", REQUEST_BIN_URL, API_SUFFIX, name ))
                .build();
        String responseData;
        try (Response response = client.newCall(request).execute()) {
            responseData = Objects.requireNonNull(response.body()).string();
        }
        RequestBinResult[] results =
                gson.fromJson(responseData, RequestBinResult[].class);

        return new ArrayList<>(Arrays.asList(results));
    }

    private String createRequestBinInstance() throws IOException {
        Request request = new Request
                .Builder()
                .post(RequestBody.create(HttpClient.JSON, ""))
                .url(REQUEST_BIN_URL + API_SUFFIX)
                .build();


        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            RequestBinEntity entity =
                    gson.fromJson(Objects.requireNonNull(responseBody).string(), RequestBinEntity.class);

            return entity.getName();
        }
    }

    private class RequestBinEntity {
        private String name;

        public RequestBinEntity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
