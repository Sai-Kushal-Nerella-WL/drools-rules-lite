package com.worldlink.ruleseditor.controller;

import com.worldlink.ruleseditor.model.*;
import com.worldlink.ruleseditor.service.ExcelService;
import com.worldlink.ruleseditor.service.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class RulesController {

    private final ExcelService excelService;
    private final ValidateService validateService;
    
    @Value("${rules.path}")
    private String rulesPath;

    @GetMapping
    public ResponseEntity<DecisionTable> getRules() {
        try {
            Path path = Paths.get(rulesPath);
            if (!Files.exists(path)) {
                log.error("Rules file not found at: {}", path.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            DecisionTable table = excelService.read(path);
            log.info("Successfully loaded rules from: {}", path.toAbsolutePath());
            return ResponseEntity.ok(table);
        } catch (IOException e) {
            log.error("Error reading rules file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateRules(@RequestBody DecisionTable table) {
        try {
            ValidationResult result = validateService.validate(table);
            log.info("Validation completed. OK: {}, Errors: {}", result.ok(), result.errors().size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error validating rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ValidationResult> saveRules(@RequestBody DecisionTable table) {
        try {
            ValidationResult validation = validateService.validate(table);
            if (!validation.ok()) {
                log.warn("Validation failed, not saving rules. Errors: {}", validation.errors().size());
                return ResponseEntity.badRequest().body(validation);
            }
            
            Path path = Paths.get(rulesPath);
            
            Path backupPath = Paths.get(rulesPath + ".bak");
            if (Files.exists(path)) {
                Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Created backup at: {}", backupPath.toAbsolutePath());
            }
            
            MetaBlock metaBlock = new MetaBlock(java.util.List.of());
            if (Files.exists(path)) {
                try {
                    DecisionTable currentTable = excelService.read(path);
                    metaBlock = new MetaBlock(java.util.List.of());
                } catch (Exception e) {
                    log.warn("Could not read existing file for meta block: {}", e.getMessage());
                }
            }
            
            excelService.write(table, metaBlock, path);
            log.info("Successfully saved rules to: {}", path.toAbsolutePath());
            
            return ResponseEntity.ok(validation);
        } catch (IOException e) {
            log.error("Error saving rules file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error saving rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
