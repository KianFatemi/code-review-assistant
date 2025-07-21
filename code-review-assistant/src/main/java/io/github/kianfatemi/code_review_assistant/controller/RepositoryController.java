package io.github.kianfatemi.code_review_assistant.controller;

import io.github.kianfatemi.code_review_assistant.model.RepositoryConfig;
import io.github.kianfatemi.code_review_assistant.service.GitHubService;
import io.github.kianfatemi.code_review_assistant.repository.RepositoryConfigRepository;

import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);

    @Autowired
    private GitHubService githubService;

    @Autowired
    private RepositoryConfigRepository repoConfigRepository;

    // displays a list of the users repos and their analysis status
    @GetMapping("/repositories")
    public String listRepositories(Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return "redirect:/";
        }

        List<GHRepository> userRepos = githubService.getUserRepositories();

        List<String> configuredRepoNames = repoConfigRepository.findAll().stream()
                .map(RepositoryConfig::getRepositoryName)
                .collect(Collectors.toList());

        model.addAttribute("repos", userRepos);
        model.addAttribute("configuredRepos", configuredRepoNames);
        model.addAttribute("username", user.getAttribute("login"));

        return "repositories";
    }

    @PostMapping("/repositories/configue")
    public String configureRepository(@RequestParam String repoName, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return "redirect:/";
        }

        logger.info("User '{}' requested to configure repository: {}", user.getAttribute("login"), repoName);

        repoConfigRepository.findByRepositoryName(repoName).ifPresentOrElse(
                config -> logger.info("Repository {} is already configured. Toggling status in a future step.", repoName),
                () -> {
                    RepositoryConfig newConfig = new RepositoryConfig();
                    newConfig.setRepositoryName(repoName);
                    newConfig.setActive(true);
                    newConfig.setConfiguredByUserId(user.getAttribute("login"));
                    repoConfigRepository.save(newConfig);
                    logger.info("Saved new configuration for repository: {}", repoName);
                }
        );
        return "redirect:/repositories";
    }
}
