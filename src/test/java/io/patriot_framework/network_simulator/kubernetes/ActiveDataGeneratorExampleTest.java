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
import io.patriot_framework.network_simulator.kubernetes.device.KubeDevice;
import io.patriot_framework.network_simulator.kubernetes.network.KubeNetwork;
import io.patriot_framework.network_simulator.kubernetes.utils.RequestBin;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActiveDataGeneratorExampleTest extends AbstractTest{
    @Test
    public void createActiveDataGeneratorDeviceTest() throws IOException, InterruptedException {
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
}
