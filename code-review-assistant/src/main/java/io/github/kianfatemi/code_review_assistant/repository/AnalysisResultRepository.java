package io.github.kianfatemi.code_review_assistant.repository;

import io.github.kianfatemi.code_review_assistant.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import io.github.kianfatemi.code_review_assistant.model.ViolationCountDTO;

import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByRepositoryName(String repositoryName);

    List<AnalysisResult> findByCommitSha(String commitSha);

    @Query("SELECT new io.github.kianfatemi.code_review_assistant.model.ViolationCountDTO(CAST(ar.detectedAt AS LocalDate), COUNT(ar.id)) " +
            "FROM AnalysisResult ar " +
            "GROUP BY CAST(ar.detectedAt AS LocalDate) " +
            "ORDER BY CAST(ar.detectedAt AS LocalDate) ASC")
    List<ViolationCountDTO> countViolationsByDate();
}
