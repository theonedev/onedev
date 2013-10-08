package com.pmease.gitop.web;

import java.util.Set;

import javax.inject.Singleton;

import com.codahale.dropwizard.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.core.validation.ProjectNameReservation;
import com.pmease.gitop.core.validation.UserNameReservation;
import com.pmease.gitop.web.resource.RestResourceModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(AbstractWicketConfig.class).to(GitopWebApp.class);		
		bind(SitePaths.class).in(Singleton.class);
		
		contribute(ServletConfigurator.class, WebServletConfigurator.class);
		contribute(UserNameReservation.class, WebUserNameReservation.class);

		install(new RestResourceModule());

		contribute(ProjectNameReservation.class, DefaultProjectNameReservation.class);
		contribute(UserNameReservation.class, DefaultUserNameReservation.class);
	}

	public static class DefaultProjectNameReservation implements ProjectNameReservation {

		@Override
		public Set<String> getReserved() {
			return ImmutableSet.<String>of(
			);
		}
	}
	
	public static class DefaultUserNameReservation implements UserNameReservation {

		@Override
		public Set<String> getReserved() {
			return ImmutableSet.<String>of(
					"rest",
					"assets"
			);
		}
	}
	
	
	@Provides
	@Singleton
	public ObjectMapper objectMapper() {
		return Jackson.newObjectMapper();
	}
}
