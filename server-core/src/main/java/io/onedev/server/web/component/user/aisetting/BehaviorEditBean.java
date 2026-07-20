package io.onedev.server.web.component.user.aisetting;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;

@Editable
public class BehaviorEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String systemPrompt;

	private boolean proactive;

	@Editable(order=100, description="""
			Optional instructions that define how the AI user should behave""")
	@Multiline
	@Nullable
	public String getSystemPrompt() {
		return systemPrompt;
	}

	public void setSystemPrompt(@Nullable String systemPrompt) {
		this.systemPrompt = systemPrompt;
	}

	@Editable(order=200, description="""
			If enabled, when the AI user is involved in an issue or pull request \
			(assigned, reviewing, or previously mentioned), it will check each comment \
			even if not mentioned, and respond when relevant. This provides a better \
			experience but may consume more tokens""")
	public boolean isProactive() {
		return proactive;
	}

	public void setProactive(boolean proactive) {
		this.proactive = proactive;
	}

}
