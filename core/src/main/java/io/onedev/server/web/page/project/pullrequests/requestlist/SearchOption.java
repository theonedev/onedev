package io.onedev.server.web.page.project.pullrequests.requestlist;

import static io.onedev.server.model.PullRequest.CriterionHelper.ofTarget;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofTargetProject;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.editable.annotation.BranchChoice;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.UserChoice;

@SuppressWarnings("serial")
@Editable
public class SearchOption implements Serializable {
	
	private static final String PARAM_STATUS = "status";
	
	private static final String PARAM_SUBMITTER = "submitter";
	
	private static final String PARAM_BRANCH = "branch";
	
	private static final String PARAM_TITLE = "title";
	
	private static final String PARAM_BEGIN_DATE = "begin";
	
	private static final String PARAM_END_DATE = "end";
	
	public enum Status {OPEN, CLOSED, ALL};
	
	private Status status = Status.OPEN;

	private String submitterName;
	
	private String targetBranch;
	
	private String title;
	
	private Date beginDate;
	
	private Date endDate;
	
	@Editable(order=100)
	@NotNull(message="may not be empty")
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Editable(order=300, name="Submitted By")
	@UserChoice
	public String getSubmitterName() {
		return submitterName;
	}

	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}

	@Editable(order=400, description="Target branch for incoming request, or source branch for outgoing request")
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

	@Editable(order=700, name="Submitted After", description="Date should be specified with format <i>yyyy-MM-dd</i>.")
	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Editable(order=800, name="Submitted Before", description="Date should be specified with format <i>yyyy-MM-dd</i>.")
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
		
		submitterName = params.get(PARAM_SUBMITTER).toString();
		
		targetBranch = params.get(PARAM_BRANCH).toString();
		title = params.get(PARAM_TITLE).toString();
		
		value = params.get(PARAM_BEGIN_DATE).toString();
		if (value != null)
			beginDate = new Date(Long.valueOf(value));
		
		value = params.get(PARAM_END_DATE).toString();
		if (value != null)
			endDate = new Date(Long.valueOf(value));
	}

	public EntityCriteria<PullRequest> getCriteria(Project project) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		if (targetBranch != null)
			criteria.add(ofTarget(new ProjectAndBranch(project, targetBranch)));
		else
			criteria.add(ofTargetProject(project));
		
		if (status == Status.OPEN) 
			criteria.add(PullRequest.CriterionHelper.ofOpen());
		else if (status == Status.CLOSED) 
			criteria.add(PullRequest.CriterionHelper.ofClosed());
		
		if (submitterName != null) {
			criteria.createCriteria("submitter").add(Restrictions.eq("name", submitterName));
		}
		if (title != null)
			criteria.add(Restrictions.ilike("title", "%" + title + "%"));
		if (beginDate != null)
			criteria.add(Restrictions.ge("createDate", beginDate));
		if (endDate != null)
			criteria.add(Restrictions.le("createDate", endDate));
		return criteria;
	}
	
	public void fillPageParams(PageParameters params) {
		params.set(PARAM_STATUS, status.name());
		if (submitterName != null)
			params.set(PARAM_SUBMITTER, submitterName);
		if (targetBranch != null)
			params.set(PARAM_BRANCH, targetBranch);
		if (title != null)
			params.set(PARAM_TITLE, title);
		if (beginDate != null)
			params.set(PARAM_BEGIN_DATE, beginDate.getTime());
		if (endDate != null)
			params.set(PARAM_END_DATE, endDate.getTime());
	}
	
}
