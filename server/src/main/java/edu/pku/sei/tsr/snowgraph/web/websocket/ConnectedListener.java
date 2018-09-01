package edu.pku.sei.tsr.snowgraph.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Component
public class ConnectedListener implements ApplicationListener<SessionConnectEvent> {
    private final UserSessionManager userSessionManager;

    @Autowired
    public ConnectedListener(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        var sha = StompHeaderAccessor.wrap(event.getMessage());
        var userSession = new UserSession(sha.getSessionId());
        userSessionManager.register(userSession);
    }
}
