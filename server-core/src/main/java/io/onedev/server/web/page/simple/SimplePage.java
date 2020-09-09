package io.onedev.server.web.page.simple;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class SimplePage extends BasePage {

	public SimplePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new SpriteImage("logo", getLogoHref()));
		add(new Label("title", getTitle()));
		if (getSubTitle() != null)
			add(new Label("subTitle", getSubTitle()));
		else
			add(new WebMarkupContainer("subTitle").setVisible(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleCssResourceReference()));
	}

	protected abstract String getTitle();
	
	@Nullable
	protected abstract String getSubTitle();
	
	protected String getLogoHref() {
		return "logo";
	}
	
}
