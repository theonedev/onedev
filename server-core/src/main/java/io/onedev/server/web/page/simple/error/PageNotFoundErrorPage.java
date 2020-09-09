package io.onedev.server.web.page.simple.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class PageNotFoundErrorPage extends SimplePage {

	public PageNotFoundErrorPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new BookmarkablePageLink<Void>("goHome", getApplication().getHomePage()));
	}

	@Override
	protected String getLogoHref() {
		return "sad-panda";
	}

	@Override
	protected String getTitle() {
		return "Page Not Found";
	}

	@Override
	protected String getSubTitle() {
		return "I didn't eat it. I swear!";
	}

}
