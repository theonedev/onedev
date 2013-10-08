package com.pmease.gitop.web.page.account.setting;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.account.setting.password.AccountPasswordPage;
import com.pmease.gitop.web.page.account.setting.permission.AccountPermissionPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.page.account.setting.repos.AccountReposPage;

@SuppressWarnings("serial")
public abstract class AccountSettingPage extends AbstractLayoutPage {

	public static enum Category {
		PROFILE(AccountProfilePage.class, "Profile"),
		PASSWORD(AccountPasswordPage.class, "Change Password"),
		REPOS(AccountReposPage.class, "Repositories"),
		PERMISSION(AccountPermissionPage.class, "Permissions");
		
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

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new UserAvatarLink("userlink", new UserModel(getAccount())));
		
		add(new ListView<Category>("setting", ImmutableList.<Category>copyOf(Category.values())) {

			@Override
			protected void populateItem(ListItem<Category> item) {
				final Category category = item.getModelObject();
				Link<?> link = new BookmarkablePageLink<Void>("link", category.getPageClass());
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
	}

	abstract protected Category getSettingCategory();
	
	@Override
	protected boolean isPermitted() {
		User user = getAccount();
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user));
	}
	
	protected User getAccount() {
		return User.getCurrent();
	}
}
