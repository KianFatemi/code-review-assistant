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

    public GitHub github;

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

    public void postPullRequestComment(String repoName, int prNumber, String commitSha, String filePath, int line, String body) {
        try {
            GHRepository repo = github.getRepository(repoName);
            GHPullRequest pr = repo.getPullRequest(prNumber);

            pr.createReviewComment(body, commitSha, filePath, line);

            logger.info("Successfully posted comment to PR #{} in repository {}", prNumber, repoName);
        } catch (IOException e) {
            logger.error("Failed to post comment to PR #{} in repository {}", prNumber, repoName, e);
        }
    }

    public void postGeneralPullRequestComment(String repoName, int prNumber, String body) {
        try {
            GHRepository repo = github.getRepository(repoName);
            GHPullRequest pr = repo.getPullRequest(prNumber);
            pr.comment(body);
            logger.info("Successfully posted general comment to PR #{} in repository {}", prNumber, repoName);
        } catch (IOException e) {
            logger.error("Failed to post general comment to PR #{} in repository {}", prNumber, repoName, e);
        }
    }

    public String getPullRequestDiff(String repoName, int prNumber) {
        try {
            GHRepository repo = github.getRepository(repoName);
            GHPullRequest pr = repo.getPullRequest(prNumber);
            StringBuilder diffBuilder = new StringBuilder();
            for (GHPullRequestFileDetail file : pr.listFiles()) {
                diffBuilder.append("diff --git a/").append(file.getFilename()).append(" b/").append(file.getFilename()).append("\n");
                diffBuilder.append("--- a/").append(file.getFilename()).append("\n");
                diffBuilder.append("+++ b/").append(file.getFilename()).append("\n");
                diffBuilder.append(file.getPatch()).append("\n");
            }

            return diffBuilder.toString();

        } catch (IOException e) {
            logger.error("Failed to get diff for PR #{} in repository {}", prNumber, repoName, e);
            return null;
        }
    }
}
