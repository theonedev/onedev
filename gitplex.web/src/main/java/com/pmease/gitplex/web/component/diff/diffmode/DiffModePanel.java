package com.pmease.gitplex.web.component.diff.diffmode;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;

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
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
			}

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
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
					
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getCompleteHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return "$('#ajax-loading-indicator').show();";
					}
					
					@Override
					public CharSequence getBeforeHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					
				});
			}
			
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
