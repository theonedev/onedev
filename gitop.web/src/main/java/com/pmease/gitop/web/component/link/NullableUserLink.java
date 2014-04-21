package com.pmease.gitop.web.component.link;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.service.AvatarManager;

/**
 * Displays git person, name and avatar
 *
 */
@SuppressWarnings("serial")
public class NullableUserLink extends Panel {

	private final Mode mode;
	
	private User user;
	
	public NullableUserLink(String id, User user, Mode mode) {
		super(id);

		this.user = user;
		this.mode = checkNotNull(mode, "mode");
	}
	
	public NullableUserLink(String id, User user) {
		this(id, user, Mode.NAME_AND_AVATAR);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (user != null) {
			add(new UserLink("content", user, mode));
		} else {
			Fragment fragment = new Fragment("content", "nullFrag", this);
			if (mode == Mode.NAME_AND_AVATAR || mode == Mode.AVATAR) {
				String url = Gitop.getInstance(AvatarManager.class).getDefaultAvatarUrl();
				fragment.add(new WebComponent("avatar").add(AttributeModifier.replace("src", url)));
			} else {
				fragment.add(new WebMarkupContainer("avatar").setVisible(false));
			}
			
			if (mode == Mode.NAME_AND_AVATAR || mode == Mode.NAME) {
				fragment.add(new WebMarkupContainer("name"));
			} else {
				fragment.add(new Label("name").setVisible(false));
			}
			add(fragment);
		}
	}

	@Override
	protected void onDetach() {
		user = null;
		super.onDetach();
	}

}
