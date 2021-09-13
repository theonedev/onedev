package io.onedev.server.plugin.executor.remoteshell;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RemoteShellModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobExecutor.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(RemoteShellExecutor.class);
			}
			
		});
		
		contribute(JobExecutorDiscoverer.class, new JobExecutorDiscoverer() {

			@Override
			public JobExecutor discover() {					
				return new RemoteShellExecutor() {

					private static final long serialVersionUID = 1L;

					@Override
					public void execute(String jobToken, JobContext jobContext) {
						jobContext.getLogger().warning("No docker/kubernetes executor found, fall back "
								+ "to use shell executor...");
						super.execute(jobToken, jobContext);
					}
					
				};
			}
			
		});

	}

}
