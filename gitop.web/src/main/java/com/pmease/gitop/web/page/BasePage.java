package com.pmease.gitop.web.page;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.assets.BaseResourceBehavior;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.init.ServerInitPage;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	private WebMarkupContainer body;
	
	public BasePage() {
		commonInit();
	}
	
	public BasePage(IModel<?> model) {
		super(model);
		commonInit();
	}
	
	public BasePage(PageParameters params) {
		super(params);
		commonInit();
	}
	
	private void commonInit() {
		body = new TransparentWebMarkupContainer("body");
		add(body);
		body.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String css = getPageCssClass();
				return Strings.isNullOrEmpty(css) ? "" : css;
			}
		}));

		if (!isServerReady() && getClass() != ServerInitPage.class) {
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
		}
		
		if (!isPermitted()) {
			throw new AccessDeniedException();
		}
		
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
		
		/*
		 * Bind global resources here so that they can appear in page header before 
		 * any other resources. Simply rendering the resource in renderHead method of 
		 * base page will not work as renderHead method of container will be called 
		 * after contained components, and this will cause components with resources 
		 * using global resources not working properly.
		 *   
		 */
		add(new WebMarkupContainer("globalResourceBinder").add(new BaseResourceBehavior()));
	}
	
	protected String getPageCssClass() {
		String name = getClass().getSimpleName();
		return StringUtils.camelCaseToLowerCaseWithHyphen(name);
	}

	protected boolean isPermitted() {
		return true;
	}
	
	protected boolean isServerReady() {
		return Gitop.getInstance().isReady();
	}
	
	/*
	 * For pages, we make this final to prevent sub classes from putting page initialization 
	 * logics here. Instead, one should put all page initialization logic in page 
	 * constructor to avoid the situation that if page constructor throws an exception 
	 * intentionally (such as RestartResponseException) to by pass initialization logic 
	 * but onInitialize will still be called to cause undesired behavior.  
	 */
	@Override
	protected final void onInitialize() {
		super.onInitialize();
	}
	
	protected abstract String getPageTitle();
	
	protected int getPageRefreshInterval() {
		return 0;
	}
}
