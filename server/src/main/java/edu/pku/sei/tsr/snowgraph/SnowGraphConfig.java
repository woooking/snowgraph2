package edu.pku.sei.tsr.snowgraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SnowGraphConfig {
    private static Logger logger = LoggerFactory.getLogger(SnowGraphConfig.class);

    public SnowGraphConfig() {
//        var mapper = new ObjectMapper();
//        var home = System.getProperty("user.home");
//        var configDir = Paths.get(home, ".snowgraph");
//        var configFile = configDir.resolve("config.json").toFile();
//
//        if (!configDir.toFile().exists()) {
//            logger.info("Config directory {} not existed, will create one.", configDir.normalize());
//            if (!configDir.toFile().mkdirs()) {
//                logger.error("Error occured when creating config directory {}.", configDir.normalize());
//            }
//        }
//
//        try {
//            config = mapper.readValue(configFile, GraphBuildConfig.class);
//        } catch (IOException e) {
//            logger.info("Config file {} not existed, will create one.", configFile.getAbsolutePath());
//            config = new GraphBuildConfig();
//            try {
//                mapper.writeValue(configFile, config);
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
    }

}
