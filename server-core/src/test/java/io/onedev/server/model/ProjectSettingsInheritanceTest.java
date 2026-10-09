package io.onedev.server.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.model.support.build.DefaultFixedIssueFilter;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.model.support.wiki.SpecifiedPath;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;

public class ProjectSettingsInheritanceTest {

	private MockedStatic<OneDev> oneDev;
	private SubscriptionService subscription;
	private ProjectService projects;
	private Project defaults;
	private Project root;
	private Project child;

	@BeforeEach
	void setUp() {
		oneDev = mockStatic(OneDev.class);
		subscription = mock(SubscriptionService.class);
		projects = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		root = project(1L, "root");
		child = project(2L, "child");
		child.setParent(root);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
		when(subscription.isSubscriptionActive()).thenReturn(true);
	}

	@AfterEach
	void tearDown() {
		oneDev.close();
	}

	private Project project(Long id, String name) {
		var project = new Project();
		project.setId(id);
		project.setName(name);
		return project;
	}

	@Test
	void defaultsExtendSettingsButNotTheProjectTree() {
		assertSame(defaults, root.getSettingsParent());
		assertSame(root, child.getSettingsParent());
		assertNull(defaults.getSettingsParent());
		assertNull(root.getParent());
		assertEquals("root/child", child.calcPath());
		assertFalse(defaults.isSelfOrAncestorOf(child));
		assertTrue(defaults.isSelfOrSettingsAncestorOf(child));
		assertTrue(root.isSelfOrSettingsAncestorOf(child));
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertNull(root.getSettingsParent());
		assertSame(root, child.getSettingsParent());
		assertFalse(defaults.isSelfOrSettingsAncestorOf(child));
	}

	@Test
	void scalarSettingsUseDefaultsAndAllowRootAndChildOverrides() {
		defaults.getCodeIndexingSetting().setAnalyzeFiles("**.java");
		defaults.getCodeIndexingSetting().setRequireLoginForAutoIndexing(true);
		defaults.getAiSetting().setExcludedReviewFiles("vendor/**");
		defaults.getIssueSetting().setBranchPrefix("issue-");
		defaults.getBuildSetting().setCachePreserveDays(30);
		defaults.getPullRequestSetting().setDefaultMergeStrategy(MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS);
		defaults.getPullRequestSetting().setDeleteSourceBranchAfterMerge(true);
		var folder = new SpecifiedPath();
		folder.setPath("docs");
		defaults.getWikiSetting().setFolder(folder);
		for (var project : List.of(root, child)) {
			assertEquals("**.java", project.findCodeAnalysisFiles());
			assertTrue(project.findRequireLoginForAutoIndexing());
			assertEquals("vendor/**", project.findExcludedAiReviewFiles());
			assertEquals("issue-", project.findIssueBranchPrefix());
			assertEquals(30, project.getHierarchyCachePreserveDays());
			assertEquals(MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS, project.findDefaultPullRequestMergeStrategy());
			assertTrue(project.findDeleteBranchAfterPullRequestMerge());
			assertSame(folder, project.getWikiFolder());
		}
		root.getCodeIndexingSetting().setAnalyzeFiles("**.go");
		root.getCodeIndexingSetting().setRequireLoginForAutoIndexing(false);
		root.getAiSetting().setExcludedReviewFiles("generated/**");
		root.getIssueSetting().setBranchPrefix("ticket-");
		root.getBuildSetting().setCachePreserveDays(10);
		root.getPullRequestSetting().setDefaultMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
		root.getPullRequestSetting().setDeleteSourceBranchAfterMerge(false);
		var rootFolder = new SpecifiedPath();
		root.getWikiSetting().setFolder(rootFolder);
		assertEquals("**.go", child.findCodeAnalysisFiles());
		assertFalse(child.findRequireLoginForAutoIndexing());
		assertEquals("generated/**", child.findExcludedAiReviewFiles());
		assertEquals("ticket-", child.findIssueBranchPrefix());
		assertEquals(10, child.getHierarchyCachePreserveDays());
		assertEquals(MergeStrategy.CREATE_MERGE_COMMIT, child.findDefaultPullRequestMergeStrategy());
		assertFalse(child.findDeleteBranchAfterPullRequestMerge());
		assertSame(rootFolder, child.getWikiFolder());
		child.getIssueSetting().setBranchPrefix("child-");
		assertEquals("child-", child.findIssueBranchPrefix());
	}

	@Test
	void inactiveSubscriptionKeepsBuiltInFallbacks() {
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertEquals("**", child.findCodeAnalysisFiles());
		assertFalse(child.findRequireLoginForAutoIndexing());
		assertNull(child.findExcludedAiReviewFiles());
		assertNull(child.findIssueBranchPrefix());
		assertEquals(ProjectBuildSetting.DEFAULT_CACHE_PRESERVE_DAYS, child.getHierarchyCachePreserveDays());
		assertEquals(MergeStrategy.CREATE_MERGE_COMMIT, child.findDefaultPullRequestMergeStrategy());
		assertFalse(child.findDeleteBranchAfterPullRequestMerge());
		assertEquals("wiki", child.getWikiFolder().getPath());
		assertTrue(child.findDefaultPullRequestAssignees().isEmpty());
		verify(projects, never()).load(Project.DEFAULT_ID);
	}

	@Test
	void additiveSettingsKeepChildParentDefaultOrderAndIgnoreInactiveDefaults() {
		var defaultBranch = new BranchProtection();
		var rootBranch = new BranchProtection();
		var childBranch = new BranchProtection();
		defaults.getBranchProtections().add(defaultBranch);
		root.getBranchProtections().add(rootBranch);
		child.getBranchProtections().add(childBranch);
		var tag = new TagProtection();
		var secret = new JobSecret();
		var filter = new DefaultFixedIssueFilter();
		var preservation = new BuildPreservation();
		var hook = new WebHook();
		defaults.getTagProtections().add(tag);
		defaults.getBuildSetting().getJobSecrets().add(secret);
		defaults.getBuildSetting().getDefaultFixedIssueFilters().add(filter);
		defaults.getBuildSetting().getBuildPreservations().add(preservation);
		defaults.getWebHooks().add(hook);
		assertEquals(List.of(childBranch, rootBranch, defaultBranch), child.getHierarchyBranchProtections());
		assertEquals(List.of(tag), child.getHierarchyTagProtections());
		assertEquals(List.of(secret), child.getHierarchyJobSecrets());
		assertEquals(List.of(filter), child.getHierarchyDefaultFixedIssueFilters());
		assertEquals(List.of(preservation), child.getHierarchyBuildPreservations());
		assertEquals(List.of(hook), child.getHierarchyWebHooks());
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertEquals(List.of(childBranch, rootBranch), child.getHierarchyBranchProtections());
		assertTrue(child.getHierarchyTagProtections().isEmpty());
		assertTrue(child.getHierarchyJobSecrets().isEmpty());
		assertTrue(child.getHierarchyDefaultFixedIssueFilters().isEmpty());
		assertTrue(child.getHierarchyBuildPreservations().isEmpty());
		assertTrue(child.getHierarchyWebHooks().isEmpty());
	}

	@Test
	void namedSettingsOverrideDefaultsByName() {
		var defaultProperty = new JobProperty();
		defaultProperty.setName("image");
		var rootProperty = new JobProperty();
		rootProperty.setName("image");
		defaults.getBuildSetting().getJobProperties().add(defaultProperty);
		assertEquals(List.of(defaultProperty), root.getHierarchyJobProperties());
		root.getBuildSetting().getJobProperties().add(rootProperty);
		assertEquals(List.of(rootProperty), child.getHierarchyJobProperties());
		var defaultSpec = new WorkspaceSpec();
		defaultSpec.setName("dev");
		var rootSpec = new WorkspaceSpec();
		rootSpec.setName("dev");
		var extraSpec = new WorkspaceSpec();
		extraSpec.setName("extra");
		defaults.getWorkspaceSpecs().add(defaultSpec);
		defaults.getWorkspaceSpecs().add(extraSpec);
		root.getWorkspaceSpecs().add(rootSpec);
		assertEquals(List.of(rootSpec, extraSpec), child.getHierarchyWorkspaceSpecs());
	}

	@Test
	void defaultAssigneesAreCheckedAgainstTheTargetProject() {
		var users = mock(UserService.class);
		var user = mock(User.class);
		oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(users);
		when(users.findByName("reviewer")).thenReturn(user);
		defaults.getPullRequestSetting().setDefaultAssignees(new ArrayList<>(List.of("reviewer")));
		try (var security = mockStatic(SecurityUtils.class)) {
			security.when(() -> SecurityUtils.canWriteCode(user.asSubject(), child)).thenReturn(true);
			assertEquals(List.of(user), child.findDefaultPullRequestAssignees());
			security.when(() -> SecurityUtils.canWriteCode(user.asSubject(), child)).thenReturn(false);
			assertThrows(io.onedev.commons.utils.ExplicitException.class, child::findDefaultPullRequestAssignees);
		}
	}
}
