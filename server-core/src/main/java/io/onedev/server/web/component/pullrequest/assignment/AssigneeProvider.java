package io.onedev.server.web.component.pullrequest.assignment;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.user.choice.AbstractUserChoiceProvider;

public abstract class AssigneeProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<User> response) {
		PullRequest request = getPullRequest();
		UserManager userManager = OneDev.getInstance(UserManager.class);

		List<User> users = new ArrayList<>(userManager.getAuthorizedUsers(request.getProject(), new WriteCode()));
		
		users.removeAll(request.getAssignees());
		
		UserCache cache = userManager.cloneCache();
		users.sort(cache.comparingDisplayName(request.getParticipants()));

		new ResponseFiller<>(response).fill(new Similarities<>(users) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(User object) {
				return cache.getSimilarScore(object, term);
			}

		}, page, WebConstants.PAGE_SIZE);
	}

	protected abstract PullRequest getPullRequest();
	
}