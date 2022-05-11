package io.onedev.server.web.page.simple.error;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class MethodNotAllowedErrorPage extends SimplePage {

	public MethodNotAllowedErrorPage(PageParameters params) {
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
		return "Method Not Allowed";
	}

	@Override
	protected void setHeaders(final WebResponse response) {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Override
	protected String getSubTitle() {
		return "This http method is not allowed";
	}

}