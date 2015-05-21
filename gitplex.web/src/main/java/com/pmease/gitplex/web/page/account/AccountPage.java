package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;
import com.pmease.gitplex.web.component.userchoice.UserChoiceProvider;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AccountPage extends LayoutPage {
	
	private static final String PARAM_USER = "user";
	
	protected final IModel<User> accountModel;
	
	public AccountPage(PageParameters params) {
		super(params);
		
		String name = params.get(PARAM_USER).toString();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		User user = GitPlex.getInstance(UserManager.class).findByName(name);
		if (user == null) 
			throw (new EntityNotFoundException("User " + name + " not found"));
		
		accountModel = new UserModel(user);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarByUser("accountAvatar", accountModel, false));
		
		final IModel<User> accountModel = Model.of(getAccount());
		Select2Choice<User> accountChoice = new Select2Choice<User>("accountName", accountModel, new UserChoiceProvider()) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Typing to find an account...");
				getSettings().setFormatResult("gitplex.account.choiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.account.choiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.account.choiceFormatter.escapeMarkup");
			}
			
		};
		accountChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(getPage().getClass(), paramsOf(accountModel.getObject()));
			}
			
		});
		add(accountChoice);
		
		add(new Link<Void>("runAsAccount") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().runAs(getAccount().getPrincipals());
				setResponsePage(getPage().getClass(), getPageParameters());
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User currentUser = userManager.getCurrent();
				setVisible(!getAccount().equals(currentUser) && SecurityUtils.canManage(getAccount()));
			}
			
		});
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountTab("Repositories", "fa fa-ext fa-fw fa-repo", AccountReposPage.class));
		
		if (SecurityUtils.canManage(getAccount())) {
			tabs.add(new AccountTab("Notifications", "fa fa-fw fa-bell-o", AccountNotificationsPage.class));
			tabs.add(new AccountTab("Setting", "fa fa-fw fa-cog", ProfileEditPage.class, 
					AvatarEditPage.class, PasswordEditPage.class));
		}
		add(new Tabbable("accountTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(AccountPage.class, "account.js")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(AccountPage.class, "account.css")));
	}

	@Override
	protected void onDetach() {
		accountModel.detach();
		
		super.onDetach();
	}
	
	public User getAccount() {
		return accountModel.getObject();
	}
	
	public static PageParameters paramsOf(User user) {
		PageParameters params = new PageParameters();
		params.set(PARAM_USER, user.getName());
		return params;
	}

	public static PageParameters paramsOf(String userName) {
		PageParameters params = new PageParameters();
		params.set(PARAM_USER, userName);
		return params;
	}
	
}
