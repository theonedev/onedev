package io.onedev.server.plugin.report.playwright;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.buildspec.step.PublishReportStep;

public class PlaywrightModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return PublishReportStep.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(PublishPlaywrightReportStep.class);
			}

		});
	}

}
