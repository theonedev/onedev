package io.onedev.server.web.component.pullrequest.assignment;

import static io.onedev.server.util.match.MatchScoreUtils.filterAndSort;

import java.util.List;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.user.choice.AbstractUserChoiceProvider;

public abstract class AssigneeProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<User> response) {
		PullRequest request = getPullRequest();
		List<User> assignees = OneDev.getInstance(UserManager.class).queryAndSort(request.getParticipants());
		assignees.retainAll(SecurityUtils.getAuthorizedUsers(request.getTargetProject(), new WriteCode()));
		assignees.removeAll(request.getAssignments().stream().map(it->it.getUser()).collect(Collectors.toSet()));
 		
		new ResponseFiller<User>(response).fill(filterAndSort(assignees, new MatchScoreProvider<User>() {

			@Override
			public double getMatchScore(User object) {
				return object.getMatchScore(term) 
						* (assignees.size() - assignees.indexOf(object)) 
						/ assignees.size();
			}
			
		}), page, WebConstants.PAGE_SIZE);
	}

	protected abstract PullRequest getPullRequest();
	
}