package io.onedev.server.web;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.HttpSession;

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
	
	private volatile Cursor packCursor;
	
	private volatile Cursor pullRequestCursor; 

	private volatile ZoneId zoneId;
	
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
	}
	
	@Nullable
	public Cursor getIssueCursor() {
		return issueCursor;
	}

	@Nullable
	public Cursor getBuildCursor() {
		return buildCursor;
	}

	public Cursor getPackCursor() {
		return packCursor;
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

	public void setPackCursor(@Nullable Cursor packCursor) {
		this.packCursor = packCursor;
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

	@Nullable
	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(@Nullable ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	public static WebSession from(HttpSession session) {
		String attributeName = "wicket:" + OneDev.getInstance(WicketServlet.class).getServletName() + ":session";
		return (WebSession) session.getAttribute(attributeName);		
	}
	
}
