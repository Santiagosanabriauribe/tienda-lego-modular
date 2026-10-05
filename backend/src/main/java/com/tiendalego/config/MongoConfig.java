package com.tiendalego.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${MONGODB_URI:mongodb://localhost:27017/tienda_lego}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        ConnectionString cs = new ConnectionString(mongoUri);
        String db = cs.getDatabase();
        return db != null ? db : "tienda_lego";
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString cs = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();
        return MongoClients.create(settings);
    }
}
