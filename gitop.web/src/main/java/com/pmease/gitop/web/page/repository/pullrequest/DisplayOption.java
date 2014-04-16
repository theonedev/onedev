package com.pmease.gitop.web.page.repository.pullrequest;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
class DisplayOption implements Serializable {
	
	private boolean open;

	public static final Long SHOW_REQUESTS_OF_ALL_USERS = -1L;
	
	public static final Long SHOW_REQUESTS_OF_CURRENT_USER = -2L;

	public static final Long SHOW_REQUESTS_OF_SELECTED_USER = -3L;
	
	private Long submitterId = SHOW_REQUESTS_OF_ALL_USERS;
	
	private SortOption sortOption = SortOption.CREATE_DATE_DESCENDING;

	public DisplayOption(boolean open) {
		this.open = open;
	}
	
	public boolean isOpen() {
		return open;
	}

	public @Nullable Long getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(@Nullable Long submitterId) {
		this.submitterId = submitterId;
	}

	public SortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(SortOption sortOption) {
		this.sortOption = sortOption;
	}

	public DetachedCriteria getCriteria(Repository repository, boolean withOrderBy) {
		DetachedCriteria criteria = DetachedCriteria.forClass(PullRequest.class);
		criteria.createCriteria("target").add(Restrictions.eq("repository", repository));
		if (open) {
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		} else {
			criteria.add(PullRequest.CriterionHelper.ofClosed());
		}
		if (submitterId > 0L) {
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		} else if (submitterId == SHOW_REQUESTS_OF_CURRENT_USER) {
			criteria.add(Restrictions.eq("submitter.id", User.getCurrentId()));
		}
		if (withOrderBy)
			criteria.addOrder(sortOption.getOrder());
		return criteria;
	}
}
