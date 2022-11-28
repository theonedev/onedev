package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;

@Api(order=2000, description="In most cases, issue resource is operated with issue id, which is different from issue number. "
		+ "To get issue id of a particular issue number, use the <a href='/~help/api/io.onedev.server.rest.IssueResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;projectName#100&quot;</code>")
@Path("/issues")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueResource {

	private final SettingManager settingManager;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	private final MilestoneManager milestoneManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public IssueResource(SettingManager settingManager, IssueManager issueManager, 
			IssueChangeManager issueChangeManager, MilestoneManager milestoneManager, 
			ProjectManager projectManager) {
		this.settingManager = settingManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.milestoneManager = milestoneManager;
		this.projectManager = projectManager;
	}

	@Api(order=100)
	@Path("/{issueId}")
    @GET
    public Issue getBasicInfo(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue;
    }

	@Api(order=200)
	@Path("/{issueId}/fields")
    @GET
    public Collection<IssueField> getFields(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getFields();
    }
	
	@Api(order=300)
	@Path("/{issueId}/changes")
    @GET
    public Collection<IssueChange> getChanges(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getChanges();
    }
	
	@Api(order=400)
	@Path("/{issueId}/comments")
    @GET
    public Collection<IssueComment> getComments(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getComments();
    }
	
	@Api(order=450)
	@Path("/{issueId}/milestones")
    @GET
    public Collection<Milestone> getMilestones(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getMilestones();
    }
	
	@Api(order=500)
	@Path("/{issueId}/votes")
    @GET
    public Collection<IssueVote> getVotes(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getVotes();
    }
	
	@Api(order=600)
	@Path("/{issueId}/watches")
    @GET
    public Collection<IssueWatch> getWatches(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getWatches();
    }
	
	@Api(order=700)
	@Path("/{issueId}/pulls")
    @GET
    public Collection<PullRequest> getPullRequests(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canReadCode(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getPullRequests();
    }
	
	@Api(order=800)
	@Path("/{issueId}/commits")
    @GET
    public List<FixCommit> getCommits(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canReadCode(issue.getProject())) 
			throw new UnauthorizedException();
    	
    	List<FixCommit> issueCommits = new ArrayList<>();
    	for (Issue.FixCommit commit: issue.getCommits()) {
    		FixCommit issueCommit = new FixCommit();
    		issueCommit.setProjectId(commit.getProject().getId());
    		issueCommit.setCommitHash(commit.getCommit().name());
    		issueCommits.add(issueCommit);
    	}
    	return issueCommits;
    }
	
	@Api(order=900)
	@GET
    public List<Issue> queryBasicInfo(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in <a href='/issues'>issues page</a>", example="\"Number\" is \"projectName#100\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
    	if (count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	IssueQuery parsedQuery;
		try {
			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
			parsedQuery = IssueQuery.parse(null, query, option, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return issueManager.query(null, parsedQuery, false, offset, count);
    }
	
	@Api(order=1000)
    @POST
    public Long create(@NotNull @Valid IssueOpenData data) {
    	User user = SecurityUtils.getUser();
    	
    	Project project = projectManager.load(data.getProjectId());
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();

    	if (!data.getMilestoneIds().isEmpty() && !SecurityUtils.canScheduleIssues(project))
			throw new UnauthorizedException();
    	
    	Issue issue = new Issue();
    	issue.setTitle(data.getTitle());
    	issue.setDescription(data.getDescription());
    	issue.setConfidential(data.isConfidential());
    	issue.setProject(project);
    	issue.setSubmitDate(new Date());
    	issue.setSubmitter(user);
		issue.setState(settingManager.getIssueSetting().getInitialStateSpec().getName());
		
    	for (Long milestoneId: data.getMilestoneIds()) {
    		Milestone milestone = milestoneManager.load(milestoneId);
    	    if (!milestone.getProject().isSelfOrAncestorOf(project))
    	    	throw new InvalidParamException("Milestone is not defined in project hierarchy of the issue");
    	    IssueSchedule schedule = new IssueSchedule();
    	    schedule.setIssue(issue);
    	    schedule.setMilestone(milestone);
    	    issue.getSchedules().add(schedule);
    	}
    	
    	for (Map.Entry<String, String> entry: data.fields.entrySet()) {
    		FieldSpec fieldSpec = settingManager.getIssueSetting().getFieldSpec(entry.getKey());
    		if (fieldSpec != null) {
        		IssueField field = new IssueField();
        		field.setIssue(issue);
        		field.setName(entry.getKey());
        		field.setValue(entry.getValue());
        		field.setType(fieldSpec.getType());
        		field.setOrdinal(issue.getFieldOrdinal(field.getName(), field.getValue()));
        		issue.getFields().add(field);
    		}
    	}
		issueManager.open(issue);
		return issue.getId();
    }
	
	@Api(order=1100)
	@Path("/{issueId}/title")
    @POST
    public Response setTitle(@PathParam("issueId") Long issueId, @NotEmpty String title) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueChangeManager.changeTitle(issue, title);
		return Response.ok().build();
    }
	
	@Api(order=1200)
	@Path("/{issueId}/description")
    @POST
    public Response setDescription(@PathParam("issueId") Long issueId, String description) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueManager.saveDescription(issue, description);
		return Response.ok().build();
    }
	
	@Api(order=1250)
	@Path("/{issueId}/confidential")
    @POST
    public Response setConfidential(@PathParam("issueId") Long issueId, boolean confidential) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueChangeManager.changeConfidential(issue, confidential);
		return Response.ok().build();
    }
	
	@Api(order=1300, description="Schedule issue into specified milestones with list of milestone id")
	@Path("/{issueId}/milestones")
    @POST
    public Response setMilestones(@PathParam("issueId") Long issueId, List<Long> milestoneIds) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canScheduleIssues(issue.getProject()))
			throw new UnauthorizedException();
    	Collection<Milestone> milestones = new HashSet<>();
    	for (Long milestoneId: milestoneIds) {
    		Milestone milestone = milestoneManager.load(milestoneId);
	    	if (!milestone.getProject().isSelfOrAncestorOf(issue.getProject()))
	    		throw new InvalidParamException("Milestone is not defined in project hierarchy of the issue");
	    	milestones.add(milestone);
    	}
    	
    	issueChangeManager.changeMilestones(issue, milestones);
    	
		return Response.ok().build();
    }
	
	@Api(order=1400)
	@Path("/{issueId}/fields")
    @POST
    public Response setFields(@PathParam("issueId") Long issueId, @NotNull Map<String, String> fields) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canManageIssues(issue.getProject()))
			throw new UnauthorizedException();
    	
    	Map<String, List<String>> fieldLists = new HashMap<>();
    	for (Map.Entry<String, String> entry: fields.entrySet()) {
    		List<String> values = fieldLists.get(entry.getKey());
    		if (values == null) {
    			values = new ArrayList<>();
    			fieldLists.put(entry.getKey(), values);
    		}
    		values.add(entry.getValue());
    	}
    	
    	Map<String, Object> fieldObjs = new HashMap<>();
    	for (Map.Entry<String, List<String>> entry: fieldLists.entrySet()) {
    		FieldSpec fieldSpec = settingManager.getIssueSetting().getFieldSpec(entry.getKey());
    		if (fieldSpec != null)
    			fieldObjs.put(entry.getKey(), fieldSpec.convertToObject(entry.getValue()));
    	}
    	
		issueChangeManager.changeFields(issue, fieldObjs);
		return Response.ok().build();
    }
	
	@Api(order=1500)
	@Path("/{issueId}/state-transitions")
    @POST
    public Response transitState(@PathParam("issueId") Long issueId, @NotNull @Valid StateTransitionData data) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canManageIssues(issue.getProject()))
			throw new UnauthorizedException();
    	
		issueChangeManager.changeState(issue, data.getState(), getFieldObjs(data.getFields()), 
				data.getRemoveFields(), data.getComment());
		return Response.ok().build();
    }
	
	private Map<String, Object> getFieldObjs(Map<String, String> fields) {
    	Map<String, List<String>> fieldLists = new HashMap<>();
    	for (Map.Entry<String, String> entry: fields.entrySet()) {
    		List<String> values = fieldLists.get(entry.getKey());
    		if (values == null) {
    			values = new ArrayList<>();
    			fieldLists.put(entry.getKey(), values);
    		}
    		values.add(entry.getValue());
    	}
    	
    	Map<String, Object> fieldObjs = new HashMap<>();
    	for (Map.Entry<String, List<String>> entry: fieldLists.entrySet()) {
    		FieldSpec fieldSpec = settingManager.getIssueSetting().getFieldSpec(entry.getKey());
    		if (fieldSpec != null)
    			fieldObjs.put(entry.getKey(), fieldSpec.convertToObject(entry.getValue()));
    	}
    	
    	return fieldObjs;
	}
	
	@Api(order=1600)
	@Path("/{issueId}")
    @DELETE
    public Response delete(@PathParam("issueId") Long issueId) {
    	Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canManageIssues(issue.getProject()))
			throw new UnauthorizedException();
    	issueManager.delete(issue);
    	return Response.ok().build();
    }
	
	@EntityCreate(Issue.class)
	public static class IssueOpenData implements Serializable {

		private static final long serialVersionUID = 1L;

		private Long projectId;
		
		private String title;
		
		private String description;
		
		private boolean confidential;
		
		private List<Long> milestoneIds = new ArrayList<>();
		
		private Map<String, String> fields = new HashMap<>();

		@NotNull
		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@NotNull
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isConfidential() {
			return confidential;
		}

		public void setConfidential(boolean confidential) {
			this.confidential = confidential;
		}

		public List<Long> getMilestoneIds() {
			return milestoneIds;
		}

		public void setMilestoneIds(List<Long> milestoneIds) {
			this.milestoneIds = milestoneIds;
		}

		@NotNull
		public Map<String, String> getFields() {
			return fields;
		}

		public void setFields(Map<String, String> fields) {
			this.fields = fields;
		}
		
	}
	
	public static class StateTransitionData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String state;
		
		private Map<String, String> fields = new HashMap<>();
		
		private Collection<String> removeFields = new HashSet<>();
		
		private String comment;

		@NotEmpty
		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		@NotNull
		public Map<String, String> getFields() {
			return fields;
		}

		public void setFields(Map<String, String> fields) {
			this.fields = fields;
		}

		@NotNull
		public Collection<String> getRemoveFields() {
			return removeFields;
		}

		public void setRemoveFields(Collection<String> removeFields) {
			this.removeFields = removeFields;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
		
	}
	
	public static class FixCommit implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private Long projectId;
		
		private String commitHash;

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		public String getCommitHash() {
			return commitHash;
		}

		public void setCommitHash(String commitHash) {
			this.commitHash = commitHash;
		}
		
	}
	
}
