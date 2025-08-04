package io.github.kianfatemi.code_review_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import io.github.kianfatemi.code_review_assistant.model.AnalysisResult;
import io.github.kianfatemi.code_review_assistant.repository.AnalysisResultRepository;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private final GitHubService gitHubService;
    private final AnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AIService aiService;

    public AnalysisService(GitHubService gitHubService, AnalysisResultRepository analysisResultRepository, AIService aiService) {
        this.gitHubService = gitHubService;
        this.analysisResultRepository = analysisResultRepository;
        this.aiService = aiService;
    }

    public void analyzePullRequestEvent(String payload) {
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            String action = rootNode.path("action").asText();

            if (!"opened".equals(action) && !"synchronize".equals(action)) {
                logger.info("Ignoring PR event with action: {}", action);
                return;
            }
            JsonNode prNode = rootNode.path("pull_request");
            String repoName = rootNode.path("repository").path("full_name").asText();
            int prNumber = prNode.path("number").asInt();
            String commitSha = prNode.path("head").path("sha").asText();

            logger.info("Analyzing PR #{} in repository '{}' at commit '{}'", prNumber, repoName, commitSha);

            String diff = gitHubService.getPullRequestDiff(repoName, prNumber);
            if (diff != null && !diff.isEmpty()) {
                logger.info("Sending PR diff to AI service for high-level feedback.");
                String aiFeedback = aiService.getAIFeedback(diff);
                String finalComment = "### AI-Powered Code Review\n\n" + aiFeedback;
                gitHubService.postGeneralPullRequestComment(repoName, prNumber, finalComment);
            }

            GHRepository repo = gitHubService.github.getRepository(repoName);
            GHPullRequest pr = repo.getPullRequest(prNumber);

            pr.listFiles().forEach(file -> {
                String filePath = file.getFilename();
                if (!filePath.endsWith(".java")) {
                    return;
                }
                //find all violations
                List<AnalysisResult> violations = findViolationsInFile(repoName, filePath, commitSha);

                //post a comment for each violation found
                for (AnalysisResult violation : violations) {
                    gitHubService.postPullRequestComment(
                            repoName,
                            prNumber,
                            commitSha,
                            filePath,
                            violation.getLineNumber(),
                            "**Code Review Assistant** " + violation.getMessage()
                    );
                }
            });
        } catch (IOException e) {
            logger.error("Failed to parse pull request event payload", e);
        }
    }

    private void processFile(String repoName, String filePath, String commitSha) {
        if (!filePath.endsWith(".java")) return;

        List<AnalysisResult> results = findViolationsInFile(repoName, filePath, commitSha);
        results.forEach(analysisResultRepository::save);
        logger.info("Saved {} analysis results to the database for file {}", results.size(), filePath);
    }

     // Analyzes a push event from a GitHub webhook.
     public void analyzePushEvent(String payload) {
         try {
             JsonNode rootNode = objectMapper.readTree(payload);
             String repoName = rootNode.path("repository").path("full_name").asText();
             String afterCommitSha = rootNode.path("after").asText();

             logger.info("Analyzing push to repository '{}' at commit '{}'", repoName, afterCommitSha);

             rootNode.path("commits").forEach(commit -> {
                 commit.path("modified").forEach(fileNode -> processFile(repoName, fileNode.asText(), afterCommitSha));
                 commit.path("added").forEach(fileNode -> processFile(repoName, fileNode.asText(), afterCommitSha));
             });
         } catch (IOException e) {
             logger.error("Failed to parse push event payload", e);
         }
     }

    private List<AnalysisResult> findViolationsInFile(String repoName, String filePath, String commitSha) {
        logger.info("Analyzing file: {}", filePath);
        List<AnalysisResult> results = new ArrayList<>();
        String fileContent = gitHubService.getFileContent(repoName, filePath, commitSha);

        if (fileContent == null) {
            logger.error("Could not retrieve content for file: {}", filePath);
            return results;
        }
        CompilationUnit cu = StaticJavaParser.parse(fileContent);

        cu.findAll(FieldDeclaration.class).forEach(field -> {
            if (field.isPublic() && field.isStatic() && !field.isFinal()) {
                int line = field.getRange().map(r -> r.begin.line).orElse(-1);
                String message = String.format("Public static field '%s' should be final.", field.getVariable(0).getNameAsString());
                logger.warn("VIOLATION in {}: Line {}: Public static field '{}' should be final.",
                        filePath, line, field.getVariable(0).getNameAsString());

                AnalysisResult result = new AnalysisResult();
                result.setRepositoryName(repoName);
                result.setFilePath(filePath);
                result.setLineNumber(line);
                result.setCommitSha(commitSha);
                result.setRuleId("PUBLIC_STATIC_NON_FINAL");
                result.setMessage(message);
                result.setDetectedAt(LocalDateTime.now());
                results.add(result);
            }
        });
        return results;
    }
}
