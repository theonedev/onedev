package io.onedev.server.plugin.serverdocker;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.ci.job.JobExecutorDiscoverer;
import io.onedev.server.model.support.JobExecutor;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ServerDockerModule extends AbstractPluginModule {

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
				return Sets.newHashSet(ServerDockerExecutor.class);
			}
			
		});
		
		contribute(JobExecutorDiscoverer.class, new JobExecutorDiscoverer() {

			@Override
			public JobExecutor discover() {
				Commandline docker = new Commandline("docker");
				docker.addArgs("version");
				try {
					docker.execute(new LineConsumer() {
			
						@Override
						public void consume(String line) {
						}
						
					}, new LineConsumer() {
			
						@Override
						public void consume(String line) {
						}
						
					}).checkReturnCode();
					return new ServerDockerExecutor();
				} catch (Exception e) {
					return null;
				}
			}
			
		});
	}

}
