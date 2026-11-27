package io.onedev.server.rest.resource;

import static org.mockito.Mockito.*;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestUpdateService;

class PullRequestResourceTest {

	@Test
	void reviewEndpointsUseTheRequestedUpdate() throws Exception {
		var resource = new PullRequestResource();
		var updates = mock(PullRequestUpdateService.class);
		var reviews = mock(PullRequestReviewService.class);
		FieldUtils.writeField(resource, "pullRequestUpdateService", updates, true);
		FieldUtils.writeField(resource, "pullRequestReviewService", reviews, true);
		var user = new User();
		var project = new Project();
		var request = new PullRequest();
		request.setTargetProject(project);
		var update = new PullRequestUpdate();
		update.setId(3L);
		update.setRequest(request);
		when(updates.load(3L)).thenReturn(update);
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(SecurityUtils::getUser).thenReturn(user);
			resource.approve(3L, " looks good ");
			resource.requestForChanges(3L, " please revise ");
			security.verify(() -> SecurityUtils.checkWorkOnProject(project), times(2));
			verify(reviews).approve(user, update, "looks good");
			verify(reviews).requestChanges(user, update, "please revise");
		}
	}

}
