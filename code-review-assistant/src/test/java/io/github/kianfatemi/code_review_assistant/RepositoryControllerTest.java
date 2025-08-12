package io.github.kianfatemi.code_review_assistant;

import io.github.kianfatemi.code_review_assistant.controller.RepositoryController;
import io.github.kianfatemi.code_review_assistant.repository.RepositoryConfigRepository;
import io.github.kianfatemi.code_review_assistant.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;


@WebMvcTest(RepositoryController.class)
public class RepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private  GitHubService gitHubService;

    @MockBean
    private RepositoryConfigRepository repoConfigRepository;

    @Test
    @WithMockUser
    void listRepositoryTest() throws Exception {
        GHRepository mockRepo = mock(GHRepository.class);
        when(mockRepo.getFullName()).thenReturn("user/test-repo");
        when(gitHubService.getUserRepositories()).thenReturn(Collections.singletonList(mockRepo));
        when(repoConfigRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/repositories").with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(view().name("repositories"))
                .andExpect(model().attributeExists("repos"))
                .andExpect(model().attributeExists("configuredRepos"));
    }
}
