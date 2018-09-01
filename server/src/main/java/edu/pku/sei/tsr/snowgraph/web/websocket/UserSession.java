package edu.pku.sei.tsr.snowgraph.web.websocket;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public class UserSession {
    @Getter private final String sessionId;

    @Nullable @Setter private SnowGraph snowGraph;

    public UserSession(String sessionId) {
        this.sessionId = sessionId;
    }
}
