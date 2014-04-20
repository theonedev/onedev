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
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.component.avatar.GravatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

/**
 * Displays git person, name and avatar
 *
 */
public class PersonLink extends Panel {
	private static final long serialVersionUID = 1L;

	public static enum Mode {
		NAME,
		AVATAR,
		NAME_AND_AVATAR
	}
	
	private final Mode mode;
	private WebMarkupContainer link;
	private Component image;
	
	private boolean enableTooltip;
	private String tooltipPosition;
	
	public PersonLink(String id, IModel<PersonIdent> person, Mode mode) {
		super(id, person);
		this.mode = mode;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PersonIdent person = getPerson();
		User user = Gitop.getInstance(UserManager.class).findByPerson(person);
		
		link = newLink("link", user);
		add(link);
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.AVATAR) {
			if (user != null) {
				image = new AvatarImage("avatar", user);
				link.add(image);
			} else {
				Fragment frag = new Fragment("avatar", "imgfrag", this);
				image = new GravatarImage("img", Model.of(person.getEmailAddress()));
				frag.add(image);
				link.add(frag);
			}
		}
		
		if (mode == Mode.NAME_AND_AVATAR || mode == Mode.NAME) {
			if (user != null) {
				link.add(new Label("name", user.getName()));
			} else {
				link.add(new Label("name", person.getName()));
			}
		}
		
		if (mode == Mode.AVATAR) {
			link.add(new WebMarkupContainer("name").setVisibilityAllowed(false));
		}
		
		if (mode == Mode.NAME) {
			link.add(new WebMarkupContainer("avatar").setVisibilityAllowed(false));
		}
		
		setupTooltip(user != null? user.getName() : person.getName());
	}

	public PersonLink enableTooltip() {
		return enableTooltip(null);
	}
	
	public PersonLink enableTooltip(String position) {
		this.enableTooltip = true;
		this.tooltipPosition = position;
		return this;
	}
	
	private void setupTooltip(String title) {
		if (!enableTooltip) {
			return;
		}
		
		Component c = null;
		if (image != null) {
			c = image;
		} else if (link != null) {
			c = link;
		} else {
			throw new IllegalStateException();
		}
		
		c.add(AttributeModifier.append("class", "has-tip"));
		if (!Strings.isNullOrEmpty(tooltipPosition)) {
			c.add(AttributeModifier.replace("data-placement", tooltipPosition));
		}
		
		c.add(AttributeModifier.replace("title", title));
	}
	
	@SuppressWarnings("serial")
	protected WebMarkupContainer newLink(String id, User user) {
		if (user != null) {
			return new BookmarkablePageLink<Void>(id, 
					AccountHomePage.class, PageSpec.forUser(user));
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
	
	private PersonIdent getPerson() {
		return (PersonIdent) getDefaultModelObject();
	}
}
