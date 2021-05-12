package io.patriot_framework.network_simulator.kubernetes;

import io.patriot_framework.generator.controll.client.CoapControlClient;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.dataFeed.NormalDistVariateDataFeed;
import io.patriot_framework.generator.device.Device;
import io.patriot_framework.generator.device.impl.basicSensors.Thermometer;
import io.patriot_framework.network_simulator.kubernetes.device.Application;
import io.patriot_framework.network_simulator.kubernetes.device.DataGenerator;
import io.patriot_framework.network_simulator.kubernetes.device.DeviceConfig;
import io.patriot_framework.network_simulator.kubernetes.device.KubeDevice;
import io.patriot_framework.network_simulator.kubernetes.exceptions.KubernetesSimulationException;
import io.patriot_framework.network_simulator.kubernetes.network.KubeNetwork;
import io.patriot_framework.network_simulator.kubernetes.utils.Utils;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataGeneratorExampleTest extends AbstractTest {

    @Test
    public void deployDataGeneratorDeviceTest() throws ConnectorException, IOException, KubernetesSimulationException {
        // Create network and deploy DataGenerator inside it
        KubeNetwork network = new KubeNetwork("example-network");
        controller.createNetwork(network);

        DataFeed dataFeed = new NormalDistVariateDataFeed(18, 2);
        Device device = new Thermometer("simpleThermometer", dataFeed);

        KubeDevice kubeDevice = new DataGenerator("simple-thermometer", network, device);

        controller.deployDevice(kubeDevice);

        // If we want to communicate with deployed device,
        // we need to enable communication with IP address of this machine
        controller.deviceIsSeenBy(kubeDevice, Utils.getLocalIp());

        // Create CoapControlClient for deployed device
        CoapControlClient client = new CoapControlClient(
                String.format("coap://%s:%s",
                        kubeDevice.getPublicIpAddress(),
                        kubeDevice.getManagementPort().getPort()));

        // Assert that the device is accessible with CoAP protocol
        assertTrue(new String(client.get("/sensor/simpleThermometer").getPayload()).contains("Thermometer"));
    }

    @Test
    public void deployedDataGeneratorIsAccessibleFromOtherNetwork() throws IOException, InterruptedException, KubernetesSimulationException {
        // Create network and deploy data generator inside the network
        KubeNetwork deviceNetwork = new KubeNetwork("my-nice-network");
        controller.createNetwork(deviceNetwork);

        DataFeed dataFeed = new NormalDistVariateDataFeed(18, 2);
        Device device = new Thermometer("simpleThermometer", dataFeed);

        KubeDevice kubeDevice = new DataGenerator("simple-thermometer", deviceNetwork, device);
        controller.deployDevice(kubeDevice);

        // Create another network and deploy HTTP_COAP_TESTING_APP
        // which provides REST API and makes CoAP request to given address
        KubeNetwork anotherNetwork = new KubeNetwork("another-network");
        controller.createNetwork(anotherNetwork);

        DeviceConfig deviceConfig = new DeviceConfig(Utils.HTTP_COAP_TESTING_APP_IMAGE);
        KubeDevice app = new Application("my-app2", anotherNetwork, deviceConfig);
        controller.deployDevice(app);

        controller.deviceIsSeenBy(app, Utils.getLocalIp());

        controller.connectDevicesBothWays(kubeDevice, app);

        Thread.sleep(5000);

        // Make HTTP request to HTTP_COAP_TESTING_APP which is deployed inside simulated network
        // The app will make CoAP request based on the parameters and returns response of the request
        String hostname = Utils.httpTestingHostname(app, "/get");
        String result = httpClient.get(hostname,
                kubeDevice.getPrivateIpAddress(),
                5683,
                "/sensor/simpleThermometer");

        // Asserts that the response was successful and the DataGenerator replied with data
        assertTrue(result.contains("Thermometer"), result);
    }
}
