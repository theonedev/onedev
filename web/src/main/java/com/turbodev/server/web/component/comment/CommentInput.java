package com.turbodev.server.web.component.comment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.turbodev.utils.StringUtils;
import com.turbodev.utils.matchscore.MatchScoreProvider;
import com.turbodev.utils.matchscore.MatchScoreUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.facade.UserFacade;
import com.turbodev.server.web.component.markdown.MarkdownEditor;
import com.turbodev.server.web.component.markdown.PullRequestReferenceSupport;
import com.turbodev.server.web.component.markdown.UserMentionSupport;

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
				return TurboDev.getInstance(Dao.class).findRange(criteria, 0, count);
			}
			
		};
	}

	protected abstract Project getProject();
	
}
