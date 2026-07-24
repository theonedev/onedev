package io.onedev.server.web.component.user.aisetting;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;

@Editable
public class BehaviorEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String systemPrompt;

	private boolean proactive;

	private int maxLoopCount;

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
			By default, AI user only responds when assigned tasks or mentioned in issue/PR context. 
			Enable this to have it respond as long as the comment is relevant even if not mentioned. 
			This provides a better interaction experience, especially in case of service desks, 
			but may consume more tokens""")
	public boolean isProactive() {
		return proactive;
	}

	public void setProactive(boolean proactive) {
		this.proactive = proactive;
	}

	@Editable(order=300, description="""
			Maximum number of times this AI user can be awakened in a single event chain. \
			When the limit is reached, the AI user will not run and a system comment will be posted instead""")
	@Min(value=1, message="At least 1 should be specified")
	public int getMaxLoopCount() {
		return maxLoopCount;
	}

	public void setMaxLoopCount(int maxLoopCount) {
		this.maxLoopCount = maxLoopCount;
	}

}
