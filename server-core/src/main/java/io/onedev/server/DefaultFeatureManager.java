package io.onedev.server;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

import javax.inject.Singleton;

@Singleton
public class DefaultFeatureManager implements FeatureManager {
	
	@Override
	public boolean isEEAvailable() {
		return false;
	}

	@Override
	public boolean isEEActivated() {
		return false;
	}

	@Override
	public Component renderSupportRequestLink(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}

}
