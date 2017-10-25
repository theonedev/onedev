package com.gitplex.server.web.component.comment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.component.markdown.MarkdownEditor;
import com.gitplex.server.web.component.markdown.PullRequestReferenceSupport;
import com.gitplex.server.web.component.markdown.UserMentionSupport;
import com.gitplex.utils.StringUtils;
import com.gitplex.utils.matchscore.MatchScoreProvider;
import com.gitplex.utils.matchscore.MatchScoreUtils;

@SuppressWarnings("serial")
public abstract class CommentInput extends MarkdownEditor {

	public CommentInput(String id, IModel<String> model, boolean compactMode) {
		super(id, model, compactMode, null);
	}

	@Override
	protected UserMentionSupport getUserMentionSupport() {
		return new UserMentionSupport() {

			@Override
			public List<UserFacade> findUsers(String query, int count) {
				List<UserFacade> users = new ArrayList<>(
						SecurityUtils.getAuthorizedUsers(getProject().getFacade(), ProjectPrivilege.READ));
				users.sort(Comparator.comparing(UserFacade::getDisplayName));
				
				users = MatchScoreUtils.filterAndSort(users, new MatchScoreProvider<UserFacade>() {

					@Override
					public double getMatchScore(UserFacade object) {
						return object.getMatchScore(query);
					}
					
				});
				
				if (users.size() > count)
					return users.subList(0, count);
				else
					return users;
			}
			
		};
	}

	@Override
	protected PullRequestReferenceSupport getPullRequestReferenceSupport() {
		return new PullRequestReferenceSupport() {

			@Override
			public List<PullRequest> findRequests(String query, int count) {
				EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
				criteria.add(Restrictions.eq("targetProject", getProject()));
				if (StringUtils.isNotBlank(query)) {
					query = StringUtils.deleteWhitespace(query);
					criteria.add(Restrictions.or(
							Restrictions.ilike("noSpaceTitle", query, MatchMode.ANYWHERE), 
							Restrictions.ilike("numberStr", query, MatchMode.START)));
				}
				criteria.addOrder(Order.desc("number"));
				return GitPlex.getInstance(Dao.class).findRange(criteria, 0, count);
			}
			
		};
	}

	protected abstract Project getProject();
	
}
