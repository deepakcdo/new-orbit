#include "WebsocketServer.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <asio/io_service.hpp>
#include <cstdlib>

//The port number the WebSocket server listens on
#define PORT_NUMBER 8080

using namespace std;

int main(int argc, char* argv[])
{
	WebsocketServer server;
	
	//Start the networking thread
	std::thread serverThread([&server]() {
		server.run(PORT_NUMBER);
	});

	while (true) {
		this_thread::sleep_for(chrono::milliseconds(10000));
	}
	
	return 0;
}
