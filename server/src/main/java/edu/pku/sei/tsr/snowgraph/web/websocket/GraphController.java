package edu.pku.sei.tsr.snowgraph.web.websocket;

import edu.pku.sei.tsr.snowgraph.SnowGraphFactory;
import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import edu.pku.sei.tsr.snowgraph.web.message.CreateGraphMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("graph")
public class GraphController {
    private final SnowGraphRepository snowGraphRepository;
    private final SimpMessagingTemplate template;
    private final UserSessionManager userSessionManager;

    public GraphController(SnowGraphRepository snowGraphRepository, SimpMessagingTemplate template, UserSessionManager userSessionManager) {
        this.snowGraphRepository = snowGraphRepository;
        this.template = template;
        this.userSessionManager = userSessionManager;
    }

    @MessageMapping("getAll")
    public void graphs() {
//        snowGraphRepository.getAllGraphs().collectList()
//            .map(OnGetAllMessage::new)
//            .subscribe(this::sendUserMessage);
    }

    @MessageMapping("createAndInit")
    public void build(CreateGraphMessage message, SimpMessageHeaderAccessor headerAccessor) {
        var graph = SnowGraphFactory.create(
            message.getName(),
            message.getSrcDir(),
            message.getDestination(),
            message.getPluginConfigs()
        );
//        snowGraphRepository.createGraph(
//            message.getName(), message.getSrcDir(), message.getDestination(), message.getPluginConfigs()
//        )
//            .map(OnCreateGraphMessage::new)
//            .subscribe(this::sendUserMessage);
    }

    @MessageMapping("create")
    public void create(CreateGraphMessage message, SimpMessageHeaderAccessor headerAccessor) {
        var sessionId = headerAccessor.getSessionId();
        var graph = SnowGraphFactory.create(
            message.getName(),
            message.getSrcDir(),
            message.getDestination(),
            message.getPluginConfigs()
        );
        userSessionManager.get(sessionId).setSnowGraph(graph);
        template.convertAndSend("/user/graph/create", "");
//        snowGraphRepository.createGraph(
//            message.getName(), message.getSrcDir(), message.getDestination(), message.getPluginConfigs()
//        )
//            .map(OnCreateGraphMessage::new)
//            .subscribe(this::sendUserMessage);
    }
}
