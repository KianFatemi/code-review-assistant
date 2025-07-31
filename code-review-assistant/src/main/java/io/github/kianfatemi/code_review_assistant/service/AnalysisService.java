package io.github.kianfatemi.code_review_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class AnalysisService {
    public static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private GitHubService gitHubService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void analyzePushEvent(String payload) {
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            String repoName = rootNode.path("repository").path("full_name").asText();
            String afterCommitSha = rootNode.path("after").asText();

            logger.info("Analyzing push to repository '{}' at commit '{}'", repoName, afterCommitSha);

            JsonNode commits = rootNode.path("commits");

            for (JsonNode commit : commits) {
                commit.path("added").forEach(fileNode -> {
                    String filePath = fileNode.asText();
                    if (filePath.endsWith(".java")) {
                        analyzeFile(repoName, filePath, afterCommitSha);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Failed to parse webhook payload", e);
        }
    }

    private void analyzeFile(String repoName, String filePath, String commitSha) {
        logger.info("Analyzing file: {}", filePath);
        String fileContent = gitHubService.getFileContent(repoName, filePath, commitSha);

        if (fileContent == null) {
            logger.error("Could not retrieve content for file: {}", filePath);
            return;
        }

        CompilationUnit cu = StaticJavaParser.parse(fileContent);

        cu.findAll(FieldDeclaration.class).forEach(field -> {
            if (field.isPublic() && field.isStatic() && !field.isFinal()) {
                int line = field.getRange().map(r -> r.begin.line).orElse(-1);
                logger.warn("VIOLATION in {}: Line {}: Public static field '{}' should be final.",
                    filePath, line, field.getVariable(0).getNameAsString());
            }
        });
    }
}
