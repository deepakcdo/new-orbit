FROM ubuntu

RUN apt-get update
RUN apt install curl
RUN apt install telnet
RUN apt-get install iputils-ping

ENTRYPOINT ['sh', '-c', 'echo The app is running! && sleep 3600']


