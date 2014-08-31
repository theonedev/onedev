package com.pmease.commons.wicket;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.protocol.http.WebApplication;

import com.pmease.commons.bootstrap.Bootstrap;

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
				
				for (Component component: map.values())
					target.appendJavaScript((String.format("$(document).trigger('replace', '%s');", component.getMarkupId())));
				
				target.appendJavaScript("$(document.body).trigger('sticky_kit:recalc')");
			}

			@Override
			public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response) {
			}
			
		});

		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}
	
}
