package io.onedev.server.ai.dispatch;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.model.support.administration.AiDispatchBackend;
import io.onedev.server.model.support.administration.AiSetting;

public class AiDispatchModelUtils {

	private AiDispatchModelUtils() {
	}

	public static List<String> availableModels(AiSetting aiSetting, @Nullable AiDispatchAgent agent) {
		var models = new LinkedHashSet<String>();
		if (agent != null) {
			var agentSetting = aiSetting.getDispatchAgentSetting(agent);
			if (agent == AiDispatchAgent.COPILOT && agentSetting.getBackend() == AiDispatchBackend.COPILOT_API) {
				addModel(models, aiSetting.getCopilotApiSetting().getModel());
				return models.stream().collect(Collectors.toList());
			}
			addModel(models, aiSetting.getLiteModelSetting());
			addModel(models, agentSetting.getModelSetting());
		} else {
			addModel(models, aiSetting.getLiteModelSetting());
		}
		return models.stream().collect(Collectors.toList());
	}

	private static void addModel(LinkedHashSet<String> models, @Nullable AiModelSetting setting) {
		if (setting != null)
			addModel(models, setting.getName());
	}

	private static void addModel(LinkedHashSet<String> models, @Nullable String modelName) {
		if (StringUtils.isNotBlank(modelName))
			models.add(modelName);
	}

}
