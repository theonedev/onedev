package com.pmease.gitplex.web.component.user;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.home.AccountHomePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class UserLink extends Panel {

	private TooltipConfig tooltipConfig;
	
	private AvatarMode avatarMode;
	
	/**
	 * @param id
	 * @param userModel
	 * 			model of the user to display link for. If <tt>userModel.getObject()</tt> 
	 * 			returns null, link of unknown user will be displayed
	 * @param avatarMode
	 */
	public UserLink(String id, IModel<User> userModel, AvatarMode avatarMode) {
		super(id, checkNotNull(userModel, "userModel"));
		this.avatarMode = checkNotNull(avatarMode, "avatarMode");
	}
	
	public UserLink(String id, IModel<User> userModel) {
		this(id, userModel, AvatarMode.NAME_AND_AVATAR);
	}

	private User getUser() {
		return (User) getDefaultModelObject();
	}
	
	@Override
	protected void onBeforeRender() {
		User user = getUser();

		WebMarkupContainer link;
		if (user != null) {
			link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, AccountPage.paramsOf(user));
		} else {
			link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
		}
		
		String displayName;
		if (user != null)
			displayName = user.getDisplayName();
		else
			displayName = "GitPlex";

		if (avatarMode == AvatarMode.NAME_AND_AVATAR || avatarMode == AvatarMode.AVATAR) {
			Component avatar = new AvatarByUser("avatar", new UserModel(user), false);
			if (tooltipConfig != null) 
				avatar.add(new TooltipBehavior(Model.of(displayName), tooltipConfig));
			link.add(avatar);
		} else {
			link.add(new WebMarkupContainer("avatar").setVisible(false));
		}
		
		if (avatarMode == AvatarMode.NAME_AND_AVATAR || avatarMode == AvatarMode.NAME) {
			link.add(new Label("name", displayName));
		} else {
			link.add(new Label("name").setVisible(false));
		}
		addOrReplace(link);
		
		super.onBeforeRender();
	}

	public UserLink withTooltipConfig(@Nullable TooltipConfig tooltipConfig) {
		this.tooltipConfig = tooltipConfig;
		return this;
	}
	
}
