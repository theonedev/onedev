package com.pmease.gitplex.web.page;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.assets.PageResourceReference;

@SuppressWarnings("serial")
public abstract class BasePage extends CommonPage {

	public BasePage() {
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
