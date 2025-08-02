package io.github.kianfatemi.code_review_assistant.controller;

import io.github.kianfatemi.code_review_assistant.model.AnalysisResult;
import io.github.kianfatemi.code_review_assistant.repository.AnalysisResultRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AnalysisResultController {

    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisResultController(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    @GetMapping("/results")
    public String viewResults(@RequestParam String repoName, Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return "redirect:/";
        }

        List<AnalysisResult> results = analysisResultRepository.findByRepositoryName(repoName);

        model.addAttribute("results", results);
        model.addAttribute("repoName", repoName);
        model.addAttribute("username", user.getAttribute("login"));

        return "results";
    }
}
