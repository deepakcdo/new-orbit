#include <iostream>
#include <thread>
#include <string.h> 
#include<cstdlib>
#include "SimplePocoHandler.h"
#include "json.hpp"
#include "utils.hpp"

#include <bsoncxx/builder/stream/document.hpp>
#include <bsoncxx/json.hpp>

#include <mongocxx/client.hpp>
#include <mongocxx/instance.hpp>
#include <bsoncxx/builder/stream/document.hpp>
#include <bsoncxx/json.hpp>

using namespace std;
using json = nlohmann::json;

using bsoncxx::builder::stream::close_array;
using bsoncxx::builder::stream::close_document;
using bsoncxx::builder::stream::document;
using bsoncxx::builder::stream::finalize;
using bsoncxx::builder::stream::open_array;
using bsoncxx::builder::stream::open_document;

void generatePrimes(int n);

int doCalculation(int num1, int num2, int calc_type) {
    int res;
    if (calc_type == 1) {
        res = num1 + num2;
    } else {
        res = (num1 + num2) * 1000;
    }
    /*
    if (num1 == 5 || num1 == 10) {
        this_thread::sleep_for(chrono::milliseconds(num1 * 1000));
    }
    */
    //int upperBound = rand() % 500000 + 1500000;
    //cout << "Upper bound for prime number generation: " << upperBound << endl;
    generatePrimes(1200000);
    return res;
}

void generatePrimes(int n) {
    int low, high, i, flag;

    low = 1;
    high = n;

    while (low < high)
    {
        flag = 0;

        for(i = 2; i <= low/2; ++i)
        {
            if(low % i == 0)
            {
                flag = 1;
                break;
            }
        }
        cout << low << endl;

        ++low;
    }
}

string convertMilliToTime(long duration) {
    long hr = duration / 3600000;
    duration -= 3600000 * hr;
    long min = duration / 60000;
    duration -= 60000 * min;
    long sec = duration / 1000;
    duration -= 1000 * sec;

    char res[40];
    sprintf(res, "%02ld:%02ld:%02ld.%03ld", hr, min, sec, duration);
    return res;
}

string getCalDuration(string request_time, string response_time) {
    //Tue Aug 18 2020 08:51:13.539
    string milli_request = request_time.substr(request_time.length() - 12, 12);
    string milli_response = response_time.substr(response_time.length() - 12, 12);

    char request_array[milli_request.length() + 1]; 
    strcpy(request_array, milli_request.c_str());

    char response_array[milli_response.length() + 1]; 
    strcpy(response_array, milli_response.c_str()); 

    int hour1, min1, sec1, milli1;
    int hour2, min2, sec2, milli2;
    long millisec1;
    long millisec2;

    hour1 = atoi( strtok( request_array, ":." ) );
    min1 = atoi( strtok( NULL, ":." ) );
    sec1 = atoi( strtok( NULL, ":." ) );
    milli1 = atoi( strtok( NULL, ":." ) );

    hour2 = atoi( strtok( response_array, ":." ) );
    min2 = atoi( strtok( NULL, ":." ) );
    sec2 = atoi( strtok( NULL, ":." ) );
    milli2 = atoi( strtok( NULL, ":." ) );

    millisec1 = ((hour1 * 60 * 60) + (min1 * 60) + sec1)*1000 + milli1;
    millisec2 = ((hour2 * 60 * 60) + (min2 * 60) + sec2)*1000 + milli2;
    
    long duration = millisec2 - millisec1;

    return convertMilliToTime(duration);
}

void addServerMetaData(json &j) {
    j["server_ver"] = "V1";
    j["server_ip"] = getIPaddress();
    j["response_time"] = getCurrentTime();
    j["cal_duration"] = getCalDuration(j["request_time"], j["response_time"]);
}

void auditRequest(json &j, string cid, mongocxx::collection &requestsCollection) {
    j["response_sent"] = false;
    requestsCollection.insert_one(bsoncxx::from_json(j.dump()));
    cout << "Request recorded in database" << endl;
}

void auditResponse(json &j, string cid, mongocxx::collection &requestsCollection) {
    // Check whether this CID exists in the database
    bsoncxx::stdx::optional<bsoncxx::document::value> maybe_result = 
        requestsCollection.find_one(document{} << "cid" << cid << finalize);
    if(maybe_result) {
        string response_time = j["response_time"];
        string duration = j["cal_duration"];
        int response = j["response"];
        requestsCollection.update_one(document{} << "cid" << cid << finalize,
                      document{} << "$set" << open_document <<
                        "response_sent" << true << "response_time" << response_time 
                        << "response" << response << "cal_duration" << duration << close_document << finalize);
        cout << "Response recorded in database" << endl;
    }
}

int main(void)
{
    // Set up connection to audit database
    mongocxx::instance inst;
    mongocxx::client conn{mongocxx::uri{"mongodb+srv://admin:Thermalsight1@cluster0.x1ane.azure.mongodb.net/audit?retryWrites=true&w=majority"}};
    mongocxx::collection requestsCollection = conn["audit"]["requests"];
    
    srand((unsigned) time(0));

    //SimplePocoHandler handler("192.168.0.14", 5672);
    SimplePocoHandler handler("172.30.164.21", 5672);

    AMQP::Connection connection(&handler, AMQP::Login("guest", "guest"), "/");
    AMQP::Channel channel(&connection);
    channel.setQos(1);

    channel.consume("requests").onReceived([&channel, &requestsCollection](const AMQP::Message &message,
            uint64_t deliveryTag,
            bool redelivered)
    {
        auto j = json::parse(message.message());
        cout << "Received request for " << j["cid"] << endl;
        auditRequest(j, j["cid"], requestsCollection);

        j["response"] = doCalculation(j["num1"], j["num2"], j["cal_type"]);
        addServerMetaData(j);

        j["response_sent"] = true;
        
        AMQP::Envelope env(j.dump());
        env.setCorrelationID(message.correlationID());

        channel.publish("", message.replyTo(), env);
        channel.ack(deliveryTag);
        
        cout << "Response sent for " << j["cid"] << " is: " << j << endl;
        auditResponse(j, j["cid"], requestsCollection);
        cout << "===========================================" << endl;
    });

    std::cout << "Waiting for requests" << std::endl;
    handler.loop();
    return 0;
}
