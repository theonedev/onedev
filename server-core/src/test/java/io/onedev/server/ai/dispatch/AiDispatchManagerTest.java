package io.onedev.server.ai.dispatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CancellationException;

import org.junit.Test;

public class AiDispatchManagerTest {

	@Test
	public void shouldTreatCancellationAsHandledTermination() {
		assertTrue(AiDispatchManager.isHandledTermination(new CancellationException("cancelled")));
		assertFalse(AiDispatchManager.isHandledTermination(new IllegalStateException("boom")));
	}

}
