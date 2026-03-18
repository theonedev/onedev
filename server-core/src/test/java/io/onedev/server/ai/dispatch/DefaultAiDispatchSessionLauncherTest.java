package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.onedev.server.model.AiDispatchRun;

public class DefaultAiDispatchSessionLauncherTest {

@Test
public void shouldAppendTrailingNewlineForInteractiveInput() {
assertEquals("guide the agent\n", DefaultAiDispatchSessionLauncher.normalizeInteractiveInput("guide the agent"));
}

@Test
public void shouldKeepExistingTrailingNewline() {
assertEquals("guide the agent\n", DefaultAiDispatchSessionLauncher.normalizeInteractiveInput("guide the agent\n"));
}

	@Test
	public void shouldBuildConversationMessagesFromDispatchTranscript() {
		var context = new DefaultAiDispatchSessionLauncher.ConversationContext(
				AiDispatchAgent.COPILOT.getMentionName(),
				12L,
				"feature/chat",
				"main",
				false,
				List.of(
						new AiDispatchConversation.Message(AiDispatchConversation.Role.SYSTEM, "Queued session"),
						new AiDispatchConversation.Message(AiDispatchConversation.Role.USER, "Review the caching changes"),
						new AiDispatchConversation.Message(AiDispatchConversation.Role.ASSISTANT,
								"Here is the first pass review.")));
		var worktree = new AiDispatchWorktreeManager.PreparedWorktree(null, new File("/tmp/worktree"), null);
		var messages = DefaultAiDispatchSessionLauncher.buildConversationMessages(context, worktree);

		assertEquals(3, messages.size());
		assertTrue(messages.get(0) instanceof SystemMessage);
assertTrue(messages.get(1) instanceof UserMessage);
assertTrue(messages.get(2) instanceof AiMessage);
assertTrue(((SystemMessage) messages.get(0)).text().contains("Pull request #12"));
		assertEquals("Review the caching changes", ((UserMessage) messages.get(1)).singleText());
		assertEquals("Here is the first pass review.", ((AiMessage) messages.get(2)).text());
	}

	@Test
	public void shouldIgnoreNonTranscriptEventsWhenBuildingConversationMessages() {
		var context = new DefaultAiDispatchSessionLauncher.ConversationContext(
				AiDispatchAgent.COPILOT.getMentionName(),
				12L,
				"feature/chat",
				"main",
				false,
				List.of(
						new AiDispatchConversation.Message(AiDispatchConversation.Role.SYSTEM, "Queued session",
								AiDispatchConversation.EventType.PROGRESS, null),
						new AiDispatchConversation.Message(AiDispatchConversation.Role.USER, "/review calc.py",
								AiDispatchConversation.EventType.COMMAND, "review"),
						new AiDispatchConversation.Message(AiDispatchConversation.Role.ASSISTANT, "Thinking aloud",
								AiDispatchConversation.EventType.THINKING, null),
						new AiDispatchConversation.Message(AiDispatchConversation.Role.ASSISTANT,
								"Here is the actual review.")));
		var worktree = new AiDispatchWorktreeManager.PreparedWorktree(null, new File("/tmp/worktree"), null);
		var messages = DefaultAiDispatchSessionLauncher.buildConversationMessages(context, worktree);

		assertEquals(3, messages.size());
		assertTrue(messages.get(0) instanceof SystemMessage);
		assertTrue(messages.get(1) instanceof UserMessage);
		assertTrue(messages.get(2) instanceof AiMessage);
		assertEquals("/review calc.py", ((UserMessage) messages.get(1)).singleText());
		assertEquals("Here is the actual review.", ((AiMessage) messages.get(2)).text());
	}

	@Test
	public void shouldFailFastWhenDispatchRunAgentIsMissing() {
		var run = new AiDispatchRun();

		var exception = assertThrows(IllegalStateException.class,
				() -> DefaultAiDispatchSessionLauncher.requireAgent(run));
		assertEquals("AI dispatch run is missing agent configuration", exception.getMessage());
	}

}
