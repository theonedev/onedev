package io.onedev.server.plugin.executor.serversandbox;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ServerSandboxModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobExecutor.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Set.of(ServerSandboxExecutor.class);
			}

		});

		contribute(JobExecutorDiscoverer.class, new JobExecutorDiscoverer() {

			@Override
			public JobExecutor discover() {
				var executor = new ServerSandboxExecutor();
				try {
					new Commandline(executor.getSandboxExecutable()).addArgs("ls", "--quiet").timeout(30)
							.execute(OutputStream.nullOutputStream(), OutputStream.nullOutputStream()).checkReturnCode();
					return executor;
				} catch (Exception e) {
					if (ExceptionUtils.find(e, InterruptedException.class) != null)
						throw ExceptionUtils.unchecked(e);
					else
						return null;
				}
			}

			@Override
			public int getOrder() {
				return ServerSandboxExecutor.ORDER;
			}

		});
	}

}
