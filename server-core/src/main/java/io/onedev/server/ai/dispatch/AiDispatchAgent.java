package io.onedev.server.ai.dispatch;

import org.jspecify.annotations.Nullable;

public enum AiDispatchAgent {

	CLAUDE("claude"),
	COPILOT("copilot"),
	CODEX("codex");

	private final String mentionName;

	AiDispatchAgent(String mentionName) {
		this.mentionName = mentionName;
	}

	public String getMentionName() {
		return mentionName;
	}

	@Nullable
	public static AiDispatchAgent fromMentionName(String mentionName) {
		for (var agent: values()) {
			if (agent.getMentionName().equalsIgnoreCase(mentionName))
				return agent;
		}
		return null;
	}

}
