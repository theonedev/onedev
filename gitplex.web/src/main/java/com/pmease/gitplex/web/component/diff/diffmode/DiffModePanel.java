package com.pmease.gitplex.web.component.diff.diffmode;

import javax.servlet.http.Cookie;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

@SuppressWarnings("serial")
public abstract class DiffModePanel extends Panel {

	private static final String COOKIE_NAME = "gitplex.diff.mode";
	
	private boolean unified;
	
	public DiffModePanel(String id) {
		super(id);
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_NAME);
		if (cookie == null)
			unified = true;
		else
			unified = cookie.getValue().equals("unified");
	}

	public void setUnified(boolean unified) {
		this.unified = unified;
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie = new Cookie(COOKIE_NAME, unified?"unified":"split");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("unified") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				setUnified(true);
				target.add(DiffModePanel.this);
				target.focusComponent(null);
				onModeChange(target);
			}
			
		}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return unified?" active":"";
			}
			
		})));
		
		add(new AjaxLink<Void>("split") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				setUnified(false);
				target.add(DiffModePanel.this);
				target.focusComponent(null);
				onModeChange(target);
			}
			
		}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !unified?" active":"";
			}
			
		})));

		setOutputMarkupId(true);
	}

	public boolean isUnified() {
		return unified;
	}

	protected abstract void onModeChange(AjaxRequestTarget target);
}
