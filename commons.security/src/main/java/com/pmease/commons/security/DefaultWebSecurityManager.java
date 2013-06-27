package com.pmease.commons.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.security.extensionpoint.RealmContribution;

@Singleton
public class DefaultWebSecurityManager extends org.apache.shiro.web.mgt.DefaultWebSecurityManager {

	@Inject
	public DefaultWebSecurityManager(PluginManager pluginManager, RememberMeManager rememberMeManager) {
		setRememberMeManager(rememberMeManager);
		
		Collection<Realm> realms = new ArrayList<Realm>();

		for (RealmContribution contrib: pluginManager.getExtensions(RealmContribution.class)) {
			realms.addAll(contrib.getRealms());
		}

		Preconditions.checkState(!realms.isEmpty(), "No authentication realms found.");
		
		setRealms(realms);	
	}

	/**
	 * This method is overriden to make sure that anonymous user (the user with id 0) is not saved 
	 * to session. This is important as otherwise requests to RESTful services will trigger creation 
	 * of sessions.   
	 * 
	 * @see org.apache.shiro.mgt.DefaultSecurityManager#save(org.apache.shiro.subject.Subject)
	 */
	@Override
	protected void save(Subject subject) {
		Long guestId = 0L;
		if (!guestId.equals(subject.getPrincipal()))
			super.save(subject);
	}

}
