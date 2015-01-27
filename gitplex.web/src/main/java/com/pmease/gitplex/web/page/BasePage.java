package com.pmease.gitplex.web.page;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.assets.PageResourceReference;
import com.pmease.gitplex.web.exception.AccessDeniedException;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.shiro.LoginPage;

@SuppressWarnings("serial")
public abstract class BasePage extends CommonPage {

	public BasePage() {
		checkReady();
	}

	public BasePage(IModel<?> model) {
		super(model);
		checkReady();
	}

	public BasePage(PageParameters params) {
		super(params);
		checkReady();
	}

	private void checkReady() {
		if (!GitPlex.getInstance().isReady() && getClass() != ServerInitPage.class) {
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
		}
	}

	protected boolean isPermitted() {
		return true;
	}
	
	protected User getCurrentUser() {
		return GitPlex.getInstance(UserManager.class).getCurrent();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted()) {
			if (getCurrentUser() != null) {
				throw new AccessDeniedException("Access denied");
			} else {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
		}

		add(new BookmarkablePageLink<Void>("home-link", Application.get().getHomePage()));
		
		add(new Label("title", getPageTitle()));

		add(new WebMarkupContainer("refresh") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("content", getPageRefreshInterval());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPageRefreshInterval() != 0);
			}

		});
	}
	
	protected String getPageTitle() {
		return "GitPlex - Enterprise Git Management System";
	};

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(PageResourceReference.get())));
	}
}
