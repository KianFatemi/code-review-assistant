package controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles requests for the main dashboard and home page.
 */
@Controller
public class DashboardController {

    /**
     * Displays the home page. If the user is authenticated, it shows their GitHub username.
     *
     * @param model The Spring Model to add attributes to for the view.
     * @param oauth2User The authenticated user object, injected by Spring Security.
     * This will be null if the user is not logged in.
     * @return The name of the Thymeleaf template to render (in this case, "index").
     */
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User != null) {
            // If the user is logged in, get their GitHub login name and add it to the model.
            String username = oauth2User.getAttribute("login");
            model.addAttribute("username", username);
        }
        // We will create an 'index.html' file later to display this.
        return "index";
    }
}
