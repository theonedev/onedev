package com.gitplex.server.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.behavior.TooltipBehavior;
import com.gitplex.server.web.util.avatar.AvatarManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class Avatar extends WebComponent {

	private final Long accountId;
	
	private String url;
	
	private final String name;
	
	private final TooltipConfig tooltipConfig;
	
	public Avatar(String id, @Nullable Account account) {
		this(id, account, null);
	}
	
	public Avatar(String id, @Nullable Account account, @Nullable TooltipConfig tooltipConfig) {
		super(id);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		if (account != null) {
			accountId = account.getId();
			url = avatarManager.getAvatarUrl(account);
			name = account.getDisplayName();
		} else {
			accountId = null;
			url = avatarManager.getAvatarUrl(account);
			name = "Unknown";
		}
		this.tooltipConfig = tooltipConfig;
	}
	
	public Avatar(String id, PersonIdent person) {
		this(id, person, null);
	}
	
	public Avatar(String id, PersonIdent person, @Nullable TooltipConfig tooltipConfig) {
		super(id);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		Account account = GitPlex.getInstance(AccountManager.class).find(person);
		if (account != null) { 
			accountId = account.getId();
			url = avatarManager.getAvatarUrl(account);
			name = account.getDisplayName();
		} else {
			accountId = null;
			url = avatarManager.getAvatarUrl(person);
			name = person.getName();
		}
		this.tooltipConfig = tooltipConfig;
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
		
		if (tooltipConfig != null)
			add(new TooltipBehavior(Model.of(name), tooltipConfig));
		
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
