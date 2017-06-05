package com.gitplex.server.web.component.userchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.page.project.ProjectPage;
import com.gitplex.server.web.util.WicketUtils;

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
			choices = new ArrayList<UserFacade>(GitPlex.getInstance(CacheManager.class).getUsers().values());
		}
			
		for (Iterator<UserFacade> it = choices.iterator(); it.hasNext();) {
			UserFacade user = it.next();
			if (!user.matchesQuery(term))
				it.remove();
		}
		choices.sort(Comparator.comparing(UserFacade::getName));
		
		new ResponseFiller<UserFacade>(response).fill(choices, page, WebConstants.PAGE_SIZE);
	}

}