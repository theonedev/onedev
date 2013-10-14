package com.pmease.gitop.web.page;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage.AvatarImageType;
import com.pmease.gitop.web.page.account.AccountHomePage;
import com.pmease.gitop.web.page.project.ProjectHomePage;
import com.pmease.gitop.web.util.WicketUtils;

public class PageSpec {

	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String USER = "user";
	public static final String PROJECT = "project";
	public static final String REPO = "repo";
	public static final String TAB = "tab";

	public static PageParameters avatarOfUser(User user) {
		return WicketUtils.newPageParams(TYPE, AvatarImageType.USER.name()
				.toLowerCase(), ID, String.valueOf(user.getId()));
	}

	public static PageParameters forUser(User user) {
		return WicketUtils.newPageParams(USER, user.getName());
	}

	public static PageParameters forProject(Project project) {
		return WicketUtils.newPageParams(USER, project.getOwner().getName(), 
										 PROJECT, project.getName());
	}

	public static Link<?> newUserHomeLink(String id, User user) {
		return new BookmarkablePageLink<Void>(id, AccountHomePage.class, forUser(user));
	}

	public static Link<?> newProjectHomeLink(String id, Project project) {
		return new BookmarkablePageLink<Void>(id, ProjectHomePage.class, forProject(project));
	}

}
