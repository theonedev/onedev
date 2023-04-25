package io.onedev.server.ee.storage;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;

import static com.beust.jcommander.internal.Lists.newArrayList;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class StorageModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(AdministrationSettingContribution.class, () -> newArrayList(StorageSetting.class));
		
		bind(StorageManager.class).to(EEStorageManager.class);
	}

}
