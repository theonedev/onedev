package io.onedev.server.ai.dispatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.model.IssueComment;

public class IssueCommentDispatchListenerTest {

	@Test
	public void shouldDispatchCreatedIssueComment() {
		var manager = mock(AiDispatchManager.class);
		var comment = mock(IssueComment.class);
		var event = mock(IssueCommentCreated.class);
		when(event.getComment()).thenReturn(comment);

		new IssueCommentDispatchListener(manager).on(event);

		verify(manager).dispatch(comment);
	}

}
