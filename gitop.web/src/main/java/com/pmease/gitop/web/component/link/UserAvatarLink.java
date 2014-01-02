package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.util.WicketUtils;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

@SuppressWarnings("serial")
public class UserAvatarLink extends Panel {

	public static enum Mode {
		AVATAR_ONLY,
		NAME_ONLY,
		BOTH
	}
	
	private final Mode mode;
	
	public UserAvatarLink(String id, IModel<User> model, Mode mode) {
		super(id, model);
		this.mode = mode;
	}
	
	public UserAvatarLink(String id, IModel<User> model) {
		this(id, model, Mode.BOTH);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, 
				WicketUtils.newPageParams(PageSpec.USER, getUser().getName()));
		
		add(link);
		link.add(new AvatarImage("avatar", (IModel<User>) getDefaultModel()) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(mode != Mode.NAME_ONLY);
			}
		});
		
		link.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getUser().getName();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(mode != Mode.AVATAR_ONLY);
			}
		});
	}
	
	protected User getUser() {
		return Preconditions.checkNotNull((User) getDefaultModelObject());
	}
}
