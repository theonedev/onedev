package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.onedev.server.model.support.administration.CopilotApiSetting;

public class DefaultCopilotApiManagerTest {

	@Test
	public void shouldBuildModelsUrlAndModelSetting() {
		var setting = new CopilotApiSetting();
		setting.setEndpoint("http://127.0.0.1:4141/v1/");
		setting.setModel("gpt-5.4");
		setting.setStartupTimeoutSeconds(42);

		assertEquals("http://127.0.0.1:4141/v1/models", DefaultCopilotApiManager.getModelsUrl(setting));
		var modelSetting = DefaultCopilotApiManager.newModelSetting(setting);
		assertEquals("http://127.0.0.1:4141/v1/", modelSetting.getBaseUrl());
		assertEquals("dummy", modelSetting.getApiKey());
		assertEquals("gpt-5.4", modelSetting.getName());
		assertEquals(42, modelSetting.getTimeoutSeconds());
	}

	@Test
	public void shouldBuildDockerRunArgsWithoutLeakingBlankToken() {
		var setting = new CopilotApiSetting();
		setting.setEndpoint("http://127.0.0.1:4242/v1");
		setting.setContainerName("copilot-api-test");
		setting.setDockerImage("copilot-api-image");
		setting.setAuthDataDir("/tmp/copilot-auth");
		setting.setGitHubToken("   ");

		var args = DefaultCopilotApiManager.buildDockerRunArgs(setting);
		assertTrue(args.contains("4242:4141"));
		assertTrue(args.contains("/tmp/copilot-auth:/root/.local/share/copilot-api"));
		assertFalse(args.stream().anyMatch(it -> it.startsWith("GH_TOKEN=")));
		assertEquals("copilot-api-image", args.get(args.size()-1));
	}

	@Test
	public void shouldIncludeGithubTokenWhenProvided() {
		var setting = new CopilotApiSetting();
		setting.setGitHubToken("ghu_test");

		var args = DefaultCopilotApiManager.buildDockerRunArgs(setting);
		assertTrue(args.contains("GH_TOKEN=ghu_test"));
	}

}
