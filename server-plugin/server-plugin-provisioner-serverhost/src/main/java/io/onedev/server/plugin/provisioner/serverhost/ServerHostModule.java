package io.onedev.server.plugin.provisioner.serverhost;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ServerHostModule extends AbstractPluginModule {

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
				return Sets.newHashSet(ServerHostProvisioner.class);
			}
			
		});
		
	}

}
