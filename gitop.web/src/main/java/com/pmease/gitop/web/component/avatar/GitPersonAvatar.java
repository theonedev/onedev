package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public class GitPersonAvatar extends Panel {

	private final boolean enableTooltip;
	
	public GitPersonAvatar(String id, IModel<PersonIdent> model) {
		this(id, model, false);
	}
	
	public GitPersonAvatar(String id, IModel<PersonIdent> model, boolean enableTooltip) {
		super(id, model);
		this.enableTooltip = enableTooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User user = Gitop.getInstance(UserManager.class).findByPerson(getPerson());
		Component image;
		if (user != null) {
			image = new AvatarImage("avatar", user);
			add(image);
		} else {
			Fragment frag = new Fragment("avatar", "imgfrag", this);
			image = new GravatarImage("img", Model.of(getPerson().getEmailAddress()));
			frag.add(image);
			add(frag);
		}
	
		if (enableTooltip) {
			image.add(AttributeModifier.replace("data-toggle", "tooltip"));
			image.add(AttributeModifier.replace("title", new PropertyModel<String>(getDefaultModel(), "name")));
		}
	}
	
	private PersonIdent getPerson() {
		return (PersonIdent) getDefaultModelObject();
	}
}
