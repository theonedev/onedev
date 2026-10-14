package io.onedev.server.commandhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UpgradeWrapperConfTest {

	@Test
	void preservesLegacyLaunchdIdentityAcrossUpgrades() {
		var config = "wrapper.java.mainclass=org.tanukisoftware.wrapper.WrapperSimpleApp\r\n"
				+ "wrapper.name=onedev\r\n";
		var migrated = Upgrade.migrateWrapperConf(config);
		assertTrue(migrated.contains("wrapper.java.mainclass=io.onedev.wrapper.WrapperSimpleApp"));
		assertTrue(migrated.contains("wrapper.launchd.label=org.tanukisoftware.wrapper.onedev"));
		assertTrue(migrated.contains("wrapper.name=onedev\r\n"));
		assertEquals(migrated, Upgrade.migrateWrapperConf(migrated));
	}

	@Test
	void handlesCustomNamesWhitespaceAndComments() {
		var config = " wrapper.java.mainclass = org.tanukisoftware.wrapper.WrapperSimpleApp # bridge\n"
				+ "#wrapper.launchd.label=example\nwrapper.name=old-name\n wrapper.name = custom.server # service\n";
		var migrated = Upgrade.migrateWrapperConf(config);
		assertTrue(migrated.contains("io.onedev.wrapper.WrapperSimpleApp # bridge"));
		assertTrue(migrated.contains("wrapper.launchd.label=org.tanukisoftware.wrapper.custom.server"));
		assertFalse(migrated.contains("org.tanukisoftware.wrapper.old-name"));
	}

	@Test
	void keepsExplicitLabelAndNativeConfigurations() {
		var config = "wrapper.java.mainclass=org.tanukisoftware.wrapper.WrapperSimpleApp\n"
				+ "wrapper.name=onedev\n wrapper.launchd.label = custom.label\n";
		assertEquals(config.replace("org.tanukisoftware.wrapper.WrapperSimpleApp", "io.onedev.wrapper.WrapperSimpleApp"),
				Upgrade.migrateWrapperConf(config));
		var nativeConfig = "wrapper.java.mainclass=io.onedev.wrapper.WrapperSimpleApp\nwrapper.name=onedev.server\n";
		assertEquals(nativeConfig, Upgrade.migrateWrapperConf(nativeConfig));
		assertEquals(nativeConfig + "wrapper.launchd.label=io.onedev.server\n",
				Upgrade.migrateWrapperConf(nativeConfig + "wrapper.launchd.label=io.onedev.server\n"));
	}
}
