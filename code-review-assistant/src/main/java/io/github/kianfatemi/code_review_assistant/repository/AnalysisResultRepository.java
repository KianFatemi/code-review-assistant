package io.github.kianfatemi.code_review_assistant.repository;

import io.github.kianfatemi.code_review_assistant.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByRepositoryName(String repositoryName);

    List<AnalysisResult> findByCommitSha(String commitSha);
}
