package com.pmease.commons.wicket;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;

import com.pmease.commons.bootstrap.Bootstrap;

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

		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setStripWicketTags(true);
		
		getStoreSettings().setFileStoreFolder(Bootstrap.getTempDir());

		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
	}

}
