package com.gitplex.server.web.component.comment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.component.markdown.MarkdownEditor;
import com.gitplex.server.web.component.markdown.PullRequestReferenceSupport;
import com.gitplex.server.web.component.markdown.UserMentionSupport;

@SuppressWarnings("serial")
public abstract class CommentInput extends MarkdownEditor {

	public CommentInput(String id, IModel<String> model, boolean compactMode) {
		super(id, model, compactMode);
	}

	@Override
	protected UserMentionSupport getUserMentionSupport() {
		return new UserMentionSupport() {

			@Override
			public List<User> findUsers(String query, int count) {
				List<User> users = new ArrayList<>();
				for (User user: SecurityUtils.findUsersCan(getProject(), ProjectPrivilege.READ)) {
					if (users.size() < count) {
						if (StringUtils.deleteWhitespace(user.getName()).toLowerCase().contains(query) 
								|| user.getFullName() != null && StringUtils.deleteWhitespace(user.getFullName()).toLowerCase().contains(query)) {
							users.add(user);
						}
					} else {
						break;
					}
				}
				users.sort(Comparator.comparing(User::getName));
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
