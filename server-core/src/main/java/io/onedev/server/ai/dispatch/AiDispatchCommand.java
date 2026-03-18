package io.onedev.server.ai.dispatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

public class AiDispatchCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	private final AiDispatchAgent agent;

	private final List<String> flags;

	private final String prompt;

	private final String modelName;

	public AiDispatchCommand(AiDispatchAgent agent, List<String> flags, String prompt) {
		this(agent, flags, prompt, null);
	}

	public AiDispatchCommand(AiDispatchAgent agent, List<String> flags, String prompt,
							 @Nullable String modelName) {
		this.agent = agent;
		this.flags = Collections.unmodifiableList(new ArrayList<>(flags));
		this.prompt = prompt;
		this.modelName = modelName;
	}

	public AiDispatchAgent getAgent() {
		return agent;
	}

	public List<String> getFlags() {
		return flags;
	}

	public String getPrompt() {
		return prompt;
	}

	@Nullable
	public String getModelName() {
		return modelName;
	}

	public List<String> getPersistedFlags() {
		var persisted = new ArrayList<>(flags);
		if (modelName != null && !modelName.isBlank())
			persisted.add("--model=" + modelName);
		return Collections.unmodifiableList(persisted);
	}

	public boolean hasFlag(String flag) {
		return flags.contains(flag);
	}

}
