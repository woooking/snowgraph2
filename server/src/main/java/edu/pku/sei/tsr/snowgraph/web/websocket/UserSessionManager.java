package edu.pku.sei.tsr.snowgraph.web.websocket;

import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {
    private final ConcurrentHashMap<String, UserSession> userSessions = new ConcurrentHashMap<>();

    public void register(UserSession userSession) {
        userSessions.put(userSession.getSessionId(), userSession);
    }

    public UserSession get(String userSessionId) {
        return userSessions.get(userSessionId);
    }

    public void remove(String userSessionId) {
        userSessions.remove(userSessionId);
    }
}
