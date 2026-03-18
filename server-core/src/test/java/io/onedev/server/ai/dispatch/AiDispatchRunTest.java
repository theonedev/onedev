package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.onedev.server.model.AiDispatchRun;

public class AiDispatchRunTest {

	@Test
	public void shouldParseModelNameFromFlags() {
		var run = new AiDispatchRun();
		run.setFlags("--think --model=gpt-4.1 --no-commit");
		assertEquals("gpt-4.1", run.getModelName());
	}

	@Test
	public void shouldParseModelNameFromSplitFlagTokens() {
		var run = new AiDispatchRun();
		run.setFlags("--model claude-3-7-sonnet --think");
		assertEquals("claude-3-7-sonnet", run.getModelName());
	}

	@Test
	public void shouldReturnNullWhenNoModelFlagExists() {
		var run = new AiDispatchRun();
		run.setFlags("--think --no-commit");
		assertEquals(null, run.getModelName());
	}

}
