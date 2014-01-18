package com.pmease.gitop.web.page.project.pullrequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;

@SuppressWarnings("serial")
class DisplayOption implements Serializable {
	
	private Collection<PullRequest.Status> statuses = new HashSet<>();
	
	private Long branchId;
	
	private Long submitterId;
	
	private boolean incoming = true;
	
	private boolean outgoing = true;
	
	private String sortBy;

	public DisplayOption() {
		statuses.add(PullRequest.Status.PENDING_APPROVAL);
		statuses.add(PullRequest.Status.PENDING_CHECK);
		statuses.add(PullRequest.Status.PENDING_MERGE);
		statuses.add(PullRequest.Status.PENDING_UPDATE);
	}
	
	public Collection<PullRequest.Status> getStatuses() {
		return statuses;
	}

	public void setStatuses(Collection<PullRequest.Status> statuses) {
		this.statuses = statuses;
	}

	public Long getBranchId() {
		return branchId;
	}

	public void setBranchId(Long branchId) {
		this.branchId = branchId;
	}

	public Long getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(Long submitterId) {
		this.submitterId = submitterId;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public DetachedCriteria getCriteria(Project project) {
		DetachedCriteria criteria = DetachedCriteria.forClass(PullRequest.class);
		Disjunction projectDisjunction = Restrictions.disjunction();
		Disjunction branchDisjunction = Restrictions.disjunction();
		criteria.add(projectDisjunction);
		criteria.add(branchDisjunction);
		if (incoming || !incoming && !outgoing) {
			criteria.createAlias("target", "target");
			projectDisjunction.add(Restrictions.eq("target.project", project));
			branchDisjunction.add(Restrictions.eq("target.id", branchId));
		}
		if (outgoing || !incoming && !outgoing) {
			criteria.createAlias("source", "source");
			projectDisjunction.add(Restrictions.eq("source.project", project));
			branchDisjunction.add(Restrictions.eq("source.id", branchId));
		}
		if (!statuses.isEmpty()) {
			Disjunction disjunction = Restrictions.disjunction();
			for (PullRequest.Status status: statuses) 
				disjunction.add(Restrictions.eq("status", status));
			criteria.add(disjunction);
		}
		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		return criteria;
	}
}
