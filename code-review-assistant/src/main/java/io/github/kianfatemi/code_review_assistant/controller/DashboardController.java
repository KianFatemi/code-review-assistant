package io.github.kianfatemi.code_review_assistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
       
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
            String username = principal.getAttribute("login");

            logger.info("User '{}' is authenticated via OAuth2. Adding username to model.", username);
            model.addAttribute("username", username);
        } else {
           
            if (authentication != null) {
                logger.warn("An authenticated user was found, but the principal is not an OAuth2User. Principal type: {}",
                        authentication.getPrincipal().getClass().getName());
            } else {
                logger.info("No authenticated user principal found in the request. Showing public login page.");
            }
        }

        return "index";
    }
}
