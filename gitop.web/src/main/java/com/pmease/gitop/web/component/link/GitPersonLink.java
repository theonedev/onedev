package com.pmease.gitop.web.component.link;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Optional;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.component.avatar.GravatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;
import com.pmease.gitop.web.page.project.api.GitPerson;

/**
 * Displays git person, name and avatar
 *
 */
public class GitPersonLink extends Panel {
	private static final long serialVersionUID = 1L;

	public static enum Mode {
		FULL,
		AVATAR_ONLY,
		NAME_ONLY
	}
	
	private final Mode mode;
	
	public GitPersonLink(String id, IModel<GitPerson> person, Mode mode) {
		super(id, person);
		this.mode = mode;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		GitPerson person = getPerson();
		Optional<User> user = person.asUser();
		
		WebMarkupContainer link = newLink("link", user);
		add(link);
		
		if (mode == Mode.FULL || mode == Mode.AVATAR_ONLY) {
			Component image;
			if (user.isPresent()) {
				image = new AvatarImage("avatar", new UserModel(user.get()));
				link.add(image);
			} else {
				Fragment frag = new Fragment("avatar", "imgfrag", this);
				image = new GravatarImage("img", Model.of(person.getEmailAddress()));
				frag.add(image);
				link.add(frag);
			}
			
			if (mode == Mode.AVATAR_ONLY) {
				image.add(AttributeModifier.replace("data-toggle", "tooltip"));
				image.add(AttributeModifier.replace("title", user.isPresent() ? user.get().getName() : person.getName()));
			}
		}
		
		if (mode == Mode.FULL || mode == Mode.NAME_ONLY) {
			if (user.isPresent()) {
				link.add(new Label("name", user.get().getName()));
			} else {
				link.add(new Label("name", person.getName()));
			}
		}
		
		if (mode == Mode.AVATAR_ONLY) {
			link.add(new WebMarkupContainer("name").setVisibilityAllowed(false));
		}
		
		if (mode == Mode.NAME_ONLY) {
			link.add(new WebMarkupContainer("avatar").setVisibilityAllowed(false));
		}
	}

	@SuppressWarnings("serial")
	protected WebMarkupContainer newLink(String id, Optional<User> user) {
		if (user.isPresent()) {
			return new BookmarkablePageLink<Void>(id, 
					AccountHomePage.class,
					PageSpec.forUser(user.get()));
		} else {
			AbstractLink link = new Link<Void>(id){
				@Override
				protected void onConfigure() {
					super.onConfigure();
					
					setEnabled(false);
				}

				@Override
				public void onClick() {
				}
			};
			
			return link;
		}
	}
	
	private GitPerson getPerson() {
		return (GitPerson) getDefaultModelObject();
	}
}
