package io.github.kianfatemi.code_review_assistant;

import io.github.kianfatemi.code_review_assistant.model.AnalysisResult;
import io.github.kianfatemi.code_review_assistant.repository.AnalysisResultRepository;
import io.github.kianfatemi.code_review_assistant.service.AIService;
import io.github.kianfatemi.code_review_assistant.service.AnalysisService;
import io.github.kianfatemi.code_review_assistant.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AnalysisServiceTest {

    @Mock
    private GitHubService gitHubService;

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @Mock
    private AIService aiService;

    @InjectMocks
    private AnalysisService analysisService;

    private String testJavaCode;
    private String testWebhookPayload;

    @BeforeEach
    void setUp() {
        testJavaCode = "public class TestFile {\n" +
                "    public static String BAD_FIELD = \"This should be final\";\n" +
                "}";

        // A simplified JSON payload for a push event
        testWebhookPayload = "{\"repository\":{\"full_name\":\"test/repo\"},\"after\":\"test-commit-sha\"," +
                "\"commits\":[{\"modified\":[\"src/main/java/TestFile.java\"]}]}";
    }

    @Test
    void analyzePushEventTest() {
        when(gitHubService.getFileContent("test/repo", "src/main/java/TestFile.java", "test-commit-sha"))
                .thenReturn(testJavaCode);

        analysisService.analyzePushEvent(testWebhookPayload);

        ArgumentCaptor<AnalysisResult> resultArgumentCaptor = ArgumentCaptor.forClass(AnalysisResult.class);
        verify(analysisResultRepository, times(1)).save(resultArgumentCaptor.capture());

        AnalysisResult savedResult = resultArgumentCaptor.getValue();
        assertEquals("test/repo", savedResult.getRepositoryName());
        assertEquals("src/main/java/TestFile.java", savedResult.getFilePath());
        assertEquals(2, savedResult.getLineNumber());
        assertEquals("PUBLIC_STATIC_NON_FINAL", savedResult.getRuleId());
        assertEquals("Public static field 'BAD_FIELD' should be final.", savedResult.getMessage());
    }
}
