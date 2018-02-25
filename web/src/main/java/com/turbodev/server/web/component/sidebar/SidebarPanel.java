package com.turbodev.server.web.component.sidebar;

import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.turbodev.server.web.component.tabbable.Tab;
import com.turbodev.server.web.component.tabbable.Tabbable;
import com.turbodev.server.web.util.WicketUtils;

@SuppressWarnings("serial")
public abstract class SidebarPanel extends Panel {

	private final String miniCookieKey;
	
	public SidebarPanel(String id, @Nullable String miniCookieKey) {
		super(id);
		this.miniCookieKey = miniCookieKey;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newHead("head"));
		add(new Tabbable("tabs", newTabs()));
		add(new WebMarkupContainer("miniToggle").setVisible(miniCookieKey!=null));

		add(AttributeAppender.append("class", "sidebar"));
		
		if (miniCookieKey != null) {
			add(AttributeAppender.append("class", "minimizable"));
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie miniCookie = request.getCookie(miniCookieKey);
			if (miniCookie != null) {
				if ("yes".equals(miniCookie.getValue()))
					add(AttributeAppender.append("class", "minimized"));
			} else if (WicketUtils.isDevice()) {
				add(AttributeAppender.append("class", "minimized"));
			}
		} 
	}
	
	protected Component newHead(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}
	
	protected abstract List<? extends Tab> newTabs();

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SidebarResourceReference()));
		String script = String.format("turbodev.server.sidebar.onDomReady('%s', %s);", 
				getMarkupId(true), miniCookieKey!=null?"'"+miniCookieKey+"'":"undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
