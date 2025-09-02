package io.onedev.server.web.page.error;

import static io.onedev.server.web.translation.Translation._T;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.simple.SimplePage;

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
	protected WebComponent newPageLogo(String componentId) {
		return new SpriteImage(componentId, "sad-panda");
	}

	@Override
	protected String getTitle() {
		return _T("Page Not Found");
	}

	@Override
	protected void setHeaders(final WebResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	@Override
	protected String getSubTitle() {
		return _T("I didn't eat it. I swear!");
	}

}
