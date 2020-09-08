#include <iostream>
#include <string>
#include <thread>
#include <functional>
#include <map>
#include <stdio.h>      
#include <string.h> 

#include "json.hpp"
#include "SimplePocoHandler.h"
#include "tools.h"
#include "utils.hpp"

using namespace std;
using json = nlohmann::json;

void timer_start(std::function<void(void)> func, unsigned int interval)
{
    std::thread([func, interval]() {
        while (true)
        {
            func();
            std::this_thread::sleep_for(std::chrono::milliseconds(interval));
        }
    }).detach();
}

void addInformation(json &j, string cid) {
    j["num1"] = (rand() % 10) + 1;
    j["num2"] = rand() % 1000;
    j["cal_type"] = (rand() % 2) + 1;
    j["client_ip"] = getIPaddress();
    j["client_ver"] = "V2";
    j["cid"] = cid;
    j["request_time"] = getCurrentTime();
}

int main(void)
{
    srand((unsigned) time(0));
    
    SimplePocoHandler handler("172.30.164.21", 5672);
    //SimplePocoHandler handler("172.30.164.21", 5672);
    AMQP::Connection connection(&handler, AMQP::Login("guest", "guest"), "/");
    AMQP::Channel channel(&connection);

    bool tempQueueCreated = false;
    string tempQueueName;

    AMQP::QueueCallback callback = [&tempQueueName, &tempQueueCreated](const std::string &name,
            int msgcount,
            int consumercount)
    {
        tempQueueName = name;
        tempQueueCreated = true;
        cout << "Temporary queue " << tempQueueName << " created" << endl;
    };
    channel.declareQueue(AMQP::exclusive).onSuccess(callback);

    int counter = 0;
    map<string, string> messagesSent;

    channel.onReady([&]()
    {
        if(handler.connected())
        {
            timer_start([&channel, &counter, &tempQueueName, &messagesSent]() {
                string correlationId = uuid();
                json mes_json;

                addInformation(mes_json, correlationId);

                string message = mes_json.dump();
                messagesSent.insert(make_pair(correlationId, message));

                AMQP::Envelope env(message);
                env.setCorrelationID(correlationId);
                env.setReplyTo(tempQueueName);
                channel.publish("","requests",env);
                cout << "Message " << message << " published to requests queue" << endl;
            }, 600000);
        }
    });

    auto receiveCallback = [&messagesSent](const AMQP::Message &message,
            uint64_t deliveryTag,
            bool redelivered)
    {
        // Quit if the correlation ID cannot be found in our records of messages sent
        if (messagesSent.find(message.correlationID()) == messagesSent.end()) {
            return;
        }
        cout << "Response received for " << message.correlationID() << " is: " << message.message() << endl;
        cout << "===========================================" << endl;
    };

    channel.consume("", AMQP::noack).onReceived(receiveCallback);
    handler.loop();
    return 0;
}
