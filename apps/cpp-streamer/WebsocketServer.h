#ifndef _WEBSOCKET_SERVER
#define _WEBSOCKET_SERVER

//We need to define this when using the Asio library without Boost
#define ASIO_STANDALONE

#include <websocketpp/config/asio_no_tls.hpp>
#include <websocketpp/server.hpp>
#include <json/json.h>

#include <functional>
#include <string>
#include <vector>
#include <map>
using std::string;
using std::vector;
using std::map;

typedef websocketpp::server<websocketpp::config::asio> WebsocketEndpoint;
typedef websocketpp::connection_hdl ClientConnection;

class WebsocketServer
{
	public:
		
		WebsocketServer();
		void run(int port);
		
		//Sends a message to an individual client
		//(Note: the data transmission will take place on the thread that called WebsocketServer::run())
		void sendMessage(ClientConnection conn, std::string message);
		
		//Sends a message to all connected clients
		//(Note: the data transmission will take place on the thread that called WebsocketServer::run())
		void broadcastMessage(std::string message);
		
	protected:
		
		void onOpen(ClientConnection conn);
		void onClose(ClientConnection conn);
		void onMessage(ClientConnection conn, WebsocketEndpoint::message_ptr msg);
		
		asio::io_service eventLoop;
		WebsocketEndpoint endpoint;
		vector<ClientConnection> openConnections;
};

#endif
