package io.github.kianfatemi.code_review_assistant.repository;

import io.github.kianfatemi.code_review_assistant.model.RepositoryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RepositoryConfigRepository extends JpaRepository<RepositoryConfig, Long> {

    /**
     * finds a repository configuration by its full name
     *
     * @param repositoryName
     * @return an optional containing the RepositoryConfig if found
     */
    Optional<RepositoryConfig> findByRepositoryName(String repositoryName);

    /**
     * Finds all repository configurations set up by a user
     *
     * @param userId 
     * @return A list of repository configurations
     */
    List<RepositoryConfig> findByConfiguredByUserId(String userId);
}