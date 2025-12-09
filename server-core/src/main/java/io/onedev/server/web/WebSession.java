package io.onedev.server.web;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.protocol.http.WicketServlet;
import org.apache.wicket.request.Request;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.Chat;
import io.onedev.server.web.util.Cursor;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;	
	
	private static final String ATTR_ISSUE_CURSOR = "issueCursor";
	
	private static final String ATTR_BUILD_CURSOR = "buildCursor";

	private static final String ATTR_PACK_CURSOR = "packCursor";

	private static final String ATTR_PULL_REQUEST_CURSOR = "pullRequestCursor";

	private static final String ATTR_ZONE_ID = "zoneId";

	private static final String ATTR_REDIRECT_URLS_AFTER_DELETE = "redirectUrlsAfterDelete";

	private static final String ATTR_EXPANDED_PROJECT_IDS = "expandedProjectIds";

	private static final String ATTR_CHAT_VISIBLE = "chatVisible";

	private static final String ATTR_ACTIVE_CHAT_ID = "activeChatId";

	private static final String ATTR_ANONYMOUS_CHATS = "anonymousChats";
	
	private static final String ATTR_CHAT_INPUT = "chatInput";
	
	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}
	
	public void logout() {
		SecurityUtils.getSubject().logout();	
        replaceSession();
	}
	
	@Nullable
	public Cursor getIssueCursor() {
		return (Cursor) getAttribute(ATTR_ISSUE_CURSOR);
	}

	@Nullable
	public Cursor getBuildCursor() {
		return (Cursor) getAttribute(ATTR_BUILD_CURSOR);
	}

	@Nullable
	public Cursor getPackCursor() {
		return (Cursor) getAttribute(ATTR_PACK_CURSOR);
	}

	@Nullable
	public Cursor getPullRequestCursor() {
		return (Cursor) getAttribute(ATTR_PULL_REQUEST_CURSOR);
	}
	
	public void setIssueCursor(@Nullable Cursor issueCursor) {
		setAttribute(ATTR_ISSUE_CURSOR, issueCursor);
	}

	public void setBuildCursor(@Nullable Cursor buildCursor) {
		setAttribute(ATTR_BUILD_CURSOR, buildCursor);
	}

	public void setPackCursor(@Nullable Cursor packCursor) {
		setAttribute(ATTR_PACK_CURSOR, packCursor);
	}
	
	public void setPullRequestCursor(@Nullable Cursor pullRequestCursor) {
		setAttribute(ATTR_PULL_REQUEST_CURSOR, pullRequestCursor);
	}
	
	@SuppressWarnings("unchecked")
	private Map<Class<?>, String> getRedirectUrlsAfterDelete() {
		Map<Class<?>, String> map = (Map<Class<?>, String>) getAttribute(ATTR_REDIRECT_URLS_AFTER_DELETE);
		if (map == null) {
			map = new HashMap<>();
			setAttribute(ATTR_REDIRECT_URLS_AFTER_DELETE, (Serializable) map);
		}
		return map;
	}
	
	@Nullable
	public String getRedirectUrlAfterDelete(Class<?> clazz) {
		return getRedirectUrlsAfterDelete().get(clazz);
	}

	public void setRedirectUrlAfterDelete(Class<?> clazz, String redirectUrlAfterDelete) {
		getRedirectUrlsAfterDelete().put(clazz, redirectUrlAfterDelete);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Long> getExpandedProjectIds() {
		Set<Long> set = (Set<Long>) getAttribute(ATTR_EXPANDED_PROJECT_IDS);
		if (set == null) {
			set = new HashSet<>();
			setAttribute(ATTR_EXPANDED_PROJECT_IDS, (Serializable) set);
		}
		return set;
	}

	@Nullable
	public ZoneId getZoneId() {
		return (ZoneId) getAttribute(ATTR_ZONE_ID);
	}

	public void setZoneId(@Nullable ZoneId zoneId) {
		setAttribute(ATTR_ZONE_ID, zoneId);
	}

	public boolean isChatVisible() {
		Boolean visible = (Boolean) getAttribute(ATTR_CHAT_VISIBLE);
		return visible != null && visible;
	}

	public void setChatVisible(boolean chatVisible) {
		setAttribute(ATTR_CHAT_VISIBLE, chatVisible);
	}

	@Nullable
	public Long getActiveChatId() {
		return (Long) getAttribute(ATTR_ACTIVE_CHAT_ID);
	}

	public void setActiveChatId(Long activeChatId) {
		setAttribute(ATTR_ACTIVE_CHAT_ID, activeChatId);
	}

	@SuppressWarnings("unchecked")
	public Map<Long, Chat> getAnonymousChats() {
		Map<Long, Chat> map = (Map<Long, Chat>) getAttribute(ATTR_ANONYMOUS_CHATS);
		if (map == null) {
			map = new HashMap<>();
			setAttribute(ATTR_ANONYMOUS_CHATS, (Serializable) map);
		}
		return map;
	}

	@Nullable
	public String getChatInput() {
		return (String) getAttribute(ATTR_CHAT_INPUT);
	}

	public void setChatInput(String chatInput) {
		setAttribute(ATTR_CHAT_INPUT, chatInput);
	}

	public static WebSession from(HttpSession session) {
		String attributeName = "wicket:" + OneDev.getInstance(WicketServlet.class).getServletName() + ":session";
		return (WebSession) session.getAttribute(attributeName);		
	}
	
}
