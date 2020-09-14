## **Useful Commands**

##### **Commands Regarding Setting Up and Accessing the Cluster**

1. crc setup: Set up host operating system for the CodeReady Containers(crc) virtual machine
1. crc start: Start the OpenShift Cluster
1. crc oc-env: Following this command to set up the ‘oc’ environment
1. crc console: Access the OpenShift web console
1. crc console —credentials: Get the credential information for usernames and passwords
1. crc stop: Stop the CRC virtual machine and OpenShift cluster
1. crc delete: Delete crc 
1. crc cleanup: Clean up changes from the ‘crc setup’ command

1. oc login -u <username> -p <password> <url to console>: Log in through oc command
1. oc logout: Logging out

##### **Regarding Building and Deploying the Project**

1. oc new-project: Request a new project (e.g. oc new-project dev --display-name="Dev - Spring Boot App")
1. oc new-app <builder-image>~<github url> (--context-dir=<context dir> --source-secret=<source secret> --name=<name>): To create a new app and start building and deploying automatically using Java S2I builder image from command line (NB: Output route not automatically generated)
    * oc new-app fabric8/s2i-java~git@github.com:ht1006/readfile-java.git --source-secret=java-readfile --name=readfile-fabric
1. oc expose svc/<my-application-name>: Expose the application through command line after creating a new application using step 2.
    * oc expose svc/readfile-fabric
1. oc status: View the status of all the applications under current project
1. oc get builds: View the progress of all the builds
1. oc start-build <build name>: Start (restart) the specific build
1. oc get all: View all projects and information
1. oc get pods: Monitor the deployment status
1. oc get svc: Indicate what IP address the service is running, the default port for it to deploy at is 8080
1. oc env pods --all --list: To take a look at environment variables set for each pod

1. oc rollout latest dc/<deployment name>: To trigger a build
1. oc rollout history dc/<>: To see a history of builds
1. oc rollout describe dc <>: See the description
1. oc rollout undo dc/<>: Rollback to previous deployment
1. oc new-app -e POSTGRESQL_USER=luke \
                -e POSTGRESQL_PASSWORD=secret \
                -e POSTGRESQL_DATABASE=my_data \
                openshift/postgresql-92-centos7 \
                --name=my-database: create a database instance
1. oc get -o yaml deploymentconfig.apps.openshift.io/tutorial-frontend > deploymentconfig.tutorial-frontend.yml: Export configuration yml file.
1. oc get -o yaml --export all > <yaml_filename>: Extract all yaml files.
##### **Regarding Logs**
1. tail -f crc.log: Under '.crc' repository, check the current log on command line

1. oc logs -f bc/<build name>: Check the current log for the build on command line
    * oc logs -f bc/readfile-fabric
    
#### *Remote Shell Access*
1. oc exec <pod> [-c <container>] <command> [<arg_1> ... <arg_n>]: To run a command in a container (e.g. oc exec mypod date)

1.  oc rsh <pod>: Open a remote shell to a container.

#### *Installing using yum or apt*
ubi
1. yum install iputils: Install ping.

1. yum install -y mongodb-org: Install MongoDB package.

ubuntu
1. apt-get install iputils-ping

1. apt install curl

1. apt install telnet

####

1. need to log in to docker first

1.docker pull ubuntu

1.docker images

1.docker run -it [image id] bin/bash

1.make changes inside the container

1.exit

1.docker ps -a

1.docker commit [container id] new-ubuntu

1.docker images

1.docker tag [image id] keryu/new-ubuntu:firsttry

1.docker push keryu/new-ubuntu:firsttry

#### Copying files from and out of Openshift Container
1. oc rsync <pod-name>:/remote/dir/filename ./local/dir: Copy the [filename] file in directory /remote/dir to the local dir(./local/dir) in pod [pod name]

1. oc rsync <pod-name>:/remote/dir ./local/dir: Copy the directory from the pod to local machine

1. oc rsync ./local/dir <pod-name>:/remote/dir: Copy files from the local machine to the container. Unlike when copying from the container to the local machine, there's no form for copying a single file. To copy only selected files, you'll need to use the --exclude and --include options to filter what is and isn't copied from the specified directory.
    
    e.g. oc rsync . hello-world-4-xpxjq:/home/jboss --exclude=* --include=robots.txt --no-perms: We indicate that the current directory should be copied, but use the --exclude=* option to first say that all files should be ignored when performing the copy. That pattern is then overridden for just the robots.txt file by using the --include=robots.txt file, ensuring that robots.txt is copied. The --no-perms option tells oc rsync to not attempt to update permissions; this avoids it failing and returning errors.

#### Copying files to the persistent volume
1. Have an application running

1. oc set volume dc/hello-world --add --name=tmp-mount --claim-name=data --type pvc --claim-size=1.5G --mount-path /mnt: Claim a persistent volume and mount it against the dummy application pod at the directory /mnt so that files can be copied into the persistent volume using oc rsync.

1. oc set volume dc/hello-world --add --name=tmp-mount --claim-name=data --mount-path /mnt: Mount an existing persistent volume against a dummy application pod at the directory /mnt so that files can be copied into the persistent volume using oc rsync.

#### Get IP address of a pod
1. oc get pods -o wide

#### Running a (development) react app that builds (with webhooks)
1. oc new-app ${git repo}

1. oc expose svc/${app name}

1. Copy webhook provided by openshift

1. Paste in repository (/settings/hooks on github)

Notes:
1. First 2 instructions can be replaced by using the app creation wizard (pick node base)
1. I haven’t figured out how to set external url or configure port yet, so I set react port to 8080 in package.json
1. It takes 2-4 minutes to build in my experience


#### Quarkus
1. mvn io.quarkus:quarkus-maven-plugin:1.5.2.Final:create \
       -DprojectGroupId=org.acme \
       -DprojectArtifactId=getting-started \
       -DclassName="org.acme.getting.started.GreetingResource" \
       -Dpath="/hello"
   cd getting-started: Boostrapping the project

1. ./mvnw compile quarkus:dev: Run the application

1. ./mvnw test: Run tests

1. ./mvnw package: Packaging application. It produces 2 jar files in /target:
                   
                   getting-started-1.0-SNAPSHOT.jar - containing just the classes and resources of the projects, it’s the regular artifact produced by the Maven build;
                   
                   getting-started-1.0-SNAPSHOT-runner.jar - being an executable jar. Be aware that it’s not an über-jar as the dependencies are copied into the target/lib directory.
                   
                   You can run the application using: java -jar target/getting-started-1.0-SNAPSHOT-runner.jar (The Class-Path entry of the MANIFEST.MF from the runner jar explicitly lists the jars from the lib directory. So if you want to deploy your application somewhere, you need to copy the runner jar as well as the lib directory.)
                   
1. ./mvnw quarkus:add-extension -Dextensions="mongodb-client": Add extension

1.     <dependency>
         <groupId>io.quarkus</groupId>
         <artifactId>quarkus-openshift</artifactId>
       </dependency>
       
      Adding dependency.

1. ./mvnw clean package -Dquarkus.container-image.build=true: The command above will trigger an s2i binary build.

1. ./mvnw clean package -Dquarkus.kubernetes.deploy=true: The command above will trigger a container image build and will apply the generated OpenShift resources, right after.

1. quarkus.openshift.expose=true: To expose a Route for the Quarkus application.

1. quarkus.s2i.base-jvm-image=registry.access.redhat.com/openjdk/openjdk-11-rhel7: Set the base image to official red hat image.


1. ./mvnw test: Run the test

#### ConfigMap Configurations:
1.oc create configmap game-config \
      --from-file=example-files/ : Creating ConfigMap from directory.
      
1. oc create configmap game-config-2 \
       --from-file=example-files/game.properties \
       --from-file=example-files/ui.properties: Creating ConfigMap from specific files.
       
1. oc create configmap special-config \
       --from-literal=special.how=very \
       --from-literal=special.type=charm: Creating ConfigMap from specifying literals.
             
1. Then 'Resource' -> 'ConfigMap' -> select the configmap -> 'Add to Application' -> select the wanted application.

1. oc set env dc/[dc-name] --from configmap/[configmap-name]: Or, to add the ConfigMap to the application through command line. 
