package io.patriot_framework.network_simulator.kubernetes;

import io.patriot_framework.generator.dataFeed.ConstantDataFeed;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.dataFeed.NormalDistVariateDataFeed;
import io.patriot_framework.generator.device.Device;
import io.patriot_framework.generator.device.active.Active;
import io.patriot_framework.generator.device.active.ActiveDevice;
import io.patriot_framework.generator.device.impl.basicSensors.Thermometer;
import io.patriot_framework.generator.network.NetworkAdapter;
import io.patriot_framework.generator.network.Rest;
import io.patriot_framework.generator.network.wrappers.JSONWrapper;
import io.patriot_framework.network_simulator.kubernetes.device.ActiveDataGenerator;
import io.patriot_framework.network_simulator.kubernetes.device.Application;
import io.patriot_framework.network_simulator.kubernetes.device.DeviceConfig;
import io.patriot_framework.network_simulator.kubernetes.device.KubeDevice;
import io.patriot_framework.network_simulator.kubernetes.exceptions.KubernetesSimulationException;
import io.patriot_framework.network_simulator.kubernetes.network.KubeNetwork;
import io.patriot_framework.network_simulator.kubernetes.utils.RequestBin;
import io.patriot_framework.network_simulator.kubernetes.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActiveDataGeneratorExampleTest extends AbstractTest {
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    @Test
    public void createActiveDataGeneratorDeviceTest() throws IOException, InterruptedException, KubernetesSimulationException {
        // Creates request bin instance which is used for testing webhooks
        RequestBin requestBin = new RequestBin();

        KubeNetwork network = new KubeNetwork("network123");
        controller.createNetwork(network);

        DataFeed df = new NormalDistVariateDataFeed(18, 2);
        Device temperature = new Thermometer("thermometer", df);
        NetworkAdapter na = new Rest(requestBin.url(), new JSONWrapper());
        temperature.setNetworkAdapter(na);

        DataFeed tf = new ConstantDataFeed(2000);
        Active activeDevice = new ActiveDevice(temperature, tf);

        KubeDevice kubeDevice = new ActiveDataGenerator("simple-thermometer", network, activeDevice);

        kubeDevice.getDeviceConfig().setEnableInternet(true);

        controller.deployDevice(kubeDevice);

        Thread.sleep(60000);

        // Asserts that request bin got HTTP calls and the number of calls
        // that contained the body with data (part of the string sent by the active device) is higher than 4
        assertTrue(requestBin
                .getLatestResults()
                .stream()
                .filter(result -> result
                        .getBody()
                        .contains("data"))
                .count() > 4);
    }

    @Test
    public void deployedActiveDataGeneratorIsAccessibleFromOtherNetwork() throws IOException, InterruptedException, KubernetesSimulationException {
        // Create network and deploy HTTP_STORAGE_CALLS_APP
        // which provides REST API to save and get http calls
        KubeNetwork anotherNetwork = new KubeNetwork("another-network");
        controller.createNetwork(anotherNetwork);

        DeviceConfig deviceConfig = new DeviceConfig(Utils.HTTP_STORAGE_CALLS_APP);
        KubeDevice storageApp = new Application("storage-app", anotherNetwork, deviceConfig);
        controller.deployDevice(storageApp);

        controller.deviceIsSeenBy(storageApp, Utils.getLocalIp());

        // Create network and deploy active data generator inside the network
        KubeNetwork deviceNetwork = new KubeNetwork("my-nice-network");
        controller.createNetwork(deviceNetwork);

        DataFeed df = new NormalDistVariateDataFeed(18, 2);
        Device temperature = new Thermometer("thermometer", df);
        // make active device send data to the storage app
        NetworkAdapter na = new Rest(privateUrl(storageApp), new JSONWrapper());
        temperature.setNetworkAdapter(na);

        DataFeed tf = new ConstantDataFeed(2000);
        Active activeDevice = new ActiveDevice(temperature, tf);

        KubeDevice kubeDevice = new ActiveDataGenerator("active-thermometer", deviceNetwork, activeDevice);
        controller.deployDevice(kubeDevice);

        controller.connectDevicesBothWays(kubeDevice, storageApp);

        Thread.sleep(60000);

        Request request = new Request.Builder().url(publicUrl(storageApp)).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertNotNull(response.body());
            assertTrue(response.body().string().contains("Data{data type=class java.lang.Double, data="));
        }

    }

    private String privateUrl(KubeDevice device) {
        return Utils.deviceToURL(device.getPrivateIpAddress(),
                device.getDeviceConfig().getManagementPort().getPort(),
                "/save");
    }

    private String publicUrl(KubeDevice device) {
        return Utils.deviceToURL(device.getPublicIpAddress(),
                device.getManagementPort().getPort(),
                "/get");
    }
}
