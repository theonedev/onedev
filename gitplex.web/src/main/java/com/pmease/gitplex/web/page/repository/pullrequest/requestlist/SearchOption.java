package com.pmease.gitplex.web.page.repository.pullrequest.requestlist;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.editable.UserChoice;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
@Editable
public class SearchOption implements Serializable {
	
	public enum Status {OPEN, CLOSE, ALL};
	
	private Status status = Status.OPEN;

	private Long assigneeId;
	
	private Long submitterId;
	
	private String targetBranch;
	
	private String title;
	
	private String description;
	
	private Date beginDate;
	
	private Date endDate;
	
	@Editable(order=100)
	@NotNull
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Editable(order=200, name="Assigned To")
	@UserChoice
	public Long getAssigneeId() {
		return assigneeId;
	}

	public void setAssigneeId(Long assigneeId) {
		this.assigneeId = assigneeId;
	}

	@Editable(order=300, name="Submitted By")
	@UserChoice
	public Long getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(Long submitterId) {
		this.submitterId = submitterId;
	}

	@Editable(order=400)
	@BranchChoice
	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	@Editable(order=500, name="Title Containing")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Editable(order=600, name="Description Containing")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public EntityCriteria<PullRequest> getCriteria(Repository repository) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(Restrictions.eq("targetRepo", repository));
		
		if (status == Status.OPEN) 
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		else if (status == Status.CLOSE) 
			criteria.add(PullRequest.CriterionHelper.ofClosed());
		
		if (submitterId != null)
			criteria.add(Restrictions.eq("submitter.id", submitterId));
		if (assigneeId != null)
			criteria.add(Restrictions.eq("assignee.id", assigneeId));
		if (targetBranch != null)
			criteria.add(Restrictions.eq("targetBranch", targetBranch));
		if (title != null)
			criteria.add(Restrictions.ilike("title", "%" + title + "%"));
		if (description != null)
			criteria.add(Restrictions.ilike("description", "%" + description + "%"));
		if (beginDate != null)
			criteria.add(Restrictions.ge("createDate", beginDate));
		if (endDate != null)
			criteria.add(Restrictions.le("createDate", endDate));
		return criteria;
	}
}
