package com.pmease.gitop.web.page;

import java.util.List;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.util.WicketUtils;
import com.pmease.gitop.web.component.avatar.AvatarImage.AvatarImageType;
import com.pmease.gitop.web.page.account.home.AccountHomePage;
import com.pmease.gitop.web.page.repository.source.RepositoryHomePage;
import com.pmease.gitop.web.util.UrlUtils;

public class PageSpec {

	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String USER = "user";
	public static final String REPO = "repo";
	public static final String OBJECT_ID = "objectId";
	public static final String TAB = "tab";

	public static PageParameters avatarOfUser(User user) {
		return WicketUtils.newPageParams(TYPE, AvatarImageType.USER.name()
				.toLowerCase(), ID, String.valueOf(user.getId()));
	}

	public static PageParameters forUser(User user) {
		return WicketUtils.newPageParams(USER, user.getName());
	}

	public static PageParameters forRepository(Repository repo) {
		return WicketUtils.newPageParams(USER, repo.getOwner().getName(), 
										 REPO, repo.getName());
	}

	public static PageParameters forRepositoryWithPath(Repository repo, String objectId, List<String> paths) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(objectId), "object id");
		Preconditions.checkArgument(!paths.isEmpty(), "paths should not be empty");
		
		PageParameters params = forRepository(repo);
		params.add(OBJECT_ID, objectId);
		for (int i = 0; i < paths.size(); i++) {
			params.set(i, paths.get(i));
		}
		
		return params;
	}

	public static String getPathFromParams(PageParameters params) {
		List<String> list = Lists.newArrayList();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			list.add(Preconditions.checkNotNull(params.get(i).toString()));
		}
		
		return UrlUtils.concatSegments(list);
	}
	
	public static void addPathToParameters(String path, PageParameters params) {
		Iterable<String> paths = Splitter.on("/").omitEmptyStrings().split(path);
		addPathToParameters(paths, params);
	}
	
	public static void addPathToParameters(Iterable<String> paths, PageParameters params) {
		int i = 0;
		for (String each : paths) {
			params.set(i, each);
			i++;
		}
	}
	
	public static Link<?> newUserHomeLink(String id, User user) {
		return new BookmarkablePageLink<Void>(id, AccountHomePage.class, forUser(user));
	}

	public static Link<?> newRepositoryHomeLink(String id, Repository repo) {
		return new BookmarkablePageLink<Void>(id, RepositoryHomePage.class, forRepository(repo));
	}

}
