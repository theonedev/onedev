package com.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.core.entity.CodeComment;
import com.gitplex.core.entity.CodeCommentRelation;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestUpdate;
import com.gitplex.core.entity.support.CompareContext;
import com.gitplex.core.event.codecomment.CodeCommentCreated;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentCreated;
import com.gitplex.core.manager.CodeCommentInfoManager;
import com.gitplex.core.manager.CodeCommentManager;
import com.gitplex.core.manager.CodeCommentRelationManager;
import com.gitplex.core.manager.PullRequestInfoManager;
import com.gitplex.core.manager.PullRequestManager;
import com.gitplex.core.manager.VisitInfoManager;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.loader.ListenerRegistry;

@Singleton
public class DefaultCodeCommentRelationManager extends AbstractEntityManager<CodeCommentRelation> 
		implements CodeCommentRelationManager {

	private final PullRequestInfoManager pullRequestInfoManager;
	
	private final CodeCommentInfoManager codeCommentInfoManager;
	
	private final CodeCommentManager codeCommentManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final VisitInfoManager visitInfoManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, PullRequestInfoManager pullRequestInfoManager,
			CodeCommentInfoManager codeCommentInfoManager, CodeCommentManager codeCommentManager, 
			PullRequestManager pullRequestManager, VisitInfoManager visitInfoManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);
		this.pullRequestInfoManager = pullRequestInfoManager;
		this.codeCommentInfoManager = codeCommentInfoManager;
		this.codeCommentManager = codeCommentManager;
		this.pullRequestManager = pullRequestManager;
		this.visitInfoManager = visitInfoManager;
		this.listenerRegistry = listenerRegistry;
	}

	/*
	 * Find out code comments belong to a pull request. A code comment is considered to be 
	 * belong to pull request if its commit and compare commit is covered by commits of 
	 * the pull request. The simplest approach to determine this would be to query the 
	 * database for each commit of the pull request. However that might issue a lot of 
	 * sql queries if there are many commits. To address this issue, at the time of 
	 * querying related comments, we query the comment information store first (which is
	 * fast) for each commit to see if there are new comments relevant to the pull request. 
	 */
	@Transactional
	@Override
	public List<CodeComment> findCodeComments(PullRequest request) {
		if (request.getCodeCommentRelations().size() < PullRequest.MAX_CODE_COMMENTS) {
			Collection<String> relatedComments = new HashSet<>();
			for (CodeCommentRelation relation: request.getCodeCommentRelations()) {
				relatedComments.add(relation.getComment().getUUID());
			}
			
			Collection<String> involvedCommits = new HashSet<>();
			for (PullRequestUpdate update: request.getUpdates()) {
				for (RevCommit commit: update.getCommits())
					involvedCommits.add(commit.name());
			}
			involvedCommits.add(request.getBaseCommitHash());

			Map<String, CodeComment.ComparingInfo> comparingInfos = new HashMap<>();
			for (String commit: involvedCommits) {
				Map<String, CompareContext> commentsOfCommit = codeCommentInfoManager.getComments(
						request.getTargetDepot(), ObjectId.fromString(commit));
				for (Map.Entry<String, CompareContext> entry: commentsOfCommit.entrySet()) {
					CodeComment.ComparingInfo comparingInfo = new CodeComment.ComparingInfo(commit, entry.getValue());
					comparingInfos.put(entry.getKey(), comparingInfo);
				}
			}

			comparingInfos.keySet().removeAll(relatedComments);
			
			for (Map.Entry<String, CodeComment.ComparingInfo> entry: comparingInfos.entrySet()) {
				String uuid = entry.getKey();
				CodeComment.ComparingInfo comparingInfo = entry.getValue();
				
				if (request.getRequestComparingInfo(comparingInfo) != null) {
					CodeComment comment = codeCommentManager.find(uuid);
					if (comment == null) {
						codeCommentInfoManager.removeComment(request.getTargetDepot(), ObjectId.fromString(comparingInfo.getCommit()), uuid);
					} else if (request.getCodeCommentRelations().size() < PullRequest.MAX_CODE_COMMENTS) {
						CodeCommentRelation relation = new CodeCommentRelation();
						relation.setComment(comment);
						relation.setRequest(request);
						save(relation);
						request.getCodeCommentRelations().add(relation);
					}
				}
			}
		}
		List<CodeComment> comments = request.getCodeCommentRelations()
				.stream()
				.map(CodeCommentRelation::getComment)
				.sorted(CodeComment::compareTo)
				.collect(Collectors.toList());
		Collections.reverse(comments);
		return comments;
	}
	
	@Transactional
	@Override
	public void save(CodeCommentRelation entity) {
		PullRequest request = entity.getRequest();
		CodeComment comment = entity.getComment();
		if (entity.isNew() && (request.getLastEvent() == null 
				|| comment.getDate().getTime()>request.getLastEvent().getDate().getTime())) {
			PullRequestCodeCommentCreated event = new PullRequestCodeCommentCreated(request, comment); 
			listenerRegistry.post(event);
			request.setLastEvent(event);
			pullRequestManager.save(request);
		}
		super.save(entity);
	}

	@Transactional
	@Listen
	public void on(CodeCommentCreated event) {
		CodeComment comment = event.getComment();
		ObjectId commitId = ObjectId.fromString(comment.getCommentPos().getCommit());
		boolean found = false;
		for (String uuid: pullRequestInfoManager.getRequests(comment.getDepot(), commitId)) {
			PullRequest request = pullRequestManager.find(uuid);
			if (request != null && request.getRequestComparingInfo(comment.getComparingInfo()) != null) {
				PullRequestCodeCommentCreated pullRequestCodeCommented = new PullRequestCodeCommentCreated(request, comment); 
				listenerRegistry.post(pullRequestCodeCommented);
				
				request.setLastEvent(pullRequestCodeCommented);
				
				pullRequestManager.save(request);
				
				visitInfoManager.visit(comment.getUser(), request);
				
				CodeCommentRelation relation = new CodeCommentRelation();
				relation.setComment(comment);
				relation.setRequest(request);
				save(relation);
				found = true;
			}
		}
		
		if (!found) {
			codeCommentManager.sendNotifications(event);
		}
		
	}

}
