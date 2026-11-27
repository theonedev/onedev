package io.onedev.server.service.impl;

import static io.onedev.server.model.PullRequestReview.Status.APPROVED;
import static io.onedev.server.model.PullRequestReview.Status.PENDING;
import static io.onedev.server.model.PullRequestReview.Status.REQUESTED_FOR_CHANGES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.jupiter.api.Test;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestBuildCommitUpdated;
import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.git.service.GitService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;
import io.onedev.server.model.support.code.BuildRequirement;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.exception.RedundantPullRequestException;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.xodus.CommitInfoService;

class DefaultPullRequestServiceTest extends AbstractGitTest {

	private DefaultPullRequestService service;

	private DefaultPullRequestReviewService reviews;

	private PullRequest request;

	private GitService gitService;

	private String baseCommitHash;

	private ObjectId source;

	private ObjectId target;

	@Override
	protected void setup() {
		super.setup();
		try {
			baseCommitHash = addFileAndCommit("shared.txt", "base\n", "Base");
			source = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Source change"));
			git.checkout().setCreateBranch(true).setName("target").setStartPoint(baseCommitHash).call();
			target = ObjectId.fromString(addFileAndCommit("target.txt", "target\n", "Target change"));

			service = new DefaultPullRequestService();
			reviews = new DefaultPullRequestReviewService();
			FieldUtils.writeField(service, "reviewService", reviews, true);
			FieldUtils.writeField(reviews, "commitInfoService", mock(CommitInfoService.class), true);

			var protection = new BranchProtection();
			var project = mock(Project.class);
			when(project.getBranchProtection(any(), any())).thenReturn(protection);
			when(project.getId()).thenReturn(1L);
			var projects = mock(ProjectService.class);
			when(AppLoader.getInstance(ProjectService.class)).thenReturn(projects);
			when(projects.load(1L)).thenReturn(project);
			when(project.getObjectId("refs/heads/target", true)).thenAnswer(call -> target);
			when(project.getRevCommit(any(ObjectId.class), eq(false))).thenAnswer(call -> readCommit(call.getArgument(0)));
			gitService = mock(GitService.class);
			FieldUtils.writeField(reviews, "gitService", gitService, true);
			when(AppLoader.getInstance(GitService.class)).thenReturn(gitService);
			when(gitService.getMergeBase(eq(project), any(), eq(project), any())).thenAnswer(call ->
					GitUtils.getMergeBase(git.getRepository(), call.getArgument(1), call.getArgument(3)));
			when(gitService.getChangedFiles(eq(project), any(), any(), isNull())).thenAnswer(call ->
					GitUtils.getChangedFiles(git.getRepository(), call.getArgument(1), call.getArgument(2)));
			when(gitService.getChangedFilesAfterMerge(eq(project), any(), any(), any())).thenAnswer(call ->
					GitUtils.getChangedFilesAfterMerging(git.getRepository(), call.getArgument(1),
							call.getArgument(2), call.getArgument(3)));
			request = new PullRequest();
			request.setTargetProject(project);
			request.setTargetBranch("target");
			request.setSourceProject(project);
			request.setSubmitter(newUser(1L));

			var reviewer = newUser(2L);
			var groupReviewer = newUser(3L);
			var fileReviewer = newUser(4L);
			var group = mock(Group.class);
			when(group.getName()).thenReturn("reviewers");
			when(group.getMembers()).thenReturn(List.of(groupReviewer));
			protection.setParsedReviewRequirement(new ReviewRequirement(List.of(reviewer), Map.of(group, 1)));
			var fileProtection = new FileProtection();
			fileProtection.setPaths("source.txt shared.txt other.txt");
			fileProtection.setParsedReviewRequirement(new ReviewRequirement(List.of(fileReviewer), Map.of()));
			protection.setFileProtections(List.of(fileProtection));
			for (var user : List.of(reviewer, groupReviewer, fileReviewer))
				addReview(user);
			var aiReviewer = newUser(5L);
			aiReviewer.setType(User.Type.AI);
			addReview(aiReviewer);
			addUpdate(source);
			for (var review : request.getReviews())
				review.setUpdate(request.getLatestUpdate());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void resetsReviewsForOrdinarySourceUpdate() throws Exception {
		var update = request.getLatestUpdate();
		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "More source changes")));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
		for (var review : request.getReviews())
			assertEquals(update, review.getUpdate());
	}

	@Test
	void resetsReviewsForAdditionalSourceCommits() throws Exception {
		git.checkout().setName("main").call();
		var changedSource = ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "More source changes"));
		addUpdate(merge(changedSource, target));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForUnrelatedMerge() throws Exception {
		git.checkout().setCreateBranch(true).setName("other").setStartPoint(baseCommitHash).call();
		var other = ObjectId.fromString(addFileAndCommit("other.txt", "other\n", "Unrelated change"));
		addUpdate(merge(source, other));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForEditsInMergeCommit() throws Exception {
		var merged = merge(source, target);
		git.checkout().setName(merged.name()).call();
		var edited = ObjectId.fromString(addFileAndCommit("source.txt", "unreviewed\n", "Extra edit"));
		addUpdate(commitWithTree(readCommit(edited).getTree(), source, target));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForManualConflictResolution() throws Exception {
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("shared.txt", "source\n", "Source conflict"));
		request.getUpdates().clear();
		addUpdate(source);
		for (var review : request.getReviews())
			review.setUpdate(request.getLatestUpdate());
		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("shared.txt", "target\n", "Target conflict"));
		// Resolving a conflict by keeping the source version still needs review.
		addUpdate(commitWithTree(readCommit(source).getTree(), source, target));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForAdditionalChangesBeforeRebase() throws Exception {
		git.checkout().setName("main").call();
		var changedSource = ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "Extra source change"));
		addUpdate(rebase(changedSource, target));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForAdditionalChangesAfterRebase() throws Exception {
		git.checkout().setName(rebase(source, target).name()).call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "Extra source change")));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForManualRebaseConflictResolution() throws Exception {
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("shared.txt", "source\n", "Source conflict"));
		request.getUpdates().clear();
		addUpdate(source);
		for (var review : request.getReviews())
			review.setUpdate(request.getLatestUpdate());
		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("shared.txt", "target\n", "Target conflict"));
		addFile("source.txt", "source\n");
		var resolved = ObjectId.fromString(addFileAndCommit("shared.txt", "resolved\n", "Resolve conflict"));
		addUpdate(resolved);
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForRebaseOntoUnrelatedBranch() throws Exception {
		git.checkout().setCreateBranch(true).setName("other").setStartPoint(baseCommitHash).call();
		var other = ObjectId.fromString(addFileAndCommit("other.txt", "other\n", "Unrelated change"));
		addUpdate(rebase(source, other));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(PENDING);
	}

	@Test
	void squashMergeFromTargetPreservesRequiredReviews() {
		addUpdate(GitUtils.merge(git.getRepository(), source, target, true, user, user, "Squash target", false));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		for (var review : request.getReviews())
			assertEquals(review.getUser().getType() == User.Type.AI ? PENDING : APPROVED, review.getStatus());
	}

	@Test
	void emptyCommitPreservesBothBranchDecisionsAndTheirUpdate() throws Exception {
		var named = request.getReview(newUser(2L));
		var fromGroup = request.getReview(newUser(3L));
		fromGroup.setStatus(REQUESTED_FOR_CHANGES);
		var reviewed = request.getLatestUpdate();
		git.checkout().setName("main").call();
		var empty = git.commit().setAllowEmpty(true).setMessage("Empty commit").setAuthor(user).setCommitter(user).call();
		addUpdate(empty);

		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(APPROVED, named.getStatus());
		assertEquals(REQUESTED_FOR_CHANGES, fromGroup.getStatus());
		assertEquals(reviewed, named.getUpdate());
		assertEquals(reviewed, fromGroup.getUpdate());
		verify(gitService).getChangedFilesAfterMerge(request.getTargetProject(), source, empty, target);
	}

	@Test
	void duplicateReconciliationPreservesDecisionsOnTheCurrentUpdate() {
		reviews.reconcile(request.getSubmitter(), request, List.of());
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertReviews(APPROVED);
	}

	@Test
	void protectedPathApprovalSurvivesUnrelatedUpdate() throws Exception {
		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("readme.txt", "docs", "Update documentation")));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		for (var review : request.getReviews())
			assertEquals(review.getUser().getId() == 4L ? APPROVED : PENDING, review.getStatus());
	}

	@Test
	void usesEachReviewersNetChangesAndSharesDiffsWithinReconciliation() throws Exception {
		var first = request.getLatestUpdate();
		var alice = newUser(4L);
		var bob = newUser(6L);
		var carol = newUser(7L);
		addReview(bob);
		addReview(carol);
		request.getReview(bob).setStatus(REQUESTED_FOR_CHANGES);
		request.getReview(bob).setUpdate(first);
		var group = mock(Group.class);
		when(group.getName()).thenReturn("file-reviewers");
		when(group.getMembers()).thenReturn(List.of(bob));
		var project = request.getTargetProject();
		var rule = project.getBranchProtection(request.getTargetBranch(), request.getSubmitter()).getFileProtections().get(0);
		rule.setParsedReviewRequirement(new ReviewRequirement(List.of(alice, carol), Map.of(group, 1)));

		git.checkout().setName("main").call();
		var intermediate = ObjectId.fromString(addFileAndCommit("source.txt", "temporary\n", "Edit protected file"));
		addUpdate(intermediate);
		request.getReview(carol).setUpdate(request.getLatestUpdate());
		var reverted = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Revert protected file"));
		addUpdate(reverted);

		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(APPROVED, request.getReview(alice).getStatus());
		assertEquals(REQUESTED_FOR_CHANGES, request.getReview(bob).getStatus());
		assertEquals(PENDING, request.getReview(carol).getStatus());
		verify(gitService).getChangedFilesAfterMerge(project, source, reverted, target);
		verify(gitService).getChangedFilesAfterMerge(project, intermediate, reverted, target);

		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("target.txt", "new target\n", "Advance target"));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		verify(gitService).getChangedFilesAfterMerge(project, source, reverted, target);

		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "final\n", "Change protected file again")));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(PENDING, request.getReview(alice).getStatus());
		assertEquals(PENDING, request.getReview(bob).getStatus());
	}

	@Test
	void addsReviewerForSourceChangeAlreadyPresentOnTarget() {
		var project = request.getTargetProject();
		var reviewer = newUser(6L);
		var rule = project.getBranchProtection(request.getTargetBranch(), request.getSubmitter()).getFileProtections().get(0);
		rule.setParsedReviewRequirement(new ReviewRequirement(List.of(reviewer), Map.of()));
		target = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Apply same change on target"));

		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(PENDING, request.getReview(reviewer).getStatus());
		verify(gitService).getChangedFiles(project, ObjectId.fromString(baseCommitHash), source, null);
	}

	@Test
	void rebasingOntoTargetPreservesFileApprovalWhenOnlyTargetChangedTheFile() {
		target = ObjectId.fromString(addFileAndCommit("shared.txt", "updated on target\n", "Edit protected file on target"));
		var reviewed = request.getLatestUpdate();
		addUpdate(rebase(source, target));

		reviews.reconcile(request.getSubmitter(), request, List.of());
		var review = request.getReview(newUser(4L));
		assertEquals(APPROVED, review.getStatus());
		assertEquals(reviewed, review.getUpdate());
	}

	@Test
	void revertedPathRequestsAnotherReviewOnlyFromExistingFileReviewers() throws Exception {
		var named = newUser(4L);
		var groupReviewer = newUser(6L);
		var otherMember = newUser(7L);
		var missing = newUser(8L);
		addReview(groupReviewer);
		addReview(otherMember);
		var group = mock(Group.class);
		when(group.getName()).thenReturn("reviewers");
		when(group.getMembers()).thenReturn(List.of(otherMember, groupReviewer));
		var rule = request.getTargetProject().getBranchProtection(request.getTargetBranch(), request.getSubmitter())
				.getFileProtections().get(0);
		rule.setPaths("shared.txt");
		rule.setParsedReviewRequirement(new ReviewRequirement(List.of(named, missing), Map.of(group, 1)));

		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("shared.txt", "temporary\n", "Edit protected file")));
		for (var reviewer : List.of(named, groupReviewer, otherMember))
			request.getReview(reviewer).setUpdate(request.getLatestUpdate());
		addUpdate(ObjectId.fromString(addFileAndCommit("shared.txt", "base\n", "Restore protected file")));

		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(PENDING, request.getReview(named).getStatus());
		assertEquals(PENDING, request.getReview(groupReviewer).getStatus());
		assertEquals(APPROVED, request.getReview(otherMember).getStatus());
		assertNull(request.getReview(missing));
	}

	@Test
	void revertKeepsAlreadyPendingFileReviewerPending() throws Exception {
		var review = request.getReview(newUser(4L));
		var reviewed = review.getUpdate();
		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "temporary\n", "Edit protected file")));
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(PENDING, review.getStatus());

		var reverted = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Revert protected file"));
		addUpdate(reverted);
		reviews.reconcile(request.getSubmitter(), request, List.of());
		assertEquals(PENDING, review.getStatus());
		assertEquals(reviewed, review.getUpdate());
	}

	@Test
	void shortagesKeepEligibleReviewersAndBlockMergeUntilEnoughReviewersApprove() {
		var submitter = request.getSubmitter();
		var first = newUser(3L);
		var second = newUser(4L);
		var branchGroup = mock(Group.class);
		when(branchGroup.getName()).thenReturn("Backend");
		when(branchGroup.getMembers()).thenReturn(List.of(first, submitter));
		var fileGroup = mock(Group.class);
		when(fileGroup.getName()).thenReturn("UI");
		when(fileGroup.getMembers()).thenReturn(List.of(submitter));
		var protection = request.getTargetProject().getBranchProtection(request.getTargetBranch(), submitter);
		protection.setParsedReviewRequirement(new ReviewRequirement(List.of(), Map.of(branchGroup, 2)));
		protection.getFileProtections().get(0).setParsedReviewRequirement(new ReviewRequirement(List.of(), Map.of(fileGroup, 1)));

		reviews.reconcile(submitter, request, List.of());
		assertTrue(request.isAllReviewsApproved());
		assertTrue(request.checkMergeCondition().contains("Group 'Backend' cannot meet its reviewer requirement (2 required, 1 eligible)"));
		assertTrue(request.checkMergeCondition().contains("Group 'UI' cannot meet its reviewer requirement (1 required, 0 eligible)"));
		assertThrows(NotAcceptableException.class, () -> reviews.reconcile(submitter, request, List.of(first)));
		assertEquals(APPROVED, request.getReview(first).getStatus());

		request.getReview(second).setStatus(PullRequestReview.Status.EXCLUDED);
		when(branchGroup.getMembers()).thenReturn(List.of(first, second, submitter));
		when(fileGroup.getMembers()).thenReturn(List.of(second));
		reviews.reconcile(submitter, request, List.of());
		assertNull(request.getCheckError());
		assertEquals(APPROVED, request.getReview(first).getStatus());
		assertEquals(PENDING, request.getReview(second).getStatus());
		assertFalse(request.isAllReviewsApproved());
		request.getReview(second).setStatus(APPROVED);
		assertTrue(request.isAllReviewsApproved());
	}

	@Test
	void rejectingMandatoryRemovalPreservesEveryReview() {
		var users = request.getReviews().stream().map(PullRequestReview::getUser).toList();
		assertThrows(NotAcceptableException.class, () -> reviews.reconcile(request.getSubmitter(), request, users));
		assertReviews(APPROVED);
	}

	@Test
	void legacyDecisionsAreRequestedAgainWhenRequired() {
		for (var review : request.getReviews())
			review.setUpdate(null);
		var optional = newUser(6L);
		addReview(optional);
		reviews.reconcile(request.getSubmitter(), request, List.of());
		for (var review : request.getReviews())
			assertEquals(review.getUser().equals(optional) ? APPROVED : PENDING, review.getStatus());
	}

	@Test
	void addingAReviewerPreservesDecisionsAndCanRestoreAnExcludedReviewer() {
		var actor = spy(request.getSubmitter());
		request.setSubmitter(actor);
		var reviewer = spy(newUser(6L));
		var reviewerSubject = mock(Subject.class);
		doReturn(reviewerSubject).when(reviewer).asSubject();

		var denied = assertThrows(NotAcceptableException.class, () -> reviews.addReviewer(actor, request, reviewer));
		assertEquals("Reviewer needs code read permission for the project", denied.getMessage());
		assertNull(request.getReview(reviewer));

		var actorSubject = mock(Subject.class);
		doReturn(actorSubject).when(actor).asSubject();
		when(actorSubject.isPermitted(any(ProjectPermission.class))).thenReturn(true);
		var selfReview = assertThrows(NotAcceptableException.class, () -> reviews.addReviewer(actor, request, actor));
		assertEquals("Pull request submitter cannot be reviewer", selfReview.getMessage());

		when(reviewerSubject.isPermitted(any(ProjectPermission.class))).thenReturn(true);
		reviews.addReviewer(actor, request, reviewer);
		var review = request.getReview(reviewer);
		assertEquals(PENDING, review.getStatus());
		assertNull(review.getUpdate());
		review.setStatus(APPROVED);
		review.setUpdate(request.getLatestUpdate());
		reviews.addReviewer(actor, request, reviewer);
		assertEquals(APPROVED, review.getStatus());
		assertEquals(request.getLatestUpdate(), review.getUpdate());
		reviews.requestReview(actor, request, reviewer);
		assertEquals(PENDING, review.getStatus());
		assertEquals(request.getLatestUpdate(), review.getUpdate());

		reviews.reconcile(actor, request, List.of(reviewer));
		assertEquals(PullRequestReview.Status.EXCLUDED, review.getStatus());
		assertEquals(request.getLatestUpdate(), review.getUpdate());
		reviews.addReviewer(actor, request, reviewer);
		assertEquals(PENDING, review.getStatus());
		assertEquals(1, request.getReviews().stream().filter(it -> it.getUser().equals(reviewer)).count());
	}

	@Test
	void preparedDraftPreservesEditsAndRechecksForAnEffectiveRequest() {
		var draft = new PullRequest();
		var project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		var projects = mock(ProjectService.class);
		when(AppLoader.getInstance(ProjectService.class)).thenReturn(projects);
		when(projects.load(1L)).thenReturn(project);
		when(project.getObjectId("refs/heads/source", true)).thenReturn(source);
		when(project.getObjectId("refs/heads/main", true)).thenReturn(target);
		var gitService = mock(GitService.class);
		when(AppLoader.getInstance(GitService.class)).thenReturn(gitService);
		when(gitService.getMergeBase(project, target, project, source)).thenReturn(ObjectId.fromString(baseCommitHash));
		when(project.getBranchProtection(any(), any())).thenReturn(new BranchProtection());
		when(project.findDefaultPullRequestAssignees()).thenReturn(List.of(newUser(2L)));
		draft.setSubmitter(newUser(1L));
		draft.setTargetProject(project);
		draft.setTargetBranch("main");
		draft.setSourceProject(project);
		draft.setSourceBranch("source");
		var pullRequests = spy(service);
		doReturn(null).when(pullRequests).findEffective(any(), any());
		pullRequests.prepareDraft(draft);
		assertEquals(1, draft.getAssignments().size());
		var first = draft.getLatestUpdate();
		draft.getAssignments().clear();
		pullRequests.prepareDraft(draft);
		assertTrue(draft.getAssignments().isEmpty());
		assertEquals(List.of(first), draft.getSortedUpdates());

		var existing = new PullRequest();
		doReturn(existing).when(pullRequests).findEffective(any(), any());
		assertThrows(RedundantPullRequestException.class, () -> pullRequests.open(draft));
		existing.setStatus(PullRequest.Status.MERGED);
		assertThrows(RedundantPullRequestException.class, () -> pullRequests.open(draft));
	}

	@Test
	void targetOnlyChangesAdvanceBuildCommitForStrictBuildsOrManualSync() throws Exception {
		var pr = spy(request);
		pr.setId(1L);
		pr.setNumber(1L);
		pr.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
		var project = pr.getTargetProject();
		when(project.getObjectId("refs/heads/target", false)).thenReturn(target);
		var listenerRegistry = mock(ListenerRegistry.class);
		FieldUtils.writeField(service, "listenerRegistry", listenerRegistry, true);
		FieldUtils.writeField(service, "gitService", gitService, true);

		var previous = preview(source, ObjectId.fromString(baseCommitHash), source);
		var next = preview(source, target, merge(target, source));
		pr.setMergePreview(previous);
		pr.setBuildCommitHash(previous.getMergeCommitHash());
		doReturn(new BuildRequirement(List.of("test"), false)).when(pr).getBuildRequirement();
		try (var previews = mockStatic(MergePreview.class)) {
			previews.when(() -> MergePreview.calculate(eq(pr), any(String.class))).thenReturn(next);
			service.reconcilePreview(pr, false);
			assertEquals(next, pr.getMergePreview());
			assertEquals(previous.getMergeCommitHash(), pr.getBuildCommitHash());

			service.reconcilePreview(pr, true);
			assertEquals(next.getMergeCommitHash(), pr.getBuildCommitHash());

			// Reopening or retargeting clears the preview and selects a new build commit.
			pr.setMergePreview(null);
			pr.setBuildCommitHash(previous.getMergeCommitHash());
			service.reconcilePreview(pr, false);
			assertEquals(next.getMergeCommitHash(), pr.getBuildCommitHash());

			pr.setMergePreview(previous);
			pr.setBuildCommitHash(previous.getMergeCommitHash());
			doReturn(new BuildRequirement(List.of("test"), true)).when(pr).getBuildRequirement();
			service.reconcilePreview(pr, false);
			assertEquals(next.getMergeCommitHash(), pr.getBuildCommitHash());

			pr.setMergePreview(previous);
			service.reconcilePreview(pr, false);
			service.reconcilePreview(pr, true);
			verify(listenerRegistry, times(3)).post(any(PullRequestBuildCommitUpdated.class));
		}
	}

	@Test
	void legacyPreviewCannotAuthorizeAMergeBeforeReconciliation() {
		var preview = preview(source, target, target);
		preview.setTargetBranch(null);
		request.setMergePreview(preview);
		assertFalse(preview.isUpToDate(request));
		preview.setTargetBranch("release");
		assertFalse(preview.isUpToDate(request));
	}

	private MergePreview preview(ObjectId head, ObjectId targetHead, ObjectId merged) {
		var preview = new MergePreview();
		preview.setTargetBranch(request.getTargetBranch());
		preview.setTargetHeadCommitHash(targetHead.name());
		preview.setHeadCommitHash(head.name());
		preview.setMergeStrategy(MergeStrategy.CREATE_MERGE_COMMIT);
		preview.setMergeCommitHash(merged.name());
		return preview;
	}

	private ObjectId rebase(ObjectId source, ObjectId target) {
		var rebased = GitUtils.rebase(git.getRepository(), source, target, user);
		assertNotNull(rebased);
		return rebased;
	}

	private ObjectId merge(ObjectId first, ObjectId second) {
		return GitUtils.merge(git.getRepository(), first, second, false, user, user, "Merge target", false);
	}

	private ObjectId commitWithTree(ObjectId tree, ObjectId... parents) throws Exception {
		var commit = new CommitBuilder();
		commit.setTreeId(tree);
		commit.setParentIds(parents);
		commit.setAuthor(user);
		commit.setCommitter(user);
		commit.setMessage("Merge with manual edits");
		try (var inserter = git.getRepository().newObjectInserter()) {
			var id = inserter.insert(commit);
			inserter.flush();
			return id;
		}
	}

	private RevCommit readCommit(ObjectId id) throws Exception {
		try (var walk = new RevWalk(git.getRepository())) {
			return walk.parseCommit(id);
		}
	}

	private User newUser(Long id) {
		var user = new User();
		user.setId(id);
		user.setName("user" + id);
		return user;
	}

	private void addReview(User user) {
		var review = new PullRequestReview();
		review.setRequest(request);
		review.setUser(user);
		review.setStatus(APPROVED);
		request.getReviews().add(review);
	}

	private void addUpdate(ObjectId head) {
		var update = new PullRequestUpdate();
		update.setId((long) request.getUpdates().size() + 1);
		update.setRequest(request);
		update.setHeadCommitHash(head.name());
		request.getUpdates().add(update);
	}

	private void assertReviews(PullRequestReview.Status status) {
		for (var review : request.getReviews())
			assertEquals(status, review.getStatus(), review.getUser().getName());
	}
}
