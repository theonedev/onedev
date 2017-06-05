package com.gitplex.server.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class Avatar extends WebComponent {

	private final Long userId;
	
	private String url;
	
	public Avatar(String id, @Nullable User user) {
		super(id);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		if (user == null) {
			userId = null;
		} else if (user.getId() == null) {
			userId = null;
		} else {
			userId = user.getId();
		}
		url = avatarManager.getAvatarUrl(user.getFacade());
	}
	
	public Avatar(String id, PersonIdent person) {
		super(id);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		User user = GitPlex.getInstance(UserManager.class).find(person);
		if (user != null) { 
			userId = user.getId();
			url = avatarManager.getAvatarUrl(user.getFacade());
		} else {
			userId = null;
			url = avatarManager.getAvatarUrl(person);
		}
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof AvatarChanged) {
			AvatarChanged avatarChanged = (AvatarChanged) event.getPayload();
			if (avatarChanged.getUser().getId().equals(userId)) {
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				url = avatarManager.getAvatarUrl(avatarChanged.getUser().getFacade());
				avatarChanged.getHandler().add(this);
			}
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AvatarResourceReference()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.append("class", "avatar", " ");
		tag.put("src", url);
	}

}
