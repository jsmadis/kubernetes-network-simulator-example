package io.patriot_framework.network_simulator.kubernetes.utils;

public class Utils {
    public static final String HTTP_STORAGE_CALLS_APP = "jsmadis/http-calls-storage-app:latest";

    /**
     * Returns Kubernetes URL where to run the tests.
     *
     * @return kubernetes URL
     */
    public static String getKubernetesUrl() {
        String url = System.getenv("PATRIOT_KUBERNETES_URL");
        if (url == null || url.equals("")) {
            System.err.println("You need to specify ENV variable PATRIOT_KUBERNETES_URL with valid kubernetes URL");
            throw new IllegalArgumentException();
        }
        return url;
    }

    /**
     * Returns local IP address of machine,
     * it is used for deployed devices in order to make it possible to access them from this machine
     *
     * @return local IP address
     */
    public static String getLocalIp() {
        String ip = System.getenv("PATRIOT_LOCAL_IP_ADDR");
        if (ip == null || ip.equals("")) {
            System.err.println("You need to specify ENV variable PATRIOT_LOCAL_IP_ADDR" +
                    " with valid IP address of this PC");
            throw new IllegalArgumentException();
        }
        return ip;
    }

    public static String deviceToURL(String hostname, int port, String path) {
        return String.format("http://%s:%s%s", hostname, port, path);
    }
}
