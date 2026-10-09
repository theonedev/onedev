package io.onedev.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;

class ProjectDefaultsParentTest {

	private Project project(Long id, String name) {
		var project = new Project();
		project.setId(id);
		project.setName(name);
		project.setPath(name);
		return project;
	}

	@Test
	void setupRejectsDefaultsAsTargetOrAncestorBeforeCheckingPermissions() {
		var service = spy(new DefaultProjectService(Set.of()));
		var defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		doReturn(defaults).when(service).find(any());
		var subject = mock(Subject.class);
		for (var path : new String[] {"~default", "~default/child", "~default/parent/child"})
			assertThrows(ExplicitException.class, () -> service.setup(subject, path));
		verifyNoInteractions(subject);
		assertEquals(Project.DEFAULT_NAME, defaults.getPath());
	}

	@Test
	void setupStillAllowsChildrenOfOrdinaryProjects() {
		var service = spy(new DefaultProjectService(Set.of()));
		var parent = project(1L, "parent");
		doReturn(parent).doReturn(null).when(service).find(any());
		var subject = mock(Subject.class);
		when(subject.isPermitted(any(Permission.class))).thenReturn(true);
		var child = service.setup(subject, "parent/child");
		assertTrue(child.isNew());
		assertSame(parent, child.getParent());
		assertEquals("parent/child", child.getPath());
	}

	@Test
	void createRejectsDefaultsAnywhereInTheParentChainBeforePersisting() {
		var service = new DefaultProjectService(Set.of());
		service.dao = mock(Dao.class);
		var defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		var parent = project(null, "parent");
		parent.setParent(defaults);
		for (var ancestor : new Project[] {defaults, parent}) {
			var child = project(null, "child");
			child.setParent(ancestor);
			assertThrows(ExplicitException.class, () -> service.create(null, child));
		}
		verifyNoInteractions(service.dao);
	}

	@Test
	void updateRejectsMovingAnOrdinaryProjectUnderDefaults() {
		var service = new DefaultProjectService(Set.of());
		service.dao = mock(Dao.class);
		var child = project(1L, "child");
		child.setParent(project(Project.DEFAULT_ID, Project.DEFAULT_NAME));
		assertThrows(ExplicitException.class, () -> service.update(child));
		verifyNoInteractions(service.dao);
		assertEquals("child", child.getPath());
	}

	@Test
	void updatingDefaultsSettingsIsStillAllowed() {
		var service = new DefaultProjectService(Set.of());
		service.dao = mock(Dao.class);
		var defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		service.update(defaults);
		verify(service.dao).persist(defaults);
	}
}
