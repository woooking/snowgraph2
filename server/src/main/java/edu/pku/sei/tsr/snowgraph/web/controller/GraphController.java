package edu.pku.sei.tsr.snowgraph.web.controller;

import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import org.springframework.stereotype.Controller;

@Controller
public class GraphController {
    private final SnowGraphRepository snowGraphRepository;

    public GraphController(SnowGraphRepository snowGraphRepository) {
        this.snowGraphRepository = snowGraphRepository;
    }
}
