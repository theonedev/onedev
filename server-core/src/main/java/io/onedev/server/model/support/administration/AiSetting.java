package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.ai.dispatch.AiDispatchAgent;
import io.onedev.server.model.support.AiModelSetting;

@Editable
public class AiSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROP_LITE_MODEL_SETTING = "liteModelSetting";
    
    public static final String PROP_CHAT_PRESERVE_DAYS = "chatPreserveDays";

	public static final String PROP_DISPATCH_ENABLED = "dispatchEnabled";

	public static final String PROP_CLAUDE_API_KEY = "claudeApiKey";

	public static final String PROP_CLAUDE_DISPATCH_SETTING = "claudeDispatchSetting";

	public static final String PROP_COPILOT_DISPATCH_SETTING = "copilotDispatchSetting";

	public static final String PROP_CODEX_DISPATCH_SETTING = "codexDispatchSetting";

	public static final String PROP_COPILOT_API_SETTING = "copilotApiSetting";

	public static final String PROP_MAX_DISPATCH_SESSIONS = "maxDispatchSessions";

    public static final String PROP_DISPATCH_TIMEOUT_MINUTES = "dispatchTimeoutMinutes";
    
    private AiModelSetting liteModelSetting;
    
    private int chatPreserveDays = 30;

	private boolean dispatchEnabled = true;

	private String claudeApiKey;

	private AiDispatchAgentSetting claudeDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.CLAUDE);

	private AiDispatchAgentSetting copilotDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.COPILOT);

	private AiDispatchAgentSetting codexDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.CODEX);

	private CopilotApiSetting copilotApiSetting = new CopilotApiSetting();

	private int maxDispatchSessions = 3;

    private int dispatchTimeoutMinutes = 30;
    
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

    @Editable(order=200)
    @Min(value = 1, message = "At least 1 day should be specified")
    @OmitName
    public int getChatPreserveDays() {
        return chatPreserveDays;
    }

    public void setChatPreserveDays(int chatPreserveDays) {
        this.chatPreserveDays = chatPreserveDays;
    }

	@Editable(order=300, name="Enable Dispatch", description="Allow pull request comments beginning with @claude, @copilot, or @codex to start AI dispatch sessions")
	public boolean isDispatchEnabled() {
		return dispatchEnabled;
	}

	public void setDispatchEnabled(boolean dispatchEnabled) {
		this.dispatchEnabled = dispatchEnabled;
	}

	@Nullable
	public String getClaudeApiKey() {
		return claudeApiKey;
	}

	public void setClaudeApiKey(@Nullable String claudeApiKey) {
		this.claudeApiKey = claudeApiKey;
	}

	@Editable(order=400, name="Claude")
	@NotNull
	@Valid
	public AiDispatchAgentSetting getClaudeDispatchSetting() {
		if (claudeDispatchSetting == null)
			claudeDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.CLAUDE);
		return claudeDispatchSetting;
	}

	public void setClaudeDispatchSetting(AiDispatchAgentSetting claudeDispatchSetting) {
		this.claudeDispatchSetting = claudeDispatchSetting;
	}

	@Editable(order=500, name="Copilot")
	@NotNull
	@Valid
	public AiDispatchAgentSetting getCopilotDispatchSetting() {
		if (copilotDispatchSetting == null)
			copilotDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.COPILOT);
		else if (copilotDispatchSetting.getBackend() == AiDispatchBackend.AUTO
				&& copilotDispatchSetting.getModelSetting() == null
				&& org.apache.commons.lang3.StringUtils.isBlank(copilotDispatchSetting.getCommandArguments())
				&& org.apache.commons.lang3.StringUtils.isBlank(copilotDispatchSetting.getApiKeyEnvName())
				&& org.apache.commons.lang3.StringUtils.isBlank(copilotDispatchSetting.getApiKey())
				&& org.apache.commons.lang3.StringUtils.isBlank(copilotDispatchSetting.getBaseUrlEnvName())
				&& org.apache.commons.lang3.StringUtils.isBlank(copilotDispatchSetting.getBaseUrl())
				&& "copilot".equals(org.apache.commons.lang3.StringUtils.defaultIfBlank(copilotDispatchSetting.getCommand(), "copilot"))
				&& "-p".equals(org.apache.commons.lang3.StringUtils.defaultIfBlank(copilotDispatchSetting.getPromptOption(), "-p")))
			copilotDispatchSetting.setBackend(AiDispatchBackend.COPILOT_API);
		return copilotDispatchSetting;
	}

	public void setCopilotDispatchSetting(AiDispatchAgentSetting copilotDispatchSetting) {
		this.copilotDispatchSetting = copilotDispatchSetting;
	}

	@Editable(order=600, name="Codex")
	@NotNull
	@Valid
	public AiDispatchAgentSetting getCodexDispatchSetting() {
		if (codexDispatchSetting == null)
			codexDispatchSetting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.CODEX);
		return codexDispatchSetting;
	}

	public void setCodexDispatchSetting(AiDispatchAgentSetting codexDispatchSetting) {
		this.codexDispatchSetting = codexDispatchSetting;
	}

	@Editable(order=650, name="Copilot API")
	@NotNull
	@Valid
	public CopilotApiSetting getCopilotApiSetting() {
		if (copilotApiSetting == null)
			copilotApiSetting = new CopilotApiSetting();
		else if ("gpt-5.4".equals(copilotApiSetting.getModel())
				&& "http://127.0.0.1:4141/v1".equals(copilotApiSetting.getEndpoint())
				&& "/home/default/githubprojects/copilot-api".equals(copilotApiSetting.getProjectPath())
				&& "onedev-copilot-api".equals(copilotApiSetting.getDockerImage())
				&& "onedev-copilot-api".equals(copilotApiSetting.getContainerName()))
			copilotApiSetting.setModel("gpt-4.1");
		return copilotApiSetting;
	}

	public void setCopilotApiSetting(CopilotApiSetting copilotApiSetting) {
		this.copilotApiSetting = copilotApiSetting;
	}

	public AiDispatchAgentSetting getDispatchAgentSetting(AiDispatchAgent agent) {
		return switch (agent) {
		case CLAUDE -> getClaudeDispatchSetting();
		case COPILOT -> getCopilotDispatchSetting();
		case CODEX -> getCodexDispatchSetting();
		};
	}

	@Editable(order=700, name="Max Concurrent Sessions")
	@Min(value = 1, message = "At least one session should be allowed")
	public int getMaxDispatchSessions() {
		return maxDispatchSessions;
    }

    public void setMaxDispatchSessions(int maxDispatchSessions) {
        this.maxDispatchSessions = maxDispatchSessions;
    }

	@Editable(order=800, name="Session Timeout (Minutes)")
	@Min(value = 1, message = "At least one minute should be specified")
	public int getDispatchTimeoutMinutes() {
		return dispatchTimeoutMinutes;
    }

    public void setDispatchTimeoutMinutes(int dispatchTimeoutMinutes) {
        this.dispatchTimeoutMinutes = dispatchTimeoutMinutes;
    }

}
