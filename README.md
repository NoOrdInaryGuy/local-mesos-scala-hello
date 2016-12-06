# Local Mesos Scala Hello World

This repository contains instructions to spin up a local Mesos cluster using Docker, and provides simple code a custom `Scheduler` that allows the traditional "Hello, World!" to be submitted against it.

### Assumptions
* You are using OSX / MacOS and have Docker Machine installed
* Your Docker host is at `192.168.99.100` (see `docker-machine env`)
* Mesos is installed locally (the shared libraries are required) via `brew install mesos`, and the libs are therefore available in `/usr/local/Cellar/mesos/1.1.0/lib`.

### Spin up Local Mesos Cluster

These commands use the [Mesosphere Docker images](https://github.com/mesosphere/docker-containers/tree/master/mesos) to launch:

* 1 Exhibitor Node (Zookeeper)
* 1 Mesos Master
* 1 Mesos Slave

**TODO:** Use docker-compose for this instead.

Assuming docker-machine running on 192.168.99.100:

```bash
# Create a network for the nodes
docker network create --subnet=172.20.0.0/16 mesosnet
# Add a route locally so we can address them by IP
sudo route add 172.20.0.0/16 192.168.99.100
```

```bash
# Start Exhibitor on 172.20.0.10
docker run -d --net=mesosnet --ip=172.20.0.10 netflixoss/exhibitor:1.5.2
```

```bash
# Start the Master on 172.20.0.11
docker run -d --net=mesosnet --ip=172.20.0.11 \
  -e MESOS_HOSTNAME=172.20.0.11 \
  -e MESOS_IP=172.20.0.11 \
  -e MESOS_PORT=5050 \
  -e MESOS_ZK=zk://172.20.0.10:2181/mesos \
  -e MESOS_QUORUM=1 \
  -e MESOS_REGISTRY=in_memory \
  -e MESOS_LOG_DIR=/var/log/mesos \
  -e MESOS_WORK_DIR=/var/tmp/mesos \
  -v "$(pwd)/log/mesos:/var/log/mesos" \
  -v "$(pwd)/tmp/mesos:/var/tmp/mesos" \
  -p 5050:5050 \
  mesosphere/mesos-master:1.1.01.1.0-2.0.107.ubuntu1404
```

```bash
# Start the Slave on 172.20.0.12
docker run -d --net=mesosnet --ip=172.20.0.12 --privileged \
  -e MESOS_HOSTNAME=172.20.0.12 \
  -e MESOS_IP=172.20.0.12 \
  -e MESOS_PORT=5051 \
  -e MESOS_MASTER=zk://172.20.0.10:2181/mesos \
  -e MESOS_SWITCH_USER=0 \
  -e MESOS_CONTAINERIZERS=docker,mesos \
  -e MESOS_LOG_DIR=/var/log/mesos \
  -e MESOS_WORK_DIR=/var/tmp/mesos \
  -v "$(pwd)/log/mesos:/var/log/mesos" \
  -v "$(pwd)/tmp/mesos:/var/tmp/mesos" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /usr/local/bin/docker:/usr/local/bin/docker \
  -p 5051:5051 \
  mesosphere/mesos-slave:1.1.01.1.0-2.0.107.ubuntu1404
```

### Check it Works

* Exhibitor UI: [http://172.20.0.10:8080/exhibitor/v1/ui/index.html](http://172.20.0.10:8080/exhibitor/v1/ui/index.html)
* Mesos Master UI: [http://172.20.0.11:5050/](http://172.20.0.11:5050/)

### Run the Hello-World App

> Code originally based on: [https://github.com/phatak-dev/mesos-helloworld-scala](https://github.com/phatak-dev/mesos-helloworld-scala)

Start the SBT prompt:

```bash
sbt -Djava.library.path=/usr/local/Cellar/mesos/1.1.0/lib
```

At the prompt:

```
run "/bin/echo 'Hello, World!'"
```

### Check the Log

* Find the most recently completed framework in the UI: [http://172.20.0.11:5050/#/frameworks](http://172.20.0.11:5050/#/frameworks)
* Under "Sandbox" for the completed task, you should see similar to this in `stdout`:

```
Received SUBSCRIBED event
Subscribed executor on 172.20.0.12
Received LAUNCH event
Starting task task1481039864427
/usr/libexec/mesos/mesos-containerizer launch --command="{"shell":true,"value":"\/bin\/echo 'Hello, World!'"}" --help="false" --unshare_namespace_mnt="false"
Forked command at 202
Hello, World!
Command exited with status 0 (pid: 202)
Received SHUTDOWN event
Shutting down
```