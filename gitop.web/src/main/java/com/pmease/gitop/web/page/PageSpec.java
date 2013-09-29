package com.pmease.gitop.web.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.avatar.AvatarImage.AvatarImageType;
import com.pmease.gitop.web.util.WicketUtils;

public class PageSpec {

	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String USER = "user";
	public static final String REPO = "repo";
	public static final String TAB = "tab";
	
	public static PageParameters avatarOfUser(User user) {
		return WicketUtils.newPageParams(TYPE, AvatarImageType.USER.name().toLowerCase(),
				ID, String.valueOf(user.getId()));
	}
}
