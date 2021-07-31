package io.onedev.server.util;

import static org.junit.Assert.*;

import org.junit.Test;

import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.model.Issue;

public class ReferenceMigratorTest {

	@Test
	public void test() {
		ReferenceMigrator migrator = new ReferenceMigrator(
				Issue.class, CollectionUtils.newHashMap(1L, 11L, 22L, 2222L));
		assertEquals("issue #11", migrator.migratePrefixed("QB-1", "QB-"));
		assertEquals("issue #2222", migrator.migratePrefixed("QB-22", "QB-"));
		assertEquals("QB-1QB-22", migrator.migratePrefixed("QB-1QB-22", "QB-"));
		assertEquals("issue #11 issue #2222", migrator.migratePrefixed("QB-1 QB-22", "QB-"));
		assertEquals("issue #11 issue #2222", migrator.migratePrefixed("QB-1 QB-22", "QB-"));
		assertEquals("QB-2,issue #2222", migrator.migratePrefixed("QB-2,QB-22", "QB-"));
		assertEquals("QB-1andQB-2", migrator.migratePrefixed("QB-1andQB-2", "QB-"));
		assertEquals("QB-1以及QB-2", migrator.migratePrefixed("QB-1以及QB-2", "QB-"));
		assertEquals("2QB-1,issue #2222", migrator.migratePrefixed("2QB-1,QB-22", "QB-"));
	}

}
