package com.idleitems.school.config;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, org.springframework.boot.SpringApplication application) {
        Map<String, Object> envVars = new HashMap<>();
        
        // 尝试多个可能的 .env 文件路径
        String[] possiblePaths = {
            "../.env",
            ".env",
            Paths.get(System.getProperty("user.dir"), "..", ".env").toString()
        };
        
        for (String path : possiblePaths) {
            Path envPath = Paths.get(path).normalize();
            if (Files.exists(envPath)) {
                try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                    Properties props = new Properties();
                    props.load(fis);
                    for (String key : props.stringPropertyNames()) {
                        envVars.put(key, props.getProperty(key));
                    }
                    break;
                } catch (IOException e) {
                    System.err.println("Failed to load .env from " + path + ": " + e.getMessage());
                }
            }
        }
        
        if (!envVars.isEmpty()) {
            MapPropertySource propertySource = new MapPropertySource("dotenv", envVars);
            environment.getPropertySources().addFirst(propertySource);
        }
    }
}
