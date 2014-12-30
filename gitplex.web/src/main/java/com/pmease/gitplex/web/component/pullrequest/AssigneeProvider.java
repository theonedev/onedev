package com.pmease.gitplex.web.component.pullrequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.Permission;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.service.AvatarManager;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class AssigneeProvider extends ChoiceProvider<Assignee> {

	private static final long serialVersionUID = 1L;

	private final IModel<Repository> repoModel;
	
	public AssigneeProvider(IModel<Repository> repoModel) {
		this.repoModel = repoModel;
	}
	
	@Override
	public void query(String term, int page, Response<Assignee> response) {
		List<Assignee> assignees = new ArrayList<>();
		for (User user: GitPlex.getInstance(AuthorizationManager.class)
				.listAuthorizedUsers(repoModel.getObject(), GeneralOperation.WRITE)) {
			if (StringUtils.isBlank(term) 
					|| user.getName().startsWith(term) 
					|| user.getDisplayName().startsWith(term)) {
				assignees.add(new Assignee(user, null));
			}
		}
		Collections.sort(assignees, new Comparator<Assignee>() {

			@Override
			public int compare(Assignee assignee1, Assignee assignee2) {
				return assignee1.getUser().getDisplayName().compareTo(assignee2.getUser().getDisplayName());
			}
			
		});
		if (StringUtils.isBlank(term)) {
			assignees.add(0, new Assignee(repoModel.getObject().getOwner(), "Repository Owner"));
			Permission writePermission = ObjectPermission.ofRepositoryWrite(repoModel.getObject());
			User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
			if (currentUser != null && currentUser.asSubject().isPermitted(writePermission))
				assignees.add(0, new Assignee(currentUser, "Me"));
		}

		int first = page * Constants.DEFAULT_SELECT2_PAGE_SIZE;
		int last = first + Constants.DEFAULT_SELECT2_PAGE_SIZE;
		if (last > assignees.size()) {
			response.addAll(assignees.subList(first, assignees.size()));
		} else {
			response.addAll(assignees.subList(first, last));
			response.setHasMore(last < assignees.size());
		}
	}

	@Override
	public void toJson(Assignee choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getUser().getId())
			.key("name").value(StringEscapeUtils.escapeHtml4(choice.getUser().getName()));
		if (choice.getUser().getFullName() != null)
			writer.key("fullName").value(StringEscapeUtils.escapeHtml4(choice.getUser().getFullName()));
		writer.key("email").value(StringEscapeUtils.escapeHtml4(choice.getUser().getEmail()));
		writer.key("avatar").value(GitPlex.getInstance(AvatarManager.class).getAvatarUrl(choice.getUser()));
		if (choice.getAlias() != null)
			writer.key("alias").value(StringEscapeUtils.escapeHtml4(choice.getAlias()));
	}

	@Override
	public Collection<Assignee> toChoices(Collection<String> ids) {
		List<Assignee> assignees = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			assignees.add(new Assignee(dao.load(User.class, id), null));
		}

		return assignees;
	}

	@Override
	public void detach() {
		repoModel.detach();
		
		super.detach();
	}

}