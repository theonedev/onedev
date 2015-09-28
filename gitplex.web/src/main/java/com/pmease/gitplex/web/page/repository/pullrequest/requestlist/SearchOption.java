package com.pmease.gitplex.web.page.repository.pullrequest.requestlist;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.wicket.request.mapper.parameter.PageParameters;
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
	
	private static final String PARAM_STATUS = "status";
	
	private static final String PARAM_ASSIGNEE = "assignee";
	
	private static final String PARAM_SUBMITTER = "submitter";
	
	private static final String PARAM_TARGET = "target";
	
	private static final String PARAM_TITLE = "title";
	
	private static final String PARAM_BEGIN_DATE = "begin";
	
	private static final String PARAM_END_DATE = "end";
	
	public enum Status {OPEN, CLOSED, ALL};
	
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

	@Editable(order=700, name="Created After", description="Date should be specified with format <i>yyyy-MM-dd</i>.")
	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Editable(order=800, name="Created Before", description="Date should be specified with format <i>yyyy-MM-dd</i>.")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public SearchOption() {
	}
	
	public SearchOption(PageParameters params) {
		String value = params.get(PARAM_STATUS).toString();
		if (value != null)
			status = Status.valueOf(value);
		
		value = params.get(PARAM_ASSIGNEE).toString();
		if (value != null)
			assigneeId = Long.valueOf(value);
		
		value = params.get(PARAM_SUBMITTER).toString();
		if (value != null)
			submitterId = Long.valueOf(value);
		
		targetBranch = params.get(PARAM_TARGET).toString();
		title = params.get(PARAM_TITLE).toString();
		
		value = params.get(PARAM_BEGIN_DATE).toString();
		if (value != null)
			beginDate = new Date(Long.valueOf(value));
		
		value = params.get(PARAM_END_DATE).toString();
		if (value != null)
			endDate = new Date(Long.valueOf(value));
	}

	public EntityCriteria<PullRequest> getCriteria(Repository repository) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(Restrictions.eq("targetRepo", repository));
		
		if (status == Status.OPEN) 
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		else if (status == Status.CLOSED) 
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
	
	public void fillPageParams(PageParameters params) {
		if (status != Status.ALL)
			params.set(PARAM_STATUS, status.name());
		if (assigneeId != null)
			params.set(PARAM_ASSIGNEE, assigneeId);
		if (submitterId != null)
			params.set(PARAM_SUBMITTER, submitterId);
		if (targetBranch != null)
			params.set(PARAM_TARGET, targetBranch);
		if (title != null)
			params.set(PARAM_TITLE, title);
		if (beginDate != null)
			params.set(PARAM_BEGIN_DATE, beginDate.getTime());
		if (endDate != null)
			params.set(PARAM_END_DATE, endDate.getTime());
	}
	
}
