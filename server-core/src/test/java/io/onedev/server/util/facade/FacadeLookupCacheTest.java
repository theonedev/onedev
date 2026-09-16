package io.onedev.server.util.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;

public class FacadeLookupCacheTest extends AppLoaderMocker {

	private SystemSetting systemSetting;

	private UserService userService;

	@Override
	protected void setup() {
		systemSetting = new SystemSetting();
		systemSetting.setNoreplyEmailDomain("noreply.example.com");
		var settingService = mock(SettingService.class);
		when(settingService.getSystemSetting()).thenReturn(systemSetting);
		when(AppLoader.getInstance(SettingService.class)).thenReturn(settingService);
		userService = mock(UserService.class);
		when(AppLoader.getInstance(UserService.class)).thenReturn(userService);
	}

	@Override
	protected void teardown() {
	}

	@Test
	public void userLookupsAvoidRepeatedScansAndReadCurrentRecords() {
		var records = new CountingMap<UserFacade>();
		var cache = new UserCache(records);
		var user = user(1L, "alice", false);
		records.put(1L, user);
		assertSame(user, cache.findByName("ALICE"));
		assertSame(user, cache.findByName("alice"));
		assertEquals(1, records.scans);

		// Mutate the delegate directly, as replication does, without going through the cache.
		var updated = user(1L, "alice", true);
		records.put(1L, updated);
		assertSame(updated, cache.findByName("alice"));
		assertEquals(1, records.scans);

		var renamed = user(1L, "bob", true);
		records.put(1L, renamed);
		assertNull(cache.findByName("alice"));
		assertSame(renamed, cache.findByName("bob"));
		var replacement = user(2L, "alice", false);
		records.put(2L, replacement);
		assertSame(replacement, cache.findByName("alice"));
		records.remove(2L);
		assertNull(cache.findByName("alice"));
		records.clear();
		assertNull(cache.findByName("bob"));
	}

	@Test
	public void emailValueLookupsAvoidRepeatedScansAndFollowAddressChanges() {
		var records = new CountingMap<EmailAddressFacade>();
		var cache = new EmailAddressCache(records);
		var address = new EmailAddressFacade(1L, 10L, "alice@example.com", true, "unverified");
		records.put(1L, address);
		assertSame(address, cache.findByValue("ALICE@EXAMPLE.COM"));
		assertSame(address, cache.findByValue("alice@example.com"));
		assertEquals(1, records.scans);

		var verified = new EmailAddressFacade(1L, 10L, "alice@example.com", true, null);
		records.put(1L, verified);
		assertSame(verified, cache.findByValue("alice@example.com"));
		assertEquals(1, records.scans);

		var changed = new EmailAddressFacade(1L, 10L, "bob@example.com", true, null);
		records.put(1L, changed);
		assertNull(cache.findByValue("alice@example.com"));
		assertSame(changed, cache.findByValue("bob@example.com"));
		var replacement = new EmailAddressFacade(2L, 20L, "alice@example.com", true, null);
		records.put(2L, replacement);
		assertSame(replacement, cache.findByValue("ALICE@EXAMPLE.COM"));
		records.remove(2L);
		assertNull(cache.findByValue("alice@example.com"));
		records.clear();
		assertNull(cache.findByValue("bob@example.com"));
	}

	@Test
	public void noreplyLookupsFollowCurrentDomainAndUserDespiteCachedAddress() {
		var records = new CountingMap<EmailAddressFacade>();
		var cache = new EmailAddressCache(records);
		var address = new EmailAddressFacade(1L, 10L, "alice@example.com", true, null);
		records.put(1L, address);
		assertSame(address, cache.findByValue("alice@example.com"));

		systemSetting.setNoreplyEmailDomain("example.com");
		when(userService.findFacadeByName("alice")).thenReturn(user(20L, "alice", false));
		var noreply = cache.findByValue("ALICE@EXAMPLE.COM");
		assertNull(noreply.getId());
		assertEquals(20L, noreply.getOwnerId());
		assertEquals("alice@example.com", noreply.getValue());
		assertFalse(noreply.isPrimary());
		assertTrue(noreply.isVerified());
		when(userService.findFacadeByName("alice")).thenReturn(null);
		assertNull(cache.findByValue("alice@example.com"));
		when(userService.findFacadeByName("alice")).thenReturn(user(30L, "alice", false));
		assertEquals(30L, cache.findByValue("alice@example.com").getOwnerId());
		assertEquals(1, records.scans);

		systemSetting.setNoreplyEmailDomain("noreply.example.com");
		assertSame(address, cache.findByValue("alice@example.com"));
		assertEquals(1, records.scans);
	}

	@Test
	public void primaryLookupsAvoidRepeatedScansAndFollowEmailChanges() {
		var records = new CountingMap<EmailAddressFacade>();
		var cache = new EmailAddressCache(records);
		var primary = email(1L, 10L, true, "unverified");
		records.put(1L, primary);
		assertSame(primary, cache.findPrimary(10L));
		assertSame(primary, cache.findPrimary(10L));
		assertEquals(1, records.scans);

		var verified = email(1L, 10L, true, null);
		records.put(1L, verified);
		assertSame(verified, cache.findPrimary(10L));
		assertEquals(1, records.scans);

		records.put(1L, email(1L, 10L, false, null));
		var replacement = email(2L, 10L, true, null);
		records.put(2L, replacement);
		assertSame(replacement, cache.findPrimary(10L));
		records.remove(2L);
		assertNull(cache.findPrimary(10L));
		records.put(2L, replacement);
		assertSame(replacement, cache.findPrimary(10L));

		var transferred = email(2L, 20L, true, null);
		records.put(2L, transferred);
		assertNull(cache.findPrimary(10L));
		assertSame(transferred, cache.findPrimary(20L));
		records.clear();
		assertNull(cache.findPrimary(20L));
	}

	@Test
	public void projectPathLookupsAvoidRepeatedScansAndFollowMoves() {
		var records = new CountingMap<ProjectFacade>();
		var cache = new ProjectCache(records);
		var project = project(1L, "parent/project", "PRJ");
		records.put(1L, project);
		assertSame(project, cache.findByPath("PARENT/PROJECT"));
		assertSame(project, cache.findByPath("parent/project"));
		assertEquals(1, records.scans);

		var updated = project(1L, "parent/project", "NEW");
		records.put(1L, updated);
		assertSame(updated, cache.findByPath("Parent/Project"));
		assertEquals(1, records.scans);

		var moved = project(1L, "renamed/project", "NEW");
		records.put(1L, moved);
		assertNull(cache.findByPath("parent/project"));
		assertSame(moved, cache.findByPath("renamed/project"));
		var replacement = project(2L, "parent/project", "PRJ");
		records.put(2L, replacement);
		assertSame(replacement, cache.findByPath("PARENT/PROJECT"));
		records.remove(2L);
		assertNull(cache.findByPath("parent/project"));
		records.clear();
		assertNull(cache.findByPath("renamed/project"));
	}

	@Test
	public void projectKeyLookupsPreserveCaseSensitivityAndFollowKeyChanges() {
		var records = new CountingMap<ProjectFacade>();
		var cache = new ProjectCache(records);
		var project = project(1L, "parent/project", "PRJ");
		records.put(1L, project);
		assertSame(project, cache.findByKey("PRJ"));
		assertSame(project, cache.findByKey("PRJ"));
		assertEquals(1, records.scans);
		assertNull(cache.findByKey("prj"));

		var moved = project(1L, "renamed/project", "PRJ");
		records.put(1L, moved);
		assertSame(moved, cache.findByKey("PRJ"));
		assertEquals(2, records.scans);

		var changed = project(1L, "renamed/project", "NEW");
		records.put(1L, changed);
		assertNull(cache.findByKey("PRJ"));
		assertSame(changed, cache.findByKey("NEW"));
		var replacement = project(2L, "parent/project", "PRJ");
		records.put(2L, replacement);
		assertSame(replacement, cache.findByKey("PRJ"));
		records.put(2L, project(2L, "parent/project", null));
		assertNull(cache.findByKey("PRJ"));
		records.remove(1L);
		assertNull(cache.findByKey("NEW"));
	}

	@Test
	public void missingLookupsSeeSubsequentInsertions() {
		var users = new UserCache(new HashMap<>());
		assertNull(users.findByName("alice"));
		var user = user(1L, "alice", false);
		users.put(1L, user);
		assertSame(user, users.findByName("alice"));

		var emails = new EmailAddressCache(new HashMap<>());
		assertNull(emails.findPrimary(1L));
		assertNull(emails.findByValue("1@example.com"));
		var email = email(1L, 1L, true, null);
		emails.put(1L, email);
		assertSame(email, emails.findPrimary(1L));
		assertSame(email, emails.findByValue("1@example.com"));

		var projects = new ProjectCache(new HashMap<>());
		assertNull(projects.findByPath("parent/project"));
		assertNull(projects.findByKey("PRJ"));
		var project = project(1L, "parent/project", "PRJ");
		projects.put(1L, project);
		assertSame(project, projects.findByPath("parent/project"));
		assertSame(project, projects.findByKey("PRJ"));
	}

	@Test
	public void snapshotsAndSerializationPreserveLookupBehavior() {
		var users = new UserCache(new HashMap<>());
		users.put(1L, user(1L, "alice", false));
		users.findByName("alice");
		var userSnapshot = users.clone();
		var restoredUsers = SerializationUtils.clone(users);
		users.put(1L, user(1L, "bob", false));
		assertNotNull(userSnapshot.findByName("alice"));
		assertNotNull(restoredUsers.findByName("alice"));
		assertNull(users.findByName("alice"));
		assertNotNull(users.findByName("bob"));

		var emails = new EmailAddressCache(new HashMap<>());
		emails.put(1L, email(1L, 1L, true, null));
		emails.findPrimary(1L);
		emails.findByValue("1@example.com");
		var emailSnapshot = emails.clone();
		var restoredEmails = SerializationUtils.clone(emails);
		emails.clear();
		assertNotNull(emailSnapshot.findPrimary(1L));
		assertNotNull(restoredEmails.findPrimary(1L));
		assertNull(emails.findPrimary(1L));
		assertNotNull(emailSnapshot.findByValue("1@EXAMPLE.COM"));
		assertNotNull(restoredEmails.findByValue("1@EXAMPLE.COM"));
		assertNull(emails.findByValue("1@example.com"));

		var projects = new ProjectCache(new HashMap<>());
		projects.put(1L, project(1L, "parent/project", "PRJ"));
		projects.findByPath("parent/project");
		projects.findByKey("PRJ");
		var projectSnapshot = projects.clone();
		var restoredProjects = SerializationUtils.clone(projects);
		projects.clear();
		assertNotNull(projectSnapshot.findByPath("PARENT/PROJECT"));
		assertNotNull(projectSnapshot.findByKey("PRJ"));
		assertNotNull(restoredProjects.findByPath("PARENT/PROJECT"));
		assertNotNull(restoredProjects.findByKey("PRJ"));
		assertNull(projects.findByPath("parent/project"));
		assertNull(projects.findByKey("PRJ"));
	}

	private static UserFacade user(Long id, String name, boolean disabled) {
		return new UserFacade(id, name, name, User.Type.ORDINARY, disabled, false, false);
	}

	private static EmailAddressFacade email(Long id, Long ownerId, boolean primary, String verificationCode) {
		return new EmailAddressFacade(id, ownerId, id + "@example.com", primary, verificationCode);
	}

	private static ProjectFacade project(Long id, String path, String key) {
		return new ProjectFacade(id, "project", key, path, null, true, true,
				new GitPackConfig(), id, null, null);
	}

	private static class CountingMap<V> extends HashMap<Long, V> {
		private int scans;

		@Override
		public Collection<V> values() {
			scans++;
			return super.values();
		}
	}
}
