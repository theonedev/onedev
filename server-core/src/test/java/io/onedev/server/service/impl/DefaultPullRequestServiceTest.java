package io.onedev.server.service.impl;

import static io.onedev.server.model.PullRequestReview.Status.APPROVED;
import static io.onedev.server.model.PullRequestReview.Status.EXCLUDED;
import static io.onedev.server.model.PullRequestReview.Status.PENDING;
import static io.onedev.server.model.PullRequestReview.Status.REQUESTED_FOR_CHANGES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.jupiter.api.Test;

import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.xodus.CommitInfoService;

class DefaultPullRequestServiceTest extends AbstractGitTest {

	private DefaultPullRequestService service;

	private PullRequest request;

	private ObjectId source;

	private ObjectId target;

	@Override
	protected void setup() {
		super.setup();
		try {
			var base = addFileAndCommit("shared.txt", "base\n", "Base");
			source = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Source change"));
			git.checkout().setCreateBranch(true).setName("target").setStartPoint(base).call();
			target = ObjectId.fromString(addFileAndCommit("target.txt", "target\n", "Target change"));

			var gitService = mock(GitService.class);
			when(gitService.merge(any(), any(), any(), anyBoolean(), any(), any(), anyString(), anyBoolean()))
					.thenAnswer(it -> GitUtils.merge(git.getRepository(), it.getArgument(1), it.getArgument(2),
							it.getArgument(3), it.getArgument(4), it.getArgument(5), it.getArgument(6), it.getArgument(7)));
			when(gitService.isMergedInto(any(), any(), any(), any())).thenAnswer(it -> {
				try (var walk = new RevWalk(git.getRepository())) {
					return walk.isMergedInto(walk.parseCommit(it.getArgument(2)), walk.parseCommit(it.getArgument(3)));
				}
			});
			when(gitService.getCommit(any(), any())).thenAnswer(it -> readCommit(it.getArgument(1)));
			when(gitService.getMergeBase(any(), any(), any(), any()))
					.thenAnswer(it -> GitUtils.getMergeBase(git.getRepository(), it.getArgument(1), it.getArgument(3)));
			when(gitService.rebase(any(), any(), any(), any()))
					.thenAnswer(it -> GitUtils.rebase(git.getRepository(), it.getArgument(1), it.getArgument(2), it.getArgument(3)));
			service = new DefaultPullRequestService();
			FieldUtils.writeField(service, "gitService", gitService, true);
			FieldUtils.writeField(service, "commitInfoService", mock(CommitInfoService.class), true);

			var protection = new BranchProtection();
			var project = mock(Project.class);
			when(project.getBranchProtection(any(), any())).thenReturn(protection);
			when(project.getRevCommit(any(ObjectId.class), anyBoolean())).thenAnswer(it -> readCommit(it.getArgument(0)));
			request = new PullRequest();
			request.setTargetProject(project);
			request.setTargetBranch("main");
			request.setSubmitter(newUser(1L));
			request.setBaseCommitHash(base);

			var reviewer = newUser(2L);
			var groupReviewer = newUser(3L);
			var fileReviewer = newUser(4L);
			var group = mock(Group.class);
			when(group.getName()).thenReturn("reviewers");
			when(group.getMembers()).thenReturn(List.of(groupReviewer));
			protection.setParsedReviewRequirement(new ReviewRequirement(List.of(reviewer), Map.of(group, 1)));
			var fileProtection = new FileProtection();
			fileProtection.setPaths("source.txt");
			fileProtection.setParsedReviewRequirement(new ReviewRequirement(List.of(fileReviewer), Map.of()));
			protection.setFileProtections(List.of(fileProtection));
			for (var user : List.of(reviewer, groupReviewer, fileReviewer))
				addReview(user);
			var aiReviewer = newUser(5L);
			aiReviewer.setType(User.Type.AI);
			addReview(aiReviewer);
			addUpdate(source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void preservesReviewsForBothMergeParentOrders() throws Exception {
		addUpdate(merge(source, target));
		service.checkReviews(request, true);
		assertReviews(APPROVED);
		request.getUpdates().remove(request.getLatestUpdate());
		addUpdate(merge(target, source));
		service.checkReviews(request, true);
		assertReviews(APPROVED);
	}

	@Test
	void preservesPendingAndRequestedChangesAndEnforcesRequiredReviewers() {
		request.getReviews().forEach(review -> review.setStatus(REQUESTED_FOR_CHANGES));
		addUpdate(merge(source, target));
		service.checkReviews(request, true);
		assertReviews(REQUESTED_FOR_CHANGES);
		request.getReviews().forEach(review -> review.setStatus(EXCLUDED));
		service.checkReviews(request, true);
		for (var review : request.getReviews())
			assertEquals(review.getUser().getType() == User.Type.AI ? EXCLUDED : PENDING, review.getStatus());
		service.checkReviews(request, true);
		for (var review : request.getReviews())
			assertEquals(review.getUser().getType() == User.Type.AI ? EXCLUDED : PENDING, review.getStatus());
	}

	@Test
	void preservesReviewsIfTargetAdvancesAfterMerge() {
		var merged = merge(source, target);
		target = ObjectId.fromString(addFileAndCommit("later.txt", "later\n", "Later target change"));
		addUpdate(merged);
		service.checkReviews(request, true);
		assertReviews(APPROVED);
	}

	@Test
	void resetsReviewsForOrdinarySourceUpdate() throws Exception {
		git.checkout().setName("main").call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "More source changes")));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForAdditionalSourceCommits() throws Exception {
		git.checkout().setName("main").call();
		var changedSource = ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "More source changes"));
		addUpdate(merge(changedSource, target));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForUnrelatedMerge() throws Exception {
		git.checkout().setCreateBranch(true).setName("other").setStartPoint(request.getBaseCommitHash()).call();
		var other = ObjectId.fromString(addFileAndCommit("other.txt", "other\n", "Unrelated change"));
		addUpdate(merge(source, other));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForEditsInMergeCommit() throws Exception {
		var merged = merge(source, target);
		git.checkout().setName(merged.name()).call();
		var edited = ObjectId.fromString(addFileAndCommit("source.txt", "unreviewed\n", "Extra edit"));
		addUpdate(commitWithTree(readCommit(edited).getTree(), source, target));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForManualConflictResolution() throws Exception {
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("shared.txt", "source\n", "Source conflict"));
		request.getUpdates().clear();
		addUpdate(source);
		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("shared.txt", "target\n", "Target conflict"));
		// Resolving a conflict by keeping the source version still needs review.
		addUpdate(commitWithTree(readCommit(source).getTree(), source, target));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void preservesReviewsForRebaseWithMultipleCommitsAndEmptyCommit() throws Exception {
		git.checkout().setName("main").call();
		git.commit().setAllowEmpty(true).setSign(false).setAuthor(user).setCommitter(user).setMessage("Empty commit").call();
		source = ObjectId.fromString(addFileAndCommit("second.txt", "second\n", "Second source change"));
		request.getUpdates().clear();
		addUpdate(source);
		addUpdate(rebase(source, target));
		service.checkReviews(request, true);
		assertReviews(APPROVED);
	}

	@Test
	void preservesReviewsForTerminalRebaseEvenIfTargetAdvancesBeforePush() throws Exception {
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("second.txt", "second\n", "Second source change"));
		request.getUpdates().clear();
		addUpdate(source);
		var rebased = rebaseFromTerminal();
		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("later.txt", "later\n", "Later target change"));
		addUpdate(rebased);
		service.checkReviews(request, true);
		assertReviews(APPROVED);
	}

	@Test
	void preservesReviewsWhenRebaseDropsAlreadyAppliedCommit() throws Exception {
		target = ObjectId.fromString(addFileAndCommit("source.txt", "source\n", "Apply first source change"));
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("second.txt", "second\n", "Second source change"));
		request.getUpdates().clear();
		addUpdate(source);
		addUpdate(rebaseFromTerminal());
		service.checkReviews(request, true);
		assertReviews(APPROVED);
	}

	@Test
	void resetsReviewsForAdditionalChangesBeforeRebase() throws Exception {
		git.checkout().setName("main").call();
		var changedSource = ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "Extra source change"));
		addUpdate(rebase(changedSource, target));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForAdditionalChangesAfterRebase() throws Exception {
		git.checkout().setName(rebase(source, target).name()).call();
		addUpdate(ObjectId.fromString(addFileAndCommit("source.txt", "changed\n", "Extra source change")));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForManualRebaseConflictResolution() throws Exception {
		git.checkout().setName("main").call();
		source = ObjectId.fromString(addFileAndCommit("shared.txt", "source\n", "Source conflict"));
		request.getUpdates().clear();
		addUpdate(source);
		git.checkout().setName("target").call();
		target = ObjectId.fromString(addFileAndCommit("shared.txt", "target\n", "Target conflict"));
		addFile("source.txt", "source\n");
		var resolved = ObjectId.fromString(addFileAndCommit("shared.txt", "resolved\n", "Resolve conflict"));
		addUpdate(resolved);
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForRebaseOntoUnrelatedBranch() throws Exception {
		git.checkout().setCreateBranch(true).setName("other").setStartPoint(request.getBaseCommitHash()).call();
		var other = ObjectId.fromString(addFileAndCommit("other.txt", "other\n", "Unrelated change"));
		addUpdate(rebase(source, other));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	@Test
	void resetsReviewsForSquashMergeFromTarget() {
		addUpdate(GitUtils.merge(git.getRepository(), source, target, true, user, user, "Squash target", false));
		service.checkReviews(request, true);
		assertReviews(PENDING);
	}

	private ObjectId rebase(ObjectId source, ObjectId target) {
		var rebased = GitUtils.rebase(git.getRepository(), source, target, user);
		assertNotNull(rebased);
		return rebased;
	}

	private ObjectId rebaseFromTerminal() throws Exception {
		var builder = new ProcessBuilder("git", "-c", "user.name=Test", "-c", "user.email=test@example.com",
				"-c", "commit.gpgSign=false", "-c", "core.hooksPath=/dev/null", "rebase", target.name());
		builder.directory(gitDir).redirectErrorStream(true);
		builder.environment().put("GIT_CONFIG_NOSYSTEM", "1");
		builder.environment().put("GIT_CONFIG_GLOBAL", "/dev/null");
		var process = builder.start();
		var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(0, process.waitFor(), output);
		return git.getRepository().resolve("HEAD");
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
		var update = new PullRequestUpdate() {
			@Override
			public Collection<String> getChangedFiles() {
				return List.of("source.txt", "target.txt");
			}
		};
		update.setId((long) request.getUpdates().size() + 1);
		update.setRequest(request);
		update.setHeadCommitHash(head.name());
		update.setTargetHeadCommitHash(target.name());
		request.getUpdates().add(update);
	}

	private void assertReviews(PullRequestReview.Status status) {
		for (var review : request.getReviews())
			assertEquals(status, review.getStatus(), review.getUser().getName());
	}
}
