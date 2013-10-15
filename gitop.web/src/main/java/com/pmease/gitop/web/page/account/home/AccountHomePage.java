package com.pmease.gitop.web.page.account.home;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AbstractAccountPage;

@SuppressWarnings("serial")
public class AccountHomePage extends AbstractAccountPage {

	public static enum Category {
		PROJECTS("Projects"), 
		MEMBERS("Members");
		
		final String displayName;
		Category(String displayName) {
			this.displayName = displayName;
		}
	}
	
	private Category category = Category.PROJECTS;
	
	public AccountHomePage(PageParameters params) {
		super(params);
		
		String tab = params.get("tab").toString();
		if (!Strings.isNullOrEmpty(tab)) {
			category = Category.valueOf(tab.toUpperCase());
		}
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		add(new AvatarImage("avatar", accountModel));
		add(new Label("fullname", new PropertyModel<String>(accountModel, "displayName")));
		add(new Label("username", new PropertyModel<String>(accountModel, "name")));
		
		add(new ListView<Category>("category", Lists.newArrayList(Category.values())) {

			@Override
			protected void populateItem(ListItem<Category> item) {
				Category current = item.getModelObject();
				PageParameters params = PageSpec.forUser(accountModel.getObject());
				if (current != Category.PROJECTS) {
					params.add(PageSpec.TAB, current.name().toLowerCase());
				}
				
				AbstractLink link = new BookmarkablePageLink<Void>("link", 
						AccountHomePage.class, params);
				link.add(new Label("name", current.displayName));
				item.add(link);
				item.add(AttributeAppender.append("class", category == current ? "active" : ""));
			}
		});
		
		add(createContent("content"));
	}
	
	private Component createContent(String id) {
		switch (category) {
		case PROJECTS:
			return new ProjectListPanel(id, accountModel);
			
		case MEMBERS:
			return new MemberListPanel(id, accountModel);
			
		default:
			throw new IllegalArgumentException("tab " + category);
		}
		
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(getAccount()));
	}
	
	@Override
	protected String getPageTitle() {
		return getAccount().getName() + " (" + getAccount().getDisplayName() + ")";
	}
}
