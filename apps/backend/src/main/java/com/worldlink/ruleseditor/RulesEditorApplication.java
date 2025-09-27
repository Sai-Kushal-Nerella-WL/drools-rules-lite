package com.worldlink.ruleseditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@Slf4j
public class RulesEditorApplication implements CommandLineRunner {

    @Value("${rules.path}")
    private String rulesPath;

    public static void main(String[] args) {
        SpringApplication.run(RulesEditorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Path absolutePath = Paths.get(rulesPath).toAbsolutePath();
        log.info("Rules file path configured: {}", absolutePath);
        log.info("Swagger UI available at: http://localhost:8080/swagger-ui.html");
    }
}
