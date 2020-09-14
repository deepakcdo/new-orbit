## **Study Note for Oreilly - OpenShift for Absolute Beginners** *by Mumshad Mannambeth***

* Types of build:
	1. Docker Build: Provide the Dockerfile in the source code, OpenShift will automatically run the Dockerfile and turn the app into a Docker image. The image will then be pushed to the internal Docker registry.
	
	1. Source to Image(default): A framework that takes your application code and convert it to a reusable Docker image without having to provide the Dockerfile. S2I uses a pre-built builder image and injects the app code into it, to create the final app image.
	
	*Note: The default build will use S2I, need to change the build configuration from Web Console if wanted to change the type of build.*


* Image Streams: Metadata (i.e. a name) that provides reference to the actual Docker image within OpenShift. Hence can provide a consistent reference technique for Docker images hosted at different locations from inside and outside.

* Pod: A single instance of container running the application. Replicas of pods are created for scaling and higher availability purposes if we need multiple instances of our app running. Replicas are controlled by Replication Controller.

* Deployment Strategy:
	1. Rolling(default): if we have multiple replicas and updates them, replicas will be updates one at a time.
	1. Recreate: Destroyed all old deployments and then re-deploy.
		
* Rolling back deployments: Can roll back to previous deployment on web console, by clicking 'Roll Back' in deployment configuration.

* OpenShift SDN: OpenShift Software Defined Network creates a virtual network that spans across multiple nodes in the cluster.

* Service: Help connect different applications or group of pods with one and the other(e.g. from front end to back end, from back end to database etc). Each service within OpenShift gets its own IP address and DNS entries that can be used to establish connectivity from other applications. 

* Route: Help expose the service to external users through a host name. 

* Routing Strategy:
    1. Source: Same user access to the same pod
	1. Roundrobin: Each request goes to one pod
	1. Leastcom: Route user to the pod with the lowest number of connections

* Alternate Services: (Strategy A/B) Can control the load of users going to either A or B service

* Scaling Application: Replication controller is responsible to controlling replicas of a pod. Scaling = change the number of replicas used. Web Console can deal with it easily.

* Storage: OpenShift use plugins to provide storage.(e.g. local, iSCSI...) Can create storage by 'Create Storage' in web console.

* Storage Mode: 
	1. Single User Mode(RWO): Allows the volume to be mounted as read/write at a single node only.
	1. Shared Access(RWX): Allows volumes to be mounted by multiple nodes for read/write access.
	1. Read Only(ROX): Multiple nodes, but read only.

* Storage can then be attached to pods by adding volume information in deployment configuration.