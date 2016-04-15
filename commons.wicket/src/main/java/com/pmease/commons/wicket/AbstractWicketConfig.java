package com.pmease.commons.wicket;

import java.io.IOException;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketResponse;
import org.apache.wicket.request.http.WebResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.websocket.WebSocketMessage;

import de.agilecoders.wicket.core.settings.BootstrapSettings;

public abstract class AbstractWicketConfig extends WebApplication {

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		if (Bootstrap.sandboxMode && !Bootstrap.prodMode)
			return RuntimeConfigurationType.DEVELOPMENT;
		else
			return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	protected void init() {
		super.init();

		getResourceSettings().getCachingStrategy();
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setStripWicketTags(true);
		
		getStoreSettings().setFileStoreFolder(Bootstrap.getTempDir());

		BootstrapSettings bootstrapSettings = new BootstrapSettings();
		bootstrapSettings.setAutoAppendResources(false);
		de.agilecoders.wicket.core.Bootstrap.install(this, bootstrapSettings);

		getComponentInstantiationListeners().add(new IComponentInstantiationListener() {
			
			@Override
			public void onInstantiation(Component component) {
				if ((component instanceof Page) 
						&& !(component instanceof AbstractErrorPage) 
						&& !(component instanceof CommonPage)) {
					throw new RuntimeException("All page classes should extend from CommonPage.");
				}
			}
		});
		
		getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener() {
			
			@Override
			public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
				CommonPage page = (CommonPage) target.getPage();
				if (page.getSessionFeedback().anyMessage())
					target.add(page.getSessionFeedback());
				
				for (Component component: map.values()) {
					if (!component.getBehaviors(StickyBehavior.class).isEmpty())
						target.prependJavaScript(String.format("$('#%s').trigger('sticky_kit:detach');", component.getMarkupId()));
					target.appendJavaScript((String.format("$(document).trigger('elementReplaced', '%s');", component.getMarkupId())));
				}
				
				target.appendJavaScript("$(document.body).trigger('sticky_kit:recalc');");
			}

			@Override
			public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response) {
			}

			@Override
			public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior, AjaxRequestAttributes attributes) {
			}
			
		});

		WebSocketSettings.Holder.set(this, new WebSocketSettings() {

			@Override
			public WebResponse newWebSocketResponse(IWebSocketConnection connection) {
				return new WebSocketResponse(connection) {

					@Override
					public void sendError(int sc, String msg) {
						try {
							WebSocketMessage wsMessage = new WebSocketMessage(WebSocketMessage.ERROR_MESSAGE, msg);
							String errorMessage = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(wsMessage);
							connection.sendMessage(errorMessage);
						} catch (IOException e) {
						}
					}

				};
			}
			
		});
		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}
	
}
