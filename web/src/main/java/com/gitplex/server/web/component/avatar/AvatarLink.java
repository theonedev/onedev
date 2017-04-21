package com.gitplex.server.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.behavior.TooltipBehavior;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.account.AccountPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;
import com.gitplex.server.web.util.avatar.AvatarManager;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class AvatarLink extends ViewStateAwarePageLink<Void> {

	private final Long accountId;
	
	private final PageParameters params;
	
	private final String tooltip;
	
	private String url;
	
	public AvatarLink(String id, @Nullable Account account) {
		this(id, account, false);
	}
	
	public AvatarLink(String id, @Nullable Account account, boolean showTooltip) {
		super(id, AccountOverviewPage.class);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		if (account == null) {
			accountId = null;
			params = new PageParameters();
		} else if (account.getId() == null) {
			accountId = null;
			params = new PageParameters();
		} else {
			accountId = account.getId();
			params = AccountPage.paramsOf(account);
		}
		if (showTooltip) {
			if (account != null)
				tooltip = account.getDisplayName();
			else
				tooltip = GitPlex.NAME;
		} else {
			tooltip = null;
		}
		url = avatarManager.getAvatarUrl(account);
	}
	
	public AvatarLink(String id, PersonIdent person) {
		this(id, person, false);
	}
	
	public AvatarLink(String id, PersonIdent person, boolean showTooltip) {
		super(id, AccountOverviewPage.class);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		Account account = GitPlex.getInstance(AccountManager.class).find(person);
		if (account != null) { 
			accountId = account.getId();
			params = AccountPage.paramsOf(account);
			url = avatarManager.getAvatarUrl(account);
		} else {
			accountId = null;
			params = new PageParameters();
			url = avatarManager.getAvatarUrl(person);
		}
		
		if (showTooltip)
			tooltip = person.getName();
		else
			tooltip = null;
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
			if (avatarChanged.getAccount().getId().equals(accountId)) {
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				url = avatarManager.getAvatarUrl(avatarChanged.getAccount());
				avatarChanged.getPartialPageRequestHandler().add(this);
			}
		}
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled())
			tag.setName("span");
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AvatarResourceReference()));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setEnabled(!params.isEmpty());
		setEscapeModelStrings(false);
		
		if (tooltip != null)
			add(new TooltipBehavior(Model.of(tooltip), new TooltipConfig()));
		
		setOutputMarkupId(true);
	}

}
