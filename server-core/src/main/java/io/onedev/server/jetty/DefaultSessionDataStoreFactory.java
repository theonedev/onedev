//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package io.onedev.server.jetty;

import io.onedev.server.cluster.ClusterManager;
import org.eclipse.jetty.server.session.AbstractSessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultSessionDataStoreFactory extends AbstractSessionDataStoreFactory 
		implements SessionDataStoreFactory {

	private final ClusterManager clusterManager;
	
	@Inject
	public DefaultSessionDataStoreFactory(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

    @Override
    public SessionDataStore getSessionDataStore(SessionHandler handler) {
        ClusterSessionDataStore sessionDataStore = new ClusterSessionDataStore();
        sessionDataStore.setSessionDataMap(clusterManager.getHazelcastInstance().getMap("jettySessionData"));
        return sessionDataStore;
    }

}
