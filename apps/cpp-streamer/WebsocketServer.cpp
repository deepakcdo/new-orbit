#include "WebsocketServer.h"

#include <algorithm>
#include <functional>
#include <iostream>
#include <map>

//The name of the special JSON field that holds the message type for messages
#define MESSAGE_FIELD "__MESSAGE__"

WebsocketServer::WebsocketServer()
{
	//Wire up our event handlers
	this->endpoint.set_open_handler(std::bind(&WebsocketServer::onOpen, this, std::placeholders::_1));
	this->endpoint.set_close_handler(std::bind(&WebsocketServer::onClose, this, std::placeholders::_1));
	this->endpoint.set_message_handler(std::bind(&WebsocketServer::onMessage, this, std::placeholders::_1, std::placeholders::_2));
	
	//Initialise the Asio library, using our own event loop object
	this->endpoint.init_asio(&(this->eventLoop));
}

void WebsocketServer::run(int port)
{
	//Listen on the specified port number and start accepting connections
	this->endpoint.listen(port);
	this->endpoint.start_accept();
	
	//Start the Asio event loop
	this->endpoint.run();
}

void WebsocketServer::sendMessage(ClientConnection conn, std::string message)
{
	this->endpoint.send(conn, message, websocketpp::frame::opcode::text);
}

void WebsocketServer::broadcastMessage(std::string message)
{
	// Send the message to all clients
	for (auto conn : openConnections) {
		this->sendMessage(conn, message);
	}
}

void WebsocketServer::onOpen(ClientConnection conn)
{
	openConnections.push_back(conn);

	std::cout << "A new client has connected" << std::endl;
}

void WebsocketServer::onClose(ClientConnection conn)
{
	//Remove the connection handle from our list of open connections
	auto connVal = conn.lock();
	auto newEnd = std::remove_if(openConnections.begin(), openConnections.end(), [&connVal](ClientConnection elem)
	{
		//If the pointer has expired, remove it from the vector
		if (elem.expired() == true) {
			return true;
		}
			
		//If the pointer is still valid, compare it to the handle for the closed connection
		auto elemVal = elem.lock();
		if (elemVal.get() == connVal.get()) {
			return true;
		}
			
		return false;
	});
		
	//Truncate the connections vector to erase the removed elements
	openConnections.resize(std::distance(openConnections.begin(), newEnd));

	std::cout << "A client has disconnected from the WebSocket server" << std::endl;
}

void WebsocketServer::onMessage(ClientConnection conn, WebsocketEndpoint::message_ptr msg)
{
	std::string message = msg->get_payload();
	std::cout << "Message received: " << message << std::endl;
	int firstNum;
	try {
		firstNum = std::stoi(message.substr(0, message.find("+")));
	}
	catch (const std::invalid_argument& e) {
		std::cout << "Input for number 1 is invalid" << std::endl;
		this->sendMessage(conn, "Input for number 1 is invalid");
		return;
	}

	int secondNum;
	try {
		secondNum = std::stoi(message.substr(message.find("+") + 1, std::string::npos));
	}
	catch (const std::invalid_argument& e) {
		std::cout << "Input for number 2 is invalid" << std::endl;
		this->sendMessage(conn, "Input for number 2 is invalid");
		return;
	}

	int sum = firstNum + secondNum;

	this->sendMessage(conn, "The sum is " + std::to_string(sum));
	std::cout << "A new sum was calculated and sent to the client" << std::endl;
}
