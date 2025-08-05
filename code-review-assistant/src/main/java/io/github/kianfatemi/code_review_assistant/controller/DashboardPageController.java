package io.github.kianfatemi.code_review_assistant.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class DashboardPageController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("username", user.getAttribute("login"));
        return "dashboard";
    }
}
