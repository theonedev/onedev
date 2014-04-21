package com.pmease.gitop.web.component.link;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.GitPerson;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

/**
 * Displays git person, name and avatar
 *
 */
@SuppressWarnings("serial")
public class PersonLink extends Panel {

	public static enum Mode {NAME, AVATAR, NAME_AND_AVATAR}
	
	private final GitPerson person;
	
	private final Mode mode;
	
	private TooltipConfig tooltipConfig;
	
	public PersonLink(String id, GitPerson person, Mode mode) {
		super(id);
		this.person = person;
		this.mode = mode;
	}
	
	public PersonLink withTooltipConfig(@Nullable TooltipConfig tooltipConfig) {
		this.tooltipConfig = tooltipConfig;
		return this;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		User user = Gitop.getInstance(UserManager.class).findByEmail(person.getEmailAddress());
		
		WebMarkupContainer link;
		if (user != null) {
			link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, PageSpec.forUser(user));
		} else {
			link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
		}
		
		add(link);
		
		String displayName;
		if (user != null)
			displayName = user.getDisplayName();
		else
			displayName = person.getName();
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.AVATAR) {
			AvatarImage avatar = new AvatarImage("avatar", person.getEmailAddress());
			if (tooltipConfig != null)
				avatar.add(new TooltipBehavior(Model.of(displayName), tooltipConfig));
			link.add(avatar);
		} else {
			link.add(new WebMarkupContainer("avatar").setVisible(false));
		}
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.NAME) {
			link.add(new Label("name", displayName));
		} else {
			link.add(new Label("name").setVisible(false));
		}
		
	}

}
