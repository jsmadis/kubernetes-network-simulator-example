# kubernetes-network-simulator-example
Example project showing usage of network simulation in the Kubernetes of the PatrIoT Framework.

This is the list of installed software you'll need to run this example project.

* Java in version 8 or higher
* Maven
* Docker
* Minikube
* kubectl

## Environment setup
Network simulation in the Kubernetes uses the operator, which provides an interface to simulate a network. 
To simulate the network inside Kubernetes, the kubernetes need to be running with the CNI plugin.
In this example, we are using the Calico plugin. Please read the quick start for [Calico on minikube](https://docs.projectcalico.org/getting-started/kubernetes/minikube).

Start minikube server with the calico plugin.
```shell
$ minikube start --network-plugin=cni --cni=calico
```

Pull the repository with the operator for simulating network in the Kubernetes:
```shell
$ git clone https://github.com/PatrIoT-Framework/kubernetes-network-simulator-operator.git
$ cd kubernetes-network-simulator-operator
```

Start the operator:
```shell
$ make install && make run
```

It will take a couple of minutes to start all necessary services in the Kubernetes cluster (especially the calico objects).


You can check if everything is up and running with the following command:
```shell
$ kubectl get pods --all-namespaces
```


## Running the tests
To run the tests, you need to specify 2 environment variables necessary for the testing.

* **PATRIOT_KUBERNETES_URL** - specifies the URL of the kubernetes cluster
* **PATRIOT_LOCAL_IP_ADDR** - specifies the local IP address of your machine, where you are running those tests.

You can easily export the **PATRIOT_KUBERNETES_URL** with the following command:
```shell
$ export PATRIOT_KUBERNETES_URL="https://$(minikube ip):8443"
```

Since the simulation blocks traffic to the deployed devices,
we need to specify the machine's IP address from which we are running the tests.
The tests are communicating with the deployed devices.

If you are running minikube and using calico CNI, you can easily export the **PATRIOT_LOCAL_IP_ADDR** with the following command:
```shell
$ export PATRIOT_LOCAL_IP_ADDR=$(kubectl cluster-info dump | \
  grep "projectcalico.org/IPv4Address" | \
  awk '{print $2}' | \
  sed 's/\("\|,\)//g')
```


When all of the preparation is done, you can execute all of the tests by running:

```shell
$ mvn clean test
```

The test run can take a couple of minutes (~7 min).