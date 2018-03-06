package io.onedev.server.web.component.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final UserChoice.Type type;
	
	public UserChoiceProvider(UserChoice.Type type) {
		this.type = type;
	}
	
	@Override
	public void query(String term, int page, Response<UserFacade> response) {
		List<UserFacade> choices;
		if (type == UserChoice.Type.PROJECT_READER) {
			ProjectFacade project = (((ProjectPage) WicketUtils.getPage()).getProject()).getFacade();
			choices = new ArrayList<UserFacade>(SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.READ));
		} else if (type == UserChoice.Type.PROJECT_WRITER) {
			ProjectFacade project = (((ProjectPage) WicketUtils.getPage()).getProject()).getFacade();
			choices = new ArrayList<UserFacade>(SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.WRITE));
		} else if (type == UserChoice.Type.PROJECT_ADMINISTRATOR) {
			ProjectFacade project = (((ProjectPage) WicketUtils.getPage()).getProject()).getFacade();
			choices = new ArrayList<UserFacade>(SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.ADMIN));
		} else {
			choices = new ArrayList<UserFacade>(OneDev.getInstance(CacheManager.class).getUsers().values());
		}
			
		choices.sort(Comparator.comparing(UserFacade::getDisplayName));
		
		choices = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<UserFacade>() {

			@Override
			public double getMatchScore(UserFacade object) {
				return object.getMatchScore(term);
			}
			
		});
		
		new ResponseFiller<UserFacade>(response).fill(choices, page, WebConstants.PAGE_SIZE);
	}

}