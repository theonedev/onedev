package com.pmease.gitop.web.common.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AccountHomePage;
import com.pmease.gitop.web.util.WicketUtils;

@SuppressWarnings("serial")
public class UserAvatarLink extends Panel {

	public UserAvatarLink(String id, IModel<User> model) {
		super(id, model);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, 
				WicketUtils.newPageParams(PageSpec.USER, getUser().getName()));
		
		add(link);
		link.add(new AvatarImage("avatar", (IModel<User>) getDefaultModel()));
		link.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getUser().getName();
			}
		}));
	}
	
	protected User getUser() {
		return Preconditions.checkNotNull((User) getDefaultModelObject());
	}
}
