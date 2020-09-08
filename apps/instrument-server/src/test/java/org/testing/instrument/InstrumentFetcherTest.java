package org.testing.instrument;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class InstrumentFetcherTest {
    // @Mock
    // static private MongoClient mockClient;
    // @Mock
    // static private MongoDatabase mockDB;
    // @Mock
    // static private MongoCollection<org.bson.Document> mockCollection;

    // @InjectMock
	// @RestClient
    // InstrumentFetcher instrumentFetcher;

    // @Test
    // public void testMongoInstance() {
    //     Mockito.when(mockClient.getDatabase("mockDb")).thenReturn(mockDB);
    //     Mockito.when(mockDB.getCollection("mockCollection")).thenReturn(mockCollection);
    //     Document doc = new Document("ISIN", "123456").append("data", "This is data");
    //     mockCollection.insertOne(doc);
    //     given()
    //         .when().get("/api/123456")
    //         .then()
    //         .statusCode(200)
    //         .body(is("{ISIN:123456, }"));
    // }

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/api/A")
          .then()
             .statusCode(404)
             .body(is("Bond not found"));
    }
}