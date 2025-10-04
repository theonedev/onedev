package io.onedev.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=130, name="All users")
public class AllUsers implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<User> getChoices(boolean allPossible) {
		UserCache cache = OneDev.getInstance(UserService.class).cloneCache();
		
		if (WicketUtils.getPage() instanceof IssueDetailPage) {
			IssueDetailPage issueDetailPage = (IssueDetailPage) WicketUtils.getPage();
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(issueDetailPage.getIssue().getParticipants()));
			return users;
		} else if (SecurityUtils.getAuthUser() != null) {
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(Sets.newHashSet(SecurityUtils.getAuthUser())));
			return users;
		} else {
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(Sets.newHashSet()));
			return users;
		}
	}

}
