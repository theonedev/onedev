package com.pmease.gitplex.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.depots.DepotListPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class AvatarLink extends BookmarkablePageLink<Void> {

	private final Long accountId;
	
	private final PageParameters params;
	
	private String url;
	
	private final String name;
	
	private final TooltipConfig tooltipConfig;
	
	public AvatarLink(String id, @Nullable Account user) {
		this(id, user, null);
	}
	
	public AvatarLink(String id, @Nullable Account account, @Nullable TooltipConfig tooltipConfig) {
		super(id, DepotListPage.class);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		if (account != null) {
			accountId = account.getId();
			params = AccountPage.paramsOf(account);
			name = account.getDisplayName();
		} else {
			accountId = null;
			params = new PageParameters();
			name = "Unknown";
		}
		url = avatarManager.getAvatarUrl(account);
		this.tooltipConfig = tooltipConfig;
	}
	
	public AvatarLink(String id, PersonIdent person) {
		this(id, person, null);
	}
	
	public AvatarLink(String id, PersonIdent person, @Nullable TooltipConfig tooltipConfig) {
		super(id, DepotListPage.class);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		Account account = GitPlex.getInstance(AccountManager.class).findByPerson(person);
		if (account != null) { 
			accountId = account.getId();
			params = AccountPage.paramsOf(account);
			url = avatarManager.getAvatarUrl(account);
			name = account.getDisplayName();
		} else {
			accountId = null;
			params = new PageParameters();
			url = avatarManager.getAvatarUrl(person);
			name = person.getName();
		}
		this.tooltipConfig = tooltipConfig;
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of("<img src='" + url + "' class='avatar'></img>");
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof AvatarChanged) {
			AvatarChanged avatarChanged = (AvatarChanged) event.getPayload();
			if (avatarChanged.getUser().getId().equals(accountId)) {
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				url = avatarManager.getAvatarUrl(avatarChanged.getUser());
				avatarChanged.getTarget().add(this);
			}
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(Avatar.class, "avatar.css")));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (params.isEmpty()) {
			setEnabled(false);
			setBeforeDisabledLink("");
			setAfterDisabledLink("");
		}
		setEscapeModelStrings(false);
		
		if (tooltipConfig != null)
			add(new TooltipBehavior(Model.of(name), tooltipConfig));
		
		setOutputMarkupId(true);
	}

}
