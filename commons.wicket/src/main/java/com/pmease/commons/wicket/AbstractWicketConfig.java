package com.pmease.commons.wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.protocol.http.WebApplication;

import com.google.inject.Singleton;
import com.pmease.commons.bootstrap.Bootstrap;

@Singleton
public abstract class AbstractWicketConfig extends WebApplication {

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		if (Bootstrap.isSandboxMode() && !Bootstrap.isProdMode())
			return RuntimeConfigurationType.DEVELOPMENT;
		else
			return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	protected void init() {
		super.init();

		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setStripWicketTags(true);
		
		getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
		getAjaxRequestTargetListeners().add(new AjaxRequestTarget.IListener() {
			
			@Override
			public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
				
			}
			
			@Override
			public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response) {
				List<String> quotedIds = new ArrayList<String>();
				for (String id: map.keySet())
					quotedIds.add("\"" + id + "\"");
				response.addJavaScript("onWicketAjaxCompleted([" + StringUtils.join(quotedIds, ",") + "]);");
			}
		});
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return null;
	}

}
