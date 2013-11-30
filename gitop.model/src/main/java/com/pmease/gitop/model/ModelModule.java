package com.pmease.gitop.model;

import java.util.Set;

import com.google.common.collect.Sets;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.gitop.model.validation.ProjectNameReservation;
import com.pmease.gitop.model.validation.UserNameReservation;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ModelModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		
		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(UserNameReservation.class, new UserNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});

		/*
		 * Contribute empty reservations to avoid Guice complain 
		 */
		contribute(ProjectNameReservation.class, new ProjectNameReservation() {
			
			@Override
			public Set<String> getReserved() {
				return Sets.newHashSet();
			}
		});
		
	}

}
