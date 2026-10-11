package io.onedev.server.security.permission;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.onedev.server.model.Role;

class AccessSitesTest {

	@Test
	void rolesGrantOnlyConfiguredSitesUnlessManagingProject() {
		var role = new Role();
		role.setEditableIssueFields(new io.onedev.server.model.support.role.IncludeIssueFields());
		assertFalse(role.implies(new AccessSites("docs")));
		role.setAccessibleSites("docs preview-* -preview-secret");
		assertTrue(role.implies(new AccessSites("docs")));
		assertTrue(role.implies(new AccessSites("preview-feature")));
		assertFalse(role.implies(new AccessSites("preview-secret")));
		assertFalse(role.implies(new AccessSites("other")));
		assertFalse(role.implies(new AccessSites("DOCS")));
		role.setManageProject(true);
		assertTrue(role.implies(new AccessSites("other")));
	}
}
