package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.model.support.administration.AiSetting;

public class AiDispatchModelUtilsTest {

	@Test
	public void shouldOnlyOfferCopilotApiModelsForCopilotApiBackend() {
		var setting = new AiSetting();
		var liteModel = new AiModelSetting();
		liteModel.setName("openrouter/free");
		setting.setLiteModelSetting(liteModel);

		assertEquals(List.of("gpt-4.1"),
				AiDispatchModelUtils.availableModels(setting, AiDispatchAgent.COPILOT));
	}

	@Test
	public void shouldOfferLiteAndAgentModelsForNonCopilotApiAgents() {
		var setting = new AiSetting();
		var liteModel = new AiModelSetting();
		liteModel.setName("openrouter/free");
		setting.setLiteModelSetting(liteModel);

		var claudeModel = new AiModelSetting();
		claudeModel.setName("claude-agent-model");
		setting.getClaudeDispatchSetting().setModelSetting(claudeModel);

		assertEquals(List.of("openrouter/free", "claude-agent-model"),
				AiDispatchModelUtils.availableModels(setting, AiDispatchAgent.CLAUDE));
	}

}
