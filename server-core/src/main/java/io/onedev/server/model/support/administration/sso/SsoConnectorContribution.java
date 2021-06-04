package io.onedev.server.model.support.administration.sso;

import java.util.Collection;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface SsoConnectorContribution {

	Collection<SsoConnector> getSsoConnectors();
	
}
