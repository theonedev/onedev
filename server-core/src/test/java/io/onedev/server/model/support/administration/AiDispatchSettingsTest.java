package io.onedev.server.model.support.administration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.onedev.server.ai.dispatch.AiDispatchAgent;

public class AiDispatchSettingsTest {

	@Test
	public void shouldDefaultCopilotToCopilotApiBackend() {
		var setting = AiDispatchAgentSetting.forAgent(AiDispatchAgent.COPILOT);
		assertEquals(AiDispatchBackend.COPILOT_API, setting.getBackend());
		assertEquals("copilot", setting.getCommand());
	}

	@Test
	public void shouldLazilyInitializeCopilotApiSetting() {
		var setting = new AiSetting();
		setting.setCopilotApiSetting(null);

		assertNotNull(setting.getCopilotApiSetting());
		assertEquals("http://127.0.0.1:4141/v1", setting.getCopilotApiSetting().getEndpoint());
		assertEquals("gpt-4.1", setting.getCopilotApiSetting().getModel());
	}

	@Test
	public void shouldMigrateUntouchedCopilotDispatchDefaults() {
		var setting = new AiSetting();
		var copilot = new AiDispatchAgentSetting();
		copilot.setCommand("copilot");
		copilot.setPromptOption("-p");
		copilot.setBackend(AiDispatchBackend.AUTO);
		setting.setCopilotDispatchSetting(copilot);

		assertEquals(AiDispatchBackend.COPILOT_API, setting.getCopilotDispatchSetting().getBackend());
	}

	@Test
	public void shouldMigrateOldCopilotApiDefaultModel() {
		var setting = new AiSetting();
		var copilotApi = new CopilotApiSetting();
		copilotApi.setModel("gpt-5.4");
		setting.setCopilotApiSetting(copilotApi);

		assertEquals("gpt-4.1", setting.getCopilotApiSetting().getModel());
	}

}
