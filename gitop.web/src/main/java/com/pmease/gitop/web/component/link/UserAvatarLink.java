package com.pmease.gitop.web.component.link;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.util.WicketUtils;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

@SuppressWarnings("serial")
public class UserAvatarLink extends Panel {

	public static enum Mode {
		NAME,
		AVATAR,
		NAME_AND_AVATAR
	}
	
	private final User user;
	
	private final Mode mode;
	
	public UserAvatarLink(String id, @Nullable User user, Mode mode) {
		super(id);
		this.user = user;
		this.mode = mode;
	}
	
	public UserAvatarLink(String id, @Nullable User user) {
		this(id, user, Mode.NAME_AND_AVATAR);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment fragment;
		if (user != null) {
			fragment = new Fragment("content", "notNullFrag", this);
			Link<?> link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, 
					WicketUtils.newPageParams(PageSpec.USER, user.getName()));
			
			fragment.add(link);
			link.add(new AvatarImage("avatar", user).setVisible(mode != Mode.NAME));
			
			link.add(new Label("name", user.getName()).setVisible(mode != Mode.AVATAR));
		} else {
			fragment = new Fragment("content", "nullFrag", this);
			fragment.add(new WebMarkupContainer("avatar").setVisible(mode != Mode.NAME));
			fragment.add(new WebMarkupContainer("name").setVisible(mode != Mode.AVATAR));
		}
		add(fragment);
	}
	
}
