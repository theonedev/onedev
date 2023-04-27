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

import com.hazelcast.map.IMap;
import org.eclipse.jetty.server.session.AbstractSessionDataStore;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.UnreadableSessionDataException;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Session data stored in Hazelcast
 */
@ManagedObject
public class ClusterSessionDataStore extends AbstractSessionDataStore {

	private static final Logger logger = LoggerFactory.getLogger(ClusterSessionDataStore.class);

	private IMap<String, SessionData> sessionDataMap;

	@Override
	public SessionData doLoad(String id)
			throws Exception {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Loading session {} from hazelcast", id);

			SessionData sd = sessionDataMap.get(id);
			return sd;
		} catch (Exception e) {
			throw new UnreadableSessionDataException(id, _context, e);
		}
	}

	@Override
	public boolean delete(String id)
			throws Exception {
		if (sessionDataMap == null)
			return false;

		//use delete which does not deserialize the SessionData object being removed
		sessionDataMap.delete(id);
		return true;
	}

	public void setSessionDataMap(IMap<String, SessionData> sessionDataMap) {
		this.sessionDataMap = sessionDataMap;
	}

	@Override
	public void initialize(SessionContext context) throws Exception {
		super.initialize(context);
	}

	@Override
	public void doStore(String id, SessionData data, long lastSaveTime) {
		sessionDataMap.set(id, data);
	}

	@Override
	public boolean isPassivating() {
		return true;
	}

	@Override
	public Set<String> doGetExpired(Set<String> candidates) {
		var now = System.currentTimeMillis();

		return candidates.stream().filter(candidate -> {
			try {
				SessionData sd = load(candidate);

				//if the session no longer exists
				if (sd == null) {
					return true;
				} else {
					if (_context.getWorkerName().equals(sd.getLastNode())) {
						//we are its manager, add it to the expired set if it is expired now
						if ((sd.getExpiry() > 0) && sd.getExpiry() <= now) 
							return true;
					} else {
						//if we are not the session's manager, only expire it iff:
						// this is our first expiryCheck and the session expired a long time ago
						//or
						//the session expired at least one graceperiod ago
						if (_lastExpiryCheckTime <= 0) {
							if ((sd.getExpiry() > 0) && sd.getExpiry() < (now - (1000L * (3 * _gracePeriodSec)))) 
								return true;
						} else {
							if ((sd.getExpiry() > 0) && sd.getExpiry() < (now - (1000L * _gracePeriodSec))) 
								return true;
						}
					}
				}
			} catch (Exception e) {
				return true;
			}
			return false;
		}).collect(Collectors.toSet());
	}

	@Override
	public boolean exists(String id)
			throws Exception {
		//TODO find way to do query without pulling in whole session data
		SessionData sd = load(id);
		if (sd == null)
			return false;

		if (sd.getExpiry() <= 0)
			return true; //never expires
		else
			return sd.getExpiry() > System.currentTimeMillis(); //not expired yet
	}
	
}
