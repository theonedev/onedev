package com.pmease.gitplex.web.component.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class AvatarLink extends BookmarkablePageLink<Void> {

	private final PageParameters params;
	
	private final String url;
	
	private final String name;
	
	private final TooltipConfig tooltipConfig;
	
	public AvatarLink(String id, User user, @Nullable TooltipConfig tooltipConfig) {
		super(id, AccountReposPage.class);

		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		params = AccountPage.paramsOf(user);
		url = avatarManager.getAvatarUrl(user);
		name = user.getDisplayName();
		this.tooltipConfig = tooltipConfig;
	}
	
	public AvatarLink(String id, PersonIdent person, @Nullable TooltipConfig tooltipConfig) {
		super(id, AccountReposPage.class);
		
		AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
		
		User user = GitPlex.getInstance(UserManager.class).findByPerson(person);
		if (user != null) { 
			params = AccountPage.paramsOf(user);
			url = avatarManager.getAvatarUrl(user);
			name = user.getDisplayName();
		} else {
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
	protected void onInitialize() {
		super.onInitialize();
		
		setEnabled(!params.isEmpty());
		setEscapeModelStrings(false);
		
		if (tooltipConfig != null)
			add(new TooltipBehavior(Model.of(name), tooltipConfig));
	}

}
