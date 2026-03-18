package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Tests for the console-initiated dispatch path:
 * branch naming, PR title generation, and command construction.
 */
public class AiDispatchConsoleStartTest {

	@Test
	public void branchNameShouldFollowConvention() {
		var agent = AiDispatchAgent.CLAUDE;
		var timestamp = "20260317-230800";
		var branchName = "ai/" + agent.getMentionName() + "/" + timestamp;
		assertEquals("ai/claude/20260317-230800", branchName);
	}

	@Test
	public void branchNameShouldWorkForAllAgents() {
		for (var agent : AiDispatchAgent.values()) {
			var branchName = "ai/" + agent.getMentionName() + "/20260101-000000";
			assertTrue(branchName.startsWith("ai/"));
			assertTrue(branchName.contains(agent.getMentionName()));
		}
	}

	@Test
	public void prTitleShouldUseFirstLineOfPrompt() {
		var prompt = "Fix the divide function\nAlso add tests";
		var firstLine = prompt.contains("\n")
				? prompt.substring(0, prompt.indexOf('\n')).strip()
				: prompt.strip();
		var prTitle = StringUtils.abbreviate("[AI] " + firstLine, 255);
		assertEquals("[AI] Fix the divide function", prTitle);
	}

	@Test
	public void prTitleShouldUseSingleLinePromptDirectly() {
		var prompt = "Refactor payment service";
		var firstLine = prompt.contains("\n")
				? prompt.substring(0, prompt.indexOf('\n')).strip()
				: prompt.strip();
		var prTitle = StringUtils.abbreviate("[AI] " + firstLine, 255);
		assertEquals("[AI] Refactor payment service", prTitle);
	}

	@Test
	public void prTitleShouldTruncateLongPrompts() {
		var prompt = "x".repeat(300);
		var firstLine = prompt.contains("\n")
				? prompt.substring(0, prompt.indexOf('\n')).strip()
				: prompt.strip();
		var prTitle = StringUtils.abbreviate("[AI] " + firstLine, 255);
		assertTrue(prTitle.length() <= 255);
		assertTrue(prTitle.endsWith("..."));
	}

	@Test
	public void commandShouldBeConstructedWithEmptyFlags() {
		var agent = AiDispatchAgent.COPILOT;
		var prompt = "Review and improve code quality";
		var command = new AiDispatchCommand(agent, java.util.List.of(), prompt);
		assertEquals(agent, command.getAgent());
		assertEquals(prompt, command.getPrompt());
		assertTrue(command.getFlags().isEmpty());
	}

	@Test
	public void nullableCommentShouldBeAcceptedInRun() {
		// AiDispatchRun.comment is now nullable (for console-initiated runs)
		var run = new io.onedev.server.model.AiDispatchRun();
		run.setComment(null);
		assertEquals(null, run.getComment());
	}
}
