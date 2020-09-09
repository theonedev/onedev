package io.onedev.server.web.component.project.gitprotocol;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;

@SuppressWarnings("serial")
public abstract class GitProtocolPanel extends Panel {

	private static final String COOKIE_USE_SSH = "git.useSsh";
	
	private boolean useSsh;
	
	public GitProtocolPanel(String id) {
		super(id);
	
		if (SecurityUtils.getUser() != null) {
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(COOKIE_USE_SSH);
			useSsh = cookie!=null && String.valueOf(true).equals(cookie.getValue());
		} else {
			useSsh = false;
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("current", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (useSsh)
					return "Showing SSH URL";
				else
					return "Showing HTTP(S) URL";
			}
			
		}));
		
		AjaxLink<Void> switchLink = new AjaxLink<Void>("switch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				switchProtocol(target, !useSsh);
			}
			
		};
		switchLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (useSsh)
					return "Switch to HTTP(S)";
				else
					return "Switch to SSH";
			}
			
		}));
		add(switchLink.setVisible(SecurityUtils.getUser() != null));
		
		WebMarkupContainer noSshKeysWarning = new WebMarkupContainer("noSshKeysWarning") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(useSsh && SecurityUtils.getUser().getSshKeys().isEmpty());
			}
			
		};
		noSshKeysWarning.add(new BookmarkablePageLink<Void>("sshKeys", MySshKeysPage.class));
		noSshKeysWarning.add(new AjaxLink<Void>("useHttp") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				switchProtocol(target, false);
			}
			
		});
		add(noSshKeysWarning);
		
		add(newContent("content"));
		
		SshSetting sshSetting = OneDev.getInstance(SettingManager.class).getSshSetting();
		add(new Label("fingerPrint", "Server fingerprint: " + sshSetting.getFingerPrint()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(useSsh);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private void switchProtocol(AjaxRequestTarget target, boolean useSsh) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		
		this.useSsh = useSsh;
		
		Cookie cookie;
		cookie = new Cookie(COOKIE_USE_SSH, String.valueOf(useSsh));
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		
		response.addCookie(cookie);
		
		target.add(this);
	}

	protected abstract Project getProject();
	
	protected abstract Component newContent(String componentId);
	
	public String getProtocolUrl() {
		UrlManager urlManager = OneDev.getInstance(UrlManager.class);
		return urlManager.cloneUrlFor(getProject(), useSsh);
	}
	
}
