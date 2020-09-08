# Get the base Ubuntu image from Docker Hub
FROM gcc:9.2

RUN apt-get update && apt-get install -y cmake libgtest-dev libboost-test-dev && rm -rf /var/lib/apt/lists/* 

COPY . /usr/src/cpp-streamer

WORKDIR /usr/src/cpp-streamer

EXPOSE 8080

RUN cmake .
RUN cmake --build .
RUN ls

CMD ["./server"]
