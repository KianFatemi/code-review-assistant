package io.github.kianfatemi.code_review_assistant.controller;

import io.github.kianfatemi.code_review_assistant.service.AnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final AnalysisService analysisService;

     //Constructor-based dependency injection.
    public WebhookController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestBody String payload,
                                                      @RequestHeader("X-GitHub-Event") String githubEvent) {

        logger.info("Received GitHub webhook event: {}", githubEvent);

        switch (githubEvent) {
            case "push":
                analysisService.analyzePushEvent(payload);
                break;
            case "pull_request":
                analysisService.analyzePullRequestEvent(payload);
                break;
            default:
                logger.info("Ignoring webhook event of type {}", githubEvent);
                break;
        }

        return ResponseEntity.ok("Webhook received.");
    }
}