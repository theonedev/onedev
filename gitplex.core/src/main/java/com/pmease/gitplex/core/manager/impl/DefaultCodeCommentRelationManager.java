package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.event.codecomment.CodeCommentCreated;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommented;
import com.pmease.gitplex.core.manager.CodeCommentInfoManager;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentRelationManager;
import com.pmease.gitplex.core.manager.PullRequestInfoManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;

@Singleton
public class DefaultCodeCommentRelationManager extends AbstractEntityManager<CodeCommentRelation> 
		implements CodeCommentRelationManager {

	private final PullRequestInfoManager pullRequestInfoManager;
	
	private final CodeCommentInfoManager codeCommentInfoManager;
	
	private final CodeCommentManager codeCommentManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final VisitInfoManager visitInfoManager;
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, PullRequestInfoManager pullRequestInfoManager,
			CodeCommentInfoManager codeCommentInfoManager, CodeCommentManager codeCommentManager, 
			PullRequestManager pullRequestManager, VisitInfoManager visitInfoManager) {
		super(dao);
		this.pullRequestInfoManager = pullRequestInfoManager;
		this.codeCommentInfoManager = codeCommentInfoManager;
		this.codeCommentManager = codeCommentManager;
		this.pullRequestManager = pullRequestManager;
		this.visitInfoManager = visitInfoManager;
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
	public List<CodeComment> findAllCodeComments(PullRequest request) {
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
		
		List<CodeComment> comments = new ArrayList<>();
		for (CodeCommentRelation relation: request.getCodeCommentRelations()) {
			comments.add(relation.getComment());
		}
		comments.sort(CodeComment::compareTo);
		Collections.reverse(comments);		
		
		return comments;
	}

	@Transactional
	@Override
	public void save(CodeCommentRelation entity) {
		PullRequest request = entity.getRequest();
		CodeComment comment = entity.getComment();
		if (entity.isNew() && (request.getLastEvent() == null || comment.getDate().after(request.getLastEvent().getDate()))) {
			LastEvent lastEvent = new LastEvent();
			lastEvent.setDate(comment.getDate());
			lastEvent.setDescription(EditableUtils.getName(PullRequestCodeCommented.class));
			lastEvent.setUser(comment.getUser());
			request.setLastCodeCommentEventDate(comment.getDate());
			pullRequestManager.save(request);
		}
		super.save(entity);
	}

	@Transactional
	@Listen
	public void on(CodeCommentCreated event) {
		CodeComment comment = event.getComment();
		ObjectId commitId = ObjectId.fromString(comment.getCommentPos().getCommit());
		for (String uuid: pullRequestInfoManager.getRequests(comment.getDepot(), commitId)) {
			PullRequest request = pullRequestManager.find(uuid);
			if (request != null && request.getRequestComparingInfo(comment.getComparingInfo()) != null) {
				LastEvent lastEvent = new LastEvent();
				lastEvent.setDate(comment.getDate());
				lastEvent.setDescription(EditableUtils.getName(PullRequestCodeCommented.class));
				lastEvent.setUser(comment.getUser());
				request.setLastEvent(lastEvent);
				request.setLastCodeCommentEventDate(comment.getDate());
				pullRequestManager.save(request);
				 
				visitInfoManager.visit(comment.getUser(), request);
				
				CodeCommentRelation relation = new CodeCommentRelation();
				relation.setComment(comment);
				relation.setRequest(request);
				save(relation);
			}
		}
	}

}
