package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.base.Optional;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.page.repository.api.GitPerson;

@SuppressWarnings("serial")
public class GitPersonAvatar extends Panel {

	private final boolean enableTooltip;
	
	public GitPersonAvatar(String id, IModel<GitPerson> model) {
		this(id, model, false);
	}
	
	public GitPersonAvatar(String id, IModel<GitPerson> model, boolean enableTooltip) {
		super(id, model);
		this.enableTooltip = enableTooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Optional<User> user = getGitPerson().asUser();
		Component image;
		if (user.isPresent()) {
			image = new AvatarImage("avatar", user.get());
			add(image);
		} else {
			Fragment frag = new Fragment("avatar", "imgfrag", this);
			image = new GravatarImage("img", Model.of(getGitPerson().getEmailAddress()));
			frag.add(image);
			add(frag);
		}
	
		if (enableTooltip) {
			image.add(AttributeModifier.replace("data-toggle", "tooltip"));
			image.add(AttributeModifier.replace("title", new PropertyModel<String>(getDefaultModel(), "name")));
		}
	}
	
	private GitPerson getGitPerson() {
		return (GitPerson) getDefaultModelObject();
	}
}
