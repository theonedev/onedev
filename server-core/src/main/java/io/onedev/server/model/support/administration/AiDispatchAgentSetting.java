package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.ai.dispatch.AiDispatchAgent;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.util.EditContext;

@Editable
public class AiDispatchAgentSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private AiDispatchBackend backend = AiDispatchBackend.AUTO;

	private AiModelSetting modelSetting;

	private String command;

	private String promptOption = "-p";

	private String commandArguments;

	private String apiKeyEnvName;

	private String apiKey;

	private String baseUrlEnvName;

	private String baseUrl;

	public static AiDispatchAgentSetting forAgent(AiDispatchAgent agent) {
		var setting = new AiDispatchAgentSetting();
		setting.setCommand(agent.getMentionName());
		if (agent == AiDispatchAgent.CLAUDE) {
			setting.setApiKeyEnvName("ANTHROPIC_API_KEY");
			setting.setCommandArguments("--dangerously-skip-permissions");
		} else if (agent == AiDispatchAgent.COPILOT) {
			setting.setBackend(AiDispatchBackend.COPILOT_API);
		}
		return setting;
	}

	@Editable(order=100, description="""
		Select how this mention should run. <b>AUTO</b> prefers an agent-specific model, then the shared Lite AI Model,
		and finally falls back to the configured CLI command. <b>COPILOT_API</b> is intended for <tt>@copilot</tt> and routes
		through the local <tt>copilot-api</tt> proxy configuration.""")
	public AiDispatchBackend getBackend() {
		return backend;
	}

	public void setBackend(AiDispatchBackend backend) {
		this.backend = backend;
	}

	@Editable(order=200, name="OpenAI Compatible Model", description="""
		Used when backend is <b>MODEL</b>, or preferred first when backend is <b>AUTO</b>. This also works for
		Copilot-style or Codex-style OpenAI compatible endpoints""")
	@ShowCondition("isModelConfigVisible")
	@Nullable
	public AiModelSetting getModelSetting() {
		return modelSetting;
	}

	public void setModelSetting(@Nullable AiModelSetting modelSetting) {
		this.modelSetting = modelSetting;
	}

	@Editable(order=300, name="CLI Command", placeholder="Leave empty to use mention name", description="""
		Executable to launch when backend resolves to CLI. For instance: <tt>claude</tt>, <tt>codex</tt>, or a wrapper script""")
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getCommand() {
		return command;
	}

	public void setCommand(@Nullable String command) {
		this.command = command;
	}

	@Editable(order=350, name="Prompt Option", placeholder="-p", description="""
		If specified, initial prompt is passed as <tt>{option} {prompt}</tt>. Leave empty to write the initial prompt to stdin after startup""")
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getPromptOption() {
		return promptOption;
	}

	public void setPromptOption(@Nullable String promptOption) {
		this.promptOption = promptOption;
	}

	@Editable(order=400, name="CLI Arguments", placeholder="One argument per line", description="""
		Optional extra CLI arguments. Each non-empty line becomes one process argument without shell parsing""")
	@Multiline
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getCommandArguments() {
		return commandArguments;
	}

	public void setCommandArguments(@Nullable String commandArguments) {
		this.commandArguments = commandArguments;
	}

	@Editable(order=500, name="API Key Environment Variable", placeholder="ANTHROPIC_API_KEY", description="""
		If specified together with an API key, the key is injected into the launched CLI process via this environment variable""")
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getApiKeyEnvName() {
		return apiKeyEnvName;
	}

	public void setApiKeyEnvName(@Nullable String apiKeyEnvName) {
		this.apiKeyEnvName = apiKeyEnvName;
	}

	@Editable(order=550, name="API Key", description="Optional secret to pass to the CLI process")
	@Password
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(@Nullable String apiKey) {
		this.apiKey = apiKey;
	}

	@Editable(order=600, name="Base URL Environment Variable", placeholder="OPENAI_BASE_URL", description="""
		Optional environment variable name used to pass a custom API endpoint to the launched CLI process""")
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getBaseUrlEnvName() {
		return baseUrlEnvName;
	}

	public void setBaseUrlEnvName(@Nullable String baseUrlEnvName) {
		this.baseUrlEnvName = baseUrlEnvName;
	}

	@Editable(order=650, name="Base URL", placeholder="https://example.invalid/v1", description="Optional endpoint value to inject into CLI environment")
	@Pattern(regexp="https?://.+", message="Base URL should be a valid http/https URL")
	@ShowCondition("isCliConfigVisible")
	@Nullable
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(@Nullable String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public boolean hasModelSetting() {
		return modelSetting != null;
	}

	public Iterable<String> getCommandArgumentList() {
		if (StringUtils.isBlank(commandArguments))
			return java.util.List.of();
		var args = new java.util.ArrayList<String>();
		for (var line: StringUtils.split(commandArguments, '\n')) {
			var arg = StringUtils.trimToNull(line);
			if (arg != null)
				args.add(arg);
		}
		return args;
	}

	@SuppressWarnings("unused")
	private static boolean isModelConfigVisible() {
		var backend = getBackendFromContext();
		return backend != AiDispatchBackend.CLI && backend != AiDispatchBackend.COPILOT_API;
	}

	@SuppressWarnings("unused")
	private static boolean isCliConfigVisible() {
		var backend = getBackendFromContext();
		return backend != AiDispatchBackend.MODEL && backend != AiDispatchBackend.COPILOT_API;
	}

	private static AiDispatchBackend getBackendFromContext() {
		var value = EditContext.get().getInputValue("backend");
		if (value instanceof AiDispatchBackend)
			return (AiDispatchBackend) value;
		return AiDispatchBackend.AUTO;
	}

}
