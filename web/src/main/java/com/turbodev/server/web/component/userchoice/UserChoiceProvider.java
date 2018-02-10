package com.turbodev.server.web.component.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.turbodev.utils.matchscore.MatchScoreProvider;
import com.turbodev.utils.matchscore.MatchScoreUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.editable.annotation.UserChoice;
import com.turbodev.server.util.facade.ProjectFacade;
import com.turbodev.server.util.facade.UserFacade;
import com.turbodev.server.web.WebConstants;
import com.turbodev.server.web.component.select2.Response;
import com.turbodev.server.web.component.select2.ResponseFiller;
import com.turbodev.server.web.page.project.ProjectPage;
import com.turbodev.server.web.util.WicketUtils;

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
			choices = new ArrayList<UserFacade>(TurboDev.getInstance(CacheManager.class).getUsers().values());
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