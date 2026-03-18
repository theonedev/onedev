package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class AiDispatchConversationTest {

@Test
public void shouldParseConversationMessages() {
var log = "[system] queued\n"
+ AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, "First prompt")
+ AiDispatchConversation.startAssistantBlock()
+ "First response"
+ AiDispatchConversation.endMessageBlock();

var messages = AiDispatchConversation.parseMessages(log);

assertEquals(2, messages.size());
assertTrue(messages.get(0).isUser());
assertTrue(messages.get(1).isAssistant());
assertEquals("First prompt", messages.get(0).getContent());
assertEquals("First response", messages.get(1).getContent());
}

@Test
public void shouldStripOnlyConversationMarkupFromTerminalLog() {
var log = "[system] queued\n"
+ AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, "First prompt")
+ AiDispatchConversation.startAssistantBlock()
+ "First response"
+ AiDispatchConversation.endMessageBlock()
+ "> follow up\n"
+ AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, "follow up");

assertEquals("[system] queued\nFirst response> follow up\n", AiDispatchConversation.stripMarkup(log));
}

@Test
public void shouldKeepOpenAssistantBlockVisible() {
var log = AiDispatchConversation.startAssistantBlock() + "streaming";
var messages = AiDispatchConversation.parseMessages(log);

assertEquals(1, messages.size());
assertEquals("streaming", messages.get(0).getContent());
assertEquals("streaming", AiDispatchConversation.stripMarkup(log));
}

	@Test
	public void shouldBuildSummaryWithPromptsAndResponses() {
		var messages = List.of(
				new AiDispatchConversation.Message(AiDispatchConversation.Role.USER, "Fix the divide function"),
				new AiDispatchConversation.Message(AiDispatchConversation.Role.ASSISTANT, "The issue is in `calc.py`:\n```python\nreturn a * b  # should be a / b\n```"),
				new AiDispatchConversation.Message(AiDispatchConversation.Role.SYSTEM, "Session completed.")
		);

var summary = AiDispatchConversation.buildSummary(messages, 5000);
assertTrue("Should include prompt label", summary.contains("**Prompt:**"));
assertTrue("Should include prompt text", summary.contains("Fix the divide function"));
assertTrue("Should include assistant response", summary.contains("The issue is in `calc.py`"));
assertTrue("Should exclude system messages", !summary.contains("Session completed"));
}

@Test
public void shouldTruncateLongSummary() {
var messages = List.of(
    new AiDispatchConversation.Message(AiDispatchConversation.Role.USER, "prompt"),
    new AiDispatchConversation.Message(AiDispatchConversation.Role.ASSISTANT, "x".repeat(200))
);

var summary = AiDispatchConversation.buildSummary(messages, 100);
assertTrue("Should be truncated", summary.contains("…(truncated)"));
assertTrue("Should respect max length", summary.length() < 200);
}

@Test
	public void shouldHandleEmptyMessageList() {
		var summary = AiDispatchConversation.buildSummary(List.of(), 5000);
		assertEquals("", summary);
	}

	@Test
	public void shouldParseStructuredEventBlocks() {
		var log = AiDispatchConversation.newProgressBlock("Preparing worktree")
				+ AiDispatchConversation.newThinkingBlock("Inspecting the diff")
				+ AiDispatchConversation.newErrorBlock("AUTH_FAILED", "Invalid provider credentials")
				+ AiDispatchConversation.newMessageBlock(AiDispatchConversation.Role.USER, "/plan fix the regression");

		var messages = AiDispatchConversation.parseMessages(log);

		assertEquals(4, messages.size());
		assertEquals(AiDispatchConversation.EventType.PROGRESS, messages.get(0).getEventType());
		assertEquals("Preparing worktree", messages.get(0).getContent());
		assertEquals(AiDispatchConversation.EventType.THINKING, messages.get(1).getEventType());
		assertEquals("AUTH_FAILED", messages.get(2).getTag());
		assertEquals(AiDispatchConversation.EventType.COMMAND, messages.get(3).getEventType());
		assertEquals("plan", messages.get(3).getTag());
	}

	@Test
	public void shouldParseFeedFromStructuredAndRawLog() {
		var log = "[system] Queued @copilot session.\n"
				+ AiDispatchConversation.newProgressBlock("Worktree ready")
				+ AiDispatchConversation.startAssistantBlock()
				+ "Streaming answer"
				+ AiDispatchConversation.endMessageBlock()
				+ "Running tests...\n"
				+ "[system] Model invocation failed: Unauthorized\n";

		var entries = AiDispatchConversation.parseFeed(log);

		assertTrue(entries.stream().anyMatch(it -> it.getEventType() == AiDispatchConversation.EventType.PROGRESS
				&& it.getContent().contains("Queued @copilot session")));
		assertTrue(entries.stream().anyMatch(it -> it.isAssistant()
				&& it.getEventType() == AiDispatchConversation.EventType.MESSAGE
				&& it.getContent().contains("Streaming answer")));
		assertTrue(entries.stream().anyMatch(it -> it.getEventType() == AiDispatchConversation.EventType.OUTPUT
				&& it.getContent().contains("Running tests")));
		assertTrue(entries.stream().anyMatch(it -> it.getEventType() == AiDispatchConversation.EventType.ERROR
				&& it.getContent().contains("Unauthorized")));
	}

	@Test
	public void shouldKeepStructuredSystemEventsVisibleInTerminalLog() {
		var log = AiDispatchConversation.newProgressBlock("Queued @copilot session.")
				+ AiDispatchConversation.startAssistantBlock()
				+ "Drafting response"
				+ AiDispatchConversation.endMessageBlock()
				+ AiDispatchConversation.newErrorBlock("AUTH_FAILED", "Bad token");

		var visible = AiDispatchConversation.stripMarkup(log);
		assertTrue(visible.contains("[system] Queued @copilot session."));
		assertTrue(visible.contains("Drafting response"));
		assertTrue(visible.contains("[error:AUTH_FAILED] Bad token"));
	}

}
