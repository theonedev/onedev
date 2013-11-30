package com.pmease.gitop.web.page.account.setting;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.GitopFacade;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.setting.members.AccountMembersSettingPage;
import com.pmease.gitop.web.page.account.setting.password.AccountPasswordPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.page.account.setting.projects.AccountProjectsPage;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;

@SuppressWarnings("serial")
public abstract class AccountSettingPage extends AbstractLayoutPage {

	public static enum Category {
		PROFILE(AccountProfilePage.class, "Profile"),
		PASSWORD(AccountPasswordPage.class, "Change Password"),
		PROJECTS(AccountProjectsPage.class, "Projects"),
		TEAMS(AccountTeamsPage.class, "Teams"),
		MEMBERS(AccountMembersSettingPage.class, "Members");
		
		final Class<? extends WebPage> pageClass;
		final String name;
		Category(Class<? extends WebPage> pageClass, String name) {
			this.pageClass = pageClass;
			this.name = name;
		}
		
		public Class<? extends WebPage> getPageClass() {
			return pageClass;
		}

		public String getName() {
			return name;
		}
	}

	protected IModel<User> accountModel;
	
	public AccountSettingPage(PageParameters params) {
		accountModel = newAccountModel(params);
	}
	
	protected IModel<User> newAccountModel(PageParameters params) {
		String name = params.get(PageSpec.USER).toString();
		User user = null;
		if (Strings.isNullOrEmpty(name)) {
			user = Gitop.getInstance(UserManager.class).getCurrent();
			if (user == null) {
				throw new AccessDeniedException();
			}
		} else {
			user = Gitop.getInstance(UserManager.class).findBy(name);
			if (user == null) {
				throw new EntityNotFoundException("Unable find user with name " + name);
			}
		}
		
		return new UserModel(user);
	}
	
	protected PageParameters newAccountParams() {
		if (Objects.equal(getAccount(), Gitop.getInstance(UserManager.class).getCurrent())) {
			return new PageParameters();
		} else {
			return PageSpec.forUser(getAccount());
		}
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new UserAvatarLink("userlink", new UserModel(getAccount())));
		
		add(new ListView<Category>("setting", ImmutableList.<Category>copyOf(Category.values())) {

			@Override
			protected void populateItem(ListItem<Category> item) {
				final Category category = item.getModelObject();
				Link<?> link = new BookmarkablePageLink<Void>("link", category.getPageClass(), newAccountParams());
				item.add(link);
				link.add(new Label("label", category.getName()));
				
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return category == getSettingCategory() ?
								"active" : "";
					}
					
				}));
			}
			
		});
		
		IModel<List<User>> model = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				List<User> users = Gitop.getInstance(GitopFacade.class).getManagableUsers(Gitop.getInstance(UserManager.class).getCurrent());
				List<User> result = Lists.newArrayList();
				
				for (User each : users) {
					if (!Objects.equal(each, getAccount())) {
						result.add(each);
					}
				}
				
				return result;
			}
			
		};
		
		ListView<User> listView = new ListView<User>("owner", model) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User user = item.getModelObject();
				
				PageParameters params;
				
				if (Objects.equal(user, Gitop.getInstance(UserManager.class).getCurrent())) {
					params = new PageParameters();
				} else {
					params = PageSpec.forUser(user);
				}
				
				AbstractLink link = new BookmarkablePageLink<Void>("link",
						AccountProfilePage.class,
						params);
				link.add(new AvatarImage("avatar", new UserModel(user)));
				item.add(link);
				link.add(new Label("name", user.getName()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				this.setVisibilityAllowed(!getList().isEmpty());
			}
		};
		
		add(listView);
	}

	abstract protected Category getSettingCategory();
	
	@Override
	protected boolean isPermitted() {
		User user = getAccount();
		return user != null &&
				SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user));
	}
	
	protected User getAccount() {
		return accountModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (accountModel != null) {
			accountModel.detach();
		}
		
		super.onDetach();
	}
}
