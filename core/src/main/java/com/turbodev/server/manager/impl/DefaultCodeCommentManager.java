package com.turbodev.server.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.turbodev.launcher.loader.ListenerRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.turbodev.server.event.codecomment.CodeCommentCreated;
import com.turbodev.server.git.GitUtils;
import com.turbodev.server.git.command.RevListCommand;
import com.turbodev.server.manager.CodeCommentManager;
import com.turbodev.server.manager.CommitInfoManager;
import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.support.TextRange;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.util.diff.DiffUtils;
import com.turbodev.server.util.diff.WhitespaceOption;

@Singleton
public class DefaultCodeCommentManager extends AbstractEntityManager<CodeComment> implements CodeCommentManager {

	private final int MAX_HISTORY_COMMITS_TO_CHECK = 50000;
	
	private final int MAX_HISTORY_FILES_TO_CHECK = 500;
	
	private final ListenerRegistry listenerRegistry;
	
	private final CommitInfoManager commitInfoManager;
	
	@Inject
	public DefaultCodeCommentManager(Dao dao, ListenerRegistry listenerRegistry, CommitInfoManager commitInfoManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.commitInfoManager = commitInfoManager;
	}

	@Transactional
	@Override
	public void save(CodeComment comment, PullRequest request) {
		if (comment.isNew()) {
			dao.persist(comment);
			CodeCommentCreated event = new CodeCommentCreated(comment, request);
			listenerRegistry.post(event);
		} else {
			dao.persist(comment);
		}
	}

	@Sessional
	@Override
	public CodeComment find(String uuid) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<CodeComment> findAllAfter(Project project, String commentUUID, int count) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.addOrder(Order.asc("id"));
		if (commentUUID != null) {
			CodeComment comment = find(commentUUID);
			if (comment != null) {
				criteria.add(Restrictions.gt("id", comment.getId()));
			}
		}
		return findRange(criteria, 0, count);
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Project project, ObjectId commitId, String path) {
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("markPos.commit", commitId.name()));
		if (path != null)
			criteria.add(Restrictions.eq("markPos.path", path));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<CodeComment> findAll(Project project, ObjectId... commitIds) {
		Preconditions.checkArgument(commitIds.length > 0);
		
		EntityCriteria<CodeComment> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		List<Criterion> criterions = new ArrayList<>();
		for (ObjectId commitId: commitIds) {
			criterions.add(Restrictions.eq("markPos.commit", commitId.name()));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[criterions.size()])));
		return findAll(criteria);
	}
	
	@Override
	public Map<CodeComment, TextRange> findHistory(Project project, ObjectId commitId, String path) {
		Map<CodeComment, TextRange> comments = new HashMap<>();
		
		Map<String, Map<String, List<CodeComment>>> possibleComments = new HashMap<>();
		for (String possibleHistoryPath: commitInfoManager.getPossibleHistoryPaths(project, path)) {
			EntityCriteria<CodeComment> criteria = EntityCriteria.of(CodeComment.class);
			criteria.add(Restrictions.eq("markPos.path", possibleHistoryPath));
			for (CodeComment comment: findAll(criteria)) {
				if (comment.getMarkPos().getCommit().equals(commitId.name()) && possibleHistoryPath.equals(path)) {
					comments.put(comment, comment.getMarkPos().getRange());
				} else {
					Map<String, List<CodeComment>> commentsOnCommit = 
							possibleComments.get(comment.getMarkPos().getCommit());
					if (commentsOnCommit == null) {
						commentsOnCommit = new HashMap<>();
						possibleComments.put(comment.getMarkPos().getCommit(), commentsOnCommit);
					}
					List<CodeComment> commentsOnPath = commentsOnCommit.get(possibleHistoryPath);
					if (commentsOnPath == null) {
						commentsOnPath = new ArrayList<>();
						commentsOnCommit.put(possibleHistoryPath, commentsOnPath);
					}
					commentsOnPath.add(comment);
				}
			}
		}

		try (RevWalk revWalk = new RevWalk(project.getRepository())) {
			Date oldestDate = null;
			List<RevCommit> historyCommits = new ArrayList<>();
			for (Map.Entry<String, Map<String, List<CodeComment>>> entry: possibleComments.entrySet()) {
				try {
					RevCommit commit = revWalk.parseCommit(ObjectId.fromString(entry.getKey()));
					historyCommits.add(commit);
					PersonIdent committer = commit.getCommitterIdent();
					if (committer != null && committer.getWhen() != null 
							&& (oldestDate == null || committer.getWhen().before(oldestDate))) {
						oldestDate = committer.getWhen();
					}
				} catch (MissingObjectException e) {
				}
			}
			
			if (oldestDate != null) {
				RevListCommand command = new RevListCommand(project.getRepository().getDirectory());
				command.after(DateUtils.addDays(oldestDate, -1));
				command.revisions(Lists.newArrayList(commitId.name()));
				command.count(MAX_HISTORY_COMMITS_TO_CHECK);
				Set<String> revisions = new HashSet<>(command.call());
				
				RevCommit commit = revWalk.parseCommit(commitId);
				List<String> newLines = GitUtils.readLines(project.getRepository(), commit, path, 
						WhitespaceOption.DEFAULT);

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
							List<String> oldLines = GitUtils.readLines(project.getRepository(), historyCommit, 
									pathEntry.getKey(), WhitespaceOption.DEFAULT);
							Map<Integer, Integer> lineMapping = DiffUtils.mapLines(oldLines, newLines);
							for (CodeComment comment: pathEntry.getValue()) {
								TextRange newRange = DiffUtils.mapRange(lineMapping, comment.getMarkPos().getRange());
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
			
			return comments;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 

	}

}
