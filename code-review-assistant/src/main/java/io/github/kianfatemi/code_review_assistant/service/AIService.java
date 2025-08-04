package io.github.kianfatemi.code_review_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.model.endpoint}")
    private String apiEndpoint;

    @Value("${ai.model.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAIFeedback(String pullRequestDiff) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        try {
            String prompt = "As a senior software engineer, please review the following pull request diff. " +
                    "Focus on overall code logic, potential optimizations, adherence to best practices, " +
                    "and any potential bugs or security concerns. Do not comment on simple style issues. " +
                    "Provide your feedback in a concise, constructive manner using markdown formatting.\n\n" +
                    "--- PULL REQUEST DIFF ---\n" +
                    pullRequestDiff;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Construct the request body for the Gemini API
            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // Parse the Gemini API response
            JsonNode root = objectMapper.readTree(response);
            String generatedText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return generatedText.trim();

        } catch (Exception e) {
            logger.error("Error getting feedback from Gemini AI model", e);
            return "Could not retrieve AI-powered feedback at this time.";
        }
    }
}
