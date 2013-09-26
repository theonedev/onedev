package com.pmease.gitop.web.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

@SuppressWarnings("serial")
public class LogoutPage extends WebPage {

	/**
	 * Bookmarkable constructor. If a {@code "to"} parameter is provided, assume
	 * it is a URI and redirect to that URI upon logout. Otherwise redirect to
	 * the home page.
	 */
	public LogoutPage(PageParameters params) {
		super(params);
	}

	/**
	 * Interrupt the rendering process and perform the logout, then redirect to
	 * a different page by throwing an exception. The {@code LogoutPage} itself
	 * will therefore never be rendered.
	 */
	@Override
	protected void onBeforeRender() throws RedirectToUrlException {
		logout();
		redirectAfterLogout();
	}

	/**
	 * Called by {@link #onBeforeRender} to instruct Shiro to log the current
	 * user out, then delegate to {@link ShiroWicketPlugin#onLoggedOut} to place
	 * a feedback message in the session.
	 */
	protected void logout() {
		// Perform Shiro logout
		SecurityUtils.getSubject().logout();

		// Delegate to plugin to perform any futher logout tasks
		ShiroWicketPlugin.get().onLoggedOut();
	}

	/**
	 * Called by {@link #onBeforeRender} after {@link #logout} to redirect to
	 * another page. By default this is the application home page. However if a
	 * {@code "to"} page parameter was provided, assume it is a URI and redirect
	 * to that URI instead. For security reasons, full URLs (i.e. something
	 * starting with {@code http}) are ignored.
	 * 
	 * @throws RedirectToUrlException
	 *             to cause Wicket to perform a 302 redirect
	 */
	protected void redirectAfterLogout() throws RedirectToUrlException {
		StringValue to = getPageParameters().get("to");

		// If "to" param was not specified, or was erroneously set to
		// an absolute URL (i.e. containing a ":" like "http://blah"), then fall
		// back
		// to the home page.
		if (null == to || to.isNull() || to.toString().indexOf(":") >= 0) {
			to = StringValue.valueOf(urlFor(getApplication().getHomePage(),
					null));
		}

		throw new RedirectToUrlException(to.toString(), 302);
	}
}
