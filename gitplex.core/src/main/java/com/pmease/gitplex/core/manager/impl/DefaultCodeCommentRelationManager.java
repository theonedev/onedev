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

import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.manager.CodeCommentInfoManager;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.CodeCommentRelationManager;

@Singleton
public class DefaultCodeCommentRelationManager extends AbstractEntityManager<CodeCommentRelation> 
		implements CodeCommentRelationManager {

	private final CodeCommentInfoManager codeCommentInfoManager;
	
	private final CodeCommentManager codeCommentManager;
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, CodeCommentInfoManager codeCommentInfoManager, 
			CodeCommentManager codeCommentManager) {
		super(dao);
		this.codeCommentInfoManager = codeCommentInfoManager;
		this.codeCommentManager = codeCommentManager;
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
	public List<CodeComment> queryCodeComments(PullRequest request) {
		if (request.getCodeCommentRelations().size() < PullRequest.MAX_CODE_COMMENTS) {
			Collection<String> relatedComments = new HashSet<>();
			for (CodeCommentRelation relation: request.getCodeCommentRelations()) {
				relatedComments.add(relation.getComment().getUUID());
			}
			
			Collection<String> involvedCommits = new HashSet<>();
			for (PullRequestUpdate update: request.getUpdates()) {
				for (BriefCommit commit: update.getCommits())
					involvedCommits.add(commit.getHash());
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
						dao.persist(relation);
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

}
