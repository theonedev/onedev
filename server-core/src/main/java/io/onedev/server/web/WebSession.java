package io.onedev.server.web;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import io.onedev.server.web.page.admin.ssosetting.SsoProcessPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.http.WicketServlet;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.collections.ConcurrentHashSet;

import io.onedev.server.OneDev;
import io.onedev.server.web.util.Cursor;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;	
	
	private volatile Cursor issueCursor; 

	private volatile Cursor buildCursor; 
	
	private volatile Cursor pullRequestCursor; 
	
	private Map<Class<?>, String> redirectUrlsAfterDelete = new ConcurrentHashMap<>(); 
	
	private Set<Long> expandedProjectIds = new ConcurrentHashSet<>();
	
	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}

	public void login(AuthenticationToken token) {
		Subject subject = SecurityUtils.getSubject();

		// Force a new session to prevent session fixation attack.
		// We have to invalidate via both Shiro and Wicket; otherwise it doesn't
		// work.
		subject.getSession().stop();
		replaceSession(); 

		subject.login(token);
	}
	
	public void logout() {
		SecurityUtils.getSubject().logout();
        replaceSession();
		SsoProcessPage.clearConnectorCookie();
	}
	
	@Nullable
	public Cursor getIssueCursor() {
		return issueCursor;
	}

	@Nullable
	public Cursor getBuildCursor() {
		return buildCursor;
	}
	
	@Nullable
	public Cursor getPullRequestCursor() {
		return pullRequestCursor;
	}
	
	public void setIssueCursor(@Nullable Cursor issueCursor) {
		this.issueCursor = issueCursor;
	}

	public void setBuildCursor(@Nullable Cursor buildCursor) {
		this.buildCursor = buildCursor;
	}
	
	public void setPullRequestCursor(@Nullable Cursor pullRequestCursor) {
		this.pullRequestCursor = pullRequestCursor;
	}
	
	@Nullable
	public String getRedirectUrlAfterDelete(Class<?> clazz) {
		return redirectUrlsAfterDelete.get(clazz);
	}

	public void setRedirectUrlAfterDelete(Class<?> clazz, String redirectUrlAfterDelete) {
		redirectUrlsAfterDelete.put(clazz, redirectUrlAfterDelete);
	}
	
	public Set<Long> getExpandedProjectIds() {
		return expandedProjectIds;
	}

	public static WebSession from(HttpSession session) {
		String attributeName = "wicket:" + OneDev.getInstance(WicketServlet.class).getServletName() + ":session";
		return (WebSession) session.getAttribute(attributeName);		
	}
	
}
