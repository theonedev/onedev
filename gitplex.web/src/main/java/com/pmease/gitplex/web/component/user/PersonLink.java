package com.pmease.gitplex.web.component.user;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountHomePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class PersonLink extends Panel {

	private TooltipConfig tooltipConfig;
	
	private AvatarMode avatarMode;
	
	public PersonLink(String id, IModel<PersonIdent> personModel, AvatarMode avatarMode) {
		super(id, checkNotNull(personModel, "personModel"));
		this.avatarMode = checkNotNull(avatarMode, "avatarMode");
	}
	
	public PersonLink(String id, IModel<PersonIdent> personModel) {
		this(id, personModel, AvatarMode.NAME_AND_AVATAR);
	}

	private PersonIdent getPerson() {
		return (PersonIdent) getDefaultModelObject();
	}
	
	@Override
	protected void onBeforeRender() {
		PersonIdent person = getPerson();

		User user = GitPlex.getInstance(UserManager.class).findByEmail(person.getEmailAddress());

		WebMarkupContainer link;
		if (user != null) {
			link = new BookmarkablePageLink<Void>("link", AccountHomePage.class, AccountHomePage.paramsOf(user));
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
			displayName = person.getName();
		
		if (avatarMode == AvatarMode.NAME_AND_AVATAR || avatarMode == AvatarMode.AVATAR) {
			Component avatar = new AvatarByPerson("avatar", new AbstractReadOnlyModel<PersonIdent>(){

				@Override
				public PersonIdent getObject() {
					return getPerson();
				}
				
			}, false);
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

	public PersonLink withTooltipConfig(@Nullable TooltipConfig tooltipConfig) {
		this.tooltipConfig = tooltipConfig;
		return this;
	}
	
}
