package io.onedev.server.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.http.WicketServlet;
import org.apache.wicket.request.Request;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.Cursor;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;	
	
	private final Map<Long, Cursor> issueCursors = new ConcurrentHashMap<>(); 

	private final Map<Long, Cursor> buildCursors = new ConcurrentHashMap<>(); 
	
	private final Map<Long, Cursor> pullRequestCursors = new ConcurrentHashMap<>(); 
	
	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}

	public void login(String userName, String password, boolean rememberMe) {
		Subject subject = SecurityUtils.getSubject();

		// Force a new session to prevent session fixation attack.
		// We have to invalidate via both Shiro and Wicket; otherwise it doesn't
		// work.
		subject.getSession().stop();
		WebSession.get().replaceSession(); 

		UsernamePasswordToken token;
		token = new UsernamePasswordToken(userName, password, rememberMe);
		
		subject.login(token);
	}
	
	public void logout() {
		SecurityUtils.getSubject().logout();
        WebSession session = WebSession.get();
        session.replaceSession();
	}
	
	@Nullable
	public Cursor getIssueCursor(Project project) {
		return issueCursors.get(project.getId());
	}

	@Nullable
	public Cursor getBuildCursor(Project project) {
		return buildCursors.get(project.getId());
	}
	
	@Nullable
	public Cursor getPullRequestCursor(Project project) {
		return pullRequestCursors.get(project.getId());
	}
	
	public void setIssueCursor(Project project, @Nullable Cursor cursor) {
		if (cursor != null)
			issueCursors.put(project.getId(), cursor);
		else
			issueCursors.remove(project.getId());
	}

	public void setBuildCursor(Project project, @Nullable Cursor cursor) {
		if (cursor != null)
			buildCursors.put(project.getId(), cursor);
		else
			buildCursors.remove(project.getId());
	}
	
	public void setPullRequestCursor(Project project, @Nullable Cursor cursor) {
		if (cursor != null)
			pullRequestCursors.put(project.getId(), cursor);
		else
			pullRequestCursors.remove(project.getId());
	}
	
	public static WebSession from(HttpSession session) {
		String attributeName = "wicket:" + OneDev.getInstance(WicketServlet.class).getServletName() + ":session";
		return (WebSession) session.getAttribute(attributeName);		
	}
	
}
