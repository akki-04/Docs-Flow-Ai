package com.aiDoc.AiDoc.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DifyClientService {
        
        @Value("${dify.base-url}")
        private String baseUrl;
        
        @Value("${dify.knowledge-api-key}")
        private String knowledgeApiKey;
        
        @Value("${dify.dataset-id}")
        private String datasetId;
        
        @Value("${dify.workflow-api-key}")
        private String workflowApiKey;
        
        private final RestClient restClient;
        private final ObjectMapper objectMapper;
        
        
        public DifyClientService(
                RestClient.Builder builder,
                ObjectMapper objectMapper) {

        this.restClient = builder.build();
        this.objectMapper = objectMapper;
}

    // =====================================================
    // PDF UPLOAD TO DIFY KNOWLEDGE BASE
    // =====================================================
    
    public String uploadDocument(MultipartFile file) {
            
            try {
                    
            String url = baseUrl
                    + "/datasets/"
                    + datasetId
                    + "/document/create-by-file";

            ByteArrayResource fileResource =
                    new ByteArrayResource(file.getBytes()) {

                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

            String data = """
                    {
                      "indexing_technique": "high_quality",
                      "process_rule": {
                        "mode": "automatic"
                      }
                    }
                    """;

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            body.add("file", fileResource);
            body.add("data", data);

            return restClient.post()
                    .uri(url)
                    .header(
                            "Authorization",
                            "Bearer " + knowledgeApiKey
                    )
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Dify document upload failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // SEND QUERY TO DIFY CHAT FLOW
    // =====================================================

   public String sendQuery(String question) {

    try {

        String url = baseUrl + "/chat-messages";

        String jsonBody = """
                {
                  "inputs": {
                    "query": "%s"
                  },
                  "query": "%s",
                  "response_mode": "blocking",
                  "user": "local-user"
                }
                """.formatted(
                        escapeJson(question),
                        escapeJson(question)
                );

        System.out.println("====================================");
        System.out.println("Sending to Dify Chat:");
        System.out.println(jsonBody);
        System.out.println("====================================");

        String difyResponse = restClient.post()
                .uri(url)
                .header(
                        "Authorization",
                        "Bearer " + workflowApiKey
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(String.class);

        System.out.println("====================================");
        System.out.println("Dify Raw Response:");
        System.out.println(difyResponse);
        System.out.println("====================================");

        JsonNode response =
                objectMapper.readTree(difyResponse);

        JsonNode answerNode = response.get("answer");

        if (answerNode != null && !answerNode.isNull()) {
            return answerNode.asText();
        }

        return difyResponse;

    } catch (Exception e) {

        throw new RuntimeException(
                "Dify query failed: "
                        + e.getMessage(),
                e
        );
    }
}

private String escapeJson(String text) {

    return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
}
}