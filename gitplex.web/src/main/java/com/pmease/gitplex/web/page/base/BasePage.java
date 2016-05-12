package com.pmease.gitplex.web.page.base;

import javax.annotation.Nullable;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.security.LoginPage;

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
		if (!GitPlex.getInstance().isReady() && getClass() != ServerInitPage.class)
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
	}

	protected boolean isPermitted() {
		return true;
	}
	
	@Nullable
	protected final Account getLoginUser() {
		return GitPlex.getInstance(AccountManager.class).getCurrent();
	}
	
	public void unauthorized() {
		if (getLoginUser() != null) 
			throw new UnauthorizedException();
		else 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted())
			unauthorized();
		
		add(new Label("pageTitle", getPageTitle()));

		add(new WebMarkupContainer("pageRefresh") {

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
		
		add(new WebMarkupContainer("favicon") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				CharSequence url = urlFor(
						new PackageResourceReference(BasePage.class, "favicon.ico"), 
						new PageParameters()); 
				tag.put("href", url);
			}

		});
		add(new WebMarkupContainer("appleTouchIcon") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				CharSequence url = urlFor(
						new PackageResourceReference(BasePage.class, "apple-touch-icon.png"), 
						new PageParameters()); 
				tag.put("href", url);
			}

		});
		add(new WebMarkupContainer("appleTouchIconPrecomposed") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				CharSequence url = urlFor(
						new PackageResourceReference(BasePage.class, "apple-touch-icon.png"), 
						new PageParameters()); 
				tag.put("href", url);
			}
			
		});
	}
	
	protected final String getPageTitle() {
		return "GitPlex - Enterprise Git Management System";
	};

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(new PriorityHeaderItem(
				JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(BasePage.class, "base.js"))));
		response.render(new PriorityHeaderItem(
				CssHeaderItem.forReference(new CssResourceReference(BasePage.class, "base.css"))));
		response.render(new PriorityHeaderItem(
				CssHeaderItem.forReference(new CssResourceReference(BasePage.class, "fontext/fontext.css"))));
	}
	
}
