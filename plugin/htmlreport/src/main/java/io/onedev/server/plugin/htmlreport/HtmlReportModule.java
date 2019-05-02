package io.onedev.server.plugin.htmlreport;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.server.ci.job.JobOutcome;

public class HtmlReportModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobOutcome.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(JobHtmlReport.class);
			}
			
		});
	}

}
