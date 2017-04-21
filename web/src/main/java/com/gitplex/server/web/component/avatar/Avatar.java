package com.gitplex.server.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.util.avatar.AvatarManager;

@SuppressWarnings("serial")
public class Avatar extends WebComponent {

	private final Long accountId;
	
	private String url;
	
	public Avatar(String id, @Nullable Account account) {
		super(id);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		if (account == null) {
			accountId = null;
		} else if (account.getId() == null) {
			accountId = null;
		} else {
			accountId = account.getId();
		}
		url = avatarManager.getAvatarUrl(account);
	}
	
	public Avatar(String id, PersonIdent person) {
		super(id);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		Account account = GitPlex.getInstance(AccountManager.class).find(person);
		if (account != null) { 
			accountId = account.getId();
			url = avatarManager.getAvatarUrl(account);
		} else {
			accountId = null;
			url = avatarManager.getAvatarUrl(person);
		}
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof AvatarChanged) {
			AvatarChanged avatarChanged = (AvatarChanged) event.getPayload();
			if (avatarChanged.getAccount().getId().equals(accountId)) {
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				url = avatarManager.getAvatarUrl(avatarChanged.getAccount());
				avatarChanged.getPartialPageRequestHandler().add(this);
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
