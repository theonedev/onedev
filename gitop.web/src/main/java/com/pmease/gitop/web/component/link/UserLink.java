package com.pmease.gitop.web.component.link;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

/**
 * Displays git person, name and avatar
 *
 */
@SuppressWarnings("serial")
public class UserLink extends AvatarLink {

	private User user;
	
	public UserLink(String id, User user, Mode mode) {
		super(id, mode);

		this.user = checkNotNull(user, "user");
	}
	
	public UserLink(String id, User user) {
		this(id, user, Mode.NAME_AND_AVATAR);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, PageSpec.forUser(user));
		add(link);
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.AVATAR) {
			AvatarImage avatar = new AvatarImage("avatar", user);
			if (tooltipConfig != null)
				avatar.add(new TooltipBehavior(Model.of(user.getDisplayName()), tooltipConfig));
			link.add(avatar);
		} else {
			link.add(new WebMarkupContainer("avatar").setVisible(false));
		}
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.NAME) {
			link.add(new Label("name", user.getDisplayName()));
		} else {
			link.add(new Label("name").setVisible(false));
		}
		
	}

	@Override
	protected void onDetach() {
		user = null;
		super.onDetach();
	}

}
