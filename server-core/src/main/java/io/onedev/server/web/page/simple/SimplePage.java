package io.onedev.server.web.page.simple;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.brandlogo.BrandLogoPanel;
import io.onedev.server.web.page.base.BasePage;

public abstract class SimplePage extends BasePage {

	public SimplePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newPageLogo("logo"));
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getTitle();
			}
			
		}));
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		
		if (getSubTitle() != null)
			addOrReplace(new Label("subTitle", getSubTitle()));
		else
			addOrReplace(new WebMarkupContainer("subTitle").setVisible(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleCssResourceReference()));
	}

	protected abstract String getTitle();
	
	@Nullable
	protected abstract String getSubTitle();
	
	protected Component newPageLogo(String componentId) {
		return new BrandLogoPanel(componentId, null);
	}
	
}
