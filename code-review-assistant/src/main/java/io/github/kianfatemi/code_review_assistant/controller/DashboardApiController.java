package io.github.kianfatemi.code_review_assistant.controller;

import io.github.kianfatemi.code_review_assistant.model.ViolationCountDTO;
import io.github.kianfatemi.code_review_assistant.repository.AnalysisResultRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final AnalysisResultRepository analysisResultRepository;


    public DashboardApiController(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    @GetMapping("/stats")
    public List<ViolationCountDTO> getViolationsStats() {
        return analysisResultRepository.countViolationsByDate();
    }
}
