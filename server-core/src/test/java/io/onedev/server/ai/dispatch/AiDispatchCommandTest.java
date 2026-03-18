package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class AiDispatchCommandTest {

	@Test
	public void shouldConstructConsoleCommand() {
		var command = new AiDispatchCommand(AiDispatchAgent.CLAUDE, List.of(), "Fix the divide function");
		assertEquals(AiDispatchAgent.CLAUDE, command.getAgent());
		assertEquals("Fix the divide function", command.getPrompt());
		assertTrue(command.getFlags().isEmpty());
	}

	@Test
	public void shouldPreserveFlags() {
		var command = new AiDispatchCommand(AiDispatchAgent.COPILOT,
				List.of("--think", "--no-commit"), "Review code");
		assertEquals(2, command.getFlags().size());
		assertTrue(command.hasFlag("--think"));
		assertTrue(command.hasFlag("--no-commit"));
		assertFalse(command.hasFlag("--verbose"));
	}

	@Test
	public void shouldReturnImmutableFlagList() {
		var command = new AiDispatchCommand(AiDispatchAgent.CODEX, List.of("--think"), "test");
		try {
			command.getFlags().add("--hack");
			// If we get here the list is not unmodifiable
			assertFalse("Should throw UnsupportedOperationException", true);
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void shouldSupportCodexAgent() {
		var command = new AiDispatchCommand(AiDispatchAgent.CODEX, List.of(), "refactor");
		assertEquals(AiDispatchAgent.CODEX, command.getAgent());
		assertEquals("codex", command.getAgent().getMentionName());
	}

	@Test
	public void shouldSupportMultilinePrompt() {
		var prompt = "Fix the divide function\nAlso add tests\nAnd update docs";
		var command = new AiDispatchCommand(AiDispatchAgent.CLAUDE, List.of(), prompt);
		assertTrue(command.getPrompt().contains("\n"));
		assertEquals(prompt, command.getPrompt());
	}

	@Test
	public void shouldExposeModelOverride() {
		var command = new AiDispatchCommand(AiDispatchAgent.COPILOT, List.of("--think"),
				"Refactor payment service", "gpt-4.1");
		assertEquals("gpt-4.1", command.getModelName());
		assertEquals(List.of("--think", "--model=gpt-4.1"), command.getPersistedFlags());
	}

	@Test
	public void shouldPersistFlagsWithoutModelWhenUnset() {
		var command = new AiDispatchCommand(AiDispatchAgent.CLAUDE, List.of("--no-commit"), "Review only");
		assertEquals(null, command.getModelName());
		assertEquals(List.of("--no-commit"), command.getPersistedFlags());
	}
}
