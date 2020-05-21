package io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider;

import java.util.List;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=130, name="All users")
public class AllUsers implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<User> getChoices(boolean allPossible) {
		UserManager userManager = OneDev.getInstance(UserManager.class);
		if (WicketUtils.getPage() instanceof IssueDetailPage) {
			IssueDetailPage issueDetailPage = (IssueDetailPage) WicketUtils.getPage();
			return userManager.queryAndSort(issueDetailPage.getIssue().getParticipants());
		} else if (SecurityUtils.getUser() != null) {
			return userManager.queryAndSort(Sets.newHashSet(SecurityUtils.getUser()));
		} else {
			return userManager.queryAndSort(Sets.newHashSet());
		}
	}

}
