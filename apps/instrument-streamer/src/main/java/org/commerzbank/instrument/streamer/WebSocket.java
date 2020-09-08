package org.commerzbank.instrument.streamer;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;

@ServerEndpoint("/bonds/{isin}")
@ApplicationScoped
public class WebSocket {

    Map<String, List<Session>> sessions = new ConcurrentHashMap<>(); 
    Logger LOGGER = Logger.getLogger(WebSocket.class.getName());

    @OnOpen
    public void onOpen(Session session, @PathParam("isin") String isin) {
        if (!sessions.containsKey(isin)) {
            sessions.put(isin, new ArrayList<>());
        }
        sessions.get(isin).add(session);
        LOGGER.info("A new client " + session.getId() + " has subscribed to ISIN " + isin);
    }

    @OnClose
    public void onClose(Session session, @PathParam("isin") String isin) {
        sessions.get(isin).remove(session);
        LOGGER.info("The client " + session.getId() + " has unsubscribed from ISIN " + isin);
    }

    @OnError
    public void onError(Session session, @PathParam("isin") String isin, Throwable throwable) {
        sessions.get(isin).remove(session);
        LOGGER.info("The client " + session.getId() + " has been unsubscribed from ISIN " + isin + " because of error: " + throwable);
    }

    public void broadcast(String isin, String message) {
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
}