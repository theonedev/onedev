package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.codecomment.*;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentCreated;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.git.service.GitService;
import io.onedev.server.xodus.CommitInfoManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.Mark;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT;

@Singleton
public class DefaultCodeCommentManager extends BaseEntityManager<CodeComment> implements CodeCommentManager {

	private final int MAX_HISTORY_COMMITS_TO_CHECK = 50000;
	
	private final int MAX_HISTORY_FILES_TO_CHECK = 500;
	
	private final ListenerRegistry listenerRegistry;
	
	private final CommitInfoManager commitInfoManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, 
									 CommitInfoManager commitInfoManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.commitInfoManager = commitInfoManager;
	}

	@Transactional
	@Override
	public void create(CodeComment comment) {
		Preconditions.checkState(comment.isNew());
		
		LastActivity lastActivity = new LastActivity();
		lastActivity.setUser(comment.getUser());
		lastActivity.setDescription("added");
		lastActivity.setDate(comment.getCreateDate());
		comment.setLastActivity(lastActivity);
		dao.persist(comment);
		
		listenerRegistry.post(new CodeCommentCreated(comment));
		
		PullRequest request = comment.getCompareContext().getPullRequest();
		if (request != null && comment.getCreateDate().after(request.getLastActivity().getDate())) 
			listenerRegistry.post(new PullRequestCodeCommentCreated(request, comment));
	}

	@Transactional
	@Override
	public void update(CodeComment comment) {
 		Preconditions.checkState(!comment.isNew());
		dao.persist(comment);
		listenerRegistry.post(new CodeCommentEdited(SecurityUtils.getUser(), comment));
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (!(event instanceof CodeCommentCreated))
			event.getComment().setLastActivity(event.getLastUpdate());
	}
	
	@Sessional
	@Override
	public Collection<CodeComment> query(Project project, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq(CodeComment.PROP_PROJECT, project));
		criteria.add(Restrictions.eq(CodeComment.PROP_MARK + "." + Mark.PROP_COMMIT_HASH, commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq(CodeComment.PROP_MARK + "." + Mark.PROP_PATH, path));
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> query(Project project, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq(CodeComment.PROP_PROJECT, project));
		List<Criterion> criterions = new ArrayList<>();
		for (ObjectId commitId: commitIds) {
			criterions.add(Restrictions.eq(CodeComment.PROP_MARK + "." + Mark.PROP_COMMIT_HASH, commitId.name()));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[criterions.size()])));
		return query(criteria);
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	@Override
	public Map<CodeComment, PlanarRange> queryInHistory(Project project, ObjectId commitId, String path) {
		Map<CodeComment, PlanarRange> comments = new HashMap<>();
		
		Map<String, Map<String, List<CodeComment>>> possibleComments = new HashMap<>();
		Collection<String> possiblePaths = Sets.newHashSet(path);
		possiblePaths.addAll(commitInfoManager.getHistoryPaths(project.getId(), path));
		
		EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
		criteria.add(Restrictions.eq(CodeComment.PROP_PROJECT, project));
		criteria.add(Restrictions.in(CodeComment.PROP_MARK + "." + Mark.PROP_PATH, possiblePaths));
		
		for (CodeComment comment: query(criteria)) {
			String possiblePath = comment.getMark().getPath();
			if (comment.getMark().getCommitHash().equals(commitId.name()) && possiblePath.equals(path)) {
				comments.put(comment, comment.getMark().getRange());
			} else {
				Map<String, List<CodeComment>> commentsOnCommit = 
						possibleComments.get(comment.getMark().getCommitHash());
				if (commentsOnCommit == null) {
					commentsOnCommit = new HashMap<>();
					possibleComments.put(comment.getMark().getCommitHash(), commentsOnCommit);
				}
				List<CodeComment> commentsOnPath = commentsOnCommit.get(possiblePath);
				if (commentsOnPath == null) {
					commentsOnPath = new ArrayList<>();
					commentsOnCommit.put(possiblePath, commentsOnPath);
				}
				commentsOnPath.add(comment);
			}
		}

		var possibleCommitIds = possibleComments.keySet().stream()
				.map(it->ObjectId.fromString(it)).collect(Collectors.toSet());
		List<RevCommit> historyCommits = getGitService().sortValidCommits(
				project, possibleCommitIds);

		Date oldestDate = null;
		for (RevCommit commit: historyCommits) {
			PersonIdent committer = commit.getCommitterIdent();
			if (committer != null && committer.getWhen() != null 
					&& (oldestDate == null || committer.getWhen().before(oldestDate))) {
				oldestDate = committer.getWhen();
			}
		}
		
		if (oldestDate != null) {
			RevListOptions options = new RevListOptions();
			options.revisions(Lists.newArrayList(commitId.name()))
					.after(ISO_8601_EXTENDED_DATE_FORMAT.format(DateUtils.addDays(oldestDate, -1)))
					.ignoreCase(false).skip(0).count(MAX_HISTORY_COMMITS_TO_CHECK);
			
			var revisions = new HashSet<>(getGitService().revList(project, options));
			
			List<String> newLines = Preconditions.checkNotNull(project.readLines(
					new BlobIdent(commitId.name(), path, FileMode.REGULAR_FILE.getBits()), 
					WhitespaceOption.DEFAULT, true));

			Collections.sort(historyCommits, new Comparator<RevCommit>() {

				@Override
				public int compare(RevCommit o1, RevCommit o2) {
					return o2.getCommitTime() - o1.getCommitTime();
				}
				
			});
			int checkedHistoryFiles = 0;
			for (RevCommit historyCommit: historyCommits) {
				if (revisions.contains(historyCommit.name())) {
					Map<String, List<CodeComment>> commentsOnCommit = 
							Preconditions.checkNotNull(possibleComments.get(historyCommit.name()));
					for (Map.Entry<String, List<CodeComment>> pathEntry: commentsOnCommit.entrySet()) {
						List<String> oldLines = project.readLines( 
								new BlobIdent(historyCommit.name(), pathEntry.getKey(), FileMode.REGULAR_FILE.getBits()), 
								WhitespaceOption.DEFAULT, false);
						if (oldLines != null) {
							Map<Integer, Integer> lineMapping;
							if (oldLines.size() + newLines.size() <= DiffUtils.MAX_DIFF_SIZE)
								lineMapping = DiffUtils.mapLines(oldLines, newLines);
							else
								lineMapping = new HashMap<>();
							for (CodeComment comment: pathEntry.getValue()) {
								PlanarRange newRange = DiffUtils.mapRange(lineMapping, comment.getMark().getRange());
								if (newRange != null) 
									comments.put(comment, newRange);
							}
							if (++checkedHistoryFiles == MAX_HISTORY_FILES_TO_CHECK) {
								return comments;
							}
						}
					}
				}
			}
		} 
		
		return comments;
	}

	private Predicate[] getPredicates(Project project, @Nullable Criteria<CodeComment> criteria, @Nullable PullRequest request, 
			CriteriaQuery<?> query, From<CodeComment, CodeComment> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (request != null) 
			predicates.add(builder.equal(CodeCommentQuery.getPath(root, CodeComment.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST), request));
		else 
			predicates.add(builder.equal(root.get(CodeComment.PROP_PROJECT), project));
		if (criteria != null) 
			predicates.add(criteria.getPredicate(query, root, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<CodeComment> buildCriteriaQuery(Project project, Session session, 
			PullRequest request, EntityQuery<CodeComment> commentQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<CodeComment> query = builder.createQuery(CodeComment.class);
		Root<CodeComment> root = query.from(CodeComment.class);
		
		query.where(getPredicates(project, commentQuery.getCriteria(), request, query, root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: commentQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(CodeCommentQuery.getPath(root, CodeComment.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(CodeCommentQuery.getPath(root, CodeComment.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty()) 
			orders.add(builder.desc(CodeCommentQuery.getPath(root, CodeComment.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE)));
		query.orderBy(orders);
		
		return query;
	}

	@Sessional
	@Override
	public List<CodeComment> query(Project project, PullRequest request, 
			EntityQuery<CodeComment> commentQuery, int firstResult, int maxResults) {
		CriteriaQuery<CodeComment> criteriaQuery = buildCriteriaQuery(project, getSession(), request, commentQuery);
		Query<CodeComment> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}
	
	@Sessional
	@Override
	public int count(Project project, PullRequest request,  Criteria<CodeComment> commentCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<CodeComment> root = criteriaQuery.from(CodeComment.class);

		criteriaQuery.where(getPredicates(project, commentCriteria, request, criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Transactional
	@Override
	public void delete(Collection<CodeComment> comments, Project project) {
		for (CodeComment comment: comments)
			dao.remove(comment);
		listenerRegistry.post(new CodeCommentsDeleted(project, comments));
	}

	@Transactional
	@Override
	public void delete(CodeComment comment) {
		dao.remove(comment);
		listenerRegistry.post(new CodeCommentDeleted(comment));
	}
	
	@Override
	public CodeComment findByUUID(String uuid) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq(CodeComment.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
	}

}
