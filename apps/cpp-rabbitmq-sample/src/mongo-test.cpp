#include <iostream>

#include <bsoncxx/builder/stream/document.hpp>
#include <bsoncxx/json.hpp>

#include <mongocxx/client.hpp>
#include <mongocxx/instance.hpp>

int main(int, char**) {
    mongocxx::instance inst;
    mongocxx::client conn{mongocxx::uri{"mongodb://admin:Thermalsight1@cluster0-shard-00-00.x1ane.azure.mongodb.net:27017,cluster0-shard-00-01.x1ane.azure.mongodb.net:27017,cluster0-shard-00-02.x1ane.azure.mongodb.net:27017/finance?ssl=true&replicaSet=atlas-60jw2s-shard-0&authSource=admin&retryWrites=true&w=majority"}};
    auto collection = conn["finance"]["bonds"];

    auto cursor = collection.find({});

    for (auto&& doc : cursor) {
        std::cout << bsoncxx::to_json(doc) << std::endl;
    }
}