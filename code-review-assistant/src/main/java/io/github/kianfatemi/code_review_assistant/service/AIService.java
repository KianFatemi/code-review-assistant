package io.github.kianfatemi.code_review_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.model.endpoint}")
    private String apiEndpoint;

    @Value("${ai.model.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getAIFeedback(String pullRequestDiff) {
        try {
            String prompt = "As a senior software engineer, please review the following pull request diff. " +
                    "Focus on overall code logic, potential optimizations, adherence to best practices, " +
                    "and any potential bugs or security concerns. Do not comment on simple style issues. " +
                    "Provide your feedback in a concise, constructive manner using markdown formatting.\n\n" +
                    "--- PULL REQUEST DIFF ---\n" +
                    pullRequestDiff;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of("max_new_tokens", 250) //limit response length
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(apiEndpoint, entity, String.class);

            JsonNode root = objectMapper.readTree(response);

            String generatedText = root.get(0).path("generated_text").asText();
            return generatedText.replace(prompt, "").trim();

        } catch (Exception e){
            logger.error("Error getting feedback from AI model", e);
            return "Could not retreive AI feedback at this time";
        }
    }
}
