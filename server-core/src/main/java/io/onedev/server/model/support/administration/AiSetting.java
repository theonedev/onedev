package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.AiModelSetting;

@Editable
public class AiSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_CODE_EXPLANATION_PROMPT = "Help me understand highlighted text.";

    public static final String DEFAULT_ISSUE_SUMMARY_PROMPT = "Summarize comments of current issue.";

    public static final String DEFAULT_PULL_REQUEST_SUMMARY_PROMPT = "Summarize comments of current pull request.";

    public static final String DEFAULT_BUILD_FAILURE_ISSUE_PROMPT = """
        Create an issue for the build failure:
        Title: Job '<job name>' is failed on '<ref name>'
        Description: <The build summary>
        Type: <Build Failed>
        Priority: Major
        Build: <build number>
        Assignees: <your own user name>""";

    public static final String PROP_LITE_MODEL_SETTING = "liteModelSetting";

    public static final String PROP_CODE_EXPLANATION_PROMPT = "codeExplanationPrompt";

    public static final String PROP_ISSUE_SUMMARY_PROMPT = "issueSummaryPrompt";

    public static final String PROP_PULL_REQUEST_SUMMARY_PROMPT = "pullRequestSummaryPrompt";

    public static final String PROP_BUILD_FAILURE_ISSUE_PROMPT = "buildFailureIssuePrompt";
    
    public static final String PROP_CHAT_PRESERVE_DAYS = "chatPreserveDays";
    
    private AiModelSetting liteModelSetting;

    private String codeExplanationPrompt = DEFAULT_CODE_EXPLANATION_PROMPT;

    private String issueSummaryPrompt = DEFAULT_ISSUE_SUMMARY_PROMPT;

    private String pullRequestSummaryPrompt = DEFAULT_PULL_REQUEST_SUMMARY_PROMPT;

    private String buildFailureIssuePrompt = DEFAULT_BUILD_FAILURE_ISSUE_PROMPT;
    
    private int chatPreserveDays = 30;
    
    @Editable(order=100)
    @Nullable
    public AiModelSetting getLiteModelSetting() {
        return liteModelSetting;
    }

    public void setLiteModelSetting(AiModelSetting liteModelSetting) {
        this.liteModelSetting = liteModelSetting;
    }

    @Nullable
    public ChatModel getLiteModel() {
        return liteModelSetting != null ? liteModelSetting.getChatModel() : null;
    }

    @Editable(order=200, description="Prompt to use when explaining highlighted code. Display language will be appended automatically")
    @Multiline
    @NotEmpty
    public String getCodeExplanationPrompt() {
        return codeExplanationPrompt;
    }

    public void setCodeExplanationPrompt(String codeExplanationPrompt) {
        this.codeExplanationPrompt = codeExplanationPrompt;
    }

    @Editable(order=300, description="Prompt to use when summarizing issue comments. Display language will be appended automatically")
    @Multiline
    @NotEmpty
    public String getIssueSummaryPrompt() {
        return issueSummaryPrompt;
    }

    public void setIssueSummaryPrompt(String issueSummaryPrompt) {
        this.issueSummaryPrompt = issueSummaryPrompt;
    }

    @Editable(order=400, description="Prompt to use when summarizing pull request comments. Display language will be appended automatically")
    @Multiline
    @NotEmpty
    public String getPullRequestSummaryPrompt() {
        return pullRequestSummaryPrompt;
    }

    public void setPullRequestSummaryPrompt(String pullRequestSummaryPrompt) {
        this.pullRequestSummaryPrompt = pullRequestSummaryPrompt;
    }

    @Editable(order=500, description="Prompt to use when creating an issue for a failed build. Display language will be appended automatically")
    @Multiline
    @NotEmpty
    public String getBuildFailureIssuePrompt() {
        return buildFailureIssuePrompt;
    }

    public void setBuildFailureIssuePrompt(String buildFailureIssuePrompt) {
        this.buildFailureIssuePrompt = buildFailureIssuePrompt;
    }

    @Editable(order=600)
    @Min(value = 1, message = "At least 1 day should be specified")
    @OmitName
    public int getChatPreserveDays() {
        return chatPreserveDays;
    }

    public void setChatPreserveDays(int chatPreserveDays) {
        this.chatPreserveDays = chatPreserveDays;
    }

}
