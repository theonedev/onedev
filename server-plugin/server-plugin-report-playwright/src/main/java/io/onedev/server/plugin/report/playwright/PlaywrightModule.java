package io.onedev.server.plugin.report.playwright;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BaseResourceMapper;

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

		contribute(WebApplicationConfigurator.class, application -> application.mount(
				new BaseResourceMapper(
						"~downloads/projects/${project}/builds/${build}/playwright/${report}",
						new PlaywrightReportDownloadResourceReference())));
	}

}
