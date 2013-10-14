package com.pmease.gitop.web.page.account.setting.permission;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountPermissionPage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Account Level Permissions";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PERMISSION;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		AjaxLink<?> link = new AjaxLink<Void>("anonymouslink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				User account = getAccount();
				account.setPubliclyAccessible(!account.isPubliclyAccessible());
				Gitop.getInstance(UserManager.class).save(account);
				
				target.add(this);
			}
			
		};
		
		add(link);
		link.setOutputMarkupId(true);
		link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getAccount().isPubliclyAccessible() ?
						"checked" : "";
			}
			
		}));
		
		WebMarkupContainer loggedInPermissions = new WebMarkupContainer("loggedInPermissions");
		add(loggedInPermissions);
		loggedInPermissions.setOutputMarkupId(true);
		loggedInPermissions.add(new ListView<GeneralOperation>("permissions",
				ImmutableList.<GeneralOperation>of(GeneralOperation.NO_ACCESS, GeneralOperation.READ, GeneralOperation.WRITE)) {

					@Override
					protected void populateItem(ListItem<GeneralOperation> item) {
						AjaxLink<?> link = new PermissionLink("permission", item.getModelObject());
						item.add(link);
						link.add(new Label("name", item.getModelObject().toString()));
					}
			
		});
		
		add(new TeamsPanel("teams", new UserModel(getAccount())));
	}
	
	class PermissionLink extends AjaxLink<Void> {
		final GeneralOperation permssion;
		
		PermissionLink(String id, final GeneralOperation permission) {
			super(id);
			this.permssion = permission;
			
			add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return getAccount().getDefaultAuthorizedOperation() == permission ?
							"btn-default active" : "btn-default";
				}
			}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			User account = getAccount();
			if (account.getDefaultAuthorizedOperation() == permssion) {
				return;
			}
			
			account.setDefaultAuthorizedOperation(permssion);
			Gitop.getInstance(UserManager.class).save(account);
			
			target.add(AccountPermissionPage.this.get("loggedInPermissions"));
		}
	}
}
