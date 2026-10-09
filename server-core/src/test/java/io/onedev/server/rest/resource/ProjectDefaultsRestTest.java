package io.onedev.server.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PathParam;

import org.apache.shiro.authz.UnauthorizedException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.*;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.PosixShell;
import io.onedev.server.model.support.workspace.spec.shell.WorkspaceShell;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.rest.resource.support.ProjectDefaults;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.*;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.validation.HibernateValidationTestSupport;
import io.onedev.server.web.util.WicketUtils;

class ProjectDefaultsRestTest extends HibernateValidationTestSupport {

	private ObjectMapper newObjectMapper() {
		var implementations = mock(ImplementationRegistry.class);
		when(implementations.getImplementations(WorkspaceShell.class)).thenReturn(List.of(PosixShell.class));
		return new ObjectMapperProvider(Set.of(), Set.of(), implementations, Set.of()).get();
	}

	@Test
	void defaultsJsonExcludesUnsupportedSettingsWithoutChangingOrdinaryProjectSettings() throws Exception {
		var project = new Project();
		project.getBuildSetting().setNamedQueries(List.of(new NamedBuildQuery("Recent", null)));
		var mapper = newObjectMapper();
		var defaults = mapper.readTree(mapper.writeValueAsString(ProjectDefaults.from(project)));
		var ordinary = mapper.readTree(mapper.writeValueAsString(ProjectResource.ProjectSetting.from(project)));
		assertFalse(defaults.has("defaultRoleIds"));
		assertFalse(defaults.has("defaultRoles"));
		assertFalse(defaults.has("gitPackConfig"));
		var ordinaryData = mapper.readTree(mapper.writeValueAsString(ProjectResource.ProjectData.from(project)));
		assertTrue(ordinaryData.has("gitPackConfig"));
		for (var name: List.of("packSetting", "workspaceSetting", "namedCommitQueries", "namedCodeCommentQueries")) {
			assertFalse(defaults.has(name), name);
			assertTrue(ordinary.has(name), name);
		}
		for (var name: List.of("listFields", "listLinks", "boardSpecs", "namedQueries", "transitionSpecs", "timesheetSettings")) {
			assertFalse(defaults.get("issueSetting").has(name), name);
			assertTrue(ordinary.get("issueSetting").has(name), name);
		}
		for (var name: List.of("listParams", "namedQueries")) {
			assertFalse(defaults.get("buildSetting").has(name), name);
			assertTrue(ordinary.get("buildSetting").has(name), name);
		}
		assertFalse(defaults.get("pullRequestSetting").has("namedQueries"));
		assertTrue(ordinary.get("pullRequestSetting").has("namedQueries"));
	}

	@Test
	void defaultsJsonRejectsUnsupportedSettings() {
		var mapper = newObjectMapper();
		for (var name: List.of("defaultRoles", "defaultRoleIds", "gitPackConfig", "packSetting", "workspaceSetting", "namedCommitQueries", "namedCodeCommentQueries"))
			assertThrows(UnrecognizedPropertyException.class,
					() -> mapper.readValue("{\"" + name + "\":null}", ProjectDefaults.class), name);
		for (var name: List.of("listFields", "listLinks", "boardSpecs", "namedQueries", "transitionSpecs", "timesheetSettings"))
			assertThrows(UnrecognizedPropertyException.class,
					() -> mapper.readValue("{\"issueSetting\":{\"" + name + "\":null}}", ProjectDefaults.class), name);
		for (var name: List.of("listParams", "namedQueries"))
			assertThrows(UnrecognizedPropertyException.class,
					() -> mapper.readValue("{\"buildSetting\":{\"" + name + "\":null}}", ProjectDefaults.class), name);
		assertThrows(UnrecognizedPropertyException.class,
				() -> mapper.readValue("{\"pullRequestSetting\":{\"namedQueries\":[]}}", ProjectDefaults.class));
	}

	@Test
	void supportedDefaultsRoundTripThroughJsonAndPreserveExcludedSettings() throws Exception {
		var source = new Project();
		source.getBuildSetting().setCachePreserveDays(30);
		var property = new JobProperty();
		property.setName("image");
		property.setValue("alpine");
		source.getBuildSetting().getJobProperties().add(property);
		source.getIssueSetting().setBranchPrefix("ticket");
		source.getPullRequestSetting().setDefaultMergeStrategy(MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS);
		source.getPullRequestSetting().setDefaultAssignees(List.of("reviewer"));
		source.getPullRequestSetting().setDeleteSourceBranchAfterMerge(true);
		var mapper = newObjectMapper();
		var defaults = mapper.readValue(mapper.writeValueAsString(ProjectDefaults.from(source)), ProjectDefaults.class);
		assertPaths(validator.validate(defaults));
		var target = new Project();
		var queries = List.of(new NamedBuildQuery("Existing", null));
		target.getBuildSetting().setNamedQueries(queries);
		var gitPackConfig = target.getGitPackConfig();
		gitPackConfig.setThreads("3");
		var packSetting = target.getPackSetting();
		var workspaceSetting = target.getWorkspaceSetting();
		defaults.populate(target);
		assertEquals(30, target.getBuildSetting().getCachePreserveDays());
		assertEquals("alpine", target.getBuildSetting().getJobProperties().get(0).getValue());
		assertEquals("ticket", target.getIssueSetting().getBranchPrefix());
		assertEquals(MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS, target.getPullRequestSetting().getDefaultMergeStrategy());
		assertEquals(List.of("reviewer"), target.getPullRequestSetting().getDefaultAssignees());
		assertTrue(target.getPullRequestSetting().getDeleteSourceBranchAfterMerge());
		assertSame(queries, target.getBuildSetting().getNamedQueries());
		assertSame(gitPackConfig, target.getGitPackConfig());
		assertEquals("3", target.getGitPackConfig().getThreads());
		assertSame(packSetting, target.getPackSetting());
		assertSame(workspaceSetting, target.getWorkspaceSetting());
	}

	private void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	@Test
	void everyProjectAndRepositoryIdEndpointRejectsNegativeIdsBeforeLoading() throws Exception {
		var projects = mock(ProjectService.class);
		int checked = 0;
		for (var resource: List.of(new ProjectResource(), new RepositoryResource())) {
			inject(resource, "projectService", projects);
			for (var method: resource.getClass().getDeclaredMethods()) {
				var parameters = method.getParameters();
				if (parameters.length != 0 && parameters[0].isAnnotationPresent(PathParam.class)
						&& parameters[0].getAnnotation(PathParam.class).value().equals("projectId")) {
					var args = new Object[parameters.length];
					args[0] = Project.DEFAULT_ID;
					for (int i = 1; i < args.length; i++) {
						if (parameters[i].getType() == int.class)
							args[i] = 0;
						else if (parameters[i].getType() == boolean.class)
							args[i] = false;
					}
					var exception = assertThrows(InvocationTargetException.class, () -> method.invoke(resource, args), method.getName());
					assertInstanceOf(NotFoundException.class, exception.getCause(), method.getName());
					checked++;
				}
			}
		}
		assertTrue(checked >= 25);
		verifyNoInteractions(projects);
	}

	@Test
	void parentAndForkReferencesCannotTargetDefaults() {
		var projects = mock(ProjectService.class);
		var project = new Project();
		for (boolean parent: List.of(true, false)) {
			var data = new ProjectResource.ProjectData();
			if (parent)
				data.setParentId(Project.DEFAULT_ID);
			else
				data.setForkedFromId(Project.DEFAULT_ID);
			assertThrows(NotFoundException.class, () -> data.populate(project, projects));
		}
		verifyNoInteractions(projects);
	}

	@Test
	void normalProjectSettingsRemainAccessibleToTheirManager() throws Exception {
		var projects = mock(ProjectService.class);
		var project = new Project(); project.setId(1L);
		when(projects.load(1L)).thenReturn(project);
		var resource = new ProjectResource(); inject(resource, "projectService", projects);
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(() -> SecurityUtils.canManageProject(project)).thenReturn(true);
			assertSame(project.getBuildSetting(), resource.getSetting(1L).getBuildSetting());
		}
	}

	@Test
	void defaultsRequireAdministratorAndActiveSubscriptionBeforeLoading() throws Exception {
		var projects = mock(ProjectService.class);
		var resource = new SettingResource(mock(SettingService.class), mock(AuditService.class), validator);
		inject(resource, "projectService", projects);
		try (var security = mockStatic(SecurityUtils.class); var wicket = mockStatic(WicketUtils.class)) {
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			assertThrows(UnauthorizedException.class, resource::getProjectDefaults);
			assertThrows(UnauthorizedException.class, () -> resource.setProjectDefaults(new ProjectDefaults()));
			assertThrows(UnauthorizedException.class, resource::getProjectDefaultUserAuthorizations);
			assertThrows(UnauthorizedException.class, resource::getProjectDefaultGroupAuthorizations);
			assertThrows(UnauthorizedException.class, resource::getProjectDefaultBaseAuthorizations);
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(false);
			assertThrows(NoSubscriptionException.class, resource::getProjectDefaults);
			assertThrows(NoSubscriptionException.class, () -> resource.setProjectDefaults(new ProjectDefaults()));
			assertThrows(NoSubscriptionException.class, resource::getProjectDefaultBaseAuthorizations);
			verifyNoInteractions(projects);
		}
	}

	@Test
	void administratorsCanRoundTripAllDefaultsWithoutChangingProjectIdentity() throws Exception {
		var projects = mock(ProjectService.class);
		var project = new Project(); project.setId(Project.DEFAULT_ID); project.setName(Project.DEFAULT_NAME);
		project.getGitPackConfig().setThreads("2");
		project.getCodeIndexingSetting().setAnalyzeFiles("**.java");
		project.getAiSetting().setExcludedReviewFiles("vendor/**");
		var spec = new WorkspaceSpec(); spec.setName("dev"); project.getWorkspaceSpecs().add(spec);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(project);
		var audit = mock(AuditService.class);
		var resource = new SettingResource(mock(SettingService.class), audit, validator);
		inject(resource, "projectService", projects);
		var role = new Role(); role.setId(5L); role.setName("Reader");
		var transactions = mock(TransactionService.class);
		doAnswer(invocation -> {
			invocation.getArgument(0, Runnable.class).run();
			return null;
		}).when(transactions).run(any());
		inject(resource, "transactionService", transactions);
		var authorization = new BaseAuthorization(); authorization.setRole(role); authorization.setProject(project);
		project.getBaseAuthorizations().add(authorization);
		try (var security = mockStatic(SecurityUtils.class); var wicket = mockStatic(WicketUtils.class);
				var xml = mockStatic(VersionedXmlDoc.class)) {
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			var document = mock(VersionedXmlDoc.class); when(document.toXML()).thenReturn("audit");
			xml.when(() -> VersionedXmlDoc.fromBean(any())).thenReturn(document);
			var defaults = resource.getProjectDefaults();
			assertEquals("**.java", defaults.getCodeIndexingSetting().getAnalyzeFiles());
			assertEquals("vendor/**", defaults.getAiSetting().getExcludedReviewFiles());
			assertEquals(List.of(spec), defaults.getWorkspaceSpecs());
			var mapper = newObjectMapper();
			defaults = mapper.readValue(mapper.writeValueAsString(defaults), ProjectDefaults.class);
			try (var response = resource.setProjectDefaults(defaults)) {
				assertEquals(200, response.getStatus());
			}
			assertEquals(List.of(authorization), List.copyOf(project.getBaseAuthorizations()));
			defaults = new ProjectDefaults();
			defaults.getIssueSetting().setBranchPrefix("issue-");
			try (var response = resource.setProjectDefaults(defaults)) {
				assertEquals(200, response.getStatus());
			}
			assertEquals("2", project.getGitPackConfig().getThreads());
			assertEquals("issue-", project.getIssueSetting().getBranchPrefix());
			assertEquals(Project.DEFAULT_ID, project.getId());
			assertEquals(Project.DEFAULT_NAME, project.getName());
			assertNull(project.getParent());
			assertEquals(List.of(authorization), List.copyOf(resource.getProjectDefaultBaseAuthorizations()));
			verify(projects, times(2)).update(project);
			verify(audit, times(2)).audit(eq(project), anyString(), eq("audit"), eq("audit"));
			verify(transactions, times(2)).run(any());
		}
	}

	@Test
	void defaultsValidateRequiredContainersAndNestedSettings() {
		var defaults = new ProjectDefaults();
		assertPaths(validator.validate(defaults));
		defaults.setAiSetting(null);
		defaults.setBuildSetting(null);
		defaults.setBranchProtections(null);
		assertPaths(validator.validate(defaults), "aiSetting", "buildSetting", "branchProtections");
		defaults = new ProjectDefaults();
		var property = new JobProperty(); property.setName("");
		defaults.getBuildSetting().getJobProperties().add(property);
		assertFalse(validator.validate(defaults).isEmpty());
	}

	@Test
	void defaultsAuthorizationsDoNotTrustInheritedManagementPermission() throws Exception {
		var project = new Project(); project.setId(Project.DEFAULT_ID);
		var userAuth = new UserAuthorization(); userAuth.setProject(project);
		var groupAuth = new GroupAuthorization(); groupAuth.setProject(project);
		var baseAuth = new BaseAuthorization(); baseAuth.setProject(project);
		var users = mock(UserAuthorizationService.class); when(users.load(1L)).thenReturn(userAuth);
		var groups = mock(GroupAuthorizationService.class); when(groups.load(1L)).thenReturn(groupAuth);
		var bases = mock(BaseAuthorizationService.class); when(bases.load(1L)).thenReturn(baseAuth);
		var audit = mock(AuditService.class);
		var userResource = new UserAuthorizationResource(users, audit);
		var groupResource = new GroupAuthorizationResource(groups, audit);
		var baseResource = new BaseAuthorizationResource(bases, audit);
		try (var security = mockStatic(SecurityUtils.class); var wicket = mockStatic(WicketUtils.class)) {
			security.when(() -> SecurityUtils.canManageProject(project)).thenReturn(true);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			assertThrows(UnauthorizedException.class, () -> userResource.createAuthorization(userAuth));
			assertThrows(UnauthorizedException.class, () -> groupResource.createAuthorization(groupAuth));
			assertThrows(UnauthorizedException.class, () -> baseResource.createAuthorization(baseAuth));
			assertThrows(UnauthorizedException.class, () -> userResource.deleteAuthorization(1L));
			assertThrows(UnauthorizedException.class, () -> groupResource.deleteAuthorization(1L));
			assertThrows(UnauthorizedException.class, () -> baseResource.deleteAuthorization(1L));
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			assertSame(userAuth, userResource.getAuthorization(1L));
			assertSame(groupAuth, groupResource.getAuthorization(1L));
			assertSame(baseAuth, baseResource.getAuthorization(1L));
			verify(users, never()).createOrUpdate(any());
			verify(groups, never()).createOrUpdate(any());
			verify(bases, never()).create(any());
			verifyNoInteractions(audit);
		}
	}
	@Test
	void invalidDefaultsDoNotOverwriteStoredSettings() throws Exception {
		var projects = mock(ProjectService.class);
		var project = new Project(); project.setId(Project.DEFAULT_ID);
		project.getCodeIndexingSetting().setAnalyzeFiles("**.java");
		when(projects.load(Project.DEFAULT_ID)).thenReturn(project);
		var audit = mock(AuditService.class);
		var resource = new SettingResource(mock(SettingService.class), audit, validator);
		inject(resource, "projectService", projects);
		var transactions = mock(TransactionService.class);
		inject(resource, "transactionService", transactions);
		try (var security = mockStatic(SecurityUtils.class); var wicket = mockStatic(WicketUtils.class)) {
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			var defaults = new ProjectDefaults(); defaults.setCodeIndexingSetting(null);
			assertThrows(ExplicitException.class, () -> resource.setProjectDefaults(defaults));
			var nullElements = new ProjectDefaults();
			nullElements.getWorkspaceSpecs().add(null);
			nullElements.getBuildSetting().getJobSecrets().add(null);
			assertPaths(validator.validate(nullElements), "workspaceSpecs[0].<list element>",
					"buildSetting.jobSecrets[0].<list element>");
			assertThrows(ExplicitException.class, () -> resource.setProjectDefaults(nullElements));
			var duplicateSpecs = new ProjectDefaults();
			var firstSpec = new WorkspaceSpec(); firstSpec.setName("dev");
			var secondSpec = new WorkspaceSpec(); secondSpec.setName("dev");
			secondSpec.setDescription("Different configuration with the same name");
			duplicateSpecs.getWorkspaceSpecs().addAll(List.of(firstSpec, secondSpec));
			assertPaths(validator.validate(duplicateSpecs), "workspaceSpecs");
			assertEquals("workspaceSpecs: Workspace spec names must be unique",
					assertThrows(ExplicitException.class, () -> resource.setProjectDefaults(duplicateSpecs)).getMessage());
			assertEquals("**.java", project.getCodeIndexingSetting().getAnalyzeFiles());
			assertTrue(project.getWorkspaceSpecs().isEmpty());
			verify(projects, never()).update(any());
			verifyNoInteractions(audit, transactions);
			secondSpec.setName("Dev");
			assertPaths(validator.validate(duplicateSpecs));
		}
	}

	@Test
	void administratorsCanReadDefaultAuthorizations() throws Exception {
		var project = new Project(); project.setId(Project.DEFAULT_ID);
		var projects = mock(ProjectService.class); when(projects.load(Project.DEFAULT_ID)).thenReturn(project);
		var resource = new SettingResource(mock(SettingService.class), mock(AuditService.class), validator);
		inject(resource, "projectService", projects);
		try (var security = mockStatic(SecurityUtils.class); var wicket = mockStatic(WicketUtils.class)) {
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			assertSame(project.getUserAuthorizations(), resource.getProjectDefaultUserAuthorizations());
			assertSame(project.getGroupAuthorizations(), resource.getProjectDefaultGroupAuthorizations());
			assertSame(project.getBaseAuthorizations(), resource.getProjectDefaultBaseAuthorizations());
		}
	}

}
