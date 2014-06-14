package com.pmease.gitop.web.page;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Optional;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.assets.PageResourceReference;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.init.ServerInitPage;
import com.pmease.gitop.web.shiro.LoginPage;

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
		if (!Gitop.getInstance().isReady() && getClass() != ServerInitPage.class) {
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
		}
	}

	protected boolean isPermitted() {
		return true;
	}
	
	protected Optional<User> currentUser() {
		return Optional.fromNullable(Gitop.getInstance(UserManager.class).getCurrent());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted()) {
			if (currentUser().isPresent()) {
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
		return "Gitop - Enterprise Git Management System";
	};

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(PageResourceReference.get()));
	}
}
