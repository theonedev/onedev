package io.onedev.server.plugin.provisioner.serversandbox;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.workspace.WorkspaceProvisionerDiscoverer;

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
				return WorkspaceProvisioner.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Set.of(ServerSandboxProvisioner.class);
			}

		});

		contribute(WorkspaceProvisionerDiscoverer.class, new WorkspaceProvisionerDiscoverer() {

			@Override
			public WorkspaceProvisioner discover() {
				var provisioner = new ServerSandboxProvisioner();
				try {
					new Commandline(provisioner.getSandboxExecutable()).addArgs("ls", "--quiet").timeout(30)
							.execute(OutputStream.nullOutputStream(), OutputStream.nullOutputStream()).checkReturnCode();
					return provisioner;
				} catch (Exception e) {
					if (ExceptionUtils.find(e, InterruptedException.class) != null)
						throw ExceptionUtils.unchecked(e);
					else
						return null;
				}
			}

			@Override
			public int getOrder() {
				return ServerSandboxProvisioner.ORDER;
			}

		});
	}

}
