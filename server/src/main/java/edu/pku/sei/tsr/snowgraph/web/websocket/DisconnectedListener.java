package edu.pku.sei.tsr.snowgraph.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class DisconnectedListener implements ApplicationListener<SessionDisconnectEvent> {
    private final UserSessionManager userSessionManager;

    @Autowired
    public DisconnectedListener(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        var sha = StompHeaderAccessor.wrap(event.getMessage());
        userSessionManager.remove(sha.getSessionId());
    }
}
