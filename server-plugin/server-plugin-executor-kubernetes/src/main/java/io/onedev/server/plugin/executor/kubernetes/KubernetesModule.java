package io.onedev.server.plugin.executor.kubernetes;

import java.util.Collection;

import org.glassfish.jersey.server.ResourceConfig;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.rest.jersey.JerseyConfigurator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class KubernetesModule extends AbstractPluginModule {

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
				return Sets.newHashSet(KubernetesExecutor.class);
			}
			
		});
		contribute(JobExecutorDiscoverer.class, new JobExecutorDiscoverer() {
			
			@Override
			public JobExecutor discover() {
				Commandline kubectl = new Commandline("kubectl");
				kubectl.addArgs("cluster-info");
				try {
					kubectl.execute(new LineConsumer() {
			
						@Override
						public void consume(String line) {
						}
						
					}, new LineConsumer() {
			
						@Override
						public void consume(String line) {
						}
						
					}).checkReturnCode();
					
					return new KubernetesExecutor();
				} catch (Exception e) {
					if (ExceptionUtils.find(e, InterruptedException.class) != null)
						throw ExceptionUtils.unchecked(e);
					else
						return null;
				}
			}
			
		});
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.register(KubernetesResource.class);
			}
			
		});
		
	}

}
