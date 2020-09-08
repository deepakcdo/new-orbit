import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;

import org.bson.Document;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import org.jboss.logging.Logger;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
@ClientEndpoint
public class InstrumentEnricher implements QuarkusApplication {

    @Inject MongoClient mongoClient;

    @Inject
    EnricherServerEndpoint enricherServerEndpoint;

    private static final Logger LOG = Logger.getLogger(InstrumentEnricher.class);
    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("Connected ... " + session.getId());
    }

    @OnMessage
    public String onMessage(String message, Session session) throws ParseException {
        int isinIndex = 0;
        int dataIndex = 1;
        //LOG.info("Received ...." + message);
        List<String> res = processMessage(message);
        enricherServerEndpoint.broadcast(res.get(isinIndex), res.get(dataIndex));
        return res.get(dataIndex);
    }

    private List<String> processMessage(String message) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(message);
        String isin = (String) json.get("isin");
        Long offer = (Long) json.get("offer");
        Long bid = (Long) json.get("bid");

        Long spread = bid - offer;
        Boolean alertFlag = spread >= 500;

        JSONObject data = new JSONObject();
        data.put("isin", json.get("isin"));
        data.put("spread", spread);
        data.put("alertFlag", alertFlag);
        data.put("offer", offer);
        data.put("bid", bid);

        List<String> res = new ArrayList<>();
        res.add(isin);
        res.add(data.toString());
        return res;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOG.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        latch.countDown();
    }

    @Override
    public int run(String... args) throws Exception {
        latch = new CountDownLatch(1);
        List<String> isins = getAllISIN();

        for (String isin : isins) {
            ClientManager client = ClientManager.createClient();
            String URI = "ws://instrument-streamer-new-orbit-helen.e4ff.pro-eu-west-1.openshiftapps.com/bonds/" + isin;
            try {
                client.connectToServer(this, new URI(URI));
            } catch (DeploymentException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        LOG.info("Broadcasting all the bonds...");
        latch.await();
        return 10;
    }

    public List<String> getAllISIN() throws IOException {
        List<String> isins = new ArrayList<>();
        FindIterable<Document> documents = mongoClient.getDatabase("finance").getCollection("bonds").find().projection(Projections.fields(Projections.include("isin"), Projections.excludeId()));
        for (Document document : documents) {
            isins.add((String)document.get("isin"));
        }
        return isins;
    }
}