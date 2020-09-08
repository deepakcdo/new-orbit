import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;

@ServerEndpoint("/enriched-bonds/{isin}")
@ApplicationScoped
public class EnricherServerEndpoint {

    Map<String, List<Session>> sessions = new ConcurrentHashMap<>(); 
    Logger LOG = Logger.getLogger(EnricherServerEndpoint.class.getName());

    @OnOpen
    public void onOpen(Session session, @PathParam("isin") String isin) {
        session.setMaxIdleTimeout(0);
        if (!sessions.containsKey(isin)) {
            sessions.put(isin, new ArrayList<>());
        }
        sessions.get(isin).add(session);
        LOG.info("A new client " + session.getId() + " has subscribed to ISIN " + isin);
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }

    @OnClose
    public void onClose(Session session, @PathParam("isin") String isin) {
        sessions.get(isin).remove(session);
        LOG.info("The client " + session.getId() + " has unsubscribed from ISIN " + isin);
        logOpeningState(session);
    }

    @OnError
    public void onError(Session session, @PathParam("isin") String isin, Throwable throwable) {
        sessions.get(isin).remove(session);
        LOG.info("The client " + session.getId() + " has been unsubscribed from ISIN " + isin + " because of error: " + throwable);
    }

    public void broadcast(String isin, String message) {
        //LOG.info("Broadcasting message for bond with isin " + isin + " and data with " + message + " ....");
        if (!sessions.containsKey(isin)) {
            return;
        }
        sessions.get(isin).forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    public void logOpeningState(Session session) {
        if (!session.isOpen()) {
            LOG.info("Session " + session.getId() + " has closed");
        } else {
            LOG.info("Session " + session.getId() + " is still open after unsubcribing");
        }
    }
}