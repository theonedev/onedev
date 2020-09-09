package io.onedev.server.web.component.user;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class UserAvatar extends WebComponent {

	private String url;
	
	private boolean system;
	
	public UserAvatar(String id, @Nullable Long userId, String displayName) {
		super(id);
		url = getAvatarManager().getAvatarUrl(userId, displayName);
		system = (userId != null && userId == -1);
	}
	
	public UserAvatar(String id, User user) {
		this(id, user.getId(), user.getDisplayName());
	}
	
	public UserAvatar(String id, PersonIdent personIdent) {
		super(id);
		url = getAvatarManager().getAvatarUrl(personIdent);
	}
	
	private AvatarManager getAvatarManager() {
		return OneDev.getInstance(AvatarManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "avatar"));
		if (system)
			add(AttributeAppender.append("class", "system-avatar"));
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.put("src", url);
	}

}
