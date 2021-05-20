package io.onedev.server.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityId;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;

@Api(order=2000)
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
    public Issue get(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue;
    }

	@Api(order=200)
	@Path("/{issueId}/fields")
    @GET
    public Collection<IssueField> getFields(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getFields();
    }
	
	@Api(order=300)
	@Path("/{issueId}/changes")
    @GET
    public Collection<IssueChange> getChanges(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getChanges();
    }
	
	@Api(order=400)
	@Path("/{issueId}/comments")
    @GET
    public Collection<IssueComment> getComments(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getComments();
    }
	
	@Api(order=500)
	@Path("/{issueId}/votes")
    @GET
    public Collection<IssueVote> getVotes(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getVotes();
    }
	
	@Api(order=600)
	@Path("/{issueId}/watches")
    @GET
    public Collection<IssueWatch> getWatches(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getWatches();
    }
	
	@Api(order=700)
	@Path("/{issueId}/pull-requests")
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
    public Collection<String> getCommits(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canReadCode(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getCommits().stream().map(it->it.name()).collect(Collectors.toList());
    }
	
	@Api(order=900)
	@GET
    public List<Issue> query(@QueryParam("query") String query, @QueryParam("offset") int offset, 
    		@QueryParam("count") int count) {
		
    	if (count > RestUtils.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should be less than " + RestUtils.MAX_PAGE_SIZE);

    	IssueQuery parsedQuery;
		try {
			parsedQuery = IssueQuery.parse(null, query, true, true, false, false, false);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
    	return issueManager.query(null, parsedQuery, offset, count, false);
    }
	
	@Api(order=1000)
    @POST
    public Long open(@NotNull @Valid IssueOpenData data) {
    	User user = SecurityUtils.getUser();
    	
    	Project project = projectManager.load(data.getProjectId());
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();

    	Milestone milestone;
    	if (data.getMilestoneId() != null) {
        	if (!SecurityUtils.canScheduleIssues(project))
    			throw new UnauthorizedException();
        	milestone = milestoneManager.load(data.getMilestoneId());
    	    if (!milestone.getProject().equals(project))
    	    	throw new InvalidParamException("Milestone is not defined in current project");
    	} else {
    		milestone = null;
    	}
    	
    	Issue issue = new Issue();
    	issue.setTitle(data.getTitle());
    	issue.setDescription(data.getDescription());
    	issue.setProject(project);
    	issue.setSubmitDate(new Date());
    	issue.setSubmitter(user);
		issue.setState(settingManager.getIssueSetting().getInitialStateSpec().getName());
		issue.setMilestone(milestone);
    	
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
		issueChangeManager.changeDescription(issue, description);
		return Response.ok().build();
    }
	
	@Api(order=1300)
	@Path("/{issueId}/milestone")
    @POST
    public Response setMilestone(@PathParam("issueId") Long issueId, Long milestoneId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canScheduleIssues(issue.getProject()))
			throw new UnauthorizedException();
    	Milestone milestone;
    	if (milestoneId != null) {
    		milestone = milestoneManager.load(milestoneId);
	    	if (!milestone.getProject().equals(issue.getProject()))
	    		throw new InvalidParamException("Milestone is not defined in current project");
    	} else {
    		milestone = null;
    	}
		issueChangeManager.changeMilestone(issue, milestone);
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
	
	public static class IssueOpenData implements Serializable {

		private static final long serialVersionUID = 1L;

		@EntityId(Project.class)
		private Long projectId;
		
		private String title;
		
		private String description;
		
		@EntityId(Project.class)
		private Long milestoneId;
		
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

		public Long getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(Long milestoneId) {
			this.milestoneId = milestoneId;
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
	
}
