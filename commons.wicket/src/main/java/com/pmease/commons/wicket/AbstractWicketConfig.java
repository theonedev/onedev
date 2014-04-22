package com.pmease.commons.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.application.IComponentInitializationListener;
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

		getComponentInitializationListeners().add(new IComponentInitializationListener() {
			
			@Override
			public void onInitialize(Component component) {
				if (component instanceof Page) {
					component.add(CommonResourcesBehavior.get());
				}
			}
			
		});
		
		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}

}
