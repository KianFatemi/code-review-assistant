package io.github.kianfatemi.code_review_assistant.service;

import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;



@Service
public class GitHubService {

    public static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Value("${github.api.token}")
    private String apiToken;

    private GitHub github;

    @PostConstruct
    private void init()
    {
        try
        {
            logger.info("Initializing GitHub service...");
            this.github = new GitHubBuilder().withOAuthToken(apiToken).build();
            this.github.checkApiUrlValidity();
            logger.info("Successfully connected to GitHub as user: {}", github.getMyself().getLogin());
        } catch (IOException e)
        {
            logger.error("Failed to connect to GitHub API. Please check your personal access token.", e);
        }
    }

    public GHHook createWebhook(String repoName, String webhookUrl) {
        try {
            GHRepository repo = github.getRepository(repoName);
            logger.info("Creating webhook for repository: {}", repoName);

            List<org.kohsuke.github.GHEvent> events = Arrays.asList(
                    org.kohsuke.github.GHEvent.PUSH,
                    org.kohsuke.github.GHEvent.PULL_REQUEST
            );

            Map<String, String> config = Map.of(
                    "url", webhookUrl,
                    "content_type", "json"
            );

            GHHook hook = repo.createHook("web", config, events, true);
            logger.info("Successfully created webhook with ID: {} for repository: {}", hook.getId(), repoName);
            return hook;
        } catch (IOException e) {
            logger.error("Could not create webhook for repository: {}", repoName, e);
            return null;
        }
    }

    public List<GHRepository> getUserRepositories() {
        try {
            return new ArrayList<>(github.getMyself().listRepositories(100).toList());

        } catch (IOException e) {
            logger.error("Could not fetch user repositories", e);
            return Collections.emptyList();
        }
    }

    public String getFileContent(String repoName, String filePath, String commitSha) {
        try {
            GHRepository repo = github.getRepository(repoName);
            GHContent content = repo.getFileContent(filePath, commitSha);
            try (InputStream is = content.read()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Failed to get content for file '{}' in repo '{}'", filePath, repoName, e);
            return null;
        }
    }
}
