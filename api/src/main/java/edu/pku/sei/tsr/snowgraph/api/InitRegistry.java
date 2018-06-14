package edu.pku.sei.tsr.snowgraph.api;

import org.slf4j.Logger;

import java.util.List;

public interface InitRegistry {
    List<String> getArgs();

    Logger getLogger();

}
