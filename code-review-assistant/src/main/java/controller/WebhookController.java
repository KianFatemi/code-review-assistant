package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles incoming webhook events from GitHub.
 */
@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    /**
     * Endpoint to receive webhook payloads from GitHub.
     *
     * @param payload The raw JSON payload from the webhook event.
     * @param githubEvent The type of event (e.g., 'push', 'pull_request').
     * @return A 200 OK response to acknowledge receipt of the event.
     */
    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestBody String payload,
                                                      @RequestHeader("X-GitHub-Event") String githubEvent) {

        logger.info("Received GitHub webhook event: {}", githubEvent);
        logger.info("Payload: {}", payload);

        // Here is where you would trigger the analysis service based on the event type.
        // For now, we just log it and return OK.

        return ResponseEntity.ok("Webhook received.");
    }
}
