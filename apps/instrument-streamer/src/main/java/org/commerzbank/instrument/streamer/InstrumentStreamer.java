package org.commerzbank.instrument.streamer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

import org.bson.Document;
import org.json.simple.JSONObject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain    
public class InstrumentStreamer implements QuarkusApplication {

    @Inject
    WebSocket webSocket;
    public static String mongoUri = "mongodb://admin:Thermalsight1@cluster0-shard-00-00.x1ane.azure.mongodb.net:27017,cluster0-shard-00-01.x1ane.azure.mongodb.net:27017,cluster0-shard-00-02.x1ane.azure.mongodb.net:27017/finance?ssl=true&replicaSet=atlas-60jw2s-shard-0&authSource=admin&retryWrites=true&w=majority";

    Logger LOGGER = Logger.getLogger(InstrumentStreamer.class.getName());
    Random random = new Random();

    @Override
    public int run(String... args) throws Exception {   
        LOGGER.info("instrument-streamer has started running");
        
        MongoClientURI uri = new MongoClientURI(mongoUri);
        
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("finance");

        MongoCollection<Document> bonds = database.getCollection("bonds");
        LOGGER.info("Database connection established");

        List<String> isins = new ArrayList<>();

        FindIterable<Document> documents = bonds.find().projection(Projections.fields(Projections.include("isin"), Projections.excludeId()));
        for (Document document : documents) {
            isins.add((String)document.get("isin"));
        }

        while (true) {
            for (String isin : isins) {
                // Construct a JSON object containing the price data for the given ISIN (prices are random for now)
                int bid = random.nextInt(5000) + 500;
                int offer = random.nextInt(bid);
                JSONObject data = new JSONObject();
                data.put("isin", isin);
                data.put("bid", bid);
                data.put("offer", offer);

                // Broadcast JSON to frontend clients
                webSocket.broadcast(isin, data.toString());
            }
            LOGGER.info("Prices for all ISINs were broadcasted");
            Thread.sleep(2000);
        }
    }
}