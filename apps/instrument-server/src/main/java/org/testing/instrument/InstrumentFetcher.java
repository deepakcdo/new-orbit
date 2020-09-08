package org.testing.instrument;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;

import org.bson.Document;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@Path("/api")
public class InstrumentFetcher {
    @Inject MongoClient mongoClient;

    private static final Logger LOG = Logger.getLogger(InstrumentFetcher.class);

    /*
     * Search for bonds that has longDescription starts with {longDes}, ISIN number starts with {initialISIN}. It is also 
     * able to fetch specific number of documents starting with index {startIndex} and ending with index {endIndex}
     * Example request: api/filter?startIndex=0&endIndex=14&longDes=BUNDESOBLIGATION&initialISIN=DE
     * Note if the param of startIndex and endIndex is invalid, this will throw an error.
     */
    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchLongDes(@QueryParam("longDes") String longDes, @QueryParam("initialISIN") String initialISIN,
                @QueryParam("startIndex") Integer startIndex, @QueryParam("endIndex") Integer endIndex) {
        
        BasicDBObject longDesQuery = regexQuery("longDescription", longDes);
        BasicDBObject initialISINQuery = regexQuery("isin", initialISIN);

        FindIterable<Document> iterable = getCollection().find(and(longDesQuery, initialISINQuery)).skip(startIndex).limit(endIndex - startIndex + 1);
       
        ArrayList<Document> docList = new ArrayList<Document>(); 
        addDataToList(docList, iterable);
        return Response.ok(docList).build();
    }

    /*
     * Helper function to create regex query for searching purpose
     */
    private BasicDBObject regexQuery(String field, String pattern) {
        if (!pattern.equals("")) {
            LOG.info("Instrument Fetcher is now looking for bond that has " + field + " starting with " + pattern);
        }
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put(field, new BasicDBObject("$regex", pattern + ".*"));
        return regexQuery;
    }

    /*
     * Search for bonds that has ISIN number of {isin}
     * Example request: /api/DE0001141760
     */
    @GET
    @Path("/{isin}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByISIN(@PathParam String isin) {
        LOG.info("Instrument Fetcher is now looking for bond with ISIN number " + isin);
        Document res = getCollection().find(eq("isin", isin)).first();
        if (res == null) {
            LOG.info("Can not found bond with ISIN number " + isin);
            return Response.status(Response.Status.NOT_FOUND).entity("Bond not found").build();
        }

        LOG.info("Found bond with ISIN number " + isin);
        return Response.ok(res).build();
    }

    /*
     * Search for the first N bonds in the database with alphabetical order in longDescription
     * Example request: /api/first10
     */
    @GET
    @Path("/first{n}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirstN(@PathParam String n) {
        LOG.info("Server is now looking for the first " + n + " bonds in database...");
        ArrayList<Document> firstn = new ArrayList<Document>(); 
        FindIterable<Document> iterable = getCollection().find().sort(new BasicDBObject("longDescription",1)).limit(Integer.valueOf(n));
        addDataToList(firstn, iterable);
        return Response.ok(firstn).build();
    }

    /*
     * Search for all the bonds in the database
     * Example request: /api/all
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        LOG.info("Server is now looking for all the bonds in database...");
        ArrayList<Document> allDoc = new ArrayList<Document>(); 
        FindIterable<Document> iterable = getCollection().find();
        addDataToList(allDoc, iterable);
        return Response.ok(allDoc).build();
    }

    /*
     * Fetch for all the isin in database
     * Example request: /api/allISIN
     */
    @GET
    @Path("/allISIN")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllISIN() {
        LOG.info("Retreiving all the isins...");

        List<String> isins = new ArrayList<>();
        FindIterable<Document> documents = getCollection().find().projection(Projections.fields(Projections.include("isin"), Projections.excludeId()));
        for (Document document : documents) {
            isins.add((String)document.get("isin"));
        }
        return Response.ok(isins).build();
    }

    /*
     * Helper function to save the items in the list
     */
    private void addDataToList(ArrayList<Document> docs, FindIterable<Document> iterable) {
        MongoCursor<Document> cursor = iterable.iterator();
        try {
            while(cursor.hasNext()) {
                docs.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        LOG.info("Server has fetched " + docs.size() + " bonds from database.");
    }

    /*
     * Helper function to get the collection from the database
     */
    private MongoCollection<Document> getCollection(){
        return mongoClient.getDatabase("finance").getCollection("bonds");
    }
}