package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AiDispatchPromptUtilsTest {

	@Test
	public void shouldBuildIssuePromptWithDescriptionAndOperatorRequest() {
		var prompt = AiDispatchPromptUtils.buildIssuePrompt(
				"DEMO-123",
				"Fix the flaky login flow",
				"Users intermittently get redirected back to sign in.",
				"Investigate the root cause and propose a fix.");

		assertTrue(prompt.contains("Issue DEMO-123: Fix the flaky login flow"));
		assertTrue(prompt.contains("Issue description:"));
		assertTrue(prompt.contains("Users intermittently get redirected back to sign in."));
		assertTrue(prompt.contains("Operator request:"));
		assertTrue(prompt.contains("Investigate the root cause and propose a fix."));
	}

	@Test
	public void shouldOmitBlankSectionsFromIssuePrompt() {
		var prompt = AiDispatchPromptUtils.buildIssuePrompt(
				"DEMO-123",
				"Fix the flaky login flow",
				"",
				"");

		assertEquals("Issue DEMO-123: Fix the flaky login flow", prompt);
	}

}
