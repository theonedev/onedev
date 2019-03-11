package io.onedev.server.web.component.project.comment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.component.markdown.AtWhoReferenceSupport;
import io.onedev.server.web.component.markdown.UserMentionSupport;

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
						SecurityUtils.getAuthorizedUsers(getProject().getFacade(), ProjectPrivilege.ISSUE_READ));
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
	protected AtWhoReferenceSupport getReferenceSupport() {
		return new AtWhoReferenceSupport() {

			@Override
			public List<PullRequest> findPullRequests(Project project, String query, int count) {
				EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
				if (project != null)
					criteria.add(Restrictions.eq("targetProject", project));
				else
					criteria.add(Restrictions.eq("targetProject", getProject()));
				if (StringUtils.isNotBlank(query)) {
					query = StringUtils.deleteWhitespace(query);
					criteria.add(Restrictions.or(
							Restrictions.ilike("noSpaceTitle", query, MatchMode.ANYWHERE), 
							Restrictions.ilike("numberStr", query, MatchMode.START)));
				}
				criteria.addOrder(Order.desc("number"));
				return OneDev.getInstance(Dao.class).query(criteria, 0, count);
			}

			@Override
			public List<Issue> findIssues(Project project, String query, int count) {
				EntityCriteria<Issue> criteria = EntityCriteria.of(Issue.class);
				if (project != null)
					criteria.add(Restrictions.eq("project", project));
				else
					criteria.add(Restrictions.eq("project", getProject()));
				if (StringUtils.isNotBlank(query)) {
					query = StringUtils.deleteWhitespace(query);
					criteria.add(Restrictions.or(
							Restrictions.ilike("noSpaceTitle", query, MatchMode.ANYWHERE), 
							Restrictions.ilike("numberStr", query, MatchMode.START)));
				}
				criteria.addOrder(Order.desc("number"));
				return OneDev.getInstance(Dao.class).query(criteria, 0, count);
			}
			
		};
	}

	protected abstract Project getProject();
	
}
