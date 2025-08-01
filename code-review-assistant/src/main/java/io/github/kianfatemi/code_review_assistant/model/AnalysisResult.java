package io.github.kianfatemi.code_review_assistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Data
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repositoryName;

    private String filePath;

    private int lineNumber;

    private String commitSha;

    private String ruleId; // "PUBLIC_STATIC_NON_FINAL"

    private String message; // "Public static field 'BAD_FIELD' should be final"

    private LocalDateTime detectedAt;
}

