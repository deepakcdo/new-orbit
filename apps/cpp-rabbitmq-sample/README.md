# Sample C++ RabbitMQ project

## How to Push the image to Openshift registry
Log in to Openshift in terminal first

Log in to Docker:

    docker login registry.pro-eu-west-1.openshift.com -u $(oc whoami) -p $(oc whoami -t)

For Client:

    docker tag cpp-client:v2 registry.pro-eu-west-1.openshift.com/new-orbit-helen/cpp-client:v2
    docker push registry.pro-eu-west-1.openshift.com/new-orbit-helen/cpp-client:v2

For Server:

    docker tag cpp-server:v1 registry.pro-eu-west-1.openshift.com/new-orbit-helen/cpp-server:v1
    docker push registry.pro-eu-west-1.openshift.com/new-orbit-helen/cpp-server:v1


## How to run a local RabbitMQ instance:
    
    docker container run --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management

Then go to localhost:15672 and log in using username guest and password guest
  
## How to run the program locally inside container:

For Client:

    docker build . -t cpp-client:v2 -f Dockerfile-client
    docker run -i -t cpp-client:v2 /bin/bash
    ./src/send

For Server:

    docker build . -t cpp-server:v1 -f Dockerfile-server
    docker run -i -t cpp-server:v1 /bin/bash
    ./src/receive