package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestCheckFailed;
import io.onedev.server.event.project.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.project.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.xodus.CommitInfoService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

@Singleton
public class DefaultPullRequestReviewService extends BaseEntityService<PullRequestReview>
		implements PullRequestReviewService {

	@Inject
	private PullRequestChangeService changeService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Inject
	private CommitInfoService commitInfoService;

	@Inject
	private UserService userService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private GitService gitService;

	private void lockOpenRequest(PullRequest request) {
		if (!request.isNew())
			request.lockForUpdate(getSession());
		if (!request.isOpen())
			throw new NotAcceptableException("Pull request is closed");
	}

	@Transactional
	@Override
	public void addReviewer(User actor, PullRequest request, User reviewer) {
		lockOpenRequest(request);
		if (!SecurityUtils.canReadCode(reviewer.asSubject(), request.getProject()))
			throw new NotAcceptableException("Reviewer needs code read permission for the project");
		if (reviewer.equals(request.getSubmitter()))
			throw new NotAcceptableException("Pull request submitter cannot be reviewer");

		var review = request.getReview(reviewer);
		if (review == null) {
			saveReviewerChange(actor, newReview(request, reviewer));
		} else if (review.getStatus() == Status.EXCLUDED) {
			review.setStatus(Status.PENDING);
			saveReviewerChange(actor, review);
		}
	}

	@Transactional
	@Override
	public void requestReview(User actor, PullRequest request, User reviewer) {
		lockOpenRequest(request);
		var review = getActiveReview(request, reviewer);
		if (review.getStatus() != Status.PENDING) {
			review.setStatus(Status.PENDING);
			saveReviewerChange(actor, review);
		}
	}

	private PullRequestReview newReview(PullRequest request, User reviewer) {
		var review = new PullRequestReview();
		review.setRequest(request);
		review.setUser(reviewer);
		request.getReviews().add(review);
		return review;
	}

	private PullRequestReview getActiveReview(PullRequest request, User reviewer) {
		var review = request.getReview(reviewer);
		if (review == null || review.getStatus() == Status.EXCLUDED)
			throw new NotAcceptableException("User is not an active reviewer of this pull request");
		return review;
	}

	private void saveReviewerChange(User actor, PullRequestReview review) {
		var request = review.getRequest();
		if (!request.isNew()) {
			dao.persist(review);
			if (review.getStatus() == Status.PENDING)
				listenerRegistry.post(new PullRequestReviewRequested(actor, new Date(), request, review.getUser()));
			else
				listenerRegistry.post(new PullRequestReviewerRemoved(actor, new Date(), request, review.getUser()));
		}
	}

	@Sessional
	@Override
	public void reconcile(User actor, PullRequest request, Collection<User> proposedExclusions) {
		if (request.isNew()) {
			request.setCheckError(reconcileReviews(actor, request, proposedExclusions));
			return;
		}
		transactionService.run(() -> {
			lockOpenRequest(request);
			var shortage = reconcileReviews(actor, request, proposedExclusions);
			var previousError = request.getCheckError();
			// Only a full reconciliation may clear a check error from an earlier failure.
			if (proposedExclusions.isEmpty() || previousError == null) {
				request.setCheckError(shortage);
				if (previousError == null && shortage != null)
					listenerRegistry.post(new PullRequestCheckFailed(request));
			}
		});
	}

	private @Nullable String reconcileReviews(User actor, PullRequest request, Collection<User> proposedExclusions) {
		var exclusions = new HashSet<>(proposedExclusions);
		var shortages = new LinkedHashSet<String>();
		var latestUpdate = request.getLatestUpdate();
		var protection = request.getTargetProject().getBranchProtection(request.getTargetBranch(), request.getSubmitter());
		var branchRule = protection.getParsedReviewRequirement();
		Collection<String> changedFiles = branchRule.getGroups().isEmpty() && protection.getFileProtections().isEmpty()
				? Set.of()
				: gitService.getChangedFiles(request.getTargetProject(), request.getMergeBase(), latestUpdate.getHeadCommitId(), null);
		var branchReviewers = selectReviewers(request, branchRule, changedFiles, exclusions, Set.of(), shortages);
		var requiredReviewers = new HashSet<>(branchReviewers);
		var fileReviewers = new FileReviewers(request, protection, exclusions);
		if (!protection.getFileProtections().isEmpty()) {
			for (var path : changedFiles) {
				requiredReviewers.addAll(fileReviewers.forPath(path, rule ->
						selectReviewers(request, rule, Set.of(path), exclusions, requiredReviewers, shortages)));
			}
		}

		// Validate the complete selection and calculate all changes before mutating a draft or posting events.
		var diffs = new ReviewDiffs(latestUpdate);
		var statuses = new LinkedHashMap<PullRequestReview, Status>();
		for (var review : request.getReviews()) {
			var reviewer = review.getUser();
			boolean required = requiredReviewers.remove(reviewer);
			var status = review.getStatus();
			if (exclusions.contains(reviewer)) {
				if (status != Status.EXCLUDED)
					statuses.put(review, Status.EXCLUDED);
				continue;
			}
			if (status == Status.EXCLUDED) {
				if (required)
					statuses.put(review, Status.PENDING);
				continue;
			}

			var reviewedUpdate = review.getUpdate();
			if (status == Status.PENDING || latestUpdate.equals(reviewedUpdate))
				continue;
			boolean needsReview;
			if (branchReviewers.contains(reviewer))
				needsReview = reviewedUpdate == null || !diffs.since(reviewedUpdate.getHeadCommitId()).isEmpty();
			else if (reviewer.getType() == User.Type.AI)
				needsReview = true;
			else if (reviewedUpdate == null)
				needsReview = required;
			else
				needsReview = fileReviewers.requiresReviewSince(reviewer, reviewedUpdate, diffs);
			if (needsReview)
				statuses.put(review, Status.PENDING);
		}

		statuses.forEach(PullRequestReview::setStatus);
		var changes = new ArrayList<>(statuses.keySet());
		for (var reviewer : requiredReviewers)
			changes.add(newReview(request, reviewer));
		for (var review : changes)
			saveReviewerChange(actor, review);
		return shortages.isEmpty() ? null : String.join(" ", shortages);
	}

	private static class FileReviewers {
		private final BranchProtection protection;
		private final Set<User> active = new HashSet<>();
		private final Map<String, Set<User>> byPath = new HashMap<>();
		private final Map<String, Set<User>> byRule = new HashMap<>();

		FileReviewers(PullRequest request, BranchProtection protection, Set<User> exclusions) {
			this.protection = protection;
			for (var review : request.getReviews()) {
				if (review.getStatus() != Status.EXCLUDED && !exclusions.contains(review.getUser()))
					active.add(review.getUser());
			}
			active.remove(request.getSubmitter());
		}

		Set<User> forPath(String path, Function<ReviewRequirement, Set<User>> select) {
			return byPath.computeIfAbsent(path, key -> {
				var fileProtection = protection.getFileProtection(key);
				if (fileProtection.getReviewRequirement() == null)
					return Set.of();
				return byRule.computeIfAbsent(fileProtection.getReviewRequirement(), ignored ->
						select.apply(fileProtection.getParsedReviewRequirement()));
			});
		}

		boolean requiresReviewSince(User reviewer, PullRequestUpdate reviewedUpdate, ReviewDiffs diffs) {
			if (!protection.getFileProtections().isEmpty()) {
				for (var path : diffs.since(reviewedUpdate.getHeadCommitId())) {
					if (forPath(path, this::selectExisting).contains(reviewer))
						return true;
				}
			}
			return false;
		}

		// Rules found only in the reviewer's diff can request another review from an active reviewer.
		private Set<User> selectExisting(ReviewRequirement rule) {
			var selected = new HashSet<>(rule.getUsers());
			selected.retainAll(active);
			for (var entry : rule.getGroups().entrySet()) {
				entry.getKey().getMembers().stream().filter(active::contains)
						.sorted(Comparator.comparing(User::getId)).limit(entry.getValue()).forEach(selected::add);
			}
			return selected;
		}
	}

	/** One target snapshot and one diff per reviewed head for the duration of reconciliation. */
	private class ReviewDiffs {
		private final PullRequestUpdate latestUpdate;
		private final Map<ObjectId, Collection<String>> filesSince = new HashMap<>();
		private ObjectId targetHead;

		ReviewDiffs(PullRequestUpdate latestUpdate) {
			this.latestUpdate = latestUpdate;
		}

		private ObjectId getTargetHead() {
			if (targetHead == null)
				targetHead = latestUpdate.getRequest().getTarget().getObjectId();
			return targetHead;
		}

		Collection<String> since(ObjectId fromCommitId) {
			var headCommitId = latestUpdate.getHeadCommitId();
			return filesSince.computeIfAbsent(fromCommitId, it -> gitService.getChangedFilesAfterMerge(
					latestUpdate.getRequest().getTargetProject(), it, headCommitId, getTargetHead()));
		}
	}

	private Set<User> selectReviewers(PullRequest request, ReviewRequirement rule, Collection<String> files,
			Set<User> exclusions, Set<User> required, Set<String> shortages) {
		var selected = new HashSet<User>();
		for (var user : rule.getUsers()) {
			if (!user.equals(request.getSubmitter())) {
				if (exclusions.contains(user))
					throw new NotAcceptableException("Mandatory reviewer cannot be removed: " + user.getDisplayName());
				selected.add(user);
			}
		}
		for (var entry : rule.getGroups().entrySet()) {
			var members = new ArrayList<>(entry.getKey().getMembers());
			members.remove(request.getSubmitter());
			boolean excludingMember = members.removeAll(exclusions);
			members.sort(Comparator.comparing(User::getId));
			int remaining = entry.getValue();
			if (remaining > members.size()) {
				var shortage = "Group '%s' cannot meet its reviewer requirement (%d required, %d eligible)."
						.formatted(entry.getKey().getName(), remaining, members.size());
				if (excludingMember)
					throw new NotAcceptableException(shortage);
				shortages.add(shortage);
				selected.addAll(members);
				continue;
			}
			var candidates = new HashMap<Long, Collection<EmailAddressFacade>>();
			var previouslyExcluded = new ArrayList<User>();
			for (var user : members) {
				var review = request.getReview(user);
				if (required.contains(user) || selected.contains(user) || review != null && review.getStatus() != Status.EXCLUDED) {
					selected.add(user);
					if (--remaining == 0)
						break;
				} else if (review == null) {
					candidates.put(user.getId(), user.getEmailAddresses().stream().map(it -> it.getFacade()).toList());
				} else {
					previouslyExcluded.add(user);
				}
			}
			if (remaining > 0 && !candidates.isEmpty()) {
				for (var id : commitInfoService.sortUsersByContribution(candidates, request.getTargetProject().getId(), files)) {
					selected.add(userService.load(id));
					if (--remaining == 0)
						break;
				}
			}
			if (remaining > 0) {
				for (var user : previouslyExcluded) {
					selected.add(user);
					if (--remaining == 0)
						break;
				}
			}
			Preconditions.checkState(remaining == 0);
		}
		return selected;
	}

	@Transactional
	@Override
	public void approve(User reviewer, PullRequestUpdate update, @Nullable String note) {
		recordDecision(reviewer, update, Status.APPROVED, () -> note);
	}

	@Transactional
	@Override
	public void requestChanges(User reviewer, PullRequestUpdate update, @Nullable String note) {
		recordDecision(reviewer, update, Status.REQUESTED_FOR_CHANGES, () -> note);
	}

	@Transactional
	@Override
	public void submitDecision(Subject subject, PullRequestUpdate update, boolean approved, Supplier<@Nullable String> note) {
		var reviewer = SecurityUtils.getUser(subject);
		if (reviewer == null)
			throw new UnauthenticatedException();
		var project = update.getRequest().getProject();
		if (!SecurityUtils.canReadCode(subject, project))
			throw new UnauthorizedException("No permission to access pull request update: " + update.getId());
		SecurityUtils.checkWorkOnProject(subject, project);
		recordDecision(reviewer, update, approved ? Status.APPROVED : Status.REQUESTED_FOR_CHANGES, note);
	}

	private void recordDecision(User reviewer, PullRequestUpdate update, Status decision, Supplier<@Nullable String> note) {
		Preconditions.checkArgument(!update.isNew(), "Only a saved pull request update can be reviewed");
		var request = update.getRequest();
		lockOpenRequest(request);
		if (!request.getLatestUpdate().equals(update))
			throw new NotAcceptableException("Pull request has a newer update. Refresh before submitting your review");
		var review = getActiveReview(request, reviewer);
		var content = note.get();
		review.setUpdate(update);
		review.setStatus(decision);
		dao.persist(review);

		var change = new PullRequestChange();
		change.setRequest(request);
		change.setUser(reviewer);
		change.setData(decision == Status.APPROVED ? new PullRequestApproveData() : new PullRequestRequestedForChangesData());
		changeService.create(change, content);
	}

	@Sessional
	@Override
	public void populateReviews(Collection<PullRequest> requests) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestReview> query = builder.createQuery(PullRequestReview.class);
		
		Root<PullRequestReview> root = query.from(PullRequestReview.class);
		query.select(root);
		Join<PullRequestReview, PullRequest> join = root.join(PullRequestReview.PROP_REQUEST);
		query.where(join.in(requests));
		
		for (PullRequest request: requests) 
			request.setReviews(new ArrayList<>());
		
		for (PullRequestReview review: getSession().createQuery(query).getResultList())
			review.getRequest().getReviews().add(review);
	}

}
