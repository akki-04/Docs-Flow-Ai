package com.aiDoc.AiDoc.controller;

import com.aiDoc.AiDoc.service.DifyClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controller {

    private final DifyClientService difyClientService;

    public Controller(DifyClientService difyClientService) {
        this.difyClientService = difyClientService;
    }

    // PDF Upload
    @PostMapping("/documents/upload")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("File is empty");
        }

        String response = difyClientService.uploadDocument(file);

        return ResponseEntity.ok(response);
    }

    // AI Query
    @PostMapping("/query")
    public ResponseEntity<String> query(
            @RequestBody Map<String, String> request) {

        String question = request.get("query");

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Query is empty");
        }

        String response = difyClientService.sendQuery(question);

        return ResponseEntity.ok(response);
    }
}