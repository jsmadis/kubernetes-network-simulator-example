package io.patriot_framework.network_simulator.kubernetes;

import io.patriot_framework.generator.controll.client.CoapControlClient;
import io.patriot_framework.generator.dataFeed.DataFeed;
import io.patriot_framework.generator.dataFeed.NormalDistVariateDataFeed;
import io.patriot_framework.generator.device.Device;
import io.patriot_framework.generator.device.impl.basicSensors.Thermometer;
import io.patriot_framework.network_simulator.kubernetes.device.DataGenerator;
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
}
