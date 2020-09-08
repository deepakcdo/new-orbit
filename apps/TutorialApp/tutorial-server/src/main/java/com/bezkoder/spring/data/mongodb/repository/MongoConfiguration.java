package com.bezkoder.spring.data.mongodb.repository;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {

    private final String database = "tutorials-db";

    @Override
    public String getDatabaseName() {
        return database;
    }

    @Override
    public MongoClient mongoClient() {
        String mongoUri = System.getenv("mongoUri");
        if (mongoUri == null){
            mongoUri = System.getProperty("mongoUri");
        }
        System.out.println("Going to use mongo from " + mongoUri + "!!!!!");
        MongoClientURI connectionString = new MongoClientURI(mongoUri);
        return new MongoClient(connectionString);
    }
}