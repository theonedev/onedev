package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class AiDispatchCommentParserTest {

	private final AiDispatchCommentParser parser = new AiDispatchCommentParser();

	@Test
	public void shouldParseLineLeadingMention() {
		var command = parser.parse("@claude fix the null check").get();
		assertEquals(AiDispatchAgent.CLAUDE, command.getAgent());
		assertEquals(List.of(), command.getFlags());
		assertEquals("fix the null check", command.getPrompt());
	}

	@Test
	public void shouldParseFlagsCaseInsensitively() {
		var command = parser.parse("  @CoPiLoT --THINK --no-commit review this PR").get();
		assertEquals(AiDispatchAgent.COPILOT, command.getAgent());
		assertEquals(List.of("--think", "--no-commit"), command.getFlags());
		assertEquals("review this PR", command.getPrompt());
		assertTrue(command.hasFlag("--no-commit"));
	}

	@Test
	public void shouldParseCodexMention() {
		var command = parser.parse("@codex tighten this validation and add coverage").get();
		assertEquals(AiDispatchAgent.CODEX, command.getAgent());
		assertEquals(List.of(), command.getFlags());
		assertEquals("tighten this validation and add coverage", command.getPrompt());
	}

	@Test
	public void shouldIgnoreMidSentenceMentions() {
		assertFalse(parser.parse("please ask @claude to review this").isPresent());
	}

	@Test
	public void shouldParseMentionOnLaterLine() {
		var command = parser.parse("Some context first\n@claude fix it").get();
		assertEquals("fix it", command.getPrompt());
	}

	@Test
	public void shouldKeepTrailingLinesInPrompt() {
		var command = parser.parse("@claude --think refactor payment service\nbehind an interface").get();
		assertEquals(List.of("--think"), command.getFlags());
		assertEquals("refactor payment service\nbehind an interface", command.getPrompt());
	}

	@Test
	public void shouldRejectEmptyPrompt() {
		assertFalse(parser.parse("@claude --think").isPresent());
	}

	@Test
	public void shouldParseModelFlagWithSeparateValue() {
		var command = parser.parse("@copilot --model gpt-4.1 fix the failing tests").get();
		assertEquals(AiDispatchAgent.COPILOT, command.getAgent());
		assertEquals("gpt-4.1", command.getModelName());
		assertEquals(List.of(), command.getFlags());
		assertEquals("fix the failing tests", command.getPrompt());
	}

	@Test
	public void shouldParseModelFlagWithEqualsSyntax() {
		var command = parser.parse("@claude --model=claude-3-7-sonnet --think improve this flow").get();
		assertEquals("claude-3-7-sonnet", command.getModelName());
		assertEquals(List.of("--think"), command.getFlags());
		assertEquals("improve this flow", command.getPrompt());
	}

}
